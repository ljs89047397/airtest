package kr.go.molit.icas.er.rprt.afbr;

import kr.go.molit.icas.er.rprt.afbr.domain.ErAfbrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 항공기 유형별 평균 연료연소율 MyBatis 매퍼 (er.tn_er_afbr).
 *
 * <p>SQL 은 {@code mapper/er/rprt/afbr/ErAfbrMapper.xml} 에 위치.
 * PK 가 (er_id, acft_type_cd) 자연키이므로 sn 채번 없음.
 */
@Mapper
public interface ErAfbrMapper {

    /**
     * er_id 기준 전체 목록 조회 (acft_type_cd ASC, 유효구간 필터 포함).
     *
     * @param erId ER ID
     * @return 평균 연료연소율 목록
     */
    List<ErAfbrVO> selectByErId(@Param("erId") String erId);

    /**
     * 단건 조회 (자연키 PK, 유효구간 필터 포함).
     *
     * @param erId       ER ID
     * @param acftTypeCd 항공기 유형 코드
     * @return 평균 연료연소율 VO, 없으면 null
     */
    ErAfbrVO selectOne(@Param("erId") String erId,
                       @Param("acftTypeCd") String acftTypeCd);

    /**
     * 같은 (er_id, acft_type_cd) PK 존재 여부 확인.
     *
     * @param erId       ER ID
     * @param acftTypeCd 항공기 유형 코드
     * @return 존재 시 true
     */
    boolean existsByPk(@Param("erId") String erId,
                       @Param("acftTypeCd") String acftTypeCd);

    /**
     * 평균 연료연소율 신규 등록.
     *
     * @param vo 등록 데이터 (erId, acftTypeCd, afbrVal 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertAfbr(ErAfbrVO vo);

    /**
     * 평균 연료연소율 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (erId, acftTypeCd 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateAfbr(ErAfbrVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param erId       ER ID
     * @param acftTypeCd 항공기 유형 코드
     * @param userId     수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDeleteOne(@Param("erId") String erId,
                      @Param("acftTypeCd") String acftTypeCd,
                      @Param("userId") String userId);

    /**
     * 신버전 ER 생성 시 자식 데이터 복사 (src → dst).
     *
     * @param srcErId 복사 원본 ER ID
     * @param dstErId 복사 대상 ER ID
     * @param userId  복사 수행 사용자 ID
     * @return 영향 행 수 (복사된 건수)
     */
    int copyToNewEr(@Param("srcErId") String srcErId,
                    @Param("dstErId") String dstErId,
                    @Param("userId") String userId);
}
