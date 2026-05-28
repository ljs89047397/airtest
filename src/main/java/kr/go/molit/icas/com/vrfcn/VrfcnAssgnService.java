package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 검증기관-항공사 배정 비즈니스 서비스.
 *
 * <ul>
 *   <li>읽기 전용 기본 트랜잭션 (클래스 레벨)</li>
 *   <li>변경 메서드는 {@code @Transactional} 오버라이드</li>
 *   <li>기존 {@link VrfcnAssgnMapper} 를 그대로 사용 (수정 금지)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrfcnAssgnService {

    private final VrfcnAssgnMapper vrfcnAssgnMapper;

    /**
     * 전체 배정 목록 조회 (보고연도 필터 가능).
     *
     * @param rprtYr 보고연도 4자리 (null 이면 전체)
     * @return vrfcnInstId, vrfcnInstNm, oprtrId, oprtrNm, rprtYr, assgnDt 포함 Map 목록
     */
    public List<Map<String, Object>> selectAssgnList(String rprtYr) {
        return vrfcnAssgnMapper.selectAssgnList(rprtYr);
    }

    /**
     * 특정 검증기관에 배정된 항공사 ID 목록 조회.
     *
     * @param vrfcnInstId 검증기관 ID
     * @param rprtYr      보고연도 4자리
     * @return 배정된 항공사 ID 목록
     */
    public List<String> selectAssignedOprtrIds(String vrfcnInstId, String rprtYr) {
        if (vrfcnInstId == null || vrfcnInstId.isBlank()) {
            throw BusinessException.badRequest("검증기관 ID 는 필수입니다.");
        }
        if (rprtYr == null || rprtYr.isBlank()) {
            throw BusinessException.badRequest("보고연도는 필수입니다.");
        }
        return vrfcnAssgnMapper.selectAssignedOprtrIds(vrfcnInstId, rprtYr);
    }

    /**
     * 검증기관-항공사 배정 등록.
     * <ul>
     *   <li>동일 (vrfcnInstId, oprtrId, rprtYr) 조합이 이미 유효하면 {@code CONFLICT} 예외</li>
     * </ul>
     *
     * @throws BusinessException CONFLICT   — 이미 유효한 배정이 존재할 경우
     * @throws BusinessException BAD_REQUEST — 필수 파라미터 누락
     */
    @Transactional
    public void createAssgn(String vrfcnInstId, String oprtrId, String rprtYr, IcasUser user) {
        validateAssgnParams(vrfcnInstId, oprtrId, rprtYr);

        // 중복 배정 체크
        boolean exists = vrfcnAssgnMapper.existsAssgn(vrfcnInstId, oprtrId, rprtYr);
        if (exists) {
            throw BusinessException.conflict(
                    "해당 검증기관과 항공사의 배정이 이미 존재합니다. (보고연도: " + rprtYr + ")");
        }

        int affected = vrfcnAssgnMapper.insertAssgn(vrfcnInstId, oprtrId, rprtYr, user.getUserId());
        if (affected == 0) {
            throw BusinessException.badRequest("배정 등록에 실패했습니다.");
        }
    }

    /**
     * 검증기관-항공사 배정 소프트삭제.
     *
     * @throws BusinessException NOT_FOUND — 유효한 배정이 존재하지 않을 경우
     * @throws BusinessException BAD_REQUEST — 필수 파라미터 누락
     */
    @Transactional
    public void softDeleteAssgn(String vrfcnInstId, String oprtrId, String rprtYr, IcasUser user) {
        validateAssgnParams(vrfcnInstId, oprtrId, rprtYr);

        int affected = vrfcnAssgnMapper.softDeleteAssgn(vrfcnInstId, oprtrId, rprtYr, user.getUserId());
        if (affected == 0) {
            throw BusinessException.notFound("검증기관-항공사 배정");
        }
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    private void validateAssgnParams(String vrfcnInstId, String oprtrId, String rprtYr) {
        if (vrfcnInstId == null || vrfcnInstId.isBlank()) {
            throw BusinessException.badRequest("검증기관 ID 는 필수입니다.");
        }
        if (oprtrId == null || oprtrId.isBlank()) {
            throw BusinessException.badRequest("항공사 ID 는 필수입니다.");
        }
        if (rprtYr == null || rprtYr.isBlank()) {
            throw BusinessException.badRequest("보고연도는 필수입니다.");
        }
        if (!rprtYr.matches("\\d{4}")) {
            throw BusinessException.badRequest("보고연도는 4자리 숫자여야 합니다.");
        }
    }
}
