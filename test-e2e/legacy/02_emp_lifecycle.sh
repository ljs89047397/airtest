#!/usr/bin/env bash
# EMP (배출량 모니터링 계획서) 라이프사이클 E2E
# 시나리오: AIRLINE 작성 → 수정 → 제출 → KOTSA 검토·권고 → MOLIT 승인
# RFP 박스 ① EMP 관리
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== 02. EMP 라이프사이클 (RFP 박스 ①) ===="

# ── AIRLINE 으로 작성 단계 ──────────────────────────────
section "2.1 AIRLINE (kal_user) 로 신규 EMP 작성"
login "kal_user" "admin1234!" || { echo "로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# 시드된 KAL EMP 가 이미 APRVD 상태(EMP2026KAL) — 새 버전 생성 시나리오
req POST "/api/emp/plan/EMP2026KAL/new-version" "{}"
assert_code "POST /api/emp/plan/EMP2026KAL/new-version (새 버전 생성)" "200"
NEW_EMP_ID=$(echo "$LAST_BODY" | grep -oE '"empPlanId":"[^"]+"' | head -1 | cut -d'"' -f4)
if [[ -z "$NEW_EMP_ID" ]]; then
  echo -e "  ${YELLOW}!${RESET} 신규 empPlanId 추출 실패 — 시드 또는 권한 이슈일 수 있음"
  # 폴백: KAL 의 DRAFT EMP 가 없으니 신규 등록 시도
  req POST "/api/emp/plan" '{"oprtrId":"KAL","rprtYr":"2027","empVer":"1.0"}'
  assert_code "POST /api/emp/plan (신규 등록 fallback)" "200"
  NEW_EMP_ID=$(echo "$LAST_BODY" | grep -oE '"empPlanId":"[^"]+"' | head -1 | cut -d'"' -f4)
fi
echo "  신규 EMP ID: ${NEW_EMP_ID:-(추출불가)}"

section "2.2 단건 조회"
if [[ -n "$NEW_EMP_ID" ]]; then
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_code "GET /api/emp/plan/$NEW_EMP_ID" "200"
  assert_json_contains "조회 응답에 empPlanId 포함" "$NEW_EMP_ID"
fi

section "2.3 수정 (DRAFT 상태에서)"
if [[ -n "$NEW_EMP_ID" ]]; then
  req PUT "/api/emp/plan/$NEW_EMP_ID" "{\"empPlanId\":\"$NEW_EMP_ID\",\"oprtrId\":\"KAL\",\"rprtYr\":\"2026\",\"empVer\":\"2.0\",\"rmrk\":\"E2E 테스트 비고\",\"sigChgYn\":\"N\"}"
  assert_code "PUT /api/emp/plan/$NEW_EMP_ID (수정)" "200"
fi

section "2.4 제출 (DRAFT → SBMTD)"
if [[ -n "$NEW_EMP_ID" ]]; then
  req POST "/api/emp/plan/$NEW_EMP_ID/submit" "{}"
  assert_code "POST /api/emp/plan/$NEW_EMP_ID/submit" "200"
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_json_contains "제출 후 상태=SBMTD" '"empStCd":"SBMTD"'
fi

# ── KOTSA 로 검토 단계 ───────────────────────────────────
section "2.5 KOTSA (kotsa01) 로 전환 - 검토 → 권고"
logout
login "kotsa01" "admin1234!" || { echo "kotsa01 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} kotsa01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

if [[ -n "$NEW_EMP_ID" ]]; then
  req POST "/api/emp/plan/$NEW_EMP_ID/review" "{}"
  assert_code "POST .../review (KOTSA 검토 진입)" "200"
  req POST "/api/emp/plan/$NEW_EMP_ID/recommend" "{}"
  assert_code "POST .../recommend (KOTSA → MOLIT 권고)" "200"
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_json_contains "권고 후 상태=RCMDD" '"empStCd":"RCMDD"'
fi

# ── MOLIT 승인 단계 ─────────────────────────────────────
section "2.6 MOLIT (admin01) 로 최종 승인"
logout
login "admin01" "admin1234!" || { echo "admin01 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

if [[ -n "$NEW_EMP_ID" ]]; then
  req POST "/api/emp/plan/$NEW_EMP_ID/approve" "{}"
  assert_code "POST .../approve (MOLIT 승인)" "200"
  req GET "/api/emp/plan/$NEW_EMP_ID" > /dev/null
  assert_json_contains "승인 후 상태=APRVD" '"empStCd":"APRVD"'
fi

# ── 자식 도메인 CRUD 회귀 (acft/cnct/cntry/co2/risk/info/ctrl) ──
section "2.7 EMP 자식 도메인 API 헬스 (활성 EMP=$NEW_EMP_ID)"
TARGET_EMP="${NEW_EMP_ID:-EMP2026AAR}"
req GET "/api/emp/plan/$TARGET_EMP/acft" > /dev/null;         assert_code "GET .../acft" "200"
req GET "/api/emp/plan/$TARGET_EMP/cnct" > /dev/null;         assert_code "GET .../cnct" "200"
req GET "/api/emp/plan/$TARGET_EMP/info" > /dev/null;         assert_code "GET .../info" "200"
req GET "/api/emp/plan/$TARGET_EMP/cntry-pair" > /dev/null;   assert_code "GET .../cntry-pair" "200"
req GET "/api/emp/plan/$TARGET_EMP/co2-calc" > /dev/null;     assert_code "GET .../co2-calc" "200"
req GET "/api/emp/plan/$TARGET_EMP/co2-detail" > /dev/null;   assert_code "GET .../co2-detail" "200"
req GET "/api/emp/plan/$TARGET_EMP/data-ctrl" > /dev/null;    assert_code "GET .../data-ctrl" "200"
req GET "/api/emp/plan/$TARGET_EMP/risk" > /dev/null;         assert_code "GET .../risk" "200"

# ── 권한 거부 회귀: AIRLINE 이 타사 데이터 조회 시도 ──
section "2.8 권한 거부 회귀 (AIRLINE 의 타사 데이터 접근)"
logout
login "kal_user" "admin1234!" >/dev/null
req GET "/api/emp/plan/EMP2026AAR" > /dev/null
if [[ "$LAST_CODE" == "403" ]] || [[ "$LAST_CODE" == "404" ]] ; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1))
  echo -e "  ${GREEN}✓${RESET} kal_user → EMP2026AAR (HTTP $LAST_CODE — 차단됨)"
else
  FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1))
  FAILED_NAMES+=("권한 거부 회귀: kal_user → EMP2026AAR (got $LAST_CODE)")
  echo -e "  ${RED}✗${RESET} 권한 차단 실패 (HTTP $LAST_CODE)"
fi

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
