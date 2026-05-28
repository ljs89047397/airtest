package kr.go.molit.icas.ptl.actn;

import kr.go.molit.icas.ptl.actn.domain.UserActnSearch;
import kr.go.molit.icas.ptl.actn.domain.UserActnVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * TH_ 이력 테이블 — 수정/삭제 없음, 조회 + 삽입만.
 */
@Mapper
public interface UserActnMapper {
    /** 목록 조회 (검색 + 페이징) */
    List<UserActnVO> selectActns(UserActnSearch search);

    /** 총 건수 (페이징용) */
    int countActns(UserActnSearch search);

    /** 행위 이력 등록 (bigserial 자동 채번) */
    void insertActn(UserActnVO vo);
}
