package com.yuan.exam.repository;

import com.yuan.exam.entity.Role;
import com.yuan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRoleOrderByIdAsc(Role role);

    List<User> findByUsernameContainingIgnoreCaseOrderByIdAsc(String username);

    List<User> findByRoleAndUsernameContainingIgnoreCaseOrderByIdAsc(Role role, String username);
}
