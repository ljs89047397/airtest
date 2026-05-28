package kr.go.molit.icas.com.user.domain;

import kr.go.molit.icas.common.dto.PageRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * 사용자 목록 검색 조건 DTO.
 * PageRequest 를 상속하여 page, size, sort 포함.
 */
@Getter
@Setter
public class UserSearch extends PageRequest {

    /** 소속 기관 ID (완전 일치) */
    private String ognzId;

    /** 기관 구분 코드 (MOLIT/KOTSA/AIRLINE/VERIFIER 완전 일치) */
    private String ognzSeCd;

    /** 사용자 ID LIKE 검색 (부분 일치) */
    private String userIdLike;

    /** 사용자명 LIKE 검색 (부분 일치) */
    private String userNmLike;

    /** 계정 잠금 여부 필터 (Y/N) */
    private String acntLockYn;
}
