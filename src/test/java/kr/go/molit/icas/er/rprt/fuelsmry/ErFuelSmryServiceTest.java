package kr.go.molit.icas.er.rprt.fuelsmry;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.fuelsmry.domain.ErFuelSmryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErFuelSmryService 단위 테스트 — 연료 유형별 총사용량 upsert + 검증")
class ErFuelSmryServiceTest {

    @Mock ErFuelSmryMapper   erFuelSmryMapper;
    @Mock ErMapper           erMapper;
    @Mock DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErFuelSmryService erFuelSmryService;

    private IcasUser airlineUserOP0001;
    private IcasUser airlineUserOP0002;

    @BeforeEach
    void setUpFixtures() {
        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    // ── helpers ──

    private ErVO makeDraftEr(String erId, String oprtrId, String rprtYr) {
        ErVO vo = new ErVO();
        vo.setErId(erId);
        vo.setOprtrId(oprtrId);
        vo.setRprtYr(rprtYr);
        vo.setErStCd("DRAFT");
        return vo;
    }

    private ErVO makeSbmtdEr(String erId, String oprtrId, String rprtYr) {
        ErVO vo = makeDraftEr(erId, oprtrId, rprtYr);
        vo.setErStCd("SBMTD");
        return vo;
    }

    private ErFuelSmryVO makeFuelSmryVO(BigDecimal ttlFuelWght, BigDecimal ttlCo2Emsn) {
        ErFuelSmryVO vo = new ErFuelSmryVO();
        vo.setTtlFuelWght(ttlFuelWght);
        vo.setTtlCo2Emsn(ttlCo2Emsn);
        return vo;
    }

    private ErFuelSmryVO makeSavedFuelSmry(String erId, String fuelTypeCd,
                                           BigDecimal ttlFuelWght, BigDecimal ttlCo2Emsn) {
        ErFuelSmryVO vo = new ErFuelSmryVO();
        vo.setErId(erId);
        vo.setFuelTypeCd(fuelTypeCd);
        vo.setTtlFuelWght(ttlFuelWght);
        vo.setTtlCo2Emsn(ttlCo2Emsn);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // list — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 가시범위 통과, 목록 정상 반환")
    void list_정상조회() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(erFuelSmryMapper.selectByErId("ER0001"))
                .willReturn(List.of(
                        makeSavedFuelSmry("ER0001", "JET_A", new BigDecimal("10000.0000"), new BigDecimal("31500.0000")),
                        makeSavedFuelSmry("ER0001", "JET_B", new BigDecimal("5000.0000"), new BigDecimal("15750.0000"))));

        List<ErFuelSmryVO> result = erFuelSmryService.list("ER0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFuelTypeCd()).isEqualTo("JET_A");
        assertThat(result.get(1).getTtlFuelWght()).isEqualByComparingTo("5000.0000");
    }

    @Test
    @DisplayName("목록 조회 — 부모 ER 미존재 → NOT_FOUND(404)")
    void list_부모ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erFuelSmryService.list("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erFuelSmryMapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("목록 조회 — 가시범위 위반 → FORBIDDEN(403)")
    void list_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erFuelSmryService.list("ER0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erFuelSmryMapper).should(never()).selectByErId(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // upsert — insert/update 분기
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upsert insert 경로 — existsByPk=false → insertFuelSmry 호출 verify")
    void upsert_insert_경로() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erFuelSmryMapper.existsByPk("ER0001", "JET_A")).willReturn(false);
        given(erFuelSmryMapper.insertFuelSmry(any(ErFuelSmryVO.class))).willReturn(1);
        given(erFuelSmryMapper.selectOne("ER0001", "JET_A"))
                .willReturn(makeSavedFuelSmry("ER0001", "JET_A", new BigDecimal("10000"), new BigDecimal("31500")));

        ErFuelSmryVO req = makeFuelSmryVO(new BigDecimal("10000"), new BigDecimal("31500"));
        ErFuelSmryVO result = erFuelSmryService.upsert("ER0001", "JET_A", req, airlineUserOP0001);

        assertThat(result.getFuelTypeCd()).isEqualTo("JET_A");
        then(erFuelSmryMapper).should(times(1)).insertFuelSmry(any(ErFuelSmryVO.class));
        then(erFuelSmryMapper).should(never()).updateFuelSmry(any());
    }

    @Test
    @DisplayName("upsert update 경로 — existsByPk=true → updateFuelSmry 호출 verify")
    void upsert_update_경로() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erFuelSmryMapper.existsByPk("ER0001", "JET_A")).willReturn(true);
        given(erFuelSmryMapper.updateFuelSmry(any(ErFuelSmryVO.class))).willReturn(1);
        given(erFuelSmryMapper.selectOne("ER0001", "JET_A"))
                .willReturn(makeSavedFuelSmry("ER0001", "JET_A", new BigDecimal("12000"), new BigDecimal("37800")));

        ErFuelSmryVO req = makeFuelSmryVO(new BigDecimal("12000"), new BigDecimal("37800"));
        ErFuelSmryVO result = erFuelSmryService.upsert("ER0001", "JET_A", req, airlineUserOP0001);

        assertThat(result.getTtlFuelWght()).isEqualByComparingTo("12000");
        then(erFuelSmryMapper).should(times(1)).updateFuelSmry(any(ErFuelSmryVO.class));
        then(erFuelSmryMapper).should(never()).insertFuelSmry(any());
    }

