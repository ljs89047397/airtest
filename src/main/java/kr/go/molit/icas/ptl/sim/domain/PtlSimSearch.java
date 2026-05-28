package kr.go.molit.icas.ptl.sim.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import kr.go.molit.icas.common.dto.PageRequest;

/**
 * 시뮬레이션 검색 조건 VO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PtlSimSearch extends PageRequest {
    /** 소유 사용자 ID 필터 */
    private String ownerUserId;
    /** 범위 구분 코드 필터: ALL / ORG / OPRTR */
    private String scopeSeCd;
    /** 공유 구분 코드 필터: PRIVATE / ORG / PUBLIC */
    private String shareSeCd;
    /** 기준 연도 필터 */
    private String baseYr;
}
