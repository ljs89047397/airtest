package kr.go.molit.icas.emp.plan;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import kr.go.molit.icas.emp.plan.domain.EmpChgHstryVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanSearch;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EMP Plan 비즈니스 서비스.
 *
 * <h2>상태기</h2>
 * <pre>
 * DRAFT → submit → SBMTD → review → RVWNG
 *                                      ├ reject → DRAFT
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
 *   <li>상태 전이 중복 코드는 {@link #transitState} private helper 로 통합</li>
 *   <li>approve 시 직전 APRVD 자동 만료는 {@link #expirePreviousApproved} 로 분리</li>
 *   <li>변경이력 chg_cn 은 단순 JSON 문자열 직렬화 (Jackson 미사용, 리터럴 조립)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpPlanService {

    private static final String EMP_PLAN_ID_PREFIX = "EP";

    private final EmpPlanMapper     empPlanMapper;
    private final EmpChgHstryMapper empChgHstryMapper;
    private final OprtrMapper       oprtrMapper;
    private final DataScopeValidator dataScopeValidator;
    private final IdGenerator        idGenerator;

    private final kr.go.molit.icas.emp.plan.info.EmpOprtrInfoMapper      empOprtrInfoMapper;
    private final kr.go.molit.icas.emp.plan.co2.EmpCo2CalcMapper          empCo2CalcMapper;
    private final kr.go.molit.icas.emp.plan.ctrl.EmpDataCtrlMapper        empDataCtrlMapper;
    private final kr.go.molit.icas.emp.plan.cnct.EmpOprtrCnctMapper       empOprtrCnctMapper;
    private final kr.go.molit.icas.emp.plan.acft.EmpAcftMapper            empAcftMapper;
    private final kr.go.molit.icas.emp.plan.cntry.EmpCntryPairMapper      empCntryPairMapper;
    private final kr.go.molit.icas.emp.plan.co2detail.EmpCo2DetailMapper  empCo2DetailMapper;
    private final kr.go.molit.icas.emp.plan.risk.EmpRiskMapper            empRiskMapper;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * EMP Plan 목록 검색 (페이징).
     *
     * <p>역할별 가시범위 강제 적용:
     * <ul>
     *   <li>MOLIT / KOTSA — 전체</li>
     *   <li>AIRLINE — 본인 oprtrId 강제 주입</li>
     *   <li>VERIFIER — verifierScope=true + vrfcnInstId 주입 (rprtYr 은 search 파라미터에서)</li>
     *   <li>그 외 — forbidden</li>
     * </ul>
     *
     * @param search 검색 조건 DTO
     * @param user   로그인 사용자
     * @return 페이징 결과
     */
    public PageResponse<EmpPlanVO> searchEmpPlans(EmpPlanSearch search, IcasUser user) {
        if (user.isMaster() || user.isMolitOrKotsa()) {
            // 전체 가시
        } else if (user.isAirline()) {
            search.setOprtrId(user.getOprtrId());
        } else if (user.isVerifier()) {
            search.setVerifierScope(true);
            search.setVrfcnInstId(user.getVrfcnInstId());
        } else {
            throw BusinessException.forbidden("EMP Plan 조회 권한이 없습니다.");
        }

        long total = empPlanMapper.countEmpPlans(search);
        List<EmpPlanVO> rows = empPlanMapper.selectEmpPlans(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    /**
     * EMP Plan 단건 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return EMP Plan VO
     * @throws BusinessException NOT_FOUND — plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public EmpPlanVO getEmpPlan(String empPlanId, IcasUser user) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return plan;
    }

    /**
     * EMP Plan 변경 이력 목록 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return 변경 이력 목록 (최신순)
     */
    public List<EmpChgHstryVO> getEmpPlanHistory(String empPlanId, IcasUser user) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empChgHstryMapper.selectHstryByEmpPlanId(empPlanId);
    }

    // ══════════════════════════════════════════════════════
    // 신규 등록
    // ══════════════════════════════════════════════════════

    /**
     * EMP Plan 신규 DRAFT 생성.
     *
     * <p>권한: AIRLINE 본인 oprtrId 한정.
     *
     * @param vo   등록 데이터 (oprtrId, rprtYr, rmrk 포함)
     * @param user 로그인 사용자
     * @return 생성된 EMP Plan VO
     * @throws BusinessException FORBIDDEN  — AIRLINE 외 또는 타 운영사
     * @throws BusinessException NOT_FOUND  — 운영사 미존재
     * @throws BusinessException BAD_REQUEST — 필수값 누락
     */
    @Transactional
    public EmpPlanVO createEmpPlan(EmpPlanVO vo, IcasUser user) {
        // 1. 권한: AIRLINE 본인만
        dataScopeValidator.assertOwnAirline(user, vo.getOprtrId());

        // 2. 운영사 FK 유효성 (유효구간 포함)
        validateOprtrExists(vo.getOprtrId());

        // 3. 필수값 검증
        if (isBlank(vo.getRprtYr())) {
            throw BusinessException.badRequest("보고연도(rprtYr)는 필수입니다.");
        }

        // 4. emp_plan_id 채번: EP + 4자리
        int seq = empPlanMapper.countByPrefix(EMP_PLAN_ID_PREFIX) + 1;
        String empPlanId = idGenerator.managementPk(EMP_PLAN_ID_PREFIX, seq);

        // 5. emp_ver 채번: 같은 oprtr 의 max(emp_ver) + 1.0
        String empVer = nextEmpVer(vo.getOprtrId());

        vo.setEmpPlanId(empPlanId);
        vo.setEmpVer(empVer);
        vo.setEmpStCd("DRAFT");
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        empPlanMapper.insertEmpPlan(vo);

        // 6. 변경 이력 기록
        insertHstry(empPlanId, null, "MASTER", "{\"action\":\"신규등록\"}", vo.getSigChgYn(), user.getUserId());

        return empPlanMapper.selectByEmpPlanId(empPlanId);
    }

    // ══════════════════════════════════════════════════════
    // 수정 (DRAFT 한정)
    // ══════════════════════════════════════════════════════

    /**
     * EMP Plan DRAFT 마스터 수정.
     *
     * <p>권한: AIRLINE 본인 oprtrId, 상태 DRAFT 한정.
     *
     * @param empPlanId 수정 대상 EMP Plan ID
     * @param vo        수정 데이터 (rprtYr, sigChgYn, rmrk)
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND  — plan 미존재
     * @throws BusinessException FORBIDDEN  — 권한 없음
     * @throws BusinessException CONFLICT   — DRAFT 상태 아님
     */
    @Transactional
    public void updateEmpPlan(String empPlanId, EmpPlanVO vo, IcasUser user) {
        // 1. 조회 + 가시범위 검증
        EmpPlanVO existing = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (existing == null) throw BusinessException.notFound("EMP Plan");
        dataScopeValidator.assertOwnAirline(user, existing.getOprtrId());

        // 2. 상태 검증
        assertStatus(existing, "DRAFT", "DRAFT 상태에서만 수정할 수 있습니다.");

        // 3. 수정
        vo.setEmpPlanId(empPlanId);
        vo.setLastChgUserId(user.getUserId());
        int affected = empPlanMapper.updateEmpPlan(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 EMP Plan 이 DRAFT 상태가 아니거나 존재하지 않습니다.");

        // 4. 변경 이력
        String reason = (vo.getRmrk() != null && !vo.getRmrk().isBlank())
                ? escapeJson(vo.getRmrk()) : "마스터 수정";
        insertHstry(empPlanId, existing.getPrevEmpPlanId(), "MASTER",
                "{\"reason\":\"" + reason + "\"}", vo.getSigChgYn(), user.getUserId());
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제 (DRAFT 한정)
    // ══════════════════════════════════════════════════════

    /**
     * EMP Plan 소프트삭제 (DRAFT 한정).
     *
     * <p>권한: AIRLINE 본인 oprtrId.
     *
     * @param empPlanId 삭제 대상 EMP Plan ID
     * @param user      로그인 사용자
     * @throws BusinessException NOT_FOUND — 미존재
     * @throws BusinessException CONFLICT  — DRAFT 상태 아님
     */
    @Transactional
    public void softDeleteEmpPlan(String empPlanId, IcasUser user) {
        EmpPlanVO existing = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (existing == null) throw BusinessException.notFound("EMP Plan");
        dataScopeValidator.assertOwnAirline(user, existing.getOprtrId());
        assertStatus(existing, "DRAFT", "DRAFT 상태에서만 삭제할 수 있습니다.");

        int affected = empPlanMapper.softDeleteEmpPlan(empPlanId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("삭제 대상 EMP Plan 이 DRAFT 상태가 아니거나 존재하지 않습니다.");
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
    public void submit(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadAndAssert(empPlanId, user, true, "DRAFT");
        int affected = empPlanMapper.updateSubmit(empPlanId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("제출 처리에 실패했습니다. 현재 상태를 확인하세요.");
        transitStateHistory(empPlanId, plan, "DRAFT", "SBMTD", null, user.getUserId());
    }

    /**
     * 검토 진입 (SBMTD → RVWNG).
     *
     * <p>권한: KOTSA.
     */
    @Transactional
    public void review(String empPlanId, IcasUser user) {
        assertKotsa(user);
        EmpPlanVO plan = loadAndAssertStatus(empPlanId, "SBMTD");
        int affected = empPlanMapper.updateEmpStCd(empPlanId, "RVWNG", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("검토 진입 처리에 실패했습니다. 현재 상태를 확인하세요.");
        transitStateHistory(empPlanId, plan, "SBMTD", "RVWNG", null, user.getUserId());
    }

    /**
     * 반려 (RVWNG → DRAFT).
     *
     * <p>권한: KOTSA. 사유 필수.
     */
    @Transactional
    public void reject(String empPlanId, String reason, IcasUser user) {
        assertKotsa(user);
        if (isBlank(reason)) throw BusinessException.badRequest("반려 사유는 필수입니다.");
        EmpPlanVO plan = loadAndAssertStatus(empPlanId, "RVWNG");
        int affected = empPlanMapper.updateReject(empPlanId, reason, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("반려 처리에 실패했습니다. 현재 상태를 확인하세요.");
        transitStateHistory(empPlanId, plan, "RVWNG", "DRAFT", reason, user.getUserId());
    }

    /**
     * 권고 (RVWNG → RCMDD).
     *
     * <p>권한: KOTSA.
     */
    @Transactional
    public void recommend(String empPlanId, IcasUser user) {
        assertKotsa(user);
        EmpPlanVO plan = loadAndAssertStatus(empPlanId, "RVWNG");
        int affected = empPlanMapper.updateEmpStCd(empPlanId, "RCMDD", user.getUserId());
        if (affected == 0) throw BusinessException.conflict("권고 처리에 실패했습니다. 현재 상태를 확인하세요.");
        transitStateHistory(empPlanId, plan, "RVWNG", "RCMDD", null, user.getUserId());
    }

    /**
     * 승인 (RVWNG 또는 RCMDD → APRVD).
     *
     * <p>권한: MOLIT. approve 시 같은 운영사의 기존 APRVD 버전 자동 만료.
     */
    @Transactional
    public void approve(String empPlanId, IcasUser user) {
        assertMolit(user);
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        if (!"RVWNG".equals(plan.getEmpStCd()) && !"RCMDD".equals(plan.getEmpStCd())) {
            throw BusinessException.badRequest(
                    "승인은 RVWNG 또는 RCMDD 상태에서만 가능합니다. 현재 상태: " + plan.getEmpStCd());
        }

        // 직전 APRVD 자동 만료
        expirePreviousApproved(plan.getOprtrId(), empPlanId, user.getUserId());

        int affected = empPlanMapper.updateApprove(empPlanId, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("승인 처리에 실패했습니다. 현재 상태를 확인하세요.");
        transitStateHistory(empPlanId, plan, plan.getEmpStCd(), "APRVD", null, user.getUserId());
    }

    /**
     * 취소 (APRVD → CNCLD).
     *
     * <p>권한: MOLIT. 사유 필수.
     */
    @Transactional
    public void cancel(String empPlanId, String reason, IcasUser user) {
        assertMolit(user);
        if (isBlank(reason)) throw BusinessException.badRequest("취소 사유는 필수입니다.");
        EmpPlanVO plan = loadAndAssertStatus(empPlanId, "APRVD");
        int affected = empPlanMapper.updateCancel(empPlanId, reason, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("취소 처리에 실패했습니다. 현재 상태를 확인하세요.");
        transitStateHistory(empPlanId, plan, "APRVD", "CNCLD", reason, user.getUserId());
    }

    // ══════════════════════════════════════════════════════
    // 신버전 생성
    // ══════════════════════════════════════════════════════

    /**
     * 신버전 EMP Plan 생성 (APRVD 또는 CNCLD 후 재작성).
     *
     * <p>권한: AIRLINE 본인.
     * <p>기존 plan 은 APRVD 또는 CNCLD 이어야 함.
     * <p>같은 oprtr 에 DRAFT/SBMTD/RVWNG/RCMDD 인 plan 이 있으면 CONFLICT.
     * <p>자식 8종(oprtr_info, oprtr_cnct, acft, cntry_pair, co2_calc, co2_detail, data_ctrl, risk)을 모두 신버전으로 복사한다.
     *
     * @param baseEmpPlanId 기반이 될 기존 EMP Plan ID
     * @param user          로그인 사용자
     * @return 새로 생성된 EMP Plan VO
     */
    @Transactional
    public EmpPlanVO createNewVersion(String baseEmpPlanId, IcasUser user) {
        // 1. 기존 plan 조회
        EmpPlanVO basePlan = empPlanMapper.selectByEmpPlanId(baseEmpPlanId);
        if (basePlan == null) throw BusinessException.notFound("EMP Plan");

        // 2. 권한: AIRLINE 본인
        dataScopeValidator.assertOwnAirline(user, basePlan.getOprtrId());

        // 3. 기존 plan 상태 검증 (APRVD 또는 CNCLD 여야 함)
        String baseSt = basePlan.getEmpStCd();
        if (!"APRVD".equals(baseSt) && !"CNCLD".equals(baseSt)) {
            throw BusinessException.badRequest(
                    "신버전은 APRVD 또는 CNCLD 상태의 EMP Plan 에서만 생성할 수 있습니다. 현재 상태: " + baseSt);
        }

        // 4. 진행 중 plan conflict 검증 (한 번에 하나만 진행)
        int inProgress = empPlanMapper.countInProgressByOprtrId(basePlan.getOprtrId(), baseEmpPlanId);
        if (inProgress > 0) {
            throw BusinessException.conflict(
                    "이미 진행 중인 EMP Plan 이 존재합니다. 기존 진행 중인 plan 을 완료하거나 취소한 후 새 버전을 생성하세요.");
        }

        // 5. emp_plan_id 채번
        int seq = empPlanMapper.countByPrefix(EMP_PLAN_ID_PREFIX) + 1;
        String newEmpPlanId = idGenerator.managementPk(EMP_PLAN_ID_PREFIX, seq);

        // 6. emp_ver 채번 (기존 max + 1.0)
        String newEmpVer = nextEmpVer(basePlan.getOprtrId());

        // 7. 신규 plan 구성 (prev_emp_plan_id = 기존 plan)
        EmpPlanVO newPlan = new EmpPlanVO();
        newPlan.setEmpPlanId(newEmpPlanId);
        newPlan.setOprtrId(basePlan.getOprtrId());
        newPlan.setEmpVer(newEmpVer);
        newPlan.setEmpStCd("DRAFT");
        newPlan.setRprtYr(basePlan.getRprtYr());
        newPlan.setSigChgYn("N");
        newPlan.setPrevEmpPlanId(baseEmpPlanId);
        newPlan.setFrstRegUserId(user.getUserId());
        newPlan.setLastChgUserId(user.getUserId());

        empPlanMapper.insertEmpPlan(newPlan);

        // 8. 자식 데이터 복사 (1:1 + 1:N 모두)
        String srcId = baseEmpPlanId;
        String dstId = newEmpPlanId;
        String uid = user.getUserId();

        int copiedInfo     = empOprtrInfoMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedCo2Calc  = empCo2CalcMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedDataCtrl = empDataCtrlMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedCnct     = empOprtrCnctMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedAcft     = empAcftMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedCntry    = empCntryPairMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedCo2Det   = empCo2DetailMapper.copyToNewPlan(srcId, dstId, uid);
        int copiedRisk     = empRiskMapper.copyToNewPlan(srcId, dstId, uid);

        int totalCopied = copiedInfo + copiedCo2Calc + copiedDataCtrl
                         + copiedCnct + copiedAcft + copiedCntry
                         + copiedCo2Det + copiedRisk;

        // 9. 변경 이력 기록
        insertHstry(newEmpPlanId, baseEmpPlanId, "MASTER",
                "{\"action\":\"신버전생성\",\"basePlanId\":\"" + baseEmpPlanId
                + "\",\"newVer\":\"" + newEmpVer + "\",\"copiedChildRows\":" + totalCopied + "}",
                "N", user.getUserId());

        return empPlanMapper.selectByEmpPlanId(newEmpPlanId);
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════

    /**
     * 상태 전이 + 변경이력 기록 통합 헬퍼.
     *
     * <p>모든 상태 전이는 이 메서드를 통해 이력을 남긴다.
     *
     * @param empPlanId   대상 EMP Plan ID
     * @param plan        현재 plan VO (prevEmpPlanId 참조용)
     * @param fromSt      전이 전 상태
     * @param toSt        전이 후 상태
     * @param reason      사유 (nullable — 반려/취소 시 사용)
     * @param userId      수행 사용자 ID
     */
    private void transitStateHistory(String empPlanId, EmpPlanVO plan,
                                     String fromSt, String toSt,
                                     String reason, String userId) {
        String chgCn;
        if (reason != null && !reason.isBlank()) {
            chgCn = "{\"from\":\"" + fromSt + "\",\"to\":\"" + toSt
                    + "\",\"reason\":\"" + escapeJson(reason) + "\"}";
        } else {
            chgCn = "{\"from\":\"" + fromSt + "\",\"to\":\"" + toSt + "\"}";
        }
        insertHstry(empPlanId, plan.getPrevEmpPlanId(), "STATUS", chgCn, plan.getSigChgYn(), userId);
    }

    /**
     * 직전 APRVD 버전 자동 만료 헬퍼 (도메인가이드 §5-1 "운영사별 1개 활성 버전").
     *
     * <p>같은 oprtr 의 APRVD + 유효구간 내 plan 을 모두 만료시킴 (현재 plan 제외).
     *
     * @param oprtrId     운영사 ID
     * @param currentPlanId 현재 승인 처리 중인 plan ID (만료 제외)
     * @param userId      수행 사용자 ID
     */
    private void expirePreviousApproved(String oprtrId, String currentPlanId, String userId) {
        List<EmpPlanVO> aprvdList = empPlanMapper.selectAprvdByOprtrId(oprtrId, currentPlanId);
        for (EmpPlanVO prev : aprvdList) {
            empPlanMapper.expirePlan(prev.getEmpPlanId(), userId);
            // 만료 이력 기록
            insertHstry(prev.getEmpPlanId(), prev.getPrevEmpPlanId(), "STATUS",
                    "{\"from\":\"APRVD\",\"to\":\"EXPIRED\",\"reason\":\"신버전 승인으로 자동 만료\"}",
                    prev.getSigChgYn(), userId);
        }
    }

    /**
     * 변경이력 단건 insert 래퍼.
     */
    private void insertHstry(String empPlanId, String prevEmpPlanId,
                              String chgChptr, String chgCn,
                              String sigChgYn, String userId) {
        EmpChgHstryVO hstry = new EmpChgHstryVO();
        hstry.setEmpPlanId(empPlanId);
        hstry.setPrevEmpPlanId(prevEmpPlanId);
        hstry.setChgChptr(chgChptr);
        hstry.setChgCn(chgCn);
        hstry.setSigChgYn(sigChgYn != null ? sigChgYn : "N");
        hstry.setChgUserId(userId);
        empChgHstryMapper.insertHstry(hstry);
    }

    /**
     * 운영사 FK 유효성 검증 (유효구간 포함).
     *
     * @param oprtrId 검증할 운영사 ID
     * @throws BusinessException NOT_FOUND — 운영사 미존재 또는 만료
     */
    private void validateOprtrExists(String oprtrId) {
        OprtrVO oprtr = oprtrMapper.selectByOprtrId(oprtrId);
        if (oprtr == null) {
            throw BusinessException.notFound("운영사(oprtrId=" + oprtrId + ")");
        }
    }

    /**
     * 단건 조회 + 상태 검증 (상태 불일치 시 BAD_REQUEST).
     */
    private EmpPlanVO loadAndAssertStatus(String empPlanId, String expectedSt) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        assertStatus(plan, expectedSt,
                expectedSt + " 상태에서만 가능한 작업입니다. 현재 상태: " + plan.getEmpStCd());
        return plan;
    }

    /**
     * AIRLINE 권한 + 상태 검증 통합 (submit 등 AIRLINE 전용 작업용).
     *
     * @param ownAirline true 이면 AIRLINE 본인 검증도 수행
     */
    private EmpPlanVO loadAndAssert(String empPlanId, IcasUser user,
                                    boolean ownAirline, String expectedSt) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        if (ownAirline) {
            dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        }
        assertStatus(plan, expectedSt,
                expectedSt + " 상태에서만 가능한 작업입니다. 현재 상태: " + plan.getEmpStCd());
        return plan;
    }

    /**
     * 단일 상태 검증.
     */
    private void assertStatus(EmpPlanVO plan, String expected, String message) {
        if (!expected.equals(plan.getEmpStCd())) {
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
     * emp_ver 채번: 같은 oprtr 의 max(emp_ver) + 1.
     * 처음이면 "1.0", 다음은 "2.0", ...
     */
    private String nextEmpVer(String oprtrId) {
        String maxVer = empPlanMapper.selectMaxEmpVer(oprtrId);
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

    /** JSON 문자열 내 특수문자 이스케이프 (간단 처리) */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
