package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.model.entity.TrainProject;
import com.training.model.entity.TrainProjectPublish;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserScopeService userScopeService;

    public ProjectController(ProjectService projectService, UserScopeService userScopeService) {
        this.projectService = projectService;
        this.userScopeService = userScopeService;
    }

    @GetMapping
    public ApiResponse<List<TrainProject>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        if (RoleGuard.isTeacher()) {
            return ApiResponse.success(projectService.listProjectsByTeacher(AuthContext.getUserId(), keyword));
        }
        return ApiResponse.success(projectService.listProjects(keyword));
    }

    @PostMapping
    public ApiResponse<TrainProject> create(@RequestBody TrainProject project) {
        RoleGuard.requireTeacherOrAdmin();
        return ApiResponse.success(projectService.createProject(project));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainProject project) {
        RoleGuard.requireTeacherOrAdmin();
        project.setId(id);
        projectService.updateProject(project);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        projectService.deleteProject(id);
        return ApiResponse.success();
    }

    @GetMapping("/publishes")
    public ApiResponse<List<TrainProjectPublish>> listPublishes(
            @RequestParam(value = "className", required = false) String className) {
        if (RoleGuard.isStudent()) {
            final String studentClass = userScopeService.currentStudentClassName();
            List<TrainProjectPublish> list = projectService.listPublishes(studentClass, null);
            list = list.stream()
                    .filter(item -> userScopeService.matchesClassScope(studentClass, item.getClassName()))
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        }
        if (RoleGuard.isTeacher()) {
            return ApiResponse.success(projectService.listPublishes(className, AuthContext.getUserId()));
        }
        return ApiResponse.success(projectService.listPublishes(className, null));
    }

    @PostMapping("/publishes")
    public ApiResponse<TrainProjectPublish> createPublish(@RequestBody TrainProjectPublish publish) {
        RoleGuard.requireTeacherOrAdmin();
        if (RoleGuard.isTeacher()) {
            publish.setTeacherId(AuthContext.getUserId());
        } else if (publish.getTeacherId() == null) {
            throw new BizException("请为开设计划选择指导老师");
        }
        return ApiResponse.success(projectService.createPublish(publish));
    }

    @PutMapping("/publishes/{id}")
    public ApiResponse<Void> updatePublish(@PathVariable("id") Long id, @RequestBody TrainProjectPublish publish) {
        RoleGuard.requireTeacherOrAdmin();
        userScopeService.requireManagePublish(id);
        publish.setId(id);
        if (RoleGuard.isTeacher()) {
            publish.setTeacherId(AuthContext.getUserId());
        } else if (publish.getTeacherId() == null) {
            throw new BizException("请为开设计划选择指导老师");
        }
        projectService.updatePublish(publish);
        return ApiResponse.success();
    }

    @DeleteMapping("/publishes/{id}")
    public ApiResponse<Void> deletePublish(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        userScopeService.requireManagePublish(id);
        projectService.deletePublish(id);
        return ApiResponse.success();
    }
}
