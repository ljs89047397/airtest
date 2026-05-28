package kr.go.molit.icas.com.atrz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AtrzTaskController 슬라이스 테스트.
 *
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 * SecurityMockMvcRequestPostProcessors.authentication() 으로 IcasUser 컨텍스트 주입.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtrzTaskController 슬라이스 테스트")
class AtrzTaskControllerTest {

    MockMvc mockMvc;

    @Mock
    AtrzTaskService atrzTaskService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser kotsaUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        AtrzTaskController controller = new AtrzTaskController(atrzTaskService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        molitUser = IcasUser.builder()
                .userId("molit01").userNm("국토부 담당자")
                .ognzSeCd("MOLIT").ognzId("ORG_MOLIT").master(false)
                .roleIds(List.of("ADMIN")).build();

        kotsaUser = IcasUser.builder()
                .userId("kotsa01").userNm("교통안전공단 담당자")
                .ognzSeCd("KOTSA").ognzId("ORG_KOTSA").master(false)
                .roleIds(List.of("KOTSA_ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private AtrzTaskVO sampleVO() {
        AtrzTaskVO vo = new AtrzTaskVO();
        vo.setAtrzTaskId("ATZ_EMP_PLAN");
        vo.setAtrzTaskNm("고용계획 결재");
        vo.setAtrzTaskDesc("고용계획서 결재 업무");
        vo.setSysSeCd("EMP");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/atrz-task
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — sysSeCd 없이 전체 목록 200 반환")
    void listTasks_전체목록_200() throws Exception {
        given(atrzTaskService.selectAtrzTasks(null)).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/atrz-task")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].atrzTaskId").value("ATZ_EMP_PLAN"));

        then(atrzTaskService).should().selectAtrzTasks(null);
    }

    @Test
    @DisplayName("목록 조회 — sysSeCd=EMP 필터로 Mapper 호출")
    void listTasks_sysSeCd필터_200() throws Exception {
        given(atrzTaskService.selectAtrzTasks("EMP")).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/atrz-task")
                        .param("sysSeCd", "EMP")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        then(atrzTaskService).should().selectAtrzTasks("EMP");
    }

    @Test
    @DisplayName("목록 조회 — 잘못된 sysSeCd Service BAD_REQUEST → 400 반환")
    void listTasks_잘못된sysSeCd_400() throws Exception {
        given(atrzTaskService.selectAtrzTasks("INVALID"))
                .willThrow(BusinessException.badRequest("sys_se_cd 는 COM / EMP / ER / VR / SAF / PTL 중 하나여야 합니다."));

        mockMvc.perform(get("/api/com/atrz-task")
                        .param("sysSeCd", "INVALID")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/atrz-task/{taskId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 taskId 조회 200 반환")
    void getTask_존재하는ID_200() throws Exception {
        given(atrzTaskService.selectByTaskId("ATZ_EMP_PLAN")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/atrz-task/ATZ_EMP_PLAN")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.atrzTaskId").value("ATZ_EMP_PLAN"))
                .andExpect(jsonPath("$.data.sysSeCd").value("EMP"));
    }

    @Test
    @DisplayName("단건 조회 — 미존재 ID 시 Service NOT_FOUND → 404 반환")
    void getTask_미존재ID_404() throws Exception {
        given(atrzTaskService.selectByTaskId("ATZ_NOT_EXIST"))
                .willThrow(BusinessException.notFound("결재 업무"));

        mockMvc.perform(get("/api/com/atrz-task/ATZ_NOT_EXIST")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/atrz-task
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 업무 등록 — MOLIT 사용자 정상 등록 200 반환")
    void createTask_MOLIT_정상등록_200() throws Exception {
        AtrzTaskVO reqVO = sampleVO();
        given(atrzTaskService.createAtrzTask(any(AtrzTaskVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/atrz-task")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결재 업무가 등록되었습니다."))
                .andExpect(jsonPath("$.data.atrzTaskId").value("ATZ_EMP_PLAN"));
    }

    @Test
    @DisplayName("결재 업무 등록 — AIRLINE 사용자 시도 시 Service FORBIDDEN → 403 반환")
    void createTask_AIRLINE_FORBIDDEN_403() throws Exception {
        AtrzTaskVO reqVO = sampleVO();
        given(atrzTaskService.createAtrzTask(any(AtrzTaskVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("국토부 또는 한국교통안전공단 사용자만 결재 업무를 관리할 수 있습니다."));

        mockMvc.perform(post("/api/com/atrz-task")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("결재 업무 등록 — atrz_task_id 중복 시 Service CONFLICT → 409 반환")
    void createTask_ID중복_CONFLICT_409() throws Exception {
        AtrzTaskVO reqVO = sampleVO();
        given(atrzTaskService.createAtrzTask(any(AtrzTaskVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 사용 중인 결재 업무 ID 입니다: ATZ_EMP_PLAN"));

        mockMvc.perform(post("/api/com/atrz-task")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/com/atrz-task/{taskId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 업무 수정 — MOLIT 사용자 정상 수정 200 반환")
    void updateTask_MOLIT_정상수정_200() throws Exception {
        AtrzTaskVO updateVO = new AtrzTaskVO();
        updateVO.setAtrzTaskNm("고용계획 결재 수정");
        updateVO.setSysSeCd("EMP");
        willDoNothing().given(atrzTaskService).updateAtrzTask(eq("ATZ_EMP_PLAN"), any(AtrzTaskVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/atrz-task/ATZ_EMP_PLAN")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결재 업무가 수정되었습니다."));
    }

    @Test
    @DisplayName("결재 업무 수정 — 미존재 ID 수정 시 Service NOT_FOUND → 404 반환")
    void updateTask_미존재ID_NOT_FOUND_404() throws Exception {
        AtrzTaskVO updateVO = new AtrzTaskVO();
        updateVO.setSysSeCd("EMP");
        willThrow(BusinessException.notFound("결재 업무"))
                .given(atrzTaskService).updateAtrzTask(eq("ATZ_NOT_EXIST"), any(AtrzTaskVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/atrz-task/ATZ_NOT_EXIST")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/atrz-task/{taskId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("결재 업무 소프트삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void deleteTask_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(atrzTaskService).softDeleteAtrzTask(eq("ATZ_EMP_PLAN"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/atrz-task/ATZ_EMP_PLAN")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결재 업무가 삭제되었습니다."));
    }

    @Test
    @DisplayName("결재 업무 소프트삭제 — 미존재 ID 삭제 시 Service NOT_FOUND → 404 반환")
    void deleteTask_미존재ID_NOT_FOUND_404() throws Exception {
        willThrow(BusinessException.notFound("결재 업무"))
                .given(atrzTaskService).softDeleteAtrzTask(eq("ATZ_NOT_EXIST"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/atrz-task/ATZ_NOT_EXIST")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
