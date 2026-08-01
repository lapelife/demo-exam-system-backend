package com.yuan.exam.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        PageResult<T> result = new PageResult<>();
        result.list = list;
        result.total = total;
        result.page = page;
        result.size = size;
        return result;
    }

    public static <T> PageResult<T> from(Page<T> pageData) {
        return of(
                pageData.getContent(),
                pageData.getTotalElements(),
                pageData.getNumber() + 1,
                pageData.getSize()
        );
    }
}
