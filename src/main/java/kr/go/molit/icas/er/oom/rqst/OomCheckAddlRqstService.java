package kr.go.molit.icas.er.oom.rqst;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.OomCheckService;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.oom.rqst.domain.OomCheckAddlRqstVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OoM 추가 설명 요청 서비스 (게시판 스타일).
 *
 * <p>워크플로우: KOTSA 가 요청 → AIRLINE(본인 oprtrId) 가 응답.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OomCheckAddlRqstService {

    private final OomCheckAddlRqstMapper rqstMapper;
    private final OomCheckService        oomCheckService;

    public List<OomCheckAddlRqstVO> list(String oomId, IcasUser user) {
        oomCheckService.loadForRead(oomId, user);
        return rqstMapper.selectByOomId(oomId);
    }

    /** KOTSA 가 요청 등록 — OoM INPRG 한정. */
    @Transactional
    public OomCheckAddlRqstVO add(String oomId, String rqstCn, IcasUser user) {
        oomCheckService.assertOomInprgForChildEdit(oomId, user);
        if (rqstCn == null || rqstCn.isBlank()) {
            throw BusinessException.badRequest("요청 내용(rqstCn)은 필수입니다.");
        }
        int nextSn = rqstMapper.selectNextSn(oomId);

        OomCheckAddlRqstVO vo = new OomCheckAddlRqstVO();
        vo.setOomId(oomId);
        vo.setRqstSn(nextSn);
        vo.setRqstUserId(user.getUserId());
        vo.setRqstCn(rqstCn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        rqstMapper.insertRqst(vo);
        return rqstMapper.selectOne(oomId, nextSn);
    }

    /**
     * AIRLINE 응답 입력 — 본인 oprtrId 의 OoM 한정, 응답 미입력 상태에서만.
     */
    @Transactional
    public void respond(String oomId, int rqstSn, String respCn, IcasUser user) {
        if (!user.isAirline() && !user.isMaster()) {
            throw BusinessException.forbidden("AIRLINE 사용자만 응답할 수 있습니다.");
        }
        if (respCn == null || respCn.isBlank()) {
            throw BusinessException.badRequest("응답 내용(respCn)은 필수입니다.");
        }
        OomCheckVO oom = oomCheckService.loadForRead(oomId, user);
        // loadForRead 가 AIRLINE 본인 가시범위 검증 수행
        if (!"INPRG".equals(oom.getOomStCd())) {
            throw BusinessException.conflict("INPRG 상태의 OoM 에만 응답 가능합니다.");
        }
        OomCheckAddlRqstVO existing = rqstMapper.selectOne(oomId, rqstSn);
        if (existing == null) throw BusinessException.notFound("요청");
        if (existing.getRespDt() != null) {
            throw BusinessException.conflict("이미 응답이 입력된 요청입니다.");
        }
        int affected = rqstMapper.updateResponse(oomId, rqstSn, respCn, user.getUserId());
        if (affected == 0) throw BusinessException.conflict("응답 입력에 실패했습니다.");
    }

    @Transactional
    public void softDelete(String oomId, int rqstSn, IcasUser user) {
        oomCheckService.assertOomInprgForChildEdit(oomId, user);
        int affected = rqstMapper.softDeleteOne(oomId, rqstSn, user.getUserId());
        if (affected == 0) throw BusinessException.notFound("요청");
    }
}
