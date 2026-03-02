package com.training.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementView {
    private Long id;
    private Long publishId;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Integer priority;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer readFlag;
    private Long readCount;
    private Long unreadCount;
    private Long targetCount;
}
