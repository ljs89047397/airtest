package kr.go.molit.icas.com.atrz.domain;

import java.time.LocalDateTime;

/**
 * 결재 처리 단계 VO (com.tn_atrz_prcs).
 * PK = (atrz_dmnd_id, atrz_seq).
 * MyBatis mapUnderscoreToCamelCase=true 로 snake→camel 자동 매핑.
 */
public class AtrzPrcsVO {

    /** 결재 요청 ID (복합 PK 구성, FK → com.tn_atrz_dmnd) */
    private String        atrzDmndId;

    /** 결재 순번 (복합 PK 구성, 1부터 시작) */
    private Integer       atrzSeq;

    /** 결재자 사용자 ID (FK → com.tn_user) */
    private String        atrzUserId;

    /** 결재자 역할 코드 (예: DEPT_HEAD, TEAM_LEAD) */
    private String        atrzRoleCd;

    /**
     * 결재 결과 코드.
     * null = 미처리(PENDING), APRVD = 승인, RJCTD = 반려
     */
    private String        atrzRsltCd;

    /** 결재 처리 일시 */
    private LocalDateTime atrzDt;

    /** 결재 의견 */
    private String        atrzOpnn;

    /** 유효 시작일시 */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시 */
    private LocalDateTime useEndDt;

    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String        frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String        lastChgUserId;

    // ── getters ────────────────────────────────────────

    public String        getAtrzDmndId()    { return atrzDmndId; }
    public Integer       getAtrzSeq()       { return atrzSeq; }
    public String        getAtrzUserId()    { return atrzUserId; }
    public String        getAtrzRoleCd()    { return atrzRoleCd; }
    public String        getAtrzRsltCd()    { return atrzRsltCd; }
    public LocalDateTime getAtrzDt()        { return atrzDt; }
    public String        getAtrzOpnn()      { return atrzOpnn; }
    public LocalDateTime getUseBgngDt()     { return useBgngDt; }
    public LocalDateTime getUseEndDt()      { return useEndDt; }
    public LocalDateTime getFrstRegDt()     { return frstRegDt; }
    public String        getFrstRegUserId() { return frstRegUserId; }
    public LocalDateTime getLastChgDt()     { return lastChgDt; }
    public String        getLastChgUserId() { return lastChgUserId; }

    // ── setters ────────────────────────────────────────

    public void setAtrzDmndId(String atrzDmndId)       { this.atrzDmndId = atrzDmndId; }
    public void setAtrzSeq(Integer atrzSeq)            { this.atrzSeq = atrzSeq; }
    public void setAtrzUserId(String atrzUserId)       { this.atrzUserId = atrzUserId; }
    public void setAtrzRoleCd(String atrzRoleCd)       { this.atrzRoleCd = atrzRoleCd; }
    public void setAtrzRsltCd(String atrzRsltCd)       { this.atrzRsltCd = atrzRsltCd; }
    public void setAtrzDt(LocalDateTime atrzDt)        { this.atrzDt = atrzDt; }
    public void setAtrzOpnn(String atrzOpnn)           { this.atrzOpnn = atrzOpnn; }
    public void setUseBgngDt(LocalDateTime useBgngDt)  { this.useBgngDt = useBgngDt; }
    public void setUseEndDt(LocalDateTime useEndDt)    { this.useEndDt = useEndDt; }
    public void setFrstRegDt(LocalDateTime frstRegDt)  { this.frstRegDt = frstRegDt; }
    public void setFrstRegUserId(String frstRegUserId) { this.frstRegUserId = frstRegUserId; }
    public void setLastChgDt(LocalDateTime lastChgDt)  { this.lastChgDt = lastChgDt; }
    public void setLastChgUserId(String lastChgUserId) { this.lastChgUserId = lastChgUserId; }
}
