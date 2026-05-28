<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CORSIA 세부항목 검증 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
.rule-badge { font-size: 0.7rem; padding: 2px 6px; border-radius: 3px; font-weight: 600; display:inline-block; min-width: 56px; text-align:center; }
.rule-pass { background:#d4edda; color:#155724; }
.rule-warn { background:#fff3cd; color:#856404; }
.rule-fail { background:#f8d7da; color:#721c24; }
.rule-na   { background:#e2e3e5; color:#383d41; }
.summary-card { border-radius: 12px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#128203; CORSIA 세부항목 검증</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/er/list" class="text-decoration-none">배출량관리</a></li>
            <li class="breadcrumb-item active">CORSIA 세부항목 검증 (RFP 박스 ⑦)</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- 요약 카드 -->
    <div class="row g-3 mb-3">
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body py-2 px-3">
            <div class="text-muted small">총 검증 건</div>
            <div class="fs-4 fw-bold" id="cntTotal" style="color:#0F2C72;">-</div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body py-2 px-3">
            <div class="text-muted small">PASS</div>
            <div class="fs-4 fw-bold text-success" id="cntPass">-</div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body py-2 px-3">
            <div class="text-muted small">FAIL / HOLD</div>
            <div class="fs-4 fw-bold text-danger" id="cntFail">-</div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card summary-card">
          <div class="card-body py-2 px-3">
            <div class="text-muted small">진행중</div>
            <div class="fs-4 fw-bold text-warning" id="cntInprg">-</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 필터 -->
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
            <input type="text" id="filterOprtr" class="form-control form-control-sm" placeholder="ICAO" style="width:140px;">
          </div>
          <div class="col-auto">
            <button id="btnSearch" class="btn btn-sm" style="background:#0F2C72;color:white;">
              <i class="bi bi-search me-1"></i>조회
            </button>
            <button id="btnReset" class="btn btn-sm btn-outline-secondary ms-1">초기화</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 목록 -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <table id="qchkGrid" class="table table-icas table-hover mb-0" aria-label="CORSIA 세부항목 검증 목록">
          <thead>
            <tr>
              <th style="width:90px;">검토ID</th>
              <th style="width:80px;">연도</th>
              <th>운영사</th>
              <th style="width:90px;">상태</th>
              <th style="width:90px;">판정</th>
              <th style="width:80px;">항목수</th>
              <th style="width:80px;">PASS</th>
              <th style="width:80px;">WARN</th>
              <th style="width:80px;">FAIL</th>
              <th style="width:120px;">최종실행</th>
              <th style="width:80px;">관리</th>
            </tr>
          </thead>
          <tbody id="qchkTbody">
            <tr><td colspan="11" class="text-center text-muted py-4">조회 중...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Rule 18종 안내 -->
    <div class="card border-0 shadow-sm mt-3">
      <div class="card-header bg-white py-2 px-3 fw-semibold" style="color:#0F2C72;">정량 검증 18종</div>
      <div class="card-body py-2 px-3 small">
        <div class="row">
          <div class="col-md-4">
            R001 ICAO 지정어 · R002 제출기한 · R003 ER-VR 일치성 · R004 작성일자 · R005 보고의무(1만톤) · R006 CERT 일계치
          </div>
          <div class="col-md-4">
            R007 연료유형 · R008 등록기호 중복 · R009 국가쌍 분류 · R010 국가쌍-연료 · R011 국내선 오류 · R012 연료소비 이상치
          </div>
          <div class="col-md-4">
            R013 CERT 편차 · R014 데이터 갭 초과 · R015 데이터 갭 정합 · R016 검증기관 인증 · R017 팀리더 연속 · R018 전년대비 이상치
          </div>
        </div>
      </div>
    </div>

  </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script src="/resources/js/common/icas-csrf.js"></script>
<script>
(function(){
  var csrfToken  = $('meta[name="_csrf"]').attr('content');
  var csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-XSRF-TOKEN';

  function esc(s){ return (s==null?'':String(s)).replace(/[&<>"']/g, function(c){
    return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]; }); }

  function badgeRslt(s){
    var cls = 'rule-na', txt = s || '-';
    if (s==='PASS') cls='rule-pass';
    else if (s==='FAIL') cls='rule-fail';
    else if (s==='HOLD') cls='rule-warn';
    else if (s==='INPRG') { cls='rule-warn'; txt='진행중'; }
    return '<span class="rule-badge '+cls+'">'+esc(txt)+'</span>';
  }
  function badgeSt(s){
    var cls='rule-na', txt=s||'-';
    if (s==='DONE') cls='rule-pass';
    else if (s==='INPRG') { cls='rule-warn'; txt='진행중'; }
    return '<span class="rule-badge '+cls+'">'+esc(txt)+'</span>';
  }

  function loadList(){
    var params = {
      rprtYr   : $('#filterYr').val(),
      finalRslt: $('#filterRslt').val(),
      oprtrId  : $('#filterOprtr').val()
    };
    $.get('/api/er/oom', params, function(resp){
      var rows = (resp && resp.data) ? resp.data : (resp.list || resp.items || resp || []);
      if (!Array.isArray(rows)) rows = (rows.content || []);
      renderRows(rows);
      renderSummary(rows);
    }).fail(function(xhr){
      $('#qchkTbody').html('<tr><td colspan="11" class="text-center text-danger py-3">조회 실패 ('+xhr.status+')</td></tr>');
    });
  }

  function renderRows(rows){
    if (!rows.length){
      $('#qchkTbody').html('<tr><td colspan="11" class="text-center text-muted py-4">데이터 없음</td></tr>');
      return;
    }
    var html = rows.map(function(r){
      var id = r.oomCheckId || r.checkId || r.oomId || '';
      return '<tr>'
        + '<td><a href="/er/oom/qchk/'+esc(id)+'" class="text-decoration-none fw-semibold">'+esc(id)+'</a></td>'
        + '<td>'+esc(r.rprtYr)+'</td>'
        + '<td>'+esc(r.oprtrNm || r.oprtrId)+'</td>'
        + '<td>'+badgeSt(r.oomStCd || r.stCd)+'</td>'
        + '<td>'+badgeRslt(r.finalRsltCd || r.rsltCd)+'</td>'
        + '<td class="text-end">'+esc(r.itemCnt||0)+'</td>'
        + '<td class="text-end text-success">'+esc(r.passCnt||0)+'</td>'
        + '<td class="text-end text-warning">'+esc(r.warnCnt||0)+'</td>'
        + '<td class="text-end text-danger">'+esc(r.failCnt||0)+'</td>'
        + '<td>'+esc((r.lastRunDt||'').substring(0,16))+'</td>'
        + '<td><a href="/er/oom/qchk/'+esc(id)+'" class="btn btn-sm btn-outline-primary">상세</a></td>'
        + '</tr>';
    }).join('');
    $('#qchkTbody').html(html);
  }

  function renderSummary(rows){
    var t = rows.length, p=0, f=0, ip=0;
    rows.forEach(function(r){
      var rs = r.finalRsltCd || r.rsltCd, st = r.oomStCd || r.stCd;
      if (rs==='PASS') p++;
      else if (rs==='FAIL' || rs==='HOLD') f++;
      if (st==='INPRG') ip++;
    });
    $('#cntTotal').text(t);
    $('#cntPass').text(p);
    $('#cntFail').text(f);
    $('#cntInprg').text(ip);
  }

  $('#btnSearch').on('click', loadList);
  $('#btnReset').on('click', function(){
    $('#filterYr').val('2026');
    $('#filterRslt').val('');
    $('#filterOprtr').val('');
    loadList();
  });

  loadList();
})();
</script>
</body>
</html>
