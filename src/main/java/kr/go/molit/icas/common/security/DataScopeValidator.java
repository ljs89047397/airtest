package kr.go.molit.icas.common.security;

import kr.go.molit.icas.com.vrfcn.VrfcnAssgnMapper;
import kr.go.molit.icas.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 행 단위 가시범위 검증.
 *
 * <p>모든 도메인 Service 의 (조회 단건 / 변경 / 삭제) 첫 줄에서 호출.
 * 검증 누락은 코드 리뷰에서 차단.
 *
 * <pre>
 *   public ErResponse selectEr(String erId, IcasUser user) {
 *       ErVO er = erMapper.selectErById(erId);
 *       if (er == null) throw BusinessException.notFound("배출량 보고서");
 *       dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
 *       return ErResponse.from(er);
 *   }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class DataScopeValidator {

    private final VrfcnAssgnMapper vrfcnAssgnMapper;

    /**
     * 사용자가 해당 (운영사, 보고연도) 데이터에 접근 가능한지 검증.
     *
     * @param user     현재 로그인 사용자
     * @param oprtrId  접근하려는 항공기 운영사 ID
     * @param rprtYr   보고연도 (VERIFIER 가시범위 검증에 필요). null 허용
     */
    public void assertOprtrAccessible(IcasUser user, String oprtrId, String rprtYr) {
        if (user == null) throw BusinessException.forbidden("로그인이 필요합니다.");
        if (user.isMaster()) return;

        if (user.isMolitOrKotsa()) return;  // 전사 가시

        if (user.isAirline()) {
            if (oprtrId == null || !oprtrId.equals(user.getOprtrId())) {
                throw BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다.");
            }
            return;
        }

        if (user.isVerifier()) {
            if (rprtYr == null) {
                throw BusinessException.forbidden("검증기관은 보고연도가 지정된 데이터만 접근할 수 있습니다.");
            }
            boolean assigned = vrfcnAssgnMapper.existsAssgn(user.getVrfcnInstId(), oprtrId, rprtYr);
            if (!assigned) {
                throw BusinessException.forbidden("배정되지 않은 항공사 데이터에는 접근할 수 없습니다.");
            }
            return;
        }

        throw BusinessException.forbidden("알 수 없는 기관 유형입니다: " + user.getOgnzSeCd());
    }

    /** 항공사 사용자만 호출 가능한 액션 — 운영사 ID 가 본인 소속과 일치해야 함. */
    public void assertOwnAirline(IcasUser user, String oprtrId) {
        if (user.isMaster()) return;
        if (!user.isAirline()) {
            throw BusinessException.forbidden("항공사 사용자만 수행할 수 있는 작업입니다.");
        }
        if (oprtrId == null || !oprtrId.equals(user.getOprtrId())) {
            throw BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다.");
        }
    }
}
