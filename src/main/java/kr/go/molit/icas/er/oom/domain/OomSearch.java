package kr.go.molit.icas.er.oom.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * OoM-check 검색 조건.
 *
 * <p>가시범위:
 * <ul>
 *   <li>MOLIT / KOTSA — 전체</li>
 *   <li>AIRLINE — 본인 oprtrId</li>
 *   <li>VERIFIER — vrfcn_assgn 배정 운영사만</li>
 * </ul>
 */
@Getter
@Setter
public class OomSearch {

    private String oprtrId;
    private String rprtYr;
    private String oomStCd;
    private String oomRsltCd;

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
