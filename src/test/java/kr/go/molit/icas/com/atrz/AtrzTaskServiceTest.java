package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtrzTaskService 단위 테스트")
class AtrzTaskServiceTest {

    @Mock
    AtrzTaskMapper atrzTaskMapper;

    @InjectMocks
    AtrzTaskService atrzTaskService;

    // ── 공통 fixture ──
    private IcasUser molitUser;
    private IcasUser kotsaUser;
    private IcasUser airlineUser;

    @BeforeEach
    void setUpFixtures() {
        molitUser = IcasUser.builder()
                .userId("molit01")
                .userNm("국토부 담당자")
                .ognzSeCd("MOLIT")
                .ognzId("ORG_MOLIT")
                .master(false)
                .roleIds(List.of("ADMIN"))
                .build();

        kotsaUser = IcasUser.builder()
                .userId("kotsa01")
                .userNm("교통안전공단 담당자")
                .ognzSeCd("KOTSA")
                .ognzId("ORG_KOTSA")
                .master(false)
                .roleIds(List.of("KOTSA_ADMIN"))
                .build();

        airlineUser = IcasUser.builder()
                .userId("airline01")
                .userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE")
                .ognzId("ORG_AIR01")
                .oprtrId("OP0001")
                .master(false)
                .roleIds(List.of("AIRLINE_USER"))
                .build();
    }

