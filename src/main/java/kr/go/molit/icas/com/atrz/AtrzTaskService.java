package kr.go.molit.icas.com.atrz;

import kr.go.molit.icas.com.atrz.domain.AtrzTaskVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 결재 업무 마스터 서비스 (com.tn_atrz_task).
 *
 * <ul>
 *   <li>MOLIT / KOTSA 사용자만 CUD 가능</li>
 *   <li>sys_se_cd 화이트리스트 검증</li>
 *   <li>atrz_task_id 중복 체크</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AtrzTaskService {

    /** sys_se_cd 허용 값 집합 */
    private static final Set<String> ALLOWED_SYS_SE_CD =
            Set.of("COM", "EMP", "ER", "VR", "SAF", "PTL");

    private final AtrzTaskMapper atrzTaskMapper;

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    /**
     * 유효한 결재 업무 목록 조회.
     *
     * @param sysSeCd 시스템 구분 코드 필터 (null/blank 면 전체 조회)
     * @return 결재 업무 목록
     */
    public List<AtrzTaskVO> selectAtrzTasks(String sysSeCd) {
        if (sysSeCd != null && !sysSeCd.isBlank()) {
            assertValidSysSeCd(sysSeCd);
        }
        return atrzTaskMapper.selectAtrzTasks(sysSeCd);
    }

    /**
     * 결재 업무 단건 조회.
     *
     * @param atrzTaskId 결재 업무 ID
     * @return 결재 업무 VO
     * @throws BusinessException NOT_FOUND — 존재하지 않거나 유효기간 만료
     */
    public AtrzTaskVO selectByTaskId(String atrzTaskId) {
        AtrzTaskVO vo = atrzTaskMapper.selectByTaskId(atrzTaskId);
        if (vo == null) throw BusinessException.notFound("결재 업무");
        return vo;
    }

    // ──────────────────────────────────────────────
    // 생성
    // ──────────────────────────────────────────────

    /**
     * 결재 업무 등록.
     * MOLIT / KOTSA 전용.
     *
     * @param vo   등록 VO (atrzTaskId 수동 입력)
     * @param user 로그인 사용자
     * @return 등록된 VO
     * @throws BusinessException BAD_REQUEST  — 필수값 누락 / sys_se_cd 오류 / ID 중복
     * @throws BusinessException FORBIDDEN    — MOLIT/KOTSA 외 접근
     */
    @Transactional
    public AtrzTaskVO createAtrzTask(AtrzTaskVO vo, IcasUser user) {
        assertMolitOrKotsa(user);

        // 필수 값 검증
        if (vo.getAtrzTaskId() == null || vo.getAtrzTaskId().isBlank()) {
            throw BusinessException.badRequest("결재 업무 ID 는 필수입니다.");
        }
        if (vo.getAtrzTaskNm() == null || vo.getAtrzTaskNm().isBlank()) {
            throw BusinessException.badRequest("결재 업무명은 필수입니다.");
        }
        assertValidSysSeCd(vo.getSysSeCd());

        // ID 중복 체크 (유효구간 무관)
        if (atrzTaskMapper.existsAtrzTaskId(vo.getAtrzTaskId())) {
            throw BusinessException.conflict("이미 사용 중인 결재 업무 ID 입니다: " + vo.getAtrzTaskId());
        }

        vo.setFrstRegUserId(user.getUsername());
        vo.setLastChgUserId(user.getUsername());

        atrzTaskMapper.insertAtrzTask(vo);
        return vo;
    }

    // ──────────────────────────────────────────────
    // 수정
    // ──────────────────────────────────────────────

    /**
     * 결재 업무 수정.
     * MOLIT / KOTSA 전용.
     *
     * @param atrzTaskId 결재 업무 ID
     * @param vo         수정 VO
     * @param user       로그인 사용자
     * @throws BusinessException NOT_FOUND   — 존재하지 않거나 유효기간 만료
     * @throws BusinessException BAD_REQUEST  — sys_se_cd 오류
     * @throws BusinessException FORBIDDEN   — MOLIT/KOTSA 외 접근
     */
    @Transactional
    public void updateAtrzTask(String atrzTaskId, AtrzTaskVO vo, IcasUser user) {
        assertMolitOrKotsa(user);

        // 존재 여부 확인
        AtrzTaskVO existing = atrzTaskMapper.selectByTaskId(atrzTaskId);
        if (existing == null) throw BusinessException.notFound("결재 업무");

        assertValidSysSeCd(vo.getSysSeCd());

        vo.setAtrzTaskId(atrzTaskId);
        vo.setLastChgUserId(user.getUsername());

        int affected = atrzTaskMapper.updateAtrzTask(vo);
        if (affected == 0) throw BusinessException.notFound("결재 업무");
    }

    // ──────────────────────────────────────────────
    // 소프트삭제
    // ──────────────────────────────────────────────

    /**
     * 결재 업무 소프트삭제.
     * MOLIT / KOTSA 전용.
     *
     * @param atrzTaskId 결재 업무 ID
     * @param user       로그인 사용자
     * @throws BusinessException NOT_FOUND — 존재하지 않거나 이미 삭제됨
     * @throws BusinessException FORBIDDEN — MOLIT/KOTSA 외 접근
     */
    @Transactional
    public void softDeleteAtrzTask(String atrzTaskId, IcasUser user) {
        assertMolitOrKotsa(user);
        int affected = atrzTaskMapper.softDeleteAtrzTask(atrzTaskId, user.getUsername());
        if (affected == 0) throw BusinessException.notFound("결재 업무");
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    private void assertMolitOrKotsa(IcasUser user) {
        if (!user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("국토부 또는 한국교통안전공단 사용자만 결재 업무를 관리할 수 있습니다.");
        }
    }

    private void assertValidSysSeCd(String sysSeCd) {
        if (sysSeCd == null || !ALLOWED_SYS_SE_CD.contains(sysSeCd)) {
            throw BusinessException.badRequest(
                    "sys_se_cd 는 COM / EMP / ER / VR / SAF / PTL 중 하나여야 합니다. 입력값: " + sysSeCd);
        }
    }
}
