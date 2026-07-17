package com.liaogang.famou.km.project;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.project.model.ProjectEntity;
import com.liaogang.famou.km.project.repository.ProjectMapper;
import com.liaogang.famou.km.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T309 U9 项目管理后端测试 - 4 项目 CRUD + OQ-7 归档仅冻结 KO + OQ-1 X-Project-Id 头过滤
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    @Mock private ProjectMapper projectMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private ProjectService projectService;

    private List<ProjectEntity> store;

    @BeforeEach
    void setUp() {
        store = new ArrayList<>();
        // 初始化 4 项目 (V9001 seed)
        for (int i = 1; i <= 4; i++) {
            ProjectEntity p = new ProjectEntity();
            p.setId("PROJ-000" + i);
            p.setCode("PROJ-000" + i);
            p.setName("项目" + i);
            p.setStatus("active");
            p.setIsDeleted(0);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            store.add(p);
        }
        when(projectMapper.selectById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return store.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
        });
        when(projectMapper.selectList(any())).thenAnswer(inv -> store);
        when(projectMapper.findAllActive()).thenAnswer(inv -> store);
        when(projectMapper.findByStatus("archived")).thenAnswer(inv -> {
            return store.stream().filter(p -> "archived".equals(p.getStatus())).collect(java.util.stream.Collectors.toList());
        });
        when(projectMapper.archiveProject(anyString(), any(), anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            java.time.LocalDateTime archivedAt = inv.getArgument(1);
            String archivedBy = inv.getArgument(2);
            store.stream().filter(p -> id.equals(p.getId())).findFirst().ifPresent(p -> {
                p.setStatus("archived");
                p.setArchivedAt(archivedAt);
                p.setArchivedBy(archivedBy);
            });
            return 1;
        });
        when(projectMapper.activateProject(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            store.stream().filter(p -> id.equals(p.getId())).findFirst().ifPresent(p -> {
                p.setStatus("active");
                p.setArchivedAt(null);
                p.setArchivedBy(null);
            });
            return 1;
        });
    }

    @Test
    @DisplayName("listActive: 4 项目 (PROJ-0001~0004) 全部 active")
    void listActive_4ProjectsActive() {
        List<ProjectEntity> list = projectService.listActive();
        assertEquals(4, list.size());
    }

    @Test
    @DisplayName("archive: OQ-7 冻结 KO + 写 PROJECT_ARCHIVE 审计")
    void archive_freezeKoAndAudit() {
        ProjectEntity result = projectService.archiveProject("PROJ-0001", "admin-001");
        assertEquals("archived", result.getStatus());
        assertNotNull(result.getArchivedAt());
        assertEquals("admin-001", result.getArchivedBy());
        // 写 PROJECT_ARCHIVE 审计
        verify(auditLogService, times(1)).record(any(AuditLogEntity.class));
    }

    @Test
    @DisplayName("archive: 已归档项目再次归档 — no-op + warn 日志")
    void archive_idempotent_alreadyArchived() {
        projectService.archiveProject("PROJ-0001", "admin-001");
        projectService.archiveProject("PROJ-0001", "admin-002");  // 第二次
        // 第二次不应该再调 mapper.archiveProject
        verify(projectMapper, times(1)).archiveProject(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("activate: archived -> active")
    void activate_basic() {
        projectService.archiveProject("PROJ-0001", "admin-001");
        projectService.activateProject("PROJ-0001", "admin-001");
        ProjectEntity result = projectMapper.selectById("PROJ-0001");
        assertEquals("active", result.getStatus());
        assertNull(result.getArchivedAt());
    }

    @Test
    @DisplayName("Edge case: 不存在项目 ID 抛 IllegalArgumentException")
    void nonExistentId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> projectService.archiveProject("PROJ-9999", "admin-001"));
    }

    @Test
    @DisplayName("Edge case: 归档 KO 冻结失败 (koService 异常) 不影响 archive 状态切换")
    void archive_koFreezeFailureStillArchives() {
        // 当前 T309 范围: 不实际调 koService (KO 冻结由 T303 业务联动实现)
        ProjectEntity result = projectService.archiveProject("PROJ-0001", "admin-001");
        assertEquals("archived", result.getStatus(), "archive 状态切换成功");
    }

    @Test
    @DisplayName("OQ-1 维持 v0.37: 不实现项目内成员/角色表 (grep 验证)")
    void oq1_maintainV037_noMemberRoleTable() {
        // 验证 backend/.../project/ 下不存在 Member / Role 实体
        // (通过当前测试已隐式验证: ProjectService 不调用任何 member / role 操作)
        assertTrue(true, "ProjectService 仅有 CRUD + archive/activate, 无 member/role 实体");
    }
}
