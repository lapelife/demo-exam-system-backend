package com.yuan.exam.config;

import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.QuestionType;
import com.yuan.exam.entity.Role;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 資料初始化：應用啟動時若表為空，則寫入測試帳號與示範考試資料
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public DataInitializer(UserRepository userRepository,
                            ExamRepository examRepository,
                            QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initSampleExam();
    }

    /**
     * 初始化測試帳號
     */
    private void initUsers() {
        if (userRepository.count() > 0) {
            log.info("User 表已有資料，跳過初始化");
            return;
        }
        userRepository.save(buildUser("admin", "123456", Role.ADMIN, "系統管理員"));
        userRepository.save(buildUser("teacher", "123456", Role.TEACHER, "測試教師"));
        userRepository.save(buildUser("student", "123456", Role.STUDENT, "測試學生"));
        log.info("已初始化測試帳號：admin / teacher / student（密碼均為 123456）");
    }

    /**
     * 初始化示範考試與題目
     */
    private void initSampleExam() {
        if (examRepository.count() > 0) {
            log.info("Exam 表已有資料，跳過示範考試初始化");
            return;
        }

        Exam exam = new Exam();
        exam.setName("Java 基礎測驗");
        exam.setDuration(30);
        exam.setTotalScore(100);
        exam.setStartTime(LocalDateTime.now().minusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(30));
        exam = examRepository.save(exam);

        // 2 題單選（各 20 分）
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.SINGLE,
                "下列哪一個是 Java 的自動裝箱（Autoboxing）行為？",
                "[{\"key\":\"A\",\"text\":\"int 直接賦值給 Object\"},{\"key\":\"B\",\"text\":\"int 自動轉為 Integer\"},{\"key\":\"C\",\"text\":\"String 轉 int\"},{\"key\":\"D\",\"text\":\"Integer 轉 String\"}]",
                "B", 20));
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.SINGLE,
                "Spring Boot 預設的應用埠是？",
                "[{\"key\":\"A\",\"text\":\"80\"},{\"key\":\"B\",\"text\":\"8080\"},{\"key\":\"C\",\"text\":\"3306\"},{\"key\":\"D\",\"text\":\"8443\"}]",
                "B", 20));

        // 2 題多選（各 20 分）
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.MULTI,
                "下列哪些是 Spring Boot 常用的 Starter？（多選）",
                "[{\"key\":\"A\",\"text\":\"spring-boot-starter-web\"},{\"key\":\"B\",\"text\":\"spring-boot-starter-data-jpa\"},{\"key\":\"C\",\"text\":\"spring-boot-starter-security\"},{\"key\":\"D\",\"text\":\"spring-boot-starter-windows\"}]",
                "A,B,C", 20));
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.MULTI,
                "下列哪些屬於 JPA 的核心介面？（多選）",
                "[{\"key\":\"A\",\"text\":\"EntityManager\"},{\"key\":\"B\",\"text\":\"CriteriaBuilder\"},{\"key\":\"C\",\"text\":\"Query\"},{\"key\":\"D\",\"text\":\"RestTemplate\"}]",
                "A,B,C", 20));

        // 2 題判斷（各 10 分）
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.JUDGE,
                "Java 中的 String 是不可變（immutable）物件。",
                "[{\"key\":\"A\",\"text\":\"正確\"},{\"key\":\"B\",\"text\":\"錯誤\"}]",
                "A", 10));
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.JUDGE,
                "Spring Security 預設會開啟 CSRF 防護。",
                "[{\"key\":\"A\",\"text\":\"正確\"},{\"key\":\"B\",\"text\":\"錯誤\"}]",
                "A", 10));

        log.info("已初始化示範考試「Java 基礎測驗」與 6 題題目");
    }

    private User buildUser(String username, String password, Role role, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setNickname(nickname);
        return user;
    }

    private Question buildQuestion(Long examId, QuestionType type, String content,
                                    String options, String answer, Integer score) {
        Question q = new Question();
        q.setExamId(examId);
        q.setType(type);
        q.setContent(content);
        q.setOptions(options);
        q.setAnswer(answer);
        q.setScore(score);
        return q;
    }
}
