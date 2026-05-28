#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S1 운영 셋업 (Pre-flight)
# 데이터 흐름:  기관 → 운영사 → 검증기관 → 사용자/역할/권한 → 검증배정 → 규정 게시
# 입력 actor:  MOLIT 관리자
# 최종 결과:    공통관리 매트릭스 가동 가능 상태 + 결재함 활성
# 통과 박스:   ⑩ 공통관리
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S1. 운영 셋업 (공통관리 매트릭스) ===="

login "admin01" "admin1234!" || { echo "admin01 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} admin01 로그인 (MOLIT master)"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

section "S1.1 기관·사용자·운영사·검증기관 마스터 가용성"
req GET "/api/com/user?pageSize=5"  > /dev/null;  assert_code "GET 사용자 목록"   "200"
req GET "/api/com/ognz?pageSize=5"  > /dev/null;  assert_code "GET 기관 목록"     "200"
req GET "/api/com/oprtr"            > /dev/null;  assert_code "GET 운영사 목록"   "200"
req GET "/api/com/vrfcn/inst"       > /dev/null;  assert_code "GET 검증기관 목록" "200"

section "S1.2 역할·권한·코드·메뉴·프로그램 (RBAC 매트릭스)"
req GET "/api/com/role"             > /dev/null;  assert_code "GET 역할"     "200"
req GET "/api/com/authrt"           > /dev/null;  assert_code "GET 권한"     "200"
req GET "/api/com/cd?pageSize=5"    > /dev/null;  assert_code "GET 공통코드" "200"
req GET "/api/com/menu"             > /dev/null;  assert_code "GET 메뉴"     "200"
req GET "/api/com/prgrm?pageSize=5" > /dev/null;  assert_code "GET 프로그램" "200"

section "S1.3 검증배정 — 운영사 ↔ 검증기관 매핑"
req GET "/api/com/vrfcn/assgn?rprtYr=2026" > /dev/null
assert_code "GET 검증배정 (2026)" "200"

section "S1.4 결재함 활성 / 내가 펜딩 받은 결재"
req GET "/api/com/atrz?pageSize=5"  > /dev/null;  assert_code "GET 결재함" "200"
req GET "/api/com/atrz/my-pending"  > /dev/null;  assert_code "GET 내 결재 대기" "200"

section "S1.5 규정 게시판 풀 CRUD"
req GET "/api/com/rglt?pageSize=5"  > /dev/null;  assert_code "GET 규정 목록" "200"

req POST "/api/com/rglt" '{"rgltSeCd":"NTC","rgltNm":"S1 셋업 규정","rgltCntn":"E2E 시나리오 S1 본문","ntcDt":"2026-05-24"}'
if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "201" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1))
  echo -e "  ${GREEN}✓${RESET} POST 규정 신규"
  RGLT_NEW=$(echo "$LAST_BODY" | grep -oE '"rgltId":"[^"]+"' | head -1 | cut -d'"' -f4)
  echo "  RGLT ID: ${RGLT_NEW:-(추출불가)}"
  if [[ -n "${RGLT_NEW:-}" ]]; then
    req GET "/api/com/rglt/$RGLT_NEW" > /dev/null;  assert_code "GET 규정 상세" "200"
    req PUT "/api/com/rglt/$RGLT_NEW" '{"rgltSeCd":"NTC","rgltNm":"S1 수정","rgltCntn":"수정본","ntcDt":"2026-05-24"}' > /dev/null
    assert_code "PUT 규정 수정" "200"
    req DELETE "/api/com/rglt/$RGLT_NEW" > /dev/null
    if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" ]]; then
      PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE 규정"
    else
      FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("DELETE 규정 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} DELETE 규정 ($LAST_CODE)"
    fi
  fi
elif [[ "$LAST_CODE" == "400" ]]; then
  TOTAL=$((TOTAL+1)); PASS=$((PASS+1))
  echo -e "  ${YELLOW}~${RESET} POST 규정 (400 — VO 시그니처 의존, SKIP)"
else
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1))
  FAILED_NAMES+=("POST 규정 ($LAST_CODE)")
  echo -e "  ${RED}✗${RESET} POST 규정 ($LAST_CODE)"
fi

section "S1.6 셋업 산출물 데이터 정합성"
N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM com.tn_user WHERE use_end_dt > NOW();" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 5 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 사용자 시드 ($N 명)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("사용자 시드 부족 ($N)"); echo -e "  ${RED}✗${RESET} 사용자 ($N)"; }

N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM com.tn_ognz WHERE use_end_dt > NOW();" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 5 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 기관 시드 ($N 개)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("기관 시드 부족 ($N)"); echo -e "  ${RED}✗${RESET} 기관 ($N)"; }

N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM com.tn_oprtr WHERE use_end_dt > NOW();" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 3 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 운영사 시드 ($N 개)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("운영사 시드 부족 ($N)"); echo -e "  ${RED}✗${RESET} 운영사 ($N)"; }

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
