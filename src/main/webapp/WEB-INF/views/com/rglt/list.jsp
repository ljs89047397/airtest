<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>규정 게시판 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header th { background: var(--icas-primary); color: #fff; font-weight: 500; font-size: 0.82rem; }
.rglt-title { cursor: pointer; color: var(--icas-primary); text-decoration: underline; }
.rglt-title:hover { opacity: 0.8; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <div class="d-flex align-items-center justify-content-between mb-3">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">규정 게시판</h5>
        <small class="text-muted">국제항공 탄소배출 관련 규정·지침 (등록은 MOLIT 전용)</small>
      </div>
      <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="openCreateModal()" id="btnRgltCreate">
        <i class="bi bi-file-earmark-plus me-1"></i>규정 등록
      </button>
    </div>

    <!-- 검색 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-2">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">규정유형</label>
            <select id="searchRgltTypCd" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="LAW">법령</option>
              <option value="RULE">규정</option>
              <option value="GUID">지침</option>
              <option value="NTCE">공지</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">제목 검색</label>
            <input type="text" id="searchRgltTitle" class="form-control form-control-sm" placeholder="제목 입력">
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">시작일</label>
            <input type="date" id="searchDateFrom" class="form-control form-control-sm">
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">종료일</label>
            <input type="date" id="searchDateTo" class="form-control form-control-sm">
          </div>
          <div class="col-md-2">
            <button class="btn btn-sm btn-primary w-100" onclick="loadRglt(1)">
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
                <th class="ps-3" style="width:80px;">번호</th>
                <th>유형</th>
                <th>제목</th>
                <th>시행일</th>
                <th>작성자</th>
                <th>등록일</th>
                <th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="rgltTableBody">
              <tr><td colspan="7" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
        <div id="rgltPaging" class="d-flex justify-content-center py-3"></div>
      </div>
    </div>

  </div>
</div>

<!-- 등록/수정 모달 -->
<div class="modal fade" id="rgltModal" tabindex="-1" aria-labelledby="rgltModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="rgltModalLabel">규정 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalRgltId">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-semibold">규정유형 <span class="text-danger">*</span></label>
            <select id="fRgltTypCd" class="form-select form-select-sm">
              <option value="">선택</option>
              <option value="LAW">법령</option>
              <option value="RULE">규정</option>
              <option value="GUID">지침</option>
              <option value="NTCE">공지</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">시행일</label>
            <input type="date" id="fEnfcDt" class="form-control form-control-sm">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">제목 <span class="text-danger">*</span></label>
            <input type="text" id="fRgltTitle" class="form-control form-control-sm">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">내용</label>
            <textarea id="fRgltCn" class="form-control form-control-sm" rows="6" placeholder="규정 내용을 입력하세요."></textarea>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">규정번호 (RG 채번)</label>
            <input type="text" id="fRgltNo" class="form-control form-control-sm" placeholder="자동 채번 또는 직접 입력">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">공개여부</label>
            <select id="fOpenYn" class="form-select form-select-sm">
              <option value="Y">공개</option>
              <option value="N">비공개</option>
            </select>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveRglt()">
          <i class="bi bi-save me-1"></i>저장
        </button>
      </div>
    </div>
  </div>
</div>

<!-- 상세 보기 모달 -->
<div class="modal fade" id="rgltViewModal" tabindex="-1" aria-labelledby="rgltViewModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="rgltViewModalLabel">규정 상세</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body" id="rgltViewBody"></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>
var rgltModal     = new bootstrap.Modal(document.getElementById('rgltModal'));
var rgltViewModal = new bootstrap.Modal(document.getElementById('rgltViewModal'));
var curPage = 1;

$(function() { loadRglt(1); });

function loadRglt(page) {
  curPage = page;
  var params = {
    rgltTypCd: $('#searchRgltTypCd').val(),
    title: $('#searchRgltTitle').val(),
    dateFrom: $('#searchDateFrom').val(),
    dateTo: $('#searchDateTo').val(),
    page: page, pageSize: 15
  };
  $.get('/api/com/rglt', params).done(function(res) {
    renderTable(res.data, page);
    renderPaging(res.data, page);
  }).fail(function(xhr) { showErr('목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function renderTable(page, curPg) {
  var rows = (page && (page.rows || page.content || (Array.isArray(page) ? page : []))) || [];
  var totalElements = (page && (page.total || page.totalElements || page.totalCount)) || (rows ? rows.length : 0);
  var pageSize = page && page.size ? page.size : 15;
  if (!rows.length) {
    $('#rgltTableBody').html('<tr><td colspan="7" class="text-center py-4 text-muted small">조회된 규정이 없습니다.</td></tr>');
    return;
  }
  var typMap = { LAW:'법령', RULE:'규정', GUID:'지침', NTCE:'공지' };
  var typBadge = { LAW:'primary', RULE:'info', GUID:'success', NTCE:'warning' };
  var html = '';
  rows.forEach(function(r, idx) {
    var no = totalElements - (curPg - 1) * pageSize - idx;
    var typNm = typMap[r.rgltTypCd] || r.rgltTypCd || '-';
    var badge = typBadge[r.rgltTypCd] || 'secondary';
    html += '<tr>'
      + '<td class="ps-3 small text-muted">' + no + '</td>'
      + '<td><span class="badge bg-' + badge + ' small">' + escHtml(typNm) + '</span></td>'
      + '<td class="small"><a class="rglt-title" onclick="viewRglt(\'' + escHtml(r.rgltId) + '\');return false;" href="#">' + escHtml(r.title || r.rgltNm || '-') + '</a></td>'
      + '<td class="small">' + escHtml(r.enfcDt || '-') + '</td>'
      + '<td class="small">' + escHtml(r.frstRgtrNm || r.frstRgtrId || '-') + '</td>'
      + '<td class="small text-muted">' + (r.frstRgtrDt || '').substring(0,10) + '</td>'
      + '<td class="text-center">'
      +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openEditModal(\'' + escHtml(r.rgltId) + '\')"><i class="bi bi-pencil"></i></button>'
      +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="archiveRglt(\'' + escHtml(r.rgltId) + '\')"><i class="bi bi-eye-slash"></i></button>'
      + '</td></tr>';
  });
  $('#rgltTableBody').html(html);
}

function renderPaging(page, cur) {
  if (!page || !page.totalPages) { $('#rgltPaging').html(''); return; }
  var html = '<nav><ul class="pagination pagination-sm mb-0">';
  for (var i = 1; i <= page.totalPages; i++) {
    html += '<li class="page-item' + (i===cur?' active':'') + '"><a class="page-link" href="#" onclick="loadRglt(' + i + ');return false;">' + i + '</a></li>';
  }
  html += '</ul></nav>';
  $('#rgltPaging').html(html);
}

function viewRglt(rgltId) {
  $.get('/api/com/rglt/' + encodeURIComponent(rgltId)).done(function(res) {
    var r = res.data;
    var typMap = { LAW:'법령', RULE:'규정', GUID:'지침', NTCE:'공지' };
    var html = '<dl class="row small">'
      + '<dt class="col-sm-3">규정번호</dt><dd class="col-sm-9">' + escHtml(r.rgltNo || r.rgltId) + '</dd>'
      + '<dt class="col-sm-3">유형</dt><dd class="col-sm-9">' + escHtml(typMap[r.rgltTypCd] || r.rgltTypCd || '-') + '</dd>'
      + '<dt class="col-sm-3">제목</dt><dd class="col-sm-9 fw-semibold">' + escHtml(r.title || r.rgltNm || '-') + '</dd>'
      + '<dt class="col-sm-3">시행일</dt><dd class="col-sm-9">' + escHtml(r.enfcDt || '-') + '</dd>'
      + '<dt class="col-sm-3">공개여부</dt><dd class="col-sm-9">' + (r.openYn === 'Y' ? '공개' : '비공개') + '</dd>'
      + '</dl>'
      + '<hr><div class="small" style="white-space:pre-wrap; line-height:1.7;">' + escHtml(r.rgltCn || r.contents || '') + '</div>';
    $('#rgltViewBody').html(html);
    $('#rgltViewModalLabel').text(r.title || r.rgltNm || '규정 상세');
    rgltViewModal.show();
  }).fail(function(xhr) { showErr('상세 조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function openCreateModal() {
  $('#rgltModalLabel').text('규정 등록');
  $('#modalRgltId,#fRgltTitle,#fRgltCn,#fRgltNo,#fEnfcDt').val('');
  $('#fRgltTypCd').val('');
  $('#fOpenYn').val('Y');
  rgltModal.show();
}

function openEditModal(rgltId) {
  $.get('/api/com/rglt/' + encodeURIComponent(rgltId)).done(function(res) {
    var r = res.data;
    $('#rgltModalLabel').text('규정 수정');
    $('#modalRgltId').val(r.rgltId);
    $('#fRgltTypCd').val(r.rgltTypCd || '');
    $('#fEnfcDt').val(r.enfcDt || '');
    $('#fRgltTitle').val(r.title || r.rgltNm || '');
    $('#fRgltCn').val(r.rgltCn || r.contents || '');
    $('#fRgltNo').val(r.rgltNo || '');
    $('#fOpenYn').val(r.openYn || 'Y');
    rgltModal.show();
  }).fail(function(xhr) { showErr('조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function saveRglt() {
  var id = $('#modalRgltId').val();
  var isEdit = !!id;
  var payload = {
    rgltTypCd: $('#fRgltTypCd').val(),
    title: $('#fRgltTitle').val(),
    rgltCn: $('#fRgltCn').val(),
    rgltNo: $('#fRgltNo').val(),
    enfcDt: $('#fEnfcDt').val(),
    openYn: $('#fOpenYn').val()
  };
  if (!payload.title) { IcasAlert.warning('제목을 입력하세요.'); return; }
  if (!payload.rgltTypCd) { IcasAlert.warning('규정유형을 선택하세요.'); return; }
  var url = isEdit ? '/api/com/rglt/' + encodeURIComponent(id) : '/api/com/rglt';
  $.ajax({ url: url, type: isEdit ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() { rgltModal.hide(); loadRglt(curPage); })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function archiveRglt(rgltId) {
  if (!confirm('[' + rgltId + ']를 비공개 처리하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/rglt/' + encodeURIComponent(rgltId), type: 'DELETE' })
    .done(function() { loadRglt(curPage); })
    .fail(function(xhr) { showErr('비공개 처리 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function escHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#x27;');
}
function showErr(msg) { IcasAlert.error('[오류] ' + msg); }
</script>
</body>
</html>
