package com.training.model.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private Long userCount;
    private Long projectCount;
    private Long teamCount;
    private Long taskCount;
    private Long submissionCount;
    private Long qaThreadCount;
    private Long pendingTeamReviewCount;
    private Long unreadAnnouncementCount;
}
