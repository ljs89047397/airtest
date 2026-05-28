<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>사용자 관리 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header th { background: var(--icas-primary); color: #fff; font-weight: 500; font-size: 0.82rem; }
.table tbody tr:hover { background: #f8f9ff; }
.badge-role { font-size: 0.7rem; padding: 2px 7px; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <!-- 페이지 타이틀 -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">사용자 관리</h5>
        <small class="text-muted">시스템 사용자 등록 및 역할 배정</small>
      </div>
      <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="openCreateModal()">
        <i class="bi bi-person-plus me-1"></i>사용자 등록
      </button>
    </div>

    <!-- 검색 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-2">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">사용자명</label>
            <input type="text" id="searchUserNm" class="form-control form-control-sm" placeholder="성명 검색">
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">기관</label>
            <select id="searchOgnzId" class="form-select form-select-sm">
              <option value="">전체</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">잠금여부</label>
            <select id="searchLockYn" class="form-select form-select-sm">
              <option value="">전체</option>
              <option value="N">정상</option>
              <option value="Y">잠금</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-sm btn-primary w-100" onclick="loadUsers(1)">
              <i class="bi bi-search me-1"></i>검색
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 목록 -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">사용자ID</th>
                <th>성명</th>
                <th>기관</th>
                <th>이메일</th>
                <th>휴대폰</th>
                <th>역할</th>
                <th>상태</th>
                <th>등록일</th>
                <th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="userTableBody">
              <tr><td colspan="9" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
        <div id="userPaging" class="d-flex justify-content-center py-3"></div>
      </div>
    </div>

  </div>
</div>

<!-- 등록/수정 모달 -->
<div class="modal fade" id="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="userModalLabel">사용자 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="modalUserId">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-semibold">사용자 ID <span class="text-danger">*</span></label>
            <input type="text" id="fUserId" class="form-control form-control-sm" placeholder="영문/숫자 조합">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">성명 <span class="text-danger">*</span></label>
            <input type="text" id="fUserNm" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">기관 <span class="text-danger">*</span></label>
            <select id="fOgnzId" class="form-select form-select-sm"></select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">이메일</label>
            <input type="email" id="fEmlAddr" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">휴대폰</label>
            <input type="text" id="fMblphnNo" class="form-control form-control-sm" placeholder="010-0000-0000">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">초기 비밀번호 <span class="text-danger" id="pwRequired">*</span></label>
            <input type="password" id="fPassword" class="form-control form-control-sm">
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-semibold">마스터 여부</label>
            <select id="fMasterYn" class="form-select form-select-sm">
              <option value="N">일반</option>
              <option value="Y">마스터</option>
            </select>
          </div>
        </div>
        <!-- 역할 배정 영역 (수정 시) -->
        <div id="roleSection" class="mt-3 d-none">
          <hr>
          <h6 class="small fw-bold text-muted">역할 배정</h6>
          <div class="d-flex gap-2 mb-2">
            <select id="roleSelect" class="form-select form-select-sm" style="width:200px;"></select>
            <button class="btn btn-sm btn-outline-primary" onclick="grantRole()">
              <i class="bi bi-plus-circle me-1"></i>부여
            </button>
          </div>
          <div id="roleList" class="d-flex flex-wrap gap-1"></div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveUser()">
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
var userModal = new bootstrap.Modal(document.getElementById('userModal'));
var allRoles = [];
var currentPage = 1;

$(function() {
  loadOgnzOptions();
  loadRoles();
  loadUsers(1);
});

function loadOgnzOptions() {
  $.get('/api/com/ognz').done(function(res) {
    var opts = '<option value="">전체</option>';
    var fOpts = '<option value="">선택</option>';
    (res.data || []).forEach(function(o) {
      opts  += '<option value="' + escHtml(o.ognzId) + '">' + escHtml(o.ognzNm) + '</option>';
      fOpts += '<option value="' + escHtml(o.ognzId) + '">' + escHtml(o.ognzNm) + '</option>';
    });
    $('#searchOgnzId').html(opts);
    $('#fOgnzId').html(fOpts);
  }).fail(function() { showErr('기관 목록 로드 실패'); });
}

function loadRoles() {
  $.get('/api/com/role').done(function(res) {
    allRoles = res.data || [];
    var opts = '<option value="">역할 선택</option>';
    allRoles.forEach(function(r) {
      opts += '<option value="' + escHtml(r.roleId) + '">' + escHtml(r.roleNm) + '</option>';
    });
    $('#roleSelect').html(opts);
  });
}

function loadUsers(page) {
  currentPage = page;
  var params = {
    userNm: $('#searchUserNm').val(),
    ognzId: $('#searchOgnzId').val(),
    lckYn: $('#searchLockYn').val(),
    page: page, pageSize: 15
  };
  $.get('/api/com/user', params).done(function(res) {
    renderUserTable(res.data);
    renderPaging(res.data, page);
  }).fail(function(xhr) {
    showErr('사용자 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText));
  });
}

function renderUserTable(page) {
  var rows = page && (page.rows || page.content || (Array.isArray(page) ? page : [])) || [];
  if (!rows.length) {
    $('#userTableBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  var html = '';
  rows.forEach(function(u) {
    var lockBadge = u.lckYn === 'Y'
      ? '<span class="badge bg-danger">잠금</span>'
      : '<span class="badge bg-success">정상</span>';
    html += '<tr>'
      + '<td class="ps-3 small text-muted">' + escHtml(u.userId) + '</td>'
      + '<td class="fw-semibold small">' + escHtml(u.userNm || '') + '</td>'
      + '<td class="small">' + escHtml(u.ognzNm || u.ognzId || '') + '</td>'
      + '<td class="small">' + escHtml(u.emlAddr || '') + '</td>'
      + '<td class="small">' + escHtml(u.mblphnNo || '') + '</td>'
      + '<td class="small">' + renderRoleBadges(u.roles) + '</td>'
      + '<td>' + lockBadge + '</td>'
      + '<td class="small text-muted">' + (u.frstRgtrDt || '').substring(0,10) + '</td>'
      + '<td class="text-center">'
      +   '<button class="btn btn-xs btn-outline-primary btn-sm py-0 px-2 me-1" onclick="openEditModal(\'' + escHtml(u.userId) + '\')"><i class="bi bi-pencil"></i></button>'
      +   (u.lckYn === 'Y' ? '<button class="btn btn-xs btn-outline-warning btn-sm py-0 px-2 me-1" onclick="unlockUser(\'' + escHtml(u.userId) + '\')"><i class="bi bi-unlock"></i></button>' : '')
      +   '<button class="btn btn-xs btn-outline-danger btn-sm py-0 px-2" onclick="deleteUser(\'' + escHtml(u.userId) + '\')"><i class="bi bi-trash"></i></button>'
      + '</td></tr>';
  });
  $('#userTableBody').html(html);
}

function renderRoleBadges(roles) {
  if (!roles || !roles.length) return '<span class="text-muted">-</span>';
  return roles.map(function(r) {
    return '<span class="badge bg-primary badge-role me-1">' + escHtml(r.roleNm || r.roleId) + '</span>';
  }).join('');
}

function renderPaging(page, current) {
  if (!page || !page.totalPages) { $('#userPaging').html(''); return; }
  var html = '<nav><ul class="pagination pagination-sm mb-0">';
  for (var i = 1; i <= page.totalPages; i++) {
    html += '<li class="page-item' + (i === current ? ' active' : '') + '">'
      + '<a class="page-link" href="#" onclick="loadUsers(' + i + ');return false;">' + i + '</a></li>';
  }
  html += '</ul></nav>';
  $('#userPaging').html(html);
}

function openCreateModal() {
  $('#userModalLabel').text('사용자 등록');
  $('#modalUserId').val('');
  $('#fUserId').val('').prop('disabled', false);
  $('#fUserNm,#fEmlAddr,#fMblphnNo,#fPassword').val('');
  $('#fOgnzId').val('');
  $('#fMasterYn').val('N');
  $('#pwRequired').show();
  $('#roleSection').addClass('d-none');
  userModal.show();
}

function openEditModal(userId) {
  $.get('/api/com/user/' + encodeURIComponent(userId)).done(function(res) {
    var u = res.data;
    $('#userModalLabel').text('사용자 수정');
    $('#modalUserId').val(u.userId);
    $('#fUserId').val(u.userId).prop('disabled', true);
    $('#fUserNm').val(u.userNm || '');
    $('#fOgnzId').val(u.ognzId || '');
    $('#fEmlAddr').val(u.emlAddr || '');
    $('#fMblphnNo').val(u.mblphnNo || '');
    $('#fPassword').val('');
    $('#fMasterYn').val(u.masterYn || 'N');
    $('#pwRequired').hide();
    loadUserRoles(u.userId);
    $('#roleSection').removeClass('d-none');
    userModal.show();
  }).fail(function(xhr) { showErr('사용자 조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function loadUserRoles(userId) {
  $.get('/api/com/user-role/user/' + encodeURIComponent(userId)).done(function(res) {
    var roles = res.data || [];
    var html = roles.length ? '' : '<span class="text-muted small">배정된 역할 없음</span>';
    roles.forEach(function(r) {
      html += '<span class="badge bg-primary badge-role">'
        + escHtml(r.roleNm || r.roleId)
        + ' <a href="#" class="text-white text-decoration-none" onclick="revokeRole(\'' + escHtml(userId) + '\',\'' + escHtml(r.roleId) + '\');return false;">&times;</a>'
        + '</span> ';
    });
    $('#roleList').html(html);
  });
}

function grantRole() {
  var userId = $('#modalUserId').val();
  var roleId = $('#roleSelect').val();
  if (!roleId) { IcasAlert.warning('역할을 선택하세요.'); return; }
  $.ajax({ url: '/api/com/user-role', type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({ userId: userId, roleId: roleId })
  }).done(function() { loadUserRoles(userId); })
    .fail(function(xhr) { showErr('역할 부여 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function revokeRole(userId, roleId) {
  if (!confirm('역할을 회수하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/user-role?userId=' + encodeURIComponent(userId) + '&roleId=' + encodeURIComponent(roleId), type: 'DELETE' })
    .done(function() { loadUserRoles(userId); })
    .fail(function(xhr) { showErr('역할 회수 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function saveUser() {
  var userId = $('#modalUserId').val();
  var isEdit = !!userId;
  var payload = {
    userId: $('#fUserId').val(),
    userNm: $('#fUserNm').val(),
    ognzId: $('#fOgnzId').val(),
    emlAddr: $('#fEmlAddr').val(),
    mblphnNo: $('#fMblphnNo').val(),
    masterYn: $('#fMasterYn').val(),
    password: $('#fPassword').val()
  };
  if (!payload.userNm) { IcasAlert.warning('성명을 입력하세요.'); return; }
  if (!isEdit && !payload.password) { IcasAlert.warning('비밀번호를 입력하세요.'); return; }
  var url = isEdit ? '/api/com/user/' + encodeURIComponent(userId) : '/api/com/user';
  var method = isEdit ? 'PUT' : 'POST';
  $.ajax({ url: url, type: method, contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function(res) {
      userModal.hide();
      loadUsers(currentPage);
    })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteUser(userId) {
  if (!confirm('사용자 [' + userId + ']를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/user/' + encodeURIComponent(userId), type: 'DELETE' })
    .done(function() { loadUsers(currentPage); })
    .fail(function(xhr) { showErr('삭제 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function unlockUser(userId) {
  if (!confirm('계정 잠금을 해제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/com/user/' + encodeURIComponent(userId) + '/unlock', type: 'POST' })
    .done(function() { loadUsers(currentPage); })
    .fail(function(xhr) { showErr('잠금 해제 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function escHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#x27;');
}
function showErr(msg) { IcasAlert.error('[오류] ' + msg); }
</script>
</body>
</html>
