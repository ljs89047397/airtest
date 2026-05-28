package kr.go.molit.icas.emp.plan.co2detail;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.emp.plan.EmpPlanMapper;
import kr.go.molit.icas.emp.plan.co2detail.domain.EmpCo2DetailVO;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * CO2 측정 상세 비즈니스 서비스 (emp.TN_EMP_CO2_DETAIL).
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>부모 plan 이 DRAFT 상태일 때만 추가/수정/삭제 가능</li>
 *   <li>mntr_mthd_cd 가 PK 이자 자연키 — sn 채번 없음</li>
 *   <li>mntr_mthd_cd 화이트리스트: MTHD_A / MTHD_B / BLOCK_ON_OFF / REFUEL / BLOCK_ALLOC</li>
 *   <li>같은 plan 에 동일 mntr_mthd_cd 중복 금지 (친절한 에러 위해 PK 직전 사전 체크)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpCo2DetailService {

    private static final Set<String> VALID_MNTR_MTHD_CD =
            Set.of("MTHD_A", "MTHD_B", "BLOCK_ON_OFF", "REFUEL", "BLOCK_ALLOC");

    private final EmpCo2DetailMapper empCo2DetailMapper;
    private final EmpPlanMapper      empPlanMapper;
    private final DataScopeValidator dataScopeValidator;

    // ══════════════════════════════════════════════════════
    // 조회
    // ══════════════════════════════════════════════════════

    /**
     * 특정 EMP Plan 의 CO2 측정 상세 전체 목록 조회.
     *
     * @param empPlanId EMP Plan ID
     * @param user      로그인 사용자
     * @return CO2 측정 상세 목록
     * @throws BusinessException NOT_FOUND — plan 미존재
     * @throws BusinessException FORBIDDEN — 가시범위 밖
     */
    public List<EmpCo2DetailVO> listByPlan(String empPlanId, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        return empCo2DetailMapper.selectByPlanId(empPlanId);
    }

    /**
     * CO2 측정 상세 단건 조회.
     *
     * @param empPlanId  EMP Plan ID
     * @param mntrMthdCd 모니터링 방법 코드
     * @param user       로그인 사용자
     * @return CO2 측정 상세 VO
     * @throws BusinessException NOT_FOUND — plan 또는 측정 상세 미존재
     */
    public EmpCo2DetailVO getOne(String empPlanId, String mntrMthdCd, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOprtrAccessible(user, plan.getOprtrId(), plan.getRprtYr());
        EmpCo2DetailVO detail = empCo2DetailMapper.selectOne(empPlanId, mntrMthdCd);
        if (detail == null) throw BusinessException.notFound("CO2 측정 상세");
        return detail;
    }

    // ══════════════════════════════════════════════════════
    // 추가
    // ══════════════════════════════════════════════════════

    /**
     * CO2 측정 상세 추가.
     *
     * <p>같은 plan 에 동일 mntr_mthd_cd 가 이미 존재하면 CONFLICT.
     *
     * @param empPlanId EMP Plan ID
     * @param vo        등록 데이터 (mntrMthdCd 필수)
     * @param user      로그인 사용자
     * @return 생성된 CO2 측정 상세 VO
     * @throws BusinessException BAD_REQUEST — 검증 실패
     * @throws BusinessException CONFLICT    — 방법 코드 중복
     */
    @Transactional
    public EmpCo2DetailVO addChild(String empPlanId, EmpCo2DetailVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        validateMntrMthdCd(vo.getMntrMthdCd());

        // mntr_mthd_cd 중복 체크 (PK 이지만 친절한 에러 메시지 위해 사전 체크)
        int dupCount = empCo2DetailMapper.existsByMethod(empPlanId, vo.getMntrMthdCd());
        if (dupCount > 0) {
            throw BusinessException.conflict(
                    "이미 등록된 모니터링 방법입니다: " + vo.getMntrMthdCd() + ". 수정을 이용해 주세요.");
        }

        vo.setEmpPlanId(empPlanId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        empCo2DetailMapper.insertEmpCo2Detail(vo);
        return empCo2DetailMapper.selectOne(empPlanId, vo.getMntrMthdCd());
    }

    // ══════════════════════════════════════════════════════
    // 수정
    // ══════════════════════════════════════════════════════

    /**
     * CO2 측정 상세 수정.
     *
     * <p>mntr_mthd_cd 는 PK 이므로 수정 불가. 서술 컬럼만 업데이트.
     *
     * @param empPlanId  EMP Plan ID
     * @param mntrMthdCd 모니터링 방법 코드
     * @param vo         수정 데이터
     * @param user       로그인 사용자
     * @throws BusinessException NOT_FOUND — 측정 상세 미존재
     */
    @Transactional
    public void updateChild(String empPlanId, String mntrMthdCd, EmpCo2DetailVO vo, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        EmpCo2DetailVO existing = empCo2DetailMapper.selectOne(empPlanId, mntrMthdCd);
        if (existing == null) throw BusinessException.notFound("CO2 측정 상세");

        vo.setEmpPlanId(empPlanId);
        vo.setMntrMthdCd(mntrMthdCd);
        vo.setLastChgUserId(user.getUserId());

        int affected = empCo2DetailMapper.updateEmpCo2Detail(vo);
        if (affected == 0) throw BusinessException.conflict("수정 대상 CO2 측정 상세가 존재하지 않거나 이미 만료되었습니다.");
    }

    // ══════════════════════════════════════════════════════
    // 소프트삭제
    // ══════════════════════════════════════════════════════

    /**
     * CO2 측정 상세 소프트삭제.
     *
     * @param empPlanId  EMP Plan ID
     * @param mntrMthdCd 모니터링 방법 코드
     * @param user       로그인 사용자
     * @throws BusinessException NOT_FOUND — 측정 상세 미존재
     */
    @Transactional
    public void softDeleteChild(String empPlanId, String mntrMthdCd, IcasUser user) {
        EmpPlanVO plan = loadPlan(empPlanId);
        dataScopeValidator.assertOwnAirline(user, plan.getOprtrId());
        assertDraft(plan);

        int affected = empCo2DetailMapper.softDeleteOne(empPlanId, mntrMthdCd, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("CO2 측정 상세");
    }

    // ══════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════

    private EmpPlanVO loadPlan(String empPlanId) {
        EmpPlanVO plan = empPlanMapper.selectByEmpPlanId(empPlanId);
        if (plan == null) throw BusinessException.notFound("EMP Plan");
        return plan;
    }

    private void assertDraft(EmpPlanVO plan) {
        if (!"DRAFT".equals(plan.getEmpStCd())) {
            throw BusinessException.badRequest("DRAFT 상태의 EMP Plan 에서만 수정할 수 있습니다. 현재 상태: " + plan.getEmpStCd());
        }
    }

    private void validateMntrMthdCd(String mntrMthdCd) {
        if (mntrMthdCd == null || !VALID_MNTR_MTHD_CD.contains(mntrMthdCd)) {
            throw BusinessException.badRequest(
                    "모니터링 방법 코드(mntrMthdCd) 허용값: MTHD_A, MTHD_B, BLOCK_ON_OFF, REFUEL, BLOCK_ALLOC. 입력값: " + mntrMthdCd);
        }
    }
}
