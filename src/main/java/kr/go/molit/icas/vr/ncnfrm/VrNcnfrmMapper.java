package kr.go.molit.icas.vr.ncnfrm;

import kr.go.molit.icas.vr.ncnfrm.domain.VrNcnfrmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VrNcnfrmMapper {
    List<VrNcnfrmVO> selectByVrId(@Param("vrId") String vrId);
    VrNcnfrmVO selectByPk(@Param("vrId") String vrId, @Param("itemNo") int itemNo);
    int maxItemNo(@Param("vrId") String vrId);
    int insertNcnfrm(VrNcnfrmVO vo);
    int updateNcnfrm(VrNcnfrmVO vo);
    int updateResolve(@Param("vrId")       String vrId,
                      @Param("itemNo")     int    itemNo,
                      @Param("resolDescCn") String resolDescCn,
                      @Param("resolDt")   String resolDt,
                      @Param("userId")    String userId);
    int deleteByPk(@Param("vrId") String vrId, @Param("itemNo") int itemNo);
    int deleteByVrId(@Param("vrId") String vrId);
    /** 미해결 부적합 건수 — REASONABLE 차단 판단용 */
    int countUnresolved(@Param("vrId") String vrId);
}
