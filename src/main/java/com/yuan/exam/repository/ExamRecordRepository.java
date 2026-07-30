package com.yuan.exam.repository;

import com.yuan.exam.entity.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 考试作答记录 Repository
 */
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    /**
     * 依学生 ID 查询所有作答记录
     */
    List<ExamRecord> findByUserIdOrderByStartTimeDesc(Long userId);

    /**
     * 依考试 ID 与学生 ID 查询作答记录（判断是否已开始/已提交）
     */
    List<ExamRecord> findByExamIdAndUserId(Long examId, Long userId);
}
