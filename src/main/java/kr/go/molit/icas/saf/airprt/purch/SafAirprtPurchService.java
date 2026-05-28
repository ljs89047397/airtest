package kr.go.molit.icas.saf.airprt.purch;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.airprt.purch.domain.SafAirprtPurchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공항별 SAF 구매 서비스 (SFR-050).
 * PK: (airprt_id, rprt_yr, oprtr_id, purch_sn) — purch_sn 자동 채번.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafAirprtPurchService {

    private final SafAirprtPurchMapper purchMapper;
    private final DataScopeValidator   dataScopeValidator;

    public List<SafAirprtPurchVO> listByOprtrYr(String oprtrId, String rprtYr, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOprtrAccessible(user, oprtrId, rprtYr);
        }
        return purchMapper.selectByOprtrYr(oprtrId, rprtYr);
    }

    @Transactional
    public SafAirprtPurchVO add(SafAirprtPurchVO vo, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());
        }
        validatePurch(vo);
        int nextSn = purchMapper.maxPurchSn(vo.getAirprtId(), vo.getRprtYr(), vo.getOprtrId()) + 1;
        vo.setPurchSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        purchMapper.insertPurch(vo);
        return purchMapper.selectByPk(vo.getAirprtId(), vo.getRprtYr(), vo.getOprtrId(), nextSn);
    }

    @Transactional
    public SafAirprtPurchVO update(String airprtId, String rprtYr, String oprtrId, int purchSn,
                                   SafAirprtPurchVO vo, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOwnAirline(user, oprtrId);
        }
        assertExists(airprtId, rprtYr, oprtrId, purchSn);
        validatePurch(vo);
        vo.setAirprtId(airprtId); vo.setRprtYr(rprtYr);
        vo.setOprtrId(oprtrId);   vo.setPurchSn(purchSn);
        vo.setLastChgUserId(user.getUserId());
        purchMapper.updatePurch(vo);
        return purchMapper.selectByPk(airprtId, rprtYr, oprtrId, purchSn);
    }

    @Transactional
    public void delete(String airprtId, String rprtYr, String oprtrId, int purchSn, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOwnAirline(user, oprtrId);
        }
        assertExists(airprtId, rprtYr, oprtrId, purchSn);
        purchMapper.deleteByPk(airprtId, rprtYr, oprtrId, purchSn);
    }

    private void assertExists(String airprtId, String rprtYr, String oprtrId, int purchSn) {
        if (purchMapper.selectByPk(airprtId, rprtYr, oprtrId, purchSn) == null)
            throw BusinessException.notFound("공항별 SAF 구매(purchSn=" + purchSn + ")");
    }

    private void validatePurch(SafAirprtPurchVO vo) {
        if (vo.getPurchQty() == null || vo.getPurchQty().signum() <= 0)
            throw BusinessException.badRequest("구매량(purchQty)은 0 초과여야 합니다.");
    }
}
