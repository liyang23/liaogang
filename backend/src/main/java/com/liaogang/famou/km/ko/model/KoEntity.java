package com.liaogang.famou.km.ko.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * KO 库主表实体（v0.32 PRD §5.2.1 + §10.7.7）。
 *
 * <p>ID 格式：KO-{TYPE}-{NNNN}（§10.7.7.1），按类型独立自增，**非数据库自增**（ID 字符串含类型前缀）
 * <p>6 类型：CON（约束）/ RUL（规则）/ PAR（参数）/ SCH（数据结构）/ PRM（提示词模板）/ DOC（文档）
 * <p>5 状态：Draft / Review / Approved / Published / Active（§5.2.1.3）
 * <p>DOC 类型豁免（v0.32 §5.2.1.4）：上传后直接 Active，不经 Review
 * <p>PRM 类型走标准 KO Version 流程（OQ-6）
 *
 * <p>F-26 修复：T201 实施时新建；Sprint 2 范围
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ko")
public class KoEntity {

    /** PK: KO-{TYPE}-{NNNN}（§10.7.7.1，INPUT 表示 ID 由业务生成） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** KO 类型: CON/RUL/PAR/SCH/PRM/DOC */
    private String type;

    /** KO 标题 */
    private String title;

    /** KO 编码（业务唯一） */
    private String code;

    /** 所属项目 ID（FK → project.id） */
    private String projectId;

    /** 形式化定义（JSON 或数学表达式） */
    private String definition;

    /** 约束效力: Hard/Soft/Normative */
    private String effect;

    /** KO 层级: L1/L2/L3 */
    private String level;

    /** 所属组织 */
    private String organization;

    /** 状态: Draft/Review/Approved/Published/Active */
    private String status;

    /** 当前版本: v{MAJOR}.{MINOR}.{PATCH}（§10.7.7.5） */
    private String version;

    /** 软删除标记（MyBatis-Plus 自动过滤 0/1） */
    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
