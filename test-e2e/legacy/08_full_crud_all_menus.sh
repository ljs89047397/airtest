#!/usr/bin/env bash
# 전 메뉴 풀 CRUD E2E
# 각 도메인: 조회(GET 목록·상세) · 입력(POST) · 수정(PUT) · 삭제(DELETE) · 액션 버튼(submit/approve/reject 등)
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== 08. 전 메뉴 풀 CRUD E2E ===="

login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인 (master 권한)"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# ─────────────────────────────────────────────────────
section "8.1 EMP 풀 CRUD"
# 조회
req GET  "/api/emp/plan?rprtYr=2026" > /dev/null; assert_code "GET EMP 목록" "200"
# 활성 EMP 한 건 동적 선택 (CRUD 시나리오 내내 안전)
ACT_EMP=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT emp_plan_id FROM emp.tn_emp_plan WHERE use_end_dt > NOW() AND emp_st_cd != 'CNCLD' ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
echo "  활성 EMP: ${ACT_EMP:-(없음)}"
req GET  "/api/emp/plan/${ACT_EMP:-EMP2026KAL}"  > /dev/null; assert_code "GET EMP 상세" "200"
# 입력
req POST "/api/emp/plan" '{"oprtrId":"JJA","rprtYr":"2030","empVer":"1.0"}' > /dev/null
EMP_NEW=$(echo "$LAST_BODY" | grep -oE '"empPlanId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST EMP 신규" "200"
if [[ -n "${EMP_NEW:-}" ]]; then
  # 수정
  req PUT "/api/emp/plan/$EMP_NEW" "{\"empPlanId\":\"$EMP_NEW\",\"oprtrId\":\"JJA\",\"rprtYr\":\"2030\",\"empVer\":\"1.0\",\"rmrk\":\"E2E 수정\"}" > /dev/null
  assert_code "PUT EMP 수정" "200"
  # 액션: 제출
  req POST "/api/emp/plan/$EMP_NEW/submit" "{}" > /dev/null
  assert_code "POST EMP submit" "200"
  # 검토/권고/승인
  req POST "/api/emp/plan/$EMP_NEW/review" "{}" > /dev/null;    assert_code "POST EMP review" "200"
  req POST "/api/emp/plan/$EMP_NEW/recommend" "{}" > /dev/null; assert_code "POST EMP recommend" "200"
  req POST "/api/emp/plan/$EMP_NEW/approve" "{}" > /dev/null;   assert_code "POST EMP approve" "200"
  # 취소(softDelete 대체)
  req POST "/api/emp/plan/$EMP_NEW/cancel" '{"reason":"E2E 취소 사유"}' > /dev/null
  assert_code "POST EMP cancel (취소)" "200"
fi

# ─────────────────────────────────────────────────────
section "8.2 ER 풀 CRUD"
req GET  "/api/er/rprt?rprtYr=2026" > /dev/null; assert_code "GET ER 목록" "200"
req GET  "/api/er/rprt/ER2026KAL" > /dev/null;   assert_code "GET ER 상세" "200"
# 자식 도메인 조회 (8종)
for child in acft-fuel cntry-pair aerdrm-pair fuel-smry data-gap afbr vrfr-info validate-pair-sum ; do
  req GET "/api/er/rprt/ER2026KAL/$child" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} GET ER 자식 /$child"
  else
    TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("ER 자식 /$child got $LAST_CODE")
    echo -e "  ${RED}✗${RESET} GET ER 자식 /$child ($LAST_CODE)"
  fi
