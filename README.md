# 線上考試系統 - 後端

> 個人作品展示專案

## 技術棧
- Spring Boot 4.0
- Spring Data JPA
- MySQL 8.0
- Spring Security + JWT（方法級角色授權）
- Lombok

## 已完成
- [x] 後端骨架搭建
- [x] 統一返回格式 Result<T>
- [x] 全局異常處理
- [x] JPA 實體設計（User / Exam / Question / ExamRecord / AnswerRecord）
- [x] 健康檢查接口
- [x] 用戶登入認證（JWT）
- [x] 考試管理 CRUD（限 ADMIN/TEACHER）
- [x] 題目管理 CRUD（限 ADMIN/TEACHER）
- [x] 答題模組（開始作答 / 提交）
- [x] 自動判分（單選/判斷全對給分；多選集合比對，全對才給分）
- [x] 成績查詢（我的成績列表 + 單次明細）
- [x] 角色權限控制（@PreAuthorize + 401/403 JSON 回應）
- [x] 示範資料初始化（admin/teacher/student + Java 基礎測驗 6 題）

## 接口一覽
| 方法 | 路徑 | 說明 | 權限 |
|---|---|---|---|
| POST | /api/auth/login | 登入 | 公開 |
| GET  | /api/auth/info | 當前使用者資訊 | 登入 |
| GET  | /api/exams | 考試列表 | 登入 |
| GET  | /api/exams/{id} | 單一考試 | 登入 |
| POST | /api/exams | 新增考試 | ADMIN/TEACHER |
| PUT  | /api/exams/{id} | 更新考試 | ADMIN/TEACHER |
| DELETE | /api/exams/{id} | 刪除考試 | ADMIN/TEACHER |
| GET  | /api/exams/{examId}/questions | 題目列表 | 登入 |
| POST | /api/exams/{examId}/questions | 新增題目 | ADMIN/TEACHER |
| PUT  | /api/exams/{examId}/questions/{qid} | 更新題目 | ADMIN/TEACHER |
| DELETE | /api/exams/{examId}/questions/{qid} | 刪除題目 | ADMIN/TEACHER |
| POST | /api/take/start/{examId} | 開始作答 | 登入 |
| POST | /api/take/submit | 提交並判分 | 登入 |
| GET  | /api/scores | 我的成績 | 登入 |
| GET  | /api/scores/{examRecordId} | 成績明細 | 登入 |

## 運行
1. 建庫：`CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;`
2. 修改 `application.yml` 的 MySQL 帳號密碼
3. 執行 `mvn spring-boot:run`
4. 訪問 `http://localhost:8080/api/health`

## 測試帳號
| 帳號 | 密碼 | 角色 |
|---|---|---|
| admin | 123456 | ADMIN |
| teacher | 123456 | TEACHER |
| student | 123456 | STUDENT |

## 接口示例
`GET /api/health` → `{"code":200,"msg":"success","data":"ok"}`
