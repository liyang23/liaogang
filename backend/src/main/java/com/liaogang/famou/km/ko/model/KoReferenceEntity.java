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
 * KO 引用关系实体（v0.32 PRD §5.2.3 + §5.2.4）。
 *
 * <p>3 类关系：
 * <ul>
 *   <li>DEPENDENCY：A KO 依赖 B KO（PRM 装配需要 RUL/PAR）</li>
 *   <li>REFERENCE：A KO 引用 B KO（PRM 模板引用其他 KO）</li>
 *   <li>CONFLICT：A KO 与 B KO 冲突（§5.2.3.1.1 冲突类型清单 C1-C6 / H1-H6）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ko_references")
public class KoReferenceEntity {

    /** PK: 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 源 KO ID（FK → ko.id） */
    private String sourceKoId;

    /** 目标 KO ID（FK → ko.id） */
    private String targetKoId;

    /** 关系类型: DEPENDENCY/REFERENCE/CONFLICT */
    private String relationType;

    /** 关系描述 */
    private String description;

    private LocalDateTime createdAt;
}
