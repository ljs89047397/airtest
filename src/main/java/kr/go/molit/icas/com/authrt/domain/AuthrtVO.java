package kr.go.molit.icas.com.authrt.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_sys_authrt DB row 1:1 매핑 VO.
 * snake_case → camelCase 자동 변환 (MyBatis mapUnderscoreToCamelCase=true)
 */
@Data
public class AuthrtVO {

    /** 권한 ID (의미있는 영문 코드, 예: AUTHRT_USER_MGMT) */
    private String        authrtId;

    /** 권한명 */
    private String        authrtNm;

    /** 권한 설명 */
    private String        authrtDesc;

    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
