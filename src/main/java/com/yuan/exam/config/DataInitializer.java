package com.yuan.exam.config;

import com.yuan.exam.entity.BankQuestion;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.QuestionType;
import com.yuan.exam.entity.Role;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.BankQuestionRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据初始化：应用启动时若表为空，则写入测试账号与示范考试数据
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "123456";
    private static final List<String> DEMO_USERNAMES = List.of("admin", "teacher", "student");

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final BankQuestionRepository bankQuestionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                            ExamRepository examRepository,
                            QuestionRepository questionRepository,
                            BankQuestionRepository bankQuestionRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.bankQuestionRepository = bankQuestionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUsers();
        upgradePlaintextDemoPasswords();
        initSampleExam();
        initBankQuestions();
    }

    /**
     * 初始化测试账号（密码以 BCrypt 存储）
     */
    private void initUsers() {
        if (userRepository.count() > 0) {
            log.info("User 表已有数据，跳过初始化");
            return;
        }
        userRepository.save(buildUser("admin", DEMO_PASSWORD, Role.ADMIN, "系统管理员"));
        userRepository.save(buildUser("teacher", DEMO_PASSWORD, Role.TEACHER, "测试教师"));
        userRepository.save(buildUser("student", DEMO_PASSWORD, Role.STUDENT, "测试学生"));
        log.info("已初始化测试账号：admin / teacher / student（密码均为 123456，BCrypt 存储）");
    }

    /**
     * 兼容旧库：demo 账号若仍为明文密码，升级为 BCrypt
     */
    private void upgradePlaintextDemoPasswords() {
        for (String username : DEMO_USERNAMES) {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                continue;
            }
            String password = user.getPassword();
            if (password != null && isBcryptHash(password)) {
                continue;
            }
            user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            userRepository.save(user);
            log.info("已将 demo 账号 {} 的密码升级为 BCrypt", username);
        }
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
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
        questionRepository.save(buildQuestion(exam, QuestionType.SINGLE,
                "下列哪一个是 Java 的自动装箱（Autoboxing）行为？",
                "[{\"key\":\"A\",\"text\":\"int 直接赋值给 Object\"},{\"key\":\"B\",\"text\":\"int 自动转为 Integer\"},{\"key\":\"C\",\"text\":\"String 转 int\"},{\"key\":\"D\",\"text\":\"Integer 转 String\"}]",
                "B", 20));
        questionRepository.save(buildQuestion(exam, QuestionType.SINGLE,
                "Spring Boot 默认的应用端口是？",
                "[{\"key\":\"A\",\"text\":\"80\"},{\"key\":\"B\",\"text\":\"8080\"},{\"key\":\"C\",\"text\":\"3306\"},{\"key\":\"D\",\"text\":\"8443\"}]",
                "B", 20));

        // 2 题多选（各 20 分）
        questionRepository.save(buildQuestion(exam, QuestionType.MULTI,
                "下列哪些是 Spring Boot 常用的 Starter？（多选）",
                "[{\"key\":\"A\",\"text\":\"spring-boot-starter-web\"},{\"key\":\"B\",\"text\":\"spring-boot-starter-data-jpa\"},{\"key\":\"C\",\"text\":\"spring-boot-starter-security\"},{\"key\":\"D\",\"text\":\"spring-boot-starter-windows\"}]",
                "A,B,C", 20));
        questionRepository.save(buildQuestion(exam, QuestionType.MULTI,
                "下列哪些属于 JPA 的核心接口？（多选）",
                "[{\"key\":\"A\",\"text\":\"EntityManager\"},{\"key\":\"B\",\"text\":\"CriteriaBuilder\"},{\"key\":\"C\",\"text\":\"Query\"},{\"key\":\"D\",\"text\":\"RestTemplate\"}]",
                "A,B,C", 20));

        // 2 题判断（各 10 分）
        questionRepository.save(buildQuestion(exam, QuestionType.JUDGE,
                "Java 中的 String 是不可变（immutable）对象。",
                "[{\"key\":\"A\",\"text\":\"正确\"},{\"key\":\"B\",\"text\":\"错误\"}]",
                "A", 10));
        questionRepository.save(buildQuestion(exam, QuestionType.JUDGE,
                "Spring Security 默认会开启 CSRF 防护。",
                "[{\"key\":\"A\",\"text\":\"正确\"},{\"key\":\"B\",\"text\":\"错误\"}]",
                "A", 10));

        log.info("已初始化示范考试「Java 基础测验」与 6 题题目");
    }

    private void initBankQuestions() {
        if (bankQuestionRepository.count() > 0) {
            log.info("题库已有数据，跳过初始化");
            return;
        }
        bankQuestionRepository.save(buildBank(QuestionType.SINGLE,
                "HTTP 默认端口是？",
                "[{\"key\":\"A\",\"text\":\"21\"},{\"key\":\"B\",\"text\":\"80\"},{\"key\":\"C\",\"text\":\"443\"},{\"key\":\"D\",\"text\":\"22\"}]",
                "B", 10, "网络"));
        bankQuestionRepository.save(buildBank(QuestionType.JUDGE,
                "REST 接口通常使用 JSON 作为数据交换格式。",
                "[{\"key\":\"A\",\"text\":\"正确\"},{\"key\":\"B\",\"text\":\"错误\"}]",
                "A", 10, "网络"));
        bankQuestionRepository.save(buildBank(QuestionType.MULTI,
                "下列哪些是关系型数据库？（多选）",
                "[{\"key\":\"A\",\"text\":\"MySQL\"},{\"key\":\"B\",\"text\":\"PostgreSQL\"},{\"key\":\"C\",\"text\":\"Redis\"},{\"key\":\"D\",\"text\":\"MongoDB\"}]",
                "A,B", 15, "数据库"));
        log.info("已初始化题库示范题目 3 道");
    }

    private User buildUser(String username, String rawPassword, Role role, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setNickname(nickname);
        return user;
    }

    private Question buildQuestion(Exam exam, QuestionType type, String content,
                                    String options, String answer, Integer score) {
        Question q = new Question();
        q.setExam(exam);
        q.setType(type);
        q.setContent(content);
        q.setOptions(options);
        q.setAnswer(answer);
        q.setScore(score);
        return q;
    }

    private BankQuestion buildBank(QuestionType type, String content, String options,
                                   String answer, Integer score, String tag) {
        BankQuestion q = new BankQuestion();
        q.setType(type);
        q.setContent(content);
        q.setOptions(options);
        q.setAnswer(answer);
        q.setScore(score);
        q.setTag(tag);
        return q;
    }
}
