<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>검증보고서(VR) 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#9989; 검증보고서(VR) 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">검증보고서(VR)</li>
          </ol>
        </nav>
      </div>
      <div>
        <button id="btnCreate" class="btn btn-sm" style="background:#0F2C72;color:white;" data-bs-toggle="modal" data-bs-target="#vrCreateModal">
          <i class="bi bi-plus-circle me-1"></i>신규 등록 (검증기관)
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
              <option value="SBMTD">제출됨</option>
              <option value="RCMDD">권고</option>
              <option value="APRVD">승인</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">운영사</label>
            <input type="text" id="filterOprtr" class="form-control form-control-sm" placeholder="운영사명 또는 ICAO" style="width:180px;">
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">검증기관</label>
            <input type="text" id="filterVrfcn" class="form-control form-control-sm" placeholder="검증기관명" style="width:180px;">
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
                <th>운영사명</th>
                <th>ICAO</th>
                <th>보고연도</th>
                <th>VR 유형</th>
                <th>검증기관</th>
                <th>버전</th>
                <th>상태</th>
                <th>제출일</th>
                <th>승인일</th>
                <th style="width:80px;">액션</th>
              </tr>
            </thead>
            <tbody id="vrListBody">
              <tr>
                <td colspan="11" class="text-center py-4 text-muted small">
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

<!-- 신규 등록 모달 (검증기관 사용자 전용) -->
<div class="modal fade" id="vrCreateModal" tabindex="-1" aria-labelledby="vrCreateModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h5 class="modal-title" id="vrCreateModalLabel"><i class="bi bi-plus-circle me-2"></i>VR 신규 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="vrCreateForm">
          <div class="mb-3">
            <label for="newOprtrId" class="form-label small fw-semibold">검증 대상 운영사 <span class="text-danger">*</span></label>
            <select id="newOprtrId" name="oprtrId" class="form-select form-select-sm" required>
              <option value="">선택...</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newRprtYr" class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
            <select id="newRprtYr" name="rprtYr" class="form-select form-select-sm" required>
              <option value="2026">2026</option>
              <option value="2025">2025</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newVrType" class="form-label small fw-semibold">검증 유형 <span class="text-danger">*</span></label>
            <select id="newVrType" name="vrTypeCd" class="form-select form-select-sm" required>
              <option value="ER">ER (배출량보고서) 검증</option>
              <option value="EUCR">EUCR (배출권 취소) 검증</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newVrfcnInst" class="form-label small fw-semibold">검증기관 <span class="text-danger">*</span></label>
            <select id="newVrfcnInst" name="vrfcnInstId" class="form-select form-select-sm" required>
              <option value="">선택...</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newErId" class="form-label small fw-semibold">대상 ER ID (선택)</label>
            <input type="text" id="newErId" name="erId" class="form-control form-control-sm" placeholder="예: ER2026KAL (선택)">
          </div>
          <div class="alert alert-warning small mb-0">
            <i class="bi bi-shield-check me-1"></i>
            VR 은 ICAO CCR 공인 검증기관 사용자가 자기 검증기관에 대해서만 생성할 수 있습니다.
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnSubmitCreate" class="btn btn-sm" style="background:#0F2C72;color:white;">
          <i class="bi bi-check2 me-1"></i>등록
        </button>
      </div>
    </div>
  </div>
</div>

