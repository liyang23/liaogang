package com.liaogang.famou.km.ko;

import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.ko.service.KoStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.liaogang.famou.km.ko.service.KoStateMachine.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * KoStateMachine 状态机单元测试（T201 done_signal: 5/5 测试通过）。
 *
 * <p>覆盖：
 * <ul>
 *   <li>标准 5 状态转换（CON/RUL/PAR/SCH/PRM）</li>
 *   <li>DOC 类型豁免（直接 Active）</li>
 *   <li>非法转换拦截（BusinessException）</li>
 *   <li>Active 终止态约束（OQ-12）</li>
 *   <li>单 in-flight 工作版本约束（OQ-12）</li>
 * </ul>
 */
@DisplayName("KoStateMachine 5 状态 + 类型豁免单测")
class KoStateMachineTest {

    private final KoStateMachine sm = new KoStateMachine();

    @Test
    @DisplayName("标准转换路径：CON Draft → Review → Approved → Published → Active")
    void standardTransitionPath() {
        // 5 步合法转换，每步不应抛异常
        assertThatCode(() -> sm.checkTransition(STATUS_DRAFT, STATUS_REVIEW, TYPE_CON))
            .doesNotThrowAnyException();
        assertThatCode(() -> sm.checkTransition(STATUS_REVIEW, STATUS_APPROVED, TYPE_CON))
            .doesNotThrowAnyException();
        assertThatCode(() -> sm.checkTransition(STATUS_APPROVED, STATUS_PUBLISHED, TYPE_CON))
            .doesNotThrowAnyException();
        assertThatCode(() -> sm.checkTransition(STATUS_PUBLISHED, STATUS_ACTIVE, TYPE_CON))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DOC 类型豁免：Draft → Active 直接转换（不经 Review/Approved/Published）")
    void docExemptionDirectToActive() {
        // DOC 特殊豁免（§5.2.1.4）
        assertThatCode(() -> sm.checkTransition(STATUS_DRAFT, STATUS_ACTIVE, TYPE_DOC))
            .doesNotThrowAnyException();

        // initialStatus 应直接返回 Active
        assertThat(sm.initialStatus(TYPE_DOC)).isEqualTo(STATUS_ACTIVE);
    }

    @Test
    @DisplayName("非 DOC 类型不能跳过 Review/Approved/Published")
    void nonDocCannotSkipReviewApproved() {
        // CON 试图 Draft → Active 跳过中间状态（应该失败）
        assertThatThrownBy(() -> sm.checkTransition(STATUS_DRAFT, STATUS_ACTIVE, TYPE_CON))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("状态转换非法")
            .hasMessageContaining("Draft → Active");
    }

    @Test
    @DisplayName("Active 终止态：不可继续转换到任何状态")
    void activeIsTerminalState() {
        // Active → Draft / Review / Approved / Published 都应该失败
        for (String target : new String[]{STATUS_DRAFT, STATUS_REVIEW, STATUS_APPROVED, STATUS_PUBLISHED}) {
            assertThatThrownBy(() -> sm.checkTransition(STATUS_ACTIVE, target, TYPE_CON))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态转换非法");
        }
    }

    @Test
    @DisplayName("PRM 类型走标准流程（OQ-6）：与 CON/RUL/PAR/SCH 一致，不豁免")
    void prmFollowsStandardFlow() {
        // PRM 必须走标准 5 状态转换（不能像 DOC 那样直接 Active）
        assertThatThrownBy(() -> sm.checkTransition(STATUS_DRAFT, STATUS_ACTIVE, TYPE_PRM))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("状态转换非法");
    }

    @Test
    @DisplayName("Active 状态 KO 不允许创建新工作版本（OQ-12 状态机约束）")
    void activeCannotCreateNewVersion() {
        assertThatThrownBy(() -> sm.checkCanCreateNewVersion(STATUS_ACTIVE))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Active 状态的 KO 不允许创建新工作版本");
    }

    @Test
    @DisplayName("非 Active 状态允许创建新工作版本（OQ-12）")
    void nonActiveCanCreateNewVersion() {
        assertThatCode(() -> sm.checkCanCreateNewVersion(STATUS_DRAFT)).doesNotThrowAnyException();
        assertThatCode(() -> sm.checkCanCreateNewVersion(STATUS_REVIEW)).doesNotThrowAnyException();
        assertThatCode(() -> sm.checkCanCreateNewVersion(STATUS_PUBLISHED)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("单 in-flight 工作版本约束（OQ-12）：已存在 1 个工作版本时不能再创建")
    void singleInFlightVersionConstraint() {
        assertThatThrownBy(() -> sm.checkSingleInFlightVersion(1))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("同时最多 1 个 in-flight 工作版本");
    }

    @Test
    @DisplayName("单 in-flight 工作版本约束：未存在工作版本时允许")
    void singleInFlightVersionAllowed() {
        assertThatCode(() -> sm.checkSingleInFlightVersion(0)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("initialStatus 5 类型返回值")
    void initialStatusAllTypes() {
        // DOC → Active（豁免）
        assertThat(sm.initialStatus(TYPE_DOC)).isEqualTo(STATUS_ACTIVE);
        // 其他 5 类型 → Draft
        for (String type : new String[]{TYPE_CON, TYPE_RUL, TYPE_PAR, TYPE_SCH, TYPE_PRM}) {
            assertThat(sm.initialStatus(type))
                .as("类型 %s 应返回 Draft", type)
                .isEqualTo(STATUS_DRAFT);
        }
    }

    @Test
    @DisplayName("同状态幂等：from == to 不抛异常")
    void sameStateIdempotent() {
        // 同状态不转换
        for (String state : new String[]{STATUS_DRAFT, STATUS_REVIEW, STATUS_APPROVED, STATUS_PUBLISHED, STATUS_ACTIVE}) {
            assertThatCode(() -> sm.checkTransition(state, state, TYPE_CON))
                .as("同状态 %s 应幂等", state)
                .doesNotThrowAnyException();
        }
    }
}
