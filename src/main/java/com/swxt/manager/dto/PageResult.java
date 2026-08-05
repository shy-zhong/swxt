package com.swxt.manager.dto;

import lombok.Data;

import java.util.List;


@Data
public class PageResult<T> {

    
    private List<T> list;

    
    private long total;

    
    private int page;

    
    private int size;

    
    private int totalPages;

/**
 * 构造分页结果，并根据 total 与 size 计算总页数
 */
    public PageResult(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
    }
    public PageResult() {

    }
}
