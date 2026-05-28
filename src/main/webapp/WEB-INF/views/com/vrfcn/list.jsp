<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>검증기관 관리 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header th { background: var(--icas-primary); color: #fff; font-weight: 500; font-size: 0.82rem; }
.nav-tabs .nav-link.active { border-bottom: 3px solid var(--icas-primary); color: var(--icas-primary); font-weight: 600; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <div class="d-flex align-items-center justify-content-between mb-3">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">검증기관 관리</h5>
        <small class="text-muted">검증기관 등록 및 항공사 배정</small>
      </div>
      <div id="tabActionArea">
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" id="btnCreate" onclick="openCreateModal()">
          <i class="bi bi-building-add me-1"></i>기관 등록
        </button>
      </div>
    </div>

    <!-- 탭 -->
    <ul class="nav nav-tabs mb-3" id="vrfcnTab">
      <li class="nav-item">
        <a class="nav-link active" href="#" onclick="switchTab('inst');return false;">검증기관</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="#" onclick="switchTab('assgn');return false;">검증배정</a>
      </li>
    </ul>

    <!-- 검증기관 탭 -->
    <div id="tab-inst">
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">기관ID</th>
                  <th>기관명</th>
                  <th>대표자</th>
                  <th>인증번호</th>
                  <th>인증만료일</th>
                  <th>전화</th>
                  <th>등록일</th>
                  <th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="instTableBody">
                <tr><td colspan="8" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 검증배정 탭 -->
    <div id="tab-assgn" class="d-none">
      <div class="card border-0 shadow-sm mb-3">
        <div class="card-body py-2">
          <div class="row g-2 align-items-end">
            <div class="col-md-3">
              <label class="form-label small mb-1">보고연도</label>
              <input type="number" id="assgnYr" class="form-control form-control-sm" value="2026">
            </div>
            <div class="col-md-2">
              <button class="btn btn-sm btn-primary w-100" onclick="loadAssgn()">
                <i class="bi bi-search me-1"></i>조회
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
                  <th class="ps-3">검증기관</th>
                  <th>운항사</th>
                  <th>보고연도</th>
                  <th>배정일</th>
                  <th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="assgnTableBody">
                <tr><td colspan="5" class="text-center py-4 text-muted">조회 버튼을 누르세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

  </div>
</div>

<!-- 검증기관 등록/수정 모달 -->
<div class="modal fade" id="instModal" tabindex="-1" aria-labelledby="instModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="instModalLabel">검증기관 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalInstId">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기관ID <span class="text-danger">*</span></label>
            <input type="text" id="fInstId" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기관명 <span class="text-danger">*</span></label>
            <input type="text" id="fInstNm" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">대표자명</label>
            <input type="text" id="fRprsNm" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">인증번호</label>
            <input type="text" id="fCertNo" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">인증만료일</label>
            <input type="date" id="fCertExprDt" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">전화번호</label>
            <input type="text" id="fInstTlphn" class="form-control form-control-sm">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">주소</label>
            <input type="text" id="fInstAddr" class="form-control form-control-sm">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveInst()">
          <i class="bi bi-save me-1"></i>저장
        </button>
      </div>
    </div>
  </div>
</div>

<!-- 배정 등록 모달 -->
<div class="modal fade" id="assgnModal" tabindex="-1" aria-labelledby="assgnModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="assgnModalLabel">검증배정 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">검증기관 <span class="text-danger">*</span></label>
          <select id="fAssgnInstId" class="form-select form-select-sm"></select>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">운항사 <span class="text-danger">*</span></label>
          <select id="fAssgnOprtrId" class="form-select form-select-sm"></select>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
          <input type="number" id="fAssgnYr" class="form-control form-control-sm" value="2026">
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveAssgn()">
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
var instModal  = new bootstrap.Modal(document.getElementById('instModal'));
var assgnModal = new bootstrap.Modal(document.getElementById('assgnModal'));
var curTab = 'inst';

$(function() {
  loadInst();
  loadInstForSelect();
  loadOprtrForSelect();
});

function switchTab(tab) {
  curTab = tab;
  $('#tab-inst, #tab-assgn').addClass('d-none');
  $('#tab-' + tab).removeClass('d-none');
  $('#vrfcnTab .nav-link').removeClass('active');
  $('#vrfcnTab .nav-link').eq(tab === 'inst' ? 0 : 1).addClass('active');
  if (tab === 'inst') {
    $('#btnCreate').show().text('기관 등록').attr('onclick','openCreateModal()');
  } else {
    $('#btnCreate').show().html('<i class="bi bi-plus-circle me-1"></i>배정 등록').attr('onclick','openAssgnModal()');
    loadAssgn();
  }
}

function loadInst() {
  $.get('/api/com/vrfcn/inst').done(function(res) {
    var rows = res.data || [];
    if (!rows.length) {
      $('#instTableBody').html('<tr><td colspan="8" class="text-center py-4 text-muted small">데이터가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    rows.forEach(function(o) {
      html += '<tr>'
        + '<td class="ps-3 small text-muted">' + escHtml(o.vrfcnInstId) + '</td>'
        + '<td class="fw-semibold small">' + escHtml(o.vrfcnInstNm || '') + '</td>'
        + '<td class="small">' + escHtml(o.rprsNm || '-') + '</td>'
        + '<td class="small">' + escHtml(o.certNo || '-') + '</td>'
        + '<td class="small">' + escHtml(o.certExprDt || '-') + '</td>'
        + '<td class="small">' + escHtml(o.tlphnNo || '-') + '</td>'
        + '<td class="small text-muted">' + (o.frstRgtrDt || '').substring(0,10) + '</td>'
        + '<td class="text-center">'
        +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openEditInstModal(\'' + escHtml(o.vrfcnInstId) + '\')"><i class="bi bi-pencil"></i></button>'
        +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteInst(\'' + escHtml(o.vrfcnInstId) + '\')"><i class="bi bi-trash"></i></button>'
        + '</td></tr>';
    });
    $('#instTableBody').html(html);
  }).fail(function(xhr) { showErr('검증기관 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function loadInstForSelect() {
  $.get('/api/com/vrfcn/inst').done(function(res) {
    var opts = '<option value="">선택</option>';
    (res.data || []).forEach(function(o) {
      opts += '<option value="' + escHtml(o.vrfcnInstId) + '">' + escHtml(o.vrfcnInstNm) + '</option>';
    });
    $('#fAssgnInstId').html(opts);
  });
}

function loadOprtrForSelect() {
  $.get('/api/com/oprtr').done(function(res) {
    var opts = '<option value="">선택</option>';
    (res.data || []).forEach(function(o) {
      opts += '<option value="' + escHtml(o.oprtrId) + '">' + escHtml(o.oprtrNm || o.oprtrId) + '</option>';
    });
    $('#fAssgnOprtrId').html(opts);
  });
}

function loadAssgn() {
  var yr = $('#assgnYr').val();
  $.get('/api/com/vrfcn/assgn', { rprtYr: yr }).done(function(res) {
    var rows = res.data || [];
    if (!rows.length) {
      $('#assgnTableBody').html('<tr><td colspan="5" class="text-center py-4 text-muted small">배정 데이터가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    rows.forEach(function(o) {
      html += '<tr>'
        + '<td class="ps-3 small">' + escHtml(o.vrfcnInstNm || o.vrfcnInstId) + '</td>'
        + '<td class="small">' + escHtml(o.oprtrNm || o.oprtrId) + '</td>'
        + '<td class="small">' + escHtml(o.rprtYr || yr) + '</td>'
        + '<td class="small text-muted">' + (o.frstRgtrDt || o.assgnDt || '').substring(0,10) + '</td>'
        + '<td class="text-center">'
        +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteAssgn(\'' + escHtml(o.vrfcnInstId) + '\',\'' + escHtml(o.oprtrId) + '\',\'' + escHtml(o.rprtYr || yr) + '\')"><i class="bi bi-trash"></i></button>'
        + '</td></tr>';
    });
    $('#assgnTableBody').html(html);
  }).fail(function(xhr) { showErr('배정 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function openCreateModal() {
  $('#instModalLabel').text('검증기관 등록');
  $('#modalInstId,#fInstId,#fInstNm,#fRprsNm,#fCertNo,#fCertExprDt,#fInstTlphn,#fInstAddr').val('');
  $('#fInstId').prop('disabled', false);
  instModal.show();
}

function openEditInstModal(id) {
  $.get('/api/com/vrfcn/inst/' + encodeURIComponent(id)).done(function(res) {
    var o = res.data;
    $('#instModalLabel').text('검증기관 수정');
    $('#modalInstId').val(o.vrfcnInstId);
    $('#fInstId').val(o.vrfcnInstId).prop('disabled', true);
    $('#fInstNm').val(o.vrfcnInstNm || '');
    $('#fRprsNm').val(o.rprsNm || '');
    $('#fCertNo').val(o.certNo || '');
    $('#fCertExprDt').val(o.certExprDt || '');
    $('#fInstTlphn').val(o.tlphnNo || '');
    $('#fInstAddr').val(o.addr || '');
    instModal.show();
  }).fail(function(xhr) { showErr('조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function saveInst() {
  var id = $('#modalInstId').val();
  var isEdit = !!id;
  var payload = {
    vrfcnInstId: $('#fInstId').val(), vrfcnInstNm: $('#fInstNm').val(),
    rprsNm: $('#fRprsNm').val(), certNo: $('#fCertNo').val(),
    certExprDt: $('#fCertExprDt').val(), tlphnNo: $('#fInstTlphn').val(), addr: $('#fInstAddr').val()
  };
  if (!payload.vrfcnInstNm) { IcasAlert.warning('기관명을 입력하세요.'); return; }
  var url = isEdit ? '/api/com/vrfcn/inst/' + encodeURIComponent(id) : '/api/com/vrfcn/inst';
  $.ajax({ url: url, type: isEdit ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { instModal.hide(); loadInst(); loadInstForSelect(); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteInst(id) {
  if (!confirm('검증기관 [' + id + ']을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/vrfcn/inst/' + encodeURIComponent(id), type: 'DELETE' })
    .done(function() { loadInst(); })
    .fail(function(xhr) { showErr('삭제 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function openAssgnModal() {
  $('#fAssgnInstId,#fAssgnOprtrId').val('');
  $('#fAssgnYr').val($('#assgnYr').val() || '2026');
  assgnModal.show();
}

function saveAssgn() {
  var payload = {
    vrfcnInstId: $('#fAssgnInstId').val(),
    oprtrId: $('#fAssgnOprtrId').val(),
    rprtYr: $('#fAssgnYr').val()
  };
  if (!payload.vrfcnInstId) { IcasAlert.warning('검증기관을 선택하세요.'); return; }
  if (!payload.oprtrId) { IcasAlert.warning('운항사를 선택하세요.'); return; }
  if (!payload.rprtYr) { IcasAlert.warning('보고연도를 입력하세요.'); return; }
  $.ajax({ url: '/api/com/vrfcn/assgn', type: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { assgnModal.hide(); loadAssgn(); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteAssgn(instId, oprtrId, yr) {
  if (!confirm('배정을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/vrfcn/assgn?vrfcnInstId=' + encodeURIComponent(instId)
    + '&oprtrId=' + encodeURIComponent(oprtrId) + '&rprtYr=' + encodeURIComponent(yr), type: 'DELETE' })
    .done(function() { loadAssgn(); })
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
