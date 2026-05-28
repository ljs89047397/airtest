package kr.go.molit.icas.er.cef.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * CEF 목록 검색 조건 DTO.
 *
 * <p>가시범위 적용 (Service 에서 강제 주입):
 * <ul>
 *   <li>MOLIT / KOTSA — 전체</li>
 *   <li>AIRLINE — 본인 oprtrId</li>
 *   <li>VERIFIER — vrfcnInstId + rprtYr 로 배정 운영사만</li>
 * </ul>
 */
@Getter
@Setter
public class CefSearch {

    private String oprtrId;
    private String rprtYr;
    private String cefStCd;

    // ── 페이징 ──
    private int page     = 1;
    private int pageSize = 20;

    public int getOffset() {
        return (Math.max(page, 1) - 1) * pageSize;
    }

    // ── 가시범위 (서비스 주입, 외부 노출 X) ──
    private String  vrfcnInstId;
    private boolean verifierScope = false;
}
