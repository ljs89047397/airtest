<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<style>
/* 사이드바 디자인 보강 — 활성 바 + 호버 트랜지션 + 그룹 헤더 */
#sidebar { scrollbar-color: #475569 #1e293b; }
#sidebar::-webkit-scrollbar { width: 6px; }
#sidebar::-webkit-scrollbar-track { background: #1e293b; }
#sidebar::-webkit-scrollbar-thumb { background: #475569; border-radius: 3px; }

#sidebar .sidebar-link {
  position: relative;
  font-size: 0.86rem;
  margin-bottom: 2px;
  border-radius: 6px;
  transition: background .15s ease, color .15s ease, padding-left .15s ease;
  border-left: 3px solid transparent;
}
#sidebar .sidebar-link:hover {
  background: rgba(59,130,246,0.12);
  color: #fff !important;
  padding-left: calc(0.75rem + 4px);
}
#sidebar .sidebar-link.active {
  background: rgba(59,130,246,0.18);
  color: #fff !important;
  border-left: 3px solid #3B82F6;
  font-weight: 600;
  padding-left: calc(0.75rem - 3px);
}

#sidebar .sidebar-group {
  font-size: 0.68rem;
  letter-spacing: 0.12em;
  color: #64748b;
  text-transform: uppercase;
  font-weight: 600;
  padding: 12px 10px 6px;
  margin-top: 8px;
  border-top: 1px solid rgba(255,255,255,0.05);
}
#sidebar .sidebar-group:first-of-type { border-top: none; margin-top: 0; }
</style>
<div id="sidebar" style="width:220px;height:calc(100vh - 60px);max-height:calc(100vh - 60px);background:#1e293b;position:fixed;top:60px;left:0;overflow-y:auto;overflow-x:hidden;">
  <div class="p-3">

    <!-- 메인 -->
    <div class="sidebar-group">메인</div>
    <a href="/main" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'dashboard'}">active</c:if>">
      &#128202; 대시보드
    </a>
    <a href="/com/user/me/password" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'passwordChange'}">active</c:if>">
      &#128274; 비밀번호 변경
    </a>

    <!-- 모니터링 계획 -->
    <div class="sidebar-group">모니터링 계획</div>
    <a href="/emp/plan" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'emp'}">active</c:if>">
      &#128221; 배출량 모니터링(EMP)
    </a>

    <!-- 배출량관리 -->
    <div class="sidebar-group">배출량관리</div>
    <a href="/er/list" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'er'}">active</c:if>">
      &#128203; 배출량보고서(ER)
    </a>
    <a href="/vr/list" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'vr'}">active</c:if>">
      &#9989; 검증보고서(VR)
    </a>
    <a href="/er/cef/list" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'cef'}">active</c:if>">
      &#9981; 적격연료(CEF)
    </a>
    <a href="/er/eucr/list" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'eucr'}">active</c:if>">
      &#128179; 배출권취소(EUCR)
    </a>
    <a href="/er/oom/list" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'oom'}">active</c:if>">
      &#128270; 적정성검토(OoM)
    </a>
    <a href="/er/oom/qchk/list" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'qchk'}">active</c:if>">
      &#128203; CORSIA 세부항목 검증
    </a>

    <!-- SAF -->
    <div class="sidebar-group">SAF</div>
    <a href="/saf/dashboard" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'saf'}">active</c:if>">
      &#127807; SAF 이행현황
    </a>
    <a href="/saf/cert" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'safCert'}">active</c:if>">
      &#128209; SAF 인증서
    </a>
    <a href="/saf/batch" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'safBatch'}">active</c:if>">
      &#128230; SAF 배치
    </a>
    <a href="/saf/airprt" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'safAirprt'}">active</c:if>">
      &#9992; 공항별 급유&middot;구매
    </a>
    <a href="/saf/mntr" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'safMntr'}">active</c:if>">
      &#128200; 혼합비율 모니터링
    </a>

    <!-- 포털 -->
    <div class="sidebar-group">포털</div>
    <a href="/ptl/workflow" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'workflow'}">active</c:if>">
      &#128260; 통합 워크플로우
    </a>
    <a href="/ptl/stat" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'stat'}">active</c:if>">
      &#128200; 통계/시뮬레이션
    </a>
    <a href="/ptl/ccr" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'ccr'}">active</c:if>">
      &#128229; CCR 추출
    </a>
    <a href="/ptl/sim" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'sim'}">active</c:if>">
      &#129518; 상쇄비용 시뮬
    </a>
    <a href="/ptl/calendar" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'calendar'}">active</c:if>">
      &#128197; 운영 일정
    </a>
    <a href="/ptl/actn" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'actn'}">active</c:if>">
      &#128221; 감사로그
    </a>

    <!-- 공통 AI 서비스 (RFP 박스 ⑩ — 2차년도 자리) -->
    <div class="sidebar-group">AI 서비스</div>
    <a href="/ai/console" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'aiConsole'}">active</c:if>">
      &#129302; AI 콘솔 (2차년도)
    </a>

    <!-- 관리 (MOLIT/KOTSA 전용) -->
    <c:if test="${sessionScope.ognzSeCd == 'MOLIT' or sessionScope.ognzSeCd == 'KOTSA' or empty sessionScope.ognzSeCd}">
      <div class="sidebar-group">관리</div>
      <a href="/com/user" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comUser'}">active</c:if>">
        &#128100; 사용자 관리
      </a>
      <a href="/com/ognz" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comOgnz'}">active</c:if>">
        &#127970; 조직 관리
      </a>
      <a href="/com/oprtr" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comOprtr'}">active</c:if>">
        &#9992; 항공기 등록부
      </a>
      <a href="/com/vrfcn" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comVrfcn'}">active</c:if>">
        &#9989; 검증기관
      </a>
      <a href="/com/authrt" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comAuthrt'}">active</c:if>">
        &#128272; 권한 관리
      </a>
      <a href="/com/role" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comRole'}">active</c:if>">
        &#127918; 역할 관리
      </a>
      <a href="/com/menu" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comMenu'}">active</c:if>">
        &#128196; 메뉴 관리
      </a>
      <a href="/com/prgrm" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comPrgrm'}">active</c:if>">
        &#128187; 프로그램 관리
      </a>
      <a href="/com/cd" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comCd'}">active</c:if>">
        &#9776; 공통코드
      </a>
      <a href="/com/atrz" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comAtrz'}">active</c:if>">
        &#128221; 결재함
      </a>
      <a href="/com/rglt" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'comRglt'}">active</c:if>">
        &#128218; 규정 게시판
      </a>
      <a href="/com/eco-fleet" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'ecoFleet'}">active</c:if>">
        &#127757; 친환경 항공기 도입
      </a>
      <a href="/admin/icao-submit" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'icaoSubmit'}">active</c:if>">
        &#9992; ICAO 송신 콘솔
      </a>
      <a href="/admin/health" class="d-block px-3 py-2 rounded text-decoration-none text-white-50 sidebar-link <c:if test="${currentMenu == 'adminHealth'}">active</c:if>">
        &#128268; 시스템 상태
      </a>
    </c:if>

  </div>
