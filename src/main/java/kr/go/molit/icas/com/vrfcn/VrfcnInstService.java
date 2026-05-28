package kr.go.molit.icas.com.vrfcn;

import kr.go.molit.icas.com.ognz.OgnzService;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 검증기관 비즈니스 서비스.
 *
 * <ul>
 *   <li>읽기 전용 기본 트랜잭션 (클래스 레벨)</li>
 *   <li>변경 메서드는 {@code @Transactional} 오버라이드</li>
 *   <li>권한 분기는 Controller 에서 처리 후 Service 진입</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrfcnInstService {

    private static final String ID_PREFIX = "VI";

    private final VrfcnInstMapper vrfcnInstMapper;
    private final IdGenerator     idGenerator;
    private final OgnzService     ognzService;

    /**
     * 유효한 검증기관 전체 목록 조회.
     */
    public List<VrfcnInstVO> selectAll() {
        return vrfcnInstMapper.selectAll();
    }

    /**
     * 검증기관 단건 조회.
     *
     * @throws BusinessException NOT_FOUND — 존재하지 않거나 유효기간 만료
     */
    public VrfcnInstVO selectByVrfcnInstId(String vrfcnInstId) {
        VrfcnInstVO vo = vrfcnInstMapper.selectByVrfcnInstId(vrfcnInstId);
        if (vo == null) throw BusinessException.notFound("검증기관");
        return vo;
    }

    /**
     * 검증기관 등록.
     * <ul>
     *   <li>채번: VI + 4자리 순번 (VIxxxx)</li>
     *   <li>icaoCcrAccrdYn 값 검증: 'Y' 또는 'N' 만 허용</li>
     * </ul>
     *
     * @throws BusinessException BAD_REQUEST — 필수 값 누락 또는 icaoCcrAccrdYn 형식 오류
     */
    @Transactional
    public VrfcnInstVO createVrfcnInst(VrfcnInstVO vo, IcasUser user) {
        // 필수 값 검증
        if (vo.getOgnzId() == null || vo.getOgnzId().isBlank()) {
            throw BusinessException.badRequest("조직 ID 는 필수입니다.");
        }
        if (vo.getVrfcnInstNm() == null || vo.getVrfcnInstNm().isBlank()) {
            throw BusinessException.badRequest("검증기관명(한글)은 필수입니다.");
        }
        if (vo.getVrfcnInstNmEn() == null || vo.getVrfcnInstNmEn().isBlank()) {
            throw BusinessException.badRequest("검증기관명(영문)은 필수입니다.");
        }

        // ognz_se_cd = VERIFIER 검증
        ognzService.requireOgnzOfType(vo.getOgnzId(), "VERIFIER");

        // icaoCcrAccrdYn 검증
        validateIcaoCcrAccrdYn(vo.getIcaoCcrAccrdYn());

        // 채번: VI + 4자리 (최대 순번 + 1)
        int nextSeq = vrfcnInstMapper.countByPrefix(ID_PREFIX) + 1;
        String newId = idGenerator.managementPk(ID_PREFIX, nextSeq);
        vo.setVrfcnInstId(newId);

        // 감사 필드 설정
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        vrfcnInstMapper.insertVrfcnInst(vo);
        return vo;
    }

    /**
     * 검증기관 수정.
     *
     * @throws BusinessException NOT_FOUND — 해당 검증기관이 없거나 만료
     * @throws BusinessException BAD_REQUEST — icaoCcrAccrdYn 형식 오류
     */
    @Transactional
    public void updateVrfcnInst(String vrfcnInstId, VrfcnInstVO vo, IcasUser user) {
        // 존재 여부 확인
        VrfcnInstVO existing = vrfcnInstMapper.selectByVrfcnInstId(vrfcnInstId);
        if (existing == null) throw BusinessException.notFound("검증기관");

        // ognz_se_cd = VERIFIER 검증 (부분 수정 허용 — null 이면 skip)
        if (vo.getOgnzId() != null && !vo.getOgnzId().isBlank()) {
            ognzService.requireOgnzOfType(vo.getOgnzId(), "VERIFIER");
        }

        // icaoCcrAccrdYn 검증
        validateIcaoCcrAccrdYn(vo.getIcaoCcrAccrdYn());

        vo.setVrfcnInstId(vrfcnInstId);
        vo.setLastChgUserId(user.getUserId());

        int affected = vrfcnInstMapper.updateVrfcnInst(vo);
        if (affected == 0) throw BusinessException.notFound("검증기관");
    }

    /**
     * 검증기관 소프트삭제.
     *
     * @throws BusinessException NOT_FOUND — 해당 검증기관이 없거나 이미 삭제됨
     */
    @Transactional
    public void softDeleteVrfcnInst(String vrfcnInstId, IcasUser user) {
        int affected = vrfcnInstMapper.softDeleteVrfcnInst(vrfcnInstId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("검증기관");
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    private void validateIcaoCcrAccrdYn(String value) {
        if (value == null || (!value.equals("Y") && !value.equals("N"))) {
            throw BusinessException.badRequest("ICAO CCR 공인 여부는 'Y' 또는 'N' 만 허용됩니다.");
        }
    }
}
