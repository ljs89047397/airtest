package kr.go.molit.icas.com.prgrm.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_sys_authrt_prgrm_mpng — 권한-프로그램 매핑.
 * PK = (authrt_id, prgrm_id)
 */
@Data
public class AuthrtPrgrmMpngVO {

    /** 권한 ID (FK → tn_sys_authrt) */
    private String        authrtId;

    /** 프로그램 ID (FK → tn_prgrm) */
    private String        prgrmId;

    /** 조회 권한 여부 (Y/N) */
    private String        inqAuthrtYn;

    /** 입력 권한 여부 (Y/N) */
    private String        inptAuthrtYn;

    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록자 ID */
    private String        frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경자 ID */
    private String        lastChgUserId;

    // ---- 조회 시 JOIN 컬럼 ----

    /** 프로그램 명칭 (JOIN) */
    private String        prgrmNm;

    /** 시스템 구분 코드 (JOIN) */
    private String        sysSeCd;
}
