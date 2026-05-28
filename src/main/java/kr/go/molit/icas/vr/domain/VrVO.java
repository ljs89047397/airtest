package kr.go.molit.icas.vr.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 검증보고서 마스터 VO — vr.tn_vr 1:1 매핑.
 * UK: (oprtr_id, rprt_yr, vr_type_cd, vr_ver)
 * vr_type_cd: ER / EUCR
 * vr_st_cd:   DRAFT / SBMTD / RCMDD / APRVD
 */
@Data
public class VrVO {
    /** VR ID (채번: VR0001 ~) */
    private String vrId;
    /** 항공사 운영사 ID */
    private String oprtrId;
    /** 보고연도 (4자리) */
    private String rprtYr;
    /** VR 버전 (1부터 시작, 재작성 시 증가) */
    private Integer vrVer;
    /** VR 유형: ER / EUCR */
    private String vrTypeCd;
    /** VR 상태: DRAFT / SBMTD / RCMDD / APRVD */
    private String vrStCd;
    /** 검증기관 ID (FK → com.tn_vrfcn_inst) */
    private String vrfcnInstId;
    /** 연계 ER ID */
    private String erId;
    /** 연계 EUCR ID */
    private String eucrId;
    /** 제출일 */
    private LocalDate sbmtDt;
    /** 권고일 (KOTSA → RCMMD) */
    private LocalDate rcmmdDt;
    /** 승인일 (MOLIT → APRVD) */
    private LocalDate aprvDt;
    /** 반려 사유 */
    private String rjctRsn;
    /** 유효시작일시 */
    private LocalDateTime useBgngDt;
    /** 유효종료일시 */
    private LocalDateTime useEndDt;
    /** 최초등록일시 */
    private LocalDateTime frstRegDt;
    /** 최초등록사용자 ID */
    private String frstRegUserId;
    /** 최종변경일시 */
    private LocalDateTime lastChgDt;
    /** 최종변경사용자 ID */
    private String lastChgUserId;
}
