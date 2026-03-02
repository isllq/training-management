package com.training.service.impl;

import com.training.mapper.SysUserMapper;
import com.training.mapper.TrainAnnouncementMapper;
import com.training.mapper.TrainProjectMapper;
import com.training.mapper.TrainQaThreadMapper;
import com.training.mapper.TrainSubmissionMapper;
import com.training.mapper.TrainTaskMapper;
import com.training.mapper.TrainTeamMapper;
import com.training.model.dto.DashboardStats;
import com.training.model.entity.SysUser;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final SysUserMapper userMapper;
    private final TrainProjectMapper projectMapper;
    private final TrainTeamMapper teamMapper;
    private final TrainTaskMapper taskMapper;
    private final TrainSubmissionMapper submissionMapper;
    private final TrainQaThreadMapper qaThreadMapper;
    private final TrainAnnouncementMapper announcementMapper;

    public DashboardServiceImpl(SysUserMapper userMapper, TrainProjectMapper projectMapper, TrainTeamMapper teamMapper,
                                TrainTaskMapper taskMapper, TrainSubmissionMapper submissionMapper,
                                TrainQaThreadMapper qaThreadMapper, TrainAnnouncementMapper announcementMapper) {
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
        this.teamMapper = teamMapper;
        this.taskMapper = taskMapper;
        this.submissionMapper = submissionMapper;
        this.qaThreadMapper = qaThreadMapper;
        this.announcementMapper = announcementMapper;
    }

    @Override
    public DashboardStats stats() {
        DashboardStats stats = new DashboardStats();
        stats.setUserCount(userMapper.countAll());
        stats.setProjectCount(projectMapper.countAll());
        stats.setTeamCount(teamMapper.countAll());
        stats.setTaskCount(taskMapper.countAll());
        stats.setSubmissionCount(submissionMapper.countAll());
        stats.setQaThreadCount(qaThreadMapper.countAll());
        stats.setPendingTeamReviewCount(teamMapper.countPendingReview());
        if (!RoleGuard.isStudent()) {
            stats.setUnreadAnnouncementCount(announcementMapper.countUnread(AuthContext.getUserId()));
        } else {
            SysUser me = userMapper.selectById(AuthContext.getUserId());
            String className = me == null ? null : me.getClassName();
            if (className == null || className.trim().isEmpty()) {
                stats.setUnreadAnnouncementCount(0L);
            } else {
                stats.setUnreadAnnouncementCount(announcementMapper.countUnreadByClass(AuthContext.getUserId(), className));
            }
        }
        return stats;
    }
}
