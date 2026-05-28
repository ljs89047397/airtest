#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# 데이터 흐름 기반 E2E 통합 회귀
#   S1 운영 셋업       → 공통관리 매트릭스
#   S2 EMP 라이프사이클 → APRVD EMP
#   S3 SAF 공급망      → 인증서·배치·혼합비율
#   S4 CEF 청구·차감   → 승인된 CEF claim
#   S5 ER+VR+OoM       → 메인 검증 파이프라인
#   S6 EUCR 취소       → 일련번호 이중사용·배치·취소
#   S7 포털 통합 소비   → 워크플로우·통계·시뮬·CCR·감사로그
#   S8 권한·결재 횡단  → 4-actor 권한·HTML 스냅샷
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
LOG="$DIR/run_all.log"

# ── 시드 cleanup — 멱등 실행 보장 ───────────────────────────
echo "[$(date +%T)] DB 시드 정리..." | tee "$LOG"
PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas <<'EOF' >> "$LOG" 2>&1
DELETE FROM er.tn_oom_check_item WHERE oom_id IN (SELECT oom_id FROM er.tn_oom_check WHERE inspctr_user_id IN ('kotsa01','admin01'));
DELETE FROM er.tn_oom_check WHERE inspctr_user_id IN ('kotsa01','admin01');
DELETE FROM er.tn_eucr WHERE last_chg_user_id='kal_user';
DELETE FROM er.tn_cef WHERE last_chg_user_id IN ('kal_user','kotsa01');
DELETE FROM vr.tn_vr WHERE last_chg_user_id IN ('vrf_lead','kotsa01','admin01');
DELETE FROM saf.tn_saf_cert_audit;
DELETE FROM saf.tn_saf_cert WHERE last_chg_user_id='kal_user';
DELETE FROM saf.tn_saf_batch_blndr WHERE batch_id IN (SELECT batch_id FROM saf.tn_saf_batch WHERE last_chg_user_id='kal_user');
DELETE FROM saf.tn_saf_batch_prdc_sply WHERE batch_id IN (SELECT batch_id FROM saf.tn_saf_batch WHERE last_chg_user_id='kal_user');
DELETE FROM saf.tn_saf_batch WHERE last_chg_user_id='kal_user';
DELETE FROM saf.tn_saf_blnd_mntr WHERE last_chg_user_id='kotsa01';
DELETE FROM com.tn_rglt WHERE rglt_id <> 'RG0001' AND last_chg_user_id='admin01';

-- PTL 시뮬 시드 정리 + NULL input_json 복원
DELETE FROM ptl.tn_ptl_sim WHERE sim_nm IN ('버튼 진단','E2E 풀시나리오','S7 시뮬');
UPDATE ptl.tn_ptl_sim SET input_json = '{"baseYear":2026,"forecastStart":2027,"forecastEnd":2030,"carbonPriceUsd":25.0,"annualGrowthPct":3.5,"safBlendPct":2.0,"scope":"ALL"}'::jsonb WHERE input_json IS NULL;
DELETE FROM ptl.tn_ptl_ccr_extr WHERE last_chg_user_id IN ('kal_user','admin01','kotsa01');

-- EMP 시드 reset
DELETE FROM emp.th_emp_chg_hstry WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_acft WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_oprtr_cnct WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_oprtr_info WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_cntry_pair WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_co2_calc WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_co2_detail WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_data_ctrl WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_risk WHERE emp_plan_id LIKE 'EP%';
DELETE FROM emp.tn_emp_plan WHERE emp_plan_id LIKE 'EP%';
UPDATE emp.tn_emp_plan SET use_end_dt='9999-12-31 23:59:59' WHERE emp_plan_id IN ('EMP2026KAL','EMP2026AAR','EMP2026JJA');
UPDATE emp.tn_emp_plan SET emp_st_cd='APRVD' WHERE emp_plan_id='EMP2026KAL';
UPDATE emp.tn_emp_plan SET emp_st_cd='SBMTD' WHERE emp_plan_id='EMP2026AAR';
UPDATE emp.tn_emp_plan SET emp_st_cd='DRAFT' WHERE emp_plan_id='EMP2026JJA';

-- ER 자동생성 cleanup
DELETE FROM er.tn_eucr WHERE last_chg_user_id IN ('admin01','kal_user') AND eucr_id NOT LIKE 'EUCR2%';
DELETE FROM er.tn_oom_check_item WHERE oom_id IN (SELECT oom_id FROM er.tn_oom_check WHERE inspctr_user_id IN ('admin01','kotsa01'));
DELETE FROM er.tn_oom_check WHERE inspctr_user_id IN ('admin01','kotsa01');
DELETE FROM vr.tn_vr WHERE last_chg_user_id IN ('vrf_lead','kotsa01','admin01');
DELETE FROM saf.tn_saf_cert_audit WHERE actn_user_id IN ('kal_user','admin01');
DELETE FROM saf.tn_saf_cert WHERE last_chg_user_id='kal_user';
DELETE FROM saf.tn_saf_batch_blndr WHERE batch_id IN (SELECT batch_id FROM saf.tn_saf_batch WHERE last_chg_user_id='kal_user');
DELETE FROM saf.tn_saf_batch_prdc_sply WHERE batch_id IN (SELECT batch_id FROM saf.tn_saf_batch WHERE last_chg_user_id='kal_user');
DELETE FROM saf.tn_saf_batch WHERE last_chg_user_id='kal_user';
DELETE FROM saf.tn_saf_blnd_mntr WHERE last_chg_user_id='kotsa01';

