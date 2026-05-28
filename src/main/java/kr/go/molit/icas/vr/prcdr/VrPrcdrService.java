package kr.go.molit.icas.vr.prcdr;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.prcdr.domain.VrPrcdrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 검증 절차·분석 서비스 — tn_vr_prcdr (1:1).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrPrcdrService {

    private final VrPrcdrMapper vrPrcdrMapper;
    private final VrService     vrService;

    public VrPrcdrVO get(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        VrPrcdrVO p = vrPrcdrMapper.selectByVrId(vrId);
        if (p == null) throw BusinessException.notFound("VR 절차·분석 정보");
        return p;
    }

    @Transactional
    public VrPrcdrVO saveOrUpdate(String vrId, VrPrcdrVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        vo.setVrId(vrId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        VrPrcdrVO existing = vrPrcdrMapper.selectByVrId(vrId);
        if (existing == null) {
            vrPrcdrMapper.insertPrcdr(vo);
        } else {
            vrPrcdrMapper.updatePrcdr(vo);
        }
        return vrPrcdrMapper.selectByVrId(vrId);
    }
}
