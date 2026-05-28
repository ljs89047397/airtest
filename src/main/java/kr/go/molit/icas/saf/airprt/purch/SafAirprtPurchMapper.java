package kr.go.molit.icas.saf.airprt.purch;

import kr.go.molit.icas.saf.airprt.purch.domain.SafAirprtPurchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SafAirprtPurchMapper {
    SafAirprtPurchVO selectByPk(@Param("airprtId") String airprtId,
                                @Param("rprtYr")   String rprtYr,
                                @Param("oprtrId")  String oprtrId,
                                @Param("purchSn")  int    purchSn);
    List<SafAirprtPurchVO> selectByOprtrYr(@Param("oprtrId") String oprtrId,
                                           @Param("rprtYr")  String rprtYr);
    int maxPurchSn(@Param("airprtId") String airprtId,
                   @Param("rprtYr")   String rprtYr,
                   @Param("oprtrId")  String oprtrId);
    int insertPurch(SafAirprtPurchVO vo);
    int updatePurch(SafAirprtPurchVO vo);
    int deleteByPk(@Param("airprtId") String airprtId,
                   @Param("rprtYr")   String rprtYr,
                   @Param("oprtrId")  String oprtrId,
                   @Param("purchSn")  int    purchSn);
    /** 연간 SAF 구매량 합산 (혼합비율 계산용) */
    BigDecimal sumPurchQty(@Param("oprtrId") String oprtrId,
                           @Param("rprtYr")  String rprtYr);
}
