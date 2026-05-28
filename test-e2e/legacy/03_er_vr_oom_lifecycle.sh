#!/usr/bin/env bash
# ER → VR → OoM → CORSIA 정량검증 통합 라이프사이클 E2E
# RFP 박스 ② ER · ④ VR · ⑥ OoM · ⑦ CORSIA 세부항목 검증
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== 03. ER+VR+OoM+CORSIA 라이프사이클 ===="

# ── 시나리오: KAL 2025 ER 이미 APRVD — 동일하게 새 검증서·OoM 흐름 진행
# 사용 가능한 ER: ER2026KAL (SBMTD), ER2026AAR (DRAFT)

# ────── ER 영역 ──────────────────────────────────────────
section "3.1 ER 단건 조회 + 자식 헬스 (admin01)"
login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/er/rprt/ER2026KAL" > /dev/null;          assert_code "GET /api/er/rprt/ER2026KAL" "200"
req GET "/api/er/rprt/ER2026KAL/acft-fuel" > /dev/null;  assert_code "GET .../acft-fuel" "200"
req GET "/api/er/rprt/ER2026KAL/cntry-pair" > /dev/null; assert_code "GET .../cntry-pair" "200"
req GET "/api/er/rprt/ER2026KAL/aerdrm-pair" > /dev/null;assert_code "GET .../aerdrm-pair" "200"
req GET "/api/er/rprt/ER2026KAL/fuel-smry" > /dev/null;  assert_code "GET .../fuel-smry" "200"
req GET "/api/er/rprt/ER2026KAL/data-gap" > /dev/null;   assert_code "GET .../data-gap" "200"
req GET "/api/er/rprt/ER2026KAL/afbr" > /dev/null;       assert_code "GET .../afbr" "200"
req GET "/api/er/rprt/ER2026KAL/vrfr-info" > /dev/null;  assert_code "GET .../vrfr-info" "200"

