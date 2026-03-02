package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainTeam {
    private Long id;
    private Long publishId;
    private String teamName;
    private Long leaderStudentId;
    private Integer memberCount;
    private Integer groupSizeLimit;
    private Integer status;
    private Integer reviewStatus;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
