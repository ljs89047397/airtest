<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>프로그램 관리 &mdash; ICAS-CEMS</title>
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
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">프로그램 관리</h5>
        <small class="text-muted">프로그램 정의 &rarr; 권한 매핑</small>
      </div>
    </div>

    <div class="row g-3">

      <!-- 좌: 프로그램 목록 -->
      <div class="col-md-7">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2">
            <span class="fw-semibold small" style="color:var(--icas-primary);">프로그램 목록</span>
            <div class="d-flex gap-2">
              <select id="filterSys" class="form-select form-select-sm" style="width:130px;">
                <option value="">전체 시스템</option>
                <option value="COM">COM</option><option value="EMP">EMP</option>
                <option value="ER">ER</option><option value="VR">VR</option>
                <option value="SAF">SAF</option><option value="PTL">PTL</option>
              </select>
              <button class="btn btn-sm text-white py-0 px-2" style="background:var(--icas-primary);" onclick="openPrgrmModal()">
                <i class="bi bi-plus-circle me-1"></i>프로그램 등록
              </button>
            </div>
          </div>
          <div class="card-body p-0">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">프로그램 ID</th><th>시스템</th><th>프로그램명</th>
                  <th>화면 URL</th><th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="prgrmTableBody">
                <tr><td colspan="5" class="text-center py-4 text-muted">로딩 중...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 우: 권한 매핑 -->
      <div class="col-md-5">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2">
            <span class="fw-semibold small" style="color:var(--icas-primary);">
              권한 매핑 <span id="selPrgrmLabel" class="text-muted">(프로그램 선택)</span>
            </span>
            <button class="btn btn-sm btn-outline-primary py-0 px-2" id="btnAddAuthrt" disabled onclick="openAuthrtModal()">
              <i class="bi bi-shield-plus me-1"></i>권한 매핑
            </button>
          </div>
          <div class="card-body p-0">
            <table class="table table-hover table-sm mb-0">
              <thead class="page-header">
                <tr>
                  <th class="ps-3">권한 ID</th><th>권한명</th><th class="text-center">관리</th>
                </tr>
              </thead>
              <tbody id="apmTableBody">
                <tr><td colspan="3" class="text-center py-4 text-muted">프로그램을 선택하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</div>

<!-- 프로그램 등록/수정 모달 -->
<div class="modal fade" id="prgrmModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="prgrmModalTitle">프로그램 등록</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="prgrmIsEdit" value="0">
        <div class="row g-2">
          <div class="col-6">
            <label class="form-label small fw-semibold">프로그램 ID <span class="text-danger">*</span></label>
            <input type="text" id="prgrmId" class="form-control form-control-sm" maxlength="40">
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
            <label class="form-label small fw-semibold">프로그램명 <span class="text-danger">*</span></label>
            <input type="text" id="prgrmNm" class="form-control form-control-sm" maxlength="60">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">화면 URL</label>
            <input type="text" id="prgrmUrl" class="form-control form-control-sm" placeholder="/emp/plan" maxlength="100">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">API 경로 접두어</label>
            <input type="text" id="apiPathPrefix" class="form-control form-control-sm" placeholder="/api/emp/plan" maxlength="100">
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">설명</label>
            <textarea id="prgrmDesc" class="form-control form-control-sm" rows="2" maxlength="200"></textarea>
          </div>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="savePrgrm()">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- 권한 매핑 모달 -->
<div class="modal fade" id="authrtModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">권한 매핑</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-2">
          <label class="form-label small fw-semibold">권한 ID <span class="text-danger">*</span></label>
          <input type="text" id="apmAuthrtId" class="form-control form-control-sm" placeholder="예: AUTHRT_ADMIN">
        </div>
      </div>
      <div class="modal-footer py-2">
        <button class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button class="btn btn-sm text-white" style="background:var(--icas-primary);" onclick="saveAuthrtMap()">매핑</button>
      </div>
    </div>
  </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
var selectedPrgrmId = null;

