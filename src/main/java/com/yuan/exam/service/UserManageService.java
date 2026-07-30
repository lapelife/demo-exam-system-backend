package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.common.SecurityUtils;
import com.yuan.exam.dto.ResetPasswordRequest;
import com.yuan.exam.dto.UserCreateRequest;
import com.yuan.exam.dto.UserUpdateRequest;
import com.yuan.exam.dto.UserVo;
import com.yuan.exam.entity.Role;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户管理（仅 ADMIN）：新增学生/教师、编辑、禁用、重置密码
 */
@Service
public class UserManageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManageService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Result<List<UserVo>> list(Role role, String username) {
        List<User> users;
        boolean hasName = username != null && !username.isBlank();
        if (role != null && hasName) {
            users = userRepository.findByRoleAndUsernameContainingIgnoreCaseOrderByIdAsc(role, username.trim());
        } else if (role != null) {
            users = userRepository.findByRoleOrderByIdAsc(role);
        } else if (hasName) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrderByIdAsc(username.trim());
        } else {
            users = userRepository.findAll();
        }
        return Result.success(users.stream().map(this::toVo).toList());
    }

    @Transactional
    public Result<UserVo> create(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return Result.error(400, "用户名已存在");
        }
        if (req.getRole() == Role.ADMIN) {
            return Result.error(400, "不允许通过此接口创建管理员账号");
        }
        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setEnabled(true);
        user = userRepository.save(user);
        return Result.success(toVo(user));
    }

    @Transactional
    public Result<UserVo> update(Long id, UserUpdateRequest req) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "用户不存在");
        }
        User user = opt.get();
        String current = SecurityUtils.getCurrentUsername();
        if (user.getUsername().equals(current) && Boolean.FALSE.equals(req.getEnabled())) {
            return Result.error(400, "不能禁用当前登录账号");
        }
        if (user.getRole() == Role.ADMIN && req.getRole() != Role.ADMIN) {
            return Result.error(400, "不能降级管理员角色");
        }
        if (user.getRole() != Role.ADMIN && req.getRole() == Role.ADMIN) {
            return Result.error(400, "不允许将用户提升为管理员");
        }
        user.setRole(req.getRole());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setEnabled(req.getEnabled());
        user = userRepository.save(user);
        return Result.success(toVo(user));
    }

    @Transactional
    public Result<Void> resetPassword(Long id, ResetPasswordRequest req) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "用户不存在");
        }
        User user = opt.get();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return Result.success();
    }

    @Transactional
    public Result<Void> delete(Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "用户不存在");
        }
        User user = opt.get();
        String current = SecurityUtils.getCurrentUsername();
        if (user.getUsername().equals(current)) {
            return Result.error(400, "不能删除当前登录账号");
        }
        if (user.getRole() == Role.ADMIN) {
            return Result.error(400, "不能删除管理员账号");
        }
        userRepository.deleteById(id);
        return Result.success();
    }

    private UserVo toVo(User user) {
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setEnabled(user.getEnabled() == null || user.getEnabled());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
