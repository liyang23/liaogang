package com.liaogang.famou.km.governance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.governance.model.ConflictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 冲突实体 MyBatis-Plus Mapper (U7 / T301)
 */
@Mapper
public interface ConflictMapper extends BaseMapper<ConflictEntity> {

    @Select("SELECT * FROM km_conflict WHERE fingerprint = #{fingerprint} LIMIT 1")
    ConflictEntity findByFingerprint(@Param("fingerprint") String fingerprint);

    @Select("SELECT * FROM km_conflict WHERE status = #{status} ORDER BY created_at DESC")
    List<ConflictEntity> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM km_conflict WHERE ko_a_id = #{koId} OR ko_b_id = #{koId} ORDER BY created_at DESC")
    List<ConflictEntity> findByKoId(@Param("koId") String koId);
}
