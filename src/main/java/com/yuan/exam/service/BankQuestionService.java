package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.AssemblePaperDto;
import com.yuan.exam.dto.BankQuestionVo;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.entity.BankQuestion;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.Question;
import com.yuan.exam.repository.BankQuestionRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 题库管理与组卷
 */
@Service
public class BankQuestionService {

    private final BankQuestionRepository bankQuestionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamService examService;

    public BankQuestionService(BankQuestionRepository bankQuestionRepository,
                               ExamRepository examRepository,
                               QuestionRepository questionRepository,
                               ExamService examService) {
        this.bankQuestionRepository = bankQuestionRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examService = examService;
    }

    @Transactional(readOnly = true)
    public Result<List<BankQuestionVo>> list(String tag) {
        List<BankQuestion> list = (tag == null || tag.isBlank())
                ? bankQuestionRepository.findAll()
                : bankQuestionRepository.findByTag(tag);
        return Result.success(list.stream().map(this::toVo).toList());
    }

    @Transactional
    public Result<BankQuestionVo> create(BankQuestionVo vo) {
        BankQuestion q = new BankQuestion();
        apply(q, vo);
        q = bankQuestionRepository.save(q);
        return Result.success(toVo(q));
    }

    @Transactional
    public Result<BankQuestionVo> update(Long id, BankQuestionVo vo) {
        Optional<BankQuestion> opt = bankQuestionRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "题库题目不存在");
        }
        BankQuestion q = opt.get();
        apply(q, vo);
        q = bankQuestionRepository.save(q);
        return Result.success(toVo(q));
    }

    @Transactional
    public Result<Void> delete(Long id) {
        if (!bankQuestionRepository.existsById(id)) {
            return Result.error(404, "题库题目不存在");
        }
        bankQuestionRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 将题库题目复制到指定考试（组卷）
     */
    @Transactional
    public Result<List<QuestionVo>> assemble(Long examId, AssemblePaperDto dto) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        if (dto.getBankQuestionIds() == null || dto.getBankQuestionIds().isEmpty()) {
            return Result.error(400, "请选择至少一道题库题目");
        }
        Exam exam = examOpt.get();
        List<QuestionVo> created = new ArrayList<>();
        for (Long bankId : dto.getBankQuestionIds()) {
            BankQuestion bq = bankQuestionRepository.findById(bankId).orElse(null);
            if (bq == null) {
                continue;
            }
            Question q = new Question();
            q.setExam(exam);
            q.setType(bq.getType());
            q.setContent(bq.getContent());
            q.setOptions(bq.getOptions());
            q.setAnswer(bq.getAnswer());
            q.setScore(bq.getScore());
            q = questionRepository.save(q);
            created.add(QuestionService.toVo(q, true));
        }
        examService.syncTotalScore(examId);
        return Result.success(created);
    }

    private void apply(BankQuestion q, BankQuestionVo vo) {
        q.setType(vo.getType());
        q.setContent(vo.getContent());
        q.setOptions(vo.getOptions());
        q.setAnswer(vo.getAnswer());
        q.setScore(vo.getScore());
        q.setTag(vo.getTag());
    }

    private BankQuestionVo toVo(BankQuestion q) {
        BankQuestionVo vo = new BankQuestionVo();
        vo.setId(q.getId());
        vo.setType(q.getType());
        vo.setContent(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setScore(q.getScore());
        vo.setTag(q.getTag());
        vo.setCreateTime(q.getCreateTime());
        return vo;
    }
}
