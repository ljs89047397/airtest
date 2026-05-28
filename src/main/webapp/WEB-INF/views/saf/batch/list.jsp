<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAF 배치 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; cursor: pointer; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">SAF 배치 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/saf/dashboard" class="text-decoration-none">SAF</a></li>
            <li class="breadcrumb-item active">배치 목록</li>
          </ol>
        </nav>
      </div>
      <button type="button" class="btn btn-primary btn-sm" id="openBatchRegBtn">
        <i class="bi bi-plus-lg"></i> 신규 배치 등록
      </button>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 검색 필터 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-3">
        <form id="searchForm" class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">배치 ID (자연키)</label>
            <input type="text" name="batchId" class="form-control form-control-sm" placeholder="생산자 PoS Batch ID">
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">PoC 번호</label>
            <input type="text" name="pocIdNo" class="form-control form-control-sm" placeholder="PoC ID">
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">수신 공항</label>
            <input type="text" name="safRecvArprtNm" class="form-control form-control-sm" placeholder="공항명">
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">밀도 구분</label>
            <select name="dnstySecCd" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="DEFAULT">DEFAULT</option>
              <option value="ACTUAL">ACTUAL</option>
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
          <table id="batchGrid" aria-label="SAF 배치 목록" class="table table-hover table-sm mb-0 table-icas">
            <thead>
              <tr>
                <th class="ps-3" style="width:4%;">No</th>
                <th style="width:22%;">배치 ID (자연키)</th>
                <th style="width:15%;">PoC 번호</th>
                <th style="width:12%;">PoC 발행일</th>
                <th style="width:15%;">수신 운영사</th>
                <th style="width:12%;">수신 공항</th>
                <th style="width:10%;">배치 수량 (L)</th>
                <th style="width:10%;">등록일</th>
              </tr>
            </thead>
            <tbody id="batchTableBody">
              <tr><td colspan="8" class="text-center py-4 text-muted small">조회 버튼을 눌러 데이터를 로드하세요.</td></tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card-footer bg-white border-top py-2 d-flex align-items-center justify-content-between">
        <div class="small text-muted" id="pageInfo"></div>
        <nav><ul class="pagination pagination-sm mb-0" id="pagination"></ul></nav>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     신규 배치 등록 모달
====================================================== -->
<div class="modal fade" id="batchRegModal" tabindex="-1" aria-labelledby="batchRegModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="batchRegModalLabel">
          <i class="bi bi-plus-circle me-1"></i> SAF 배치 신규 등록
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-info py-2 small mb-3">
          <i class="bi bi-info-circle-fill me-1"></i>
          배치 ID는 생산자 PoS에서 받은 자연키를 그대로 입력합니다 (외부 고유값).
        </div>
        <form id="batchRegForm" novalidate>
          <div class="row g-3">
            <div class="col-md-8">
              <label class="form-label small fw-semibold">배치 ID (자연키) <span class="text-danger">*</span></label>
              <input type="text" id="breg_batchId" class="form-control form-control-sm"
                     placeholder="예) BATCH-2024-EU-KR-001" required maxlength="100">
              <div class="invalid-feedback">배치 ID를 입력하세요.</div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">운영사 ID <span class="text-danger">*</span></label>
              <input type="text" id="breg_oprtrId" class="form-control form-control-sm"
                     placeholder="예) KAL" required maxlength="20">
              <div class="invalid-feedback">운영사 ID를 입력하세요.</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">PoC 번호</label>
              <input type="text" id="breg_pocIdNo" class="form-control form-control-sm"
                     placeholder="예) POC-2024-001" maxlength="100">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">PoC 발행일</label>
              <input type="date" id="breg_pocIsueDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">밀도 구분</label>
              <select id="breg_dnstySecd" class="form-select form-select-sm">
                <option value="DEFAULT">DEFAULT</option>
                <option value="ACTUAL">ACTUAL</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">수신처 회사명</label>
              <input type="text" id="breg_dptrRecvCoNm" class="form-control form-control-sm"
                     placeholder="수신 운영사명" maxlength="200">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">수신 공항명</label>
              <input type="text" id="breg_safRecvArprtNm" class="form-control form-control-sm"
                     placeholder="예) 인천국제공항" maxlength="100">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">수신 국가코드</label>
              <input type="text" id="breg_safRecvCntryCd" class="form-control form-control-sm"
                     placeholder="예) KR" maxlength="2">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">배치 수량 (kg)</label>
              <input type="number" id="breg_batchQty" class="form-control form-control-sm"
                     placeholder="0.00" min="0" step="0.001">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">에너지 함량 (MJ)</label>
              <input type="number" id="breg_energyCn" class="form-control form-control-sm"
                     placeholder="0.00" min="0" step="0.001">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="batchRegSubmitBtn">
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

