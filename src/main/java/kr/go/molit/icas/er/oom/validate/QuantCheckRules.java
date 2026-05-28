package kr.go.molit.icas.er.oom.validate;

import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.er.oom.validate.domain.QuantCheckResult;
import kr.go.molit.icas.er.rprt.domain.ErVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * SFR-034 18종 정량 검증 Rule 모음 (Stateless inner classes).
 *
 * <p>Factory ({@link QuantCheckRuleFactory}) 가 본 클래스의 모든 Rule 을 인스턴스화하여
 * Validator 에 전달.
 *
 * <h2>구현 원칙</h2>
 * <ul>
 *   <li>외부 데이터 가용 → 실제 검증 수행 (PASS/FAIL)</li>
 *   <li>외부 데이터 부족 → WARN + rmrk="데이터 부족" 으로 자동판정 보류 (점검자가 수동 입력)</li>
 *   <li>예외 발생 시도 WARN/FAIL 로 변환 — Validator 가 죽지 않게</li>
 * </ul>
 *
 * <h2>1차년도 제한</h2>
 * <p>대부분 외부 데이터(전년 ER 집계, CORSIA 참여국 마스터, ICAO CERT)는 2차년도 연계 후 활성화.
 * 1차에는 즉시 판정 가능한 5~6 항목만 실제 검증, 나머지는 WARN 으로 항목 자리만 마련.
 */
public final class QuantCheckRules {

    private QuantCheckRules() {}

    /** ICAO 항공사 지정어 유효성 — oprtr 존재 검증 */
    public static class Rule01OprtrDesigValid implements QuantCheckRule {
        @Override public int itemNo() { return 1; }
        @Override public String itemNm() { return "ICAO 항공사 지정어 유효성"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            if (ctx.getOprtr() == null) {
                return warnNoData("운영사 정보 없음");
            }
            String desig = ctx.getOprtr().getIcaoDesig();
            boolean valid = desig != null && desig.matches("[A-Z0-9]{3}");
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal("3자리 영숫자")
                    .rprtdVal(desig != null ? desig : "")
                    .judgCd(valid ? "PASS" : "FAIL")
                    .rmrk(valid ? "ICAO 지정어 형식 OK" : "ICAO 지정어 형식 오류")
                    .build();
        }
    }

    /** 보고 기한 준수 — er.isue_dt ≤ N+1 의 4월 30일 */
    public static class Rule02ReportingDeadline implements QuantCheckRule {
        @Override public int itemNo() { return 2; }
        @Override public String itemNm() { return "보고 기한 준수 (N+1 4월 30일)"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            ErVO er = ctx.getEr();
            if (er == null || er.getIsueDt() == null || er.getRprtYr() == null) {
                return warnNoData("ER 또는 발행일/보고연도 누락");
            }
            int yr = Integer.parseInt(er.getRprtYr());
            LocalDate deadline = LocalDate.of(yr + 1, 4, 30);
            boolean ok = !er.getIsueDt().isAfter(deadline);
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal(deadline.toString())
                    .rprtdVal(er.getIsueDt().toString())
                    .judgCd(ok ? "PASS" : "FAIL")
                    .rmrk(ok ? "기한 내 발행" : "기한 초과")
                    .build();
        }
    }

    /** ER ↔ VR 총 연료/항공편수 일치 — VR 도메인 미연결 시 WARN */
    public static class Rule03ErVrFuelMatch implements QuantCheckRule {
        @Override public int itemNo() { return 3; }
        @Override public String itemNm() { return "ER ↔ VR 총 연료/항공편수 일치성"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            if (ctx.getOom().getVrId() == null) {
                return warnNoData("VR 미연결");
            }
            // 1차: VR 데이터 비교는 vr 도메인 진행 후 활성화
            return warnNoData("VR 비교 로직 미구현 (2차년도)");
        }
    }

    /** 보고서 작성 일자 적정성 — er.isue_dt ≥ N+1 의 1월 1일 */
    public static class Rule04ReportingDateValidity implements QuantCheckRule {
        @Override public int itemNo() { return 4; }
        @Override public String itemNm() { return "보고서 작성일자 적정성"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            ErVO er = ctx.getEr();
            if (er == null || er.getIsueDt() == null || er.getRprtYr() == null) {
                return warnNoData("ER 또는 발행일/보고연도 누락");
            }
            int yr = Integer.parseInt(er.getRprtYr());
            LocalDate earliest = LocalDate.of(yr + 1, 1, 1);
            boolean ok = !er.getIsueDt().isBefore(earliest);
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal("≥ " + earliest)
                    .rprtdVal(er.getIsueDt().toString())
                    .judgCd(ok ? "PASS" : "WARN")
                    .rmrk(ok ? "보고연도 종료 이후 작성 OK" : "보고연도 종료 이전 작성")
                    .build();
        }
    }

