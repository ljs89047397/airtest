<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CCR 추출 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-ccr thead th { background: #0F2C72; color: white; font-size: 0.8rem; font-weight: 500; border: none; white-space: nowrap; }
.table-ccr tbody tr:hover { background: #f8f9ff; }
.table-ccr td { vertical-align: middle; font-size: 0.85rem; }
.badge-inprg { background: #ffc107; color: #333; }
.badge-done  { background: #198754; color: white; }
.badge-fail  { background: #dc3545; color: white; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-cloud-download me-1"></i>CCR 추출</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">CCR 추출</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 추출 요청 폼 -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header bg-white border-bottom py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-send me-1"></i>추출 요청</h6>
      </div>
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-auto">
            <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
            <select id="rprtYr" class="form-select form-select-sm" style="width:110px;">
              <option value="2026" selected>2026</option>
              <option value="2025">2025</option>
              <option value="2024">2024</option>
              <option value="2023">2023</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold">추출범위 <span class="text-danger">*</span></label>
            <select id="extrScopeCd" class="form-select form-select-sm" style="width:130px;">
              <option value="ALL" selected>전체(ALL)</option>
              <option value="PARTIAL">부분(PARTIAL)</option>
            </select>
          </div>
          <div class="col-auto">
            <button id="btnExtract" class="btn btn-sm" style="background:#0F2C72;color:white;">
              <i class="bi bi-cloud-download me-1"></i>추출 요청
            </button>
          </div>
          <div class="col-auto">
            <div id="extractMsg" class="small"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 추출 이력 그리드 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-clock-history me-1"></i>추출 이력</h6>
        <div class="d-flex align-items-center gap-2">
          <span id="autoRefreshBadge" class="badge bg-warning text-dark d-none">
            <i class="bi bi-arrow-repeat me-1"></i>자동 새로고침 (10초)
          </span>
          <button id="btnReload" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-arrow-clockwise me-1"></i>새로고침
          </button>
        </div>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0 table-ccr">
            <thead>
              <tr>
                <th class="ps-3" style="width:110px;">CE 번호</th>
                <th style="width:80px;">보고연도</th>
                <th style="width:110px;">추출범위</th>
                <th style="width:100px;">상태</th>
                <th style="width:90px;">요청자</th>
                <th>생성일시</th>
                <th style="width:80px;">파일</th>
              </tr>
            </thead>
            <tbody id="ccrTableBody">
              <tr><td colspan="7" class="text-center py-4 text-muted small">
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
var autoRefreshTimer = null;

var SCOPE_LABEL = { 'ALL': '전체', 'PARTIAL': '부분' };

function renderStatus(cd) {
  if (cd === 'INPRG') return '<span class="badge badge-inprg">진행중</span>';
  if (cd === 'DONE')  return '<span class="badge badge-done">완료</span>';
  if (cd === 'FAIL')  return '<span class="badge bg-danger">실패</span>';
  return '<span class="badge bg-secondary">' + (cd || '-') + '</span>';
}

function renderTable(list) {
  if (!list || !list.length) {
    $('#ccrTableBody').html('<tr><td colspan="7" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    stopAutoRefresh();
    return;
  }
  var hasInprog = false;
  var html = '';
  list.forEach(function(row) {
    if (row.extrStCd === 'INPRG') hasInprog = true;
    var fileBtn = (row.extrStCd === 'DONE' && row.fileId)
      ? '<a href="/api/ptl/ccr/' + encodeURIComponent(row.extrId) + '/download" class="btn btn-xs btn-sm btn-outline-primary py-0 px-1 small"><i class="bi bi-download"></i></a>'
      : '<span class="text-muted small">-</span>';
    html += '<tr>'
      + '<td class="ps-3 fw-semibold">' + (row.extrId || '-') + '</td>'
      + '<td>' + (row.rprtYr || '-') + '</td>'
      + '<td>' + (SCOPE_LABEL[row.extrScopeCd] || row.extrScopeCd || '-') + '</td>'
      + '<td>' + renderStatus(row.extrStCd) + '</td>'
      + '<td class="text-muted">' + (row.extrUserId || '-') + '</td>'
      + '<td class="text-muted small">' + (row.extrDt ? row.extrDt.replace('T', ' ').substring(0,19) : '-') + '</td>'
      + '<td>' + fileBtn + '</td>'
      + '</tr>';
  });
  $('#ccrTableBody').html(html);
  if (hasInprog) {
    startAutoRefresh();
  } else {
    stopAutoRefresh();
  }
}

function loadHistory() {
  $.get('/api/ptl/ccr?page=0&size=20')
    .done(function(res) {
      var data = (res.data && res.data.rows || res.data.content) ? res.data.rows || res.data.content : (res.data || []);
      renderTable(data);
    })
    .fail(function(xhr) {
      $('#ccrTableBody').html('<tr><td colspan="7" class="text-center py-4 text-danger small"><i class="bi bi-exclamation-triangle me-1"></i>데이터 조회 실패 (HTTP ' + xhr.status + ')</td></tr>');
      stopAutoRefresh();
    });
}

function startAutoRefresh() {
  if (autoRefreshTimer) return;
  $('#autoRefreshBadge').removeClass('d-none');
  autoRefreshTimer = setInterval(function() { loadHistory(); }, 10000);
}

function stopAutoRefresh() {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
    autoRefreshTimer = null;
  }
  $('#autoRefreshBadge').addClass('d-none');
}

function requestExtraction() {
  var yr = $('#rprtYr').val();
  var scope = $('#extrScopeCd').val();
  if (!yr) { $('#extractMsg').html('<span class="text-danger">보고연도를 선택하세요.</span>'); return; }

  $('#btnExtract').prop('disabled', true);
  $('#extractMsg').html('<span class="text-muted"><span class="spinner-border spinner-border-sm me-1"></span>요청 중...</span>');

  $.ajax({
    url: '/api/ptl/ccr/extract',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({ rprtYr: yr, extrScopeCd: scope }),
    success: function(res) {
      var msg = (res && res.message) ? res.message : 'CCR 추출 요청이 접수되었습니다.';
      $('#extractMsg').html('<span class="text-success"><i class="bi bi-check-circle me-1"></i>' + $('<span>').text(msg).html() + '</span>');
      loadHistory();
    },
    error: function(xhr) {
      var errMsg = '추출 요청 실패';
      try { errMsg = JSON.parse(xhr.responseText).message || errMsg; } catch(e) {}
      $('#extractMsg').html('<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>' + $('<span>').text(errMsg).html() + '</span>');
    },
    complete: function() {
      $('#btnExtract').prop('disabled', false);
    }
  });
}

$(function() {
  loadHistory();
  $('#btnExtract').on('click', requestExtraction);
  $('#btnReload').on('click', loadHistory);
});
</script>
</body>
</html>
