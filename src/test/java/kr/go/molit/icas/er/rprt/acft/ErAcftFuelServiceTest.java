package kr.go.molit.icas.er.rprt.acft;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.acft.domain.ErAcftFuelVO;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErAcftFuelService 단위 테스트 — 항공기·연료 CRUD + 검증")
class ErAcftFuelServiceTest {

    @Mock ErAcftFuelMapper erAcftFuelMapper;
    @Mock ErMapper         erMapper;
    @Mock DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErAcftFuelService erAcftFuelService;

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

    private ErAcftFuelVO makeAcftFuelVO(String regisMark, String fuelTypeCd,
                                        String ownrLsSeCd, String dnstySecCd) {
        ErAcftFuelVO vo = new ErAcftFuelVO();
        vo.setRegisMark(regisMark);
        vo.setFuelTypeCd(fuelTypeCd);
        vo.setOwnrLsSeCd(ownrLsSeCd);
        vo.setDnstySecCd(dnstySecCd);
        return vo;
    }

    private ErAcftFuelVO makeSavedAcftFuel(String erId, int acftSn, String regisMark) {
        ErAcftFuelVO vo = new ErAcftFuelVO();
        vo.setErId(erId);
        vo.setAcftSn(acftSn);
        vo.setRegisMark(regisMark);
        vo.setFuelTypeCd("JET_A");
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
        given(erAcftFuelMapper.selectByErId("ER0001"))
                .willReturn(List.of(
                        makeSavedAcftFuel("ER0001", 1, "HL1234"),
                        makeSavedAcftFuel("ER0001", 2, "HL5678")));

        List<ErAcftFuelVO> result = erAcftFuelService.list("ER0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAcftSn()).isEqualTo(1);
        assertThat(result.get(1).getRegisMark()).isEqualTo("HL5678");
    }

    @Test
    @DisplayName("목록 조회 — 부모 ER 미존재 → NOT_FOUND(404)")
    void list_부모ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erAcftFuelService.list("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erAcftFuelMapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("목록 조회 — 가시범위 위반 → FORBIDDEN(403)")
    void list_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erAcftFuelService.list("ER0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erAcftFuelMapper).should(never()).selectByErId(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // add — 항공기·연료 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("추가 정상 — sn 채번 + insert 호출 verify (ArgumentCaptor)")
    void add_정상추가_sn채번_insert() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAcftFuelMapper.existsByRegisMark("ER0001", "HL1234", 0)).willReturn(false);
        given(erAcftFuelMapper.selectNextSn("ER0001")).willReturn(1);
        given(erAcftFuelMapper.insertAcftFuel(any(ErAcftFuelVO.class))).willReturn(1);
        given(erAcftFuelMapper.selectOne("ER0001", 1))
                .willReturn(makeSavedAcftFuel("ER0001", 1, "HL1234"));

        ErAcftFuelVO req = makeAcftFuelVO("HL1234", "JET_A", "OWN", "STD");
        ErAcftFuelVO result = erAcftFuelService.add("ER0001", req, airlineUserOP0001);

        assertThat(result.getAcftSn()).isEqualTo(1);
        assertThat(result.getErId()).isEqualTo("ER0001");

        ArgumentCaptor<ErAcftFuelVO> captor = ArgumentCaptor.forClass(ErAcftFuelVO.class);
        then(erAcftFuelMapper).should(times(1)).insertAcftFuel(captor.capture());
        assertThat(captor.getValue().getAcftSn()).isEqualTo(1);
        assertThat(captor.getValue().getErId()).isEqualTo("ER0001");
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("추가 — 부모 ER 이 DRAFT 아님(SBMTD) → BAD_REQUEST(400)")
    void add_부모ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO("HL1234", "JET_A", null, null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAcftFuelMapper).should(never()).insertAcftFuel(any());
    }

    @Test
    @DisplayName("추가 — AIRLINE 본인 외 → FORBIDDEN(403)")
    void add_타운영사_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO("HL1234", "JET_A", null, null), airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("추가 — regis_mark 누락 → BAD_REQUEST(400)")
    void add_regisMark_누락_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO(null, "JET_A", null, null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAcftFuelMapper).should(never()).insertAcftFuel(any());
    }

    @Test
    @DisplayName("추가 — fuel_type_cd 누락 → BAD_REQUEST(400)")
    void add_fuelTypeCd_누락_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO("HL1234", null, null, null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAcftFuelMapper).should(never()).insertAcftFuel(any());
    }

    @Test
    @DisplayName("추가 — ownr_ls_se_cd 화이트리스트 외(XXX) → BAD_REQUEST(400)")
    void add_ownrLsSeCd_화이트리스트외_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO("HL1234", "JET_A", "XXX", null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAcftFuelMapper).should(never()).insertAcftFuel(any());
    }

    @Test
    @DisplayName("추가 — dnsty_se_cd 화이트리스트 외(INVALID) → BAD_REQUEST(400)")
    void add_dnstySecCd_화이트리스트외_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO("HL1234", "JET_A", "OWN", "INVALID"), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erAcftFuelMapper).should(never()).insertAcftFuel(any());
    }

    @Test
    @DisplayName("추가 — 같은 ER 내 regis_mark 중복 → CONFLICT(409)")
    void add_regisMark_중복_CONFLICT() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAcftFuelMapper.existsByRegisMark("ER0001", "HL1234", 0)).willReturn(true);

        assertThatThrownBy(() -> erAcftFuelService.add("ER0001",
                makeAcftFuelVO("HL1234", "JET_A", null, null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(erAcftFuelMapper).should(never()).insertAcftFuel(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update — 항공기·연료 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — 같은 regis_mark 본인 행 수정: excludeSn 활용 verify")
    void update_본인행_regisMark_수정_excludeSn_verify() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAcftFuelMapper.selectOne("ER0001", 1))
                .willReturn(makeSavedAcftFuel("ER0001", 1, "HL1234"));
        // 자기 자신(acftSn=1) 제외하면 중복 없음
        given(erAcftFuelMapper.existsByRegisMark("ER0001", "HL1234", 1)).willReturn(false);
        given(erAcftFuelMapper.updateAcftFuel(any(ErAcftFuelVO.class))).willReturn(1);

        ErAcftFuelVO req = makeAcftFuelVO("HL1234", "JET_A", "OWN", "STD");

        assertThatCode(() -> erAcftFuelService.update("ER0001", 1, req, airlineUserOP0001))
                .doesNotThrowAnyException();

        // excludeSn=1 (자기 자신 제외) 로 existsByRegisMark 호출 verify
        then(erAcftFuelMapper).should().existsByRegisMark("ER0001", "HL1234", 1);
    }

    @Test
    @DisplayName("수정 — 자식 미존재 → NOT_FOUND(404)")
    void update_자식미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erAcftFuelMapper.selectOne("ER0001", 99)).willReturn(null);

        assertThatThrownBy(() -> erAcftFuelService.update("ER0001", 99,
                makeAcftFuelVO("HL1234", "JET_A", null, null), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erAcftFuelMapper).should(never()).updateAcftFuel(any());
    }
}
