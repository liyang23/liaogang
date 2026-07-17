package com.liaogang.famou.km.project.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.project.model.ProjectEntity;
import com.liaogang.famou.km.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * U9 项目管理 REST API 控制器 (T309)
 *
 * <p>API：
 * <ul>
 *   <li>GET /api/project — 列出所有 active 项目 (ProjectSwitcher 默认列表)
 *   <li>GET /api/project?includeArchived=true — 包含 archived 项目
 *   <li>POST /api/project/{id}/archive — 归档项目 (OQ-7)
 *   <li>POST /api/project/{id}/activate — 激活项目 (archived -> active)
 * </ul>
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public Result<List<ProjectEntity>> list(
            @RequestParam(value = "includeArchived", defaultValue = "false") boolean includeArchived) {
        if (includeArchived) {
            // 返回 active + archived 合并
            List<ProjectEntity> result = projectService.listActive();
            result.addAll(projectService.listArchived());
            return Result.ok(result);
        }
        return Result.ok(projectService.listActive());
    }

    @PostMapping("/{id}/archive")
    public Result<ProjectEntity> archive(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Sub", required = false) String userSub) {
        return Result.ok(projectService.archiveProject(id, userSub != null ? userSub : "system"));
    }

    @PostMapping("/{id}/activate")
    public Result<ProjectEntity> activate(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Sub", required = false) String userSub) {
        return Result.ok(projectService.activateProject(id, userSub != null ? userSub : "system"));
    }
}
