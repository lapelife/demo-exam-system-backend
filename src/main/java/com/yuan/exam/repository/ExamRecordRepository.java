package com.yuan.exam.repository;

import com.yuan.exam.entity.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 考试作答记录 Repository
 */
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    List<ExamRecord> findByUser_IdOrderByStartTimeDesc(Long userId);

    List<ExamRecord> findByExam_IdAndUser_Id(Long examId, Long userId);

    List<ExamRecord> findByExam_IdOrderByStartTimeDesc(Long examId);

    void deleteByExam_Id(Long examId);
}
