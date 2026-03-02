package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.TrainQaReplyMapper;
import com.training.mapper.TrainQaThreadMapper;
import com.training.mapper.TrainProjectPublishMapper;
import com.training.model.entity.TrainQaReply;
import com.training.model.entity.TrainQaThread;
import com.training.model.entity.TrainProjectPublish;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.QaService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qa")
public class QaController {
    private final QaService qaService;
    private final TrainQaThreadMapper threadMapper;
    private final TrainQaReplyMapper replyMapper;
    private final UserScopeService userScopeService;
    private final TrainProjectPublishMapper publishMapper;

    public QaController(QaService qaService,
                        TrainQaThreadMapper threadMapper,
                        TrainQaReplyMapper replyMapper,
                        UserScopeService userScopeService,
                        TrainProjectPublishMapper publishMapper) {
        this.qaService = qaService;
        this.threadMapper = threadMapper;
        this.replyMapper = replyMapper;
        this.userScopeService = userScopeService;
        this.publishMapper = publishMapper;
    }

    @GetMapping("/threads")
    public ApiResponse<List<TrainQaThread>> listThreads(@RequestParam(value = "publishId", required = false) Long publishId,
                                                        @RequestParam(value = "className", required = false) String className) {
        if (RoleGuard.isStudent()) {
            if (publishId != null) {
                userScopeService.requirePublishInCurrentStudentClass(publishId);
            }
            return ApiResponse.success(qaService.listThreadsByClass(publishId, userScopeService.currentStudentClassName()));
        }
        if (StringUtils.hasText(className)) {
            return ApiResponse.success(qaService.listThreadsByClass(publishId, className.trim()));
        }
        return ApiResponse.success(qaService.listThreads(publishId));
    }

    @PostMapping("/threads")
    public ApiResponse<TrainQaThread> createThread(@RequestBody TrainQaThread thread) {
        if (RoleGuard.isStudent()) {
            userScopeService.requirePublishInCurrentStudentClass(thread.getPublishId());
        }
        thread.setCreatorId(AuthContext.getUserId());
        return ApiResponse.success(qaService.createThread(thread));
    }

    @PutMapping("/threads/{id}")
    public ApiResponse<Void> updateThread(@PathVariable("id") Long id, @RequestBody TrainQaThread thread) {
        TrainQaThread existing = threadMapper.selectById(id);
        if (existing == null) {
            throw new BizException("问答主题不存在或已删除");
        }
        if (RoleGuard.isStudent()) {
            RoleGuard.requireSelfOrTeacherOrAdmin(existing.getCreatorId());
            ensureThreadInStudentClass(existing);
            if (thread.getPublishId() != null && !thread.getPublishId().equals(existing.getPublishId())) {
                userScopeService.requirePublishInCurrentStudentClass(thread.getPublishId());
            }
        }
        thread.setId(id);
        qaService.updateThread(thread);
        return ApiResponse.success();
    }

    @DeleteMapping("/threads/{id}")
    public ApiResponse<Void> deleteThread(@PathVariable("id") Long id) {
        TrainQaThread existing = threadMapper.selectById(id);
        if (existing == null) {
            throw new BizException("问答主题不存在或已删除");
        }
        if (RoleGuard.isStudent()) {
            RoleGuard.requireSelfOrTeacherOrAdmin(existing.getCreatorId());
            ensureThreadInStudentClass(existing);
        }
        qaService.deleteThread(id);
        return ApiResponse.success();
    }

    @GetMapping("/threads/{threadId}/replies")
    public ApiResponse<List<TrainQaReply>> listReplies(@PathVariable("threadId") Long threadId) {
        TrainQaThread thread = ensureThreadExists(threadId);
        if (RoleGuard.isStudent()) {
            ensureThreadInStudentClass(thread);
        }
        return ApiResponse.success(qaService.listReplies(threadId));
    }

    @PostMapping("/threads/{threadId}/replies")
    public ApiResponse<TrainQaReply> createReply(@PathVariable("threadId") Long threadId, @RequestBody TrainQaReply reply) {
        TrainQaThread thread = ensureThreadExists(threadId);
        if (RoleGuard.isStudent()) {
            ensureThreadInStudentClass(thread);
        }
        reply.setThreadId(threadId);
        reply.setCreatorId(AuthContext.getUserId());
        return ApiResponse.success(qaService.createReply(reply));
    }

    @DeleteMapping("/replies/{id}")
    public ApiResponse<Void> deleteReply(@PathVariable("id") Long id) {
        TrainQaReply existing = replyMapper.selectById(id);
        if (existing == null) {
            throw new BizException("问答回复不存在或已删除");
        }
        TrainQaThread thread = ensureThreadExists(existing.getThreadId());
        if (RoleGuard.isStudent()) {
            RoleGuard.requireSelfOrTeacherOrAdmin(existing.getCreatorId());
            ensureThreadInStudentClass(thread);
        }
        qaService.deleteReply(id);
        return ApiResponse.success();
    }

    private TrainQaThread ensureThreadExists(Long threadId) {
        TrainQaThread thread = threadMapper.selectById(threadId);
        if (thread == null) {
            throw new BizException("问答主题不存在或已删除");
        }
        return thread;
    }

    private void ensureThreadInStudentClass(TrainQaThread thread) {
        if (!RoleGuard.isStudent()) {
            return;
        }
        TrainProjectPublish publish = publishMapper.selectById(thread.getPublishId());
        String className = userScopeService.currentStudentClassName();
        if (publish == null || !userScopeService.matchesClassScope(className, publish.getClassName())) {
            throw new BizException(4030, "无权限访问该班级答疑数据");
        }
    }
}
