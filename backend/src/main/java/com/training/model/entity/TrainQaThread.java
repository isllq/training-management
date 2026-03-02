package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainQaThread {
    private Long id;
    private Long publishId;
    private String title;
    private String content;
    private Long creatorId;
    private String creatorName;
    private Integer replyCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
