package com.yuan.exam.dto;

import com.yuan.exam.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {
    private Long id;
    private String username;
    private Role role;
    private String nickname;
    private String email;
    private Boolean enabled;
    private LocalDateTime createTime;
}