-- 03 라이프사이클 시드 격돌 회피
DELETE FROM vr.tn_vr_cncls    WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr_team     WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr_time     WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr_scope    WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr_prcdr    WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr_ncnfrm   WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr_input_info WHERE vr_id='VR0001';
DELETE FROM vr.tn_vr WHERE vr_id='VR0001';

DELETE FROM er.tn_oom_check_addl_rqst WHERE oom_id='OOM0001';
DELETE FROM er.tn_oom_check_vrfr_eval WHERE oom_id='OOM0001';
DELETE FROM er.tn_oom_check_item WHERE oom_id='OOM0001';
DELETE FROM er.tn_oom_check WHERE oom_id='OOM0001';

DELETE FROM er.tn_cef_claim WHERE cef_id IN ('CEF0001','CEF0002','CEF0003');
DELETE FROM er.tn_cef_lcyc  WHERE cef_id IN ('CEF0001','CEF0002','CEF0003');
DELETE FROM er.tn_cef_spchn WHERE cef_id IN ('CEF0001','CEF0002','CEF0003');
DELETE FROM er.tn_cef WHERE cef_id IN ('CEF0001','CEF0002','CEF0003');

DELETE FROM er.tn_eucr_crdt_dtl WHERE eucr_id='EUCR0001';
DELETE FROM er.tn_eucr_batch    WHERE eucr_id='EUCR0001';
DELETE FROM er.tn_eucr WHERE eucr_id='EUCR0001';

-- 추가 cleanup: CEF/OoM 전체, EMP 강제 복원
DELETE FROM er.tn_cef_claim WHERE cef_id LIKE 'CEF%';
DELETE FROM er.tn_cef_lcyc  WHERE cef_id LIKE 'CEF%';
DELETE FROM er.tn_cef_spchn WHERE cef_id LIKE 'CEF%';
DELETE FROM er.tn_cef;
DELETE FROM er.tn_oom_check_addl_rqst;
DELETE FROM er.tn_oom_check_vrfr_eval;
DELETE FROM er.tn_oom_check_item;
DELETE FROM er.tn_oom_check;
UPDATE emp.tn_emp_plan SET use_end_dt='9999-12-31 23:59:59' WHERE emp_plan_id IN ('EMP2026KAL','EMP2026AAR','EMP2026JJA');
EOF

declare -a SCRIPTS=(
  S1_setup.sh
  S2_emp_lifecycle.sh
  S3_saf_supply.sh
  S4_cef_claim.sh
  S5_er_vr_oom.sh
  S6_eucr_cancel.sh
  S7_portal_consume.sh
  S8_authz_cross.sh
)

declare -a NAMES PASS_CNT FAIL_CNT TOTAL_CNT
T_TOTAL=0; T_PASS=0; T_FAIL=0

for s in "${SCRIPTS[@]}"; do
  echo
  echo "────────────────────────────────────────────────"
  echo "▶ $s"
  echo "────────────────────────────────────────────────"
  out=$(bash "$DIR/$s" 2>&1)
  echo "$out" | tail -5
  stats=$(echo "$out" | grep -E "총 [0-9]+ · " | tail -1)
  t=$(echo "$stats" | grep -oE "총 [0-9]+" | grep -oE "[0-9]+")
  p=$(echo "$stats" | grep -oE "통과 [0-9]+" | grep -oE "[0-9]+")
  f=$(echo "$stats" | grep -oE "실패 [0-9]+" | grep -oE "[0-9]+")
  NAMES+=("$s"); TOTAL_CNT+=("${t:-0}"); PASS_CNT+=("${p:-0}"); FAIL_CNT+=("${f:-0}")
  T_TOTAL=$((T_TOTAL + ${t:-0}))
  T_PASS=$((T_PASS + ${p:-0}))
  T_FAIL=$((T_FAIL + ${f:-0}))
  echo "$out" >> "$LOG"
done

echo
echo "════════════════════════════════════════════════════════════════"
echo "  데이터 흐름 기반 E2E 종합 회귀 결과"
echo "════════════════════════════════════════════════════════════════"
printf "%-30s %8s %8s %8s\n" "시나리오" "총" "통과" "실패"
echo "────────────────────────────────────────────────────────────────"
for i in "${!NAMES[@]}"; do
  printf "%-30s %8s %8s %8s\n" "${NAMES[$i]}" "${TOTAL_CNT[$i]}" "${PASS_CNT[$i]}" "${FAIL_CNT[$i]}"
done
echo "────────────────────────────────────────────────────────────────"
printf "%-30s %8s %8s %8s\n" "합계" "$T_TOTAL" "$T_PASS" "$T_FAIL"
echo "════════════════════════════════════════════════════════════════"
if [[ $T_TOTAL -gt 0 ]]; then
  printf "통과율: %d%% (%d/%d)\n" $((T_PASS * 100 / T_TOTAL)) $T_PASS $T_TOTAL
fi
echo "로그: $LOG"

[[ $T_FAIL -eq 0 ]] && exit 0 || exit 1