function escHtml(s) { return $('<div>').text(s == null ? '-' : String(s)).html(); }

function renderTable(data, totalCount, page) {
  $('#totalCount').text(totalCount);
  var offset = (page - 1) * pageSize;
  var html = '';
  if (!data || data.length === 0) {
    html = '<tr><td colspan="8" class="text-center py-4 text-muted small">조건에 해당하는 데이터가 없습니다.</td></tr>';
  } else {
    $.each(data, function(i, row) {
      html += '<tr onclick="location.href=\'/saf/batch/' + encodeURIComponent(row.batchId) + '\'">'
        + '<td class="ps-3 small text-muted">' + (offset + i + 1) + '</td>'
        + '<td class="small fw-semibold text-break">' + escHtml(row.batchId) + '</td>'
        + '<td class="small">' + escHtml(row.pocIdNo) + '</td>'
        + '<td class="small">' + escHtml(row.pocIsueDt) + '</td>'
        + '<td class="small">' + escHtml(row.dprtrRecvCoNm) + '</td>'
        + '<td class="small">' + escHtml(row.safRecvArprtNm) + '</td>'
        + '<td class="small text-end">' + (row.batchQty ? Number(row.batchQty).toLocaleString() : '-') + '</td>'
        + '<td class="small text-muted">' + escHtml(row.frstRegDt ? row.frstRegDt.substring(0,10) : null) + '</td>'
        + '</tr>';
    });
  }
  $('#batchTableBody').html(html);
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
  $.get('/api/saf/batch?' + params)
    .done(function(res) {
      var d = res.data || res;
      var rows = d.rows || d.content || (Array.isArray(d) ? d : []);
      var total = d.total || d.totalCount || d.totalElements || rows.length;
      renderTable(rows, total, page);
    })
    .fail(function(xhr) {
      $('#batchTableBody').html('<tr><td colspan="8" class="text-center py-4 text-danger small">데이터 조회 중 오류가 발생했습니다. (HTTP ' + xhr.status + ')</td></tr>');
    });
}

$(function() {
  $('#searchForm').on('submit', function(e) { e.preventDefault(); loadData(1); });
  $(document).on('click', '#pagination .page-link', function(e) {
    e.preventDefault();
    loadData(parseInt($(this).data('page')));
  });
  $('#excelExportBtn').on('click', function() {
    location.href = '/api/saf/batch/excel?' + $('#searchForm').serialize();
  });

  /* 배치 등록 모달 열기 */
  $('#openBatchRegBtn').on('click', function() {
    $('#batchRegForm')[0].reset();
    $('#batchRegForm').removeClass('was-validated');
    new bootstrap.Modal('#batchRegModal').show();
  });

  /* 배치 등록 제출 */
  $('#batchRegSubmitBtn').on('click', function() {
    var $form = $('#batchRegForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;

    var batchQtyVal  = $('#breg_batchQty').val();
    var energyCnVal  = $('#breg_energyCn').val();

    var payload = {
      batchId        : $('#breg_batchId').val().trim(),
      oprtrId        : $('#breg_oprtrId').val().trim(),
      pocIdNo        : $('#breg_pocIdNo').val().trim() || null,
      pocIsueDt      : $('#breg_pocIsueDt').val() || null,
      dnstySecd      : $('#breg_dnstySecd').val(),
      dptrRecvCoNm   : $('#breg_dptrRecvCoNm').val().trim() || null,
      safRecvArprtNm : $('#breg_safRecvArprtNm').val().trim() || null,
      safRecvCntryCd : $('#breg_safRecvCntryCd').val().trim() || null,
      batchQty       : batchQtyVal ? parseFloat(batchQtyVal) : null,
      energyCn       : energyCnVal ? parseFloat(energyCnVal) : null
    };

    var $btn = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>등록 중...');

    $.ajax({
      url         : '/api/saf/batch',
      type        : 'POST',
      contentType : 'application/json',
      data        : JSON.stringify(payload)
    })
    .done(function(res) {
      bootstrap.Modal.getInstance('#batchRegModal').hide();
      IcasAlert.success(res.message || 'SAF 배치가 등록되었습니다.');
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
