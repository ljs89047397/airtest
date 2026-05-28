package kr.go.molit.icas.com.authrt.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * tn_sys_authrt_role_mpng DB row 1:1 매핑 VO.
 * PK = (authrt_id, role_id)
 */
@Data
public class AuthrtRoleMpngVO {

    private String        authrtId;
    private String        roleId;

    private LocalDateTime useBgngDt;
    private LocalDateTime useEndDt;
    private LocalDateTime frstRegDt;
    private String        frstRegUserId;
    private LocalDateTime lastChgDt;
    private String        lastChgUserId;

    /* JOIN 조회 시 병합 필드 (선택) */
    private String        authrtNm;
    private String        authrtDesc;
    private String        roleNm;
}
