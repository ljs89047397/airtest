#!/usr/bin/env bash
# 화면 HTML 스냅샷 + 콘텐츠 검증
# RFP 박스 9개 + 공통관리 화면이 실제 UI를 렌더하는지 확인
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
SNAP_DIR="$DIR/snapshots"
mkdir -p "$SNAP_DIR"

echo "==== 06. 화면 HTML 스냅샷 + 콘텐츠 검증 ===="

login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# 페이지별 스냅샷 + 키워드 검증
check_page() {
  local path="$1" needle="$2" desc="$3"
  local fname=$(echo "$path" | sed 's|/|_|g;s|^_||')
  /usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path" -o "$SNAP_DIR/$fname.html"
  local size=$(stat -f%z "$SNAP_DIR/$fname.html" 2>/dev/null || stat -c%s "$SNAP_DIR/$fname.html")
  if grep -q "$needle" "$SNAP_DIR/$fname.html"; then
    PASS=$((PASS+1)); TOTAL=$((TOTAL+1))
    printf "  ${GREEN}✓${RESET} %-30s [%6s bytes] %s\n" "$path" "$size" "$desc ('$needle' 확인)"
  else
    FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1))
    FAILED_NAMES+=("$path missing '$needle'")
    printf "  ${RED}✗${RESET} %-30s [%6s bytes] %s\n" "$path" "$size" "$desc ('$needle' 누락)"
  fi
}

section "6.1 RFP 개념도 박스 ①~⑩ 화면 콘텐츠 검증"
check_page "/main"                "대시보드"          "메인 대시보드"
check_page "/emp/plan"            "배출량 모니터링"   "박스 ① EMP"
check_page "/er/list"             "배출량보고서"      "박스 ② ER"
check_page "/er/cef"              "적격연료\|CEF"      "박스 ③ CEF"
check_page "/vr/list"             "검증보고서\|VR"     "박스 ④ VR"
check_page "/er/eucr"             "배출권취소\|EUCR"   "박스 ⑤ EUCR"
check_page "/er/oom"              "적정성검토\|OoM"    "박스 ⑥ OoM"
check_page "/er/oom/qchk"         "CORSIA 세부항목 검증" "박스 ⑦ CORSIA 검증"
check_page "/saf/dashboard"       "SAF"               "박스 ⑧ SAF"
check_page "/ptl/workflow"        "통합 워크플로우"   "박스 ⑨ 포털"
check_page "/ai/console"          "공통 AI 서비스"    "박스 ⑩ AI (2차)"

section "6.2 SAF 자식 화면"
check_page "/saf/cert"            "인증서"            "SAF 인증서"
check_page "/saf/batch"           "배치"              "SAF 배치"
check_page "/saf/airprt"          "공항"              "SAF 공항"
check_page "/saf/mntr"            "혼합\|모니터링"     "SAF 혼합 모니터링"

section "6.3 포털 화면"
check_page "/ptl/stat"            "통계"              "통계/시뮬레이션"
check_page "/ptl/sim"             "시뮬레이션"        "시뮬레이션"
check_page "/ptl/ccr"             "CCR"               "CCR 추출"
check_page "/ptl/actn"            "감사로그\|actn"     "감사로그"

section "6.4 공통관리 화면 (MOLIT)"
check_page "/com/user"            "사용자"            "사용자 관리"
check_page "/com/ognz"            "조직\|기관"         "조직 관리"
check_page "/com/oprtr"           "항공기\|운영사"     "운영사"
check_page "/com/vrfcn"           "검증기관"          "검증기관"
check_page "/com/role"            "역할"              "역할"
check_page "/com/authrt"          "권한"              "권한"
check_page "/com/cd"              "공통코드\|코드"     "공통코드"
check_page "/com/atrz"            "결재"              "결재함"
check_page "/com/rglt"            "규정"              "규정 게시판"
check_page "/com/menu"            "메뉴"              "메뉴 관리"
check_page "/com/prgrm"           "프로그램"          "프로그램 관리"

section "6.5 사이드바 메뉴 항목 11박스 모두 노출"
/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL/main" -o "$SNAP_DIR/_sidebar_check.html"
for kw in "배출량 모니터링" "배출량보고서" "검증보고서" "적격연료" "배출권취소" "적정성검토" "CORSIA 세부항목" "SAF" "통합 워크플로우" "AI 콘솔"; do
  TOTAL=$((TOTAL+1))
  if grep -q "$kw" "$SNAP_DIR/_sidebar_check.html"; then
    PASS=$((PASS+1)); echo -e "  ${GREEN}✓${RESET} 사이드바 '$kw' 노출"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("사이드바 '$kw' 누락"); echo -e "  ${RED}✗${RESET} 사이드바 '$kw' 누락"
  fi
done

logout
print_summary
echo "스냅샷: $SNAP_DIR/"
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
