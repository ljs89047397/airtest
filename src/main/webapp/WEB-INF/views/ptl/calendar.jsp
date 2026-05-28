<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CORSIA 운영 일정 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f5f7fb; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
.cal-summary { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:24px; }
.cal-card { background:#fff; border-left:4px solid var(--icas-primary); border-radius:8px; padding:16px 18px; box-shadow:0 2px 8px rgba(15,44,114,0.06); }
.cal-card.due-soon { border-left-color:#dc3545; background:#fff5f5; }
.cal-card .lbl { font-size:0.8rem; color:#6c757d; margin-bottom:4px; }
.cal-card .val { font-size:1.4rem; font-weight:700; color:var(--icas-primary); }
.cal-card .sub { font-size:0.75rem; color:#888; margin-top:4px; }
.timeline { background:#fff; border-radius:8px; box-shadow:0 2px 8px rgba(15,44,114,0.06); padding:24px; }
.timeline-header { font-weight:700; color:var(--icas-primary); margin-bottom:20px; font-size:1.05rem; }
.timeline-grid { display:grid; grid-template-columns:repeat(12,1fr); gap:4px; }
.month-col { padding:8px 4px; text-align:center; font-size:0.85rem; color:#666; border-bottom:2px solid #e9ecef; }
.month-col.current { background:#fff5d6; font-weight:600; color:#856404; }
.task-row { display:grid; grid-template-columns:200px 1fr; gap:12px; padding:10px 0; border-bottom:1px solid #f0f0f0; align-items:center; }
.task-row:last-child { border-bottom:none; }
.task-actor { font-size:0.7rem; padding:2px 6px; border-radius:3px; margin-right:6px; }
.task-actor.molit { background:var(--icas-primary); color:#fff; }
.task-actor.kotsa { background:#2563eb; color:#fff; }
.task-actor.airline { background:#10b981; color:#fff; }
.task-actor.verifier { background:#f59e0b; color:#fff; }
.task-bar-wrap { position:relative; height:24px; background:#f8f9fa; border-radius:4px; }
.task-bar { position:absolute; top:0; bottom:0; background:linear-gradient(90deg,var(--icas-primary),#2563eb); border-radius:4px; display:flex; align-items:center; padding:0 8px; color:#fff; font-size:0.72rem; font-weight:600; white-space:nowrap; overflow:hidden; }
.task-bar.icao { background:linear-gradient(90deg,#dc3545,#ef4444); }
.task-bar.notice { background:linear-gradient(90deg,#f59e0b,#fbbf24); color:#000; }
.task-bar.airline { background:linear-gradient(90deg,#059669,#10b981); }
.task-bar.verifier { background:linear-gradient(90deg,#d97706,#f59e0b); }
.legend { display:flex; gap:16px; margin-top:24px; font-size:0.8rem; color:#555; flex-wrap:wrap; }
.legend-dot { width:14px; height:14px; border-radius:3px; display:inline-block; vertical-align:middle; margin-right:6px; }
.badge-due  { background:#dc3545; color:#fff; padding:2px 6px; border-radius:3px; font-size:0.7rem; font-weight:600; }
.badge-soon { background:#fbbf24; color:#000; padding:2px 6px; border-radius:3px; font-size:0.7rem; font-weight:600; }
.badge-ok   { background:#10b981; color:#fff; padding:2px 6px; border-radius:3px; font-size:0.7rem; font-weight:600; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="page-header-bar px-4 py-3">
    <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-calendar3 me-2"></i>CORSIA 운영 일정 캘린더</h5>
    <div class="text-muted small mt-1">2026년 국제항공 탄소 배출량 관리 시행계획 — 연간 핵심 마감 일정</div>
  </div>

  <div class="p-4">
    <!-- 요약 카드 -->
    <div class="cal-summary" id="summaryCards"></div>

    <!-- 타임라인 -->
    <div class="timeline mb-3">
      <div class="timeline-header"><i class="bi bi-bar-chart-steps me-2"></i>2026년 연간 워크플로우</div>
      <div class="timeline-grid">
        <c:forEach var="m" begin="1" end="12">
          <div class="month-col" id="monthCol${m}">${m}월</div>
        </c:forEach>
      </div>

      <div class="mt-3">
        <div class="task-row"><div><span class="task-actor airline">항공사</span>EMP 제출</div>
          <div class="task-bar-wrap"><div class="task-bar airline" style="left:0%;width:25%;">1~3월 EMP 작성·제출</div></div></div>
        <div class="task-row"><div><span class="task-actor airline">항공사</span>ER 본 보고 작성</div>
          <div class="task-bar-wrap"><div class="task-bar airline" style="left:0%;width:33%;">1~4월 ER 작성·제출</div></div></div>
        <div class="task-row"><div><span class="task-actor verifier">검증기관</span>VR 검증 + 의견서</div>
          <div class="task-bar-wrap"><div class="task-bar verifier" style="left:33.33%;width:16.67%;">5~6월 검증·집계</div></div></div>
        <div class="task-row"><div><span class="task-actor kotsa">KOTSA</span>OoM Rule 18종</div>
          <div class="task-bar-wrap"><div class="task-bar" style="left:41.67%;width:16.67%;">6~7월 적정성 검토</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>ICAO 배출량 제출</div>
          <div class="task-bar-wrap"><div class="task-bar icao" style="left:50%;width:8.33%;">7월 ICAO 제출</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>상쇄의무량 산정·통보</div>
          <div class="task-bar-wrap"><div class="task-bar notice" style="left:83.33%;width:8.33%;">11월 의무량 통보</div></div></div>
        <div class="task-row"><div><span class="task-actor airline">항공사</span>EUCR 배출권 취소</div>
          <div class="task-bar-wrap"><div class="task-bar airline" style="left:91.67%;width:8.33%;">12월~ EUCR 등록</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>SAF '30년 의무비율 확정</div>
          <div class="task-bar-wrap"><div class="task-bar" style="left:41.67%;width:8.33%;">6월 (3~5%)</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>항공환경세미나(제12회)</div>
          <div class="task-bar-wrap"><div class="task-bar" style="left:33.33%;width:8.33%;">5월</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>ICAO Aviation Climate Week</div>
          <div class="task-bar-wrap"><div class="task-bar" style="left:41.67%;width:4%;">6.2~6.6</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>UN COP31</div>
          <div class="task-bar-wrap"><div class="task-bar icao" style="left:83.33%;width:4%;">11.9~11.21</div></div></div>
        <div class="task-row"><div><span class="task-actor molit">국토부</span>감축기술 매뉴얼 개정</div>
          <div class="task-bar-wrap"><div class="task-bar" style="left:91.67%;width:8.33%;">12월</div></div></div>
      </div>

      <div class="legend">
        <span><span class="legend-dot" style="background:#0F2C72;"></span>일반 마감</span>
        <span><span class="legend-dot" style="background:#dc3545;"></span>ICAO 의무 송신</span>
        <span><span class="legend-dot" style="background:#fbbf24;"></span>의무량 통보</span>
        <span><span class="legend-dot" style="background:#10b981;"></span>운영사 작업</span>
        <span><span class="legend-dot" style="background:#f59e0b;"></span>검증기관 작업</span>
      </div>
    </div>

    <!-- 상세 일정표 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-list-check me-2"></i>상세 일정표 (시행계획 근거)</h6>
      </div>
      <div class="card-body p-0">
        <table class="table table-hover table-sm mb-0">
          <thead style="background:#f5f7fb;">
            <tr>
              <th class="ps-3" style="width:110px;">기한</th>
              <th style="width:120px;">담당</th>
              <th>업무</th>
              <th style="width:160px;">근거</th>
              <th style="width:90px;">상태</th>
            </tr>
          </thead>
          <tbody id="dueListBody"></tbody>
        </table>
      </div>
    </div>

  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
(function () {
  var today = new Date(2026, 4, 24); // 2026-05-24
  var curMonth = today.getMonth() + 1;
  var el = document.getElementById('monthCol' + curMonth);
  if (el) el.classList.add('current');

  var SCHEDULES = [
    { dueDate:'2026-03-31', actor:'항공사',   task:'EMP(모니터링계획) 제출 마감',         src:'관리법 §6' },
    { dueDate:'2026-04-30', actor:'항공사',   task:'ER(배출량보고서) 작성·제출',          src:'관리법 §7' },
    { dueDate:'2026-05-15', actor:'KOTSA',   task:'제12회 항공환경세미나',                src:'시행계획 p.14' },
    { dueDate:'2026-06-06', actor:'국토부',   task:'ICAO Aviation Climate Week 참석',     src:'시행계획 p.12' },
    { dueDate:'2026-06-30', actor:'검증기관', task:'VR 검증의견서 제출 (MRV 집계)',        src:'CORSIA MRV 5~6월' },
    { dueDate:'2026-06-30', actor:'국토부',   task:'SAF 30년 혼합의무 비율 확정(3~5%)',    src:'시행계획 p.5' },
    { dueDate:'2026-07-31', actor:'국토부',   task:'ICAO 배출량 보고서 송신',              src:'CORSIA MRV 7월' },
    { dueDate:'2026-08-31', actor:'KOTSA',   task:'OoM Rule 18종 적정성 검토 종료',       src:'관리법 §9' },
    { dueDate:'2026-11-09', actor:'국토부',   task:'UN COP31 (브라질 벨렘) 참석',          src:'시행계획 p.12' },
    { dueDate:'2026-11-30', actor:'국토부',   task:'국적사별 상쇄의무량 산정·공식 통보',   src:'시행계획 p.13' },
    { dueDate:'2026-12-31', actor:'국토부',   task:'항공온실가스 감축기술 매뉴얼 개정',    src:'시행계획 p.13' },
    { dueDate:'2028-01-31', actor:'항공사',   task:'CORSIA 1주기 상쇄의무 정산 (EUCR 제출)', src:'시행계획 p.2' }
  ];

  function daysBetween(d1, d2) { return Math.ceil((d2 - d1) / 86400000); }
  function getBadge(dueDateStr) {
    var diff = daysBetween(today, new Date(dueDateStr));
    if (diff < 0)  return '<span class="badge-ok">완료</span>';
    if (diff <= 14) return '<span class="badge-due">D-' + diff + '</span>';
    if (diff <= 60) return '<span class="badge-soon">D-' + diff + '</span>';
    return '<span class="badge-ok">D-' + diff + '</span>';
  }
  function actorBadge(a) {
    if (a === '국토부')   return '<span class="task-actor molit">국토부</span>';
    if (a === 'KOTSA')   return '<span class="task-actor kotsa">KOTSA</span>';
    if (a === '항공사')   return '<span class="task-actor airline">항공사</span>';
    return '<span class="task-actor verifier">검증기관</span>';
  }

  var upcoming = SCHEDULES.filter(function(s){ return new Date(s.dueDate) >= today; }).slice(0, 4);
  var sumHtml = '';
  upcoming.forEach(function(s){
    var due = new Date(s.dueDate);
    var diff = daysBetween(today, due);
    var cls = diff <= 30 ? 'due-soon' : '';
    sumHtml += '<div class="cal-card ' + cls + '">'
      + '<div class="lbl">' + s.actor + ' · D-' + diff + '</div>'
      + '<div class="val">' + due.toLocaleDateString('ko-KR') + '</div>'
      + '<div class="sub">' + s.task + '</div>'
      + '</div>';
  });
  document.getElementById('summaryCards').innerHTML = sumHtml;

  var rowsHtml = '';
  SCHEDULES.forEach(function(s){
    rowsHtml += '<tr>'
      + '<td class="ps-3">' + new Date(s.dueDate).toLocaleDateString('ko-KR') + '</td>'
      + '<td>' + actorBadge(s.actor) + '</td>'
      + '<td>' + s.task + '</td>'
      + '<td><span class="text-muted small">' + s.src + '</span></td>'
      + '<td>' + getBadge(s.dueDate) + '</td>'
      + '</tr>';
  });
  document.getElementById('dueListBody').innerHTML = rowsHtml;
})();
</script>
</body>
</html>
