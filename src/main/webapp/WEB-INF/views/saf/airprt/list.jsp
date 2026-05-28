<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>공항별 급유·구매 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
.nav-tabs .nav-link { color: #495057; font-size: 0.88rem; }
.nav-tabs .nav-link.active { color: #0F2C72; font-weight: 600; border-bottom: 2px solid #0F2C72; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">공항별 SAF 급유·구매</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/saf/dashboard" class="text-decoration-none">SAF</a></li>
            <li class="breadcrumb-item active">공항별 급유·구매</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 공통 필터 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-3">
        <form id="searchForm" class="row g-2 align-items-end">
          <div class="col-md-2">
            <label class="form-label small mb-1">보고연도</label>
            <select id="rprtYr" name="rprtYr" class="form-select form-select-sm">
              <option value="2026" selected>2026</option>
              <option value="2025">2025</option>
              <option value="2024">2024</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">운영사 ID</label>
            <input type="text" id="oprtrId" name="oprtrId" class="form-control form-control-sm" placeholder="운영사 코드 (ICAO 등)">
          </div>
          <div class="col-md-2">
            <label class="form-label small mb-1">공항 (ICAO)</label>
            <input type="text" name="airprtId" class="form-control form-control-sm" placeholder="ICAO 코드">
          </div>
          <div class="col-md-2">
            <button type="submit" class="btn btn-primary btn-sm w-100">
              <i class="bi bi-search"></i> 조회
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 탭 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-0">
        <ul class="nav nav-tabs border-0" id="airprtTabs">
          <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#tabFuel">급유 실적 (fuel)</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tabPurch">SAF 구매 (purch)</a></li>
        </ul>
      </div>
      <div class="tab-content card-body p-0">

        <!-- 급유 탭 -->
        <div class="tab-pane fade show active" id="tabFuel">
          <div class="d-flex align-items-center justify-content-between px-3 py-2 border-bottom">
            <span class="small text-muted">총 <strong id="fuelTotal">0</strong>건</span>
            <div class="d-flex gap-2">
              <button class="btn btn-primary btn-sm" id="openFuelRegBtn">
                <i class="bi bi-plus-lg me-1"></i> 급유 실적 등록/수정
              </button>
              <button class="btn btn-outline-success btn-sm" id="fuelExcelBtn">
                <i class="bi bi-file-earmark-excel"></i> 엑셀
              </button>
            </div>
          </div>
          <div class="table-responsive">
            <table aria-label="급유 실적" class="table table-hover table-sm mb-0 table-icas">
              <thead>
                <tr>
                  <th class="ps-3">공항 (ICAO)</th>
                  <th>운영사</th>
                  <th>항공편 수</th>
                  <th>비행시간 (h)</th>
                  <th>필요 연료량 (L)</th>
                  <th>실제 급유량 (L)</th>
                  <th>연간 비탱커 (L)</th>
                  <th>탱커 안전량 (L)</th>
                </tr>
              </thead>
              <tbody id="fuelTableBody">
                <tr><td colspan="8" class="text-center py-4 text-muted small">조회 버튼을 눌러 데이터를 로드하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 구매 탭 -->
        <div class="tab-pane fade" id="tabPurch">
          <div class="d-flex align-items-center justify-content-between px-3 py-2 border-bottom">
            <span class="small text-muted">총 <strong id="purchTotal">0</strong>건</span>
            <div class="d-flex gap-2">
              <button class="btn btn-primary btn-sm" id="openPurchRegBtn">
                <i class="bi bi-plus-lg me-1"></i> SAF 구매 등록
              </button>
              <button class="btn btn-outline-success btn-sm" id="purchExcelBtn">
                <i class="bi bi-file-earmark-excel"></i> 엑셀
              </button>
            </div>
          </div>
          <div class="table-responsive">
            <table aria-label="SAF 구매" class="table table-hover table-sm mb-0 table-icas">
              <thead>
                <tr>
                  <th class="ps-3">공항 (ICAO)</th>
                  <th>운영사</th>
                  <th>공급사</th>
                  <th>배치 ID</th>
                  <th>구매량 (L)</th>
                  <th>연료 유형</th>
                  <th>원산지</th>
                </tr>
              </thead>
              <tbody id="purchTableBody">
                <tr><td colspan="7" class="text-center py-4 text-muted small">급유 탭 조회 후 구매 탭을 선택하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     급유 실적 등록/수정 모달 (PUT saveOrUpdate)
====================================================== -->
<div class="modal fade" id="fuelRegModal" tabindex="-1" aria-labelledby="fuelRegModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="fuelRegModalLabel">
          <i class="bi bi-fuel-pump me-1"></i> 공항별 급유 실적 등록 / 수정
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-info py-2 small mb-3">
          <i class="bi bi-info-circle-fill me-1"></i>
          공항코드 + 보고연도 + 운영사 ID가 PK입니다. 동일 키 존재 시 수정, 없으면 신규 등록됩니다.
        </div>
        <form id="fuelRegForm" novalidate>
          <div class="row g-3">
            <div class="col-md-3">
              <label class="form-label small fw-semibold">공항코드 (ICAO 4자리) <span class="text-danger">*</span></label>
              <input type="text" id="fu_airprtId" class="form-control form-control-sm"
                     placeholder="예) RKSI" required maxlength="4" style="text-transform:uppercase;">
              <div class="invalid-feedback">ICAO 4자리 코드를 입력하세요.</div>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
              <select id="fu_rprtYr" class="form-select form-select-sm" required>
                <option value="2026">2026</option>
                <option value="2025">2025</option>
                <option value="2024">2024</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">운영사 ID <span class="text-danger">*</span></label>
              <input type="text" id="fu_oprtrId" class="form-control form-control-sm"
                     placeholder="예) KAL" required maxlength="20">
              <div class="invalid-feedback">운영사 ID를 입력하세요.</div>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">항공편 수</label>
              <input type="number" id="fu_fltCnt" class="form-control form-control-sm"
                     placeholder="0" min="0" step="1">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">비행 시간 (h)</label>
              <input type="number" id="fu_fltTime" class="form-control form-control-sm"
                     placeholder="0.0" min="0" step="0.01">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">필요 연료량 (kg)</label>
              <input type="number" id="fu_reqFuelQty" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">실제 급유량 (kg)</label>
              <input type="number" id="fu_actlFuelQty" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">연간 비탱커량 (kg)</label>
              <input type="number" id="fu_yrNonTankedQty" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">안전 탱커링 허용량 (kg)</label>
              <input type="number" id="fu_yrTankedSafetyQty" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="fuelRegSubmitBtn">
          <i class="bi bi-check-lg me-1"></i> 저장 (등록/수정)
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     SAF 구매 등록 모달 (POST)
====================================================== -->
<div class="modal fade" id="purchRegModal" tabindex="-1" aria-labelledby="purchRegModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="purchRegModalLabel">
          <i class="bi bi-bag-plus me-1"></i> SAF 구매 실적 신규 등록
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="purchRegForm" novalidate>
          <div class="row g-3">
            <div class="col-md-3">
              <label class="form-label small fw-semibold">공항코드 (ICAO) <span class="text-danger">*</span></label>
              <input type="text" id="pu_airprtId" class="form-control form-control-sm"
                     placeholder="예) RKSI" required maxlength="4" style="text-transform:uppercase;">
              <div class="invalid-feedback">ICAO 4자리 코드를 입력하세요.</div>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
              <select id="pu_rprtYr" class="form-select form-select-sm" required>
                <option value="2026">2026</option>
                <option value="2025">2025</option>
                <option value="2024">2024</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">운영사 ID <span class="text-danger">*</span></label>
              <input type="text" id="pu_oprtrId" class="form-control form-control-sm"
                     placeholder="예) KAL" required maxlength="20">
              <div class="invalid-feedback">운영사 ID를 입력하세요.</div>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">연료 유형</label>
              <select id="pu_fuelTypeCd" class="form-select form-select-sm">
                <option value="">선택</option>
                <option value="SAF">SAF</option>
                <option value="JET_A">Jet-A</option>
                <option value="JET_A1">Jet-A1</option>
                <option value="JP8">JP-8</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">공급사 정보</label>
              <input type="text" id="pu_splyCoInfo" class="form-control form-control-sm"
                     placeholder="공급사명" maxlength="500">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">배치 ID (연결)</label>
              <input type="text" id="pu_batchId" class="form-control form-control-sm"
                     placeholder="연결 배치 ID" maxlength="100">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">구매량 (kg) <span class="text-danger">*</span></label>
              <input type="number" id="pu_purchQty" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001" required>
              <div class="invalid-feedback">구매량을 입력하세요.</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">원산지 정보</label>
              <input type="text" id="pu_orgnInfo" class="form-control form-control-sm"
                     placeholder="예) KR,US" maxlength="200">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="purchRegSubmitBtn">
          <i class="bi bi-check-lg me-1"></i> 등록
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
function escHtml(s) { return $('<div>').text(s == null ? '-' : String(s)).html(); }
function numFmt(v)  { return (v == null) ? '-' : Number(v).toLocaleString(); }
var fuelLoaded  = false;
var purchLoaded = false;

function loadFuel() {
  var params = $('#searchForm').serialize();
  $.get('/api/saf/airprt-fuel?' + params)
    .done(function(res) {
      var list = res.data || res;
      $('#fuelTotal').text(Array.isArray(list) ? list.length : 0);
      var html = '';
      if (!list || list.length === 0) {
        html = '<tr><td colspan="8" class="text-center py-4 text-muted small">데이터가 없습니다.</td></tr>';
      } else {
        $.each(list, function(i, r) {
          html += '<tr>'
            + '<td class="ps-3 small fw-semibold">' + escHtml(r.airprtId) + '</td>'
            + '<td class="small">' + escHtml(r.oprtrId) + '</td>'
            + '<td class="small text-end">' + numFmt(r.fltCnt) + '</td>'
            + '<td class="small text-end">' + numFmt(r.fltTime) + '</td>'
            + '<td class="small text-end">' + numFmt(r.reqFuelQty) + '</td>'
            + '<td class="small text-end">' + numFmt(r.actlFuelQty) + '</td>'
            + '<td class="small text-end">' + numFmt(r.yrNonTankedQty) + '</td>'
            + '<td class="small text-end">' + numFmt(r.yrTankedSafetyQty) + '</td>'
            + '</tr>';
        });
      }
      $('#fuelTableBody').html(html);
      fuelLoaded = true;
    })
    .fail(function(xhr) {
      $('#fuelTableBody').html('<tr><td colspan="8" class="text-center py-4 text-danger small">급유 실적 조회 오류 (HTTP ' + xhr.status + ')</td></tr>');
    });
}

function loadPurch() {
  var params = $('#searchForm').serialize();
  $.get('/api/saf/airprt-purch?' + params)
    .done(function(res) {
      var list = res.data || res;
      $('#purchTotal').text(Array.isArray(list) ? list.length : 0);
      var html = '';
      if (!list || list.length === 0) {
        html = '<tr><td colspan="7" class="text-center py-4 text-muted small">데이터가 없습니다.</td></tr>';
      } else {
        $.each(list, function(i, r) {
          html += '<tr>'
            + '<td class="ps-3 small fw-semibold">' + escHtml(r.airprtId) + '</td>'
            + '<td class="small">' + escHtml(r.oprtrId) + '</td>'
            + '<td class="small">' + escHtml(r.splyCoInfo) + '</td>'
            + '<td class="small text-break">' + escHtml(r.batchId) + '</td>'
            + '<td class="small text-end">' + numFmt(r.purchQty) + '</td>'
            + '<td class="small">' + escHtml(r.fuelTypeCd) + '</td>'
            + '<td class="small">' + escHtml(r.orgnInfo) + '</td>'
            + '</tr>';
        });
      }
      $('#purchTableBody').html(html);
      purchLoaded = true;
    })
    .fail(function(xhr) {
      $('#purchTableBody').html('<tr><td colspan="7" class="text-center py-4 text-danger small">SAF 구매 조회 오류 (HTTP ' + xhr.status + ')</td></tr>');
    });
}

$(function() {
  $('#searchForm').on('submit', function(e) {
    e.preventDefault();
    fuelLoaded = false; purchLoaded = false;
    loadFuel();
  });

  $('a[data-bs-toggle="tab"]').on('shown.bs.tab', function(e) {
    var target = $(e.target).attr('href');
    if (target === '#tabPurch' && !purchLoaded) loadPurch();
    if (target === '#tabFuel'  && !fuelLoaded)  loadFuel();
  });

  $('#fuelExcelBtn').on('click', function() {
    location.href = '/api/saf/airprt-fuel/excel?' + $('#searchForm').serialize();
  });
  $('#purchExcelBtn').on('click', function() {
    location.href = '/api/saf/airprt-purch/excel?' + $('#searchForm').serialize();
  });

  /* 급유 등록/수정 모달 열기 */
  $('#openFuelRegBtn').on('click', function() {
    $('#fuelRegForm')[0].reset();
    $('#fuelRegForm').removeClass('was-validated');
    // 검색 필터값 자동 채우기
    $('#fu_rprtYr').val($('#rprtYr').val() || '2026');
    $('#fu_oprtrId').val($('#oprtrId').val() || '');
    new bootstrap.Modal('#fuelRegModal').show();
  });

  $('#fuelRegSubmitBtn').on('click', function() {
    var $form = $('#fuelRegForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;
    var payload = {
      airprtId          : $('#fu_airprtId').val().trim().toUpperCase(),
      rprtYr            : $('#fu_rprtYr').val(),
      oprtrId           : $('#fu_oprtrId').val().trim(),
      fltCnt            : $('#fu_fltCnt').val() ? parseInt($('#fu_fltCnt').val()) : null,
      fltTime           : $('#fu_fltTime').val() ? parseFloat($('#fu_fltTime').val()) : null,
      reqFuelQty        : $('#fu_reqFuelQty').val() ? parseFloat($('#fu_reqFuelQty').val()) : null,
      actlFuelQty       : $('#fu_actlFuelQty').val() ? parseFloat($('#fu_actlFuelQty').val()) : null,
      yrNonTankedQty    : $('#fu_yrNonTankedQty').val() ? parseFloat($('#fu_yrNonTankedQty').val()) : null,
      yrTankedSafetyQty : $('#fu_yrTankedSafetyQty').val() ? parseFloat($('#fu_yrTankedSafetyQty').val()) : null
    };
    var $btn = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>저장 중...');
    $.ajax({ url: '/api/saf/airprt-fuel', type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(res) {
        bootstrap.Modal.getInstance('#fuelRegModal').hide();
        IcasAlert.success(res.message || '급유 실적이 저장되었습니다.');
        fuelLoaded = false;
        loadFuel();
      })
      .fail(function(xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
        IcasAlert.error('저장 실패: ' + msg);
      })
      .always(function() { $btn.prop('disabled', false).html('<i class="bi bi-check-lg me-1"></i> 저장 (등록/수정)'); });
  });

  /* SAF 구매 등록 모달 열기 */
  $('#openPurchRegBtn').on('click', function() {
    $('#purchRegForm')[0].reset();
    $('#purchRegForm').removeClass('was-validated');
    $('#pu_rprtYr').val($('#rprtYr').val() || '2026');
    $('#pu_oprtrId').val($('#oprtrId').val() || '');
    new bootstrap.Modal('#purchRegModal').show();
  });

  $('#purchRegSubmitBtn').on('click', function() {
    var $form = $('#purchRegForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;
    var payload = {
      airprtId  : $('#pu_airprtId').val().trim().toUpperCase(),
      rprtYr    : $('#pu_rprtYr').val(),
      oprtrId   : $('#pu_oprtrId').val().trim(),
      fuelTypeCd: $('#pu_fuelTypeCd').val() || null,
      splyCoInfo: $('#pu_splyCoInfo').val().trim() || null,
      batchId   : $('#pu_batchId').val().trim() || null,
      purchQty  : parseFloat($('#pu_purchQty').val()),
      orgnInfo  : $('#pu_orgnInfo').val().trim() || null
    };
    var $btn = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>등록 중...');
    $.ajax({ url: '/api/saf/airprt-purch', type: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(res) {
        bootstrap.Modal.getInstance('#purchRegModal').hide();
        IcasAlert.success(res.message || 'SAF 구매 실적이 등록되었습니다.');
        purchLoaded = false;
        loadPurch();
      })
      .fail(function(xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
        IcasAlert.error('등록 실패: ' + msg);
      })
      .always(function() { $btn.prop('disabled', false).html('<i class="bi bi-check-lg me-1"></i> 등록'); });
  });

  loadFuel();
});
</script>
</body>
</html>
