package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 결재 업무 마스터 MyBatis Mapper 인터페이스 (com.tn_atrz_task).
 * SQL 은 AtrzTaskMapper.xml 에서 관리. 어노테이션 SQL 금지.
 */
@Mapper
public interface AtrzTaskMapper {

    /**
     * 유효한 결재 업무 전체 조회.
     * sysSeCd 가 null/blank 가 아니면 필터 적용.
     */
    List<AtrzTaskVO> selectAtrzTasks(@Param("sysSeCd") String sysSeCd);

    /**
     * 결재 업무 단건 조회 (유효구간 포함).
     *
     * @param atrzTaskId 결재 업무 ID
     * @return VO 또는 null
     */
    AtrzTaskVO selectByTaskId(@Param("atrzTaskId") String atrzTaskId);

    /**
     * 결재 업무 ID 존재 여부 확인 (유효구간 무관 중복 체크용).
     *
     * @param atrzTaskId 검사할 ID
     * @return true if exists
     */
    boolean existsAtrzTaskId(@Param("atrzTaskId") String atrzTaskId);

    /**
     * 결재 업무 등록.
     *
     * @param vo 등록 VO
     * @return 영향 행 수
     */
    int insertAtrzTask(AtrzTaskVO vo);

    /**
     * 결재 업무 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 VO (atrzTaskId 필드 필수)
     * @return 영향 행 수
     */
    int updateAtrzTask(AtrzTaskVO vo);

    /**
     * 결재 업무 소프트삭제 (use_end_dt = NOW() - 1 minute).
     *
     * @param atrzTaskId 결재 업무 ID
     * @param userId     처리 사용자 ID
     * @return 영향 행 수
     */
    int softDeleteAtrzTask(@Param("atrzTaskId") String atrzTaskId,
                           @Param("userId")     String userId);
}
