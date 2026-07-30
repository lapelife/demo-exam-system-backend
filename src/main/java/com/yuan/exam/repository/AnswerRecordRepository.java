package com.yuan.exam.repository;

import com.yuan.exam.entity.AnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 单题作答记录 Repository
 */
public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, Long> {

    /**
     * 依考试作答记录 ID 查询所有单题作答
     */
    List<AnswerRecord> findByExamRecord_Id(Long examRecordId);
}
