package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainQaReply {
    private Long id;
    private Long threadId;
    private String content;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private Integer isDeleted;
}
