package com.training.controller;

import com.training.common.ApiResponse;
import com.training.model.entity.TrainProject;
import com.training.model.entity.TrainProjectPublish;
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
            List<TrainProjectPublish> list = projectService.listPublishes(studentClass);
            list = list.stream()
                    .filter(item -> userScopeService.matchesClassScope(studentClass, item.getClassName()))
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        }
        return ApiResponse.success(projectService.listPublishes(className));
    }

    @PostMapping("/publishes")
    public ApiResponse<TrainProjectPublish> createPublish(@RequestBody TrainProjectPublish publish) {
        RoleGuard.requireTeacherOrAdmin();
        return ApiResponse.success(projectService.createPublish(publish));
    }

    @PutMapping("/publishes/{id}")
    public ApiResponse<Void> updatePublish(@PathVariable("id") Long id, @RequestBody TrainProjectPublish publish) {
        RoleGuard.requireTeacherOrAdmin();
        publish.setId(id);
        projectService.updatePublish(publish);
        return ApiResponse.success();
    }

    @DeleteMapping("/publishes/{id}")
    public ApiResponse<Void> deletePublish(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        projectService.deletePublish(id);
        return ApiResponse.success();
    }
}
