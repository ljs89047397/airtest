package kr.go.molit.icas.emp.plan;

import kr.go.molit.icas.emp.plan.domain.EmpPlanSearch;
import kr.go.molit.icas.emp.plan.domain.EmpPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EMP Plan 마스터 MyBatis 매퍼.
 * SQL 은 {@code mapper/emp/plan/EmpPlanMapper.xml} 에 위치.
 */
@Mapper
public interface EmpPlanMapper {

    // ── 조회 ──

    /**
     * 검색 조건 기반 목록 조회 (페이징).
     * 가시범위 SQL 분기: verifierScope=true 시 vrfcn_assgn JOIN 적용.
     */
    List<EmpPlanVO> selectEmpPlans(EmpPlanSearch search);

    /** 검색 조건 기반 전체 건수 (페이징용) */
    long countEmpPlans(EmpPlanSearch search);

    /** 단건 조회 (유효구간 필터 포함) */
    EmpPlanVO selectByEmpPlanId(@Param("empPlanId") String empPlanId);

    /** 같은 운영사의 APRVD 상태 plan 중 현재 plan 제외한 가장 최근 버전 (approve 시 직전 만료 대상) */
    List<EmpPlanVO> selectAprvdByOprtrId(@Param("oprtrId") String oprtrId,
                                          @Param("excludeId") String excludeId);

    /** 같은 운영사의 진행 중(DRAFT/SBMTD/RVWNG/RCMDD) plan 건수 (신버전 conflict 검증) */
    int countInProgressByOprtrId(@Param("oprtrId") String oprtrId,
                                  @Param("excludeId") String excludeId);

    /** emp_plan_id 채번: EP prefix 최대 순번 조회 */
    int countByPrefix(@Param("prefix") String prefix);

    /** 같은 운영사의 최대 emp_ver (정수 변환 가능 문자열) */
    String selectMaxEmpVer(@Param("oprtrId") String oprtrId);

    // ── 등록/수정 ──

    /** DRAFT 신규 등록 */
    int insertEmpPlan(EmpPlanVO vo);

    /** DRAFT 상태 마스터 수정 */
    int updateEmpPlan(EmpPlanVO vo);

    /** 소프트삭제: use_end_dt = NOW() - 1분 (DRAFT 한정, SQL 에서 상태 조건 포함) */
    int softDeleteEmpPlan(@Param("empPlanId") String empPlanId,
                           @Param("userId") String userId);

    // ── 상태 전이 (개별 전이별 필요 컬럼만 UPDATE) ──

    /** 상태 코드만 변경 (submit / review / recommend) */
    int updateEmpStCd(@Param("empPlanId") String empPlanId,
                       @Param("empStCd") String empStCd,
                       @Param("userId") String userId);

    /** 제출: emp_st_cd = SBMTD + sbmt_dt = NOW() */
    int updateSubmit(@Param("empPlanId") String empPlanId,
                      @Param("userId") String userId);

    /** 반려: emp_st_cd = DRAFT + rjct_dt / rjct_rsn 기록 */
    int updateReject(@Param("empPlanId") String empPlanId,
                      @Param("rjctRsn") String rjctRsn,
                      @Param("userId") String userId);

    /** 승인: emp_st_cd = APRVD + aprv_dt / aprv_user_id 기록 */
    int updateApprove(@Param("empPlanId") String empPlanId,
                       @Param("userId") String userId);

    /** 취소: emp_st_cd = CNCLD + rjct_rsn 기록 */
    int updateCancel(@Param("empPlanId") String empPlanId,
                      @Param("rjctRsn") String rjctRsn,
                      @Param("userId") String userId);

    /** 직전 APRVD 버전 만료: use_end_dt = NOW() - 1분 */
    int expirePlan(@Param("empPlanId") String empPlanId,
                    @Param("userId") String userId);
}
