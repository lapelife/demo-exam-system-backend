package com.yuan.exam.repository;

import com.yuan.exam.entity.AnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 單題作答記錄 Repository
 */
public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, Long> {

    /**
     * 依考試作答記錄 ID 查詢所有單題作答
     */
    List<AnswerRecord> findByExamRecordId(Long examRecordId);
}
