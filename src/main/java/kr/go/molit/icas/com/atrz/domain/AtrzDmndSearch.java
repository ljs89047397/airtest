package kr.go.molit.icas.com.atrz.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 결재 요청 검색 조건 DTO.
 * 페이징: page(1-based) / pageSize.
 */
@Getter
@Setter
public class AtrzDmndSearch {

    /** 결재 업무 ID 필터 */
    private String atrzTaskId;

    /**
     * 결재 상태 코드 필터.
     * PEND / INPRG / APRVD / RJCTD / CNCLD
     */
    private String atrzStCd;

    /** 결재 요청 사용자 ID 필터 */
    private String dmndUserId;

    /** 요청일 시작 (yyyy-MM-dd 문자열, SQL 측에서 캐스팅) */
    private String dateFrom;

    /** 요청일 종료 (yyyy-MM-dd 문자열, SQL 측에서 캐스팅) */
    private String dateTo;

    /** 페이지 번호 (1-based, 기본값 1) */
    private int page     = 1;

    /** 페이지 크기 (기본값 20) */
    private int pageSize = 20;

    /** LIMIT / OFFSET 계산용 오프셋 반환 */
    public int getOffset() {
        return (Math.max(page, 1) - 1) * pageSize;
    }
}