<!-- 토스트 컨테이너 -->
<div id="toastContainer" class="position-fixed top-0 end-0 p-3" style="z-index:9999;"></div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>
/* ===== 샘플 데이터 (API 연동 전 fallback) ===== */
const sampleVrList = [
  {seq:1, oprtrNm:'대한항공',    icaoCd:'KAL', rprtYr:2026, vrTypeCd:'ER',   vrfcnInstNm:'한국인증원',   vrVer:1, vrStCd:'APRVD', sbmtDt:'2026-04-10', aprvDt:'2026-05-01', vrId:'VR202600001'},
  {seq:2, oprtrNm:'아시아나항공', icaoCd:'AAR', rprtYr:2026, vrTypeCd:'ER',   vrfcnInstNm:'DNV Korea',   vrVer:1, vrStCd:'RCMDD', sbmtDt:'2026-04-15', aprvDt:null,         vrId:'VR202600002'},
  {seq:3, oprtrNm:'제주항공',    icaoCd:'JJA', rprtYr:2026, vrTypeCd:'ER',   vrfcnInstNm:'SGS Korea',   vrVer:1, vrStCd:'SBMTD', sbmtDt:'2026-04-20', aprvDt:null,         vrId:'VR202600003'},
  {seq:4, oprtrNm:'진에어',      icaoCd:'JNA', rprtYr:2026, vrTypeCd:'ER',   vrfcnInstNm:'한국인증원',   vrVer:1, vrStCd:'DRAFT',  sbmtDt:null,         aprvDt:null,         vrId:'VR202600004'},
  {seq:5, oprtrNm:'티웨이항공',  icaoCd:'TWB', rprtYr:2026, vrTypeCd:'EUCR', vrfcnInstNm:'Bureau Veritas', vrVer:1, vrStCd:'SBMTD', sbmtDt:'2026-04-18', aprvDt:null, vrId:'VR202600005'},
];

const STATUS_MAP = {
  'DRAFT':  ['bg-secondary', '작성중'],
  'SBMTD':  ['bg-primary',   '제출됨'],
  'RCMDD':  ['bg-warning text-dark', '권고'],
  'APRVD':  ['bg-success',   '승인']
};

function renderBadge(cd) {
  if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
  const [cls, lbl] = STATUS_MAP[cd] || ['bg-secondary', cd];
  return '<span class="badge status-badge ' + cls + '">' + lbl + '</span>';
}

