#!/usr/bin/env bash
# 전 화면 모든 버튼/액션 API 종합 회귀
# 각 화면이 노출하는 버튼이 호출하는 endpoint 를 직접 호출하여 실패 패턴 진단
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== 09. 전 화면 모든 버튼/액션 API 회귀 ===="

login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# 활성 ID 동적 조회
EMP_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT emp_plan_id FROM emp.tn_emp_plan WHERE use_end_dt > NOW() AND emp_st_cd != 'CNCLD' ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
ER_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT er_id FROM er.tn_er WHERE use_end_dt > NOW() ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
CEF_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT cef_id FROM er.tn_cef WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
EUCR_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT eucr_id FROM er.tn_eucr WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
OOM_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT oom_id FROM er.tn_oom_check WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
VR_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT vr_id FROM vr.tn_vr WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
CERT_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT cert_id FROM saf.tn_saf_cert WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
BATCH_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT batch_id FROM saf.tn_saf_batch WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
SIM_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT sim_id FROM ptl.tn_ptl_sim WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
RGLT_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT rglt_id FROM com.tn_rglt WHERE use_end_dt > NOW() LIMIT 1;" 2>/dev/null | tr -d ' ')
echo "  대상 ID: EMP=$EMP_ID ER=$ER_ID CEF=$CEF_ID EUCR=$EUCR_ID OoM=$OOM_ID VR=$VR_ID CERT=$CERT_ID BATCH=$BATCH_ID SIM=$SIM_ID RGLT=$RGLT_ID"

# 버튼이 호출하는 endpoint 별 검증 (GET 은 200, POST 액션은 2xx/4xx 모두 비즈룰 정상)
btn_test() {
  local desc="$1" method="$2" path="$3" body="${4:-}" acceptable="${5:-200}"
  req "$method" "$path" "$body" > /dev/null
  TOTAL=$((TOTAL+1))
  if echo "$acceptable" | grep -qw "$LAST_CODE"; then
    PASS=$((PASS+1)); printf "  ${GREEN}✓${RESET} %-50s [%3s] %s\n" "$desc" "$LAST_CODE" "$method $path"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("$desc method=$method path=$path code=$LAST_CODE expected=$acceptable")
    printf "  ${RED}✗${RESET} %-50s [%3s] %s\n" "$desc" "$LAST_CODE" "$method $path"
  fi
}

# ─────────────────────────────────────────────────────
section "9.1 메인 대시보드 버튼"
btn_test "헤더 매뉴얼 페이지"        GET /manual               "" "200"
btn_test "보고연도 변경 워크플로우"   GET /api/ptl/workflow?rprtYr=2025 "" "200"
btn_test "보고연도 변경 통계"        GET /api/ptl/stat/2025    "" "200"

# ─────────────────────────────────────────────────────
section "9.2 EMP 화면 버튼"
btn_test "GET EMP 목록 (조회)"       GET /api/emp/plan?rprtYr=2026 "" "200"
[[ -n "$EMP_ID" ]] && {
  btn_test "GET EMP 상세 (상세 버튼)"  GET /api/emp/plan/$EMP_ID "" "200"
  btn_test "GET EMP 자식 acft"         GET /api/emp/plan/$EMP_ID/acft "" "200"
  btn_test "GET EMP 자식 cnct"         GET /api/emp/plan/$EMP_ID/cnct "" "200"
  btn_test "GET EMP 자식 info"         GET /api/emp/plan/$EMP_ID/info "" "200"
  btn_test "GET EMP 자식 cntry-pair"   GET /api/emp/plan/$EMP_ID/cntry-pair "" "200"
  btn_test "GET EMP 자식 co2-calc"     GET /api/emp/plan/$EMP_ID/co2-calc "" "200"
  btn_test "GET EMP 자식 co2-detail"   GET /api/emp/plan/$EMP_ID/co2-detail "" "200"
  btn_test "GET EMP 자식 data-ctrl"    GET /api/emp/plan/$EMP_ID/data-ctrl "" "200"
  btn_test "GET EMP 자식 risk"         GET /api/emp/plan/$EMP_ID/risk "" "200"
  btn_test "GET EMP 변경이력"          GET /api/emp/plan/$EMP_ID/history "" "200 404"
}

