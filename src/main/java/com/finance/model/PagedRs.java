package com.finance.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedRs<T> {

    private List<T> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean last;
}

