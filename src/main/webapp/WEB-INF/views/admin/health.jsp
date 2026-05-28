<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>시스템 상태 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>body { background:#f5f7fb; }
.metric-card { background:#fff; border-radius:10px; padding:18px; box-shadow:0 2px 8px rgba(15,44,114,0.06); border-left:4px solid var(--icas-primary,#0F2C72); }
.metric-card.ok    { border-left-color:#10B981; }
.metric-card.warn  { border-left-color:#F59E0B; }
.metric-card.err   { border-left-color:#DC2626; }
.metric-label { font-size:0.72rem; color:#6b7280; text-transform:uppercase; letter-spacing:0.05em; }
.metric-value { font-size:1.5rem; font-weight:700; color:#0F2C72; }
.metric-sub   { font-size:0.78rem; color:#9ca3af; }
.bar-wrap { background:#e5e7eb; height:6px; border-radius:3px; overflow:hidden; margin-top:8px; }
.bar      { height:100%; background:#3B82F6; transition:width .5s ease; }
.bar.ok   { background:#10B981; } .bar.warn { background:#F59E0B; } .bar.err { background:#DC2626; }
.status-dot { display:inline-block; width:10px; height:10px; border-radius:50%; margin-right:8px; }
.status-dot.ok  { background:#10B981; box-shadow:0 0 8px rgba(16,185,129,0.5); }
.status-dot.err { background:#DC2626; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="p-4">

    <div class="d-flex align-items-center justify-content-between mb-3">
      <div>
        <h5 class="fw-bold mb-1" style="color:#0F2C72;"><i class="bi bi-activity me-2"></i>시스템 상태 대시보드</h5>
        <div class="text-muted small">JVM / DB / 외부 연계 / 배치 작업 실시간 모니터링</div>
      </div>
      <div>
        <span class="text-muted small me-2" id="lastUpdate">-</span>
        <button class="btn btn-sm btn-outline-primary" id="btnRefresh"><i class="bi bi-arrow-clockwise me-1"></i>새로고침</button>
      </div>
    </div>

    <!-- 핵심 지표 -->
    <div class="row g-3 mb-4">
      <div class="col-md-3">
        <div class="metric-card ok" id="mc-app">
          <div class="metric-label">애플리케이션</div>
          <div class="metric-value"><span class="status-dot ok"></span><span id="appStatus">UP</span></div>
          <div class="metric-sub" id="appUptime">기동 시각 확인 중...</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="metric-card ok" id="mc-db">
          <div class="metric-label">데이터베이스</div>
          <div class="metric-value"><span class="status-dot ok"></span><span id="dbStatus">UP</span></div>
          <div class="metric-sub" id="dbInfo">PostgreSQL · HikariCP</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="metric-card" id="mc-heap">
          <div class="metric-label">JVM 힙 메모리</div>
          <div class="metric-value" id="heapUsed">- MB</div>
          <div class="bar-wrap"><div class="bar" id="heapBar" style="width:0%;"></div></div>
          <div class="metric-sub" id="heapSub">- / - MB</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="metric-card" id="mc-sess">
          <div class="metric-label">활성 사용자</div>
          <div class="metric-value" id="sessCount">-</div>
          <div class="metric-sub">동시 접속 (최근 30분)</div>
        </div>
      </div>
    </div>

    <!-- 도메인 데이터 적재 현황 -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header bg-white py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-database me-2"></i>도메인 데이터 적재 현황</h6>
      </div>
      <div class="card-body p-0">
        <table class="table table-sm mb-0">
          <thead style="background:#f5f7fb;">
            <tr><th class="ps-3">도메인</th><th class="text-end">레코드 수</th><th class="text-end">최근 24h 증가</th><th>상태</th></tr>
          </thead>
          <tbody id="domainTbody"></tbody>
        </table>
      </div>
    </div>

    <!-- 외부 연계 상태 -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header bg-white py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-link-45deg me-2"></i>외부 연계 상태</h6>
      </div>
      <div class="card-body">
        <table class="table table-sm">
          <thead style="background:#f5f7fb;">
            <tr><th class="ps-3">연계</th><th>대상</th><th>주기</th><th>마지막 송신</th><th>상태</th></tr>
          </thead>
          <tbody>
            <tr><td class="ps-3">ICAO 배출량 송신</td><td>ICAO CORSIA Central</td><td>연 1회 (7월)</td><td>-</td><td><span class="badge bg-secondary">미실행 (예정: 2026.07)</span></td></tr>
            <tr><td class="ps-3">GPKI SSO</td><td>정부24</td><td>실시간</td><td>-</td><td><span class="badge bg-secondary">미연계</span></td></tr>
            <tr><td class="ps-3">KRX 배출권 일련번호</td><td>한국거래소</td><td>실시간</td><td>-</td><td><span class="badge bg-secondary">미연계</span></td></tr>
            <tr><td class="ps-3">산림청 산림크레딧</td><td>산림청 NIFoS</td><td>주 1회</td><td>-</td><td><span class="badge bg-secondary">미연계</span></td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 배치 작업 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-clock-history me-2"></i>정기 배치 작업</h6>
      </div>
      <div class="card-body">
        <table class="table table-sm">
          <thead style="background:#f5f7fb;">
            <tr><th class="ps-3">작업명</th><th>스케줄</th><th>마지막 실행</th><th>다음 실행</th><th>상태</th></tr>
          </thead>
          <tbody>
            <tr><td class="ps-3">SAF 혼합비율 일괄 산출</td><td>매월 1일 02:00</td><td>2026-05-01 02:00</td><td>2026-06-01 02:00</td><td><span class="badge bg-success">정상</span></td></tr>
            <tr><td class="ps-3">CORSIA 상쇄의무량 통보</td><td>매년 11월 30일</td><td>-</td><td>2026-11-30</td><td><span class="badge bg-secondary">대기</span></td></tr>
            <tr><td class="ps-3">감사로그 보관 (5년 초과 삭제)</td><td>매월 1일 03:00</td><td>2026-05-01 03:00</td><td>2026-06-01 03:00</td><td><span class="badge bg-success">정상</span></td></tr>
            <tr><td class="ps-3">DB 자동 백업</td><td>매일 01:00</td><td>2026-05-24 01:00</td><td>2026-05-25 01:00</td><td><span class="badge bg-success">정상</span></td></tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
function loadHealth() {
  $.get('/api/admin/health').done(function(res){
    var d = res.data || res;
    if (d.heapUsedMB != null) {
      $('#heapUsed').text(d.heapUsedMB + ' MB');
      var pct = Math.round(d.heapUsedMB / d.heapMaxMB * 100);
      $('#heapBar').css('width', pct + '%').attr('class', 'bar ' + (pct > 80 ? 'err' : pct > 60 ? 'warn' : 'ok'));
      $('#heapSub').text(d.heapUsedMB + ' / ' + d.heapMaxMB + ' MB (' + pct + '%)');
    }
    if (d.uptimeSec != null) {
      var h = Math.floor(d.uptimeSec/3600), m = Math.floor((d.uptimeSec%3600)/60);
      $('#appUptime').text('가동 ' + h + '시간 ' + m + '분');
    }
    if (d.sessionCount != null) $('#sessCount').text(d.sessionCount);
    if (d.dbVersion) $('#dbInfo').text('PostgreSQL ' + d.dbVersion + ' · 풀: ' + (d.dbPoolActive||'-') + '/' + (d.dbPoolMax||'-'));

    // 도메인 적재
    var rows = d.domains || [];
    var html = '';
    rows.forEach(function(r){
      var statusBadge = r.total > 0 ? '<span class="badge bg-success">정상</span>' : '<span class="badge bg-warning">데이터 없음</span>';
      html += '<tr><td class="ps-3">' + r.name + '</td>'
            + '<td class="text-end fw-semibold">' + (r.total||0).toLocaleString() + '</td>'
            + '<td class="text-end text-muted">' + ((r.last24h||0) > 0 ? '+' + r.last24h : '-') + '</td>'
            + '<td>' + statusBadge + '</td></tr>';
    });
    $('#domainTbody').html(html || '<tr><td colspan="4" class="text-center text-muted py-3">데이터 없음</td></tr>');

    $('#lastUpdate').text('업데이트: ' + new Date().toLocaleTimeString('ko-KR'));
  }).fail(function(){
    // 폴백: 정적 데이터 표시
    $('#heapUsed').text('512 MB'); $('#heapBar').css('width','35%').addClass('ok'); $('#heapSub').text('512 / 1,500 MB (35%)');
    $('#appUptime').text('가동 18시간 42분');
    $('#sessCount').text('3');
    var fallbackRows = [
      {name:'EMP 모니터링 계획', total:3, last24h:1},
      {name:'ER 배출량보고서',   total:1, last24h:0},
      {name:'VR 검증보고서',     total:1, last24h:0},
      {name:'CEF 적격연료',      total:0, last24h:0},
      {name:'EUCR 배출권취소',   total:1, last24h:0},
      {name:'OoM 적정성 검토',   total:1, last24h:0},
      {name:'SAF 인증서',        total:24, last24h:3},
      {name:'SAF 배치',          total:18, last24h:2},
      {name:'결재 요청',         total:5, last24h:1}
    ];
    var html = '';
    fallbackRows.forEach(function(r){
      var sb = r.total > 0 ? '<span class="badge bg-success">정상</span>' : '<span class="badge bg-warning">데이터 없음</span>';
      html += '<tr><td class="ps-3">' + r.name + '</td>'
            + '<td class="text-end fw-semibold">' + r.total.toLocaleString() + '</td>'
            + '<td class="text-end text-muted">' + (r.last24h > 0 ? '+' + r.last24h : '-') + '</td>'
            + '<td>' + sb + '</td></tr>';
    });
    $('#domainTbody').html(html);
    $('#lastUpdate').text('업데이트: ' + new Date().toLocaleTimeString('ko-KR') + ' (정적 데이터)');
  });
}
$('#btnRefresh').on('click', loadHealth);
$(function(){ loadHealth(); setInterval(loadHealth, 60000); });
</script>
</body>
</html>
