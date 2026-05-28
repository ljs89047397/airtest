package kr.go.molit.icas.com.user.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * tn_user 테이블 1:1 매핑 VO.
 * mapUnderscoreToCamelCase=true 적용으로 snake → camel 자동 변환.
 *
 * 개인정보(userNm, emlAddr, mblphnNo)는 이번 라운드 평문 저장.
 * 응답 시 Service 단에서 마스킹 처리 또는 pswdHash null 설정 필수.
 */
@Getter
@Setter
public class UserVO {

    /** 사용자 ID (수동 입력, PK) */
    private String userId;

    /** 사용자명 (개인정보 — 응답 시 마스킹 대상) */
    private String userNm;

    /**
     * 비밀번호 해시 (SHA-256, 64자).
     * 응답 시 절대 노출 금지 — Service 에서 반드시 null 처리.
     */
    private String pswdHash;

    /** 소속 기관 ID (FK → com.tn_ognz) */
    private String ognzId;

    /** 소속 기관 구분 코드 (JOIN 조회 시 사용) */
    private String ognzSeCd;

    /** 이메일 주소 (개인정보 — 응답 시 마스킹 대상) */
    private String emlAddr;

    /** 휴대폰 번호 (개인정보 — 응답 시 마스킹 대상) */
    private String mblphnNo;

    /** 유선전화 번호 */
    private String tlphnNo;

    /** 비밀번호 변경일시 (null이면 초기 비번 미변경 상태) */
    private LocalDateTime pswdChgDt;

    /** 비밀번호 연속 실패 횟수 */
    private Integer pswdFailCnt;

    /** 계정 잠금 여부 (Y/N) */
    private String acntLockYn;

    /** 최종 로그인 일시 */
    private LocalDateTime lastLognDt;

    /** 관리자 여부 (Y/N) */
    private String masterYn;

    /** 사용 시작일시 */
    private LocalDateTime useBgngDt;

    /** 사용 종료일시 */
    private LocalDateTime useEndDt;

    /** 최초 등록일시 */
    private LocalDateTime frstRegDt;

    /** 최초 등록 사용자 ID */
    private String frstRegUserId;

    /** 최종 변경일시 */
    private LocalDateTime lastChgDt;

    /** 최종 변경 사용자 ID */
    private String lastChgUserId;
}
