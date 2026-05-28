package kr.go.molit.icas.emp.plan.cnct;

import kr.go.molit.icas.emp.plan.cnct.domain.EmpOprtrCnctVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 담당자 연락처 MyBatis 매퍼 (emp.TN_EMP_OPRTR_CNCT).
 *
 * <p>SQL 은 {@code mapper/emp/plan/cnct/EmpOprtrCnctMapper.xml} 에 위치.
 */
@Mapper
public interface EmpOprtrCnctMapper {

    /**
     * emp_plan_id 기준 전체 목록 조회 (cnct_sn ASC, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @return 연락처 목록
     */
    List<EmpOprtrCnctVO> selectByPlanId(@Param("empPlanId") String empPlanId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @param sn        연락처 일련번호
     * @return 연락처 VO, 없으면 null
     */
    EmpOprtrCnctVO selectOne(@Param("empPlanId") String empPlanId,
                              @Param("sn") int sn);

    /**
     * 다음 cnct_sn 채번: 같은 emp_plan_id 의 max(cnct_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param empPlanId EMP Plan ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("empPlanId") String empPlanId);

    /**
     * 같은 plan 내 특정 구분코드 건수 조회 (PRIMARY 유일성 사전 체크용).
     *
     * @param empPlanId EMP Plan ID
     * @param cnctSeCd  연락처 구분 코드
     * @return 해당 구분코드 행 수 (유효구간 내)
     */
    int countByPlanAndSeCd(@Param("empPlanId") String empPlanId,
                            @Param("cnctSeCd") String cnctSeCd);

    /**
     * 신규 연락처 등록.
     *
     * @param vo 등록 데이터 (empPlanId, cnctSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertEmpOprtrCnct(EmpOprtrCnctVO vo);

    /**
     * 연락처 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (empPlanId, cnctSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateEmpOprtrCnct(EmpOprtrCnctVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        연락처 일련번호
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
