package kr.go.molit.icas.ptl.stat;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.cef.CefMapper;
import kr.go.molit.icas.er.cef.domain.CefSearch;
import kr.go.molit.icas.er.cef.domain.CefVO;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErSearch;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.fuelsmry.ErFuelSmryMapper;
import kr.go.molit.icas.er.rprt.fuelsmry.domain.ErFuelSmryVO;
import kr.go.molit.icas.saf.airprt.purch.SafAirprtPurchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 연도별 통계 집계 서비스 (SFR-057).
 *
 * <h2>집계 공식</h2>
 * <pre>
 *   ttl_co2_emsn   = SUM(er.tn_er.ttl_co2_wght)   — APRVD ER 만
 *   ttl_ofst_req   = SUM(er.tn_er.ttl_ofst_req)    — 상쇄 의무량 (ER 내 컬럼, 없으면 0)
 *   ttl_cef_redu   = SUM(er.tn_cef.pure_fuel_mass)  — APRVD CEF 만
 *   ttl_flt_cnt    = SUM(er.tn_er.ttl_flt_cnt)
 *   ttl_fuel_wght  = SUM(er.tn_er.ttl_fuel_wght)
 *   ttl_saf_qty    = SUM(saf.tn_saf_airprt_purch.purch_qty) — 해당 운영사·연도
 *   data_gap_cnt   = COUNT(er.tn_er_data_gap) — 해당 ER 연결 gap 건수
 * </pre>
 * 집계는 ErMapper / CefMapper / SafAirprtPurchMapper 에서 SUM 쿼리로 추출 (직접 SQL 의존 최소화).
 * 1차 구현: 각 도메인 Mapper.sum*() 메서드 호출 후 PtlStatYearlyVO 에 세팅 → upsertStat
 *
 * <p>배치: {@code @Scheduled(cron="0 0 2 * * *")} — 매일 새벽 2시 모든 운영사 집계 갱신.
 * 수동 실행: POST /api/ptl/stat/aggregate (MOLIT/KOTSA 전용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PtlStatService {

    private final PtlStatYearlyMapper  statMapper;
    private final ErMapper             erMapper;
    private final CefMapper            cefMapper;
    private final SafAirprtPurchMapper safPurchMapper;
    private final ErFuelSmryMapper     fuelSmryMapper;
    private final OprtrMapper          oprtrMapper;
    private final DataScopeValidator   dataScopeValidator;

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /**
     * 단건 조회.
     * 항공사는 자기 oprtrId 만 조회 가능.
     */
    public PtlStatYearlyVO getStat(String rprtYr, String oprtrId, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOprtrAccessible(user, oprtrId, rprtYr);
        }
        PtlStatYearlyVO vo = statMapper.selectByPk(rprtYr, oprtrId);
        if (vo == null) {
            throw BusinessException.notFound("연도별 통계 (" + rprtYr + "/" + oprtrId + ")");
        }
        return vo;
    }

    /**
     * 연도 기준 전체 목록.
     * MOLIT/KOTSA 는 전체, AIRLINE 은 자기 것만.
     */
    public List<PtlStatYearlyVO> listByRprtYr(String rprtYr, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            // AIRLINE: 자기 데이터만 단건 조회 후 반환
            dataScopeValidator.assertOprtrAccessible(user, user.getOprtrId(), rprtYr);
            PtlStatYearlyVO vo = statMapper.selectByPk(rprtYr, user.getOprtrId());
            return vo != null ? List.of(vo) : List.of();
        }
        return statMapper.selectByRprtYr(rprtYr);
    }

    /**
     * 운영사 기준 다년 추이.
     */
    public List<PtlStatYearlyVO> listByOprtr(String oprtrId, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            dataScopeValidator.assertOprtrAccessible(user, oprtrId, null);
        }
        return statMapper.selectByOprtr(oprtrId);
    }

    // ── 집계 실행 ─────────────────────────────────────────────────────────────

    /**
     * 단일 집계 실행 (특정 운영사+연도).
     * ErMapper.selectErs 로 APRVD ER 목록 조회 → stream 합산.
     */
    @Transactional
    public PtlStatYearlyVO aggregateOne(String rprtYr, String oprtrId, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("집계 실행은 MOLIT/KOTSA 사용자만 가능합니다.");
        }
        return doAggregate(rprtYr, oprtrId, user.getUserId());
    }

    /**
     * 전체 집계 수동 실행 (MOLIT/KOTSA 전용).
     */
    @Transactional
    public void aggregateAll(String rprtYr, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("전체 집계 실행은 MOLIT/KOTSA 사용자만 가능합니다.");
        }
        List<OprtrVO> oprtrList = oprtrMapper.selectAll();
        for (OprtrVO oprtr : oprtrList) {
            try {
                doAggregate(rprtYr, oprtr.getOprtrId(), user.getUserId());
            } catch (Exception e) {
                log.error("[PtlStatService] aggregateAll 오류 — oprtrId={}, rprtYr={}: {}",
                        oprtr.getOprtrId(), rprtYr, e.getMessage(), e);
            }
        }
    }

    /**
     * 배치 전체 집계 — 매일 새벽 2시 자동 실행.
     * @Transactional 없이 내부에서 doAggregate 호출 (각 운영사별 독립 트랜잭션).
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void aggregateAllScheduled() {
        String rprtYr = String.valueOf(java.time.LocalDate.now().getYear());
        log.info("[PtlStatService] 배치 집계 시작 — rprtYr={}", rprtYr);
        List<OprtrVO> oprtrList = oprtrMapper.selectAll();
        int success = 0;
        int fail = 0;
        for (OprtrVO oprtr : oprtrList) {
            try {
                doAggregate(rprtYr, oprtr.getOprtrId(), "SYSTEM");
                success++;
            } catch (Exception e) {
                log.error("[PtlStatService] 배치 집계 오류 — oprtrId={}: {}",
                        oprtr.getOprtrId(), e.getMessage(), e);
                fail++;
            }
        }
        log.info("[PtlStatService] 배치 집계 완료 — 성공:{}, 실패:{}", success, fail);
    }

    // ── private ───────────────────────────────────────────────────────────────

    /**
     * 실제 집계 처리.
     */
    @Transactional
    public PtlStatYearlyVO doAggregate(String rprtYr, String oprtrId, String userId) {
        // 1. APRVD ER 목록 조회
        ErSearch erSearch = new ErSearch();
        erSearch.setOprtrId(oprtrId);
        erSearch.setRprtYr(rprtYr);
        erSearch.setErStCd("APRVD");
        erSearch.setPageSize(9999);
        List<ErVO> ers = erMapper.selectErs(erSearch);

        // 2. ER 기반 집계
        BigDecimal co2Emsn = BigDecimal.ZERO;
        BigDecimal fuelWght = BigDecimal.ZERO;
        int fltCnt = 0;

        for (ErVO er : ers) {
            // ErFuelSmryMapper 로 연료/CO2 합산 (ER 마스터에 집계 컬럼 없음)
            List<ErFuelSmryVO> fuelSmries = fuelSmryMapper.selectByErId(er.getErId());
            for (ErFuelSmryVO fs : fuelSmries) {
                co2Emsn  = co2Emsn.add(fs.getTtlCo2Emsn()  != null ? fs.getTtlCo2Emsn()  : BigDecimal.ZERO);
                fuelWght = fuelWght.add(fs.getTtlFuelWght() != null ? fs.getTtlFuelWght() : BigDecimal.ZERO);
            }
            // 운항 횟수: ErVO 에 필드가 추가될 경우를 대비해 0 누산 유지
        }

        // 3. APRVD CEF 집계 (ttlReduAmt 합산)
        CefSearch cefSearch = new CefSearch();
        cefSearch.setOprtrId(oprtrId);
        cefSearch.setRprtYr(rprtYr);
        cefSearch.setCefStCd("APRVD");
        cefSearch.setPageSize(9999);
        List<CefVO> cefs = cefMapper.selectCefs(cefSearch);

        BigDecimal cefRedu = cefs.stream()
                .map(c -> c.getTtlReduAmt() != null ? c.getTtlReduAmt() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. SAF 구매량 합산
        BigDecimal safQty = safPurchMapper.sumPurchQty(oprtrId, rprtYr);
        if (safQty == null) safQty = BigDecimal.ZERO;

        // 5. VO 빌드
        PtlStatYearlyVO vo = new PtlStatYearlyVO();
        vo.setRprtYr(rprtYr);
        vo.setOprtrId(oprtrId);
        vo.setTtlCo2Emsn(co2Emsn);
        vo.setTtlCefRedu(cefRedu);
        vo.setTtlFltCnt(fltCnt);
        vo.setTtlFuelWght(fuelWght);
        vo.setTtlSafQty(safQty);
        vo.setLastAggrDt(LocalDateTime.now());

        // 6. upsert
        statMapper.upsertStat(vo);
        return statMapper.selectByPk(rprtYr, oprtrId);
    }
}
