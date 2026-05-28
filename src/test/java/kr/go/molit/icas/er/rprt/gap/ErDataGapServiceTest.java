package kr.go.molit.icas.er.rprt.gap;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.cntry.ErCntryPairCo2Mapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.gap.domain.ErDataGapVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErDataGapService 단위 테스트 — 데이터 갭 CRUD + 5% 임계치 자동 판정")
class ErDataGapServiceTest {

    @Mock
    ErDataGapMapper erDataGapMapper;

    @Mock
    ErCntryPairCo2Mapper erCntryPairCo2Mapper;

    @Mock
    ErMapper erMapper;

    @Mock
    DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErDataGapService erDataGapService;

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
        ErVO er = new ErVO();
        er.setErId(erId);
        er.setOprtrId(oprtrId);
        er.setRprtYr(rprtYr);
        er.setErStCd("DRAFT");
        return er;
    }

    private ErVO makeSubmittedEr(String erId, String oprtrId, String rprtYr) {
        ErVO er = makeDraftEr(erId, oprtrId, rprtYr);
        er.setErStCd("SBMTD");
        return er;
    }

    private ErDataGapVO makeGapVO(BigDecimal afctCo2Emsn, String thrshld5pctXcYn) {
        ErDataGapVO vo = new ErDataGapVO();
        vo.setAfctCo2Emsn(afctCo2Emsn);
        vo.setThrshld5pctXcYn(thrshld5pctXcYn); // 사용자 입력값 — 서비스에서 무시됨
        vo.setGapCauseCd("DATA_MISSING");
        vo.setGapTypeCd("GAP_01");
        return vo;
    }

    private ErDataGapVO makeSavedGap(String erId, int gapSn, String thrshld) {
        ErDataGapVO vo = new ErDataGapVO();
        vo.setErId(erId);
        vo.setGapSn(gapSn);
        vo.setThrshld5pctXcYn(thrshld);
        return vo;
    }

    /**
     * add 호출을 위한 공통 설정 (DRAFT ER + assertOwnAirline + selectNextSn + insert + selectOne).
     */
    private void setUpAddStubs(String erId, BigDecimal totalCo2, int nextSn,
                                ErDataGapVO savedGap) {
        given(erMapper.selectByErId(erId)).willReturn(makeDraftEr(erId, "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erCntryPairCo2Mapper.sumCo2EmsnByEr(erId)).willReturn(totalCo2);
        given(erDataGapMapper.selectNextSn(erId)).willReturn(nextSn);
        given(erDataGapMapper.insert(any(ErDataGapVO.class))).willReturn(1);
        given(erDataGapMapper.selectOne(erId, nextSn)).willReturn(savedGap);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // list
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list — 정상: ER 존재 + 가시범위 통과 → 목록 반환")
    void list_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));
        given(erDataGapMapper.selectByErId("ER0001"))
                .willReturn(List.of(makeSavedGap("ER0001", 1, "N"),
                                    makeSavedGap("ER0001", 2, "Y")));

        List<ErDataGapVO> result = erDataGapService.list("ER0001", airlineUserOP0001);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getGapSn()).isEqualTo(1);
    }

    @Test
    @DisplayName("list — 부모 ER 미존재 → NOT_FOUND(404)")
    void list_ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erDataGapService.list("ER9999", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erDataGapMapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("list — 가시범위 위반 → FORBIDDEN(403)")
    void list_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator)
                .assertOprtrAccessible(eq(airlineUserOP0002), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erDataGapService.list("ER0001", airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // add — 5% 임계치 자동 판정 (핵심 시나리오)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("add — 자동판정 시나리오1: totalCo2=1,000,000 / afct=60,000 (6%) → thrshld5pctXcYn='Y' verify")
    void add_자동판정_6퍼센트_Y() {
        BigDecimal totalCo2 = new BigDecimal("1000000");
        BigDecimal afctCo2  = new BigDecimal("60000");
        ErDataGapVO savedGap = makeSavedGap("ER0001", 1, "Y");

        setUpAddStubs("ER0001", totalCo2, 1, savedGap);

        ErDataGapVO reqVo = makeGapVO(afctCo2, "N"); // 사용자 입력 'N' 이어도
        erDataGapService.add("ER0001", reqVo, airlineUserOP0001);

        ArgumentCaptor<ErDataGapVO> captor = ArgumentCaptor.forClass(ErDataGapVO.class);
        then(erDataGapMapper).should(times(1)).insert(captor.capture());
        assertThat(captor.getValue().getThrshld5pctXcYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("add — 자동판정 시나리오2: totalCo2=1,000,000 / afct=40,000 (4%) → thrshld5pctXcYn='N' verify")
    void add_자동판정_4퍼센트_N() {
        BigDecimal totalCo2 = new BigDecimal("1000000");
        BigDecimal afctCo2  = new BigDecimal("40000");
        ErDataGapVO savedGap = makeSavedGap("ER0001", 1, "N");

        setUpAddStubs("ER0001", totalCo2, 1, savedGap);

        ErDataGapVO reqVo = makeGapVO(afctCo2, "Y"); // 사용자 입력 'Y' 이어도
        erDataGapService.add("ER0001", reqVo, airlineUserOP0001);

        ArgumentCaptor<ErDataGapVO> captor = ArgumentCaptor.forClass(ErDataGapVO.class);
        then(erDataGapMapper).should(times(1)).insert(captor.capture());
        assertThat(captor.getValue().getThrshld5pctXcYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("add — 자동판정 시나리오3: totalCo2=0 (cntry_pair_co2 미입력) → thrshld5pctXcYn='Y' verify (안전 쪽)")
    void add_자동판정_총CO2_0_Y() {
        BigDecimal totalCo2 = BigDecimal.ZERO;
        BigDecimal afctCo2  = new BigDecimal("1000");
        ErDataGapVO savedGap = makeSavedGap("ER0001", 1, "Y");

        setUpAddStubs("ER0001", totalCo2, 1, savedGap);

        ErDataGapVO reqVo = makeGapVO(afctCo2, "N");
        erDataGapService.add("ER0001", reqVo, airlineUserOP0001);

        ArgumentCaptor<ErDataGapVO> captor = ArgumentCaptor.forClass(ErDataGapVO.class);
        then(erDataGapMapper).should(times(1)).insert(captor.capture());
        assertThat(captor.getValue().getThrshld5pctXcYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("add — 자동판정 시나리오4: 사용자 입력 'N' 이어도 6% 결과로 자동 덮어쓰기 verify")
    void add_자동판정_사용자입력무시_덮어쓰기() {
        BigDecimal totalCo2 = new BigDecimal("1000000");
        BigDecimal afctCo2  = new BigDecimal("60000"); // 6% → Y
        ErDataGapVO savedGap = makeSavedGap("ER0001", 1, "Y");

        setUpAddStubs("ER0001", totalCo2, 1, savedGap);

        ErDataGapVO reqVo = makeGapVO(afctCo2, "N"); // 사용자는 N 으로 설정
        erDataGapService.add("ER0001", reqVo, airlineUserOP0001);

        ArgumentCaptor<ErDataGapVO> captor = ArgumentCaptor.forClass(ErDataGapVO.class);
        then(erDataGapMapper).should(times(1)).insert(captor.capture());
        // 서비스가 강제로 Y 로 덮어써야 함
        assertThat(captor.getValue().getThrshld5pctXcYn())
                .as("사용자 입력 'N' 이 자동 판정 결과 'Y' 로 덮어쓰여야 한다")
                .isEqualTo("Y");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // add — 기본 검증
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("add — 부모 ER DRAFT 아님 → BAD_REQUEST(400)")
    void add_ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSubmittedEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() -> erDataGapService.add("ER0001", makeGapVO(new BigDecimal("1000"), "N"), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("add — AIRLINE 본인 외 → FORBIDDEN(403)")
    void add_타운영사_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 등록·수정할 수 있습니다."))
                .given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0002), eq("OP0001"));

        assertThatThrownBy(() -> erDataGapService.add("ER0001", makeGapVO(new BigDecimal("1000"), "N"), airlineUserOP0002))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("add — afctCo2Emsn 음수 → BAD_REQUEST(400)")
    void add_afctCo2Emsn_음수_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        assertThatThrownBy(() ->
                erDataGapService.add("ER0001", makeGapVO(new BigDecimal("-100"), "N"), airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update — 동일 자동판정 적용
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update — 동일 자동판정 적용: totalCo2=1,000,000 / afct=60,000 → thrshld5pctXcYn='Y'")
    void update_자동판정_적용() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erDataGapMapper.selectOne("ER0001", 1)).willReturn(makeSavedGap("ER0001", 1, "N"));
        given(erCntryPairCo2Mapper.sumCo2EmsnByEr("ER0001")).willReturn(new BigDecimal("1000000"));
        given(erDataGapMapper.update(any(ErDataGapVO.class))).willReturn(1);

        ErDataGapVO reqVo = makeGapVO(new BigDecimal("60000"), "N"); // 사용자 입력 N
        erDataGapService.update("ER0001", 1, reqVo, airlineUserOP0001);

        ArgumentCaptor<ErDataGapVO> captor = ArgumentCaptor.forClass(ErDataGapVO.class);
        then(erDataGapMapper).should(times(1)).update(captor.capture());
        assertThat(captor.getValue().getThrshld5pctXcYn()).isEqualTo("Y");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDelete
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("softDelete — 정상 소프트삭제")
    void softDelete_정상() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erDataGapMapper.softDelete("ER0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> erDataGapService.softDelete("ER0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erDataGapMapper).should(times(1)).softDelete("ER0001", 1, "airline01");
    }

    @Test
    @DisplayName("softDelete — 자식 미존재(affected=0) → NOT_FOUND(404)")
    void softDelete_자식미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001", "OP0001", "2026"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erDataGapMapper.softDelete("ER0001", 99, "airline01")).willReturn(0);

        assertThatThrownBy(() -> erDataGapService.softDelete("ER0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
