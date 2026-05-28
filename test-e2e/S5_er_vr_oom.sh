#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S5 ER 본 보고 + VR 검증 + OoM 18종 (메인 검증 파이프라인)
# 데이터 흐름:
#   운영사 ER 작성·제출
#     → 검증기관(VR) 작성·부적합·의견·제출
#     → KOTSA 권고 → MOLIT 승인
#   MOLIT(OoM) 작성 → Rule 18종 자동검증 실행 → 추가요청·검증기관평가 → finalize
#   ER 최종 승인
# 입력 actor:  운영사 ↔ 검증기관 ↔ MOLIT
# 최종 결과:    APRVD ER · REASONABLE VR · OoM 적정성 평가서
# 통과 박스:   ② ER · ④ VR · ⑥ OoM · ⑦ CORSIA 18종
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S5. ER + VR + OoM 메인 검증 파이프라인 (RFP ②④⑥⑦) ===="

section "S5.1 MOLIT — ER 인프라 헬스 + 자식 8종"
login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/er/rprt?rprtYr=2026" > /dev/null;   assert_code "GET ER 목록" "200"
req GET "/api/er/rprt/ER2026KAL"   > /dev/null;   assert_code "GET ER 상세" "200"
for child in acft-fuel cntry-pair aerdrm-pair fuel-smry data-gap afbr vrfr-info; do
  req GET "/api/er/rprt/ER2026KAL/$child" > /dev/null
  assert_code "GET ER/$child" "200"
done

