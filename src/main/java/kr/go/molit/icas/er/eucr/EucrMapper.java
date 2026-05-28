package kr.go.molit.icas.er.eucr;

import kr.go.molit.icas.er.eucr.domain.EucrSearch;
import kr.go.molit.icas.er.eucr.domain.EucrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * EUCR 마스터 MyBatis 매퍼 (er.tn_eucr).
 * SQL: {@code mapper/er/eucr/EucrMapper.xml}.
 */
@Mapper
public interface EucrMapper {

    // ── 조회 ──
    List<EucrVO> selectEucrs(EucrSearch search);
    long countEucrs(EucrSearch search);
    EucrVO selectByEucrId(@Param("eucrId") String eucrId);

    // ── 채번 ──
    int countByPrefix(@Param("prefix") String prefix);
    String selectMaxEucrVer(@Param("oprtrId") String oprtrId,
                            @Param("rprtYr") String rprtYr);

    // ── 등록 / 수정 / 삭제 ──
    int insertEucr(EucrVO vo);
    int updateOfstReqQty(@Param("eucrId") String eucrId,
                         @Param("ofstReqQty") BigDecimal ofstReqQty,
                         @Param("userId") String userId);
    int softDeleteEucr(@Param("eucrId") String eucrId, @Param("userId") String userId);

    // ── 합계 재계산 + 의무 충족 판정 ──
    /** SUM(batch.sub_qty) */
    BigDecimal sumBatchQty(@Param("eucrId") String eucrId);

    int updateTtlQtyAndFulfilled(@Param("eucrId") String eucrId,
                                 @Param("ttlQty") BigDecimal ttlQty,
                                 @Param("fulfilledYn") String fulfilledYn,
                                 @Param("userId") String userId);

    // ── 상태 전이 ──
    int updateSubmit(@Param("eucrId") String eucrId, @Param("userId") String userId);
    int updateEucrStCd(@Param("eucrId") String eucrId,
                       @Param("newSt") String newSt,
                       @Param("userId") String userId);
    int updateApprove(@Param("eucrId") String eucrId, @Param("userId") String userId);
    int updateCancel(@Param("eucrId") String eucrId,
                     @Param("reason") String reason,
                     @Param("userId") String userId);
}
