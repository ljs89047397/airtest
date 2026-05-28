package kr.go.molit.icas.vr.ncnfrm;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.ncnfrm.domain.VrNcnfrmVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 부적합·허위진술 서비스 — tn_vr_ncnfrm (1:N).
 * 미해결(resol_dt IS NULL) 부적합이 있으면 tn_vr_cncls.final_opnn_cd = REASONABLE 이 차단됨.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrNcnfrmService {

    private final VrNcnfrmMapper vrNcnfrmMapper;
    private final VrService      vrService;

    public List<VrNcnfrmVO> list(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        return vrNcnfrmMapper.selectByVrId(vrId);
    }

    @Transactional
    public VrNcnfrmVO add(String vrId, VrNcnfrmVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        validateNcnfrm(vo);

        int nextNo = vrNcnfrmMapper.maxItemNo(vrId) + 1;
        vo.setVrId(vrId);
        vo.setItemNo(nextNo);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        vrNcnfrmMapper.insertNcnfrm(vo);
        return vrNcnfrmMapper.selectByPk(vrId, nextNo);
    }

    @Transactional
    public VrNcnfrmVO update(String vrId, int itemNo, VrNcnfrmVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        validateNcnfrm(vo);
        assertExists(vrId, itemNo);

        vo.setVrId(vrId);
        vo.setItemNo(itemNo);
        vo.setLastChgUserId(user.getUserId());
        vrNcnfrmMapper.updateNcnfrm(vo);
        return vrNcnfrmMapper.selectByPk(vrId, itemNo);
    }

    /** 해결 처리 — resolDescCn + resolDt 등록. DRAFT 상태 불필요 (제출 전 해결도 가능). */
    @Transactional
    public void resolve(String vrId, int itemNo, String resolDescCn, String resolDt, IcasUser user) {
        vrService.loadForRead(vrId, user);
        // VERIFIER 만 해결 처리 가능
        if (!user.isMaster() && !user.isVerifier())
            throw BusinessException.forbidden("검증기관 사용자만 부적합을 해결 처리할 수 있습니다.");
        assertExists(vrId, itemNo);
        if (resolDt == null || resolDt.isBlank())
            throw BusinessException.badRequest("해결 완료일(resolDt)은 필수입니다.");

        vrNcnfrmMapper.updateResolve(vrId, itemNo, resolDescCn, resolDt, user.getUserId());
    }

    @Transactional
    public void delete(String vrId, int itemNo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        assertExists(vrId, itemNo);
        vrNcnfrmMapper.deleteByPk(vrId, itemNo);
    }

    /** 미해결 건수 조회 — VrCnclsService 에서 REASONABLE 차단 판단용. */
    public int countUnresolved(String vrId) {
        return vrNcnfrmMapper.countUnresolved(vrId);
    }

    private void validateNcnfrm(VrNcnfrmVO vo) {
        if (vo.getDescCn() == null || vo.getDescCn().isBlank())
            throw BusinessException.badRequest("부적합 설명(descCn)은 필수입니다.");
        if (vo.getNcnfrmSeCd() == null ||
                !List.of("MINOR","MAJOR","MISSTATEMENT").contains(vo.getNcnfrmSeCd()))
            throw BusinessException.badRequest("부적합 구분(ncnfrmSeCd)은 MINOR / MAJOR / MISSTATEMENT 중 하나여야 합니다.");
    }

    private void assertExists(String vrId, int itemNo) {
        if (vrNcnfrmMapper.selectByPk(vrId, itemNo) == null)
            throw BusinessException.notFound("부적합(itemNo=" + itemNo + ")");
    }
}