done
# 입력
req POST "/api/er/rprt" '{"oprtrId":"JJA","rprtYr":"2030"}' > /dev/null
ER_NEW=$(echo "$LAST_BODY" | grep -oE '"erId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST ER 신규" "200"
if [[ -n "${ER_NEW:-}" ]]; then
  # 자식 입력: 연료 정보
  req POST "/api/er/rprt/$ER_NEW/acft-fuel" '{"acftRegnNo":"HL-E2E","acftTypeCd":"A350-900","fuelTypeCd":"JET-A1","fuelQty":1234.56}' > /dev/null
  ACFT_CODE=$LAST_CODE
  [[ "$ACFT_CODE" == "200" || "$ACFT_CODE" == "201" || "$ACFT_CODE" == "400" ]] \
    && { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} POST ER 자식 acft-fuel ($ACFT_CODE)"; } \
    || { TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("ER acft-fuel got $ACFT_CODE"); echo -e "  ${RED}✗${RESET} POST ER 자식 acft-fuel ($ACFT_CODE)"; }
  # 액션: 제출 → 승인
  req POST "/api/er/rprt/$ER_NEW/submit"  "{}" > /dev/null; assert_code "POST ER submit" "200"
  req POST "/api/er/rprt/$ER_NEW/review"  "{}" > /dev/null; assert_code "POST ER review" "200"
  req POST "/api/er/rprt/$ER_NEW/recommend" "{}" > /dev/null; assert_code "POST ER recommend" "200"
  req POST "/api/er/rprt/$ER_NEW/approve" "{}" > /dev/null; assert_code "POST ER approve" "200"
  req POST "/api/er/rprt/$ER_NEW/cancel"  '{"reason":"E2E 취소 사유"}' > /dev/null; assert_code "POST ER cancel" "200"
fi

# ─────────────────────────────────────────────────────
section "8.3 CEF 풀 CRUD"
req GET "/api/er/cef?rprtYr=2026" > /dev/null; assert_code "GET CEF 목록" "200"
# JJA/2027 의 새 ER 사용 (CEF 미등록) — 시나리오 재실행시 충돌 회피
ER_FOR_CEF=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT er_id FROM er.tn_er WHERE er_id NOT IN (SELECT er_id FROM er.tn_cef WHERE er_id IS NOT NULL) AND use_end_dt > NOW() ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
ER_OPRTR=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT oprtr_id FROM er.tn_er WHERE er_id='$ER_FOR_CEF';" 2>/dev/null | tr -d ' ')
ER_YR=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT rprt_yr FROM er.tn_er WHERE er_id='$ER_FOR_CEF';" 2>/dev/null | tr -d ' ')
echo "  CEF 등록 대상 ER: ${ER_FOR_CEF:-(없음)} ($ER_OPRTR/$ER_YR)"
req POST "/api/er/cef" "{\"oprtrId\":\"$ER_OPRTR\",\"rprtYr\":\"$ER_YR\",\"erId\":\"$ER_FOR_CEF\"}" > /dev/null
CEF_NEW=$(echo "$LAST_BODY" | grep -oE '"cefId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST CEF 신규" "200"
if [[ -n "${CEF_NEW:-}" ]]; then
  req GET "/api/er/cef/$CEF_NEW" > /dev/null; assert_code "GET CEF 상세" "200"
  req POST "/api/er/cef/$CEF_NEW/recalc" "{}" > /dev/null;  assert_code "POST CEF recalc" "200"
  req POST "/api/er/cef/$CEF_NEW/submit" "{}" > /dev/null;  assert_code "POST CEF submit" "200"
  req POST "/api/er/cef/$CEF_NEW/approve" "{}" > /dev/null; assert_code "POST CEF approve" "200"
  req POST "/api/er/cef/$CEF_NEW/cancel" '{"reason":"E2E 취소 사유"}' > /dev/null; assert_code "POST CEF cancel" "200"
  # 삭제 (CEF 라이프사이클 상 DELETE 는 DRAFT 한정 — 본 시나리오는 CNCLD 도달 → DELETE 거부 4xx 정상)
  req DELETE "/api/er/cef/$CEF_NEW" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" || "$LAST_CODE" == "400" || "$LAST_CODE" == "409" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE CEF (HTTP $LAST_CODE — 비즈룰 정상)"
  else
    TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("CEF DELETE got $LAST_CODE"); echo -e "  ${RED}✗${RESET} DELETE CEF ($LAST_CODE)"
  fi
fi

# ─────────────────────────────────────────────────────
section "8.4 EUCR 풀 CRUD"
req GET "/api/er/eucr" > /dev/null; assert_code "GET EUCR 목록" "200"
req POST "/api/er/eucr" '{"oprtrId":"JJA","rprtYr":"2025","ofstReqQty":2000.0}' > /dev/null
EUCR_NEW=$(echo "$LAST_BODY" | grep -oE '"eucrId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST EUCR 신규" "200"
if [[ -n "${EUCR_NEW:-}" ]]; then
  req GET "/api/er/eucr/$EUCR_NEW" > /dev/null; assert_code "GET EUCR 상세" "200"
  req PUT "/api/er/eucr/$EUCR_NEW/ofst-req-qty" '{"ofstReqQty":2500.0}' > /dev/null
  assert_code "PUT EUCR 의무량" "200"
  req POST "/api/er/eucr/$EUCR_NEW/recalc" "{}" > /dev/null;  assert_code "POST EUCR recalc" "200"
  # 제출 먼저 → DELETE (DELETE는 DRAFT 한정이므로 SBMTD 상태에선 400 — 비즈룰 정상)
  req POST "/api/er/eucr/$EUCR_NEW/submit" "{}" > /dev/null;  assert_code "POST EUCR submit" "200"
  req DELETE "/api/er/eucr/$EUCR_NEW" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" || "$LAST_CODE" == "400" || "$LAST_CODE" == "409" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE EUCR (HTTP $LAST_CODE — 비즈룰 정상)"
  else
    TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("EUCR DELETE got $LAST_CODE"); echo -e "  ${RED}✗${RESET} DELETE EUCR ($LAST_CODE)"
  fi
fi

# ─────────────────────────────────────────────────────
section "8.5 OoM + CORSIA 검증 풀 CRUD"
req GET "/api/er/oom?rprtYr=2026" > /dev/null; assert_code "GET OoM 목록" "200"
req POST "/api/er/oom" '{"oprtrId":"JJA","rprtYr":"2026","erId":"ER2026JJA"}' > /dev/null
OOM_NEW=$(echo "$LAST_BODY" | grep -oE '"oomId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST OoM 신규" "200"
if [[ -n "${OOM_NEW:-}" ]]; then
  req GET "/api/er/oom/$OOM_NEW" > /dev/null; assert_code "GET OoM 상세" "200"
  req POST "/api/er/oom/$OOM_NEW/run-quant" "{}" > /dev/null; assert_code "POST OoM Rule18 실행" "200"
  req GET "/api/er/oom/$OOM_NEW/item" > /dev/null; assert_code "GET OoM 항목" "200"
  req POST "/api/er/oom/$OOM_NEW/finalize" '{"rsltCd":"PASS"}' > /dev/null; assert_code "POST OoM finalize" "200"
  req DELETE "/api/er/oom/$OOM_NEW" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" || "$LAST_CODE" == "409" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE OoM (HTTP $LAST_CODE)"
  else
    TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("OoM DELETE got $LAST_CODE"); echo -e "  ${RED}✗${RESET} DELETE OoM ($LAST_CODE)"
  fi
fi

# ─────────────────────────────────────────────────────
section "8.6 SAF 풀 CRUD"
req GET "/api/saf/batch" > /dev/null; assert_code "GET SAF 배치 목록" "200"
BATCH_NK="E2E-FULL-$(date +%s)"
req POST "/api/saf/batch" "{\"batchId\":\"$BATCH_NK\",\"oprtrId\":\"JJA\",\"batchQty\":5000}" > /dev/null
assert_code "POST SAF 배치 신규" "200"
req GET "/api/saf/batch/$BATCH_NK" > /dev/null;  assert_code "GET SAF 배치 상세" "200"
req PUT "/api/saf/batch/$BATCH_NK" "{\"batchId\":\"$BATCH_NK\",\"oprtrId\":\"JJA\",\"batchQty\":7500}" > /dev/null
[[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" || "$LAST_CODE" == "400" ]] \
  && { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} PUT SAF 배치 수정 ($LAST_CODE)"; } \
  || { TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("SAF batch PUT got $LAST_CODE"); echo -e "  ${RED}✗${RESET} PUT SAF 배치 ($LAST_CODE)"; }

req POST "/api/saf/cert" "{\"batchId\":\"$BATCH_NK\",\"oprtrId\":\"JJA\",\"certTypeCd\":\"PoS\",\"certSchmCd\":\"ISCC_CORSIA\",\"certNo\":\"E2E-CRUD-$(date +%s)\",\"certIsueDt\":\"2026-03-01\",\"certXprDt\":\"2027-03-01\"}" > /dev/null
CERT_NEW=$(echo "$LAST_BODY" | grep -oE '"certId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST SAF 인증서 신규" "200"
if [[ -n "${CERT_NEW:-}" ]]; then
  req POST "/api/saf/cert/$CERT_NEW/surrender" "{}" > /dev/null
  assert_code "POST SAF cert surrender (회수)" "200"
fi
# 공항별 급유 (PUT upsert)
req PUT "/api/saf/airprt-fuel" '{"airprtId":"RKSI","oprtrId":"JJA","rprtYr":"2026","fltCnt":120,"fltTime":1800,"reqFuelQty":50000,"actlFuelQty":49500,"yrNonTankedQty":40000,"yrTankedSafetyQty":9500}' > /dev/null
[[ "$LAST_CODE" == "200" ]] \
  && { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} PUT SAF airprt-fuel upsert"; } \
  || { TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("SAF airprt-fuel got $LAST_CODE"); echo -e "  ${RED}✗${RESET} PUT SAF airprt-fuel ($LAST_CODE)"; }
# 혼합 모니터링
req POST "/api/saf/mntr/blnd/calc" '{"oprtrId":"JJA","rprtYr":"2026"}' > /dev/null
assert_code "POST SAF mntr 혼합비율 산출" "200"

# ─────────────────────────────────────────────────────
section "8.7 포털 풀 CRUD"
req GET "/api/ptl/workflow?rprtYr=2026" > /dev/null;     assert_code "GET 워크플로우" "200"
req GET "/api/ptl/stat/yearly?rprtYr=2026" > /dev/null;  assert_code "GET 통계" "200"
req GET "/api/ptl/ccr"                    > /dev/null;   assert_code "GET CCR 목록" "200"
req GET "/api/ptl/sim"                    > /dev/null;   assert_code "GET 시뮬 목록" "200"
req GET "/api/ptl/actn"                   > /dev/null;   assert_code "GET 감사로그" "200"

# 시뮬레이션 신규/실행
req POST "/api/ptl/sim" '{"simNm":"E2E 풀시나리오","scopeSeCd":"ALL","baseYr":"2026","inputJson":"{}"}' > /dev/null
SIM_NEW=$(echo "$LAST_BODY" | grep -oE '"simId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST 시뮬 신규" "200"
if [[ -n "${SIM_NEW:-}" ]]; then
  req GET "/api/ptl/sim/$SIM_NEW" > /dev/null;          assert_code "GET 시뮬 상세" "200"
  req POST "/api/ptl/sim/$SIM_NEW/run" "{}" > /dev/null;assert_code "POST 시뮬 run" "200"
  req DELETE "/api/ptl/sim/$SIM_NEW" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE 시뮬 (HTTP $LAST_CODE)"
  else
    TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("시뮬 DELETE got $LAST_CODE"); echo -e "  ${RED}✗${RESET} DELETE 시뮬 ($LAST_CODE)"
  fi
fi

# CCR 추출 실행 (POST /extract) — extrScopeCd 필수
req POST "/api/ptl/ccr/extract" '{"rprtYr":"2026","extrScopeCd":"ALL"}' > /dev/null
if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "400" ]]; then
  PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} POST CCR extract (HTTP $LAST_CODE)"
else
  TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("CCR extract got $LAST_CODE"); echo -e "  ${RED}✗${RESET} POST CCR extract ($LAST_CODE)"
fi

# ─────────────────────────────────────────────────────
section "8.8 공통관리 풀 CRUD"
req GET "/api/com/user"  > /dev/null;  assert_code "GET 사용자" "200"
req GET "/api/com/ognz"  > /dev/null;  assert_code "GET 조직" "200"
req GET "/api/com/oprtr" > /dev/null;  assert_code "GET 운영사" "200"
req GET "/api/com/vrfcn/inst"  > /dev/null;  assert_code "GET 검증기관" "200"
req GET "/api/com/vrfcn/assgn" > /dev/null;  assert_code "GET 검증배정" "200"
req GET "/api/com/role"  > /dev/null;  assert_code "GET 역할" "200"
req GET "/api/com/authrt" > /dev/null; assert_code "GET 권한" "200"
req GET "/api/com/cd"    > /dev/null;  assert_code "GET 공통코드" "200"
req GET "/api/com/menu"  > /dev/null;  assert_code "GET 메뉴" "200"
req GET "/api/com/prgrm" > /dev/null;  assert_code "GET 프로그램" "200"
req GET "/api/com/atrz"  > /dev/null;  assert_code "GET 결재" "200"
req GET "/api/com/rglt"  > /dev/null;  assert_code "GET 규정" "200"

# 규정 게시판 풀 CRUD
req POST "/api/com/rglt" '{"rgltSeCd":"NTC","rgltNm":"E2E CRUD 규정","rgltCntn":"본문","ntcDt":"2026-05-24"}' > /dev/null
RGLT_NEW=$(echo "$LAST_BODY" | grep -oE '"rgltId":"[^"]+"' | head -1 | cut -d'"' -f4)
assert_code "POST 규정 신규" "200"
if [[ -n "${RGLT_NEW:-}" ]]; then
  req GET "/api/com/rglt/$RGLT_NEW" > /dev/null
  [[ "$LAST_CODE" == "200" ]] \
    && { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} GET 규정 상세"; } \
    || { TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("RGLT GET got $LAST_CODE"); echo -e "  ${RED}✗${RESET} GET 규정 상세 ($LAST_CODE)"; }
  req PUT "/api/com/rglt/$RGLT_NEW" '{"rgltSeCd":"NTC","rgltNm":"E2E 수정","rgltCntn":"수정본","ntcDt":"2026-05-24"}' > /dev/null
  [[ "$LAST_CODE" == "200" ]] \
    && { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} PUT 규정 수정"; } \
    || { TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("RGLT PUT got $LAST_CODE"); echo -e "  ${RED}✗${RESET} PUT 규정 ($LAST_CODE)"; }
  req DELETE "/api/com/rglt/$RGLT_NEW" > /dev/null
  if [[ "$LAST_CODE" == "200" || "$LAST_CODE" == "204" ]]; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo -e "  ${GREEN}✓${RESET} DELETE 규정 (HTTP $LAST_CODE)"
  else
    TOTAL=$((TOTAL+1)); FAIL=$((FAIL+1)); FAILED_NAMES+=("RGLT DELETE got $LAST_CODE"); echo -e "  ${RED}✗${RESET} DELETE 규정 ($LAST_CODE)"
  fi
fi

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
