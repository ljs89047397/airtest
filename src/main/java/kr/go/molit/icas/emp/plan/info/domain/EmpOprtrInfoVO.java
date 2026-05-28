package kr.go.molit.icas.emp.plan.info.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 운영사 식별정보 VO (emp.TN_EMP_OPRTR_INFO).
 *
 * <p>emp_plan_id 가 PK 이자 FK (→ emp.TN_EMP_PLAN). 1 plan당 1 행.
 * SFR-002 운영사 식별정보 섹션에 해당.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} 설정에 의해
 * snake_case 컬럼이 camelCase 필드로 자동 매핑된다.
 */
@Getter
@Setter
public class EmpOprtrInfoVO {

    // ── PK / FK ──────────────────────────────────────────
    /** EMP Plan ID (PK, FK → emp.tn_emp_plan) */
    private String empPlanId;

    // ── 필수 식별 정보 ────────────────────────────────────
    /** 운영사명 (한국어, 필수) */
    private String oprtrNm;

    /** 운영사명 영문 (필수) */
    private String oprtrNmEn;

    // ── 주소·대리인 ────────────────────────────────────────
    /** 주소 */
    private String addr;

    /** 법정대리인 성명 */
    private String lglrprNm;

    // ── 항공 식별 정보 ─────────────────────────────────────
    /**
     * ICAO 항공사 지정부호 (DESIGNATOR), 정확히 3자.
     * 입력 시 3자 검증 필수.
     */
    private String icaoDesig;

    /**
     * 항공기 등록기호 목록 (콤마 구분, varchar 2000).
     * 복수 등록기호를 하나의 문자열로 저장.
     */
    private String regisMarkList;

    // ── AOC 정보 ───────────────────────────────────────────
    /** AOC(항공운항증명) 번호 */
    private String aocNo;

    /** AOC 발급일 */
    private LocalDate aocIsueDt;

    /** AOC 만료일 (aocIsueDt < aocXprDt 검증) */
    private LocalDate aocXprDt;

    /** AOC 발급기관명 */
    private String aocAthrtyNm;

    // ── 지배구조 ───────────────────────────────────────────
    /** 모회사명 */
    private String parentCoNm;

    /**
     * 자회사 정보 (varchar 2000).
     * 자회사가 여러 개인 경우 서술형으로 기재.
     */
    private String sbsdryInfo;

    // ── 공통 유효구간 ──────────────────────────────────────
    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    // ── 공통 감사 컬럼 ─────────────────────────────────────
    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
