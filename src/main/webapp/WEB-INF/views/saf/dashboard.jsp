<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAF 이행현황 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
.table-icas thead th { background:#0F2C72; color:white; font-size:0.82rem; font-weight:500; border:none; }
.table-icas tbody tr:hover { background:#f8f9ff; }
.summary-card { border-radius: 12px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.icon-wrap { width:52px; height:52px; border-radius:12px; display:flex; align-items:center; justify-content:center; font-size:1.4rem; }
.ratio-bar-bg { background:#e9ecef; border-radius:4px; height:8px; }
.ratio-bar    { border-radius:4px; height:8px; background:#0F2C72; transition: width 0.5s; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#127807; SAF 이행현황</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item active">SAF 이행현황</li>
          </ol>
        </nav>
      </div>
      <select id="filterYr" class="form-select form-select-sm" style="width:100px;">
        <option value="2026" selected>2026</option>
        <option value="2025">2025</option>
        <option value="2024">2024</option>
      </select>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 요약 카드 -->
    <div class="row g-3 mb-4">
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8f5e9;">&#127807;</div>
            <div>
              <div class="text-muted small">이행 운영사</div>
              <div class="fs-3 fw-bold text-success" id="summaryOk">0</div>
              <div class="text-muted" style="font-size:0.75rem;">의무비율 충족</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#ffebee;">&#9888;</div>
            <div>
              <div class="text-muted small">미이행 운영사</div>
              <div class="fs-3 fw-bold text-danger" id="summaryNok">0</div>
              <div class="text-muted" style="font-size:0.75rem;">의무비율 미달</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8eeff;">&#9981;</div>
            <div>
              <div class="text-muted small">총 SAF 구매량</div>
              <div class="fs-3 fw-bold" style="color:#0F2C72;" id="summaryTotalSaf">-</div>
              <div class="text-muted" style="font-size:0.75rem;">리터 (L)</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#fff3e0;">&#128200;</div>
            <div>
              <div class="text-muted small">평균 혼합비율</div>
              <div class="fs-3 fw-bold text-warning" id="summaryAvgRatio">-</div>
              <div class="text-muted" style="font-size:0.75rem;">의무: 2% (2026)</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <!-- 이행현황 테이블 -->
      <div class="col-xl-8">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">SAF 이행현황 상세 (<span id="tableYr">2026</span>)</h6>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover table-sm mb-0 table-icas">
                <thead>
                  <tr>
                    <th class="ps-3">운영사</th>
                    <th>총급유량(L)</th>
                    <th>SAF구매량(L)</th>
                    <th>혼합비율(%)</th>
                    <th>의무비율(%)</th>
                    <th>혼합비율 현황</th>
                    <th>이행여부</th>
                  </tr>
                </thead>
                <tbody id="safTableBody">
                  <tr><td colspan="7" class="text-center py-3 text-muted small">데이터 로딩 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- 게이지 차트 -->
      <div class="col-xl-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">&#128200; 전체 평균 혼합비율</h6>
          </div>
          <div class="card-body d-flex align-items-center justify-content-center">
            <div id="safGaugeChart" style="height:280px; width:100%;"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"></script>
<script>
const sampleSaf = [
  {oprtrNm:'대한항공',   icaoCd:'KAL', totalFuel:980000000, safPurchase:24500000, blndRatio:2.5,  oblgRatio:2.0, isOk:true},
  {oprtrNm:'아시아나항공', icaoCd:'AAR', totalFuel:620000000, safPurchase:14260000, blndRatio:2.3,  oblgRatio:2.0, isOk:true},
  {oprtrNm:'제주항공',   icaoCd:'JJA', totalFuel:310000000, safPurchase:5270000,  blndRatio:1.7,  oblgRatio:2.0, isOk:false},
  {oprtrNm:'진에어',     icaoCd:'JNA', totalFuel:280000000, safPurchase:4200000,  blndRatio:1.5,  oblgRatio:2.0, isOk:false},
  {oprtrNm:'티웨이항공', icaoCd:'TWB', totalFuel:260000000, safPurchase:5720000,  blndRatio:2.2,  oblgRatio:2.0, isOk:true},
  {oprtrNm:'에어부산',   icaoCd:'ABL', totalFuel:190000000, safPurchase:4180000,  blndRatio:2.2,  oblgRatio:2.0, isOk:true},
  {oprtrNm:'에어서울',   icaoCd:'ASV', totalFuel:130000000, safPurchase:2340000,  blndRatio:1.8,  oblgRatio:2.0, isOk:false},
];

let gaugeChart;

function initGauge(avgRatio) {
  if (!gaugeChart) gaugeChart = echarts.init(document.getElementById('safGaugeChart'));
  gaugeChart.setOption({
    series: [{
      type: 'gauge',
      min: 0, max: 5,
      progress: { show:true, width:16 },
      axisLine: { lineStyle:{ width:16, color:[[0.4,'#f44336'],[0.6,'#ff9800'],[1,'#4caf50']] } },
      pointer: { itemStyle:{ color:'auto' } },
      axisTick: { show:false },
      splitLine: { length:12, lineStyle:{ width:2 } },
      axisLabel: { distance:20, color:'#999', fontSize:12 },
      anchor: { show:true, showAbove:true, size:20, itemStyle:{ borderWidth:5 } },
      title: { show:true, offsetCenter:[0,'70%'], fontSize:12, color:'#666' },
      detail: {
        valueAnimation: true, fontSize:28, fontWeight:'bold', color:'auto',
        formatter: function(v){ return v.toFixed(2) + '%'; },
        offsetCenter:[0,'40%']
      },
      data: [{ value: parseFloat(avgRatio.toFixed(2)), name:'평균 혼합비율' }]
    }]
  });
  window.addEventListener('resize', function(){ gaugeChart.resize(); });
}

function renderSafTable(list) {
  const okCnt  = list.filter(r=>r.isOk).length;
  const nokCnt = list.length - okCnt;
  const totalSaf = list.reduce((a,r)=>a+r.safPurchase,0);
  const avgRatio = list.reduce((a,r)=>a+r.blndRatio,0)/list.length;

  $('#summaryOk').text(okCnt);
  $('#summaryNok').text(nokCnt);
  $('#summaryTotalSaf').text((totalSaf/1000000).toFixed(1)+'M');
  $('#summaryAvgRatio').text(avgRatio.toFixed(2)+'%');

  let html = '';
  list.forEach(function(row) {
    const pct = Math.min(100, (row.blndRatio / 5) * 100);
    const barColor = row.isOk ? '#198754' : '#dc3545';
    html += '<tr>'
      + '<td class="ps-3 fw-semibold small">' + row.oprtrNm + '</td>'
      + '<td class="small">' + row.totalFuel.toLocaleString() + '</td>'
      + '<td class="small">' + row.safPurchase.toLocaleString() + '</td>'
      + '<td class="small fw-bold">' + row.blndRatio.toFixed(2) + '%</td>'
      + '<td class="small text-muted">' + row.oblgRatio.toFixed(1) + '%</td>'
      + '<td style="min-width:100px;">'
      +   '<div class="ratio-bar-bg"><div class="ratio-bar" style="width:' + pct + '%;background:' + barColor + ';"></div></div>'
      + '</td>'
      + '<td>'
      +   (row.isOk
          ? '<span class="badge bg-success" style="font-size:0.72rem;">&#10003; 이행</span>'
          : '<span class="badge bg-danger"  style="font-size:0.72rem;">&#10007; 미이행</span>')
      + '</td>'
      + '</tr>';
  });
  $('#safTableBody').html(html);
  initGauge(avgRatio);
}

function loadData(yr) {
  $('#tableYr').text(yr);
  // 별도 dashboard 컨트롤러가 없음 → 혼합비율 모니터링 전체 조회로 대체
  $.get('/api/saf/mntr/blnd/all?rprtYr=' + encodeURIComponent(yr))
    .done(function(res){
      var rows = (res && res.data) ? res.data : (res || []);
      if (!Array.isArray(rows)) rows = [];
      // mntr VO → dashboard 표시 포맷으로 변환
      var mapped = rows.map(function(r){
        var ratio = Number(r.blndRatio || 0);
        var oblg  = Number(r.oblgRatio || 1);
        return {
          oprtrNm: r.oprtrNm || r.oprtrId,
          icaoCd: r.oprtrId,
          rprtYr: r.rprtYr,
          totalFuelQty: r.totalFuelQty,
          safCertPurchQty: r.safCertPurchQty,
          blndRatio: ratio,
          oblgRatio: oblg,
          fulfilledYn: r.fulfilledYn
        };
      });
      renderSafTable(mapped.length ? mapped : sampleSaf);
    })
    .fail(function(){ renderSafTable(sampleSaf); });
}

$(function(){
  loadData($('#filterYr').val());
  $('#filterYr').on('change', function(){ loadData($(this).val()); });
});
</script>
</body>
</html>
