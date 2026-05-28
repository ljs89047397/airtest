package kr.go.molit.icas.vr.cncls;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.cncls.domain.VrCnclsVO;
import kr.go.molit.icas.vr.ncnfrm.VrNcnfrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * VR 결론·검증 의견 서비스 — tn_vr_cncls (1:1).
 *
 * <p>SFR-027: 미해결 부적합(resol_dt IS NULL) 이 있으면 final_opnn_cd = REASONABLE 선택 차단.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrCnclsService {

    private static final Set<String> VALID_OPNN = Set.of("REASONABLE","LIMITED","QUALIFIED","ADVERSE");

    private final VrCnclsMapper  vrCnclsMapper;
    private final VrService      vrService;
    private final VrNcnfrmService vrNcnfrmService;

    public VrCnclsVO get(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        VrCnclsVO c = vrCnclsMapper.selectByVrId(vrId);
        if (c == null) throw BusinessException.notFound("VR 결론 정보");
        return c;
    }

    @Transactional
    public VrCnclsVO saveOrUpdate(String vrId, VrCnclsVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        validateOpnn(vo.getFinalOpnnCd());

        // REASONABLE: 미해결 부적합 차단 (SFR-027)
        if ("REASONABLE".equals(vo.getFinalOpnnCd())) {
            int unresolved = vrNcnfrmService.countUnresolved(vrId);
            if (unresolved > 0) {
                throw BusinessException.badRequest(
                        "미해결 부적합 " + unresolved + "건이 있으면 'REASONABLE(적정)' 의견을 선택할 수 없습니다.");
            }
        }

        vo.setVrId(vrId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        VrCnclsVO existing = vrCnclsMapper.selectByVrId(vrId);
        if (existing == null) {
            vrCnclsMapper.insertCncls(vo);
        } else {
            vrCnclsMapper.updateCncls(vo);
        }
        return vrCnclsMapper.selectByVrId(vrId);
    }

    private void validateOpnn(String opnnCd) {
        if (opnnCd == null || !VALID_OPNN.contains(opnnCd)) {
            throw BusinessException.badRequest(
                    "최종의견(finalOpnnCd)은 REASONABLE / LIMITED / QUALIFIED / ADVERSE 중 하나여야 합니다.");
        }
    }
}
