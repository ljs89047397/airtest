#!/usr/bin/env bash
# RFP 목표시스템 개념도(p.10) 11박스의 세부 기능을 화면별 키워드로 회귀
# 각 박스마다 화면 URL 진입 → HTML 응답에서 세부 기능 키워드 존재 확인
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/lib.sh"
echo "==== 07. RFP 개념도 박스별 세부기능 회귀 ===="

login "admin01" "admin1234!" || exit 1
echo -e "  ${GREEN}✓${RESET} admin01 로그인"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1))

# 페이지+필요 키워드 검증 함수
check_keyword() {
  local path="$1" keyword="$2" desc="$3"
  local body
  body=$(/usr/bin/curl -s -b "$COOKIE_FILE" "$BASE_URL$path")
  TOTAL=$((TOTAL+1))
  if echo "$body" | grep -q "$keyword"; then
    PASS=$((PASS+1))
    printf "  ${GREEN}✓${RESET} %-22s  %s\n" "$path" "$desc ('$keyword')"
  else
    FAIL=$((FAIL+1)); FAILED_NAMES+=("$path missing '$keyword'")
    printf "  ${RED}✗${RESET} %-22s  %s\n" "$path" "$desc ('$keyword' 누락)"
  fi
}

# ─────────────────────────────────────────────────────
# 박스 ① EMP 관리
# ─────────────────────────────────────────────────────
section "① EMP 관리 (배출량 모니터링 계획)"
check_keyword /emp/plan      "버전"               "EMP 버전 관리"
check_keyword /emp/plan      "운영사"             "운영사 식별정보"
check_keyword /emp/plan      "신규"               "신규 작성 UI"
# EMP 시드 ID 사용
check_keyword /emp/plan/EMP2026KAL  "항공기"      "항공기/운항 데이터"
check_keyword /emp/plan/EMP2026KAL  "배출량 계산\|계산방법" "배출량 계산방법"
check_keyword /emp/plan/EMP2026KAL  "데이터 품질\|품질통제\|데이터관리" "데이터관리/품질통제"
check_keyword /emp/plan/EMP2026KAL  "법정\|출력"  "법정 서식 출력"
check_keyword /emp/plan/EMP2026KAL  "sLLM\|2차"   "(2차) sLLM 서술형 검증 placeholder"

# ─────────────────────────────────────────────────────
# 박스 ② ER 관리
# ─────────────────────────────────────────────────────
section "② ER 관리 (배출량보고서)"
check_keyword /er/list           "운영사"             "운영사/기본정보"
check_keyword /er/list           "보고연도"           "보고서 기본정보"
check_keyword /er/list           "신규 등록"          "신규 등록 UI"
check_keyword /er/ER2026KAL      "항공기.*연료\|연료" "항공기/연료 정보"
check_keyword /er/ER2026KAL      "국가.*쌍\|국가 쌍" "배출량-국가 쌍"
check_keyword /er/ER2026KAL      "비행장.*쌍\|비행장 쌍" "배출량-비행장 쌍"
check_keyword /er/ER2026KAL      "데이터 갭\|data-gap" "데이터 갭 관리"
check_keyword /er/ER2026KAL      "법정\|출력"        "법정 서식 출력"

# ─────────────────────────────────────────────────────
# 박스 ③ CEF 관리
# ─────────────────────────────────────────────────────
section "③ CEF 관리 (적격연료)"
check_keyword /er/cef            "적격연료"           "CEF 화면"
check_keyword /er/cef            "신규 등록"          "신규 등록 UI"
# CEF가 있으면 상세 확인
CEF_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT cef_id FROM er.tn_cef LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "${CEF_ID:-}" ]]; then
  check_keyword "/er/cef/$CEF_ID" "청구\|claim"       "적격연료 청구"
  check_keyword "/er/cef/$CEF_ID" "수명주기\|lcyc\|LCA" "수명주기 배출량"
  check_keyword "/er/cef/$CEF_ID" "공급망\|spchn"     "공급망 정보"
  check_keyword "/er/cef/$CEF_ID" "이중\|중복"        "이중청구 스캐닝"
