package kr.go.molit.icas.vr.time.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 검증 시간 VO — vr.tn_vr_time 1:1 매핑.
 * total_hrs = onsite_hrs + offsite_hrs (서비스에서 자동 계산).
 */
@Data
public class VrTimeVO {
    private String vrId;
    /** 현장 검증 시간 (시간 단위) */
    private BigDecimal onsiteHrs;
    /** 비현장(원격) 검증 시간 */
    private BigDecimal offsiteHrs;
    /** 총 시간 (자동 합산) */
    private BigDecimal totalHrs;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
