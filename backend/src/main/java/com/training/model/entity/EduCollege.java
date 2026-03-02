package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EduCollege {
    private Long id;
    private String collegeCode;
    private String collegeName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
