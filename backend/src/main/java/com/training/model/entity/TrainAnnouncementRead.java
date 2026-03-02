package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainAnnouncementRead {
    private Long id;
    private Long announcementId;
    private Long userId;
    private LocalDateTime readTime;
    private Integer isDeleted;
}
