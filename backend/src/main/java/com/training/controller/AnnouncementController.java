package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.TrainAnnouncementMapper;
import com.training.mapper.TrainProjectPublishMapper;
import com.training.model.dto.AnnouncementView;
import com.training.model.entity.TrainAnnouncement;
import com.training.model.entity.TrainProjectPublish;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;
    private final TrainAnnouncementMapper announcementMapper;
    private final UserScopeService userScopeService;
    private final TrainProjectPublishMapper publishMapper;

    public AnnouncementController(AnnouncementService announcementService,
                                  TrainAnnouncementMapper announcementMapper,
                                  UserScopeService userScopeService,
                                  TrainProjectPublishMapper publishMapper) {
        this.announcementService = announcementService;
        this.announcementMapper = announcementMapper;
        this.userScopeService = userScopeService;
        this.publishMapper = publishMapper;
    }

    @GetMapping
    public ApiResponse<List<AnnouncementView>> list(
            @RequestParam(value = "publishId", required = false) Long publishId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Long uid = AuthContext.getUserId();
        if (RoleGuard.isStudent()) {
            if (publishId != null) {
                userScopeService.requirePublishInCurrentStudentClass(publishId);
            }
            List<AnnouncementView> list = announcementService.list(publishId, keyword, uid, null, true);
            list = list.stream()
                    .filter(item -> item.getPublishId() == null || userScopeService.canAccessPublishForCurrentStudent(item.getPublishId()))
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        }
        return ApiResponse.success(announcementService.list(publishId, keyword, uid, null, false));
    }

    @PostMapping
    public ApiResponse<TrainAnnouncement> create(@RequestBody TrainAnnouncement announcement) {
        RoleGuard.requireTeacherOrAdmin();
        announcement.setAuthorId(AuthContext.getUserId());
        return ApiResponse.success(announcementService.create(announcement));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainAnnouncement announcement) {
        RoleGuard.requireTeacherOrAdmin();
        ensureAuthorOrAdmin(id);
        announcement.setId(id);
        announcementService.update(announcement);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        ensureAuthorOrAdmin(id);
        announcementService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable("id") Long id) {
        if (RoleGuard.isStudent()) {
            TrainAnnouncement announcement = announcementMapper.selectById(id);
            if (announcement == null) {
                throw new BizException("公告不存在或不可读");
            }
            ensureStudentCanAccessAnnouncement(announcement);
        }
        announcementService.markRead(id, AuthContext.getUserId());
        return ApiResponse.success();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        Long uid = AuthContext.getUserId();
        if (RoleGuard.isStudent()) {
            List<AnnouncementView> list = announcementService.list(null, null, uid, null, true);
            long count = list.stream()
                    .filter(item -> item.getPublishId() == null || userScopeService.canAccessPublishForCurrentStudent(item.getPublishId()))
                    .filter(item -> item.getReadFlag() == null || item.getReadFlag() == 0)
                    .count();
            return ApiResponse.success(count);
        }
        return ApiResponse.success(announcementService.unreadCount(uid, null));
    }

    private void ensureAuthorOrAdmin(Long announcementId) {
        if (RoleGuard.isAdmin()) {
            return;
        }
        TrainAnnouncement existing = announcementMapper.selectById(announcementId);
        if (existing == null) {
            throw new BizException("公告不存在或已删除");
        }
        Long uid = AuthContext.getUserId();
        if (uid == null || !uid.equals(existing.getAuthorId())) {
            throw new BizException(4030, "仅发布者或管理员可编辑该公告");
        }
    }

    private void ensureStudentCanAccessAnnouncement(TrainAnnouncement announcement) {
        if (!RoleGuard.isStudent()) {
            return;
        }
        if (announcement.getPublishId() == null) {
            return;
        }
        TrainProjectPublish publish = publishMapper.selectById(announcement.getPublishId());
        String className = userScopeService.currentStudentClassName();
        if (publish == null || !userScopeService.matchesClassScope(className, publish.getClassName())) {
            throw new BizException(4030, "无权限访问该班级公告");
        }
    }
}
