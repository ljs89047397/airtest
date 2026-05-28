<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>적격연료(CEF) 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
.status-badge { font-size: 0.72rem; padding: 3px 8px; border-radius: 4px; font-weight: 600; }
.info-label { font-size: 0.78rem; color: #6c757d; font-weight: 500; margin-bottom: 2px; }
.info-value { font-size: 0.92rem; font-weight: 600; }
.nav-tabs .nav-link.active { border-bottom: 2px solid #0F2C72; color: #0F2C72; font-weight: 600; }
.nav-tabs .nav-link { color: #6c757d; }
.validate-panel { border-left: 4px solid #dee2e6; }
.validate-panel.grade-ok      { border-left-color: #198754; }
.validate-panel.grade-warning { border-left-color: #ffc107; }
.validate-panel.grade-blocked { border-left-color: #dc3545; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-fuel-pump me-2"></i>적격연료(CEF) 상세</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/er/cef/list" class="text-decoration-none">적격연료(CEF)</a></li>
            <li class="breadcrumb-item active" id="breadCefId">상세</li>
          </ol>
        </nav>
      </div>
      <div>
        <a href="/er/cef/list" class="btn btn-sm btn-outline-secondary">
          <i class="bi bi-arrow-left me-1"></i>목록
        </a>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- ① 기본정보 카드 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-header bg-white d-flex align-items-center justify-content-between py-2">
        <span class="fw-semibold small" style="color:#0F2C72;"><i class="bi bi-info-circle me-1"></i>기본정보</span>
        <span id="headerBadge"></span>
      </div>
      <div class="card-body py-3">
        <div class="row g-3" id="basicInfoRow">
          <div class="col-12 text-center py-3 text-muted small">
            <div class="spinner-border spinner-border-sm me-2" role="status"></div>로딩 중...
          </div>
        </div>
      </div>
    </div>

    <!-- ② 라이프사이클 액션 버튼 -->
    <div class="card border-0 shadow-sm mb-3" id="actionCard" style="display:none!important;">
      <div class="card-body py-2 px-3">
        <div class="d-flex align-items-center gap-2 flex-wrap">
          <span class="small fw-semibold text-muted me-2">라이프사이클 액션</span>
          <button id="btnSubmit"  class="btn btn-sm btn-primary"        style="display:none;" onclick="doAction('submit')">
            <i class="bi bi-send me-1"></i>제출
          </button>
          <button id="btnApprove" class="btn btn-sm btn-success"        style="display:none;" onclick="doAction('approve')">
            <i class="bi bi-check-circle me-1"></i>KOTSA 승인
          </button>
          <button id="btnReject"  class="btn btn-sm btn-warning text-dark" style="display:none;" onclick="doAction('reject')">
            <i class="bi bi-arrow-counterclockwise me-1"></i>KOTSA 반려
          </button>
          <button id="btnCancel"  class="btn btn-sm btn-danger"         style="display:none;" onclick="promptCancel()">
            <i class="bi bi-x-circle me-1"></i>MOLIT 취소
          </button>
        </div>
      </div>
    </div>

    <!-- ③ 이중청구 검증 패널 -->
    <div class="card border-0 shadow-sm mb-3" id="validatePanel" style="display:none!important;">
      <div class="card-header bg-white py-2 d-flex align-items-center justify-content-between">
        <span class="fw-semibold small" style="color:#0F2C72;"><i class="bi bi-shield-check me-1"></i>이중청구 검증</span>
        <button class="btn btn-xs btn-outline-secondary" style="font-size:0.75rem;padding:2px 8px;" onclick="runValidation()">
          <i class="bi bi-arrow-clockwise me-1"></i>재검증
        </button>
      </div>
      <div class="card-body py-3" id="validateBody">
        <span class="text-muted small">청구 배치 ID 를 선택하면 이중청구 여부를 확인할 수 있습니다.</span>
      </div>
    </div>

    <!-- ④ 탭 (청구내역 / 라이프사이클 / 공급체인) -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white pt-3 pb-0 px-3">
        <ul class="nav nav-tabs card-header-tabs" id="cefTabs">
          <li class="nav-item">
            <button class="nav-link active" data-tab="claim">
              <i class="bi bi-list-check me-1"></i>청구내역
            </button>
          </li>
          <li class="nav-item">
            <button class="nav-link" data-tab="lcyc">
              <i class="bi bi-recycle me-1"></i>라이프사이클
            </button>
          </li>
          <li class="nav-item">
            <button class="nav-link" data-tab="spchn">
              <i class="bi bi-diagram-3 me-1"></i>공급체인
            </button>
          </li>
        </ul>
      </div>
      <div class="card-body p-0">
        <!-- 청구내역 탭 -->
        <div id="tabClaim" class="tab-pane-custom p-3">
          <div class="d-flex justify-content-end mb-2">
            <button class="btn btn-sm" style="background:#0F2C72;color:white;" id="btnAddClaim" onclick="openAddClaimModal()">
              <i class="bi bi-plus me-1"></i>청구 추가
            </button>
          </div>
          <div class="table-responsive">
            <table class="table table-hover table-sm mb-0 table-icas">
              <thead>
                <tr>
                  <th class="ps-3">청구번호</th>
                  <th>배치 ID</th>
                  <th>연료유형</th>
                  <th class="text-end pe-3">질량(kg)</th>
                  <th>이중청구</th>
                  <th style="width:90px;">액션</th>
                </tr>
              </thead>
              <tbody id="claimListBody">
                <tr><td colspan="6" class="text-center py-3 text-muted small">데이터를 불러오는 중...</td></tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 라이프사이클 탭 -->
        <div id="tabLcyc" class="tab-pane-custom p-3" style="display:none;">
          <p class="text-muted small mb-3">청구내역에서 청구번호를 선택하면 해당 청구의 수명주기 정보를 확인할 수 있습니다.</p>
          <div id="lcycContent">
            <div class="text-muted small text-center py-4">청구를 선택하세요.</div>
          </div>
        </div>

        <!-- 공급체인 탭 -->
        <div id="tabSpchn" class="tab-pane-custom p-3" style="display:none;">
          <p class="text-muted small mb-3">청구내역에서 청구번호를 선택하면 해당 청구의 공급체인 정보를 확인할 수 있습니다.</p>
          <div id="spchnContent">
            <div class="text-muted small text-center py-4">청구를 선택하세요.</div>
          </div>
        </div>
      </div>
    </div>

  </div><!-- /container-fluid -->
</div><!-- /main wrapper -->

<!-- ============================================================
     청구 등록/수정 모달
     ============================================================ -->
<div class="modal fade" id="claimModal" tabindex="-1" aria-labelledby="claimModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title" id="claimModalLabel">청구 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="claimEditNo">
        <div class="mb-3">
          <label for="claimBatchIdNo" class="form-label small fw-semibold">배치 ID <span class="text-danger">*</span></label>
          <input type="text" id="claimBatchIdNo" class="form-control form-control-sm" maxlength="100" placeholder="배치 식별자">
          <div class="invalid-feedback" id="claimBatchIdNo-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="claimFuelTypeCd" class="form-label small fw-semibold">연료 유형 <span class="text-danger">*</span></label>
          <select id="claimFuelTypeCd" class="form-select form-select-sm">
            <option value="">선택</option>
            <option value="SAF">SAF</option>
            <option value="JET_A">Jet-A</option>
            <option value="JET_A1">Jet-A1</option>
            <option value="TS_1">TS-1</option>
          </select>
          <div class="invalid-feedback" id="claimFuelTypeCd-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="claimFuelMass" class="form-label small fw-semibold">연료 질량 (kg) <span class="text-danger">*</span></label>
          <input type="number" id="claimFuelMass" class="form-control form-control-sm" min="0" step="0.01" placeholder="kg">
          <div class="invalid-feedback" id="claimFuelMass-error" role="alert"></div>
        </div>
        <div class="alert alert-danger py-2 small mt-2 d-none" id="claimModalError" role="alert"></div>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnClaimModalSave" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     라이프사이클 수정 모달 (claim 당 0..1 행 upsert)
     ============================================================ -->
<div class="modal fade" id="lcycModal" tabindex="-1" aria-labelledby="lcycModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title" id="lcycModalLabel">수명주기 정보 수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="lcycEditClaimNo">
        <div class="row g-3">
          <div class="col-md-4">
            <label for="lcycAbsorptionMass" class="form-label small fw-semibold">흡수량 (kg)</label>
            <input type="number" id="lcycAbsorptionMass" class="form-control form-control-sm" min="0" step="0.0001">
          </div>
          <div class="col-md-4">
            <label for="lcycCo2EmsnMass" class="form-label small fw-semibold">CO2 배출량 (kg)</label>
            <input type="number" id="lcycCo2EmsnMass" class="form-control form-control-sm" min="0" step="0.0001">
          </div>
          <div class="col-md-4">
            <label for="lcycN2oEmsnMass" class="form-label small fw-semibold">N2O 배출량 (kg)</label>
            <input type="number" id="lcycN2oEmsnMass" class="form-control form-control-sm" min="0" step="0.0001">
          </div>
          <div class="col-md-4">
            <label for="lcycCh4EmsnMass" class="form-label small fw-semibold">CH4 배출량 (kg)</label>
            <input type="number" id="lcycCh4EmsnMass" class="form-control form-control-sm" min="0" step="0.0001">
          </div>
          <div class="col-md-4">
            <label for="lcycWttReduMass" class="form-label small fw-semibold">WTT 절감량 (kg)</label>
            <input type="number" id="lcycWttReduMass" class="form-control form-control-sm" min="0" step="0.0001">
          </div>
        </div>
        <div class="alert alert-danger py-2 small mt-2 d-none" id="lcycModalError" role="alert"></div>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnLcycModalSave" class="btn btn-sm" style="background:#0F2C72;color:white;">저장 (Upsert)</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     공급체인 등록/수정 모달
     ============================================================ -->
<div class="modal fade" id="spchnModal" tabindex="-1" aria-labelledby="spchnModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title" id="spchnModalLabel">공급체인 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="spchnEditChnSn">
        <input type="hidden" id="spchnEditClaimNo">
        <div class="mb-3">
          <label for="spchnSplyNm" class="form-label small fw-semibold">공급사명 <span class="text-danger">*</span></label>
          <input type="text" id="spchnSplyNm" class="form-control form-control-sm" maxlength="200" placeholder="공급사 명칭">
          <div class="invalid-feedback" id="spchnSplyNm-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="spchnCtryCd" class="form-label small fw-semibold">국가코드</label>
          <input type="text" id="spchnCtryCd" class="form-control form-control-sm text-uppercase" maxlength="2" placeholder="예: KR">
        </div>
        <div class="mb-3">
          <label for="spchnSplyQty" class="form-label small fw-semibold">공급량 (kg)</label>
          <input type="number" id="spchnSplyQty" class="form-control form-control-sm" min="0" step="0.01">
        </div>
        <div class="alert alert-danger py-2 small mt-2 d-none" id="spchnModalError" role="alert"></div>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnSpchnModalSave" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- 취소 사유 모달 -->
<div class="modal fade" id="cancelModal" tabindex="-1" aria-labelledby="cancelModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-sm">
    <div class="modal-content">
      <div class="modal-header py-2">
        <h6 class="modal-title" id="cancelModalLabel">취소 사유 입력</h6>
        <button type="button" class="btn-close btn-sm" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <textarea id="cancelReason" class="form-control form-control-sm" rows="4"
                  placeholder="취소 사유를 입력하세요 (필수)"></textarea>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-sm btn-danger" onclick="doCancel()">취소 실행</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script src="/resources/js/er/cef/cef-common.js"></script>
<script>/* ── 세션 권한 주입 (서버사이드 EL) ── */
var __OGNZ_SE_CD = '${sessionScope.ognzSeCd}';</script>
<script>
// ── 상태 관리 ──────────────────────────────────────────────
const CEF_ID    = location.pathname.split('/').pop();
let   cefData   = null;
let   claimList = [];
let   selClaimNo = null;

// ── 초기화 ─────────────────────────────────────────────────
$(function() {
  loadCef();

  // 탭 전환
  $('#cefTabs .nav-link').on('click', function() {
    const tab = $(this).data('tab');
    $('#cefTabs .nav-link').removeClass('active');
    $(this).addClass('active');
    $('.tab-pane-custom').hide();
    $('#tab' + tab.charAt(0).toUpperCase() + tab.slice(1)).show();

    if (tab === 'lcyc' && selClaimNo)  loadLcyc(selClaimNo);
    if (tab === 'spchn' && selClaimNo) loadSpchn(selClaimNo);
  });
});

// ── CEF 기본정보 로드 ───────────────────────────────────────
function loadCef() {
  $.get('/api/er/cef/' + CEF_ID)
    .done(function(res) {
      cefData = res.data || res;
      renderBasicInfo(cefData);
      renderActionButtons(cefData.sttsCd);
      loadClaims();
    })
    .fail(function(xhr) {
      $('#basicInfoRow').html('<div class="col-12 text-danger small py-2"><i class="bi bi-exclamation-triangle me-1"></i>'
        + 'CEF 정보 조회 실패 (HTTP ' + xhr.status + ')</div>');
    });
}

// ── 기본정보 렌더 ──────────────────────────────────────────
function renderBasicInfo(d) {
  $('#breadCefId').text(d.cefId || 'CEF 상세');
  $('#headerBadge').html(renderCefBadge(d.sttsCd));

  const mass = d.ttlReduMass != null
    ? Number(d.ttlReduMass).toLocaleString('ko-KR', {minimumFractionDigits: 2, maximumFractionDigits: 2}) + ' kg'
    : '-';

  $('#basicInfoRow').html(
    col('보고연도',  esc(String(d.rprtYr || '')))  +
    col('운영사명',  esc(d.oprtrNm || ''))         +
    col('ICAO 코드', esc(d.icaoCd  || ''))         +
    col('CEF 번호',  esc(d.cefId   || ''))         +
    col('순연료질량', mass)                         +
    col('관련 ER',   d.erId ? '<a href="/er/detail?id=' + esc(d.erId) + '">' + esc(d.erId) + '</a>' : '-')
  );
}

function col(label, value) {
  return '<div class="col-6 col-md-2">'
    + '<div class="info-label">' + label + '</div>'
    + '<div class="info-value">' + value + '</div>'
    + '</div>';
}

// ── 라이프사이클 버튼 표시 (ognzSeCd 권한 가드) ────────────
function renderActionButtons(sttsCd) {
  $('#btnSubmit, #btnApprove, #btnReject, #btnCancel').hide();

  /* 미인증 세션이면 버튼 전체 숨김 — actionCard 자체도 숨김 */
  var ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
  if (!ognzSeCd) { $('#actionCard').hide(); return; }

  if (sttsCd === 'DRAFT'  && ognzSeCd === 'AIRLINE') { $('#btnSubmit').show(); }
  if (sttsCd === 'SBMTD'  && ognzSeCd === 'KOTSA')   { $('#btnApprove, #btnReject').show(); }
  if (sttsCd === 'APRVD'  && ognzSeCd === 'MOLIT')    { $('#btnCancel').show(); }
  $('#actionCard').css('display', '');
}

// ── 라이프사이클 액션 실행 ─────────────────────────────────
function doAction(action) {
  const labels = { submit:'제출', approve:'승인', reject:'반려' };
  if (!confirm('CEF 를 ' + labels[action] + ' 처리하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.post('/api/er/cef/' + CEF_ID + '/' + action)
    .done(function(res) { IcasAlert.success((res && res.message) || labels[action] + ' 처리되었습니다.'); loadCef(); })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '처리 중 오류가 발생했습니다.';
      IcasAlert.error('오류: ' + msg);
    });
}

function promptCancel() { new bootstrap.Modal(document.getElementById('cancelModal')).show(); }
function doCancel() {
  const reason = $('#cancelReason').val().trim();
  if (!reason) { IcasAlert.warning('취소 사유를 입력하세요.'); return; }
  $.ajax({ url:'/api/er/cef/' + CEF_ID + '/cancel', type:'POST',
           contentType:'application/json', data: JSON.stringify({ reason: reason }) })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('cancelModal')).hide();
      IcasAlert.success((res && res.message) || '취소 처리되었습니다.');
      loadCef();
    })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '취소 처리 중 오류가 발생했습니다.';
      IcasAlert.error('오류: ' + msg);
    });
}

// ── 청구내역 로드 ──────────────────────────────────────────
function loadClaims() {
  $.get('/api/er/cef/' + CEF_ID + '/claim')
    .done(function(res) {
      claimList = res.data || res || [];
      renderClaimTable(claimList);
      $('#validatePanel').css('display', '');
    })
    .fail(function(xhr) {
      $('#claimListBody').html('<tr><td colspan="6" class="text-center py-3 text-danger small">'
        + '청구 목록 조회 실패 (HTTP ' + xhr.status + ')</td></tr>');
    });
}

function renderClaimTable(list) {
  if (!list.length) {
    $('#claimListBody').html('<tr><td colspan="6" class="text-center py-3 text-muted small">등록된 청구가 없습니다.</td></tr>');
    return;
  }
  const canEdit = cefData && (cefData.sttsCd === 'DRAFT' || cefData.sttsCd === 'RJCTD');
  const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
  const showEdit = canEdit && ognzSeCd === 'AIRLINE';
  let html = '';
  list.forEach(function(c) {
    const mass = c.fuelMass != null
      ? Number(c.fuelMass).toLocaleString('ko-KR', {minimumFractionDigits: 2, maximumFractionDigits: 2})
      : '-';
    html += '<tr style="cursor:pointer;" onclick="selectClaim(\'' + esc(c.claimNo) + '\')">'
      + '<td class="ps-3 small fw-semibold text-primary">' + esc(c.claimNo || '') + '</td>'
      + '<td class="small">' + esc(c.batchIdNo || '') + '</td>'
      + '<td class="small">' + esc(c.fuelTypeCd || '') + '</td>'
      + '<td class="small text-end pe-3">' + mass + '</td>'
      + '<td><span id="dcResult_' + esc(c.claimNo) + '" class="badge bg-light text-muted border" style="font-size:0.70rem;">-</span></td>'
      + '<td>';
    if (showEdit) {
      html += '<button class="btn btn-outline-primary btn-xs me-1" style="font-size:0.70rem;padding:2px 6px;"'
        + ' onclick="event.stopPropagation();openClaimEditModal(\'' + esc(c.claimNo) + '\',\'' + esc(c.batchIdNo || '') + '\',\'' + esc(c.fuelTypeCd || '') + '\',' + (c.fuelMass != null ? c.fuelMass : '\'\'') + ')">수정</button>'
        + '<button class="btn btn-outline-danger btn-xs" style="font-size:0.70rem;padding:2px 6px;"'
        + ' onclick="event.stopPropagation();deleteClaim(\'' + esc(c.claimNo) + '\')">삭제</button>';
    }
    html += '</td></tr>';
  });
  $('#claimListBody').html(html);

  // 추가 버튼 표시 제어
  $('#btnAddClaim').toggle(showEdit);
}

function selectClaim(claimNo) {
  selClaimNo = claimNo;
  const activeTab = $('#cefTabs .nav-link.active').data('tab');
  if (activeTab === 'lcyc')  loadLcyc(claimNo);
  if (activeTab === 'spchn') loadSpchn(claimNo);
  validateDoubleClaim(claimNo);
}

function deleteClaim(claimNo) {
  if (!confirm('청구 [' + claimNo + '] 를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url:'/api/er/cef/' + CEF_ID + '/claim/' + claimNo, type:'DELETE' })
    .done(function() { loadClaims(); })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '삭제 중 오류가 발생했습니다.';
      IcasAlert.error('오류: ' + msg);
    });
}

// ── 청구 등록 모달 (추가) ─────────────────────────────────
function openAddClaimModal() {
  $('#claimEditNo').val('');
  $('#claimBatchIdNo').val('').removeClass('is-invalid');
  $('#claimFuelTypeCd').val('').removeClass('is-invalid');
  $('#claimFuelMass').val('').removeClass('is-invalid');
  $('#claimModalError').addClass('d-none').text('');
  $('#claimModalLabel').text('청구 등록');
  new bootstrap.Modal(document.getElementById('claimModal')).show();
}

// ── 청구 수정 모달 ────────────────────────────────────────
function openClaimEditModal(claimNo, batchIdNo, fuelTypeCd, fuelMass) {
  $('#claimEditNo').val(claimNo);
  $('#claimBatchIdNo').val(batchIdNo).removeClass('is-invalid');
  $('#claimFuelTypeCd').val(fuelTypeCd).removeClass('is-invalid');
  $('#claimFuelMass').val(fuelMass !== null && fuelMass !== '' ? fuelMass : '').removeClass('is-invalid');
  $('#claimModalError').addClass('d-none').text('');
  $('#claimModalLabel').text('청구 수정 [' + claimNo + ']');
  new bootstrap.Modal(document.getElementById('claimModal')).show();
}

// ── 라이프사이클 수정 모달 오픈 ──────────────────────────
function openLcycEditModal(claimNo) {
  $('#lcycEditClaimNo').val(claimNo);
  $('#lcycModalLabel').text('수명주기 정보 저장 — ' + claimNo);
  $('#lcycAbsorptionMass,#lcycCo2EmsnMass,#lcycN2oEmsnMass,#lcycCh4EmsnMass,#lcycWttReduMass').val('');
  $('#lcycModalError').addClass('d-none').text('');
  // 기존 데이터 로드
  $.get('/api/er/cef/' + CEF_ID + '/claim/' + claimNo + '/lcyc')
    .done(function(res) {
      const d = res.data || res;
      if (d && d.claimNo) {
        $('#lcycAbsorptionMass').val(d.absorptionMass != null ? d.absorptionMass : '');
        $('#lcycCo2EmsnMass').val(d.co2EmsnMass != null ? d.co2EmsnMass : '');
        $('#lcycN2oEmsnMass').val(d.n2oEmsnMass != null ? d.n2oEmsnMass : '');
        $('#lcycCh4EmsnMass').val(d.ch4EmsnMass != null ? d.ch4EmsnMass : '');
        $('#lcycWttReduMass').val(d.wttReduMass != null ? d.wttReduMass : '');
      }
    });
  new bootstrap.Modal(document.getElementById('lcycModal')).show();
}

// ── 공급체인 추가 모달 ────────────────────────────────────
function openSpchnAddModal(claimNo) {
  $('#spchnEditChnSn').val('');
  $('#spchnEditClaimNo').val(claimNo);
  $('#spchnSplyNm').val('').removeClass('is-invalid');
  $('#spchnCtryCd').val('');
  $('#spchnSplyQty').val('');
  $('#spchnModalError').addClass('d-none').text('');
  $('#spchnModalLabel').text('공급체인 등록');
  new bootstrap.Modal(document.getElementById('spchnModal')).show();
}

// ── 공급체인 수정 모달 ────────────────────────────────────
function openSpchnEditModal(claimNo, chnSn, splyNm, ctryCd, splyQty) {
  $('#spchnEditChnSn').val(chnSn);
  $('#spchnEditClaimNo').val(claimNo);
  $('#spchnSplyNm').val(splyNm).removeClass('is-invalid');
  $('#spchnCtryCd').val(ctryCd);
  $('#spchnSplyQty').val(splyQty != null ? splyQty : '');
  $('#spchnModalError').addClass('d-none').text('');
  $('#spchnModalLabel').text('공급체인 수정');
  new bootstrap.Modal(document.getElementById('spchnModal')).show();
}

// ── 공급체인 삭제 ─────────────────────────────────────────
function deleteSpchn(claimNo, chnSn) {
  if (!confirm('공급체인 항목을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $.ajax({ url: '/api/er/cef/' + CEF_ID + '/claim/' + claimNo + '/spchn/' + chnSn, type: 'DELETE' })
    .done(function() { loadSpchn(claimNo); })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '삭제 중 오류가 발생했습니다.';
      IcasAlert.error('오류: ' + msg);
    });
}

// ── 라이프사이클 로드 ──────────────────────────────────────
function loadLcyc(claimNo) {
  $('#lcycContent').html('<div class="text-center py-3 text-muted small"><div class="spinner-border spinner-border-sm me-2"></div>로딩 중...</div>');
  $.get('/api/er/cef/' + CEF_ID + '/claim/' + claimNo + '/lcyc')
    .done(function(res) {
      const d = res.data || res;
      const canEdit = cefData && (cefData.sttsCd === 'DRAFT' || cefData.sttsCd === 'RJCTD');
      const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
      const showEdit = canEdit && ognzSeCd === 'AIRLINE';
      let editBtn = showEdit
        ? '<button class="btn btn-sm btn-outline-primary ms-2" style="font-size:0.78rem;" onclick="openLcycEditModal(\'' + esc(claimNo) + '\')">'
          + '<i class="bi bi-pencil me-1"></i>수정</button>'
        : '';
      if (!d || !d.claimNo) {
        let addBtn = showEdit
          ? '<button class="btn btn-sm btn-outline-success" style="font-size:0.78rem;" onclick="openLcycEditModal(\'' + esc(claimNo) + '\')">'
            + '<i class="bi bi-plus me-1"></i>등록</button>'
          : '';
        $('#lcycContent').html('<div class="text-muted small text-center py-3">등록된 수명주기 정보가 없습니다. ' + addBtn + '</div>');
        return;
      }
      let html = '<div class="d-flex align-items-center mb-2">'
        + '<span class="small fw-semibold">청구번호: ' + esc(d.claimNo) + '</span>' + editBtn + '</div>'
        + '<table class="table table-sm table-bordered" style="font-size:0.82rem;max-width:600px;">'
        + '<tbody>';
      const fields = [
        ['흡수량(kg)', d.absorptionMass],
        ['CO2 배출량(kg)', d.co2EmsnMass], ['N2O 배출량(kg)', d.n2oEmsnMass],
        ['CH4 배출량(kg)', d.ch4EmsnMass], ['WTT 절감량(kg)', d.wttReduMass]
      ];
      fields.forEach(function(f) {
        html += '<tr><th class="bg-light" style="width:160px;">' + f[0] + '</th>'
          + '<td>' + esc(f[1] != null ? String(f[1]) : '-') + '</td></tr>';
      });
      html += '</tbody></table>';
      $('#lcycContent').html(html);
    })
    .fail(function(xhr) {
      $('#lcycContent').html('<div class="text-danger small py-2"><i class="bi bi-exclamation-triangle me-1"></i>수명주기 조회 실패 (HTTP ' + xhr.status + ')</div>');
    });
}

// ── 공급체인 로드 ──────────────────────────────────────────
function loadSpchn(claimNo) {
  $('#spchnContent').html('<div class="text-center py-3 text-muted small"><div class="spinner-border spinner-border-sm me-2"></div>로딩 중...</div>');
  $.get('/api/er/cef/' + CEF_ID + '/claim/' + claimNo + '/spchn')
    .done(function(res) {
      const list = res.data || res || [];
      const canEdit = cefData && (cefData.sttsCd === 'DRAFT' || cefData.sttsCd === 'RJCTD');
      const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
      const showEdit = canEdit && ognzSeCd === 'AIRLINE';
      let addHtml = showEdit
        ? '<div class="d-flex justify-content-end mb-2"><button class="btn btn-sm" style="background:#0F2C72;color:white;" onclick="openSpchnAddModal(\'' + esc(claimNo) + '\')">'
          + '<i class="bi bi-plus me-1"></i>공급체인 추가</button></div>'
        : '';
      if (!list.length) {
        $('#spchnContent').html(addHtml + '<div class="text-muted small text-center py-3">등록된 공급체인 항목이 없습니다.</div>');
        return;
      }
      let html = addHtml + '<div class="table-responsive"><table class="table table-sm table-hover mb-0 table-icas">'
        + '<thead><tr><th class="ps-3">순번</th><th>공급사명</th><th>국가코드</th><th class="text-end pe-3">공급량(kg)</th>'
        + (showEdit ? '<th style="width:120px;">액션</th>' : '')
        + '</tr></thead><tbody>';
      list.forEach(function(s, i) {
        const qty = s.splyQty != null
          ? Number(s.splyQty).toLocaleString('ko-KR', {minimumFractionDigits: 2, maximumFractionDigits: 2})
          : '-';
        html += '<tr>'
          + '<td class="ps-3 small text-muted">' + (i + 1) + '</td>'
          + '<td class="small">' + esc(s.splyNm || '') + '</td>'
          + '<td class="small text-muted">' + esc(s.ctryCd || '') + '</td>'
          + '<td class="small text-end pe-3">' + qty + '</td>';
        if (showEdit) {
          html += '<td>'
            + '<button class="btn btn-outline-primary btn-xs me-1" style="font-size:0.70rem;padding:2px 5px;"'
            + ' onclick="openSpchnEditModal(\'' + esc(claimNo) + '\',' + s.chnSn + ',\'' + esc(s.splyNm || '') + '\',\'' + esc(s.ctryCd || '') + '\',' + (s.splyQty != null ? s.splyQty : 'null') + ')">수정</button>'
            + '<button class="btn btn-outline-danger btn-xs" style="font-size:0.70rem;padding:2px 5px;"'
            + ' onclick="deleteSpchn(\'' + esc(claimNo) + '\',' + s.chnSn + ')">삭제</button>'
            + '</td>';
        }
        html += '</tr>';
      });
      html += '</tbody></table></div>';
      $('#spchnContent').html(html);
    })
    .fail(function(xhr) {
      $('#spchnContent').html('<div class="text-danger small py-2"><i class="bi bi-exclamation-triangle me-1"></i>공급체인 조회 실패 (HTTP ' + xhr.status + ')</div>');
    });
}

// ── 이중청구 검증 ────────────────────────────────
function validateDoubleClaim(claimNo) {
  const claim = claimList.find(function(c) { return c.claimNo === claimNo; });
  if (!claim || !claim.batchIdNo) return;

  $('#validateBody').html('<div class="text-center py-2 text-muted small">'
    + '<div class="spinner-border spinner-border-sm me-2"></div>검증 중...</div>');

  $.get('/api/er/cef/validate-double-claim', {
      batchIdNo:    claim.batchIdNo,
      excludeCefId: CEF_ID,
      excludeClaimNo: claimNo
    })
    .done(function(res) {
      renderValidateResult(res.data || res, claim.batchIdNo, claimNo);
    })
    .fail(function(xhr) {
      $('#validateBody').html('<div class="text-danger small"><i class="bi bi-exclamation-triangle me-1"></i>검증 API 호출 실패 (HTTP ' + xhr.status + ')</div>');
    });
}

function runValidation() {
  if (!selClaimNo) { IcasAlert.warning('청구 목록에서 행을 선택하세요.'); return; }
  validateDoubleClaim(selClaimNo);
}

// ── 청구 모달 저장 ────────────────────────────────────────
$(document).on('click', '#btnClaimModalSave', function() {
  const editNo   = $('#claimEditNo').val();
  const batchId  = $('#claimBatchIdNo').val().trim();
  const fuelType = $('#claimFuelTypeCd').val();
  const fuelMass = $('#claimFuelMass').val();
  let valid = true;
  if (!batchId) { $('#claimBatchIdNo').addClass('is-invalid'); $('#claimBatchIdNo-error').text('배치 ID를 입력하세요.'); valid = false; }
  else $('#claimBatchIdNo').removeClass('is-invalid');
  if (!fuelType) { $('#claimFuelTypeCd').addClass('is-invalid'); $('#claimFuelTypeCd-error').text('연료 유형을 선택하세요.'); valid = false; }
  else $('#claimFuelTypeCd').removeClass('is-invalid');
  if (!fuelMass || isNaN(Number(fuelMass)) || Number(fuelMass) < 0) {
    $('#claimFuelMass').addClass('is-invalid'); $('#claimFuelMass-error').text('질량을 올바르게 입력하세요.'); valid = false;
  } else $('#claimFuelMass').removeClass('is-invalid');
  if (!valid) return;
  const payload = { batchIdNo: batchId, fuelTypeCd: fuelType, fuelMass: Number(fuelMass) };
  const isEdit = !!editNo;
  const url  = '/api/er/cef/' + CEF_ID + '/claim' + (isEdit ? '/' + encodeURIComponent(editNo) : '');
  const mthd = isEdit ? 'PUT' : 'POST';
  $('#claimModalError').addClass('d-none').text('');
  $.ajax({ url: url, type: mthd, contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('claimModal')).hide();
      IcasAlert.success((res && res.message) || '저장되었습니다.');
      loadClaims();
    })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '저장 중 오류가 발생했습니다.';
      $('#claimModalError').removeClass('d-none').text(msg);
    });
});

