<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>역할 관리 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header th { background: var(--icas-primary); color: #fff; font-weight: 500; font-size: 0.82rem; }
.row-sel { cursor: pointer; }
.row-sel.selected { background: #e8eeff !important; }
.row-sel:hover { background: #f8f9ff; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <div class="d-flex align-items-center mb-3">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">역할 관리</h5>
        <small class="text-muted">역할 정의 &rarr; 사용자 매핑</small>
      </div>
    </div>

    <div class="row g-3">

      <!-- 좌: 역할 목록 -->
      <div class="col-md-5">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2">
            <span class="fw-semibold small" style="color:var(--icas-primary);">역할 목록</span>
            <button class="btn btn-sm text-white py-0 px-2" style="background:var(--icas-primary);" onclick="openRoleModal()">
              <i class="bi bi-plus-circle me-1"></i>역할 등록
            </button>
          </div>
          <div class="card-body p-0">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">역할 ID</th><th>역할명</th><th>허용 기관</th><th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="roleTableBody">
                <tr><td colspan="4" class="text-center py-4 text-muted">로딩 중...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 우: 선택 역할의 매핑된 사용자 -->
      <div class="col-md-7">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2">
            <span class="fw-semibold small" style="color:var(--icas-primary);">
              매핑된 사용자 <span id="selRoleLabel" class="text-muted">(역할 선택)</span>
            </span>
            <button class="btn btn-sm btn-outline-primary py-0 px-2" id="btnAddUserRole" disabled onclick="openUserRoleModal()">
              <i class="bi bi-person-plus me-1"></i>사용자 매핑
            </button>
          </div>
          <div class="card-body p-0">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">사용자 ID</th><th>사용자명</th><th>조직</th><th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="urmTableBody">
                <tr><td colspan="4" class="text-center py-4 text-muted">역할을 선택하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</div>

<!-- 역할 등록/수정 모달 -->
<div class="modal fade" id="roleModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="roleModalTitle">역할 등록</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="roleIsEdit" value="0">
        <div class="mb-2">
          <label class="form-label small fw-semibold">역할 ID <span class="text-danger">*</span></label>
          <input type="text" id="roleId" class="form-control form-control-sm" placeholder="예: ROLE_ADMIN" maxlength="40">
        </div>
        <div class="mb-2">
          <label class="form-label small fw-semibold">역할명 <span class="text-danger">*</span></label>
          <input type="text" id="roleNm" class="form-control form-control-sm" maxlength="60">
        </div>
        <div class="mb-2">
          <label class="form-label small fw-semibold">허용 기관 (쉼표 구분) <span class="text-danger">*</span></label>
          <input type="text" id="ognzSeCdAllowed" class="form-control form-control-sm" placeholder="MOLIT,KOTSA,AIRLINE,VERIFIER" maxlength="60">
        </div>
        <div class="mb-2">
          <label class="form-label small fw-semibold">설명</label>
          <textarea id="roleDesc" class="form-control form-control-sm" rows="2" maxlength="200"></textarea>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveRole()">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- 사용자-역할 매핑 모달 -->
<div class="modal fade" id="userRoleModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">사용자 매핑</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-2">
          <label class="form-label small fw-semibold">사용자 ID <span class="text-danger">*</span></label>
          <input type="text" id="urmUserId" class="form-control form-control-sm" placeholder="예: admin01">
          <div class="form-text small">시스템에 존재하는 사용자 ID</div>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveUserRole()">매핑</button>
      </div>
    </div>
  </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
var selectedRoleId = null;

function esc(s){ return window.IcasEsc ? IcasEsc.esc(s) : (s==null?'':String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')); }
function alertErr(msg){ if (window.IcasAlert) IcasAlert.error(msg); else IcasAlert.info(msg); }
function alertOk(msg){ if (window.IcasAlert) IcasAlert.success(msg); else IcasAlert.info(msg); }

function loadRoles(){
  $.get('/api/com/role').done(function(res){
    var list = (res && res.data) ? res.data : res;
    if (!Array.isArray(list)) list = [];
    if (!list.length){ $('#roleTableBody').html('<tr><td colspan="4" class="text-center py-4 text-muted">데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function(r){
      html += '<tr class="row-sel" data-roleid="'+esc(r.roleId)+'" data-rolenm="'+esc(r.roleNm)+'">'
        + '<td class="ps-3 fw-semibold small">'+esc(r.roleId)+'</td>'
        + '<td class="small">'+esc(r.roleNm)+'</td>'
        + '<td class="small text-muted">'+esc(r.ognzSeCdAllowed||'')+'</td>'
        + '<td class="text-center"><button class="btn btn-xs btn-sm btn-outline-primary py-0 px-1" onclick="event.stopPropagation();editRole(\''+esc(r.roleId)+'\')"><i class="bi bi-pencil"></i></button> '
        + '<button class="btn btn-xs btn-sm btn-outline-danger py-0 px-1" onclick="event.stopPropagation();delRole(\''+esc(r.roleId)+'\')"><i class="bi bi-trash"></i></button></td>'
        + '</tr>';
    });
    $('#roleTableBody').html(html);
    $('.row-sel').on('click', function(){
      $('.row-sel').removeClass('selected');
      $(this).addClass('selected');
      selectedRoleId = $(this).data('roleid');
      $('#selRoleLabel').text('('+$(this).data('rolenm')+')').removeClass('text-muted');
      $('#btnAddUserRole').prop('disabled', false);
      loadUserRoles(selectedRoleId);
    });
  }).fail(function(xhr){ alertErr('역할 목록 조회 실패 (HTTP '+xhr.status+')'); });
}

function openRoleModal(){
  $('#roleIsEdit').val('0');
  $('#roleModalTitle').text('역할 등록');
  $('#roleId').val('').prop('readonly', false);
  $('#roleNm').val(''); $('#ognzSeCdAllowed').val('MOLIT,KOTSA,AIRLINE,VERIFIER'); $('#roleDesc').val('');
  new bootstrap.Modal('#roleModal').show();
}

function editRole(roleId){
  $.get('/api/com/role/'+encodeURIComponent(roleId)).done(function(res){
    var r = (res && res.data) ? res.data : res;
    $('#roleIsEdit').val('1');
    $('#roleModalTitle').text('역할 수정');
    $('#roleId').val(r.roleId).prop('readonly', true);
    $('#roleNm').val(r.roleNm); $('#ognzSeCdAllowed').val(r.ognzSeCdAllowed||''); $('#roleDesc').val(r.roleDesc||'');
    new bootstrap.Modal('#roleModal').show();
  }).fail(function(xhr){ alertErr('역할 조회 실패 (HTTP '+xhr.status+')'); });
}

function saveRole(){
  var payload = {
    roleId: $('#roleId').val().trim(),
    roleNm: $('#roleNm').val().trim(),
    ognzSeCdAllowed: $('#ognzSeCdAllowed').val().trim(),
    roleDesc: $('#roleDesc').val().trim()
  };
  if (!payload.roleId || !payload.roleNm){ alertErr('역할 ID와 역할명은 필수입니다.'); return; }
  var isEdit = $('#roleIsEdit').val() === '1';
  $.ajax({
    url: isEdit ? '/api/com/role/'+encodeURIComponent(payload.roleId) : '/api/com/role',
    type: isEdit ? 'PUT' : 'POST',
    contentType: 'application/json',
    data: JSON.stringify(payload)
  }).done(function(){
    alertOk('저장되었습니다.');
    bootstrap.Modal.getInstance(document.getElementById('roleModal')).hide();
    loadRoles();
  }).fail(function(xhr){ alertErr('저장 실패 (HTTP '+xhr.status+')'); });
}

function delRole(roleId){
  if (!confirm('역할 '+roleId+'을(를) 삭제하시겠습니까?')) return;
  $.ajax({ url: '/api/com/role/'+encodeURIComponent(roleId), type: 'DELETE' })
    .done(function(){ alertOk('삭제되었습니다.'); loadRoles(); })
    .fail(function(xhr){ alertErr('삭제 실패 (HTTP '+xhr.status+')'); });
}

function loadUserRoles(roleId){
  $('#urmTableBody').html('<tr><td colspan="4" class="text-center py-4 text-muted">로딩 중...</td></tr>');
  $.get('/api/com/user-role', { roleId: roleId }).done(function(res){
    var list = (res && res.data) ? res.data : res;
    if (!Array.isArray(list)) list = (list && list.rows) ? list.rows : [];
    if (!list.length){ $('#urmTableBody').html('<tr><td colspan="4" class="text-center py-4 text-muted">매핑된 사용자가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function(u){
      html += '<tr>'
        + '<td class="ps-3 fw-semibold small">'+esc(u.userId)+'</td>'
        + '<td class="small">'+esc(u.userNm||'')+'</td>'
        + '<td class="small text-muted">'+esc(u.ognzId||'')+'</td>'
        + '<td class="text-center"><button class="btn btn-xs btn-sm btn-outline-danger py-0 px-1" onclick="delUserRole(\''+esc(u.userId)+'\')"><i class="bi bi-x-lg"></i></button></td>'
        + '</tr>';
    });
    $('#urmTableBody').html(html);
  }).fail(function(){ $('#urmTableBody').html('<tr><td colspan="4" class="text-center py-4 text-muted">매핑된 사용자가 없습니다.</td></tr>'); });
}

function openUserRoleModal(){
  if (!selectedRoleId){ alertErr('역할을 먼저 선택하세요.'); return; }
  $('#urmUserId').val('');
  new bootstrap.Modal('#userRoleModal').show();
}

function saveUserRole(){
  var userId = $('#urmUserId').val().trim();
  if (!userId){ alertErr('사용자 ID를 입력하세요.'); return; }
  $.ajax({
    url: '/api/com/user-role', type: 'POST', contentType: 'application/json',
    data: JSON.stringify({ userId: userId, roleId: selectedRoleId })
  }).done(function(){
    alertOk('매핑되었습니다.');
    bootstrap.Modal.getInstance(document.getElementById('userRoleModal')).hide();
    loadUserRoles(selectedRoleId);
  }).fail(function(xhr){ alertErr('매핑 실패 (HTTP '+xhr.status+')'); });
}

function delUserRole(userId){
  if (!confirm('매핑을 해제하시겠습니까?')) return;
  $.ajax({
    url: '/api/com/user-role', type: 'DELETE', contentType: 'application/json',
    data: JSON.stringify({ userId: userId, roleId: selectedRoleId })
  }).done(function(){ alertOk('해제되었습니다.'); loadUserRoles(selectedRoleId); })
    .fail(function(xhr){ alertErr('해제 실패 (HTTP '+xhr.status+')'); });
}

$(function(){ loadRoles(); });
</script>
</body>
</html>