function esc(s){ return window.IcasEsc ? IcasEsc.esc(s) : (s==null?'':String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')); }
function alertErr(m){ if(window.IcasAlert) IcasAlert.error(m); else IcasAlert.info(m); }
function alertOk(m){ if(window.IcasAlert) IcasAlert.success(m); else IcasAlert.info(m); }

function loadPrgrms(){
  var sys = $('#filterSys').val();
  var params = sys ? { sysSeCd: sys } : {};
  $.get('/api/com/prgrm', params).done(function(res){
    var list = (res && res.data) ? res.data : res;
    if (!Array.isArray(list)) list = (list && list.rows) ? list.rows : [];
    if (!list.length){ $('#prgrmTableBody').html('<tr><td colspan="5" class="text-center py-4 text-muted">데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function(p){
      html += '<tr class="row-sel" data-prgrmid="'+esc(p.prgrmId)+'" data-prgrmnm="'+esc(p.prgrmNm)+'">'
        + '<td class="ps-3 fw-semibold small">'+esc(p.prgrmId)+'</td>'
        + '<td class="small"><span class="badge bg-light text-dark border">'+esc(p.sysSeCd)+'</span></td>'
        + '<td class="small">'+esc(p.prgrmNm)+'</td>'
        + '<td class="small text-muted">'+esc(p.prgrmUrl||'-')+'</td>'
        + '<td class="text-center"><button class="btn btn-xs btn-sm btn-outline-primary py-0 px-1" onclick="event.stopPropagation();editPrgrm(\''+esc(p.prgrmId)+'\')"><i class="bi bi-pencil"></i></button> '
        + '<button class="btn btn-xs btn-sm btn-outline-danger py-0 px-1" onclick="event.stopPropagation();delPrgrm(\''+esc(p.prgrmId)+'\')"><i class="bi bi-trash"></i></button></td>'
        + '</tr>';
    });
    $('#prgrmTableBody').html(html);
    $('.row-sel').on('click', function(){
      $('.row-sel').removeClass('selected');
      $(this).addClass('selected');
      selectedPrgrmId = $(this).data('prgrmid');
      $('#selPrgrmLabel').text('('+$(this).data('prgrmnm')+')').removeClass('text-muted');
      $('#btnAddAuthrt').prop('disabled', false);
      loadAuthrtMap(selectedPrgrmId);
    });
  }).fail(function(xhr){ alertErr('프로그램 조회 실패 (HTTP '+xhr.status+')'); });
}

function openPrgrmModal(){
  $('#prgrmIsEdit').val('0');
  $('#prgrmModalTitle').text('프로그램 등록');
  $('#prgrmId').val('').prop('readonly', false);
  $('#sysSeCd').val('COM'); $('#prgrmNm').val(''); $('#prgrmUrl').val('');
  $('#apiPathPrefix').val(''); $('#prgrmDesc').val('');
  new bootstrap.Modal('#prgrmModal').show();
}

function editPrgrm(id){
  $.get('/api/com/prgrm/'+encodeURIComponent(id)).done(function(res){
    var p = (res && res.data) ? res.data : res;
    $('#prgrmIsEdit').val('1');
    $('#prgrmModalTitle').text('프로그램 수정');
    $('#prgrmId').val(p.prgrmId).prop('readonly', true);
    $('#sysSeCd').val(p.sysSeCd); $('#prgrmNm').val(p.prgrmNm);
    $('#prgrmUrl').val(p.prgrmUrl||''); $('#apiPathPrefix').val(p.apiPathPrefix||'');
    $('#prgrmDesc').val(p.prgrmDesc||'');
    new bootstrap.Modal('#prgrmModal').show();
  }).fail(function(xhr){ alertErr('프로그램 조회 실패 (HTTP '+xhr.status+')'); });
}

function savePrgrm(){
  var payload = {
    prgrmId: $('#prgrmId').val().trim(),
    sysSeCd: $('#sysSeCd').val(),
    prgrmNm: $('#prgrmNm').val().trim(),
    prgrmUrl: $('#prgrmUrl').val().trim() || null,
    apiPathPrefix: $('#apiPathPrefix').val().trim() || null,
    prgrmDesc: $('#prgrmDesc').val().trim() || null
  };
  if (!payload.prgrmId || !payload.prgrmNm){ alertErr('프로그램 ID와 프로그램명은 필수입니다.'); return; }
  var isEdit = $('#prgrmIsEdit').val() === '1';
  $.ajax({
    url: isEdit ? '/api/com/prgrm/'+encodeURIComponent(payload.prgrmId) : '/api/com/prgrm',
    type: isEdit ? 'PUT' : 'POST', contentType: 'application/json',
    data: JSON.stringify(payload)
  }).done(function(){
    alertOk('저장되었습니다.');
    bootstrap.Modal.getInstance(document.getElementById('prgrmModal')).hide();
    loadPrgrms();
  }).fail(function(xhr){ alertErr('저장 실패 (HTTP '+xhr.status+')'); });
}

function delPrgrm(id){
  if (!confirm('프로그램 '+id+'을(를) 삭제하시겠습니까?')) return;
  $.ajax({ url: '/api/com/prgrm/'+encodeURIComponent(id), type: 'DELETE' })
    .done(function(){ alertOk('삭제되었습니다.'); loadPrgrms(); })
    .fail(function(xhr){ alertErr('삭제 실패 (HTTP '+xhr.status+')'); });
}

function loadAuthrtMap(prgrmId){
  $('#apmTableBody').html('<tr><td colspan="3" class="text-center py-4 text-muted">로딩 중...</td></tr>');
  $.get('/api/com/authrt-prgrm/prgrm/'+encodeURIComponent(prgrmId)).done(function(res){
    var list = (res && res.data) ? res.data : res;
    if (!Array.isArray(list)) list = (list && list.rows) ? list.rows : [];
    if (!list.length){ $('#apmTableBody').html('<tr><td colspan="3" class="text-center py-4 text-muted">매핑된 권한이 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function(a){
      html += '<tr>'
        + '<td class="ps-3 fw-semibold small">'+esc(a.authrtId)+'</td>'
        + '<td class="small">'+esc(a.authrtNm||'')+'</td>'
        + '<td class="text-center"><button class="btn btn-xs btn-sm btn-outline-danger py-0 px-1" onclick="delAuthrtMap(\''+esc(a.authrtId)+'\')"><i class="bi bi-x-lg"></i></button></td>'
        + '</tr>';
    });
    $('#apmTableBody').html(html);
  }).fail(function(){ $('#apmTableBody').html('<tr><td colspan="3" class="text-center py-4 text-muted">매핑된 권한이 없습니다.</td></tr>'); });
}

function openAuthrtModal(){
  if (!selectedPrgrmId){ alertErr('프로그램을 먼저 선택하세요.'); return; }
  $('#apmAuthrtId').val('');
  new bootstrap.Modal('#authrtModal').show();
}

function saveAuthrtMap(){
  var authrtId = $('#apmAuthrtId').val().trim();
  if (!authrtId){ alertErr('권한 ID를 입력하세요.'); return; }
  $.ajax({
    url: '/api/com/authrt-prgrm', type: 'POST', contentType: 'application/json',
    data: JSON.stringify({ authrtId: authrtId, prgrmId: selectedPrgrmId })
  }).done(function(){
    alertOk('매핑되었습니다.');
    bootstrap.Modal.getInstance(document.getElementById('authrtModal')).hide();
    loadAuthrtMap(selectedPrgrmId);
  }).fail(function(xhr){ alertErr('매핑 실패 (HTTP '+xhr.status+')'); });
}

function delAuthrtMap(authrtId){
  if (!confirm('매핑을 해제하시겠습니까?')) return;
  $.ajax({
    url: '/api/com/authrt-prgrm', type: 'DELETE', contentType: 'application/json',
    data: JSON.stringify({ authrtId: authrtId, prgrmId: selectedPrgrmId })
  }).done(function(){ alertOk('해제되었습니다.'); loadAuthrtMap(selectedPrgrmId); })
    .fail(function(xhr){ alertErr('해제 실패 (HTTP '+xhr.status+')'); });
}

$(function(){
  loadPrgrms();
  $('#filterSys').on('change', loadPrgrms);
});
</script>
</body>
</html>
