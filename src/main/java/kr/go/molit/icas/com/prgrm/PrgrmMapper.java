package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.PrgrmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrgrmMapper {

    /** 전체 또는 sysSeCd 필터 목록 조회 */
    List<PrgrmVO> selectPrgrms(@Param("sysSeCd") String sysSeCd);

    /** 단건 조회 */
    PrgrmVO selectPrgrm(@Param("prgrmId") String prgrmId);

    /** ID 중복 체크 */
    boolean existsPrgrm(@Param("prgrmId") String prgrmId);

    /** 등록 */
    int insertPrgrm(PrgrmVO vo);

    /** 수정 */
    int updatePrgrm(PrgrmVO vo);

    /** 소프트 삭제 */
    int softDeletePrgrm(@Param("prgrmId") String prgrmId, @Param("userId") String userId);
}
