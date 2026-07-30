# 在线考试系统 - 后端

> 个人作品展示项目

## 技术栈
- Spring Boot 4.0
- Spring Data JPA
- MySQL 8.0
- Spring Security + JWT（方法级角色授权）
- BCrypt 密码哈希
- Lombok

## 已完成
- [x] 后端骨架搭建
- [x] 统一返回格式 Result<T>
- [x] 全局异常处理
- [x] JPA 实体设计（User / Exam / Question / ExamRecord / AnswerRecord，含 @ManyToOne 外键）
- [x] 健康检查接口
- [x] 用户登录认证（JWT + BCrypt）
- [x] 考试管理 CRUD（限 ADMIN/TEACHER）
- [x] 题目管理 CRUD（限 ADMIN/TEACHER）
- [x] 答题模块（开始作答 / 提交，限 STUDENT）
- [x] 考试时间窗与作答时长后端校验
- [x] 自动判分（单选/判断全对给分；多选集合比对，全对才给分）
- [x] 成绩查询（我的成绩列表 + 单次明细）
- [x] 角色权限控制（@PreAuthorize + 401/403 JSON 响应）
- [x] 示范数据初始化（admin/teacher/student + Java 基础测验 6 题）
- [x] 敏感配置支持环境变量覆盖

## 接口一览
| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | /api/auth/login | 登录 | 公开 |
| GET  | /api/auth/info | 当前用户信息 | 登录 |
| GET  | /api/exams | 考试列表 | 登录 |
| GET  | /api/exams/{id} | 单一考试 | 登录 |
| POST | /api/exams | 新增考试 | ADMIN/TEACHER |
| PUT  | /api/exams/{id} | 更新考试 | ADMIN/TEACHER |
| DELETE | /api/exams/{id} | 删除考试 | ADMIN/TEACHER |
| GET  | /api/exams/{examId}/questions | 题目列表 | 登录 |
| POST | /api/exams/{examId}/questions | 新增题目 | ADMIN/TEACHER |
| PUT  | /api/exams/{examId}/questions/{qid} | 更新题目 | ADMIN/TEACHER |
| DELETE | /api/exams/{examId}/questions/{qid} | 删除题目 | ADMIN/TEACHER |
| POST | /api/take/start/{examId} | 开始作答 | STUDENT |
| POST | /api/take/submit | 提交并判分 | STUDENT |
| GET  | /api/scores | 我的成绩 | 登录 |
| GET  | /api/scores/{examRecordId} | 成绩明细 | 登录 |

## 运行
1. 建库：`CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;`
2. （可选）用环境变量覆盖敏感配置，见下表；不设则使用本地默认值
3. 执行 `mvn spring-boot:run`
4. 访问 `http://localhost:8080/api/health`

### 环境变量
| 变量 | 说明 | 本地默认 |
|---|---|---|
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | `123456` |
| `JWT_SECRET` | JWT 签名密钥 | `application.yml` 内置长字符串 |

### 安全说明
- 用户密码以 **BCrypt** 存储；登录使用 `PasswordEncoder.matches`
- 若本地库仍有明文 demo 账号（admin/teacher/student），启动时会自动升级为 BCrypt（密码仍为 `123456`）
- 作答接口仅 **STUDENT** 可调用
- 开始作答须在考试 `[startTime, endTime]` 窗口内；提交还校验是否超过 `duration` 分钟

## 测试账号
| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | ADMIN |
| teacher | 123456 | TEACHER |
| student | 123456 | STUDENT |

## 接口示例
`GET /api/health` → `{"code":200,"msg":"success","data":"ok"}`
