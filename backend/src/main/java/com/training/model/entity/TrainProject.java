package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainProject {
    private Long id;
    private String projectCode;
    private String projectName;
    private String description;
    private Integer totalHours;
    private String difficulty;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