fi

# ─────────────────────────────────────────────────────
# 박스 ④ VR 관리
# ─────────────────────────────────────────────────────
section "④ VR 관리 (검증보고서)"
check_keyword /vr/list           "검증보고서\|VR"    "VR 화면"
check_keyword /vr/list           "검증기관"          "검증기관 노출"
check_keyword /vr/list           "신규 등록"         "신규 등록 UI"
VR_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT vr_id FROM vr.tn_vr LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "${VR_ID:-}" ]]; then
  check_keyword "/vr/$VR_ID" "범위\|식별정보"        "범위/식별정보"
  check_keyword "/vr/$VR_ID" "시간\|일반"            "시간/범위/일반"
  check_keyword "/vr/$VR_ID" "절차\|분석"            "절차 및 분석"
  check_keyword "/vr/$VR_ID" "결론\|검증의견"        "결론/검증의견"
fi

# ─────────────────────────────────────────────────────
# 박스 ⑤ EUCR 관리
# ─────────────────────────────────────────────────────
section "⑤ EUCR 관리 (배출권 취소)"
check_keyword /er/eucr           "배출권취소\|EUCR"  "EUCR 화면"
check_keyword /er/eucr           "운영사"            "운영사 정보"
check_keyword /er/eucr           "상쇄\|배출권\|의무" "상쇄요건/배출권"
check_keyword /er/eucr           "충족\|fulfilled"   "의무량 충족 판정"
EUCR_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT eucr_id FROM er.tn_eucr LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "${EUCR_ID:-}" ]]; then
  check_keyword "/er/eucr/$EUCR_ID" "배치\|업로드\|취소" "취소 배출권 식별/배치"
fi

# ─────────────────────────────────────────────────────
# 박스 ⑥ OoM-Check 관리
# ─────────────────────────────────────────────────────
section "⑥ OoM-Check 관리"
check_keyword /er/oom            "적정성검토\|OoM"   "OoM 화면"
check_keyword /er/oom            "운영사\|운항"      "운항 활동 데이터"
check_keyword /er/oom            "판정\|결과"        "OoM 판정결과"
OOM_ID=$(PGPASSWORD='icas1234!' psql -h localhost -U icas_admin -d icas -t -c "SELECT oom_id FROM er.tn_oom_check LIMIT 1;" 2>/dev/null | tr -d ' ')
if [[ -n "${OOM_ID:-}" ]]; then
  check_keyword "/er/oom/$OOM_ID" "비행장\|국가 쌍"  "비행장/국가 쌍"
  check_keyword "/er/oom/$OOM_ID" "연료\|연료량"    "비행횟수/연료량"
  check_keyword "/er/oom/$OOM_ID" "증빙\|설명"      "증빙/설명 요청"
  check_keyword "/er/oom/$OOM_ID" "검증기관\|품질"  "검증기관 품질등급"
fi

# ─────────────────────────────────────────────────────
# 박스 ⑦ CORSIA 세부항목 검증
# ─────────────────────────────────────────────────────
section "⑦ CORSIA 세부항목 검증 (Rule 18종)"
check_keyword /er/oom/qchk       "CORSIA"            "CORSIA 검증 화면"
check_keyword /er/oom/qchk       "R001 ICAO\|ICAO 지정어" "R001 ICAO 지정어"
check_keyword /er/oom/qchk       "R002 제출기한\|제출기한 준수" "R002 제출기한 준수"
check_keyword /er/oom/qchk       "R003 ER-VR\|ER-VR 일치성" "R003 ER-VR 일치성"
check_keyword /er/oom/qchk       "R004 작성일자\|작성일자" "R004 작성일자 적정성"
check_keyword /er/oom/qchk       "R005 보고의무\|보고의무" "R005 보고의무(1만톤)"
check_keyword /er/oom/qchk       "R006 CERT 일계치\|CERT 일계치" "R006 CERT 일계치"
check_keyword /er/oom/qchk       "R007 연료유형\|연료유형" "R007 연료유형 일치"
check_keyword /er/oom/qchk       "R008 등록기호\|등록기호 중복" "R008 등록기호 중복"
check_keyword /er/oom/qchk       "R013 CERT 편차\|CERT 편차" "R013 CERT 편차"
check_keyword /er/oom/qchk       "R014 데이터 갭\|데이터 갭" "R014 데이터 갭 초과"
check_keyword /er/oom/qchk       "R016 검증기관 인증\|검증기관 인증" "R016 검증기관 인증"
check_keyword /er/oom/qchk       "R017 팀리더\|팀리더 연속" "R017 팀리더 연속"
check_keyword /er/oom/qchk       "R018 전년대비\|전년대비" "R018 전년대비 이상치"

