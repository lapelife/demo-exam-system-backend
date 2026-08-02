package com.yuan.exam.util;

import com.yuan.exam.entity.QuestionType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

/**
 * 题库题目指纹与相似度：规范化题干后哈希去重，并支持近重复判定。
 */
public final class QuestionContentFingerprint {

    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.88;

    private QuestionContentFingerprint() {
    }

    public static String normalizeStem(String content) {
        if (content == null) {
            return "";
        }
        String s = Normalizer.normalize(content, Normalizer.Form.NFKC);
        s = s.toLowerCase(Locale.ROOT);
        s = s.replaceAll("[\\s\\p{Punct}\\p{IsPunctuation}·•…—–～~「」『』【】（）()《》<>\"'“”‘’、，。！？；：]+", "");
        return s;
    }

    public static String of(QuestionType type, String content) {
        String t = type == null ? "?" : type.name();
        String raw = t + "|" + normalizeStem(content);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    /**
     * 题干相似度 [0,1]：完全一致为 1；包含关系接近 1；否则为字符 bigram Jaccard。
     */
    public static double similarity(String contentA, String contentB) {
        String a = normalizeStem(contentA);
        String b = normalizeStem(contentB);
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        if (a.equals(b)) {
            return 1.0;
        }
        int minLen = Math.min(a.length(), b.length());
        if (minLen >= 8 && (a.contains(b) || b.contains(a))) {
            double ratio = (double) minLen / Math.max(a.length(), b.length());
            return Math.max(0.9, ratio);
        }
        return bigramJaccard(a, b);
    }

    public static boolean isHighlySimilar(String contentA, String contentB, double threshold) {
        return similarity(contentA, contentB) >= threshold;
    }

    private static double bigramJaccard(String a, String b) {
        Set<String> ba = bigrams(a);
        Set<String> bb = bigrams(b);
        if (ba.isEmpty() || bb.isEmpty()) {
            return 0;
        }
        int inter = 0;
        for (String x : ba) {
            if (bb.contains(x)) {
                inter++;
            }
        }
        int union = ba.size() + bb.size() - inter;
        return union == 0 ? 0 : (double) inter / union;
    }

    private static Set<String> bigrams(String s) {
        Set<String> set = new HashSet<>();
        if (s.length() < 2) {
            set.add(s);
            return set;
        }
        for (int i = 0; i < s.length() - 1; i++) {
            set.add(s.substring(i, i + 2));
        }
        return set;
    }
}