</div>
<style>
.sidebar-link:hover, .sidebar-link.active { background:#0F2C72 !important; color:white !important; }
/* 사이드바 스크롤바 스타일 (overflow-y:auto 보강) */
#sidebar::-webkit-scrollbar { width:6px; }
#sidebar::-webkit-scrollbar-track { background:#1e293b; }
#sidebar::-webkit-scrollbar-thumb { background:#475569; border-radius:3px; }
#sidebar::-webkit-scrollbar-thumb:hover { background:#64748b; }
/* 마지막 항목 하단 여백 */
#sidebar > .p-3 { padding-bottom:2rem !important; }
</style>
<script>
/* ─────────────────────────────────────────────────────────
   사이드바 스크롤 위치 보존 (sessionStorage)
   메뉴 클릭으로 페이지 이동해도 사용자가 보던 위치 유지
   ───────────────────────────────────────────────────────── */
(function () {
  var SIDEBAR_SCROLL_KEY = 'icasSidebarScrollTop';
  function restoreScroll() {
    var sb = document.getElementById('sidebar');
    if (!sb) return;
    var saved = sessionStorage.getItem(SIDEBAR_SCROLL_KEY);
    if (saved != null) sb.scrollTop = parseInt(saved, 10) || 0;
    // 메뉴 클릭 시 현재 스크롤 위치 저장
    sb.querySelectorAll('a.sidebar-link').forEach(function (a) {
      a.addEventListener('click', function () {
        sessionStorage.setItem(SIDEBAR_SCROLL_KEY, sb.scrollTop);
      });
    });
    // 스크롤 위치도 throttle 로 저장 (사용자가 스크롤만 하고 클릭 안 해도 보존)
    var t = null;
    sb.addEventListener('scroll', function () {
      if (t) clearTimeout(t);
      t = setTimeout(function () {
        sessionStorage.setItem(SIDEBAR_SCROLL_KEY, sb.scrollTop);
      }, 100);
    });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', restoreScroll);
  else restoreScroll();
})();
</script>
