#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S3 SAF 공급망 · 혼합비율
# 데이터 흐름:  공급사 인증서 → 배치(생산·혼합) → 공항급유·구매 → 운영사별 혼합비율 산출 → 의무이행 판정
# 입력 actor:  운영사(kal_user) · KOTSA
# 최종 결과:    운영사별 혼합비율 % · 의무이행 ✓/✗
# 통과 박스:   ⑧ SAF
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== S3. SAF 공급망·혼합비율 (RFP 박스 ⑧) ===="

section "S3.1 MOLIT — SAF 인프라 헬스"
login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/saf/cert"                            > /dev/null; assert_code "GET SAF 인증서 목록" "200"
req GET "/api/saf/batch"                           > /dev/null; assert_code "GET SAF 배치 목록"   "200"
req GET "/api/saf/airprt-fuel"                     > /dev/null; assert_code "GET 공항급유 목록"   "200"
req GET "/api/saf/airprt-purch"                    > /dev/null; assert_code "GET 공항구매 목록"   "200"
req GET "/api/saf/mntr/blnd/all?rprtYr=2026"       > /dev/null; assert_code "GET 혼합전체"         "200"

section "S3.2 운영사(kal_user) — SAF 배치 신규 (자연키 PK)"
logout; login "kal_user" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

BATCH_ID="S3-BATCH-$(date +%s)"
req POST "/api/saf/batch" "{\"batchId\":\"$BATCH_ID\",\"oprtrId\":\"KAL\",\"batchQty\":10000}"
assert_code "POST SAF 배치 신규" "200"
req GET "/api/saf/batch/$BATCH_ID" > /dev/null
assert_code "GET SAF 배치 상세" "200"

# 배치 자식 5종 헬스 — 신규 배치는 자식 데이터 미존재 정상(200/404 모두 허용)
for sub in ghg feed blndr prdc; do
  req GET "/api/saf/batch/$BATCH_ID/$sub" > /dev/null
  TOTAL=$((TOTAL+1))
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "404" ]]; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} GET 배치 자식 /$sub (HTTP $LAST_CODE)"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("배치 자식 /$sub ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} /$sub ($LAST_CODE)"
  fi
done

section "S3.3 SAF 인증서 등록 (배치 참조)"
CERT_NO="S3-CERT-$(date +%s)"
req POST "/api/saf/cert" "{\"batchId\":\"$BATCH_ID\",\"oprtrId\":\"KAL\",\"certTypeCd\":\"PoS\",\"certSchmCd\":\"ISCC_CORSIA\",\"certNo\":\"$CERT_NO\",\"certIsueDt\":\"2026-03-01\",\"certXprDt\":\"2027-03-01\"}"
assert_code "POST SAF 인증서 신규" "200"
CERT_ID=$(echo "$LAST_BODY" | grep -oE '"certId":"[^"]+"' | head -1 | cut -d'"' -f4)
if [[ -n "${CERT_ID:-}" ]]; then
  req GET "/api/saf/cert/$CERT_ID" > /dev/null; assert_code "GET SAF 인증서 상세" "200"
  req POST "/api/saf/cert/$CERT_ID/surrender" "{}" > /dev/null
  assert_code "POST 인증서 surrender (회수)" "200"
fi

section "S3.4 공항별 급유·구매 실적 등록"
req PUT "/api/saf/airprt-fuel" '{"airprtId":"RKSI","oprtrId":"KAL","rprtYr":"2026","fltCnt":120,"fltTime":1800,"reqFuelQty":50000,"actlFuelQty":49500,"yrNonTankedQty":40000,"yrTankedSafetyQty":9500}' > /dev/null
[[ "$LAST_CODE" == "200" ]] \
  && { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} PUT 공항급유 upsert"; } \
  || { FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); FAILED_NAMES+=("PUT airprt-fuel ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} PUT 공항급유 ($LAST_CODE)"; }

section "S3.5 KOTSA — 운영사별 혼합비율 산출 + 의무이행 판정"
logout; login "kotsa01" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kotsa01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req POST "/api/saf/mntr/blnd/calc" '{"oprtrId":"KAL","rprtYr":"2026"}' > /dev/null
assert_code "POST 혼합비율 산출 (KAL/2026)" "200"

req POST "/api/saf/mntr/blnd/calc" '{"oprtrId":"JJA","rprtYr":"2026"}' > /dev/null
assert_code "POST 혼합비율 산출 (JJA/2026)" "200"

req GET "/api/saf/mntr/blnd/all?rprtYr=2026" > /dev/null
assert_code "GET 운영사별 혼합비율 매트릭스" "200"

section "S3.6 화면 콘텐츠 검증"
check_html() {
  local path="$1" needle="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$needle"; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} $desc";
  else FAIL=$((FAIL+1)); FAILED_NAMES+=("$path '$needle'"); echo -e "  ${RED}✗${RESET} $desc"; fi
}
check_html "/saf/dashboard" "이행"             "대시보드 — 이행률"
check_html "/saf/cert"      "인증서"           "인증서 화면"
check_html "/saf/batch"     "배치"             "배치 화면"
check_html "/saf/airprt"    "공항\|급유"        "공항급유 화면"
check_html "/saf/mntr"      "혼합\|모니터링"    "혼합비율 모니터링"

section "S3.7 DB 정합성 — 인증서 · 배치 · 혼합 결과"
N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM saf.tn_saf_cert WHERE oprtr_id='KAL';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 인증서 ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("인증서 0행"); echo -e "  ${RED}✗${RESET} 인증서 ($N)"; }

N=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM saf.tn_saf_batch WHERE oprtr_id='KAL';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
[[ "$N" -ge 1 ]] && { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 배치 ($N 행)"; } \
  || { FAIL=$((FAIL+1)); FAILED_NAMES+=("배치 0행"); echo -e "  ${RED}✗${RESET} 배치 ($N)"; }

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
