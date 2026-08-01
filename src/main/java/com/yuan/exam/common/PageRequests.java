package com.yuan.exam.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 分页参数工具（前端 page 从 1 开始）
 */
public final class PageRequests {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 500;

    private PageRequests() {
    }

    public static Pageable of(Integer page, Integer size) {
        return of(page, size, Sort.unsorted());
    }

    public static Pageable of(Integer page, Integer size, Sort sort) {
        int p = page == null || page < 1 ? DEFAULT_PAGE : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(p - 1, s, sort == null ? Sort.unsorted() : sort);
    }

    public static int page(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    public static int size(Integer size) {
        return size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }
}
