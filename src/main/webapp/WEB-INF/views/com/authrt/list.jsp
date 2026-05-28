<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>권한 관리 &mdash; ICAS-CEMS</title>
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
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">권한 관리</h5>
        <small class="text-muted">권한·역할·프로그램·메뉴 통합 관리 (MOLIT/KOTSA 전용)</small>
      </div>
      <button class="btn btn-sm text-white" style="background:var(--icas-primary);" id="btnCreate" onclick="openCreateModal()">
        <i class="bi bi-plus-circle me-1"></i>등록
      </button>
    </div>

    <!-- 탭 4개 -->
    <ul class="nav nav-tabs mb-3" id="authrtTab">
      <li class="nav-item"><a class="nav-link active" href="#" onclick="switchTab('authrt');return false;">권한</a></li>
      <li class="nav-item"><a class="nav-link" href="#" onclick="switchTab('role');return false;">역할</a></li>
      <li class="nav-item"><a class="nav-link" href="#" onclick="switchTab('prgrm');return false;">프로그램</a></li>
      <li class="nav-item"><a class="nav-link" href="#" onclick="switchTab('menu');return false;">메뉴</a></li>
    </ul>

    <!-- 권한 탭 -->
    <div id="tab-authrt">
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">권한ID</th><th>권한명</th><th>설명</th><th>등록일</th><th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="authrtTableBody">
              <tr><td colspan="5" class="text-center py-4 text-muted">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 역할 탭 -->
    <div id="tab-role" class="d-none">
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">역할ID</th><th>역할명</th><th>설명</th><th>등록일</th><th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="roleTableBody">
              <tr><td colspan="5" class="text-center py-4 text-muted">로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 프로그램 탭 -->
    <div id="tab-prgrm" class="d-none">
      <div class="card border-0 shadow-sm mb-3">
        <div class="card-body py-2">
          <div class="row g-2 align-items-end">
            <div class="col-md-3">
              <label class="form-label small mb-1">시스템구분</label>
              <select id="prgrmSysSeCd" class="form-select form-select-sm">
                <option value="">전체</option>
                <option value="COM">공통(COM)</option>
                <option value="EMP">항공사(EMP)</option>
                <option value="VR">검증(VR)</option>
              </select>
            </div>
            <div class="col-md-2">
              <button class="btn btn-sm btn-primary w-100" onclick="loadPrgrm()">
                <i class="bi bi-search me-1"></i>조회
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">프로그램ID</th><th>프로그램명</th><th>URL</th><th>시스템</th><th>등록일</th><th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="prgrmTableBody">
              <tr><td colspan="6" class="text-center py-4 text-muted">로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 메뉴 탭 -->
    <div id="tab-menu" class="d-none">
      <div class="card border-0 shadow-sm mb-3">
        <div class="card-body py-2">
          <div class="row g-2 align-items-end">
            <div class="col-md-3">
              <label class="form-label small mb-1">시스템구분</label>
              <select id="menuSysSeCd" class="form-select form-select-sm">
                <option value="">전체</option>
                <option value="COM">공통(COM)</option>
              </select>
            </div>
            <div class="col-md-2">
              <button class="btn btn-sm btn-primary w-100" onclick="loadMenu()">
                <i class="bi bi-search me-1"></i>조회
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <table class="table table-hover table-sm mb-0">
            <thead class="page-header">
              <tr>
                <th class="ps-3">메뉴ID</th><th>메뉴명</th><th>상위메뉴</th><th>URL</th><th>순서</th><th>등록일</th><th class="text-center">관리</th>
              </tr>
            </thead>
            <tbody id="menuTableBody">
              <tr><td colspan="7" class="text-center py-4 text-muted">로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
</div>

<!-- 공통 등록/수정 모달 (탭별 동적 폼) -->
<div class="modal fade" id="authrtModal" tabindex="-1" aria-labelledby="authrtModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);">
        <h6 class="modal-title text-white fw-bold" id="authrtModalLabel">등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body" id="authrtModalBody"><!-- 동적 폼 --></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveItem()">
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
var authrtModal = new bootstrap.Modal(document.getElementById('authrtModal'));
var curTab = 'authrt';
var editId = null;

$(function() { loadAuthrt(); });

function switchTab(tab) {
  curTab = tab;
  $('#tab-authrt,#tab-role,#tab-prgrm,#tab-menu').addClass('d-none');
  $('#tab-' + tab).removeClass('d-none');
  $('#authrtTab .nav-link').removeClass('active');
  var idx = ['authrt','role','prgrm','menu'].indexOf(tab);
  $('#authrtTab .nav-link').eq(idx).addClass('active');
  if (tab === 'authrt') loadAuthrt();
  else if (tab === 'role') loadRole();
  else if (tab === 'prgrm') loadPrgrm();
  else if (tab === 'menu') loadMenu();
}

