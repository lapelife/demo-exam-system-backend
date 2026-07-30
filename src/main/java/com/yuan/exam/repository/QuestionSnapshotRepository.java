package com.yuan.exam.repository;

import com.yuan.exam.entity.QuestionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionSnapshotRepository extends JpaRepository<QuestionSnapshot, Long> {

    List<QuestionSnapshot> findByExamRecord_Id(Long examRecordId);

    void deleteByExamRecord_Exam_Id(Long examId);
}
