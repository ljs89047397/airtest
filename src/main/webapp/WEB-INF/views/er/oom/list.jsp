<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>적정성검토(OoM) 목록 &mdash; ICAS-CEMS</title>
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#128270; 적정성검토(OoM) 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/er/list" class="text-decoration-none">배출량보고서(ER)</a></li>
            <li class="breadcrumb-item active">적정성검토(OoM)</li>
          </ol>
        </nav>
      </div>
      <!-- KOTSA 전용: 신규 생성 버튼 -->
      <div id="kotsa-actions" style="display:none;">
        <button id="btnCreate" class="btn btn-sm" style="background:#0F2C72;color:white;">
          <i class="bi bi-plus-circle me-1"></i>OoM 생성
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
            <label class="form-label small fw-semibold mb-1">판정결과</label>
            <select id="filterRslt" class="form-select form-select-sm" style="width:120px;">
              <option value="">전체</option>
              <option value="INPRG">진행중</option>
              <option value="PASS">PASS</option>
              <option value="FAIL">FAIL</option>
              <option value="HOLD">HOLD</option>
            </select>
          </div>
          <div class="col-auto">
            <label class="form-label small fw-semibold mb-1">운영사</label>
            <input type="text" id="filterOprtr" class="form-control form-control-sm" placeholder="운영사명 또는 ICAO" style="width:180px;">
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
          <table class="table table-hover table-sm mb-0 table-icas" aria-label="적정성검토 목록">
            <thead>
              <tr>
                <th class="ps-3" style="width:40px;">No</th>
                <th>운영사명</th>
                <th>ICAO</th>
                <th>보고연도</th>
                <th>연계 ER</th>
                <th>연계 VR</th>
                <th>판정결과</th>
                <th>생성일</th>
                <th>확정일</th>
                <th style="width:80px;">액션</th>
              </tr>
            </thead>
            <tbody id="oomListBody">
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

<!-- OoM 생성 모달 (KOTSA 전용) -->
<div class="modal fade" id="createModal" tabindex="-1" aria-labelledby="createModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="createModalLabel"><i class="bi bi-plus-circle me-1"></i>OoM 생성</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
          <select id="newRprtYr" class="form-select form-select-sm">
            <option value="2026" selected>2026</option>
            <option value="2025">2025</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">연계 ER ID <span class="text-danger">*</span></label>
          <input type="text" id="newErId" class="form-control form-control-sm" placeholder="ER202600001">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">연계 VR ID</label>
          <input type="text" id="newVrId" class="form-control form-control-sm" placeholder="VR202600001 (선택)">
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnConfirmCreate" class="btn btn-sm" style="background:#0F2C72;color:white;">생성</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>
/* ── 샘플 데이터 (API 실패 시 fallback) ─────────────────────── */
const sampleOomList = [
  { oomId:'OOM202600001', oprtrNm:'대한항공',    icaoCd:'KAL', rprtYr:2026, erId:'ER202600001', vrId:'VR202600001', rsltCd:'PASS', crtDt:'2026-04-10', fnlzDt:'2026-04-20' },
  { oomId:'OOM202600002', oprtrNm:'아시아나항공', icaoCd:'AAR', rprtYr:2026, erId:'ER202600002', vrId:null,          rsltCd:'INPRG',crtDt:'2026-04-12', fnlzDt:null         },
  { oomId:'OOM202600003', oprtrNm:'제주항공',    icaoCd:'JJA', rprtYr:2026, erId:'ER202600003', vrId:'VR202600003', rsltCd:'HOLD', crtDt:'2026-04-14', fnlzDt:null         },
  { oomId:'OOM202600004', oprtrNm:'진에어',      icaoCd:'JNA', rprtYr:2026, erId:'ER202600004', vrId:'VR202600004', rsltCd:'FAIL', crtDt:'2026-04-15', fnlzDt:'2026-04-25' },
];

/* ── 판정 배지 ───────────────────────────────────────────────── */
const RSLT_MAP = {
  'PASS':  ['bg-success',          'PASS'],
  'FAIL':  ['bg-danger',           'FAIL'],
  'HOLD':  ['bg-warning text-dark','HOLD'],
  'INPRG': ['bg-secondary',        '진행중']
};

function renderRsltBadge(cd) {
  if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
  var entry = RSLT_MAP[cd] || ['bg-secondary', cd];
  return '<span class="badge status-badge ' + entry[0] + '">' + entry[1] + '</span>';
}

