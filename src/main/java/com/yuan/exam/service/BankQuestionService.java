package com.yuan.exam.service;

import com.yuan.exam.common.PageRequests;
import com.yuan.exam.common.PageResult;
import com.yuan.exam.common.Result;
import com.yuan.exam.dto.AssemblePaperDto;
import com.yuan.exam.dto.BankBatchCreateRequest;
import com.yuan.exam.dto.BankBatchCreateResponse;
import com.yuan.exam.dto.BankDedupApplyRequest;
import com.yuan.exam.dto.BankDedupApplyResponse;
import com.yuan.exam.dto.BankDedupGroupDecision;
import com.yuan.exam.dto.BankDedupScanRequest;
import com.yuan.exam.dto.BankDedupScanResponse;
import com.yuan.exam.dto.BankDuplicateCheckItemResult;
import com.yuan.exam.dto.BankDuplicateCheckRequest;
import com.yuan.exam.dto.BankDuplicateCheckResponse;
import com.yuan.exam.dto.BankQuestionVo;
import com.yuan.exam.dto.BankSimilarGroupVo;
import com.yuan.exam.dto.BankSimilarQuestionVo;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.entity.BankQuestion;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.QuestionType;
import com.yuan.exam.repository.BankQuestionRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.util.QuestionContentFingerprint;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    /** 为历史题目补全指纹，便于去重 */
    @PostConstruct
    public void backfillContentFingerprints() {
        List<BankQuestion> missing = bankQuestionRepository.findByContentFpIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (BankQuestion q : missing) {
            q.setContentFp(QuestionContentFingerprint.of(q.getType(), q.getContent()));
        }
        bankQuestionRepository.saveAll(missing);
    }

    @Transactional(readOnly = true)
    public Result<PageResult<BankQuestionVo>> list(String tag, Integer page, Integer size) {
        Pageable pageable = PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<BankQuestion> questionPage = (tag == null || tag.isBlank())
                ? bankQuestionRepository.findAll(pageable)
                : bankQuestionRepository.findByTag(tag.trim(), pageable);
        return Result.success(PageResult.of(
                questionPage.getContent().stream().map(this::toVo).toList(),
                questionPage.getTotalElements(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize()));
    }

    @Transactional
    public Result<BankQuestionVo> create(BankQuestionVo vo) {
        String fp = QuestionContentFingerprint.of(vo.getType(), vo.getContent());
        Optional<BankQuestion> dup = bankQuestionRepository.findFirstByContentFp(fp);
        if (dup.isPresent()) {
            return Result.error(409, "题库中已存在相同或等价题干的题目（ID=" + dup.get().getId() + "）");
        }
        BankQuestion q = new BankQuestion();
        apply(q, vo);
        q = bankQuestionRepository.save(q);
        return Result.success(toVo(q));
    }

    @Transactional(readOnly = true)
    public Result<BankDuplicateCheckResponse> checkDuplicates(BankDuplicateCheckRequest request) {
        List<BankQuestionVo> items = request.getItems() == null ? List.of() : request.getItems();
        Set<String> fps = new HashSet<>();
        for (BankQuestionVo item : items) {
            if (item != null && item.getContent() != null && !item.getContent().isBlank()) {
                fps.add(QuestionContentFingerprint.of(item.getType(), item.getContent()));
            }
        }
        Map<String, Long> existing = new HashMap<>();
        if (!fps.isEmpty()) {
            for (BankQuestion q : bankQuestionRepository.findByContentFpIn(fps)) {
                existing.putIfAbsent(q.getContentFp(), q.getId());
            }
        }

        BankDuplicateCheckResponse resp = new BankDuplicateCheckResponse();
        Set<String> batchSeen = new HashSet<>();
        int dupCount = 0;
        for (int i = 0; i < items.size(); i++) {
            BankQuestionVo item = items.get(i);
            BankDuplicateCheckItemResult r = new BankDuplicateCheckItemResult();
            r.setIndex(i);
            if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                r.setDuplicate(false);
                resp.getResults().add(r);
                continue;
            }
            String fp = QuestionContentFingerprint.of(item.getType(), item.getContent());
            boolean inBatch = !batchSeen.add(fp);
            Long existingId = existing.get(fp);
            r.setDuplicateInBatch(inBatch);
            r.setExistingId(existingId);
            r.setDuplicate(inBatch || existingId != null);
            if (r.isDuplicate()) {
                dupCount++;
            }
            resp.getResults().add(r);
        }
        resp.setDuplicateCount(dupCount);
        return Result.success(resp);
    }

    @Transactional
    public Result<BankBatchCreateResponse> batchCreate(BankBatchCreateRequest request) {
        List<BankQuestionVo> items = request.getItems() == null ? List.of() : request.getItems();
        boolean skip = request.isSkipDuplicates();
        BankBatchCreateResponse resp = new BankBatchCreateResponse();
        Set<String> batchSeen = new HashSet<>();
        int skipped = 0;
        for (BankQuestionVo vo : items) {
            if (vo == null || vo.getContent() == null || vo.getContent().isBlank()) {
                skipped++;
                continue;
            }
            String fp = QuestionContentFingerprint.of(vo.getType(), vo.getContent());
            if (!batchSeen.add(fp) || bankQuestionRepository.existsByContentFp(fp)) {
                if (skip) {
                    skipped++;
                    continue;
                }
                return Result.error(409, "存在重复题目，已中止批量入库：" + abbreviate(vo.getContent()));
            }
            BankQuestion q = new BankQuestion();
            apply(q, vo);
            q = bankQuestionRepository.save(q);
            resp.getSaved().add(toVo(q));
        }
        resp.setSavedCount(resp.getSaved().size());
        resp.setSkippedCount(skipped);
        return Result.success(resp);
    }

    @Transactional
    public Result<BankQuestionVo> update(Long id, BankQuestionVo vo) {
        Optional<BankQuestion> opt = bankQuestionRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "题库题目不存在");
        }
        String fp = QuestionContentFingerprint.of(vo.getType(), vo.getContent());
        Optional<BankQuestion> dup = bankQuestionRepository.findFirstByContentFp(fp);
        if (dup.isPresent() && !dup.get().getId().equals(id)) {
            return Result.error(409, "题库中已存在相同或等价题干的题目（ID=" + dup.get().getId() + "）");
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
     * 扫描题库中相似度极高的题目分组，供用户勾选保留后删除。
     */
    @Transactional(readOnly = true)
    public Result<BankDedupScanResponse> scanDuplicates(BankDedupScanRequest request) {
        boolean exactOnly = request != null && request.isExactOnly();
        double threshold = request != null && request.getThreshold() != null
                ? request.getThreshold()
                : QuestionContentFingerprint.DEFAULT_SIMILARITY_THRESHOLD;
        if (threshold < 0.5 || threshold > 1.0) {
            return Result.error(400, "相似度阈值需在 0.5～1.0 之间");
        }
        String tag = request == null ? null : request.getTag();
        List<BankQuestion> all = (tag == null || tag.isBlank())
                ? bankQuestionRepository.findAllByOrderByIdAsc()
                : bankQuestionRepository.findByTagOrderByIdAsc(tag.trim());

        final int maxScan = 3000;
        if (all.size() > maxScan) {
            return Result.error(400, "题量超过 " + maxScan + "，请先按标签筛选后再扫描");
        }

        Map<QuestionType, List<BankQuestion>> byType = new EnumMap<>(QuestionType.class);
        for (BankQuestion q : all) {
            if (q.getType() == null || q.getContent() == null || q.getContent().isBlank()) {
                continue;
            }
            byType.computeIfAbsent(q.getType(), t -> new ArrayList<>()).add(q);
        }

        BankDedupScanResponse resp = new BankDedupScanResponse();
        resp.setScannedCount(all.size());
        resp.setThreshold(threshold);
        resp.setExactOnly(exactOnly);

        int groupIndex = 1;
        int dupQuestionCount = 0;
        for (Map.Entry<QuestionType, List<BankQuestion>> entry : byType.entrySet()) {
            List<List<BankQuestion>> clusters = clusterSimilar(entry.getValue(), threshold, exactOnly);
            for (List<BankQuestion> cluster : clusters) {
                if (cluster.size() < 2) {
                    continue;
                }
                cluster.sort(Comparator.comparing(BankQuestion::getId));
                BankQuestion anchor = cluster.get(0);
                BankSimilarGroupVo group = new BankSimilarGroupVo();
                group.setGroupIndex(groupIndex++);
                group.setType(entry.getKey());
                group.setSuggestedKeepId(anchor.getId());
                double maxSim = 0;
                for (BankQuestion q : cluster) {
                    double sim = q.getId().equals(anchor.getId())
                            ? 1.0
                            : QuestionContentFingerprint.similarity(anchor.getContent(), q.getContent());
                    maxSim = Math.max(maxSim, sim);
                    BankSimilarQuestionVo item = toSimilarVo(q, sim, q.getId().equals(anchor.getId()));
                    group.getQuestions().add(item);
                }
                group.setMaxSimilarity(maxSim);
                resp.getGroups().add(group);
                dupQuestionCount += cluster.size();
            }
        }
        resp.setGroupCount(resp.getGroups().size());
        resp.setDuplicateQuestionCount(dupQuestionCount);
        return Result.success(resp);
    }

    /**
     * 按用户勾选结果删除未保留题目。每组至少保留 1 题。
     */
    @Transactional
    public Result<BankDedupApplyResponse> applyDedup(BankDedupApplyRequest request) {
        if (request == null || request.getGroups() == null || request.getGroups().isEmpty()) {
            return Result.error(400, "请提交要处理的重复分组");
        }
        Set<Long> toDelete = new HashSet<>();
        for (BankDedupGroupDecision g : request.getGroups()) {
            if (g == null) {
                continue;
            }
            List<Long> keep = g.getKeepIds() == null ? List.of() : g.getKeepIds().stream()
                    .filter(id -> id != null).distinct().toList();
            List<Long> remove = g.getRemoveIds() == null ? List.of() : g.getRemoveIds().stream()
                    .filter(id -> id != null).distinct().toList();
            if (keep.isEmpty()) {
                return Result.error(400, "每个重复组至少保留一道题");
            }
            for (Long id : keep) {
                if (remove.contains(id)) {
                    return Result.error(400, "保留与删除列表存在冲突：ID=" + id);
                }
            }
            // 安全校验：删除的题必须存在
            for (Long id : remove) {
                if (!bankQuestionRepository.existsById(id)) {
                    return Result.error(404, "待删除题目不存在：ID=" + id);
                }
                toDelete.add(id);
            }
        }
        if (toDelete.isEmpty()) {
            BankDedupApplyResponse empty = new BankDedupApplyResponse();
            empty.setDeletedCount(0);
            return Result.success(empty);
        }
        bankQuestionRepository.deleteAllById(toDelete);
        BankDedupApplyResponse resp = new BankDedupApplyResponse();
        resp.setDeletedCount(toDelete.size());
        resp.setDeletedIds(new ArrayList<>(toDelete));
        return Result.success(resp);
    }

    private List<List<BankQuestion>> clusterSimilar(List<BankQuestion> questions,
                                                    double threshold,
                                                    boolean exactOnly) {
        int n = questions.size();
        if (n < 2) {
            return List.of();
        }
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        java.util.function.IntUnaryOperator find = new java.util.function.IntUnaryOperator() {
            @Override
            public int applyAsInt(int x) {
                if (parent[x] != x) {
                    parent[x] = applyAsInt(parent[x]);
                }
                return parent[x];
            }
        };
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean similar;
                if (exactOnly) {
                    String a = QuestionContentFingerprint.normalizeStem(questions.get(i).getContent());
                    String b = QuestionContentFingerprint.normalizeStem(questions.get(j).getContent());
                    similar = !a.isEmpty() && a.equals(b);
                } else {
                    similar = QuestionContentFingerprint.isHighlySimilar(
                            questions.get(i).getContent(), questions.get(j).getContent(), threshold);
                }
                if (similar) {
                    int pi = find.applyAsInt(i);
                    int pj = find.applyAsInt(j);
                    if (pi != pj) {
                        parent[pj] = pi;
                    }
                }
            }
        }
        Map<Integer, List<BankQuestion>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            groups.computeIfAbsent(find.applyAsInt(i), k -> new ArrayList<>()).add(questions.get(i));
        }
        return groups.values().stream().filter(g -> g.size() >= 2).toList();
    }

    private BankSimilarQuestionVo toSimilarVo(BankQuestion q, double similarity, boolean suggestedKeep) {
        BankSimilarQuestionVo vo = new BankSimilarQuestionVo();
        vo.setId(q.getId());
        vo.setType(q.getType());
        vo.setContent(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setScore(q.getScore());
        vo.setTag(q.getTag());
        vo.setCreateTime(q.getCreateTime());
        vo.setSimilarity(similarity);
        vo.setSuggestedKeep(suggestedKeep);
        return vo;
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
        q.setContentFp(QuestionContentFingerprint.of(vo.getType(), vo.getContent()));
    }

    private static String abbreviate(String content) {
        String t = content == null ? "" : content.trim();
        return t.length() <= 40 ? t : t.substring(0, 40) + "…";
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
