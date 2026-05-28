package kr.go.molit.icas.com.menu.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_sys_menu — 시스템 메뉴 정보 (self-ref 트리).
 */
@Data
public class MenuVO {

    /** 메뉴 ID (수동 입력, PK) */
    private String        menuId;

    /** 시스템 구분 코드 (COM/EMP/ER/VR/SAF/PTL) */
    private String        sysSeCd;

    /** 메뉴 명칭 */
    private String        menuNm;

    /** 상위 메뉴 ID (NULL = 루트) */
    private String        upperMenuId;

    /** 정렬 순서 */
    private Integer       menuOrd;

    /** 연결 프로그램 ID (nullable — 폴더형 메뉴는 null) */
    private String        prgrmId;

    /** 아이콘 명칭 */
    private String        iconNm;

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

    // ------------------------------------------------------------------ //
    //  트리 조회용 JOIN 컬럼 (tn_prgrm LEFT JOIN)
    // ------------------------------------------------------------------ //

    /** 프로그램 화면 URL (JOIN from tn_prgrm) */
    private String        prgrmUrl;

    /** API 경로 접두어 (JOIN from tn_prgrm, 권한 필터링 기준) */
    private String        apiPathPrefix;
}
