package kr.go.molit.icas.emp.plan.cntry;

import kr.go.molit.icas.emp.plan.cntry.domain.EmpCntryPairVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 운항 국가 쌍 MyBatis 매퍼 (emp.TN_EMP_CNTRY_PAIR).
 *
 * <p>SQL 은 {@code mapper/emp/plan/cntry/EmpCntryPairMapper.xml} 에 위치.
 */
@Mapper
public interface EmpCntryPairMapper {

    /**
     * emp_plan_id 기준 전체 목록 조회 (pair_sn ASC, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @return 국가쌍 목록
     */
    List<EmpCntryPairVO> selectByPlanId(@Param("empPlanId") String empPlanId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param empPlanId EMP Plan ID
     * @param sn        국가쌍 일련번호
     * @return 국가쌍 VO, 없으면 null
     */
    EmpCntryPairVO selectOne(@Param("empPlanId") String empPlanId,
                             @Param("sn") int sn);

    /**
     * 다음 pair_sn 채번: 같은 emp_plan_id 의 max(pair_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param empPlanId EMP Plan ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("empPlanId") String empPlanId);

    /**
     * 같은 (출발, 도착) 조합 중복 체크.
     * 자기 자신(sn)은 제외 (수정 시 동일 쌍 허용).
     * excludeSn = -1 을 넘기면 전체 중복 검사 (신규 등록 시).
     *
     * @param empPlanId   EMP Plan ID
     * @param dprtrCntryCd 출발 국가 코드
     * @param arvlCntryCd  도착 국가 코드
     * @param excludeSn   제외할 sn (-1: 제외 없음)
     * @return 중복 건수
     */
    int existsByCntryPair(@Param("empPlanId") String empPlanId,
                          @Param("dprtrCntryCd") String dprtrCntryCd,
                          @Param("arvlCntryCd") String arvlCntryCd,
                          @Param("excludeSn") int excludeSn);

    /**
     * 신규 국가쌍 등록.
     *
     * @param vo 등록 데이터 (empPlanId, pairSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertEmpCntryPair(EmpCntryPairVO vo);

    /**
     * 국가쌍 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (empPlanId, pairSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateEmpCntryPair(EmpCntryPairVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param empPlanId EMP Plan ID
     * @param sn        국가쌍 일련번호
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
