package kr.go.molit.icas.ptl.stat;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PtlStatYearlyMapper {
    /** PK 단건 조회 */
    PtlStatYearlyVO selectByPk(@Param("rprtYr") String rprtYr, @Param("oprtrId") String oprtrId);

    /** 연도별 전 운영사 목록 (JOIN tn_oprtr — oprtr_nm, icao_desig 포함) */
    List<PtlStatYearlyVO> selectByRprtYr(@Param("rprtYr") String rprtYr);

    /** 특정 운영사의 다년 추이 */
    List<PtlStatYearlyVO> selectByOprtr(@Param("oprtrId") String oprtrId);

    /** 배치 갱신: 없으면 INSERT, 있으면 UPDATE (PostgreSQL ON CONFLICT) */
    void upsertStat(PtlStatYearlyVO vo);
}
