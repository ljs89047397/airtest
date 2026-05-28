<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAF 인증서 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; cursor: pointer; }
.badge-pos  { background: #1565c0; font-size: 0.72rem; }
.badge-poc  { background: #6a1b9a; font-size: 0.72rem; }
.badge-srnd { background: #c62828; font-size: 0.72rem; }
.badge-ok   { background: #2e7d32; font-size: 0.72rem; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">SAF 인증서 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/saf/dashboard" class="text-decoration-none">SAF</a></li>
            <li class="breadcrumb-item active">인증서 목록</li>
          </ol>
        </nav>
      </div>
      <button type="button" class="btn btn-primary btn-sm" id="openRegModalBtn">
        <i class="bi bi-plus-lg"></i> 인증서 등록
      </button>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 검색 필터 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-3">
        <form id="searchForm" class="row g-2 align-items-end">
          <div class="col-md-2">
            <label class="form-label small mb-1">SC 번호</label>
            <input type="text" id="certNo" name="certNo" class="form-control form-control-sm" placeholder="인증서 번호">
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">인증서 유형</label>
            <select id="certTypeCd" name="certTypeCd" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="PoS">PoS</option>
              <option value="PoC">PoC</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">회수 여부</label>
            <select id="srndYn" name="srndYn" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="N">미회수</option>
              <option value="Y">회수됨</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">인증 체계</label>
            <select id="certSchmCd" name="certSchmCd" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="ISCC">ISCC</option>
              <option value="RSB">RSB</option>
              <option value="CORSIA">CORSIA</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">보고연도</label>
            <select id="rprtYr" name="rprtYr" class="form-select form-select-sm">
              <option value="2026" selected>2026</option>
              <option value="2025">2025</option>
              <option value="2024">2024</option>
            </select>
          </div>
          <div class="col-md-2">
            <button type="submit" class="btn btn-primary btn-sm w-100">
              <i class="bi bi-search"></i> 조회
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 그리드 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-2 d-flex align-items-center justify-content-between">
        <span class="small text-muted">총 <strong id="totalCount">0</strong>건</span>
        <button type="button" class="btn btn-outline-success btn-sm" id="excelExportBtn">
          <i class="bi bi-file-earmark-excel"></i> 엑셀 다운로드
        </button>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table id="certGrid" aria-label="SAF 인증서 목록" class="table table-hover table-sm mb-0 table-icas">
            <thead>
              <tr>
                <th class="ps-3" style="width:5%;">No</th>
                <th style="width:18%;">SC 번호 (인증서 번호)</th>
                <th style="width:10%;">유형</th>
                <th style="width:10%;">인증 체계</th>
                <th style="width:20%;">배치 ID</th>
                <th style="width:12%;">회수 여부</th>
                <th style="width:13%;">등록일시</th>
                <th style="width:12%;">관리</th>
              </tr>
            </thead>
            <tbody id="certTableBody">
              <tr><td colspan="8" class="text-center py-4 text-muted small">조회 버튼을 눌러 데이터를 로드하세요.</td></tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- 페이징 -->
      <div class="card-footer bg-white border-top py-2 d-flex align-items-center justify-content-between">
        <div class="small text-muted" id="pageInfo"></div>
        <nav><ul class="pagination pagination-sm mb-0" id="pagination"></ul></nav>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     신규 인증서 등록 모달
====================================================== -->
<div class="modal fade" id="certRegModal" tabindex="-1" aria-labelledby="certRegModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="certRegModalLabel">
          <i class="bi bi-plus-circle me-1"></i> SAF 인증서 신규 등록
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-info py-2 small mb-3">
          <i class="bi bi-info-circle-fill me-1"></i>
          SC 번호(certId)는 저장 시 자동 채번됩니다. 입력한 인증서 번호(certNo)는 외부 발급 번호입니다.
        </div>
        <form id="certRegForm" novalidate>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">인증서 유형 <span class="text-danger">*</span></label>
              <select name="certTypeCd" id="reg_certTypeCd" class="form-select form-select-sm" required>
                <option value="">선택</option>
                <option value="PoS">PoS (Proof of Sustainability)</option>
                <option value="PoC">PoC (Proof of Care)</option>
              </select>
              <div class="invalid-feedback">인증서 유형을 선택하세요.</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">인증 체계 <span class="text-danger">*</span></label>
              <select name="certSchmCd" id="reg_certSchmCd" class="form-select form-select-sm" required>
                <option value="">선택</option>
                <option value="ISCC">ISCC</option>
                <option value="RSB">RSB</option>
                <option value="ISCC_PLUS">ISCC+</option>
                <option value="CORSIA">CORSIA</option>
                <option value="RSPO">RSPO</option>
              </select>
              <div class="invalid-feedback">인증 체계를 선택하세요.</div>
            </div>
            <div class="col-md-8">
              <label class="form-label small fw-semibold">인증서 번호 (외부 발급 번호) <span class="text-danger">*</span></label>
              <input type="text" name="certNo" id="reg_certNo" class="form-control form-control-sm"
                     placeholder="예) ISCC-EU-2024-00123" required maxlength="100">
              <div class="invalid-feedback">인증서 번호를 입력하세요.</div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">운영사 ID <span class="text-danger">*</span></label>
              <input type="text" name="oprtrId" id="reg_oprtrId" class="form-control form-control-sm"
                     placeholder="예) KAL" required maxlength="20">
              <div class="invalid-feedback">운영사 ID를 입력하세요.</div>
            </div>
            <div class="col-md-8">
              <label class="form-label small fw-semibold">연결 배치 ID (SAF Batch ID)</label>
              <input type="text" name="batchId" id="reg_batchId" class="form-control form-control-sm"
                     placeholder="예) BATCH-2024-KR-001 (선택)" maxlength="100">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">적용 시작일</label>
              <input type="date" name="useBgngDt" id="reg_useBgngDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">적용 종료일</label>
              <input type="date" name="useEndDt" id="reg_useEndDt" class="form-control form-control-sm">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="certRegSubmitBtn">
          <i class="bi bi-check-lg me-1"></i> 등록
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
var currentPage = 1;
var pageSize    = 20;

function certTypeBadge(t) {
  if (t === 'PoS') return '<span class="badge badge-pos">PoS</span>';
  if (t === 'PoC') return '<span class="badge badge-poc">PoC</span>';
  return '<span class="badge bg-secondary">' + $('<div>').text(t).html() + '</span>';
}

function srndBadge(yn) {
  return yn === 'Y'
    ? '<span class="badge badge-srnd"><i class="bi bi-x-circle-fill"></i> 회수됨</span>'
    : '<span class="badge badge-ok"><i class="bi bi-check-circle-fill"></i> 유효</span>';
}

function renderTable(data, totalCount, page) {
  $('#totalCount').text(totalCount);
  var offset = (page - 1) * pageSize;
  var html = '';
  if (!data || data.length === 0) {
    html = '<tr><td colspan="8" class="text-center py-4 text-muted small">조건에 해당하는 데이터가 없습니다.</td></tr>';
  } else {
    $.each(data, function(i, row) {
      var certNoSafe = $('<div>').text(row.certNo || '-').html();
      var batchIdSafe = $('<div>').text(row.batchId || '-').html();
      html += '<tr onclick="location.href=\'/saf/cert/' + encodeURIComponent(row.certId) + '\'">'
        + '<td class="ps-3 small text-muted">' + (offset + i + 1) + '</td>'
        + '<td class="small fw-semibold">' + certNoSafe + '</td>'
        + '<td class="small">' + certTypeBadge(row.certTypeCd) + '</td>'
        + '<td class="small">' + $('<div>').text(row.certSchmCd || '-').html() + '</td>'
        + '<td class="small text-monospace">' + batchIdSafe + '</td>'
        + '<td class="small">' + srndBadge(row.srndYn) + '</td>'
        + '<td class="small text-muted">' + (row.frstRegDt ? row.frstRegDt.substring(0,16) : '-') + '</td>'
        + '<td class="small"><a href="/saf/cert/' + encodeURIComponent(row.certId) + '" class="btn btn-link btn-sm p-0" onclick="event.stopPropagation()">상세</a></td>'
        + '</tr>';
    });
  }
  $('#certTableBody').html(html);
  renderPaging(totalCount, page);
}

function renderPaging(total, page) {
  var totalPages = Math.ceil(total / pageSize);
  var start = Math.max(1, page - 4);
  var end   = Math.min(totalPages, page + 4);
  var html  = '';
  if (page > 1) html += '<li class="page-item"><a class="page-link" href="#" data-page="' + (page-1) + '">&laquo;</a></li>';
  for (var p = start; p <= end; p++) {
    html += '<li class="page-item' + (p === page ? ' active' : '') + '"><a class="page-link" href="#" data-page="' + p + '">' + p + '</a></li>';
  }
  if (page < totalPages) html += '<li class="page-item"><a class="page-link" href="#" data-page="' + (page+1) + '">&raquo;</a></li>';
  $('#pagination').html(html);
  $('#pageInfo').text('페이지 ' + page + ' / ' + (totalPages || 1));
}

function loadData(page) {
  currentPage = page;
  var params = $('#searchForm').serialize() + '&page=' + page + '&size=' + pageSize;
  $.get('/api/saf/cert?' + params)
    .done(function(res) {
      var d = res.data || res;
      var rows = d.rows || d.content || (Array.isArray(d) ? d : []);
      var total = d.total || d.totalCount || d.totalElements || rows.length;
      renderTable(rows, total, page);
    })
    .fail(function(xhr) {
      $('#certTableBody').html('<tr><td colspan="8" class="text-center py-4 text-danger small">데이터 조회 중 오류가 발생했습니다. (HTTP ' + xhr.status + ')</td></tr>');
    });
}

$(function() {
  $('#searchForm').on('submit', function(e) { e.preventDefault(); loadData(1); });
  $(document).on('click', '#pagination .page-link', function(e) {
    e.preventDefault();
    loadData(parseInt($(this).data('page')));
  });
  $('#excelExportBtn').on('click', function() {
    location.href = '/api/saf/cert/excel?' + $('#searchForm').serialize();
  });

  /* 등록 모달 열기 */
  $('#openRegModalBtn').on('click', function() {
    $('#certRegForm')[0].reset();
    $('#certRegForm').removeClass('was-validated');
    new bootstrap.Modal('#certRegModal').show();
  });

  /* 등록 제출 */
  $('#certRegSubmitBtn').on('click', function() {
    var $form = $('#certRegForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;

    var payload = {
      certTypeCd  : $('#reg_certTypeCd').val(),
      certSchmCd  : $('#reg_certSchmCd').val(),
      certNo      : $('#reg_certNo').val().trim(),
      oprtrId     : $('#reg_oprtrId').val().trim(),
      batchId     : $('#reg_batchId').val().trim() || null,
      useBgngDt   : $('#reg_useBgngDt').val() || null,
      useEndDt    : $('#reg_useEndDt').val() || null
    };

    var $btn = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>등록 중...');

    $.ajax({
      url         : '/api/saf/cert',
      type        : 'POST',
      contentType : 'application/json',
      data        : JSON.stringify(payload)
    })
    .done(function(res) {
      bootstrap.Modal.getInstance('#certRegModal').hide();
      IcasAlert.success(res.message || 'SAF 인증서가 등록되었습니다.');
      loadData(1);
    })
    .fail(function(xhr) {
      var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
      IcasAlert.error('등록 실패: ' + msg);
    })
    .always(function() {
      $btn.prop('disabled', false).html('<i class="bi bi-check-lg me-1"></i> 등록');
    });
  });

  loadData(1);
});
</script>
</body>
</html>
