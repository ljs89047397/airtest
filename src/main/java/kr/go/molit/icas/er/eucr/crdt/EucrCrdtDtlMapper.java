package kr.go.molit.icas.er.eucr.crdt;

import kr.go.molit.icas.er.eucr.crdt.domain.EucrCrdtDtlVO;
import kr.go.molit.icas.er.eucr.validate.domain.CrdtConflictRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EUCR 일련번호 상세 매퍼 (er.tn_eucr_crdt_dtl).
 * SQL: {@code mapper/er/eucr/crdt/EucrCrdtDtlMapper.xml}.
 *
 * <p>전역 UK: crdt_no — DB 레벨 이중사용 차단.
 * 일괄 등록 시 사전 충돌 검사 메서드 제공.
 */
@Mapper
public interface EucrCrdtDtlMapper {

    List<EucrCrdtDtlVO> selectByEucrId(@Param("eucrId") String eucrId);

    List<EucrCrdtDtlVO> selectByBatch(@Param("eucrId") String eucrId,
                                       @Param("batchNo") String batchNo);

    EucrCrdtDtlVO selectOne(@Param("eucrId") String eucrId,
                            @Param("crdtNo") String crdtNo);

    int insertCrdt(EucrCrdtDtlVO vo);
    int softDeleteOne(@Param("eucrId") String eucrId,
                      @Param("crdtNo") String crdtNo,
                      @Param("userId") String userId);

    /**
     * 이중사용 교차 스캔.
     *
     * <p>입력 crdt_no 리스트 중 자기 EUCR 제외하고 다른 EUCR 에 이미 점유된 항목 반환.
     *
     * @param crdtNos     검사 일련번호 목록 (빈 리스트 안전 — empty 시 빈 결과)
     * @param excludeEucrId 제외할 EUCR ID (수정/추가 시 자기 자신 — null 허용)
     * @return 충돌 행 목록
     */
    List<CrdtConflictRow> findConflicts(@Param("crdtNos") List<String> crdtNos,
                                         @Param("excludeEucrId") String excludeEucrId);
}