    @Test
    @DisplayName("upsert — 부모 ER DRAFT 아님(SBMTD) → BAD_REQUEST(400)")
    void upsert_부모ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erFuelSmryService.upsert("ER0001", "JET_A",
                makeFuelSmryVO(new BigDecimal("10000"), null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erFuelSmryMapper).should(never()).insertFuelSmry(any());
        then(erFuelSmryMapper).should(never()).updateFuelSmry(any());
    }

    @Test
    @DisplayName("upsert — AIRLINE 본인 외 → FORBIDDEN(403)")
    void upsert_타운영사_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> erFuelSmryService.upsert("ER0001", "JET_A",
                makeFuelSmryVO(new BigDecimal("10000"), null), airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("upsert — ttl_fuel_wght 음수 → BAD_REQUEST(400)")
    void upsert_ttlFuelWght_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erFuelSmryService.upsert("ER0001", "JET_A",
                makeFuelSmryVO(new BigDecimal("-1.00"), null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erFuelSmryMapper).should(never()).insertFuelSmry(any());
    }

    @Test
    @DisplayName("upsert — ttl_co2_emsn 음수 → BAD_REQUEST(400)")
    void upsert_ttlCo2Emsn_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erFuelSmryService.upsert("ER0001", "JET_A",
                makeFuelSmryVO(null, new BigDecimal("-0.01")), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erFuelSmryMapper).should(never()).insertFuelSmry(any());
    }

    @Test
    @DisplayName("upsert — ttl_fuel_wght=0 (기본값) 정상 처리")
    void upsert_ttlFuelWght_0_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erFuelSmryMapper.existsByPk("ER0001", "JET_A")).willReturn(false);
        given(erFuelSmryMapper.insertFuelSmry(any(ErFuelSmryVO.class))).willReturn(1);
        given(erFuelSmryMapper.selectOne("ER0001", "JET_A"))
                .willReturn(makeSavedFuelSmry("ER0001", "JET_A", BigDecimal.ZERO, BigDecimal.ZERO));

        // ttlFuelWght=0 은 허용 (>= 0 조건)
        assertThatCode(() -> erFuelSmryService.upsert("ER0001", "JET_A",
                makeFuelSmryVO(BigDecimal.ZERO, BigDecimal.ZERO), airlineUserOP0001))
                .doesNotThrowAnyException();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDelete — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDelete_자식미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erFuelSmryMapper.softDeleteOne("ER0001", "NONE_CD", "airline01")).willReturn(0);

        assertThatThrownBy(() -> erFuelSmryService.softDelete("ER0001", "NONE_CD", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
