package kr.go.molit.icas.emp.plan.risk;

import kr.go.molit.icas.emp.plan.risk.domain.EmpRiskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 위험·통제 항목 MyBatis 매퍼 (emp.TN_EMP_RISK).
 *
 * <p>SQL 은 {@code mapper/emp/plan/risk/EmpRiskMapper.xml} 에 위치.
 */
@Mapper
public interface EmpRiskMapper {

    /**
     * emp_plan_id 기준 전체 목록 조회 (risk_sn ASC, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @return 위험·통제 목록
     */
    List<EmpRiskVO> selectByPlanId(@Param("empPlanId") String empPlanId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @param sn        위험 항목 일련번호
     * @return 위험·통제 VO, 없으면 null
     */
    EmpRiskVO selectOne(@Param("empPlanId") String empPlanId,
                        @Param("sn") int sn);

    /**
     * 다음 risk_sn 채번: 같은 emp_plan_id 의 max(risk_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param empPlanId EMP Plan ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("empPlanId") String empPlanId);

    /**
     * 신규 위험·통제 항목 등록.
     *
     * @param vo 등록 데이터 (empPlanId, riskSn, riskDesc 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertEmpRisk(EmpRiskVO vo);

    /**
     * 위험·통제 항목 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (empPlanId, riskSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateEmpRisk(EmpRiskVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        위험 항목 일련번호
     * @param userId    수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDeleteOne(@Param("empPlanId") String empPlanId,
                      @Param("sn") int sn,
                      @Param("userId") String userId);

    /**
     * 신버전 생성 시 자식 데이터 복사 (src → dst).
     *
     * @param srcEmpPlanId 복사 원본 EMP Plan ID
     * @param dstEmpPlanId 복사 대상 EMP Plan ID
     * @param userId       복사 수행 사용자 ID
     * @return 영향 행 수 (복사된 건수)
     */
    int copyToNewPlan(@Param("srcEmpPlanId") String srcEmpPlanId,
                      @Param("dstEmpPlanId") String dstEmpPlanId,
                      @Param("userId") String userId);
}
