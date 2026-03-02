package com.training.service;

import com.training.model.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    List<SysUser> list(String keyword, String className, String sortBy);
    List<SysUser> listOptions(String userType, String className);

    SysUser create(SysUser user, String rawPassword);

    void update(SysUser user);

    void remove(Long id);

    void resetPassword(Long id, String rawPassword);

    int importUsersFromExcel(MultipartFile file);
}
