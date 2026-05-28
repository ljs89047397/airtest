package kr.go.molit.icas.er.rprt.aerdrm;

import kr.go.molit.icas.er.rprt.aerdrm.domain.ErAerdrmPairCo2VO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 비행장 쌍 배출량 MyBatis 매퍼 (er.tn_er_aerdrm_pair_co2).
 *
 * <p>SQL 은 {@code mapper/er/rprt/aerdrm/ErAerdrmPairCo2Mapper.xml} 에 위치.
 */
@Mapper
public interface ErAerdrmPairCo2Mapper {

    /**
     * er_id 기준 전체 목록 조회 (pair_sn ASC, 유효구간 필터 포함).
     *
     * @param erId ER ID
     * @return 비행장 쌍 배출량 목록
     */
    List<ErAerdrmPairCo2VO> selectByErId(@Param("erId") String erId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param erId   ER ID
     * @param pairSn 비행장 쌍 일련번호
     * @return 비행장 쌍 배출량 VO, 없으면 null
     */
    ErAerdrmPairCo2VO selectOne(@Param("erId") String erId,
                                @Param("pairSn") int pairSn);

    /**
     * 다음 pair_sn 채번: 같은 er_id 의 max(pair_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param erId ER ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("erId") String erId);

    /**
     * 같은 er_id 내 (dprtr_aerdrm_cd, arvl_aerdrm_cd, fuel_type_cd) 조합 중복 체크.
     * 수정 시 자기 자신 제외를 위해 excludeSn 파라미터 사용 (신규 시 excludeSn=-1 전달).
     *
     * @param erId          ER ID
     * @param dprtrAerdrmCd 출발 비행장 코드
     * @param arvlAerdrmCd  도착 비행장 코드
     * @param fuelTypeCd    연료 유형 코드
     * @param excludeSn     제외할 일련번호 (신규 시 -1)
     * @return 중복 존재 시 true
     */
    boolean existsByAerdrmPair(@Param("erId") String erId,
                               @Param("dprtrAerdrmCd") String dprtrAerdrmCd,
                               @Param("arvlAerdrmCd") String arvlAerdrmCd,
                               @Param("fuelTypeCd") String fuelTypeCd,
                               @Param("excludeSn") int excludeSn);

    /**
     * 해당 ER 의 전체 CO₂ 배출량 합계.
     * 행이 없으면 0 반환 (COALESCE 처리).
     * 국가 쌍 ↔ 비행장 쌍 합계 일치 검증에 사용.
     *
     * @param erId ER ID
     * @return CO₂ 합계 (null 없음, 0 이상)
     */
    BigDecimal sumCo2EmsnByEr(@Param("erId") String erId);

    /**
     * 신규 비행장 쌍 배출량 등록.
     *
     * @param vo 등록 데이터 (erId, pairSn, fuelTypeCd 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insert(ErAerdrmPairCo2VO vo);

    /**
     * 비행장 쌍 배출량 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (erId, pairSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int update(ErAerdrmPairCo2VO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param erId   ER ID
     * @param pairSn 비행장 쌍 일련번호
     * @param userId 수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDelete(@Param("erId") String erId,
                   @Param("pairSn") int pairSn,
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
