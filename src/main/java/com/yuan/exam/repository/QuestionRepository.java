package com.yuan.exam.repository;

import com.yuan.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 題目 Repository
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {
}
