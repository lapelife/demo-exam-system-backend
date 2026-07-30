package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Entity
public class User {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名称 */
    private String username;

    /** 密码 */
    private String password;

    /** 角色 */
    @Enumerated(EnumType.STRING)
    private Role role;

    /** 暱称（可空） */
    private String nickname;

    /** 电子邮件（可空） */
    private String email;

    /** 建立时间 */
    @CreationTimestamp
    private LocalDateTime createTime;
}
