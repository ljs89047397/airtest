package kr.go.molit.icas.er.oom.item.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OoM 점검 항목 VO — er.tn_oom_check_item 1:1 매핑 (SFR-034).
 *
 * <p>복합 PK: (oom_id, item_no). 18종 자동 검증 + 사용자 추가 항목.
 */
@Data
public class OomCheckItemVO {

    /** PK 부분 — FK → er.tn_oom_check.oom_id */
    private String oomId;

    /**
     * PK 부분 — 항목 번호.
     * 1~18 = SFR-034 정량 검증 / 100+ = 사용자 추가
     */
    private int itemNo;

    /** 항목명 (필수) */
    private String itemNm;

    /** 예상값 (varchar 100 — 숫자/문자/문장 모두 허용) */
    private String expctdVal;

    /** 보고값 (varchar 100) */
    private String rprtdVal;

    /** 편차율 (numeric 10,4) */
    private BigDecimal dvtnRate;

    /** 판정 코드: PASS / WARN / FAIL */
    private String judgCd;

    /** 비고 (text) */
    private String rmrk;

    // ── 공통 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
