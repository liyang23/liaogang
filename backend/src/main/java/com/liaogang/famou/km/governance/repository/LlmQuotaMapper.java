package com.liaogang.famou.km.governance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.governance.model.LlmQuotaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * LLM 配额实体 MyBatis-Plus Mapper (U7 / T301)
 */
@Mapper
public interface LlmQuotaMapper extends BaseMapper<LlmQuotaEntity> {

    @Select("SELECT * FROM km_llm_quota WHERE scope = #{scope} AND scope_key = #{scopeKey} AND quota_date = #{quotaDate} LIMIT 1")
    LlmQuotaEntity findByScopeAndDate(@Param("scope") String scope,
                                     @Param("scopeKey") String scopeKey,
                                     @Param("quotaDate") String quotaDate);
}
