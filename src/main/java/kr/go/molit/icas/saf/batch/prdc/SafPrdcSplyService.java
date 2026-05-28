package kr.go.molit.icas.saf.batch.prdc;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.SafBatchService;
import kr.go.molit.icas.saf.batch.prdc.domain.SafPrdcSplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAF 생산사·공급사 서비스 — saf.tn_saf_prdc_sply (1:1).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafPrdcSplyService {

    private final SafPrdcSplyMapper prdcSplyMapper;
    private final SafBatchService   batchService;

    public SafPrdcSplyVO get(String batchId, IcasUser user) {
        batchService.get(batchId, user);
        SafPrdcSplyVO p = prdcSplyMapper.selectByBatchId(batchId);
        if (p == null) throw BusinessException.notFound("SAF 생산사·공급사 정보");
        return p;
    }

    @Transactional
    public SafPrdcSplyVO saveOrUpdate(String batchId, SafPrdcSplyVO vo, IcasUser user) {
        batchService.assertBatchOwnedByAirline(batchId, user);
        vo.setBatchId(batchId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        SafPrdcSplyVO existing = prdcSplyMapper.selectByBatchId(batchId);
        if (existing == null) {
            prdcSplyMapper.insertPrdc(vo);
        } else {
            prdcSplyMapper.updatePrdc(vo);
        }
        return prdcSplyMapper.selectByBatchId(batchId);
    }
}