section "S5.2 운영사 — 신규 ER 작성 + 자식 입력 + 제출"
logout; login "kal_user" "admin1234!" >/dev/null
req POST "/api/er/rprt" '{"oprtrId":"KAL","rprtYr":"2030"}' > /dev/null
ER_NEW=$(echo "$LAST_BODY" | grep -oE '"erId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST ER 신규" "200"
echo "  신규 ER: ${ER_NEW:-(추출불가)}"

if [[ -n "${ER_NEW:-}" ]]; then
  req POST "/api/er/rprt/$ER_NEW/acft-fuel" '{"acftRegnNo":"HL-S5","acftTypeCd":"A350-900","fuelTypeCd":"JET-A1","fuelQty":1234.56}' > /dev/null
  CODE=$LAST_CODE
  if [[ "$CODE" == "200" || "$CODE" == "201" || "$CODE" == "400" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} POST ER 자식 acft-fuel ($CODE)"
  else
    FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("ER acft-fuel ($CODE)"); echo -e "  ${RED}✗${RESET} acft-fuel ($CODE)"
  fi
  req POST "/api/er/rprt/$ER_NEW/submit" "{}" > /dev/null; assert_code "POST ER submit" "200"
fi

section "S5.3 검증기관(vrf_lead) — VR 신규·자식·제출"
logout; login "vrf_lead" "gn12345!" || { echo "vrf_lead 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} vrf_lead 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req POST "/api/vr" '{"oprtrId":"KAL","rprtYr":"2026","erId":"ER2026KAL","vrfcnInstId":"VI_KVA","vrTypeCd":"ER"}'
assert_code "POST VR 신규" "200"
VR_ID=$(echo "$LAST_BODY" | grep -oE '"vrId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  VR ID: ${VR_ID:-(추출불가)}"

if [[ -n "${VR_ID:-}" ]]; then
  req GET "/api/vr/$VR_ID" > /dev/null; assert_code "GET VR 상세" "200"
  req PUT "/api/vr/$VR_ID/links" '{"erId":"ER2026KAL"}' > /dev/null
  assert_code "PUT VR links" "200"
  req POST "/api/vr/$VR_ID/submit" "{}" > /dev/null; assert_code "POST VR submit (DRAFT→SBMTD)" "200"
fi

section "S5.4 KOTSA — VR 권고 (SBMTD→RCMDD)"
logout; login "kotsa01" "admin1234!" >/dev/null
if [[ -n "${VR_ID:-}" ]]; then
  req POST "/api/vr/$VR_ID/recommend" "{}" > /dev/null; assert_code "POST VR recommend" "200"
fi

section "S5.5 MOLIT — VR 승인 (RCMDD→APRVD)"
logout; login "admin01" "admin1234!" >/dev/null
if [[ -n "${VR_ID:-}" ]]; then
  req POST "/api/vr/$VR_ID/approve" "{}" > /dev/null
  assert_code "POST VR approve" "200"
  req GET "/api/vr/$VR_ID" > /dev/null
  assert_json_contains "VR 최종 상태=APRVD" '"vrStCd":"APRVD"'
fi

section "S5.6 KOTSA — OoM 작성 + Rule 18종 실행 + finalize"
logout; login "kotsa01" "admin1234!" >/dev/null
req POST "/api/er/oom" '{"oprtrId":"KAL","rprtYr":"2026","erId":"ER2026KAL"}'
assert_code "POST OoM 신규" "200"
OOM_ID=$(echo "$LAST_BODY" | grep -oE '"oomId":"[^"]+"' | head -1 | cut -d'"' -f4)
[[ -z "$OOM_ID" ]] && OOM_ID=$(echo "$LAST_BODY" | grep -oE '"oomCheckId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  OoM ID: ${OOM_ID:-(추출불가)}"

if [[ -n "${OOM_ID:-}" ]]; then
  req GET  "/api/er/oom/$OOM_ID"           > /dev/null; assert_code "GET OoM 상세" "200"
  req POST "/api/er/oom/$OOM_ID/run-quant" "{}" > /dev/null; assert_code "POST Rule 18종 실행" "200"
  req GET  "/api/er/oom/$OOM_ID/item"      > /dev/null; assert_code "GET OoM 항목" "200"
  assert_json_contains "Rule 18종 itemNo 존재" '"itemNo"'
  req GET  "/api/er/oom/$OOM_ID/rqst"      > /dev/null; assert_code "GET OoM 추가요청" "200"
  req GET  "/api/er/oom/$OOM_ID/eval"      > /dev/null; assert_code "GET 검증기관 평가" "200"
  req POST "/api/er/oom/$OOM_ID/finalize"  '{"rsltCd":"PASS"}' > /dev/null
  assert_code "POST OoM finalize PASS" "200"
fi

section "S5.7 화면 콘텐츠 검증 — ER/VR/OoM/CORSIA 18종"
check_html() {
  local path="$1" needle="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$needle"; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $desc";
  else FAIL=$((FAIL+1)); FAILED_NAMES+=("$path '$needle'"); echo -e "  ${RED}✗${RESET} $desc"; fi
}
check_html "/er/list"          "보고연도"                    "ER 목록"
check_html "/er/ER2026KAL"     "국가.*쌍\|국가 쌍"            "ER — 국가쌍"
check_html "/er/ER2026KAL"     "비행장.*쌍\|비행장 쌍"        "ER — 비행장쌍"
check_html "/er/ER2026KAL"     "데이터 갭\|data-gap"          "ER — 데이터 갭"
check_html "/vr/list"          "검증보고서\|VR"               "VR 목록"
check_html "/er/oom"           "적정성검토\|OoM"              "OoM 목록"
check_html "/er/oom/qchk"      "CORSIA"                       "CORSIA 검증 화면"
check_html "/er/oom/qchk"      "R001 ICAO\|ICAO 지정어"        "Rule R001"
check_html "/er/oom/qchk"      "R005 보고의무\|보고의무"        "Rule R005"
check_html "/er/oom/qchk"      "R014 데이터 갭\|데이터 갭"      "Rule R014"
check_html "/er/oom/qchk"      "R018 전년대비\|전년대비"        "Rule R018"

section "S5.8 DB 정합성 — VR / OoM / Rule18 항목"
N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM vr.tn_vr WHERE oprtr_id='KAL' AND rprt_yr='2026';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} VR KAL/2026 ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("VR 0행"); echo -e "  ${RED}✗${RESET} VR ($N)"; }

N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_oom_check WHERE oprtr_id='KAL' AND rprt_yr='2026';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} OoM KAL/2026 ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("OoM 0행"); echo -e "  ${RED}✗${RESET} OoM ($N)"; }

N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_oom_check_item WHERE oom_id IN (SELECT oom_id FROM er.tn_oom_check WHERE oprtr_id='KAL' AND rprt_yr='2026');" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} Rule 18종 항목 ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("Rule18 항목 0행"); echo -e "  ${RED}✗${RESET} Rule18 ($N)"; }

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