# ─────────────────────────────────────────────────────
section "9.3 ER 화면 버튼"
btn_test "GET ER 목록"               GET /api/er/rprt?rprtYr=2026 "" "200"
[[ -n "$ER_ID" ]] && {
  btn_test "GET ER 상세"               GET /api/er/rprt/$ER_ID "" "200"
  btn_test "GET ER acft-fuel"          GET /api/er/rprt/$ER_ID/acft-fuel "" "200"
  btn_test "GET ER cntry-pair"         GET /api/er/rprt/$ER_ID/cntry-pair "" "200"
  btn_test "GET ER aerdrm-pair"        GET /api/er/rprt/$ER_ID/aerdrm-pair "" "200"
  btn_test "GET ER fuel-smry"          GET /api/er/rprt/$ER_ID/fuel-smry "" "200"
  btn_test "GET ER data-gap"           GET /api/er/rprt/$ER_ID/data-gap "" "200"
  btn_test "GET ER afbr"               GET /api/er/rprt/$ER_ID/afbr "" "200"
  btn_test "GET ER vrfr-info"          GET /api/er/rprt/$ER_ID/vrfr-info "" "200"
  btn_test "GET ER validate-pair-sum"  GET /api/er/rprt/$ER_ID/validate-pair-sum "" "200 404"
}

# ─────────────────────────────────────────────────────
section "9.4 CEF 화면 버튼"
btn_test "GET CEF 목록"               GET /api/er/cef "" "200"
[[ -n "$CEF_ID" ]] && {
  btn_test "GET CEF 상세"               GET /api/er/cef/$CEF_ID "" "200"
  btn_test "GET CEF by-er"              GET /api/er/cef/by-er/${ER_ID:-ER2026KAL} "" "200"
  btn_test "GET CEF 이중청구 검증"      GET "/api/er/cef/validate-double-claim?batchIdNo=BATCH001&currentOprtrId=KAL" "" "200 400"
}

# ─────────────────────────────────────────────────────
section "9.5 EUCR 화면 버튼"
btn_test "GET EUCR 목록"               GET /api/er/eucr "" "200"
[[ -n "$EUCR_ID" ]] && {
  btn_test "GET EUCR 상세"               GET /api/er/eucr/$EUCR_ID "" "200"
  btn_test "POST EUCR 이중사용 검증"     POST /api/er/eucr/validate-double-using '{"crdtSnList":["A001","A002"]}' "200 400"
}

# ─────────────────────────────────────────────────────
section "9.6 OoM + CORSIA 화면 버튼"
btn_test "GET OoM 목록"                GET /api/er/oom "" "200"
[[ -n "$OOM_ID" ]] && {
  btn_test "GET OoM 상세"                GET /api/er/oom/$OOM_ID "" "200"
  btn_test "GET OoM 항목"                GET /api/er/oom/$OOM_ID/item "" "200"
  btn_test "GET OoM 추가요청"            GET /api/er/oom/$OOM_ID/rqst "" "200"
  btn_test "GET OoM 검증기관평가"        GET /api/er/oom/$OOM_ID/eval "" "200"
}

# ─────────────────────────────────────────────────────
section "9.7 VR 화면 버튼"
btn_test "GET VR 목록"                 GET /api/vr "" "200"
[[ -n "$VR_ID" ]] && {
  btn_test "GET VR 상세"                 GET /api/vr/$VR_ID "" "200"
}

