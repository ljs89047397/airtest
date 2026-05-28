package kr.go.molit.icas.er.cef.spchn;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.CefMapper;
import kr.go.molit.icas.er.cef.CefService;
import kr.go.molit.icas.er.cef.claim.CefClaimMapper;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.cef.spchn.domain.CefSpchnVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * CEF 공급망 비즈니스 서비스 (er.tn_cef_spchn, SFR-019).
 *
 * <p>claim 당 1:N. chn_sn 은 max+1 자동 채번.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CefSpchnService {

    private static final Set<String> VALID_ROLE_CD = Set.of("MID_BUYER", "SHIPPER", "BLENDER");

    private final CefSpchnMapper     cefSpchnMapper;
    private final CefClaimMapper     cefClaimMapper;
    private final CefMapper          cefMapper;
    private final CefService         cefService;
    private final DataScopeValidator dataScopeValidator;

    public List<CefSpchnVO> list(String cefId, String claimNo, IcasUser user) {
        loadCefForRead(cefId, user);
        return cefSpchnMapper.selectByClaim(cefId, claimNo);
    }

    public CefSpchnVO getOne(String cefId, String claimNo, int chnSn, IcasUser user) {
        loadCefForRead(cefId, user);
        CefSpchnVO vo = cefSpchnMapper.selectOne(cefId, claimNo, chnSn);
        if (vo == null) throw BusinessException.notFound("CEF 공급망 항목");
        return vo;
    }

    @Transactional
    public CefSpchnVO add(String cefId, String claimNo, CefSpchnVO vo, IcasUser user) {
        cefService.assertCefDraftForChildEdit(cefId, user);
        if (cefClaimMapper.selectOne(cefId, claimNo) == null) {
            throw BusinessException.notFound("CEF 청구건");
        }
        validate(vo);

        int nextSn = cefSpchnMapper.selectNextSn(cefId, claimNo);
        vo.setCefId(cefId);
        vo.setClaimNo(claimNo);
        vo.setChnSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        cefSpchnMapper.insertSpchn(vo);
        return cefSpchnMapper.selectOne(cefId, claimNo, nextSn);
    }

    @Transactional
    public void update(String cefId, String claimNo, int chnSn, CefSpchnVO vo, IcasUser user) {
        cefService.assertCefDraftForChildEdit(cefId, user);
        if (cefSpchnMapper.selectOne(cefId, claimNo, chnSn) == null) {
            throw BusinessException.notFound("CEF 공급망 항목");
        }
        validate(vo);

        vo.setCefId(cefId);
        vo.setClaimNo(claimNo);
        vo.setChnSn(chnSn);
        vo.setLastChgUserId(user.getUserId());

        int affected = cefSpchnMapper.updateSpchn(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 공급망 항목이 존재하지 않거나 만료되었습니다.");
    }

    @Transactional
    public void softDelete(String cefId, String claimNo, int chnSn, IcasUser user) {
        cefService.assertCefDraftForChildEdit(cefId, user);
        int affected = cefSpchnMapper.softDeleteOne(cefId, claimNo, chnSn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("CEF 공급망 항목");
    }

    // ── Private ──

    private CefVO loadCefForRead(String cefId, IcasUser user) {
        CefVO cef = cefMapper.selectByCefId(cefId);
        if (cef == null) throw BusinessException.notFound("CEF");
        dataScopeValidator.assertOprtrAccessible(user, cef.getOprtrId(), cef.getRprtYr());
        return cef;
    }

    private void validate(CefSpchnVO vo) {
        if (vo.getSplyChnRoleCd() == null || !VALID_ROLE_CD.contains(vo.getSplyChnRoleCd())) {
            throw BusinessException.badRequest(
                    "공급망 역할 코드(splyChnRoleCd) 허용값: MID_BUYER, SHIPPER, BLENDER. 입력값: "
                            + vo.getSplyChnRoleCd());
        }
        if (vo.getBlndRatio() != null) {
            BigDecimal r = vo.getBlndRatio();
            if (r.signum() < 0 || r.compareTo(BigDecimal.ONE) > 0) {
                throw BusinessException.badRequest("혼합비율(blndRatio)은 0~1 범위여야 합니다.");
            }
        }
    }
}
