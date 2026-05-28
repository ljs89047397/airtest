package kr.go.molit.icas.vr;

import kr.go.molit.icas.vr.domain.VrSearch;
import kr.go.molit.icas.vr.domain.VrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VrMapper {

    VrVO selectByVrId(@Param("vrId") String vrId);

    VrVO selectByUk(@Param("oprtrId") String oprtrId,
                    @Param("rprtYr")  String rprtYr,
                    @Param("vrTypeCd") String vrTypeCd,
                    @Param("vrVer")   int    vrVer);

    /** 최신 버전 조회 (최대 vr_ver) */
    VrVO selectLatest(@Param("oprtrId") String oprtrId,
                      @Param("rprtYr")  String rprtYr,
                      @Param("vrTypeCd") String vrTypeCd);

    long countVrs(VrSearch search);
    List<VrVO> selectVrs(VrSearch search);

    int countByPrefix(@Param("prefix") String prefix);

    int insertVr(VrVO vo);

    int updateVr(VrVO vo);

    int updateSubmit(@Param("vrId")     String vrId,
                     @Param("userId")   String userId);

    int updateRecommend(@Param("vrId")   String vrId,
                        @Param("userId") String userId);

    int updateApprove(@Param("vrId")   String vrId,
                      @Param("userId") String userId);

    int updateReject(@Param("vrId")    String vrId,
                     @Param("rjctRsn") String rjctRsn,
                     @Param("userId")  String userId);

    /** DRAFT 로 버전 올려 신규 행 삽입 (재작성) */
    int insertNewVersion(VrVO vo);

    int softDeleteVr(@Param("vrId")   String vrId,
                     @Param("userId") String userId);
}
