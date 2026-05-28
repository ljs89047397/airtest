<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAF 혼합비율 모니터링 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
.ratio-bar-bg { background: #e9ecef; border-radius: 4px; height: 8px; }
.ratio-bar    { border-radius: 4px; height: 8px; transition: width 0.4s; }
.badge-fulfilled { background: #2e7d32; font-size: 0.72rem; }
.badge-short     { background: #c62828; font-size: 0.72rem; }
.summary-card { border-radius: 10px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.icon-wrap { width: 46px; height: 46px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.3rem; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">SAF 혼합비율 모니터링</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/saf/dashboard" class="text-decoration-none">SAF</a></li>
            <li class="breadcrumb-item active">혼합비율 모니터링</li>
          </ol>
        </nav>
      </div>
      <div class="d-flex gap-2 align-items-center">
        <select id="filterYr" class="form-select form-select-sm" style="width:100px;">
          <option value="2026" selected>2026</option>
          <option value="2025">2025</option>
          <option value="2024">2024</option>
        </select>
        <button class="btn btn-outline-secondary btn-sm" id="singleCalcBtn">
          <i class="bi bi-person-check me-1"></i> 운영사별 산출
        </button>
        <button class="btn btn-outline-primary btn-sm" id="calcAllBtn">
          <i class="bi bi-calculator me-1"></i> 전체 일괄 산출
        </button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 요약 카드 -->
    <div class="row g-3 mb-4" id="summaryCards">
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8f5e9;"><i class="bi bi-check-circle-fill text-success"></i></div>
            <div>
              <div class="text-muted small">의무 충족</div>
              <div class="fs-3 fw-bold text-success" id="cntFulfilled">-</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#ffebee;"><i class="bi bi-x-circle-fill text-danger"></i></div>
            <div>
              <div class="text-muted small">의무 미달</div>
              <div class="fs-3 fw-bold text-danger" id="cntShort">-</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8eeff;"><i class="bi bi-percent" style="color:#0F2C72;"></i></div>
            <div>
              <div class="text-muted small">전체 평균 혼합비율</div>
              <div class="fs-3 fw-bold" style="color:#0F2C72;" id="avgRatio">-</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#fff3e0;"><i class="bi bi-flag-fill text-warning"></i></div>
            <div>
              <div class="text-muted small">의무 비율 (국가 고시)</div>
              <div class="fs-3 fw-bold text-warning" id="oblgRatio">1.0%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 의무 구분 탭 (시행계획 p.5~6 — 공급의무 '27~ / 급유의무 '28~) -->
    <ul class="nav nav-tabs mb-3" id="oblgTabs">
      <li class="nav-item">
        <a class="nav-link active" data-bs-toggle="tab" href="#tabFuelOblg">
          <i class="bi bi-airplane me-1"></i>급유의무 (항공사)
          <span class="badge bg-secondary ms-1">'28년~</span>
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" data-bs-toggle="tab" href="#tabSupplyOblg">
          <i class="bi bi-droplet-fill me-1"></i>공급의무 (정유사)
          <span class="badge bg-secondary ms-1">'27년~</span>
        </a>
      </li>
    </ul>
    <div class="tab-content">

    <!-- 공급의무 탭 (정유사·수출입업자) -->
    <div class="tab-pane fade" id="tabSupplyOblg">
      <div class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white border-bottom py-2">
          <h6 class="fw-bold mb-0" style="color:#0F2C72;">정유사·수출입업자 SAF 공급의무 이행률 (Mass &amp; Balance 연단위)</h6>
        </div>
        <div class="card-body">
          <div class="alert alert-info py-2 px-3 mb-3 small">
            <i class="bi bi-info-circle me-1"></i>
            <strong>대상자</strong>: 석유정제업자·석유수출입업자·일정 이상 석유제품 판매업자 ·
            <strong>대상연료</strong>: 기존 항공유 대비 10% 이상 탄소 감축 연료 ·
            <strong>과징금</strong>: 불이행시 평균거래가 × 150% (2년 유예) — <em>시행계획 p.5</em>
          </div>
          <table class="table table-sm table-hover">
            <thead style="background:#f5f7fb;">
              <tr>
                <th>공급사</th>
                <th class="text-end">총 항공유 공급량 (L)</th>
                <th class="text-end">SAF 공급량 (L)</th>
                <th class="text-end">공급비율 (%)</th>
                <th class="text-end">의무비율 ('27)</th>
                <th>이행 상태</th>
                <th class="text-end">불이행시 과징금</th>
              </tr>
            </thead>
            <tbody id="supplyTbody">
              <tr><td>SK에너지</td><td class="text-end">8,400,000,000</td><td class="text-end">84,000,000</td><td class="text-end text-success fw-bold">1.00%</td><td class="text-end">1.0%</td><td><span class="badge bg-success">충족</span></td><td class="text-end text-muted">-</td></tr>
              <tr><td>GS칼텍스</td><td class="text-end">7,200,000,000</td><td class="text-end">68,400,000</td><td class="text-end text-warning fw-bold">0.95%</td><td class="text-end">1.0%</td><td><span class="badge bg-warning">미달</span></td><td class="text-end fw-bold text-danger">유예 (’28~’29)</td></tr>
              <tr><td>S-OIL</td><td class="text-end">5,800,000,000</td><td class="text-end">58,000,000</td><td class="text-end text-success fw-bold">1.00%</td><td class="text-end">1.0%</td><td><span class="badge bg-success">충족</span></td><td class="text-end text-muted">-</td></tr>
              <tr><td>HD현대오일뱅크</td><td class="text-end">4,200,000,000</td><td class="text-end">31,500,000</td><td class="text-end text-danger fw-bold">0.75%</td><td class="text-end">1.0%</td><td><span class="badge bg-danger">미달</span></td><td class="text-end fw-bold text-danger">유예 (’28~’29)</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 급유의무 탭 (항공사) -->
    <div class="tab-pane fade show active" id="tabFuelOblg">

    <!-- 모니터링 테이블 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-2 d-flex align-items-center justify-content-between">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;">항공사별 SAF 급유의무 이행률 (의무이행 자동 산출, <span id="tableYr">2026</span>)</h6>
        <button class="btn btn-outline-success btn-sm" id="excelExportBtn">
          <i class="bi bi-file-earmark-excel"></i> 엑셀
        </button>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table aria-label="SAF 혼합비율 모니터링" class="table table-hover table-sm mb-0 table-icas">
            <thead>
              <tr>
                <th class="ps-3" style="width:6%;">No</th>
                <th style="width:15%;">운영사 ID</th>
                <th style="width:18%;">총 급유량 (L)</th>
                <th style="width:18%;">SAF 인증서 구매량 (L)</th>
                <th style="width:12%;">혼합비율 (%)</th>
                <th style="width:14%;">혼합비율 현황</th>
                <th style="width:10%;">의무비율 (%)</th>
                <th style="width:7%;">이행여부</th>
              </tr>
            </thead>
            <tbody id="mntrTableBody">
              <tr><td colspan="8" class="text-center py-4 text-muted small">데이터 로딩 중...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

    </div><!-- /#tabFuelOblg -->
    </div><!-- /.tab-content -->

<!-- ======================================================
     운영사별 단건 산출 모달
====================================================== -->
<div class="modal fade" id="singleCalcModal" tabindex="-1" aria-labelledby="singleCalcModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="singleCalcModalLabel">
          <i class="bi bi-person-check me-1"></i> 운영사별 혼합비율 산출
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="singleCalcForm" novalidate>
          <div class="row g-3">
            <div class="col-md-8">
              <label class="form-label small fw-semibold">운영사 ID <span class="text-danger">*</span></label>
              <input type="text" id="sc_oprtrId" class="form-control form-control-sm"
                     placeholder="예) KAL, AAR, JJA" required maxlength="20">
              <div class="invalid-feedback">운영사 ID를 입력하세요.</div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">보고연도 <span class="text-danger">*</span></label>
              <select id="sc_rprtYr" class="form-select form-select-sm" required>
                <option value="2026">2026</option>
                <option value="2025">2025</option>
                <option value="2024">2024</option>
              </select>
            </div>
          </div>
          <div class="mt-3 p-2 bg-light rounded small text-muted">
            <i class="bi bi-info-circle me-1"></i>
            해당 운영사의 SAF 구매량 / 총 급유량을 집계하여 혼합비율을 재산출하고 DB에 반영합니다.
            의무비율(1.0%) 이상이면 이행 여부 Y로 자동 처리됩니다.
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="singleCalcSubmitBtn">
          <i class="bi bi-calculator me-1"></i> 산출 실행
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     전체 일괄 산출 확인 모달
====================================================== -->
<div class="modal fade" id="calcAllModal" tabindex="-1" aria-labelledby="calcAllModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="calcAllModalLabel">
          <i class="bi bi-calculator me-1"></i> 운영사 전체 일괄 산출 확인
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-warning py-2 small mb-3">
          <i class="bi bi-exclamation-triangle-fill me-1"></i>
          현재 조회된 모든 운영사의 혼합비율을 일괄 재산출합니다.
          기존 산출 결과가 덮어쓰입니다.
        </div>
        <p class="small mb-1">
          대상 보고연도: <strong id="calcAllYrDisplay" class="text-primary"></strong>
        </p>
        <p class="small mb-0">
          대상 운영사: <strong id="calcAllOprtrCount" class="text-primary"></strong>건
        </p>
        <div id="calcAllProgressArea" class="mt-3" style="display:none;">
          <div class="d-flex align-items-center gap-2 small text-muted">
            <div class="spinner-border spinner-border-sm text-primary"></div>
            <span id="calcAllProgressMsg">산출 진행 중...</span>
          </div>
          <div class="progress mt-2" style="height:6px;">
            <div id="calcAllProgressBar" class="progress-bar bg-primary" style="width:0%;transition:width 0.3s;"></div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal" id="calcAllCancelBtn">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="calcAllConfirmBtn">
          <i class="bi bi-play-fill me-1"></i> 일괄 산출 시작
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

/* 현재 테이블에 표시된 운영사 목록 캐시 */
var currentOprtrList = [];

function renderTable(list) {
  currentOprtrList = list || [];
  if (!list || list.length === 0) {
    $('#mntrTableBody').html('<tr><td colspan="8" class="text-center py-4 text-muted small">데이터가 없습니다.</td></tr>');
    $('#cntFulfilled, #cntShort, #avgRatio').text('-');
    return;
  }
  var fulfilled = list.filter(function(r) { return r.fulfilledYn === 'Y'; }).length;
  var shortCnt  = list.length - fulfilled;
  var avgR      = list.reduce(function(a, r) { return a + (parseFloat(r.blndRatio) || 0); }, 0) / list.length;
  var oblg      = parseFloat(list[0].oblgRatio) || 1.0;

  $('#cntFulfilled').text(fulfilled);
  $('#cntShort').text(shortCnt);
  $('#avgRatio').text(avgR.toFixed(2) + '%');
  $('#oblgRatio').text(oblg.toFixed(1) + '%');

  var html = '';
  $.each(list, function(i, r) {
    var ratio    = parseFloat(r.blndRatio) || 0;
    var oblgR    = parseFloat(r.oblgRatio) || 1.0;
    var pct      = Math.min(100, (ratio / (oblgR * 2)) * 100);
    var barColor = r.fulfilledYn === 'Y' ? '#198754' : '#dc3545';
    html += '<tr>'
      + '<td class="ps-3 small text-muted">' + (i + 1) + '</td>'
      + '<td class="small fw-semibold">' + escHtml(r.oprtrId) + '</td>'
      + '<td class="small text-end">' + numFmt(r.totalFuelQty) + '</td>'
      + '<td class="small text-end">' + numFmt(r.safCertPurchQty) + '</td>'
      + '<td class="small fw-bold text-end">' + ratio.toFixed(2) + '%</td>'
      + '<td style="min-width:110px;">'
      +   '<div class="ratio-bar-bg"><div class="ratio-bar" style="width:' + pct + '%;background:' + barColor + ';"></div></div>'
      +   '<div class="small text-muted mt-1" style="font-size:0.70rem;">의무 ' + oblgR.toFixed(1) + '%</div>'
      + '</td>'
      + '<td class="small text-center">' + oblgR.toFixed(1) + '</td>'
      + '<td>'
      +   (r.fulfilledYn === 'Y'
          ? '<span class="badge badge-fulfilled">&#10003; 이행</span>'
          : '<span class="badge badge-short">&#10007; 미달</span>')
      + '</td>'
      + '</tr>';
  });
  $('#mntrTableBody').html(html);
}

function loadData(yr) {
  $('#tableYr').text(yr);
  $('#mntrTableBody').html('<tr><td colspan="8" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary me-2"></div>산출 중...</td></tr>');
  $.get('/api/saf/mntr/blnd/all?rprtYr=' + yr)
    .done(function(res) { renderTable(res.data || res); })
    .fail(function(xhr) {
      $('#mntrTableBody').html('<tr><td colspan="8" class="text-center py-4 text-danger small">데이터 조회 오류 (HTTP ' + xhr.status + ')</td></tr>');
    });
}

$(function() {
  loadData($('#filterYr').val());

  $('#filterYr').on('change', function() { loadData($(this).val()); });

  /* 운영사별 단건 산출 모달 열기 */
  $('#singleCalcBtn').on('click', function() {
    $('#singleCalcForm')[0].reset();
    $('#singleCalcForm').removeClass('was-validated');
    $('#sc_rprtYr').val($('#filterYr').val());
    new bootstrap.Modal('#singleCalcModal').show();
  });

  $('#singleCalcSubmitBtn').on('click', function() {
    var $form = $('#singleCalcForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;

    var oprtrId = $('#sc_oprtrId').val().trim();
    var rprtYr  = $('#sc_rprtYr').val();
    var $btn    = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>산출 중...');

    $.ajax({
      url         : '/api/saf/mntr/blnd/calc',
      type        : 'POST',
      contentType : 'application/json',
      data        : JSON.stringify({ oprtrId: oprtrId, rprtYr: rprtYr })
    })
    .done(function(res) {
      bootstrap.Modal.getInstance('#singleCalcModal').hide();
      IcasAlert.success(res.message || oprtrId + ' 혼합비율이 산출되었습니다.');
      loadData(rprtYr);
    })
    .fail(function(xhr) {
      var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
      IcasAlert.error('산출 실패: ' + msg);
    })
    .always(function() { $btn.prop('disabled', false).html('<i class="bi bi-calculator me-1"></i> 산출 실행'); });
  });

  /* 전체 일괄 산출 모달 열기 */
  $('#calcAllBtn').on('click', function() {
    var yr = $('#filterYr').val();
    $('#calcAllYrDisplay').text(yr);
    $('#calcAllOprtrCount').text(currentOprtrList.length || '?');
    $('#calcAllProgressArea').hide();
    $('#calcAllProgressBar').css('width', '0%');
    $('#calcAllConfirmBtn').prop('disabled', false).html('<i class="bi bi-play-fill me-1"></i> 일괄 산출 시작');
    $('#calcAllCancelBtn').prop('disabled', false);
    new bootstrap.Modal('#calcAllModal').show();
  });

  /* 전체 일괄 산출 실행 — 운영사 목록 순회 */
  $('#calcAllConfirmBtn').on('click', function() {
    var yr     = $('#filterYr').val();
    var $btn   = $(this);
    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>실행 중...');
    $('#calcAllCancelBtn').prop('disabled', true);
    $('#calcAllProgressArea').show();

    /* 현재 테이블에 운영사가 있으면 순회 산출, 없으면 일괄 API 1회 호출 */
    var oprtrIds = currentOprtrList.map(function(r) { return r.oprtrId; });
    if (oprtrIds.length === 0) {
      /* 목록이 없으면 rprtYr 만 보내서 서버 측 전체 처리 */
      $.ajax({ url: '/api/saf/mntr/blnd/calc', type: 'POST', contentType: 'application/json',
               data: JSON.stringify({ rprtYr: yr }) })
        .done(function() {
          bootstrap.Modal.getInstance('#calcAllModal').hide();
          IcasAlert.success('혼합비율이 산출되었습니다.');
          loadData(yr);
        })
        .fail(function(xhr) {
          var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
          IcasAlert.error('산출 실패: ' + msg);
          $btn.prop('disabled', false).html('<i class="bi bi-play-fill me-1"></i> 일괄 산출 시작');
          $('#calcAllCancelBtn').prop('disabled', false);
        });
      return;
    }

    /* 운영사 순회 순차 처리 */
    var total   = oprtrIds.length;
    var done    = 0;
    var errList = [];

    function calcNext(idx) {
      if (idx >= oprtrIds.length) {
        $('#calcAllProgressBar').css('width', '100%');
        setTimeout(function() {
          bootstrap.Modal.getInstance('#calcAllModal').hide();
          var msg = '전체 ' + total + '개 운영사 산출 완료.';
          if (errList.length) msg += ' (실패: ' + errList.join(', ') + ')';
          IcasAlert.success(msg);
          loadData(yr);
        }, 300);
        return;
      }
      var oId = oprtrIds[idx];
      $('#calcAllProgressMsg').text('(' + (idx+1) + '/' + total + ') ' + oId + ' 산출 중...');
      $('#calcAllProgressBar').css('width', Math.round(((idx+1)/total)*100) + '%');

      $.ajax({ url: '/api/saf/mntr/blnd/calc', type: 'POST', contentType: 'application/json',
               data: JSON.stringify({ oprtrId: oId, rprtYr: yr }) })
        .fail(function() { errList.push(oId); })
        .always(function() { done++; calcNext(idx + 1); });
    }
    calcNext(0);
  });

  $('#excelExportBtn').on('click', function() {
    location.href = '/api/saf/mntr/blnd/excel?rprtYr=' + $('#filterYr').val();
  });
});
</script>
</body>
</html>
