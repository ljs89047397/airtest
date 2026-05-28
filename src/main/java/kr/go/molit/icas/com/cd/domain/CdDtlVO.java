package kr.go.molit.icas.com.cd.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CdDtlVO {
    private String        grpId;
    private String        cd;
    private String        cdNm;
    private String        cdDesc;
    private Integer       cdOrd;
    private String        cdAttr1;
    private String        cdAttr2;
    private String        cdAttr3;
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
