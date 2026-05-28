#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S7 포털 통합 소비 (read-only)
# 데이터 흐름:  (S2~S6 산출물 소비) 통합 워크플로우 매트릭스
#               → 통계/시뮬레이션 → CCR 추출 → 감사로그
# 입력 actor:  — (다른 시나리오의 산출물)
# 최종 결과:    MOLIT 관제 화면 가동 + 외부 연계 자료 추출
# 통과 박스:   ⑨ 포털
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S7. 포털 통합 소비 (RFP 박스 ⑨) ===="

login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

section "S7.1 통합 워크플로우 매트릭스 (운영사 × 도메인 상태)"
req GET "/api/ptl/workflow?rprtYr=2026" > /dev/null;  assert_code "GET 워크플로우 2026" "200"
req GET "/api/ptl/workflow?rprtYr=2025" > /dev/null;  assert_code "GET 워크플로우 2025" "200"

section "S7.2 통계/시뮬레이션"
req GET "/api/ptl/stat/yearly?rprtYr=2026" > /dev/null;  assert_code "GET 통계 yearly" "200"
req GET "/api/ptl/stat/2026"               > /dev/null;  assert_code "GET 통계 (2026)" "200"
req GET "/api/ptl/sim"                     > /dev/null;  assert_code "GET 시뮬 목록"   "200"

req POST "/api/ptl/sim" '{"simNm":"S7 시뮬","scopeSeCd":"ALL","baseYr":"2026","inputJson":"{\"carbonPrice\":25,\"growthRate\":3.5,\"safRatio\":2.0}"}'
assert_code "POST 시뮬 신규" "200"
SIM_ID=$(echo "$LAST_BODY" | grep -oE '"simId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  SIM ID: ${SIM_ID:-(추출불가)}"
if [[ -n "${SIM_ID:-}" ]]; then
  req GET "/api/ptl/sim/$SIM_ID"        > /dev/null;  assert_code "GET 시뮬 상세" "200"
  req POST "/api/ptl/sim/$SIM_ID/run" "{}" > /dev/null
  assert_code "POST 시뮬 run (재실행)" "200"
  req DELETE "/api/ptl/sim/$SIM_ID" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE 시뮬"
  else
    FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("DELETE 시뮬 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} DELETE ($LAST_CODE)"
  fi
fi

section "S7.3 CCR 추출 (외부 연계 자료)"
req GET "/api/ptl/ccr" > /dev/null;  assert_code "GET CCR 목록" "200"
req POST "/api/ptl/ccr/extract" '{"rprtYr":"2026","extrScopeCd":"ALL"}' > /dev/null
if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "400" || "$LAST_CODE" == "409" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} POST CCR 추출 ($LAST_CODE)"
else
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("CCR 추출 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} CCR ($LAST_CODE)"
fi

section "S7.4 감사로그 (운영기록 일관성)"
req GET "/api/ptl/actn?pageSize=5" > /dev/null;  assert_code "GET 감사로그" "200"

section "S7.5 메인 대시보드 진입 + 매뉴얼"
req GET "/main"    > /dev/null;  assert_code "GET 메인 대시보드" "200"
req GET "/manual"  > /dev/null;  assert_code "GET 매뉴얼"        "200"

section "S7.6 화면 콘텐츠 검증"
check_html() {
  local path="$1" needle="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$needle"; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $desc";
  else FAIL=$((FAIL+1)); FAILED_NAMES+=("$path '$needle'"); echo -e "  ${RED}✗${RESET} $desc"; fi
}
check_html "/ptl/workflow" "워크플로우\|Workflow" "워크플로우 화면"
check_html "/ptl/stat"     "통계\|배출"           "통계 화면"
check_html "/ptl/sim"      "시뮬\|상쇄"           "시뮬 화면"
check_html "/ptl/ccr"      "CCR"                  "CCR 화면"
check_html "/ptl/actn"     "감사로그\|actn"        "감사로그 화면"
check_html "/ai/console"   "sLLM"                  "AI 콘솔 (2차) sLLM"
check_html "/ai/console"   "2차년도\|2차"          "AI 콘솔 — 2차년도 명시"

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
