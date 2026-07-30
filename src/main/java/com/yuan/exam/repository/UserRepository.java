package com.yuan.exam.repository;

import com.yuan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 使用者 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 依使用者名稱查詢
     *
     * @param username 使用者名稱
     * @return 使用者實體，找不到則為 null
     */
    User findByUsername(String username);
}
