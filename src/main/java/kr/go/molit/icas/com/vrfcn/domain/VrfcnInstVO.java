package kr.go.molit.icas.com.vrfcn.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 검증기관 VO — com.tn_vrfcn_inst 1:1 매핑 (snake → camel, mapUnderscoreToCamelCase=true).
 */
@Data
public class VrfcnInstVO {

    /** 검증기관 ID (채번 형식: VI0001 ~ VI9999) */
    private String        vrfcnInstId;

    /** 조직 ID (FK → com.tn_ognz) */
    private String        ognzId;

    /** 검증기관명 (한글) */
    private String        vrfcnInstNm;

    /** 검증기관명 (영문) */
    private String        vrfcnInstNmEn;

    /** 주소 */
    private String        addr;

    /** ICAO CCR 공인 여부 (Y/N) */
    private String        icaoCcrAccrdYn;

    /** ICAO CCR 공인 번호 */
    private String        icaoCcrAccrdNo;

    /** ICAO CCR 공인 만료일 */
    private LocalDate     icaoCcrAccrdXprDt;

    /** 유효시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효종료일시 */
    private LocalDateTime useEndDt;

    /** 최초등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초등록사용자 ID */
    private String        frstRegUserId;

    /** 최종변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종변경사용자 ID */
    private String        lastChgUserId;
}
