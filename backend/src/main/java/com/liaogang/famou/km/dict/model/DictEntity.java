package com.liaogang.famou.km.dict.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典实体 (U9 / T310)
 *
 * <p>6 字典: 类型介绍 / 效力分级 / 权威分级 / 类型分组 / 知识对象概念 / 量纲配置
 * 软删除 disabled 字段 (0=启用, 1=停用)
 */
@Data
@NoArgsConstructor
@TableName("km_dict")
public class DictEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("dict_type")
    private String dictType;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("disabled")
    private Integer disabled;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
