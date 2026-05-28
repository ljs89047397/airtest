#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S2 EMP 라이프사이클
# 데이터 흐름:  운영사 EMP 작성 → 제출 → KOTSA 검토 → KOTSA 권고 → MOLIT 승인
# 입력 actor:  운영사(kal_user)
# 최종 결과:    APRVD EMP → 차년도 ER 작성 자격 확보
# 통과 박스:   ① EMP
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S2. EMP 라이프사이클 (RFP 박스 ①) ===="

section "S2.1 운영사(kal_user) — EMP 신규 작성 (새 버전)"
login "kal_user" "admin1234!" || { echo "kal_user 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req POST "/api/emp/plan/EMP2026KAL/new-version" "{}"
assert_code "POST EMP 새 버전 생성" "200"
NEW_EMP_ID=$(echo "$LAST_BODY" | grep -oE '"empPlanId":"[^"]+"' | head -1 | cut -d'"' -f4)
if [[ -z "$NEW_EMP_ID" ]]; then
  req POST "/api/emp/plan" '{"oprtrId":"KAL","rprtYr":"2027","empVer":"1.0"}'
  assert_code "POST EMP 신규 등록 (fallback)" "200"
  NEW_EMP_ID=$(echo "$LAST_BODY" | grep -oE '"empPlanId":"[^"]+"' | head -1 | cut -d'"' -f4)
fi
echo "  신규 EMP ID: ${NEW_EMP_ID:-(추출불가)}"

section "S2.2 EMP 상세 조회 + 자식 도메인 헬스 (8종)"
if [[ -n "$NEW_EMP_ID" ]]; then
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_code "GET EMP 상세" "200"
  assert_json_contains "응답에 empPlanId 포함" "$NEW_EMP_ID"
fi
TARGET="${NEW_EMP_ID:-EMP2026KAL}"
for child in acft cnct info cntry-pair co2-calc co2-detail data-ctrl risk; do
  req GET "/api/emp/plan/$TARGET/$child" > /dev/null
  assert_code "GET EMP/$child" "200"
done

section "S2.3 DRAFT 수정 → 제출 (DRAFT→SBMTD)"
if [[ -n "$NEW_EMP_ID" ]]; then
  req PUT "/api/emp/plan/$NEW_EMP_ID" "{\"empPlanId\":\"$NEW_EMP_ID\",\"oprtrId\":\"KAL\",\"rprtYr\":\"2026\",\"empVer\":\"2.0\",\"rmrk\":\"S2 시나리오\",\"sigChgYn\":\"N\"}"
  assert_code "PUT EMP 수정" "200"
  req POST "/api/emp/plan/$NEW_EMP_ID/submit" "{}"
  assert_code "POST EMP submit" "200"
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_json_contains "상태 = SBMTD" '"empStCd":"SBMTD"'
fi

section "S2.4 KOTSA — 검토 → 권고 (SBMTD→RVWNG→RCMDD)"
logout
login "kotsa01" "admin1234!" || { echo "kotsa01 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} kotsa01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

if [[ -n "$NEW_EMP_ID" ]]; then
  req POST "/api/emp/plan/$NEW_EMP_ID/review" "{}"
  assert_code "POST EMP review (KOTSA 검토)" "200"
  req POST "/api/emp/plan/$NEW_EMP_ID/recommend" "{}"
  assert_code "POST EMP recommend (KOTSA 권고)" "200"
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_json_contains "상태 = RCMDD" '"empStCd":"RCMDD"'
fi

section "S2.5 MOLIT — 최종 승인 (RCMDD→APRVD)"
logout
login "admin01" "admin1234!" || { echo "admin01 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

if [[ -n "$NEW_EMP_ID" ]]; then
  req POST "/api/emp/plan/$NEW_EMP_ID/approve" "{}"
  assert_code "POST EMP approve (MOLIT 승인)" "200"
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_json_contains "최종 상태 = APRVD" '"empStCd":"APRVD"'
fi

section "S2.6 화면 콘텐츠 검증 (운영사·항공기·국가쌍·품질통제·법정서식)"
check_html() {
  local path="$1" needle="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$needle"; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $desc ('$needle')"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("$path missing '$needle'"); echo -e "  ${RED}✗${RESET} $desc ('$needle' 누락)"
  fi
}
check_html "/emp/plan"            "버전"                                   "EMP 목록 — 버전관리"
check_html "/emp/plan/EMP2026KAL" "항공기"                                  "상세 — 항공기"
check_html "/emp/plan/EMP2026KAL" "배출량 계산\|계산방법"                   "상세 — 계산방법"
check_html "/emp/plan/EMP2026KAL" "데이터 품질\|품질통제\|데이터관리"       "상세 — 품질통제"
check_html "/emp/plan/EMP2026KAL" "법정\|출력"                              "상세 — 법정서식"

section "S2.7 DB 정합성 — APRVD 영속 + 변경이력"
if [[ -n "$NEW_EMP_ID" ]]; then
  ST=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT emp_st_cd FROM emp.tn_emp_plan WHERE emp_plan_id='$NEW_EMP_ID';" 2>/dev/null | tr -d ' ')
  TOTAL=$((TOTAL+1))
  [[ "$ST" == "APRVD" ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} DB 상태 = APRVD"; } \
    || { FAIL=$((FAIL+1)); FAILED_NAMES+=("DB 상태 = $ST"); echo -e "  ${RED}✗${RESET} DB 상태 = $ST"; }

  HC=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM emp.th_emp_chg_hstry WHERE emp_plan_id='$NEW_EMP_ID';" 2>/dev/null | tr -d ' ')
  TOTAL=$((TOTAL+1))
  [[ "$HC" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 변경이력 적재 ($HC 행)"; } \
    || { FAIL=$((FAIL+1)); FAILED_NAMES+=("변경이력 0행"); echo -e "  ${RED}✗${RESET} 변경이력 ($HC)"; }
fi

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