    /** CORSIA 10,000 tCO2 임계치 보고의무 — ER 합계 데이터 필요 */
    public static class Rule05Corsia10kTon implements QuantCheckRule {
        @Override public int itemNo() { return 5; }
        @Override public String itemNm() { return "CORSIA 10,000 tCO2 보고의무 충족"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            // 1차: ER 마스터에는 CO2 합계 컬럼이 없음. cntry_pair 합계 조회 별도 필요.
            return warnNoData("ER CO2 합계 조회 미구현 (cntry_pair_co2.sum)");
        }
    }

    /** CERT 500k/50k 임계치 — 외부 CERT 계산 결과 필요 */
    public static class Rule06Cert500k50k implements QuantCheckRule {
        @Override public int itemNo() { return 6; }
        @Override public String itemNm() { return "ICAO CERT 500,000 / 50,000 tCO2 사용 임계치"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("ICAO CERT 외부 계산값 필요 (2차년도 연계)");
        }
    }

    /** 승인 EMP 기반 연료 유형 일치성 — EMP Plan 적용 검증 */
    public static class Rule07EmpFuelTypeMatch implements QuantCheckRule {
        @Override public int itemNo() { return 7; }
        @Override public String itemNm() { return "승인 EMP 기반 연료 유형 일치"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            ErVO er = ctx.getEr();
            if (er == null) return warnNoData("ER 미연결");
            if (er.getEmpPlanIdApld() == null || er.getEmpPlanIdApld().isBlank()) {
                return QuantCheckResult.builder()
                        .itemNo(itemNo()).itemNm(itemNm())
                        .judgCd("FAIL")
                        .rmrk("ER 에 적용 EMP Plan 이 지정되지 않음")
                        .build();
            }
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal("EMP Plan 적용")
                    .rprtdVal(er.getEmpPlanIdApld())
                    .judgCd("PASS")
                    .rmrk("EMP Plan " + er.getEmpPlanIdApld() + " 적용 확인 (연료유형 상세 비교는 별도)")
                    .build();
        }
    }

    /** 항공기 등록기호 중복 — er.acft_fuel 행 단위 검사 필요 */
    public static class Rule08RegisMarkDup implements QuantCheckRule {
        @Override public int itemNo() { return 8; }
        @Override public String itemNm() { return "항공기 등록기호 중복"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("er_acft_fuel 중복 스캔 미구현 (Service 단 add 시 중복 차단 중)");
        }
    }

    /** CORSIA 참여국 분류 정확성 — 참여국 마스터 필요 */
    public static class Rule09CorsiaCntryClsfn implements QuantCheckRule {
        @Override public int itemNo() { return 9; }
        @Override public String itemNm() { return "CORSIA 참여국 분류 정확성"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("CORSIA 참여국 마스터 미연계 (2차년도)");
        }
    }

    /** 국가쌍 + 연료유형 중복 — cntry_pair 행 단위 */
    public static class Rule10CntryFuelDup implements QuantCheckRule {
        @Override public int itemNo() { return 10; }
        @Override public String itemNm() { return "국가쌍 + 연료유형 중복"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("cntry_pair × fuel_type 그룹 중복 스캔 미구현");
        }
    }

    /** 국내선 오류 — 출발=도착 국가코드 */
    public static class Rule11DomesticErrCheck implements QuantCheckRule {
        @Override public int itemNo() { return 11; }
        @Override public String itemNm() { return "국내선 오류 (출발=도착 국가)"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("cntry_pair 출발=도착 스캔 미구현");
        }
    }

    /** 항공편당 연료소비량 상한/하한 */
    public static class Rule12FuelPerFlight implements QuantCheckRule {
        @Override public int itemNo() { return 12; }
        @Override public String itemNm() { return "항공편당 연료소비량 상하한 (CORSIA 이상치)"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("항공편 단위 연료 데이터 필요");
        }
    }

    /** ICAO CERT 편차 */
    public static class Rule13CertDeviation implements QuantCheckRule {
        @Override public int itemNo() { return 13; }
        @Override public String itemNm() { return "ICAO CERT 계산값 기반 편차"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("CERT 외부 계산값 미연계");
        }
    }

    /** CORSIA 데이터갭 임계치 초과 */
    public static class Rule14DataGapThrshld implements QuantCheckRule {
        @Override public int itemNo() { return 14; }
        @Override public String itemNm() { return "CORSIA 데이터갭 임계치 (5%) 초과 여부"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("data_gap 합계 / 총 CO2 비율 산출 미구현 (gap 도메인 sum 필요)");
        }
    }

