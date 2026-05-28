package kr.go.molit.icas.com.ognz.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * tn_ognz 테이블 1:1 매핑 VO.
 * mapUnderscoreToCamelCase=true 적용으로 snake → camel 자동 변환.
 */
@Getter
@Setter
public class OgnzVO {

    /** 기관 ID (수동 입력 — 'MOLIT', 'KOTSA' 등 의미 있는 코드) */
    private String ognzId;

    /** 기관 구분 코드: MOLIT / KOTSA / AIRLINE / VERIFIER */
    private String ognzSeCd;

    /** 기관명 (국문) */
    private String ognzNm;

    /** 기관명 (영문) */
    private String ognzNmEn;

    /** 사업자등록번호 (숫자 10자리, 선택) */
    private String bizNo;

    /** 주소 */
    private String addr;

    /** 대표자 명 */
    private String rprstvNm;

    /** 대표자 연락처 */
    private String rprstvCnct;

    /** 비고 */
    private String rmrk;

    /** 사용 시작일 */
    private LocalDateTime useBgngDt;

    /** 사용 종료일 */
    private LocalDateTime useEndDt;

    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
