package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.register.RegisterRequest;
import com.swxt.manager.dto.user.UpdateUserRequest;
import com.swxt.manager.entity.User;
import com.swxt.manager.mysql.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

/**
 * 登录校验
 */
    public User login(String keyword, String password) {
        User user = null;

        try {
            Long id = Long.parseLong(keyword);
            user = userMapper.loginById(id.toString());
        } catch (NumberFormatException e) {

        }

        if (user == null) {
            int count = userMapper.countByUsername(keyword);
            if (count > 1) {
                throw new RuntimeException("存在多个同名用户，请使用ID登录");
            }
            if (count == 1) {
                user = userMapper.loginByUsername(keyword);
            }
        }

        if (user == null) {
            log.warn("用户不存在: {}", keyword);
            return null;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("密码验证失败: {}", keyword);
            return null;
        }

        log.info("登录成功: {}", user.getUsername());
        return user;
    }

/**
 * 注册新用户
 */
    public User register(RegisterRequest request,boolean skipSystemConfig) {
        if (!systemConfigService.getSystemConfigBoolean("allow_register")
                && !skipSystemConfig) {
            throw new BusinessException(Core.ResultCode.REGISTER_CLOSED);
        }
        int minLen = systemConfigService.getSystemConfigInt("password_min_length");
        if (minLen > 0 && request.getPassword().length() < minLen) {
            throw new BusinessException(Core.ResultCode.PASSWORD_TOO_SHORT);
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (userMapper.createNewUser(user) <= 0) {
            throw new BusinessException(Core.ResultCode.SERVER_ERROR, "注册失败");
        }

        return user;
    }

/**
 * 查询全部用户
 */
    public List<User> listUsers() {
        return userMapper.listUsers();
    }

/**
 * 按字段与值分页搜索用户
 */
    public PageResult<User> searchUsersByCondition(String key, String value, int page, int size) {
        if (key.isEmpty()) return listUsersPage(page, size);
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<User> list = userMapper.searchUsersPage(key, value, offset, size);
        long total = userMapper.countSearchUsers(key, value);
        return new PageResult<>(list, total, page, size);
    }

/**
 * 用户分页查询，并对 page/size 做非法值兜底
 */
    public PageResult<User> listUsersPage(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<User> list = userMapper.listUsersPage(offset, size);
        long total = userMapper.countUsers();
        return new PageResult<>(list, total, page, size);
    }

    /**
     * 用户分页查询
     */
    public PageResult<User> listUsersPage(int page, int size, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listUsersPage(page, size);
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<User> list = userMapper.listUsersKeywordPage(keyword.trim(), offset, size);
        long total = userMapper.countUsersKeyword(keyword.trim());
        return new PageResult<>(list, total, page, size);
    }

/**
 * 按 ID 查询用户
 */
    public User getUserById(Long id) {
        return userMapper.loginById(String.valueOf(id));
    }

/**
 * 删除用户
 */
    public boolean deleteUser(Long id) {
        return userMapper.deleteUserById(id) > 0;
    }

/**
 * 更新用户信息与权限
 */
    public boolean updateUser(UpdateUserRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return userMapper.updateUser(user) > 0;
    }
}
