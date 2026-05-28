package kr.go.molit.icas.ptl.sim;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.ptl.sim.domain.PtlSimSearch;
import kr.go.molit.icas.ptl.sim.domain.PtlSimVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 상쇄비용 시뮬레이션 서비스 (SFR-056).
 *
 * <h2>시뮬레이션 계산 로직 (1차 단순화)</h2>
 * <pre>
 * 입력(input_json 파싱):
 *   - growthRate   : 연평균 운항 성장률 (%)
 *   - baseEmission : 기준연도 CO2 배출량 (ton)
 *   - carbonPrice  : 연도별 배출권 단가 맵 (USD/t)
 *   - safRatio     : 연도별 SAF 의무비율 맵 (%)
 *
 * 출력(rslt_json):
 *   연도별 배열:
 *   - year
 *   - projectedEmission = baseEmission * (1 + growthRate)^n
 *   - offsetReq         = projectedEmission * CORSIA_FACTOR (0.1 고정 — 1차)
 *   - safReduction      = projectedEmission * safRatio / 100
 *   - netOffset         = offsetReq - safReduction (max 0)
 *   - estimatedCost     = netOffset * carbonPrice
 * </pre>
 *
 * input_json/rslt_json은 jackson ObjectMapper로 직렬화.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PtlSimService {

    private static final String SIM_PREFIX    = "SM";
    private static final double CORSIA_FACTOR = 0.1;  // 1차 하드코딩

    private final PtlSimMapper simMapper;
    private final IdGenerator  idGenerator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── 등록 ──────────────────────────────────────────────────────────────────

    /**
     * 시뮬레이션 신규 생성.
     */
    @Transactional
    public PtlSimVO createSim(PtlSimVO vo, IcasUser user) {
        // 1. SM 채번
        int seq = simMapper.countByPrefix() + 1;
        String simId = idGenerator.managementPk(SIM_PREFIX, seq);
        vo.setSimId(simId);

        // 2. 소유자 설정
        vo.setOwnerUserId(user.getUserId());
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        // 3. inputJson 기본값 채움 + 시뮬레이션 실행 (항상 결과 생성)
        if (vo.getInputJson() == null || vo.getInputJson().isBlank()) {
            vo.setInputJson(defaultInputJson(vo));
        }
        String rsltJson = calculateSimulation(vo.getInputJson());
        vo.setRsltJson(rsltJson);

        // 4. shareSeCd 기본값
        if (vo.getShareSeCd() == null || vo.getShareSeCd().isBlank()) {
            vo.setShareSeCd("PRIVATE");
        }

        // 5. INSERT
        simMapper.insertSim(vo);
        return simMapper.selectBySimId(simId);
    }

    /**
     * 기존 시뮬레이션에 대해 계산 재실행.
     * inputJson 이 비어있는 시드/레거시 데이터는 기본 시나리오로 fallback 후 실행한다.
     */
    @Transactional
    public PtlSimVO runSim(String simId, IcasUser user) {
        PtlSimVO vo = requireSim(simId);
        assertOwnerOrAdmin(vo, user);

        if (vo.getInputJson() == null || vo.getInputJson().isBlank()) {
            // 시드/레거시 데이터 호환 — 화면 기본값으로 입력 채워 실행
            vo.setInputJson(defaultInputJson(vo));
        }
        String rsltJson = calculateSimulation(vo.getInputJson());
        vo.setRsltJson(rsltJson);
        vo.setLastChgUserId(user.getUserId());
        simMapper.updateSim(vo);
        return simMapper.selectBySimId(simId);
    }

    /**
     * 화면 기본값과 동일한 시뮬 입력 JSON 을 생성한다.
     * 탄소가격 25 USD/tCO₂e, SAF 의무비율 2%, 연간 성장률 3.5%, 예측 2027~2030.
     */
    private String defaultInputJson(PtlSimVO vo) {
        int baseYear;
        try {
            baseYear = (vo.getBaseYr() != null && !vo.getBaseYr().isBlank())
                ? Integer.parseInt(vo.getBaseYr())
                : 2026;
        } catch (NumberFormatException nfe) {
            baseYear = 2026;
        }
        return "{"
            + "\"baseYear\":" + baseYear + ","
            + "\"forecastStart\":" + (baseYear + 1) + ","
            + "\"forecastEnd\":" + (baseYear + 4) + ","
            + "\"carbonPriceUsd\":25.0,"
            + "\"annualGrowthPct\":3.5,"
            + "\"safBlendPct\":2.0,"
            + "\"scope\":\"ALL\""
            + "}";
    }

    // ── 수정 / 삭제 ───────────────────────────────────────────────────────────

    /**
     * 시뮬레이션 수정. 소유자만 가능.
     */
    @Transactional
    public PtlSimVO updateSim(PtlSimVO vo, IcasUser user) {
        PtlSimVO existing = requireSim(vo.getSimId());
        if (!user.isMaster() && !user.getUserId().equals(existing.getOwnerUserId())) {
            throw BusinessException.forbidden("본인이 작성한 시뮬레이션만 수정할 수 있습니다.");
        }
        vo.setLastChgUserId(user.getUserId());
        // 입력값이 변경된 경우 재계산
        if (vo.getInputJson() != null && !vo.getInputJson().isBlank()) {
            vo.setRsltJson(calculateSimulation(vo.getInputJson()));
        }
        simMapper.updateSim(vo);
        return simMapper.selectBySimId(vo.getSimId());
    }

    /**
     * 시뮬레이션 소프트 삭제. 소유자 또는 MOLIT 가능.
     */
    @Transactional
    public void deleteSim(String simId, IcasUser user) {
        PtlSimVO vo = requireSim(simId);
        if (!user.isMaster()) {
            boolean isOwner = user.getUserId().equals(vo.getOwnerUserId());
            boolean isMolit = "MOLIT".equals(user.getOgnzSeCd());
            if (!isOwner && !isMolit) {
                throw BusinessException.forbidden("본인이 작성한 시뮬레이션 또는 MOLIT 권한이 필요합니다.");
            }
        }
        simMapper.softDeleteSim(simId, user.getUserId());
    }

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /**
     * 단건 조회.
     * share_se_cd 기반 가시범위:
     * PRIVATE → 소유자만
     * ORG     → 같은 기관
     * PUBLIC  → 전체
     */
    public PtlSimVO getSim(String simId, IcasUser user) {
        PtlSimVO vo = requireSim(simId);
        assertVisible(vo, user);
        return vo;
    }

    /**
     * 목록 조회.
     * PUBLIC + 자기 ORG + 자기 PRIVATE 가시범위 적용.
     */
    public PageResponse<PtlSimVO> listSims(PtlSimSearch search, IcasUser user) {
        // MOLIT/KOTSA/MASTER 는 전체 조회
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            search.setOwnerUserId(user.getUserId());
        }
        List<PtlSimVO> rows = simMapper.selectSims(search);
        int total = simMapper.countSims(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private PtlSimVO requireSim(String simId) {
        PtlSimVO vo = simMapper.selectBySimId(simId);
        if (vo == null) throw BusinessException.notFound("시뮬레이션 (" + simId + ")");
        return vo;
    }

    private void assertOwnerOrAdmin(PtlSimVO vo, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) return;
        if (!user.getUserId().equals(vo.getOwnerUserId())) {
            throw BusinessException.forbidden("본인이 작성한 시뮬레이션만 접근할 수 있습니다.");
        }
    }

    private void assertVisible(PtlSimVO vo, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) return;
        String shareSeCd = vo.getShareSeCd();
        if ("PUBLIC".equals(shareSeCd)) return;
        if ("ORG".equals(shareSeCd)) {
            // 같은 기관(ognzId) 확인 — 1차: 소유자 본인이면 허용, 2차에서 ognzId 비교 강화
            // PRIVATE 또는 ORG 모두 소유자 본인 접근은 허용
            if (!user.getUserId().equals(vo.getOwnerUserId())) {
                throw BusinessException.forbidden("같은 기관 사용자만 접근할 수 있는 시뮬레이션입니다.");
            }
            return;
        }
        // PRIVATE
        if (!user.getUserId().equals(vo.getOwnerUserId())) {
            throw BusinessException.forbidden("본인이 작성한 시뮬레이션만 접근할 수 있습니다.");
        }
    }

    /**
     * 시뮬레이션 계산 수행.
     * Jackson ObjectMapper로 Map<String, Object> 파싱 후 연도별 계산.
     *
     * @param inputJson 입력 JSON 문자열
     * @return 결과 JSON 문자열
     */
    private String calculateSimulation(String inputJson) {
        try {
            Map<String, Object> input = objectMapper.readValue(
                    inputJson, new TypeReference<Map<String, Object>>() {});

            // 화면 신규폼: growthRate (또는 annualGrowthPct)
            double growthRate = toDouble(firstNonNull(input.get("growthRate"), input.get("annualGrowthPct")));
            // 기본 연간 배출량 (운영사별 미지정 시 4.8M tCO₂e 가정)
            double baseEmission = input.get("baseEmission") != null
                    ? toDouble(input.get("baseEmission"))
                    : 4_800_000.0;

            // carbonPrice / safRatio 는 스칼라(화면 입력) 또는 연도별 Map(API 입력) 둘 다 허용
            Object carbonPriceRaw = input.getOrDefault("carbonPrice", input.get("carbonPriceUsd"));
            Object safRatioRaw    = input.getOrDefault("safRatio", input.getOrDefault("safBlendPct", 0));
            double scalarCarbonPrice = (carbonPriceRaw instanceof Map) ? 0.0 : toDouble(carbonPriceRaw);
            double scalarSafRatio    = (safRatioRaw    instanceof Map) ? 0.0 : toDouble(safRatioRaw);
            @SuppressWarnings("unchecked")
            Map<String, Object> carbonPriceMap = (carbonPriceRaw instanceof Map)
                    ? (Map<String, Object>) carbonPriceRaw : Map.of();
            @SuppressWarnings("unchecked")
            Map<String, Object> safRatioMap    = (safRatioRaw    instanceof Map)
                    ? (Map<String, Object>) safRatioRaw    : Map.of();

            // 예측 연도 범위 — 화면(forecastStart/End) > 레거시(fromYr/toYr) > 기본(2027~2030)
            int fromYr = toInt(firstNonNull(input.get("forecastStart"), input.get("fromYr")), 2027);
            int toYr   = toInt(firstNonNull(input.get("forecastEnd"),   input.get("toYr")),   2030);
            if (toYr < fromYr) toYr = fromYr;

            List<Map<String, Object>> rows = new ArrayList<>();
            List<String> years = new ArrayList<>();
            List<Double> emissions = new ArrayList<>();
            List<Double> offsetCost = new ArrayList<>();
            for (int yr = fromYr; yr <= toYr; yr++) {
                int n = yr - fromYr;
                double projected   = baseEmission * Math.pow(1 + growthRate / 100.0, n);
                double offsetReq   = projected * CORSIA_FACTOR;
                double safRatioPct = !safRatioMap.isEmpty()
                        ? toDouble(safRatioMap.getOrDefault(String.valueOf(yr), 0))
                        : scalarSafRatio;
                double safReduction = projected * safRatioPct / 100.0;
                double netOffset   = Math.max(0, offsetReq - safReduction);
                double carbonPrice = !carbonPriceMap.isEmpty()
                        ? toDouble(carbonPriceMap.getOrDefault(String.valueOf(yr), 0))
                        : scalarCarbonPrice;
                double estimatedCost = netOffset * carbonPrice;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("year",              yr);
                row.put("projectedEmission", round(projected));
                row.put("offsetReq",         round(offsetReq));
                row.put("safReduction",      round(safReduction));
                row.put("netOffset",         round(netOffset));
                row.put("estimatedCost",     round(estimatedCost));
                rows.add(row);

                years.add(String.valueOf(yr));
                emissions.add(round(projected));
                offsetCost.add(round(estimatedCost));
            }

            // 화면(renderCharts)이 기대하는 평탄화 키 + 상세 행을 함께 반환
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("years", years);
            out.put("emissions", emissions);
            out.put("offsetCost", offsetCost);
            out.put("rows", rows);
            return objectMapper.writeValueAsString(out);

        } catch (Exception e) {
            log.error("[PtlSimService] 시뮬레이션 계산 오류: {}", e.getMessage(), e);
            throw BusinessException.badRequest("시뮬레이션 입력값 파싱 오류: " + e.getMessage());
        }
    }

    private Object firstNonNull(Object a, Object b) { return a != null ? a : b; }

    private int toInt(Object val, int def) {
        if (val == null) return def;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString().trim()); } catch (Exception e) { return def; }
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private double round(double val) {
        return new BigDecimal(val).round(new MathContext(8)).doubleValue();
    }
}
