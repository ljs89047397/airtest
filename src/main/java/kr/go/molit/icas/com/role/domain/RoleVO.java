package kr.go.molit.icas.com.role.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_role DB row 1:1 매핑 VO.
 * snake_case → camelCase 자동 변환 (MyBatis mapUnderscoreToCamelCase=true)
 */
@Data
public class RoleVO {

    /** 역할 ID (의미있는 영문 코드, 예: ROLE_ADMIN) */
    private String        roleId;

    /** 역할명 */
    private String        roleNm;

    /**
     * 허용 조직구분코드 (쉼표 구분, 예: MOLIT,KOTSA).
     * MOLIT / KOTSA / AIRLINE / VERIFIER 중 하나 이상.
     */
    private String        ognzSeCdAllowed;

    /** 역할 설명 */
    private String        roleDesc;

    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;
}
