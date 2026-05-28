package kr.go.molit.icas.er.eucr.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * EUCR 검색 조건 DTO.
 *
 * <p>가시범위 (Service 에서 강제 주입):
 * <ul>
 *   <li>MOLIT / KOTSA — 전체</li>
 *   <li>AIRLINE — 본인 oprtrId</li>
 *   <li>VERIFIER — vrfcnInstId + rprtYr 배정만</li>
 * </ul>
 */
@Getter
@Setter
public class EucrSearch {

    private String oprtrId;
    private String rprtYr;
    private String eucrStCd;
    private String fulfilledYn;

    // ── 페이징 ──
    private int page     = 1;
    private int pageSize = 20;

    public int getOffset() {
        return (Math.max(page, 1) - 1) * pageSize;
    }

    // ── 가시범위 ──
    private String  vrfcnInstId;
    private boolean verifierScope = false;
}