// ── 라이프사이클 모달 저장 ────────────────────────────────
$(document).on('click', '#btnLcycModalSave', function() {
  const claimNo = $('#lcycEditClaimNo').val();
  const payload = {
    absorptionMass: $('#lcycAbsorptionMass').val() ? Number($('#lcycAbsorptionMass').val()) : null,
    co2EmsnMass:    $('#lcycCo2EmsnMass').val()    ? Number($('#lcycCo2EmsnMass').val())    : null,
    n2oEmsnMass:    $('#lcycN2oEmsnMass').val()    ? Number($('#lcycN2oEmsnMass').val())    : null,
    ch4EmsnMass:    $('#lcycCh4EmsnMass').val()    ? Number($('#lcycCh4EmsnMass').val())    : null,
    wttReduMass:    $('#lcycWttReduMass').val()    ? Number($('#lcycWttReduMass').val())    : null
  };
  $('#lcycModalError').addClass('d-none').text('');
  $.ajax({
    url: '/api/er/cef/' + CEF_ID + '/claim/' + claimNo + '/lcyc',
    type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload)
  })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('lcycModal')).hide();
      IcasAlert.success((res && res.message) || '수명주기 정보가 저장되었습니다.');
      loadLcyc(claimNo);
    })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '저장 중 오류가 발생했습니다.';
      $('#lcycModalError').removeClass('d-none').text(msg);
    });
});

