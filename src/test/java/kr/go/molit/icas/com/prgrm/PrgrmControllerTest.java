package kr.go.molit.icas.com.prgrm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.com.prgrm.domain.PrgrmVO;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PrgrmController 슬라이스 테스트.
 * Spring Boot 없이 순수 MockMvcBuilders.standaloneSetup 사용.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PrgrmController 슬라이스 테스트")
class PrgrmControllerTest {

    MockMvc mockMvc;

    @Mock
    PrgrmService prgrmService;

    ObjectMapper objectMapper;

    // ── fixture ──
    IcasUser molitUser;
    IcasUser airlineUser;

    @BeforeEach
    void setUp() {
        PrgrmController controller = new PrgrmController(prgrmService);
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
                .prgrmPathsInq(Set.of("/api/com/prgrm"))
                .prgrmPathsInpt(Set.of("/api/com/prgrm"))
                .roleIds(List.of("ADMIN")).build();

        airlineUser = IcasUser.builder()
                .userId("airline01").userNm("항공사 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .prgrmPathsInq(Set.of())
                .prgrmPathsInpt(Set.of())
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private PrgrmVO sampleVO() {
        PrgrmVO vo = new PrgrmVO();
        vo.setPrgrmId("PG001");
        vo.setSysSeCd("COM");
        vo.setPrgrmNm("공통 프로그램");
        vo.setPrgrmUrl("/com/prgrm");
        vo.setApiPathPrefix("/api/com/prgrm");
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/prgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 전체 목록 200 반환")
    void list_전체목록_200() throws Exception {
        given(prgrmService.selectPrgrms(null)).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/prgrm")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        then(prgrmService).should().selectPrgrms(null);
    }

    @Test
    @DisplayName("목록 조회 — sysSeCd 필터 적용 200 반환")
    void list_sysSeCd_필터_200() throws Exception {
        given(prgrmService.selectPrgrms("COM")).willReturn(List.of(sampleVO()));

        mockMvc.perform(get("/api/com/prgrm")
                        .param("sysSeCd", "COM")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sysSeCd").value("COM"));

        then(prgrmService).should().selectPrgrms("COM");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/com/prgrm/{prgrmId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 — 존재하는 ID 200 반환")
    void get_단건조회_200() throws Exception {
        given(prgrmService.selectPrgrm("PG001")).willReturn(sampleVO());

        mockMvc.perform(get("/api/com/prgrm/PG001")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prgrmId").value("PG001"));
    }

    @Test
    @DisplayName("단건 조회 — 존재하지 않는 ID → 404 반환")
    void get_없는ID_404() throws Exception {
        given(prgrmService.selectPrgrm("PG999"))
                .willThrow(BusinessException.notFound("프로그램"));

        mockMvc.perform(get("/api/com/prgrm/PG999")
                        .with(authentication(authToken(molitUser)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/com/prgrm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램 등록 — MOLIT 사용자 정상 등록 200 반환")
    void create_MOLIT_정상등록_200() throws Exception {
        PrgrmVO reqVO = sampleVO();
        given(prgrmService.insertPrgrm(any(PrgrmVO.class), any(IcasUser.class))).willReturn(sampleVO());

        mockMvc.perform(post("/api/com/prgrm")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("등록되었습니다."))
                .andExpect(jsonPath("$.data.prgrmId").value("PG001"));
    }

    @Test
    @DisplayName("프로그램 등록 — Service에서 BAD_REQUEST(유효하지 않은 sysSeCd) → 400 반환")
    void create_잘못된_sysSeCd_400() throws Exception {
        PrgrmVO reqVO = sampleVO();
        reqVO.setSysSeCd("INVALID");
        given(prgrmService.insertPrgrm(any(PrgrmVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("유효하지 않은 시스템 구분 코드입니다."));

        mockMvc.perform(post("/api/com/prgrm")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("프로그램 등록 — Service에서 CONFLICT(중복 prgrmId) → 409 반환")
    void create_중복ID_409() throws Exception {
        PrgrmVO reqVO = sampleVO();
        given(prgrmService.insertPrgrm(any(PrgrmVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("이미 존재하는 프로그램 ID 입니다: PG001"));

        mockMvc.perform(post("/api/com/prgrm")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("프로그램 등록 — Service에서 FORBIDDEN(권한 없음) → 403 반환")
    void create_권한없음_403() throws Exception {
        PrgrmVO reqVO = sampleVO();
        given(prgrmService.insertPrgrm(any(PrgrmVO.class), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("MOLIT/KOTSA 사용자만 프로그램을 변경할 수 있습니다."));

        mockMvc.perform(post("/api/com/prgrm")
                        .with(authentication(authToken(airlineUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/com/prgrm/{prgrmId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램 수정 — MOLIT 사용자 정상 수정 200 반환")
    void update_MOLIT_정상수정_200() throws Exception {
        PrgrmVO reqVO = sampleVO();
        willDoNothing().given(prgrmService).updatePrgrm(eq("PG001"), any(PrgrmVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/prgrm/PG001")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("수정되었습니다."));
    }

    @Test
    @DisplayName("프로그램 수정 — 존재하지 않는 ID → 404 반환")
    void update_없는ID_404() throws Exception {
        PrgrmVO reqVO = sampleVO();
        willThrow(BusinessException.notFound("프로그램"))
                .given(prgrmService).updatePrgrm(eq("PG999"), any(PrgrmVO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/com/prgrm/PG999")
                        .with(authentication(authToken(molitUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/com/prgrm/{prgrmId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("프로그램 삭제 — MOLIT 사용자 정상 삭제 200 반환")
    void delete_MOLIT_정상삭제_200() throws Exception {
        willDoNothing().given(prgrmService).softDeletePrgrm(eq("PG001"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/prgrm/PG001")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }

    @Test
    @DisplayName("프로그램 삭제 — 존재하지 않는 ID → 404 반환")
    void delete_없는ID_404() throws Exception {
        willThrow(BusinessException.notFound("프로그램"))
                .given(prgrmService).softDeletePrgrm(eq("PG999"), any(IcasUser.class));

        mockMvc.perform(delete("/api/com/prgrm/PG999")
                        .with(authentication(authToken(molitUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
