package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EduMajor {
    private Long id;
    private Long collegeId;
    private String collegeName;
    private String majorCode;
    private String majorName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
