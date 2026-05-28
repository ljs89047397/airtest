#!/usr/bin/env bash
# E2E 테스트 공통 라이브러리 — CSRF 로그인·세션 쿠키 관리·요청 래퍼·assert 헬퍼
set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
COOKIE_FILE="${COOKIE_FILE:-/tmp/icas-e2e-cookies.txt}"
CURL=/usr/bin/curl

# 통계
TOTAL=0
PASS=0
FAIL=0
declare -a FAILED_NAMES=()

CYAN='\033[36m'; GREEN='\033[32m'; RED='\033[31m'; YELLOW='\033[33m'; RESET='\033[0m'

# CSRF 토큰 저장 (전역)
CSRF_TOKEN=""

# 로그인 — userId/password 받아 세션 + CSRF 적재
login() {
  local userId="$1" password="$2"
  rm -f "$COOKIE_FILE"
  local html
  html=$($CURL -s -c "$COOKIE_FILE" "$BASE_URL/login")
  CSRF_TOKEN=$(echo "$html" | grep -oE 'name="_csrf" content="[^"]+"' | sed 's/.*content="\([^"]*\)"/\1/' | head -1)
  if [[ -z "$CSRF_TOKEN" ]]; then echo -e "${RED}로그인 페이지 CSRF 토큰 추출 실패${RESET}" >&2; return 1; fi
  local code
  code=$($CURL -s -b "$COOKIE_FILE" -c "$COOKIE_FILE" -X POST "$BASE_URL/api/com/auth/login" \
      -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
      --data-urlencode "userId=$userId" \
      --data-urlencode "password=$password" \
      --data-urlencode "_csrf=$CSRF_TOKEN" \
      -o /dev/null -w "%{http_code}")
  # 로그인 성공 후 CSRF 재발급
  html=$($CURL -s -b "$COOKIE_FILE" "$BASE_URL/main")
  local new_csrf
  new_csrf=$(echo "$html" | grep -oE 'name="_csrf" content="[^"]+"' | sed 's/.*content="\([^"]*\)"/\1/' | head -1)
  [[ -n "$new_csrf" ]] && CSRF_TOKEN="$new_csrf"
  [[ "$code" == "200" ]] && return 0 || return 1
}

logout() {
  $CURL -s -b "$COOKIE_FILE" -X POST "$BASE_URL/api/com/auth/logout" \
      -H "X-XSRF-TOKEN: $CSRF_TOKEN" -o /dev/null
  rm -f "$COOKIE_FILE"
}

# HTTP 메서드 래퍼: req <GET|POST|PUT|DELETE> <path> [body-json] → 응답 본문 출력, code 는 LAST_CODE 변수
LAST_CODE=""
LAST_BODY=""
req() {
  local method="$1" path="$2" body="${3:-}"
  local args=(-s -b "$COOKIE_FILE" -X "$method" "$BASE_URL$path" -H "X-XSRF-TOKEN: $CSRF_TOKEN")
  if [[ -n "$body" ]]; then args+=(-H "Content-Type: application/json" -d "$body"); fi
  args+=(-w '\n<<CODE:%{http_code}>>')
  local resp
  resp=$($CURL "${args[@]}")
  LAST_CODE=$(echo "$resp" | grep -oE '<<CODE:[0-9]+>>' | grep -oE '[0-9]+')
  LAST_BODY=$(echo "$resp" | sed 's/<<CODE:[0-9]*>>//')
  echo "$LAST_BODY"
}

# assert helpers
assert_code() {
  local name="$1" expected="$2"
  TOTAL=$((TOTAL+1))
  if [[ "$LAST_CODE" == "$expected" ]]; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $name (HTTP $LAST_CODE)"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("$name (expected $expected, got $LAST_CODE)")
    echo -e "  ${RED}✗${RESET} $name (expected $expected, got $LAST_CODE)"
    echo "    body: $(echo "$LAST_BODY" | head -c 200)"
  fi
}

assert_json_contains() {
  local name="$1" needle="$2"
  TOTAL=$((TOTAL+1))
  if echo "$LAST_BODY" | grep -q "$needle"; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $name (contains '$needle')"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("$name (missing '$needle')")
    echo -e "  ${RED}✗${RESET} $name (missing '$needle')"
    echo "    body: $(echo "$LAST_BODY" | head -c 200)"
  fi
}

section() {
  echo
  echo -e "${CYAN}── $* ──${RESET}"
}

print_summary() {
  echo
  echo "════════════════════════════════════════════════"
  echo -e "${CYAN}E2E 결과${RESET}: 총 $TOTAL · ${GREEN}통과 $PASS${RESET} · ${RED}실패 $FAIL${RESET}"
  if [[ $FAIL -gt 0 ]]; then
    echo "실패 목록:"
    for n in "${FAILED_NAMES[@]}"; do echo "  - $n"; done
  fi
  echo "════════════════════════════════════════════════"
}
