package kr.go.molit.icas.vr.ncnfrm.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 부적합·허위진술 VO — vr.tn_vr_ncnfrm 1:N 매핑.
 * PK: (vr_id, item_no)
 * ncnfrm_se_cd: MINOR / MAJOR / MISSTATEMENT
 */
@Data
public class VrNcnfrmVO {
    private String vrId;
    private Integer itemNo;
    /** 부적합 구분: MINOR / MAJOR / MISSTATEMENT */
    private String ncnfrmSeCd;
    /** 부적합 설명 */
    private String descCn;
    /** 해결 조치 내용 */
    private String resolDescCn;
    /** 해결 완료일 (null = 미해결) */
    private LocalDate resolDt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
