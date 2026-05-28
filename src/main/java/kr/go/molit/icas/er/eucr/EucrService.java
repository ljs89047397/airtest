package kr.go.molit.icas.er.eucr;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.er.eucr.domain.EucrSearch;
import kr.go.molit.icas.er.eucr.domain.EucrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * EUCR(Emission Unit Cancellation Report) 마스터 서비스 (SFR-030).
 *
 * <h2>라이프사이클 (ER 동일)</h2>
 * <pre>
 *   DRAFT → submit → SBMTD → review → RVWNG
 *                                ├ reject    → DRAFT
 *                                └ recommend → RCMDD → approve → APRVD → cancel → CNCLD
 * </pre>
 *
 * <h2>자동 산출</h2>
 * <ul>
 *   <li>{@code ttl_qty} — SUM(batch.sub_qty)</li>
 *   <li>{@code fulfilled_yn} — ttl_qty ≥ ofst_req_qty 시 'Y'</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EucrService {

    private static final String EUCR_ID_PREFIX = "EUCR";

    private final EucrMapper         eucrMapper;
    private final OprtrMapper        oprtrMapper;
    private final DataScopeValidator dataScopeValidator;
    private final IdGenerator        idGenerator;

    // ── 조회 ──

    public PageResponse<EucrVO> searchEucrs(EucrSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전체
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else if (user.isVerifier()) {
            search.setVerifierScope(true);
            search.setVrfcnInstId(user.getVrfcnInstId());
        } else {
            throw BusinessException.forbidden("EUCR 조회 권한이 없습니다.");
        }
        long total = eucrMapper.countEucrs(search);
        List<EucrVO> rows = eucrMapper.selectEucrs(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    public EucrVO getEucr(String eucrId, IcasUser user) {
        EucrVO e = eucrMapper.selectByEucrId(eucrId);
        if (e == null) throw BusinessException.notFound("EUCR");
        dataScopeValidator.assertOprtrAccessible(user, e.getOprtrId(), e.getRprtYr());
        return e;
    }

    // ── 신규 / 수정 / 삭제 ──

    @Transactional
    public EucrVO createEucr(EucrVO vo, IcasUser user) {
        dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());
        validateRequired(vo);
        validateOprtrExists(vo.getOprtrId());

        int seq = eucrMapper.countByPrefix(EUCR_ID_PREFIX) + 1;
        String eucrId = idGenerator.managementPk(EUCR_ID_PREFIX, seq);
        String ver    = nextEucrVer(vo.getOprtrId(), vo.getRprtYr());

        vo.setEucrId(eucrId);
        vo.setEucrVer(ver);
        vo.setEucrStCd("DRAFT");
        if (vo.getOfstReqQty() == null) vo.setOfstReqQty(BigDecimal.ZERO);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        eucrMapper.insertEucr(vo);
        return eucrMapper.selectByEucrId(eucrId);
    }

    @Transactional
    public void updateOfstReqQty(String eucrId, BigDecimal ofstReqQty, IcasUser user) {
        EucrVO e = loadAndAssertOwn(eucrId, user);
        assertStatus(e, "DRAFT", "DRAFT 상태에서만 의무량을 수정할 수 있습니다.");
        if (ofstReqQty == null || ofstReqQty.signum() < 0) {
            throw BusinessException.badRequest("상쇄 의무량(ofstReqQty)은 0 이상이어야 합니다.");
        }
        int affected = eucrMapper.updateOfstReqQty(eucrId, ofstReqQty, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("수정 대상 EUCR 이 DRAFT 상태가 아닙니다.");
        // 의무량 변경 후 충족 여부 재판정
        recalcTtlAndFulfilled(eucrId, user);
    }

    @Transactional
    public void softDeleteEucr(String eucrId, IcasUser user) {
        EucrVO e = loadAndAssertOwn(eucrId, user);
        assertStatus(e, "DRAFT", "DRAFT 상태에서만 삭제할 수 있습니다.");
        int affected = eucrMapper.softDeleteEucr(eucrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("삭제 대상 EUCR 이 DRAFT 상태가 아닙니다.");
    }

    // ── 합계 재계산 + 의무 충족 판정 ──

    /**
     * 자식 batch 변경 시 호출. ttl_qty 및 fulfilled_yn 갱신.
     *
     * @return 갱신 후 결과 {@code {ttlQty, fulfilledYn}}
     */
    @Transactional
    public EucrVO recalcTtlAndFulfilled(String eucrId, IcasUser user) {
        EucrVO e = loadAndAssertOwn(eucrId, user);
        BigDecimal sum = eucrMapper.sumBatchQty(eucrId);
        if (sum == null) sum = BigDecimal.ZERO;

        BigDecimal req = e.getOfstReqQty() == null ? BigDecimal.ZERO : e.getOfstReqQty();
        String fulfilled = sum.compareTo(req) >= 0 ? "Y" : "N";

        eucrMapper.updateTtlQtyAndFulfilled(eucrId, sum, fulfilled, user.getUserId());
        return eucrMapper.selectByEucrId(eucrId);
    }

    // ── 상태 전이 ──

    @Transactional
    public void submit(String eucrId, IcasUser user) {
        EucrVO e = loadAndAssertOwn(eucrId, user);
        assertStatus(e, "DRAFT", "DRAFT 상태에서만 제출할 수 있습니다.");
        // 제출 직전 합계 재산출
        recalcTtlAndFulfilled(eucrId, user);
        int affected = eucrMapper.updateSubmit(eucrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("제출 처리에 실패했습니다.");
    }

    @Transactional
    public void review(String eucrId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertStatus(eucrId, "SBMTD");
        int affected = eucrMapper.updateEucrStCd(eucrId, "RVWNG", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("검토 진입 처리에 실패했습니다.");
    }

    @Transactional
    public void reject(String eucrId, String reason, IcasUser user) {
        assertKotsa(user);
        if (isBlank(reason)) throw BusinessException.badRequest("반려 사유는 필수입니다.");
        loadAndAssertStatus(eucrId, "RVWNG");
        int affected = eucrMapper.updateEucrStCd(eucrId, "DRAFT", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("반려 처리에 실패했습니다.");
    }

    @Transactional
    public void recommend(String eucrId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertStatus(eucrId, "RVWNG");
        int affected = eucrMapper.updateEucrStCd(eucrId, "RCMDD", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("권고 처리에 실패했습니다.");
    }

    @Transactional
    public void approve(String eucrId, IcasUser user) {
        assertMolit(user);
        EucrVO e = eucrMapper.selectByEucrId(eucrId);
        if (e == null) throw BusinessException.notFound("EUCR");
        if (!"RVWNG".equals(e.getEucrStCd()) && !"RCMDD".equals(e.getEucrStCd())) {
            throw BusinessException.badRequest(
                    "승인은 RVWNG 또는 RCMDD 상태에서만 가능합니다. 현재 상태: " + e.getEucrStCd());
        }
        int affected = eucrMapper.updateApprove(eucrId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("승인 처리에 실패했습니다.");
    }

    @Transactional
    public void cancel(String eucrId, String reason, IcasUser user) {
        assertMolit(user);
        if (isBlank(reason)) throw BusinessException.badRequest("취소 사유는 필수입니다.");
        loadAndAssertStatus(eucrId, "APRVD");
        int affected = eucrMapper.updateCancel(eucrId, reason, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("취소 처리에 실패했습니다.");
    }

    // ── 자식 Service 용 헬퍼 ──

    /** DRAFT 상태인지 검증 후 EUCR 반환 (자식이 호출). */
    public EucrVO assertEucrDraftForChildEdit(String eucrId, IcasUser user) {
        EucrVO e = loadAndAssertOwn(eucrId, user);
        if (!"DRAFT".equals(e.getEucrStCd())) {
            throw BusinessException.badRequest(
                    "DRAFT 상태의 EUCR 에서만 자식 데이터를 변경할 수 있습니다. 현재 상태: " + e.getEucrStCd());
        }
        return e;
    }

    /** 합계 재계산 — 자식 Service 가 변경 후 호출. */
    public void recalcAfterChildChange(String eucrId, IcasUser user) {
        recalcTtlAndFulfilled(eucrId, user);
    }

    // ── Private ──

    private EucrVO loadAndAssertOwn(String eucrId, IcasUser user) {
        EucrVO e = eucrMapper.selectByEucrId(eucrId);
        if (e == null) throw BusinessException.notFound("EUCR");
        dataScopeValidator.assertOwnAirline(user, e.getOprtrId());
        return e;
    }

    private EucrVO loadAndAssertStatus(String eucrId, String expectedSt) {
        EucrVO e = eucrMapper.selectByEucrId(eucrId);
        if (e == null) throw BusinessException.notFound("EUCR");
        assertStatus(e, expectedSt,
                expectedSt + " 상태에서만 가능한 작업입니다. 현재 상태: " + e.getEucrStCd());
        return e;
    }

    private void assertStatus(EucrVO e, String expected, String message) {
        if (!expected.equals(e.getEucrStCd())) {
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
        OprtrVO o = oprtrMapper.selectByOprtrId(oprtrId);
        if (o == null) throw BusinessException.notFound("운영사(oprtrId=" + oprtrId + ")");
    }

    private void validateRequired(EucrVO vo) {
        if (isBlank(vo.getOprtrId())) throw BusinessException.badRequest("운영사 ID(oprtrId)는 필수입니다.");
        if (isBlank(vo.getRprtYr()))  throw BusinessException.badRequest("보고연도(rprtYr)는 필수입니다.");
        if (!vo.getRprtYr().matches("\\d{4}")) {
            throw BusinessException.badRequest("보고연도(rprtYr)는 4자리 숫자여야 합니다.");
        }
        if (vo.getOfstReqQty() != null && vo.getOfstReqQty().signum() < 0) {
            throw BusinessException.badRequest("상쇄 의무량(ofstReqQty)은 0 이상이어야 합니다.");
        }
    }

    private String nextEucrVer(String oprtrId, String rprtYr) {
        String maxVer = eucrMapper.selectMaxEucrVer(oprtrId, rprtYr);
        if (maxVer == null || maxVer.startsWith("null")) return "1.0";
        try {
            return (Integer.parseInt(maxVer.split("\\.")[0]) + 1) + ".0";
        } catch (NumberFormatException ex) {
            return "1.0";
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
