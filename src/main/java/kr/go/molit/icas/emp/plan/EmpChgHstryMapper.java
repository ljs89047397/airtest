package kr.go.molit.icas.emp.plan;

import kr.go.molit.icas.emp.plan.domain.EmpChgHstryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EMP 변경 이력 MyBatis 매퍼.
 * SQL 은 {@code mapper/emp/plan/EmpChgHstryMapper.xml} 에 위치.
 *
 * <p>이력 테이블(th_emp_chg_hstry)은 append-only — SELECT / INSERT 만 제공.
 */
@Mapper
public interface EmpChgHstryMapper {

    /**
     * 특정 EMP Plan 의 변경 이력 전체 조회 (최신순).
     *
     * @param empPlanId EMP Plan ID
     * @return 변경 이력 목록
     */
    List<EmpChgHstryVO> selectHstryByEmpPlanId(@Param("empPlanId") String empPlanId);

    /**
     * 변경 이력 단건 insert (모든 상태 전이 / master 변경 시 호출).
     *
     * @param vo 이력 VO (chg_hstry_id 는 bigserial 자동채번이므로 미설정)
     * @return 영향 행 수
     */
    int insertHstry(EmpChgHstryVO vo);
}
