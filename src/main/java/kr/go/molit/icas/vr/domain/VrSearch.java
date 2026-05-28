package kr.go.molit.icas.vr.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import kr.go.molit.icas.common.dto.PageRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class VrSearch extends PageRequest {
    private String oprtrId;
    private String rprtYr;
    private String vrTypeCd;
    private String vrStCd;
    private String vrfcnInstId;
    /** VERIFIER 가시범위 — true 이면 vrfcnInstId 기준 필터 활성화 */
    private boolean verifierScope;
}
