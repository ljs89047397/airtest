package kr.go.molit.icas.ptl.ccr.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import kr.go.molit.icas.common.dto.PageRequest;

/**
 * CCR 추출 이력 검색 조건 VO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PtlCcrExtrSearch extends PageRequest {
    /** 보고연도 필터 */
    private String rprtYr;
    /** 추출 상태 필터: INPRG / DONE / FAIL */
    private String extrStCd;
    /** 추출 요청 사용자 ID 필터 */
    private String extrUserId;
}
