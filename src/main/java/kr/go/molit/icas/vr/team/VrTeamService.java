package kr.go.molit.icas.vr.team;

import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.vr.VrService;
import kr.go.molit.icas.vr.team.domain.VrTeamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 검증팀 구성원 서비스 — tn_vr_team (1:N).
 * LEAD 역할의 conscutv_cnt > 3 이면 OoM Rule 17 에서 경고 — 여기서는 차단하지 않음 (참고용 저장만).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VrTeamService {

    private final VrTeamMapper vrTeamMapper;
    private final VrService    vrService;

    public List<VrTeamVO> list(String vrId, IcasUser user) {
        vrService.loadForRead(vrId, user);
        return vrTeamMapper.selectByVrId(vrId);
    }

    @Transactional
    public VrTeamVO add(String vrId, VrTeamVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        validateTeam(vo);

        int nextSn = vrTeamMapper.maxTeamSn(vrId) + 1;
        vo.setVrId(vrId);
        vo.setTeamSn(nextSn);
        vo.setFrstRegUserId(user.getUserId());
        vo.setLastChgUserId(user.getUserId());

        vrTeamMapper.insertTeam(vo);
        return vrTeamMapper.selectByPk(vrId, nextSn);
    }

    @Transactional
    public VrTeamVO update(String vrId, int teamSn, VrTeamVO vo, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        validateTeam(vo);
        assertExists(vrId, teamSn);

        vo.setVrId(vrId);
        vo.setTeamSn(teamSn);
        vo.setLastChgUserId(user.getUserId());
        vrTeamMapper.updateTeam(vo);
        return vrTeamMapper.selectByPk(vrId, teamSn);
    }

    @Transactional
    public void delete(String vrId, int teamSn, IcasUser user) {
        vrService.assertVrDraftForChildEdit(vrId, user);
        assertExists(vrId, teamSn);
        vrTeamMapper.deleteByPk(vrId, teamSn);
    }

    private void validateTeam(VrTeamVO vo) {
        if (vo.getUserNm() == null || vo.getUserNm().isBlank())
            throw BusinessException.badRequest("검증팀 구성원 성명(userNm)은 필수입니다.");
        if (vo.getRoleCd() == null || !List.of("LEAD","MEMBER","INDEP_REVIEWER").contains(vo.getRoleCd()))
            throw BusinessException.badRequest("역할(roleCd)은 LEAD / MEMBER / INDEP_REVIEWER 중 하나여야 합니다.");
    }

    private void assertExists(String vrId, int teamSn) {
        if (vrTeamMapper.selectByPk(vrId, teamSn) == null)
            throw BusinessException.notFound("검증팀 구성원(teamSn=" + teamSn + ")");
    }
}
