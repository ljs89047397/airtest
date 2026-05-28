<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>통합 워크플로우 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
.table-wf thead th { background:#0F2C72; color:white; font-size:0.8rem; font-weight:500; border:none; white-space:nowrap; }
.table-wf tbody tr:hover { background:#f8f9ff; }
.table-wf td { vertical-align: middle; }
.wf-badge { font-size:0.7rem; padding:3px 7px; border-radius:4px; font-weight:600; white-space:nowrap; }
.legend-dot { display:inline-block; width:10px; height:10px; border-radius:2px; margin-right:4px; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3">
    <div class="d-flex align-items-center justify-content-between">
      <div>
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#128260; 통합 워크플로우</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">통합 워크플로우</li>
          </ol>
        </nav>
      </div>
      <div class="d-flex align-items-center gap-2">
        <label class="small text-muted mb-0">보고연도</label>
        <select id="filterYr" class="form-select form-select-sm" style="width:100px;">
          <option value="2026" selected>2026</option>
          <option value="2025">2025</option>
          <option value="2024">2024</option>
        </select>
        <button id="btnRefresh" class="btn btn-sm" style="background:#0F2C72;color:white;">
          <i class="bi bi-arrow-clockwise me-1"></i>새로고침
        </button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 범례 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-2 px-4">
        <div class="d-flex flex-wrap align-items-center gap-3 small">
          <strong class="text-muted me-2">상태 범례:</strong>
          <span><span class="legend-dot bg-secondary"></span>DRAFT (작성중)</span>
          <span><span class="legend-dot bg-primary"></span>SBMTD (제출)</span>
          <span><span class="legend-dot" style="background:#ffc107;"></span>RVWNG (검토중)</span>
          <span><span class="legend-dot bg-danger"></span>RJCTD (반려)</span>
          <span><span class="legend-dot bg-success"></span>APRVD (승인)</span>
          <span><span class="legend-dot" style="background:#dee2e6;border:1px solid #adb5bd;"></span>미작성</span>
        </div>
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="row g-3 mb-3">
      <div class="col-auto">
        <div class="card border-0 shadow-sm text-center px-4 py-2">
          <div class="small text-muted">전체 운영사</div>
          <div class="fw-bold fs-5" style="color:#0F2C72;" id="sumTotal">-</div>
        </div>
      </div>
      <div class="col-auto">
        <div class="card border-0 shadow-sm text-center px-4 py-2">
          <div class="small text-muted">ER 승인</div>
          <div class="fw-bold fs-5 text-success" id="sumErAprvd">-</div>
        </div>
      </div>
      <div class="col-auto">
        <div class="card border-0 shadow-sm text-center px-4 py-2">
          <div class="small text-muted">ER 검토중</div>
          <div class="fw-bold fs-5 text-warning" id="sumErRvwng">-</div>
        </div>
      </div>
      <div class="col-auto">
        <div class="card border-0 shadow-sm text-center px-4 py-2">
          <div class="small text-muted">VR 완료</div>
          <div class="fw-bold fs-5 text-success" id="sumVrAprvd">-</div>
        </div>
      </div>
    </div>

    <!-- 워크플로우 매트릭스 테이블 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;">운영사 × 도메인 상태 매트릭스 (<span id="tableYr">2026</span>)</h6>
        <span class="text-muted small">총 <strong id="totalRows">0</strong>개 운영사</span>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0 table-wf">
            <thead>
              <tr>
                <th class="ps-3" style="min-width:130px;">운영사</th>
                <th style="width:70px;">ICAO</th>
                <th style="width:90px;">EMP</th>
                <th style="width:90px;">ER</th>
                <th style="width:90px;">CEF</th>
                <th style="width:90px;">EUCR</th>
                <th style="width:90px;">VR</th>
                <th style="width:90px;">SAF</th>
                <th>최근 업데이트</th>
              </tr>
            </thead>
            <tbody id="wfTableBody">
              <tr><td colspan="9" class="text-center py-4 text-muted small">
                <div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...
              </td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
const sampleWf = [
  {oprtrNm:'대한항공',   icaoCd:'KAL', empSttsCd:'APRVD', erSttsCd:'APRVD', cefSttsCd:'APRVD', eucrSttsCd:'SBMTD', vrSttsCd:'RVWNG', safSttsCd:'APRVD', lastUpdtDt:'2026-04-10'},
  {oprtrNm:'아시아나항공', icaoCd:'AAR', empSttsCd:'APRVD', erSttsCd:'SBMTD', cefSttsCd:'SBMTD', eucrSttsCd:'SBMTD', vrSttsCd:'SBMTD', safSttsCd:'RVWNG', lastUpdtDt:'2026-04-08'},
  {oprtrNm:'제주항공',   icaoCd:'JJA', empSttsCd:'APRVD', erSttsCd:'RVWNG', cefSttsCd:'APRVD', eucrSttsCd:'DRAFT', vrSttsCd:null,    safSttsCd:'SBMTD', lastUpdtDt:'2026-04-05'},
  {oprtrNm:'진에어',     icaoCd:'JNA', empSttsCd:'SBMTD', erSttsCd:'DRAFT',  cefSttsCd:'DRAFT',  eucrSttsCd:null,    vrSttsCd:null,    safSttsCd:'DRAFT',  lastUpdtDt:'2026-03-28'},
  {oprtrNm:'티웨이항공', icaoCd:'TWB', empSttsCd:'APRVD', erSttsCd:'RVWNG', cefSttsCd:'SBMTD', eucrSttsCd:'SBMTD', vrSttsCd:'RVWNG', safSttsCd:'SBMTD', lastUpdtDt:'2026-04-07'},
  {oprtrNm:'에어부산',   icaoCd:'ABL', empSttsCd:'APRVD', erSttsCd:'RJCTD',  cefSttsCd:'APRVD', eucrSttsCd:'SBMTD', vrSttsCd:null,    safSttsCd:'DRAFT',  lastUpdtDt:'2026-04-01'},
  {oprtrNm:'에어서울',   icaoCd:'ASV', empSttsCd:'APRVD', erSttsCd:'APRVD',  cefSttsCd:'APRVD', eucrSttsCd:'APRVD', vrSttsCd:'APRVD', safSttsCd:'SBMTD', lastUpdtDt:'2026-04-12'},
];

const STATUS_CFG = {
  'DRAFT':  {cls:'bg-secondary',               label:'DRAFT'},
  'SBMTD':  {cls:'bg-primary',                 label:'SBMTD'},
  'RVWNG':  {cls:'bg-warning text-dark',        label:'RVWNG'},
  'RJCTD':  {cls:'bg-danger',                   label:'RJCTD'},
  'APRVD':  {cls:'bg-success',                  label:'APRVD'},
};

function renderBadge(cd) {
  if (!cd) return '<span class="badge wf-badge bg-light text-muted border">&#8212;</span>';
  const cfg = STATUS_CFG[cd] || {cls:'bg-secondary', label:cd};
  return '<span class="badge wf-badge ' + cfg.cls + '">' + cfg.label + '</span>';
}

function renderTable(list) {
  $('#totalRows').text(list.length);
  $('#sumTotal').text(list.length);
  $('#sumErAprvd').text(list.filter(r=>r.erSttsCd==='APRVD').length);
  $('#sumErRvwng').text(list.filter(r=>r.erSttsCd==='RVWNG').length);
  $('#sumVrAprvd').text(list.filter(r=>r.vrSttsCd==='APRVD').length);

  if (!list.length) {
    $('#wfTableBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  let html = '';
  list.forEach(function(row) {
    html += '<tr>'
      + '<td class="ps-3 fw-semibold small">' + row.oprtrNm + '</td>'
      + '<td class="small text-muted">' + row.icaoCd + '</td>'
      + '<td>' + renderBadge(row.empSttsCd)  + '</td>'
      + '<td>' + renderBadge(row.erSttsCd)   + '</td>'
      + '<td>' + renderBadge(row.cefSttsCd)  + '</td>'
      + '<td>' + renderBadge(row.eucrSttsCd) + '</td>'
      + '<td>' + renderBadge(row.vrSttsCd)   + '</td>'
      + '<td>' + renderBadge(row.safSttsCd)  + '</td>'
      + '<td class="small text-muted">' + (row.lastUpdtDt || '-') + '</td>'
      + '</tr>';
  });
  $('#wfTableBody').html(html);
}

function loadData(yr) {
  $('#tableYr').text(yr);
  $('#wfTableBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small"><div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...</td></tr>');
  $.get('/api/ptl/workflow?rprtYr=' + yr)
    .done(function(res){ renderTable(res.data || res || sampleWf); })
    .fail(function()   { renderTable(sampleWf); });
}

$(function(){
  loadData($('#filterYr').val());
  $('#filterYr').on('change', function(){ loadData($(this).val()); });
  $('#btnRefresh').on('click', function(){ loadData($('#filterYr').val()); });
});
</script>
</body>
</html>
