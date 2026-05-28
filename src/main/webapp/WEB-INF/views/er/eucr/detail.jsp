<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배출권취소(EUCR) 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background:white; border-bottom:1px solid #e5e7eb; }
.table-icas thead th { background:#0F2C72; color:white; font-size:0.82rem; font-weight:500; border:none; }
.table-icas tbody tr:hover { background:#f8f9ff; }
.status-badge { font-size:0.72rem; padding:3px 8px; border-radius:4px; font-weight:600; }
.info-label { font-size:0.78rem; color:#6c757d; font-weight:600; text-transform:uppercase; letter-spacing:.04em; }
.info-value { font-size:0.92rem; font-weight:600; color:#1a1a2e; }
.lifecycle-step { display:inline-flex; align-items:center; gap:4px; padding:5px 14px; border-radius:20px; font-size:0.78rem; font-weight:600; border:2px solid transparent; cursor:default; }
.lifecycle-step.done    { background:#e8f5e9; color:#2e7d32; border-color:#a5d6a7; }
.lifecycle-step.active  { background:#0F2C72; color:white; border-color:#0F2C72; }
.lifecycle-step.pending { background:#f5f5f5; color:#9e9e9e; border-color:#e0e0e0; }
.lifecycle-arrow { color:#9e9e9e; font-size:0.85rem; margin:0 2px; }
.validate-panel { border:2px solid #dee2e6; border-radius:8px; }
.validate-panel.blocked  { border-color:#dc3545; background:#fff5f5; }
.validate-panel.ok       { border-color:#198754; background:#f0fff4; }
.fulfilled-y { color:#198754; font-weight:700; font-size:1rem; }
.fulfilled-n { color:#dc3545; font-weight:700; font-size:1rem; }
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
        <h5 class="fw-bold mb-0" style="color:#0F2C72;"><i class="bi bi-file-earmark-check me-2"></i>배출권취소(EUCR) 상세</h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/er/eucr/list" class="text-decoration-none">배출권취소(EUCR)</a></li>
            <li class="breadcrumb-item active" id="breadEucrId">상세</li>
          </ol>
        </nav>
      </div>
      <div>
        <a href="/er/eucr/list" class="btn btn-sm btn-outline-secondary me-1"><i class="bi bi-list me-1"></i>목록</a>
        <button id="btnRecalc" class="btn btn-sm btn-outline-info me-1" title="합계 재계산">
          <i class="bi bi-arrow-clockwise me-1"></i>재계산
        </button>
        <button id="btnDelete" class="btn btn-sm btn-outline-danger" title="삭제(DRAFT 한정)">
          <i class="bi bi-trash me-1"></i>삭제
        </button>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- ① 기본정보 카드 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-header py-2 px-4" style="background:#f8f9ff; border-bottom:1px solid #e5e7eb;">
        <span class="fw-bold small" style="color:#0F2C72;"><i class="bi bi-info-circle me-1"></i>기본 정보</span>
      </div>
      <div class="card-body px-4 py-3">
        <div class="row g-3" id="basicInfoRow">
          <div class="col-auto text-center py-3">
            <div class="spinner-border" style="color:#0F2C72;" role="status"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- ② 라이프사이클 액션 바 -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body px-4 py-3">
        <div class="d-flex align-items-center flex-wrap gap-2 mb-3" id="lifecycleSteps">
          <!-- JS 렌더링 -->
        </div>
        <div class="d-flex flex-wrap gap-2" id="actionButtons">
          <!-- JS 렌더링 -->
        </div>
      </div>
    </div>

    <!-- ②-1 MOLIT 상쇄의무량 통보 안내 (시행계획 p.13 - 11월) -->
    <div class="alert alert-warning d-flex align-items-start py-2 px-3 mb-3" style="border-left:4px solid #f59e0b;">
      <i class="bi bi-megaphone-fill me-2 mt-1" style="color:#f59e0b;"></i>
      <div class="small flex-grow-1">
        <div class="fw-bold mb-1" style="color:#92400e;">상쇄의무량 산정·통보 안내</div>
        <div>
          국토부는 매년 <strong>11월</strong>에 국적사별 상쇄의무량을 산정하여 공식 통보합니다.
          통보 받은 의무량은 본 EUCR 화면의 <strong>상쇄의무량</strong> 값으로 자동 반영되며,
          항공사는 <strong>CORSIA 1주기 종료일(’28.1월)</strong>까지 적격 배출권 구매 → 일련번호 등록을 통해 정산해야 합니다.
          <span class="text-muted">(근거: 시행계획 p.13 · CORSIA 이행 흐름도)</span>
        </div>
      </div>
    </div>

    <!-- ③ 탭 (배치 / 크레딧 상세) -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-header px-4 py-0" style="background:white; border-bottom:1px solid #e5e7eb;">
        <ul class="nav nav-tabs border-0" id="eucrTabs">
          <li class="nav-item">
            <button class="nav-link active fw-semibold small px-4 py-3" data-bs-toggle="tab" data-bs-target="#tabBatch">
              <i class="bi bi-collection me-1"></i>일괄배치 (Batch)
            </button>
          </li>
          <li class="nav-item">
            <button class="nav-link fw-semibold small px-4 py-3" data-bs-toggle="tab" data-bs-target="#tabCrdt">
              <i class="bi bi-123 me-1"></i>크레딧 일련번호
            </button>
          </li>
        </ul>
      </div>
      <div class="tab-content" id="eucrTabContent">

        <!-- 배치 탭 -->
        <div class="tab-pane fade show active p-4" id="tabBatch">
          <!-- 일련번호 이중사용 사전 검증 패널 -->
          <div class="validate-panel p-3 mb-4" id="validatePanel">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-bold small" style="color:#0F2C72;">
                <i class="bi bi-shield-check me-1"></i>일련번호 이중사용 사전 검증
              </span>
              <button id="btnValidate" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-search me-1"></i>검증 실행
              </button>
            </div>
            <div class="mb-2">
              <textarea id="validateInput" class="form-control form-control-sm font-monospace"
                rows="3"
                placeholder="일련번호를 줄 단위로 입력하세요.&#10;예) VCS-2024-001&#10;VCS-2024-002"></textarea>
            </div>
            <div id="validateResult" class="small d-none"></div>
          </div>

          <!-- 배치 등록 폼 -->
          <div class="card border mb-3" id="batchFormWrap">
            <div class="card-header py-2 px-3 d-flex align-items-center justify-content-between" style="background:#f8f9ff;">
              <span class="small fw-bold">배치 추가</span>
              <button class="btn btn-link btn-sm p-0" type="button" data-bs-toggle="collapse" data-bs-target="#batchFormBody">
                <i class="bi bi-chevron-down"></i>
              </button>
            </div>
            <div class="collapse show" id="batchFormBody">
              <div class="card-body p-3">
                <div class="row g-2">
                  <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">배치번호 <span class="text-danger">*</span></label>
                    <input type="text" id="bfBatchNo" class="form-control form-control-sm" maxlength="50" placeholder="배치 식별자">
                  </div>
                  <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">배출권유형 <span class="text-danger">*</span></label>
                    <select id="bfCrdtTypeCd" class="form-select form-select-sm">
                      <option value="">선택</option>
                      <option value="VCU">VCU (Verra)</option>
                      <option value="GS">Gold Standard</option>
                      <option value="CDM">CDM CER</option>
                      <option value="KOC">국내 KOC</option>
                      <option value="ETC">기타</option>
                    </select>
                  </div>
                  <div class="col-md-2">
                    <label class="form-label small fw-semibold mb-1">취소수량 <span class="text-danger">*</span></label>
                    <input type="number" id="bfSubQty" class="form-control form-control-sm" min="0" step="0.0001" placeholder="tCO2e">
                  </div>
                  <div class="col-md-2">
                    <label class="form-label small fw-semibold mb-1">빈티지연도</label>
                    <input type="text" id="bfVntgYr" class="form-control form-control-sm" maxlength="4" placeholder="예: 2023">
                  </div>
                  <div class="col-md-2">
                    <label class="form-label small fw-semibold mb-1">취소일자</label>
                    <input type="date" id="bfCnclDt" class="form-control form-control-sm">
                  </div>
                  <div class="col-md-4">
                    <label class="form-label small fw-semibold mb-1">프로그램명</label>
                    <input type="text" id="bfPrgrmNm" class="form-control form-control-sm" placeholder="예: Verra VCS v4.0">
                  </div>
                  <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">일련번호(시작)</label>
                    <input type="text" id="bfCrdtNoFrom" class="form-control form-control-sm" placeholder="VCS-001">
                  </div>
                  <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">일련번호(끝)</label>
                    <input type="text" id="bfCrdtNoTo" class="form-control form-control-sm" placeholder="VCS-100">
                  </div>
                  <div class="col-md-2 d-flex align-items-end">
                    <button id="btnAddBatch" class="btn btn-sm w-100" style="background:#0F2C72;color:white;">
                      <i class="bi bi-plus-lg me-1"></i>배치 등록
                    </button>
                  </div>
                </div>
                <div id="batchFormError" class="alert alert-danger py-2 small mt-2 d-none"></div>
              </div>
            </div>
          </div>

          <!-- 배치 목록 -->
          <div class="table-responsive">
            <table class="table table-hover table-sm mb-0 table-icas">
              <thead>
                <tr>
                  <th class="ps-3">배치번호</th>
                  <th>유형</th>
                  <th class="text-end pe-3">취소수량</th>
                  <th>빈티지</th>
                  <th>일련번호범위</th>
                  <th>취소일자</th>
                  <th>프로그램</th>
                  <th class="text-center">액션</th>
                </tr>
              </thead>
              <tbody id="batchListBody">
                <tr><td colspan="8" class="text-center py-3 text-muted small">배치 없음</td></tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 크레딧 일련번호 탭 -->
        <div class="tab-pane fade p-4" id="tabCrdt">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <span class="small text-muted">등록된 개별 일련번호 목록입니다.</span>
            <div class="d-flex gap-2 align-items-center flex-wrap">
              <select id="crdtBatchFilter" class="form-select form-select-sm" style="width:180px;">
                <option value="">전체 배치</option>
              </select>
              <button id="btnLoadCrdt" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-arrow-clockwise me-1"></i>새로고침
              </button>
              <button id="btnOpenCrdtAdd" class="btn btn-sm btn-outline-primary" style="display:none;">
                <i class="bi bi-plus me-1"></i>단건 등록
              </button>
              <button id="btnOpenCrdtBulk" class="btn btn-sm" style="background:#0F2C72;color:white;display:none;">
                <i class="bi bi-list-ul me-1"></i>일괄 등록
              </button>
            </div>
          </div>
          <div class="table-responsive">
            <table class="table table-hover table-sm mb-0 table-icas">
              <thead>
                <tr>
                  <th class="ps-3" style="width:40px;">No</th>
                  <th>일련번호</th>
                  <th>배치번호</th>
                  <th>빈티지연도</th>
                  <th>방법론ID</th>
                  <th>등록일</th>
                  <th class="text-center">삭제</th>
                </tr>
              </thead>
              <tbody id="crdtListBody">
                <tr><td colspan="7" class="text-center py-3 text-muted small">배치 탭에서 배치를 선택하거나 새로고침하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>

      </div><!-- tab-content -->
    </div><!-- card -->

  </div><!-- container -->
</div><!-- main -->

<!-- ============================================================
     크레딧 단건 등록 모달
     ============================================================ -->
<div class="modal fade" id="modalCrdtAdd" tabindex="-1" aria-labelledby="modalCrdtAddLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="modalCrdtAddLabel">크레딧 일련번호 단건 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">일련번호 <span class="text-danger">*</span></label>
          <input type="text" id="crdtAddNo" class="form-control form-control-sm font-monospace" maxlength="100" placeholder="예: VCS-2024-001">
          <div class="invalid-feedback" id="crdtAddNo-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">배치번호</label>
          <select id="crdtAddBatchNo" class="form-select form-select-sm">
            <option value="">배치 미지정</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">빈티지연도</label>
          <input type="text" id="crdtAddVntgYr" class="form-control form-control-sm" maxlength="4" placeholder="예: 2023">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">방법론 ID</label>
          <input type="text" id="crdtAddMthdlgyId" class="form-control form-control-sm" maxlength="50" placeholder="예: VM0033">
        </div>
        <div class="alert alert-danger py-2 small mt-2 d-none" id="crdtAddError" role="alert"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnCrdtAddSave" class="btn btn-sm" style="background:#0F2C72;color:white;">등록</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     크레딧 일괄 등록 모달 (textarea 줄 단위)
     ============================================================ -->
<div class="modal fade" id="modalCrdtBulk" tabindex="-1" aria-labelledby="modalCrdtBulkLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="modalCrdtBulkLabel">크레딧 일련번호 일괄 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">배치번호</label>
          <select id="crdtBulkBatchNo" class="form-select form-select-sm">
            <option value="">배치 미지정</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">빈티지연도</label>
          <input type="text" id="crdtBulkVntgYr" class="form-control form-control-sm" maxlength="4" placeholder="예: 2023">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">방법론 ID</label>
          <input type="text" id="crdtBulkMthdlgyId" class="form-control form-control-sm" maxlength="50" placeholder="예: VM0033">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">일련번호 목록 <span class="text-danger">*</span>
            <small class="text-muted fw-normal ms-2">한 줄에 하나씩 입력 (최대 500건)</small>
          </label>
          <textarea id="crdtBulkNos" class="form-control form-control-sm font-monospace" rows="8"
            placeholder="VCS-2024-001&#10;VCS-2024-002&#10;VCS-2024-003"></textarea>
          <div class="invalid-feedback d-block" id="crdtBulkNos-error" role="alert"></div>
          <div class="text-end mt-1">
            <small class="text-muted"><span id="crdtBulkCount">0</span>건 입력됨</small>
          </div>
        </div>
        <div class="alert alert-danger py-2 small mt-2 d-none" id="crdtBulkError" role="alert"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnCrdtBulkSave" class="btn btn-sm" style="background:#0F2C72;color:white;">일괄 등록</button>
      </div>
    </div>
  </div>
</div>

<!-- 반려/취소 사유 모달 -->
<div class="modal fade" id="modalReason" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="modalReasonTitle">사유 입력</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <label class="form-label small fw-semibold">사유 <span class="text-danger">*</span></label>
        <textarea id="reasonText" class="form-control" rows="4" placeholder="사유를 입력하세요."></textarea>
        <div id="reasonError" class="alert alert-danger py-2 small mt-2 d-none"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnConfirmReason" class="btn btn-sm btn-danger">확인</button>
      </div>
    </div>
  </div>
</div>

<!-- 상쇄의무량 수정 모달 -->
<div class="modal fade" id="modalOfstReq" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold">상쇄 의무량 수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <label class="form-label small fw-semibold">상쇄 의무량 (tCO2e) <span class="text-danger">*</span></label>
        <input type="number" id="ofstReqInput" class="form-control" min="0" step="0.0001">
        <div id="ofstReqError" class="alert alert-danger py-2 small mt-2 d-none"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnSaveOfstReq" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>/* ── 세션 권한 주입 (서버사이드 EL) ── */
var __OGNZ_SE_CD = '${sessionScope.ognzSeCd}';</script>
<script>
// ── 유틸 ──────────────────────────────────────────────────────────────────────
function esc(v) {
  if (v == null) return '';
  return String(v)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}
function fmtNum(v) {
  if (v == null || v === '') return '-';
  return Number(v).toLocaleString('ko-KR', {minimumFractionDigits:0, maximumFractionDigits:4});
}
function fmtDt(v) { return v ? String(v).substring(0,10) : '-'; }

const EUCR_STATUS_MAP = {
  'DRAFT': ['bg-secondary', '작성중'],
  'SBMTD': ['bg-primary',   '제출'],
  'RVWNG': ['bg-warning text-dark', '검토중'],
  'RCMDD': ['bg-info text-dark',    '권고'],
  'RJCTD': ['bg-danger',   '반려'],
  'APRVD': ['bg-success',  '승인'],
  'CNCLD': ['bg-dark',     '취소']
};
const LIFECYCLE_STEPS = ['DRAFT','SBMTD','RVWNG','RCMDD','APRVD','CNCLD'];
const STEP_LABELS     = {
  DRAFT:'작성중', SBMTD:'제출', RVWNG:'검토중',
  RCMDD:'권고', APRVD:'승인', CNCLD:'취소'
};

function renderBadge(cd) {
  if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
  const [cls, lbl] = EUCR_STATUS_MAP[cd] || ['bg-secondary', cd];
  return '<span class="badge status-badge ' + cls + '">' + lbl + '</span>';
}

// ── 전역 상태 ─────────────────────────────────────────────────────────────────
const eucrId = (function() {
  const m = location.pathname.match(/\/er\/eucr\/([^/]+)/);
  return m ? m[1] : null;
})();

let currentEucr = null;
let pendingReasonAction = null;

// ── 기본정보 렌더링 ───────────────────────────────────────────────────────────
function renderBasicInfo(e) {
  document.title = 'EUCR ' + esc(e.eucrId) + ' 상세 — ICAS-CEMS';
  $('#breadEucrId').text(e.eucrId || '상세');
  const fy = e.fulfilledYn === 'Y'
    ? '<span class="fulfilled-y"><i class="bi bi-check-circle-fill me-1"></i>충족</span>'
    : '<span class="fulfilled-n"><i class="bi bi-x-circle-fill me-1"></i>미충족</span>';
  const html =
    '<div class="col-md-2"><div class="info-label mb-1">EUCR ID</div><div class="info-value font-monospace small">' + esc(e.eucrId) + '</div></div>' +
    '<div class="col-md-2"><div class="info-label mb-1">운영사</div><div class="info-value">' + esc(e.oprtrNm) + '</div></div>' +
    '<div class="col-md-1"><div class="info-label mb-1">보고연도</div><div class="info-value">' + esc(e.rprtYr) + '</div></div>' +
    '<div class="col-md-1"><div class="info-label mb-1">버전</div><div class="info-value">v' + esc(e.eucrVer) + '</div></div>' +
    '<div class="col-md-2"><div class="info-label mb-1">상태</div><div class="info-value">' + renderBadge(e.eucrStCd) + '</div></div>' +
    '<div class="col-md-2"><div class="info-label mb-1">상쇄의무량 <button class="btn btn-link p-0 ms-1" id="btnEditOfstReq" style="font-size:0.7rem;" title="수정"><i class="bi bi-pencil"></i></button></div>' +
      '<div class="info-value">' + fmtNum(e.ofstReqQty) + ' <small class="text-muted fw-normal">tCO2e</small></div></div>' +
    '<div class="col-md-1"><div class="info-label mb-1">총취소량</div><div class="info-value">' + fmtNum(e.ttlQty) + ' <small class="text-muted fw-normal">tCO2e</small></div></div>' +
    '<div class="col-md-1"><div class="info-label mb-1">충족여부</div><div class="info-value">' + fy + '</div></div>';
  $('#basicInfoRow').html(html);

  // 상쇄의무량 수정 버튼
  $(document).off('click','#btnEditOfstReq').on('click','#btnEditOfstReq', function() {
    $('#ofstReqInput').val(e.ofstReqQty || '');
    $('#ofstReqError').addClass('d-none').text('');
    new bootstrap.Modal(document.getElementById('modalOfstReq')).show();
  });
}

// ── 라이프사이클 렌더링 (ognzSeCd 권한 가드) ─────────────────────────────────
function renderLifecycle(stCd) {
  const curIdx = LIFECYCLE_STEPS.indexOf(stCd);
  let stepsHtml = '';
  LIFECYCLE_STEPS.forEach(function(s, i) {
    if (i > 0) stepsHtml += '<span class="lifecycle-arrow"><i class="bi bi-chevron-right"></i></span>';
    const cls = i < curIdx ? 'done' : (i === curIdx ? 'active' : 'pending');
    stepsHtml += '<span class="lifecycle-step ' + cls + '">' + STEP_LABELS[s] + '</span>';
  });
  $('#lifecycleSteps').html(stepsHtml);

  /* 미인증 세션이면 액션 버튼 전체 숨김 — 스텝퍼(조회)는 유지 */
  const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
  if (!ognzSeCd) {
    $('#actionButtons').html('<span class="text-muted small">로그인이 필요합니다.</span>');
    return;
  }

  const btnStyle = 'style="background:#0F2C72;color:white;"';
  let acts = '';
  /* 제출 — DRAFT, AIRLINE */
  if (stCd === 'DRAFT' && ognzSeCd === 'AIRLINE') {
    acts += '<button class="btn btn-sm ' + btnStyle + ' me-1" data-action="submit"><i class="bi bi-send me-1"></i>제출</button>';
  }
  /* 검토 시작 — SBMTD, KOTSA */
  if (stCd === 'SBMTD' && ognzSeCd === 'KOTSA') {
    acts += '<button class="btn btn-sm btn-outline-warning me-1" data-action="review"><i class="bi bi-eye me-1"></i>검토 시작</button>';
  }
  /* 권고·반려 — RVWNG, KOTSA */
  if (stCd === 'RVWNG' && ognzSeCd === 'KOTSA') {
    acts += '<button class="btn btn-sm btn-outline-info me-1" data-action="recommend"><i class="bi bi-hand-thumbs-up me-1"></i>권고</button>';
    acts += '<button class="btn btn-sm btn-outline-danger me-1" data-action="reject"><i class="bi bi-x-lg me-1"></i>반려</button>';
  }
  /* 승인·반려 — RCMDD, MOLIT(승인) / KOTSA(반려) */
  if (stCd === 'RCMDD') {
    if (ognzSeCd === 'MOLIT') {
      acts += '<button class="btn btn-sm btn-success me-1" data-action="approve"><i class="bi bi-check-lg me-1"></i>승인</button>';
    }
    if (ognzSeCd === 'KOTSA') {
      acts += '<button class="btn btn-sm btn-outline-danger me-1" data-action="reject"><i class="bi bi-x-lg me-1"></i>반려</button>';
    }
  }
  /* 취소 — APRVD, MOLIT */
  if (stCd === 'APRVD' && ognzSeCd === 'MOLIT') {
    acts += '<button class="btn btn-sm btn-outline-dark me-1" data-action="cancel"><i class="bi bi-ban me-1"></i>취소(CANCEL)</button>';
  }
  $('#actionButtons').html(acts || '<span class="text-muted small">가능한 액션이 없습니다.</span>');
}

// ── API 헬퍼 ──────────────────────────────────────────────────────────────────
function apiAction(path, method, body, onDone) {
  $.ajax({ url: '/api/er/eucr/' + eucrId + path, method: method || 'POST',
    contentType: 'application/json', data: body ? JSON.stringify(body) : undefined })
    .done(function(res) { onDone && onDone(res); loadEucrDetail(); })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '처리에 실패하였습니다.';
      IcasAlert.error('[오류] ' + msg);
    });
}

// ── 상세 로드 ─────────────────────────────────────────────────────────────────
function loadEucrDetail() {
  $.get('/api/er/eucr/' + eucrId)
    .done(function(res) {
      currentEucr = res.data || res;
      renderBasicInfo(currentEucr);
      renderLifecycle(currentEucr.eucrStCd);
      const canEdit = ['DRAFT'].indexOf(currentEucr.eucrStCd) >= 0;
      $('#btnDelete').toggle(canEdit);
      $('#batchFormWrap').toggle(canEdit);
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '데이터 조회 실패';
      $('#basicInfoRow').html('<div class="col"><span class="text-danger small"><i class="bi bi-exclamation-triangle me-1"></i>' + esc(msg) + '</span></div>');
    });
}

// ── 배치 로드 ─────────────────────────────────────────────────────────────────
function loadBatch() {
  $.get('/api/er/eucr/' + eucrId + '/batch')
    .done(function(res) {
      const list = res.data || res || [];
      // 배치 필터 select 갱신
      let opts = '<option value="">전체 배치</option>';
      list.forEach(function(b) { opts += '<option value="' + esc(b.batchNo) + '">' + esc(b.batchNo) + '</option>'; });
      $('#crdtBatchFilter').html(opts);
      renderBatchTable(list);
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '배치 조회 실패';
      $('#batchListBody').html('<tr><td colspan="8" class="text-center py-3 text-danger small">' + esc(msg) + '</td></tr>');
    });
}

function renderBatchTable(list) {
  if (!list.length) {
    $('#batchListBody').html('<tr><td colspan="8" class="text-center py-3 text-muted small">등록된 배치가 없습니다.</td></tr>');
    return;
  }
  const canEdit = currentEucr && ['DRAFT'].indexOf(currentEucr.eucrStCd) >= 0;
  let html = '';
  list.forEach(function(b) {
    const range = b.crdtNoFrom ? esc(b.crdtNoFrom) + (b.crdtNoTo ? ' ~ ' + esc(b.crdtNoTo) : '') : '-';
    html += '<tr>'
      + '<td class="ps-3 small fw-semibold">' + esc(b.batchNo) + '</td>'
      + '<td class="small">' + esc(b.crdtTypeCd) + '</td>'
      + '<td class="small text-end pe-3">' + fmtNum(b.subQty) + '</td>'
      + '<td class="small">' + esc(b.vntgYr || '-') + '</td>'
      + '<td class="small font-monospace">' + range + '</td>'
      + '<td class="small">' + fmtDt(b.cnclDt) + '</td>'
      + '<td class="small">' + esc(b.prgrmNm || '-') + '</td>'
      + '<td class="text-center">'
        + (canEdit ? '<button class="btn btn-xs btn-outline-danger btn-del-batch" data-bno="' + esc(b.batchNo) + '" style="font-size:0.72rem;padding:2px 8px;">삭제</button>' : '-')
      + '</td>'
      + '</tr>';
  });
  $('#batchListBody').html(html);
}

// ── 크레딧 일련번호 로드 ─────────────────────────────────────────────────────
function loadCrdt(batchNo) {
  const url = batchNo
    ? '/api/er/eucr/' + eucrId + '/crdt/by-batch/' + encodeURIComponent(batchNo)
    : '/api/er/eucr/' + eucrId + '/crdt';
  $('#crdtListBody').html('<tr><td colspan="7" class="text-center py-3 text-muted small"><div class="spinner-border spinner-border-sm me-2"></div>로딩 중...</td></tr>');
  $.get(url)
    .done(function(res) {
      const list = res.data || res || [];
      if (!list.length) {
        $('#crdtListBody').html('<tr><td colspan="7" class="text-center py-3 text-muted small">등록된 일련번호가 없습니다.</td></tr>');
        return;
      }
      const canEdit = currentEucr && ['DRAFT'].indexOf(currentEucr.eucrStCd) >= 0;
      let html = '';
      list.forEach(function(c, i) {
        html += '<tr>'
          + '<td class="ps-3 text-muted small">' + (i+1) + '</td>'
          + '<td class="small font-monospace">' + esc(c.crdtNo) + '</td>'
          + '<td class="small">' + esc(c.batchNo || '-') + '</td>'
          + '<td class="small">' + esc(c.vntgYr || '-') + '</td>'
          + '<td class="small">' + esc(c.mthdlgyId || '-') + '</td>'
          + '<td class="small">' + fmtDt(c.frstRegDt) + '</td>'
          + '<td class="text-center">'
            + (canEdit ? '<button class="btn btn-xs btn-outline-danger btn-del-crdt" data-crdt-no="' + esc(c.crdtNo) + '" style="font-size:0.72rem;padding:2px 8px;">삭제</button>' : '-')
          + '</td>'
          + '</tr>';
      });
      $('#crdtListBody').html(html);
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '조회 실패';
      $('#crdtListBody').html('<tr><td colspan="7" class="text-center py-3 text-danger small">' + esc(msg) + '</td></tr>');
    });
}

// ── 이중사용 검증 ──────────────────────────────────────────────────────────────
function runValidate() {
  const raw   = $('#validateInput').val();
  const lines = raw.split('\n').map(function(l){ return l.trim(); }).filter(Boolean);
  if (!lines.length) {
    $('#validateResult').removeClass('d-none alert-success alert-danger')
      .addClass('alert alert-warning').text('일련번호를 입력하세요.');
    return;
  }
  $('#btnValidate').prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span>검증중...');
  $.ajax({
    url: '/api/er/eucr/validate-double-using',
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({ crdtNos: lines, excludeEucrId: eucrId })
  })
  .done(function(res) {
    const r = res.data || res;
    const panel = $('#validatePanel');
    $('#validateResult').removeClass('d-none');
    if (r.severity === 'OK') {
      panel.removeClass('blocked').addClass('ok');
      $('#validateResult').removeClass('alert-danger').addClass('alert alert-success')
        .html('<i class="bi bi-check-circle-fill me-1"></i>' + esc(r.message || '충돌 없음. 등록 가능합니다.'));
    } else {
      panel.removeClass('ok').addClass('blocked');
      let conflictHtml = '<i class="bi bi-exclamation-triangle-fill me-1"></i><strong>이중사용 감지 — 등록이 차단됩니다.</strong><br>' + esc(r.message || '');
      if (r.conflicts && r.conflicts.length) {
        conflictHtml += '<ul class="mb-0 mt-1">';
        r.conflicts.forEach(function(c) {
          conflictHtml += '<li><code>' + esc(c.crdtNo) + '</code> → 이미 사용중: ' + esc(c.occupiedByEucrId) + '</li>';
        });
        conflictHtml += '</ul>';
      }
      $('#validateResult').removeClass('alert-success').addClass('alert alert-danger').html(conflictHtml);
    }
  })
  .fail(function(xhr) {
    const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '검증 요청 실패';
    $('#validateResult').removeClass('d-none').addClass('alert alert-danger').text(msg);
  })
  .always(function() {
    $('#btnValidate').prop('disabled', false).html('<i class="bi bi-search me-1"></i>검증 실행');
  });
}

// ── 이벤트 바인딩 ─────────────────────────────────────────────────────────────
$(function() {
  if (!eucrId) {
    IcasAlert.error('EUCR ID를 찾을 수 없습니다.');
    location.href = '/er/eucr/list';
    return;
  }

  loadEucrDetail();
  loadBatch();

  // 라이프사이클 액션 버튼
  $('#actionButtons').on('click', '[data-action]', function() {
    const action = $(this).data('action');
    if (action === 'reject' || action === 'cancel') {
      pendingReasonAction = action;
      $('#modalReasonTitle').text(action === 'reject' ? '반려 사유 입력' : '취소(CANCEL) 사유 입력');
      $('#reasonText').val('');
      $('#reasonError').addClass('d-none').text('');
      $('#btnConfirmReason').off('click').on('click', function() {
        const reason = $.trim($('#reasonText').val());
        if (!reason) { $('#reasonError').removeClass('d-none').text('사유를 입력하세요.'); return; }
        bootstrap.Modal.getInstance(document.getElementById('modalReason')).hide();
        apiAction('/' + pendingReasonAction, 'POST', { reason: reason });
      });
      new bootstrap.Modal(document.getElementById('modalReason')).show();
    } else {
      if (!confirm(STEP_LABELS[action] || action + ' 처리하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
      apiAction('/' + action, 'POST');
    }
  });

  // 재계산
  $('#btnRecalc').on('click', function() {
    if (!confirm('합계를 재계산하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    apiAction('/recalc', 'POST');
  });

  // 삭제
  $('#btnDelete').on('click', function() {
    if (!confirm('EUCR를 삭제하시겠습니까? (DRAFT 상태만 가능)')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/eucr/' + eucrId, method: 'DELETE' })
      .done(function() { location.href = '/er/eucr/list'; })
      .fail(function(xhr) {
        const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '삭제 실패';
        IcasAlert.error('[오류] ' + msg);
      });
  });

  // 상쇄의무량 저장
  $('#btnSaveOfstReq').on('click', function() {
    const qty = $.trim($('#ofstReqInput').val());
    if (!qty || isNaN(Number(qty)) || Number(qty) < 0) {
      $('#ofstReqError').removeClass('d-none').text('올바른 값을 입력하세요.');
      return;
    }
    $.ajax({
      url: '/api/er/eucr/' + eucrId + '/ofst-req-qty',
      method: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify({ ofstReqQty: qty })
    })
    .done(function() {
      bootstrap.Modal.getInstance(document.getElementById('modalOfstReq')).hide();
      loadEucrDetail();
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '저장 실패';
      $('#ofstReqError').removeClass('d-none').text(msg);
    });
  });

  // 배치 등록
  $('#btnAddBatch').on('click', function() {
    // 이중사용 검증 결과 확인 (BLOCKED 이면 차단)
    if ($('#validatePanel').hasClass('blocked')) {
      IcasAlert.warning('이중사용이 감지된 일련번호가 있습니다. 검증 결과를 확인하세요.');
      return;
    }
    const bno   = $.trim($('#bfBatchNo').val());
    const type  = $('#bfCrdtTypeCd').val();
    const qty   = $.trim($('#bfSubQty').val());
    if (!bno)  { $('#batchFormError').removeClass('d-none').text('배치번호를 입력하세요.'); return; }
    if (!type) { $('#batchFormError').removeClass('d-none').text('배출권 유형을 선택하세요.'); return; }
    if (!qty || isNaN(Number(qty)) || Number(qty) <= 0) {
      $('#batchFormError').removeClass('d-none').text('취소수량을 올바르게 입력하세요.'); return;
    }
    $('#batchFormError').addClass('d-none');
    const payload = {
      batchNo:    bno,
      crdtTypeCd: type,
      subQty:     qty,
      vntgYr:     $.trim($('#bfVntgYr').val()) || null,
      cnclDt:     $('#bfCnclDt').val() || null,
      prgrmNm:    $.trim($('#bfPrgrmNm').val()) || null,
      crdtNoFrom: $.trim($('#bfCrdtNoFrom').val()) || null,
      crdtNoTo:   $.trim($('#bfCrdtNoTo').val()) || null
    };
    $(this).prop('disabled', true).text('처리중...');
    $.ajax({
      url: '/api/er/eucr/' + eucrId + '/batch',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload)
    })
    .done(function() {
      $('#bfBatchNo,#bfSubQty,#bfVntgYr,#bfPrgrmNm,#bfCrdtNoFrom,#bfCrdtNoTo').val('');
      $('#bfCrdtTypeCd').val('');
      $('#bfCnclDt').val('');
      loadBatch();
      loadEucrDetail();
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '배치 등록 실패';
      $('#batchFormError').removeClass('d-none').text(msg);
    })
    .always(function() { $('#btnAddBatch').prop('disabled', false).html('<i class="bi bi-plus-lg me-1"></i>배치 등록'); });
  });

  // 배치 삭제 (이벤트 위임)
  $('#batchListBody').on('click', '.btn-del-batch', function() {
    const bno = $(this).data('bno');
    if (!confirm('배치 [' + bno + ']를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/eucr/' + eucrId + '/batch/' + encodeURIComponent(bno), method: 'DELETE' })
      .done(function() { loadBatch(); loadEucrDetail(); })
      .fail(function(xhr) {
        const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '배치 삭제 실패';
        IcasAlert.error('[오류] ' + msg);
      });
  });

  // 크레딧 탭 진입 시 자동 로드
  $('button[data-bs-target="#tabCrdt"]').on('shown.bs.tab', function() { loadCrdt(''); });
  $('#btnLoadCrdt').on('click', function() { loadCrdt($('#crdtBatchFilter').val()); });
  $('#crdtBatchFilter').on('change', function() { loadCrdt($(this).val()); });

  // 크레딧 삭제
  $('#crdtListBody').on('click', '.btn-del-crdt', function() {
    const cno = $(this).data('crdt-no');
    if (!confirm('일련번호 [' + cno + ']를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/eucr/' + eucrId + '/crdt/' + encodeURIComponent(cno), method: 'DELETE' })
      .done(function() { loadCrdt($('#crdtBatchFilter').val()); loadEucrDetail(); })
      .fail(function(xhr) {
        const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '삭제 실패';
        IcasAlert.error('[오류] ' + msg);
      });
  });

  // 이중사용 검증
  $('#btnValidate').on('click', runValidate);

  // 검증 입력 초기화 시 패널 스타일 리셋
  $('#validateInput').on('input', function() {
    if (!$.trim($(this).val())) {
      $('#validatePanel').removeClass('blocked ok');
      $('#validateResult').addClass('d-none').text('');
    }
  });

  // ── 크레딧 단건/일괄 등록 버튼 노출 제어 (DRAFT + AIRLINE) ──
  function updateCrdtEditButtons() {
    const canEdit = currentEucr && ['DRAFT'].indexOf(currentEucr.eucrStCd) >= 0;
    const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
    const showEdit = canEdit && ognzSeCd === 'AIRLINE';
    $('#btnOpenCrdtAdd, #btnOpenCrdtBulk').toggle(showEdit);
  }

  // 크레딧 탭 진입 시 버튼 업데이트
  $('button[data-bs-target="#tabCrdt"]').on('shown.bs.tab', function() {
    updateCrdtEditButtons();
  });

  // ── 단건 등록 모달 오픈 ──
  $('#btnOpenCrdtAdd').on('click', function() {
    // 배치 목록 동기화
    $('#crdtAddBatchNo').html($('#crdtBatchFilter').html());
    $('#crdtAddNo').val('').removeClass('is-invalid');
    $('#crdtAddBatchNo').val('');
    $('#crdtAddVntgYr, #crdtAddMthdlgyId').val('');
    $('#crdtAddError').addClass('d-none').text('');
    new bootstrap.Modal(document.getElementById('modalCrdtAdd')).show();
  });

  // ── 단건 등록 저장 ──
  $('#btnCrdtAddSave').on('click', function() {
    const crdtNo = $.trim($('#crdtAddNo').val());
    if (!crdtNo) { $('#crdtAddNo').addClass('is-invalid'); $('#crdtAddNo-error').text('일련번호를 입력하세요.'); return; }
    $('#crdtAddNo').removeClass('is-invalid');
    // 이중사용 검증 패널이 BLOCKED 이면 차단
    if ($('#validatePanel').hasClass('blocked')) {
      $('#crdtAddError').removeClass('d-none').text('이중사용이 감지된 일련번호가 있습니다. 검증 결과를 확인하세요.');
      return;
    }
    const payload = {
      crdtNo:     crdtNo,
      batchNo:    $('#crdtAddBatchNo').val() || null,
      vntgYr:     $.trim($('#crdtAddVntgYr').val()) || null,
      mthdlgyId:  $.trim($('#crdtAddMthdlgyId').val()) || null
    };
    $('#crdtAddError').addClass('d-none');
    $.ajax({
      url: '/api/er/eucr/' + eucrId + '/crdt',
      method: 'POST', contentType: 'application/json', data: JSON.stringify(payload)
    })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('modalCrdtAdd')).hide();
      IcasAlert.success((res && res.message) || '일련번호가 등록되었습니다.');
      loadCrdt($('#crdtBatchFilter').val());
      loadEucrDetail();
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '등록 실패';
      $('#crdtAddError').removeClass('d-none').text(msg);
    });
  });

  // ── 일괄 등록 모달 오픈 ──
  $('#btnOpenCrdtBulk').on('click', function() {
    $('#crdtBulkBatchNo').html($('#crdtBatchFilter').html());
    $('#crdtBulkBatchNo').val('');
    $('#crdtBulkVntgYr, #crdtBulkMthdlgyId').val('');
    $('#crdtBulkNos').val('');
    $('#crdtBulkCount').text('0');
    $('#crdtBulkNos-error').text('');
    $('#crdtBulkError').addClass('d-none').text('');
    new bootstrap.Modal(document.getElementById('modalCrdtBulk')).show();
  });

  // ── 일괄 등록 textarea 글자수 카운트 ──
  $(document).on('input', '#crdtBulkNos', function() {
    const lines = $(this).val().split('\n').map(function(l){ return l.trim(); }).filter(Boolean);
    $('#crdtBulkCount').text(lines.length);
  });

  // ── 일괄 등록 저장 ──
  $('#btnCrdtBulkSave').on('click', function() {
    const raw = $('#crdtBulkNos').val();
    const lines = raw.split('\n').map(function(l){ return l.trim(); }).filter(Boolean);
    if (!lines.length) {
      $('#crdtBulkNos-error').text('일련번호를 입력하세요.');
      return;
    }
    if (lines.length > 500) {
      $('#crdtBulkNos-error').text('최대 500건까지 입력 가능합니다.');
      return;
    }
    $('#crdtBulkNos-error').text('');
    if ($('#validatePanel').hasClass('blocked')) {
      $('#crdtBulkError').removeClass('d-none').text('이중사용이 감지된 일련번호가 있습니다. 검증 결과를 확인하세요.');
      return;
    }
    const batchNo    = $('#crdtBulkBatchNo').val() || null;
    const vntgYr     = $.trim($('#crdtBulkVntgYr').val()) || null;
    const mthdlgyId  = $.trim($('#crdtBulkMthdlgyId').val()) || null;
    const payload = lines.map(function(no) {
      return { crdtNo: no, batchNo: batchNo, vntgYr: vntgYr, mthdlgyId: mthdlgyId };
    });
    $('#crdtBulkError').addClass('d-none');
    $('#btnCrdtBulkSave').prop('disabled', true).text('처리중...');
    $.ajax({
      url: '/api/er/eucr/' + eucrId + '/crdt/bulk',
      method: 'POST', contentType: 'application/json', data: JSON.stringify(payload)
    })
    .done(function(res) {
      bootstrap.Modal.getInstance(document.getElementById('modalCrdtBulk')).hide();
      IcasAlert.success((res && res.message) || lines.length + '건이 일괄 등록되었습니다.');
      loadCrdt($('#crdtBatchFilter').val());
      loadEucrDetail();
    })
    .fail(function(xhr) {
      const msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '일괄 등록 실패';
      $('#crdtBulkError').removeClass('d-none').text(msg);
    })
    .always(function() {
      $('#btnCrdtBulkSave').prop('disabled', false).html('<i class="bi bi-list-ul me-1"></i>일괄 등록');
    });
  });
});
</script>
</body>
</html>
