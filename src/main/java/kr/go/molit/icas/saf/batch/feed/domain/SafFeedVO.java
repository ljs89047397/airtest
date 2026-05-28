package kr.go.molit.icas.saf.batch.feed.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * SAF 원료·제품 정보 VO — saf.tn_saf_feed (1:1 with tn_saf_batch).
 * PK: batch_id
 * fdstk_type_cd: UCO / TALLOW / PFAD / CORN_OIL / SUGARCANE / MSW / etc.
 * conv_proc_cd: HVO / ATJ / FT / SIP / CHJ / etc.
 */
@Data
public class SafFeedVO {
    private String batchId;
    /** 원료 유형: UCO / TALLOW / PFAD / CORN_OIL / SUGARCANE / MSW */
    private String fdstkTypeCd;
    /** 추가 원료 상세 설명 */
    private String addlFdstkDtl;
    /** 폐기물·잔류물 여부: Y / N */
    private String wasteResidueYn;
    /** 전환공정 코드: HVO / ATJ / FT / SIP / CHJ */
    private String convProcCd;
    /** 원산지 국가코드 목록 (쉼표 구분, 예: "KR,US,BR") */
    private String orgnCntryCds;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
