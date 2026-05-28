package kr.go.molit.icas.er.rprt.aerdrm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.exception.GlobalExceptionHandler;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.rprt.aerdrm.domain.ErAerdrmPairCo2VO;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ErAerdrmPairCo2Controller 슬라이스 테스트")
class ErAerdrmPairCo2ControllerTest {

    MockMvc mockMvc;

    @Mock
    ErAerdrmPairCo2Service erAerdrmPairCo2Service;

    ObjectMapper objectMapper;

    IcasUser airlineUserOP0001;
    IcasUser airlineUserOP0002;

    @BeforeEach
    void setUp() {
        ErAerdrmPairCo2Controller controller = new ErAerdrmPairCo2Controller(erAerdrmPairCo2Service);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        airlineUserOP0001 = IcasUser.builder()
                .userId("airline01").userNm("항공사A 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR01").oprtrId("OP0001").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();

        airlineUserOP0002 = IcasUser.builder()
                .userId("airline02").userNm("항공사B 담당자")
                .ognzSeCd("AIRLINE").ognzId("ORG_AIR02").oprtrId("OP0002").master(false)
                .roleIds(List.of("AIRLINE_USER")).build();
    }

    private UsernamePasswordAuthenticationToken authToken(IcasUser user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private ErAerdrmPairCo2VO samplePair(String erId, int sn) {
        ErAerdrmPairCo2VO vo = new ErAerdrmPairCo2VO();
        vo.setErId(erId);
        vo.setPairSn(sn);
        vo.setDprtrAerdrmCd("RKSI");
        vo.setArvlAerdrmCd("RJTT");
        vo.setDprtrCntryCd("KR");
        vo.setArvlCntryCd("JP");
        vo.setFuelTypeCd("JET-A");
        vo.setCo2Emsn(new BigDecimal("158000.0000"));
        return vo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET — 목록 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회 — 정상 200 응답, data 배열 포함")
    void list_정상_200() throws Exception {
        given(erAerdrmPairCo2Service.list(eq("ER0001"), any(IcasUser.class)))
                .willReturn(List.of(samplePair("ER0001", 1), samplePair("ER0001", 2)));

        mockMvc.perform(get("/api/er/rprt/ER0001/aerdrm-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].dprtrAerdrmCd").value("RKSI"))
                .andExpect(jsonPath("$.data[0].pairSn").value(1));
    }

    @Test
    @DisplayName("목록 조회 — Service NOT_FOUND 시 404 응답")
    void list_ER미존재_404() throws Exception {
        given(erAerdrmPairCo2Service.list(eq("ER9999"), any(IcasUser.class)))
                .willThrow(BusinessException.notFound("ER"));

        mockMvc.perform(get("/api/er/rprt/ER9999/aerdrm-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회 — Service FORBIDDEN 시 403 응답")
    void list_가시범위위반_403() throws Exception {
        given(erAerdrmPairCo2Service.list(eq("ER0001"), any(IcasUser.class)))
                .willThrow(BusinessException.forbidden("본인 항공사 데이터만 접근할 수 있습니다."));

        mockMvc.perform(get("/api/er/rprt/ER0001/aerdrm-pair")
                        .with(authentication(authToken(airlineUserOP0002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST — 추가
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("추가 — 정상 200 응답, data.pairSn / message 검증")
    void add_정상_200() throws Exception {
        ErAerdrmPairCo2VO req = samplePair("ER0001", 0);
        given(erAerdrmPairCo2Service.add(eq("ER0001"), any(ErAerdrmPairCo2VO.class), any(IcasUser.class)))
                .willReturn(samplePair("ER0001", 1));

        mockMvc.perform(post("/api/er/rprt/ER0001/aerdrm-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비행장 쌍 배출량이 추가되었습니다."))
                .andExpect(jsonPath("$.data.pairSn").value(1));
    }

    @Test
    @DisplayName("추가 — Service BAD_REQUEST 시 400 응답 (코드 길이 오류)")
    void add_검증실패_400() throws Exception {
        ErAerdrmPairCo2VO req = samplePair("ER0001", 0);
        given(erAerdrmPairCo2Service.add(eq("ER0001"), any(ErAerdrmPairCo2VO.class), any(IcasUser.class)))
                .willThrow(BusinessException.badRequest("dprtrAerdrmCd 는 4자리 ICAO 비행장 코드여야 합니다."));

        mockMvc.perform(post("/api/er/rprt/ER0001/aerdrm-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("추가 — Service CONFLICT 시 409 응답 (중복 쌍)")
    void add_중복_409() throws Exception {
        ErAerdrmPairCo2VO req = samplePair("ER0001", 0);
        given(erAerdrmPairCo2Service.add(eq("ER0001"), any(ErAerdrmPairCo2VO.class), any(IcasUser.class)))
                .willThrow(BusinessException.conflict("동일한 출발비행장·도착비행장·연료유형 조합이 이미 등록되어 있습니다."));

        mockMvc.perform(post("/api/er/rprt/ER0001/aerdrm-pair")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT — 수정
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("수정 — 정상 200 응답, message 검증")
    void update_정상_200() throws Exception {
        ErAerdrmPairCo2VO req = samplePair("ER0001", 1);
        willDoNothing().given(erAerdrmPairCo2Service)
                .update(eq("ER0001"), eq(1), any(ErAerdrmPairCo2VO.class), any(IcasUser.class));

        mockMvc.perform(put("/api/er/rprt/ER0001/aerdrm-pair/1")
                        .with(authentication(authToken(airlineUserOP0001)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비행장 쌍 배출량이 수정되었습니다."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — 소프트삭제
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("소프트삭제 — 정상 200 응답, message 검증")
    void delete_정상_200() throws Exception {
        willDoNothing().given(erAerdrmPairCo2Service)
                .softDelete(eq("ER0001"), eq(1), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/aerdrm-pair/1")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비행장 쌍 배출량이 삭제되었습니다."));
    }

    @Test
    @DisplayName("소프트삭제 — Service NOT_FOUND 시 404 응답")
    void delete_자식미존재_404() throws Exception {
        willThrow(BusinessException.notFound("비행장 쌍 배출량"))
                .given(erAerdrmPairCo2Service).softDelete(eq("ER0001"), eq(99), any(IcasUser.class));

        mockMvc.perform(delete("/api/er/rprt/ER0001/aerdrm-pair/99")
                        .with(authentication(authToken(airlineUserOP0001))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
