package kr.go.molit.icas.er.rprt.vrfr;

import kr.go.molit.icas.com.vrfcn.VrfcnInstMapper;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.vrfr.domain.ErVrfrInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ER 참여 검증기관 정보 비즈니스 서비스 (er.tn_er_vrfr_info).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 ER 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>vrfcn_inst_id 는 com.tn_vrfcn_inst 에 존재하는 유효한 기관이어야 함</li>
 *   <li>같은 er_id 내 동일 vrfcn_inst_id 중복 등록 금지</li>
 *   <li>cnct_desc 최대 500자, accrd_dtl 최대 1000자</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErVrfrInfoService {

    private static final int MAX_CNCT_DESC_LENGTH = 500;
    private static final int MAX_ACCRD_DTL_LENGTH  = 1000;

    private final ErVrfrInfoMapper erVrfrInfoMapper;
    private final ErMapper         erMapper;
    private final VrfcnInstMapper  vrfcnInstMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 ER 의 참여 검증기관 목록 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 참여 검증기관 목록 (vrfr_sn ASC)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<ErVrfrInfoVO> listByErId(String erId, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return erVrfrInfoMapper.selectByErId(erId);
    }

    /**
     * 참여 검증기관 단건 조회.
     *
     * @param erId   ER ID
     * @param vrfrSn 검증기관 일련번호
     * @param user   로그인 사용자
     * @return 검증기관 정보 VO
     * @throws BusinessException NOT_FOUND — ER 또는 항목 미존재
     */
    public ErVrfrInfoVO getOne(String erId, int vrfrSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        ErVrfrInfoVO vrfr = erVrfrInfoMapper.selectOne(erId, vrfrSn);
        if (vrfr == null) throw BusinessException.notFound("참여 검증기관 정보");
        return vrfr;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 참여 검증기관 추가.
     *
     * @param erId ER ID
     * @param vo   등록 데이터 (vrfcnInstId 필수)
     * @param user 로그인 사용자
     * @return 생성된 검증기관 정보 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패 (기관 미존재, 중복 등)
     */
    @Transactional
    public ErVrfrInfoVO addVrfr(String erId, ErVrfrInfoVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        // 검증기관 유효성
        validateVrfcnInst(vo.getVrfcnInstId());

        // 업무 컬럼 길이 검증
        validateCnctDesc(vo.getCnctDesc());
        validateAccrdDtl(vo.getAccrdDtl());

        // 같은 ER 내 중복 검증기관 체크 (신규이므로 excludeSn=0)
        if (erVrfrInfoMapper.existsByVrfcnInst(erId, vo.getVrfcnInstId(), 0)) {
            throw BusinessException.badRequest("이미 동일한 검증기관이 해당 ER 에 등록되어 있습니다.");
        }

        int nextSn = erVrfrInfoMapper.selectNextSn(erId);

        vo.setErId(erId);
        vo.setVrfrSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        erVrfrInfoMapper.insertVrfrInfo(vo);
        return erVrfrInfoMapper.selectOne(erId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 참여 검증기관 수정.
     *
     * @param erId   ER ID
     * @param vrfrSn 검증기관 일련번호
     * @param vo     수정 데이터
     * @param user   로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void updateVrfr(String erId, int vrfrSn, ErVrfrInfoVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        ErVrfrInfoVO existing = erVrfrInfoMapper.selectOne(erId, vrfrSn);
        if (existing == null) throw BusinessException.notFound("참여 검증기관 정보");

        // 검증기관 변경 시 유효성 재검증
        if (!isBlank(vo.getVrfcnInstId())) {
            validateVrfcnInst(vo.getVrfcnInstId());
            // 자기 자신 제외 중복 체크
            if (erVrfrInfoMapper.existsByVrfcnInst(erId, vo.getVrfcnInstId(), vrfrSn)) {
                throw BusinessException.badRequest("이미 동일한 검증기관이 해당 ER 에 등록되어 있습니다.");
            }
        }

        validateCnctDesc(vo.getCnctDesc());
        validateAccrdDtl(vo.getAccrdDtl());

        vo.setErId(erId);
        vo.setVrfrSn(vrfrSn);
        vo.setLastChgUserId(user.getUserId());

        int affected = erVrfrInfoMapper.updateVrfrInfo(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 참여 검증기관 정보가 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 참여 검증기관 소프트삭제.
     *
     * @param erId   ER ID
     * @param vrfrSn 검증기관 일련번호
     * @param user   로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDeleteVrfr(String erId, int vrfrSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        int affected = erVrfrInfoMapper.softDeleteOne(erId, vrfrSn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("참여 검증기관 정보");
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════

    private ErVO loadEr(String erId) {
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        return er;
    }

    private void assertDraft(ErVO er) {
        if (!"DRAFT".equals(er.getErStCd())) {
            throw BusinessException.badRequest(
                    "DRAFT 상태의 ER 에서만 수정할 수 있습니다. 현재 상태: " + er.getErStCd());
        }
    }

    private void validateVrfcnInst(String vrfcnInstId) {
        if (isBlank(vrfcnInstId)) {
            throw BusinessException.badRequest("검증기관 ID(vrfcnInstId)는 필수입니다.");
        }
        VrfcnInstVO inst = vrfcnInstMapper.selectByVrfcnInstId(vrfcnInstId);
        if (inst == null) {
            throw BusinessException.badRequest("존재하지 않는 검증기관입니다. (vrfcnInstId=" + vrfcnInstId + ")");
        }
    }

    private void validateCnctDesc(String cnctDesc) {
        if (cnctDesc != null && cnctDesc.length() > MAX_CNCT_DESC_LENGTH) {
            throw BusinessException.badRequest(
                    "참여 개요(cnctDesc)는 최대 " + MAX_CNCT_DESC_LENGTH + "자입니다. 입력 길이: " + cnctDesc.length());
        }
    }

    private void validateAccrdDtl(String accrdDtl) {
        if (accrdDtl != null && accrdDtl.length() > MAX_ACCRD_DTL_LENGTH) {
            throw BusinessException.badRequest(
                    "인증 상세(accrdDtl)는 최대 " + MAX_ACCRD_DTL_LENGTH + "자입니다. 입력 길이: " + accrdDtl.length());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
