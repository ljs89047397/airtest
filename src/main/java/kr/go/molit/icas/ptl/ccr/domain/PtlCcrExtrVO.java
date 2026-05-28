package kr.go.molit.icas.ptl.ccr.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * CCR 추출 이력 VO — ptl.tn_ptl_ccr_extr 1:1 매핑.
 * PK: extr_id (채번: CE0001 ~)
 * extr_st_cd: INPRG / DONE / FAIL
 */
@Data
public class PtlCcrExtrVO {
    /** 추출 ID — PK (채번: CE0001 ~) */
    private String extrId;
    /** 보고연도 (4자리) */
    private String rprtYr;
    /** 추출 범위 코드: ALL / PARTIAL */
    private String extrScopeCd;
    /** 추출 상태 코드: INPRG / DONE / FAIL */
    private String extrStCd;
    /** 완료 시 생성된 파일 ID (FK → 파일 관리 테이블) */
    private String fileId;
    /** 추출 요청 사용자 ID */
    private String extrUserId;
    /** 추출 요청 일시 */
    private LocalDateTime extrDt;
    /** 비고 */
    private String rmrk;
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
