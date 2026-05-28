<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>대시보드 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.summary-card { border-radius: 12px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.summary-card .icon-wrap { width:52px; height:52px; border-radius:12px; display:flex; align-items:center; justify-content:center; font-size:1.4rem; }
.table-status th { background: #0F2C72; color: white; font-weight:500; font-size:0.82rem; }
.status-badge { font-size:0.72rem; padding:3px 8px; border-radius:4px; font-weight:600; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="container-fluid p-4">

    <!-- 페이지 타이틀 -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">대시보드</h5>
        <small class="text-muted">보고연도 2026 기준 현황</small>
      </div>
      <span class="badge" style="background:#0F2C72;">2026년 기준</span>
    </div>

    <!-- 요약 카드 4개 -->
    <div class="row g-3 mb-4">
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8eeff;">&#9992;</div>
            <div>
              <div class="text-muted small">총 운영사 수</div>
              <div class="fs-3 fw-bold" style="color:#0F2C72;" id="summaryTotalOprtr">12</div>
              <div class="text-muted" style="font-size:0.75rem;">ICAO 등록 항공사</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8f5e9;">&#128203;</div>
            <div>
              <div class="text-muted small">보고서 제출율</div>
              <div class="fs-3 fw-bold text-success" id="summaryErRate">75%</div>
              <div class="text-muted" style="font-size:0.75rem;">9/12 운영사 제출 완료</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#e8f5e9;">&#127807;</div>
            <div>
              <div class="text-muted small">SAF 이행율</div>
              <div class="fs-3 fw-bold text-success" id="summarySafRate">58%</div>
              <div class="text-muted" style="font-size:0.75rem;">7/12 운영사 기준 충족</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="card summary-card h-100">
          <div class="card-body d-flex align-items-center gap-3">
            <div class="icon-wrap" style="background:#fff3e0;">&#9729;</div>
            <div>
              <div class="text-muted small">2026 총 CO&#8322; (tCO&#8322;)</div>
              <div class="fs-3 fw-bold text-warning" id="summaryCo2">4,821,305</div>
              <div class="text-muted" style="font-size:0.75rem;">전년 대비 +3.2%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-3 mb-4">
      <!-- 워크플로우 현황 테이블 -->
      <div class="col-xl-8">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">&#128260; 워크플로우 현황 (2026)</h6>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover table-sm mb-0 table-status">
                <thead>
                  <tr>
                    <th class="ps-3">운영사</th>
                    <th>ICAO</th>
                    <th>ER</th>
                    <th>CEF</th>
                    <th>EUCR</th>
                    <th>VR</th>
                    <th>SAF</th>
                  </tr>
                </thead>
                <tbody id="workflowTableBody">
                  <tr>
                    <td colspan="7" class="text-center py-3 text-muted small">데이터 로딩 중...</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- 빠른 링크 -->
      <div class="col-xl-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">&#9889; 빠른 메뉴</h6>
          </div>
          <div class="card-body">
            <div class="d-grid gap-2">
              <a href="/er/list" class="btn btn-outline-primary btn-sm text-start">
                <i class="bi bi-file-earmark-text me-2"></i>배출량보고서(ER) 목록
              </a>
              <a href="/vr/list" class="btn btn-outline-success btn-sm text-start">
                <i class="bi bi-check2-square me-2"></i>검증보고서(VR) 목록
              </a>
              <a href="/er/cef/list" class="btn btn-outline-warning btn-sm text-start">
                <i class="bi bi-fuel-pump me-2"></i>적격연료(CEF) 목록
              </a>
              <a href="/er/eucr/list" class="btn btn-outline-danger btn-sm text-start">
                <i class="bi bi-credit-card me-2"></i>배출권취소(EUCR) 목록
              </a>
              <a href="/saf/dashboard" class="btn btn-outline-success btn-sm text-start">
                <i class="bi bi-leaf me-2"></i>SAF 이행현황
              </a>
              <a href="/ptl/workflow" class="btn btn-sm text-start text-white" style="background:#0F2C72;">
                <i class="bi bi-diagram-3 me-2"></i>통합 워크플로우
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- CO2 배출량 연도별 추이 차트 -->
    <div class="row g-3">
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white border-bottom py-3">
            <h6 class="fw-bold mb-0" style="color:#0F2C72;">&#128200; CO&#8322; 배출량 연도별 추이 (tCO&#8322;)</h6>
          </div>
          <div class="card-body">
            <div id="co2TrendChart" style="height:300px;"></div>
          </div>
        </div>
      </div>
    </div>

  </div><!-- /container-fluid -->
</div><!-- /main content -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"></script>
<script>
// 상태 배지 렌더링
function renderStatusBadge(status) {
  if (!status) return '<span class="badge status-badge bg-light text-muted border">미작성</span>';
  const map = {
    'DRAFT':  ['bg-secondary', '작성중'],
    'SBMTD':  ['bg-primary',   '제출'],
    'RVWNG':  ['bg-warning text-dark', '검토중'],
    'RJCTD':  ['bg-danger',    '반려'],
    'APRVD':  ['bg-success',   '승인']
  };
  const [cls, label] = map[status] || ['bg-secondary', status];
  return '<span class="badge status-badge ' + cls + '">' + label + '</span>';
}

// 샘플 데이터
const sampleWorkflow = [
  {oprtrNm:'대한항공', icaoCd:'KAL', erSttsCd:'APRVD', cefSttsCd:'APRVD', eucrSttsCd:'SBMTD', vrSttsCd:'RVWNG', safSttsCd:'APRVD'},
  {oprtrNm:'아시아나항공', icaoCd:'AAR', erSttsCd:'SBMTD', cefSttsCd:'SBMTD', eucrSttsCd:'SBMTD', vrSttsCd:'SBMTD', safSttsCd:'RVWNG'},
  {oprtrNm:'제주항공', icaoCd:'JJA', erSttsCd:'RVWNG', cefSttsCd:'APRVD', eucrSttsCd:'DRAFT', vrSttsCd:null, safSttsCd:'SBMTD'},
  {oprtrNm:'진에어', icaoCd:'JNA', erSttsCd:'DRAFT', cefSttsCd:'DRAFT', eucrSttsCd:null, vrSttsCd:null, safSttsCd:'DRAFT'},
  {oprtrNm:'티웨이항공', icaoCd:'TWB', erSttsCd:'SBMTD', cefSttsCd:'SBMTD', eucrSttsCd:'SBMTD', vrSttsCd:'RVWNG', safSttsCd:'SBMTD'},
  {oprtrNm:'에어부산', icaoCd:'ABL', erSttsCd:'RJCTD', cefSttsCd:'APRVD', eucrSttsCd:'SBMTD', vrSttsCd:null, safSttsCd:'DRAFT'},
];

function renderWorkflowTable(data) {
  const tbody = $('#workflowTableBody');
  if (!data || data.length === 0) {
    tbody.html('<tr><td colspan="7" class="text-center py-3 text-muted small">데이터가 없습니다.</td></tr>');
    return;
  }
  let html = '';
  data.forEach(function(row) {
    html += '<tr>'
      + '<td class="ps-3 fw-semibold small">' + (row.oprtrNm || row.oprtrId) + '</td>'
      + '<td class="small text-muted">' + (row.icaoDesig || row.icaoCd || '-') + '</td>'
      + '<td>' + renderStatusBadge(row.erStCd || row.erSttsCd) + '</td>'
      + '<td>' + renderStatusBadge(row.cefStCd || row.cefSttsCd) + '</td>'
      + '<td>' + renderStatusBadge(row.eucrStCd || row.eucrSttsCd) + '</td>'
      + '<td>' + renderStatusBadge(row.vrStCd || row.vrSttsCd) + '</td>'
      + '<td>' + renderStatusBadge(row.oomStCd || row.safSttsCd) + '</td>'
      + '</tr>';
  });
  tbody.html(html);
}

// CO2 추이 차트
function initCo2Chart(data) {
  const chart = echarts.init(document.getElementById('co2TrendChart'));
  const years  = data ? data.years  : ['2022','2023','2024','2025','2026'];
  const values = data ? data.values : [4120000, 4350000, 4580000, 4672000, 4821305];
  chart.setOption({
    tooltip: { trigger:'axis', formatter: function(p) { return p[0].name + '년<br/>CO&#8322;: ' + p[0].value.toLocaleString() + ' tCO&#8322;'; } },
    grid: { left:'5%', right:'3%', bottom:'3%', containLabel:true },
    xAxis: { type:'category', data:years, axisLabel:{ formatter:'{value}년' } },
    yAxis: { type:'value', axisLabel:{ formatter: function(v){ return (v/10000).toFixed(0) + '만'; } } },
    series: [{
      type:'line', data:values, smooth:true, symbol:'circle', symbolSize:8,
      itemStyle:{ color:'#0F2C72' },
      lineStyle:{ color:'#0F2C72', width:3 },
      areaStyle:{ color:'rgba(15,44,114,0.1)' }
    }]
  });
  window.addEventListener('resize', function(){ chart.resize(); });
}

$(function(){
  // 워크플로우 데이터 로드
  $.get('/api/ptl/workflow?rprtYr=2026')
    .done(function(res){ renderWorkflowTable(res.data || res); })
    .fail(function(){ renderWorkflowTable(sampleWorkflow); });

  // CO2 통계 차트 로드
  $.get('/api/ptl/stat/2026')
    .done(function(res){ initCo2Chart(res); })
    .fail(function(){ initCo2Chart(null); });
});
</script>
</body>
</html>
