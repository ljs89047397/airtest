package kr.go.molit.icas.com.atrz.domain;

import java.time.LocalDateTime;

/**
 * 결재 요청 VO (com.tn_atrz_dmnd).
 * MyBatis mapUnderscoreToCamelCase=true 로 snake→camel 자동 매핑.
 */
public class AtrzDmndVO {

    /** 결재 요청 ID (자동 채번: AD + 4자리, 예: AD0001) */
    private String        atrzDmndId;

    /** 결재 업무 ID (FK → com.tn_atrz_task) */
    private String        atrzTaskId;

    /** 참조 엔터티 테이블명 (예: emp.tn_emp_plan) */
    private String        rfrncTblNm;

    /**
     * 참조 PK JSON 문자열.
     * 예: {"empPlanId":"EP0001"}
     */
    private String        rfrncKeyCn;

    /** 결재 요청 사용자 ID (FK → com.tn_user) */
    private String        dmndUserId;

    /** 결재 요청 일시 (DB default: NOW()) */
    private LocalDateTime dmndDt;

    /**
     * 결재 상태 코드.
     * PEND(대기) / INPRG(진행중) / APRVD(승인) / RJCTD(반려) / CNCLD(취소)
     */
    private String        atrzStCd;

    /** 결재 요청 제목 */
    private String        title;

    /** 결재 요청 내용 */
    private String        contents;

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
    public String        getAtrzTaskId()    { return atrzTaskId; }
    public String        getRfrncTblNm()    { return rfrncTblNm; }
    public String        getRfrncKeyCn()    { return rfrncKeyCn; }
    public String        getDmndUserId()    { return dmndUserId; }
    public LocalDateTime getDmndDt()        { return dmndDt; }
    public String        getAtrzStCd()      { return atrzStCd; }
    public String        getTitle()         { return title; }
    public String        getContents()      { return contents; }
    public LocalDateTime getUseBgngDt()     { return useBgngDt; }
    public LocalDateTime getUseEndDt()      { return useEndDt; }
    public LocalDateTime getFrstRegDt()     { return frstRegDt; }
    public String        getFrstRegUserId() { return frstRegUserId; }
    public LocalDateTime getLastChgDt()     { return lastChgDt; }
    public String        getLastChgUserId() { return lastChgUserId; }

    // ── setters ────────────────────────────────────────

    public void setAtrzDmndId(String atrzDmndId)       { this.atrzDmndId = atrzDmndId; }
    public void setAtrzTaskId(String atrzTaskId)       { this.atrzTaskId = atrzTaskId; }
    public void setRfrncTblNm(String rfrncTblNm)       { this.rfrncTblNm = rfrncTblNm; }
    public void setRfrncKeyCn(String rfrncKeyCn)       { this.rfrncKeyCn = rfrncKeyCn; }
    public void setDmndUserId(String dmndUserId)       { this.dmndUserId = dmndUserId; }
    public void setDmndDt(LocalDateTime dmndDt)        { this.dmndDt = dmndDt; }
    public void setAtrzStCd(String atrzStCd)           { this.atrzStCd = atrzStCd; }
    public void setTitle(String title)                 { this.title = title; }
    public void setContents(String contents)           { this.contents = contents; }
    public void setUseBgngDt(LocalDateTime useBgngDt)  { this.useBgngDt = useBgngDt; }
    public void setUseEndDt(LocalDateTime useEndDt)    { this.useEndDt = useEndDt; }
    public void setFrstRegDt(LocalDateTime frstRegDt)  { this.frstRegDt = frstRegDt; }
    public void setFrstRegUserId(String frstRegUserId) { this.frstRegUserId = frstRegUserId; }
    public void setLastChgDt(LocalDateTime lastChgDt)  { this.lastChgDt = lastChgDt; }
    public void setLastChgUserId(String lastChgUserId) { this.lastChgUserId = lastChgUserId; }
}
