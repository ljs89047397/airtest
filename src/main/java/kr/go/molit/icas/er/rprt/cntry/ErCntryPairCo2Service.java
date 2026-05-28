package kr.go.molit.icas.er.rprt.cntry;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.cntry.domain.ErCntryPairCo2VO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 국가 쌍 배출량 비즈니스 서비스 (er.tn_er_cntry_pair_co2).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 ER 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>국가 코드: 2자 필수</li>
 *   <li>cerEstmYn / ofstReqYn 화이트리스트: Y / N</li>
 *   <li>fltCnt ≥ 0, fuelWght ≥ 0, convFctr > 0, co2Emsn ≥ 0, cefReduAmt ≥ 0</li>
 *   <li>같은 er_id 내 (dprtrCntryCd, arvlCntryCd, fuelTypeCd) 중복 금지</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErCntryPairCo2Service {

    private static final Set<String> VALID_YN = Set.of("Y", "N");

    private final ErCntryPairCo2Mapper erCntryPairCo2Mapper;
    private final ErMapper             erMapper;
    private final DataScopeValidator   dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 ER 의 국가 쌍 배출량 전체 목록 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 국가 쌍 배출량 목록 (pair_sn ASC)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<ErCntryPairCo2VO> list(String erId, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return erCntryPairCo2Mapper.selectByErId(erId);
    }

    /**
     * 국가 쌍 배출량 단건 조회.
     *
     * @param erId   ER ID
     * @param pairSn 국가 쌍 일련번호
     * @param user   로그인 사용자
     * @return 국가 쌍 배출량 VO
     * @throws BusinessException NOT_FOUND — ER 또는 국가 쌍 미존재
     */
    public ErCntryPairCo2VO getOne(String erId, int pairSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        ErCntryPairCo2VO pair = erCntryPairCo2Mapper.selectOne(erId, pairSn);
        if (pair == null) throw BusinessException.notFound("국가 쌍 배출량");
        return pair;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 국가 쌍 배출량 추가.
     *
     * @param erId ER ID
     * @param vo   등록 데이터 (fuelTypeCd, convFctr 필수)
     * @param user 로그인 사용자
     * @return 생성된 국가 쌍 배출량 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     * @throws BusinessException CONFLICT    — 중복 쌍
     */
    @Transactional
    public ErCntryPairCo2VO add(String erId, ErCntryPairCo2VO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        validateFields(vo);

        // (출발국, 도착국, 연료유형) 중복 체크 (신규이므로 excludeSn=-1)
        if (erCntryPairCo2Mapper.existsByPair(erId,
                vo.getDprtrCntryCd(), vo.getArvlCntryCd(), vo.getFuelTypeCd(), -1)) {
            throw BusinessException.conflict(
                    "동일한 출발국·도착국·연료유형 조합(" +
                    vo.getDprtrCntryCd() + " → " + vo.getArvlCntryCd() +
                    ", " + vo.getFuelTypeCd() + ")이 이미 등록되어 있습니다.");
        }

        int nextSn = erCntryPairCo2Mapper.selectNextSn(erId);

        applyDefaults(vo);
        vo.setErId(erId);
        vo.setPairSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        erCntryPairCo2Mapper.insert(vo);
        return erCntryPairCo2Mapper.selectOne(erId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 국가 쌍 배출량 수정.
     *
     * @param erId   ER ID
     * @param pairSn 국가 쌍 일련번호
     * @param vo     수정 데이터
     * @param user   로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     * @throws BusinessException CONFLICT  — 중복 쌍
     */
    @Transactional
    public void update(String erId, int pairSn, ErCntryPairCo2VO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        ErCntryPairCo2VO existing = erCntryPairCo2Mapper.selectOne(erId, pairSn);
        if (existing == null) throw BusinessException.notFound("국가 쌍 배출량");

        validateFields(vo);

        // 자신(pairSn) 제외 중복 체크
        if (erCntryPairCo2Mapper.existsByPair(erId,
                vo.getDprtrCntryCd(), vo.getArvlCntryCd(), vo.getFuelTypeCd(), pairSn)) {
            throw BusinessException.conflict(
                    "동일한 출발국·도착국·연료유형 조합(" +
                    vo.getDprtrCntryCd() + " → " + vo.getArvlCntryCd() +
                    ", " + vo.getFuelTypeCd() + ")이 이미 등록되어 있습니다.");
        }

        applyDefaults(vo);
        vo.setErId(erId);
        vo.setPairSn(pairSn);
        vo.setLastChgUserId(user.getUserId());

        int affected = erCntryPairCo2Mapper.update(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 국가 쌍 배출량이 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 국가 쌍 배출량 소프트삭제.
     *
     * @param erId   ER ID
     * @param pairSn 국가 쌍 일련번호
     * @param user   로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDelete(String erId, int pairSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        int affected = erCntryPairCo2Mapper.softDelete(erId, pairSn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("국가 쌍 배출량");
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

    private void validateFields(ErCntryPairCo2VO vo) {
        validateCntryCd(vo.getDprtrCntryCd(), "출발 국가 코드(dprtrCntryCd)");
        validateCntryCd(vo.getArvlCntryCd(), "도착 국가 코드(arvlCntryCd)");

        if (vo.getFuelTypeCd() == null || vo.getFuelTypeCd().isBlank()) {
            throw BusinessException.badRequest("연료 유형 코드(fuelTypeCd)는 필수입니다.");
        }
        if (vo.getFuelTypeCd().length() > 20) {
            throw BusinessException.badRequest("연료 유형 코드(fuelTypeCd)는 최대 20자입니다.");
        }

        validateYn(vo.getCerEstmYn(), "CORSIA 인증 추정 여부(cerEstmYn)");
        validateYn(vo.getOfstReqYn(), "상쇄 요건 여부(ofstReqYn)");

        if (vo.getFltCnt() != null && vo.getFltCnt() < 0) {
            throw BusinessException.badRequest("항공편 수(fltCnt)는 0 이상이어야 합니다.");
        }
        if (vo.getFuelWght() != null && vo.getFuelWght().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("연료 중량(fuelWght)은 0 이상이어야 합니다.");
        }
        if (vo.getConvFctr() == null) {
            throw BusinessException.badRequest("변환 계수(convFctr)는 필수입니다.");
        }
        if (vo.getConvFctr().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("변환 계수(convFctr)는 0 초과여야 합니다.");
        }
        if (vo.getCo2Emsn() != null && vo.getCo2Emsn().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("CO₂ 배출량(co2Emsn)은 0 이상이어야 합니다.");
        }
        if (vo.getCefReduAmt() != null && vo.getCefReduAmt().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("CEF 감축량(cefReduAmt)은 0 이상이어야 합니다.");
        }
    }

    private void validateCntryCd(String cntryCd, String fieldLabel) {
        if (cntryCd == null || cntryCd.length() != 2) {
            throw BusinessException.badRequest(fieldLabel + "는 2자리 국가 코드여야 합니다. 입력값: " + cntryCd);
        }
    }

    private void validateYn(String yn, String fieldLabel) {
        if (yn != null && !VALID_YN.contains(yn)) {
            throw BusinessException.badRequest(fieldLabel + " 허용값: Y, N. 입력값: " + yn);
        }
    }

    /** null 필드에 DDL 기본값 적용 */
    private void applyDefaults(ErCntryPairCo2VO vo) {
        if (vo.getCerEstmYn()   == null) vo.setCerEstmYn("N");
        if (vo.getFltCnt()      == null) vo.setFltCnt(0);
        if (vo.getFuelWght()    == null) vo.setFuelWght(BigDecimal.ZERO);
        if (vo.getCo2Emsn()     == null) vo.setCo2Emsn(BigDecimal.ZERO);
        if (vo.getOfstReqYn()   == null) vo.setOfstReqYn("N");
        if (vo.getCefReduAmt()  == null) vo.setCefReduAmt(BigDecimal.ZERO);
    }
}
