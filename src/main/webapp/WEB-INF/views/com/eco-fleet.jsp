<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>친환경 항공기 도입 트래커 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>body { background:#f5f7fb; }
.kpi-card { background:#fff; border-radius:10px; padding:18px; box-shadow:0 2px 8px rgba(15,44,114,0.06); border-left:4px solid #10B981; }
.kpi-label { font-size:0.72rem; color:#6b7280; text-transform:uppercase; letter-spacing:0.05em; }
.kpi-value { font-size:1.8rem; font-weight:700; color:#0F2C72; }
.kpi-sub { font-size:0.78rem; color:#9ca3af; }
.progress-card { background:#fff; border-radius:10px; padding:16px 20px; box-shadow:0 2px 8px rgba(15,44,114,0.06); }
.progress-card h6 { color:#0F2C72; margin-bottom:8px; font-size:0.95rem; font-weight:600; }
.progress-bar { transition: width .6s ease; }
.fleet-tbl th { background:#0F2C72; color:#fff; font-size:0.82rem; font-weight:500; }
.fleet-tbl td { font-size:0.88rem; }
.fleet-tbl tbody tr:nth-child(odd) { background:#fafbfd; }
.aircraft-badge { display:inline-block; font-size:0.7rem; padding:2px 8px; border-radius:10px; margin-right:4px; }
.aircraft-badge.b737   { background:#dbeafe; color:#1e40af; }
.aircraft-badge.a321   { background:#dcfce7; color:#166534; }
.aircraft-badge.b787   { background:#fef3c7; color:#92400e; }
.aircraft-badge.a350   { background:#fce7f3; color:#9f1239; }
.aircraft-badge.a330   { background:#ede9fe; color:#5b21b6; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="p-4">

    <div class="d-flex align-items-center justify-content-between mb-3">
      <div>
        <h5 class="fw-bold mb-1" style="color:#0F2C72;"><i class="bi bi-airplane-engines me-2"></i>친환경 항공기 도입 트래커</h5>
        <div class="text-muted small">2026년 국제항공 탄소 배출량 관리 시행계획 p.8 — 국적 항공사별 도입 계획 vs 실적</div>
      </div>
      <select class="form-select form-select-sm" style="width:100px;" id="yrSel">
        <option>2026</option><option>2025</option><option>2024</option>
      </select>
    </div>

    <!-- KPI 카드 -->
    <div class="row g-3 mb-4">
      <div class="col-md-3"><div class="kpi-card"><div class="kpi-label">'25년 보유</div><div class="kpi-value">138 <small style="font-size:0.8rem;">대</small></div><div class="kpi-sub">친환경 항공기 (비중 25.2%)</div></div></div>
      <div class="col-md-3"><div class="kpi-card"><div class="kpi-label">'26년 목표</div><div class="kpi-value">178 <small style="font-size:0.8rem;">대</small></div><div class="kpi-sub">'25 대비 +40대</div></div></div>
      <div class="col-md-3"><div class="kpi-card" style="border-left-color:#3B82F6;"><div class="kpi-label">'26년 실제 도입</div><div class="kpi-value">28 <small style="font-size:0.8rem;">대</small></div><div class="kpi-sub">70% 진척 (40대 중)</div></div></div>
      <div class="col-md-3"><div class="kpi-card" style="border-left-color:#F59E0B;"><div class="kpi-label">'30년 목표</div><div class="kpi-value">5% <small style="font-size:0.8rem;">감축</small></div><div class="kpi-sub">친환경 항공기 기여도 (BAU 대비)</div></div></div>
    </div>

    <!-- 운영사별 진척 -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header bg-white py-3">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-bar-chart me-2"></i>국적 항공사별 친환경 항공기 도입 진척 (2026)</h6>
      </div>
      <div class="card-body p-0">
        <table class="table mb-0 fleet-tbl">
          <thead>
            <tr>
              <th class="ps-3" style="width:14%;">운영사</th>
              <th style="width:18%;">'26 도입 계획</th>
              <th style="width:14%;">실제 도입</th>
              <th style="width:32%;">진척률</th>
              <th>기종별 (계획 → 실적)</th>
            </tr>
          </thead>
          <tbody id="fleetTbody">
            <!-- JS 채움 -->
          </tbody>
          <tfoot style="background:#f5f7fb;font-weight:600;">
            <tr>
              <td class="ps-3">합계</td>
              <td><span id="totalPlan">40</span> 대</td>
              <td><span id="totalActual">28</span> 대</td>
              <td>
                <div class="progress" style="height:18px;">
                  <div class="progress-bar bg-success" id="totalBar" style="width:70%;">70%</div>
                </div>
              </td>
              <td class="text-muted small">5사 합산 도입계획 (시행계획 p.8 기준)</td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>

    <!-- 기종별 비교 -->
    <div class="row g-3">
      <div class="col-lg-7">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-graph-up me-2"></i>기종별 도입 분포</h6>
          </div>
          <div class="card-body">
            <div id="aircraftChart" style="height:280px;"></div>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-info-circle me-2"></i>친환경 항공기 정의</h6>
          </div>
          <div class="card-body small text-muted">
            <p>친환경 항공기는 <strong>구형 대비 연료 효율 15% 이상 개선</strong>된 차세대 기종을 의미합니다.</p>
            <ul class="ps-3">
              <li><strong>B737-8</strong> (737 MAX 8) — Boeing 차세대 협폭기</li>
              <li><strong>A321neo</strong> — Airbus 신엔진 옵션</li>
              <li><strong>B787-10</strong> — 드림라이너 광폭기</li>
              <li><strong>A350-900</strong> — XWB 광폭기</li>
              <li><strong>A330neo</strong> — Airbus 차세대 광폭기</li>
            </ul>
            <hr class="my-2">
            <p class="mb-0">
              운수권 배분 시 친환경 항공기 도입 우수 항공사에 <strong>가점 부여</strong> (시행계획 p.8).
            </p>
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
// 시행계획 p.8 — 2026년 국적 항공사별 친환경 항공기 도입계획 (5사 총 40대)
var FLEET = [
  { id:'KAL', nm:'대한항공',   plan:{B737:1, A321:6, B787:1, A350:7, A330:3}, actual:{B737:1, A321:4, B787:0, A350:5, A330:2}, total:18, actualTotal:12 },
  { id:'AAR', nm:'아시아나항공', plan:{B737:0, A321:0, B787:0, A350:0, A330:0}, actual:{B737:0, A321:0, B787:0, A350:0, A330:0}, total:0,  actualTotal:0  },
  { id:'JJA', nm:'제주항공',   plan:{B737:5, A321:0, B787:0, A350:0, A330:0}, actual:{B737:4, A321:0, B787:0, A350:0, A330:0}, total:5,  actualTotal:4  },
  { id:'JNA', nm:'진에어',     plan:{B737:0, A321:0, B787:0, A350:0, A330:0}, actual:{B737:0, A321:0, B787:0, A350:0, A330:0}, total:0,  actualTotal:0  },
  { id:'TWB', nm:'티웨이항공', plan:{B737:10,A321:4, B787:0, A350:0, A330:0}, actual:{B737:7, A321:3, B787:0, A350:0, A330:0}, total:14, actualTotal:10 },
  { id:'ESR', nm:'이스타항공', plan:{B737:3, A321:0, B787:0, A350:0, A330:0}, actual:{B737:2, A321:0, B787:0, A350:0, A330:0}, total:3,  actualTotal:2  }
];

var html = '';
FLEET.forEach(function(f){
  var pct = f.total > 0 ? Math.round(f.actualTotal / f.total * 100) : 0;
  var barClass = pct >= 80 ? 'bg-success' : pct >= 50 ? 'bg-info' : pct >= 1 ? 'bg-warning' : 'bg-secondary';
  var planTxt = '';
  ['B737','A321','B787','A350','A330'].forEach(function(t){
    if (f.plan[t] > 0) planTxt += '<span class="aircraft-badge ' + t.toLowerCase() + '">' + t + ' ' + f.actual[t] + '/' + f.plan[t] + '</span>';
  });
  if (!planTxt) planTxt = '<span class="text-muted small">도입 계획 없음</span>';
  html += '<tr>'
       + '<td class="ps-3 fw-semibold">' + f.nm + '</td>'
       + '<td><span class="fs-6 fw-bold text-primary">' + f.total + '</span> <small class="text-muted">대</small></td>'
       + '<td><span class="fs-6 fw-bold">' + f.actualTotal + '</span> <small class="text-muted">대</small></td>'
       + '<td><div class="progress" style="height:18px;"><div class="progress-bar ' + barClass + '" style="width:' + pct + '%;">' + pct + '%</div></div></td>'
       + '<td>' + planTxt + '</td>'
       + '</tr>';
});
$('#fleetTbody').html(html);

// 기종별 합계 차트
var sumPlan = {B737:0, A321:0, B787:0, A350:0, A330:0};
var sumActual = {B737:0, A321:0, B787:0, A350:0, A330:0};
FLEET.forEach(function(f){ ['B737','A321','B787','A350','A330'].forEach(function(t){ sumPlan[t]+=f.plan[t]; sumActual[t]+=f.actual[t]; }); });

var chart = echarts.init(document.getElementById('aircraftChart'));
chart.setOption({
  tooltip: { trigger:'axis', axisPointer:{type:'shadow'} },
  legend:  { data:['계획','실적'], bottom:0 },
  grid:    { left:36, right:24, top:24, bottom:32 },
  xAxis:   { type:'category', data:['B737-8','A321neo','B787-10','A350-900','A330neo'] },
  yAxis:   { type:'value', name:'대수' },
  series: [
    { name:'계획', type:'bar', barWidth:'32%', itemStyle:{color:'#cbd5e1'}, data:[sumPlan.B737, sumPlan.A321, sumPlan.B787, sumPlan.A350, sumPlan.A330], label:{show:true,position:'top'} },
    { name:'실적', type:'bar', barWidth:'32%', itemStyle:{color:'#0F2C72'}, data:[sumActual.B737, sumActual.A321, sumActual.B787, sumActual.A350, sumActual.A330], label:{show:true,position:'top'} }
  ]
});
window.addEventListener('resize', function(){ chart.resize(); });
</script>
</body>
</html>
