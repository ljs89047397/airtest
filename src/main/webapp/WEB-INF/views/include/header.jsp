<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:csrfMetaTags/>
<%-- Spring Security CSRF 메타 태그 +
     Pretendard 폰트 + 전역 디자인 토큰 (모든 화면이 header.jsp 를 include) --%>
<link rel="stylesheet" as="style" crossorigin
      href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.css" />
<style>
:root {
  --icas-primary:   #0F2C72;
  --icas-primary-2: #1A3A8C;
  --icas-secondary: #3B82F6;
  --icas-accent:    #F59E0B;
  --icas-success:   #10B981;
  --icas-danger:    #DC3545;
  --icas-bg:        #F5F7FB;
  --icas-bg-card:   #FFFFFF;
  --icas-border:    #E5E7EB;
  --icas-text:      #1F2937;
  --icas-text-muted:#6B7280;
}

/* 전역 폰트 (Pretendard) + 기본 톤 */
html, body, input, select, textarea, button {
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont,
               'Apple SD Gothic Neo', 'Noto Sans KR', 'Malgun Gothic',
               system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
body { color: var(--icas-text); }

/* 카드 그림자 + 여백 통일 */
.card.shadow-sm { box-shadow: 0 4px 16px rgba(15,44,114,0.06) !important; }
.card { border-radius: 8px; border-color: var(--icas-border); }
.card-header { padding: 14px 20px; }
.card-body  { padding: 20px; }

/* 테이블 — 행 호버 + 헤더 가독성 */
.table { color: var(--icas-text); }
.table thead th { font-weight: 600; }
.table tbody tr:hover { background: #f0f5ff !important; }
.table-icas tbody tr:nth-child(odd) { background: #fafbfd; }

/* 버튼 마이크로 인터랙션 */
.btn {
  transition: transform .15s ease, box-shadow .15s ease;
  font-weight: 500;
}
.btn:hover:not(:disabled) { transform: translateY(-1px); }
.btn-primary, .btn-icas {
  background: var(--icas-primary); border-color: var(--icas-primary);
}
.btn-primary:hover, .btn-icas:hover {
  background: var(--icas-primary-2); border-color: var(--icas-primary-2);
  box-shadow: 0 4px 12px rgba(15,44,114,0.25);
}

/* 알림 벨 + 결재 배지 */
.notify-wrap { position: relative; }
.notify-bell {
  display: inline-flex; align-items: center; justify-content: center;
  width: 34px; height: 34px; border-radius: 50%;
  background: rgba(255,255,255,0.08); color: #fff; text-decoration: none;
  transition: background .15s ease;
}
.notify-bell:hover { background: rgba(255,255,255,0.2); color: #fff; }
.notify-badge {
  position: absolute; top:-4px; right:-4px;
  min-width: 18px; height: 18px; padding: 0 5px; border-radius: 9px;
  background: var(--icas-danger); color: #fff;
  font-size: 11px; font-weight: 700; line-height: 18px; text-align: center;
  border: 2px solid var(--icas-primary);
}

/* 헤더 슬로건 */
.header-tagline { color: rgba(255,255,255,0.6); font-size: 0.72rem; margin-left: 4px; }

/* Empty State — 빈 데이터 행 */
.empty-state { text-align:center; padding:48px 24px; color:#9ca3af; }
.empty-state .empty-icon { font-size:2.5rem; color:#d1d5db; margin-bottom:12px; }
.empty-state .empty-title { font-size:0.95rem; font-weight:600; color:#6b7280; margin-bottom:4px; }
.empty-state .empty-desc  { font-size:0.82rem; color:#9ca3af; }
td.empty-state-cell { background:#fafbfc !important; }

/* Cmd+K 검색 팔레트 키보드 단축키 강조 */
kbd { background:#f1f5f9; color:#475569; border-radius:3px; padding:1px 6px; font-size:0.7rem; border:1px solid #e2e8f0; font-family: 'SF Mono', Consolas, monospace; }

/* 라이프사이클 스테퍼 — EMP/ER/VR/EUCR/CEF 공용 */
.lifecycle-step {
  display:inline-flex; align-items:center; padding:6px 14px; border-radius:20px;
  font-size:0.78rem; font-weight:600; transition: all .2s ease;
  border:1.5px solid transparent;
}
.lifecycle-step.is-done   { background:#10B981; color:#fff; border-color:#10B981; }
.lifecycle-step.is-active { background:#3B82F6; color:#fff; border-color:#3B82F6; box-shadow:0 0 0 4px rgba(59,130,246,0.18); }
.lifecycle-step.is-todo   { background:#fff; color:#9CA3AF; border-color:#E5E7EB; }
.lifecycle-step.is-reject { background:#FEE2E2; color:#DC2626; border-color:#FCA5A5; }
.lifecycle-arrow { color:#CBD5E1; margin:0 4px; }

/* 결재 라벨 색상 톤 */
.badge.bg-secondary { background:#94A3B8 !important; }
.badge.bg-primary   { background:#3B82F6 !important; }
.badge.bg-warning   { background:#F59E0B !important; color:#fff !important; }
.badge.bg-info      { background:#0EA5E9 !important; color:#fff !important; }
.badge.bg-success   { background:#10B981 !important; }
.badge.bg-danger    { background:#DC2626 !important; }
</style>

<nav class="navbar navbar-expand-lg fixed-top" style="background:#0F2C72; height:60px;">
  <div class="container-fluid px-4">
    <a class="navbar-brand text-white fw-bold d-flex align-items-baseline" href="/main">
      <span>&#9992; 국제항공 탄소배출량 관리시스템</span>
      <span class="header-tagline d-none d-lg-inline">국토교통부 · 한국교통안전공단</span>
    </a>
    <div class="d-flex align-items-center gap-3 ms-auto">
      <span class="text-white-50 small">보고연도</span>
      <select id="globalYr" class="form-select form-select-sm" style="width:90px;background:#1a3a8c;color:white;border-color:#2d4fa0;">
        <option value="2026">2026</option>
        <option value="2025">2025</option>
        <option value="2024">2024</option>
      </select>
      <div class="text-white-50 small">|</div>

      <%-- 전역 검색 (Cmd+K / Ctrl+K) --%>
      <button type="button" class="notify-bell" id="btnGlobalSearch"
              title="화면 빠른 이동 (Cmd+K / Ctrl+K)" style="border:0;">
        <i class="bi bi-search"></i>
      </button>

      <%-- 알림 벨 — 결재 대기 N건 (KOTSA 패턴) --%>
      <div class="notify-wrap">
        <a href="/com/atrz" class="notify-bell" title="결재 대기 알림">
          <i class="bi bi-bell-fill"></i>
        </a>
        <span class="notify-badge d-none" id="atrzPendingBadge">0</span>
      </div>

      <a href="/manual" target="_blank" class="btn btn-sm btn-outline-light" title="시스템 매뉴얼 (새 창)">
        <i class="bi bi-book me-1"></i>매뉴얼
      </a>
      <span class="text-white small"><i class="bi bi-person-circle me-1"></i>관리자</span>
      <button type="button" id="btnLogout" class="btn btn-sm btn-outline-light">로그아웃</button>
    </div>
  </div>
</nav>

<%-- 전역 검색 팔레트 (Cmd+K) --%>
<div id="searchPalette" style="display:none;position:fixed;inset:0;background:rgba(15,23,42,0.5);z-index:1080;align-items:flex-start;justify-content:center;padding-top:80px;">
  <div style="width:560px;max-width:92%;background:#fff;border-radius:12px;box-shadow:0 20px 60px rgba(0,0,0,0.25);overflow:hidden;">
    <div style="display:flex;align-items:center;border-bottom:1px solid #e5e7eb;padding:14px 18px;">
      <i class="bi bi-search me-2" style="color:#6b7280;"></i>
      <input type="text" id="searchPaletteInput" placeholder="화면명 또는 키워드 입력 (예: EMP, 시뮬, 결재)"
             style="flex:1;border:0;outline:0;font-size:1rem;background:transparent;">
      <span class="badge bg-light text-dark border" style="font-size:0.7rem;">Esc</span>
    </div>
    <div id="searchPaletteResults" style="max-height:420px;overflow-y:auto;"></div>
    <div style="border-top:1px solid #e5e7eb;padding:8px 18px;background:#f9fafb;font-size:0.72rem;color:#6b7280;display:flex;gap:16px;">
      <span><kbd>↑↓</kbd> 이동</span>
      <span><kbd>Enter</kbd> 이동</span>
      <span><kbd>Esc</kbd> 닫기</span>
    </div>
  </div>
</div>

<script src="/resources/js/common/icas-esc.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<%-- icas-csrf.js: jQuery 로드된 뒤 실행되도록 동적 삽입 --%>
<script>
(function() {
  var SCREEN_INDEX = [
    {path:'/main',                title:'대시보드',              keywords:'main dashboard'},
    {path:'/emp/plan',            title:'EMP 모니터링 계획',     keywords:'emp 배출량 모니터링 계획'},
    {path:'/er/list',             title:'ER 배출량보고서',       keywords:'er 배출량 보고서'},
    {path:'/vr/list',             title:'VR 검증보고서',         keywords:'vr 검증 보고서'},
    {path:'/er/cef',              title:'CEF 적격연료',          keywords:'cef 적격 연료 청구'},
    {path:'/er/eucr',             title:'EUCR 배출권취소',       keywords:'eucr 배출권 취소'},
    {path:'/er/oom',              title:'OoM 적정성 검토',       keywords:'oom 적정성'},
    {path:'/er/oom/qchk',         title:'CORSIA 세부항목 검증',  keywords:'corsia rule 18 정량'},
    {path:'/saf/dashboard',       title:'SAF 이행현황',          keywords:'saf 이행률 대시보드'},
    {path:'/saf/cert',            title:'SAF 인증서',            keywords:'saf cert 인증서'},
    {path:'/saf/batch',           title:'SAF 배치',              keywords:'saf batch 배치'},
    {path:'/saf/airprt',          title:'공항별 SAF 급유·구매',  keywords:'saf airprt 공항 급유 구매'},
    {path:'/saf/mntr',            title:'SAF 혼합비율 모니터링', keywords:'saf 혼합 모니터링 의무'},
    {path:'/ptl/workflow',        title:'통합 워크플로우',       keywords:'ptl workflow 워크플로우'},
    {path:'/ptl/stat',            title:'통계/시뮬레이션',       keywords:'ptl stat 통계 시뮬'},
    {path:'/ptl/sim',             title:'상쇄비용 시뮬',         keywords:'ptl sim 상쇄 시뮬'},
    {path:'/ptl/ccr',             title:'CCR 추출',              keywords:'ptl ccr 추출'},
    {path:'/ptl/calendar',        title:'CORSIA 운영 일정',      keywords:'ptl calendar 일정 캘린더'},
    {path:'/ptl/actn',            title:'감사로그',              keywords:'ptl actn 감사 로그'},
    {path:'/ai/console',          title:'AI 콘솔 (2차)',         keywords:'ai console sllm'},
    {path:'/com/user',            title:'사용자 관리',           keywords:'com user 사용자'},
    {path:'/com/ognz',            title:'조직 관리',             keywords:'com ognz 조직'},
    {path:'/com/oprtr',           title:'항공기 등록부',         keywords:'com oprtr 운영사 항공기'},
    {path:'/com/vrfcn',           title:'검증기관 관리',         keywords:'com vrfcn 검증 기관'},
    {path:'/com/role',            title:'역할 관리',             keywords:'com role 역할'},
    {path:'/com/authrt',          title:'권한 관리',             keywords:'com authrt 권한'},
    {path:'/com/cd',              title:'공통코드 관리',         keywords:'com cd 공통 코드'},
    {path:'/com/menu',            title:'메뉴 관리',             keywords:'com menu 메뉴'},
    {path:'/com/prgrm',           title:'프로그램 관리',         keywords:'com prgrm 프로그램'},
    {path:'/com/atrz',            title:'결재함',                keywords:'com atrz 결재 결재함'},
    {path:'/com/rglt',            title:'규정 게시판',           keywords:'com rglt 규정 게시판'},
    {path:'/manual',              title:'시스템 매뉴얼',         keywords:'manual 매뉴얼 도움말'}
  ];

  var palette = document.getElementById('searchPalette');
  var input   = document.getElementById('searchPaletteInput');
  var results = document.getElementById('searchPaletteResults');
  var currentIndex = 0;
  var currentRows  = [];

  function renderResults(q) {
    q = (q||'').trim().toLowerCase();
    var rows = SCREEN_INDEX.filter(function(s){
      if (!q) return true;
      return (s.title.toLowerCase().indexOf(q) >= 0) ||
             (s.keywords.toLowerCase().indexOf(q) >= 0) ||
             (s.path.toLowerCase().indexOf(q) >= 0);
    }).slice(0, 12);
    currentRows = rows;
    if (currentIndex >= rows.length) currentIndex = 0;
    if (rows.length === 0) {
      results.innerHTML = '<div style="padding:24px;text-align:center;color:#9ca3af;font-size:0.9rem;">검색 결과 없음</div>';
      return;
    }
    var html = '';
    rows.forEach(function(s, i){
      var active = i === currentIndex;
      html += '<a href="' + s.path + '" data-idx="' + i + '" class="palette-row" '
            + 'style="display:flex;align-items:center;gap:12px;padding:10px 18px;text-decoration:none;color:#1f2937;'
            + (active ? 'background:#eef2ff;' : '') + '">'
            + '<i class="bi bi-arrow-right-circle" style="color:#3b82f6;"></i>'
            + '<div style="flex:1;"><div style="font-weight:500;">' + s.title + '</div>'
            + '<div style="font-size:0.75rem;color:#9ca3af;">' + s.path + '</div></div>'
            + (active ? '<span class="badge bg-primary">Enter</span>' : '')
            + '</a>';
    });
    results.innerHTML = html;
  }

  function openPalette() {
    palette.style.display = 'flex';
    currentIndex = 0;
    input.value = '';
    renderResults('');
    setTimeout(function(){ input.focus(); }, 50);
  }
  function closePalette() { palette.style.display = 'none'; }

  document.getElementById('btnGlobalSearch').addEventListener('click', openPalette);

  document.addEventListener('keydown', function(e){
    var isMod = e.metaKey || e.ctrlKey;
    if (isMod && e.key.toLowerCase() === 'k') { e.preventDefault(); openPalette(); return; }
    if (palette.style.display !== 'flex') return;
    if (e.key === 'Escape')   { closePalette(); }
    else if (e.key === 'ArrowDown') { e.preventDefault(); currentIndex = Math.min(currentRows.length-1, currentIndex+1); renderResults(input.value); }
    else if (e.key === 'ArrowUp')   { e.preventDefault(); currentIndex = Math.max(0, currentIndex-1); renderResults(input.value); }
    else if (e.key === 'Enter' && currentRows[currentIndex]) { location.href = currentRows[currentIndex].path; }
  });
  input.addEventListener('input', function(){ currentIndex = 0; renderResults(input.value); });
  palette.addEventListener('click', function(e){ if (e.target === palette) closePalette(); });

  /* 로그아웃 — Spring Security 는 POST 만 받음. 명시적 POST 호출로 세션 무효화. */
  function doLogout(){
    function go() {
      var csrf = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
      var token = csrf ? decodeURIComponent(csrf[1]) : '';
      fetch('/api/com/auth/logout', { method:'POST', headers: token ? {'X-XSRF-TOKEN': token} : {} })
        .then(function(){ location.href = '/login'; })
        .catch(function(){ location.href = '/login'; });
    }
    if (typeof jQuery !== 'undefined') go(); else setTimeout(doLogout, 50);
  }
  var btnLogout = document.getElementById('btnLogout');
  if (btnLogout) btnLogout.addEventListener('click', doLogout);

  /* CSRF + 결재 펜딩 알림 */
  function loadCsrfScript() {
    if (typeof jQuery === 'undefined') { setTimeout(loadCsrfScript, 10); return; }
    var s = document.createElement('script');
    s.src = '/resources/js/common/icas-csrf.js'; s.defer = false;
    document.head.appendChild(s);
    function refreshAtrzBadge() {
      jQuery.get('/api/com/atrz/my-pending').done(function(res){
        var rows = (res && res.data) ? res.data : [];
        var cnt = Array.isArray(rows) ? rows.length : 0;
        var $b = jQuery('#atrzPendingBadge');
        if (cnt > 0) { $b.text(cnt > 99 ? '99+' : cnt).removeClass('d-none'); }
        else         { $b.addClass('d-none'); }
      }).fail(function(){});
    }
    if (window.location.pathname !== '/login') {
      setTimeout(refreshAtrzBadge, 500);
      setInterval(refreshAtrzBadge, 60000);
    }
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', loadCsrfScript);
  else loadCsrfScript();

  /* ─────────────────────────────────────────────────────
     세션 타임아웃 (Tomcat 기본 30분) + 만료 5분 전 경고
     사용자 활동 감지 시 카운터 리셋 (debounce 60s)
     ───────────────────────────────────────────────────── */
  var SESS_TIMEOUT_SEC = 1800;   /* 30분 */
  var SESS_WARN_SEC    = 300;    /* 만료 5분 전 경고 */
  var lastActivity = Date.now();
  var warnShown = false;

  function onActivity() {
    var now = Date.now();
    /* 60초 throttle */
    if (now - lastActivity < 60000) return;
    lastActivity = now;
    warnShown = false;
    /* 서버 세션도 갱신 (가벼운 GET) */
    if (typeof jQuery !== 'undefined') {
      jQuery.get('/api/com/user/me').fail(function(){ /* 무시 */ });
    }
  }
  ['click','keydown','mousemove','scroll'].forEach(function(ev){
    document.addEventListener(ev, onActivity, { passive:true });
  });

  function checkSession() {
    if (window.location.pathname === '/login') return;
    var idleSec = Math.floor((Date.now() - lastActivity) / 1000);
    var remainSec = SESS_TIMEOUT_SEC - idleSec;
    if (remainSec <= 0) {
      /* 만료 — 강제 로그아웃 */
      var csrf = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
      var token = csrf ? decodeURIComponent(csrf[1]) : '';
      fetch('/api/com/auth/logout', { method:'POST', headers: token?{'X-XSRF-TOKEN':token}:{} })
        .finally(function(){ location.href = '/login?expired=1'; });
    } else if (remainSec <= SESS_WARN_SEC && !warnShown) {
      warnShown = true;
      if (window.IcasAlert && IcasAlert.warning) {
        IcasAlert.warning('세션이 약 ' + Math.ceil(remainSec/60) + '분 후 만료됩니다. 작업을 저장하고 페이지를 클릭하면 연장됩니다.');
      }
    }
  }
  setInterval(checkSession, 30000); /* 30초 주기 */
})();
</script>
