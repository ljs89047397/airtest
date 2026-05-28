package kr.go.molit.icas.er.rprt.vrfr;

import kr.go.molit.icas.er.rprt.vrfr.domain.ErVrfrInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ER 참여 검증기관 정보 MyBatis 매퍼 (er.tn_er_vrfr_info).
 *
 * <p>SQL 은 {@code mapper/er/rprt/vrfr/ErVrfrInfoMapper.xml} 에 위치.
 */
@Mapper
public interface ErVrfrInfoMapper {

    /**
     * er_id 기준 전체 목록 조회 (vrfr_sn ASC, 유효구간 필터 포함).
     *
     * @param erId ER ID
     * @return 검증기관 정보 목록
     */
    List<ErVrfrInfoVO> selectByErId(@Param("erId") String erId);

    /**
     * 단건 조회 (복합 PK, 유효구간 필터 포함).
     *
     * @param erId   ER ID
     * @param vrfrSn 검증기관 일련번호
     * @return 검증기관 정보 VO, 없으면 null
     */
    ErVrfrInfoVO selectOne(@Param("erId") String erId,
                           @Param("vrfrSn") int vrfrSn);

    /**
     * 다음 vrfr_sn 채번: 같은 er_id 의 max(vrfr_sn) + 1.
     * 행이 없으면 1 반환.
     *
     * @param erId ER ID
     * @return 다음 일련번호
     */
    int selectNextSn(@Param("erId") String erId);

    /**
     * 같은 er_id 에 같은 vrfcn_inst_id 가 이미 존재하는지 확인 (중복 방지).
     * 수정 시 자기 자신 제외를 위해 excludeSn 파라미터 사용 (신규 시 excludeSn=0 전달).
     *
     * @param erId        ER ID
     * @param vrfcnInstId 검증기관 ID
     * @param excludeSn   제외할 일련번호 (신규 시 0)
     * @return 중복 존재 시 true
     */
    boolean existsByVrfcnInst(@Param("erId") String erId,
                               @Param("vrfcnInstId") String vrfcnInstId,
                               @Param("excludeSn") int excludeSn);

    /**
     * 신규 검증기관 정보 등록.
     *
     * @param vo 등록 데이터 (erId, vrfrSn, vrfcnInstId 필수)
     * @return 영향 행 수 (정상 1)
     */
    int insertVrfrInfo(ErVrfrInfoVO vo);

    /**
     * 검증기관 정보 수정 (유효구간 필터 포함).
     *
     * @param vo 수정 데이터 (erId, vrfrSn 필수)
     * @return 영향 행 수 (정상 1)
     */
    int updateVrfrInfo(ErVrfrInfoVO vo);

    /**
     * 소프트삭제: use_end_dt = NOW() - INTERVAL '1 minute'.
     *
     * @param erId   ER ID
     * @param vrfrSn 검증기관 일련번호
     * @param userId 수행 사용자 ID
     * @return 영향 행 수 (정상 1)
     */
    int softDeleteOne(@Param("erId") String erId,
                      @Param("vrfrSn") int vrfrSn,
                      @Param("userId") String userId);

    /**
     * 신버전 생성 시 자식 데이터 복사 (src → dst).
     *
     * @param srcErId 복사 원본 ER ID
     * @param dstErId 복사 대상 ER ID
     * @param userId  복사 수행 사용자 ID
     * @return 영향 행 수 (복사된 건수)
     */
    int copyToNewEr(@Param("srcErId") String srcErId,
                    @Param("dstErId") String dstErId,
                    @Param("userId") String userId);
}
