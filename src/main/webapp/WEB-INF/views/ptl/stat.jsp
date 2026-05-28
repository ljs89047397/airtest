<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>통계/시뮬레이션 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="page-header-bar px-4 py-3">
    <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#128200; 통계/시뮬레이션</h5>
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb mb-0 small">
        <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
        <li class="breadcrumb-item active">통계/시뮬레이션</li>
      </ol>
    </nav>
  </div>

  <div class="container-fluid p-4">
    <div class="row g-3">
      <!-- 연도별 배출량 추이 -->
      <div class="col-xl-8">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">CO&#8322; 배출량 연도별 추이</h6>
          </div>
          <div class="card-body">
            <div id="trendChart" style="height:320px;"></div>
          </div>
        </div>
      </div>

      <!-- 운영사별 배출량 비중 -->
      <div class="col-xl-4">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">운영사별 배출량 비중 (2026)</h6>
          </div>
          <div class="card-body">
            <div id="pieChart" style="height:320px;"></div>
          </div>
        </div>
      </div>

      <!-- 감축수단별 기여도 (시행계획 p.7 — BAU 대비 10%) -->
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">&#127757; 감축수단별 기여도 (BAU 대비 목표 10% 감축)</h6>
            <span class="badge bg-light text-dark border" style="font-size:0.7rem;">시행계획 2026 p.7</span>
          </div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-md-7">
                <div id="reductChart" style="height:280px;"></div>
              </div>
              <div class="col-md-5">
                <table class="table table-sm small mb-0">
                  <thead style="background:#f5f7fb;">
                    <tr>
                      <th>감축 수단</th>
                      <th class="text-end">목표 기여도</th>
                      <th class="text-end">현재 실적</th>
                      <th class="text-end">달성률</th>
                    </tr>
                  </thead>
                  <tbody id="reductTblBody">
                    <!-- JS 채움 -->
                  </tbody>
                  <tfoot style="background:#f5f7fb;font-weight:600;">
                    <tr>
                      <td>합계</td>
                      <td class="text-end" id="reductTotalGoal">10.0%</td>
                      <td class="text-end" id="reductTotalAct">-</td>
                      <td class="text-end" id="reductTotalRate">-</td>
                    </tr>
                  </tfoot>
                </table>
                <div class="small text-muted mt-2">
                  ※ 기준: BAU(’26) 24.7백만 t · 목표(’30) 29.8백만 t의 10% = 약 2.98백만 t 감축
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- SAF 혼합비율 시뮬레이션 -->
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">&#127807; SAF 혼합비율 시뮬레이션</h6>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-end mb-3">
              <div class="col-auto">
                <label class="form-label small fw-semibold">SAF 의무비율 (%)</label>
                <input type="number" id="simRatio" class="form-control form-control-sm" value="2.0" step="0.1" min="0" max="10" style="width:100px;">
              </div>
              <div class="col-auto">
                <button id="btnSim" class="btn btn-sm" style="background:#0F2C72;color:white;">
                  <i class="bi bi-calculator me-1"></i>시뮬레이션 실행
                </button>
              </div>
            </div>
            <div id="simResult" class="table-responsive"></div>
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
const sampleTrend = {
  years:  ['2022','2023','2024','2025','2026'],
  values: [4120000, 4350000, 4580000, 4672000, 4821305]
};
const samplePie = [
  {name:'대한항공',   value:2100000},
  {name:'아시아나항공', value:980000},
  {name:'제주항공',   value:620000},
  {name:'티웨이항공', value:480000},
  {name:'기타',      value:641305},
];
const sampleOprtr = [
  {oprtrNm:'대한항공',   totalFuel:980000000, blndRatio:2.5},
  {oprtrNm:'아시아나항공', totalFuel:620000000, blndRatio:2.3},
  {oprtrNm:'제주항공',   totalFuel:310000000, blndRatio:1.7},
  {oprtrNm:'진에어',     totalFuel:280000000, blndRatio:1.5},
  {oprtrNm:'티웨이항공', totalFuel:260000000, blndRatio:2.2},
  {oprtrNm:'에어부산',   totalFuel:190000000, blndRatio:2.2},
  {oprtrNm:'에어서울',   totalFuel:130000000, blndRatio:1.8},
];

// 추이 차트
function initTrend(data) {
  const chart = echarts.init(document.getElementById('trendChart'));
  chart.setOption({
    tooltip:{ trigger:'axis' },
    grid:{ left:'5%', right:'3%', bottom:'3%', containLabel:true },
    xAxis:{ type:'category', data:data.years, axisLabel:{ formatter:'{value}년' } },
    yAxis:{ type:'value', axisLabel:{ formatter:function(v){ return (v/10000).toFixed(0)+'만'; } } },
    series:[{
      type:'bar', data:data.values, barMaxWidth:50,
      itemStyle:{ color:function(p){ return p.dataIndex===data.years.length-1?'#0F2C72':'#5b8af0'; } },
      label:{ show:true, position:'top', fontSize:11, formatter:function(p){ return (p.value/10000).toFixed(0)+'만'; } }
    }]
  });
  window.addEventListener('resize', function(){ chart.resize(); });
}

// 파이 차트
function initPie(data) {
  const chart = echarts.init(document.getElementById('pieChart'));
  chart.setOption({
    tooltip:{ trigger:'item', formatter:'{b}: {c}({d}%)' },
    legend:{ bottom:0, type:'scroll' },
    series:[{
      type:'pie', radius:['40%','70%'], center:['50%','45%'],
      data: data,
      label:{ formatter:'{b}\n{d}%' }
    }]
  });
  window.addEventListener('resize', function(){ chart.resize(); });
}

