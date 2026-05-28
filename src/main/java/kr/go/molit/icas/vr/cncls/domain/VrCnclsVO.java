package kr.go.molit.icas.vr.cncls.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * VR 결론·검증 의견 VO — vr.tn_vr_cncls 1:1 매핑.
 * final_opnn_cd: REASONABLE / LIMITED / QUALIFIED / ADVERSE
 */
@Data
public class VrCnclsVO {
    private String vrId;
    /** 데이터 품질 평가 내용 */
    private String dataQltyEval;
    /** 중요성 평가 내용 */
    private String mtrltyEval;
    /** ER 결론 내용 */
    private String erCncls;
    /** EUCR 결론 내용 (nullable) */
    private String eucrCncls;
    /** 최종 판단 내용 */
    private String judgCn;
    /** 독립 검토 내용 */
    private String indepReviewCn;
    /** 독립 검토자 성명 */
    private String indepReviewUserNm;
    /** 최종 검증 의견: REASONABLE / LIMITED / QUALIFIED / ADVERSE */
    private String finalOpnnCd;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
