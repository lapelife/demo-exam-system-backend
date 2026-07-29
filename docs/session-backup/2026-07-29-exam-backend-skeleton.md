# 會話備份：線上考試系統後端骨架搭建

> 備份時間：2026-07-29 14:38 (UTC+8)
> 會話主題：搭建 `com.yuan.exam` 線上考試系統後端骨架
> 工作目錄：`d:\workspace\demo-exam-system\backend`

---

## 一、需求回顧

1. 包結構：`com.yuan.exam` 下分 `controller / service / entity / repository / dto / config / common`
2. 統一返回格式 `Result<T>`（放 `common` 包），欄位 `code(int)`、`msg(String)`、`data(T)`
   - 靜態方法：`success()`、`success(T data)`、`error(int code, String msg)`
3. 全局異常處理 `GlobalExceptionHandler`（`@RestControllerAdvice`）
   - 處理 `Exception`、`RuntimeException`、`MethodArgumentNotValidException`
4. `application.yml` 配置：
   - `spring.datasource`：`jdbc:mysql://localhost:3306/exam_system?useSSL=false&serverTimezone=Asia/Shanghai`，`username=root`，`password=你的密碼`
   - `spring.jpa`：`hibernate.ddl-auto=update`，`show-sql=true`
   - `server.port=8080`
5. JPA 實體（Lombok `@Data`）：
   - `User`：`id(Long, @GeneratedValue IDENTITY)`、`username`、`password`、`role(enum: ADMIN/TEACHER/STUDENT)`、`createTime(LocalDateTime, @CreationTimestamp)`
   - `Exam`：`id`、`name`、`duration(Integer, 分鐘)`、`startTime`、`endTime`、`createTime`
   - `Question`：`id`、`examId(Long)`、`type(enum: SINGLE/MULTI/JUDGE)`、`content`、`options(String, JSON 字串)`、`answer`、`score(Integer)`
6. 各實體對應 Repository（繼承 `JpaRepository`）
7. 健康檢查接口 `HealthController`：`GET /api/health` 返回 `Result.success("ok")`
8. Security 暫時關閉：空的 `SecurityConfig`，`@EnableWebSecurity`，`securityFilterChain` 全部 `permitAll`
9. 啟動類 `ExamSystemApplication`
10. 全部用繁體中文註釋

---

## 二、產出檔案清單

### 啟動類
- `src/main/java/com/yuan/exam/ExamSystemApplication.java`

### common（統一返回 + 異常處理）
- `src/main/java/com/yuan/exam/common/Result.java`
- `src/main/java/com/yuan/exam/common/GlobalExceptionHandler.java`

### entity（JPA 實體 + 枚舉）
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

### service / dto（預留包結構）
- `src/main/java/com/yuan/exam/service/package-info.java`
- `src/main/java/com/yuan/exam/dto/package-info.java`

### 配置
- `src/main/resources/application.yml`

### 測試
- `src/test/java/com/yuan/exam/ExamSystemApplicationTests.java`

---

## 三、關鍵設計說明

### Result<T>
- 欄位：`code`、`msg`、`data`
- 靜態方法：
  - `success()` → `code=200, msg="success", data=null`
  - `success(T data)` → `code=200, msg="success", data=data`
  - `error(int code, String msg)` → 自訂錯誤碼與訊息

### GlobalExceptionHandler
| 異常類型 | HTTP 狀態 | 回傳 code | 說明 |
|---|---|---|---|
| `Exception` | 500 | 500 | 系統異常 |
| `RuntimeException` | 500 | 500 | 執行時期異常 |
| `MethodArgumentNotValidException` | 400 | 400 | 參數校驗失敗，聚合所有欄位錯誤訊息 |

### 實體設計重點
- `User.role`、`Question.type` 皆以 `@Enumerated(EnumType.STRING)` 持久化，避免 enum 順序變動造成資料錯亂
- `User.createTime`、`Exam.createTime` 使用 `@CreationTimestamp` 自動填入建立時間
- `Question.options` 以 JSON 字串儲存，前端自行序列化/反序列化

### SecurityConfig
- `@EnableWebSecurity`
- 關閉 CSRF
- `anyRequest().permitAll()` 全放行，後續接 JWT/登入時再補過濾鏈

### application.yml
- `server.port=8080`
- MySQL 連線：`jdbc:mysql://localhost:3306/exam_system?useSSL=false&serverTimezone=Asia/Shanghai`
- JPA：`ddl-auto=update`、`show-sql=true`、`format_sql=true`

---

## 四、清理動作

- 刪除舊包 `com.yuan.exam_system` 下的 `ExamSystemApplication.java`
- 刪除舊的 `application.yaml`（改用 `application.yml`）
- 測試類 `ExamSystemApplicationTests.java` 的 `package` 宣告由 `com.yuan.exam_system` 改為 `com.yuan.exam`

---

## 五、使用提醒

1. **MySQL 密碼**：`application.yml` 中 `password: 你的密碼` 需替換為實際密碼，並先建立資料庫 `exam_system`
2. **DDL**：`ddl-auto=update` 會自動建表，正式環境建議改 `validate` 或 `none`
3. **Security**：目前全放行，後續接鑑權時再於 `SecurityConfig` 補上
4. **啟動**：`./mvnw spring-boot:run`，訪問 `http://localhost:8080/api/health` 應回傳：
   ```json
   {"code":200,"msg":"success","data":"ok"}
   ```

---

## 六、後續可擴展方向

- `service` 層：`UserService`、`ExamService`、`QuestionService`（介面 + 實作）
- `dto` 層：登入請求/回應、考試建立/查詢、題目 CRUD 等 DTO
- `controller`：`AuthController`（登入/註冊）、`ExamController`、`QuestionController`、`UserController`
- 鑑權：JWT 過濾鏈、密碼加密（`BCryptPasswordEncoder`）
- 考試作答與判分邏輯、成績查詢
