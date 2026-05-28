package kr.go.molit.icas.emp.plan.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * EMP Plan 검색 조건 DTO.
 *
 * <p>가시범위 적용:
 * <ul>
 *   <li>MOLIT / KOTSA — 전체</li>
 *   <li>AIRLINE — 본인 {@code oprtrId} 강제 주입</li>
 *   <li>VERIFIER — {@code vrfcnInstId} + {@code rprtYr} 로 배정 운영사만</li>
 * </ul>
 */
@Getter
@Setter
public class EmpPlanSearch {

    /** 운영사 ID 필터 (AIRLINE 은 서비스에서 강제 주입) */
    private String oprtrId;

    /** 보고연도 필터 (예: 2026) */
    private String rprtYr;

    /** EMP 상태 코드 필터 (DRAFT / SBMTD / RVWNG / RCMDD / APRVD / CNCLD) */
    private String empStCd;

    /** 중대 변경 여부 필터 (Y / N) */
    private String sigChgYn;

    // ── 페이징 ──
    private int page     = 1;    // 1-based
    private int pageSize = 20;

    public int getOffset() {
        return (Math.max(page, 1) - 1) * pageSize;
    }

    // ── 가시범위 내부 파라미터 (서비스에서 주입, 요청 파라미터로 노출하지 않음) ──

    /** VERIFIER 가시범위: 검증기관 ID */
    private String vrfcnInstId;

    /** VERIFIER 가시범위 모드 활성 여부 */
    private boolean verifierScope = false;
}
