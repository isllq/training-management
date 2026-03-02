package com.training.security;

import com.training.common.BizException;

import java.util.Arrays;

public final class RoleGuard {
    public static final String ADMIN = "ADMIN";
    public static final String TEACHER = "TEACHER";
    public static final String STUDENT = "STUDENT";

    private RoleGuard() {
    }

    public static boolean isAdmin() {
        return ADMIN.equals(AuthContext.getUserType());
    }

    public static boolean isTeacher() {
        return TEACHER.equals(AuthContext.getUserType());
    }

    public static boolean isStudent() {
        return STUDENT.equals(AuthContext.getUserType());
    }

    public static void requireAdmin() {
        requireAny(ADMIN);
    }

    public static void requireTeacherOrAdmin() {
        requireAny(ADMIN, TEACHER);
    }

    public static void requireAny(String... userTypes) {
        String current = AuthContext.getUserType();
        if (current == null || Arrays.stream(userTypes).noneMatch(current::equals)) {
            throw new BizException(4030, "无权限操作");
        }
    }

    public static void requireSelfOrTeacherOrAdmin(Long ownerUserId) {
        if (isAdmin() || isTeacher()) {
            return;
        }
        Long uid = AuthContext.getUserId();
        if (uid == null || ownerUserId == null || !uid.equals(ownerUserId)) {
            throw new BizException(4030, "无权限操作");
        }
    }
}
