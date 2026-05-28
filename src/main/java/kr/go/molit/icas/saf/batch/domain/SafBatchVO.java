package kr.go.molit.icas.saf.batch.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SAF 배치 마스터 VO — saf.tn_saf_batch.
 * PK: batch_id (생산자 PoS Batch ID, 자연키 — 외부에서 받은 고유값)
 * dnsty_se_cd: DEFAULT / ACTUAL
 */
@Data
public class SafBatchVO {
    /** 배치 ID (자연키, 생산자 PoS Batch ID) */
    private String batchId;
    /** 항공사 운영사 ID */
    private String oprtrId;
    /** PoC 번호 */
    private String pocIdNo;
    /** PoC 발행일 */
    private LocalDate pocIsueDt;
    /** 수신처 회사명 */
    private String dptrRecvCoNm;
    /** SAF 수령 공항명 */
    private String safRecvArprtNm;
    /** SAF 수령 국가코드 (ISO alpha-2) */
    private String safRecvCntryCd;
    /** 관리연쇄 모델 코드 */
    private String custChnModlCd;
    /** 배치 물량 (kg) */
    private BigDecimal batchQty;
    /** 에너지 함량 (MJ/kg) */
    private BigDecimal energyCn;
    /** 밀도 구분: DEFAULT / ACTUAL */
    private String dnstySecd;
    /** 실제 밀도 (g/mL, dnsty_se_cd=ACTUAL 시 입력) */
    private BigDecimal neatSafDnsty;
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
