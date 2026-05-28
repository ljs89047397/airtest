package kr.go.molit.icas.er.cef;

import kr.go.molit.icas.er.cef.domain.CefSearch;
import kr.go.molit.icas.er.cef.domain.CefVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * CEF 마스터 MyBatis 매퍼 (er.tn_cef).
 * SQL: {@code mapper/er/cef/CefMapper.xml}.
 */
@Mapper
public interface CefMapper {

    // ── 조회 ──
    List<CefVO> selectCefs(CefSearch search);
    long countCefs(CefSearch search);
    CefVO selectByCefId(@Param("cefId") String cefId);
    CefVO selectByErId(@Param("erId") String erId);

    // ── 채번 ──
    int countByPrefix(@Param("prefix") String prefix);

    // ── 등록 / 수정 / 삭제 ──
    int insertCef(CefVO vo);
    int updateCef(CefVO vo);
    int softDeleteCef(@Param("cefId") String cefId, @Param("userId") String userId);

    // ── 합계 재계산 ──
    /**
     * 청구건의 pure_fuel_mass 합계 조회 (감축량 산출 기초값).
     * 1차: 단순 합계. LCA 정교화는 SFR-018 후속.
     */
    BigDecimal sumClaimMass(@Param("cefId") String cefId);

    /**
     * 마스터 ttl_redu_amt 직접 갱신.
     */
    int updateTtlReduAmt(@Param("cefId") String cefId,
                         @Param("ttlReduAmt") BigDecimal ttlReduAmt,
                         @Param("userId") String userId);

    // ── 상태 전이 ──
    int updateSubmit(@Param("cefId") String cefId, @Param("userId") String userId);
    int updateCefStCd(@Param("cefId") String cefId,
                      @Param("newSt") String newSt,
                      @Param("userId") String userId);
    int updateApprove(@Param("cefId") String cefId, @Param("userId") String userId);
    int updateCancel(@Param("cefId") String cefId,
                     @Param("reason") String reason,
                     @Param("userId") String userId);
}
