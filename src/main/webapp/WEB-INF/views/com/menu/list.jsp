<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>메뉴 관리 &mdash; ICAS-CEMS</title>
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
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">메뉴 관리</h5>
        <small class="text-muted">시스템 메뉴 트리</small>
      </div>
      <div class="d-flex gap-2">
        <select id="filterSys" class="form-select form-select-sm" style="width:150px;">
          <option value="">전체 시스템</option>
          <option value="COM">COM (공통)</option>
          <option value="EMP">EMP</option>
          <option value="ER">ER</option>
          <option value="VR">VR</option>
          <option value="SAF">SAF</option>
          <option value="PTL">PTL</option>
        </select>
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="openMenuModal()">
          <i class="bi bi-plus-circle me-1"></i>메뉴 등록
        </button>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover table-sm mb-0">
          <thead class="page-header">
            <tr>
              <th class="ps-3">메뉴 ID</th>
              <th>시스템</th>
              <th>메뉴명</th>
              <th>상위 메뉴</th>
              <th class="text-end">순서</th>
              <th>연결 프로그램</th>
              <th>아이콘</th>
              <th class="text-center">관리</th>
            </tr>
          </thead>
          <tbody id="menuTableBody">
            <tr><td colspan="8" class="text-center py-4 text-muted">로딩 중...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</div>

<!-- 메뉴 등록/수정 모달 -->
<div class="modal fade" id="menuModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="menuModalTitle">메뉴 등록</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="menuIsEdit" value="0">
        <div class="row g-2">
          <div class="col-6">
            <label class="form-label small fw-semibold">메뉴 ID <span class="text-danger">*</span></label>
            <input type="text" id="menuId" class="form-control form-control-sm" maxlength="40">
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">시스템 <span class="text-danger">*</span></label>
            <select id="sysSeCd" class="form-select form-select-sm">
              <option value="COM">COM</option><option value="EMP">EMP</option>
              <option value="ER">ER</option><option value="VR">VR</option>
              <option value="SAF">SAF</option><option value="PTL">PTL</option>
            </select>
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">메뉴명 <span class="text-danger">*</span></label>
            <input type="text" id="menuNm" class="form-control form-control-sm" maxlength="60">
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">상위 메뉴 ID</label>
            <input type="text" id="upperMenuId" class="form-control form-control-sm" placeholder="비우면 루트" maxlength="40">
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">정렬 순서</label>
            <input type="number" id="menuOrd" class="form-control form-control-sm" value="100">
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">프로그램 ID</label>
            <input type="text" id="prgrmId" class="form-control form-control-sm" placeholder="폴더형이면 비움" maxlength="40">
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold">아이콘</label>
            <input type="text" id="iconNm" class="form-control form-control-sm" placeholder="bi-list" maxlength="40">
          </div>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveMenu()">저장</button>
      </div>
    </div>
  </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