// ── 공급체인 모달 저장 ────────────────────────────────────
$(document).on('click', '#btnSpchnModalSave', function() {
  const claimNo = $('#spchnEditClaimNo').val();
  const chnSn   = $('#spchnEditChnSn').val();
  const splyNm  = $('#spchnSplyNm').val().trim();
  if (!splyNm) { $('#spchnSplyNm').addClass('is-invalid'); $('#spchnSplyNm-error').text('공급사명을 입력하세요.'); return; }
  $('#spchnSplyNm').removeClass('is-invalid');
  const payload = {
    splyNm:  splyNm,
    ctryCd:  $('#spchnCtryCd').val().toUpperCase().trim() || null,
    splyQty: $('#spchnSplyQty').val() ? Number($('#spchnSplyQty').val()) : null
  };
  $('#spchnModalError').addClass('d-none').text('');
  const isEdit = !!chnSn;
  const url  = '/api/er/cef/' + CEF_ID + '/claim/' + claimNo + '/spchn' + (isEdit ? '/' + chnSn : '');
  const mthd = isEdit ? 'PUT' : 'POST';
  $.ajax({ url: url, type: mthd, contentType: 'application/json', data: JSON.stringify(payload) })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('spchnModal')).hide();
      IcasAlert.success((res && res.message) || '저장되었습니다.');
      loadSpchn(claimNo);
    })
    .fail(function(xhr) {
      const msg = (xhr.responseJSON && xhr.responseJSON.message) || '저장 중 오류가 발생했습니다.';
      $('#spchnModalError').removeClass('d-none').text(msg);
    });
});

