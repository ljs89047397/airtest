package kr.go.molit.icas.er.rprt;

import kr.go.molit.icas.er.rprt.domain.ErSearch;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ER(Emission Report) 마스터 MyBatis 매퍼.
 * SQL 은 {@code mapper/er/rprt/ErMapper.xml} 에 위치.
 */
@Mapper
public interface ErMapper {

    // ── 조회 ──────────────────────────────────────────────────────

    /**
     * 검색 조건 기반 목록 조회 (페이징).
     * 가시범위 SQL 분기: verifierScope=true 시 vrfcn_assgn JOIN 적용.
     */
    List<ErVO> selectErs(ErSearch search);

    /** 검색 조건 기반 전체 건수 (페이징용) */
    long countErs(ErSearch search);

    /** 단건 조회 (유효구간 필터 포함) */
    ErVO selectByErId(@Param("erId") String erId);

    /**
     * er_id 채번: ER prefix 최대 순번 조회.
     * (예: MAX(CAST(SUBSTRING(er_id, 3) AS INT)) → 없으면 0)
     */
    int countByPrefix(@Param("prefix") String prefix);

    /**
     * 같은 (oprtr_id, rprt_yr) 의 최대 er_ver (정수 변환 가능 문자열).
     * NULL 이면 등록 이력 없음.
     */
    String selectMaxErVer(@Param("oprtrId") String oprtrId,
                          @Param("rprtYr") String rprtYr);

    // ── 등록/수정 ──────────────────────────────────────────────────

    /** DRAFT 신규 등록 */
    int insertEr(ErVO vo);

    /** DRAFT 상태 마스터 수정 (상태 DRAFT 조건 포함) */
    int updateEr(ErVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute' (DRAFT 한정, SQL 에서 상태 조건 포함).
     */
    int softDeleteEr(@Param("erId") String erId,
                     @Param("userId") String userId);

    // ── 상태 전이 ─────────────────────────────────────────────────

    /** 제출: er_st_cd = SBMTD + sbmt_dt = NOW() */
    int updateSubmit(@Param("erId") String erId,
                     @Param("userId") String userId);

    /**
     * 상태 코드만 변경 (review: SBMTD→RVWNG / recommend: RVWNG→RCMDD).
     * 사유 없는 단순 전이용 통합 메서드.
     */
    int updateErStCd(@Param("erId") String erId,
                     @Param("newSt") String newSt,
                     @Param("userId") String userId);

    /** 반려: er_st_cd = DRAFT + rjct_dt / rjct_rsn 기록 */
    int updateReject(@Param("erId") String erId,
                     @Param("reason") String reason,
                     @Param("userId") String userId);

    /** 승인: er_st_cd = APRVD + aprv_dt / aprv_user_id 기록 (RVWNG 또는 RCMDD 상태) */
    int updateApprove(@Param("erId") String erId,
                      @Param("userId") String userId);

    /** 취소: er_st_cd = CNCLD + rjct_rsn 기록 */
    int updateCancel(@Param("erId") String erId,
                     @Param("reason") String reason,
                     @Param("userId") String userId);
}
