package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.ExamVo;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 考试管理 Service：负责考试的 CRUD
 */
@Service
public class ExamService {

    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    /**
     * 列出全部考试
     */
    public Result<List<ExamVo>> list() {
        List<ExamVo> list = examRepository.findAll().stream()
                .map(this::toVo)
                .toList();
        return Result.success(list);
    }

    /**
     * 取得单一考试
     */
    public Result<ExamVo> get(Long id) {
        Optional<Exam> opt = examRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        return Result.success(toVo(opt.get()));
    }

    /**
     * 新增考试
     */
    public Result<ExamVo> create(ExamVo vo) {
        Exam exam = new Exam();
        exam.setName(vo.getName());
        exam.setDuration(vo.getDuration());
        exam.setTotalScore(vo.getTotalScore());
        exam.setStartTime(vo.getStartTime());
        exam.setEndTime(vo.getEndTime());
        exam = examRepository.save(exam);
        return Result.success(toVo(exam));
    }

    /**
     * 更新考试
     */
    public Result<ExamVo> update(Long id, ExamVo vo) {
        Optional<Exam> opt = examRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        Exam exam = opt.get();
        exam.setName(vo.getName());
        exam.setDuration(vo.getDuration());
        exam.setTotalScore(vo.getTotalScore());
        exam.setStartTime(vo.getStartTime());
        exam.setEndTime(vo.getEndTime());
        exam = examRepository.save(exam);
        return Result.success(toVo(exam));
    }

    /**
     * 删除考试（同时删除其题目由呼叫端或数据库串联处理，此处仅删考试本身）
     */
    public Result<Void> delete(Long id) {
        if (!examRepository.existsById(id)) {
            return Result.error(404, "考试不存在");
        }
        examRepository.deleteById(id);
        return Result.success();
    }

    private ExamVo toVo(Exam exam) {
        ExamVo vo = new ExamVo();
        vo.setId(exam.getId());
        vo.setName(exam.getName());
        vo.setDuration(exam.getDuration());
        vo.setTotalScore(exam.getTotalScore());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        return vo;
    }
}
