package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainSubmission {
    private Long id;
    private Long taskId;
    private Long teamId;
    private Long studentId;
    private Integer versionNo;
    private String content;
    private String fileUrl;
    private LocalDateTime submitTime;
    private Integer status;
    private String teacherFeedback;
    private Integer isDeleted;
}
