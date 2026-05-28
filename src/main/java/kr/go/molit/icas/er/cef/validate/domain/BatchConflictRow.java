package kr.go.molit.icas.er.cef.validate.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 이중청구 교차 스캔 결과 한 행 (CEF↔CEF 또는 CEF↔SAF).
 *
 * <p>Mapper 가 동일 batch_id 를 사용하는 외부 청구건/배치를 행 단위로 반환.
 * Service 단에서 운영사 일치 여부로 경고/차단 정책 적용.
 */
@Data
public class BatchConflictRow {

    /**
     * 출처 유형.
     * 허용값: {@code CEF} (er.tn_cef_claim) / {@code SAF} (saf.tn_saf_batch)
     */
    private String sourceType;

    /** CEF: cef_id / SAF: batch_id */
    private String sourceId;

    /** CEF: claim_no / SAF: null */
    private String claimNo;

    /** 운영사 ID (소유자) */
    private String oprtrId;

    /** 운영사명 (JOIN 조회) */
    private String oprtrNm;

    /** 보고연도 (CEF). SAF 는 null */
    private String rprtYr;

    /** 충돌 mass (CEF: pure_fuel_mass, SAF: batch_qty) */
    private BigDecimal mass;
}
