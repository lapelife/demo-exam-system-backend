package com.yuan.exam.repository;

import com.yuan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 使用者 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
