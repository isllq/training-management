package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainAnnouncementMapper;
import com.training.mapper.TrainAnnouncementReadMapper;
import com.training.model.dto.AnnouncementView;
import com.training.model.entity.TrainAnnouncement;
import com.training.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    private final TrainAnnouncementMapper announcementMapper;
    private final TrainAnnouncementReadMapper announcementReadMapper;

    public AnnouncementServiceImpl(TrainAnnouncementMapper announcementMapper, TrainAnnouncementReadMapper announcementReadMapper) {
        this.announcementMapper = announcementMapper;
        this.announcementReadMapper = announcementReadMapper;
    }

    @Override
    public List<AnnouncementView> list(Long publishId, String keyword, Long userId, String className, Boolean onlyPublished) {
        return announcementMapper.list(publishId, keyword, userId, className, onlyPublished);
    }

    @Override
    public TrainAnnouncement create(TrainAnnouncement announcement) {
        validate(announcement);
        if (announcement.getPriority() == null) {
            announcement.setPriority(1);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public void update(TrainAnnouncement announcement) {
        validate(announcement);
        TrainAnnouncement existing = announcementMapper.selectById(announcement.getId());
        if (existing == null) {
            throw new BizException("公告不存在或已删除");
        }
        if (existing.getStatus() != null && existing.getStatus() == 1) {
            throw new BizException("已发布公告不可编辑，请删除后重新发布");
        }
        if (announcement.getPriority() == null) {
            announcement.setPriority(existing.getPriority() == null ? 1 : existing.getPriority());
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(existing.getStatus() == null ? 0 : existing.getStatus());
        }
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(existing.getPublishTime());
        }
        if (announcement.getExpireTime() == null) {
            announcement.setExpireTime(existing.getExpireTime());
        }
        int affected = announcementMapper.update(announcement);
        if (affected <= 0) {
            throw new BizException("公告不存在或已删除");
        }
    }

    @Override
    public void delete(Long id) {
        int affected = announcementMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("公告不存在或已删除");
        }
    }

    @Override
    public void markRead(Long announcementId, Long userId) {
        TrainAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null || announcement.getStatus() == null || announcement.getStatus() != 1) {
            throw new BizException("公告不存在或不可读");
        }
        announcementReadMapper.markRead(announcementId, userId);
    }

    @Override
    public Long unreadCount(Long userId, String className) {
        if (StringUtils.hasText(className)) {
            return announcementMapper.countUnreadByClass(userId, className);
        }
        return announcementMapper.countUnread(userId);
    }

    private void validate(TrainAnnouncement announcement) {
        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new BizException("公告标题不能为空");
        }
        if (!StringUtils.hasText(announcement.getContent())) {
            throw new BizException("公告内容不能为空");
        }
    }
}
