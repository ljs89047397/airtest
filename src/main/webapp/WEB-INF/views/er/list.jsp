<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배출량보고서(ER) 목록 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
.table-icas thead th { background:#0F2C72; color:white; font-size:0.82rem; font-weight:500; border:none; }
.table-icas tbody tr:hover { background:#f8f9ff; }
.status-badge { font-size:0.72rem; padding:3px 8px; border-radius:4px; font-weight:600; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#128203; 배출량보고서(ER) 목록</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">배출량보고서</li>
          </ol>
        </nav>
      </div>
      <div>
        <button id="btnCreate" class="btn btn-sm" style="background:#0F2C72;color:white;" data-bs-toggle="modal" data-bs-target="#erCreateModal">
          <i class="bi bi-plus-circle me-1"></i>신규 등록
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
              <option value="RVWNG">검토중</option>
              <option value="RJCTD">반려</option>
              <option value="APRVD">승인</option>
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
          <table class="table table-hover table-sm mb-0 table-icas" aria-label="배출량보고서 목록">
            <thead>
              <tr>
                <th class="ps-3" style="width:40px;">No</th>
                <th>운영사</th>
                <th>ICAO</th>
                <th>보고연도</th>
                <th>버전</th>
                <th>상태</th>
                <th>제출일</th>
                <th>승인일</th>
                <th style="width:80px;">관리</th>
              </tr>
            </thead>
            <tbody id="erListBody">
              <tr>
                <td colspan="9" class="text-center py-4 text-muted small">
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

<!-- 신규 등록 모달 -->
<div class="modal fade" id="erCreateModal" tabindex="-1" aria-labelledby="erCreateModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h5 class="modal-title" id="erCreateModalLabel"><i class="bi bi-plus-circle me-2"></i>ER 신규 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <form id="erCreateForm">
          <div class="mb-3">
            <label for="newOprtrId" class="form-label small fw-semibold">운영사 (ICAO) <span class="text-danger">*</span></label>
            <select id="newOprtrId" name="oprtrId" class="form-select form-select-sm" required>
              <option value="">선택...</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newRprtYr" class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
            <select id="newRprtYr" name="rprtYr" class="form-select form-select-sm" required>
              <option value="2026">2026</option>
              <option value="2025">2025</option>
              <option value="2027">2027</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="newEmpPlanId" class="form-label small fw-semibold">적용 EMP Plan ID (선택)</label>
            <input type="text" id="newEmpPlanId" name="empPlanIdApld" class="form-control form-control-sm" placeholder="예: EMP2026KAL">
            <div class="form-text small">생략 시 운영사의 최신 승인 EMP가 자동 적용됩니다.</div>
          </div>
          <div class="alert alert-info small mb-0">
            <i class="bi bi-info-circle me-1"></i>
            ER 은 항공사(AIRLINE) 사용자가 자기 항공사 데이터로만 생성할 수 있습니다.
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-csrf.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>
(function(){
  var csrfToken  = $('meta[name="_csrf"]').attr('content');
  var csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';

  const STATUS_MAP = {
    'DRAFT':  ['bg-secondary', '작성중'],
    'SBMTD':  ['bg-primary',   '제출'],
    'RVWNG':  ['bg-warning text-dark', '검토중'],
    'RCMDD':  ['bg-info',      '권고'],
    'RJCTD':  ['bg-danger',    '반려'],
    'APRVD':  ['bg-success',   '승인'],
    'CNCLD':  ['bg-dark',      '취소']
  };

  function esc(s){ return (s==null?'':String(s)).replace(/[&<>"']/g, function(c){
    return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]; }); }

  function renderBadge(cd) {
    if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
    const m = STATUS_MAP[cd] || ['bg-secondary', cd];
    return '<span class="badge status-badge ' + m[0] + '">' + m[1] + '</span>';
  }

  function renderTable(list) {
    $('#totalCount').text(list.length);
    if (!list.length) {
      $('#erListBody').html('<tr><td colspan="9" class="text-center py-4 text-muted small">조회된 데이터가 없습니다.</td></tr>');
      return;
    }
    let html = '';
    list.forEach(function(row, idx) {
      var sbmt = (row.sbmtDt || '').toString().substring(0,10);
      var aprv = (row.aprvDt || '').toString().substring(0,10);
      html += '<tr>'
        + '<td class="ps-3 text-muted small">' + (idx + 1) + '</td>'
        + '<td class="fw-semibold small">' + esc(row.oprtrNm || row.oprtrId) + '</td>'
        + '<td class="small text-muted">' + esc(row.oprtrId) + '</td>'
        + '<td class="small">' + esc(row.rprtYr) + '</td>'
        + '<td class="small text-center">v' + esc(row.erVer || '1.0') + '</td>'
        + '<td>' + renderBadge(row.erStCd) + '</td>'
        + '<td class="small text-muted">' + esc(sbmt || '-') + '</td>'
        + '<td class="small text-muted">' + esc(aprv || '-') + '</td>'
        + '<td><a href="/er/' + esc(row.erId) + '" class="btn btn-sm btn-outline-primary" style="font-size:0.72rem;padding:2px 8px;">상세</a></td>'
        + '</tr>';
    });
    $('#erListBody').html(html);
  }

  function applyFilter(list) {
    const status = $('#filterStatus').val();
    const oprtr  = $('#filterOprtr').val().toLowerCase();
    return list.filter(function(row) {
      var nm = (row.oprtrNm || '').toLowerCase();
      var id = (row.oprtrId || '').toLowerCase();
      return (!status || row.erStCd === status)
          && (!oprtr  || nm.includes(oprtr) || id.includes(oprtr));
    });
  }

  let allData = [];

  function loadData(yr) {
    $.get('/api/er/rprt?rprtYr=' + encodeURIComponent(yr))
      .done(function(res){
        var rows = (res && res.data) ? res.data : (res || []);
        if (!Array.isArray(rows)) rows = rows.content || rows.rows || [];
        allData = rows;
        renderTable(applyFilter(allData));
      })
      .fail(function(xhr){
        $('#erListBody').html('<tr><td colspan="9" class="text-center py-4 text-danger small">조회 실패 (HTTP ' + xhr.status + ') — ' + (xhr.responseJSON && xhr.responseJSON.message || '') + '</td></tr>');
      });
  }

  function loadOprtrList() {
    $.get('/api/com/oprtr?size=200')
      .done(function(res){
        var rows = (res && res.data) ? res.data : (res || []);
        if (!Array.isArray(rows)) rows = rows.content || rows.rows || rows.list || [];
        var opts = '<option value="">선택...</option>';
        rows.forEach(function(o){
          opts += '<option value="' + esc(o.oprtrId) + '">' + esc((o.oprtrNm||o.oprtrId) + ' ('+o.oprtrId+')') + '</option>';
        });
        $('#newOprtrId').html(opts);
      });
  }

  $('#btnSubmitCreate').on('click', function(){
    var body = {
      oprtrId: $('#newOprtrId').val(),
      rprtYr:  $('#newRprtYr').val(),
      empPlanIdApld: $('#newEmpPlanId').val() || null
    };
    if (!body.oprtrId) { IcasAlert.warning('운영사를 선택해주세요.'); return; }
    var headers = {};
    headers[csrfHeader] = csrfToken;
    $.ajax({
      url: '/api/er/rprt',
      type: 'POST',
      contentType: 'application/json',
      headers: headers,
      data: JSON.stringify(body)
    }).done(function(res){
      IcasAlert.success('ER 등록 성공: ' + (res.data && res.data.erId));
      bootstrap.Modal.getInstance(document.getElementById('erCreateModal')).hide();
      loadData($('#filterYr').val());
    }).fail(function(xhr){
      var msg = (xhr.responseJSON && xhr.responseJSON.message) || ('HTTP ' + xhr.status);
      IcasAlert.error('등록 실패: ' + msg);
    });
  });

  $(function(){
    loadData($('#filterYr').val());
    loadOprtrList();
    $('#btnSearch').on('click', function(){ renderTable(applyFilter(allData)); });
    $('#btnReset').on('click', function(){
      $('#filterStatus').val(''); $('#filterOprtr').val('');
      renderTable(applyFilter(allData));
    });
    $('#filterYr').on('change', function(){ loadData($(this).val()); });
  });
})();
</script>
</body>
</html>
