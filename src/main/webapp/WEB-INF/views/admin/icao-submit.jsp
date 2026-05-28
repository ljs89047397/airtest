<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ICAO 송신 콘솔 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>body { background:#f5f7fb; }
.code-block { background:#0F172A; color:#e2e8f0; padding:16px; border-radius:8px; font-family:'SF Mono',Consolas,monospace; font-size:0.78rem; overflow-x:auto; max-height:280px; overflow-y:auto; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="p-4">

    <h5 class="fw-bold mb-3" style="color:#0F2C72;"><i class="bi bi-cloud-upload me-2"></i>ICAO 배출량 보고서 송신 콘솔</h5>
    <div class="alert alert-warning small py-2 px-3">
      <i class="bi bi-info-circle me-1"></i>
      <strong>Mock 인터페이스 (1차년도 명세 구현, 실 송신은 ICAO API 협의 후 활성화)</strong> ·
      시행계획 p.13 — 매년 7월 ICAO 제출 의무
    </div>

    <div class="row g-3 mb-3">
      <div class="col-md-6">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3"><h6 class="fw-bold mb-0" style="color:#0F2C72;">송신 대상 설정</h6></div>
          <div class="card-body">
            <div class="mb-3">
              <label class="form-label small fw-semibold">보고연도</label>
              <select id="rprtYr" class="form-select form-select-sm"><option>2025</option><option>2024</option></select>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-semibold">송신 대상 운영사</label>
              <select id="oprtrSel" class="form-select form-select-sm" multiple style="height:120px;">
                <option value="KAL" selected>대한항공 (KAL)</option>
                <option value="AAR" selected>아시아나항공 (AAR)</option>
                <option value="JJA" selected>제주항공 (JJA)</option>
                <option value="JNA">진에어 (JNA)</option>
                <option value="TWB">티웨이항공 (TWB)</option>
                <option value="ESR">이스타항공 (ESR)</option>
                <option value="ABL">에어부산 (ABL)</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-semibold">송신 채널</label>
              <select id="channel" class="form-select form-select-sm">
                <option value="CCB">ICAO CORSIA Central Repository (CCR)</option>
                <option value="SARP">ICAO SARP eSubmission</option>
              </select>
            </div>
            <div class="d-flex gap-2">
              <button class="btn btn-primary" id="btnPreview"><i class="bi bi-eye me-1"></i>미리보기</button>
              <button class="btn btn-success" id="btnSubmit" disabled><i class="bi bi-send me-1"></i>송신 (Mock)</button>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-6">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3"><h6 class="fw-bold mb-0" style="color:#0F2C72;">송신 페이로드 (ICAO XML 표준)</h6></div>
          <div class="card-body">
            <div class="code-block" id="payloadBlock">미리보기 버튼을 누르세요.</div>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white py-3"><h6 class="fw-bold mb-0" style="color:#0F2C72;">송신 이력</h6></div>
      <div class="card-body p-0">
        <table class="table table-sm mb-0">
          <thead style="background:#f5f7fb;"><tr><th class="ps-3">송신 일시</th><th>보고연도</th><th>대상</th><th>채널</th><th>응답코드</th><th>응답메시지</th></tr></thead>
          <tbody id="histTbody">
            <tr><td colspan="6" class="text-center text-muted py-4">아직 송신 이력이 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
$('#btnPreview').on('click', function(){
  var yr = $('#rprtYr').val();
  var sel = $('#oprtrSel').val();
  var ch = $('#channel').val();
  var xml = '<?xml version="1.0" encoding="UTF-8"?>\n'
    + '<CORSIASubmission xmlns="http://www.icao.int/CORSIA/2020/1.0">\n'
    + '  <ReportingState>KOR</ReportingState>\n'
    + '  <ReportingYear>' + yr + '</ReportingYear>\n'
    + '  <SubmissionDate>' + new Date().toISOString().slice(0,10) + '</SubmissionDate>\n'
    + '  <SubmittingAuthority>MOLIT</SubmittingAuthority>\n'
    + '  <Channel>' + ch + '</Channel>\n'
    + '  <Operators>\n';
  sel.forEach(function(o){
    xml += '    <Operator>\n'
        + '      <ICAOCode>' + o + '</ICAOCode>\n'
        + '      <TotalEmissions unit="tCO2">XXXXXX</TotalEmissions>\n'
        + '      <OffsetRequirement unit="tCO2">XXXX</OffsetRequirement>\n'
        + '      <SAFCredit unit="L">XXXX</SAFCredit>\n'
        + '      <VerificationStatus>VERIFIED</VerificationStatus>\n'
        + '    </Operator>\n';
  });
  xml += '  </Operators>\n</CORSIASubmission>';
  $('#payloadBlock').text(xml);
  $('#btnSubmit').prop('disabled', false);
});
$('#btnSubmit').on('click', function(){
  if (!window.IcasAlert || !IcasAlert.confirm) {
    if (!confirm('실제 ICAO 송신은 비활성 상태입니다. Mock 송신을 시뮬레이션할까요?')) return;
    doMockSubmit();
  } else {
    IcasAlert.confirm('실제 ICAO 송신은 비활성 상태입니다. Mock 송신을 시뮬레이션할까요?', doMockSubmit);
  }
});
function doMockSubmit(){
  var now = new Date().toLocaleString('ko-KR');
  var yr = $('#rprtYr').val();
  var sel = $('#oprtrSel').val().join(',');
  var ch = $('#channel').val();
  var row = '<tr><td class="ps-3">' + now + '</td><td>' + yr + '</td><td>' + sel + '</td><td>' + ch + '</td>'
          + '<td><span class="badge bg-success">200 OK (Mock)</span></td>'
          + '<td class="small text-muted">Submission accepted. RefId: MOCK-' + Date.now() + '</td></tr>';
  var $tb = $('#histTbody');
  if ($tb.find('tr').length === 1 && $tb.find('td').length === 1) $tb.empty();
  $tb.prepend(row);
  if (window.IcasAlert) IcasAlert.success('ICAO Mock 송신이 기록되었습니다.');
}
</script>
</body>
</html>
