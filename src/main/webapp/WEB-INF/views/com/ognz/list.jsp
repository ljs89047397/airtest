<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>기관 관리 &mdash; ICAS-CEMS</title>
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
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">기관 관리</h5>
        <small class="text-muted">MOLIT / KOTSA / AIRLINE / VERIFIER 구분 기관 목록</small>
      </div>
      <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="openCreateModal()">
        <i class="bi bi-building-add me-1"></i>기관 등록
      </button>
    </div>

    <!-- 검색 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-2">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">기관구분</label>
            <select id="searchSeCd" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="MOLIT">국토부(MOLIT)</option>
              <option value="KOTSA">교통안전공단(KOTSA)</option>
              <option value="AIRLINE">항공사(AIRLINE)</option>
              <option value="VERIFIER">검증기관(VERIFIER)</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">기관명</label>
            <input type="text" id="searchOgnzNm" class="form-control form-control-sm" placeholder="기관명 검색">
          </div>
          <div class="col-md-2">
            <button class="btn btn-sm btn-primary w-100" onclick="loadOgnz()">
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
                <th class="ps-3">기관ID</th>
                <th>기관명</th>
                <th>구분</th>
                <th>ICAO 코드</th>
                <th>담당자</th>
                <th>전화</th>
                <th>등록일</th>
                <th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="ognzTableBody">
              <tr><td colspan="8" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
</div>

<!-- 등록/수정 모달 -->
<div class="modal fade" id="ognzModal" tabindex="-1" aria-labelledby="ognzModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="ognzModalLabel">기관 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalOgnzId">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기관ID <span class="text-danger">*</span></label>
            <input type="text" id="fOgnzId" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기관명 <span class="text-danger">*</span></label>
            <input type="text" id="fOgnzNm" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기관구분 <span class="text-danger">*</span></label>
            <select id="fOgnzSeCd" class="form-select form-select-sm">
              <option value="">선택</option>
              <option value="MOLIT">국토부(MOLIT)</option>
              <option value="KOTSA">교통안전공단(KOTSA)</option>
              <option value="AIRLINE">항공사(AIRLINE)</option>
              <option value="VERIFIER">검증기관(VERIFIER)</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">ICAO 코드</label>
            <input type="text" id="fIcaoCd" class="form-control form-control-sm" placeholder="예: KAL">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">담당자명</label>
            <input type="text" id="fChrgNm" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">전화번호</label>
            <input type="text" id="fTlphnNo" class="form-control form-control-sm">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">주소</label>
            <input type="text" id="fAddr" class="form-control form-control-sm">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveOgnz()">
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
var ognzModal = new bootstrap.Modal(document.getElementById('ognzModal'));
var allOgnz = [];

$(function() { loadOgnz(); });

function loadOgnz() {
  $.get('/api/com/ognz').done(function(res) {
    allOgnz = res.data || [];
    var seCd = $('#searchSeCd').val();
    var nm = $('#searchOgnzNm').val().toLowerCase();
    var filtered = allOgnz.filter(function(o) {
      return (!seCd || o.ognzSeCd === seCd) && (!nm || (o.ognzNm||'').toLowerCase().includes(nm));
    });
    renderTable(filtered);
  }).fail(function(xhr) { showErr('기관 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function renderTable(rows) {
  if (!rows.length) {
    $('#ognzTableBody').html('<tr><td colspan="8" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  var seBadge = { MOLIT:'primary', KOTSA:'info', AIRLINE:'success', VERIFIER:'warning' };
  var html = '';
  rows.forEach(function(o) {
    var badge = seBadge[o.ognzSeCd] || 'secondary';
    html += '<tr>'
      + '<td class="ps-3 small text-muted">' + escHtml(o.ognzId) + '</td>'
      + '<td class="fw-semibold small">' + escHtml(o.ognzNm || '') + '</td>'
      + '<td><span class="badge bg-' + badge + ' small">' + escHtml(o.ognzSeCd || '') + '</span></td>'
      + '<td class="small">' + escHtml(o.icaoCd || '-') + '</td>'
      + '<td class="small">' + escHtml(o.chrgNm || '-') + '</td>'
      + '<td class="small">' + escHtml(o.tlphnNo || '-') + '</td>'
      + '<td class="small text-muted">' + (o.frstRgtrDt || '').substring(0,10) + '</td>'
      + '<td class="text-center">'
      +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openEditModal(\'' + escHtml(o.ognzId) + '\')"><i class="bi bi-pencil"></i></button>'
      +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteOgnz(\'' + escHtml(o.ognzId) + '\')"><i class="bi bi-trash"></i></button>'
      + '</td></tr>';
  });
  $('#ognzTableBody').html(html);
}

function openCreateModal() {
  $('#ognzModalLabel').text('기관 등록');
  $('#modalOgnzId,#fOgnzId,#fOgnzNm,#fIcaoCd,#fChrgNm,#fTlphnNo,#fAddr').val('');
  $('#fOgnzSeCd').val('');
  $('#fOgnzId').prop('disabled', false);
  ognzModal.show();
}

function openEditModal(ognzId) {
  $.get('/api/com/ognz/' + encodeURIComponent(ognzId)).done(function(res) {
    var o = res.data;
    $('#ognzModalLabel').text('기관 수정');
    $('#modalOgnzId').val(o.ognzId);
    $('#fOgnzId').val(o.ognzId).prop('disabled', true);
    $('#fOgnzNm').val(o.ognzNm || '');
    $('#fOgnzSeCd').val(o.ognzSeCd || '');
    $('#fIcaoCd').val(o.icaoCd || '');
    $('#fChrgNm').val(o.chrgNm || '');
    $('#fTlphnNo').val(o.tlphnNo || '');
    $('#fAddr').val(o.addr || '');
    ognzModal.show();
  }).fail(function(xhr) { showErr('기관 조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function saveOgnz() {
  var ognzId = $('#modalOgnzId').val();
  var isEdit = !!ognzId;
  var payload = {
    ognzId: $('#fOgnzId').val(), ognzNm: $('#fOgnzNm').val(),
    ognzSeCd: $('#fOgnzSeCd').val(), icaoCd: $('#fIcaoCd').val(),
    chrgNm: $('#fChrgNm').val(), tlphnNo: $('#fTlphnNo').val(), addr: $('#fAddr').val()
  };
  if (!payload.ognzNm) { IcasAlert.warning('기관명을 입력하세요.'); return; }
  if (!payload.ognzSeCd) { IcasAlert.warning('기관구분을 선택하세요.'); return; }
  var url = isEdit ? '/api/com/ognz/' + encodeURIComponent(ognzId) : '/api/com/ognz';
  $.ajax({ url: url, type: isEdit ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { ognzModal.hide(); loadOgnz(); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteOgnz(ognzId) {
  if (!confirm('기관 [' + ognzId + ']을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/ognz/' + encodeURIComponent(ognzId), type: 'DELETE' })
    .done(function() { loadOgnz(); })
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
