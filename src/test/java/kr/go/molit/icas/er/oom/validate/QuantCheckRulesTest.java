package kr.go.molit.icas.er.oom.validate;

import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.oom.validate.domain.QuantCheckResult;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("QuantCheckRules 단위 테스트 — SFR-034 18종 정량 검증")
class QuantCheckRulesTest {

    private OprtrVO makeOprtr(String desig) {
        OprtrVO o = new OprtrVO();
        o.setIcaoDesig(desig);
        return o;
    }

    private ErVO makeEr(String yr, LocalDate isueDt, String empPlanIdApld) {
        ErVO er = new ErVO();
        er.setRprtYr(yr);
        er.setIsueDt(isueDt);
        er.setEmpPlanIdApld(empPlanIdApld);
        return er;
    }

    private QuantCheckContext ctx(OprtrVO oprtr, ErVO er, ErVO prevEr) {
        return QuantCheckContext.builder()
                .oom(new OomCheckVO())
                .oprtr(oprtr)
                .er(er)
                .prevYearEr(prevEr)
                .build();
    }

    private QuantCheckContext ctxWithVr(String vrId, VrfcnInstVO inst, Integer leadCnt) {
        OomCheckVO oom = new OomCheckVO();
        oom.setVrId(vrId);
        return QuantCheckContext.builder()
                .oom(oom)
                .vrfcnInst(inst)
                .leadConscutvCnt(leadCnt)
                .build();
    }

    private VrfcnInstVO makeInst(LocalDate xprDt) {
        VrfcnInstVO inst = new VrfcnInstVO();
        inst.setVrfcnInstId("VI0001");
        inst.setIcaoCcrAccrdXprDt(xprDt);
        return inst;
    }

    // ── Rule 01: ICAO 지정어 ──

    @Test
    @DisplayName("Rule01: 정상 3자리 영숫자 → PASS")
    void rule01_정상_PASS() {
        QuantCheckResult r = new QuantCheckRules.Rule01OprtrDesigValid()
                .check(ctx(makeOprtr("KAL"), null, null));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
        assertThat(r.getRprtdVal()).isEqualTo("KAL");
    }

