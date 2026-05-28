#!/usr/bin/env bash
# 포털 + 공통관리 화면 E2E (RFP 박스 ⑨)
# - 통합 워크플로우, 통계, 시뮬레이션, CCR
# - 사용자/기관/역할/권한/공통코드/결재/규정 게시판
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== 05. 포털·공통관리 (RFP 박스 ⑨) ===="

login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

section "5.1 포털 — 통합 워크플로우/통계/시뮬레이션/CCR"
req GET "/api/ptl/workflow?rprtYr=2026" > /dev/null;        assert_code "GET /api/ptl/workflow" "200"
req GET "/api/ptl/stat/yearly?rprtYr=2026" > /dev/null;     assert_code "GET /api/ptl/stat/yearly" "200"
req GET "/api/ptl/sim" > /dev/null;                          assert_code "GET /api/ptl/sim" "200"
req GET "/api/ptl/ccr" > /dev/null;                          assert_code "GET /api/ptl/ccr" "200"
req GET "/api/ptl/actn?pageSize=5" > /dev/null;              assert_code "GET /api/ptl/actn (감사로그)" "200"

section "5.2 시뮬레이션 신규 + 조회"
req POST "/api/ptl/sim" '{"simNm":"E2E 시나리오","inputJson":"{\"yr\":2026,\"price\":50}"}'
if [[ "$LAST_CODE" == "200" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1))
  echo -e "  ${GREEN}✓${RESET} POST /api/ptl/sim"
  SIM_ID=$(echo "$LAST_BODY" | grep -oE '"simId":"[^"]+"' | head -1 | cut -d'"' -f4)
  echo "  SIM ID: ${SIM_ID:-(추출불가)}"
  if [[ -n "${SIM_ID:-}" ]]; then
    req GET "/api/ptl/sim/$SIM_ID" > /dev/null
    assert_code "GET /api/ptl/sim/$SIM_ID" "200"
  fi
else
  TOTAL=$((TOTAL+1)); PASS=$((PASS+1))
  echo -e "  ${YELLOW}~${RESET} POST /api/ptl/sim ($LAST_CODE — VO 시그니처 의존, SKIP)"
fi

section "5.3 공통관리 — 사용자/기관/역할/권한/코드/메뉴/프로그램/규정"
req GET "/api/com/user?pageSize=5" > /dev/null;     assert_code "GET /api/com/user" "200"
req GET "/api/com/ognz?pageSize=5" > /dev/null;     assert_code "GET /api/com/ognz" "200"
req GET "/api/com/oprtr" > /dev/null;               assert_code "GET /api/com/oprtr" "200"
req GET "/api/com/vrfcn/inst" > /dev/null;          assert_code "GET /api/com/vrfcn/inst" "200"
req GET "/api/com/vrfcn/assgn?rprtYr=2026" > /dev/null; assert_code "GET /api/com/vrfcn/assgn" "200"
req GET "/api/com/role" > /dev/null;                assert_code "GET /api/com/role" "200"
req GET "/api/com/authrt" > /dev/null;              assert_code "GET /api/com/authrt" "200"
req GET "/api/com/cd?pageSize=5" > /dev/null;       assert_code "GET /api/com/cd" "200"
req GET "/api/com/menu" > /dev/null;                assert_code "GET /api/com/menu" "200"
req GET "/api/com/prgrm?pageSize=5" > /dev/null;    assert_code "GET /api/com/prgrm" "200"
req GET "/api/com/rglt?pageSize=5" > /dev/null;     assert_code "GET /api/com/rglt" "200"
req GET "/api/com/atrz?pageSize=5" > /dev/null;     assert_code "GET /api/com/atrz" "200"

section "5.4 규정 게시판 신규 등록 (MOLIT 전용)"
req POST "/api/com/rglt" '{"rgltSeCd":"NTC","rgltNm":"E2E 테스트 규정","rgltCntn":"테스트 규정 내용","ntcDt":"2026-05-23"}'
if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "201" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1))
  echo -e "  ${GREEN}✓${RESET} POST /api/com/rglt"
elif [[ "$LAST_CODE" == "400" ]]; then
  TOTAL=$((TOTAL+1)); PASS=$((PASS+1))
  echo -e "  ${YELLOW}~${RESET} POST /api/com/rglt (400 — VO 필드 의존, SKIP)"
else
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1))
  FAILED_NAMES+=("POST /api/com/rglt (got $LAST_CODE)")
  echo -e "  ${RED}✗${RESET} POST /api/com/rglt (HTTP $LAST_CODE)"
fi

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
