package kr.go.molit.icas.saf.airprt.fuel;

import kr.go.molit.icas.saf.airprt.fuel.domain.SafAirprtFuelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafAirprtFuelMapper {
    SafAirprtFuelVO selectByPk(@Param("airprtId") String airprtId,
                               @Param("rprtYr")   String rprtYr,
                               @Param("oprtrId")  String oprtrId);
    List<SafAirprtFuelVO> selectByOprtrYr(@Param("oprtrId") String oprtrId,
                                          @Param("rprtYr")  String rprtYr);
    int insertFuel(SafAirprtFuelVO vo);
    int updateFuel(SafAirprtFuelVO vo);
    int softDeleteFuel(@Param("airprtId") String airprtId,
                       @Param("rprtYr")   String rprtYr,
                       @Param("oprtrId")  String oprtrId,
                       @Param("userId")   String userId);
    /** 연간 총 급유량 합산 (혼합비율 계산용) */
    java.math.BigDecimal sumActlFuelQty(@Param("oprtrId") String oprtrId,
                                        @Param("rprtYr")  String rprtYr);
}
