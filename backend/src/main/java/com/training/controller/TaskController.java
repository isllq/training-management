package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.TrainTaskMapper;
import com.training.model.entity.TrainTask;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserScopeService userScopeService;
    private final TrainTaskMapper taskMapper;

    public TaskController(TaskService taskService, UserScopeService userScopeService, TrainTaskMapper taskMapper) {
        this.taskService = taskService;
        this.userScopeService = userScopeService;
        this.taskMapper = taskMapper;
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
        if (RoleGuard.isTeacher()) {
            if (publishId != null) {
                userScopeService.requireManagePublish(publishId);
            }
            List<TrainTask> list = taskService.list(publishId);
            if (publishId == null) {
                list = list.stream()
                        .filter(item -> userScopeService.canManagePublish(item.getPublishId()))
                        .collect(Collectors.toList());
            }
            return ApiResponse.success(list);
        }
        return ApiResponse.success(taskService.list(publishId));
    }

    @PostMapping
    public ApiResponse<TrainTask> create(@RequestBody TrainTask task) {
        RoleGuard.requireTeacherOrAdmin();
        userScopeService.requireManagePublish(task.getPublishId());
        return ApiResponse.success(taskService.create(task));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainTask task) {
        RoleGuard.requireTeacherOrAdmin();
        TrainTask existing = taskMapper.selectById(id);
        if (existing == null) {
            throw new BizException("任务不存在或已删除");
        }
        userScopeService.requireManagePublish(existing.getPublishId());
        if (task.getPublishId() != null && !task.getPublishId().equals(existing.getPublishId())) {
            userScopeService.requireManagePublish(task.getPublishId());
        }
        task.setId(id);
        taskService.update(task);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        TrainTask existing = taskMapper.selectById(id);
        if (existing == null) {
            throw new BizException("任务不存在或已删除");
        }
        userScopeService.requireManagePublish(existing.getPublishId());
        taskService.delete(id);
        return ApiResponse.success();
    }
}
