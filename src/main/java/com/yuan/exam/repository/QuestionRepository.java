package com.yuan.exam.repository;

import com.yuan.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 題目 Repository
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 依考試 ID 查詢所有題目
     */
    List<Question> findByExamId(Long examId);
}
