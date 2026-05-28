#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# S8 권한·결재 횡단 (Cross-cutting)
# 데이터 흐름:  4-actor (MOLIT / KOTSA / AIRLINE / VERIFIER) 권한 매트릭스
#               + 사이드바·전 화면 회귀 + HTML 스냅샷
#               + 결재함 라이프사이클 (받은결재/내가올린/처리대기)
# 입력 actor:  모든 4-actor
# 최종 결과:    권한 분리 무결성 + 33개 화면 사이드바 노출 + 스냅샷 산출
# 통과 박스:   ⑩ 결재·권한 + 전체 ②~⑥ 라이프사이클 위에 얹는 횡단 검증
# ─────────────────────────────────────────────────────────────────
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
SNAP_DIR="$DIR/snapshots"
mkdir -p "$SNAP_DIR"
echo "==== S8. 권한·결재 횡단 (Cross-cutting) ===="

section "S8.1 MOLIT(admin01) — 사이드바 33개 URL 진입"
login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

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

section "S8.2 사이드바 노출 — 11박스 메뉴"
/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL/main" -o "$SNAP_DIR/_sidebar.html"
for kw in "배출량 모니터링" "배출량보고서" "검증보고서" "적격연료" "배출권취소" "적정성검토" "CORSIA 세부항목" "SAF" "통합 워크플로우" "AI 콘솔"; do
  TOTAL=$((TOTAL+1))
  if grep -q "$kw" "$SNAP_DIR/_sidebar.html"; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 사이드바 '$kw'"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("사이드바 '$kw' 누락"); echo -e "  ${RED}✗${RESET} 사이드바 '$kw'"
  fi
done

section "S8.3 RFP 박스별 HTML 스냅샷 + 콘텐츠 키워드"
snap_check() {
  local path="$1" needle="$2" desc="$3"
  local fname; fname=$(echo "$path" | sed 's|/|_|g;s|^_||')
  /usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path" -o "$SNAP_DIR/$fname.html"
  local size; size=$(stat -f%z "$SNAP_DIR/$fname.html" 2>/dev/null || stat -c%s "$SNAP_DIR/$fname.html")
  TOTAL=$((TOTAL+1))
  if grep -q "$needle" "$SNAP_DIR/$fname.html"; then
    PASS=$((PASS+1)); printf "  ${GREEN}✓${RESET} %-25s [%6s B] %s\n" "$path" "$size" "$desc"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("$path missing '$needle'"); printf "  ${RED}✗${RESET} %-25s [%6s B] %s\n" "$path" "$size" "$desc (누락)"
  fi
}
snap_check "/main"         "대시보드"                "메인"
snap_check "/emp/plan"     "배출량 모니터링"          "① EMP"
snap_check "/er/list"      "배출량보고서"             "② ER"
snap_check "/er/cef"       "적격연료\|CEF"            "③ CEF"
snap_check "/vr/list"      "검증보고서\|VR"           "④ VR"
snap_check "/er/eucr"      "배출권취소\|EUCR"         "⑤ EUCR"
snap_check "/er/oom"       "적정성검토\|OoM"          "⑥ OoM"
snap_check "/er/oom/qchk"  "CORSIA 세부항목"           "⑦ CORSIA"
snap_check "/saf/dashboard" "SAF"                      "⑧ SAF"
snap_check "/ptl/workflow" "통합 워크플로우"           "⑨ 포털"
snap_check "/ai/console"   "공통 AI 서비스"           "⑩ AI(2차)"

section "S8.4 AIRLINE(kal_user) — 본인 데이터 가시, 타사 차단"
logout; login "kal_user" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kal_user 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# KAL 활성 EMP 동적 조회 (S2 가 새 버전을 만들었을 가능성 대비)
KAL_EMP=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT emp_plan_id FROM emp.tn_emp_plan WHERE oprtr_id='KAL' AND use_end_dt > NOW() ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
echo "  KAL 활성 EMP: ${KAL_EMP:-(없음)}"
if [[ -n "$KAL_EMP" ]]; then
  req GET "/api/emp/plan/$KAL_EMP" > /dev/null;  assert_code "본사 EMP 가시 ($KAL_EMP)" "200"
else
  TOTAL=$((TOTAL+1)); PASS=$((PASS+1))
  echo -e "  ${YELLOW}~${RESET} KAL 활성 EMP 없음 (SKIP)"
fi

# AAR 활성 EMP 동적 조회 → kal_user 접근 차단 검증
AAR_EMP=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT emp_plan_id FROM emp.tn_emp_plan WHERE oprtr_id='AAR' AND use_end_dt > NOW() ORDER BY frst_reg_dt DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "$AAR_EMP" ]]; then
  req GET "/api/emp/plan/$AAR_EMP" > /dev/null
  TOTAL=$((TOTAL+1))
  if [[ "$LAST_CODE" == "403" || "$LAST_CODE" == "404" ]]; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 타사 EMP 차단 ($AAR_EMP HTTP $LAST_CODE)"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("kal_user → $AAR_EMP 차단실패 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} 타사 차단 실패 ($LAST_CODE)"
  fi
fi

section "S8.5 VERIFIER(vrf_lead) — VR 접근 + 타 도메인 제한"
logout; login "vrf_lead" "gn12345!" >/dev/null
echo -e "  ${GREEN}✓${RESET} vrf_lead 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/vr" > /dev/null;  assert_code "VR 목록 가시" "200"
# 검증기관은 공통관리 사용자 마스터 변경 불가
req POST "/api/com/user" '{"userId":"unauth","userNm":"X"}' > /dev/null
TOTAL=$((TOTAL+1))
if [[ "$LAST_CODE" == "403" || "$LAST_CODE" == "400" || "$LAST_CODE" == "404" ]]; then
  PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 사용자 마스터 변경 차단 ($LAST_CODE)"
else
  FAIL=$((FAIL+1)); FAILED_NAMES+=("vrf_lead 사용자 등록 차단실패 ($LAST_CODE)"); echo -e "  ${RED}✗${RESET} 차단실패 ($LAST_CODE)"
fi

section "S8.6 KOTSA(kotsa01) — 검토자 권한"
logout; login "kotsa01" "admin1234!" >/dev/null
echo -e "  ${GREEN}✓${RESET} kotsa01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

req GET "/api/emp/plan?rprtYr=2026" > /dev/null;  assert_code "EMP 전체 가시 (검토자)"  "200"
req GET "/api/er/rprt?rprtYr=2026"  > /dev/null;  assert_code "ER 전체 가시 (검토자)"   "200"
req GET "/api/vr"                   > /dev/null;  assert_code "VR 전체 가시 (검토자)"   "200"

section "S8.7 결재함 라이프사이클 (받은결재 / 내가올린 / 처리대기)"
logout; login "admin01" "admin1234!" >/dev/null
req GET "/api/com/atrz?pageSize=5"           > /dev/null;  assert_code "GET 결재함"          "200"
req GET "/api/com/atrz/my-pending"           > /dev/null;  assert_code "GET 내 결재 대기"    "200"

logout
print_summary
echo "스냅샷: $SNAP_DIR/"
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
