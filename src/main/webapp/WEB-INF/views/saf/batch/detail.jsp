<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAF 배치 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.detail-label { font-size: 0.80rem; color: #6c757d; font-weight: 500; }
.detail-value { font-size: 0.92rem; font-weight: 600; color: #212529; }
.nav-tabs .nav-link { color: #495057; font-size: 0.88rem; }
.nav-tabs .nav-link.active { color: #0F2C72; font-weight: 600; border-bottom: 2px solid #0F2C72; }
.ghg-calc-box { background: #e8eeff; border: 1px solid #c5d0f7; border-radius: 8px; }
.ghg-result   { font-size: 1.6rem; font-weight: 700; color: #0F2C72; }
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
        <button class="btn btn-sm btn-outline-secondary me-2" onclick="history.back()">
          <i class="bi bi-arrow-left"></i> 목록으로
        </button>
        <h5 class="fw-bold mb-0 d-inline" style="color:#0F2C72;">SAF 배치 상세</h5>
        <nav aria-label="breadcrumb" class="d-inline-block ms-2">
          <ol class="breadcrumb mb-0 small d-inline-flex">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/saf/dashboard" class="text-decoration-none">SAF</a></li>
            <li class="breadcrumb-item"><a href="/saf/batch" class="text-decoration-none">배치 목록</a></li>
            <li class="breadcrumb-item active" id="batchIdBreadcrumb">상세</li>
          </ol>
        </nav>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">
    <!-- 배치 기본정보 카드 -->
    <div class="card border-0 shadow-sm mb-3" id="batchInfoCard">
      <div class="card-header bg-white border-bottom py-2">
        <h6 class="fw-bold mb-0" style="color:#0F2C72;">배치 기본 정보</h6>
      </div>
      <div class="card-body" id="batchInfoBody">
        <div class="text-center py-3 text-muted"><div class="spinner-border spinner-border-sm text-primary"></div> 로딩 중...</div>
      </div>
    </div>

    <!-- 서브도메인 탭 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom py-0">
        <ul class="nav nav-tabs border-0" id="batchTabs">
          <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#tabPrdc">생산·공급</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tabBlndr">혼합</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tabFeed">원료</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tabGhg">GHG 온실가스</a></li>
        </ul>
      </div>
      <div class="tab-content card-body" id="batchTabContent">

        <!-- 탭1: 생산·공급 -->
        <div class="tab-pane fade show active" id="tabPrdc">
          <div class="d-flex justify-content-end mb-2">
            <button class="btn btn-outline-primary btn-sm" id="prdcEditBtn">
              <i class="bi bi-pencil-square me-1"></i> 생산·공급 정보 편집
            </button>
          </div>
          <div id="prdcBody"><div class="text-center py-3 text-muted small">탭 선택 시 데이터가 로드됩니다.</div></div>
        </div>

        <!-- 탭2: 혼합 -->
        <div class="tab-pane fade" id="tabBlndr">
          <div class="d-flex justify-content-end mb-2">
            <button class="btn btn-outline-primary btn-sm" id="blndrEditBtn">
              <i class="bi bi-pencil-square me-1"></i> 혼합 정보 편집
            </button>
          </div>
          <div id="blndrBody"><div class="text-center py-3 text-muted small">로딩 대기 중...</div></div>
        </div>

        <!-- 탭3: 원료 -->
        <div class="tab-pane fade" id="tabFeed">
          <div class="d-flex justify-content-end mb-2">
            <button class="btn btn-outline-primary btn-sm" id="feedEditBtn">
              <i class="bi bi-pencil-square me-1"></i> 원료 정보 편집
            </button>
          </div>
          <div id="feedBody"><div class="text-center py-3 text-muted small">로딩 대기 중...</div></div>
        </div>

        <!-- 탭4: GHG -->
        <div class="tab-pane fade" id="tabGhg">
          <div class="d-flex justify-content-end mb-2">
            <button class="btn btn-outline-primary btn-sm" id="ghgEditBtn">
              <i class="bi bi-pencil-square me-1"></i> GHG 정보 편집
            </button>
          </div>
          <div id="ghgBody"><div class="text-center py-3 text-muted small">로딩 대기 중...</div></div>
        </div>

      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     생산·공급 편집 모달
====================================================== -->
<div class="modal fade" id="prdcModal" tabindex="-1" aria-labelledby="prdcModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="prdcModalLabel">
          <i class="bi bi-factory me-1"></i> 생산·공급 정보 편집
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="prdcForm" novalidate>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">생산사 정보</label>
              <input type="text" id="p_prdcCoInfo" class="form-control form-control-sm"
                     placeholder="생산사명 / 위치" maxlength="500">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">공급사 정보</label>
              <input type="text" id="p_splyCoInfo" class="form-control form-control-sm"
                     placeholder="공급사명 / 위치" maxlength="500">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">생산자 PoS 배치 ID</label>
              <input type="text" id="p_prdcPosBatchId" class="form-control form-control-sm"
                     placeholder="생산자 PoS Batch ID" maxlength="100">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">PoS 발행일</label>
              <input type="date" id="p_prdcPosIsueDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">원료 SAF 수량 (kg)</label>
              <input type="number" id="p_orgnSafQty" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">SAF 생산일</label>
              <input type="date" id="p_safPrdcDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">취득일</label>
              <input type="date" id="p_acqstnDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">생산 위치 주소</label>
              <input type="text" id="p_prdcLcAddr" class="form-control form-control-sm"
                     placeholder="예) Rotterdam, Netherlands" maxlength="500">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="prdcSaveBtn">
          <i class="bi bi-check-lg me-1"></i> 저장
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     혼합 편집 모달
====================================================== -->
<div class="modal fade" id="blndrModal" tabindex="-1" aria-labelledby="blndrModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="blndrModalLabel">
          <i class="bi bi-arrow-left-right me-1"></i> 혼합 정보 편집
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="blndrForm" novalidate>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">혼합사 정보</label>
              <input type="text" id="b_blndrCoInfo" class="form-control form-control-sm"
                     placeholder="혼합사명 / 위치" maxlength="500">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">혼합 위치</label>
              <input type="text" id="b_blndLcAddr" class="form-control form-control-sm"
                     placeholder="혼합 장소 주소" maxlength="500">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">수령일</label>
              <input type="date" id="b_recvDt" class="form-control form-control-sm">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">수령 중량 (kg)</label>
              <input type="number" id="b_recvMass" class="form-control form-control-sm"
                     placeholder="0.000" min="0" step="0.001">
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">연료 유형</label>
              <select id="b_fuelTypeCd" class="form-select form-select-sm">
                <option value="">선택</option>
                <option value="SAF">SAF</option>
                <option value="JET_A">Jet-A</option>
                <option value="JET_A1">Jet-A1</option>
                <option value="JP8">JP-8</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label small fw-semibold">혼합 비율 (%)</label>
              <input type="number" id="b_blndRatio" class="form-control form-control-sm"
                     placeholder="0.00" min="0" max="100" step="0.01">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">운송사 정보</label>
              <input type="text" id="b_trnsprtCoInfo" class="form-control form-control-sm"
                     placeholder="운송사명" maxlength="500">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">중간 구매자 정보</label>
              <input type="text" id="b_midBuyerCoInfo" class="form-control form-control-sm"
                     placeholder="중간 구매자명" maxlength="500">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="blndrSaveBtn">
          <i class="bi bi-check-lg me-1"></i> 저장
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     원료 편집 모달
====================================================== -->
<div class="modal fade" id="feedModal" tabindex="-1" aria-labelledby="feedModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="feedModalLabel">
          <i class="bi bi-leaf me-1"></i> 원료 정보 편집
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="feedForm" novalidate>
          <div class="row g-3">
            <div class="col-md-4">
              <label class="form-label small fw-semibold">원료 유형 (fdstk) <span class="text-danger">*</span></label>
              <select id="f_fdstkTypeCd" class="form-select form-select-sm" required>
                <option value="">선택</option>
                <option value="UCO">UCO (사용된 식용유)</option>
                <option value="TALLOW">TALLOW (수지)</option>
                <option value="PFAD">PFAD (팜유 지방산 증류물)</option>
                <option value="CORN_OIL">CORN_OIL (옥수수유)</option>
                <option value="SUGARCANE">SUGARCANE (사탕수수)</option>
                <option value="MSW">MSW (도시 고형 폐기물)</option>
                <option value="OTHER">기타</option>
              </select>
              <div class="invalid-feedback">원료 유형을 선택하세요.</div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">전환 공정</label>
              <select id="f_convProcCd" class="form-select form-select-sm">
                <option value="">선택</option>
                <option value="HVO">HVO (수소화 식물유)</option>
                <option value="ATJ">ATJ (알코올→젯 연료)</option>
                <option value="FT">FT (피셔-트롭시)</option>
                <option value="SIP">SIP (발효 이소파라핀)</option>
                <option value="CHJ">CHJ (공동 수소처리)</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">폐기물·잔류물 여부</label>
              <select id="f_wasteResidueYn" class="form-select form-select-sm">
                <option value="N">N (해당 없음)</option>
                <option value="Y">Y (폐기물·잔류물)</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">추가 원료 상세</label>
              <input type="text" id="f_addlFdstkDtl" class="form-control form-control-sm"
                     placeholder="추가 원료 설명" maxlength="500">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">원산지 국가코드 (쉼표 구분)</label>
              <input type="text" id="f_orgnCntryCds" class="form-control form-control-sm"
                     placeholder="예) KR,US,BR" maxlength="200">
              <div class="form-text small text-muted">ISO alpha-2 코드를 쉼표로 구분하여 입력 (예: KR,US)</div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="feedSaveBtn">
          <i class="bi bi-check-lg me-1"></i> 저장
        </button>
      </div>
    </div>
  </div>
</div>

<!-- ======================================================
     GHG 편집 모달
====================================================== -->
<div class="modal fade" id="ghgModal" tabindex="-1" aria-labelledby="ghgModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;">
        <h6 class="modal-title fw-bold text-white" id="ghgModalLabel">
          <i class="bi bi-cloud-haze2 me-1"></i> GHG 온실가스 정보 편집
        </h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <form id="ghgForm" novalidate>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">GHG 값 구분 <span class="text-danger">*</span></label>
              <select id="g_ghgValSeCd" class="form-select form-select-sm" required>
                <option value="DEFAULT">DEFAULT (기본값)</option>
                <option value="ACTUAL">ACTUAL (실측값)</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Core LCA 기본값 (gCO2eq/MJ) <span class="text-danger">*</span></label>
              <input type="number" id="g_coreLcaDefVal" class="form-control form-control-sm"
                     placeholder="예) 29.43" step="0.0001" required>
              <div class="invalid-feedback">Core LCA 값을 입력하세요.</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">ILUC 배출량 (gCO2eq/MJ) <span class="text-danger">*</span></label>
              <input type="number" id="g_ilucEmsn" class="form-control form-control-sm"
                     placeholder="예) 5.50" step="0.0001" required>
              <div class="invalid-feedback">ILUC 배출량을 입력하세요.</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">총 LCA 기본값 (자동 산출)</label>
              <input type="text" id="g_ttlLcaDefVal_display" class="form-control form-control-sm bg-light"
                     readonly placeholder="Core + ILUC 자동 계산">
              <div class="form-text small text-muted">= Core LCA + ILUC 자동 계산</div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary btn-sm" id="ghgSaveBtn">
          <i class="bi bi-check-lg me-1"></i> 저장
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
var BATCH_ID = location.pathname.split('/').pop();
var loaded   = { prdc: false, blndr: false, feed: false, ghg: false };

function escHtml(s) { return $('<div>').text(s == null ? '-' : String(s)).html(); }
function numFmt(v)  { return (v == null) ? '-' : Number(v).toLocaleString(); }

/* ── 배치 기본정보 ─────────────────── */
function renderBatchInfo(d) {
  $('#batchIdBreadcrumb').text(d.batchId || BATCH_ID);
  var html = '<div class="row g-3">'
    + '<div class="col-md-4"><div class="detail-label">배치 ID (자연키)</div><div class="detail-value small">' + escHtml(d.batchId) + '</div></div>'
    + '<div class="col-md-3"><div class="detail-label">PoC 번호</div><div class="detail-value">' + escHtml(d.pocIdNo) + '</div></div>'
    + '<div class="col-md-2"><div class="detail-label">PoC 발행일</div><div class="detail-value">' + escHtml(d.pocIsueDt) + '</div></div>'
    + '<div class="col-md-3"><div class="detail-label">수신 운영사</div><div class="detail-value">' + escHtml(d.dprtrRecvCoNm) + '</div></div>'
    + '<div class="col-md-3"><div class="detail-label">수신 공항</div><div class="detail-value">' + escHtml(d.safRecvArprtNm) + '</div></div>'
    + '<div class="col-md-2"><div class="detail-label">수신 국가</div><div class="detail-value">' + escHtml(d.safRecvCntryCd) + '</div></div>'
    + '<div class="col-md-2"><div class="detail-label">배치 수량 (L)</div><div class="detail-value">' + numFmt(d.batchQty) + '</div></div>'
    + '<div class="col-md-2"><div class="detail-label">에너지 함량 (MJ)</div><div class="detail-value">' + numFmt(d.energyCn) + '</div></div>'
    + '<div class="col-md-3"><div class="detail-label">밀도 구분</div><div class="detail-value">' + escHtml(d.dnstySecCd) + '</div></div>'
    + '</div>';
  $('#batchInfoBody').html(html);
}

/* ── 탭1: 생산·공급 ─────────────────── */
function loadPrdc() {
  if (loaded.prdc) return;
  loaded.prdc = true;
  $.get('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/prdc')
    .done(function(res) {
      var d = res.data || res;
      prdcData = d || {};
      var html = '<div class="row g-3">'
        + '<div class="col-md-4"><div class="detail-label">생산사 정보</div><div class="detail-value small">' + escHtml(d.prdcCoInfo) + '</div></div>'
        + '<div class="col-md-4"><div class="detail-label">공급사 정보</div><div class="detail-value small">' + escHtml(d.splyCoInfo) + '</div></div>'
        + '<div class="col-md-4"><div class="detail-label">생산자 PoS 배치 ID</div><div class="detail-value small">' + escHtml(d.prdcPosBatchId) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">원료 SAF 수량 (L)</div><div class="detail-value">' + numFmt(d.orgnSafQty) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">SAF 생산일</div><div class="detail-value">' + escHtml(d.safPrdcDt) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">취득일</div><div class="detail-value">' + escHtml(d.acqstnDt) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">생산 위치</div><div class="detail-value small">' + escHtml(d.prdcLcAddr) + '</div></div>'
        + '</div>';
      $('#prdcBody').html(html);
    })
    .fail(function(xhr) { $('#prdcBody').html('<div class="text-danger small">생산·공급 정보 로드 오류 (HTTP ' + xhr.status + ')</div>'); });
}

/* ── 탭2: 혼합 ─────────────────── */
function loadBlndr() {
  if (loaded.blndr) return;
  loaded.blndr = true;
  $.get('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/blndr')
    .done(function(res) {
      var d = res.data || res;
      blndrData = d || {};
      var html = '<div class="row g-3">'
        + '<div class="col-md-4"><div class="detail-label">혼합사 정보</div><div class="detail-value small">' + escHtml(d.blndrCoInfo) + '</div></div>'
        + '<div class="col-md-4"><div class="detail-label">혼합 위치</div><div class="detail-value small">' + escHtml(d.blndLcAddr) + '</div></div>'
        + '<div class="col-md-2"><div class="detail-label">수령일</div><div class="detail-value">' + escHtml(d.recvDt) + '</div></div>'
        + '<div class="col-md-2"><div class="detail-label">수령 중량 (kg)</div><div class="detail-value">' + numFmt(d.recvMass) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">연료 유형</div><div class="detail-value">' + escHtml(d.fuelTypeCd) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">혼합 비율 (%)</div><div class="detail-value text-success fw-bold">' + (d.blndRatio != null ? d.blndRatio.toFixed(2) + '%' : '-') + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">운송사</div><div class="detail-value small">' + escHtml(d.trnsprtCoInfo) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">중간 구매자</div><div class="detail-value small">' + escHtml(d.midBuyerCoInfo) + '</div></div>'
        + '</div>';
      $('#blndrBody').html(html);
    })
    .fail(function(xhr) { $('#blndrBody').html('<div class="text-danger small">혼합 정보 로드 오류 (HTTP ' + xhr.status + ')</div>'); });
}

/* ── 탭3: 원료 ─────────────────── */
function loadFeed() {
  if (loaded.feed) return;
  loaded.feed = true;
  $.get('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/feed')
    .done(function(res) {
      var d = res.data || res;
      feedData = d || {};
      var html = '<div class="row g-3">'
        + '<div class="col-md-3"><div class="detail-label">원료 유형</div><div class="detail-value">' + escHtml(d.fdstkTypeCd) + '</div></div>'
        + '<div class="col-md-4"><div class="detail-label">추가 원료 상세</div><div class="detail-value small">' + escHtml(d.addlFdstkDtl) + '</div></div>'
        + '<div class="col-md-2"><div class="detail-label">폐기물/잔류물 여부</div><div class="detail-value">' + (d.wasteResidueYn === 'Y' ? '<span class="badge bg-warning text-dark">Y</span>' : '<span class="badge bg-secondary">N</span>') + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">전환 공정</div><div class="detail-value">' + escHtml(d.convProcCd) + '</div></div>'
        + '<div class="col-md-12"><div class="detail-label">원산지 국가 코드</div><div class="detail-value small">' + escHtml(Array.isArray(d.orgnCntryCds) ? d.orgnCntryCds.join(', ') : d.orgnCntryCds) + '</div></div>'
        + '</div>';
      $('#feedBody').html(html);
    })
    .fail(function(xhr) { $('#feedBody').html('<div class="text-danger small">원료 정보 로드 오류 (HTTP ' + xhr.status + ')</div>'); });
}

/* ── 탭4: GHG ─────────────────── */
function loadGhg() {
  if (loaded.ghg) return;
  loaded.ghg = true;
  $.get('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/ghg')
    .done(function(res) {
      var d = res.data || res;
      ghgData = d || {};
      // ttlLcaDefVal = core + iluc (자동 계산 표시)
      var core = parseFloat(d.coreLcaDefVal) || 0;
      var iluc = parseFloat(d.ilucEmsn)      || 0;
      var total = core + iluc;
      var storedTotal = parseFloat(d.ttlLcaDefVal) || 0;
      var mismatch = Math.abs(storedTotal - total) > 0.001;

      var html = '<div class="row g-3 mb-3">'
        + '<div class="col-md-3"><div class="detail-label">GHG 값 구분</div><div class="detail-value">' + escHtml(d.ghgValSecCd) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">Core LCA 배출값 (gCO2eq/MJ)</div><div class="detail-value">' + escHtml(d.coreLcaDefVal) + '</div></div>'
        + '<div class="col-md-3"><div class="detail-label">ILUC 배출량 (gCO2eq/MJ)</div><div class="detail-value">' + escHtml(d.ilucEmsn) + '</div></div>'
        + '</div>'
        + '<div class="ghg-calc-box p-3">'
        +   '<div class="small text-muted mb-1">총 LCA 배출값 자동 산출 (ttlLcaDefVal = core + iluc)</div>'
        +   '<div class="ghg-result">' + total.toFixed(4) + ' <small class="text-muted fs-6">gCO2eq/MJ</small></div>'
        +   '<div class="small mt-1">'
        +     '<span class="text-muted">Core: </span><strong>' + core.toFixed(4) + '</strong>'
        +     ' + <span class="text-muted">ILUC: </span><strong>' + iluc.toFixed(4) + '</strong>'
        +   '</div>'
        +   (mismatch ? '<div class="text-danger small mt-2"><i class="bi bi-exclamation-triangle-fill me-1"></i>저장된 값(' + storedTotal + ')과 산출값(' + total.toFixed(4) + ')이 다릅니다. 저장 필요.</div>' : '<div class="text-success small mt-1"><i class="bi bi-check-circle-fill me-1"></i>저장된 값과 일치합니다.</div>')
        + '</div>';
      $('#ghgBody').html(html);
    })
    .fail(function(xhr) { $('#ghgBody').html('<div class="text-danger small">GHG 정보 로드 오류 (HTTP ' + xhr.status + ')</div>'); });
}

/* ── 탭 데이터 캐시 (모달 초기값용) ─── */
var prdcData = {}, blndrData = {}, feedData = {}, ghgData = {};

/* ── PUT 공통 헬퍼 ─── */
function putTabData(url, payload, $btn, originalLabel, callback) {
  $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>저장 중...');
  $.ajax({ url: url, type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function(res) {
      IcasAlert.success(res.message || '저장되었습니다.');
      if (callback) callback();
    })
    .fail(function(xhr) {
      var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'HTTP ' + xhr.status;
      IcasAlert.error('저장 실패: ' + msg);
    })
    .always(function() {
      $btn.prop('disabled', false).html(originalLabel);
    });
}

$(function() {
  // 배치 기본정보 로드
  $.get('/api/saf/batch/' + encodeURIComponent(BATCH_ID))
    .done(function(res) { renderBatchInfo(res.data || res); })
    .fail(function(xhr) { $('#batchInfoBody').html('<div class="text-danger small">배치 정보 로드 오류 (HTTP ' + xhr.status + ')</div>'); });

  // 첫 탭(생산·공급) 즉시 로드
  loadPrdc();

  // 탭 전환 시 lazy load
  $('a[data-bs-toggle="tab"]').on('shown.bs.tab', function(e) {
    var target = $(e.target).attr('href');
    if      (target === '#tabBlndr') loadBlndr();
    else if (target === '#tabFeed')  loadFeed();
    else if (target === '#tabGhg')   loadGhg();
  });

  /* ── 생산·공급 편집 모달 ── */
  $('#prdcEditBtn').on('click', function() {
    var d = prdcData;
    $('#p_prdcCoInfo').val(d.prdcCoInfo || '');
    $('#p_splyCoInfo').val(d.splyCoInfo || '');
    $('#p_prdcPosBatchId').val(d.prdcPosBatchId || '');
    $('#p_prdcPosIsueDt').val(d.prdcPosIsueDt ? String(d.prdcPosIsueDt).substring(0,10) : '');
    $('#p_orgnSafQty').val(d.orgnSafQty != null ? d.orgnSafQty : '');
    $('#p_safPrdcDt').val(d.safPrdcDt ? String(d.safPrdcDt).substring(0,10) : '');
    $('#p_acqstnDt').val(d.acqstnDt ? String(d.acqstnDt).substring(0,10) : '');
    $('#p_prdcLcAddr').val(d.prdcLcAddr || '');
    new bootstrap.Modal('#prdcModal').show();
  });

  $('#prdcSaveBtn').on('click', function() {
    var payload = {
      prdcCoInfo    : $('#p_prdcCoInfo').val().trim() || null,
      splyCoInfo    : $('#p_splyCoInfo').val().trim() || null,
      prdcPosBatchId: $('#p_prdcPosBatchId').val().trim() || null,
      prdcPosIsueDt : $('#p_prdcPosIsueDt').val() || null,
      orgnSafQty    : $('#p_orgnSafQty').val() ? parseFloat($('#p_orgnSafQty').val()) : null,
      safPrdcDt     : $('#p_safPrdcDt').val() || null,
      acqstnDt      : $('#p_acqstnDt').val() || null,
      prdcLcAddr    : $('#p_prdcLcAddr').val().trim() || null
    };
    putTabData('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/prdc', payload,
      $(this), '<i class="bi bi-check-lg me-1"></i> 저장', function() {
        bootstrap.Modal.getInstance('#prdcModal').hide();
        loaded.prdc = false;
        loadPrdc();
      });
  });

  /* ── 혼합 편집 모달 ── */
  $('#blndrEditBtn').on('click', function() {
    var d = blndrData;
    $('#b_blndrCoInfo').val(d.blndrCoInfo || '');
    $('#b_blndLcAddr').val(d.blndLcAddr || '');
    $('#b_recvDt').val(d.recvDt ? String(d.recvDt).substring(0,10) : '');
    $('#b_recvMass').val(d.recvMass != null ? d.recvMass : '');
    $('#b_fuelTypeCd').val(d.fuelTypeCd || '');
    $('#b_blndRatio').val(d.blndRatio != null ? d.blndRatio : '');
    $('#b_trnsprtCoInfo').val(d.trnsprtCoInfo || '');
    $('#b_midBuyerCoInfo').val(d.midBuyerCoInfo || '');
    new bootstrap.Modal('#blndrModal').show();
  });

  $('#blndrSaveBtn').on('click', function() {
    var payload = {
      blndrCoInfo   : $('#b_blndrCoInfo').val().trim() || null,
      blndLcAddr    : $('#b_blndLcAddr').val().trim() || null,
      recvDt        : $('#b_recvDt').val() || null,
      recvMass      : $('#b_recvMass').val() ? parseFloat($('#b_recvMass').val()) : null,
      fuelTypeCd    : $('#b_fuelTypeCd').val() || null,
      blndRatio     : $('#b_blndRatio').val() ? parseFloat($('#b_blndRatio').val()) : null,
      trnsprtCoInfo : $('#b_trnsprtCoInfo').val().trim() || null,
      midBuyerCoInfo: $('#b_midBuyerCoInfo').val().trim() || null
    };
    putTabData('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/blndr', payload,
      $(this), '<i class="bi bi-check-lg me-1"></i> 저장', function() {
        bootstrap.Modal.getInstance('#blndrModal').hide();
        loaded.blndr = false;
        loadBlndr();
      });
  });

  /* ── 원료 편집 모달 ── */
  $('#feedEditBtn').on('click', function() {
    var d = feedData;
    $('#f_fdstkTypeCd').val(d.fdstkTypeCd || '');
    $('#f_convProcCd').val(d.convProcCd || '');
    $('#f_wasteResidueYn').val(d.wasteResidueYn || 'N');
    $('#f_addlFdstkDtl').val(d.addlFdstkDtl || '');
    var cntry = Array.isArray(d.orgnCntryCds) ? d.orgnCntryCds.join(',') : (d.orgnCntryCds || '');
    $('#f_orgnCntryCds').val(cntry);
    $('#feedForm').removeClass('was-validated');
    new bootstrap.Modal('#feedModal').show();
  });

  $('#feedSaveBtn').on('click', function() {
    var $form = $('#feedForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;
    var payload = {
      fdstkTypeCd   : $('#f_fdstkTypeCd').val(),
      convProcCd    : $('#f_convProcCd').val() || null,
      wasteResidueYn: $('#f_wasteResidueYn').val(),
      addlFdstkDtl  : $('#f_addlFdstkDtl').val().trim() || null,
      orgnCntryCds  : $('#f_orgnCntryCds').val().trim() || null
    };
    putTabData('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/feed', payload,
      $(this), '<i class="bi bi-check-lg me-1"></i> 저장', function() {
        bootstrap.Modal.getInstance('#feedModal').hide();
        loaded.feed = false;
        loadFeed();
      });
  });

  /* ── GHG 편집 모달 ── */
  $('#ghgEditBtn').on('click', function() {
    var d = ghgData;
    $('#g_ghgValSeCd').val(d.ghgValSeCd || 'DEFAULT');
    $('#g_coreLcaDefVal').val(d.coreLcaDefVal != null ? d.coreLcaDefVal : '');
    $('#g_ilucEmsn').val(d.ilucEmsn != null ? d.ilucEmsn : '');
    calcGhgTotal();
    $('#ghgForm').removeClass('was-validated');
    new bootstrap.Modal('#ghgModal').show();
  });

  /* GHG 합계 자동계산 */
  function calcGhgTotal() {
    var c = parseFloat($('#g_coreLcaDefVal').val()) || 0;
    var i = parseFloat($('#g_ilucEmsn').val()) || 0;
    $('#g_ttlLcaDefVal_display').val((c + i).toFixed(4));
  }
  $('#g_coreLcaDefVal, #g_ilucEmsn').on('input', calcGhgTotal);

  $('#ghgSaveBtn').on('click', function() {
    var $form = $('#ghgForm');
    $form.addClass('was-validated');
    if (!$form[0].checkValidity()) return;
    var core = parseFloat($('#g_coreLcaDefVal').val());
    var iluc = parseFloat($('#g_ilucEmsn').val());
    var payload = {
      ghgValSeCd   : $('#g_ghgValSeCd').val(),
      coreLcaDefVal: core,
      ilucEmsn     : iluc,
      ttlLcaDefVal : parseFloat((core + iluc).toFixed(4))
    };
    putTabData('/api/saf/batch/' + encodeURIComponent(BATCH_ID) + '/ghg', payload,
      $(this), '<i class="bi bi-check-lg me-1"></i> 저장', function() {
        bootstrap.Modal.getInstance('#ghgModal').hide();
        loaded.ghg = false;
        loadGhg();
      });
  });
});
</script>
</body>
</html>
