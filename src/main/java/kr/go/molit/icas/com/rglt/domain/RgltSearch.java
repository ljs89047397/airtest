package kr.go.molit.icas.com.rglt.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import kr.go.molit.icas.common.dto.PageRequest;

/**
 * 규정 게시판 검색 조건 VO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RgltSearch extends PageRequest {
    /** 규정 구분 코드 필터: LAW / RGLTN / MNL / NTC */
    private String rgltSeCd;
    /** 규정 명칭 키워드 (LIKE 검색) */
    private String keyword;
}
