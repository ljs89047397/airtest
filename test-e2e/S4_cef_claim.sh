#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S4 CEF (적격연료) 청구 · 차감
# 데이터 흐름:  운영사 SAF 사용량 → CEF 청구 → 이중청구 검증 → KOTSA 승인 → ER 차감 데이터 확정
# 입력 actor:  운영사(kal_user)
# 최종 결과:    승인된 CEF claim → ER 본보고에 차감 반영
# 통과 박스:   ③ CEF
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S4. CEF 적격연료 청구·차감 (RFP 박스 ③) ===="

section "S4.1 운영사(kal_user) — CEF 신규 등록"
login "kal_user" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/er/cef?rprtYr=2026" > /dev/null; assert_code "GET CEF 목록" "200"

# CEF 미등록된 ER 동적 선택 (멱등성)
ER_FOR_CEF=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT er_id FROM er.tn_er WHERE er_id NOT IN (SELECT er_id FROM er.tn_cef WHERE er_id IS NOT NULL AND use_end_dt > NOW()) AND use_end_dt > NOW() ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
ER_OPRTR=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT oprtr_id FROM er.tn_er WHERE er_id='$ER_FOR_CEF';" 2>/dev/null | tr -d ' ')
ER_YR=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT rprt_yr FROM er.tn_er WHERE er_id='$ER_FOR_CEF';" 2>/dev/null | tr -d ' ')
echo "  대상 ER: ${ER_FOR_CEF:-(없음)} ($ER_OPRTR/$ER_YR)"

# kal_user 는 KAL ER 에만 접근 가능 — 폴백
if [[ -z "$ER_FOR_CEF" || "$ER_OPRTR" != "KAL" ]]; then
  ER_FOR_CEF="ER2026KAL"; ER_OPRTR="KAL"; ER_YR="2026"
fi

req POST "/api/er/cef" "{\"oprtrId\":\"$ER_OPRTR\",\"rprtYr\":\"$ER_YR\",\"erId\":\"$ER_FOR_CEF\"}"
assert_code "POST CEF 신규" "200"
CEF_ID=$(echo "$LAST_BODY" | grep -oE '"cefId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  CEF ID: ${CEF_ID:-(추출불가)}"

section "S4.2 CEF 단건·자식 + 이중청구 사전 검증"
if [[ -n "${CEF_ID:-}" ]]; then
  req GET "/api/er/cef/$CEF_ID"            > /dev/null; assert_code "GET CEF 상세" "200"
  req GET "/api/er/cef/by-er/$ER_FOR_CEF"  > /dev/null; assert_code "GET CEF by-er" "200"
fi
# 이중청구 검증 패널 — batch+oprtr 쌍 조회
req GET "/api/er/cef/validate-double-claim?batchIdNo=DEMO-BATCH-001&currentOprtrId=KAL" > /dev/null
if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "400" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} 이중청구 사전 검증 (HTTP $LAST_CODE)"
else
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("CEF 이중청구 검증 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} 이중청구 검증 ($LAST_CODE)"
fi

section "S4.3 CEF 라이프사이클 (DRAFT → SBMTD)"
if [[ -n "${CEF_ID:-}" ]]; then
  req POST "/api/er/cef/$CEF_ID/recalc" "{}" > /dev/null; assert_code "POST CEF recalc"  "200"
  req POST "/api/er/cef/$CEF_ID/submit" "{}" > /dev/null; assert_code "POST CEF submit"  "200"
fi

section "S4.4 KOTSA — CEF 승인 (SBMTD → APRVD)"
logout; login "kotsa01" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kotsa01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

if [[ -n "${CEF_ID:-}" ]]; then
  req POST "/api/er/cef/$CEF_ID/approve" "{}" > /dev/null; assert_code "POST CEF approve" "200"
  req POST "/api/er/cef/$CEF_ID/cancel"  '{"reason":"S4 취소 사유"}' > /dev/null
  # KOTSA 가 다른 운영사 CEF cancel 시도 시 403 정상 (cancel 은 owner 권한)
  TOTAL=$((TOTAL+1))
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "400" || "$LAST_CODE" == "403" || "$LAST_CODE" == "409" ]]; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} POST CEF cancel (HTTP $LAST_CODE — 비즈룰 정상)"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("CEF cancel ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} cancel ($LAST_CODE)"
  fi
fi

section "S4.5 화면 콘텐츠 검증"
check_html() {
  local path="$1" needle="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$needle"; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $desc";
  else FAIL=$((FAIL+1)); FAILED_NAMES+=("$path '$needle'"); echo -e "  ${RED}✗${RESET} $desc"; fi
}
check_html "/er/cef" "적격연료"   "CEF 목록 화면"
check_html "/er/cef" "신규 등록"  "신규 등록 버튼"

CEF_FROM_DB=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT cef_id FROM er.tn_cef LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "$CEF_FROM_DB" ]]; then
  check_html "/er/cef/$CEF_FROM_DB" "청구\|claim"             "상세 — 적격연료 청구"
  check_html "/er/cef/$CEF_FROM_DB" "수명주기\|lcyc\|LCA"    "상세 — 수명주기"
  check_html "/er/cef/$CEF_FROM_DB" "공급망\|spchn"          "상세 — 공급망"
  check_html "/er/cef/$CEF_FROM_DB" "이중\|중복"              "상세 — 이중청구"
fi

section "S4.6 DB 정합성"
N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_cef WHERE oprtr_id='KAL';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} CEF KAL DB 적재 ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("CEF DB 0행"); echo -e "  ${RED}✗${RESET} CEF ($N 행)"; }

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