// 시뮬레이션
function runSimulation() {
  const oblgRatio = parseFloat($('#simRatio').val()) || 2.0;
  let html = '<table class="table table-sm table-bordered mb-0" style="font-size:0.82rem;">'
    + '<thead style="background:#0F2C72;color:white;"><tr>'
    + '<th class="ps-3">운영사</th><th>총급유량(L)</th><th>현재비율(%)</th>'
    + '<th>의무비율(%)</th><th>추가필요SAF(L)</th><th>이행여부</th>'
    + '</tr></thead><tbody>';
  sampleOprtr.forEach(function(row) {
    const needSaf = row.blndRatio < oblgRatio
      ? Math.ceil((oblgRatio - row.blndRatio) / 100 * row.totalFuel)
      : 0;
    const isOk = row.blndRatio >= oblgRatio;
    html += '<tr>'
      + '<td class="ps-3 fw-semibold">' + row.oprtrNm + '</td>'
      + '<td>' + row.totalFuel.toLocaleString() + '</td>'
      + '<td class="fw-bold' + (isOk?' text-success':' text-danger') + '">' + row.blndRatio.toFixed(2) + '%</td>'
      + '<td>' + oblgRatio.toFixed(1) + '%</td>'
      + '<td class="' + (needSaf>0?'text-danger fw-bold':'text-success') + '">' + (needSaf>0?'+'+needSaf.toLocaleString():'충족') + '</td>'
      + '<td>' + (isOk?'<span class="badge bg-success">이행</span>':'<span class="badge bg-danger">미이행</span>') + '</td>'
      + '</tr>';
  });
  html += '</tbody></table>';
  $('#simResult').html(html);
}

// 감축수단별 기여도 (시행계획 p.7 - BAU 대비 10%)
function initReductChart() {
  // 목표/현재 시드 (시행계획 비율 + 현재 운영사 평균 혼합비율 2.03% 반영)
  // 목표: SAF 4% / 친환경 항공기 5% / 운항 효율 1% (총 10%)
  // 현재 실적: SAF 2.03%/4 ≈ 0.51 / 친환경 비중 25.2%×0.2 ≈ 5%↓일부 / 운항 효율 ≈ 0.3%
  var goal = { saf: 4.0, acft: 5.0, ops: 1.0 };
  var actual = { saf: 2.03, acft: 5.0, ops: 0.3 };
  var rate = function(a, g){ return g > 0 ? Math.min(100, Math.round(a/g*100)) : 0; };

  // 차트
  var chart = echarts.init(document.getElementById('reductChart'));
  chart.setOption({
    tooltip: { trigger:'axis', axisPointer:{type:'shadow'} },
    legend:  { data:['목표','현재 실적'], bottom:0 },
    grid:    { left:48, right:24, top:30, bottom:36 },
    xAxis:   { type:'category', data:['SAF','친환경 항공기','운항 효율화'], axisLabel:{fontSize:11} },
    yAxis:   { type:'value', name:'기여도 (%)', max:6 },
    series: [
      { name:'목표',     type:'bar', barWidth:'30%', data:[goal.saf, goal.acft, goal.ops], itemStyle:{color:'#cbd5e1'}, label:{show:true,position:'top',formatter:'{c}%'} },
      { name:'현재 실적', type:'bar', barWidth:'30%', data:[actual.saf, actual.acft, actual.ops], itemStyle:{color:'#0F2C72'}, label:{show:true,position:'top',formatter:'{c}%'} }
    ]
  });
  window.addEventListener('resize', function(){ chart.resize(); });

  // 테이블
  var rows = [
    { nm:'SAF 혼합 사용',     g:goal.saf,  a:actual.saf  },
    { nm:'친환경 항공기 도입', g:goal.acft, a:actual.acft },
    { nm:'항공운항 효율화',   g:goal.ops,  a:actual.ops  }
  ];
  var totalG = 0, totalA = 0;
  var tbody = '';
  rows.forEach(function(r){
    var pct = rate(r.a, r.g);
    var cls = pct >= 100 ? 'text-success fw-bold' : (pct >= 50 ? 'text-warning fw-bold' : 'text-danger fw-bold');
    tbody += '<tr>'
      + '<td>' + r.nm + '</td>'
      + '<td class="text-end">' + r.g.toFixed(1) + '%</td>'
      + '<td class="text-end">' + r.a.toFixed(2) + '%</td>'
      + '<td class="text-end ' + cls + '">' + pct + '%</td>'
      + '</tr>';
    totalG += r.g; totalA += r.a;
  });
  $('#reductTblBody').html(tbody);
  $('#reductTotalGoal').text(totalG.toFixed(1) + '%');
  $('#reductTotalAct').text(totalA.toFixed(2) + '%');
  var totalRate = Math.round(totalA/totalG*100);
  $('#reductTotalRate').text(totalRate + '%').addClass(totalRate >= 80 ? 'text-success' : 'text-warning');
}

$(function(){
  $.get('/api/ptl/stat/2026')
    .done(function(r){ initTrend(r.trend||sampleTrend); initPie(r.pie||samplePie); })
    .fail(function(){ initTrend(sampleTrend); initPie(samplePie); });
  initReductChart();

  runSimulation();
  $('#btnSim').on('click', runSimulation);
});
</script>
</body>
</html>
