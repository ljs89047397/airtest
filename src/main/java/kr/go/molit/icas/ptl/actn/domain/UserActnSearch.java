package kr.go.molit.icas.ptl.actn.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import kr.go.molit.icas.common.dto.PageRequest;

import java.time.LocalDate;

/**
 * 사용자 행위 감사 이력 검색 조건 VO.
 * fromDt / toDt 는 actn_dt 범위 조건에 사용.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserActnSearch extends PageRequest {
    /** 사용자 ID 필터 */
    private String userId;
    /** 행위 구분 코드 필터 */
    private String actnSeCd;
    /** 대상 테이블 필터 */
    private String targetTbl;
    /** 대상 PK 필터 */
    private String targetPk;
    /** 행위 일시 시작 (포함) */
    private LocalDate fromDt;
    /** 행위 일시 종료 (포함) */
    private LocalDate toDt;
}
