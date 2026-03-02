package com.training.controller;

import com.training.common.ApiResponse;
import com.training.model.entity.TrainTask;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserScopeService userScopeService;

    public TaskController(TaskService taskService, UserScopeService userScopeService) {
        this.taskService = taskService;
        this.userScopeService = userScopeService;
    }

    @GetMapping
    public ApiResponse<List<TrainTask>> list(@RequestParam(value = "publishId", required = false) Long publishId) {
        if (RoleGuard.isStudent()) {
            if (publishId != null) {
                userScopeService.requirePublishInCurrentStudentClass(publishId);
            }
            String className = userScopeService.currentStudentClassName();
            return ApiResponse.success(taskService.listByClassName(publishId, className));
        }
        return ApiResponse.success(taskService.list(publishId));
    }

    @PostMapping
    public ApiResponse<TrainTask> create(@RequestBody TrainTask task) {
        RoleGuard.requireTeacherOrAdmin();
        return ApiResponse.success(taskService.create(task));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainTask task) {
        RoleGuard.requireTeacherOrAdmin();
        task.setId(id);
        taskService.update(task);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        taskService.delete(id);
        return ApiResponse.success();
    }
}
