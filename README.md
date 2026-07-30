# 在线考试系统 - 后端

> 个人作品展示项目

## 技术栈
- Spring Boot 4.0
- Spring Data JPA
- MySQL 8.0
- Spring Security + JWT（方法级角色授权）
- Lombok

## 已完成
- [x] 后端骨架搭建
- [x] 统一返回格式 Result<T>
- [x] 全局异常处理
- [x] JPA 实体设计（User / Exam / Question / ExamRecord / AnswerRecord）
- [x] 健康检查接口
- [x] 用户登录认证（JWT）
- [x] 考试管理 CRUD（限 ADMIN/TEACHER）
- [x] 题目管理 CRUD（限 ADMIN/TEACHER）
- [x] 答题模块（开始作答 / 提交）
- [x] 自动判分（单选/判断全对给分；多选集合比对，全对才给分）
- [x] 成绩查询（我的成绩列表 + 单次明细）
- [x] 角色权限控制（@PreAuthorize + 401/403 JSON 响应）
- [x] 示范数据初始化（admin/teacher/student + Java 基础测验 6 题）

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
| POST | /api/take/start/{examId} | 开始作答 | 登录 |
| POST | /api/take/submit | 提交并判分 | 登录 |
| GET  | /api/scores | 我的成绩 | 登录 |
| GET  | /api/scores/{examRecordId} | 成绩明细 | 登录 |

## 运行
1. 建库：`CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;`
2. 修改 `application.yml` 的 MySQL 账号密码
3. 执行 `mvn spring-boot:run`
4. 访问 `http://localhost:8080/api/health`

## 测试账号
| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | ADMIN |
| teacher | 123456 | TEACHER |
| student | 123456 | STUDENT |

## 接口示例
`GET /api/health` → `{"code":200,"msg":"success","data":"ok"}`
