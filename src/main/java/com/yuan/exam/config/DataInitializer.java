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
 * 数据初始化：应用启动时若表为空，则写入测试账号与示范考试数据
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
     * 初始化测试账号
     */
    private void initUsers() {
        if (userRepository.count() > 0) {
            log.info("User 表已有数据，跳过初始化");
            return;
        }
        userRepository.save(buildUser("admin", "123456", Role.ADMIN, "系统管理员"));
        userRepository.save(buildUser("teacher", "123456", Role.TEACHER, "测试教师"));
        userRepository.save(buildUser("student", "123456", Role.STUDENT, "测试学生"));
        log.info("已初始化测试账号：admin / teacher / student（密码均为 123456）");
    }

    /**
     * 初始化示范考试与题目
     */
    private void initSampleExam() {
        if (examRepository.count() > 0) {
            log.info("Exam 表已有数据，跳过示范考试初始化");
            return;
        }

        Exam exam = new Exam();
        exam.setName("Java 基础测验");
        exam.setDuration(30);
        exam.setTotalScore(100);
        exam.setStartTime(LocalDateTime.now().minusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(30));
        exam = examRepository.save(exam);

        // 2 题单选（各 20 分）
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.SINGLE,
                "下列哪一个是 Java 的自动装箱（Autoboxing）行为？",
                "[{\"key\":\"A\",\"text\":\"int 直接赋值给 Object\"},{\"key\":\"B\",\"text\":\"int 自动转为 Integer\"},{\"key\":\"C\",\"text\":\"String 转 int\"},{\"key\":\"D\",\"text\":\"Integer 转 String\"}]",
                "B", 20));
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.SINGLE,
                "Spring Boot 默认的应用端口是？",
                "[{\"key\":\"A\",\"text\":\"80\"},{\"key\":\"B\",\"text\":\"8080\"},{\"key\":\"C\",\"text\":\"3306\"},{\"key\":\"D\",\"text\":\"8443\"}]",
                "B", 20));

        // 2 题多选（各 20 分）
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.MULTI,
                "下列哪些是 Spring Boot 常用的 Starter？（多选）",
                "[{\"key\":\"A\",\"text\":\"spring-boot-starter-web\"},{\"key\":\"B\",\"text\":\"spring-boot-starter-data-jpa\"},{\"key\":\"C\",\"text\":\"spring-boot-starter-security\"},{\"key\":\"D\",\"text\":\"spring-boot-starter-windows\"}]",
                "A,B,C", 20));
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.MULTI,
                "下列哪些属于 JPA 的核心接口？（多选）",
                "[{\"key\":\"A\",\"text\":\"EntityManager\"},{\"key\":\"B\",\"text\":\"CriteriaBuilder\"},{\"key\":\"C\",\"text\":\"Query\"},{\"key\":\"D\",\"text\":\"RestTemplate\"}]",
                "A,B,C", 20));

        // 2 题判断（各 10 分）
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.JUDGE,
                "Java 中的 String 是不可变（immutable）对象。",
                "[{\"key\":\"A\",\"text\":\"正确\"},{\"key\":\"B\",\"text\":\"错误\"}]",
                "A", 10));
        questionRepository.save(buildQuestion(exam.getId(), QuestionType.JUDGE,
                "Spring Security 默认会开启 CSRF 防护。",
                "[{\"key\":\"A\",\"text\":\"正确\"},{\"key\":\"B\",\"text\":\"错误\"}]",
                "A", 10));

        log.info("已初始化示范考试「Java 基础测验」与 6 题题目");
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
