#!/usr/bin/env bash
# 스모크 테스트: 모든 사이드바 URL 200 OK 회귀 + 핵심 API 헬스
# 사용: BASE_URL=http://localhost:8080 ./01_smoke.sh
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"

echo "==== 01. SMOKE 테스트 ===="
echo "BASE_URL=$BASE_URL"

section "1.1 로그인 (admin01 / MOLIT_ADMIN)"
if login "admin01" "admin1234!"; then
  echo -e "  ${GREEN}✓${RESET} admin01 로그인 성공"
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1))
else
  echo -e "  ${RED}✗${RESET} admin01 로그인 실패"
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1))
  print_summary; exit 1
fi

section "1.2 사이드바 화면 33개 URL 회귀 (200 OK 확인)"
URLS=(
  /main /emp/plan /er/list /er/cef /er/eucr /er/oom /er/oom/qchk
  /vr/list /vr/rprt
  /saf/dashboard /saf/cert /saf/batch /saf/airprt /saf/airprt/fuel /saf/airprt/purch /saf/mntr
  /ptl/workflow /ptl/stat /ptl/sim /ptl/ccr /ptl/actn
  /ai/console
  /com/user /com/ognz /com/oprtr /com/vrfcn /com/role /com/authrt
  /com/cd /com/atrz /com/rglt /com/menu /com/prgrm
)
for u in "${URLS[@]}"; do
  req GET "$u" > /dev/null
  assert_code "GET $u" "200"
done

section "1.3 핵심 API 헬스 (목록 조회 정상 응답)"
req GET "/health" > /dev/null
assert_code "GET /health" "200"

req GET "/api/emp/plan?rprtYr=2026" > /dev/null
assert_code "GET /api/emp/plan?rprtYr=2026" "200"

req GET "/api/er/rprt?rprtYr=2026" > /dev/null
assert_code "GET /api/er/rprt?rprtYr=2026" "200"

req GET "/api/vr?rprtYr=2026" > /dev/null
assert_code "GET /api/vr?rprtYr=2026" "200"

req GET "/api/er/cef?rprtYr=2026" > /dev/null
assert_code "GET /api/er/cef?rprtYr=2026" "200"

req GET "/api/er/eucr?rprtYr=2026" > /dev/null
assert_code "GET /api/er/eucr?rprtYr=2026" "200"

req GET "/api/er/oom?rprtYr=2026" > /dev/null
assert_code "GET /api/er/oom?rprtYr=2026" "200"

req GET "/api/saf/cert" > /dev/null
assert_code "GET /api/saf/cert" "200"

req GET "/api/saf/batch" > /dev/null
assert_code "GET /api/saf/batch" "200"

req GET "/api/saf/mntr/blnd/all?rprtYr=2026" > /dev/null
assert_code "GET /api/saf/mntr/blnd/all?rprtYr=2026" "200"

req GET "/api/com/oprtr" > /dev/null
assert_code "GET /api/com/oprtr" "200"

req GET "/api/com/user" > /dev/null
assert_code "GET /api/com/user" "200"

req GET "/api/com/role" > /dev/null
assert_code "GET /api/com/role" "200"

req GET "/api/com/cd" > /dev/null
assert_code "GET /api/com/cd" "200"

req GET "/api/com/rglt" > /dev/null
assert_code "GET /api/com/rglt" "200"

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
