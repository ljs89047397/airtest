package kr.go.molit.icas.er.oom.eval;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.OomCheckService;
import kr.go.molit.icas.er.oom.eval.domain.OomCheckVrfrEvalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * OoM 검증기관 품질 평가 서비스 (SFR-033).
 *
 * <p>VERIFIER 가 자신의 vrfcn_inst_id 로 1행 등록. UK (oom_id, vrfcn_inst_id).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OomCheckVrfrEvalService {

    private static final Set<String> VALID_GRD = Set.of("GOOD", "AVG", "POOR");

    private final OomCheckVrfrEvalMapper evalMapper;
    private final OomCheckService        oomCheckService;

    public List<OomCheckVrfrEvalVO> list(String oomId, IcasUser user) {
        oomCheckService.loadForRead(oomId, user);
        return evalMapper.selectByOomId(oomId);
    }

    /**
     * Upsert — 같은 (oom_id, vrfcn_inst_id) 가 있으면 UPDATE, 없으면 INSERT.
     * VERIFIER 는 본인 vrfcn_inst_id 만 사용 강제.
     */
    @Transactional
    public OomCheckVrfrEvalVO saveByVerifier(String oomId, OomCheckVrfrEvalVO vo, IcasUser user) {
        if (!user.isVerifier() && !user.isMaster()) {
            throw BusinessException.forbidden("검증기관(VERIFIER) 사용자만 평가를 입력할 수 있습니다.");
        }
        oomCheckService.loadForRead(oomId, user);  // 가시범위 검증

        validate(vo);

        // VERIFIER 는 본인 vrfcnInstId 강제
        if (user.isVerifier()) {
            vo.setVrfcnInstId(user.getVrfcnInstId());
        } else if (vo.getVrfcnInstId() == null || vo.getVrfcnInstId().isBlank()) {
            throw BusinessException.badRequest("vrfcnInstId 는 필수입니다.");
        }
        vo.setOomId(oomId);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        OomCheckVrfrEvalVO existing = evalMapper.selectOne(oomId, vo.getVrfcnInstId());
        if (existing == null) {
            evalMapper.insertEval(vo);
        } else {
            evalMapper.updateEval(vo);
        }
        return evalMapper.selectOne(oomId, vo.getVrfcnInstId());
    }

    @Transactional
    public void softDelete(String oomId, String vrfcnInstId, IcasUser user) {
        if (!user.isMaster()) {
            // VERIFIER 는 본인 평가만 삭제
            if (user.isVerifier() && !vrfcnInstId.equals(user.getVrfcnInstId())) {
                throw BusinessException.forbidden("본인 검증기관 평가만 삭제할 수 있습니다.");
            }
            if (!user.isVerifier()) {
                throw BusinessException.forbidden("검증기관 사용자만 평가를 삭제할 수 있습니다.");
            }
        }
        int affected = evalMapper.softDeleteOne(oomId, vrfcnInstId, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("OoM 검증기관 평가");
    }

    private void validate(OomCheckVrfrEvalVO vo) {
        if (vo.getEvalGrdCd() == null || !VALID_GRD.contains(vo.getEvalGrdCd())) {
            throw BusinessException.badRequest(
                    "평가 등급(evalGrdCd) 허용값: GOOD, AVG, POOR. 입력값: " + vo.getEvalGrdCd());
        }
    }
}
