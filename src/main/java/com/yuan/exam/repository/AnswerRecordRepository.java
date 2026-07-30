package com.yuan.exam.repository;

import com.yuan.exam.entity.AnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 单题作答记录 Repository
 */
public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, Long> {

    List<AnswerRecord> findByExamRecord_Id(Long examRecordId);

    Optional<AnswerRecord> findByExamRecord_IdAndQuestion_Id(Long examRecordId, Long questionId);

    void deleteByExamRecord_Exam_Id(Long examId);
}
