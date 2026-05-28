package kr.go.molit.icas.ptl.actn.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 행위 감사 이력 VO — ptl.th_user_actn 1:1 매핑.
 * TH_ 이력 테이블 — soft delete 없음, INSERT + 조회만.
 * actn_se_cd: SUBMIT / APPROVE / REJECT / EXTRACT / CCR_EXTR / SURRENDER 등
 */
@Data
public class UserActnVO {
    /** 행위 ID — bigserial 자동 채번 */
    private Long actnId;
    /** 행위 사용자 ID */
    private String userId;
    /** 행위 구분 코드: SUBMIT / APPROVE / REJECT / EXTRACT / CCR_EXTR / SURRENDER 등 */
    private String actnSeCd;
    /** 대상 테이블 명 (예: er.tn_er) */
    private String targetTbl;
    /** 대상 PK 값 (예: ER0001) */
    private String targetPk;
    /** 행위 일시 */
    private LocalDateTime actnDt;
    /** 처리 결과 코드: SUCCESS / FAIL / FORBIDDEN */
    private String rsltCd;
    /** 클라이언트 IP 주소 */
    private String ipAddr;
    /** 사용자 에이전트 */
    private String userAgent;
    /** 비고 */
    private String rmrk;
}
