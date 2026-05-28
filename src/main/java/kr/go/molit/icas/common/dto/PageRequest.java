package kr.go.molit.icas.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {
    private int page = 1;        // 1-based
    private int size = 20;
    private String sort;         // 예: "useBgngDt,desc"

    public int getOffset()   { return (Math.max(page, 1) - 1) * size; }
    public int getPageSize() { return size; }

    public String getSortColumn() {
        if (sort == null || sort.isBlank()) return null;
        int comma = sort.indexOf(',');
        return comma >= 0 ? sort.substring(0, comma) : sort;
    }

    public String getSortDirection() {
        if (sort == null || sort.isBlank()) return "ASC";
        int comma = sort.indexOf(',');
        if (comma < 0) return "ASC";
        String d = sort.substring(comma + 1).trim().toUpperCase();
        return "DESC".equals(d) ? "DESC" : "ASC";
    }
}
