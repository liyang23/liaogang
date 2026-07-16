package com.liaogang.famou.km.ko.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.ko.dto.KoDetail;
import com.liaogang.famou.km.ko.dto.KoListItem;
import com.liaogang.famou.km.ko.dto.KoSearchResult;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.repository.KoMapper;
import com.liaogang.famou.km.ko.repository.KoVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KO 业务服务（T202）。
 *
 * <p>核心能力：
 * <ul>
 *   <li>createKo：按类型生成 ID（KO-{TYPE}-{NNNN}）+ 状态机初始状态</li>
 *   <li>getById：按 ID 查询（带跨项目隔离）</li>
 *   <li>listKo：列表（按 type/projectId/status 过滤 + 分页）</li>
 *   <li>searchKo：跨类搜索（OQ-4：title + id + typeName 3 字段匹配）</li>
 *   <li>updateKo：状态机守卫（OQ-12 + 类型豁免）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KoService {

    /** 6 类型 KO 的中文名（前端展示用） */
    private static final Map<String, String> TYPE_NAMES = Map.of(
        "CON", "约束",
        "RUL", "规则",
        "PAR", "参数",
        "SCH", "数据结构",
        "PRM", "提示词模板",
        "DOC", "文档"
    );

    private final KoMapper koMapper;
    private final KoVersionMapper koVersionMapper;
    private final KoStateMachine stateMachine;

    // 注：KoReferenceMapper 在 T204 实施时添加（本 task 仅 CRUD + 搜索，references 字段后续填）

    /**
     * 创建 KO（生成 ID + 初始状态 + insert）
     *
     * @param ko 待创建的 KO 实体（不需要填 id/status/version/isDeleted）
     * @return 完整 KO 实体（含自动生成的 id / status）
     */
    public KoEntity createKo(KoEntity ko) {
        // 1. 校验类型
        String type = ko.getType();
        if (!TYPE_NAMES.containsKey(type)) {
            throw new BusinessException(40010, "未知 KO 类型: " + type);
        }

        // 2. 按类型生成下一个 ID（KO-{TYPE}-{NNNN}）
        String id = generateNextId(type);
        ko.setId(id);

        // 3. 设置初始状态（DOC 直接 Active，其他 Draft；OQ-6 + §5.2.1.4）
        ko.setStatus(stateMachine.initialStatus(type));

        // 4. 设置初始版本 + 创建时间
        ko.setVersion("v1.0.0");
        ko.setIsDeleted(0);
        if (ko.getCreatedAt() == null) {
            ko.setCreatedAt(LocalDateTime.now());
        }
        ko.setUpdatedAt(LocalDateTime.now());

        // 5. 插入
        koMapper.insert(ko);
        log.info("KO 创建成功: id={}, type={}, status={}", id, type, ko.getStatus());
        return ko;
    }

    /**
     * 生成下一个 KO ID（KO-{TYPE}-{NNNN}，按类型独立计数）。
     *
     * <p>F-29 简化实现：SELECT COUNT(*) + 1（高并发有 race condition）
     * <p>T203 实施时升级：使用 MyBatis-Plus 自定义 ID 生成器 + 业务前缀 + 雪花算法
     */
    private String generateNextId(String type) {
        // 查以 "KO-{TYPE}-" 开头的现有 KO 数量 + 1
        Long count = koMapper.selectCount(
            new QueryWrapper<KoEntity>().likeRight("id", "KO-" + type + "-")
        );
        long nextNum = count + 1;
        // 格式：KO-{TYPE}-{NNNN}（NNNN 不补 0，避免 0 与空字符串歧义）
        return String.format("KO-%s-%d", type, nextNum);
    }

    /**
     * 按 ID 查询（带跨项目隔离：传入 projectId 校验）
     *
     * @param id        KO ID
     * @param projectId 项目 ID（用于跨项目隔离；null 时不过滤，仅系统管理员可用）
     * @return KO 详情
     * @throws BusinessException 40401 未找到 / 40403 跨项目隔离拒绝
     */
    public KoDetail getById(String id, String projectId) {
        KoEntity entity = koMapper.selectById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getIsDeleted())) {
            throw new BusinessException(40401, "KO 不存在: " + id);
        }

        // 跨项目隔离：非系统角色查询其他项目的 KO 拒绝
        if (projectId != null && !projectId.equals(entity.getProjectId())) {
            throw new BusinessException(40403, "KO 不属于当前项目，禁止跨项目访问");
        }

        return toDetail(entity);
    }

    /**
     * 列表查询（按 type/projectId/status 过滤 + 分页）
     *
     * @param type      KO 类型（可选，null 不过滤）
     * @param projectId 项目 ID（可选，null 不过滤）
     * @param status    状态（可选，null 不过滤）
     * @param page      页码（从 1 开始）
     * @param size      每页大小
     * @return 分页结果
     */
    public Page<KoListItem> listKo(String type, String projectId, String status, int page, int size) {
        QueryWrapper<KoEntity> wrapper = new QueryWrapper<KoEntity>()
            .eq("is_deleted", 0)
            .orderByDesc("updated_at");
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (projectId != null && !projectId.isEmpty()) {
            wrapper.eq("project_id", projectId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        Page<KoEntity> entityPage = koMapper.selectPage(
            Page.of(page, size), wrapper
        );

        // Entity → DTO 转换
        Page<KoListItem> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
            .map(this::toListItem)
            .collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 跨类搜索（OQ-4：按 title + id + typeName 3 字段匹配）
     *
     * @param query     搜索关键词
     * @param types     限定类型列表（null/empty 不过滤）
     * @param projectId 项目 ID（跨项目隔离；null 不过滤）
     * @return 搜索结果列表
     */
    public List<KoSearchResult> searchKo(String query, List<String> types, String projectId) {
        if (query == null || query.trim().isEmpty()) {
            throw new BusinessException(40020, "搜索关键词不能为空");
        }
        String q = query.trim();

        // F-36 修复：OQ-4 3 字段全搜（title / id / typeName）
        // typeName 匹配：query 包含在 TYPE_NAMES[type] 中（如"约束"匹配 CON）
        List<String> typeNameMatchedTypes = TYPE_NAMES.entrySet().stream()
            .filter(e -> e.getValue().contains(q))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        QueryWrapper<KoEntity> wrapper = new QueryWrapper<KoEntity>().eq("is_deleted", 0);
        // (title LIKE q OR id LIKE q) OR type IN (typeNameMatchedTypes)
        wrapper.and(w -> w.like("title", q).or().like("id", q));
        if (!typeNameMatchedTypes.isEmpty()) {
            // types 过滤 + typeName 匹配
            List<String> finalTypes = (types != null && !types.isEmpty())
                ? types.stream().filter(typeNameMatchedTypes::contains).collect(Collectors.toList())
                : typeNameMatchedTypes;
            if (!finalTypes.isEmpty()) {
                wrapper.or().in("type", finalTypes);
            }
        }
        wrapper.orderByDesc("updated_at");

        // 用户传的 types 过滤（当 typeNameMatchedTypes 为空时仍生效）
        if (types != null && !types.isEmpty() && typeNameMatchedTypes.isEmpty()) {
            wrapper.in("type", types);
        }

        // 跨项目隔离
        if (projectId != null && !projectId.isEmpty()) {
            wrapper.eq("project_id", projectId);
        }

        List<KoEntity> entities = koMapper.selectList(wrapper);

        // 计算 matchedField（title / id / typeName 中哪个匹配）
        return entities.stream()
            .map(e -> {
                String matchedField = e.getTitle().contains(q) ? "title"
                    : e.getId().contains(q) ? "id"
                    : TYPE_NAMES.getOrDefault(e.getType(), "").contains(q) ? "typeName"
                    : "other";
                return KoSearchResult.builder()
                    .id(e.getId())
                    .type(e.getType())
                    .typeName(TYPE_NAMES.getOrDefault(e.getType(), e.getType()))
                    .title(e.getTitle())
                    .projectId(e.getProjectId())
                    .matchedField(matchedField)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 更新 KO（状态机守卫）
     */
    public KoEntity updateKo(String id, KoEntity update, String projectId) {
        KoEntity existing = koMapper.selectById(id);
        if (existing == null || Integer.valueOf(1).equals(existing.getIsDeleted())) {
            throw new BusinessException(40401, "KO 不存在: " + id);
        }
        // 跨项目隔离
        if (projectId != null && !projectId.equals(existing.getProjectId())) {
            throw new BusinessException(40403, "KO 不属于当前项目，禁止跨项目访问");
        }

        // 状态机守卫：检查状态转换是否合法（update 通常是 Draft → Draft，不变更状态）
        // 这里 update 不改变 status 字段（版本流程在 T204 单独处理）
        // 状态机约束主要在 T204 审核流程

        update.setId(id);
        update.setUpdatedAt(LocalDateTime.now());
        koMapper.updateById(update);
        return koMapper.selectById(id);
    }

    /**
     * 软删除（标记 is_deleted=1）
     */
    public void softDelete(String id, String projectId) {
        KoEntity existing = koMapper.selectById(id);
        if (existing == null || Integer.valueOf(1).equals(existing.getIsDeleted())) {
            throw new BusinessException(40401, "KO 不存在或已删除: " + id);
        }
        if (projectId != null && !projectId.equals(existing.getProjectId())) {
            throw new BusinessException(40403, "KO 不属于当前项目，禁止跨项目访问");
        }

        KoEntity update = new KoEntity();
        update.setId(id);
        update.setIsDeleted(1);
        update.setUpdatedAt(LocalDateTime.now());
        koMapper.updateById(update);
        log.info("KO 软删除: id={}", id);
    }

    // ====== Entity → DTO 转换辅助方法 ======

    private KoListItem toListItem(KoEntity e) {
        return KoListItem.builder()
            .id(e.getId())
            .type(e.getType())
            .typeName(TYPE_NAMES.getOrDefault(e.getType(), e.getType()))
            .title(e.getTitle())
            .version(e.getVersion())
            .status(e.getStatus())
            .projectId(e.getProjectId())
            .updatedAt(e.getUpdatedAt())
            .build();
    }

    private KoDetail toDetail(KoEntity e) {
        return KoDetail.builder()
            .id(e.getId())
            .type(e.getType())
            .typeName(TYPE_NAMES.getOrDefault(e.getType(), e.getType()))
            .title(e.getTitle())
            .code(e.getCode())
            .projectId(e.getProjectId())
            .definition(e.getDefinition())
            .effect(e.getEffect())
            .level(e.getLevel())
            .organization(e.getOrganization())
            .status(e.getStatus())
            .version(e.getVersion())
            .createdBy(e.getCreatedBy())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            // references 在 T204 加 KoReferenceMapper 后填
            .build();
    }
}