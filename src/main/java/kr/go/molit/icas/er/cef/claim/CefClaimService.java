package kr.go.molit.icas.er.cef.claim;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.CefMapper;
import kr.go.molit.icas.er.cef.CefService;
import kr.go.molit.icas.er.cef.claim.domain.CefClaimVO;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.cef.validate.CefDoubleClaimingValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * CEF 청구건 비즈니스 서비스 (er.tn_cef_claim, SFR-017).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 CEF 가 DRAFT 일 때만 추가/수정/삭제</li>
 *   <li>{@code claim_no} 는 사용자 입력 — 같은 cef_id 내 중복 금지</li>
 *   <li>batch_id_no 입력 시 {@link CefDoubleClaimingValidator} 로 BLOCKED 차단</li>
 *   <li>추가/수정/삭제 후 부모 ttl_redu_amt 재계산</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CefClaimService {

    private final CefClaimMapper             cefClaimMapper;
    private final CefMapper                  cefMapper;
    private final CefService                 cefService;
    private final CefDoubleClaimingValidator doubleClaimingValidator;
    private final DataScopeValidator         dataScopeValidator;

    public List<CefClaimVO> list(String cefId, IcasUser user) {
        CefVO cef = loadCefForRead(cefId, user);
        return cefClaimMapper.selectByCefId(cefId);
    }

    public CefClaimVO getOne(String cefId, String claimNo, IcasUser user) {
        loadCefForRead(cefId, user);
        CefClaimVO vo = cefClaimMapper.selectOne(cefId, claimNo);
        if (vo == null) throw BusinessException.notFound("CEF 청구건");
        return vo;
    }

    @Transactional
    public CefClaimVO add(String cefId, CefClaimVO vo, IcasUser user) {
        CefVO cef = cefService.assertCefDraftForChildEdit(cefId, user);
        validateClaim(vo, true);

        if (cefClaimMapper.existsClaimNo(cefId, vo.getClaimNo())) {
            throw BusinessException.conflict("이미 동일한 claim_no 가 등록되어 있습니다: " + vo.getClaimNo());
        }

        // 이중청구 BLOCKED 검사 (신규 — exclude 키 없음)
        doubleClaimingValidator.assertNotBlocked(
                vo.getBatchIdNo(), cef.getOprtrId(), null, null);

        vo.setCefId(cefId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        cefClaimMapper.insertClaim(vo);
        recalcParent(cefId, user);
        return cefClaimMapper.selectOne(cefId, vo.getClaimNo());
    }

    @Transactional
    public void update(String cefId, String claimNo, CefClaimVO vo, IcasUser user) {
        CefVO cef = cefService.assertCefDraftForChildEdit(cefId, user);

        CefClaimVO existing = cefClaimMapper.selectOne(cefId, claimNo);
        if (existing == null) throw BusinessException.notFound("CEF 청구건");

        validateClaim(vo, false);

        // 이중청구 검사 (자기 자신 제외)
        doubleClaimingValidator.assertNotBlocked(
                vo.getBatchIdNo(), cef.getOprtrId(), cefId, claimNo);

        vo.setCefId(cefId);
        vo.setClaimNo(claimNo);
        vo.setLastChgUserId(user.getUserId());

        int affected = cefClaimMapper.updateClaim(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 청구건이 존재하지 않거나 만료되었습니다.");
        recalcParent(cefId, user);
    }

    @Transactional
    public void softDelete(String cefId, String claimNo, IcasUser user) {
        cefService.assertCefDraftForChildEdit(cefId, user);
        int affected = cefClaimMapper.softDeleteOne(cefId, claimNo, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("CEF 청구건");
        recalcParent(cefId, user);
    }

    // ── Private ──────────────────────────────────────────

    private CefVO loadCefForRead(String cefId, IcasUser user) {
        CefVO cef = cefMapper.selectByCefId(cefId);
        if (cef == null) throw BusinessException.notFound("CEF");
        dataScopeValidator.assertOprtrAccessible(user, cef.getOprtrId(), cef.getRprtYr());
        return cef;
    }

    private void validateClaim(CefClaimVO vo, boolean checkClaimNo) {
        if (checkClaimNo && isBlank(vo.getClaimNo())) {
            throw BusinessException.badRequest("청구건 번호(claimNo)는 필수입니다.");
        }
        if (isBlank(vo.getBatchIdNo())) {
            throw BusinessException.badRequest("배치 ID(batchIdNo)는 필수입니다.");
        }
        if (isBlank(vo.getFuelTypeCd())) {
            throw BusinessException.badRequest("연료 유형 코드(fuelTypeCd)는 필수입니다.");
        }
        if (vo.getPureFuelMass() == null || vo.getPureFuelMass().signum() <= 0) {
            throw BusinessException.badRequest("순수 연료 질량(pureFuelMass)은 0 보다 커야 합니다.");
        }
        if (vo.getPureFuelPurchDt() == null) {
            throw BusinessException.badRequest("순수 연료 구매일(pureFuelPurchDt)은 필수입니다.");
        }
        if (vo.getBatchPurchRatio() != null) {
            BigDecimal r = vo.getBatchPurchRatio();
            if (r.signum() < 0 || r.compareTo(BigDecimal.ONE) > 0) {
                throw BusinessException.badRequest("배치 구매 비율(batchPurchRatio)은 0~1 범위여야 합니다.");
            }
        }
    }

    private void recalcParent(String cefId, IcasUser user) {
        BigDecimal sum = cefMapper.sumClaimMass(cefId);
        cefMapper.updateTtlReduAmt(cefId, sum == null ? BigDecimal.ZERO : sum, user.getUserId());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