# ─────────────────────────────────────────────────────
section "9.8 SAF 화면 버튼"
btn_test "GET SAF 인증서 목록"          GET /api/saf/cert "" "200"
btn_test "GET SAF 배치 목록"            GET /api/saf/batch "" "200"
btn_test "GET SAF 공항급유"             GET /api/saf/airprt-fuel "" "200"
btn_test "GET SAF 공항구매"             GET /api/saf/airprt-purch "" "200"
btn_test "GET SAF 혼합 전체"            GET /api/saf/mntr/blnd/all?rprtYr=2026 "" "200"
btn_test "POST SAF 혼합산출 (KOTSA)"     POST /api/saf/mntr/blnd/calc '{"oprtrId":"KAL","rprtYr":"2026"}' "200 403"
[[ -n "$CERT_ID" ]] && {
  btn_test "GET SAF 인증서 상세"          GET /api/saf/cert/$CERT_ID "" "200"
  btn_test "GET SAF 인증서 audit"         GET /api/saf/cert/$CERT_ID/audit "" "200 404"
}
[[ -n "$BATCH_ID" ]] && {
  btn_test "GET SAF 배치 상세"            GET /api/saf/batch/$BATCH_ID "" "200"
  btn_test "GET SAF 배치 ghg"             GET /api/saf/batch/$BATCH_ID/ghg "" "200"
  btn_test "GET SAF 배치 feed"            GET /api/saf/batch/$BATCH_ID/feed "" "200"
  btn_test "GET SAF 배치 blndr"           GET /api/saf/batch/$BATCH_ID/blndr "" "200"
  btn_test "GET SAF 배치 prdc"            GET /api/saf/batch/$BATCH_ID/prdc "" "200"
}

# ─────────────────────────────────────────────────────
section "9.9 포털 화면 버튼"
btn_test "GET 통합 워크플로우"          GET /api/ptl/workflow?rprtYr=2026 "" "200"
btn_test "GET 통계 (2026)"              GET /api/ptl/stat/2026 "" "200"
btn_test "GET CCR 목록"                 GET /api/ptl/ccr "" "200"
btn_test "POST CCR 추출 (MOLIT)"        POST /api/ptl/ccr/extract '{"rprtYr":"2026","extrScopeCd":"ALL"}' "200 400 409"
btn_test "GET 시뮬 목록"                GET /api/ptl/sim "" "200"
btn_test "POST 시뮬 신규"               POST /api/ptl/sim '{"simNm":"버튼 진단","scopeSeCd":"ALL","baseYr":"2026"}' "200"
[[ -n "$SIM_ID" ]] && {
  btn_test "GET 시뮬 상세"                GET /api/ptl/sim/$SIM_ID "" "200"
  btn_test "POST 시뮬 run"                POST /api/ptl/sim/$SIM_ID/run "{}" "200"
}
btn_test "GET 감사로그"                 GET /api/ptl/actn "" "200"

# ─────────────────────────────────────────────────────
section "9.10 공통관리 화면 버튼"
btn_test "GET 사용자"                   GET /api/com/user "" "200"
btn_test "GET 조직"                     GET /api/com/ognz "" "200"
btn_test "GET 운영사"                   GET /api/com/oprtr "" "200"
btn_test "GET 검증기관"                 GET /api/com/vrfcn/inst "" "200"
btn_test "GET 검증배정"                 GET /api/com/vrfcn/assgn "" "200"
btn_test "GET 역할"                     GET /api/com/role "" "200"
btn_test "GET 권한"                     GET /api/com/authrt "" "200"
btn_test "GET 공통코드"                 GET /api/com/cd "" "200"
btn_test "GET 메뉴"                     GET /api/com/menu "" "200"
btn_test "GET 프로그램"                 GET /api/com/prgrm "" "200"
btn_test "GET 결재함"                   GET /api/com/atrz "" "200"
btn_test "GET 결재 내 펜딩"             GET /api/com/atrz/my-pending "" "200"
btn_test "GET 규정"                     GET /api/com/rglt "" "200"
[[ -n "$RGLT_ID" ]] && {
  btn_test "GET 규정 상세"                GET /api/com/rglt/$RGLT_ID "" "200"
}

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
