package kr.go.molit.icas.com.role.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_user_role_mpng DB row 1:1 매핑 VO.
 * PK = (user_id, role_id, use_bgng_dt) — 시계열 설계.
 */
@Data
public class UserRoleMpngVO {

    private String        userId;
    private String        roleId;

    /** PK 3번째 컬럼. 부여 시작일시 (INSERT 시 NOW()로 채번). */
    private LocalDateTime useBgngDt;

    /** 유효 종료일시. 부여 시 '9999-12-31 23:59:59', 회수 시 NOW() - 1분. */
    private LocalDateTime useEndDt;

    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;

    /* JOIN 조회 시 역할 정보 병합 (선택 필드) */
    private String        roleNm;
    private String        ognzSeCdAllowed;
}
