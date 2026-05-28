<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배출량보고서(ER) 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root {
  --icas-primary:       #0F2C72;
  --icas-primary-hover: #0A2058;
  --icas-accent:        #0D6EFD;
  --icas-success:       #198754;
  --icas-warning:       #FFC107;
  --icas-danger:        #DC3545;
  --icas-border:        #DEE2E6;
  --icas-bg:            #F8F9FA;
}
body { background: #f0f2f5; font-family: 'Pretendard', '맑은 고딕', sans-serif; }

/* 페이지 헤더 */
.page-header-bar { background: white; border-bottom: 1px solid var(--icas-border); }
.page-back-row   { background: white; border-bottom: 1px solid var(--icas-border); padding: 8px 24px; }

/* 기본정보 카드 */
.info-card { border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.07); border-radius: 8px; }
.info-card .card-header { background: var(--icas-primary); color: white; border-radius: 8px 8px 0 0; font-weight: 600; font-size: 0.9rem; padding: 12px 20px; }
.info-label { font-size: 0.78rem; color: #6c757d; font-weight: 500; margin-bottom: 2px; }
.info-value { font-size: 0.9rem; color: #212529; font-weight: 500; }
.form-control[readonly] { background: #f8f9fa; }

/* 상태 배지 */
.status-badge { font-size: 0.75rem; padding: 4px 10px; border-radius: 5px; font-weight: 700; letter-spacing: 0.3px; }
.st-DRAFT  { background: #6c757d; color: #fff; }
.st-SBMTD  { background: #0d6efd; color: #fff; }
.st-RVWNG  { background: #ffc107; color: #212529; }
.st-RJCTD  { background: #dc3545; color: #fff; }
.st-APRVD  { background: #198754; color: #fff; }
.st-CNCLD  { background: #212529; color: #fff; }
.st-RCMDD  { background: #6f42c1; color: #fff; }

/* 라이프사이클 액션 바 */
.action-bar { background: white; border: 1px solid var(--icas-border); border-radius: 8px; padding: 14px 20px; margin-bottom: 16px; }
.action-bar .action-label { font-size: 0.8rem; color: #6c757d; margin-bottom: 6px; font-weight: 500; }

/* 합계검증 패널 */
.validation-panel { border-radius: 8px; padding: 14px 20px; font-size: 0.85rem; }
.validation-panel.pass   { background: #d1e7dd; border: 1px solid #a3cfbb; }
.validation-panel.fail   { background: #f8d7da; border: 1px solid #f1aeb5; }
.validation-panel.pending { background: #fff3cd; border: 1px solid #ffe69c; }

/* 탭 */
.nav-tabs .nav-link { color: #495057; font-size: 0.85rem; padding: 8px 16px; }
.nav-tabs .nav-link.active { color: var(--icas-primary); font-weight: 700; border-bottom: 2px solid var(--icas-primary); background: transparent; }
.tab-content { background: white; border: 1px solid var(--icas-border); border-top: none; border-radius: 0 0 8px 8px; min-height: 280px; }
.tab-pane { padding: 20px; }

/* 그리드 공통 */
.grid-toolbar { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.grid-toolbar .total-count { font-size: 0.82rem; color: #6c757d; }
.table-icas thead th { background: var(--icas-primary); color: white; font-size: 0.8rem; font-weight: 500; border: none; white-space: nowrap; }
.table-icas tbody tr:hover { background: #f8f9ff; }
.table-icas td { font-size: 0.82rem; vertical-align: middle; }

/* 편집 모드 */
.edit-mode-indicator { font-size: 0.78rem; color: #0d6efd; font-weight: 600; }
.btn-edit-toggle { font-size: 0.82rem; }

/* 모달 */
.reason-modal .modal-header { background: var(--icas-primary); color: white; }

/* 반응형 테이블 */
.table-responsive-icas { overflow-x: auto; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">

  <!-- 뒤로가기 -->
  <div class="page-back-row">
    <a href="/er/list" class="btn btn-sm btn-link text-decoration-none" style="color:var(--icas-primary);">
      <i class="bi bi-arrow-left me-1"></i>목록으로
    </a>
  </div>

  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3">
    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
      <div>
        <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">
          <i class="bi bi-file-earmark-text me-2"></i>배출량보고서(ER) 상세
        </h5>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/er/list" class="text-decoration-none">배출량보고서</a></li>
            <li class="breadcrumb-item active" id="breadcrumbErId">상세</li>
          </ol>
        </nav>
      </div>
      <!-- 상태 배지 + ER ID + 법정 서식 출력 -->
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <span class="text-muted small" id="headerErId"></span>
        <span class="status-badge" id="headerStatusBadge"></span>
        <div class="btn-group btn-group-sm" role="group" aria-label="법정 서식 출력">
          <button type="button" class="btn btn-outline-primary" id="btnExportPdf" title="법정 서식 출력 (PDF)" onclick="exportLegalForm('pdf')">
            <i class="bi bi-file-earmark-pdf me-1"></i>법정 서식 출력 (PDF)
          </button>
          <button type="button" class="btn btn-outline-secondary" id="btnExportExcel" title="법정 서식 출력 (Excel)" onclick="exportLegalForm('xlsx')">
            <i class="bi bi-file-earmark-excel me-1"></i>Excel
          </button>
        </div>
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- 기본 정보 카드 -->
    <div class="card info-card mb-3" id="basicInfoCard">
      <div class="card-header d-flex align-items-center justify-content-between">
        <span><i class="bi bi-info-circle me-2"></i>기본 정보</span>
        <div class="d-flex align-items-center gap-2">
          <span class="edit-mode-indicator d-none" id="editModeLabel"><i class="bi bi-pencil me-1"></i>편집 중</span>
          <button type="button" class="btn btn-sm btn-outline-light btn-edit-toggle" id="btnEditToggle">
            <i class="bi bi-pencil me-1"></i>수정
          </button>
        </div>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-2">
            <div class="info-label">보고연도</div>
            <div class="info-value" id="dispRprtYr">&mdash;</div>
            <input type="number" class="form-control form-control-sm d-none" id="inputRprtYr" name="rprtYr" maxlength="4" min="2020" max="2050" aria-label="보고연도">
          </div>
          <div class="col-md-3">
            <div class="info-label">운영사</div>
            <div class="info-value" id="dispOprtrNm">&mdash;</div>
            <input type="text" class="form-control form-control-sm d-none" id="inputOprtrNm" name="oprtrNm" maxlength="100" readonly aria-label="운영사명">
          </div>
          <div class="col-md-3">
            <div class="info-label">검증기관</div>
            <div class="info-value" id="dispVrfcnInstNm">&mdash;</div>
          </div>
          <div class="col-md-2">
            <div class="info-label">버전</div>
            <div class="info-value" id="dispErVer">&mdash;</div>
          </div>
          <div class="col-md-2">
            <div class="info-label">상태</div>
            <div id="dispErStCd"></div>
          </div>
          <div class="col-md-3">
            <div class="info-label">제출일시</div>
            <div class="info-value" id="dispSbmtDt">&mdash;</div>
          </div>
          <div class="col-md-3">
            <div class="info-label">승인일시</div>
            <div class="info-value" id="dispAprvDt">&mdash;</div>
          </div>
          <div class="col-md-3">
            <div class="info-label">적용 EMP 버전</div>
            <div class="info-value" id="dispEmpVerApld">&mdash;</div>
          </div>
          <div class="col-md-3">
            <div class="info-label">인증 사용 여부 (certUseYn)</div>
            <div class="info-value" id="dispCertUseYn">&mdash;</div>
          </div>
          <div class="col-md-12 d-none" id="editActionRow">
            <div class="d-flex gap-2 mt-2">
              <button type="button" class="btn btn-sm btn-primary" id="btnSaveBasic">
                <i class="bi bi-check2 me-1"></i>저장
              </button>
              <button type="button" class="btn btn-sm btn-outline-secondary" id="btnCancelEdit">
                <i class="bi bi-x me-1"></i>취소
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 라이프사이클 액션 바 -->
    <div class="action-bar mb-3" id="lifecycleActionBar">
      <div class="action-label">워크플로우 액션</div>
      <div class="d-flex flex-wrap gap-2" id="actionButtons">
        <!-- JS 로 동적 렌더링 -->
        <span class="text-muted small">로딩 중...</span>
      </div>
    </div>

    <!-- 합계검증 패널 -->
    <div class="validation-panel pending mb-3" id="pairSumPanel">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
        <div>
          <strong><i class="bi bi-calculator me-1"></i>국가쌍 &harr; 비행장쌍 CO&#8322; 합계 검증</strong>
          <span class="ms-2 text-muted small" id="pairSumMsg">검증 버튼을 눌러 확인하세요.</span>
        </div>
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <span class="small" id="pairSumDetail"></span>
          <button type="button" class="btn btn-sm btn-outline-secondary" id="btnValidatePairSum">
            <i class="bi bi-arrow-repeat me-1"></i>검증 실행
          </button>
        </div>
      </div>
    </div>

    <!-- 탭 7개 -->
    <ul class="nav nav-tabs" id="erDetailTabs" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active" id="tab-acft-fuel"    data-bs-toggle="tab" data-bs-target="#pane-acft-fuel"    type="button" role="tab">
          <i class="bi bi-airplane me-1"></i>항공기&#183;연료
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-aerdrm-pair"  data-bs-toggle="tab" data-bs-target="#pane-aerdrm-pair"  type="button" role="tab">
          <i class="bi bi-signpost-2 me-1"></i>공항쌍
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-afbr"          data-bs-toggle="tab" data-bs-target="#pane-afbr"          type="button" role="tab">
          <i class="bi bi-graph-up me-1"></i>자국비행(AFBR)
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-cntry-pair"   data-bs-toggle="tab" data-bs-target="#pane-cntry-pair"   type="button" role="tab">
          <i class="bi bi-globe2 me-1"></i>국가쌍
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-data-gap"     data-bs-toggle="tab" data-bs-target="#pane-data-gap"     type="button" role="tab">
          <i class="bi bi-exclamation-triangle me-1"></i>데이터갭
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-fuel-smry"    data-bs-toggle="tab" data-bs-target="#pane-fuel-smry"    type="button" role="tab">
          <i class="bi bi-fuel-pump me-1"></i>연료요약
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="tab-vrfr-info"    data-bs-toggle="tab" data-bs-target="#pane-vrfr-info"    type="button" role="tab">
          <i class="bi bi-shield-check me-1"></i>검증기관정보
        </button>
      </li>
    </ul>
    <div class="tab-content" id="erDetailTabContent">

      <!-- TAB 1: 항공기·연료 -->
      <div class="tab-pane fade show active" id="pane-acft-fuel" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">항공기&#183;연료 목록</span>
          <span class="total-count">총 <strong id="acftFuelTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddAcftFuel">
            <i class="bi bi-plus me-1"></i>추가
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="acftFuelTable" aria-label="항공기 연료 목록">
            <thead>
              <tr>
                <th>항공기 유형</th>
                <th>등록기호</th>
                <th>소유/임차</th>
                <th>연료 유형</th>
                <th>밀도 구분</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="acftFuelBody">
              <tr><td colspan="6" class="text-center py-4 text-muted small">
                <div class="spinner-border spinner-border-sm me-2" role="status"></div>로딩 중...
              </td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 2: 공항쌍 배출량 -->
      <div class="tab-pane fade" id="pane-aerdrm-pair" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">비행장 쌍 배출량</span>
          <span class="total-count">총 <strong id="aerdrmPairTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddAerdrmPair">
            <i class="bi bi-plus me-1"></i>추가
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="aerdrmPairTable" aria-label="비행장 쌍 배출량 목록">
            <thead>
              <tr>
                <th>출발 공항</th>
                <th>도착 공항</th>
                <th>출발 국가</th>
                <th>도착 국가</th>
                <th class="text-end">항공편 수</th>
                <th class="text-end">연료중량(t)</th>
                <th class="text-end">CO&#8322;(t)</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="aerdrmPairBody">
              <tr><td colspan="8" class="text-center py-4 text-muted small">탭을 선택하면 데이터를 불러옵니다.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 3: 자국비행 AFBR -->
      <div class="tab-pane fade" id="pane-afbr" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">항공기 유형별 평균 연료연소율(AFBR)</span>
          <span class="total-count">총 <strong id="afbrTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddAfbr">
            <i class="bi bi-plus me-1"></i>추가/수정
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="afbrTable" aria-label="자국비행 AFBR 목록">
            <thead>
              <tr>
                <th>항공기 유형 코드</th>
                <th class="text-end">AFBR 값</th>
                <th>단위</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="afbrBody">
              <tr><td colspan="4" class="text-center py-4 text-muted small">탭을 선택하면 데이터를 불러옵니다.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 4: 국가쌍 배출량 -->
      <div class="tab-pane fade" id="pane-cntry-pair" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">국가 쌍 배출량</span>
          <span class="total-count">총 <strong id="cntryPairTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddCntryPair">
            <i class="bi bi-plus me-1"></i>추가
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="cntryPairTable" aria-label="국가 쌍 배출량 목록">
            <thead>
              <tr>
                <th>출발 국가</th>
                <th>도착 국가</th>
                <th>CER 추정</th>
                <th class="text-end">항공편 수</th>
                <th class="text-end">연료중량(t)</th>
                <th class="text-end">변환계수</th>
                <th class="text-end">CO&#8322;(t)</th>
                <th>상쇄 요건</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="cntryPairBody">
              <tr><td colspan="9" class="text-center py-4 text-muted small">탭을 선택하면 데이터를 불러옵니다.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 5: 데이터갭 -->
      <div class="tab-pane fade" id="pane-data-gap" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">데이터 갭</span>
          <span class="total-count">총 <strong id="dataGapTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddDataGap">
            <i class="bi bi-plus me-1"></i>추가
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="dataGapTable" aria-label="데이터 갭 목록">
            <thead>
              <tr>
                <th>갭 발생일</th>
                <th>참조정보</th>
                <th>원인 코드</th>
                <th>갭 유형</th>
                <th class="text-end">영향 CO&#8322;(t)</th>
                <th>5% 초과</th>
                <th>대체방법 설명</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="dataGapBody">
              <tr><td colspan="8" class="text-center py-4 text-muted small">탭을 선택하면 데이터를 불러옵니다.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 6: 연료요약 -->
      <div class="tab-pane fade" id="pane-fuel-smry" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">연료 유형별 총사용량 요약</span>
          <span class="total-count">총 <strong id="fuelSmryTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddFuelSmry">
            <i class="bi bi-plus me-1"></i>추가/수정
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="fuelSmryTable" aria-label="연료요약 목록">
            <thead>
              <tr>
                <th>연료 유형 코드</th>
                <th class="text-end">총 연료 중량(t)</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="fuelSmryBody">
              <tr><td colspan="3" class="text-center py-4 text-muted small">탭을 선택하면 데이터를 불러옵니다.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 7: 검증기관정보 -->
      <div class="tab-pane fade" id="pane-vrfr-info" role="tabpanel">
        <div class="grid-toolbar">
          <span class="fw-semibold small" style="color:var(--icas-primary);">참여 검증기관 정보</span>
          <span class="total-count">총 <strong id="vrfrInfoTotal">0</strong>건</span>
          <button type="button" class="btn btn-sm ms-auto" style="background:var(--icas-primary);color:white;" id="btnAddVrfrInfo">
            <i class="bi bi-plus me-1"></i>추가
          </button>
        </div>
        <div class="table-responsive-icas">
          <table class="table table-hover table-sm mb-0 table-icas" id="vrfrInfoTable" aria-label="검증기관 정보 목록">
            <thead>
              <tr>
                <th>검증기관 ID</th>
                <th>연락처 설명</th>
                <th>인증 상세</th>
                <th style="width:90px;">액션</th>
              </tr>
            </thead>
            <tbody id="vrfrInfoBody">
              <tr><td colspan="4" class="text-center py-4 text-muted small">탭을 선택하면 데이터를 불러옵니다.</td></tr>
            </tbody>
          </table>
        </div>
      </div>

    </div><!-- /tab-content -->
  </div><!-- /container-fluid -->
</div><!-- /main content -->

<!-- ===== 반려/취소 사유 입력 모달 ===== -->
<div class="modal fade reason-modal" id="reasonModal" tabindex="-1" aria-labelledby="reasonModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="reasonModalLabel">사유 입력</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <label for="reasonInput" class="form-label fw-semibold">
          처리 사유 <span class="text-danger" aria-hidden="true">*</span>
        </label>
        <textarea id="reasonInput" class="form-control" rows="4" maxlength="500"
                  placeholder="사유를 입력하세요 (최대 500자)" aria-required="true"></textarea>
        <div class="invalid-feedback" id="reasonInputError" role="alert"></div>
        <small class="text-muted"><span id="reasonLen">0</span>/500자</small>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-danger" id="btnReasonConfirm">확인</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 항공기·연료 행 추가/수정 모달 ===== -->
<div class="modal fade" id="acftFuelModal" tabindex="-1" aria-labelledby="acftFuelModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="acftFuelModalLabel">항공기&#183;연료 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="acftFuelAcftSn">
        <div class="row g-3">
          <div class="col-md-4">
            <label for="acftTypeCd" class="form-label small fw-semibold">항공기 유형 코드 <span class="text-danger">*</span></label>
            <input type="text" id="acftTypeCd" class="form-control form-control-sm" maxlength="10"
                   placeholder="예: B738" aria-required="true">
            <div class="invalid-feedback" id="acftTypeCdError" role="alert"></div>
          </div>
          <div class="col-md-4">
            <label for="regisMark" class="form-label small fw-semibold">등록기호 <span class="text-danger">*</span></label>
            <input type="text" id="regisMark" class="form-control form-control-sm" maxlength="20"
                   placeholder="예: HL8000" aria-required="true">
            <div class="invalid-feedback" id="regisMarkError" role="alert"></div>
          </div>
          <div class="col-md-4">
            <label for="ownrLsSeCd" class="form-label small fw-semibold">소유/임차 구분</label>
            <select id="ownrLsSeCd" class="form-select form-select-sm">
              <option value="">선택</option>
              <option value="OWN">소유</option>
              <option value="LEASE">임차</option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="fuelTypeCd" class="form-label small fw-semibold">연료 유형 <span class="text-danger">*</span></label>
            <select id="fuelTypeCd" class="form-select form-select-sm" aria-required="true">
              <option value="">선택</option>
              <option value="JET_A">Jet-A</option>
              <option value="JET_A1">Jet-A1</option>
              <option value="TS_1">TS-1</option>
              <option value="SAF">SAF</option>
            </select>
            <div class="invalid-feedback" id="fuelTypeCdError" role="alert"></div>
          </div>
          <div class="col-md-4">
            <label for="dnstySecCd" class="form-label small fw-semibold">밀도 구분</label>
            <select id="dnstySecCd" class="form-select form-select-sm">
              <option value="STD">STD (표준)</option>
              <option value="ACT">ACT (실측)</option>
            </select>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveAcftFuel">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 공항쌍 추가/수정 모달 ===== -->
<div class="modal fade" id="aerdrmPairModal" tabindex="-1" aria-labelledby="aerdrmPairModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="aerdrmPairModalLabel">비행장 쌍 배출량 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="aerdrmPairSn">
        <div class="row g-3">
          <div class="col-md-3">
            <label for="aerdrmDprtrCd" class="form-label small fw-semibold">출발 공항(ICAO) <span class="text-danger">*</span></label>
            <input type="text" id="aerdrmDprtrCd" class="form-control form-control-sm" maxlength="4"
                   placeholder="ICAO 4자" style="text-transform:uppercase;" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="aerdrmArvlCd" class="form-label small fw-semibold">도착 공항(ICAO) <span class="text-danger">*</span></label>
            <input type="text" id="aerdrmArvlCd" class="form-control form-control-sm" maxlength="4"
                   placeholder="ICAO 4자" style="text-transform:uppercase;" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="aerdrmDprtrCntryCd" class="form-label small fw-semibold">출발 국가</label>
            <input type="text" id="aerdrmDprtrCntryCd" class="form-control form-control-sm" maxlength="2"
                   placeholder="ISO-2" style="text-transform:uppercase;">
          </div>
          <div class="col-md-3">
            <label for="aerdrmArvlCntryCd" class="form-label small fw-semibold">도착 국가</label>
            <input type="text" id="aerdrmArvlCntryCd" class="form-control form-control-sm" maxlength="2"
                   placeholder="ISO-2" style="text-transform:uppercase;">
          </div>
          <div class="col-md-3">
            <label for="aerdrmFltCnt" class="form-label small fw-semibold">항공편 수 <span class="text-danger">*</span></label>
            <input type="number" id="aerdrmFltCnt" class="form-control form-control-sm" min="0" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="aerdrmFuelWght" class="form-label small fw-semibold">연료중량(t) <span class="text-danger">*</span></label>
            <input type="number" id="aerdrmFuelWght" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="aerdrmCo2Emsn" class="form-label small fw-semibold">CO&#8322; 배출량(t) <span class="text-danger">*</span></label>
            <input type="number" id="aerdrmCo2Emsn" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveAerdrmPair">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== AFBR 추가/수정 모달 ===== -->
<div class="modal fade" id="afbrModal" tabindex="-1" aria-labelledby="afbrModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="afbrModalLabel">AFBR 등록/수정</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <div class="row g-3">
          <div class="col-md-4">
            <label for="afbrAcftTypeCd" class="form-label small fw-semibold">항공기 유형 코드 <span class="text-danger">*</span></label>
            <input type="text" id="afbrAcftTypeCd" class="form-control form-control-sm" maxlength="10"
                   placeholder="예: B738" aria-required="true">
          </div>
          <div class="col-md-4">
            <label for="afbrVal" class="form-label small fw-semibold">AFBR 값 <span class="text-danger">*</span></label>
            <input type="number" id="afbrVal" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
          <div class="col-md-4">
            <label for="afbrUnit" class="form-label small fw-semibold">단위</label>
            <input type="text" id="afbrUnit" class="form-control form-control-sm" maxlength="20" placeholder="kg/km">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveAfbr">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 국가쌍 추가/수정 모달 ===== -->
<div class="modal fade" id="cntryPairModal" tabindex="-1" aria-labelledby="cntryPairModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="cntryPairModalLabel">국가 쌍 배출량 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="cntryPairSn">
        <div class="row g-3">
          <div class="col-md-3">
            <label for="cntryDprtrCd" class="form-label small fw-semibold">출발 국가(ISO) <span class="text-danger">*</span></label>
            <input type="text" id="cntryDprtrCd" class="form-control form-control-sm" maxlength="2"
                   placeholder="예: KR" style="text-transform:uppercase;" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="cntryArvlCd" class="form-label small fw-semibold">도착 국가(ISO) <span class="text-danger">*</span></label>
            <input type="text" id="cntryArvlCd" class="form-control form-control-sm" maxlength="2"
                   placeholder="예: JP" style="text-transform:uppercase;" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="cntryCerEstmYn" class="form-label small fw-semibold">CER 추정 여부</label>
            <select id="cntryCerEstmYn" class="form-select form-select-sm">
              <option value="N">N</option>
              <option value="Y">Y</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="cntryFltCnt" class="form-label small fw-semibold">항공편 수 <span class="text-danger">*</span></label>
            <input type="number" id="cntryFltCnt" class="form-control form-control-sm" min="0" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="cntryFuelWght" class="form-label small fw-semibold">연료중량(t) <span class="text-danger">*</span></label>
            <input type="number" id="cntryFuelWght" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="cntryConvFctr" class="form-label small fw-semibold">변환계수 <span class="text-danger">*</span></label>
            <input type="number" id="cntryConvFctr" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="cntryCo2Emsn" class="form-label small fw-semibold">CO&#8322; 배출량(t) <span class="text-danger">*</span></label>
            <input type="number" id="cntryCo2Emsn" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
          <div class="col-md-3">
            <label for="cntryOfstReqYn" class="form-label small fw-semibold">상쇄 요건</label>
            <select id="cntryOfstReqYn" class="form-select form-select-sm">
              <option value="N">N</option>
              <option value="Y">Y</option>
            </select>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveCntryPair">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 데이터갭 추가/수정 모달 ===== -->
<div class="modal fade" id="dataGapModal" tabindex="-1" aria-labelledby="dataGapModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="dataGapModalLabel">데이터 갭 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="dataGapSn">
        <div class="row g-3">
          <div class="col-md-4">
            <label for="gapDt" class="form-label small fw-semibold">갭 발생일 <span class="text-danger">*</span></label>
            <input type="date" id="gapDt" class="form-control form-control-sm" aria-required="true">
          </div>
          <div class="col-md-4">
            <label for="gapCauseCd" class="form-label small fw-semibold">원인 코드</label>
            <input type="text" id="gapCauseCd" class="form-control form-control-sm" maxlength="20">
          </div>
          <div class="col-md-4">
            <label for="gapTypeCd" class="form-label small fw-semibold">갭 유형</label>
            <input type="text" id="gapTypeCd" class="form-control form-control-sm" maxlength="20">
          </div>
          <div class="col-md-6">
            <label for="gapRefInfo" class="form-label small fw-semibold">참조정보</label>
            <input type="text" id="gapRefInfo" class="form-control form-control-sm" maxlength="500">
          </div>
          <div class="col-md-3">
            <label for="gapAfctCo2Emsn" class="form-label small fw-semibold">영향 CO&#8322;(t)</label>
            <input type="number" id="gapAfctCo2Emsn" class="form-control form-control-sm" step="0.0001" min="0">
          </div>
          <div class="col-md-12">
            <label for="gapReplMthdDesc" class="form-label small fw-semibold">대체방법 설명</label>
            <textarea id="gapReplMthdDesc" class="form-control form-control-sm" rows="3" maxlength="2000"></textarea>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveDataGap">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 연료요약 추가/수정 모달 ===== -->
<div class="modal fade" id="fuelSmryModal" tabindex="-1" aria-labelledby="fuelSmryModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="fuelSmryModalLabel">연료요약 등록/수정</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <div class="row g-3">
          <div class="col-md-6">
            <label for="fuelSmryFuelTypeCd" class="form-label small fw-semibold">연료 유형 코드 <span class="text-danger">*</span></label>
            <select id="fuelSmryFuelTypeCd" class="form-select form-select-sm" aria-required="true">
              <option value="">선택</option>
              <option value="JET_A">Jet-A</option>
              <option value="JET_A1">Jet-A1</option>
              <option value="TS_1">TS-1</option>
              <option value="SAF">SAF</option>
            </select>
          </div>
          <div class="col-md-6">
            <label for="fuelSmryTtlFuelWght" class="form-label small fw-semibold">총 연료 중량(t) <span class="text-danger">*</span></label>
            <input type="number" id="fuelSmryTtlFuelWght" class="form-control form-control-sm" step="0.0001" min="0" aria-required="true">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveFuelSmry">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 검증기관정보 추가/수정 모달 ===== -->
<div class="modal fade" id="vrfrInfoModal" tabindex="-1" aria-labelledby="vrfrInfoModalLabel" aria-modal="true" role="dialog">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="vrfrInfoModalLabel">참여 검증기관 정보 등록</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="vrfrInfoSn">
        <div class="row g-3">
          <div class="col-md-4">
            <label for="vrfcnInstId" class="form-label small fw-semibold">검증기관 ID <span class="text-danger">*</span></label>
            <input type="text" id="vrfcnInstId" class="form-control form-control-sm" maxlength="50" aria-required="true">
          </div>
          <div class="col-md-8">
            <label for="cnctDesc" class="form-label small fw-semibold">연락처 설명</label>
            <input type="text" id="cnctDesc" class="form-control form-control-sm" maxlength="500">
          </div>
          <div class="col-md-12">
            <label for="accrdDtl" class="form-label small fw-semibold">인증 상세</label>
            <textarea id="accrdDtl" class="form-control form-control-sm" rows="3" maxlength="2000"></textarea>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" class="btn btn-primary" id="btnSaveVrfrInfo">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 스크립트 ===== -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>/* ── 세션 권한 주입 (서버사이드 EL) ── */
var __OGNZ_SE_CD = '${sessionScope.ognzSeCd}';</script>
<script>
(function () {
  'use strict';

  /* ── 공통 유틸 ── */
  function esc(s) {
    if (s == null) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function fmt(v, digits) {
    if (v == null || v === '') return '-';
    var n = parseFloat(v);
    if (isNaN(n)) return esc(v);
    return n.toLocaleString('ko-KR', { minimumFractionDigits: digits || 0, maximumFractionDigits: digits || 0 });
  }

  var STATUS_MAP = {
    'DRAFT' : ['st-DRAFT',  '작성중'],
    'SBMTD' : ['st-SBMTD',  '제출됨'],
    'RVWNG' : ['st-RVWNG',  '검토중'],
    'RJCTD' : ['st-RJCTD',  '반려'],
    'APRVD' : ['st-APRVD',  '승인'],
    'CNCLD' : ['st-CNCLD',  '취소'],
    'RCMDD' : ['st-RCMDD',  '권고됨']
  };

  function statusBadge(cd) {
    if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
    var m = STATUS_MAP[cd] || ['bg-secondary', cd];
    return '<span class="badge status-badge ' + m[0] + '">' + m[1] + '</span>';
  }

  function yn(v) { return v === 'Y' ? '<span class="text-success fw-bold">Y</span>' : '<span class="text-secondary">N</span>'; }

  /* ── URL 에서 erId 추출 ── */
  var pathParts = window.location.pathname.split('/');
  var erId = pathParts[pathParts.length - 1] || '';
  // query string 폴백 (?id=...)
  if (!erId || erId === 'detail') {
    var params = new URLSearchParams(window.location.search);
    erId = params.get('id') || '';
  }

  /* ── 편집 모드 상태 ── */
  var editMode = false;
  var origBasicData = {};
  var currentErSt = 'DRAFT';
  var isEditable = false; // DRAFT 상태만 true

  /* ── 탭 로드 추적 ── */
  var tabLoaded = {};

  /* ── Toast 헬퍼 ── */
  var _toastEl;
  function showToast(msg, type) {
    if (!_toastEl) {
      var el = $('<div id="_icasToast" class="toast align-items-center position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;min-width:280px;"></div>');
      $('body').append(el);
      _toastEl = el;
    }
    var cls = type === 'error' ? 'text-bg-danger' : (type === 'warn' ? 'text-bg-warning' : 'text-bg-success');
    _toastEl.attr('class', 'toast align-items-center position-fixed top-0 start-50 translate-middle-x mt-3 ' + cls);
    _toastEl.html('<div class="d-flex"><div class="toast-body">' + esc(msg) + '</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>');
    var t = new bootstrap.Toast(_toastEl[0], { delay: type === 'error' ? 4000 : 1800 });
    t.show();
  }

  function toastOk(msg)   { showToast(msg, 'ok'); }
  function toastErr(xhr)  {
    var msg = '오류가 발생했습니다.';
    try { msg = xhr.responseJSON.message || msg; } catch(e) {}
    showToast(msg, 'error');
  }

  /* ── API 베이스 ── */
  var BASE = '/api/er/rprt/' + encodeURIComponent(erId);

  /* ════════════════════════════════════════════
     1. ER 기본정보 로드
  ════════════════════════════════════════════ */
  function loadBasicInfo() {
    $.get(BASE)
      .done(function (res) {
        var d = res.data || res;
        origBasicData = d;
        currentErSt = d.erStCd || 'DRAFT';
        isEditable = (currentErSt === 'DRAFT');
        renderBasicInfo(d);
        renderActionButtons(d);
        // 첫 탭 데이터 로드
        if (!tabLoaded['acft-fuel']) { loadTab('acft-fuel'); }
      })
      .fail(function (xhr) {
        toastErr(xhr);
        renderActionButtons({erStCd: 'DRAFT'});
      });
  }

  function renderBasicInfo(d) {
    $('#breadcrumbErId').text(esc(d.erId || erId));
    $('#headerErId').text(esc(d.erId || erId));
    $('#headerStatusBadge').removeClass().addClass('badge status-badge ' + (STATUS_MAP[d.erStCd] || ['bg-secondary'])[0]).text((STATUS_MAP[d.erStCd] || ['','?'])[1]);
    $('#dispRprtYr').text(esc(d.rprtYr) || '-');
    $('#dispOprtrNm').text(esc(d.oprtrNm || d.oprtrId) || '-');
    $('#dispVrfcnInstNm').text(esc(d.vrfcnInstNm || d.vrfcnInstId) || '-');
    $('#dispErVer').text(d.erVer != null ? 'v' + esc(d.erVer) : '-');
    $('#dispErStCd').html(statusBadge(d.erStCd));
    $('#dispSbmtDt').text(esc(d.sbmtDt) || '-');
    $('#dispAprvDt').text(esc(d.aprvDt) || '-');
    $('#dispEmpVerApld').text(d.empVerApld != null ? 'v' + esc(d.empVerApld) : '-');
    $('#dispCertUseYn').html(yn(d.certUseYn));
    // 편집 버튼 표시/숨김
    $('#btnEditToggle').toggle(isEditable);
  }

  /* ════════════════════════════════════════════
     2. 기본정보 Inline 편집 토글
  ════════════════════════════════════════════ */
  $('#btnEditToggle').on('click', function () {
    if (!isEditable) return;
    enterEditMode();
  });

  function enterEditMode() {
    editMode = true;
    $('#inputRprtYr').val($('#dispRprtYr').text().trim()).removeClass('d-none');
    $('#dispRprtYr').addClass('d-none');
    $('#editModeLabel').removeClass('d-none');
    $('#editActionRow').removeClass('d-none');
    $('#btnEditToggle').addClass('d-none');
  }

  function exitEditMode(restore) {
    editMode = false;
    if (restore) { renderBasicInfo(origBasicData); }
    $('#inputRprtYr').addClass('d-none');
    $('#dispRprtYr').removeClass('d-none');
    $('#editModeLabel').addClass('d-none');
    $('#editActionRow').addClass('d-none');
    $('#btnEditToggle').removeClass('d-none');
  }

  $('#btnCancelEdit').on('click', function () { exitEditMode(true); });

  $('#btnSaveBasic').on('click', function () {
    var yr = parseInt($('#inputRprtYr').val(), 10);
    if (!yr || yr < 2020 || yr > 2050) {
      $('#inputRprtYr').addClass('is-invalid');
      return;
    }
    $('#inputRprtYr').removeClass('is-invalid');
    var payload = { rprtYr: yr };
    $.ajax({ url: BASE, method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('기본 정보가 수정되었습니다.');
        exitEditMode(false);
        loadBasicInfo();
      })
      .fail(toastErr);
  });

  /* ════════════════════════════════════════════
     3. 라이프사이클 액션 버튼 렌더링
     ognzSeCd 권한 가드 포함
  ════════════════════════════════════════════ */
  function renderActionButtons(d) {
    var st = d.erStCd || 'DRAFT';
    var btns = [];

    /* 미인증 세션이면 버튼 전체 숨김 — 화면(조회)은 유지 */
    var ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
    if (!ognzSeCd) {
      $('#actionButtons').html('<span class="text-muted small">로그인이 필요합니다.</span>');
      return;
    }

    // 제출 — DRAFT, AIRLINE 역할
    if (st === 'DRAFT' && ognzSeCd === 'AIRLINE') {
      btns.push('<button type="button" class="btn btn-sm btn-primary" data-action="submit"><i class="bi bi-send me-1"></i>제출</button>');
    }
    // 검토 진입 — SBMTD, KOTSA
    if (st === 'SBMTD' && ognzSeCd === 'KOTSA') {
      btns.push('<button type="button" class="btn btn-sm btn-warning" data-action="review"><i class="bi bi-eye me-1"></i>검토 시작</button>');
    }
    // 반려·권고 — RVWNG, KOTSA
    if (st === 'RVWNG' && ognzSeCd === 'KOTSA') {
      btns.push('<button type="button" class="btn btn-sm btn-outline-danger" data-action="reject"><i class="bi bi-x-circle me-1"></i>반려</button>');
      btns.push('<button type="button" class="btn btn-sm btn-success" data-action="recommend"><i class="bi bi-hand-thumbs-up me-1"></i>권고</button>');
    }
    // 승인 — RVWNG/RCMDD, MOLIT
    if ((st === 'RVWNG' || st === 'RCMDD') && ognzSeCd === 'MOLIT') {
      btns.push('<button type="button" class="btn btn-sm btn-success" data-action="approve"><i class="bi bi-check-circle me-1"></i>승인</button>');
    }
    // 취소 — APRVD, MOLIT (사유 필수)
    if (st === 'APRVD' && ognzSeCd === 'MOLIT') {
      btns.push('<button type="button" class="btn btn-sm btn-outline-dark" data-action="cancel"><i class="bi bi-slash-circle me-1"></i>취소</button>');
    }
    // 새 버전 — APRVD/RJCTD, AIRLINE
    if ((st === 'APRVD' || st === 'RJCTD') && ognzSeCd === 'AIRLINE') {
      btns.push('<button type="button" class="btn btn-sm btn-outline-primary" data-action="new-version"><i class="bi bi-files me-1"></i>새 버전</button>');
    }

    if (btns.length === 0) {
      btns.push('<span class="text-muted small">현재 상태(' + esc(st) + ')에서 수행 가능한 액션이 없습니다.</span>');
    }
    $('#actionButtons').html(btns.join(''));
  }

  /* 액션 버튼 이벤트 위임 */
  var _pendingAction = '';
  var _confirmCallback = null;

  $('#actionButtons').on('click', '[data-action]', function () {
    var action = $(this).data('action');
    if (action === 'submit') {
      showConfirm('제출 확인', 'ER을 제출하시겠습니까? 제출 후에는 수정할 수 없습니다.', '제출', function () { doAction('submit'); });
    } else if (action === 'review') {
      showConfirm('검토 시작', 'ER 검토를 시작하시겠습니까?', '검토 시작', function () { doAction('review'); });
    } else if (action === 'recommend') {
      showConfirm('권고 처리', 'ER을 권고 처리하시겠습니까?', '권고', function () { doAction('recommend'); });
    } else if (action === 'approve') {
      showConfirm('승인 확인', 'ER을 승인하시겠습니까?', '승인', function () { doAction('approve'); });
    } else if (action === 'reject') {
      openReasonModal('reject', '반려 사유 입력', '반려');
    } else if (action === 'cancel') {
      openReasonModal('cancel', '취소 사유 입력', '취소');
    } else if (action === 'new-version') {
      showConfirm('새 버전 생성', '새 버전 ER을 생성하시겠습니까?', '생성', function () { doNewVersion(); });
    }
  });

  function doAction(action) {
    var url = BASE + '/' + action;
    $.ajax({ url: url, method: 'POST', contentType: 'application/json', data: '{}' })
      .done(function (res) {
        toastOk((res && res.message) ? res.message : '처리되었습니다.');
        loadBasicInfo();
      })
      .fail(toastErr);
  }

  function doActionWithReason(action, reason) {
    var url = BASE + '/' + action;
    $.ajax({ url: url, method: 'POST', contentType: 'application/json', data: JSON.stringify({ reason: reason }) })
      .done(function (res) {
        toastOk((res && res.message) ? res.message : '처리되었습니다.');
        $('#reasonModal').modal ? $('#reasonModal').modal('hide') : bootstrap.Modal.getInstance($('#reasonModal')[0]).hide();
        loadBasicInfo();
      })
      .fail(function (xhr) {
        toastErr(xhr);
      });
  }

  function doNewVersion() {
    $.ajax({ url: BASE, method: 'POST', contentType: 'application/json',
             data: JSON.stringify({ baseErId: erId }) })
      .done(function (res) {
        var newId = (res.data && res.data.erId) ? res.data.erId : null;
        toastOk('새 버전 ER이 생성되었습니다.');
        if (newId) {
          setTimeout(function () { location.href = '/er/' + encodeURIComponent(newId); }, 1200);
        } else {
          loadBasicInfo();
        }
      })
      .fail(toastErr);
  }

  /* 확인 모달 (Bootstrap 5 alert 대체) */
  var _confirmModal;
  function showConfirm(title, message, okLabel, onOk) {
    if (!$('#_confirmModal').length) {
      $('body').append(
        '<div class="modal fade" id="_confirmModal" tabindex="-1" aria-modal="true" role="dialog">' +
        '<div class="modal-dialog modal-dialog-centered">' +
        '<div class="modal-content">' +
        '<div class="modal-header" style="background:var(--icas-primary);color:white;">' +
        '<h5 class="modal-title" id="_confirmModalTitle"></h5>' +
        '<button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>' +
        '<div class="modal-body" id="_confirmModalBody"></div>' +
        '<div class="modal-footer">' +
        '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>' +
        '<button type="button" class="btn btn-primary" id="_confirmOk"></button>' +
        '</div></div></div></div>'
      );
    }
    $('#_confirmModalTitle').text(title);
    $('#_confirmModalBody').text(message);
    $('#_confirmOk').text(okLabel).off('click').on('click', function () {
      bootstrap.Modal.getInstance($('#_confirmModal')[0]).hide();
      if (typeof onOk === 'function') onOk();
    });
    new bootstrap.Modal($('#_confirmModal')[0]).show();
  }

  /* 사유 모달 */
  var _reasonAction = '';
  function openReasonModal(action, title, okLabel) {
    _reasonAction = action;
    $('#reasonModalLabel').text(title);
    $('#btnReasonConfirm').text(okLabel);
    $('#reasonInput').val('').removeClass('is-invalid');
    $('#reasonInputError').text('');
    $('#reasonLen').text('0');
    new bootstrap.Modal($('#reasonModal')[0]).show();
  }

  $('#reasonInput').on('input', function () {
    $('#reasonLen').text($(this).val().length);
  });

  $('#btnReasonConfirm').on('click', function () {
    var reason = $('#reasonInput').val().trim();
    if (!reason) {
      $('#reasonInput').addClass('is-invalid');
      $('#reasonInputError').text('사유를 입력해주세요.');
      return;
    }
    $('#reasonInput').removeClass('is-invalid');
    doActionWithReason(_reasonAction, reason);
  });

  /* ════════════════════════════════════════════
     4. 합계검증 패널
  ════════════════════════════════════════════ */
  $('#btnValidatePairSum').on('click', function () {
    var $panel = $('#pairSumPanel');
    $panel.removeClass('pass fail pending').addClass('pending');
    $('#pairSumMsg').text('검증 중...');
    $('#pairSumDetail').text('');
    $.get(BASE + '/validate-pair-sum')
      .done(function (res) {
        var d = res.data || res;
        if (d.passed) {
          $panel.removeClass('pending fail').addClass('pass');
          $('#pairSumMsg').html('<strong class="text-success"><i class="bi bi-check-circle me-1"></i>검증 통과</strong>');
        } else {
          $panel.removeClass('pending pass').addClass('fail');
          $('#pairSumMsg').html('<strong class="text-danger"><i class="bi bi-x-circle me-1"></i>검증 실패</strong>');
        }
        var detail = '국가쌍: ' + fmt(d.cntrySum, 4) + 't / 비행장쌍: ' + fmt(d.aerdrmSum, 4) + 't / 편차율: ' + fmt(d.deviationPct, 4) + '%';
        if (d.message) detail = esc(d.message);
        $('#pairSumDetail').text(detail);
      })
      .fail(function (xhr) {
        $panel.removeClass('pass pending').addClass('fail');
        $('#pairSumMsg').text('검증 요청 실패');
        toastErr(xhr);
      });
  });

  /* ════════════════════════════════════════════
     5. 탭 AJAX 데이터 로드
  ════════════════════════════════════════════ */
  function loadTab(tabKey) {
    if (tabLoaded[tabKey]) return;
    tabLoaded[tabKey] = true;
    switch (tabKey) {
      case 'acft-fuel':    loadAcftFuel();    break;
      case 'aerdrm-pair':  loadAerdrmPair();  break;
      case 'afbr':         loadAfbr();        break;
      case 'cntry-pair':   loadCntryPair();   break;
      case 'data-gap':     loadDataGap();     break;
      case 'fuel-smry':    loadFuelSmry();    break;
      case 'vrfr-info':    loadVrfrInfo();    break;
    }
  }

  $('#erDetailTabs button[data-bs-toggle="tab"]').on('shown.bs.tab', function (e) {
    var target = $(e.target).data('bs-target').replace('#pane-', '');
    loadTab(target);
  });

  /* ── Tab: 항공기·연료 ── */
  function loadAcftFuel() {
    $.get(BASE + '/acft-fuel')
      .done(function (res) { renderAcftFuel(res.data || res || []); })
      .fail(function (xhr) { $('#acftFuelBody').html('<tr><td colspan="6" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderAcftFuel(list) {
    $('#acftFuelTotal').text(list.length);
    if (!list.length) { $('#acftFuelBody').html('<tr><td colspan="6" class="text-center text-muted small py-3">등록된 항공기·연료 정보가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.acftTypeCd) + '</td>'
        + '<td>' + esc(r.regisMark) + '</td>'
        + '<td>' + esc(r.ownrLsSeCd) + '</td>'
        + '<td>' + esc(r.fuelTypeCd) + '</td>'
        + '<td>' + esc(r.dnstySecCd || r.dnstySeCd) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-xs btn-outline-primary btn-sm me-1" style="font-size:0.72rem;" data-edit-acft="' + esc(r.acftSn) + '">수정</button>'
                       + '<button type="button" class="btn btn-xs btn-outline-danger btn-sm" style="font-size:0.72rem;" data-del-acft="' + esc(r.acftSn) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#acftFuelBody').html(html);
    // 삭제 버튼
    $('#acftFuelBody').off('click', '[data-del-acft]').on('click', '[data-del-acft]', function () {
      var sn = $(this).data('del-acft');
      showConfirm('삭제 확인', '선택한 항공기·연료 항목을 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/acft-fuel/' + sn, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['acft-fuel'] = false; loadAcftFuel(); })
          .fail(toastErr);
      });
    });
    // 수정 버튼
    $('#acftFuelBody').off('click', '[data-edit-acft]').on('click', '[data-edit-acft]', function () {
      var sn = $(this).data('edit-acft');
      $.get(BASE + '/acft-fuel/' + sn)
        .done(function (res) { openAcftFuelModal(res.data || res); })
        .fail(toastErr);
    });
  }

  function openAcftFuelModal(d) {
    d = d || {};
    $('#acftFuelAcftSn').val(d.acftSn || '');
    $('#acftTypeCd').val(d.acftTypeCd || '');
    $('#regisMark').val(d.regisMark || '');
    $('#ownrLsSeCd').val(d.ownrLsSeCd || '');
    $('#fuelTypeCd').val(d.fuelTypeCd || '');
    $('#dnstySecCd').val(d.dnstySecCd || d.dnstySeCd || 'STD');
    $('#acftFuelModalLabel').text(d.acftSn ? '항공기·연료 수정' : '항공기·연료 등록');
    new bootstrap.Modal($('#acftFuelModal')[0]).show();
  }

  $('#btnAddAcftFuel').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openAcftFuelModal({});
  });

  $('#btnSaveAcftFuel').on('click', function () {
    var sn   = $('#acftFuelAcftSn').val();
    var payload = {
      acftTypeCd: $('#acftTypeCd').val().trim(),
      regisMark:  $('#regisMark').val().trim(),
      ownrLsSeCd: $('#ownrLsSeCd').val(),
      fuelTypeCd: $('#fuelTypeCd').val(),
      dnstySeCd:  $('#dnstySecCd').val()
    };
    if (!payload.acftTypeCd) { $('#acftTypeCd').addClass('is-invalid'); return; }
    if (!payload.regisMark)  { $('#regisMark').addClass('is-invalid');  return; }
    if (!payload.fuelTypeCd) { $('#fuelTypeCd').addClass('is-invalid'); return; }
    var method = sn ? 'PUT' : 'POST';
    var url    = sn ? (BASE + '/acft-fuel/' + sn) : (BASE + '/acft-fuel');
    $.ajax({ url: url, method: method, contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#acftFuelModal')[0]).hide();
        tabLoaded['acft-fuel'] = false; loadAcftFuel();
      })
      .fail(toastErr);
  });

  /* ── Tab: 공항쌍 ── */
  function loadAerdrmPair() {
    $.get(BASE + '/aerdrm-pair')
      .done(function (res) { renderAerdrmPair(res.data || res || []); })
      .fail(function (xhr) { $('#aerdrmPairBody').html('<tr><td colspan="8" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderAerdrmPair(list) {
    $('#aerdrmPairTotal').text(list.length);
    if (!list.length) { $('#aerdrmPairBody').html('<tr><td colspan="8" class="text-center text-muted small py-3">등록된 비행장 쌍 데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.dprtrAerdrmCd) + '</td>'
        + '<td>' + esc(r.arvlAerdrmCd) + '</td>'
        + '<td>' + esc(r.dprtrCntryCd) + '</td>'
        + '<td>' + esc(r.arvlCntryCd) + '</td>'
        + '<td class="text-end">' + fmt(r.fltCnt, 0) + '</td>'
        + '<td class="text-end">' + fmt(r.fuelWght, 4) + '</td>'
        + '<td class="text-end">' + fmt(r.co2Emsn, 4) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-sm btn-outline-primary me-1" style="font-size:0.72rem;" data-edit-aerdrm="' + esc(r.pairSn) + '">수정</button>'
                       + '<button type="button" class="btn btn-sm btn-outline-danger" style="font-size:0.72rem;" data-del-aerdrm="' + esc(r.pairSn) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#aerdrmPairBody').html(html);
    $('#aerdrmPairBody').off('click', '[data-del-aerdrm]').on('click', '[data-del-aerdrm]', function () {
      var sn = $(this).data('del-aerdrm');
      showConfirm('삭제 확인', '선택한 비행장 쌍 데이터를 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/aerdrm-pair/' + sn, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['aerdrm-pair'] = false; loadAerdrmPair(); })
          .fail(toastErr);
      });
    });
    $('#aerdrmPairBody').off('click', '[data-edit-aerdrm]').on('click', '[data-edit-aerdrm]', function () {
      var sn = $(this).data('edit-aerdrm');
      $.get(BASE + '/aerdrm-pair/' + sn).done(function (res) { openAerdrmPairModal(res.data || res); }).fail(toastErr);
    });
  }

  function openAerdrmPairModal(d) {
    d = d || {};
    $('#aerdrmPairSn').val(d.pairSn || '');
    $('#aerdrmDprtrCd').val(d.dprtrAerdrmCd || '');
    $('#aerdrmArvlCd').val(d.arvlAerdrmCd || '');
    $('#aerdrmDprtrCntryCd').val(d.dprtrCntryCd || '');
    $('#aerdrmArvlCntryCd').val(d.arvlCntryCd || '');
    $('#aerdrmFltCnt').val(d.fltCnt || '');
    $('#aerdrmFuelWght').val(d.fuelWght || '');
    $('#aerdrmCo2Emsn').val(d.co2Emsn || '');
    $('#aerdrmPairModalLabel').text(d.pairSn ? '비행장 쌍 배출량 수정' : '비행장 쌍 배출량 등록');
    new bootstrap.Modal($('#aerdrmPairModal')[0]).show();
  }

  $('#btnAddAerdrmPair').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openAerdrmPairModal({});
  });

  $('#btnSaveAerdrmPair').on('click', function () {
    var sn = $('#aerdrmPairSn').val();
    var payload = {
      dprtrAerdrmCd: $('#aerdrmDprtrCd').val().toUpperCase().trim(),
      arvlAerdrmCd:  $('#aerdrmArvlCd').val().toUpperCase().trim(),
      dprtrCntryCd:  $('#aerdrmDprtrCntryCd').val().toUpperCase().trim(),
      arvlCntryCd:   $('#aerdrmArvlCntryCd').val().toUpperCase().trim(),
      fltCnt:        parseInt($('#aerdrmFltCnt').val(), 10) || 0,
      fuelWght:      parseFloat($('#aerdrmFuelWght').val()) || 0,
      co2Emsn:       parseFloat($('#aerdrmCo2Emsn').val()) || 0
    };
    var method = sn ? 'PUT' : 'POST';
    var url    = sn ? (BASE + '/aerdrm-pair/' + sn) : (BASE + '/aerdrm-pair');
    $.ajax({ url: url, method: method, contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#aerdrmPairModal')[0]).hide();
        tabLoaded['aerdrm-pair'] = false; loadAerdrmPair();
      })
      .fail(toastErr);
  });

  /* ── Tab: AFBR ── */
  function loadAfbr() {
    $.get(BASE + '/afbr')
      .done(function (res) { renderAfbr(res.data || res || []); })
      .fail(function (xhr) { $('#afbrBody').html('<tr><td colspan="4" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderAfbr(list) {
    $('#afbrTotal').text(list.length);
    if (!list.length) { $('#afbrBody').html('<tr><td colspan="4" class="text-center text-muted small py-3">등록된 AFBR 데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.acftTypeCd) + '</td>'
        + '<td class="text-end">' + fmt(r.afbrVal, 4) + '</td>'
        + '<td>' + esc(r.afbrUnit) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-sm btn-outline-primary me-1" style="font-size:0.72rem;" data-edit-afbr="' + esc(r.acftTypeCd) + '">수정</button>'
                       + '<button type="button" class="btn btn-sm btn-outline-danger" style="font-size:0.72rem;" data-del-afbr="' + esc(r.acftTypeCd) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#afbrBody').html(html);
    $('#afbrBody').off('click', '[data-del-afbr]').on('click', '[data-del-afbr]', function () {
      var cd = $(this).data('del-afbr');
      showConfirm('삭제 확인', '선택한 AFBR 항목을 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/afbr/' + cd, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['afbr'] = false; loadAfbr(); })
          .fail(toastErr);
      });
    });
    $('#afbrBody').off('click', '[data-edit-afbr]').on('click', '[data-edit-afbr]', function () {
      var cd = $(this).data('edit-afbr');
      $.get(BASE + '/afbr/' + cd).done(function (res) { openAfbrModal(res.data || res); }).fail(toastErr);
    });
  }

  function openAfbrModal(d) {
    d = d || {};
    $('#afbrAcftTypeCd').val(d.acftTypeCd || '').prop('readonly', !!d.acftTypeCd);
    $('#afbrVal').val(d.afbrVal || '');
    $('#afbrUnit').val(d.afbrUnit || '');
    $('#afbrModalLabel').text(d.acftTypeCd ? 'AFBR 수정' : 'AFBR 등록');
    new bootstrap.Modal($('#afbrModal')[0]).show();
  }

  $('#btnAddAfbr').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openAfbrModal({});
  });

  $('#btnSaveAfbr').on('click', function () {
    var cd = $('#afbrAcftTypeCd').val().trim();
    if (!cd) { showToast('항공기 유형 코드를 입력하세요.', 'warn'); return; }
    var payload = { afbrVal: parseFloat($('#afbrVal').val()) || 0, afbrUnit: $('#afbrUnit').val().trim() };
    $.ajax({ url: BASE + '/afbr/' + encodeURIComponent(cd), method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#afbrModal')[0]).hide();
        tabLoaded['afbr'] = false; loadAfbr();
      })
      .fail(toastErr);
  });

  /* ── Tab: 국가쌍 ── */
  function loadCntryPair() {
    $.get(BASE + '/cntry-pair')
      .done(function (res) { renderCntryPair(res.data || res || []); })
      .fail(function (xhr) { $('#cntryPairBody').html('<tr><td colspan="9" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderCntryPair(list) {
    $('#cntryPairTotal').text(list.length);
    if (!list.length) { $('#cntryPairBody').html('<tr><td colspan="9" class="text-center text-muted small py-3">등록된 국가 쌍 데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.dprtrCntryCd) + '</td>'
        + '<td>' + esc(r.arvlCntryCd) + '</td>'
        + '<td class="text-center">' + yn(r.cerEstmYn) + '</td>'
        + '<td class="text-end">' + fmt(r.fltCnt, 0) + '</td>'
        + '<td class="text-end">' + fmt(r.fuelWght, 4) + '</td>'
        + '<td class="text-end">' + fmt(r.convFctr, 4) + '</td>'
        + '<td class="text-end">' + fmt(r.co2Emsn, 4) + '</td>'
        + '<td class="text-center">' + yn(r.ofstReqYn) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-sm btn-outline-primary me-1" style="font-size:0.72rem;" data-edit-cntry="' + esc(r.pairSn) + '">수정</button>'
                       + '<button type="button" class="btn btn-sm btn-outline-danger" style="font-size:0.72rem;" data-del-cntry="' + esc(r.pairSn) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#cntryPairBody').html(html);
    $('#cntryPairBody').off('click', '[data-del-cntry]').on('click', '[data-del-cntry]', function () {
      var sn = $(this).data('del-cntry');
      showConfirm('삭제 확인', '선택한 국가 쌍 데이터를 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/cntry-pair/' + sn, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['cntry-pair'] = false; loadCntryPair(); })
          .fail(toastErr);
      });
    });
    $('#cntryPairBody').off('click', '[data-edit-cntry]').on('click', '[data-edit-cntry]', function () {
      var sn = $(this).data('edit-cntry');
      $.get(BASE + '/cntry-pair/' + sn).done(function (res) { openCntryPairModal(res.data || res); }).fail(toastErr);
    });
  }

  function openCntryPairModal(d) {
    d = d || {};
    $('#cntryPairSn').val(d.pairSn || '');
    $('#cntryDprtrCd').val(d.dprtrCntryCd || '');
    $('#cntryArvlCd').val(d.arvlCntryCd || '');
    $('#cntryCerEstmYn').val(d.cerEstmYn || 'N');
    $('#cntryFltCnt').val(d.fltCnt || '');
    $('#cntryFuelWght').val(d.fuelWght || '');
    $('#cntryConvFctr').val(d.convFctr || '');
    $('#cntryCo2Emsn').val(d.co2Emsn || '');
    $('#cntryOfstReqYn').val(d.ofstReqYn || 'N');
    $('#cntryPairModalLabel').text(d.pairSn ? '국가 쌍 배출량 수정' : '국가 쌍 배출량 등록');
    new bootstrap.Modal($('#cntryPairModal')[0]).show();
  }

  $('#btnAddCntryPair').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openCntryPairModal({});
  });

  $('#btnSaveCntryPair').on('click', function () {
    var sn = $('#cntryPairSn').val();
    var payload = {
      dprtrCntryCd: $('#cntryDprtrCd').val().toUpperCase().trim(),
      arvlCntryCd:  $('#cntryArvlCd').val().toUpperCase().trim(),
      cerEstmYn:    $('#cntryCerEstmYn').val(),
      fltCnt:       parseInt($('#cntryFltCnt').val(), 10) || 0,
      fuelWght:     parseFloat($('#cntryFuelWght').val()) || 0,
      convFctr:     parseFloat($('#cntryConvFctr').val()) || 0,
      co2Emsn:      parseFloat($('#cntryCo2Emsn').val()) || 0,
      ofstReqYn:    $('#cntryOfstReqYn').val()
    };
    var method = sn ? 'PUT' : 'POST';
    var url    = sn ? (BASE + '/cntry-pair/' + sn) : (BASE + '/cntry-pair');
    $.ajax({ url: url, method: method, contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#cntryPairModal')[0]).hide();
        tabLoaded['cntry-pair'] = false; loadCntryPair();
      })
      .fail(toastErr);
  });

  /* ── Tab: 데이터갭 ── */
  function loadDataGap() {
    $.get(BASE + '/data-gap')
      .done(function (res) { renderDataGap(res.data || res || []); })
      .fail(function (xhr) { $('#dataGapBody').html('<tr><td colspan="8" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderDataGap(list) {
    $('#dataGapTotal').text(list.length);
    if (!list.length) { $('#dataGapBody').html('<tr><td colspan="8" class="text-center text-muted small py-3">등록된 데이터 갭이 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.gapDt) + '</td>'
        + '<td class="text-truncate" style="max-width:120px;" title="' + esc(r.refInfo) + '">' + esc(r.refInfo) + '</td>'
        + '<td>' + esc(r.gapCauseCd) + '</td>'
        + '<td>' + esc(r.gapTypeCd) + '</td>'
        + '<td class="text-end">' + fmt(r.afctCo2Emsn, 4) + '</td>'
        + '<td class="text-center">' + yn(r.thrshld5pctXcYn) + '</td>'
        + '<td class="text-truncate" style="max-width:150px;" title="' + esc(r.replMthdDesc) + '">' + esc(r.replMthdDesc) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-sm btn-outline-primary me-1" style="font-size:0.72rem;" data-edit-gap="' + esc(r.gapSn) + '">수정</button>'
                       + '<button type="button" class="btn btn-sm btn-outline-danger" style="font-size:0.72rem;" data-del-gap="' + esc(r.gapSn) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#dataGapBody').html(html);
    $('#dataGapBody').off('click', '[data-del-gap]').on('click', '[data-del-gap]', function () {
      var sn = $(this).data('del-gap');
      showConfirm('삭제 확인', '선택한 데이터 갭 항목을 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/data-gap/' + sn, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['data-gap'] = false; loadDataGap(); })
          .fail(toastErr);
      });
    });
    $('#dataGapBody').off('click', '[data-edit-gap]').on('click', '[data-edit-gap]', function () {
      var sn = $(this).data('edit-gap');
      $.get(BASE + '/data-gap/' + sn).done(function (res) { openDataGapModal(res.data || res); }).fail(toastErr);
    });
  }

  function openDataGapModal(d) {
    d = d || {};
    $('#dataGapSn').val(d.gapSn || '');
    $('#gapDt').val(d.gapDt || '');
    $('#gapCauseCd').val(d.gapCauseCd || '');
    $('#gapTypeCd').val(d.gapTypeCd || '');
    $('#gapRefInfo').val(d.refInfo || '');
    $('#gapAfctCo2Emsn').val(d.afctCo2Emsn || '');
    $('#gapReplMthdDesc').val(d.replMthdDesc || '');
    $('#dataGapModalLabel').text(d.gapSn ? '데이터 갭 수정' : '데이터 갭 등록');
    new bootstrap.Modal($('#dataGapModal')[0]).show();
  }

  $('#btnAddDataGap').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openDataGapModal({});
  });

  $('#btnSaveDataGap').on('click', function () {
    var sn = $('#dataGapSn').val();
    if (!$('#gapDt').val()) { showToast('갭 발생일을 입력하세요.', 'warn'); return; }
    var payload = {
      gapDt:         $('#gapDt').val(),
      gapCauseCd:    $('#gapCauseCd').val().trim(),
      gapTypeCd:     $('#gapTypeCd').val().trim(),
      refInfo:       $('#gapRefInfo').val().trim(),
      afctCo2Emsn:   parseFloat($('#gapAfctCo2Emsn').val()) || null,
      replMthdDesc:  $('#gapReplMthdDesc').val().trim()
    };
    var method = sn ? 'PUT' : 'POST';
    var url    = sn ? (BASE + '/data-gap/' + sn) : (BASE + '/data-gap');
    $.ajax({ url: url, method: method, contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#dataGapModal')[0]).hide();
        tabLoaded['data-gap'] = false; loadDataGap();
      })
      .fail(toastErr);
  });

  /* ── Tab: 연료요약 ── */
  function loadFuelSmry() {
    $.get(BASE + '/fuel-smry')
      .done(function (res) { renderFuelSmry(res.data || res || []); })
      .fail(function (xhr) { $('#fuelSmryBody').html('<tr><td colspan="3" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderFuelSmry(list) {
    $('#fuelSmryTotal').text(list.length);
    if (!list.length) { $('#fuelSmryBody').html('<tr><td colspan="3" class="text-center text-muted small py-3">등록된 연료요약 데이터가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.fuelTypeCd) + '</td>'
        + '<td class="text-end">' + fmt(r.ttlFuelWght, 4) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-sm btn-outline-primary me-1" style="font-size:0.72rem;" data-edit-fsmry="' + esc(r.fuelTypeCd) + '">수정</button>'
                       + '<button type="button" class="btn btn-sm btn-outline-danger" style="font-size:0.72rem;" data-del-fsmry="' + esc(r.fuelTypeCd) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#fuelSmryBody').html(html);
    $('#fuelSmryBody').off('click', '[data-del-fsmry]').on('click', '[data-del-fsmry]', function () {
      var cd = $(this).data('del-fsmry');
      showConfirm('삭제 확인', '선택한 연료요약 항목을 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/fuel-smry/' + cd, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['fuel-smry'] = false; loadFuelSmry(); })
          .fail(toastErr);
      });
    });
    $('#fuelSmryBody').off('click', '[data-edit-fsmry]').on('click', '[data-edit-fsmry]', function () {
      var cd = $(this).data('edit-fsmry');
      $.get(BASE + '/fuel-smry/' + cd).done(function (res) { openFuelSmryModal(res.data || res); }).fail(toastErr);
    });
  }

  function openFuelSmryModal(d) {
    d = d || {};
    $('#fuelSmryFuelTypeCd').val(d.fuelTypeCd || '').prop('disabled', !!d.fuelTypeCd);
    $('#fuelSmryTtlFuelWght').val(d.ttlFuelWght || '');
    $('#fuelSmryModalLabel').text(d.fuelTypeCd ? '연료요약 수정' : '연료요약 등록');
    new bootstrap.Modal($('#fuelSmryModal')[0]).show();
  }

  $('#btnAddFuelSmry').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openFuelSmryModal({});
  });

  $('#btnSaveFuelSmry').on('click', function () {
    var cd = $('#fuelSmryFuelTypeCd').val();
    if (!cd) { showToast('연료 유형 코드를 선택하세요.', 'warn'); return; }
    var payload = { ttlFuelWght: parseFloat($('#fuelSmryTtlFuelWght').val()) || 0 };
    $.ajax({ url: BASE + '/fuel-smry/' + encodeURIComponent(cd), method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#fuelSmryModal')[0]).hide();
        tabLoaded['fuel-smry'] = false; loadFuelSmry();
      })
      .fail(toastErr);
  });

  /* ── Tab: 검증기관정보 ── */
  function loadVrfrInfo() {
    $.get(BASE + '/vrfr-info')
      .done(function (res) { renderVrfrInfo(res.data || res || []); })
      .fail(function (xhr) { $('#vrfrInfoBody').html('<tr><td colspan="4" class="text-center text-danger small py-3">데이터 로드 실패</td></tr>'); toastErr(xhr); });
  }

  function renderVrfrInfo(list) {
    $('#vrfrInfoTotal').text(list.length);
    if (!list.length) { $('#vrfrInfoBody').html('<tr><td colspan="4" class="text-center text-muted small py-3">등록된 검증기관 정보가 없습니다.</td></tr>'); return; }
    var html = '';
    list.forEach(function (r) {
      html += '<tr>'
        + '<td>' + esc(r.vrfcnInstId) + '</td>'
        + '<td class="text-truncate" style="max-width:180px;" title="' + esc(r.cnctDesc) + '">' + esc(r.cnctDesc) + '</td>'
        + '<td class="text-truncate" style="max-width:200px;" title="' + esc(r.accrdDtl) + '">' + esc(r.accrdDtl) + '</td>'
        + '<td>'
        + (isEditable ? '<button type="button" class="btn btn-sm btn-outline-primary me-1" style="font-size:0.72rem;" data-edit-vrfr="' + esc(r.vrfrSn) + '">수정</button>'
                       + '<button type="button" class="btn btn-sm btn-outline-danger" style="font-size:0.72rem;" data-del-vrfr="' + esc(r.vrfrSn) + '">삭제</button>'
                      : '<span class="text-muted small">-</span>')
        + '</td></tr>';
    });
    $('#vrfrInfoBody').html(html);
    $('#vrfrInfoBody').off('click', '[data-del-vrfr]').on('click', '[data-del-vrfr]', function () {
      var sn = $(this).data('del-vrfr');
      showConfirm('삭제 확인', '선택한 검증기관 정보를 삭제하시겠습니까?', '삭제', function () {
        $.ajax({ url: BASE + '/vrfr-info/' + sn, method: 'DELETE' })
          .done(function () { toastOk('삭제되었습니다.'); tabLoaded['vrfr-info'] = false; loadVrfrInfo(); })
          .fail(toastErr);
      });
    });
    $('#vrfrInfoBody').off('click', '[data-edit-vrfr]').on('click', '[data-edit-vrfr]', function () {
      var sn = $(this).data('edit-vrfr');
      $.get(BASE + '/vrfr-info/' + sn).done(function (res) { openVrfrInfoModal(res.data || res); }).fail(toastErr);
    });
  }

  function openVrfrInfoModal(d) {
    d = d || {};
    $('#vrfrInfoSn').val(d.vrfrSn || '');
    $('#vrfcnInstId').val(d.vrfcnInstId || '');
    $('#cnctDesc').val(d.cnctDesc || '');
    $('#accrdDtl').val(d.accrdDtl || '');
    $('#vrfrInfoModalLabel').text(d.vrfrSn ? '참여 검증기관 정보 수정' : '참여 검증기관 정보 등록');
    new bootstrap.Modal($('#vrfrInfoModal')[0]).show();
  }

  $('#btnAddVrfrInfo').on('click', function () {
    if (!isEditable) { showToast('DRAFT 상태에서만 추가할 수 있습니다.', 'warn'); return; }
    openVrfrInfoModal({});
  });

  $('#btnSaveVrfrInfo').on('click', function () {
    var sn = $('#vrfrInfoSn').val();
    var instId = $('#vrfcnInstId').val().trim();
    if (!instId) { showToast('검증기관 ID를 입력하세요.', 'warn'); return; }
    var payload = { vrfcnInstId: instId, cnctDesc: $('#cnctDesc').val().trim(), accrdDtl: $('#accrdDtl').val().trim() };
    var method = sn ? 'PUT' : 'POST';
    var url    = sn ? (BASE + '/vrfr-info/' + sn) : (BASE + '/vrfr-info');
    $.ajax({ url: url, method: method, contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function () {
        toastOk('저장되었습니다.');
        bootstrap.Modal.getInstance($('#vrfrInfoModal')[0]).hide();
        tabLoaded['vrfr-info'] = false; loadVrfrInfo();
      })
      .fail(toastErr);
  });

  /* ════════════════════════════════════════════
     초기화
  ════════════════════════════════════════════ */
  $(function () {
    if (!erId || erId === 'detail') {
      $('#pairSumPanel').hide();
      $('#lifecycleActionBar').hide();
      $('#erDetailTabs').hide();
      $('#erDetailTabContent').hide();
      $('#basicInfoCard .card-body').html('<div class="text-danger">ER ID가 없습니다. URL을 확인하세요.</div>');
      return;
    }
    loadBasicInfo();
  });

})();

/**
 * 법정 서식 출력 — RFP 박스 ② "법정 서식 출력" 항목
 * @param {string} fmt 'pdf' | 'xlsx'
 */
function exportLegalForm(fmt) {
  var erId = (typeof ER_ID !== 'undefined' && ER_ID)
           || document.getElementById('headerErId')?.innerText?.trim()
           || (location.pathname.split('/').pop());
  if (!erId) {
    if (window.IcasAlert) IcasAlert.error('ER ID를 확인할 수 없습니다.');
    return;
  }
  // 발주처 직구매 리포팅 툴 연계 자리 (1차: placeholder)
  if (window.IcasAlert) {
    IcasAlert.info('법정 서식 출력 (' + fmt.toUpperCase() + ') — ER ' + erId + ' (RFP "법정 서식 출력" 항목, 리포팅 툴 직접구매 연계)');
  }
  // 2차/직구매 리포팅 SW 연동 시:
  // window.open('/api/er/rprt/' + encodeURIComponent(erId) + '/export?format=' + fmt, '_blank');
}
</script>
</body>
</html>
