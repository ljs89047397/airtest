package kr.go.molit.icas.er.oom.item;

import kr.go.molit.icas.er.oom.item.domain.OomCheckItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OoM 점검 항목 매퍼 (er.tn_oom_check_item).
 * SQL: {@code mapper/er/oom/item/OomCheckItemMapper.xml}.
 */
@Mapper
public interface OomCheckItemMapper {

    List<OomCheckItemVO> selectByOomId(@Param("oomId") String oomId);

    OomCheckItemVO selectOne(@Param("oomId") String oomId,
                              @Param("itemNo") int itemNo);

    int selectNextSn(@Param("oomId") String oomId,
                     @Param("startFrom") int startFrom);

    int insertItem(OomCheckItemVO vo);
    int updateItem(OomCheckItemVO vo);
    int softDeleteOne(@Param("oomId") String oomId,
                      @Param("itemNo") int itemNo,
                      @Param("userId") String userId);

    /**
     * 18종 자동 검증 재실행 전 1~18 항목 일괄 만료 (사용자 추가 100+ 항목은 보존).
     */
    int softDeleteAutoItems(@Param("oomId") String oomId,
                            @Param("userId") String userId);

    /** PASS/WARN/FAIL 별 건수 — 종합 결과 산출용 */
    int countByJudg(@Param("oomId") String oomId,
                    @Param("judgCd") String judgCd);
}