function renderValidateResult(d, batchIdNo, claimNo) {
  const grade = (d && d.grade) ? d.grade.toUpperCase() : 'UNKNOWN';
  const conflictCnt = (d && d.conflictCount != null) ? d.conflictCount : 0;

  const gradeCls   = { OK:'success', WARNING:'warning', BLOCKED:'danger' };
  const gradeLabel = { OK:'이상 없음', WARNING:'경고', BLOCKED:'차단' };
  const panelCls   = { OK:'grade-ok', WARNING:'grade-warning', BLOCKED:'grade-blocked' };

  const bsCls  = gradeCls[grade]  || 'secondary';
  const lbl    = gradeLabel[grade] || grade;
  const pCls   = panelCls[grade]  || '';

  // 검증 패널 스타일
  $('.validate-panel').removeClass('grade-ok grade-warning grade-blocked').addClass(pCls);

  $('#validateBody').html(
    '<div class="d-flex align-items-start gap-3">'
    + '<div>'
    + '<div class="info-label mb-1">배치 ID</div>'
    + '<div class="small fw-semibold">' + esc(batchIdNo) + '</div>'
    + '</div>'
    + '<div>'
    + '<div class="info-label mb-1">검증 등급</div>'
    + '<span class="badge bg-' + bsCls + ' status-badge">' + lbl + '</span>'
    + '</div>'
    + '<div>'
    + '<div class="info-label mb-1">충돌 건수</div>'
    + '<div class="small fw-semibold">' + conflictCnt + ' 건</div>'
    + '</div>'
    + (d && d.message ? '<div class="flex-grow-1"><div class="info-label mb-1">메시지</div>'
    + '<div class="small text-muted">' + esc(d.message) + '</div></div>' : '')
    + '</div>'
  );

  // 청구 테이블 내 이중청구 열 업데이트
  const badgeCls = { OK:'bg-success', WARNING:'bg-warning text-dark', BLOCKED:'bg-danger' };
  $('#dcResult_' + claimNo).removeClass('bg-light text-muted border bg-success bg-warning bg-danger text-dark')
    .addClass(badgeCls[grade] || 'bg-secondary').text(lbl);
}
</script>
</body>
</html>
