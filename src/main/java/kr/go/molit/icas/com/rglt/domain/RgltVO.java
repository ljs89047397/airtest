package kr.go.molit.icas.com.rglt.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 규정 게시판 VO — com.tn_rglt 1:1 매핑.
 * PK: rglt_id (채번: RG0001 ~)
 * rglt_se_cd: LAW / RGLTN / MNL / NTC
 */
@Data
public class RgltVO {
    /** 규정 ID — PK (채번: RG0001 ~) */
    private String rgltId;
    /** 규정 구분 코드: LAW / RGLTN / MNL / NTC */
    private String rgltSeCd;
    /** 규정 명칭 */
    private String rgltNm;
    /** 시행일 */
    private LocalDate effDt;
    /** 만료일 */
    private LocalDate expDt;
    /** 요약 */
    private String summary;
    /** 본문 HTML */
    private String contentsHtml;
    /** 유효시작일시 */
    private LocalDateTime useBgngDt;
    /** 유효종료일시 */
    private LocalDateTime useEndDt;
    /** 최초등록일시 */
    private LocalDateTime frstRegDt;
    /** 최초등록사용자 ID */
    private String frstRegUserId;
    /** 최종변경일시 */
    private LocalDateTime lastChgDt;
    /** 최종변경사용자 ID */
    private String lastChgUserId;
}