# ─────────────────────────────────────────────────────
# 박스 ⑧ SAF 관리
# ─────────────────────────────────────────────────────
section "⑧ SAF 관리"
check_keyword /saf/dashboard     "이행"              "이행률 대시보드"
check_keyword /saf/cert          "인증서\|SC 번호\|cert" "인증서 업로드"
check_keyword /saf/cert          "회수\|유효"        "인증서 검증/회수"
check_keyword /saf/batch         "배치"              "배치 기본정보"
check_keyword /saf/airprt        "공항\|급유"        "공항별 급유"
check_keyword /saf/airprt        "구매\|purch"       "공항별 구매"
check_keyword /saf/mntr          "혼합\|모니터링"    "SAF 모니터링"

# ─────────────────────────────────────────────────────
# 박스 ⑨ 포털 서비스
# ─────────────────────────────────────────────────────
section "⑨ 포털 서비스"
check_keyword /ptl/workflow      "워크플로우\|Workflow" "통합 워크플로우"
check_keyword /ptl/stat          "통계\|배출"        "배출/상쇄/SAF 통계"
check_keyword /ptl/sim           "시뮬\|상쇄"        "상쇄비용 시뮬레이션"
check_keyword /ptl/ccr           "CCR"               "CCR 추출"
check_keyword /com/rglt          "규정"              "규정 업데이트 관리"
check_keyword /com/user          "사용자"            "이해관계자 포털"
check_keyword /com/role          "역할"              "역할기반 접근제어"
check_keyword /com/authrt        "권한"              "권한 매트릭스"

# ─────────────────────────────────────────────────────
# 박스 ⑩ 공통 AI 서비스 (2차년도 자리)
# ─────────────────────────────────────────────────────
section "⑩ 공통 AI 서비스 (2차년도)"
check_keyword /ai/console        "sLLM"              "sLLM 공동 서비스 환경"
check_keyword /ai/console        "로깅\|모니터링"    "AI 로깅·모니터링"
check_keyword /ai/console        "보안"              "입·출력 보안대책"
check_keyword /ai/console        "XAI\|설명"         "설명 가능한 AI"
check_keyword /ai/console        "2차년도\|2차"      "2차년도 명시"

# ─────────────────────────────────────────────────────
# 사이드바 — 외국항공사 액터 + 11박스 표기 확인
# ─────────────────────────────────────────────────────
section "사이드바 — 11박스 메뉴 노출"
check_keyword /main              "배출량 모니터링\|EMP" "박스 ① 사이드바"
check_keyword /main              "배출량보고서\|ER"     "박스 ② 사이드바"
check_keyword /main              "적격연료\|CEF"        "박스 ③ 사이드바"
check_keyword /main              "검증보고서\|VR"       "박스 ④ 사이드바"
check_keyword /main              "배출권취소\|EUCR"     "박스 ⑤ 사이드바"
check_keyword /main              "적정성검토\|OoM"      "박스 ⑥ 사이드바"
check_keyword /main              "CORSIA 세부항목"      "박스 ⑦ 사이드바"
check_keyword /main              "SAF"                  "박스 ⑧ 사이드바"
check_keyword /main              "통합 워크플로우"      "박스 ⑨ 사이드바"
check_keyword /main              "AI 콘솔"              "박스 ⑩ 사이드바"

logout
print_summary
[[ $FAIL -eq 0 ]] && exit 0 || exit 1
