package kr.go.molit.icas.com.prgrm.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_prgrm — 화면·API 단위 프로그램 정보.
 */
@Data
public class PrgrmVO {

    /** 프로그램 ID (수동 입력, PK) */
    private String        prgrmId;

    /** 시스템 구분 코드 (COM/EMP/ER/VR/SAF/PTL) */
    private String        sysSeCd;

    /** 프로그램 명칭 */
    private String        prgrmNm;

    /** 화면 URL */
    private String        prgrmUrl;

    /** API 경로 접두어 (e.g. /api/com/prgrm) */
    private String        apiPathPrefix;

    /** 프로그램 설명 */
    private String        prgrmDesc;

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
}
