<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>항공기 등록부 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header th { background: var(--icas-primary); color: #fff; font-weight: 500; font-size: 0.82rem; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <div class="d-flex align-items-center justify-content-between mb-3">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">항공기 등록부</h5>
        <small class="text-muted">운항사별 항공기 풀 관리</small>
      </div>
      <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="openCreateModal()">
        <i class="bi bi-airplane me-1"></i>항공기 등록
      </button>
    </div>

    <!-- 검색 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-2">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">운항사</label>
            <select id="searchOprtrId" class="form-select form-select-sm">
              <option value="">전체</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">기체등록번호</label>
            <input type="text" id="searchAcftRegNo" class="form-control form-control-sm" placeholder="HL-XXXX">
          </div>
          <div class="col-md-2">
            <button class="btn btn-sm btn-primary w-100" onclick="loadOprtr()">
              <i class="bi bi-search me-1"></i>검색
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">운항사ID</th>
                <th>운항사명</th>
                <th>ICAO 지정자</th>
                <th>기체등록번호</th>
                <th>기종</th>
                <th>좌석수</th>
                <th>최대중량(kg)</th>
                <th>등록일</th>
                <th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="oprtrTableBody">
              <tr><td colspan="9" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
</div>

<!-- 등록/수정 모달 -->
<div class="modal fade" id="oprtrModal" tabindex="-1" aria-labelledby="oprtrModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="oprtrModalLabel">항공기 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalOprtrId">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-semibold">운항사ID <span class="text-danger">*</span></label>
            <select id="fOprtrId" class="form-select form-select-sm"></select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기체등록번호 <span class="text-danger">*</span></label>
            <input type="text" id="fAcftRegNo" class="form-control form-control-sm" placeholder="HL-XXXX">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기종</label>
            <input type="text" id="fAcftTyp" class="form-control form-control-sm" placeholder="예: B737-800">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">좌석수</label>
            <input type="number" id="fSeatCnt" class="form-control form-control-sm">
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">최대이륙중량(kg)</label>
            <input type="number" id="fMtow" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">제조사</label>
            <input type="text" id="fMfgr" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">제조연도</label>
            <input type="number" id="fMfgYr" class="form-control form-control-sm" placeholder="예: 2015">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveOprtr()">
          <i class="bi bi-save me-1"></i>저장
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>
var oprtrModal = new bootstrap.Modal(document.getElementById('oprtrModal'));
var allOprtr = [];

$(function() {
  loadOgnzForSelect();
  loadOprtr();
});

function loadOgnzForSelect() {
  $.get('/api/com/ognz').done(function(res) {
    var opts = '<option value="">선택</option>';
    var sopts = '<option value="">전체</option>';
    (res.data || []).filter(function(o){ return o.ognzSeCd === 'AIRLINE'; }).forEach(function(o) {
      opts  += '<option value="' + escHtml(o.ognzId) + '">' + escHtml(o.ognzNm) + '</option>';
      sopts += '<option value="' + escHtml(o.ognzId) + '">' + escHtml(o.ognzNm) + '</option>';
    });
    $('#fOprtrId').html(opts);
    $('#searchOprtrId').html(sopts);
  });
}

function loadOprtr() {
  $.get('/api/com/oprtr').done(function(res) {
    allOprtr = res.data || [];
    var oprtrId = $('#searchOprtrId').val();
    var regNo = $('#searchAcftRegNo').val().toLowerCase();
    var filtered = allOprtr.filter(function(o) {
      return (!oprtrId || o.oprtrId === oprtrId)
          && (!regNo || (o.acftRegNo||'').toLowerCase().includes(regNo));
    });
    renderTable(filtered);
  }).fail(function(xhr) { showErr('목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function renderTable(rows) {
  if (!rows.length) {
    $('#oprtrTableBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  var html = '';
  rows.forEach(function(o) {
    html += '<tr>'
      + '<td class="ps-3 small text-muted">' + escHtml(o.oprtrId) + '</td>'
      + '<td class="fw-semibold small">' + escHtml(o.oprtrNm || '') + '</td>'
      + '<td class="small">' + escHtml(o.icaoDesig || '-') + '</td>'
      + '<td class="small">' + escHtml(o.acftRegNo || '-') + '</td>'
      + '<td class="small">' + escHtml(o.acftTyp || '-') + '</td>'
      + '<td class="small text-end">' + (o.seatCnt != null ? o.seatCnt : '-') + '</td>'
      + '<td class="small text-end">' + (o.mtow != null ? Number(o.mtow).toLocaleString() : '-') + '</td>'
      + '<td class="small text-muted">' + (o.frstRgtrDt || '').substring(0,10) + '</td>'
      + '<td class="text-center">'
      +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openEditModal(\'' + escHtml(o.oprtrId) + '\')"><i class="bi bi-pencil"></i></button>'
      +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteOprtr(\'' + escHtml(o.oprtrId) + '\')"><i class="bi bi-trash"></i></button>'
      + '</td></tr>';
  });
  $('#oprtrTableBody').html(html);
}

function openCreateModal() {
  $('#oprtrModalLabel').text('항공기 등록');
  $('#modalOprtrId,#fAcftRegNo,#fAcftTyp,#fMfgr,#fMfgYr,#fSeatCnt,#fMtow').val('');
  $('#fOprtrId').val('');
  $('#fAcftRegNo').prop('disabled', false);
  oprtrModal.show();
}

function openEditModal(oprtrId) {
  $.get('/api/com/oprtr/' + encodeURIComponent(oprtrId)).done(function(res) {
    var o = res.data;
    $('#oprtrModalLabel').text('항공기 수정');
    $('#modalOprtrId').val(o.oprtrId);
    $('#fOprtrId').val(o.oprtrId);
    $('#fAcftRegNo').val(o.acftRegNo || '').prop('disabled', true);
    $('#fAcftTyp').val(o.acftTyp || '');
    $('#fSeatCnt').val(o.seatCnt || '');
    $('#fMtow').val(o.mtow || '');
    $('#fMfgr').val(o.mfgr || '');
    $('#fMfgYr').val(o.mfgYr || '');
    oprtrModal.show();
  }).fail(function(xhr) { showErr('조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function saveOprtr() {
  var id = $('#modalOprtrId').val();
  var isEdit = !!id;
  var payload = {
    oprtrId: $('#fOprtrId').val(), acftRegNo: $('#fAcftRegNo').val(),
    acftTyp: $('#fAcftTyp').val(), seatCnt: $('#fSeatCnt').val() || null,
    mtow: $('#fMtow').val() || null, mfgr: $('#fMfgr').val(), mfgYr: $('#fMfgYr').val() || null
  };
  if (!payload.oprtrId) { IcasAlert.warning('운항사를 선택하세요.'); return; }
  if (!payload.acftRegNo) { IcasAlert.warning('기체등록번호를 입력하세요.'); return; }
  var url = isEdit ? '/api/com/oprtr/' + encodeURIComponent(id) : '/api/com/oprtr';
  $.ajax({ url: url, type: isEdit ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { oprtrModal.hide(); loadOprtr(); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteOprtr(oprtrId) {
  if (!confirm('항공기 [' + oprtrId + ']를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/oprtr/' + encodeURIComponent(oprtrId), type: 'DELETE' })
    .done(function() { loadOprtr(); })
    .fail(function(xhr) { showErr('삭제 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function escHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#x27;');
}
function showErr(msg) { IcasAlert.error('[오류] ' + msg); }
</script>
</body>
</html>
