package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.SysUserMapper;
import com.training.mapper.TrainProjectPublishMapper;
import com.training.model.entity.SysUser;
import com.training.model.entity.TrainProjectPublish;
import com.training.model.entity.TrainTeam;
import com.training.model.entity.TrainTeamMember;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final UserScopeService userScopeService;
    private final TrainProjectPublishMapper publishMapper;
    private final SysUserMapper userMapper;

    public TeamController(TeamService teamService, UserScopeService userScopeService,
                          TrainProjectPublishMapper publishMapper, SysUserMapper userMapper) {
        this.teamService = teamService;
        this.userScopeService = userScopeService;
        this.publishMapper = publishMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<TrainTeam>> list(@RequestParam(value = "publishId", required = false) Long publishId) {
        if (RoleGuard.isStudent()) {
            if (publishId != null) {
                userScopeService.requirePublishInCurrentStudentClass(publishId);
            }
            String className = userScopeService.currentStudentClassName();
            return ApiResponse.success(teamService.listTeamsByClass(publishId, className));
        }
        return ApiResponse.success(teamService.listTeams(publishId));
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> myTeam(@RequestParam("publishId") Long publishId) {
        if (RoleGuard.isStudent()) {
            userScopeService.requirePublishInCurrentStudentClass(publishId);
        }
        Long uid = AuthContext.getUserId();
        TrainTeamMember membership = teamService.findStudentMembership(uid, publishId);
        Map<String, Object> data = new HashMap<>();
        if (membership == null) {
            data.put("inTeam", false);
            return ApiResponse.success(data);
        }
        TrainTeam team = teamService.getTeam(membership.getTeamId());
        data.put("inTeam", true);
        data.put("teamId", membership.getTeamId());
        data.put("leader", team != null && uid.equals(team.getLeaderStudentId()));
        return ApiResponse.success(data);
    }

    @PostMapping
    public ApiResponse<TrainTeam> create(@RequestBody TrainTeam team) {
        RoleGuard.requireTeacherOrAdmin();
        team.setReviewStatus(1);
        team.setReviewComment("教师创建团队");
        team.setReviewedBy(AuthContext.getUserId());
        return ApiResponse.success(teamService.createTeam(team));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainTeam payload) {
        RoleGuard.requireTeacherOrAdmin();
        payload.setId(id);
        payload.setReviewStatus(1);
        payload.setReviewComment("教师维护团队");
        payload.setReviewedBy(AuthContext.getUserId());
        teamService.updateTeam(payload);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        teamService.deleteTeam(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        RoleGuard.requireTeacherOrAdmin();
        Integer reviewStatus = Integer.parseInt(String.valueOf(body.get("reviewStatus")));
        String reviewComment = body.get("reviewComment") == null ? null : String.valueOf(body.get("reviewComment"));
        teamService.reviewTeam(id, reviewStatus, reviewComment, AuthContext.getUserId());
        return ApiResponse.success();
    }

    @PostMapping("/{teamId}/join")
    public ApiResponse<Void> join(@PathVariable("teamId") Long teamId) {
        if (!RoleGuard.isStudent()) {
            throw new BizException(4030, "仅学生可加入团队");
        }
        Long uid = AuthContext.getUserId();
        TrainTeam team = mustGetTeam(teamId);
        ensureTeamInStudentClass(team);

        TrainTeamMember existing = teamService.findStudentMembership(uid, team.getPublishId());
        if (existing != null) {
            throw new BizException("你已加入当前开设的团队");
        }

        TrainTeamMember member = new TrainTeamMember();
        member.setTeamId(teamId);
        member.setStudentId(uid);
        member.setRoleInTeam(team.getLeaderStudentId() == null ? "组长" : "成员");
        teamService.addMember(member);
        return ApiResponse.success();
    }

    @DeleteMapping("/{teamId}/quit")
    public ApiResponse<Void> quit(@PathVariable("teamId") Long teamId) {
        if (!RoleGuard.isStudent()) {
            throw new BizException(4030, "仅学生可退出团队");
        }
        Long uid = AuthContext.getUserId();
        TrainTeam team = mustGetTeam(teamId);
        ensureTeamInStudentClass(team);
        TrainTeamMember member = teamService.findStudentMembership(uid, team.getPublishId());
        if (member == null || !teamId.equals(member.getTeamId())) {
            throw new BizException("你不在该团队中");
        }
        if (uid.equals(team.getLeaderStudentId())) {
            Long memberCount = teamService.countMembers(teamId);
            if (memberCount != null && memberCount > 1) {
                throw new BizException("你是组长，请先转让组长后再退出");
            }
            teamService.clearLeader(teamId);
        }
        teamService.deleteMember(member.getId());
        return ApiResponse.success();
    }

    @GetMapping("/{teamId}/members")
    public ApiResponse<List<TrainTeamMember>> listMembers(@PathVariable("teamId") Long teamId) {
        TrainTeam team = mustGetTeam(teamId);
        if (RoleGuard.isStudent()) {
            ensureTeamInStudentClass(team);
        }
        return ApiResponse.success(teamService.listMembers(teamId));
    }

    @PostMapping("/{teamId}/members")
    public ApiResponse<TrainTeamMember> addMember(@PathVariable("teamId") Long teamId, @RequestBody TrainTeamMember member) {
        TrainTeam team = mustGetTeam(teamId);
        member.setTeamId(teamId);

        if (RoleGuard.isStudent()) {
            Long uid = AuthContext.getUserId();
            ensureTeamInStudentClass(team);
            if (!uid.equals(member.getStudentId())) {
                throw new BizException(4030, "学生仅可加入本人到团队");
            }
        } else {
            RoleGuard.requireTeacherOrAdmin();
        }

        if (member.getStudentId() == null) {
            throw new BizException("请选择要加入的学生");
        }
        validateStudentCanJoin(member.getStudentId(), team);
        if (team.getLeaderStudentId() == null) {
            member.setRoleInTeam("组长");
        }
        return ApiResponse.success(teamService.addMember(member));
    }

    @DeleteMapping("/members/{memberId}")
    public ApiResponse<Void> deleteMember(@PathVariable("memberId") Long memberId) {
        TrainTeamMember member = teamService.getMember(memberId);
        if (member == null) {
            throw new BizException("成员不存在或已删除");
        }
        TrainTeam team = mustGetTeam(member.getTeamId());

        if (RoleGuard.isStudent()) {
            Long uid = AuthContext.getUserId();
            requireLeader(team, uid);
            ensureTeamInStudentClass(team);
            if (member.getStudentId() != null && member.getStudentId().equals(uid)) {
                throw new BizException("组长不能通过移除自己退出团队");
            }
        } else {
            RoleGuard.requireTeacherOrAdmin();
        }

        teamService.deleteMember(memberId);
        if (team.getLeaderStudentId() != null && team.getLeaderStudentId().equals(member.getStudentId())) {
            List<TrainTeamMember> left = teamService.listMembers(team.getId());
            if (left == null || left.isEmpty()) {
                teamService.clearLeader(team.getId());
            } else {
                teamService.transferLeader(team.getId(), left.get(0).getStudentId());
            }
        }
        return ApiResponse.success();
    }

    @PutMapping("/{teamId}/leader")
    public ApiResponse<Void> transferLeader(@PathVariable("teamId") Long teamId, @RequestBody Map<String, Object> body) {
        TrainTeam team = mustGetTeam(teamId);
        Long newLeaderStudentId = body == null || body.get("studentId") == null
                ? null
                : Long.parseLong(String.valueOf(body.get("studentId")));
        if (newLeaderStudentId == null) {
            throw new BizException("请选择要转让的成员");
        }
        if (RoleGuard.isStudent()) {
            Long uid = AuthContext.getUserId();
            ensureTeamInStudentClass(team);
            requireLeader(team, uid);
        } else {
            RoleGuard.requireTeacherOrAdmin();
        }
        TrainTeamMember target = teamService.findMemberByTeamAndStudent(teamId, newLeaderStudentId);
        if (target == null) {
            throw new BizException("仅可转让给本组成员");
        }
        teamService.transferLeader(teamId, newLeaderStudentId);
        return ApiResponse.success();
    }

    private void validateStudentCanJoin(Long studentId, TrainTeam team) {
        SysUser student = userMapper.selectById(studentId);
        if (student == null || !"STUDENT".equals(student.getUserType())) {
            throw new BizException("目标用户不是有效学生");
        }
        TrainProjectPublish publish = publishMapper.selectById(team.getPublishId());
        if (publish == null) {
            throw new BizException("开设记录不存在或已删除");
        }
        if (!userScopeService.matchesClassScope(student.getClassName(), publish.getClassName())) {
            throw new BizException("仅可加入同班同学进入团队");
        }
        TrainTeamMember existing = teamService.findStudentMembership(studentId, team.getPublishId());
        if (existing != null) {
            throw new BizException("该学生已加入当前开设下的其他团队");
        }
    }

    private TrainTeam mustGetTeam(Long id) {
        TrainTeam team = teamService.getTeam(id);
        if (team == null) {
            throw new BizException("团队不存在或已删除");
        }
        return team;
    }

    private void requireLeader(TrainTeam team, Long userId) {
        if (team.getLeaderStudentId() == null || !team.getLeaderStudentId().equals(userId)) {
            throw new BizException(4030, "仅团队组长可操作");
        }
    }

    private void ensureTeamInStudentClass(TrainTeam team) {
        String className = userScopeService.currentStudentClassName();
        TrainProjectPublish publish = publishMapper.selectById(team.getPublishId());
        if (publish == null || !userScopeService.matchesClassScope(className, publish.getClassName())) {
            throw new BizException(4030, "无权限访问该班级团队数据");
        }
    }

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generateByRule(@RequestParam("publishId") Long publishId) {
        RoleGuard.requireTeacherOrAdmin();
        TrainProjectPublish publish = publishMapper.selectById(publishId);
        if (publish == null) {
            throw new BizException("开设计划不存在或已删除");
        }
        int groupCount = publish.getGroupCount() == null ? 0 : publish.getGroupCount();
        if (groupCount <= 0) {
            throw new BizException("请先在开设计划中配置分组数量");
        }

        List<TrainTeam> existing = teamService.listTeams(publishId);
        int existingCount = existing == null ? 0 : existing.size();
        int created = 0;
        for (int i = existingCount + 1; i <= groupCount; i++) {
            TrainTeam team = new TrainTeam();
            team.setPublishId(publishId);
            team.setTeamName("第" + i + "组");
            team.setStatus(1);
            team.setReviewStatus(1);
            team.setReviewComment("按开设规则自动生成");
            team.setReviewedBy(AuthContext.getUserId());
            teamService.createTeam(team);
            created++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("publishId", publishId);
        result.put("targetGroupCount", groupCount);
        result.put("existingCount", existingCount);
        result.put("createdCount", created);
        if (created > 0) {
            result.put("message", "已生成" + created + "个小组");
        } else if (existingCount > groupCount) {
            result.put("message", "当前已有" + existingCount + "个小组，超出配置数量，请按需清理");
        } else {
            result.put("message", "当前小组数量已满足配置，无需新增");
        }
        return ApiResponse.success(result);
    }

}
