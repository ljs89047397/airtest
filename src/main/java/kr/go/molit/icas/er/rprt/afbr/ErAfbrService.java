package kr.go.molit.icas.er.rprt.afbr;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.afbr.domain.ErAfbrVO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 항공기 유형별 평균 연료연소율 비즈니스 서비스 (er.tn_er_afbr).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 ER 이 DRAFT 상태일 때만 upsert/삭제 가능</li>
 *   <li>자연키 PK (er_id, acft_type_cd) — upsert 패턴 적용</li>
 *   <li>afbr_val > 0 필수</li>
 *   <li>afbr_unit 화이트리스트: kg/min / lb/min / kg/hr (null 허용 — 기본값 kg/min)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErAfbrService {

    private static final Set<String> VALID_AFBR_UNIT = Set.of("kg/min", "lb/min", "kg/hr");

    private final ErAfbrMapper       erAfbrMapper;
    private final ErMapper           erMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 ER 의 평균 연료연소율 전체 목록 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 평균 연료연소율 목록 (acft_type_cd ASC)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<ErAfbrVO> list(String erId, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return erAfbrMapper.selectByErId(erId);
    }

    /**
     * 평균 연료연소율 단건 조회.
     *
     * @param erId       ER ID
     * @param acftTypeCd 항공기 유형 코드
     * @param user       로그인 사용자
     * @return 평균 연료연소율 VO
     * @throws BusinessException NOT_FOUND — ER 또는 항목 미존재
     */
    public ErAfbrVO getOne(String erId, String acftTypeCd, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        ErAfbrVO vo = erAfbrMapper.selectOne(erId, acftTypeCd);
        if (vo == null) throw BusinessException.notFound("평균 연료연소율 항목");
        return vo;
    }

    // ══════════════════════════════════════════════════════
    // Upsert (insert or update by natural key)
    // ══════════════════════════════════════════════════════

    /**
     * 평균 연료연소율 upsert.
     *
     * <p>(er_id, acft_type_cd) 가 이미 존재하면 update, 없으면 insert.
     *
     * @param erId       ER ID
     * @param acftTypeCd 항공기 유형 코드
     * @param vo         저장 데이터 (afbrVal 필수)
     * @param user       로그인 사용자
     * @return 저장 후 최신 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    @Transactional
    public ErAfbrVO upsert(String erId, String acftTypeCd, ErAfbrVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        validateAfbrVal(vo.getAfbrVal());
        validateAfbrUnit(vo.getAfbrUnit());

        vo.setErId(erId);
        vo.setAcftTypeCd(acftTypeCd);

        if (erAfbrMapper.existsByPk(erId, acftTypeCd)) {
            // 기존 행 업데이트
            vo.setLastChgUserId(user.getUserId());
            int affected = erAfbrMapper.updateAfbr(vo);
            if (affected == 0) throw BusinessException.conflict("수정 대상 평균 연료연소율 항목이 존재하지 않거나 이미 만료되었습니다.");
        } else {
            // 신규 등록
            vo.setFrstRegUserId(user.getUserId());
            vo.setLastChgUserId(user.getUserId());
            erAfbrMapper.insertAfbr(vo);
        }

        return erAfbrMapper.selectOne(erId, acftTypeCd);
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 평균 연료연소율 소프트삭제.
     *
     * @param erId       ER ID
     * @param acftTypeCd 항공기 유형 코드
     * @param user       로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDelete(String erId, String acftTypeCd, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        int affected = erAfbrMapper.softDeleteOne(erId, acftTypeCd, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("평균 연료연소율 항목");
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

    private void validateAfbrVal(BigDecimal afbrVal) {
        if (afbrVal == null) {
            throw BusinessException.badRequest("평균 연료연소율(afbrVal)은 필수입니다.");
        }
        if (afbrVal.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("평균 연료연소율(afbrVal)은 0 보다 커야 합니다. 입력값: " + afbrVal);
        }
    }

    private void validateAfbrUnit(String afbrUnit) {
        if (afbrUnit != null && !VALID_AFBR_UNIT.contains(afbrUnit)) {
            throw BusinessException.badRequest(
                    "연소율 단위(afbrUnit) 허용값: kg/min, lb/min, kg/hr. 입력값: " + afbrUnit);
        }
    }
}
