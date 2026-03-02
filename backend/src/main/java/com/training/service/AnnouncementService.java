package com.training.service;

import com.training.model.dto.AnnouncementView;
import com.training.model.entity.TrainAnnouncement;

import java.util.List;

public interface AnnouncementService {
    List<AnnouncementView> list(Long publishId, String keyword, Long userId, String className, Boolean onlyPublished);

    TrainAnnouncement create(TrainAnnouncement announcement);

    void update(TrainAnnouncement announcement);

    void delete(Long id);

    void markRead(Long announcementId, Long userId);

    Long unreadCount(Long userId, String className);
}
