package kr.go.molit.icas.er.oom.validate;

import kr.go.molit.icas.com.oprtr.OprtrMapper;
import kr.go.molit.icas.com.oprtr.domain.OprtrVO;
import kr.go.molit.icas.com.vrfcn.VrfcnInstMapper;
import kr.go.molit.icas.com.vrfcn.domain.VrfcnInstVO;
import kr.go.molit.icas.common.exception.BusinessException;
import kr.go.molit.icas.common.security.DataScopeValidator;
import kr.go.molit.icas.common.security.IcasUser;
import kr.go.molit.icas.er.oom.OomCheckMapper;
import kr.go.molit.icas.er.oom.domain.OomCheckVO;
import kr.go.molit.icas.er.oom.item.OomCheckItemMapper;
import kr.go.molit.icas.er.oom.item.domain.OomCheckItemVO;
import kr.go.molit.icas.er.oom.validate.domain.QuantCheckResult;
import kr.go.molit.icas.er.oom.validate.domain.QuantCheckRunResult;
import kr.go.molit.icas.er.rprt.ErMapper;
import kr.go.molit.icas.er.rprt.domain.ErSearch;
import kr.go.molit.icas.er.rprt.domain.ErVO;
import kr.go.molit.icas.vr.VrMapper;
import kr.go.molit.icas.vr.domain.VrVO;
import kr.go.molit.icas.vr.team.VrTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * CORSIA 18종 정량 검증 엔진 (SFR-034).
 *
 * <h2>실행 흐름</h2>
 * <ol>
 *   <li>OoM 마스터 + 권한·상태(INPRG) 검증</li>
 *   <li>컨텍스트 빌드 (oom, er, oprtr, prevYearEr)</li>
 *   <li>18 Rule 순회 실행 (한 Rule 실패해도 다른 Rule 계속)</li>
 *   <li>기존 1~18 자동 항목 일괄 만료 후 신규 INSERT</li>
 *   <li>요약 산출: FAIL≥1 → FAIL, WARN 만 → HOLD, 전부 PASS → PASS</li>
 * </ol>
 *
 * <p>본 메서드는 점검 결과를 확정하지는 않음. 사용자 검토 후
 * {@link kr.go.molit.icas.er.oom.OomCheckService#finalize} 호출.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorsiaQuantValidator {

    private final OomCheckMapper          oomCheckMapper;
    private final OomCheckItemMapper      oomCheckItemMapper;
    private final ErMapper                erMapper;
    private final OprtrMapper             oprtrMapper;
    private final VrMapper                vrMapper;
    private final VrTeamMapper            vrTeamMapper;
    private final VrfcnInstMapper         vrfcnInstMapper;
    private final QuantCheckRuleFactory   ruleFactory;
    private final DataScopeValidator      dataScopeValidator;

    /**
     * 18 Rule 일괄 실행 + oom_check_item 저장.
     *
     * @param oomId 대상 OoM
     * @param user  실행 사용자 (KOTSA 또는 MASTER)
     * @return 실행 요약
     * @throws BusinessException NOT_FOUND  — OoM 미존재
     * @throws BusinessException FORBIDDEN  — KOTSA/MASTER 외
     * @throws BusinessException CONFLICT   — OoM 이 INPRG 가 아님
     */
    @Transactional
    public QuantCheckRunResult runAll(String oomId, IcasUser user) {
        OomCheckVO oom = oomCheckMapper.selectByOomId(oomId);
        if (oom == null) throw BusinessException.notFound("OoM");
        assertKotsa(user);
        if (!"INPRG".equals(oom.getOomStCd())) {
            throw BusinessException.conflict("INPRG 상태의 OoM 에서만 자동 검증을 실행할 수 있습니다. 현재: " + oom.getOomStCd());
        }
        // 가시범위 추가 (KOTSA 는 전체 가시지만 표준 호출)
        dataScopeValidator.assertOprtrAccessible(user, oom.getOprtrId(), oom.getRprtYr());

        // ── 컨텍스트 빌드 ─────────────────────────────────
        QuantCheckContext ctx = buildContext(oom);

        // ── 기존 1~18 자동 항목 만료 ─────────────────────
        oomCheckItemMapper.softDeleteAutoItems(oomId, user.getUserId());

        // ── 18 Rule 순회 ────────────────────────────────
        List<QuantCheckResult> results = new ArrayList<>(18);
        int pass = 0, warn = 0, fail = 0;
        for (QuantCheckRule rule : ruleFactory.rules()) {
            QuantCheckResult r = safeRun(rule, ctx);
            results.add(r);

            switch (r.getJudgCd() == null ? "WARN" : r.getJudgCd()) {
                case "PASS" -> pass++;
                case "FAIL" -> fail++;
                default     -> warn++;
            }

            // oom_check_item 으로 저장
            OomCheckItemVO vo = new OomCheckItemVO();
            vo.setOomId(oomId);
            vo.setItemNo(r.getItemNo());
            vo.setItemNm(r.getItemNm());
            vo.setExpctdVal(r.getExpctdVal());
            vo.setRprtdVal(r.getRprtdVal());
            vo.setDvtnRate(r.getDvtnRate());
            vo.setJudgCd(r.getJudgCd());
            vo.setRmrk(r.getRmrk());
            vo.setFrstRegUserId(user.getUserId());
            vo.setLastChgUserId(user.getUserId());
            oomCheckItemMapper.insertItem(vo);
        }

        String overall = (fail > 0) ? "FAIL"
                        : (warn > 0 ? "HOLD" : "PASS");

        return QuantCheckRunResult.builder()
                .oomId(oomId)
                .totalCount(results.size())
                .passCount(pass)
                .warnCount(warn)
                .failCount(fail)
                .overallRslt(overall)
                .results(results)
                .build();
    }

    // ══════════════════════════════════════════════════════
    // Private
    // ══════════════════════════════════════════════════════

    private QuantCheckContext buildContext(OomCheckVO oom) {
        ErVO er = oom.getErId() != null ? erMapper.selectByErId(oom.getErId()) : null;
        OprtrVO oprtr = oprtrMapper.selectByOprtrId(oom.getOprtrId());

        // 전년 ER 조회 (직전 보고연도)
        ErVO prevEr = null;
        if (oom.getRprtYr() != null && oom.getRprtYr().matches("\\d{4}")) {
            String prevYr = String.valueOf(Integer.parseInt(oom.getRprtYr()) - 1);
            ErSearch s = new ErSearch();
            s.setOprtrId(oom.getOprtrId());
            s.setRprtYr(prevYr);
            s.setErStCd("APRVD");
            s.setPage(1);
            s.setPageSize(1);
            List<ErVO> rows = erMapper.selectErs(s);
            if (!rows.isEmpty()) prevEr = rows.get(0);
        }

        // VR 연계 데이터 — Rule16(CCR 유효성) / Rule17(리더 연속 횟수) 에서 사용
        VrfcnInstVO vrfcnInst    = null;
        Integer     leadCnt      = null;
        if (oom.getVrId() != null) {
            VrVO vr = vrMapper.selectByVrId(oom.getVrId());
            if (vr != null && vr.getVrfcnInstId() != null) {
                vrfcnInst = vrfcnInstMapper.selectByVrfcnInstId(vr.getVrfcnInstId());
                leadCnt   = vrTeamMapper.selectLeadConscutvCnt(oom.getVrId());
            }
        }

        return QuantCheckContext.builder()
                .oom(oom)
                .er(er)
                .oprtr(oprtr)
                .prevYearEr(prevEr)
                .vrfcnInst(vrfcnInst)
                .leadConscutvCnt(leadCnt)
                .build();
    }

    /** Rule 실행 중 예외 발생 시도 결과로 변환 (검증 전체가 죽지 않게). */
    private QuantCheckResult safeRun(QuantCheckRule rule, QuantCheckContext ctx) {
        try {
            QuantCheckResult r = rule.check(ctx);
            if (r == null) {
                return QuantCheckResult.builder()
                        .itemNo(rule.itemNo()).itemNm(rule.itemNm())
                        .judgCd("WARN").rmrk("Rule 이 null 결과를 반환")
                        .build();
            }
            return r;
        } catch (Exception ex) {
            return QuantCheckResult.builder()
                    .itemNo(rule.itemNo()).itemNm(rule.itemNm())
                    .judgCd("WARN").rmrk("검증 중 오류: " + ex.getClass().getSimpleName() + " — " + ex.getMessage())
                    .build();
        }
    }

    private void assertKotsa(IcasUser user) {
        if (user.isMaster()) return;
        if (!"KOTSA".equals(user.getOgnzSeCd())) {
            throw BusinessException.forbidden("한국교통안전공단(KOTSA) 사용자만 정량 검증을 실행할 수 있습니다.");
        }
    }
}
