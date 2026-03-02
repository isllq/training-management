package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainTeamMapper;
import com.training.mapper.TrainTeamMemberMapper;
import com.training.model.entity.TrainTeam;
import com.training.model.entity.TrainTeamMember;
import com.training.service.TeamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    private final TrainTeamMapper teamMapper;
    private final TrainTeamMemberMapper memberMapper;

    public TeamServiceImpl(TrainTeamMapper teamMapper, TrainTeamMemberMapper memberMapper) {
        this.teamMapper = teamMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    public List<TrainTeam> listTeams(Long publishId) {
        return teamMapper.list(publishId);
    }

    @Override
    public List<TrainTeam> listTeamsByClass(Long publishId, String className) {
        return teamMapper.listByClassName(publishId, className);
    }

    @Override
    public TrainTeam createTeam(TrainTeam team) {
        if (team.getStatus() == null) {
            team.setStatus(1);
        }
        if (team.getReviewStatus() == null) {
            team.setReviewStatus(1);
        }
        if (team.getReviewComment() == null || team.getReviewComment().trim().isEmpty()) {
            team.setReviewComment("系统自动通过");
        }
        teamMapper.insert(team);
        if (team.getLeaderStudentId() != null) {
            TrainTeamMember existing = memberMapper.selectByTeamAndStudent(team.getId(), team.getLeaderStudentId());
            if (existing == null) {
                TrainTeamMember leader = new TrainTeamMember();
                leader.setTeamId(team.getId());
                leader.setStudentId(team.getLeaderStudentId());
                leader.setRoleInTeam("组长");
                memberMapper.insert(leader);
            }
        }
        return team;
    }

    @Override
    public void updateTeam(TrainTeam team) {
        int affected = teamMapper.update(team);
        if (affected <= 0) {
            throw new BizException("团队不存在或已删除");
        }
    }

    @Override
    public void deleteTeam(Long id) {
        int affected = teamMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("团队不存在或已删除");
        }
    }

    @Override
    public void reviewTeam(Long id, Integer reviewStatus, String reviewComment, Long reviewedBy) {
        int affected = teamMapper.review(id, reviewStatus, reviewComment, reviewedBy);
        if (affected <= 0) {
            throw new BizException("团队不存在或已删除");
        }
    }

    @Override
    public TrainTeam getTeam(Long id) {
        return teamMapper.selectById(id);
    }

    @Override
    public TrainTeamMember getMember(Long memberId) {
        return memberMapper.selectById(memberId);
    }

    @Override
    public TrainTeamMember findStudentMembership(Long studentId, Long publishId) {
        return memberMapper.selectByStudentAndPublish(studentId, publishId);
    }

    @Override
    public List<TrainTeamMember> listMembers(Long teamId) {
        return memberMapper.listByTeamId(teamId);
    }

    @Override
    public TrainTeamMember addMember(TrainTeamMember member) {
        TrainTeamMember active = memberMapper.selectByTeamAndStudent(member.getTeamId(), member.getStudentId());
        if (active != null) {
            throw new BizException("该学生已在团队中");
        }
        TrainTeamMember any = memberMapper.selectAnyByTeamAndStudent(member.getTeamId(), member.getStudentId());
        if (any != null) {
            memberMapper.reviveByTeamAndStudent(member.getTeamId(), member.getStudentId(), member.getRoleInTeam());
            TrainTeamMember revived = memberMapper.selectByTeamAndStudent(member.getTeamId(), member.getStudentId());
            ensureLeaderWhenEmpty(member.getTeamId(), member.getStudentId());
            return revived == null ? member : revived;
        }
        memberMapper.insert(member);
        ensureLeaderWhenEmpty(member.getTeamId(), member.getStudentId());
        return member;
    }

    @Override
    public void deleteMember(Long id) {
        int affected = memberMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("成员不存在或已删除");
        }
    }

    @Override
    public Long countMembers(Long teamId) {
        Long count = memberMapper.countByTeamId(teamId);
        return count == null ? 0L : count;
    }

    @Override
    public TrainTeamMember findMemberByTeamAndStudent(Long teamId, Long studentId) {
        return memberMapper.selectByTeamAndStudent(teamId, studentId);
    }

    @Override
    public void transferLeader(Long teamId, Long newLeaderStudentId) {
        TrainTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BizException("团队不存在或已删除");
        }
        TrainTeamMember target = memberMapper.selectByTeamAndStudent(teamId, newLeaderStudentId);
        if (target == null) {
            throw new BizException("仅可转让给本组成员");
        }
        Long oldLeaderId = team.getLeaderStudentId();
        int affected = teamMapper.updateLeader(teamId, newLeaderStudentId);
        if (affected <= 0) {
            throw new BizException("团队不存在或已删除");
        }
        memberMapper.updateRoleByTeamAndStudent(teamId, newLeaderStudentId, "组长");
        if (oldLeaderId != null && !oldLeaderId.equals(newLeaderStudentId)) {
            memberMapper.updateRoleByTeamAndStudent(teamId, oldLeaderId, "成员");
        }
    }

    @Override
    public void clearLeader(Long teamId) {
        int affected = teamMapper.clearLeader(teamId);
        if (affected <= 0) {
            throw new BizException("团队不存在或已删除");
        }
    }

    private void ensureLeaderWhenEmpty(Long teamId, Long candidateStudentId) {
        TrainTeam team = teamMapper.selectById(teamId);
        if (team == null || team.getLeaderStudentId() != null) {
            return;
        }
        teamMapper.updateLeader(teamId, candidateStudentId);
        memberMapper.updateRoleByTeamAndStudent(teamId, candidateStudentId, "组长");
    }
}
