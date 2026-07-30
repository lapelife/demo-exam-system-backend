package com.yuan.exam.repository;

import com.yuan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 依用户名称查询
     *
     * @param username 用户名称
     * @return 用户实体，找不到则为 null
     */
    User findByUsername(String username);
}
