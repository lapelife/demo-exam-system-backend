package com.yuan.exam.repository;

import com.yuan.exam.entity.BankQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankQuestionRepository extends JpaRepository<BankQuestion, Long> {

    Page<BankQuestion> findByTag(String tag, Pageable pageable);
}