function esc(s) {
  if (s == null) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function renderTable(list) {
  $('#totalCount').text(list.length);
  if (!list.length) {
    $('#vrListBody').html('<tr><td colspan="11" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  let html = '';
  list.forEach(function(row, idx) {
    html += '<tr>'
      + '<td class="ps-3 text-muted small">' + (idx + 1) + '</td>'
      + '<td class="fw-semibold small">' + esc(row.oprtrNm) + '</td>'
      + '<td class="small text-muted">' + esc(row.icaoCd) + '</td>'
      + '<td class="small">' + esc(row.rprtYr) + '</td>'
      + '<td class="small"><span class="badge bg-light text-dark border">' + esc(row.vrTypeCd) + '</span></td>'
      + '<td class="small">' + esc(row.vrfcnInstNm) + '</td>'
      + '<td class="small text-center">v' + esc(row.vrVer) + '</td>'
      + '<td>' + renderBadge(row.vrStCd) + '</td>'
      + '<td class="small text-muted">' + (row.sbmtDt || '-') + '</td>'
      + '<td class="small text-muted">' + (row.aprvDt  || '-') + '</td>'
      + '<td><a href="/vr/' + esc(row.vrId) + '" class="btn btn-outline-primary" style="font-size:0.72rem;padding:2px 8px;">상세</a></td>'
      + '</tr>';
  });
  $('#vrListBody').html(html);
}

function applyFilter(list) {
  const yr     = $('#filterYr').val();
  const status = $('#filterStatus').val();
  const oprtr  = $('#filterOprtr').val().toLowerCase();
  const vrfcn  = $('#filterVrfcn').val().toLowerCase();
  return list.filter(function(row) {
    return (!yr     || String(row.rprtYr) === yr)
        && (!status || row.vrStCd === status)
        && (!oprtr  || (row.oprtrNm && row.oprtrNm.toLowerCase().includes(oprtr)) || (row.icaoCd && row.icaoCd.toLowerCase().includes(oprtr)))
        && (!vrfcn  || (row.vrfcnInstNm && row.vrfcnInstNm.toLowerCase().includes(vrfcn)));
  });
}

let allData = [];

function showToast(msg, type) {
  type = type || 'danger';
  const id = 'toast_' + Date.now();
  const html = '<div id="' + id + '" class="toast align-items-center text-white bg-' + type + ' border-0" role="alert">'
    + '<div class="d-flex"><div class="toast-body small">' + esc(msg) + '</div>'
    + '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div></div>';
  $('#toastContainer').append(html);
  const el = document.getElementById(id);
  new bootstrap.Toast(el, {delay: 4000}).show();
  el.addEventListener('hidden.bs.toast', function(){ el.remove(); });
}

function loadData(yr) {
  $('#vrListBody').html('<tr><td colspan="11" class="text-center py-4 text-muted small"><div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...</td></tr>');
  $.get('/api/vr?rprtYr=' + encodeURIComponent(yr))
    .done(function(res) {
      var rows = (res && res.data) ? res.data : res;
      if (!Array.isArray(rows)) rows = rows && (rows.content || rows.rows || rows.list) || [];
      // 컬럼 매핑 정정 — 백엔드 응답은 oprtrId/erId 등을 직접 노출
      allData = rows.map(function(r){
        return Object.assign({}, r, {
          icaoCd: r.icaoCd || r.oprtrId,
          oprtrNm: r.oprtrNm || r.oprtrId,
          vrfcnInstNm: r.vrfcnInstNm || r.vrfcnInstId,
          vrVer: r.vrVer || 1
        });
      });
      renderTable(applyFilter(allData));
    })
    .fail(function(xhr) {
      allData = [];
      $('#vrListBody').html('<tr><td colspan="11" class="text-center py-4 text-danger small">조회 실패 (HTTP ' + xhr.status + ') — ' + (xhr.responseJSON && xhr.responseJSON.message || '') + '</td></tr>');
    });
}

function loadCreateOptions(){
  $.get('/api/com/oprtr?size=200').done(function(res){
    var rows = (res && res.data) ? res.data : (res || []);
    if (!Array.isArray(rows)) rows = rows.content || rows.rows || rows.list || [];
    var opts = '<option value="">선택...</option>';
    rows.forEach(function(o){
      opts += '<option value="' + esc(o.oprtrId) + '">' + esc((o.oprtrNm||o.oprtrId)+' ('+o.oprtrId+')') + '</option>';
    });
    $('#newOprtrId').html(opts);
  });
  $.get('/api/com/vrfcn/inst').done(function(res){
    var rows = (res && res.data) ? res.data : (res || []);
    if (!Array.isArray(rows)) rows = rows.content || rows.rows || rows.list || [];
    var opts = '<option value="">선택...</option>';
    rows.forEach(function(v){
      opts += '<option value="' + esc(v.vrfcnInstId) + '">' + esc((v.vrfcnInstNm||v.vrfcnInstId)+' ('+v.vrfcnInstId+')') + '</option>';
    });
    $('#newVrfcnInst').html(opts);
  });
}

$('#btnSubmitCreate').on('click', function(){
  var body = {
    oprtrId: $('#newOprtrId').val(),
    rprtYr:  $('#newRprtYr').val(),
    vrTypeCd: $('#newVrType').val(),
    vrfcnInstId: $('#newVrfcnInst').val(),
    erId: $('#newErId').val() || null
  };
  if (!body.oprtrId || !body.vrfcnInstId) { IcasAlert.warning('운영사·검증기관을 모두 선택해주세요.'); return; }
  var csrfToken  = $('meta[name="_csrf"]').attr('content');
  var csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';
  var headers = {}; headers[csrfHeader] = csrfToken;
  $.ajax({url:'/api/vr', type:'POST', contentType:'application/json', headers:headers, data:JSON.stringify(body)})
    .done(function(res){
      IcasAlert.success('VR 등록 성공: ' + (res.data && res.data.vrId));
      bootstrap.Modal.getInstance(document.getElementById('vrCreateModal')).hide();
      loadData($('#filterYr').val());
    })
    .fail(function(xhr){
      var msg = (xhr.responseJSON && xhr.responseJSON.message) || ('HTTP ' + xhr.status);
      IcasAlert.error('등록 실패: ' + msg);
    });
});

$(function() {
  loadData($('#filterYr').val());
  loadCreateOptions();
  $('#btnSearch').on('click', function() { renderTable(applyFilter(allData)); });
  $('#btnReset').on('click', function() {
    $('#filterStatus').val('');
    $('#filterOprtr').val('');
    $('#filterVrfcn').val('');
    renderTable(applyFilter(allData));
  });
  $('#filterYr').on('change', function() { loadData($(this).val()); });
});
</script>
</body>
</html>