    /** 데이터갭 표시-상세 정합성 */
    public static class Rule15DataGapDetail implements QuantCheckRule {
        @Override public int itemNo() { return 15; }
        @Override public String itemNm() { return "데이터갭 표시-상세 정합성"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            return warnNoData("표시 항목과 상세 항목 정합 비교 미구현");
        }
    }

    /** ICAO CCR 공인 검증기관 인증 유효성 — VR 연결 시 만료일 비교 */
    public static class Rule16CcrAccrd implements QuantCheckRule {
        @Override public int itemNo() { return 16; }
        @Override public String itemNm() { return "ICAO CCR 공인 검증기관 인증 유효성"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            if (ctx.getOom().getVrId() == null) {
                return warnNoData("VR 미연결 — 검증기관 정보 없음");
            }
            VrfcnInstVO inst = ctx.getVrfcnInst();
            if (inst == null) {
                return warnNoData("검증기관 정보 조회 불가");
            }
            LocalDate xprDt = inst.getIcaoCcrAccrdXprDt();
            if (xprDt == null) {
                return warnNoData("CCR 공인 만료일 미등록");
            }
            boolean valid = !xprDt.isBefore(LocalDate.now());
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal("만료일 ≥ " + LocalDate.now())
                    .rprtdVal(xprDt.toString())
                    .judgCd(valid ? "PASS" : "FAIL")
                    .rmrk(valid ? "CCR 공인 유효" : "CCR 공인 만료 — 검증 결과 무효화 위험")
                    .build();
        }
    }

    /** 검증팀 리더 연속 검증 3년 초과 여부 */
    public static class Rule17LeaderConscutv implements QuantCheckRule {
        private static final int MAX_CONSECUTIVE = 3;

        @Override public int itemNo() { return 17; }
        @Override public String itemNm() { return "검증팀 리더 연속 검증 3년 초과"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            if (ctx.getOom().getVrId() == null) {
                return warnNoData("VR 미연결 — 검증팀 정보 없음");
            }
            Integer cnt = ctx.getLeadConscutvCnt();
            if (cnt == null) {
                return warnNoData("검증팀 LEAD 구성원 미등록");
            }
            boolean ok = cnt <= MAX_CONSECUTIVE;
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal("≤ " + MAX_CONSECUTIVE + "년")
                    .rprtdVal(cnt + "년")
                    .judgCd(ok ? "PASS" : "FAIL")
                    .rmrk(ok ? "연속 검증 " + MAX_CONSECUTIVE + "년 이내"
                             : "연속 검증 " + cnt + "년 — " + MAX_CONSECUTIVE + "년 초과로 검증 공정성 위험")
                    .build();
        }
    }

    /** 전년 대비 배출량 변동 (±30%) */
    public static class Rule18YoyVariance implements QuantCheckRule {
        private static final BigDecimal THRSHLD = new BigDecimal("30");

        @Override public int itemNo() { return 18; }
        @Override public String itemNm() { return "전년 대비 배출량 변동 (±30%)"; }
        @Override public QuantCheckResult check(QuantCheckContext ctx) {
            if (ctx.getEr() == null || ctx.getPrevYearEr() == null) {
                return warnNoData("당해/전년 ER 미연결");
            }
            // 1차: ER 마스터에 CO2 합계 컬럼이 없으므로 cntry_pair 합계 별도 조회 필요.
            // ctx 에 합계 필드를 두지 않은 상태에서는 자동 판정 보류.
            return warnNoData("ER CO2 합계 산출 미연계 (cntry_pair_co2.sum)");
        }

        /** 외부에서 두 값 알려주면 ±30% 판정 — 향후 ctx 확장 후 사용 */
        public QuantCheckResult judgeByValues(BigDecimal currentCo2, BigDecimal prevCo2) {
            if (currentCo2 == null || prevCo2 == null || prevCo2.signum() == 0) {
                return warnNoData("값 누락 또는 전년=0");
            }
            BigDecimal diff = currentCo2.subtract(prevCo2).abs();
            BigDecimal pct  = diff.divide(prevCo2, 6, RoundingMode.HALF_UP)
                                  .multiply(new BigDecimal("100"))
                                  .setScale(4, RoundingMode.HALF_UP);
            boolean within = pct.compareTo(THRSHLD) <= 0;
            return QuantCheckResult.builder()
                    .itemNo(itemNo()).itemNm(itemNm())
                    .expctdVal("±30%")
                    .rprtdVal(pct + "%")
                    .dvtnRate(pct)
                    .judgCd(within ? "PASS" : "FAIL")
                    .rmrk(within ? "변동률 임계 내" : "변동률 임계 초과 — 사유 확인 필요")
                    .build();
        }
    }
}
