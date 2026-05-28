<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>결재함 &mdash; ICAS-CEMS</title>
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

    <div class="mb-3">
      <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">결재함</h5>
      <small class="text-muted">받은결재 / 내가 올린 결재 / 처리 대기</small>
    </div>

    <!-- 탭 3개 -->
    <ul class="nav nav-tabs mb-3" id="atrzTab">
      <li class="nav-item"><a class="nav-link active" href="#" onclick="switchTab('received');return false;">받은결재</a></li>
      <li class="nav-item"><a class="nav-link" href="#" onclick="switchTab('mine');return false;">내가 올린</a></li>
      <li class="nav-item">
        <a class="nav-link" href="#" onclick="switchTab('pending');return false;">
          처리 대기 <span id="pendingBadge" class="badge bg-danger ms-1" style="font-size:0.65rem;"></span>
        </a>
      </li>
    </ul>

    <!-- 검색 (received/mine 공통) -->
    <div id="searchArea" class="card border-0 shadow-sm mb-3">
      <div class="card-body py-2">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">결재상태</label>
            <select id="searchStCd" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="PEND">대기</option>
              <option value="INPRG">처리중</option>
              <option value="APRVD">승인완료</option>
              <option value="RJCTD">반려</option>
              <option value="CANCLD">취소</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">기간(시작)</label>
            <input type="date" id="searchDateFrom" class="form-control form-control-sm">
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">기간(종료)</label>
            <input type="date" id="searchDateTo" class="form-control form-control-sm">
          </div>
          <div class="col-md-2">
            <button class="btn btn-sm btn-primary w-100" onclick="loadCurrent(1)">
              <i class="bi bi-search me-1"></i>검색
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 받은결재 탭 -->
    <div id="tab-received">
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">결재ID</th><th>제목</th><th>업무유형</th><th>요청자</th><th>요청일</th><th>상태</th><th class="text-center">처리</th>
              </tr>
            </thead>
            <tbody id="receivedTableBody">
              <tr><td colspan="7" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
          <div id="receivedPaging" class="d-flex justify-content-center py-3"></div>
        </div>
      </div>
    </div>

    <!-- 내가 올린 탭 -->
    <div id="tab-mine" class="d-none">
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">결재ID</th><th>제목</th><th>업무유형</th><th>요청일</th><th>상태</th><th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="mineTableBody">
              <tr><td colspan="6" class="text-center py-4 text-muted">로딩 중...</td></tr>
            </tbody>
          </table>
          <div id="minePaging" class="d-flex justify-content-center py-3"></div>
        </div>
      </div>
    </div>

    <!-- 처리 대기 탭 -->
    <div id="tab-pending" class="d-none">
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">결재ID</th><th>제목</th><th>업무유형</th><th>요청자</th><th>요청일</th><th>순번</th><th class="text-center">처리</th>
              </tr>
            </thead>
            <tbody id="pendingTableBody">
              <tr><td colspan="7" class="text-center py-4 text-muted">로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
</div>

<!-- 결재 상세/처리 모달 -->
<div class="modal fade" id="atrzDetailModal" tabindex="-1" aria-labelledby="atrzDetailModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="atrzDetailModalLabel">결재 상세</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body" id="atrzDetailBody"></div>
      <div class="modal-footer" id="atrzDetailFooter">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>
var detailModal = new bootstrap.Modal(document.getElementById('atrzDetailModal'));
var curTab = 'received';
var curPage = 1;

$(function() {
  loadReceived(1);
  loadPendingCount();
});

function switchTab(tab) {
  curTab = tab;
  ['received','mine','pending'].forEach(function(t) { $('#tab-' + t).addClass('d-none'); });
  $('#tab-' + tab).removeClass('d-none');
  $('#atrzTab .nav-link').removeClass('active');
  var idx = ['received','mine','pending'].indexOf(tab);
  $('#atrzTab .nav-link').eq(idx).addClass('active');
  if (tab === 'pending') { $('#searchArea').addClass('d-none'); loadPending(); }
  else { $('#searchArea').removeClass('d-none'); loadCurrent(1); }
}

function loadCurrent(page) {
  if (curTab === 'received') loadReceived(page);
  else if (curTab === 'mine') loadMine(page);
}

function buildSearch(page) {
  return { atrzStCd: $('#searchStCd').val(), dateFrom: $('#searchDateFrom').val(), dateTo: $('#searchDateTo').val(), page: page, pageSize: 15 };
}

