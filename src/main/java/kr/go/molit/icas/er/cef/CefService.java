package kr.go.molit.icas.er.cef;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.cef.domain.CefSearch;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * CEF(CORSIA Eligible Fuel) 마스터 비즈니스 서비스 (SFR-017/020).
 *
 * <h2>라이프사이클</h2>
 * <pre>
 *   DRAFT → submit  → SBMTD → approve → APRVD → cancel → CNCLD
 *                        └ reject → DRAFT
 * </pre>
 *
 * <h2>권한·가시범위</h2>
 * <ul>
 *   <li>AIRLINE — 본인 oprtr_id, DRAFT 한정 수정·삭제, submit 가능</li>
 *   <li>KOTSA   — approve / reject</li>
 *   <li>MOLIT   — cancel</li>
 *   <li>VERIFIER — vrfcn_assgn 배정 운영사만 조회</li>
 * </ul>
 *
 * <h2>합계 자동 재계산</h2>
 * <p>{@link #recalcTtlRedu(String, IcasUser)} 는 자식 청구건 변경 시 호출하여
 * {@code ttl_redu_amt} 를 갱신.
 * 1차 산식: {@code SUM(pure_fuel_mass)} (LCA 정교화는 SFR-018 후속).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CefService {

    private static final String CEF_ID_PREFIX = "CEF";

    private final CefMapper          cefMapper;
    private final ErMapper           erMapper;
    private final OprtrMapper        oprtrMapper;
    private final DataScopeValidator dataScopeValidator;
    private final IdGenerator        idGenerator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    public PageResponse<CefVO> searchCefs(CefSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전체 가시
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else if (user.isVerifier()) {
            search.setVerifierScope(true);
            search.setVrfcnInstId(user.getVrfcnInstId());
        } else {
            throw BusinessException.forbidden("CEF 조회 권한이 없습니다.");
        }
        long total = cefMapper.countCefs(search);
        List<CefVO> rows = cefMapper.selectCefs(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    public CefVO getCef(String cefId, IcasUser user) {
        CefVO cef = cefMapper.selectByCefId(cefId);
        if (cef == null) throw BusinessException.notFound("CEF");
        dataScopeValidator.assertOprtrAccessible(user, cef.getOprtrId(), cef.getRprtYr());
        return cef;
    }

    public CefVO getByErId(String erId, IcasUser user) {
        CefVO cef = cefMapper.selectByErId(erId);
        if (cef == null) throw BusinessException.notFound("CEF");
        dataScopeValidator.assertOprtrAccessible(user, cef.getOprtrId(), cef.getRprtYr());
        return cef;
    }

    // ══════════════════════════════════════════════════════
    // 신규 / 수정 / 삭제
    // ══════════════════════════════════════════════════════

    /**
     * CEF 신규 DRAFT 생성. ER 마스터 기반으로 oprtrId, rprtYr 자동 도출.
     *
     * @throws BusinessException FORBIDDEN  — AIRLINE 외 또는 타 운영사
     * @throws BusinessException NOT_FOUND  — ER 미존재
     * @throws BusinessException BAD_REQUEST— ER 가 DRAFT/SBMTD/APRVD 인데 이미 CEF 존재
     */
    @Transactional
    public CefVO createCef(String erId, IcasUser user) {
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        dataScopeValidator.assertOwnAirline(user, er.getOprtrId());

        validateOprtrExists(er.getOprtrId());

        // ER 1 : CEF 0..1 — UK 제약
        if (cefMapper.selectByErId(erId) != null) {
            throw BusinessException.conflict("해당 ER 에 이미 CEF 가 등록되어 있습니다.");
        }

        int seq = cefMapper.countByPrefix(CEF_ID_PREFIX) + 1;
        String cefId = idGenerator.managementPk(CEF_ID_PREFIX, seq);

        CefVO vo = new CefVO();
        vo.setCefId(cefId);
        vo.setErId(erId);
        vo.setOprtrId(er.getOprtrId());
        vo.setRprtYr(er.getRprtYr());
        vo.setCefStCd("DRAFT");
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        cefMapper.insertCef(vo);
        return cefMapper.selectByCefId(cefId);
    }

    @Transactional
    public void softDeleteCef(String cefId, IcasUser user) {
        CefVO cef = loadAndAssertOwn(cefId, user);
        assertStatus(cef, "DRAFT", "DRAFT 상태에서만 삭제할 수 있습니다.");
        int affected = cefMapper.softDeleteCef(cefId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("삭제 대상 CEF 가 DRAFT 상태가 아닙니다.");
    }

    // ══════════════════════════════════════════════════════
    // 합계 재계산
    // ══════════════════════════════════════════════════════

    /**
     * 청구건 변경(추가/수정/삭제) 시 호출. ttl_redu_amt 를 자식 합계로 갱신.
     *
     * <p>1차: {@code SUM(claim.pure_fuel_mass)}.
     * 향후 LCA 정교화 시 {@code (89 - ttl_lca_val)/89 × pure_fuel_mass} 적용 예정.
     */
    @Transactional
    public BigDecimal recalcTtlRedu(String cefId, IcasUser user) {
        CefVO cef = loadAndAssertOwn(cefId, user);
        BigDecimal sum = cefMapper.sumClaimMass(cefId);
        if (sum == null) sum = BigDecimal.ZERO;
        cefMapper.updateTtlReduAmt(cefId, sum, user.getUserId());
        return sum;
    }

    // ══════════════════════════════════════════════════════
    // 상태 전이
    // ══════════════════════════════════════════════════════

    @Transactional
    public void submit(String cefId, IcasUser user) {
        CefVO cef = loadAndAssertOwn(cefId, user);
        assertStatus(cef, "DRAFT", "DRAFT 상태에서만 제출할 수 있습니다.");
        // 제출 직전 합계 재계산 — 누락 방지
        BigDecimal sum = cefMapper.sumClaimMass(cefId);
        cefMapper.updateTtlReduAmt(cefId, sum == null ? BigDecimal.ZERO : sum, user.getUserId());
        int affected = cefMapper.updateSubmit(cefId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("제출 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    @Transactional
    public void approve(String cefId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertStatus(cefId, "SBMTD");
        int affected = cefMapper.updateApprove(cefId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("승인 처리에 실패했습니다.");
    }

    @Transactional
    public void reject(String cefId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertStatus(cefId, "SBMTD");
        int affected = cefMapper.updateCefStCd(cefId, "DRAFT", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("반려 처리에 실패했습니다.");
    }

    @Transactional
    public void cancel(String cefId, String reason, IcasUser user) {
        assertMolit(user);
        if (isBlank(reason)) throw BusinessException.badRequest("취소 사유는 필수입니다.");
        loadAndAssertStatus(cefId, "APRVD");
        int affected = cefMapper.updateCancel(cefId, reason, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("취소 처리에 실패했습니다.");
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers (자식 Service 에서도 사용)
    // ══════════════════════════════════════════════════════

    /** 자식 Service 가 부모 CEF 조회 + DRAFT 검증 시 사용. */
    public CefVO assertCefDraftForChildEdit(String cefId, IcasUser user) {
        CefVO cef = loadAndAssertOwn(cefId, user);
        if (!"DRAFT".equals(cef.getCefStCd())) {
            throw BusinessException.badRequest(
                    "DRAFT 상태의 CEF 에서만 청구건을 변경할 수 있습니다. 현재 상태: " + cef.getCefStCd());
        }
        return cef;
    }

    private CefVO loadAndAssertOwn(String cefId, IcasUser user) {
        CefVO cef = cefMapper.selectByCefId(cefId);
        if (cef == null) throw BusinessException.notFound("CEF");
        dataScopeValidator.assertOwnAirline(user, cef.getOprtrId());
        return cef;
    }

    private CefVO loadAndAssertStatus(String cefId, String expectedSt) {
        CefVO cef = cefMapper.selectByCefId(cefId);
        if (cef == null) throw BusinessException.notFound("CEF");
        assertStatus(cef, expectedSt,
                expectedSt + " 상태에서만 가능한 작업입니다. 현재 상태: " + cef.getCefStCd());
        return cef;
    }

    private void assertStatus(CefVO cef, String expected, String message) {
        if (!expected.equals(cef.getCefStCd())) {
            throw BusinessException.badRequest(message);
        }
    }

    private void assertMolit(IcasUser user) {
        if (user.isMaster()) return;
        if (!"MOLIT".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("국토부(MOLIT) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    private void assertKotsa(IcasUser user) {
        if (user.isMaster()) return;
        if (!"KOTSA".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("한국교통안전공단(KOTSA) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    private void validateOprtrExists(String oprtrId) {
        OprtrVO oprtr = oprtrMapper.selectByOprtrId(oprtrId);
        if (oprtr == null) {
            throw BusinessException.notFound("운영사(oprtrId=" + oprtrId + ")");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
