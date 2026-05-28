package kr.go.molit.icas.common.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> rows;
    private final int     page;
    private final int     size;
    private final long    total;
    private final int     totalPages;

    public PageResponse(List<T> rows, int page, int size, long total) {
        this.rows       = rows;
        this.page       = page;
        this.size       = size;
        this.total      = total;
        this.totalPages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);
    }
}
