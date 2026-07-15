package com.liaogang.famou.km.ko.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.model.KoVersionEntity;
import com.liaogang.famou.km.ko.repository.KoMapper;
import com.liaogang.famou.km.ko.repository.KoVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * KO 审核流服务（T204）。
 *
 * <p>5 状态转换（v0.32 PRD §5.2.1.3 + OQ-6 + OQ-12）：
 * <pre>
 *   Draft → Review → Approved → Published → Active
 *   (DOC 豁免：Draft → Active 直接转换)
 * </pre>
 *
 * <p>4 业务方法 + 1 状态查询：
 * <ul>
 *   <li>submitForReview：Draft → Review（业务专家提交）</li>
 *   <li>approve：Review → Approved（合规审核员通过；自己审自己禁止）</li>
 *   <li>reject：Review → Draft（驳回，附原因）</li>
 *   <li>publish：Approved → Published（系统管理员发布）→ 自动转 Active</li>
 * </ul>
 *
 * <p>依赖 {@link KoStateMachine} 做状态转换守卫（5 状态 + DOC 豁免 + OQ-12 约束）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KoAuditService {

    private final KoMapper koMapper;
    private final KoVersionMapper koVersionMapper;
    private final KoStateMachine stateMachine;

    /**
     * 提交审核（Draft → Review）
     * 业务专家 / 算法工程师 角色可调用
     */
    @Transactional
    public KoEntity submitForReview(String koId, String userSub) {
        KoEntity ko = getActiveKo(koId);
        // 状态机守卫：Draft → Review
        stateMachine.checkTransition(ko.getStatus(), KoStateMachine.STATUS_REVIEW, ko.getType());
        return updateStatus(ko, KoStateMachine.STATUS_REVIEW, "Submitted by " + userSub);
    }

    /**
     * 审核通过（Review → Approved）
     * 合规审核员角色可调用；不能审核自己提交的 KO Version（OQ-12）
     */
    @Transactional
    public KoEntity approve(String koId, String userSub, String currentUserSub) {
        KoEntity ko = getActiveKo(koId);

        // OQ-12：自己不能审自己（自审禁止）
        if (userSub != null && userSub.equals(currentUserSub)) {
            throw new BusinessException(40030,
                "禁止审核自己提交的 KO（OQ-12 自审禁止）");
        }

        // 状态机守卫：Review → Approved
        stateMachine.checkTransition(ko.getStatus(), KoStateMachine.STATUS_APPROVED, ko.getType());
        return updateStatus(ko, KoStateMachine.STATUS_APPROVED, "Approved by " + currentUserSub);
    }

    /**
     * 审核驳回（Review → Draft）
     * 合规审核员角色可调用
     */
    @Transactional
    public KoEntity reject(String koId, String reason, String currentUserSub) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(40031, "驳回原因不能为空");
        }
        KoEntity ko = getActiveKo(koId);
        stateMachine.checkTransition(ko.getStatus(), KoStateMachine.STATUS_DRAFT, ko.getType());
        return updateStatus(ko, KoStateMachine.STATUS_DRAFT,
            "Rejected by " + currentUserSub + ": " + reason);
    }

    /**
     * 发布（Approved → Published → 自动 Active）
     * 系统管理员角色可调用
     */
    @Transactional
    public KoEntity publish(String koId, String currentUserSub) {
        KoEntity ko = getActiveKo(koId);
        // 状态机守卫：Approved → Published
        stateMachine.checkTransition(ko.getStatus(), KoStateMachine.STATUS_PUBLISHED, ko.getType());

        // 记录版本历史
        KoVersionEntity version = KoVersionEntity.builder()
            .koId(koId)
            .version(ko.getVersion())
            .title(ko.getTitle())
            .definition(ko.getDefinition())
            .status(KoStateMachine.STATUS_PUBLISHED)
            .createdBy(currentUserSub)
            .build();
        koVersionMapper.insert(version);

        // 状态 Draft → Published → Active
        ko.setStatus(KoStateMachine.STATUS_PUBLISHED);
        ko.setUpdatedAt(LocalDateTime.now());
        koMapper.updateById(ko);

        // 立即转 Active（§5.2.1.3 终态）
        stateMachine.checkTransition(KoStateMachine.STATUS_PUBLISHED,
            KoStateMachine.STATUS_ACTIVE, ko.getType());
        ko.setStatus(KoStateMachine.STATUS_ACTIVE);
        ko.setUpdatedAt(LocalDateTime.now());
        koMapper.updateById(ko);

        log.info("KO 发布完成: id={}, version={}", koId, ko.getVersion());
        return ko;
    }

    /**
     * 查询 KO 状态（OQ-12 状态机约束检查用）
     */
    public String getStatus(String koId) {
        KoEntity ko = getActiveKo(koId);
        return ko.getStatus();
    }

    /**
     * 查询 KO 当前 in-flight 工作版本数（OQ-12 检查用）
     */
    public int countInFlightVersions(String koId) {
        Long count = koVersionMapper.selectCount(
            new QueryWrapper<KoVersionEntity>()
                .eq("ko_id", koId)
                .in("status", KoStateMachine.STATUS_DRAFT,
                    KoStateMachine.STATUS_REVIEW,
                    KoStateMachine.STATUS_APPROVED)
        );
        return count.intValue();
    }

    // ===== 内部辅助 =====

    private KoEntity getActiveKo(String koId) {
        KoEntity ko = koMapper.selectById(koId);
        if (ko == null || Integer.valueOf(1).equals(ko.getIsDeleted())) {
            throw new BusinessException(40401, "KO 不存在: " + koId);
        }
        return ko;
    }

    private KoEntity updateStatus(KoEntity ko, String newStatus, String reason) {
        ko.setStatus(newStatus);
        ko.setUpdatedAt(LocalDateTime.now());
        koMapper.updateById(ko);
        log.info("KO 状态变更: id={}, new_status={}, reason={}", ko.getId(), newStatus, reason);
        return ko;
    }
}