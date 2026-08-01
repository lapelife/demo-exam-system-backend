package com.yuan.exam.controller;

import com.yuan.exam.common.PageResult;
import com.yuan.exam.common.Result;
import com.yuan.exam.dto.ResetPasswordRequest;
import com.yuan.exam.dto.UserCreateRequest;
import com.yuan.exam.dto.UserUpdateRequest;
import com.yuan.exam.dto.UserVo;
import com.yuan.exam.entity.Role;
import com.yuan.exam.service.UserManageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理（仅 ADMIN）
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserManageService userManageService;

    public UserController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    @GetMapping
    public Result<PageResult<UserVo>> list(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return userManageService.list(role, username, page, size);
    }

    @PostMapping
    public Result<UserVo> create(@Valid @RequestBody UserCreateRequest request) {
        return userManageService.create(request);
    }

    @PutMapping("/{id}")
    public Result<UserVo> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userManageService.update(id, request);
    }

    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        return userManageService.resetPassword(id, request);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return userManageService.delete(id);
    }
}
