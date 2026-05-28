package kr.go.molit.icas.er.eucr.crdt.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * EUCR 일련번호 상세 VO — er.tn_eucr_crdt_dtl 1:1 매핑.
 *
 * <p>복합 PK: (eucr_id, crdt_no). 전역 UK: crdt_no (이중사용 DB 레벨 차단).
 * batch 와 (eucr_id, batch_no) FK 로 연결.
 */
@Data
public class EucrCrdtDtlVO {

    /** PK 부분 — FK → er.tn_eucr.eucr_id */
    private String eucrId;

    /** PK 부분 + 전역 UK — 일련번호 (최대 100자) */
    private String crdtNo;

    /** FK 부분 — 소속 배치 번호 */
    private String batchNo;

    /** 방법론 ID (배치에서 상속 가능) */
    private String mthdlgyId;

    /** 빈티지 연도 */
    private String vntgYr;

    // ── 공통 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
