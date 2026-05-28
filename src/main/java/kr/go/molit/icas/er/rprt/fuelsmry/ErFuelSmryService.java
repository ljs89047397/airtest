package kr.go.molit.icas.er.rprt.fuelsmry;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.fuelsmry.domain.ErFuelSmryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 연료 유형별 총사용량 요약 비즈니스 서비스 (er.tn_er_fuel_smry).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 ER 이 DRAFT 상태일 때만 upsert/삭제 가능</li>
 *   <li>자연키 PK (er_id, fuel_type_cd) — upsert 패턴 적용</li>
 *   <li>ttl_fuel_wght >= 0 (음수 금지)</li>
 *   <li>ttl_co2_emsn >= 0 (음수 금지)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErFuelSmryService {

    private final ErFuelSmryMapper   erFuelSmryMapper;
    private final ErMapper           erMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 ER 의 연료 유형별 총사용량 전체 목록 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 연료 유형별 총사용량 목록 (fuel_type_cd ASC)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<ErFuelSmryVO> list(String erId, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return erFuelSmryMapper.selectByErId(erId);
    }

    /**
     * 연료 유형별 총사용량 단건 조회.
     *
     * @param erId       ER ID
     * @param fuelTypeCd 연료 유형 코드
     * @param user       로그인 사용자
     * @return 연료 유형별 총사용량 VO
     * @throws BusinessException NOT_FOUND — ER 또는 항목 미존재
     */
    public ErFuelSmryVO getOne(String erId, String fuelTypeCd, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        ErFuelSmryVO vo = erFuelSmryMapper.selectOne(erId, fuelTypeCd);
        if (vo == null) throw BusinessException.notFound("연료 유형별 총사용량 항목");
        return vo;
    }

    // ══════════════════════════════════════════════════════
    // Upsert (insert or update by natural key)
    // ══════════════════════════════════════════════════════

    /**
     * 연료 유형별 총사용량 upsert.
     *
     * <p>(er_id, fuel_type_cd) 가 이미 존재하면 update, 없으면 insert.
     *
     * @param erId       ER ID
     * @param fuelTypeCd 연료 유형 코드
     * @param vo         저장 데이터
     * @param user       로그인 사용자
     * @return 저장 후 최신 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    @Transactional
    public ErFuelSmryVO upsert(String erId, String fuelTypeCd, ErFuelSmryVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        validateTtlFuelWght(vo.getTtlFuelWght());
        validateTtlCo2Emsn(vo.getTtlCo2Emsn());

        vo.setErId(erId);
        vo.setFuelTypeCd(fuelTypeCd);

        if (erFuelSmryMapper.existsByPk(erId, fuelTypeCd)) {
            // 기존 행 업데이트
            vo.setLastChgUserId(user.getUserId());
            int affected = erFuelSmryMapper.updateFuelSmry(vo);
            if (affected == 0) throw BusinessException.conflict("수정 대상 연료 유형별 총사용량 항목이 존재하지 않거나 이미 만료되었습니다.");
        } else {
            // 신규 등록
            vo.setFrstRegUserId(user.getUserId());
            vo.setLastChgUserId(user.getUserId());
            erFuelSmryMapper.insertFuelSmry(vo);
        }

        return erFuelSmryMapper.selectOne(erId, fuelTypeCd);
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 연료 유형별 총사용량 소프트삭제.
     *
     * @param erId       ER ID
     * @param fuelTypeCd 연료 유형 코드
     * @param user       로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDelete(String erId, String fuelTypeCd, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        int affected = erFuelSmryMapper.softDeleteOne(erId, fuelTypeCd, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("연료 유형별 총사용량 항목");
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

    private void validateTtlFuelWght(BigDecimal ttlFuelWght) {
        if (ttlFuelWght != null && ttlFuelWght.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest(
                    "총 연료 중량(ttlFuelWght)은 0 이상이어야 합니다. 입력값: " + ttlFuelWght);
        }
    }

    private void validateTtlCo2Emsn(BigDecimal ttlCo2Emsn) {
        if (ttlCo2Emsn != null && ttlCo2Emsn.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest(
                    "총 CO2 배출량(ttlCo2Emsn)은 0 이상이어야 합니다. 입력값: " + ttlCo2Emsn);
        }
    }
}
