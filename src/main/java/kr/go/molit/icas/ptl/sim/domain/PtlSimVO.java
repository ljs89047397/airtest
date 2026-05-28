package kr.go.molit.icas.ptl.sim.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 시뮬레이션 VO — ptl.tn_ptl_sim 1:1 매핑.
 * PK: sim_id (채번: SM0001 ~)
 * jsonb 컬럼(input_json, rslt_json)은 String으로 처리.
 * share_se_cd: PRIVATE / ORG / PUBLIC
 */
@Data
public class PtlSimVO {
    /** 시뮬레이션 ID — PK (채번: SM0001 ~) */
    private String simId;
    /** 시뮬레이션 명칭 */
    private String simNm;
    /** 소유 사용자 ID (FK → com.tn_user) */
    private String ownerUserId;
    /** 범위 구분 코드: ALL / ORG / OPRTR */
    private String scopeSeCd;
    /** 범위 대상 운영사 ID (scopeSeCd=OPRTR 시) */
    private String scopeOprtrId;
    /** 기준 연도 */
    private String baseYr;
    /** 예측 시작 연도 */
    private String prdctnYrFrom;
    /** 예측 종료 연도 */
    private String prdctnYrTo;
    /** 사용자 입력 시나리오 파라미터 (jsonb → String) */
    private String inputJson;
    /** 계산 결과 JSON (jsonb → String) */
    private String rsltJson;
    /** 공유 구분 코드: PRIVATE / ORG / PUBLIC */
    private String shareSeCd;
    /** 유효시작일시 */
    private LocalDateTime useBgngDt;
    /** 유효종료일시 */
    private LocalDateTime useEndDt;
    /** 최초등록일시 */
    private LocalDateTime frstRegDt;
    /** 최초등록사용자 ID */
    private String frstRegUserId;
    /** 최종변경일시 */
    private LocalDateTime lastChgDt;
    /** 최종변경사용자 ID */
    private String lastChgUserId;
}
