package kr.go.molit.icas.com.oprtr;

import kr.go.molit.icas.com.ognz.OgnzService;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OprtrService {

    private final OprtrMapper oprtrMapper;
    private final IdGenerator idGenerator;
    private final OgnzService ognzService;

    /** 목록 조회 — 역할별 가시범위 분기는 Controller 에서 처리 후 이 메서드 또는 selectAccessibleForUser 호출 */
    public List<OprtrVO> selectAll() {
        return oprtrMapper.selectAll();
    }

    /** VERIFIER 가시범위 목록 */
    public List<OprtrVO> selectAccessibleForUser(String ognzSeCd, String oprtrId,
                                                  String vrfcnInstId, String rprtYr) {
        return oprtrMapper.selectAccessibleForUser(ognzSeCd, oprtrId, vrfcnInstId, rprtYr);
    }

    /** 단건 조회 — 가시범위 검증 포함 */
    public OprtrVO selectByOprtrId(String oprtrId, IcasUser user) {
        OprtrVO vo = oprtrMapper.selectByOprtrId(oprtrId);
        if (vo == null) throw BusinessException.notFound("항공기 운영사");
        assertReadable(user, oprtrId);
        return vo;
    }

    /** 운영사 등록 (MOLIT/KOTSA 전용) */
    @Transactional
    public OprtrVO insert(OprtrVO vo, IcasUser user) {
        validateRequired(vo);
        ognzService.requireOgnzOfType(vo.getOgnzId(), "AIRLINE");
        validateIcaoLength(vo.getIcaoDesig());

        // ICAO 지정어 중복 사전 체크
        if (oprtrMapper.countByIcaoDesig(vo.getIcaoDesig()) > 0) {
            throw BusinessException.conflict("이미 등록된 ICAO 지정어입니다.");
        }

        // ID 채번: OP + 4자리
        int seq = oprtrMapper.countByPrefix("OP") + 1;
        vo.setOprtrId(idGenerator.managementPk("OP", seq));

        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());
        oprtrMapper.insert(vo);
        return vo;
    }

    /** 운영사 수정 (MOLIT/KOTSA 또는 본인 항공사) */
    @Transactional
    public void update(String oprtrId, OprtrVO vo, IcasUser user) {
        OprtrVO existing = oprtrMapper.selectByOprtrId(oprtrId);
        if (existing == null) throw BusinessException.notFound("항공기 운영사");

        assertWritable(user, oprtrId);

        validateRequired(vo);
        ognzService.requireOgnzOfType(vo.getOgnzId(), "AIRLINE");
        validateIcaoLength(vo.getIcaoDesig());

        // ICAO 지정어 중복 사전 체크 (본인 제외)
        if (oprtrMapper.countByIcaoDesigExclude(vo.getIcaoDesig(), oprtrId) > 0) {
            throw BusinessException.conflict("이미 등록된 ICAO 지정어입니다.");
        }

        vo.setOprtrId(oprtrId);
        vo.setLastChgUserId(user.getUserId());
        int affected = oprtrMapper.update(vo);
        if (affected == 0) throw BusinessException.notFound("항공기 운영사");
    }

    /** 소프트삭제 (MOLIT/KOTSA 전용) */
    @Transactional
    public void softDelete(String oprtrId, IcasUser user) {
        OprtrVO existing = oprtrMapper.selectByOprtrId(oprtrId);
        if (existing == null) throw BusinessException.notFound("항공기 운영사");

        int affected = oprtrMapper.softDelete(oprtrId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("항공기 운영사");
    }

    // --- 내부 검증 ---

    private void validateRequired(OprtrVO vo) {
        if (isBlank(vo.getOprtrNm()))   throw BusinessException.badRequest("운영사명(국문)은 필수입니다.");
        if (isBlank(vo.getOprtrNmEn())) throw BusinessException.badRequest("운영사명(영문)은 필수입니다.");
        if (isBlank(vo.getIcaoDesig())) throw BusinessException.badRequest("ICAO 지정어는 필수입니다.");
        if (isBlank(vo.getOgnzId()))    throw BusinessException.badRequest("기관 ID는 필수입니다.");
    }

    private void validateIcaoLength(String icaoDesig) {
        if (icaoDesig != null && icaoDesig.trim().length() != 3) {
            throw BusinessException.badRequest("ICAO 지정어는 3자여야 합니다.");
        }
    }

    /** 단건 조회 가시범위: AIRLINE 은 본인만, VERIFIER·MOLIT/KOTSA 는 허용 */
    private void assertReadable(IcasUser user, String oprtrId) {
        if (user.isMaster() || user.isMolitOrKotsa() || user.isVerifier()) return;
        if (user.isAirline() && !oprtrId.equals(user.getOprtrId())) {
            throw BusinessException.forbidden("본인 항공사 데이터만 조회할 수 있습니다.");
        }
    }

    /** 수정 가시범위: MOLIT/KOTSA 또는 본인 항공사만 */
    private void assertWritable(IcasUser user, String oprtrId) {
        if (user.isMaster() || user.isMolitOrKotsa()) return;
        if (user.isAirline() && oprtrId.equals(user.getOprtrId())) return;
        throw BusinessException.forbidden("해당 운영사 데이터를 수정할 권한이 없습니다.");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
