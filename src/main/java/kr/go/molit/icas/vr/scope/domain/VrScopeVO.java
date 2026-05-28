package kr.go.molit.icas.vr.scope.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VR 범위·식별 정보 VO — vr.tn_vr_scope 1:1 매핑.
 * PK: vr_id (tn_vr 와 동일)
 */
@Data
public class VrScopeVO {
    private String vrId;
    private String vrfcnInstNm;
    private String vrfcnInstAddr;
    private String vrfcnScopeCn;
    private LocalDate vrfcnStrtDt;
    private LocalDate vrfcnEndDt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
