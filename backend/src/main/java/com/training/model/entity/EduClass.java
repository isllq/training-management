package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EduClass {
    private Long id;
    private Long majorId;
    private String majorName;
    private String collegeName;
    private String classCode;
    private String className;
    private Integer gradeYear;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
