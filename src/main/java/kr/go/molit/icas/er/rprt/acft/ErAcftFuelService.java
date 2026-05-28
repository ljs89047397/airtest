package kr.go.molit.icas.er.rprt.acft;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.acft.domain.ErAcftFuelVO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 항공기·연료 비즈니스 서비스 (er.tn_er_acft_fuel).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 ER 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>regis_mark 필수, 같은 er_id 내 중복 금지</li>
 *   <li>fuel_type_cd 필수</li>
 *   <li>ownr_ls_se_cd 화이트리스트: OWN / LEASE</li>
 *   <li>dnsty_se_cd 화이트리스트: STD / ACT</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErAcftFuelService {

    private static final Set<String> VALID_OWNR_LS_SE_CD = Set.of("OWN", "LEASE");
    private static final Set<String> VALID_DNSTY_SE_CD   = Set.of("STD", "ACT");

    private final ErAcftFuelMapper   erAcftFuelMapper;
    private final ErMapper           erMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 ER 의 항공기·연료 전체 목록 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 항공기·연료 목록 (acft_sn ASC)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<ErAcftFuelVO> list(String erId, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return erAcftFuelMapper.selectByErId(erId);
    }

    /**
     * 항공기·연료 단건 조회.
     *
     * @param erId   ER ID
     * @param acftSn 항공기 일련번호
     * @param user   로그인 사용자
     * @return 항공기·연료 VO
     * @throws BusinessException NOT_FOUND — ER 또는 항목 미존재
     */
    public ErAcftFuelVO getOne(String erId, int acftSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        ErAcftFuelVO vo = erAcftFuelMapper.selectOne(erId, acftSn);
        if (vo == null) throw BusinessException.notFound("항공기·연료 항목");
        return vo;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 항공기·연료 추가.
     *
     * <p>같은 er_id 내 동일 regis_mark 가 이미 존재하면 CONFLICT.
     *
     * @param erId ER ID
     * @param vo   등록 데이터 (regisMark, fuelTypeCd 필수)
     * @param user 로그인 사용자
     * @return 생성된 항공기·연료 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     * @throws BusinessException CONFLICT    — 등록기호 중복
     */
    @Transactional
    public ErAcftFuelVO add(String erId, ErAcftFuelVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        validateRegisMark(vo.getRegisMark());
        validateFuelTypeCd(vo.getFuelTypeCd());
        validateOwnrLsSeCd(vo.getOwnrLsSeCd());
        validateDnstySecCd(vo.getDnstySecCd());

        // 같은 ER 내 등록기호 중복 체크 (신규이므로 excludeSn=0)
        if (erAcftFuelMapper.existsByRegisMark(erId, vo.getRegisMark(), 0)) {
            throw BusinessException.conflict(
                    "이미 동일한 등록기호가 해당 ER 에 등록되어 있습니다. (regisMark=" + vo.getRegisMark() + ")");
        }

        int nextSn = erAcftFuelMapper.selectNextSn(erId);

        vo.setErId(erId);
        vo.setAcftSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        erAcftFuelMapper.insertAcftFuel(vo);
        return erAcftFuelMapper.selectOne(erId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 항공기·연료 수정.
     *
     * @param erId   ER ID
     * @param acftSn 항공기 일련번호
     * @param vo     수정 데이터
     * @param user   로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void update(String erId, int acftSn, ErAcftFuelVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        ErAcftFuelVO existing = erAcftFuelMapper.selectOne(erId, acftSn);
        if (existing == null) throw BusinessException.notFound("항공기·연료 항목");

        validateRegisMark(vo.getRegisMark());
        validateFuelTypeCd(vo.getFuelTypeCd());
        validateOwnrLsSeCd(vo.getOwnrLsSeCd());
        validateDnstySecCd(vo.getDnstySecCd());

        // 수정 시 자기 자신(excludeSn=acftSn) 제외하고 중복 체크
        if (erAcftFuelMapper.existsByRegisMark(erId, vo.getRegisMark(), acftSn)) {
            throw BusinessException.conflict(
                    "이미 동일한 등록기호가 해당 ER 에 등록되어 있습니다. (regisMark=" + vo.getRegisMark() + ")");
        }

        vo.setErId(erId);
        vo.setAcftSn(acftSn);
        vo.setLastChgUserId(user.getUserId());

        int affected = erAcftFuelMapper.updateAcftFuel(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 항공기·연료 항목이 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 항공기·연료 소프트삭제.
     *
     * @param erId   ER ID
     * @param acftSn 항공기 일련번호
     * @param user   로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDelete(String erId, int acftSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        int affected = erAcftFuelMapper.softDeleteOne(erId, acftSn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("항공기·연료 항목");
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

    private void validateRegisMark(String regisMark) {
        if (regisMark == null || regisMark.isBlank()) {
            throw BusinessException.badRequest("항공기 등록기호(regisMark)는 필수입니다.");
        }
    }

    private void validateFuelTypeCd(String fuelTypeCd) {
        if (fuelTypeCd == null || fuelTypeCd.isBlank()) {
            throw BusinessException.badRequest("연료 유형 코드(fuelTypeCd)는 필수입니다.");
        }
    }

    private void validateOwnrLsSeCd(String ownrLsSeCd) {
        if (ownrLsSeCd != null && !VALID_OWNR_LS_SE_CD.contains(ownrLsSeCd)) {
            throw BusinessException.badRequest(
                    "소유/리스 구분 코드(ownrLsSeCd) 허용값: OWN, LEASE. 입력값: " + ownrLsSeCd);
        }
    }

    private void validateDnstySecCd(String dnstySecCd) {
        if (dnstySecCd != null && !VALID_DNSTY_SE_CD.contains(dnstySecCd)) {
            throw BusinessException.badRequest(
                    "밀도 구분 코드(dnstySecCd) 허용값: STD, ACT. 입력값: " + dnstySecCd);
        }
    }
}
