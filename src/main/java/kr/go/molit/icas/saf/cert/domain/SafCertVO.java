package kr.go.molit.icas.saf.cert.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SAF 인증서 VO — saf.tn_saf_cert 1:1 매핑.
 * PK: cert_id (SC0001 ~)
 * cert_type_cd: PoS / PoC
 * cert_schm_cd: ISCC / RSB / ISCC_PLUS / RSPO / etc.
 * srnd_yn: 회수 여부 Y/N (default N)
 */
@Data
public class SafCertVO {
    /** 인증서 ID (채번: SC0001 ~) */
    private String certId;
    /** 항공사 운영사 ID */
    private String oprtrId;
    /** SAF 배치 ID (saf.tn_saf_batch.batch_id FK) */
    private String batchId;
    /** 인증서 유형: PoS / PoC */
    private String certTypeCd;
    /** 인증 체계: ISCC / RSB / ISCC_PLUS / RSPO */
    private String certSchmCd;
    /** 인증서 번호 (암호화 대상) */
    private String certNo;
    /** 첨부파일 ID (FK → com.tn_file) */
    private String fileId;
    /** 회수 여부: Y / N */
    private String srndYn;
    /** 회수일 */
    private LocalDate srndDt;
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
