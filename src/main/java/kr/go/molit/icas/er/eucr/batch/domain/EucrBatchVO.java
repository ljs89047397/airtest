package kr.go.molit.icas.er.eucr.batch.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EUCR 배출권 배치 VO — er.tn_eucr_batch 1:1 매핑 (SFR-031).
 *
 * <p>복합 PK: (eucr_id, batch_no). 일련번호는 범위(from/to) 또는 상세 테이블에 N행 등록.
 */
@Data
public class EucrBatchVO {

    /** PK 부분 — FK → er.tn_eucr.eucr_id */
    private String eucrId;

    /** PK 부분 — 배치 번호 (사용자 입력, 최대 50자) */
    private String batchNo;

    /** 배출권 유형 코드 (FK → com 공통코드 CRDT_TYPE_CD, 필수) */
    private String crdtTypeCd;

    /** 취소 수량 (필수, numeric 20,4) */
    private BigDecimal subQty;

    /** 프로그램명 (Verra/Gold Standard 등) */
    private String prgrmNm;

    /** 빈티지 연도 (char 4) */
    private String vntgYr;

    /** 방법론 ID */
    private String mthdlgyId;

    /** 일련번호 시작 (범위 입력 모드) */
    private String crdtNoFrom;

    /** 일련번호 끝 (범위 입력 모드) */
    private String crdtNoTo;

    /** 취소일자 */
    private LocalDate cnclDt;

    // ── 공통 ──
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
