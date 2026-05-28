package kr.go.molit.icas.er.rprt.acft;

import kr.go.molit.icas.er.rprt.acft.domain.ErAcftFuelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 항공기·연료 MyBatis 매퍼 (er.tn_er_acft_fuel).
 *
 * <p>SQL 은 {@code mapper/er/rprt/acft/ErAcftFuelMapper.xml} 에 위치.
 * 복합 PK: (er_id, acft_sn) — acft_sn 은 같은 er_id 내 max+1 자동 채번.
 */
@Mapper
public interface ErAcftFuelMapper {

    /**
     * er_id 기준 전체 목록 조회 (acft_sn ASC, 유효구간 필터 포함).
     *
     * @param erId ER ID
     * @return 항공기·연료 목록
     */
    List<ErAcftFuelVO> selectByErId(@Param("erId") String erId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param erId   ER ID
     * @param acftSn 항공기 일련번호
     * @return 항공기·연료 VO, 없으면 null
     */
    ErAcftFuelVO selectOne(@Param("erId") String erId,
                           @Param("acftSn") int acftSn);

    /**
     * 다음 acft_sn 채번: 같은 er_id 의 max(acft_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param erId ER ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("erId") String erId);

    /**
     * 같은 er_id 내 동일 regis_mark 존재 여부 (중복 방지).
     * 수정 시 자기 자신 제외를 위해 excludeSn 파라미터 사용 (신규 시 excludeSn=0 전달).
     *
     * @param erId      ER ID
     * @param regisMark 항공기 등록기호
     * @param excludeSn 제외할 일련번호 (신규 시 0)
     * @return 중복 존재 시 true
     */
    boolean existsByRegisMark(@Param("erId") String erId,
                               @Param("regisMark") String regisMark,
                               @Param("excludeSn") int excludeSn);

    /**
     * 신규 항공기·연료 등록.
     *
     * @param vo 등록 데이터 (erId, acftSn, regisMark, fuelTypeCd 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertAcftFuel(ErAcftFuelVO vo);

    /**
     * 항공기·연료 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (erId, acftSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateAcftFuel(ErAcftFuelVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param erId   ER ID
     * @param acftSn 항공기 일련번호
     * @param userId 수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDeleteOne(@Param("erId") String erId,
                      @Param("acftSn") int acftSn,
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
