package kr.go.molit.icas.com.atrz.domain;

import java.time.LocalDateTime;

/**
 * 결재 업무 마스터 VO (com.tn_atrz_task).
 * MyBatis mapUnderscoreToCamelCase=true 로 snake→camel 자동 매핑.
 */
public class AtrzTaskVO {

    /** 결재 업무 ID (의미있는 영문 코드, 예: ATZ_EMP_PLAN) */
    private String        atrzTaskId;

    /** 결재 업무명 */
    private String        atrzTaskNm;

    /** 결재 업무 설명 */
    private String        atrzTaskDesc;

    /**
     * 시스템 구분 코드.
     * 허용값: COM / EMP / ER / VR / SAF / PTL
     */
    private String        sysSeCd;

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

    public String        getAtrzTaskId()    { return atrzTaskId; }
    public String        getAtrzTaskNm()    { return atrzTaskNm; }
    public String        getAtrzTaskDesc()  { return atrzTaskDesc; }
    public String        getSysSeCd()       { return sysSeCd; }
    public LocalDateTime getUseBgngDt()     { return useBgngDt; }
    public LocalDateTime getUseEndDt()      { return useEndDt; }
    public LocalDateTime getFrstRegDt()     { return frstRegDt; }
    public String        getFrstRegUserId() { return frstRegUserId; }
    public LocalDateTime getLastChgDt()     { return lastChgDt; }
    public String        getLastChgUserId() { return lastChgUserId; }

    // ── setters ────────────────────────────────────────

    public void setAtrzTaskId(String atrzTaskId)       { this.atrzTaskId = atrzTaskId; }
    public void setAtrzTaskNm(String atrzTaskNm)       { this.atrzTaskNm = atrzTaskNm; }
    public void setAtrzTaskDesc(String atrzTaskDesc)   { this.atrzTaskDesc = atrzTaskDesc; }
    public void setSysSeCd(String sysSeCd)             { this.sysSeCd = sysSeCd; }
    public void setUseBgngDt(LocalDateTime useBgngDt)  { this.useBgngDt = useBgngDt; }
    public void setUseEndDt(LocalDateTime useEndDt)    { this.useEndDt = useEndDt; }
    public void setFrstRegDt(LocalDateTime frstRegDt)  { this.frstRegDt = frstRegDt; }
    public void setFrstRegUserId(String frstRegUserId) { this.frstRegUserId = frstRegUserId; }
    public void setLastChgDt(LocalDateTime lastChgDt)  { this.lastChgDt = lastChgDt; }
    public void setLastChgUserId(String lastChgUserId) { this.lastChgUserId = lastChgUserId; }
}