    @Test
    @DisplayName("Rule01: 형식 오류 → FAIL")
    void rule01_형식오류_FAIL() {
        QuantCheckResult r = new QuantCheckRules.Rule01OprtrDesigValid()
                .check(ctx(makeOprtr("Kal"), null, null));
        assertThat(r.getJudgCd()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("Rule01: 운영사 null → WARN(데이터 부족)")
    void rule01_운영사null_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule01OprtrDesigValid()
                .check(ctx(null, null, null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    // ── Rule 02: 보고 기한 ──

    @Test
    @DisplayName("Rule02: 2026 보고서 발행일 2027-03-01 → PASS (≤ 2027-04-30)")
    void rule02_기한내_PASS() {
        ErVO er = makeEr("2026", LocalDate.of(2027, 3, 1), null);
        QuantCheckResult r = new QuantCheckRules.Rule02ReportingDeadline()
                .check(ctx(makeOprtr("KAL"), er, null));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("Rule02: 발행일 2027-05-01 → FAIL")
    void rule02_기한초과_FAIL() {
        ErVO er = makeEr("2026", LocalDate.of(2027, 5, 1), null);
        QuantCheckResult r = new QuantCheckRules.Rule02ReportingDeadline()
                .check(ctx(makeOprtr("KAL"), er, null));
        assertThat(r.getJudgCd()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("Rule02: ER null → WARN")
    void rule02_ER없음_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule02ReportingDeadline()
                .check(ctx(makeOprtr("KAL"), null, null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    // ── Rule 04: 작성일 적정성 ──

    @Test
    @DisplayName("Rule04: 2026 발행일 2027-01-15 → PASS (≥ 2027-01-01)")
    void rule04_적정_PASS() {
        ErVO er = makeEr("2026", LocalDate.of(2027, 1, 15), null);
        QuantCheckResult r = new QuantCheckRules.Rule04ReportingDateValidity()
                .check(ctx(makeOprtr("KAL"), er, null));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("Rule04: 2026 발행일 2026-12-15 → WARN (보고연도 종료 전)")
    void rule04_조기작성_WARN() {
        ErVO er = makeEr("2026", LocalDate.of(2026, 12, 15), null);
        QuantCheckResult r = new QuantCheckRules.Rule04ReportingDateValidity()
                .check(ctx(makeOprtr("KAL"), er, null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    // ── Rule 07: EMP 적용 ──

    @Test
    @DisplayName("Rule07: empPlanIdApld 입력 → PASS")
    void rule07_EMP있음_PASS() {
        ErVO er = makeEr("2026", null, "EP0001");
        QuantCheckResult r = new QuantCheckRules.Rule07EmpFuelTypeMatch()
                .check(ctx(makeOprtr("KAL"), er, null));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
        assertThat(r.getRprtdVal()).isEqualTo("EP0001");
    }

    @Test
    @DisplayName("Rule07: empPlanIdApld 없음 → FAIL")
    void rule07_EMP없음_FAIL() {
        ErVO er = makeEr("2026", null, null);
        QuantCheckResult r = new QuantCheckRules.Rule07EmpFuelTypeMatch()
                .check(ctx(makeOprtr("KAL"), er, null));
        assertThat(r.getJudgCd()).isEqualTo("FAIL");
    }

    // ── Rule 18: 전년대비 변동률 (헬퍼 judgeByValues) ──

    @Test
    @DisplayName("Rule18.judgeByValues: 변동률 15% → PASS")
    void rule18_judgeByValues_15퍼센트_PASS() {
        QuantCheckRules.Rule18YoyVariance rule = new QuantCheckRules.Rule18YoyVariance();
        QuantCheckResult r = rule.judgeByValues(new BigDecimal("115000"), new BigDecimal("100000"));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
        assertThat(r.getDvtnRate().compareTo(new BigDecimal("15.0000"))).isEqualTo(0);
    }

    @Test
    @DisplayName("Rule18.judgeByValues: 변동률 50% → FAIL")
    void rule18_judgeByValues_50퍼센트_FAIL() {
        QuantCheckRules.Rule18YoyVariance rule = new QuantCheckRules.Rule18YoyVariance();
        QuantCheckResult r = rule.judgeByValues(new BigDecimal("150000"), new BigDecimal("100000"));
        assertThat(r.getJudgCd()).isEqualTo("FAIL");
        assertThat(r.getDvtnRate().compareTo(new BigDecimal("50.0000"))).isEqualTo(0);
    }

    @Test
    @DisplayName("Rule18.judgeByValues: 정확히 30% → PASS (경계)")
    void rule18_judgeByValues_30퍼센트_경계_PASS() {
        QuantCheckRules.Rule18YoyVariance rule = new QuantCheckRules.Rule18YoyVariance();
        QuantCheckResult r = rule.judgeByValues(new BigDecimal("130000"), new BigDecimal("100000"));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("Rule18: ctx 의 prevYearEr 없음 → WARN")
    void rule18_prevYearEr없음_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule18YoyVariance()
                .check(ctx(makeOprtr("KAL"), makeEr("2026", null, null), null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    @Test
    @DisplayName("Rule18.judgeByValues: 전년=0 → WARN (계산 불가)")
    void rule18_judgeByValues_전년0_WARN() {
        QuantCheckRules.Rule18YoyVariance rule = new QuantCheckRules.Rule18YoyVariance();
        QuantCheckResult r = rule.judgeByValues(new BigDecimal("100"), BigDecimal.ZERO);
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    // ── Rule 16: CCR 공인 유효성 ──

    @Test
    @DisplayName("Rule16: VR 미연결 → WARN")
    void rule16_VR미연결_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule16CcrAccrd()
                .check(ctx(makeOprtr("KAL"), null, null));  // oom.vrId == null
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    @Test
    @DisplayName("Rule16: CCR 만료일 미래 → PASS")
    void rule16_미래만료_PASS() {
        QuantCheckResult r = new QuantCheckRules.Rule16CcrAccrd()
                .check(ctxWithVr("VR0001", makeInst(LocalDate.now().plusDays(30)), null));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
        assertThat(r.getRprtdVal()).isEqualTo(LocalDate.now().plusDays(30).toString());
    }

    @Test
    @DisplayName("Rule16: CCR 만료일 어제 → FAIL")
    void rule16_만료_FAIL() {
        QuantCheckResult r = new QuantCheckRules.Rule16CcrAccrd()
                .check(ctxWithVr("VR0001", makeInst(LocalDate.now().minusDays(1)), null));
        assertThat(r.getJudgCd()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("Rule16: 검증기관 정보 null → WARN")
    void rule16_기관정보없음_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule16CcrAccrd()
                .check(ctxWithVr("VR0001", null, null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    // ── Rule 17: 리더 연속 검증 횟수 ──

    @Test
    @DisplayName("Rule17: VR 미연결 → WARN")
    void rule17_VR미연결_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule17LeaderConscutv()
                .check(ctx(makeOprtr("KAL"), null, null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    @Test
    @DisplayName("Rule17: 연속 3년 → PASS (경계)")
    void rule17_3년_경계_PASS() {
        QuantCheckResult r = new QuantCheckRules.Rule17LeaderConscutv()
                .check(ctxWithVr("VR0001", makeInst(LocalDate.now().plusDays(30)), 3));
        assertThat(r.getJudgCd()).isEqualTo("PASS");
        assertThat(r.getRprtdVal()).isEqualTo("3년");
    }

    @Test
    @DisplayName("Rule17: 연속 4년 → FAIL")
    void rule17_4년_FAIL() {
        QuantCheckResult r = new QuantCheckRules.Rule17LeaderConscutv()
                .check(ctxWithVr("VR0001", makeInst(LocalDate.now().plusDays(30)), 4));
        assertThat(r.getJudgCd()).isEqualTo("FAIL");
        assertThat(r.getRprtdVal()).isEqualTo("4년");
    }

    @Test
    @DisplayName("Rule17: LEAD 구성원 미등록 (leadConscutvCnt=null) → WARN")
    void rule17_리더없음_WARN() {
        QuantCheckResult r = new QuantCheckRules.Rule17LeaderConscutv()
                .check(ctxWithVr("VR0001", makeInst(LocalDate.now().plusDays(30)), null));
        assertThat(r.getJudgCd()).isEqualTo("WARN");
    }

    // ── Factory 검증 ──

    @Test
    @DisplayName("RuleFactory: 18개 Rule 등록, item_no 1~18 순서")
    void factory_18개_순서() {
        QuantCheckRuleFactory factory = new QuantCheckRuleFactory();
        var rules = factory.rules();

        assertThat(rules).hasSize(18);
        for (int i = 0; i < 18; i++) {
            assertThat(rules.get(i).itemNo()).isEqualTo(i + 1);
            assertThat(rules.get(i).itemNm()).isNotBlank();
        }
    }
}
