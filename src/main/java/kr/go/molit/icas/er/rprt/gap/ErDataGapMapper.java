package kr.go.molit.icas.er.rprt.gap;

import kr.go.molit.icas.er.rprt.gap.domain.ErDataGapVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 데이터 갭 MyBatis 매퍼 (er.tn_er_data_gap).
 *
 * <p>SQL 은 {@code mapper/er/rprt/gap/ErDataGapMapper.xml} 에 위치.
 */
@Mapper
public interface ErDataGapMapper {

    /**
     * er_id 기준 전체 목록 조회 (gap_sn ASC, 유효구간 필터 포함).
     *
     * @param erId ER ID
     * @return 데이터 갭 목록
     */
    List<ErDataGapVO> selectByErId(@Param("erId") String erId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param erId  ER ID
     * @param gapSn 데이터 갭 일련번호
     * @return 데이터 갭 VO, 없으면 null
     */
    ErDataGapVO selectOne(@Param("erId") String erId,
                          @Param("gapSn") int gapSn);

    /**
     * 다음 gap_sn 채번: 같은 er_id 의 max(gap_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param erId ER ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("erId") String erId);

    /**
     * 신규 데이터 갭 등록.
     * thrshld_5pct_xc_yn 은 서비스 레이어에서 자동 계산 후 VO 에 설정됨.
     *
     * @param vo 등록 데이터 (erId, gapSn, thrshld5pctXcYn 포함)
     * @return 영향 행 수 (정상 1)
     */
    int insert(ErDataGapVO vo);

    /**
     * 데이터 갭 수정 (유효구간 필터 포함).
     * thrshld_5pct_xc_yn 은 서비스 레이어에서 자동 재계산 후 VO 에 설정됨.
     *
     * @param vo 수정 데이터 (erId, gapSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int update(ErDataGapVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param erId   ER ID
     * @param gapSn  데이터 갭 일련번호
     * @param userId 수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDelete(@Param("erId") String erId,
                   @Param("gapSn") int gapSn,
                   @Param("userId") String userId);

    /**
     * 신버전 생성 시 자식 데이터 복사 (src → dst, INSERT ... SELECT 패턴).
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