    private AtrzTaskVO validAtrzTaskVO() {
        AtrzTaskVO vo = new AtrzTaskVO();
        vo.setAtrzTaskId("ATZ_EMP_PLAN");
        vo.setAtrzTaskNm("고용계획 결재");
        vo.setAtrzTaskDesc("고용계획서 결재 업무");
        vo.setSysSeCd("EMP");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectAtrzTasks
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 목록 조회 — sysSeCd null 이면 필터 없이 Mapper 호출")
    void selectAtrzTasks_sysSeCdNull_전체조회() {
        AtrzTaskVO vo = validAtrzTaskVO();
        given(atrzTaskMapper.selectAtrzTasks(null)).willReturn(List.of(vo));

        List<AtrzTaskVO> result = atrzTaskService.selectAtrzTasks(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAtrzTaskId()).isEqualTo("ATZ_EMP_PLAN");
        then(atrzTaskMapper).should().selectAtrzTasks(null);
    }

    @Test
    @DisplayName("sysSeCd 필터 조회 — 유효한 EMP 코드로 Mapper 호출")
    void selectAtrzTasks_유효한sysSeCd_필터조회() {
        AtrzTaskVO vo = validAtrzTaskVO();
        given(atrzTaskMapper.selectAtrzTasks("EMP")).willReturn(List.of(vo));

        List<AtrzTaskVO> result = atrzTaskService.selectAtrzTasks("EMP");

        assertThat(result).hasSize(1);
        then(atrzTaskMapper).should().selectAtrzTasks("EMP");
    }

    @Test
    @DisplayName("sysSeCd 필터 조회 — 잘못된 코드(INVALID) 시 BAD_REQUEST 예외")
    void selectAtrzTasks_잘못된sysSeCd_BAD_REQUEST() {
        assertThatThrownBy(() -> atrzTaskService.selectAtrzTasks("INVALID"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(atrzTaskMapper).should(never()).selectAtrzTasks(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectByTaskId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 조회 성공")
    void selectByTaskId_존재하는ID_성공() {
        AtrzTaskVO vo = validAtrzTaskVO();
        given(atrzTaskMapper.selectByTaskId("ATZ_EMP_PLAN")).willReturn(vo);

        AtrzTaskVO result = atrzTaskService.selectByTaskId("ATZ_EMP_PLAN");

        assertThat(result.getAtrzTaskId()).isEqualTo("ATZ_EMP_PLAN");
        assertThat(result.getSysSeCd()).isEqualTo("EMP");
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID 시 NOT_FOUND 예외")
    void selectByTaskId_미존재ID_NOT_FOUND() {
        given(atrzTaskMapper.selectByTaskId("ATZ_NOT_EXIST")).willReturn(null);

        assertThatThrownBy(() -> atrzTaskService.selectByTaskId("ATZ_NOT_EXIST"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createAtrzTask
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 업무 등록 — MOLIT 사용자 정상 등록")
    void createAtrzTask_MOLIT_정상등록() {
        AtrzTaskVO vo = validAtrzTaskVO();
        given(atrzTaskMapper.existsAtrzTaskId("ATZ_EMP_PLAN")).willReturn(false);
        given(atrzTaskMapper.insertAtrzTask(any(AtrzTaskVO.class))).willReturn(1);

        AtrzTaskVO result = atrzTaskService.createAtrzTask(vo, molitUser);

        assertThat(result.getAtrzTaskId()).isEqualTo("ATZ_EMP_PLAN");
        assertThat(result.getFrstRegUserId()).isEqualTo("molit01");
        assertThat(result.getLastChgUserId()).isEqualTo("molit01");
        then(atrzTaskMapper).should().insertAtrzTask(vo);
    }

    @Test
    @DisplayName("결재 업무 등록 — KOTSA 사용자도 정상 등록 가능")
    void createAtrzTask_KOTSA_정상등록() {
        AtrzTaskVO vo = validAtrzTaskVO();
        given(atrzTaskMapper.existsAtrzTaskId("ATZ_EMP_PLAN")).willReturn(false);
        given(atrzTaskMapper.insertAtrzTask(any(AtrzTaskVO.class))).willReturn(1);

        AtrzTaskVO result = atrzTaskService.createAtrzTask(vo, kotsaUser);

        assertThat(result.getFrstRegUserId()).isEqualTo("kotsa01");
    }

    @Test
    @DisplayName("결재 업무 등록 — AIRLINE 사용자 등록 시도 시 FORBIDDEN 예외")
    void createAtrzTask_AIRLINE_등록시도_FORBIDDEN() {
        AtrzTaskVO vo = validAtrzTaskVO();

        assertThatThrownBy(() -> atrzTaskService.createAtrzTask(vo, airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(atrzTaskMapper).should(never()).insertAtrzTask(any());
    }

    @Test
    @DisplayName("결재 업무 등록 — 잘못된 sys_se_cd 시 BAD_REQUEST 예외")
    void createAtrzTask_잘못된sysSeCd_BAD_REQUEST() {
        AtrzTaskVO vo = validAtrzTaskVO();
        vo.setSysSeCd("WRONG");

        assertThatThrownBy(() -> atrzTaskService.createAtrzTask(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(400));

        then(atrzTaskMapper).should(never()).insertAtrzTask(any());
    }

    @Test
    @DisplayName("결재 업무 등록 — atrz_task_id 중복 시 CONFLICT 예외")
    void createAtrzTask_ID중복_CONFLICT() {
        AtrzTaskVO vo = validAtrzTaskVO();
        given(atrzTaskMapper.existsAtrzTaskId("ATZ_EMP_PLAN")).willReturn(true);

        assertThatThrownBy(() -> atrzTaskService.createAtrzTask(vo, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(409));

        then(atrzTaskMapper).should(never()).insertAtrzTask(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateAtrzTask
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 업무 수정 — MOLIT 사용자 정상 수정")
    void updateAtrzTask_MOLIT_정상수정() {
        AtrzTaskVO existing = validAtrzTaskVO();
        given(atrzTaskMapper.selectByTaskId("ATZ_EMP_PLAN")).willReturn(existing);
        given(atrzTaskMapper.updateAtrzTask(any(AtrzTaskVO.class))).willReturn(1);

        AtrzTaskVO updateVO = new AtrzTaskVO();
        updateVO.setAtrzTaskNm("고용계획 결재 수정");
        updateVO.setSysSeCd("EMP");

        assertThatCode(() -> atrzTaskService.updateAtrzTask("ATZ_EMP_PLAN", updateVO, molitUser))
                .doesNotThrowAnyException();

        assertThat(updateVO.getAtrzTaskId()).isEqualTo("ATZ_EMP_PLAN");
        assertThat(updateVO.getLastChgUserId()).isEqualTo("molit01");
        then(atrzTaskMapper).should().updateAtrzTask(updateVO);
    }

    @Test
    @DisplayName("결재 업무 수정 — 미존재 ID 수정 시도 NOT_FOUND 예외")
    void updateAtrzTask_미존재ID_NOT_FOUND() {
        given(atrzTaskMapper.selectByTaskId("ATZ_NOT_EXIST")).willReturn(null);

        AtrzTaskVO updateVO = new AtrzTaskVO();
        updateVO.setSysSeCd("EMP");

        assertThatThrownBy(() -> atrzTaskService.updateAtrzTask("ATZ_NOT_EXIST", updateVO, molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));

        then(atrzTaskMapper).should(never()).updateAtrzTask(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // softDeleteAtrzTask
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 업무 소프트삭제 — MOLIT 사용자 정상 삭제")
    void softDeleteAtrzTask_MOLIT_정상삭제() {
        given(atrzTaskMapper.softDeleteAtrzTask("ATZ_EMP_PLAN", "molit01")).willReturn(1);

        assertThatCode(() -> atrzTaskService.softDeleteAtrzTask("ATZ_EMP_PLAN", molitUser))
                .doesNotThrowAnyException();

        then(atrzTaskMapper).should().softDeleteAtrzTask("ATZ_EMP_PLAN", "molit01");
    }

    @Test
    @DisplayName("결재 업무 소프트삭제 — AIRLINE 사용자 시도 시 FORBIDDEN 예외")
    void softDeleteAtrzTask_AIRLINE_시도_FORBIDDEN() {
        assertThatThrownBy(() -> atrzTaskService.softDeleteAtrzTask("ATZ_EMP_PLAN", airlineUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(403));

        then(atrzTaskMapper).should(never()).softDeleteAtrzTask(any(), any());
    }

    @Test
    @DisplayName("결재 업무 소프트삭제 — 미존재 ID 삭제 시도 NOT_FOUND 예외")
    void softDeleteAtrzTask_미존재ID_NOT_FOUND() {
        given(atrzTaskMapper.softDeleteAtrzTask("ATZ_NOT_EXIST", "molit01")).willReturn(0);

        assertThatThrownBy(() -> atrzTaskService.softDeleteAtrzTask("ATZ_NOT_EXIST", molitUser))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(404));
    }
}
