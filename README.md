# 線上考試系統 - 後端

> 個人作品展示專案 · 持續開發中

## 技術棧
- Spring Boot 3.2
- Spring Data JPA
- MySQL 8.0
- Spring Security（規劃中）
- Lombok

## 已完成
- [x] 後端骨架搭建
- [x] 統一返回格式 Result<T>
- [x] 全局異常處理
- [x] JPA 實體設計（User / Exam / Question）
- [x] 健康檢查接口

## 進行中
- [ ] 用戶登入認證（JWT）
- [ ] 考試管理 CRUD
- [ ] 題目管理 CRUD

## 規劃
- [ ] 答題模組
- [ ] 自動判分
- [ ] 成績查詢
- [ ] 防作弊機制

## 運行
1. 建庫：`CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;`
2. 修改 `application.yml` 的 MySQL 帳號密碼
3. 執行 `mvn spring-boot:run`
4. 訪問 `http://localhost:8080/api/health`

## 接口示例
`GET /api/health` → `{"code":200,"msg":"success","data":"ok"}`