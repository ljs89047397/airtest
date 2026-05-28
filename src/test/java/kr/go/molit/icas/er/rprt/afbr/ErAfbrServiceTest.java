package kr.go.molit.icas.er.rprt.afbr;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.afbr.domain.ErAfbrVO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
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
@DisplayName("ErAfbrService 단위 테스트 — 평균 연료연소율 upsert + 검증")
class ErAfbrServiceTest {

    @Mock ErAfbrMapper       erAfbrMapper;
    @Mock ErMapper           erMapper;
    @Mock DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErAfbrService erAfbrService;

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

    private ErAfbrVO makeAfbrVO(BigDecimal afbrVal, String afbrUnit) {
        ErAfbrVO vo = new ErAfbrVO();
        vo.setAfbrVal(afbrVal);
        vo.setAfbrUnit(afbrUnit);
        return vo;
    }

    private ErAfbrVO makeSavedAfbr(String erId, String acftTypeCd, BigDecimal afbrVal) {
        ErAfbrVO vo = new ErAfbrVO();
        vo.setErId(erId);
        vo.setAcftTypeCd(acftTypeCd);
        vo.setAfbrVal(afbrVal);
        vo.setAfbrUnit("kg/min");
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
        given(erAfbrMapper.selectByErId("ER0001"))
                .willReturn(List.of(
                        makeSavedAfbr("ER0001", "B737", new BigDecimal("2.50")),
                        makeSavedAfbr("ER0001", "B747", new BigDecimal("3.80"))));

        List<ErAfbrVO> result = erAfbrService.list("ER0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAcftTypeCd()).isEqualTo("B737");
        assertThat(result.get(1).getAfbrVal()).isEqualByComparingTo("3.80");
    }

    @Test
    @DisplayName("목록 조회 — 부모 ER 미존재 → NOT_FOUND(404)")
    void list_부모ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erAfbrService.list("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erAfbrMapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("목록 조회 — 가시범위 위반 → FORBIDDEN(403)")
    void list_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erAfbrService.list("ER0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erAfbrMapper).should(never()).selectByErId(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // upsert — insert/update 분기
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upsert insert 경로 — existsByPk=false → insertAfbr 호출 verify")
    void upsert_insert_경로() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAfbrMapper.existsByPk("ER0001", "B737")).willReturn(false);
        given(erAfbrMapper.insertAfbr(any(ErAfbrVO.class))).willReturn(1);
        given(erAfbrMapper.selectOne("ER0001", "B737"))
                .willReturn(makeSavedAfbr("ER0001", "B737", new BigDecimal("2.50")));

        ErAfbrVO req = makeAfbrVO(new BigDecimal("2.50"), "kg/min");
        ErAfbrVO result = erAfbrService.upsert("ER0001", "B737", req, airlineUserOP0001);

        assertThat(result.getAcftTypeCd()).isEqualTo("B737");
        then(erAfbrMapper).should(times(1)).insertAfbr(any(ErAfbrVO.class));
        then(erAfbrMapper).should(never()).updateAfbr(any());
    }

    @Test
    @DisplayName("upsert update 경로 — existsByPk=true → updateAfbr 호출 verify")
    void upsert_update_경로() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAfbrMapper.existsByPk("ER0001", "B737")).willReturn(true);
        given(erAfbrMapper.updateAfbr(any(ErAfbrVO.class))).willReturn(1);
        given(erAfbrMapper.selectOne("ER0001", "B737"))
                .willReturn(makeSavedAfbr("ER0001", "B737", new BigDecimal("3.00")));

        ErAfbrVO req = makeAfbrVO(new BigDecimal("3.00"), "kg/min");
        ErAfbrVO result = erAfbrService.upsert("ER0001", "B737", req, airlineUserOP0001);

        assertThat(result.getAfbrVal()).isEqualByComparingTo("3.00");
        then(erAfbrMapper).should(times(1)).updateAfbr(any(ErAfbrVO.class));
        then(erAfbrMapper).should(never()).insertAfbr(any());
    }

    @Test
    @DisplayName("upsert — 부모 ER DRAFT 아님(SBMTD) → BAD_REQUEST(400)")
    void upsert_부모ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAfbrService.upsert("ER0001", "B737",
                makeAfbrVO(new BigDecimal("2.50"), null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAfbrMapper).should(never()).insertAfbr(any());
        then(erAfbrMapper).should(never()).updateAfbr(any());
    }

    @Test
    @DisplayName("upsert — AIRLINE 본인 외 → FORBIDDEN(403)")
    void upsert_타운영사_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> erAfbrService.upsert("ER0001", "B737",
                makeAfbrVO(new BigDecimal("2.50"), null), airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("upsert — afbr_val <= 0 → BAD_REQUEST(400)")
    void upsert_afbrVal_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAfbrService.upsert("ER0001", "B737",
                makeAfbrVO(new BigDecimal("-1.00"), null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAfbrMapper).should(never()).insertAfbr(any());
    }

    @Test
    @DisplayName("upsert — afbr_unit 화이트리스트 외(ton/hr) → BAD_REQUEST(400)")
    void upsert_afbrUnit_화이트리스트외_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAfbrService.upsert("ER0001", "B737",
                makeAfbrVO(new BigDecimal("2.50"), "ton/hr"), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAfbrMapper).should(never()).insertAfbr(any());
    }

    @Test
    @DisplayName("upsert — afbr_unit null 허용 (정상 처리)")
    void upsert_afbrUnit_null_허용_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAfbrMapper.existsByPk("ER0001", "B737")).willReturn(false);
        given(erAfbrMapper.insertAfbr(any(ErAfbrVO.class))).willReturn(1);
        given(erAfbrMapper.selectOne("ER0001", "B737"))
                .willReturn(makeSavedAfbr("ER0001", "B737", new BigDecimal("2.50")));

        // afbrUnit=null 이면 기본값 처리, 예외 없이 정상
        assertThatCode(() -> erAfbrService.upsert("ER0001", "B737",
                makeAfbrVO(new BigDecimal("2.50"), null), airlineUserOP0001))
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
        given(erAfbrMapper.softDeleteOne("ER0001", "B999", "airline01")).willReturn(0);

        assertThatThrownBy(() -> erAfbrService.softDelete("ER0001", "B999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
