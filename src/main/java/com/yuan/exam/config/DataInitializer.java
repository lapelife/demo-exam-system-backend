package com.yuan.exam.config;

import com.yuan.exam.entity.Role;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 資料初始化：應用啟動時若 User 表為空，則寫入測試帳號
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // 已有資料則跳過
        if (userRepository.count() > 0) {
            log.info("User 表已有資料，跳過初始化");
            return;
        }

        // 插入三個測試帳號（密碼目前為明文；上線應改 BCrypt）
        userRepository.save(buildUser("admin", "123456", Role.ADMIN, "系統管理員"));
        userRepository.save(buildUser("teacher", "123456", Role.TEACHER, "測試教師"));
        userRepository.save(buildUser("student", "123456", Role.STUDENT, "測試學生"));

        log.info("已初始化測試帳號：admin / teacher / student（密碼均為 123456）");
    }

    /**
     * 建立使用者實體
     */
    private User buildUser(String username, String password, Role role, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setNickname(nickname);
        return user;
    }
}
