package kr.go.molit.icas.vr.inpt.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 검증 입력자료 VO — vr.tn_vr_input_info 1:N 매핑.
 * PK: (vr_id, input_sn)
 */
@Data
public class VrInputInfoVO {
    private String vrId;
    private Integer inputSn;
    private String docNm;
    /** 자료 구분 코드 (공통코드: VR_DOC_SE_CD) */
    private String docSeCd;
    /** 첨부파일 ID (FK → com.tn_file, nullable) */
    private String fileId;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