function loadReceived(page) {
  curPage = page;
  $.get('/api/com/atrz', buildSearch(page)).done(function(res) {
    renderAtrzTable('receivedTableBody', res.data, 7, true);
    renderPaging('receivedPaging', res.data, page, 'loadReceived');
  }).fail(function(xhr) { showErr('받은결재 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function loadMine(page) {
  curPage = page;
  var params = $.extend(buildSearch(page), { dmndUserId: '_me_' });
  $.get('/api/com/atrz', params).done(function(res) {
    renderAtrzTable('mineTableBody', res.data, 6, false);
    renderPaging('minePaging', res.data, page, 'loadMine');
  }).fail(function(xhr) { showErr('내 결재 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function loadPending() {
  $.get('/api/com/atrz/my-pending').done(function(res) {
    var rows = res.data || [];
    $('#pendingBadge').text(rows.length || '');
    if (!rows.length) {
      $('#pendingTableBody').html('<tr><td colspan="7" class="text-center py-4 text-muted small">처리 대기 결재가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    rows.forEach(function(p) {
      html += '<tr>'
        + '<td class="ps-3 small text-muted">' + escHtml(p.dmndId) + '</td>'
        + '<td class="small">' + escHtml(p.title || '-') + '</td>'
        + '<td class="small">' + escHtml(p.atrzTaskId || '-') + '</td>'
        + '<td class="small">' + escHtml(p.dmndUserId || '-') + '</td>'
        + '<td class="small text-muted">' + (p.dmndDt || '').substring(0,10) + '</td>'
        + '<td class="small text-center">' + (p.atrzSeq != null ? p.atrzSeq : '-') + '</td>'
        + '<td class="text-center">'
        +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openDetail(\'' + escHtml(p.dmndId) + '\', ' + p.atrzSeq + ', true)"><i class="bi bi-check2-square me-1"></i>처리</button>'
        + '</td></tr>';
    });
    $('#pendingTableBody').html(html);
  }).fail(function(xhr) { showErr('처리대기 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function loadPendingCount() {
  $.get('/api/com/atrz/my-pending').done(function(res) {
    var cnt = (res.data || []).length;
    $('#pendingBadge').text(cnt > 0 ? cnt : '');
  });
}

function renderAtrzTable(tbodyId, page, colspan, showActions) {
  var rows = (page && (page.rows || page.content || (Array.isArray(page) ? page : []))) || [];
  if (!rows.length) {
    $('#' + tbodyId).html('<tr><td colspan="' + colspan + '" class="text-center py-4 text-muted small">조회된 결재가 없습니다.</td></tr>');
    return;
  }
  var stMap = { PEND:['bg-secondary','대기'], INPRG:['bg-warning text-dark','처리중'], APRVD:['bg-success','승인완료'], RJCTD:['bg-danger','반려'], CANCLD:['bg-light text-muted border','취소'] };
  var html = '';
  rows.forEach(function(r) {
    var st = stMap[r.atrzStCd] || ['bg-secondary', r.atrzStCd];
    var stBadge = '<span class="badge ' + st[0] + ' small">' + st[1] + '</span>';
    if (showActions) {
      html += '<tr>'
        + '<td class="ps-3 small text-muted">' + escHtml(r.dmndId) + '</td>'
        + '<td class="small">' + escHtml(r.title || '-') + '</td>'
        + '<td class="small">' + escHtml(r.atrzTaskId || '-') + '</td>'
        + '<td class="small">' + escHtml(r.dmndUserId || '-') + '</td>'
        + '<td class="small text-muted">' + (r.dmndDt || '').substring(0,10) + '</td>'
        + '<td>' + stBadge + '</td>'
        + '<td class="text-center"><button class="btn btn-sm btn-outline-primary py-0 px-2" onclick="openDetail(\'' + escHtml(r.dmndId) + '\', null, false)"><i class="bi bi-eye"></i></button></td>'
        + '</tr>';
    } else {
      html += '<tr>'
        + '<td class="ps-3 small text-muted">' + escHtml(r.dmndId) + '</td>'
        + '<td class="small">' + escHtml(r.title || '-') + '</td>'
        + '<td class="small">' + escHtml(r.atrzTaskId || '-') + '</td>'
        + '<td class="small text-muted">' + (r.dmndDt || '').substring(0,10) + '</td>'
        + '<td>' + stBadge + '</td>'
        + '<td class="text-center">'
        +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openDetail(\'' + escHtml(r.dmndId) + '\', null, false)"><i class="bi bi-eye"></i></button>'
        +   (r.atrzStCd === 'PEND' || r.atrzStCd === 'INPRG' ? '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="cancelAtrz(\'' + escHtml(r.dmndId) + '\')"><i class="bi bi-x-circle"></i></button>' : '')
        + '</td></tr>';
    }
  });
  $('#' + tbodyId).html(html);
}

function renderPaging(pid, page, cur, fnName) {
  if (!page || !page.totalPages) { $('#' + pid).html(''); return; }
  var html = '<nav><ul class="pagination pagination-sm mb-0">';
  for (var i = 1; i <= page.totalPages; i++) {
    html += '<li class="page-item' + (i===cur?' active':'') + '"><a class="page-link" href="#" onclick="' + fnName + '(' + i + ');return false;">' + i + '</a></li>';
  }
  html += '</ul></nav>';
  $('#' + pid).html(html);
}

function openDetail(dmndId, atrzSeq, canProcess) {
  $.get('/api/com/atrz/' + encodeURIComponent(dmndId)).done(function(res) {
    var d = res.data;
    var dmnd = d.dmnd || d;
    var prcsList = d.prcsList || d.prcs || [];
    var html = '<dl class="row small mb-3">'
      + '<dt class="col-sm-3">결재ID</dt><dd class="col-sm-9">' + escHtml(dmnd.dmndId) + '</dd>'
      + '<dt class="col-sm-3">제목</dt><dd class="col-sm-9">' + escHtml(dmnd.title || '-') + '</dd>'
      + '<dt class="col-sm-3">내용</dt><dd class="col-sm-9">' + escHtml(dmnd.contents || '-') + '</dd>'
      + '<dt class="col-sm-3">업무유형</dt><dd class="col-sm-9">' + escHtml(dmnd.atrzTaskId || '-') + '</dd>'
      + '<dt class="col-sm-3">상태</dt><dd class="col-sm-9">' + escHtml(dmnd.atrzStCd || '-') + '</dd>'
      + '</dl>';
    if (prcsList.length) {
      html += '<h6 class="small fw-bold text-muted">결재 이력</h6><table class="table table-sm"><thead><tr><th>순번</th><th>결재자</th><th>처리</th><th>의견</th><th>처리일</th></tr></thead><tbody>';
      prcsList.forEach(function(p) {
        html += '<tr><td>' + (p.atrzSeq||'-') + '</td><td>' + escHtml(p.atrzUserId||'-') + '</td><td>' + escHtml(p.atrzStCd||'대기') + '</td><td>' + escHtml(p.atrzOpnn||'-') + '</td><td>' + (p.prcsYmd||'').substring(0,10) + '</td></tr>';
      });
      html += '</tbody></table>';
    }
    $('#atrzDetailBody').html(html);
    var footer = '<button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>';
    if (canProcess && atrzSeq !== null) {
      footer += '<div class="ms-auto d-flex gap-2 align-items-center">'
        + '<input type="text" id="atrzOpnnInput" class="form-control form-control-sm" placeholder="의견 (선택)" style="width:200px;">'
        + '<button class="btn btn-sm btn-success" onclick="approveAtrz(\'' + escHtml(dmndId) + '\',' + atrzSeq + ')"><i class="bi bi-check-circle me-1"></i>승인</button>'
        + '<button class="btn btn-sm btn-danger" onclick="rejectAtrz(\'' + escHtml(dmndId) + '\',' + atrzSeq + ')"><i class="bi bi-x-circle me-1"></i>반려</button>'
        + '</div>';
    }
    $('#atrzDetailFooter').html(footer);
    detailModal.show();
  }).fail(function(xhr) { showErr('상세 조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function approveAtrz(dmndId, atrzSeq) {
  var opnn = $('#atrzOpnnInput').val();
  $.ajax({ url: '/api/com/atrz/' + encodeURIComponent(dmndId) + '/approve', type: 'POST',
    contentType: 'application/json', data: JSON.stringify({ atrzSeq: atrzSeq, atrzOpnn: opnn })
  }).done(function() { detailModal.hide(); loadPending(); loadPendingCount(); })
    .fail(function(xhr) { showErr('승인 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function rejectAtrz(dmndId, atrzSeq) {
  var opnn = $('#atrzOpnnInput').val();
  if (!opnn) { IcasAlert.warning('반려 시 의견을 입력하세요.'); return; }
  $.ajax({ url: '/api/com/atrz/' + encodeURIComponent(dmndId) + '/reject', type: 'POST',
    contentType: 'application/json', data: JSON.stringify({ atrzSeq: atrzSeq, atrzOpnn: opnn })
  }).done(function() { detailModal.hide(); loadPending(); loadPendingCount(); })
    .fail(function(xhr) { showErr('반려 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function cancelAtrz(dmndId) {
  if (!confirm('결재를 취소하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/atrz/' + encodeURIComponent(dmndId) + '/cancel', type: 'POST' })
    .done(function() { loadMine(curPage); })
    .fail(function(xhr) { showErr('취소 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function escHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#x27;');
}
function showErr(msg) { IcasAlert.error('[오류] ' + msg); }
</script>
</body>
</html>
