package kr.go.molit.icas.emp.plan.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * EMP 변경 이력 VO — emp.th_emp_chg_hstry 1:1 매핑.
 *
 * <p>이력 테이블이므로 {@code use_bgng_dt / use_end_dt / frst_reg_* / last_chg_*} 컬럼 없음.
 * Append-only — DELETE/UPDATE 없음.
 *
 * <p>기록 시점:
 * <ul>
 *   <li>상태 전이 시 — {@code chg_chptr = "STATUS"}, {@code chg_cn = {"from":"...","to":"..."}}</li>
 *   <li>master 데이터 변경 시 — {@code chg_chptr = "MASTER"}, {@code chg_cn = {"reason":"..."}}</li>
 * </ul>
 */
@Data
public class EmpChgHstryVO {

    /** PK — bigserial (DB 자동 채번) */
    private Long chgHstryId;

    /** EMP Plan ID (현재 버전) */
    private String empPlanId;

    /** 이전 버전 EMP Plan ID (nullable) */
    private String prevEmpPlanId;

    /** 변경일시 */
    private LocalDateTime chgDt;

    /** 변경 챕터/섹션 (예: STATUS, MASTER) — varchar 200 */
    private String chgChptr;

    /**
     * 변경 내용 — JSON 형태 (text).
     * 상태 전이: {@code {"from":"DRAFT","to":"SBMTD"}}
     * 반려: {@code {"from":"RVWNG","to":"DRAFT","reason":"AOC 갱신 필요"}}
     * 마스터 수정: {@code {"reason":"AOC 갱신"}}
     */
    private String chgCn;

    /** 중대 변경 여부 (Y / N) */
    private String sigChgYn;

    /** 변경 수행 사용자 ID */
    private String chgUserId;
}
