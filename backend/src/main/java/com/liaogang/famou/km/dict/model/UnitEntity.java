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
 * 量纲实体 (U9 / T310)
 *
 * <p>9 预制量纲: % / 元/TEU / h / 条 / 辆/岸桥 / 栏 / 人/班 / 台/班 / 箱/h
 * 部署门禁: 9 量纲缺失即抛异常退出 (DefaultDictLoader)
 */
@Data
@NoArgsConstructor
@TableName("km_unit")
public class UnitEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("symbol")
    private String symbol;

    @TableField("description")
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