# ────── VR 영역: 검증기관(vrf_lead)이 VR 작성 ─────────────
section "3.2 VR (검증기관) 신규 작성 - vrf_lead"
logout
login "vrf_lead" "gn12345!" || { echo "vrf_lead 로그인 실패"; exit 1; }
echo -e "  ${GREEN}✓${RESET} vrf_lead 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# 시드 검증배정: VI0001 → KAL, AAR, JJA (2026)
# ER2026KAL(SBMTD)에 대해 VR 작성
req POST "/api/vr" '{"oprtrId":"KAL","rprtYr":"2026","erId":"ER2026KAL","vrfcnInstId":"VI_KVA","vrTypeCd":"ER"}'
assert_code "POST /api/vr (VR 신규 작성)" "200"
VR_ID=$(echo "$LAST_BODY" | grep -oE '"vrId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  VR ID: ${VR_ID:-(추출불가)}"

if [[ -n "${VR_ID:-}" ]]; then
  req GET "/api/vr/$VR_ID" > /dev/null;  assert_code "GET /api/vr/$VR_ID" "200"
  # links 수정 (ER 링크)
  req PUT "/api/vr/$VR_ID/links" '{"erId":"ER2026KAL"}'
  assert_code "PUT .../links" "200"
fi

section "3.3 VR 라이프사이클 (DRAFT → SBMTD → 권고 → 승인)"
if [[ -n "${VR_ID:-}" ]]; then
  req POST "/api/vr/$VR_ID/submit" "{}"
  assert_code "POST .../submit (DRAFT→SBMTD)" "200"
fi
# KOTSA 권고
logout; login "kotsa01" "admin1234!" >/dev/null
if [[ -n "${VR_ID:-}" ]]; then
  req POST "/api/vr/$VR_ID/recommend" "{}"
  assert_code "POST .../recommend (KOTSA 권고)" "200"
fi
# MOLIT 승인
logout; login "admin01" "admin1234!" >/dev/null
if [[ -n "${VR_ID:-}" ]]; then
  req POST "/api/vr/$VR_ID/approve" "{}"
  assert_code "POST .../approve (MOLIT 승인)" "200"
  req GET "/api/vr/$VR_ID" > /dev/null
  assert_json_contains "VR 최종상태=APRVD" '"vrStCd":"APRVD"'
fi

# ────── OoM 영역: KOTSA가 검토 작성 ──────────────────────
section "3.4 OoM-Check (KOTSA) 신규 작성 + 18종 정량검증 실행 (RFP 박스 ⑥⑦)"
logout; login "kotsa01" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kotsa01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req POST "/api/er/oom" '{"oprtrId":"KAL","rprtYr":"2026","erId":"ER2026KAL"}'
assert_code "POST /api/er/oom (OoM 신규 작성)" "200"
OOM_ID=$(echo "$LAST_BODY" | grep -oE '"oomId":"[^"]+"' | head -1 | cut -d'"' -f4)
[[ -z "$OOM_ID" ]] && OOM_ID=$(echo "$LAST_BODY" | grep -oE '"oomCheckId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  OoM ID: ${OOM_ID:-(추출불가)}"

if [[ -n "${OOM_ID:-}" ]]; then
  req GET "/api/er/oom/$OOM_ID" > /dev/null;  assert_code "GET /api/er/oom/$OOM_ID" "200"
  # 18종 정량검증 실행 (CORSIA 박스 ⑦)
  req POST "/api/er/oom/$OOM_ID/run-quant" "{}"
  assert_code "POST .../run-quant (Rule 18종 실행)" "200"
  # 항목 조회
  req GET "/api/er/oom/$OOM_ID/item" > /dev/null
  assert_code "GET .../item (검토항목 목록)" "200"
  assert_json_contains "Rule 18종 자동 항목 존재" '"itemNo"'
  # finalize PASS
  req POST "/api/er/oom/$OOM_ID/finalize" '{"rsltCd":"PASS"}'
  assert_code "POST .../finalize PASS" "200"
fi

# ────── CEF: KAL ER에 CEF(적격연료) 청구 등록 ─────────────
section "3.5 CEF (적격연료) KAL 항공사 등록 - RFP 박스 ③"
logout; login "kal_user" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/er/cef?rprtYr=2026" > /dev/null;  assert_code "GET /api/er/cef (목록)" "200"
req POST "/api/er/cef" '{"oprtrId":"KAL","rprtYr":"2026","erId":"ER2026KAL"}'
assert_code "POST /api/er/cef (CEF 신규)" "200"
CEF_ID=$(echo "$LAST_BODY" | grep -oE '"cefId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  CEF ID: ${CEF_ID:-(추출불가)}"

if [[ -n "${CEF_ID:-}" ]]; then
  req GET "/api/er/cef/$CEF_ID" > /dev/null;  assert_code "GET /api/er/cef/$CEF_ID" "200"
  req GET "/api/er/cef/by-er/ER2026KAL" > /dev/null;  assert_code "GET .../by-er/ER2026KAL" "200"
  # CEF 제출
  req POST "/api/er/cef/$CEF_ID/submit" "{}"
  assert_code "POST .../submit (CEF DRAFT→SBMTD)" "200"
  # 이중청구 검증
  req GET "/api/er/cef/validate-double-claim?batchIdNo=DEMO-BATCH-001&currentOprtrId=KAL" > /dev/null
  assert_code "GET .../validate-double-claim" "200"
fi
# KOTSA가 CEF 승인
logout; login "kotsa01" "admin1234!" >/dev/null
if [[ -n "${CEF_ID:-}" ]]; then
  req POST "/api/er/cef/$CEF_ID/approve" "{}"
  assert_code "POST .../approve (KOTSA CEF 승인)" "200"
fi

# ────── EUCR: 배출권 취소 보고 ──────────────────────────
section "3.6 EUCR (배출권 취소) KAL 항공사 작성 - RFP 박스 ⑤"
logout; login "kal_user" "admin1234!" >/dev/null

req POST "/api/er/eucr" '{"oprtrId":"KAL","rprtYr":"2025","ofstReqQty":1000.0}'
assert_code "POST /api/er/eucr (신규)" "200"
EUCR_ID=$(echo "$LAST_BODY" | grep -oE '"eucrId":"[^"]+"' | head -1 | cut -d'"' -f4)
echo "  EUCR ID: ${EUCR_ID:-(추출불가)}"

if [[ -n "${EUCR_ID:-}" ]]; then
  req GET "/api/er/eucr/$EUCR_ID" > /dev/null;  assert_code "GET /api/er/eucr/$EUCR_ID" "200"
  req PUT "/api/er/eucr/$EUCR_ID/ofst-req-qty" '{"ofstReqQty":1500.0}'
  assert_code "PUT .../ofst-req-qty (의무량 수정)" "200"
  req POST "/api/er/eucr/$EUCR_ID/recalc" "{}"
  assert_code "POST .../recalc (재계산)" "200"
  req POST "/api/er/eucr/$EUCR_ID/submit" "{}"
  assert_code "POST .../submit" "200"
fi

# ────── 정합성: 핵심 데이터 DB 검증 ───────────────────────
section "3.7 DB 정합성 검증 (시나리오 결과 영속성 확인)"
ROW_CNT=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM vr.tn_vr WHERE oprtr_id='KAL' AND rprt_yr='2026';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
if [[ "$ROW_CNT" -ge 1 ]]; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} VR KAL/2026 DB 적재 ($ROW_CNT 행)"
else FAIL=$((FAIL+1)); FAILED_NAMES+=("VR DB 적재 (count=$ROW_CNT)"); echo -e "  ${RED}✗${RESET} VR DB 적재 ($ROW_CNT 행)"; fi

ROW_CNT=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_oom_check WHERE oprtr_id='KAL' AND rprt_yr='2026';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
if [[ "$ROW_CNT" -ge 1 ]]; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} OoM KAL/2026 DB 적재 ($ROW_CNT 행)"
else FAIL=$((FAIL+1)); FAILED_NAMES+=("OoM DB 적재 (count=$ROW_CNT)"); echo -e "  ${RED}✗${RESET} OoM DB 적재 ($ROW_CNT 행)"; fi

ROW_CNT=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_cef WHERE oprtr_id='KAL' AND rprt_yr='2026';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
if [[ "$ROW_CNT" -ge 1 ]]; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} CEF KAL/2026 DB 적재 ($ROW_CNT 행)"
else FAIL=$((FAIL+1)); FAILED_NAMES+=("CEF DB 적재 (count=$ROW_CNT)"); echo -e "  ${RED}✗${RESET} CEF DB 적재 ($ROW_CNT 행)"; fi

ROW_CNT=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_eucr WHERE oprtr_id='KAL';" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
if [[ "$ROW_CNT" -ge 1 ]]; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} EUCR KAL DB 적재 ($ROW_CNT 행)"
else FAIL=$((FAIL+1)); FAILED_NAMES+=("EUCR DB 적재 (count=$ROW_CNT)"); echo -e "  ${RED}✗${RESET} EUCR DB 적재 ($ROW_CNT 행)"; fi

ROW_CNT=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT count(*) FROM er.tn_oom_check_item WHERE oom_id IN (SELECT oom_id FROM er.tn_oom_check WHERE oprtr_id='KAL' AND rprt_yr='2026');" 2>/dev/null | tr -d ' ')
TOTAL=$((TOTAL+1))
if [[ "$ROW_CNT" -ge 1 ]]; then PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} OoM Rule 18종 항목 적재 ($ROW_CNT 행)"
else FAIL=$((FAIL+1)); FAILED_NAMES+=("OoM Rule18 항목 적재 (count=$ROW_CNT)"); echo -e "  ${RED}✗${RESET} OoM Rule18 항목 ($ROW_CNT 행)"; fi

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
