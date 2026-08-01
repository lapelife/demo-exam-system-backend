package com.yuan.exam.repository;

import com.yuan.exam.entity.Role;
import com.yuan.exam.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<User> findByRoleAndUsernameContainingIgnoreCase(Role role, String username, Pageable pageable);
}