function esc(s){ return window.IcasEsc ? IcasEsc.esc(s) : (s==null?'':String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')); }
function alertErr(m){ if(window.IcasAlert) IcasAlert.error(m); else IcasAlert.info(m); }
function alertOk(m){ if(window.IcasAlert) IcasAlert.success(m); else IcasAlert.info(m); }

function loadMenus(){
  var sys = $('#filterSys').val();
  var params = sys ? { sysSeCd: sys } : {};
  $.get('/api/com/menu', params).done(function(res){
    var list = (res && res.data) ? res.data : res;
    if (!Array.isArray(list)) list = (list && list.rows) ? list.rows : [];
    if (!list.length){ $('#menuTableBody').html('<tr><td colspan="8" class="text-center py-4 text-muted">데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function(m){
      html += '<tr>'
        + '<td class="ps-3 fw-semibold small">'+esc(m.menuId)+'</td>'
        + '<td class="small"><span class="badge bg-light text-dark border">'+esc(m.sysSeCd)+'</span></td>'
        + '<td class="small">'+esc(m.menuNm)+'</td>'
        + '<td class="small text-muted">'+esc(m.upperMenuId||'-')+'</td>'
        + '<td class="text-end small">'+(m.menuOrd||'-')+'</td>'
        + '<td class="small text-muted">'+esc(m.prgrmId||'-')+'</td>'
        + '<td class="small text-muted">'+esc(m.iconNm||'-')+'</td>'
        + '<td class="text-center"><button class="btn btn-xs btn-sm btn-outline-primary py-0 px-1" onclick="editMenu(\''+esc(m.menuId)+'\')"><i class="bi bi-pencil"></i></button> '
        + '<button class="btn btn-xs btn-sm btn-outline-danger py-0 px-1" onclick="delMenu(\''+esc(m.menuId)+'\')"><i class="bi bi-trash"></i></button></td>'
        + '</tr>';
    });
    $('#menuTableBody').html(html);
  }).fail(function(xhr){ alertErr('메뉴 조회 실패 (HTTP '+xhr.status+')'); });
}

function openMenuModal(){
  $('#menuIsEdit').val('0');
  $('#menuModalTitle').text('메뉴 등록');
  $('#menuId').val('').prop('readonly', false);
  $('#sysSeCd').val('COM'); $('#menuNm').val(''); $('#upperMenuId').val('');
  $('#menuOrd').val(100); $('#prgrmId').val(''); $('#iconNm').val('');
  new bootstrap.Modal('#menuModal').show();
}

function editMenu(id){
  $.get('/api/com/menu/'+encodeURIComponent(id)).done(function(res){
    var m = (res && res.data) ? res.data : res;
    $('#menuIsEdit').val('1');
    $('#menuModalTitle').text('메뉴 수정');
    $('#menuId').val(m.menuId).prop('readonly', true);
    $('#sysSeCd').val(m.sysSeCd); $('#menuNm').val(m.menuNm);
    $('#upperMenuId').val(m.upperMenuId||''); $('#menuOrd').val(m.menuOrd||100);
    $('#prgrmId').val(m.prgrmId||''); $('#iconNm').val(m.iconNm||'');
    new bootstrap.Modal('#menuModal').show();
  }).fail(function(xhr){ alertErr('메뉴 조회 실패 (HTTP '+xhr.status+')'); });
}

function saveMenu(){
  var payload = {
    menuId: $('#menuId').val().trim(),
    sysSeCd: $('#sysSeCd').val(),
    menuNm: $('#menuNm').val().trim(),
    upperMenuId: $('#upperMenuId').val().trim() || null,
    menuOrd: parseInt($('#menuOrd').val(), 10) || 100,
    prgrmId: $('#prgrmId').val().trim() || null,
    iconNm: $('#iconNm').val().trim() || null
  };
  if (!payload.menuId || !payload.menuNm){ alertErr('메뉴 ID와 메뉴명은 필수입니다.'); return; }
  var isEdit = $('#menuIsEdit').val() === '1';
  $.ajax({
    url: isEdit ? '/api/com/menu/'+encodeURIComponent(payload.menuId) : '/api/com/menu',
    type: isEdit ? 'PUT' : 'POST', contentType: 'application/json',
    data: JSON.stringify(payload)
  }).done(function(){
    alertOk('저장되었습니다.');
    bootstrap.Modal.getInstance(document.getElementById('menuModal')).hide();
    loadMenus();
  }).fail(function(xhr){ alertErr('저장 실패 (HTTP '+xhr.status+')'); });
}

function delMenu(id){
  if (!confirm('메뉴 '+id+'을(를) 삭제하시겠습니까?')) return;
  $.ajax({ url: '/api/com/menu/'+encodeURIComponent(id), type: 'DELETE' })
    .done(function(){ alertOk('삭제되었습니다.'); loadMenus(); })
    .fail(function(xhr){ alertErr('삭제 실패 (HTTP '+xhr.status+')'); });
}

$(function(){
  loadMenus();
  $('#filterSys').on('change', loadMenus);
});
</script>
</body>
</html>
