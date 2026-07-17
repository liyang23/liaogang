package com.liaogang.famou.km.audit.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.audit.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 MyBatis-Plus Mapper (U9 / T308)
 *
 * <p>V9007 按月分区表 PK 复合 (id, created_at) — 配合 StaleSnapshotJob 类似定时归档。
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {

    @Select("SELECT * FROM km_audit_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLogEntity> findByUserId(@Param("userId") String userId, @Param("limit") int limit);

    @Select("SELECT * FROM km_audit_log WHERE target_ko = #{targetKo} ORDER BY created_at DESC")
    List<AuditLogEntity> findByTargetKo(@Param("targetKo") String targetKo);

    @Select("SELECT * FROM km_audit_log WHERE action = #{action} AND created_at BETWEEN #{from} AND #{to} ORDER BY created_at DESC")
    List<AuditLogEntity> findByActionAndDateRange(
            @Param("action") String action,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
