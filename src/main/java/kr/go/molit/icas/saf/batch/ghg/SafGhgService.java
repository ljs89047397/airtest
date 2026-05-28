package kr.go.molit.icas.saf.batch.ghg;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.batch.SafBatchService;
import kr.go.molit.icas.saf.batch.ghg.domain.SafGhgVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * SAF GHG 배출 서비스 — saf.tn_saf_ghg (1:1).
 * ttlLcaDefVal = coreLcaDefVal + ilucEmsn 자동 계산.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafGhgService {

    private final SafGhgMapper    ghgMapper;
    private final SafBatchService batchService;

    public SafGhgVO get(String batchId, IcasUser user) {
        batchService.get(batchId, user);
        SafGhgVO g = ghgMapper.selectByBatchId(batchId);
        if (g == null) throw BusinessException.notFound("SAF GHG 배출 정보");
        return g;
    }

    @Transactional
    public SafGhgVO saveOrUpdate(String batchId, SafGhgVO vo, IcasUser user) {
        batchService.assertBatchOwnedByAirline(batchId, user);
        vo.setBatchId(batchId);
        // ttlLcaDefVal 자동 계산
        BigDecimal core  = vo.getCoreLcaDefVal() != null ? vo.getCoreLcaDefVal() : BigDecimal.ZERO;
        BigDecimal iluc  = vo.getIlucEmsn()      != null ? vo.getIlucEmsn()      : BigDecimal.ZERO;
        vo.setTtlLcaDefVal(core.add(iluc));
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        SafGhgVO existing = ghgMapper.selectByBatchId(batchId);
        if (existing == null) ghgMapper.insertGhg(vo);
        else                  ghgMapper.updateGhg(vo);
        return ghgMapper.selectByBatchId(batchId);
    }
}
