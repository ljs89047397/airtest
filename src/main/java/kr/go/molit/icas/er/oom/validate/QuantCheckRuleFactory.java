package kr.go.molit.icas.er.oom.validate;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 18종 정량 검증 Rule 등록·제공 Factory.
 *
 * <p>새 규칙 추가 시 {@link QuantCheckRules} 에 내부 클래스 추가 + 본 Factory 리스트에 추가.
 */
@Component
public class QuantCheckRuleFactory {

    /** 1~18 순서 유지 — Validator 가 이 순서로 결과 저장 */
    public List<QuantCheckRule> rules() {
        return List.of(
                new QuantCheckRules.Rule01OprtrDesigValid(),
                new QuantCheckRules.Rule02ReportingDeadline(),
                new QuantCheckRules.Rule03ErVrFuelMatch(),
                new QuantCheckRules.Rule04ReportingDateValidity(),
                new QuantCheckRules.Rule05Corsia10kTon(),
                new QuantCheckRules.Rule06Cert500k50k(),
                new QuantCheckRules.Rule07EmpFuelTypeMatch(),
                new QuantCheckRules.Rule08RegisMarkDup(),
                new QuantCheckRules.Rule09CorsiaCntryClsfn(),
                new QuantCheckRules.Rule10CntryFuelDup(),
                new QuantCheckRules.Rule11DomesticErrCheck(),
                new QuantCheckRules.Rule12FuelPerFlight(),
                new QuantCheckRules.Rule13CertDeviation(),
                new QuantCheckRules.Rule14DataGapThrshld(),
                new QuantCheckRules.Rule15DataGapDetail(),
                new QuantCheckRules.Rule16CcrAccrd(),
                new QuantCheckRules.Rule17LeaderConscutv(),
                new QuantCheckRules.Rule18YoyVariance()
        );
    }
}
