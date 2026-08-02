package com.yuan.exam.repository;

import com.yuan.exam.entity.BankQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BankQuestionRepository extends JpaRepository<BankQuestion, Long> {

    Page<BankQuestion> findByTag(String tag, Pageable pageable);

    List<BankQuestion> findByTagOrderByIdAsc(String tag);

    List<BankQuestion> findAllByOrderByIdAsc();

    Optional<BankQuestion> findFirstByContentFp(String contentFp);

    boolean existsByContentFp(String contentFp);

    List<BankQuestion> findByContentFpIn(Collection<String> contentFps);

    List<BankQuestion> findByContentFpIsNull();
}
