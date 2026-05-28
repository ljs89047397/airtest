package kr.go.molit.icas.vr.team.domain;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 검증팀 구성원 VO — vr.tn_vr_team 1:N 매핑.
 * PK: (vr_id, team_sn)
 * role_cd: LEAD / MEMBER / INDEP_REVIEWER
 */
@Data
public class VrTeamVO {
    private String vrId;
    private Integer teamSn;
    /** 역할: LEAD / MEMBER / INDEP_REVIEWER */
    private String roleCd;
    private String userNm;
    /** 자격 상세 (자격증 번호·발급기관 등) */
    private String accrdDtl;
    /** 연속 검증 횟수 (LEAD 인 경우 중요 — 3회 초과 시 OoM 경고) */
    private Integer conscutvCnt;
    private LocalDateTime frstRegDt;
    private String frstRegUserId;
    private LocalDateTime lastChgDt;
    private String lastChgUserId;
}
