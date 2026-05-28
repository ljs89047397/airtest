package kr.go.molit.icas.com.user;

import kr.go.molit.icas.com.user.domain.UserSearch;
import kr.go.molit.icas.com.user.domain.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * com.tn_user 매퍼.
 * SQL 은 UserMapper.xml 에 정의 (어노테이션 SQL 금지).
 */
@Mapper
public interface UserMapper {

    /** 검색 조건 + 페이징으로 사용자 목록 조회 */
    List<UserVO> selectUsers(UserSearch search);

    /** 검색 조건 총 건수 조회 (페이징용) */
    long countUsers(UserSearch search);

    /** 단건 조회 — ognz_se_cd JOIN 포함, 유효구간 필터 */
    UserVO selectByUserId(@Param("userId") String userId);

    /** 사용자 등록 */
    int insertUser(UserVO vo);

    /** 기본정보 수정 (비밀번호 제외) */
    int updateUser(UserVO vo);

    /** 소프트 삭제 */
    int softDeleteUser(@Param("userId") String userId, @Param("operatorId") String operatorId);

    /** 비밀번호 변경 */
    int changePassword(@Param("userId") String userId,
                       @Param("pswdHash") String pswdHash,
                       @Param("operatorId") String operatorId);

    /** 계정 잠금 해제 + 실패 횟수 초기화 */
    int unlockAccount(@Param("userId") String userId, @Param("operatorId") String operatorId);
}
