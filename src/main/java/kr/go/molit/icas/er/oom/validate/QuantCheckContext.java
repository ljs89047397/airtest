package kr.go.molit.icas.er.oom.validate;

import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.Builder;
import lombok.Getter;

/**
 * 18종 정량 검증 컨텍스트.
 *
 * <p>한번 빌드하면 각 Rule 에 동일 인스턴스가 전달되어 데이터 재조회 최소화.
 * Rule 은 ctx 에서 필요한 데이터를 꺼내고, 없으면 WARN(데이터 부족) 처리.
 */
@Getter
@Builder
public class QuantCheckContext {

    /** 점검 마스터 — 항상 존재 */
    private final OomCheckVO oom;

    /** 점검 대상 ER — 미연결 시 null (Rule 이 WARN 처리) */
    private final ErVO er;

    /** 운영사 — 항상 존재 */
    private final OprtrVO oprtr;

    /** 직전 보고연도(N-1) ER — 없으면 null */
    private final ErVO prevYearEr;

    /**
     * 배정된 검증기관 정보 — VR 이 연결되어 있을 때만 존재.
     * Rule16 (CCR 인증 유효성) 에서 사용.
     */
    private final VrfcnInstVO vrfcnInst;

    /**
     * VR 검증팀 리더의 연속 검증 횟수.
     * VR 이 연결되고 LEAD 구성원이 있을 때만 존재 (null 이면 WARN).
     * Rule17 (리더 연속 3년 초과) 에서 사용.
     */
    private final Integer leadConscutvCnt;
}
