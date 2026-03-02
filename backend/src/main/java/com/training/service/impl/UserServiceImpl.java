package com.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.training.common.BizException;
import com.training.mapper.SysUserMapper;
import com.training.model.entity.SysUser;
import com.training.service.UserService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataFormatter dataFormatter = new DataFormatter();

    public UserServiceImpl(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<SysUser> list(String keyword, String className, String sortBy) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("username", keyword).or().like("real_name", keyword));
        }
        if (StringUtils.hasText(className)) {
            wrapper.eq("class_name", className);
        }
        if ("CLASS_ASC".equals(sortBy)) {
            wrapper.last("ORDER BY CASE WHEN class_name IS NULL OR class_name = '' THEN 1 ELSE 0 END ASC, class_name ASC, id ASC");
        } else if ("NAME_INITIAL_ASC".equals(sortBy)) {
            wrapper.last("ORDER BY UPPER(SUBSTRING(real_name, 1, 1)) ASC, real_name ASC, id ASC");
        } else if ("ID_DESC".equals(sortBy)) {
            wrapper.orderByDesc("id");
        } else {
            wrapper.orderByAsc("id");
        }
        return userMapper.selectList(wrapper);
    }

    @Override
    public List<SysUser> listOptions(String userType, String className) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.select("id", "real_name", "user_type", "class_name");
        wrapper.eq("is_deleted", 0).eq("status", 1);
        if (StringUtils.hasText(userType)) {
            wrapper.eq("user_type", userType);
        }
        if (StringUtils.hasText(className)) {
            wrapper.eq("class_name", className);
        }
        wrapper.orderByAsc("id");
        return userMapper.selectList(wrapper);
    }

    @Override
    public SysUser create(SysUser user, String rawPassword) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        if (!"STUDENT".equals(user.getUserType())) {
            user.setClassName(null);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userMapper.insert(user);
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public void update(SysUser user) {
        if (user.getId() == null) {
            throw new BizException("用户ID不能为空");
        }
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BizException("用户不存在或已删除");
        }
        if (!"STUDENT".equals(user.getUserType())) {
            user.setClassName(null);
        } else if (user.getClassName() == null) {
            user.setClassName(existing.getClassName());
        }
        int affected = userMapper.update(user);
        if (affected <= 0) {
            throw new BizException("用户不存在或已删除");
        }
    }

    @Override
    public void remove(Long id) {
        int affected = userMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("用户不存在或已删除");
        }
    }

    @Override
    public void resetPassword(Long id, String rawPassword) {
        int affected = userMapper.updatePassword(id, passwordEncoder.encode(rawPassword));
        if (affected <= 0) {
            throw new BizException("用户不存在或已删除");
        }
    }

    @Override
    public int importUsersFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        int successCount = 0;
        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String username = readCell(row.getCell(0));
                String realName = readCell(row.getCell(1));
                String userType = readCell(row.getCell(2));
                String phone = readCell(row.getCell(3));
                String email = readCell(row.getCell(4));
                String password = readCell(row.getCell(5));
                String className = readCell(row.getCell(6));
                if (username.isEmpty() || realName.isEmpty() || userType.isEmpty()) {
                    continue;
                }
                if (!"ADMIN".equals(userType) && !"TEACHER".equals(userType) && !"STUDENT".equals(userType)) {
                    continue;
                }
                if (userMapper.selectByUsername(username) != null) {
                    continue;
                }
                SysUser user = new SysUser();
                user.setUsername(username);
                user.setRealName(realName);
                user.setUserType(userType);
                user.setPhone(phone);
                user.setEmail(email);
                user.setClassName("STUDENT".equals(userType) ? className : null);
                user.setStatus(1);
                user.setPasswordHash(passwordEncoder.encode(password.isEmpty() ? "123456" : password));
                userMapper.insert(user);
                successCount++;
            }
        } catch (Exception ex) {
            throw new BizException("Excel导入失败: " + ex.getMessage());
        }
        return successCount;
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        String value = dataFormatter.formatCellValue(cell);
        return value == null ? "" : value.trim();
    }
}
