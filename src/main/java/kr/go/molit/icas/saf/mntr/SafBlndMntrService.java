package kr.go.molit.icas.saf.mntr;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.saf.airprt.fuel.SafAirprtFuelMapper;
import kr.go.molit.icas.saf.airprt.purch.SafAirprtPurchMapper;
import kr.go.molit.icas.saf.mntr.domain.SafBlndMntrVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * SAF 혼합비율 의무 모니터링 서비스 (SFR-046).
 *
 * <h2>자동 산출 공식</h2>
 * <pre>
 *   totalFuelQty     = SUM(saf_airprt_fuel.actl_fuel_qty)      — 연간 총 급유량
 *   safCertPurchQty  = SUM(saf_airprt_purch.purch_qty)          — SAF 구매량
 *   oblgRatio        = 1.0 (%)   ← 1차 하드코딩, 2차에 공통코드 연동
 *   blndRatio        = safCertPurchQty / totalFuelQty × 100
 *   fulfilledYn      = blndRatio >= oblgRatio ? 'Y' : 'N'
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafBlndMntrService {

    private static final BigDecimal OBLG_RATIO = new BigDecimal("1.0");   // 1차 하드코딩

    private final SafBlndMntrMapper   mntrMapper;
    private final SafAirprtFuelMapper fuelMapper;
    private final SafAirprtPurchMapper purchMapper;
    private final DataScopeValidator   dataScopeValidator;

    // ── 조회 ──

    public SafBlndMntrVO get(String oprtrId, String rprtYr, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOprtrAccessible(user, oprtrId, rprtYr);
        }
        SafBlndMntrVO m = mntrMapper.selectByPk(oprtrId, rprtYr);
        if (m == null) throw BusinessException.notFound("SAF 혼합비율 모니터링 (미계산 상태일 수 있음)");
        return m;
    }

    public List<SafBlndMntrVO> listByRprtYr(String rprtYr, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("SAF 혼합비율 전체 조회는 KOTSA/MOLIT 만 가능합니다.");
        }
        return mntrMapper.selectByRprtYr(rprtYr);
    }

    // ── 자동 산출 실행 ──

    @Transactional
    public SafBlndMntrVO runCalc(String oprtrId, String rprtYr, IcasUser user) {
        // 권한: AIRLINE(자사), KOTSA/MOLIT(전사)
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOprtrAccessible(user, oprtrId, rprtYr);
        }

        BigDecimal totalFuel = fuelMapper.sumActlFuelQty(oprtrId, rprtYr);
        BigDecimal safPurch  = purchMapper.sumPurchQty(oprtrId, rprtYr);

        if (totalFuel == null) totalFuel = BigDecimal.ZERO;
        if (safPurch  == null) safPurch  = BigDecimal.ZERO;

        BigDecimal blndRatio;
        if (totalFuel.compareTo(BigDecimal.ZERO) == 0) {
            blndRatio = BigDecimal.ZERO;
        } else {
            blndRatio = safPurch
                    .divide(totalFuel, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(4, RoundingMode.HALF_UP);
        }

        String fulfilledYn = blndRatio.compareTo(OBLG_RATIO) >= 0 ? "Y" : "N";

        SafBlndMntrVO vo = new SafBlndMntrVO();
        vo.setOprtrId(oprtrId);
        vo.setRprtYr(rprtYr);
        vo.setTotalFuelQty(totalFuel);
        vo.setSafCertPurchQty(safPurch);
        vo.setBlndRatio(blndRatio);
        vo.setOblgRatio(OBLG_RATIO);
        vo.setFulfilledYn(fulfilledYn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        SafBlndMntrVO existing = mntrMapper.selectByPk(oprtrId, rprtYr);
        if (existing == null) mntrMapper.insertMntr(vo);
        else                  mntrMapper.updateMntr(vo);

        return mntrMapper.selectByPk(oprtrId, rprtYr);
    }
}
