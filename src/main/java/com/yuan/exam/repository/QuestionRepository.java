package com.yuan.exam.repository;

import com.yuan.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 题目 Repository
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExam_Id(Long examId);

    void deleteByExam_Id(Long examId);
}