function escHtml(s) {
  if (!s) return '-';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function renderTable(list) {
  $('#totalCount').text(list.length);
  if (!list.length) {
    $('#oomListBody').html('<tr><td colspan="10" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
    return;
  }
  var html = '';
  list.forEach(function(row, idx) {
    html += '<tr>'
      + '<td class="ps-3 text-muted small">' + (idx + 1) + '</td>'
      + '<td class="fw-semibold small">' + escHtml(row.oprtrNm) + '</td>'
      + '<td class="small text-muted">' + escHtml(row.icaoCd) + '</td>'
      + '<td class="small">' + escHtml(row.rprtYr) + '</td>'
      + '<td class="small"><a href="/er/detail?id=' + escHtml(row.erId) + '" class="text-decoration-none">' + escHtml(row.erId) + '</a></td>'
      + '<td class="small text-muted">' + (row.vrId ? '<a href="/vr/detail?id=' + escHtml(row.vrId) + '" class="text-decoration-none">' + escHtml(row.vrId) + '</a>' : '-') + '</td>'
      + '<td>' + renderRsltBadge(row.rsltCd) + '</td>'
      + '<td class="small text-muted">' + escHtml(row.crtDt) + '</td>'
      + '<td class="small text-muted">' + (row.fnlzDt ? escHtml(row.fnlzDt) : '-') + '</td>'
      + '<td><a href="/er/oom/' + encodeURIComponent(row.oomId) + '" class="btn btn-outline-primary" style="font-size:0.72rem;padding:2px 8px;">상세</a></td>'
      + '</tr>';
  });
  $('#oomListBody').html(html);
}

function applyFilter(list) {
  var yr    = $('#filterYr').val();
  var rslt  = $('#filterRslt').val();
  var oprtr = $('#filterOprtr').val().toLowerCase();
  return list.filter(function(row) {
    return (!yr    || String(row.rprtYr) === yr)
        && (!rslt  || row.rsltCd === rslt)
        && (!oprtr || (row.oprtrNm && row.oprtrNm.toLowerCase().indexOf(oprtr) !== -1)
                   || (row.icaoCd  && row.icaoCd.toLowerCase().indexOf(oprtr)  !== -1));
  });
}

var allData = [];

function loadData(yr) {
  $('#oomListBody').html('<tr><td colspan="10" class="text-center py-4 text-muted small"><div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...</td></tr>');
  $.get('/api/er/oom', { rprtYr: yr })
    .done(function(res) {
      allData = (res && res.data) ? res.data.rows || res.data.content || res.data : sampleOomList;
      renderTable(applyFilter(allData));
    })
    .fail(function() {
      allData = sampleOomList;
      renderTable(applyFilter(allData));
    });
}

/* ── KOTSA 권한 확인 (서버에서 주입하거나 세션으로 처리) ─── */
var userRole = '${sessionScope.userRole}';
if (userRole === 'KOTSA') {
  $('#kotsa-actions').show();
}

/* ── OoM 생성 ────────────────────────────────────────────── */
$('#btnConfirmCreate').on('click', function() {
  var erId = $.trim($('#newErId').val());
  if (!erId) { IcasAlert.info('연계 ER ID는 필수입니다.'); return; }
  var payload = { rprtYr: $('#newRprtYr').val(), erId: erId, vrId: $.trim($('#newVrId').val()) || null };
  $.ajax({ url: '/api/er/oom', type: 'POST', contentType: 'application/json',
           data: JSON.stringify(payload) })
    .done(function(res) {
      var newId = res && res.data && res.data.oomId;
      bootstrap.Modal.getInstance(document.getElementById('createModal')).hide();
      if (newId) { location.href = '/er/oom/' + encodeURIComponent(newId); }
      else        { loadData($('#filterYr').val()); }
    })
    .fail(function(xhr) {
      var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'OoM 생성에 실패했습니다.';
      IcasAlert.info(msg);
    });
});

$(function() {
  loadData($('#filterYr').val());

  $('#btnSearch').on('click', function() { renderTable(applyFilter(allData)); });
  $('#btnReset').on('click', function() {
    $('#filterRslt').val(''); $('#filterOprtr').val('');
    renderTable(applyFilter(allData));
  });
  $('#filterYr').on('change', function() { loadData($(this).val()); });
  $('#btnCreate').on('click', function() {
    new bootstrap.Modal(document.getElementById('createModal')).show();
  });
});
</script>
</body>
</html>
