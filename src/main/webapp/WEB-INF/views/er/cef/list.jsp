<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>적격연료(CEF) 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; cursor: pointer; }
.status-badge { font-size: 0.72rem; padding: 3px 8px; border-radius: 4px; font-weight: 600; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3">
    <div class="d-flex align-items-center justify-content-between">
      <div>
        <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-fuel-pump me-2"></i>적격연료(CEF) 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item">배출량보고서</li>
            <li class="breadcrumb-item active">적격연료(CEF)</li>
          </ol>
        </nav>
      </div>
      <div>
        <button id="btnCreate" class="btn btn-sm" style="background:#0F2C72;color:white;" data-bs-toggle="modal" data-bs-target="#cefCreateModal">
          <i class="bi bi-plus-circle me-1"></i>신규 등록 (항공사)
        </button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 검색 필터 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-3">
        <div class="row g-2 align-items-end">
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">보고연도</label>
            <select id="filterYr" class="form-select form-select-sm" style="width:100px;">
              <option value="2026" selected>2026</option>
              <option value="2025">2025</option>
              <option value="2024">2024</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">상태</label>
            <select id="filterStatus" class="form-select form-select-sm" style="width:120px;">
              <option value="">전체</option>
              <option value="DRAFT">작성중</option>
              <option value="SBMTD">제출</option>
              <option value="APRVD">승인</option>
              <option value="CNCLD">취소</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">운영사</label>
            <input type="text" id="filterOprtr" class="form-control form-control-sm"
                   placeholder="운영사명 또는 ICAO" style="width:180px;">
          </div>
          <div class="col-auto">
            <button id="btnSearch" class="btn btn-sm" style="background:#0F2C72;color:white;">
              <i class="bi bi-search me-1"></i>조회
            </button>
            <button id="btnReset" class="btn btn-sm btn-outline-secondary ms-1">초기화</button>
          </div>
          <div class="col-auto ms-auto">
            <span class="text-muted small">총 <strong id="totalCount">0</strong>건</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 목록 테이블 -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0 table-icas">
            <thead>
              <tr>
                <th class="ps-3" style="width:40px;">No</th>
                <th>CEF 번호</th>
                <th>운영사명</th>
                <th>ICAO</th>
                <th>보고연도</th>
                <th class="text-end pe-3">순연료질량(kg)</th>
                <th>상태</th>
                <th>제출일</th>
                <th>승인일</th>
                <th style="width:70px;">상세</th>
              </tr>
            </thead>
            <tbody id="cefListBody">
              <tr>
                <td colspan="10" class="text-center py-4 text-muted small">
                  <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                  데이터 로딩 중...
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script src="/resources/js/er/cef/cef-common.js"></script>
<script>
let allData = [];

function renderTable(list) {
  $('#totalCount').text(list.length);
  if (!list.length) {
    $('#cefListBody').html('<tr><td colspan="10" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  let html = '';
  list.forEach(function(row, idx) {
    const mass = row.ttlReduMass != null
      ? Number(row.ttlReduMass).toLocaleString('ko-KR', {minimumFractionDigits: 2, maximumFractionDigits: 2})
      : '-';
    html += '<tr onclick="location.href=\'/er/cef/' + esc(row.cefId) + '\'">'
      + '<td class="ps-3 text-muted small">' + (idx + 1) + '</td>'
      + '<td class="small fw-semibold text-primary">' + esc(row.cefId || '') + '</td>'
      + '<td class="small fw-semibold">' + esc(row.oprtrNm || '') + '</td>'
      + '<td class="small text-muted">' + esc(row.icaoCd || '') + '</td>'
      + '<td class="small">' + esc(String(row.rprtYr || '')) + '</td>'
      + '<td class="small text-end pe-3">' + mass + '</td>'
      + '<td>' + renderCefBadge(row.sttsCd) + '</td>'
      + '<td class="small text-muted">' + esc(row.sbmtDt || '-') + '</td>'
      + '<td class="small text-muted">' + esc(row.aprvDt  || '-') + '</td>'
      + '<td><a href="/er/cef/' + esc(row.cefId) + '" class="btn btn-outline-primary btn-xs"'
      + ' style="font-size:0.72rem;padding:2px 8px;" onclick="event.stopPropagation();">상세</a></td>'
      + '</tr>';
  });
  $('#cefListBody').html(html);
}

function applyFilter(list) {
  const yr     = $('#filterYr').val();
  const status = $('#filterStatus').val();
  const oprtr  = $('#filterOprtr').val().toLowerCase();
  return list.filter(function(row) {
    return (!yr     || String(row.rprtYr) === yr)
        && (!status || row.sttsCd === status)
        && (!oprtr  || (row.oprtrNm && row.oprtrNm.toLowerCase().includes(oprtr))
                    || (row.icaoCd  && row.icaoCd.toLowerCase().includes(oprtr)));
  });
}

function loadData(yr) {
  $('#cefListBody').html('<tr><td colspan="10" class="text-center py-4 text-muted small">'
    + '<div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...</td></tr>');
  $.get('/api/er/cef', { rprtYr: yr })
    .done(function(res) {
      var d = (res && res.data) ? res.data : res;
      allData = (d && (d.rows || d.content || (Array.isArray(d) ? d : []))) || [];
      renderTable(applyFilter(allData));
    })
    .fail(function(xhr) {
      allData = [];
      $('#cefListBody').html('<tr><td colspan="10" class="text-center py-3 text-danger small">'
        + '<i class="bi bi-exclamation-triangle me-1"></i>데이터 조회 중 오류가 발생했습니다. (HTTP ' + xhr.status + ')</td></tr>');
    });
}

function loadCefCreateOptions(){
  $.get('/api/com/oprtr?size=200').done(function(res){
    var rows = (res && res.data) ? res.data : (res || []);
    if (!Array.isArray(rows)) rows = rows.content || rows.rows || rows.list || [];
    var opts = '<option value="">선택...</option>';
    rows.forEach(function(o){
      opts += '<option value="' + esc(o.oprtrId) + '">' + esc((o.oprtrNm||o.oprtrId)+' ('+o.oprtrId+')') + '</option>';
    });
    $('#newCefOprtrId').html(opts);
  });
  $.get('/api/er/rprt?rprtYr=2026').done(function(res){
    var rows = (res && res.data) ? res.data : (res || []);
    if (!Array.isArray(rows)) rows = rows.content || rows.rows || rows.list || [];
    var opts = '<option value="">선택...</option>';
    rows.forEach(function(e){
      opts += '<option value="' + esc(e.erId) + '" data-oprtr="' + esc(e.oprtrId) + '" data-yr="' + esc(e.rprtYr) + '">'
            + esc(e.erId + ' — ' + (e.oprtrNm||e.oprtrId) + '/' + e.rprtYr) + '</option>';
    });
    $('#newCefErId').html(opts);
  });
}

$('#btnSubmitCefCreate').on('click', function(){
  var erSel = $('#newCefErId option:selected');
  var body = {
    oprtrId: $('#newCefOprtrId').val() || erSel.data('oprtr'),
    rprtYr:  $('#newCefRprtYr').val(),
    erId:    $('#newCefErId').val()
  };
  if (!body.oprtrId || !body.erId) { IcasAlert.warning('운영사·대상 ER 을 선택해주세요.'); return; }
  var csrfToken  = $('meta[name="_csrf"]').attr('content');
  var csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';
  var headers = {}; headers[csrfHeader] = csrfToken;
  $.ajax({url:'/api/er/cef', type:'POST', contentType:'application/json', headers:headers, data:JSON.stringify(body)})
    .done(function(res){
      IcasAlert.success('CEF 등록 성공: ' + (res.data && res.data.cefId));
      bootstrap.Modal.getInstance(document.getElementById('cefCreateModal')).hide();
      loadData($('#filterYr').val());
    })
    .fail(function(xhr){
      var msg = (xhr.responseJSON && xhr.responseJSON.message) || ('HTTP ' + xhr.status);
      IcasAlert.error('등록 실패: ' + msg);
    });
});

$(function() {
  loadData($('#filterYr').val());
  loadCefCreateOptions();
  $('#btnSearch').on('click',  function() { renderTable(applyFilter(allData)); });
  $('#btnReset').on('click',   function() { $('#filterStatus').val(''); $('#filterOprtr').val(''); renderTable(applyFilter(allData)); });
  $('#filterYr').on('change',  function() { loadData($(this).val()); });
});
</script>

<!-- 신규 등록 모달 (항공사 전용) -->
<div class="modal fade" id="cefCreateModal" tabindex="-1" aria-labelledby="cefCreateModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h5 class="modal-title" id="cefCreateModalLabel"><i class="bi bi-plus-circle me-2"></i>CEF 신규 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="cefCreateForm">
          <div class="mb-3">
            <label for="newCefOprtrId" class="form-label small fw-semibold">운영사 <span class="text-danger">*</span></label>
            <select id="newCefOprtrId" name="oprtrId" class="form-select form-select-sm" required>
              <option value="">선택...</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newCefRprtYr" class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
            <select id="newCefRprtYr" name="rprtYr" class="form-select form-select-sm" required>
              <option value="2026">2026</option>
              <option value="2025">2025</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newCefErId" class="form-label small fw-semibold">부속 ER ID <span class="text-danger">*</span></label>
            <select id="newCefErId" name="erId" class="form-select form-select-sm" required>
              <option value="">선택...</option>
            </select>
            <div class="form-text small">CEF 는 ER 의 부속 보고서로 등록됩니다.</div>
          </div>
          <div class="alert alert-info small mb-0">
            <i class="bi bi-info-circle me-1"></i>
            등록 후 상세 화면에서 청구 정보 (인증서·수명주기·공급망) 를 추가할 수 있습니다.
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnSubmitCefCreate" class="btn btn-sm" style="background:#0F2C72;color:white;">
          <i class="bi bi-check2 me-1"></i>등록
        </button>
      </div>
    </div>
  </div>
</div>

</body>
</html>
