package com.yuan.exam.repository;

import com.yuan.exam.entity.BankQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankQuestionRepository extends JpaRepository<BankQuestion, Long> {

    List<BankQuestion> findByTag(String tag);
}
