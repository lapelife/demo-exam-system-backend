package com.yuan.exam.repository;

import com.yuan.exam.entity.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 考試作答記錄 Repository
 */
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    /**
     * 依學生 ID 查詢所有作答記錄
     */
    List<ExamRecord> findByUserIdOrderByStartTimeDesc(Long userId);

    /**
     * 依考試 ID 與學生 ID 查詢作答記錄（判斷是否已開始/已提交）
     */
    List<ExamRecord> findByExamIdAndUserId(Long examId, Long userId);
}
