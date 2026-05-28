package kr.go.molit.icas.com.ognz;

import kr.go.molit.icas.com.ognz.domain.OgnzVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 기관(tn_ognz) 비즈니스 서비스.
 * - 등록/수정/삭제 권한 체크(MOLIT/KOTSA 여부)는 Controller 에서 assertMolitOrKotsa 수행 후 진입.
 * - ognz_se_cd 값 검증, ognz_id 중복 체크, biz_no 형식 검증은 Service 에서 수행.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OgnzService {

    private static final Set<String> VALID_OGNZ_SE_CD = Set.of("MOLIT", "KOTSA", "AIRLINE", "VERIFIER");
    private static final Pattern BIZ_NO_PATTERN = Pattern.compile("^\\d{10}$");

    private final OgnzMapper ognzMapper;

    /** 유효한 기관 전체 조회 */
    public List<OgnzVO> listAll() {
        return ognzMapper.selectAll();
    }

    /** 단건 조회 */
    public OgnzVO getOgnz(String ognzId) {
        OgnzVO vo = ognzMapper.selectByOgnzId(ognzId);
        if (vo == null) throw BusinessException.notFound("기관");
        return vo;
    }

    /** 기관 등록 */
    @Transactional
    public OgnzVO createOgnz(OgnzVO vo, IcasUser user) {
        validateOgnzSeCd(vo.getOgnzSeCd());
        validateBizNo(vo.getBizNo());

        if (vo.getOgnzId() == null || vo.getOgnzId().isBlank()) {
            throw BusinessException.badRequest("기관 ID 는 필수입니다.");
        }
        if (vo.getOgnzNm() == null || vo.getOgnzNm().isBlank()) {
            throw BusinessException.badRequest("기관명은 필수입니다.");
        }

        // 중복 체크 (유효구간 필터 포함)
        OgnzVO existing = ognzMapper.selectByOgnzId(vo.getOgnzId());
        if (existing != null) {
            throw BusinessException.conflict("이미 사용 중인 기관 ID 입니다: " + vo.getOgnzId());
        }

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        ognzMapper.insertOgnz(vo);
        return ognzMapper.selectByOgnzId(vo.getOgnzId());
    }

    /** 기관 수정 */
    @Transactional
    public OgnzVO updateOgnz(String ognzId, OgnzVO vo, IcasUser user) {
        validateOgnzSeCd(vo.getOgnzSeCd());
        validateBizNo(vo.getBizNo());

        if (vo.getOgnzNm() == null || vo.getOgnzNm().isBlank()) {
            throw BusinessException.badRequest("기관명은 필수입니다.");
        }

        vo.setOgnzId(ognzId);
        vo.setLastChgUserId(user.getUserId());
        int affected = ognzMapper.updateOgnz(vo);
        if (affected == 0) throw BusinessException.notFound("기관");
        return ognzMapper.selectByOgnzId(ognzId);
    }

    /** 소프트 삭제 */
    @Transactional
    public void softDeleteOgnz(String ognzId, IcasUser user) {
        int affected = ognzMapper.softDeleteOgnz(ognzId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("기관");
    }

    /* ── 외부 도메인 진입점용 FK 검증 ── */

    /**
     * 참조 데이터 유효성 검증 — ognz_id 가 유효구간 내 실제 존재하는지 확인.
     * 존재하지 않으면 BAD_REQUEST 던짐. (FK 위반 → 500 으로 가는 것 방지)
     * 다른 도메인 Service 에서 ognz_id 받는 진입점마다 호출.
     */
    public OgnzVO requireValidOgnz(String ognzId) {
        if (ognzId == null || ognzId.isBlank()) {
            throw BusinessException.badRequest("기관 ID 는 필수입니다.");
        }
        OgnzVO vo = ognzMapper.selectByOgnzId(ognzId);
        if (vo == null) throw BusinessException.badRequest("존재하지 않거나 유효하지 않은 기관 ID 입니다: " + ognzId);
        return vo;
    }

    /**
     * 기관의 ognz_se_cd 가 기대값과 일치하는지 검증.
     * 예: AIRLINE 운영사 등록 시 ognzId 의 ognz_se_cd 가 'AIRLINE' 인지 확인.
     */
    public OgnzVO requireOgnzOfType(String ognzId, String expectedSeCd) {
        OgnzVO vo = requireValidOgnz(ognzId);
        if (!expectedSeCd.equals(vo.getOgnzSeCd())) {
            throw BusinessException.badRequest(
                "기관 ID '" + ognzId + "' 의 구분(" + vo.getOgnzSeCd()
                + ") 이 기대값(" + expectedSeCd + ") 과 일치하지 않습니다.");
        }
        return vo;
    }

    /* ── 내부 검증 ── */

    private void validateOgnzSeCd(String ognzSeCd) {
        if (ognzSeCd == null || !VALID_OGNZ_SE_CD.contains(ognzSeCd)) {
            throw BusinessException.badRequest(
                "기관 구분 코드가 유효하지 않습니다. 허용값: MOLIT, KOTSA, AIRLINE, VERIFIER");
        }
    }

    /**
     * 사업자등록번호는 선택 입력.
     * 입력된 경우 숫자 10자리 형식 검증.
     */
    private void validateBizNo(String bizNo) {
        if (bizNo != null && !bizNo.isBlank() && !BIZ_NO_PATTERN.matcher(bizNo).matches()) {
            throw BusinessException.badRequest("사업자등록번호는 숫자 10자리여야 합니다.");
        }
    }
}
