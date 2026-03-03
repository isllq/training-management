package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.TrainTaskMapper;
import com.training.mapper.TrainSubmissionMapper;
import com.training.model.entity.TrainSubmission;
import com.training.model.entity.TrainTask;
import com.training.model.entity.TrainTeamMember;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.SubmissionService;
import com.training.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final TrainSubmissionMapper submissionMapper;
    private final TrainTaskMapper taskMapper;
    private final UserScopeService userScopeService;
    private final TeamService teamService;

    public SubmissionController(SubmissionService submissionService,
                                TrainSubmissionMapper submissionMapper,
                                TrainTaskMapper taskMapper,
                                UserScopeService userScopeService,
                                TeamService teamService) {
        this.submissionService = submissionService;
        this.submissionMapper = submissionMapper;
        this.taskMapper = taskMapper;
        this.userScopeService = userScopeService;
        this.teamService = teamService;
    }

    @GetMapping
    public ApiResponse<List<TrainSubmission>> list(@RequestParam(value = "taskId", required = false) Long taskId,
                                                   @RequestParam(value = "teamId", required = false) Long teamId) {
        List<TrainSubmission> list = submissionService.list(taskId, teamId);
        if (RoleGuard.isStudent()) {
            Long uid = AuthContext.getUserId();
            list = list.stream().filter(item -> uid != null && uid.equals(item.getStudentId())).collect(Collectors.toList());
        }
        return ApiResponse.success(list);
    }

    @PostMapping
    public ApiResponse<TrainSubmission> create(@RequestBody TrainSubmission submission) {
        if (!RoleGuard.isStudent()) {
            throw new BizException(4030, "仅学生可提交任务");
        }
        ensureTaskInStudentClass(submission.getTaskId());
        Long uid = AuthContext.getUserId();
        TrainTask task = mustGetTask(submission.getTaskId());
        requireTaskBeforeDeadline(task);
        TrainTeamMember membership = teamService.findStudentMembership(uid, task.getPublishId());
        if (membership == null) {
            throw new BizException("请先加入当前开设的小组后再提交任务");
        }
        TrainSubmission existing = submissionMapper.selectLatestByTaskAndStudent(submission.getTaskId(), uid);
        submission.setStudentId(uid);
        submission.setTeamId(membership.getTeamId());
        submission.setVersionNo(1);
        submission.setStatus(1);
        submission.setTeacherFeedback(null);
        if (existing != null) {
            submission.setId(existing.getId());
            submissionService.update(submission);
            return ApiResponse.success(submissionMapper.selectById(existing.getId()));
        }
        return ApiResponse.success(submissionService.create(submission));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainSubmission submission) {
        TrainSubmission existing = submissionMapper.selectById(id);
        if (existing == null) {
            throw new BizException("提交记录不存在或已删除");
        }

        if (RoleGuard.isStudent()) {
            RoleGuard.requireSelfOrTeacherOrAdmin(existing.getStudentId());
            ensureTaskInStudentClass(existing.getTaskId());
            TrainTask task = mustGetTask(existing.getTaskId());
            requireTaskBeforeDeadline(task);
            submission.setStudentId(existing.getStudentId());
            submission.setTeamId(existing.getTeamId());
            submission.setVersionNo(1);
            submission.setStatus(existing.getStatus());
            submission.setTeacherFeedback(existing.getTeacherFeedback());
        } else if (RoleGuard.isAdmin()) {
            // 管理员可维护异常数据，教师仅查看提交详情
        } else {
            throw new BizException(4030, "教师不可修改学生提交记录");
        }

        submission.setId(id);
        submissionService.update(submission);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        TrainSubmission existing = submissionMapper.selectById(id);
        if (existing == null) {
            throw new BizException("提交记录不存在或已删除");
        }

        if (RoleGuard.isStudent()) {
            RoleGuard.requireSelfOrTeacherOrAdmin(existing.getStudentId());
            ensureTaskInStudentClass(existing.getTaskId());
            TrainTask task = mustGetTask(existing.getTaskId());
            requireTaskBeforeDeadline(task);
        } else if (RoleGuard.isAdmin()) {
            // 管理员可删除异常数据
        } else {
            throw new BizException(4030, "教师不可删除学生提交记录");
        }

        submissionService.delete(id);
        return ApiResponse.success();
    }

    private void ensureTaskInStudentClass(Long taskId) {
        if (!RoleGuard.isStudent()) {
            return;
        }
        if (taskId == null) {
            throw new BizException("请选择任务");
        }
        TrainTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("任务不存在或已删除");
        }
        userScopeService.requirePublishInCurrentStudentClass(task.getPublishId());
    }

    private TrainTask mustGetTask(Long taskId) {
        TrainTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("任务不存在或已删除");
        }
        return task;
    }

    private void requireTaskBeforeDeadline(TrainTask task) {
        if (!RoleGuard.isStudent()) {
            return;
        }
        if (task == null || task.getDeadline() == null) {
            return;
        }
        if (!task.getDeadline().isAfter(LocalDateTime.now())) {
            throw new BizException("任务已截止，不能再提交或修改");
        }
    }
}
