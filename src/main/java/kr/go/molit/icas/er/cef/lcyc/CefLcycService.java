package kr.go.molit.icas.er.cef.lcyc;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.CefMapper;
import kr.go.molit.icas.er.cef.CefService;
import kr.go.molit.icas.er.cef.claim.CefClaimMapper;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.cef.lcyc.domain.CefLcycVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * CEF 수명주기 배출량 비즈니스 서비스 (er.tn_cef_lcyc, SFR-018).
 *
 * <p>claim 당 0..1 행 (옵셔널). saveOrUpdate 시멘틱: 이미 있으면 UPDATE, 없으면 INSERT.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CefLcycService {

    private static final Set<String> VALID_LCA_SE_CD = Set.of("DEFAULT", "ACTUAL");

    private final CefLcycMapper      cefLcycMapper;
    private final CefClaimMapper     cefClaimMapper;
    private final CefMapper          cefMapper;
    private final CefService         cefService;
    private final DataScopeValidator dataScopeValidator;

    public CefLcycVO getOne(String cefId, String claimNo, IcasUser user) {
        loadCefForRead(cefId, user);
        return cefLcycMapper.selectOne(cefId, claimNo);
    }

    /** saveOrUpdate. 청구건 존재 검증 후 INSERT 또는 UPDATE. */
    @Transactional
    public CefLcycVO save(String cefId, String claimNo, CefLcycVO vo, IcasUser user) {
        cefService.assertCefDraftForChildEdit(cefId, user);
        if (cefClaimMapper.selectOne(cefId, claimNo) == null) {
            throw BusinessException.notFound("CEF 청구건");
        }
        validate(vo);

        vo.setCefId(cefId);
        vo.setClaimNo(claimNo);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        CefLcycVO existing = cefLcycMapper.selectOne(cefId, claimNo);
        if (existing == null) {
            cefLcycMapper.insertLcyc(vo);
        } else {
            cefLcycMapper.updateLcyc(vo);
        }
        return cefLcycMapper.selectOne(cefId, claimNo);
    }

    @Transactional
    public void softDelete(String cefId, String claimNo, IcasUser user) {
        cefService.assertCefDraftForChildEdit(cefId, user);
        int affected = cefLcycMapper.softDeleteOne(cefId, claimNo, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("CEF 수명주기 항목");
    }

    private CefVO loadCefForRead(String cefId, IcasUser user) {
        CefVO cef = cefMapper.selectByCefId(cefId);
        if (cef == null) throw BusinessException.notFound("CEF");
        dataScopeValidator.assertOprtrAccessible(user, cef.getOprtrId(), cef.getRprtYr());
        return cef;
    }

    private void validate(CefLcycVO vo) {
        if (vo.getLcaValueSeCd() == null || !VALID_LCA_SE_CD.contains(vo.getLcaValueSeCd())) {
            throw BusinessException.badRequest(
                    "LCA 값 구분 코드(lcaValueSeCd) 허용값: DEFAULT, ACTUAL. 입력값: " + vo.getLcaValueSeCd());
        }
    }
}
