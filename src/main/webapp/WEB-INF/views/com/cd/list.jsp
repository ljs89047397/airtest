<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>공통코드 관리 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header th { background: var(--icas-primary); color: #fff; font-weight: 500; font-size: 0.82rem; }
.grp-row { cursor: pointer; }
.grp-row.selected { background: #e8eeff !important; }
.grp-row:hover { background: #f8f9ff; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <div class="d-flex align-items-center mb-3">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">공통코드 관리</h5>
        <small class="text-muted">그룹코드 선택 &rarr; 상세코드 관리 (좌우 분할)</small>
      </div>
    </div>

    <div class="row g-3">

      <!-- 좌: 그룹코드 -->
      <div class="col-md-5">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2">
            <span class="fw-semibold small" style="color:var(--icas-primary);">그룹코드</span>
            <button class="btn btn-xs btn-sm text-white py-0 px-2" style="background:var(--icas-primary);" onclick="openGrpCreateModal()">
              <i class="bi bi-plus-circle me-1"></i>그룹 등록
            </button>
          </div>
          <div class="card-body p-0">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">그룹ID</th><th>그룹명</th><th>설명</th><th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="grpTableBody">
                <tr><td colspan="4" class="text-center py-4 text-muted">로딩 중...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 우: 상세코드 -->
      <div class="col-md-7">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2">
            <span class="fw-semibold small" style="color:var(--icas-primary);">
              상세코드 <span id="dtlGrpLabel" class="text-muted">(그룹 선택 필요)</span>
            </span>
            <button class="btn btn-xs btn-sm text-white py-0 px-2" style="background:var(--icas-primary);" onclick="openDtlCreateModal()" id="btnDtlCreate" disabled>
              <i class="bi bi-plus-circle me-1"></i>코드 등록
            </button>
          </div>
          <div class="card-body p-0">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">코드</th><th>코드명</th><th>영문명</th><th>정렬</th><th>사용여부</th><th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="dtlTableBody">
                <tr><td colspan="6" class="text-center py-4 text-muted small">좌측에서 그룹코드를 선택하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</div>

<!-- 그룹코드 모달 -->
<div class="modal fade" id="grpModal" tabindex="-1" aria-labelledby="grpModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="grpModalLabel">그룹코드 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalGrpId">
        <div class="mb-3">
          <label class="form-label small fw-semibold">그룹ID <span class="text-danger">*</span></label>
          <input type="text" id="fGrpId" class="form-control form-control-sm" placeholder="영문대문자/숫자">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">그룹명 <span class="text-danger">*</span></label>
          <input type="text" id="fGrpNm" class="form-control form-control-sm">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">설명</label>
          <textarea id="fGrpDc" class="form-control form-control-sm" rows="2"></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveGrp()">
          <i class="bi bi-save me-1"></i>저장
        </button>
      </div>
    </div>
  </div>
</div>

<!-- 상세코드 모달 -->
<div class="modal fade" id="dtlModal" tabindex="-1" aria-labelledby="dtlModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="dtlModalLabel">상세코드 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalDtlOrigCd">
        <div class="mb-3">
          <label class="form-label small fw-semibold">코드 <span class="text-danger">*</span></label>
          <input type="text" id="fDtlCd" class="form-control form-control-sm">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">코드명 <span class="text-danger">*</span></label>
          <input type="text" id="fDtlCdNm" class="form-control form-control-sm">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">영문명</label>
          <input type="text" id="fDtlCdEngNm" class="form-control form-control-sm">
        </div>
        <div class="row g-2">
          <div class="col-6">
            <label class="form-label small fw-semibold">정렬순서</label>
            <input type="number" id="fDtlSortOrdr" class="form-control form-control-sm">
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">사용여부</label>
            <select id="fDtlUseYn" class="form-select form-select-sm">
              <option value="Y">사용</option>
              <option value="N">미사용</option>
            </select>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveDtl()">
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
var grpModal = new bootstrap.Modal(document.getElementById('grpModal'));
var dtlModal = new bootstrap.Modal(document.getElementById('dtlModal'));
var selGrpId = null;

$(function() { loadGrp(); });

function loadGrp() {
  $.get('/api/com/cd').done(function(res) {
    var rows = res.data || [];
    if (!rows.length) {
      $('#grpTableBody').html('<tr><td colspan="4" class="text-center py-4 text-muted small">데이터 없음</td></tr>');
      return;
    }
    var html = '';
    rows.forEach(function(g) {
      html += '<tr class="grp-row' + (g.grpId === selGrpId ? ' selected' : '') + '" onclick="selectGrp(\'' + escHtml(g.grpId) + '\')">'
        + '<td class="ps-3 small text-muted">' + escHtml(g.grpId) + '</td>'
        + '<td class="small fw-semibold">' + escHtml(g.grpNm || '') + '</td>'
        + '<td class="small text-muted">' + escHtml(g.grpDc || '-') + '</td>'
        + '<td class="text-center" onclick="event.stopPropagation()">'
        +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openGrpEditModal(\'' + escHtml(g.grpId) + '\')"><i class="bi bi-pencil"></i></button>'
        +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteGrp(\'' + escHtml(g.grpId) + '\')"><i class="bi bi-trash"></i></button>'
        + '</td></tr>';
    });
    $('#grpTableBody').html(html);
  }).fail(function(xhr) { showErr('그룹코드 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function selectGrp(grpId) {
  selGrpId = grpId;
  $('#dtlGrpLabel').text('[' + grpId + ']');
  $('#btnDtlCreate').prop('disabled', false);
  loadDtl(grpId);
  $('.grp-row').removeClass('selected');
  $('.grp-row').each(function() { if ($(this).find('td:first').text().trim() === grpId) $(this).addClass('selected'); });
}

function loadDtl(grpId) {
  $.get('/api/com/cd/' + encodeURIComponent(grpId)).done(function(res) {
    var rows = res.data || [];
    if (!rows.length) {
      $('#dtlTableBody').html('<tr><td colspan="6" class="text-center py-4 text-muted small">상세코드가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    rows.forEach(function(d) {
      html += '<tr>'
        + '<td class="ps-3 small text-muted">' + escHtml(d.cd) + '</td>'
        + '<td class="small fw-semibold">' + escHtml(d.cdNm || '') + '</td>'
        + '<td class="small">' + escHtml(d.cdEngNm || '-') + '</td>'
        + '<td class="small text-end">' + (d.sortOrdr != null ? d.sortOrdr : '-') + '</td>'
        + '<td class="small"><span class="badge ' + (d.useYn === 'Y' ? 'bg-success' : 'bg-secondary') + '">' + (d.useYn === 'Y' ? '사용' : '미사용') + '</span></td>'
        + '<td class="text-center">'
        +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openDtlEditModal(\'' + escHtml(d.cd) + '\')"><i class="bi bi-pencil"></i></button>'
        +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteDtl(\'' + escHtml(d.cd) + '\')"><i class="bi bi-trash"></i></button>'
        + '</td></tr>';
    });
    $('#dtlTableBody').html(html);
  }).fail(function(xhr) { showErr('상세코드 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function openGrpCreateModal() {
  $('#grpModalLabel').text('그룹코드 등록');
  $('#modalGrpId,#fGrpId,#fGrpNm,#fGrpDc').val('');
  $('#fGrpId').prop('disabled', false);
  grpModal.show();
}

function openGrpEditModal(grpId) {
  $.get('/api/com/cd').done(function(res) {
    var g = (res.data || []).find(function(x){ return x.grpId === grpId; });
    if (!g) { showErr('그룹코드를 찾을 수 없습니다.'); return; }
    $('#grpModalLabel').text('그룹코드 수정');
    $('#modalGrpId').val(g.grpId);
    $('#fGrpId').val(g.grpId).prop('disabled', true);
    $('#fGrpNm').val(g.grpNm || '');
    $('#fGrpDc').val(g.grpDc || '');
    grpModal.show();
  });
}

function saveGrp() {
  var id = $('#modalGrpId').val();
  var isEdit = !!id;
  var payload = { grpId: $('#fGrpId').val(), grpNm: $('#fGrpNm').val(), grpDc: $('#fGrpDc').val() };
  if (!payload.grpNm) { IcasAlert.warning('그룹명을 입력하세요.'); return; }
  var url = isEdit ? '/api/com/cd/' + encodeURIComponent(id) : '/api/com/cd';
  $.ajax({ url: url, type: isEdit ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { grpModal.hide(); loadGrp(); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteGrp(grpId) {
  if (!confirm('그룹코드 [' + grpId + ']를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/cd/' + encodeURIComponent(grpId), type: 'DELETE' })
    .done(function() { if (selGrpId === grpId) { selGrpId = null; $('#dtlTableBody').html(''); $('#dtlGrpLabel').text('(그룹 선택 필요)'); } loadGrp(); })
    .fail(function(xhr) { showErr('삭제 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function openDtlCreateModal() {
  if (!selGrpId) { IcasAlert.warning('그룹코드를 먼저 선택하세요.'); return; }
  $('#dtlModalLabel').text('상세코드 등록');
  $('#modalDtlOrigCd,#fDtlCd,#fDtlCdNm,#fDtlCdEngNm,#fDtlSortOrdr').val('');
  $('#fDtlUseYn').val('Y');
  $('#fDtlCd').prop('disabled', false);
  dtlModal.show();
}

function openDtlEditModal(cd) {
  $.get('/api/com/cd/' + encodeURIComponent(selGrpId)).done(function(res) {
    var d = (res.data || []).find(function(x){ return x.cd === cd; });
    if (!d) { showErr('상세코드를 찾을 수 없습니다.'); return; }
    $('#dtlModalLabel').text('상세코드 수정');
    $('#modalDtlOrigCd').val(d.cd);
    $('#fDtlCd').val(d.cd).prop('disabled', true);
    $('#fDtlCdNm').val(d.cdNm || '');
    $('#fDtlCdEngNm').val(d.cdEngNm || '');
    $('#fDtlSortOrdr').val(d.sortOrdr || '');
    $('#fDtlUseYn').val(d.useYn || 'Y');
    dtlModal.show();
  });
}

function saveDtl() {
  var origCd = $('#modalDtlOrigCd').val();
  var isEdit = !!origCd;
  var payload = {
    grpId: selGrpId, cd: $('#fDtlCd').val(), cdNm: $('#fDtlCdNm').val(),
    cdEngNm: $('#fDtlCdEngNm').val(), sortOrdr: $('#fDtlSortOrdr').val() || null, useYn: $('#fDtlUseYn').val()
  };
  if (!payload.cdNm) { IcasAlert.warning('코드명을 입력하세요.'); return; }
  var url = isEdit
    ? '/api/com/cd/' + encodeURIComponent(selGrpId) + '/dtl/' + encodeURIComponent(origCd)
    : '/api/com/cd/' + encodeURIComponent(selGrpId) + '/dtl';
  $.ajax({ url: url, type: isEdit ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { dtlModal.hide(); loadDtl(selGrpId); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteDtl(cd) {
  if (!confirm('상세코드 [' + cd + ']를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/cd/' + encodeURIComponent(selGrpId) + '/dtl/' + encodeURIComponent(cd), type: 'DELETE' })
    .done(function() { loadDtl(selGrpId); })
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
