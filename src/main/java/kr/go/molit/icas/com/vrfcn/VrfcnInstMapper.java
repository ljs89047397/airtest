package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 검증기관(com.tn_vrfcn_inst) MyBatis Mapper 인터페이스.
 * SQL 은 VrfcnInstMapper.xml 에서 관리.
 */
@Mapper
public interface VrfcnInstMapper {

    /** 유효한 검증기관 전체 조회 */
    List<VrfcnInstVO> selectAll();

    /** 검증기관 ID 로 단건 조회 (유효구간 포함) */
    VrfcnInstVO selectByVrfcnInstId(@Param("vrfcnInstId") String vrfcnInstId);

    /** 검증기관 등록 */
    int insertVrfcnInst(VrfcnInstVO vo);

    /** 검증기관 수정 */
    int updateVrfcnInst(VrfcnInstVO vo);

    /** 검증기관 소프트삭제 (use_end_dt = NOW() - 1 minute) */
    int softDeleteVrfcnInst(@Param("vrfcnInstId") String vrfcnInstId,
                            @Param("userId")      String userId);

    /**
     * 채번용 — 동일 prefix 의 현재 최대 순번 반환.
     * 예) prefix = "VI" → MAX(CAST(SUBSTRING(vrfcn_inst_id, 3) AS INT))
     */
    int countByPrefix(@Param("prefix") String prefix);
}
