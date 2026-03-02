package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainTeamMember {
    private Long id;
    private Long teamId;
    private Long studentId;
    private String roleInTeam;
    private LocalDateTime createdAt;
    private Integer isDeleted;
}
