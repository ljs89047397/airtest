package kr.go.molit.icas.er.rprt.vrfr;

import kr.go.molit.icas.com.vrfcn.VrfcnInstMapper;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.er.rprt.vrfr.domain.ErVrfrInfoVO;
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
@DisplayName("ErVrfrInfoService 단위 테스트 — 참여 검증기관 CRUD + 상태 검증")
class ErVrfrInfoServiceTest {

    @Mock ErVrfrInfoMapper erVrfrInfoMapper;
    @Mock ErMapper         erMapper;
    @Mock VrfcnInstMapper  vrfcnInstMapper;
    @Mock DataScopeValidator dataScopeValidator;

    @InjectMocks
    ErVrfrInfoService erVrfrInfoService;

    // ── 공통 fixture ──
    private IcasUser molitUser;
    private IcasUser airlineUserOP0001;

    @BeforeEach
    void setUpFixtures() {
        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    // ── helper: 상태별 ErVO fixture ──
    private ErVO makeDraftEr(String erId) {
        ErVO vo = new ErVO();
        vo.setErId(erId);
        vo.setOprtrId("OP0001");
        vo.setRprtYr("2026");
        vo.setErStCd("DRAFT");
        return vo;
    }

    private ErVO makeSbmtdEr(String erId) {
        ErVO vo = makeDraftEr(erId);
        vo.setErStCd("SBMTD");
        return vo;
    }

    // ── helper: ErVrfrInfoVO fixture ──
    private ErVrfrInfoVO makeVrfrInfo(String erId, int vrfrSn, String vrfcnInstId) {
        ErVrfrInfoVO vo = new ErVrfrInfoVO();
        vo.setErId(erId);
        vo.setVrfrSn(vrfrSn);
        vo.setVrfcnInstId(vrfcnInstId);
        vo.setCnctDesc("참여 개요");
        vo.setAccrdDtl("인증 상세");
        return vo;
    }

    // ── helper: VrfcnInstVO fixture ──
    private VrfcnInstVO makeVrfcnInst(String vrfcnInstId) {
        VrfcnInstVO vo = new VrfcnInstVO();
        vo.setVrfcnInstId(vrfcnInstId);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // listByErId — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 가시범위 통과, 목록 정상 반환")
    void listByErId_정상조회() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator)
                .assertOprtrAccessible(eq(molitUser), eq("OP0001"), eq("2026"));
        given(erVrfrInfoMapper.selectByErId("ER0001"))
                .willReturn(List.of(
                        makeVrfrInfo("ER0001", 1, "VI0001"),
                        makeVrfrInfo("ER0001", 2, "VI0002")));

        List<ErVrfrInfoVO> result = erVrfrInfoService.listByErId("ER0001", molitUser);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVrfrSn()).isEqualTo(1);
        assertThat(result.get(1).getVrfrSn()).isEqualTo(2);
    }

    @Test
    @DisplayName("목록 조회 — 부모 ER 미존재 → NOT_FOUND 예외")
    void listByErId_부모ER미존재_NOT_FOUND() {
        given(erMapper.selectByErId("ER9999")).willReturn(null);

        assertThatThrownBy(() -> erVrfrInfoService.listByErId("ER9999", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erVrfrInfoMapper).should(never()).selectByErId(any());
    }

    @Test
    @DisplayName("목록 조회 — 가시범위 위반 → FORBIDDEN 예외")
    void listByErId_가시범위위반_FORBIDDEN() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."))
                .given(dataScopeValidator).assertOprtrAccessible(eq(airlineUserOP0001), eq("OP0001"), eq("2026"));

        assertThatThrownBy(() -> erVrfrInfoService.listByErId("ER0001", airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(erVrfrInfoMapper).should(never()).selectByErId(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addVrfr — 참여 검증기관 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("참여 검증기관 추가 — 정상: sn 채번 + insert 호출 verify")
    void addVrfr_정상추가_sn채번_insert() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0001")).willReturn(makeVrfcnInst("VI0001"));
        given(erVrfrInfoMapper.existsByVrfcnInst("ER0001", "VI0001", 0)).willReturn(false);
        given(erVrfrInfoMapper.selectNextSn("ER0001")).willReturn(1);
        given(erVrfrInfoMapper.insertVrfrInfo(any(ErVrfrInfoVO.class))).willReturn(1);
        given(erVrfrInfoMapper.selectOne("ER0001", 1)).willReturn(makeVrfrInfo("ER0001", 1, "VI0001"));

        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI0001");
        reqVo.setCnctDesc("참여 개요");

        ErVrfrInfoVO result = erVrfrInfoService.addVrfr("ER0001", reqVo, airlineUserOP0001);

        assertThat(result.getVrfrSn()).isEqualTo(1);
        assertThat(result.getVrfcnInstId()).isEqualTo("VI0001");

        // insert 호출 시 sn=1, erId="ER0001" 설정 verify
        ArgumentCaptor<ErVrfrInfoVO> captor = ArgumentCaptor.forClass(ErVrfrInfoVO.class);
        then(erVrfrInfoMapper).should(times(1)).insertVrfrInfo(captor.capture());
        assertThat(captor.getValue().getVrfrSn()).isEqualTo(1);
        assertThat(captor.getValue().getErId()).isEqualTo("ER0001");
        assertThat(captor.getValue().getFrstRegUserId()).isEqualTo("airline01");
    }

    @Test
    @DisplayName("참여 검증기관 추가 — 부모 ER 이 SBMTD 상태 → BAD_REQUEST 예외 (DRAFT만 허용)")
    void addVrfr_부모ER_DRAFT아님_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeSbmtdEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));

        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI0001");

        assertThatThrownBy(() -> erVrfrInfoService.addVrfr("ER0001", reqVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erVrfrInfoMapper).should(never()).insertVrfrInfo(any());
    }

    @Test
    @DisplayName("참여 검증기관 추가 — vrfcnInstId 미존재 검증기관 → BAD_REQUEST 예외")
    void addVrfr_vrfcnInstId_미존재_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(vrfcnInstMapper.selectByVrfcnInstId("VI9999")).willReturn(null);

        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI9999");

        assertThatThrownBy(() -> erVrfrInfoService.addVrfr("ER0001", reqVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erVrfrInfoMapper).should(never()).insertVrfrInfo(any());
    }

    @Test
    @DisplayName("참여 검증기관 추가 — 동일 vrfcnInstId 중복 등록 → BAD_REQUEST 예외")
    void addVrfr_중복vrfcnInstId_BAD_REQUEST() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0001")).willReturn(makeVrfcnInst("VI0001"));
        given(erVrfrInfoMapper.existsByVrfcnInst("ER0001", "VI0001", 0)).willReturn(true); // 중복

        ErVrfrInfoVO reqVo = new ErVrfrInfoVO();
        reqVo.setVrfcnInstId("VI0001");

        assertThatThrownBy(() -> erVrfrInfoService.addVrfr("ER0001", reqVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(erVrfrInfoMapper).should(never()).insertVrfrInfo(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateVrfr — 참여 검증기관 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("참여 검증기관 수정 — 정상 수정")
    void updateVrfr_정상수정() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erVrfrInfoMapper.selectOne("ER0001", 1)).willReturn(makeVrfrInfo("ER0001", 1, "VI0001"));
        given(erVrfrInfoMapper.updateVrfrInfo(any(ErVrfrInfoVO.class))).willReturn(1);

        ErVrfrInfoVO updateVo = new ErVrfrInfoVO();
        updateVo.setCnctDesc("수정된 참여 개요");

        assertThatCode(() -> erVrfrInfoService.updateVrfr("ER0001", 1, updateVo, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erVrfrInfoMapper).should(times(1)).updateVrfrInfo(any(ErVrfrInfoVO.class));
    }

    @Test
    @DisplayName("참여 검증기관 수정 — 미존재 항목 → NOT_FOUND 예외")
    void updateVrfr_미존재항목_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erVrfrInfoMapper.selectOne("ER0001", 99)).willReturn(null);

        ErVrfrInfoVO updateVo = new ErVrfrInfoVO();
        updateVo.setCnctDesc("수정된 내용");

        assertThatThrownBy(() -> erVrfrInfoService.updateVrfr("ER0001", 99, updateVo, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(erVrfrInfoMapper).should(never()).updateVrfrInfo(any());
    }

    @Test
    @DisplayName("참여 검증기관 수정 — 다른 vrfcnInstId 로 변경 시 중복 체크 (excludeSn=vrfrSn 활용 verify)")
    void updateVrfr_다른vrfcnInstId변경시_중복체크_excludeSn_verify() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erVrfrInfoMapper.selectOne("ER0001", 1)).willReturn(makeVrfrInfo("ER0001", 1, "VI0001"));
        given(vrfcnInstMapper.selectByVrfcnInstId("VI0002")).willReturn(makeVrfcnInst("VI0002"));
        given(erVrfrInfoMapper.existsByVrfcnInst("ER0001", "VI0002", 1)).willReturn(false); // 중복 없음
        given(erVrfrInfoMapper.updateVrfrInfo(any(ErVrfrInfoVO.class))).willReturn(1);

        ErVrfrInfoVO updateVo = new ErVrfrInfoVO();
        updateVo.setVrfcnInstId("VI0002"); // 다른 기관으로 변경
        updateVo.setCnctDesc("수정된 개요");

        assertThatCode(() -> erVrfrInfoService.updateVrfr("ER0001", 1, updateVo, airlineUserOP0001))
                .doesNotThrowAnyException();

        // existsByVrfcnInst 호출 시 excludeSn=1 (자기 자신 제외) verify
        then(erVrfrInfoMapper).should().existsByVrfcnInst("ER0001", "VI0002", 1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteVrfr — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 정상 삭제")
    void softDeleteVrfr_정상삭제() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erVrfrInfoMapper.softDeleteOne("ER0001", 1, "airline01")).willReturn(1);

        assertThatCode(() -> erVrfrInfoService.softDeleteVrfr("ER0001", 1, airlineUserOP0001))
                .doesNotThrowAnyException();

        then(erVrfrInfoMapper).should().softDeleteOne("ER0001", 1, "airline01");
    }

    @Test
    @DisplayName("소프트삭제 — 미존재 항목 → NOT_FOUND 예외 (softDeleteOne 반환값 0)")
    void softDeleteVrfr_미존재항목_NOT_FOUND() {
        given(erMapper.selectByErId("ER0001")).willReturn(makeDraftEr("ER0001"));
        willDoNothing().given(dataScopeValidator).assertOwnAirline(eq(airlineUserOP0001), eq("OP0001"));
        given(erVrfrInfoMapper.softDeleteOne("ER0001", 99, "airline01")).willReturn(0); // 없음

        assertThatThrownBy(() -> erVrfrInfoService.softDeleteVrfr("ER0001", 99, airlineUserOP0001))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
