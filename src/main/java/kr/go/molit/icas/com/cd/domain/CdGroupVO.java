package kr.go.molit.icas.com.cd.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CdGroupVO {
    private String        grpId;
    private String        grpNm;
    private String        grpDesc;
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
