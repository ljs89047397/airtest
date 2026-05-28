package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzPrcsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 결재 처리(단계별) MyBatis Mapper 인터페이스 (com.tn_atrz_prcs).
 * PK = (atrz_dmnd_id, atrz_seq).
 * SQL 은 AtrzPrcsMapper.xml 에서 관리. 어노테이션 SQL 금지.
 */
@Mapper
public interface AtrzPrcsMapper {

    /**
     * 결재 요청에 속한 전체 단계 목록 조회 (atrz_seq 오름차순).
     *
     * @param atrzDmndId 결재 요청 ID
     * @return 단계별 결재 목록
     */
    List<AtrzPrcsVO> selectPrcsByDmndId(@Param("atrzDmndId") String atrzDmndId);

    /**
     * 특정 단계 단건 조회 (유효구간 포함).
     *
     * @param atrzDmndId 결재 요청 ID
     * @param atrzSeq    결재 순번
     * @return VO 또는 null
     */
    AtrzPrcsVO selectPrcs(@Param("atrzDmndId") String atrzDmndId,
                          @Param("atrzSeq")    int    atrzSeq);

    /**
     * 결재 처리 행 단건 등록 (요청 시점에 미처리 상태로 삽입).
     *
     * @param vo 등록 VO (atrzRsltCd = null)
     * @return 영향 행 수
     */
    int insertAtrzPrcs(AtrzPrcsVO vo);

    /**
     * 결재 처리 결과 업데이트 (승인 / 반려).
     *
     * @param atrzDmndId 결재 요청 ID
     * @param atrzSeq    결재 순번
     * @param atrzRsltCd 결재 결과 코드 (APRVD / RJCTD)
     * @param atrzOpnn   결재 의견
     * @param userId     처리 사용자 ID
     * @return 영향 행 수
     */
    int updateAtrzPrcs(@Param("atrzDmndId") String atrzDmndId,
                       @Param("atrzSeq")    int    atrzSeq,
                       @Param("atrzRsltCd") String atrzRsltCd,
                       @Param("atrzOpnn")   String atrzOpnn,
                       @Param("userId")     String userId);

    /**
     * 특정 순번 이전에 미처리(atrz_rslt_cd IS NULL) 행 수 조회.
     * 이 값이 0 이어야 현재 순번 결재가 가능하다 (선행 단계 강제).
     *
     * @param atrzDmndId 결재 요청 ID
     * @param atrzSeq    현재 처리하려는 순번
     * @return 미처리 선행 단계 수
     */
    int countPendingBefore(@Param("atrzDmndId") String atrzDmndId,
                           @Param("atrzSeq")    int    atrzSeq);

    /**
     * 전체 단계 수 조회 (마지막 단계 여부 판별용).
     *
     * @param atrzDmndId 결재 요청 ID
     * @return 전체 결재 단계 수
     */
    int countTotalPrcs(@Param("atrzDmndId") String atrzDmndId);

    /**
     * 내가 처리해야 할 PENDING 단계 목록.
     * 조건: atrz_rslt_cd IS NULL AND 선행 단계 모두 APRVD (현재 차례인 행).
     *
     * @param atrzUserId 결재자 사용자 ID
     * @return 내 처리 대기 목록
     */
    List<AtrzPrcsVO> selectMyPending(@Param("atrzUserId") String atrzUserId);
}
