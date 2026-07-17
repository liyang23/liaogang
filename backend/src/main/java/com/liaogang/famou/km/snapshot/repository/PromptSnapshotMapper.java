package com.liaogang.famou.km.snapshot.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.snapshot.model.PromptSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * SNP MyBatis-Plus Mapper (U8 / T305)
 */
@Mapper
public interface PromptSnapshotMapper extends BaseMapper<PromptSnapshotEntity> {

    @Select("SELECT * FROM km_prompt_snapshot WHERE hash = #{hash} LIMIT 1")
    PromptSnapshotEntity findByHash(@Param("hash") String hash);

    @Select("SELECT * FROM km_prompt_snapshot WHERE stale = #{stale} ORDER BY created_at DESC")
    List<PromptSnapshotEntity> findByStale(@Param("stale") Boolean stale);

    @Update("UPDATE km_prompt_snapshot SET stale = true, updated_at = NOW() WHERE prm_id = #{prmId} AND hash != #{excludeHash}")
    int markStaleByPrmExcludingHash(@Param("prmId") String prmId, @Param("excludeHash") String excludeHash);
}
