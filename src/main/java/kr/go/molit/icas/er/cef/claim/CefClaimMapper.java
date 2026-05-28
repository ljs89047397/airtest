package kr.go.molit.icas.er.cef.claim;

import kr.go.molit.icas.er.cef.claim.domain.CefClaimVO;
import kr.go.molit.icas.er.cef.validate.domain.BatchConflictRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CEF 청구건 MyBatis 매퍼 (er.tn_cef_claim).
 * SQL: {@code mapper/er/cef/claim/CefClaimMapper.xml}.
 *
 * <p>복합 PK: (cef_id, claim_no). claim_no 는 사용자 입력 (배치ID 유래 식별자).
 * 이중청구 교차 스캔(SFR-021) 용 메서드 포함.
 */
@Mapper
public interface CefClaimMapper {

    // ── 조회 ──
    List<CefClaimVO> selectByCefId(@Param("cefId") String cefId);
    CefClaimVO selectOne(@Param("cefId") String cefId,
                         @Param("claimNo") String claimNo);

    /** 같은 cef_id 내 동일 claim_no 존재 여부 (입력 중복 방지) */
    boolean existsClaimNo(@Param("cefId") String cefId,
                          @Param("claimNo") String claimNo);

    // ── 등록 / 수정 / 삭제 ──
    int insertClaim(CefClaimVO vo);
    int updateClaim(CefClaimVO vo);
    int softDeleteOne(@Param("cefId") String cefId,
                      @Param("claimNo") String claimNo,
                      @Param("userId") String userId);

    // ── 이중청구 교차 스캔 (SFR-021) ──

    /**
     * 동일 batch_id_no 를 사용하는 CEF 청구건 전체 조회.
     *
     * <p>제외 키 (현재 청구건) 를 받아 자기 자신을 결과에서 제외.
     *
     * @param batchIdNo      검색 대상 배치 ID
     * @param excludeCefId   제외할 cef_id (현재 청구건의 cef_id)
     * @param excludeClaimNo 제외할 claim_no
     * @return 동일 batch_id_no 를 사용하는 다른 CEF 청구건 목록
     */
    List<BatchConflictRow> findCefBatchConflicts(@Param("batchIdNo") String batchIdNo,
                                                 @Param("excludeCefId") String excludeCefId,
                                                 @Param("excludeClaimNo") String excludeClaimNo);

    /**
     * 동일 batch_id 를 가진 SAF 배치 조회 (saf.tn_saf_batch.batch_id).
     *
     * <p>CEF 의 batch_id_no 와 SAF 의 batch_id 는 동일 식별자 체계로 가정.
     */
    List<BatchConflictRow> findSafBatchConflicts(@Param("batchIdNo") String batchIdNo);
}
