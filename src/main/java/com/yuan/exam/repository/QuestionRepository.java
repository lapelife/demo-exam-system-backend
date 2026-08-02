package com.yuan.exam.repository;

import com.yuan.exam.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 题目 Repository
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExam_Id(Long examId);

    Page<Question> findByExam_Id(Long examId, Pageable pageable);

    void deleteByExam_Id(Long examId);
}
