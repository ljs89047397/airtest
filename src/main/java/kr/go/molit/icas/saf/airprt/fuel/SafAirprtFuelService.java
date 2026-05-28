package kr.go.molit.icas.saf.airprt.fuel;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.airprt.fuel.domain.SafAirprtFuelVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공항별 급유 실적 서비스 (SFR-045/049).
 * PK: (airprt_id, rprt_yr, oprtr_id) — 공항·연도·항공사 단위 upsert.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafAirprtFuelService {

    private final SafAirprtFuelMapper fuelMapper;
    private final DataScopeValidator  dataScopeValidator;

    public List<SafAirprtFuelVO> listByOprtrYr(String oprtrId, String rprtYr, IcasUser user) {
        assertAccess(user, oprtrId, rprtYr);
        return fuelMapper.selectByOprtrYr(oprtrId, rprtYr);
    }

    public SafAirprtFuelVO get(String airprtId, String rprtYr, String oprtrId, IcasUser user) {
        assertAccess(user, oprtrId, rprtYr);
        SafAirprtFuelVO m = fuelMapper.selectByPk(airprtId, rprtYr, oprtrId);
        if (m == null) throw BusinessException.notFound("공항별 급유 실적");
        return m;
    }

    @Transactional
    public SafAirprtFuelVO saveOrUpdate(SafAirprtFuelVO vo, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());
        }
        validateFuel(vo);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        SafAirprtFuelVO existing = fuelMapper.selectByPk(vo.getAirprtId(), vo.getRprtYr(), vo.getOprtrId());
        if (existing == null) fuelMapper.insertFuel(vo);
        else                  fuelMapper.updateFuel(vo);
        return fuelMapper.selectByPk(vo.getAirprtId(), vo.getRprtYr(), vo.getOprtrId());
    }

    @Transactional
    public void softDelete(String airprtId, String rprtYr, String oprtrId, IcasUser user) {
        dataScopeValidator.assertOwnAirline(user, oprtrId);
        fuelMapper.softDeleteFuel(airprtId, rprtYr, oprtrId, user.getUserId());
    }

    private void assertAccess(IcasUser user, String oprtrId, String rprtYr) {
        if (user.isMaster() || user.isMolitOrKotsa()) return;
        dataScopeValidator.assertOprtrAccessible(user, oprtrId, rprtYr);
    }

    private void validateFuel(SafAirprtFuelVO vo) {
        if (vo.getAirprtId() == null || vo.getAirprtId().isBlank())
            throw BusinessException.badRequest("공항 코드(airprtId)는 필수입니다.");
        if (vo.getRprtYr() == null || !vo.getRprtYr().matches("\\d{4}"))
            throw BusinessException.badRequest("보고연도(rprtYr)는 4자리 숫자여야 합니다.");
        if (vo.getOprtrId() == null || vo.getOprtrId().isBlank())
            throw BusinessException.badRequest("운영사 ID(oprtrId)는 필수입니다.");
    }
}
