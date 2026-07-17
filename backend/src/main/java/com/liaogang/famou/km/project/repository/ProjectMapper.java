package com.liaogang.famou.km.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liaogang.famou.km.project.model.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 项目 MyBatis-Plus Mapper (U9 / T309)
 */
@Mapper
public interface ProjectMapper extends BaseMapper<ProjectEntity> {

    @Select("SELECT * FROM project WHERE status = #{status} ORDER BY created_at ASC")
    List<ProjectEntity> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM project WHERE is_deleted = 0 ORDER BY created_at ASC")
    List<ProjectEntity> findAllActive();

    @Update("UPDATE project SET status = 'archived', archived_at = #{archivedAt}, archived_by = #{archivedBy}, updated_at = NOW() WHERE id = #{id}")
    int archiveProject(@Param("id") String id, @Param("archivedAt") LocalDateTime archivedAt, @Param("archivedBy") String archivedBy);

    @Update("UPDATE project SET status = 'active', archived_at = NULL, archived_by = NULL, updated_at = NOW() WHERE id = #{id}")
    int activateProject(@Param("id") String id);
}
