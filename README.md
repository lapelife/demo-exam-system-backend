# 在线考试系统 - 后端

> 个人作品展示项目

## 技术栈
- Spring Boot 4.0
- Spring Data JPA + MySQL 8
- Spring Security + JWT + BCrypt
- SpringDoc OpenAPI
- Lombok

## 核心能力
- 角色：ADMIN / TEACHER / STUDENT
- **用户管理（仅 ADMIN）**：新增教师/学生、编辑、禁用、重置密码、删除
- 考试与题目 CRUD；删除考试级联清理作答/快照/题目
- 题库管理 + 组卷（复制到考试）
- 学生作答：时间窗、剩余时长、草稿自动保存、超时自动交卷
- 开考题目快照锁题；判分按快照；未答计 0；满分=题目分之和
- 题目列表按角色投影（学生看不到答案）
- 教师：全员成绩、统计、试卷预览

## 角色权限（固定，不做动态权限矩阵）
| 能力 | ADMIN | TEACHER | STUDENT |
|---|---|---|---|
| 用户管理 | 是 | 否 | 否 |
| 考试/题库/成绩统计 | 是 | 是 | 否 |
| 作答 / 我的成绩 | 否 | 否 | 是 |

## 主要接口
| 方法 | 路径 | 权限 |
|---|---|---|
| POST | /api/auth/login | 公开 |
| CRUD | /api/users | ADMIN |
| POST | /api/users/{id}/reset-password | ADMIN |
| GET/POST/PUT/DELETE | /api/exams... | 读登录；写 ADMIN/TEACHER |
| GET | /api/exams/{id}/preview | ADMIN/TEACHER |
| POST | /api/exams/{id}/assemble | ADMIN/TEACHER |
| CRUD | /api/bank/questions | ADMIN/TEACHER |
| POST | /api/take/start\|draft\|submit | STUDENT |
| GET | /api/scores | 本人成绩 |
| GET | /api/scores/exam/{id}(/stats) | ADMIN/TEACHER |

Swagger：`http://localhost:8080/swagger-ui.html`

## 运行
### 本地
1. `CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;`
2. 可选环境变量：`DB_USERNAME` / `DB_PASSWORD` / `JWT_SECRET`
3. `mvn spring-boot:run`

### Docker Compose（仓库根目录）
```bash
docker compose up -d --build
```
后端：`http://localhost:8080`，MySQL：`3306`

## 测试账号
| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | ADMIN |
| teacher | 123456 | TEACHER |
| student | 123456 | STUDENT |

## 测试
```bash
mvn test
```
