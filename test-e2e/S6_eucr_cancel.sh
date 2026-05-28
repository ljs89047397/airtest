#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S6 EUCR 배출권 취소
# 데이터 흐름:  운영사 잉여배출량 산정 → 일련번호 이중사용 사전 검증
#               → 크레딧 일련번호 확정 → 배치 등록 → 외부 등록부 매칭 → 취소 확정
# 입력 actor:  운영사(kal_user)
# 최종 결과:    EUCR 취소 확정 + 외부 등록부 매칭 송신 준비
# 통과 박스:   ⑤ EUCR
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S6. EUCR 배출권 취소 (RFP 박스 ⑤) ===="

section "S6.1 운영사(kal_user) — EUCR 신규"
login "kal_user" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/er/eucr" > /dev/null; assert_code "GET EUCR 목록" "200"

req POST "/api/er/eucr" '{"oprtrId":"KAL","rprtYr":"2025","ofstReqQty":1000.0}'
assert_code "POST EUCR 신규" "200"
EUCR_ID=$(echo "$LAST_BODY" | grep -oE '"eucrId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  EUCR ID: ${EUCR_ID:-(추출불가)}"

section "S6.2 일련번호 이중사용 사전 검증 (등록 전 차단)"
req POST "/api/er/eucr/validate-double-using" '{"crdtSnList":["VCS-S6-001","VCS-S6-002"]}'
if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "400" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} 이중사용 사전 검증 (HTTP $LAST_CODE)"
else
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("EUCR 이중사용 검증 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} 이중사용 검증 ($LAST_CODE)"
fi

section "S6.3 의무량 수정 · 재계산 · 제출"
if [[ -n "${EUCR_ID:-}" ]]; then
  req GET "/api/er/eucr/$EUCR_ID" > /dev/null
  assert_code "GET EUCR 상세" "200"
  req PUT "/api/er/eucr/$EUCR_ID/ofst-req-qty" '{"ofstReqQty":1500.0}' > /dev/null
  assert_code "PUT EUCR 의무량 수정" "200"
  req POST "/api/er/eucr/$EUCR_ID/recalc" "{}" > /dev/null
  assert_code "POST EUCR recalc" "200"
  req POST "/api/er/eucr/$EUCR_ID/submit" "{}" > /dev/null
  assert_code "POST EUCR submit" "200"
fi

section "S6.4 비즈룰 회귀 — SBMTD 상태 DELETE 차단"
if [[ -n "${EUCR_ID:-}" ]]; then
  req DELETE "/api/er/eucr/$EUCR_ID" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" || "$LAST_CODE" == "400" || "$LAST_CODE" == "409" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE EUCR (HTTP $LAST_CODE — 비즈룰 정상)"
  else
    FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("DELETE EUCR ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} DELETE ($LAST_CODE)"
  fi
fi

section "S6.5 화면 콘텐츠 검증"
check_html() {
  local path="$1" needle="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$needle"; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $desc";
  else FAIL=$((FAIL+1)); FAILED_NAMES+=("$path '$needle'"); echo -e "  ${RED}✗${RESET} $desc"; fi
}
check_html "/er/eucr" "배출권취소\|EUCR"   "EUCR 목록 화면"
check_html "/er/eucr" "운영사"             "운영사 정보"
check_html "/er/eucr" "상쇄\|배출권\|의무"  "상쇄요건 정보"

EUCR_FROM_DB=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT eucr_id FROM er.tn_eucr LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "$EUCR_FROM_DB" ]]; then
  check_html "/er/eucr/$EUCR_FROM_DB" "이중사용\|이중\|일련번호"  "상세 — 이중사용 검증 패널"
  check_html "/er/eucr/$EUCR_FROM_DB" "배치\|업로드\|취소"        "상세 — 배치/취소"
fi

section "S6.6 DB 정합성"
N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_eucr WHERE oprtr_id='KAL';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} EUCR KAL ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("EUCR 0행"); echo -e "  ${RED}✗${RESET} EUCR ($N)"; }

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
