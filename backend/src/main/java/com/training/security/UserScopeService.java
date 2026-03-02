package com.training.security;

import com.training.common.BizException;
import com.training.mapper.SysUserMapper;
import com.training.mapper.TrainProjectPublishMapper;
import com.training.model.entity.SysUser;
import com.training.model.entity.TrainProjectPublish;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Service
public class UserScopeService {
    private final SysUserMapper userMapper;
    private final TrainProjectPublishMapper publishMapper;

    public UserScopeService(SysUserMapper userMapper, TrainProjectPublishMapper publishMapper) {
        this.userMapper = userMapper;
        this.publishMapper = publishMapper;
    }

    public SysUser currentUser() {
        Long uid = AuthContext.getUserId();
        if (uid == null) {
            throw new BizException(401, "未登录或token无效");
        }
        SysUser user = userMapper.selectById(uid);
        if (user == null) {
            throw new BizException(401, "用户不存在或已禁用");
        }
        return user;
    }

    public String currentStudentClassName() {
        SysUser user = currentUser();
        if (!RoleGuard.isStudent()) {
            return null;
        }
        String className = user.getClassName();
        if (!StringUtils.hasText(className) || !StringUtils.hasText(className.trim())) {
            throw new BizException(4001, "当前学生未绑定班级，无法查看班级数据");
        }
        return className.trim();
    }

    public boolean canAccessPublishForCurrentStudent(Long publishId) {
        if (!RoleGuard.isStudent()) {
            return true;
        }
        if (publishId == null) {
            return true;
        }
        String className = currentStudentClassName();
        TrainProjectPublish publish = publishMapper.selectById(publishId);
        if (publish == null) {
            return false;
        }
        return matchesClassScope(className, publish.getClassName());
    }

    public void requirePublishInCurrentStudentClass(Long publishId) {
        if (!RoleGuard.isStudent()) {
            return;
        }
        if (publishId == null) {
            throw new BizException("请选择开设计划");
        }
        if (!canAccessPublishForCurrentStudent(publishId)) {
            throw new BizException(4030, "无权限访问该班级数据");
        }
    }

    public boolean matchesClassScope(String studentClassName, String publishClassName) {
        if (!StringUtils.hasText(studentClassName) || !StringUtils.hasText(publishClassName)) {
            return false;
        }
        String normalizedStudent = studentClassName.trim();
        String normalizedPublish = publishClassName.trim();
        if (normalizedStudent.equals(normalizedPublish)) {
            return true;
        }
        String[] tokens = normalizedPublish.split("[,，;；/\\s]+");
        return Arrays.stream(tokens)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .anyMatch(normalizedStudent::equals);
    }
}
