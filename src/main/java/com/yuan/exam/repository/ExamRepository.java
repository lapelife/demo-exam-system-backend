package com.yuan.exam.repository;

import com.yuan.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 考试 Repository
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {
}
