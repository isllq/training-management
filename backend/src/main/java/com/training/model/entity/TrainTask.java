package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainTask {
    private Long id;
    private Long publishId;
    private String termName;
    private String className;
    private String projectName;
    private String stageType;
    private String title;
    private String content;
    private LocalDateTime deadline;
    private Integer expectedGroupCount;
    private Integer submittedGroupCount;
    private Integer completionRate;
    private String missingTeamNames;
    private Integer weight;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