/* ── 권한 ── */
function loadAuthrt() {
  $.get('/api/com/authrt').done(function(res) { renderList('authrtTableBody', res.data || [], 5, ['authrtId','authrtNm','authrtDc','frstRgtrDt'], 'authrt'); })
    .fail(function(xhr) { showErr('권한 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

/* ── 역할 ── */
function loadRole() {
  $.get('/api/com/role').done(function(res) { renderList('roleTableBody', res.data || [], 5, ['roleId','roleNm','roleDc','frstRgtrDt'], 'role'); })
    .fail(function(xhr) { showErr('역할 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

/* ── 프로그램 ── */
function loadPrgrm() {
  $.get('/api/com/prgrm', { sysSeCd: $('#prgrmSysSeCd').val() }).done(function(res) {
    renderList('prgrmTableBody', res.data || [], 6, ['prgrmId','prgrmNm','prgrmUrl','sysSeCd','frstRgtrDt'], 'prgrm');
  }).fail(function(xhr) { showErr('프로그램 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

/* ── 메뉴 ── */
function loadMenu() {
  $.get('/api/com/menu', { sysSeCd: $('#menuSysSeCd').val() }).done(function(res) {
    var rows = res.data || [];
    if (!rows.length) {
      $('#menuTableBody').html('<tr><td colspan="7" class="text-center py-4 text-muted small">데이터가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    rows.forEach(function(m) {
      html += '<tr>'
        + '<td class="ps-3 small text-muted">' + escHtml(m.menuId) + '</td>'
        + '<td class="small">' + escHtml(m.menuNm || '') + '</td>'
        + '<td class="small">' + escHtml(m.upprMenuId || '-') + '</td>'
        + '<td class="small">' + escHtml(m.menuUrl || '-') + '</td>'
        + '<td class="small text-end">' + (m.sortOrdr != null ? m.sortOrdr : '-') + '</td>'
        + '<td class="small text-muted">' + (m.frstRgtrDt || '').substring(0,10) + '</td>'
        + '<td class="text-center">'
        +   '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openEditModal(\'' + escHtml(m.menuId) + '\')"><i class="bi bi-pencil"></i></button>'
        +   '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteItem(\'' + escHtml(m.menuId) + '\')"><i class="bi bi-trash"></i></button>'
        + '</td></tr>';
    });
    $('#menuTableBody').html(html);
  }).fail(function(xhr) { showErr('메뉴 목록 로드 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

/* ── 공통 렌더 ── */
function renderList(tbodyId, rows, colspan, fields, tab) {
  if (!rows.length) {
    $('#' + tbodyId).html('<tr><td colspan="' + colspan + '" class="text-center py-4 text-muted small">데이터가 없습니다.</td></tr>');
    return;
  }
  var html = '';
  var idField = fields[0];
  rows.forEach(function(r) {
    html += '<tr>';
    fields.forEach(function(f) {
      var val = r[f] != null ? r[f] : '-';
      if (f.indexOf('Dt') > -1 || f === 'frstRgtrDt') val = String(val).substring(0,10);
      html += '<td class="' + (fields.indexOf(f) === 0 ? 'ps-3 small text-muted' : 'small') + '">' + escHtml(val) + '</td>';
    });
    html += '<td class="text-center">'
      + '<button class="btn btn-sm btn-outline-primary py-0 px-2 me-1" onclick="openEditModal(\'' + escHtml(r[idField]) + '\')"><i class="bi bi-pencil"></i></button>'
      + '<button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteItem(\'' + escHtml(r[idField]) + '\')"><i class="bi bi-trash"></i></button>'
      + '</td></tr>';
  });
  $('#' + tbodyId).html(html);
}

/* ── 모달 동적 폼 ── */
var formMap = {
  authrt: [
    {id:'fAuthrtId', label:'권한ID', required:true},
    {id:'fAuthrtNm', label:'권한명', required:true},
    {id:'fAuthrtDc', label:'설명', type:'textarea'}
  ],
  role: [
    {id:'fRoleId', label:'역할ID', required:true},
    {id:'fRoleNm', label:'역할명', required:true},
    {id:'fRoleDc', label:'설명', type:'textarea'}
  ],
  prgrm: [
    {id:'fPrgrmId', label:'프로그램ID', required:true},
    {id:'fPrgrmNm', label:'프로그램명', required:true},
    {id:'fPrgrmUrl', label:'URL'},
    {id:'fSysSeCd', label:'시스템구분'}
  ],
  menu: [
    {id:'fMenuId', label:'메뉴ID', required:true},
    {id:'fMenuNm', label:'메뉴명', required:true},
    {id:'fMenuUrl', label:'URL'},
    {id:'fUpprMenuId', label:'상위메뉴ID'},
    {id:'fSortOrdr', label:'정렬순서', type:'number'}
  ]
};

function buildForm(tab, data) {
  var fields = formMap[tab];
  var html = '<div class="row g-3">';
  fields.forEach(function(f) {
    html += '<div class="col-md-6"><label class="form-label small fw-semibold">'
      + escHtml(f.label) + (f.required ? ' <span class="text-danger">*</span>' : '') + '</label>';
    if (f.type === 'textarea') {
      html += '<textarea id="' + f.id + '" class="form-control form-control-sm" rows="2">'
        + (data && data[f.id.replace('f','')] != null ? escHtml(data[f.id.substring(1,2).toLowerCase() + f.id.substring(2)]) : '') + '</textarea>';
    } else {
      html += '<input type="' + (f.type || 'text') + '" id="' + f.id + '" class="form-control form-control-sm">';
    }
    html += '</div>';
  });
  html += '</div>';
  return html;
}

function openCreateModal() {
  editId = null;
  $('#authrtModalLabel').text(({authrt:'권한', role:'역할', prgrm:'프로그램', menu:'메뉴'}[curTab] || '') + ' 등록');
  $('#authrtModalBody').html(buildForm(curTab, null));
  authrtModal.show();
}

function openEditModal(id) {
  editId = id;
  var urlMap = { authrt:'/api/com/authrt/', role:'/api/com/role/', prgrm:'/api/com/prgrm/', menu:'/api/com/menu/' };
  $.get(urlMap[curTab] + encodeURIComponent(id)).done(function(res) {
    var d = res.data;
    $('#authrtModalLabel').text(({authrt:'권한', role:'역할', prgrm:'프로그램', menu:'메뉴'}[curTab] || '') + ' 수정');
    $('#authrtModalBody').html(buildForm(curTab, d));
    // 필드 채우기
    var fMap = {
      authrt: {fAuthrtId:'authrtId', fAuthrtNm:'authrtNm', fAuthrtDc:'authrtDc'},
      role:   {fRoleId:'roleId', fRoleNm:'roleNm', fRoleDc:'roleDc'},
      prgrm:  {fPrgrmId:'prgrmId', fPrgrmNm:'prgrmNm', fPrgrmUrl:'prgrmUrl', fSysSeCd:'sysSeCd'},
      menu:   {fMenuId:'menuId', fMenuNm:'menuNm', fMenuUrl:'menuUrl', fUpprMenuId:'upprMenuId', fSortOrdr:'sortOrdr'}
    };
    Object.entries(fMap[curTab]).forEach(function(e) {
      var el = document.getElementById(e[0]);
      if (el) el.value = d[e[1]] != null ? d[e[1]] : '';
    });
    var idField = Object.keys(fMap[curTab])[0];
    document.getElementById(idField).disabled = true;
    authrtModal.show();
  }).fail(function(xhr) { showErr('조회 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function saveItem() {
  var urlMap = { authrt:'/api/com/authrt', role:'/api/com/role', prgrm:'/api/com/prgrm', menu:'/api/com/menu' };
  var payloadMap = {
    authrt: function(){ return {authrtId:$('#fAuthrtId').val(), authrtNm:$('#fAuthrtNm').val(), authrtDc:$('#fAuthrtDc').val()}; },
    role:   function(){ return {roleId:$('#fRoleId').val(), roleNm:$('#fRoleNm').val(), roleDc:$('#fRoleDc').val()}; },
    prgrm:  function(){ return {prgrmId:$('#fPrgrmId').val(), prgrmNm:$('#fPrgrmNm').val(), prgrmUrl:$('#fPrgrmUrl').val(), sysSeCd:$('#fSysSeCd').val()}; },
    menu:   function(){ return {menuId:$('#fMenuId').val(), menuNm:$('#fMenuNm').val(), menuUrl:$('#fMenuUrl').val(), upprMenuId:$('#fUpprMenuId').val(), sortOrdr:$('#fSortOrdr').val()||null}; }
  };
  var payload = payloadMap[curTab]();
  var url = editId ? urlMap[curTab] + '/' + encodeURIComponent(editId) : urlMap[curTab];
  $.ajax({ url: url, type: editId ? 'PUT' : 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function() {
      authrtModal.hide();
      if (curTab==='authrt') loadAuthrt();
      else if (curTab==='role') loadRole();
      else if (curTab==='prgrm') loadPrgrm();
      else if (curTab==='menu') loadMenu();
    })
    .fail(function(xhr) { showErr('저장 실패: ' + (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)); });
}

function deleteItem(id) {
  if (!confirm('[' + id + ']을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  var urlMap = { authrt:'/api/com/authrt/', role:'/api/com/role/', prgrm:'/api/com/prgrm/', menu:'/api/com/menu/' };
  $.ajax({ url: urlMap[curTab] + encodeURIComponent(id), type: 'DELETE' })
    .done(function() {
      if (curTab==='authrt') loadAuthrt();
      else if (curTab==='role') loadRole();
      else if (curTab==='prgrm') loadPrgrm();
      else if (curTab==='menu') loadMenu();
    })
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
