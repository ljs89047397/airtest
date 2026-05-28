package kr.go.molit.icas.ptl.ccr;

import kr.go.molit.icas.ptl.ccr.domain.PtlCcrExtrSearch;
import kr.go.molit.icas.ptl.ccr.domain.PtlCcrExtrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PtlCcrExtrMapper {
    /** 단건 조회 */
    PtlCcrExtrVO selectByExtrId(String extrId);

    /** 목록 조회 (검색 + 페이징) */
    List<PtlCcrExtrVO> selectExtrs(PtlCcrExtrSearch search);

    /** 총 건수 (페이징용) */
    int countExtrs(PtlCcrExtrSearch search);

    /** 채번: CE prefix 다음 순번 */
    int countByPrefix();

    /** 신규 추출 이력 등록 */
    void insertExtr(PtlCcrExtrVO vo);

    /** 상태 및 파일 ID 갱신 (DONE/FAIL 처리 시) */
    void updateStatus(@Param("extrId") String extrId,
                      @Param("extrStCd") String extrStCd,
                      @Param("fileId") String fileId,
                      @Param("lastChgUserId") String lastChgUserId);

    /** 소프트 삭제 */
    void softDeleteExtr(@Param("extrId") String extrId, @Param("userId") String userId);
}
