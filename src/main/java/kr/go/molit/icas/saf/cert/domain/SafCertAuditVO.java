package kr.go.molit.icas.saf.cert.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * SAF 인증서 감사 추적 VO — saf.tn_saf_cert_audit 1:N 매핑.
 * PK: (cert_id, audit_sn)
 * actn_cd: UPLD(업로드) / SRND(회수) / VIEW(조회)
 */
@Data
public class SafCertAuditVO {
    private String certId;
    private Integer auditSn;
    /** 행위 코드: UPLD / SRND / VIEW */
    private String actnCd;
    private String userId;
    private LocalDateTime actnDt;
}
