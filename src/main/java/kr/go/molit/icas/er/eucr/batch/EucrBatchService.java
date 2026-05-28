package kr.go.molit.icas.er.eucr.batch;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.eucr.EucrMapper;
import kr.go.molit.icas.er.eucr.EucrService;
import kr.go.molit.icas.er.eucr.batch.domain.EucrBatchVO;
import kr.go.molit.icas.er.eucr.domain.EucrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EUCR 배출권 배치 서비스 (er.tn_eucr_batch, SFR-031).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 EUCR DRAFT 한정 변경</li>
 *   <li>batch_no 사용자 입력 — 같은 eucr_id 내 중복 금지</li>
 *   <li>sub_qty 필수, &gt; 0</li>
 *   <li>변경 후 부모 ttl_qty / fulfilled_yn 재계산</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EucrBatchService {

    private final EucrBatchMapper    eucrBatchMapper;
    private final EucrMapper         eucrMapper;
    private final EucrService        eucrService;
    private final DataScopeValidator dataScopeValidator;

    public List<EucrBatchVO> list(String eucrId, IcasUser user) {
        loadEucrForRead(eucrId, user);
        return eucrBatchMapper.selectByEucrId(eucrId);
    }

    public EucrBatchVO getOne(String eucrId, String batchNo, IcasUser user) {
        loadEucrForRead(eucrId, user);
        EucrBatchVO vo = eucrBatchMapper.selectOne(eucrId, batchNo);
        if (vo == null) throw BusinessException.notFound("EUCR 배치");
        return vo;
    }

    @Transactional
    public EucrBatchVO add(String eucrId, EucrBatchVO vo, IcasUser user) {
        eucrService.assertEucrDraftForChildEdit(eucrId, user);
        validate(vo, true);

        if (eucrBatchMapper.existsBatchNo(eucrId, vo.getBatchNo())) {
            throw BusinessException.conflict("이미 동일한 batch_no 가 등록되어 있습니다: " + vo.getBatchNo());
        }

        vo.setEucrId(eucrId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        eucrBatchMapper.insertBatch(vo);
        eucrService.recalcAfterChildChange(eucrId, user);
        return eucrBatchMapper.selectOne(eucrId, vo.getBatchNo());
    }

    @Transactional
    public void update(String eucrId, String batchNo, EucrBatchVO vo, IcasUser user) {
        eucrService.assertEucrDraftForChildEdit(eucrId, user);
        EucrBatchVO existing = eucrBatchMapper.selectOne(eucrId, batchNo);
        if (existing == null) throw BusinessException.notFound("EUCR 배치");
        validate(vo, false);

        vo.setEucrId(eucrId);
        vo.setBatchNo(batchNo);
        vo.setLastChgUserId(user.getUserId());

        int affected = eucrBatchMapper.updateBatch(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 배치가 존재하지 않거나 만료되었습니다.");
        eucrService.recalcAfterChildChange(eucrId, user);
    }

    @Transactional
    public void softDelete(String eucrId, String batchNo, IcasUser user) {
        eucrService.assertEucrDraftForChildEdit(eucrId, user);
        int affected = eucrBatchMapper.softDeleteOne(eucrId, batchNo, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("EUCR 배치");
        eucrService.recalcAfterChildChange(eucrId, user);
    }

    // ── Private ──

    private EucrVO loadEucrForRead(String eucrId, IcasUser user) {
        EucrVO e = eucrMapper.selectByEucrId(eucrId);
        if (e == null) throw BusinessException.notFound("EUCR");
        dataScopeValidator.assertOprtrAccessible(user, e.getOprtrId(), e.getRprtYr());
        return e;
    }

    private void validate(EucrBatchVO vo, boolean checkBatchNo) {
        if (checkBatchNo && isBlank(vo.getBatchNo())) {
            throw BusinessException.badRequest("배치 번호(batchNo)는 필수입니다.");
        }
        if (isBlank(vo.getCrdtTypeCd())) {
            throw BusinessException.badRequest("배출권 유형 코드(crdtTypeCd)는 필수입니다.");
        }
        if (vo.getSubQty() == null || vo.getSubQty().signum() <= 0) {
            throw BusinessException.badRequest("취소 수량(subQty)은 0 보다 커야 합니다.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
