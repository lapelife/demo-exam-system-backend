# 会话备份：在线考试系统后端骨架搭建

> 备份时间：2026-07-29 14:38 (UTC+8)
> 会话主题：搭建 `com.yuan.exam` 在线考试系统后端骨架
> 工作目录：`d:\workspace\demo-exam-system\backend`

---

## 一、需求回顾

1. 包结构：`com.yuan.exam` 下分 `controller / service / entity / repository / dto / config / common`
2. 统一返回格式 `Result<T>`（放 `common` 包），字段 `code(int)`、`msg(String)`、`data(T)`
   - 静态方法：`success()`、`success(T data)`、`error(int code, String msg)`
3. 全局异常处理 `GlobalExceptionHandler`（`@RestControllerAdvice`）
   - 处理 `Exception`、`RuntimeException`、`MethodArgumentNotValidException`
4. `application.yml` 配置：
   - `spring.datasource`：`jdbc:mysql://localhost:3306/exam_system?useSSL=false&serverTimezone=Asia/Shanghai`，`username=root`，`password=你的密码`
   - `spring.jpa`：`hibernate.ddl-auto=update`，`show-sql=true`
   - `server.port=8080`
5. JPA 实体（Lombok `@Data`）：
   - `User`：`id(Long, @GeneratedValue IDENTITY)`、`username`、`password`、`role(enum: ADMIN/TEACHER/STUDENT)`、`createTime(LocalDateTime, @CreationTimestamp)`
   - `Exam`：`id`、`name`、`duration(Integer, 分钟)`、`startTime`、`endTime`、`createTime`
   - `Question`：`id`、`examId(Long)`、`type(enum: SINGLE/MULTI/JUDGE)`、`content`、`options(String, JSON 字符串)`、`answer`、`score(Integer)`
6. 各实体对应 Repository（继承 `JpaRepository`）
7. 健康检查接口 `HealthController`：`GET /api/health` 返回 `Result.success("ok")`
8. Security 暂时关闭：空的 `SecurityConfig`，`@EnableWebSecurity`，`securityFilterChain` 全部 `permitAll`
9. 启动类 `ExamSystemApplication`
10. 全部用繁体中文注释

---

## 二、产出文件清单

### 启动类
- `src/main/java/com/yuan/exam/ExamSystemApplication.java`

### common（统一返回 + 异常处理）
- `src/main/java/com/yuan/exam/common/Result.java`
- `src/main/java/com/yuan/exam/common/GlobalExceptionHandler.java`

### entity（JPA 实体 + 枚举）
- `src/main/java/com/yuan/exam/entity/User.java`
- `src/main/java/com/yuan/exam/entity/Exam.java`
- `src/main/java/com/yuan/exam/entity/Question.java`
- `src/main/java/com/yuan/exam/entity/Role.java`
- `src/main/java/com/yuan/exam/entity/QuestionType.java`

### repository
- `src/main/java/com/yuan/exam/repository/UserRepository.java`
- `src/main/java/com/yuan/exam/repository/ExamRepository.java`
- `src/main/java/com/yuan/exam/repository/QuestionRepository.java`

### controller
- `src/main/java/com/yuan/exam/controller/HealthController.java`

### config
- `src/main/java/com/yuan/exam/config/SecurityConfig.java`

### service / dto（预留包结构）
- `src/main/java/com/yuan/exam/service/package-info.java`
- `src/main/java/com/yuan/exam/dto/package-info.java`

### 配置
- `src/main/resources/application.yml`

### 测试
- `src/test/java/com/yuan/exam/ExamSystemApplicationTests.java`

---

## 三、关键设计说明

### Result<T>
- 字段：`code`、`msg`、`data`
- 静态方法：
  - `success()` → `code=200, msg="success", data=null`
  - `success(T data)` → `code=200, msg="success", data=data`
  - `error(int code, String msg)` → 自订错误码与消息

### GlobalExceptionHandler
| 异常类型 | HTTP 状态 | 返回 code | 说明 |
|---|---|---|---|
| `Exception` | 500 | 500 | 系统异常 |
| `RuntimeException` | 500 | 500 | 运行时异常 |
| `MethodArgumentNotValidException` | 400 | 400 | 参数校验失败，聚合所有字段错误消息 |

### 实体设计重点
- `User.role`、`Question.type` 皆以 `@Enumerated(EnumType.STRING)` 持久化，避免 enum 顺序变动造成数据错乱
- `User.createTime`、`Exam.createTime` 使用 `@CreationTimestamp` 自动填入建立时间
- `Question.options` 以 JSON 字符串储存，前端自行序列化/反序列化

### SecurityConfig
- `@EnableWebSecurity`
- 关闭 CSRF
- `anyRequest().permitAll()` 全放行，后续接 JWT/登录时再补过滤链

### application.yml
- `server.port=8080`
- MySQL 连接：`jdbc:mysql://localhost:3306/exam_system?useSSL=false&serverTimezone=Asia/Shanghai`
- JPA：`ddl-auto=update`、`show-sql=true`、`format_sql=true`

---

## 四、清理动作

- 删除旧包 `com.yuan.exam_system` 下的 `ExamSystemApplication.java`
- 删除旧的 `application.yaml`（改用 `application.yml`）
- 测试类 `ExamSystemApplicationTests.java` 的 `package` 宣告由 `com.yuan.exam_system` 改为 `com.yuan.exam`

---

## 五、使用提醒

1. **MySQL 密码**：`application.yml` 中 `password: 你的密码` 需替换为实际密码，并先建立数据库 `exam_system`
2. **DDL**：`ddl-auto=update` 会自动建表，正式环境建议改 `validate` 或 `none`
3. **Security**：目前全放行，后续接鉴权时再于 `SecurityConfig` 补上
4. **启动**：`./mvnw spring-boot:run`，访问 `http://localhost:8080/api/health` 应返回：
   ```json
   {"code":200,"msg":"success","data":"ok"}
   ```

---

## 六、后续可扩展方向

- `service` 层：`UserService`、`ExamService`、`QuestionService`（接口 + 实现）
- `dto` 层：登录请求/响应、考试建立/查询、题目 CRUD 等 DTO
- `controller`：`AuthController`（登录/注册）、`ExamController`、`QuestionController`、`UserController`
- 鉴权：JWT 过滤链、密码加密（`BCryptPasswordEncoder`）
- 考试作答与判分逻辑、成绩查询
