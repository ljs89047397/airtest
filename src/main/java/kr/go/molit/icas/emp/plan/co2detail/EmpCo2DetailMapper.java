package kr.go.molit.icas.emp.plan.co2detail;

import kr.go.molit.icas.emp.plan.co2detail.domain.EmpCo2DetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CO2 측정 상세 MyBatis 매퍼 (emp.TN_EMP_CO2_DETAIL).
 *
 * <p>SQL 은 {@code mapper/emp/plan/co2detail/EmpCo2DetailMapper.xml} 에 위치.
 * PK 가 (emp_plan_id, mntr_mthd_cd) 이므로 sn 채번 없음.
 */
@Mapper
public interface EmpCo2DetailMapper {

    /**
     * emp_plan_id 기준 전체 목록 조회 (mntr_mthd_cd ASC, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @return CO2 측정 상세 목록
     */
    List<EmpCo2DetailVO> selectByPlanId(@Param("empPlanId") String empPlanId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param empPlanId  EMP Plan ID
     * @param mntrMthdCd 모니터링 방법 코드
     * @return CO2 측정 상세 VO, 없으면 null
     */
    EmpCo2DetailVO selectOne(@Param("empPlanId") String empPlanId,
                             @Param("mntrMthdCd") String mntrMthdCd);

    /**
     * 같은 plan 내 동일 mntr_mthd_cd 존재 여부 (중복 등록 사전 체크).
     *
     * @param empPlanId  EMP Plan ID
     * @param mntrMthdCd 모니터링 방법 코드
     * @return 존재 건수 (0 또는 1)
     */
    int existsByMethod(@Param("empPlanId") String empPlanId,
                       @Param("mntrMthdCd") String mntrMthdCd);

    /**
     * CO2 측정 상세 신규 등록.
     *
     * @param vo 등록 데이터 (empPlanId, mntrMthdCd 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertEmpCo2Detail(EmpCo2DetailVO vo);

    /**
     * CO2 측정 상세 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (empPlanId, mntrMthdCd 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateEmpCo2Detail(EmpCo2DetailVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param empPlanId  EMP Plan ID
     * @param mntrMthdCd 모니터링 방법 코드
     * @param userId     수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDeleteOne(@Param("empPlanId") String empPlanId,
                      @Param("mntrMthdCd") String mntrMthdCd,
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
