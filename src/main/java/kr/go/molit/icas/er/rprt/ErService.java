package kr.go.molit.icas.er.rprt;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import kr.go.molit.icas.er.rprt.domain.ErSearch;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ER(Emission Report) 비즈니스 서비스.
 *
 * <h2>상태기</h2>
 * <pre>
 * DRAFT → submit → SBMTD → review → RVWNG
 *                                      ├ reject → DRAFT (사유 필수)
 *                                      └ recommend → RCMDD → approve → APRVD → cancel → CNCLD
 *                            (또는 RVWNG → approve → APRVD)
 * </pre>
 *
 * <h2>권한·가시범위</h2>
 * <ul>
 *   <li>MOLIT / KOTSA — 전체 조회</li>
 *   <li>AIRLINE — 본인 oprtr_id 만</li>
 *   <li>VERIFIER — vrfcn_assgn 배정 운영사만 (rprt_yr 일치)</li>
 * </ul>
 *
 * <h2>핵심 설계 결정</h2>
 * <ul>
 *   <li>EMP Plan 유효성 검증 및 prefill 은 {@link #applyEmpPlanContext} private helper 로 분리</li>
 *   <li>상태 전이 헬퍼 {@link #loadAndAssert} / {@link #loadAndAssertStatus} 로 중복 제거</li>
 *   <li>er_ver 채번: 같은 (oprtr_id, rprt_yr) 의 max+1 (EMP 패턴 동일)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErService {

    private static final String ER_ID_PREFIX = "ER";

    private final ErMapper            erMapper;
    private final EmpPlanMapper       empPlanMapper;
    private final OprtrMapper         oprtrMapper;
    private final DataScopeValidator  dataScopeValidator;
    private final IdGenerator         idGenerator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * ER 목록 검색 (페이징).
     *
     * <p>역할별 가시범위 강제 적용:
     * <ul>
     *   <li>MOLIT / KOTSA — 전체</li>
     *   <li>AIRLINE — 본인 oprtrId 강제 주입</li>
     *   <li>VERIFIER — verifierScope=true + vrfcnInstId 주입</li>
     *   <li>그 외 — forbidden</li>
     * </ul>
     *
     * @param search 검색 조건 DTO
     * @param user   로그인 사용자
     * @return 페이징 결과
     */
    public PageResponse<ErVO> searchErs(ErSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전체 가시
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else if (user.isVerifier()) {
            search.setVerifierScope(true);
            search.setVrfcnInstId(user.getVrfcnInstId());
        } else {
            throw BusinessException.forbidden("ER 조회 권한이 없습니다.");
        }

        long total = erMapper.countErs(search);
        List<ErVO> rows = erMapper.selectErs(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    /**
     * ER 단건 조회.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @return ER VO
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public ErVO getEr(String erId, IcasUser user) {
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        dataScopeValidator.assertOprtrAccessible(user, er.getOprtrId(), er.getRprtYr());
        return er;
    }

    // ══════════════════════════════════════════════════════
    // 신규 등록
    // ══════════════════════════════════════════════════════

    /**
     * ER 신규 DRAFT 생성.
     *
     * <p>권한: AIRLINE 본인 oprtrId 한정.
     *
     * @param vo   등록 데이터 (oprtrId, rprtYr 필수)
     * @param user 로그인 사용자
     * @return 생성된 ER VO
     * @throws BusinessException FORBIDDEN   — AIRLINE 외 또는 타 운영사
     * @throws BusinessException NOT_FOUND   — 운영사 미존재
     * @throws BusinessException BAD_REQUEST — 필수값 누락 또는 EMP Plan 검증 실패
     */
    @Transactional
    public ErVO createEr(ErVO vo, IcasUser user) {
        // 1. 권한: AIRLINE 본인만
        dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());

        // 2. 운영사 FK 유효성 (유효구간 포함)
        validateOprtrExists(vo.getOprtrId());

        // 3. 필수값 검증
        validateRequired(vo);

        // 4. EMP Plan 검증 및 prefill (입력값 있을 때만)
        if (!isBlank(vo.getEmpPlanIdApld())) {
            applyEmpPlanContext(vo);
        }

        // 5. er_id 채번: ER + 4자리
        int seq = erMapper.countByPrefix(ER_ID_PREFIX) + 1;
        String erId = idGenerator.managementPk(ER_ID_PREFIX, seq);

        // 6. er_ver 채번: 같은 (oprtr_id, rprt_yr) 의 max+1
        String erVer = nextErVer(vo.getOprtrId(), vo.getRprtYr());

        vo.setErId(erId);
        vo.setErVer(erVer);
        vo.setErStCd("DRAFT");
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        erMapper.insertEr(vo);
        return erMapper.selectByErId(erId);
    }

    // ══════════════════════════════════════════════════════
    // 수정 (DRAFT 한정)
    // ══════════════════════════════════════════════════════

    /**
     * ER DRAFT 마스터 수정.
     *
     * <p>권한: AIRLINE 본인 oprtrId, 상태 DRAFT 한정.
     *
     * @param erId ER ID
     * @param vo   수정 데이터
     * @param user 로그인 사용자
     * @throws BusinessException NOT_FOUND  — ER 미존재
     * @throws BusinessException FORBIDDEN  — 권한 없음
     * @throws BusinessException CONFLICT   — DRAFT 상태 아님
     */
    @Transactional
    public void updateEr(String erId, ErVO vo, IcasUser user) {
        // 1. 조회 + 가시범위 검증
        ErVO existing = erMapper.selectByErId(erId);
        if (existing == null) throw BusinessException.notFound("ER");
        dataScopeValidator.assertOwnAirline(user, existing.getOprtrId());

        // 2. 상태 검증
        assertStatus(existing, "DRAFT", "DRAFT 상태에서만 수정할 수 있습니다.");

        // 3. EMP Plan 검증 및 prefill (입력값 있을 때만)
        vo.setOprtrId(existing.getOprtrId());
        if (!isBlank(vo.getEmpPlanIdApld())) {
            applyEmpPlanContext(vo);
        }

        // 4. 수정
        vo.setErId(erId);
        vo.setLastChgUserId(user.getUserId());
        int affected = erMapper.updateEr(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 ER 이 DRAFT 상태가 아니거나 존재하지 않습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제 (DRAFT 한정)
    // ══════════════════════════════════════════════════════

    /**
     * ER 소프트삭제 (DRAFT 한정).
     *
     * <p>권한: AIRLINE 본인 oprtrId.
     *
     * @param erId ER ID
     * @param user 로그인 사용자
     * @throws BusinessException NOT_FOUND — ER 미존재
     * @throws BusinessException CONFLICT  — DRAFT 상태 아님
     */
    @Transactional
    public void softDeleteEr(String erId, IcasUser user) {
        ErVO existing = erMapper.selectByErId(erId);
        if (existing == null) throw BusinessException.notFound("ER");
        dataScopeValidator.assertOwnAirline(user, existing.getOprtrId());
        assertStatus(existing, "DRAFT", "DRAFT 상태에서만 삭제할 수 있습니다.");

        int affected = erMapper.softDeleteEr(erId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("삭제 대상 ER 이 DRAFT 상태가 아니거나 존재하지 않습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 상태 전이
    // ══════════════════════════════════════════════════════

    /**
     * 제출 (DRAFT → SBMTD).
     *
     * <p>권한: AIRLINE 본인.
     */
    @Transactional
    public void submit(String erId, IcasUser user) {
        loadAndAssert(erId, user, true, "DRAFT");
        int affected = erMapper.updateSubmit(erId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("제출 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    /**
     * 검토 진입 (SBMTD → RVWNG).
     *
     * <p>권한: KOTSA.
     */
    @Transactional
    public void review(String erId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertStatus(erId, "SBMTD");
        int affected = erMapper.updateErStCd(erId, "RVWNG", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("검토 진입 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    /**
     * 반려 (RVWNG → DRAFT).
     *
     * <p>권한: KOTSA. 사유 필수.
     */
    @Transactional
    public void reject(String erId, String reason, IcasUser user) {
        assertKotsa(user);
        if (isBlank(reason)) throw BusinessException.badRequest("반려 사유는 필수입니다.");
        loadAndAssertStatus(erId, "RVWNG");
        int affected = erMapper.updateReject(erId, reason, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("반려 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    /**
     * 권고 (RVWNG → RCMDD).
     *
     * <p>권한: KOTSA.
     */
    @Transactional
    public void recommend(String erId, IcasUser user) {
        assertKotsa(user);
        loadAndAssertStatus(erId, "RVWNG");
        int affected = erMapper.updateErStCd(erId, "RCMDD", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("권고 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    /**
     * 승인 (RVWNG 또는 RCMDD → APRVD).
     *
     * <p>권한: MOLIT.
     */
    @Transactional
    public void approve(String erId, IcasUser user) {
        assertMolit(user);
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        if (!"RVWNG".equals(er.getErStCd()) && !"RCMDD".equals(er.getErStCd())) {
            throw BusinessException.badRequest(
                    "승인은 RVWNG 또는 RCMDD 상태에서만 가능합니다. 현재 상태: " + er.getErStCd());
        }
        int affected = erMapper.updateApprove(erId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("승인 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    /**
     * 취소 (APRVD → CNCLD).
     *
     * <p>권한: MOLIT. 사유 필수.
     */
    @Transactional
    public void cancel(String erId, String reason, IcasUser user) {
        assertMolit(user);
        if (isBlank(reason)) throw BusinessException.badRequest("취소 사유는 필수입니다.");
        loadAndAssertStatus(erId, "APRVD");
        int affected = erMapper.updateCancel(erId, reason, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("취소 처리에 실패했습니다. 현재 상태를 확인하세요.");
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════

    /**
     * EMP Plan 검증 및 자동 prefill.
     *
     * <p>신규/수정 시 emp_plan_id_apld 입력값이 있으면:
     * <ol>
     *   <li>EMP Plan 존재 여부 확인</li>
     *   <li>운영사 일치 여부 확인</li>
     *   <li>EMP Plan 승인 상태(APRVD) 확인</li>
     *   <li>emp_ver_apld, emp_aprv_dt, emp_eff_dt, emp_updt_dt 자동 prefill</li>
     * </ol>
     *
     * @param vo 검증 및 prefill 대상 ErVO (oprtrId, empPlanIdApld 필수)
     * @throws BusinessException BAD_REQUEST — EMP Plan 미존재, 운영사 불일치, 승인 상태 아님
     */
    private void applyEmpPlanContext(ErVO vo) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(vo.getEmpPlanIdApld());
        if (plan == null) {
            throw BusinessException.badRequest("적용 EMP Plan 이 존재하지 않습니다.");
        }
        if (!plan.getOprtrId().equals(vo.getOprtrId())) {
            throw BusinessException.badRequest("적용 EMP Plan 의 운영사가 ER 의 운영사와 일치하지 않습니다.");
        }
        if (!"APRVD".equals(plan.getEmpStCd())) {
            throw BusinessException.badRequest("승인된 EMP Plan 만 ER 작성에 사용할 수 있습니다.");
        }

        // 자동 prefill
        vo.setEmpVerApld(plan.getEmpVer());
        vo.setEmpAprvDt(plan.getAprvDt() != null ? plan.getAprvDt().toLocalDate() : null);
        vo.setEmpEffDt(plan.getUseBgngDt() != null ? plan.getUseBgngDt().toLocalDate() : null);
        vo.setEmpUpdtDt(plan.getLastChgDt() != null ? plan.getLastChgDt().toLocalDate() : null);
    }

    /**
     * 운영사 FK 유효성 검증 (유효구간 포함).
     */
    private void validateOprtrExists(String oprtrId) {
        OprtrVO oprtr = oprtrMapper.selectByOprtrId(oprtrId);
        if (oprtr == null) {
            throw BusinessException.notFound("운영사(oprtrId=" + oprtrId + ")");
        }
    }

    /**
     * 필수값 검증.
     */
    private void validateRequired(ErVO vo) {
        if (isBlank(vo.getOprtrId())) {
            throw BusinessException.badRequest("운영사 ID(oprtrId)는 필수입니다.");
        }
        if (isBlank(vo.getRprtYr())) {
            throw BusinessException.badRequest("보고연도(rprtYr)는 필수입니다.");
        }
        if (!vo.getRprtYr().matches("\\d{4}")) {
            throw BusinessException.badRequest("보고연도(rprtYr)는 4자리 숫자 형식이어야 합니다.");
        }
        if (vo.getCertUseYn() != null && !"Y".equals(vo.getCertUseYn()) && !"N".equals(vo.getCertUseYn())) {
            throw BusinessException.badRequest("인증 사용 여부(certUseYn)는 Y 또는 N 이어야 합니다.");
        }
        if (vo.getAllcUseYn() != null && !"Y".equals(vo.getAllcUseYn()) && !"N".equals(vo.getAllcUseYn())) {
            throw BusinessException.badRequest("상쇄 배분 사용 여부(allcUseYn)는 Y 또는 N 이어야 합니다.");
        }
    }

    /**
     * 단건 조회 + 상태 검증 (상태 불일치 시 BAD_REQUEST).
     */
    private ErVO loadAndAssertStatus(String erId, String expectedSt) {
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        assertStatus(er, expectedSt,
                expectedSt + " 상태에서만 가능한 작업입니다. 현재 상태: " + er.getErStCd());
        return er;
    }

    /**
     * AIRLINE 권한 + 상태 검증 통합 (submit 등 AIRLINE 전용 작업용).
     *
     * @param ownAirline true 이면 AIRLINE 본인 검증도 수행
     */
    private ErVO loadAndAssert(String erId, IcasUser user,
                               boolean ownAirline, String expectedSt) {
        ErVO er = erMapper.selectByErId(erId);
        if (er == null) throw BusinessException.notFound("ER");
        if (ownAirline) {
            dataScopeValidator.assertOwnAirline(user, er.getOprtrId());
        }
        assertStatus(er, expectedSt,
                expectedSt + " 상태에서만 가능한 작업입니다. 현재 상태: " + er.getErStCd());
        return er;
    }

    /**
     * 단일 상태 검증.
     */
    private void assertStatus(ErVO er, String expected, String message) {
        if (!expected.equals(er.getErStCd())) {
            throw BusinessException.badRequest(message);
        }
    }

    /** MOLIT 권한 검증 */
    private void assertMolit(IcasUser user) {
        if (user.isMaster()) return;
        if (!"MOLIT".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("국토부(MOLIT) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    /** KOTSA 권한 검증 */
    private void assertKotsa(IcasUser user) {
        if (user.isMaster()) return;
        if (!"KOTSA".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("한국교통안전공단(KOTSA) 사용자만 수행할 수 있는 작업입니다.");
        }
    }

    /**
     * er_ver 채번: 같은 (oprtr_id, rprt_yr) 의 max(er_ver) + 1.
     * 처음이면 "1.0", 다음은 "2.0", ...
     */
    private String nextErVer(String oprtrId, String rprtYr) {
        String maxVer = erMapper.selectMaxErVer(oprtrId, rprtYr);
        if (maxVer == null || maxVer.startsWith("null")) {
            return "1.0";
        }
        try {
            int major = Integer.parseInt(maxVer.split("\\.")[0]);
            return (major + 1) + ".0";
        } catch (NumberFormatException e) {
            return "1.0";
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
