package kr.go.molit.icas.vr.scope;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.scope.domain.VrScopeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * VR 범위·식별 정보 서비스 — tn_vr_scope (1:1).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrScopeService {

    private final VrScopeMapper vrScopeMapper;
    private final VrService     vrService;

    public VrScopeVO get(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        VrScopeVO s = vrScopeMapper.selectByVrId(vrId);
        if (s == null) throw BusinessException.notFound("VR 범위 정보");
        return s;
    }

    @Transactional
    public VrScopeVO saveOrUpdate(String vrId, VrScopeVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        vo.setVrId(vrId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        VrScopeVO existing = vrScopeMapper.selectByVrId(vrId);
        if (existing == null) {
            vrScopeMapper.insertScope(vo);
        } else {
            vrScopeMapper.updateScope(vo);
        }
        return vrScopeMapper.selectByVrId(vrId);
    }
}
