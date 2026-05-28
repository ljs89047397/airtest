package kr.go.molit.icas.com.oprtr.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OprtrVO {
    private String        oprtrId;
    private String        ognzId;
    private String        icaoDesig;
    private String        oprtrNm;
    private String        oprtrNmEn;
    private String        aocNo;
    private LocalDate     aocIsueDt;
    private LocalDate     aocXprDt;
    private String        aocAthrtyNm;
    private String        parentCoNm;
    private String        lglrprNm;
    private String        lglrprCnct;
    private String        addr;
    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
