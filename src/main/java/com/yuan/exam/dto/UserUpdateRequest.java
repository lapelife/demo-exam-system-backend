package com.yuan.exam.dto;

import com.yuan.exam.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotNull
    private Role role;

    private String nickname;
    private String email;

    @NotNull
    private Boolean enabled;
}
