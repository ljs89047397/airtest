package kr.go.molit.icas.er.rprt.gap;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.cntry.ErCntryPairCo2Mapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.gap.domain.ErDataGapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 데이터 갭 비즈니스 서비스 (er.tn_er_data_gap).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 ER 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>afctCo2Emsn ≥ 0</li>
 *   <li>thrshld5pctXcYn 은 사용자 입력값 무시 — insert/update 마다 자동 재계산 (SFR-014)</li>
 * </ul>
 *
 * <h2>5% 임계치 자동 판정 알고리즘</h2>
 * <ol>
 *   <li>해당 er_id 의 tn_er_cntry_pair_co2.SUM(co2_emsn) = totalCo2 조회</li>
 *   <li>totalCo2 = 0 → 정보 부족 → thrshld5pctXcYn = 'Y' (안전한 쪽)</li>
 *   <li>afctCo2Emsn / totalCo2 ≥ 0.05 → 'Y', 미만 → 'N'</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErDataGapService {

    /** 5% 임계치 상수 */
    private static final BigDecimal THRESHOLD_5PCT = new BigDecimal("0.05");

    private final ErDataGapMapper        erDataGapMapper;
    private final ErCntryPairCo2Mapper   erCntryPairCo2Mapper;
    private final ErMapper               erMapper;
    private final DataScopeValidator     dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 ER 의 데이터 갭 전체 목록 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return 데이터 갭 목록 (gap_sn ASC)
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<ErDataGapVO> list(String erId, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return erDataGapMapper.selectByErId(erId);
    }

    /**
     * 데이터 갭 단건 조회.
     *
     * @param erId  ER ID
     * @param gapSn 데이터 갭 일련번호
     * @param user  로그인 사용자
     * @return 데이터 갭 VO
     * @throws BusinessException NOT_FOUND — ER 또는 데이터 갭 미존재
     */
    public ErDataGapVO getOne(String erId, int gapSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        ErDataGapVO gap = erDataGapMapper.selectOne(erId, gapSn);
        if (gap == null) throw BusinessException.notFound("데이터 갭");
        return gap;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * 데이터 갭 추가.
     * thrshld5pctXcYn 은 사용자 입력값을 무시하고 자동 계산.
     *
     * @param erId ER ID
     * @param vo   등록 데이터
     * @param user 로그인 사용자
     * @return 생성된 데이터 갭 VO (thrshld5pctXcYn 자동 설정)
     * @throws BusinessException BAD_REQUEST — 검증 실패
     */
    @Transactional
    public ErDataGapVO add(String erId, ErDataGapVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        validateFields(vo);

        int nextSn = erDataGapMapper.selectNextSn(erId);

        // 5% 임계치 자동 판정 (사용자 입력값 무시)
        vo.setThrshld5pctXcYn(calcThreshold(erId, vo.getAfctCo2Emsn()));

        if (vo.getAfctCo2Emsn() == null) vo.setAfctCo2Emsn(BigDecimal.ZERO);
        vo.setErId(erId);
        vo.setGapSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        erDataGapMapper.insert(vo);
        return erDataGapMapper.selectOne(erId, nextSn);
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * 데이터 갭 수정.
     * thrshld5pctXcYn 은 사용자 입력값을 무시하고 자동 재계산.
     *
     * @param erId  ER ID
     * @param gapSn 데이터 갭 일련번호
     * @param vo    수정 데이터
     * @param user  로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void update(String erId, int gapSn, ErDataGapVO vo, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        ErDataGapVO existing = erDataGapMapper.selectOne(erId, gapSn);
        if (existing == null) throw BusinessException.notFound("데이터 갭");

        validateFields(vo);

        // 5% 임계치 자동 재판정 (사용자 입력값 무시)
        vo.setThrshld5pctXcYn(calcThreshold(erId, vo.getAfctCo2Emsn()));

        if (vo.getAfctCo2Emsn() == null) vo.setAfctCo2Emsn(BigDecimal.ZERO);
        vo.setErId(erId);
        vo.setGapSn(gapSn);
        vo.setLastChgUserId(user.getUserId());

        int affected = erDataGapMapper.update(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 데이터 갭이 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * 데이터 갭 소프트삭제.
     *
     * @param erId  ER ID
     * @param gapSn 데이터 갭 일련번호
     * @param user  로그인 사용자
     * @throws BusinessException NOT_FOUND — 항목 미존재
     */
    @Transactional
    public void softDelete(String erId, int gapSn, IcasUser user) {
        ErVO er = loadEr(erId);
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        assertDraft(er);

        int affected = erDataGapMapper.softDelete(erId, gapSn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("데이터 갭");
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

    private void validateFields(ErDataGapVO vo) {
        if (vo.getAfctCo2Emsn() != null && vo.getAfctCo2Emsn().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("갭 영향 CO₂ 배출량(afctCo2Emsn)은 0 이상이어야 합니다.");
        }
        if (vo.getRefInfo() != null && vo.getRefInfo().length() > 500) {
            throw BusinessException.badRequest("참조 정보(refInfo)는 최대 500자입니다. 입력 길이: " + vo.getRefInfo().length());
        }
        if (vo.getGapCauseCd() != null && vo.getGapCauseCd().length() > 20) {
            throw BusinessException.badRequest("갭 원인 코드(gapCauseCd)는 최대 20자입니다.");
        }
        if (vo.getGapTypeCd() != null && vo.getGapTypeCd().length() > 20) {
            throw BusinessException.badRequest("갭 유형 코드(gapTypeCd)는 최대 20자입니다.");
        }
    }

    /**
     * 5% 임계치 자동 판정.
     *
     * <p>알고리즘:
     * <ol>
     *   <li>해당 er_id 의 cntry_pair_co2 전체 CO₂ 합계 조회 (COALESCE → 0)</li>
     *   <li>totalCo2 = 0 → 정보 부족 → 'Y' (안전한 쪽)</li>
     *   <li>afctCo2 / totalCo2 ≥ 5% → 'Y', 미만 → 'N'</li>
     * </ol>
     *
     * @param erId      ER ID
     * @param afctCo2   갭 영향 CO₂ (null 이면 0 으로 처리)
     * @return 'Y' 또는 'N'
     */
    private String calcThreshold(String erId, BigDecimal afctCo2) {
        BigDecimal totalCo2 = erCntryPairCo2Mapper.sumCo2EmsnByEr(erId);

        // COALESCE 처리 결과이므로 null 은 없지만 방어적으로 처리
        if (totalCo2 == null) totalCo2 = BigDecimal.ZERO;

        // totalCo2 = 0 → 정보 부족 → 안전한 쪽(Y)
        if (totalCo2.compareTo(BigDecimal.ZERO) == 0) {
            return "Y";
        }

        BigDecimal afct = (afctCo2 == null) ? BigDecimal.ZERO : afctCo2;

        // afct / total ≥ 0.05 → 'Y'
        BigDecimal ratio = afct.divide(totalCo2, 10, RoundingMode.HALF_UP);
        return ratio.compareTo(THRESHOLD_5PCT) >= 0 ? "Y" : "N";
    }
}
