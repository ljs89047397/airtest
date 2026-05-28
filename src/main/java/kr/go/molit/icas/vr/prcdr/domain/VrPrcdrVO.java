package kr.go.molit.icas.vr.prcdr.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 검증 절차·분석 VO — vr.tn_vr_prcdr 1:1 매핑.
 */
@Data
public class VrPrcdrVO {
    private String vrId;
    /** 전략적 분석 내용 */
    private String strgAnlysCn;
    /** 리스크 평가 내용 */
    private String riskEvalCn;
    /** 샘플링 활동 내용 */
    private String smplngActvCn;
    /** 샘플링 결과 내용 */
    private String smplngRsltCn;
    /** EMP 준수 내용 */
    private String empComplCn;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
