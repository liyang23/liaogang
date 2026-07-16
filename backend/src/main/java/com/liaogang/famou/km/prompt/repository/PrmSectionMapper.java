package com.liaogang.famou.km.prompt.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * PRM Section Mapper（T208）。
 */
@Mapper
public interface PrmSectionMapper extends BaseMapper<PrmSectionEntity> {

    /**
     * 按 template_id 查询所有 Section（按 index 排序）
     */
    default List<PrmSectionEntity> selectByTemplateId(String templateId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PrmSectionEntity>()
                .eq("template_id", templateId)
                .orderByAsc("section_index")
        );
    }
}
