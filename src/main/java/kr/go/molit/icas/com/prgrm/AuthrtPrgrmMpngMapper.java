package kr.go.molit.icas.com.prgrm;

import kr.go.molit.icas.com.prgrm.domain.AuthrtPrgrmMpngVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthrtPrgrmMpngMapper {

    /** 특정 권한의 프로그램 매핑 목록 */
    List<AuthrtPrgrmMpngVO> selectByAuthrt(@Param("authrtId") String authrtId);

    /** 특정 프로그램의 권한 매핑 목록 */
    List<AuthrtPrgrmMpngVO> selectByPrgrm(@Param("prgrmId") String prgrmId);

    /** 단건 조회 (upsert 판단용) */
    AuthrtPrgrmMpngVO selectOne(@Param("authrtId") String authrtId,
                                @Param("prgrmId") String prgrmId);

    /** 신규 매핑 등록 */
    int insertMapping(AuthrtPrgrmMpngVO vo);

    /** 기존 매핑 권한 갱신 */
    int updateMapping(AuthrtPrgrmMpngVO vo);

    /** 소프트 삭제 */
    int softDeleteMapping(@Param("authrtId") String authrtId,
                          @Param("prgrmId") String prgrmId,
                          @Param("userId") String userId);
}
