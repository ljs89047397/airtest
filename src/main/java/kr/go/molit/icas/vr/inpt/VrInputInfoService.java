package kr.go.molit.icas.vr.inpt;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.inpt.domain.VrInputInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 검증 입력자료 서비스 — tn_vr_input_info (1:N).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrInputInfoService {

    private final VrInputInfoMapper vrInputInfoMapper;
    private final VrService         vrService;

    public List<VrInputInfoVO> list(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        return vrInputInfoMapper.selectByVrId(vrId);
    }

    @Transactional
    public VrInputInfoVO add(String vrId, VrInputInfoVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        if (vo.getDocNm() == null || vo.getDocNm().isBlank())
            throw BusinessException.badRequest("자료명(docNm)은 필수입니다.");

        int nextSn = vrInputInfoMapper.maxInputSn(vrId) + 1;
        vo.setVrId(vrId);
        vo.setInputSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        vrInputInfoMapper.insertInputInfo(vo);
        return vrInputInfoMapper.selectByPk(vrId, nextSn);
    }

    @Transactional
    public VrInputInfoVO update(String vrId, int inputSn, VrInputInfoVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        if (vrInputInfoMapper.selectByPk(vrId, inputSn) == null)
            throw BusinessException.notFound("입력자료(inputSn=" + inputSn + ")");

        vo.setVrId(vrId);
        vo.setInputSn(inputSn);
        vo.setLastChgUserId(user.getUserId());
        vrInputInfoMapper.updateInputInfo(vo);
        return vrInputInfoMapper.selectByPk(vrId, inputSn);
    }

    @Transactional
    public void delete(String vrId, int inputSn, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        if (vrInputInfoMapper.selectByPk(vrId, inputSn) == null)
            throw BusinessException.notFound("입력자료(inputSn=" + inputSn + ")");
        vrInputInfoMapper.deleteByPk(vrId, inputSn);
    }
}
