package com.training.model.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TrainProjectPublish {
    private Long id;
    private Long projectId;
    private String projectName;
    private String termName;
    private String className;
    private Long teacherId;
    private String teacherName;
    private Integer groupCount;
    private Integer groupSizeLimit;
    private String assessmentStandard;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
