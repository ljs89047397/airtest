<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>감사로그 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-actn thead th { background: #0F2C72; color: white; font-size: 0.8rem; font-weight: 500; border: none; white-space: nowrap; }
.table-actn tbody tr:hover { background: #f8f9ff; }
.table-actn td { vertical-align: middle; font-size: 0.82rem; }
.filter-bar { background: white; border-bottom: 1px solid #e5e7eb; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-shield-lock me-1"></i>감사로그 조회</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">감사로그</li>
          </ol>
        </nav>
      </div>
      <!-- 엑셀 다운로드 자리 -->
      <button id="btnExcel" class="btn btn-sm btn-outline-success" disabled title="엑셀 다운로드 (추후 연동)">
        <i class="bi bi-file-earmark-excel me-1"></i>엑셀 다운로드
      </button>
    </div>
  </div>

  <!-- 필터 바 -->
  <div class="filter-bar px-4 py-3">
    <div class="row g-2 align-items-end">
      <div class="col-auto">
        <label class="form-label small fw-semibold mb-1">기간 시작</label>
        <input type="date" id="filterDtFrom" class="form-control form-control-sm" style="width:140px;">
      </div>
      <div class="col-auto">
        <label class="form-label small fw-semibold mb-1">기간 종료</label>
        <input type="date" id="filterDtTo" class="form-control form-control-sm" style="width:140px;">
      </div>
      <div class="col-auto">
        <label class="form-label small fw-semibold mb-1">사용자 ID</label>
        <input type="text" id="filterUserId" class="form-control form-control-sm" placeholder="사용자 ID" style="width:130px;" maxlength="50">
      </div>
      <div class="col-auto">
        <label class="form-label small fw-semibold mb-1">액션유형</label>
        <select id="filterActnSeCd" class="form-select form-select-sm" style="width:140px;">
          <option value="">전체</option>
          <option value="SUBMIT">SUBMIT</option>
          <option value="APPROVE">APPROVE</option>
          <option value="REJECT">REJECT</option>
          <option value="EXTRACT">EXTRACT</option>
          <option value="CCR_EXTR">CCR_EXTR</option>
          <option value="SURRENDER">SURRENDER</option>
        </select>
      </div>
      <div class="col-auto">
        <label class="form-label small fw-semibold mb-1">도메인(테이블)</label>
        <input type="text" id="filterTargetTbl" class="form-control form-control-sm" placeholder="예: er.tn_er" style="width:140px;" maxlength="60">
      </div>
      <div class="col-auto">
        <label class="form-label small fw-semibold mb-1">처리결과</label>
        <select id="filterRsltCd" class="form-select form-select-sm" style="width:120px;">
          <option value="">전체</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="FAIL">FAIL</option>
          <option value="FORBIDDEN">FORBIDDEN</option>
        </select>
      </div>
      <div class="col-auto">
        <button id="btnSearch" class="btn btn-sm" style="background:#0F2C72;color:white;">
          <i class="bi bi-search me-1"></i>조회
        </button>
        <button id="btnReset" class="btn btn-sm btn-outline-secondary ms-1">초기화</button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;">조회 결과</h6>
        <span class="text-muted small">총 <strong id="totalCount">0</strong>건</span>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0 table-actn">
            <thead>
              <tr>
                <th class="ps-3" style="width:50px;">#</th>
                <th style="min-width:160px;">로그일시</th>
                <th style="width:120px;">사용자 ID</th>
                <th style="width:100px;">액션유형</th>
                <th style="min-width:140px;">도메인(테이블)</th>
                <th style="width:110px;">대상 PK</th>
                <th style="width:130px;">클라이언트 IP</th>
                <th style="width:90px;">처리결과</th>
                <th>비고</th>
              </tr>
            </thead>
            <tbody id="actnTableBody">
              <tr><td colspan="9" class="text-center py-4 text-muted small">
                조회 버튼을 눌러 감사로그를 불러오세요.
              </td></tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- 페이지네이션 -->
      <div class="card-footer bg-white border-top py-2 d-flex align-items-center justify-content-between">
        <div class="small text-muted">
          페이지 <strong id="curPage">-</strong> / <strong id="totalPages">-</strong>
        </div>
        <nav>
          <ul class="pagination pagination-sm mb-0" id="pagination"></ul>
        </nav>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
var PAGE_SIZE = 20;
var currentPage = 0;
var totalPages  = 0;

var ACTN_COLOR = {
  'SUBMIT':    'bg-primary',
  'APPROVE':   'bg-success',
  'REJECT':    'bg-danger',
  'EXTRACT':   'bg-info text-dark',
  'CCR_EXTR':  'bg-warning text-dark',
  'SURRENDER': 'bg-secondary'
};
var RSLT_COLOR = {
  'SUCCESS':   'bg-success',
  'FAIL':      'bg-danger',
  'FORBIDDEN': 'bg-warning text-dark'
};

function renderActnBadge(cd) {
  if (!cd) return '<span class="badge bg-secondary">-</span>';
  var cls = ACTN_COLOR[cd] || 'bg-secondary';
  return '<span class="badge ' + cls + '">' + cd + '</span>';
}

function renderRsltBadge(cd) {
  if (!cd) return '<span class="badge bg-light text-muted border">-</span>';
  var cls = RSLT_COLOR[cd] || 'bg-secondary';
  return '<span class="badge ' + cls + '">' + cd + '</span>';
}

function buildParams(page) {
  var params = { page: page, size: PAGE_SIZE };
  var dtFrom = $('#filterDtFrom').val();
  var dtTo   = $('#filterDtTo').val();
  var userId = $.trim($('#filterUserId').val());
  var actn   = $('#filterActnSeCd').val();
  var tbl    = $.trim($('#filterTargetTbl').val());
  var rslt   = $('#filterRsltCd').val();
  if (dtFrom)  params.actnDtFrom = dtFrom + 'T00:00:00';
  if (dtTo)    params.actnDtTo   = dtTo   + 'T23:59:59';
  if (userId)  params.userId      = userId;
  if (actn)    params.actnSeCd    = actn;
  if (tbl)     params.targetTbl   = tbl;
  if (rslt)    params.rsltCd      = rslt;
  return params;
}

function renderTable(list, page, total) {
  currentPage = page;
  totalPages  = Math.ceil(total / PAGE_SIZE) || 1;
  $('#totalCount').text(total);
  $('#curPage').text(page + 1);
  $('#totalPages').text(totalPages);
  renderPagination(page, totalPages);

  if (!list || !list.length) {
    $('#actnTableBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small">조회된 감사로그가 없습니다.</td></tr>');
    return;
  }
  var html = '';
  list.forEach(function(row, idx) {
    html += '<tr>'
      + '<td class="ps-3 text-muted">' + (page * PAGE_SIZE + idx + 1) + '</td>'
      + '<td class="small">' + (row.actnDt ? row.actnDt.replace('T', ' ').substring(0,19) : '-') + '</td>'
      + '<td class="fw-semibold">' + $('<span>').text(row.userId || '-').html() + '</td>'
      + '<td>' + renderActnBadge(row.actnSeCd) + '</td>'
      + '<td class="text-muted small">' + $('<span>').text(row.targetTbl || '-').html() + '</td>'
      + '<td class="text-muted small">' + $('<span>').text(row.targetPk  || '-').html() + '</td>'
      + '<td class="text-muted small">' + $('<span>').text(row.ipAddr    || '-').html() + '</td>'
      + '<td>' + renderRsltBadge(row.rsltCd) + '</td>'
      + '<td class="text-muted small">' + $('<span>').text(row.rmrk || '').html() + '</td>'
      + '</tr>';
  });
  $('#actnTableBody').html(html);
}

function renderPagination(page, total) {
  if (total <= 1) { $('#pagination').html(''); return; }
  var html = '';
  var start = Math.max(0, page - 4);
  var end   = Math.min(total - 1, page + 4);

  html += '<li class="page-item' + (page === 0 ? ' disabled' : '') + '">'
    + '<a class="page-link" href="#" data-page="' + (page - 1) + '">&laquo;</a></li>';
  for (var i = start; i <= end; i++) {
    html += '<li class="page-item' + (i === page ? ' active' : '') + '">'
      + '<a class="page-link" href="#" data-page="' + i + '">' + (i + 1) + '</a></li>';
  }
  html += '<li class="page-item' + (page >= total - 1 ? ' disabled' : '') + '">'
    + '<a class="page-link" href="#" data-page="' + (page + 1) + '">&raquo;</a></li>';
  $('#pagination').html(html);
}

function loadActns(page) {
  page = page || 0;
  var params = buildParams(page);

  $('#actnTableBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small">'
    + '<div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...</td></tr>');

  $.get('/api/ptl/actn', params)
    .done(function(res) {
      var pg   = (res.data && res.data.rows || res.data.content !== undefined) ? res.data : null;
      var list = pg ? pg.content : (res.data || []);
      var total = pg ? pg.totalElements : list.length;
      renderTable(list, page, total);
      if (list.length > 0) $('#btnExcel').prop('disabled', false);
    })
    .fail(function(xhr) {
      $('#actnTableBody').html('<tr><td colspan="9" class="text-center py-4 text-danger small">'
        + '<i class="bi bi-exclamation-triangle me-1"></i>조회 실패 (HTTP ' + xhr.status + ')</td></tr>');
    });
}

function resetFilters() {
  $('#filterDtFrom').val('');
  $('#filterDtTo').val('');
  $('#filterUserId').val('');
  $('#filterActnSeCd').val('');
  $('#filterTargetTbl').val('');
  $('#filterRsltCd').val('');
}

// 기본 날짜 범위: 최근 7일
(function() {
  var now  = new Date();
  var from = new Date(); from.setDate(now.getDate() - 7);
  function fmt(d) { return d.toISOString().substring(0, 10); }
  $('#filterDtFrom').val(fmt(from));
  $('#filterDtTo').val(fmt(now));
})();

$(document).on('click', '#pagination a.page-link', function(e) {
  e.preventDefault();
  var p = parseInt($(this).data('page'));
  if (!isNaN(p) && p >= 0 && p < totalPages) loadActns(p);
});

$(function() {
  $('#btnSearch').on('click', function() { loadActns(0); });
  $('#btnReset').on('click', function() { resetFilters(); });
  $('#filterDtFrom, #filterDtTo, #filterUserId, #filterActnSeCd, #filterTargetTbl, #filterRsltCd')
    .on('keydown', function(e) { if (e.key === 'Enter') loadActns(0); });

  // 엑셀 다운로드 자리 (추후 API 연동)
  $('#btnExcel').on('click', function() {
    var params = buildParams(0);
    params.size = 10000;
    IcasAlert.info('엑셀 다운로드 기능은 추후 /api/ptl/actn/excel 엔드포인트 연동 예정입니다.');
  });

  loadActns(0);
});
</script>
</body>
</html>
