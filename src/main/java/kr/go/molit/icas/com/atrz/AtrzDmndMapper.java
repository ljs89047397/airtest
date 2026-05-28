package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzDmndSearch;
import kr.go.molit.icas.com.atrz.domain.AtrzDmndVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 결재 요청 MyBatis Mapper 인터페이스 (com.tn_atrz_dmnd).
 * SQL 은 AtrzDmndMapper.xml 에서 관리. 어노테이션 SQL 금지.
 */
@Mapper
public interface AtrzDmndMapper {

    /**
     * 검색 조건 + 페이징으로 결재 요청 목록 조회.
     *
     * @param search 검색 조건 DTO
     * @return 결재 요청 목록
     */
    List<AtrzDmndVO> selectAtrzDmnds(AtrzDmndSearch search);

    /**
     * 검색 조건 총 건수 (페이징용).
     *
     * @param search 검색 조건 DTO
     * @return 전체 건수
     */
    long countAtrzDmnds(AtrzDmndSearch search);

    /**
     * 결재 요청 단건 조회 (유효구간 포함).
     *
     * @param atrzDmndId 결재 요청 ID
     * @return VO 또는 null
     */
    AtrzDmndVO selectByDmndId(@Param("atrzDmndId") String atrzDmndId);

    /**
     * 결재 요청 등록.
     *
     * @param vo 등록 VO
     * @return 영향 행 수
     */
    int insertAtrzDmnd(AtrzDmndVO vo);

    /**
     * 결재 상태 코드 업데이트 (유효구간 필터 포함).
     *
     * @param atrzDmndId 결재 요청 ID
     * @param atrzStCd   변경할 상태 코드
     * @param userId     처리 사용자 ID
     * @return 영향 행 수
     */
    int updateAtrzStCd(@Param("atrzDmndId") String atrzDmndId,
                       @Param("atrzStCd")   String atrzStCd,
                       @Param("userId")     String userId);

    /**
     * 채번용 — prefix 이후 숫자 부분의 MAX 반환. 없으면 0.
     * 예) prefix = "AD" → MAX(CAST(SUBSTRING(atrz_dmnd_id, 3) AS INT))
     *
     * @param prefix ID 접두어
     * @return 현재 최대 순번 (없으면 0)
     */
    int countByPrefix(@Param("prefix") String prefix);
}
