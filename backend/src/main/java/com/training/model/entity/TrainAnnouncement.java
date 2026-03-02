package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainAnnouncement {
    private Long id;
    private Long publishId;
    private String title;
    private String content;
    private Long authorId;
    private Integer priority;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
