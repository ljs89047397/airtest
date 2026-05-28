package kr.go.molit.icas.ptl.workflow;

import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.ptl.workflow.domain.WorkflowRowVO;
import kr.go.molit.icas.ptl.workflow.domain.WorkflowSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 통합 워크플로우 조회 서비스 (SFR-054).
 *
 * <p>모든 운영사 × 도메인 상태를 한 화면에서 조회.
 * WorkflowMapper 의 단일 쿼리(상관 서브쿼리)로 처리.
 *
 * <p>가시범위:
 * - MOLIT/KOTSA: 전체 운영사
 * - AIRLINE: 자기 운영사만 (oprtrId 강제 세팅)
 * - VERIFIER: 배정된 운영사만 (1차: 전체 조회 후 필터 — 성능 허용)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkflowService {

    private final WorkflowMapper workflowMapper;

    /**
     * 통합 워크플로우 조회.
     * 항공사(AIRLINE)는 자기 운영사만 조회 가능.
     */
    public List<WorkflowRowVO> getWorkflow(WorkflowSearch search, IcasUser user) {
        if (!user.isMaster() && !user.isMolitOrKotsa()) {
            if (user.isAirline()) {
                // AIRLINE: 자기 oprtrId 강제 세팅
                // IcasUser.getOprtrId() 활용 (AIRLINE인 경우에만 oprtrId 보유)
                search.setOprtrId(user.getOprtrId());
            }
            // VERIFIER: 1차는 전체 조회 — Mapper SQL 에서 vr_asgn 조건으로 필터 예정
        }
        return workflowMapper.selectWorkflow(search);
    }
}
