package com.liaogang.famou.km.ko.service;

import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.ko.model.KoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * KO 状态机（v0.32 PRD §5.2.1.3 + OQ-6 + OQ-12）。
 *
 * <p>5 状态转换守卫（CON/RUL/PAR/SCH 类型）：
 * <pre>
 *   Draft → Review → Approved → Published
 *                            ↓
 *                          Active
 * </pre>
 *
 * <p>类型豁免规则（§5.2.1.4）：
 * <ul>
 *   <li>DOC：上传后直接 Active，不经 Review / Approved / Published（特殊豁免）</li>
 *   <li>PRM：走标准 KO Version 流程（OQ-6，与其他类型一致）</li>
 *   <li>CON/RUL/PAR/SCH：标准 5 状态转换</li>
 * </ul>
 *
 * <p>Active 期间约束（OQ-12）：
 * <ul>
 *   <li>不允许创建新工作版本（Active KO 必须 Published 之后才能进入 Draft 状态）</li>
 *   <li>同一 KO 同时最多 1 个 in-flight 工作版本（Draft/Review/Approved）</li>
 * </ul>
 *
 * <p>T201 实现：纯 Java 工具类（不依赖 Spring State Machine），便于单测和复用
 */
@Slf4j
@Component
public class KoStateMachine {

    /** 5 个合法状态 */
    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_REVIEW = "Review";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_PUBLISHED = "Published";
    public static final String STATUS_ACTIVE = "Active";

    /** 6 个 KO 类型 */
    public static final String TYPE_CON = "CON";
    public static final String TYPE_RUL = "RUL";
    public static final String TYPE_PAR = "PAR";
    public static final String TYPE_SCH = "SCH";
    public static final String TYPE_PRM = "PRM";
    public static final String TYPE_DOC = "DOC";

    /** 状态转换映射（from → to set） */
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
        STATUS_DRAFT,     Set.of(STATUS_REVIEW),                     // Draft → Review（DOC 豁免独立处理 Draft → Active）
        STATUS_REVIEW,    Set.of(STATUS_APPROVED, STATUS_DRAFT),    // Review → Approved 或退回 Draft
        STATUS_APPROVED,  Set.of(STATUS_PUBLISHED, STATUS_DRAFT),  // Approved → Published 或退回 Draft
        STATUS_PUBLISHED, Set.of(STATUS_ACTIVE),                    // Published → Active
        STATUS_ACTIVE,    Set.of()                                   // Active 终止态（不退化）
    );

    /**
     * 检查状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @param type KO 类型（DOC 豁免）
     * @throws BusinessException 转换非法时抛出（带明确错误信息）
     */
    public void checkTransition(String from, String to, String type) {
        if (from == null || to == null) {
            throw new BusinessException(40001, "状态转换参数缺失: from=" + from + " to=" + to);
        }
        if (from.equals(to)) {
            return;  // 同状态不转换（幂等）
        }

        // DOC 特殊豁免：上传后直接 Active（跳过 Review/Approved/Published）
        if (TYPE_DOC.equals(type) && STATUS_DRAFT.equals(from) && STATUS_ACTIVE.equals(to)) {
            log.info("DOC 类型豁免：直接进入 Active 状态（§5.2.1.4）");
            return;
        }

        Set<String> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new BusinessException(40002,
                String.format("KO 状态转换非法: %s → %s（KO 类型=%s）；合法转换: %s",
                    from, to, type, allowed));
        }
        log.debug("KO 状态转换合法: {} → {}（类型={}）", from, to, type);
    }

    /**
     * 创建 KO 时的初始状态（按类型）
     *
     * @param type KO 类型
     * @return 初始状态（DOC 直接 Active，其他 Draft）
     */
    public String initialStatus(String type) {
        // DOC 豁免（§5.2.1.4）：上传后直接 Active
        if (TYPE_DOC.equals(type)) {
            return STATUS_ACTIVE;
        }
        // CON/RUL/PAR/SCH/PRM：Draft 起草
        return STATUS_DRAFT;
    }

    /**
     * OQ-12 检查：Active 状态时不能创建新工作版本
     *
     * @param currentStatus 当前状态
     * @throws BusinessException Active 状态时抛出
     */
    public void checkCanCreateNewVersion(String currentStatus) {
        if (STATUS_ACTIVE.equals(currentStatus)) {
            throw new BusinessException(40003,
                "Active 状态的 KO 不允许创建新工作版本（OQ-12 状态机约束）；请先归档或下架");
        }
    }

    /**
     * 检查同一 KO 同时最多 1 个 in-flight 工作版本
     *
     * @param existingInFlightCount 已存在的工作版本数（Draft/Review/Approved）
     * @throws BusinessException 大于 1 时抛出
     */
    public void checkSingleInFlightVersion(int existingInFlightCount) {
        if (existingInFlightCount > 0) {
            throw new BusinessException(40004,
                "同一 KO 同时最多 1 个 in-flight 工作版本（OQ-12 状态机约束）；请先完成或撤回现有版本");
        }
    }
}
