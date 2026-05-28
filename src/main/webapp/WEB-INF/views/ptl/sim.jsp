<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>상쇄비용 시뮬레이션 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-sim thead th { background: #0F2C72; color: white; font-size: 0.8rem; font-weight: 500; border: none; white-space: nowrap; }
.table-sim tbody tr:hover { background: #f8f9ff; cursor: pointer; }
.table-sim td { vertical-align: middle; font-size: 0.85rem; }
.param-label { font-size: 0.8rem; font-weight: 600; color: #495057; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3">
    <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-calculator me-1"></i>상쇄비용 시뮬레이션</h5>
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb mb-0 small">
        <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
        <li class="breadcrumb-item active">상쇄비용 시뮬레이션</li>
      </ol>
    </nav>
  </div>

  <div class="container-fluid p-4">
    <div class="row g-4">
      <!-- 입력 폼 -->
      <div class="col-xl-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-sliders me-1"></i>시뮬레이션 설정</h6>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <label class="param-label">시뮬레이션명 <span class="text-danger">*</span></label>
              <input type="text" id="simNm" class="form-control form-control-sm" placeholder="예: 2026년 기준 시나리오A" maxlength="100">
            </div>
            <div class="row g-2 mb-3">
              <div class="col-6">
                <label class="param-label">기준연도 <span class="text-danger">*</span></label>
                <select id="baseYr" class="form-select form-select-sm">
                  <option value="2026" selected>2026</option>
                  <option value="2025">2025</option>
                  <option value="2024">2024</option>
                </select>
              </div>
              <div class="col-3">
                <label class="param-label">예측 시작</label>
                <select id="prdctnYrFrom" class="form-select form-select-sm">
                  <option value="2027" selected>2027</option>
                  <option value="2028">2028</option>
                </select>
              </div>
              <div class="col-3">
                <label class="param-label">예측 종료</label>
                <select id="prdctnYrTo" class="form-select form-select-sm">
                  <option value="2030" selected>2030</option>
                  <option value="2031">2031</option>
                  <option value="2032">2032</option>
                </select>
              </div>
            </div>
            <div class="mb-3">
              <label class="param-label">대상범위</label>
              <select id="scopeSeCd" class="form-select form-select-sm">
                <option value="ALL" selected>전체 운영사</option>
                <option value="OPRTR">특정 운영사</option>
              </select>
            </div>
            <div class="mb-3" id="oprtrRow" style="display:none;">
              <label class="param-label">운영사 ID</label>
              <input type="text" id="scopeOprtrId" class="form-control form-control-sm" placeholder="예: OP001">
            </div>
            <hr class="my-3">
            <div class="mb-3">
              <label class="param-label">탄소가격 (USD/tCO&#8322;e)</label>
              <input type="number" id="paramCarbonPrice" class="form-control form-control-sm" value="25.00" step="0.5" min="0">
            </div>
            <div class="mb-3">
              <label class="param-label">연간 성장률 (%)</label>
              <input type="number" id="paramGrowthRate" class="form-control form-control-sm" value="3.5" step="0.1" min="0" max="20">
            </div>
            <div class="mb-3">
              <label class="param-label">SAF 혼합비율 (%)</label>
              <input type="number" id="paramSafRatio" class="form-control form-control-sm" value="2.0" step="0.1" min="0" max="10">
            </div>
            <div class="mb-3">
              <label class="param-label">공유범위</label>
              <select id="shareSeCd" class="form-select form-select-sm">
                <option value="PRIVATE" selected>비공개</option>
                <option value="ORG">기관공유</option>
                <option value="PUBLIC">전체공개</option>
              </select>
            </div>
            <div id="simFormMsg" class="mb-2 small"></div>
            <button id="btnRunSim" class="btn w-100" style="background:#0F2C72;color:white;">
              <i class="bi bi-play-fill me-1"></i>시뮬레이션 실행
            </button>
          </div>
        </div>
      </div>

      <!-- 결과 차트 -->
      <div class="col-xl-8">
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-bar-chart-line me-1"></i>시뮬레이션 결과</h6>
            <span id="currentSimId" class="badge bg-light text-muted border">결과 없음</span>
          </div>
          <div class="card-body">
            <div id="simChartArea">
              <div class="d-flex align-items-center justify-content-center" style="height:300px; color:#adb5bd;">
                <div class="text-center">
                  <i class="bi bi-bar-chart fs-1 d-block mb-2"></i>
                  <span class="small">시뮬레이션을 실행하면 결과가 표시됩니다.</span>
                </div>
              </div>
            </div>
            <div id="simChartWrap" style="display:none;">
              <div class="row g-3">
                <div class="col-12">
                  <div id="simLineChart" style="height:280px;"></div>
                </div>
                <div class="col-12">
                  <div id="simBarChart" style="height:220px;"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 시뮬레이션 이력 그리드 -->
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-bottom py-3 d-flex align-items-center justify-content-between">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-clock-history me-1"></i>시뮬레이션 이력</h6>
            <button id="btnReloadHist" class="btn btn-sm btn-outline-secondary">
              <i class="bi bi-arrow-clockwise me-1"></i>새로고침
            </button>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover table-sm mb-0 table-sim">
                <thead>
                  <tr>
                    <th class="ps-3" style="width:100px;">SM 번호</th>
                    <th>시뮬레이션명</th>
                    <th style="width:80px;">기준연도</th>
                    <th style="width:100px;">대상범위</th>
                    <th style="width:110px;">공유범위</th>
                    <th>등록일시</th>
                    <th style="width:80px;">액션</th>
                  </tr>
                </thead>
                <tbody id="simTableBody">
                  <tr><td colspan="7" class="text-center py-4 text-muted small">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>데이터 로딩 중...
                  </td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script src="https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"></script>
<script>
var lineChart = null;
var barChart  = null;

var SCOPE_LABEL = { 'ALL': '전체', 'OPRTR': '운영사' };
var SHARE_LABEL = { 'PRIVATE': '비공개', 'ORG': '기관공유', 'PUBLIC': '전체공개' };

$('#scopeSeCd').on('change', function() {
  $('#oprtrRow').toggle($(this).val() === 'OPRTR');
});

function buildInputJson() {
  return JSON.stringify({
    carbonPrice:  parseFloat($('#paramCarbonPrice').val()) || 25.0,
    growthRate:   parseFloat($('#paramGrowthRate').val())  || 3.5,
    safRatio:     parseFloat($('#paramSafRatio').val())    || 2.0
  });
}

function renderCharts(rsltJson, simNm) {
  var rslt;
  try { rslt = typeof rsltJson === 'string' ? JSON.parse(rsltJson) : rsltJson; } catch(e) { rslt = null; }

  // 결과 데이터 없으면 샘플로 표시
  if (!rslt || !rslt.years) {
    var baseYr = parseInt($('#baseYr').val()) || 2026;
    rslt = { years: [], offsetCost: [], emissions: [] };
    for (var y = baseYr; y <= baseYr + 4; y++) {
      rslt.years.push(String(y));
      rslt.emissions.push(Math.round(4800000 + (y - baseYr) * 120000));
      rslt.offsetCost.push(Math.round((4800000 + (y - baseYr) * 120000) * 0.25 * 25));
    }
  }

  $('#simChartArea').hide();
  $('#simChartWrap').show();

  // 라인 차트 - 상쇄비용 추이
  if (!lineChart) lineChart = echarts.init(document.getElementById('simLineChart'));
  lineChart.setOption({
    title: { text: simNm + ' — 상쇄비용 추이', textStyle: { fontSize: 13, color: '#0F2C72' } },
    tooltip: { trigger: 'axis' },
    grid: { left: '5%', right: '3%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: rslt.years, axisLabel: { formatter: '{value}년' } },
    yAxis: { type: 'value', name: 'USD', axisLabel: { formatter: function(v){ return (v/1000000).toFixed(1)+'M'; } } },
    series: [{
      name: '상쇄비용',
      type: 'line', smooth: true, data: rslt.offsetCost,
      lineStyle: { color: '#0F2C72', width: 2 },
      areaStyle: { color: 'rgba(15,44,114,0.08)' },
      itemStyle: { color: '#0F2C72' },
      label: { show: true, position: 'top', fontSize: 10, formatter: function(p){ return (p.value/1000000).toFixed(2)+'M'; } }
    }]
  });

  // 막대 차트 - 연도별 배출량
  if (!barChart) barChart = echarts.init(document.getElementById('simBarChart'));
  barChart.setOption({
    title: { text: '연도별 배출량 (tCO₂e)', textStyle: { fontSize: 13, color: '#0F2C72' } },
    tooltip: { trigger: 'axis' },
    grid: { left: '5%', right: '3%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: rslt.years, axisLabel: { formatter: '{value}년' } },
    yAxis: { type: 'value', axisLabel: { formatter: function(v){ return (v/10000).toFixed(0)+'만'; } } },
    series: [{
      type: 'bar', data: rslt.emissions, barMaxWidth: 50,
      itemStyle: { color: '#5b8af0' },
      label: { show: true, position: 'top', fontSize: 10, formatter: function(p){ return (p.value/10000).toFixed(0)+'만'; } }
    }]
  });

  window.addEventListener('resize', function() {
    if (lineChart) lineChart.resize();
    if (barChart)  barChart.resize();
  });
}

function loadSimHistory() {
  $.get('/api/ptl/sim?page=0&size=10')
    .done(function(res) {
      var list = (res.data && res.data.rows || res.data.content) ? res.data.rows || res.data.content : (res.data || []);
      renderHistTable(list);
    })
    .fail(function(xhr) {
      $('#simTableBody').html('<tr><td colspan="7" class="text-center py-3 text-danger small"><i class="bi bi-exclamation-triangle me-1"></i>조회 실패 (HTTP ' + xhr.status + ')</td></tr>');
    });
}

function renderHistTable(list) {
  if (!list || !list.length) {
    $('#simTableBody').html('<tr><td colspan="7" class="text-center py-4 text-muted small">조회된 시뮬레이션이 없습니다.</td></tr>');
    return;
  }
  var html = '';
  function escAttr(v) { return $('<div>').text(v == null ? '' : v).html().replace(/"/g, '&quot;'); }
  list.forEach(function(row) {
    var simIdSafe = escAttr(row.simId);
    var simNmSafe = escAttr(row.simNm);
    html += '<tr>'
      + '<td class="ps-3 fw-semibold">' + $('<span>').text(row.simId || '-').html() + '</td>'
      + '<td>' + $('<span>').text(row.simNm || '-').html() + '</td>'
      + '<td class="text-center">' + $('<span>').text(row.baseYr || '-').html() + '</td>'
      + '<td class="text-center">' + (SCOPE_LABEL[row.scopeSeCd] || $('<span>').text(row.scopeSeCd || '-').html()) + '</td>'
      + '<td class="text-center"><span class="badge bg-light text-dark border">' + (SHARE_LABEL[row.shareSeCd] || $('<span>').text(row.shareSeCd || '-').html()) + '</span></td>'
      + '<td class="text-muted small">' + (row.frstRegDt ? row.frstRegDt.replace('T',' ').substring(0,19) : '-') + '</td>'
      + '<td><button class="btn btn-xs btn-sm btn-outline-primary py-0 px-1 small btn-load-result" data-simid="' + simIdSafe + '" data-simnm="' + simNmSafe + '"><i class="bi bi-eye"></i></button>'
      + ' <button class="btn btn-xs btn-sm btn-outline-danger py-0 px-1 small btn-del-sim" data-simid="' + simIdSafe + '"><i class="bi bi-trash"></i></button></td>'
      + '</tr>';
  });
  $('#simTableBody').html(html);
}

function runSimulation() {
  var nm = $('#simNm').val().trim();
  if (!nm) { $('#simFormMsg').html('<span class="text-danger">시뮬레이션명을 입력하세요.</span>'); return; }

  var body = {
    simNm:        nm,
    baseYr:       $('#baseYr').val(),
    prdctnYrFrom: $('#prdctnYrFrom').val(),
    prdctnYrTo:   $('#prdctnYrTo').val(),
    scopeSeCd:    $('#scopeSeCd').val(),
    scopeOprtrId: $('#scopeOprtrId').val() || null,
    inputJson:    buildInputJson(),
    shareSeCd:    $('#shareSeCd').val()
  };

  $('#btnRunSim').prop('disabled', true);
  $('#simFormMsg').html('<span class="text-muted"><span class="spinner-border spinner-border-sm me-1"></span>실행 중...</span>');

  $.ajax({
    url: '/api/ptl/sim',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(body),
    success: function(res) {
      var vo = res.data || {};
      $('#currentSimId').text(vo.simId || 'SM????').removeClass('text-muted').addClass('text-primary');
      $('#simFormMsg').html('<span class="text-success"><i class="bi bi-check-circle me-1"></i>' + $('<span>').text(res.message || '완료').html() + '</span>');
      renderCharts(vo.rsltJson, nm);
      loadSimHistory();
    },
    error: function(xhr) {
      var errMsg = '시뮬레이션 실패';
      try { errMsg = JSON.parse(xhr.responseText).message || errMsg; } catch(e) {}
      $('#simFormMsg').html('<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>' + $('<span>').text(errMsg).html() + '</span>');
    },
    complete: function() {
      $('#btnRunSim').prop('disabled', false);
    }
  });
}

$(document).on('click', '.btn-load-result', function() {
  var simId = $(this).data('simid');
  var simNm = $(this).data('simnm');
  $.get('/api/ptl/sim/' + encodeURIComponent(simId))
    .done(function(res) {
      var vo = res.data || {};
      $('#currentSimId').text(vo.simId || simId).removeClass('text-muted').addClass('text-primary');
      renderCharts(vo.rsltJson, simNm);
    })
    .fail(function(xhr) {
      IcasAlert.error('조회 실패 (HTTP ' + xhr.status + ')');
    });
});

$(document).on('click', '.btn-del-sim', function() {
  var simId = $(this).data('simid');
  if (!confirm('[' + simId + '] 시뮬레이션을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({
    url: '/api/ptl/sim/' + encodeURIComponent(simId),
    type: 'DELETE',
    success: function() { loadSimHistory(); },
    error: function(xhr) { IcasAlert.error('삭제 실패 (HTTP ' + xhr.status + ')'); }
  });
});

$(function() {
  loadSimHistory();
  $('#btnRunSim').on('click', runSimulation);
  $('#btnReloadHist').on('click', loadSimHistory);
});
</script>
</body>
</html>
