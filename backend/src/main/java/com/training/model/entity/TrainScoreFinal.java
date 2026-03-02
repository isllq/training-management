package com.training.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TrainScoreFinal {
    private Long id;
    private Long publishId;
    private String termName;
    private String className;
    private String projectName;
    private Long studentId;
    private String studentName;
    private BigDecimal usualScore;
    private BigDecimal taskScore;
    private BigDecimal reportScore;
    private BigDecimal finalScore;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
