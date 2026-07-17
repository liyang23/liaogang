package com.liaogang.famou.km.project.service;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.audit.enums.AuditAction;
import com.liaogang.famou.km.project.model.ProjectEntity;
import com.liaogang.famou.km.project.repository.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目服务 (U9 / T309 / OQ-1 + OQ-7)
 *
 * <p>4 项目 CRUD + status active/archived 切换 + OQ-7 归档仅冻结 KO 不影响 PRP/SNP。
 * OQ-1 维持 v0.37: 无成员/角色表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final AuditLogService auditLogService;

    /** 列出所有 active 项目 */
    public List<ProjectEntity> listActive() {
        return projectMapper.findAllActive();
    }

    /** 列出 archived 项目 (供 ProjectSwitcher 归档折叠) */
    public List<ProjectEntity> listArchived() {
        return projectMapper.findByStatus("archived");
    }

    /**
     * 归档项目 (OQ-7: 冻结 KO 不影响 PRP/SNP)
     * 事务：标记 status=archived + 写 PROJECT_ARCHIVE 审计
     * KO 冻结联动 (freezeProjectKos) 已在 KoService 占位 stub (T309 PR 范围外)
     */
    @Transactional
    public ProjectEntity archiveProject(String projectId, String userSub) {
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("project not found: " + projectId);
        }
        if ("archived".equals(project.getStatus())) {
            log.warn("project {} already archived, skip", projectId);
            return project;
        }

        int updated = projectMapper.archiveProject(projectId, LocalDateTime.now(), userSub);
        if (updated == 0) {
            throw new IllegalStateException("archive project failed: " + projectId);
        }

        // OQ-7: 冻结 KO 变更 - 占位 (KoService.freezeProjectKos 由 U4 Sprint 1 落地)
        // 当前 T309 范围: 仅标记 status=archived, 实际 KO 变更冻结由 KoService 在 T303 业务联动实现

        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setAction(AuditAction.PROJECT_ARCHIVE.name());
        auditLog.setUserId(userSub);
        auditLog.setTargetKo(projectId);
        auditLog.setDetail("name=" + project.getName());
        auditLogService.record(auditLog);

        project.setStatus("archived");
        project.setArchivedAt(LocalDateTime.now());
        project.setArchivedBy(userSub);
        return project;
    }

    /**
     * 激活项目 (archived -> active)
     */
    @Transactional
    public ProjectEntity activateProject(String projectId, String userSub) {
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("project not found: " + projectId);
        }
        if ("active".equals(project.getStatus())) {
            return project;
        }
        projectMapper.activateProject(projectId);
        return project;
    }

    /**
     * OQ-1 维持 v0.37: 无成员/角色表. 切换器只通过 X-Project-Id 头过滤 KO.
     */
}
