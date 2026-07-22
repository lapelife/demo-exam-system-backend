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
 * 使用者實體
 */
@Data
@Entity
public class User {

    /** 主鍵 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 使用者名稱 */
    private String username;

    /** 密碼 */
    private String password;

    /** 角色 */
    @Enumerated(EnumType.STRING)
    private Role role;

    /** 建立時間 */
    @CreationTimestamp
    private LocalDateTime createTime;
}
