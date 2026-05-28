package kr.go.molit.icas.emp.plan.co2;

import kr.go.molit.icas.emp.plan.co2.domain.EmpCo2CalcVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 배출량 계산방법 MyBatis 매퍼 (emp.TN_EMP_CO2_CALC).
 *
 * <p>SQL 은 {@code mapper/emp/plan/co2/EmpCo2CalcMapper.xml} 에 위치.
 * emp_plan_id 가 PK 이므로 채번 없이 부모 plan ID 를 그대로 사용한다.
 */
@Mapper
public interface EmpCo2CalcMapper {

    /**
     * emp_plan_id 로 단건 조회 (유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @return 배출량 계산방법 VO, 없으면 null
     */
    EmpCo2CalcVO selectByPlanId(@Param("empPlanId") String empPlanId);

    /**
     * emp_plan_id 에 해당하는 행 존재 여부 확인 (upsert 분기 판단용).
     *
     * @param empPlanId EMP Plan ID
     * @return true — 이미 존재, false — 미존재
     */
    boolean existsByPlanId(@Param("empPlanId") String empPlanId);

    /**
     * 배출량 계산방법 신규 등록.
     *
     * @param vo 등록 데이터 (empPlanId 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertEmpCo2Calc(EmpCo2CalcVO vo);

    /**
     * 배출량 계산방법 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (empPlanId 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateEmpCo2Calc(EmpCo2CalcVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param empPlanId EMP Plan ID
     * @param userId    수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDeleteByPlanId(@Param("empPlanId") String empPlanId,
                           @Param("userId") String userId);

    /**
     * 신버전 생성 시 자식 데이터 복사 (src → dst).
     *
     * <p>INSERT INTO ... SELECT ... WHERE emp_plan_id = srcEmpPlanId 형태.
     * emp_plan_id 만 새 값(dstEmpPlanId)으로 치환하며
     * frst_reg_user_id / last_chg_user_id 를 복사 수행자로 갱신한다.
     *
     * @param srcEmpPlanId 복사 원본 EMP Plan ID
     * @param dstEmpPlanId 복사 대상 EMP Plan ID
     * @param userId       복사 수행 사용자 ID
     * @return 영향 행 수 (원본 없으면 0)
     */
    int copyToNewPlan(@Param("srcEmpPlanId") String srcEmpPlanId,
                      @Param("dstEmpPlanId") String dstEmpPlanId,
                      @Param("userId") String userId);
}
