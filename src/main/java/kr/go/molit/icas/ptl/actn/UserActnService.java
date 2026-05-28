package kr.go.molit.icas.ptl.actn;

import javax.servlet.http.HttpServletRequest;
import kr.go.molit.icas.common.dto.PageResponse;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.ptl.actn.domain.UserActnSearch;
import kr.go.molit.icas.ptl.actn.domain.UserActnVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 핵심 행위 감사 로그 서비스 (SFR-053).
 *
 * <p>다른 Service 에서 주입하여 사용. Controller 없음.
 * 실패해도 메인 트랜잭션에 영향 주지 않도록 try-catch 내부 처리.
 *
 * <p>사용 예:
 * <pre>
 *   userActnService.log("SUBMIT", "er.tn_er", erId, "SUCCESS", user, request);
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActnService {

    private final UserActnMapper userActnMapper;

    // ── 감사 로그 기록 ────────────────────────────────────────────────────────

    /**
     * 감사 로그 기록 (HttpServletRequest 포함 버전).
     * REQUIRES_NEW: 메인 트랜잭션 롤백해도 감사 로그는 보존.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actnSeCd, String targetTbl, String targetPk,
                    String rsltCd, IcasUser user, HttpServletRequest request) {
        try {
            UserActnVO vo = new UserActnVO();
            vo.setUserId(user.getUserId());
            vo.setActnSeCd(actnSeCd);
            vo.setTargetTbl(targetTbl);
            vo.setTargetPk(targetPk);
            vo.setRsltCd(rsltCd);
            vo.setIpAddr(extractIp(request));
            vo.setUserAgent(request.getHeader("User-Agent"));
            userActnMapper.insertActn(vo);
        } catch (Exception e) {
            log.error("[UserActnService] 감사 로그 기록 실패 — actnSeCd={}, targetPk={}: {}",
                    actnSeCd, targetPk, e.getMessage(), e);
        }
    }

    /**
     * 감사 로그 기록 (HttpServletRequest 없는 버전).
     * 내부 배치·스케줄러·다른 서비스 내부 호출용.
     * REQUIRES_NEW: 메인 트랜잭션 롤백해도 감사 로그는 보존.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actnSeCd, String targetTbl, String targetPk,
                    String rsltCd, String userId) {
        try {
            UserActnVO vo = new UserActnVO();
            vo.setUserId(userId);
            vo.setActnSeCd(actnSeCd);
            vo.setTargetTbl(targetTbl);
            vo.setTargetPk(targetPk);
            vo.setRsltCd(rsltCd);
            userActnMapper.insertActn(vo);
        } catch (Exception e) {
            log.error("[UserActnService] 감사 로그 기록 실패 — actnSeCd={}, targetPk={}: {}",
                    actnSeCd, targetPk, e.getMessage(), e);
        }
    }

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /**
     * 감사 로그 목록 조회 (페이징).
     * MOLIT/KOTSA 전용.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserActnVO> listActns(UserActnSearch search, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("감사 로그 조회는 MOLIT/KOTSA 사용자만 가능합니다.");
        }
        List<UserActnVO> rows = userActnMapper.selectActns(search);
        int total = userActnMapper.countActns(search);
        return new PageResponse<>(rows, search.getPage(), search.getPageSize(), total);
    }

    /**
     * 특정 엔티티의 행위 이력 조회.
     * MOLIT/KOTSA 전용.
     */
    @Transactional(readOnly = true)
    public List<UserActnVO> listByTarget(String targetTbl, String targetPk, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            throw BusinessException.forbidden("감사 로그 조회는 MOLIT/KOTSA 사용자만 가능합니다.");
        }
        UserActnSearch search = new UserActnSearch();
        search.setTargetTbl(targetTbl);
        search.setTargetPk(targetPk);
        search.setSize(9999);
        return userActnMapper.selectActns(search);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private String extractIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
