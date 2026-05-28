package kr.go.molit.icas.com.authrt;

import kr.go.molit.icas.com.authrt.domain.AuthrtVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthrtMapper {

    /** 유효한 권한 전체 목록 조회 */
    List<AuthrtVO> selectAuthrts();

    /** 단건 조회 (유효구간 필터) */
    AuthrtVO selectAuthrt(@Param("authrtId") String authrtId);

    /** authrt_id 중복 체크 (유효·만료 포함 전체) */
    boolean existsAuthrtId(@Param("authrtId") String authrtId);

    /** 권한 등록 */
    int insertAuthrt(AuthrtVO vo);

    /** 권한 수정 */
    int updateAuthrt(AuthrtVO vo);

    /** 권한 소프트 삭제 (use_end_dt = NOW() - 1분) */
    int softDeleteAuthrt(@Param("authrtId") String authrtId, @Param("userId") String userId);
}
