<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배출권취소(EUCR) 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
.table-icas thead th { background:#0F2C72; color:white; font-size:0.82rem; font-weight:500; border:none; }
.table-icas tbody tr:hover { background:#f8f9ff; }
.status-badge { font-size:0.72rem; padding:3px 8px; border-radius:4px; font-weight:600; }
.fulfilled-y { color:#198754; font-weight:700; }
.fulfilled-n { color:#dc3545; font-weight:700; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-file-earmark-check me-2"></i>배출권취소(EUCR) 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">배출권취소(EUCR)</li>
          </ol>
        </nav>
      </div>
      <div>
        <button id="btnNewEucr" class="btn btn-sm" style="background:#0F2C72;color:white;">
          <i class="bi bi-plus-lg me-1"></i>신규 등록
        </button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 검색 필터 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-3">
        <div class="row g-2 align-items-end">
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">보고연도</label>
            <select id="filterYr" class="form-select form-select-sm" style="width:100px;">
              <option value="" selected>전체</option>
              <option value="2026">2026</option>
              <option value="2025">2025</option>
              <option value="2024">2024</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">상태</label>
            <select id="filterStatus" class="form-select form-select-sm" style="width:130px;">
              <option value="">전체</option>
              <option value="DRAFT">작성중</option>
              <option value="SBMTD">제출</option>
              <option value="RVWNG">검토중</option>
              <option value="RCMDD">권고</option>
              <option value="RJCTD">반려</option>
              <option value="APRVD">승인</option>
              <option value="CNCLD">취소</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">충족여부</label>
            <select id="filterFulfilled" class="form-select form-select-sm" style="width:100px;">
              <option value="">전체</option>
              <option value="Y">충족</option>
              <option value="N">미충족</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">운영사</label>
            <input type="text" id="filterOprtr" class="form-control form-control-sm" placeholder="운영사명" style="width:160px;">
          </div>
          <div class="col-auto">
            <button id="btnSearch" class="btn btn-sm" style="background:#0F2C72;color:white;">
              <i class="bi bi-search me-1"></i>조회
            </button>
            <button id="btnReset" class="btn btn-sm btn-outline-secondary ms-1">초기화</button>
          </div>
          <div class="col-auto ms-auto">
            <span class="text-muted small">총 <strong id="totalCount">0</strong>건</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 목록 테이블 -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0 table-icas">
            <thead>
              <tr>
                <th class="ps-3" style="width:40px;">No</th>
                <th>운영사명</th>
                <th>보고연도</th>
                <th>버전</th>
                <th>상태</th>
                <th class="text-end pe-3">상쇄의무량</th>
                <th class="text-end pe-3">총취소량</th>
                <th class="text-center">충족여부</th>
                <th>제출일</th>
                <th>승인일</th>
                <th style="width:70px;">액션</th>
              </tr>
            </thead>
            <tbody id="eucrListBody">
              <tr>
                <td colspan="11" class="text-center py-4 text-muted small">
                  <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                  데이터 로딩 중...
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 페이징 -->
        <div class="d-flex justify-content-center py-3" id="paginationWrap"></div>
      </div>
    </div>
  </div>
</div>

<!-- 신규 등록 모달 -->
<div class="modal fade" id="modalNewEucr" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold"><i class="bi bi-plus-circle me-2"></i>EUCR 신규 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
          <input type="text" id="newRprtYr" class="form-control form-control-sm" placeholder="예: 2026" maxlength="4">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">상쇄 의무량 <span class="text-danger">*</span></label>
          <input type="number" id="newOfstReqQty" class="form-control form-control-sm" placeholder="tCO2e" min="0" step="0.0001">
        </div>
        <div id="newEucrError" class="alert alert-danger py-2 small d-none"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnSaveNewEucr" class="btn btn-sm" style="background:#0F2C72;color:white;">등록</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
const EUCR_STATUS_MAP = {
  'DRAFT': ['bg-secondary', '작성중'],
  'SBMTD': ['bg-primary',   '제출'],
  'RVWNG': ['bg-warning text-dark', '검토중'],
  'RCMDD': ['bg-info text-dark',    '권고'],
  'RJCTD': ['bg-danger',   '반려'],
  'APRVD': ['bg-success',  '승인'],
  'CNCLD': ['bg-dark',     '취소']
};

function esc(v) {
  if (v == null) return '';
  return String(v)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}

function renderBadge(cd) {
  if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
  const [cls, lbl] = EUCR_STATUS_MAP[cd] || ['bg-secondary', cd];
  return '<span class="badge status-badge ' + cls + '">' + lbl + '</span>';
}

function fmtNum(v) {
  if (v == null || v === '') return '-';
  return Number(v).toLocaleString('ko-KR', {minimumFractionDigits:0, maximumFractionDigits:4});
}

function fmtDt(v) {
  if (!v) return '-';
  return String(v).substring(0, 10);
}

function renderTable(list) {
  $('#totalCount').text(list.length);
  if (!list.length) {
    $('#eucrListBody').html('<tr><td colspan="11" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  let html = '';
  list.forEach(function(row, idx) {
    const fy = row.fulfilledYn === 'Y'
      ? '<span class="fulfilled-y">&#10003; 충족</span>'
      : '<span class="fulfilled-n">&#10007; 미충족</span>';
    html += '<tr>'
      + '<td class="ps-3 text-muted small">' + (idx + 1) + '</td>'
      + '<td class="fw-semibold small">' + esc(row.oprtrNm) + '</td>'
      + '<td class="small">' + esc(row.rprtYr) + '</td>'
      + '<td class="small text-center">v' + esc(row.eucrVer) + '</td>'
      + '<td>' + renderBadge(row.eucrStCd) + '</td>'
      + '<td class="small text-end pe-3">' + fmtNum(row.ofstReqQty) + '</td>'
      + '<td class="small text-end pe-3">' + fmtNum(row.ttlQty) + '</td>'
      + '<td class="small text-center">' + fy + '</td>'
      + '<td class="small text-muted">' + fmtDt(row.sbmtDt) + '</td>'
      + '<td class="small text-muted">' + fmtDt(row.aprvDt) + '</td>'
      + '<td><a href="/er/eucr/' + esc(row.eucrId) + '" class="btn btn-xs btn-outline-primary" style="font-size:0.72rem;padding:2px 8px;">상세</a></td>'
      + '</tr>';
  });
  $('#eucrListBody').html(html);
}

let allData = [];

function applyFilter(list) {
  const yr        = $('#filterYr').val();
  const status    = $('#filterStatus').val();
  const fulfilled = $('#filterFulfilled').val();
  const oprtr     = $('#filterOprtr').val().toLowerCase();
  return list.filter(function(row) {
    return (!yr        || String(row.rprtYr) === yr)
        && (!status    || row.eucrStCd === status)
        && (!fulfilled || row.fulfilledYn === fulfilled)
        && (!oprtr     || (row.oprtrNm || '').toLowerCase().includes(oprtr));
  });
}

function loadData(yr) {
  $('#eucrListBody').html('<tr><td colspan="11" class="text-center py-4 text-muted small"><div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...</td></tr>');
  $.get('/api/er/eucr', { rprtYr: yr, pageSize: 200 })
    .done(function(res) {
      allData = (res.data && res.data.rows || res.data.content) ? res.data.rows || res.data.content : (res.data || res || []);
      renderTable(applyFilter(allData));
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '데이터 조회에 실패하였습니다.';
      $('#eucrListBody').html('<tr><td colspan="11" class="text-center py-4"><span class="text-danger small"><i class="bi bi-exclamation-triangle me-1"></i>' + esc(msg) + '</span></td></tr>');
    });
}

$(function() {
  loadData($('#filterYr').val());

  $('#btnSearch').on('click', function() { renderTable(applyFilter(allData)); });
  $('#btnReset').on('click', function() {
    $('#filterStatus').val('');
    $('#filterFulfilled').val('');
    $('#filterOprtr').val('');
    renderTable(applyFilter(allData));
  });
  $('#filterYr').on('change', function() { loadData($(this).val()); });

  $('#btnNewEucr').on('click', function() {
    $('#newRprtYr').val($('#filterYr').val());
    $('#newOfstReqQty').val('');
    $('#newEucrError').addClass('d-none').text('');
    new bootstrap.Modal(document.getElementById('modalNewEucr')).show();
  });

  $('#btnSaveNewEucr').on('click', function() {
    const yr  = $.trim($('#newRprtYr').val());
    const qty = $.trim($('#newOfstReqQty').val());
    if (!yr || !/^\d{4}$/.test(yr)) {
      $('#newEucrError').removeClass('d-none').text('보고연도는 4자리 숫자로 입력하세요.');
      return;
    }
    if (!qty || isNaN(Number(qty)) || Number(qty) < 0) {
      $('#newEucrError').removeClass('d-none').text('상쇄 의무량을 올바르게 입력하세요.');
      return;
    }
    $(this).prop('disabled', true).text('처리중...');
    $.ajax({
      url: '/api/er/eucr',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ rprtYr: yr, ofstReqQty: qty })
    })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('modalNewEucr')).hide();
      const newId = res.data && res.data.eucrId ? res.data.eucrId : null;
      if (newId) {
        location.href = '/er/eucr/' + newId;
      } else {
        loadData($('#filterYr').val());
      }
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '등록에 실패하였습니다.';
      $('#newEucrError').removeClass('d-none').text(msg);
      $('#btnSaveNewEucr').prop('disabled', false).text('등록');
    });
  });
});
</script>
</body>
</html>
