<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>EMP 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root {
  --icas-primary: #0F2C72;
  --icas-primary-hover: #0A2058;
  --icas-accent: #0D6EFD;
  --icas-success: #198754;
  --icas-warning: #FFC107;
  --icas-danger: #DC3545;
  --icas-text: #495057;
  --icas-text-muted: #6C757D;
  --icas-border: #DEE2E6;
  --icas-bg: #F8F9FA;
}
body { background: #f0f2f5; font-family: 'Pretendard', '맑은 고딕', sans-serif; }
.page-header-bar { background: white; border-bottom: 1px solid var(--icas-border); }
.status-badge { font-size: 0.78rem; padding: 4px 10px; border-radius: 4px; font-weight: 600; }
.btn-icas-primary { background: var(--icas-primary); color: white; border: none; }
.btn-icas-primary:hover { background: var(--icas-primary-hover); color: white; }
.detail-card { border: none; box-shadow: 0 1px 4px rgba(0,0,0,.08); margin-bottom: 1rem; }
.detail-card .card-header {
  background: var(--icas-bg);
  border-bottom: 1px solid var(--icas-border);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--icas-primary);
  padding: 0.65rem 1rem;
}
.form-label { font-size: 0.82rem; font-weight: 600; color: var(--icas-text); margin-bottom: 3px; }
.form-control, .form-select { font-size: 0.85rem; }
.form-control:read-only { background: #f8f9fa; color: #6c757d; }
.req { color: var(--icas-danger); }
.info-row { display: flex; flex-wrap: wrap; gap: 1.5rem 2rem; }
.info-item { min-width: 180px; }
.info-item .label { font-size: 0.75rem; color: var(--icas-text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: .04em; }
.info-item .value { font-size: 0.92rem; color: var(--icas-text); font-weight: 500; margin-top: 2px; }
/* 탭 */
.nav-tabs .nav-link { font-size: 0.85rem; color: var(--icas-text); border-bottom: 2px solid transparent; }
.nav-tabs .nav-link.active { color: var(--icas-primary); font-weight: 600; border-bottom-color: var(--icas-primary); background: white; }
.tab-content { background: white; border: 1px solid var(--icas-border); border-top: none; border-radius: 0 0 6px 6px; }
/* 라이프사이클 액션 버튼 영역 */
.lifecycle-actions { background: white; border: 1px solid var(--icas-border); border-radius: 6px; padding: 0.75rem 1rem; margin-bottom: 1rem; display: flex; align-items: center; flex-wrap: wrap; gap: 0.5rem; }
.lifecycle-actions .lc-label { font-size: 0.8rem; color: var(--icas-text-muted); font-weight: 600; margin-right: 0.5rem; }
/* 그리드 내 테이블 */
.table-icas-inner thead th { background: var(--icas-primary); color: white; font-size: 0.8rem; font-weight: 500; border: none; white-space: nowrap; }
.table-icas-inner tbody td { font-size: 0.83rem; vertical-align: middle; }
.table-icas-inner tbody tr:hover { background: #f8f9ff; }
/* inline edit */
.edit-mode .form-control { background: white; }
.view-mode .form-control { background: #f8f9fa; color: #495057; border-color: transparent; }
.view-mode .form-select { background: #f8f9fa; color: #495057; border-color: transparent; pointer-events: none; }
.edit-toggle-bar { display: flex; align-items: center; justify-content: flex-end; gap: 0.5rem; margin-bottom: 0.5rem; }
/* 뒤로가기 */
.page-back-row { padding: 0.5rem 1.5rem 0; }
.page-back-row a { font-size: 0.85rem; color: var(--icas-text-muted); text-decoration: none; }
.page-back-row a:hover { color: var(--icas-primary); }
/* 로딩 오버레이 */
#tabLoadingOverlay { display: none; position: absolute; inset: 0; background: rgba(255,255,255,.7); z-index: 10; align-items: center; justify-content: center; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">

  <!-- 뒤로가기 -->
  <div class="page-back-row">
    <a href="/emp/plan" id="backToList">
      <i class="bi bi-arrow-left me-1"></i>목록으로
    </a>
  </div>

  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3 mt-1">
    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
      <div>
        <div class="d-flex align-items-center gap-2 mb-1">
          <h5 class="fw-bold mb-0" style="color:var(--icas-primary);">
            <i class="bi bi-file-earmark-text me-2"></i>EMP 상세
          </h5>
          <span id="headerStatusBadge"></span>
        </div>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/emp/plan" class="text-decoration-none">EMP 목록</a></li>
            <li class="breadcrumb-item active" id="breadcrumbId">상세</li>
          </ol>
        </nav>
      </div>
      <!-- 라이프사이클 액션 버튼 -->
      <div id="lifecycleActions" class="d-flex flex-wrap gap-2 align-items-center" style="display:none!important;">
        <!-- JS 로 동적 생성 -->
      </div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- 기본정보 카드 -->
    <div class="card detail-card" id="basicInfoCard">
      <div class="card-header d-flex align-items-center justify-content-between">
        <span><i class="bi bi-info-circle me-1"></i>기본 정보</span>
        <div class="edit-toggle-bar mb-0" id="basicInfoEditBar" style="display:none!important;">
          <button type="button" id="btnBasicEdit" class="btn btn-sm btn-outline-primary">
            <i class="bi bi-pencil me-1"></i>편집
          </button>
          <button type="button" id="btnBasicSave" class="btn btn-sm btn-icas-primary" style="display:none;">
            <i class="bi bi-check-lg me-1"></i>저장
          </button>
          <button type="button" id="btnBasicCancel" class="btn btn-sm btn-outline-secondary" style="display:none;">
            취소
          </button>
        </div>
      </div>
      <div class="card-body">
        <div id="basicInfoView">
          <!-- JS 로 렌더 -->
          <div class="text-center py-4 text-muted">
            <div class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></div>
            <span>로딩 중...</span>
          </div>
        </div>
        <form id="basicInfoForm" style="display:none;">
          <div class="row g-3">
            <div class="col-md-3">
              <label for="frmRprtYr" class="form-label">보고연도 <span class="req" aria-hidden="true">*</span></label>
              <input type="text" id="frmRprtYr" name="rprtYr" class="form-control" maxlength="4" aria-required="true" readonly>
            </div>
            <div class="col-md-3">
              <label for="frmEmpVer" class="form-label">버전</label>
              <input type="text" id="frmEmpVer" name="empVer" class="form-control" readonly>
            </div>
            <div class="col-md-3">
              <label for="frmSbmtDt" class="form-label">제출일시</label>
              <input type="text" id="frmSbmtDt" name="sbmtDt" class="form-control" readonly>
            </div>
            <div class="col-md-3">
              <label for="frmAprvDt" class="form-label">승인일시</label>
              <input type="text" id="frmAprvDt" name="aprvDt" class="form-control" readonly>
            </div>
          </div>
          <div class="invalid-feedback d-block" id="basicInfoFormError" role="alert"></div>
        </form>
      </div>
    </div>

    <!-- 탭 영역 -->
    <div class="card detail-card p-0">
      <div class="card-header pb-0" style="border-bottom:none; background:white;">
        <ul class="nav nav-tabs border-0" id="empDetailTabs" role="tablist">
          <li class="nav-item" role="presentation">
            <button class="nav-link active" id="tab-info" data-bs-toggle="tab" data-bs-target="#tabInfo"
                    type="button" role="tab" aria-controls="tabInfo" aria-selected="true">
              <i class="bi bi-building me-1"></i>운영자정보
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-acft" data-bs-toggle="tab" data-bs-target="#tabAcft"
                    type="button" role="tab" aria-controls="tabAcft" aria-selected="false">
              <i class="bi bi-airplane me-1"></i>항공기
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-cntry" data-bs-toggle="tab" data-bs-target="#tabCntry"
                    type="button" role="tab" aria-controls="tabCntry" aria-selected="false">
              <i class="bi bi-globe2 me-1"></i>국가쌍
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-cnct" data-bs-toggle="tab" data-bs-target="#tabCnct"
                    type="button" role="tab" aria-controls="tabCnct" aria-selected="false">
              <i class="bi bi-person-lines-fill me-1"></i>운영자연결
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-co2" data-bs-toggle="tab" data-bs-target="#tabCo2"
                    type="button" role="tab" aria-controls="tabCo2" aria-selected="false">
              <i class="bi bi-calculator me-1"></i>CO2계산
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-co2detail" data-bs-toggle="tab" data-bs-target="#tabCo2Detail"
                    type="button" role="tab" aria-controls="tabCo2Detail" aria-selected="false">
              <i class="bi bi-list-columns me-1"></i>CO2상세
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-ctrl" data-bs-toggle="tab" data-bs-target="#tabCtrl"
                    type="button" role="tab" aria-controls="tabCtrl" aria-selected="false">
              <i class="bi bi-shield-check me-1"></i>데이터관리
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-risk" data-bs-toggle="tab" data-bs-target="#tabRisk"
                    type="button" role="tab" aria-controls="tabRisk" aria-selected="false">
              <i class="bi bi-exclamation-triangle me-1"></i>리스크
            </button>
          </li>
        </ul>
      </div>
      <div class="tab-content" id="empDetailTabContent" style="position:relative; min-height:300px;">

        <!-- 로딩 오버레이 -->
        <div id="tabLoadingOverlay" style="display:none; position:absolute; inset:0; background:rgba(255,255,255,.7); z-index:10; align-items:center; justify-content:center;">
          <div class="spinner-border text-primary" role="status" aria-label="로딩 중"></div>
        </div>

        <!-- ── 탭 1: 운영자정보 ── -->
        <div class="tab-pane fade show active p-4" id="tabInfo" role="tabpanel" aria-labelledby="tab-info">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">운영사 식별정보</h6>
            <button type="button" class="btn btn-sm btn-outline-primary edit-btn" data-tab="info" style="display:none;">
              <i class="bi bi-pencil me-1"></i>편집
            </button>
          </div>
          <div id="infoViewArea">
            <div class="text-center py-4 text-muted small">탭을 클릭하면 데이터를 불러옵니다.</div>
          </div>
          <form id="infoEditForm" style="display:none;" data-url-tpl="/api/emp/plan/{id}/info">
            <div class="row g-3">
              <div class="col-md-6">
                <label for="infoOprtrNm" class="form-label">운영사 명칭 <span class="req" aria-hidden="true">*</span></label>
                <input type="text" id="infoOprtrNm" name="oprtrNm" class="form-control" maxlength="200" aria-required="true">
                <div class="invalid-feedback" id="infoOprtrNm-error" role="alert"></div>
              </div>
              <div class="col-md-6">
                <label for="infoAddr" class="form-label">주소</label>
                <input type="text" id="infoAddr" name="addr" class="form-control" maxlength="500">
              </div>
              <div class="col-md-4">
                <label for="infoLglrprNm" class="form-label">법정대리인</label>
                <input type="text" id="infoLglrprNm" name="lglrprNm" class="form-control" maxlength="100">
              </div>
              <div class="col-md-4">
                <label for="infoIcaoDesig" class="form-label">ICAO 지정어 <span class="req" aria-hidden="true">*</span></label>
                <input type="text" id="infoIcaoDesig" name="icaoDesig" class="form-control text-uppercase" maxlength="4" aria-required="true"
                       placeholder="예: KAL">
                <div class="invalid-feedback" id="infoIcaoDesig-error" role="alert"></div>
              </div>
              <div class="col-md-4">
                <label for="infoRegisMark" class="form-label">등록기호</label>
                <input type="text" id="infoRegisMark" name="regisMark" class="form-control" maxlength="50">
              </div>
              <div class="col-md-4">
                <label for="infoAocNo" class="form-label">AOC 번호</label>
                <input type="text" id="infoAocNo" name="aocNo" class="form-control" maxlength="50">
              </div>
              <div class="col-md-4">
                <label for="infoAocIssueDt" class="form-label">AOC 발급일</label>
                <input type="date" id="infoAocIssueDt" name="aocIssueDt" class="form-control">
              </div>
              <div class="col-md-4">
                <label for="infoAocXprDt" class="form-label">AOC 만료일</label>
                <input type="date" id="infoAocXprDt" name="aocXprDt" class="form-control">
              </div>
              <div class="col-md-6">
                <label for="infoParentCo" class="form-label">모회사</label>
                <input type="text" id="infoParentCo" name="parentCo" class="form-control" maxlength="200">
              </div>
              <div class="col-md-6">
                <label for="infoSbsdryInfo" class="form-label">자회사 정보</label>
                <textarea id="infoSbsdryInfo" name="sbsdryInfo" class="form-control" rows="2" maxlength="2000"></textarea>
              </div>
            </div>
            <div class="mt-3 d-flex gap-2 justify-content-end">
              <button type="button" class="btn btn-sm btn-outline-secondary cancel-edit-btn" data-tab="info">취소</button>
              <button type="submit" class="btn btn-sm btn-icas-primary save-edit-btn" data-tab="info">
                <i class="bi bi-check-lg me-1"></i>저장
              </button>
            </div>
          </form>
        </div>

        <!-- ── 탭 2: 항공기 ── -->
        <div class="tab-pane fade p-4" id="tabAcft" role="tabpanel" aria-labelledby="tab-acft">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">항공기 유형·연료·대수</h6>
            <button type="button" class="btn btn-sm btn-outline-primary add-child-btn" data-tab="acft" style="display:none;">
              <i class="bi bi-plus-lg me-1"></i>항공기 추가
            </button>
          </div>
          <div class="table-responsive">
            <table class="table table-sm table-icas-inner" id="acftGrid" aria-label="항공기 목록">
              <thead>
                <tr>
                  <th>항공기 유형코드</th>
                  <th>연료 유형</th>
                  <th>대수</th>
                  <th>등록일</th>
                  <th class="edit-col" style="display:none;">액션</th>
                </tr>
              </thead>
              <tbody id="acftGridBody">
                <tr><td colspan="5" class="text-center py-3 text-muted small">탭을 선택하면 로드됩니다.</td></tr>
              </tbody>
            </table>
          </div>
          <!-- 항공기 추가 폼 (인라인) -->
          <div id="acftAddRow" class="card p-3 mt-2" style="display:none; background:#f8f9fa;">
            <div class="row g-2 align-items-end">
              <div class="col-md-3">
                <label for="acftTypeCd" class="form-label">항공기 유형코드 <span class="req" aria-hidden="true">*</span></label>
                <input type="text" id="acftTypeCd" name="acftTypeCd" class="form-control form-control-sm text-uppercase" maxlength="10" placeholder="예: A320">
                <div class="invalid-feedback" id="acftTypeCd-error" role="alert"></div>
              </div>
              <div class="col-md-3">
                <label for="acftFuelTypeCd" class="form-label">연료 유형 <span class="req" aria-hidden="true">*</span></label>
                <select id="acftFuelTypeCd" name="fuelTypeCd" class="form-select form-select-sm">
                  <option value="">선택</option>
                  <option value="JET_A">Jet-A</option>
                  <option value="JET_A1">Jet-A1</option>
                  <option value="TS_1">TS-1</option>
                  <option value="SAF">SAF</option>
                </select>
                <div class="invalid-feedback" id="acftFuelTypeCd-error" role="alert"></div>
              </div>
              <div class="col-md-2">
                <label for="acftCnt" class="form-label">대수 <span class="req" aria-hidden="true">*</span></label>
                <input type="number" id="acftCnt" name="acftCnt" class="form-control form-control-sm" min="1" max="9999">
                <div class="invalid-feedback" id="acftCnt-error" role="alert"></div>
              </div>
              <div class="col-auto">
                <button type="button" id="btnAcftAddSave" class="btn btn-sm btn-icas-primary">
                  <i class="bi bi-check-lg me-1"></i>저장
                </button>
                <button type="button" id="btnAcftAddCancel" class="btn btn-sm btn-outline-secondary ms-1">취소</button>
              </div>
            </div>
          </div>
        </div>

        <!-- ── 탭 3: 국가쌍 ── -->
        <div class="tab-pane fade p-4" id="tabCntry" role="tabpanel" aria-labelledby="tab-cntry">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">운항 국가 쌍</h6>
            <button type="button" class="btn btn-sm btn-outline-primary add-child-btn" data-tab="cntry" style="display:none;">
              <i class="bi bi-plus-lg me-1"></i>국가쌍 추가
            </button>
          </div>
          <div class="table-responsive">
            <table class="table table-sm table-icas-inner" id="cntryGrid" aria-label="국가쌍 목록">
              <thead>
                <tr>
                  <th>출발국가</th>
                  <th>도착국가</th>
                  <th>국제선 여부</th>
                  <th>면제코드</th>
                  <th class="edit-col" style="display:none;">액션</th>
                </tr>
              </thead>
              <tbody id="cntryGridBody">
                <tr><td colspan="5" class="text-center py-3 text-muted small">탭을 선택하면 로드됩니다.</td></tr>
              </tbody>
            </table>
          </div>
          <!-- 국가쌍 추가 폼 -->
          <div id="cntryAddRow" class="card p-3 mt-2" style="display:none; background:#f8f9fa;">
            <div class="row g-2 align-items-end">
              <div class="col-md-2">
                <label for="cntryDprtrCd" class="form-label">출발국가 <span class="req" aria-hidden="true">*</span></label>
                <input type="text" id="cntryDprtrCd" name="dprtrCntryCd" class="form-control form-control-sm text-uppercase" maxlength="2" placeholder="예: KR">
                <div class="invalid-feedback" id="cntryDprtrCd-error" role="alert"></div>
              </div>
              <div class="col-md-2">
                <label for="cntryArvlCd" class="form-label">도착국가 <span class="req" aria-hidden="true">*</span></label>
                <input type="text" id="cntryArvlCd" name="arvlCntryCd" class="form-control form-control-sm text-uppercase" maxlength="2" placeholder="예: JP">
                <div class="invalid-feedback" id="cntryArvlCd-error" role="alert"></div>
              </div>
              <div class="col-md-2">
                <label for="cntryIntlYn" class="form-label">국제선 여부</label>
                <select id="cntryIntlYn" name="intlYn" class="form-select form-select-sm">
                  <option value="Y">국제선 (Y)</option>
                  <option value="N">국내선 (N)</option>
                </select>
              </div>
              <div class="col-md-2">
                <label for="cntryExemptCd" class="form-label">면제코드</label>
                <input type="text" id="cntryExemptCd" name="exemptCd" class="form-control form-control-sm" maxlength="20">
              </div>
              <div class="col-auto">
                <button type="button" id="btnCntryAddSave" class="btn btn-sm btn-icas-primary">
                  <i class="bi bi-check-lg me-1"></i>저장
                </button>
                <button type="button" id="btnCntryAddCancel" class="btn btn-sm btn-outline-secondary ms-1">취소</button>
              </div>
            </div>
          </div>
        </div>

        <!-- ── 탭 4: 운영자연결 (담당자) ── -->
        <div class="tab-pane fade p-4" id="tabCnct" role="tabpanel" aria-labelledby="tab-cnct">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">담당자 연락처</h6>
            <button type="button" class="btn btn-sm btn-outline-primary add-child-btn" data-tab="cnct" style="display:none;">
              <i class="bi bi-plus-lg me-1"></i>담당자 추가
            </button>
          </div>
          <div class="table-responsive">
            <table class="table table-sm table-icas-inner" id="cnctGrid" aria-label="담당자 목록">
              <thead>
                <tr>
                  <th>구분</th>
                  <th>성명</th>
                  <th>휴대폰</th>
                  <th>이메일</th>
                  <th class="edit-col" style="display:none;">액션</th>
                </tr>
              </thead>
              <tbody id="cnctGridBody">
                <tr><td colspan="5" class="text-center py-3 text-muted small">탭을 선택하면 로드됩니다.</td></tr>
              </tbody>
            </table>
          </div>
          <!-- 담당자 추가 폼 -->
          <div id="cnctAddRow" class="card p-3 mt-2" style="display:none; background:#f8f9fa;">
            <div class="row g-2 align-items-end">
              <div class="col-md-2">
                <label for="cnctSeCd" class="form-label">구분 <span class="req" aria-hidden="true">*</span></label>
                <select id="cnctSeCd" name="cnctSeCd" class="form-select form-select-sm">
                  <option value="">선택</option>
                  <option value="MAIN">주담당자</option>
                  <option value="ALT">대체담당자</option>
                </select>
                <div class="invalid-feedback" id="cnctSeCd-error" role="alert"></div>
              </div>
              <div class="col-md-3">
                <label for="cnctUserNm" class="form-label">성명 <span class="req" aria-hidden="true">*</span></label>
                <input type="text" id="cnctUserNm" name="userNm" class="form-control form-control-sm" maxlength="100">
                <div class="invalid-feedback" id="cnctUserNm-error" role="alert"></div>
              </div>
              <div class="col-md-3">
                <label for="cnctMblphnNo" class="form-label">휴대폰</label>
                <input type="tel" id="cnctMblphnNo" name="mblphnNo" class="form-control form-control-sm" maxlength="20">
              </div>
              <div class="col-md-3">
                <label for="cnctEmlAddr" class="form-label">이메일</label>
                <input type="email" id="cnctEmlAddr" name="emlAddr" class="form-control form-control-sm" maxlength="200">
              </div>
              <div class="col-auto">
                <button type="button" id="btnCnctAddSave" class="btn btn-sm btn-icas-primary">
                  <i class="bi bi-check-lg me-1"></i>저장
                </button>
                <button type="button" id="btnCnctAddCancel" class="btn btn-sm btn-outline-secondary ms-1">취소</button>
              </div>
            </div>
          </div>
        </div>

        <!-- ── 탭 5: CO2 계산방법 ── -->
        <div class="tab-pane fade p-4" id="tabCo2" role="tabpanel" aria-labelledby="tab-co2">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">배출량 계산방법</h6>
            <button type="button" class="btn btn-sm btn-outline-primary edit-btn" data-tab="co2" style="display:none;">
              <i class="bi bi-pencil me-1"></i>편집
            </button>
          </div>
          <div id="co2ViewArea">
            <div class="text-center py-4 text-muted small">탭을 클릭하면 데이터를 불러옵니다.</div>
          </div>
          <form id="co2EditForm" style="display:none;" data-url-tpl="/api/emp/plan/{id}/co2-calc">
            <div class="row g-3">
              <div class="col-md-4">
                <label for="co2MntrMthdCd" class="form-label">모니터링 방법 <span class="req" aria-hidden="true">*</span></label>
                <select id="co2MntrMthdCd" name="mntrMthdCd" class="form-select" aria-required="true">
                  <option value="">선택하세요</option>
                  <option value="MTHD_A">Method A (항공편별 연료)</option>
                  <option value="MTHD_B">Method B (연료적재+잔량)</option>
                  <option value="BLOCK">Block-off/on</option>
                  <option value="REFUEL">재급유 이중산정 제거</option>
                  <option value="ALLOC">할당</option>
                </select>
                <div class="invalid-feedback" id="co2MntrMthdCd-error" role="alert"></div>
              </div>
              <div class="col-md-2">
                <label for="co2CertUseYn" class="form-label">CERT 사용 여부</label>
                <select id="co2CertUseYn" name="certUseYn" class="form-select">
                  <option value="N">N</option>
                  <option value="Y">Y</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="co2FuelDnstySecCd" class="form-label">연료 밀도 구분</label>
                <select id="co2FuelDnstySecCd" name="fuelDnstySecCd" class="form-select">
                  <option value="">선택하세요</option>
                  <option value="STANDARD">표준값</option>
                  <option value="MEASURED">실측값</option>
                  <option value="LAB">실험실 분석</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="co2EstCo2Emsn" class="form-label">추정 CO2 배출량 (t)</label>
                <input type="number" id="co2EstCo2Emsn" name="estCo2Emsn" class="form-control" step="0.0001" min="0">
              </div>
            </div>
            <div class="mt-3 d-flex gap-2 justify-content-end">
              <button type="button" class="btn btn-sm btn-outline-secondary cancel-edit-btn" data-tab="co2">취소</button>
              <button type="submit" class="btn btn-sm btn-icas-primary save-edit-btn" data-tab="co2">
                <i class="bi bi-check-lg me-1"></i>저장
              </button>
            </div>
          </form>
        </div>

        <!-- ── 탭 6: CO2 상세 ── -->
        <div class="tab-pane fade p-4" id="tabCo2Detail" role="tabpanel" aria-labelledby="tab-co2detail">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">CO2 측정 상세</h6>
            <button type="button" class="btn btn-sm btn-outline-primary add-child-btn" data-tab="co2detail" style="display:none;">
              <i class="bi bi-plus-lg me-1"></i>방법 추가
            </button>
          </div>
          <div class="table-responsive">
            <table class="table table-sm table-icas-inner" id="co2DetailGrid" aria-label="CO2 측정 상세 목록">
              <thead>
                <tr>
                  <th>모니터링 방법</th>
                  <th>측정시점</th>
                  <th>장비정보</th>
                  <th>절차설명</th>
                  <th class="edit-col" style="display:none;">액션</th>
                </tr>
              </thead>
              <tbody id="co2DetailGridBody">
                <tr><td colspan="5" class="text-center py-3 text-muted small">탭을 선택하면 로드됩니다.</td></tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ── 탭 7: 데이터관리 ── -->
        <div class="tab-pane fade p-4" id="tabCtrl" role="tabpanel" aria-labelledby="tab-ctrl">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">데이터 품질 통제</h6>
            <button type="button" class="btn btn-sm btn-outline-primary edit-btn" data-tab="ctrl" style="display:none;">
              <i class="bi bi-pencil me-1"></i>편집
            </button>
          </div>
          <div id="ctrlViewArea">
            <div class="text-center py-4 text-muted small">탭을 클릭하면 데이터를 불러옵니다.</div>
          </div>
          <form id="ctrlEditForm" style="display:none;" data-url-tpl="/api/emp/plan/{id}/data-ctrl">
            <div class="row g-3">
              <div class="col-12">
                <label for="ctrlFlowDesc" class="form-label">데이터 흐름 설명 <span class="req" aria-hidden="true">*</span></label>
                <textarea id="ctrlFlowDesc" name="flowDesc" class="form-control" rows="3" maxlength="2000" aria-required="true"></textarea>
                <small class="text-muted"><span id="ctrlFlowDescLen">0</span>/2000</small>
                <div class="invalid-feedback" id="ctrlFlowDesc-error" role="alert"></div>
              </div>
              <div class="col-md-6">
                <label for="ctrlGapThrshld" class="form-label">5% 갭 임계값 절차</label>
                <textarea id="ctrlGapThrshld" name="gapThrshld5pct" class="form-control" rows="2" maxlength="2000"></textarea>
              </div>
              <div class="col-md-6">
                <label for="ctrlSndSrcDesc" class="form-label">대체 데이터 출처 설명</label>
                <textarea id="ctrlSndSrcDesc" name="sndSrcUseDesc" class="form-control" rows="2" maxlength="2000"></textarea>
              </div>
              <div class="col-md-6">
                <label for="ctrlRiskAnlys" class="form-label">위험 분석</label>
                <textarea id="ctrlRiskAnlys" name="riskAnlys" class="form-control" rows="2" maxlength="2000"></textarea>
              </div>
              <div class="col-md-6">
                <label for="ctrlSigChgAprvProc" class="form-label">중대 변경 승인 절차</label>
                <textarea id="ctrlSigChgAprvProc" name="sigChgAprvProc" class="form-control" rows="2" maxlength="2000"></textarea>
              </div>
            </div>
            <div class="mt-3 d-flex gap-2 justify-content-end">
              <button type="button" class="btn btn-sm btn-outline-secondary cancel-edit-btn" data-tab="ctrl">취소</button>
              <button type="submit" class="btn btn-sm btn-icas-primary save-edit-btn" data-tab="ctrl">
                <i class="bi bi-check-lg me-1"></i>저장
              </button>
            </div>
          </form>
        </div>

        <!-- ── 탭 8: 리스크 ── -->
        <div class="tab-pane fade p-4" id="tabRisk" role="tabpanel" aria-labelledby="tab-risk">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0" style="color:var(--icas-primary);">위험·통제 항목</h6>
            <button type="button" class="btn btn-sm btn-outline-primary add-child-btn" data-tab="risk" style="display:none;">
              <i class="bi bi-plus-lg me-1"></i>위험 항목 추가
            </button>
          </div>
          <div class="table-responsive">
            <table class="table table-sm table-icas-inner" id="riskGrid" aria-label="위험·통제 목록">
              <thead>
                <tr>
                  <th>No</th>
                  <th style="width:40%;">위험 설명</th>
                  <th style="width:40%;">통제 활동</th>
                  <th>등록일</th>
                  <th class="edit-col" style="display:none;">액션</th>
                </tr>
              </thead>
              <tbody id="riskGridBody">
                <tr><td colspan="5" class="text-center py-3 text-muted small">탭을 선택하면 로드됩니다.</td></tr>
              </tbody>
            </table>
          </div>
          <!-- 위험 항목 추가 폼 -->
          <div id="riskAddRow" class="card p-3 mt-2" style="display:none; background:#f8f9fa;">
            <div class="row g-2">
              <div class="col-md-5">
                <label for="riskDesc" class="form-label">위험 설명 <span class="req" aria-hidden="true">*</span></label>
                <textarea id="riskDesc" name="riskDesc" class="form-control form-control-sm" rows="2" maxlength="2000"></textarea>
                <div class="invalid-feedback" id="riskDesc-error" role="alert"></div>
              </div>
              <div class="col-md-5">
                <label for="riskCtrlActv" class="form-label">통제 활동</label>
                <textarea id="riskCtrlActv" name="ctrlActv" class="form-control form-control-sm" rows="2" maxlength="2000"></textarea>
              </div>
              <div class="col-md-2 d-flex align-items-end gap-1">
                <button type="button" id="btnRiskAddSave" class="btn btn-sm btn-icas-primary">
                  <i class="bi bi-check-lg me-1"></i>저장
                </button>
                <button type="button" id="btnRiskAddCancel" class="btn btn-sm btn-outline-secondary">취소</button>
              </div>
            </div>
          </div>
        </div>

      </div><!-- /tab-content -->
    </div><!-- /탭 카드 -->

  </div><!-- /container-fluid -->
</div><!-- /margin-left -->

<!-- ============================================================
     자식 탭 수정 모달: 항공기
     ============================================================ -->
<div class="modal fade" id="modalAcftEdit" tabindex="-1" aria-labelledby="modalAcftEditLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h6 class="modal-title" id="modalAcftEditLabel">항공기 수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="acftEditSn">
        <div class="mb-3">
          <label for="acftEditTypeCd" class="form-label">항공기 유형코드 <span class="text-danger" aria-hidden="true">*</span></label>
          <input type="text" id="acftEditTypeCd" class="form-control text-uppercase" maxlength="10" placeholder="예: A320">
          <div class="invalid-feedback" id="acftEditTypeCd-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="acftEditFuelTypeCd" class="form-label">연료 유형 <span class="text-danger" aria-hidden="true">*</span></label>
          <select id="acftEditFuelTypeCd" class="form-select">
            <option value="">선택</option>
            <option value="JET_A">Jet-A</option>
            <option value="JET_A1">Jet-A1</option>
            <option value="TS_1">TS-1</option>
            <option value="SAF">SAF</option>
          </select>
          <div class="invalid-feedback" id="acftEditFuelTypeCd-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="acftEditCnt" class="form-label">대수 <span class="text-danger" aria-hidden="true">*</span></label>
          <input type="number" id="acftEditCnt" class="form-control" min="1" max="9999">
          <div class="invalid-feedback" id="acftEditCnt-error" role="alert"></div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnAcftEditSave" class="btn btn-sm btn-icas-primary">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     자식 탭 수정 모달: 국가쌍
     ============================================================ -->
<div class="modal fade" id="modalCntryEdit" tabindex="-1" aria-labelledby="modalCntryEditLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h6 class="modal-title" id="modalCntryEditLabel">국가쌍 수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="cntryEditSn">
        <div class="row g-3">
          <div class="col-md-6">
            <label for="cntryEditDprtr" class="form-label">출발국가 <span class="text-danger" aria-hidden="true">*</span></label>
            <input type="text" id="cntryEditDprtr" class="form-control text-uppercase" maxlength="2" placeholder="예: KR">
            <div class="invalid-feedback" id="cntryEditDprtr-error" role="alert"></div>
          </div>
          <div class="col-md-6">
            <label for="cntryEditArvl" class="form-label">도착국가 <span class="text-danger" aria-hidden="true">*</span></label>
            <input type="text" id="cntryEditArvl" class="form-control text-uppercase" maxlength="2" placeholder="예: JP">
            <div class="invalid-feedback" id="cntryEditArvl-error" role="alert"></div>
          </div>
          <div class="col-md-6">
            <label for="cntryEditIntlYn" class="form-label">국제선 여부</label>
            <select id="cntryEditIntlYn" class="form-select">
              <option value="Y">국제선 (Y)</option>
              <option value="N">국내선 (N)</option>
            </select>
          </div>
          <div class="col-md-6">
            <label for="cntryEditExemptCd" class="form-label">면제코드</label>
            <input type="text" id="cntryEditExemptCd" class="form-control" maxlength="20">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnCntryEditSave" class="btn btn-sm btn-icas-primary">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     자식 탭 수정 모달: 담당자(연결)
     ============================================================ -->
<div class="modal fade" id="modalCnctEdit" tabindex="-1" aria-labelledby="modalCnctEditLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h6 class="modal-title" id="modalCnctEditLabel">담당자 수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="cnctEditSn">
        <div class="mb-3">
          <label for="cnctEditSeCd" class="form-label">구분 <span class="text-danger" aria-hidden="true">*</span></label>
          <select id="cnctEditSeCd" class="form-select">
            <option value="">선택</option>
            <option value="MAIN">주담당자</option>
            <option value="ALT">대체담당자</option>
          </select>
          <div class="invalid-feedback" id="cnctEditSeCd-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="cnctEditUserNm" class="form-label">성명 <span class="text-danger" aria-hidden="true">*</span></label>
          <input type="text" id="cnctEditUserNm" class="form-control" maxlength="100">
          <div class="invalid-feedback" id="cnctEditUserNm-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="cnctEditMblphnNo" class="form-label">휴대폰</label>
          <input type="tel" id="cnctEditMblphnNo" class="form-control" maxlength="20">
        </div>
        <div class="mb-3">
          <label for="cnctEditEmlAddr" class="form-label">이메일</label>
          <input type="email" id="cnctEditEmlAddr" class="form-control" maxlength="200">
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnCnctEditSave" class="btn btn-sm btn-icas-primary">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     자식 탭 추가/수정 모달: CO2 상세
     ============================================================ -->
<div class="modal fade" id="modalCo2DetailEdit" tabindex="-1" aria-labelledby="modalCo2DetailEditLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h6 class="modal-title" id="modalCo2DetailEditLabel">CO2 측정 상세 등록/수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="co2dEditMthdCd">
        <div class="row g-3">
          <div class="col-md-6">
            <label for="co2dEditMntrMthdCd" class="form-label">모니터링 방법 <span class="text-danger" aria-hidden="true">*</span></label>
            <select id="co2dEditMntrMthdCd" class="form-select">
              <option value="">선택하세요</option>
              <option value="MTHD_A">Method A (항공편별 연료)</option>
              <option value="MTHD_B">Method B (연료적재+잔량)</option>
              <option value="BLOCK">Block-off/on</option>
              <option value="REFUEL">재급유 이중산정 제거</option>
              <option value="ALLOC">할당</option>
            </select>
            <div class="invalid-feedback" id="co2dEditMntrMthdCd-error" role="alert"></div>
          </div>
          <div class="col-md-6">
            <label for="co2dEditMntrPnt" class="form-label">측정시점</label>
            <input type="text" id="co2dEditMntrPnt" class="form-control" maxlength="200" placeholder="예: 급유 직후">
          </div>
          <div class="col-md-6">
            <label for="co2dEditEquipInfo" class="form-label">장비정보</label>
            <input type="text" id="co2dEditEquipInfo" class="form-control" maxlength="300" placeholder="측정 장비 정보">
          </div>
          <div class="col-12">
            <label for="co2dEditProcDesc" class="form-label">절차설명</label>
            <textarea id="co2dEditProcDesc" class="form-control" rows="3" maxlength="2000" placeholder="측정 절차 상세 설명"></textarea>
          </div>
        </div>
        <div class="alert alert-danger py-2 small mt-2 d-none" id="co2dEditError" role="alert"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnCo2dEditSave" class="btn btn-sm btn-icas-primary">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ============================================================
     자식 탭 수정 모달: 리스크
     ============================================================ -->
<div class="modal fade" id="modalRiskEdit" tabindex="-1" aria-labelledby="modalRiskEditLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h6 class="modal-title" id="modalRiskEditLabel">위험·통제 항목 수정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="riskEditSn">
        <div class="mb-3">
          <label for="riskEditDesc" class="form-label">위험 설명 <span class="text-danger" aria-hidden="true">*</span></label>
          <textarea id="riskEditDesc" class="form-control" rows="3" maxlength="2000"></textarea>
          <div class="invalid-feedback" id="riskEditDesc-error" role="alert"></div>
        </div>
        <div class="mb-3">
          <label for="riskEditCtrlActv" class="form-label">통제 활동</label>
          <textarea id="riskEditCtrlActv" class="form-control" rows="3" maxlength="2000"></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnRiskEditSave" class="btn btn-sm btn-icas-primary">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- 반려/취소 사유 모달 -->
<div class="modal fade" id="modalReason" tabindex="-1" aria-labelledby="modalReasonLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:var(--icas-primary);color:white;">
        <h5 class="modal-title" id="modalReasonLabel">처리 사유 입력</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <label for="reasonInput" class="form-label fw-semibold">사유 <span class="text-danger" aria-hidden="true">*</span></label>
        <textarea id="reasonInput" class="form-control" rows="4" maxlength="500" placeholder="처리 사유를 입력하세요."></textarea>
        <div class="invalid-feedback d-block" id="reasonInput-error" role="alert"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnReasonConfirm" class="btn btn-icas-primary">확인</button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>/* ── 세션 권한 주입 (서버사이드 EL) ── */
var __OGNZ_SE_CD = '${sessionScope.ognzSeCd}';</script>
<script>
(function () {
  'use strict';

  // ─────────────────────────────────────────────
  // 상태·권한 상수
  // ─────────────────────────────────────────────
  var EMP_PLAN_ID = (function () {
    var m = location.pathname.match(/\/emp\/plan\/([^/]+)/);
    return m ? m[1] : null;
  })();

  var STATUS_MAP = {
    'DRAFT':  { cls: 'bg-secondary',           label: '작성중' },
    'SBMTD':  { cls: 'bg-primary',             label: '제출됨' },
    'RVWNG':  { cls: 'bg-warning text-dark',   label: '검토중' },
    'RCMDD':  { cls: 'bg-info text-dark',      label: '권고'   },
    'APRVD':  { cls: 'bg-success',             label: '승인'   },
    'RJCTD':  { cls: 'bg-danger',              label: '반려'   },
    'CNCLD':  { cls: 'bg-dark',                label: '취소'   }
  };

  /* 편집 가능 상태 */
  var EDITABLE_STATES = ['DRAFT', 'RJCTD'];

  /* 라이프사이클 액션 정의
   * allowedRoles: 해당 액션을 수행 가능한 ognzSeCd 목록
   */
  var LC_ACTIONS = [
    { action: 'submit',      label: '제출',       cls: 'btn-primary',          needReason: false, confirm: 'EMP 를 제출하시겠습니까? 제출 후에는 수정이 제한됩니다.', visibleSt: ['DRAFT'],          allowedRoles: ['AIRLINE'] },
    { action: 'review',      label: '검토 시작',  cls: 'btn-warning text-dark', needReason: false, confirm: '검토를 시작하시겠습니까?',                               visibleSt: ['SBMTD'],          allowedRoles: ['KOTSA'] },
    { action: 'recommend',   label: '권고',       cls: 'btn-info text-dark',   needReason: false, confirm: '권고 처리하시겠습니까?',                                  visibleSt: ['RVWNG'],          allowedRoles: ['KOTSA'] },
    { action: 'reject',      label: '반려',       cls: 'btn-outline-danger',   needReason: true,  confirm: null,                                                       visibleSt: ['RVWNG', 'RCMDD'], allowedRoles: ['KOTSA', 'MOLIT'] },
    { action: 'approve',     label: '승인',       cls: 'btn-success',          needReason: false, confirm: 'EMP 를 최종 승인하시겠습니까?',                           visibleSt: ['RVWNG', 'RCMDD'], allowedRoles: ['MOLIT'] },
    { action: 'cancel',      label: '취소',       cls: 'btn-outline-dark',     needReason: true,  confirm: null,                                                       visibleSt: ['APRVD'],          allowedRoles: ['MOLIT'] },
    { action: 'new-version', label: '신버전 생성', cls: 'btn-outline-primary',  needReason: false, confirm: '현재 EMP 를 기반으로 신버전 DRAFT 를 생성하시겠습니까?', visibleSt: ['APRVD', 'CNCLD'],  allowedRoles: ['AIRLINE'] }
  ];

  // 현재 plan 데이터 캐시
  var planData = null;

  // 탭 로드 완료 여부 추적
  var tabLoaded = { info: false, acft: false, cntry: false, cnct: false, co2: false, co2detail: false, ctrl: false, risk: false };

  // ─────────────────────────────────────────────
  // 유틸
  // ─────────────────────────────────────────────
  function escHtml(str) {
    if (str == null) return '';
    return String(str)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;')
      .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  function dash(val) { return (val == null || val === '') ? '-' : escHtml(val); }

  function renderStatusBadge(cd) {
    if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
    var m = STATUS_MAP[cd];
    if (!m) return '<span class="badge status-badge bg-secondary">' + escHtml(cd) + '</span>';
    return '<span class="badge status-badge ' + m.cls + '">' + m.label + '</span>';
  }

  function formatDateTime(val) {
    if (!val) return '-';
    return String(val).replace('T', ' ').substring(0, 16);
  }

  function isEditable(stCd) {
    return EDITABLE_STATES.indexOf(stCd) >= 0;
  }

  function apiUrl(tpl) {
    return tpl.replace('{id}', EMP_PLAN_ID);
  }

  function showTabLoading(show) {
    $('#tabLoadingOverlay').css('display', show ? 'flex' : 'none');
  }

  // ─────────────────────────────────────────────
  // 기본정보 로드
  // ─────────────────────────────────────────────
  function loadPlan() {
    if (!EMP_PLAN_ID) {
      $('#basicInfoView').html('<div class="alert alert-danger">EMP Plan ID 를 찾을 수 없습니다.</div>');
      return;
    }
    $.get('/api/emp/plan/' + EMP_PLAN_ID)
      .done(function (res) {
        planData = res.data || res;
        renderBasicInfo(planData);
        renderLifecycleActions(planData.empStCd);
        renderBackLink(planData);
        // 첫 번째 탭(info) 데이터 로드
        loadTabData('info');
        tabLoaded['info'] = true;
      })
      .fail(function (xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) || '데이터 로드에 실패했습니다.';
        $('#basicInfoView').html('<div class="alert alert-danger">' + escHtml(msg) + '</div>');
      });
  }

  function renderBasicInfo(plan) {
    // 헤더 배지 / 브레드크럼
    $('#headerStatusBadge').html(renderStatusBadge(plan.empStCd));
    $('#breadcrumbId').text('EMP-' + escHtml(plan.empPlanId));

    // 기본정보 뷰
    var html = '<div class="info-row">'
      + '<div class="info-item"><div class="label">EMP Plan ID</div><div class="value">' + dash(plan.empPlanId) + '</div></div>'
      + '<div class="info-item"><div class="label">운영사</div><div class="value">' + dash(plan.oprtrNm) + '</div></div>'
      + '<div class="info-item"><div class="label">ICAO</div><div class="value">' + dash(plan.icaoDesig) + '</div></div>'
      + '<div class="info-item"><div class="label">보고연도</div><div class="value">' + dash(plan.rprtYr) + '</div></div>'
      + '<div class="info-item"><div class="label">버전</div><div class="value">v' + dash(plan.empVer) + '</div></div>'
      + '<div class="info-item"><div class="label">상태</div><div class="value">' + renderStatusBadge(plan.empStCd) + '</div></div>'
      + '<div class="info-item"><div class="label">제출일시</div><div class="value">' + formatDateTime(plan.sbmtDt) + '</div></div>'
      + '<div class="info-item"><div class="label">승인일시</div><div class="value">' + formatDateTime(plan.aprvDt) + '</div></div>'
      + '</div>';
    $('#basicInfoView').html(html);

    // 편집 가능 상태이면 편집 버튼 표시
    if (isEditable(plan.empStCd)) {
      $('#basicInfoEditBar').css('display', 'flex');
    }

    // 탭별 편집/추가 버튼 표시
    if (isEditable(plan.empStCd)) {
      $('.edit-btn, .add-child-btn').show();
      $('.edit-col').show();
    }
  }

  function renderBackLink(plan) {
    var rprtYr = plan && plan.rprtYr ? plan.rprtYr : '';
    $('#backToList').attr('href', '/emp/plan' + (rprtYr ? '?rprtYr=' + rprtYr : ''));
  }

  // ─────────────────────────────────────────────
  // 라이프사이클 액션 버튼
  // ─────────────────────────────────────────────
  function renderLifecycleActions(stCd) {
    var $area = $('#lifecycleActions');
    $area.empty();
    if (!stCd) return;

    /* 미인증 세션이면 버튼 전체 숨김 — 화면(조회)은 유지 */
    var ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
    if (!ognzSeCd) return;

    var html = '<span class="lc-label small text-muted me-1">상태 전이:</span>';
    var hasBtn = false;
    LC_ACTIONS.forEach(function (def) {
      /* 상태 조건 + 역할 조건 동시 충족 시만 버튼 생성 */
      if (def.visibleSt.indexOf(stCd) >= 0 && def.allowedRoles.indexOf(ognzSeCd) >= 0) {
        html += '<button type="button" class="btn btn-sm ' + def.cls + ' lc-action-btn" data-action="' + def.action + '">'
          + def.label + '</button>';
        hasBtn = true;
      }
    });
    if (hasBtn) {
      $area.html(html);
      $area.css('display', 'flex');
    }
  }

  // ─────────────────────────────────────────────
  // 탭별 데이터 로드
  // ─────────────────────────────────────────────
  var TAB_CONFIG = {
    info:       { url: '/api/emp/plan/{id}/info',       type: 'single',   bodyId: 'infoViewArea',       gridId: null },
    acft:       { url: '/api/emp/plan/{id}/acft',       type: 'list',     bodyId: null,                 gridId: 'acftGridBody',     cols: 5 },
    cntry:      { url: '/api/emp/plan/{id}/cntry-pair', type: 'list',     bodyId: null,                 gridId: 'cntryGridBody',    cols: 5 },
    cnct:       { url: '/api/emp/plan/{id}/cnct',       type: 'list',     bodyId: null,                 gridId: 'cnctGridBody',     cols: 5 },
    co2:        { url: '/api/emp/plan/{id}/co2-calc',   type: 'single',   bodyId: 'co2ViewArea',        gridId: null },
    co2detail:  { url: '/api/emp/plan/{id}/co2-detail', type: 'list',     bodyId: null,                 gridId: 'co2DetailGridBody',cols: 5 },
    ctrl:       { url: '/api/emp/plan/{id}/data-ctrl',  type: 'single',   bodyId: 'ctrlViewArea',       gridId: null },
    risk:       { url: '/api/emp/plan/{id}/risk',       type: 'list',     bodyId: null,                 gridId: 'riskGridBody',     cols: 5 }
  };

  function loadTabData(tabKey) {
    var cfg = TAB_CONFIG[tabKey];
    if (!cfg) return;
    showTabLoading(true);
    $.get(apiUrl(cfg.url))
      .done(function (res) {
        var data = res.data;
        if (cfg.type === 'single') {
          renderSingleTab(tabKey, data);
        } else {
          renderListTab(tabKey, data || []);
        }
      })
      .fail(function (xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) || '데이터 로드에 실패했습니다.';
        if (cfg.gridId) {
          $('#' + cfg.gridId).html('<tr><td colspan="' + (cfg.cols || 5) + '" class="text-danger small text-center py-3">' + escHtml(msg) + '</td></tr>');
        } else if (cfg.bodyId) {
          $('#' + cfg.bodyId).html('<div class="alert alert-danger small">' + escHtml(msg) + '</div>');
        }
      })
      .always(function () { showTabLoading(false); });
  }

  function renderSingleTab(tabKey, data) {
    if (tabKey === 'info') renderInfoView(data);
    else if (tabKey === 'co2') renderCo2View(data);
    else if (tabKey === 'ctrl') renderCtrlView(data);
  }

  function renderListTab(tabKey, list) {
    if (tabKey === 'acft')      renderAcftGrid(list);
    else if (tabKey === 'cntry')    renderCntryGrid(list);
    else if (tabKey === 'cnct')     renderCnctGrid(list);
    else if (tabKey === 'co2detail') renderCo2DetailGrid(list);
    else if (tabKey === 'risk')     renderRiskGrid(list);
  }

  // ─── info 뷰 렌더 ───
  function renderInfoView(d) {
    if (!d) {
      $('#infoViewArea').html('<div class="text-muted small py-3">운영사 식별정보가 등록되지 않았습니다.</div>');
      return;
    }
    var html = '<div class="info-row">'
      + '<div class="info-item"><div class="label">운영사 명칭</div><div class="value">' + dash(d.oprtrNm) + '</div></div>'
      + '<div class="info-item"><div class="label">주소</div><div class="value">' + dash(d.addr) + '</div></div>'
      + '<div class="info-item"><div class="label">법정대리인</div><div class="value">' + dash(d.lglrprNm) + '</div></div>'
      + '<div class="info-item"><div class="label">ICAO 지정어</div><div class="value">' + dash(d.icaoDesig) + '</div></div>'
      + '<div class="info-item"><div class="label">등록기호</div><div class="value">' + dash(d.regisMark) + '</div></div>'
      + '<div class="info-item"><div class="label">AOC 번호</div><div class="value">' + dash(d.aocNo) + '</div></div>'
      + '<div class="info-item"><div class="label">AOC 발급일</div><div class="value">' + dash(d.aocIssueDt) + '</div></div>'
      + '<div class="info-item"><div class="label">AOC 만료일</div><div class="value">' + dash(d.aocXprDt) + '</div></div>'
      + '<div class="info-item"><div class="label">모회사</div><div class="value">' + dash(d.parentCo) + '</div></div>'
      + '</div>';
    $('#infoViewArea').html(html);
  }

  // ─── CO2 뷰 렌더 ───
  function renderCo2View(d) {
    if (!d) {
      $('#co2ViewArea').html('<div class="text-muted small py-3">CO2 계산방법이 등록되지 않았습니다.</div>');
      return;
    }
    var MTHD_LABEL = { MTHD_A: 'Method A', MTHD_B: 'Method B', BLOCK: 'Block', REFUEL: 'Refuel', ALLOC: '할당' };
    var html = '<div class="info-row">'
      + '<div class="info-item"><div class="label">모니터링 방법</div><div class="value">' + dash(MTHD_LABEL[d.mntrMthdCd] || d.mntrMthdCd) + '</div></div>'
      + '<div class="info-item"><div class="label">CERT 사용</div><div class="value">' + dash(d.certUseYn) + '</div></div>'
      + '<div class="info-item"><div class="label">연료 밀도 구분</div><div class="value">' + dash(d.fuelDnstySecCd) + '</div></div>'
      + '<div class="info-item"><div class="label">추정 CO2 (t)</div><div class="value">' + (d.estCo2Emsn != null ? Number(d.estCo2Emsn).toLocaleString() : '-') + '</div></div>'
      + '</div>';
    $('#co2ViewArea').html(html);
  }

  // ─── 데이터관리 뷰 렌더 ───
  function renderCtrlView(d) {
    if (!d) {
      $('#ctrlViewArea').html('<div class="text-muted small py-3">데이터 품질 통제 정보가 등록되지 않았습니다.</div>');
      return;
    }
    var html = '<div class="row g-3">'
      + '<div class="col-12"><div class="label small text-muted">데이터 흐름 설명</div><div class="border rounded p-2 bg-light small" style="white-space:pre-wrap;">' + dash(d.flowDesc) + '</div></div>'
      + '<div class="col-md-6"><div class="label small text-muted">5% 갭 임계값 절차</div><div class="border rounded p-2 bg-light small">' + dash(d.gapThrshld5pct) + '</div></div>'
      + '<div class="col-md-6"><div class="label small text-muted">대체 데이터 출처</div><div class="border rounded p-2 bg-light small">' + dash(d.sndSrcUseDesc) + '</div></div>'
      + '<div class="col-md-6"><div class="label small text-muted">위험 분석</div><div class="border rounded p-2 bg-light small">' + dash(d.riskAnlys) + '</div></div>'
      + '<div class="col-md-6"><div class="label small text-muted">중대 변경 승인 절차</div><div class="border rounded p-2 bg-light small">' + dash(d.sigChgAprvProc) + '</div></div>'
      + '</div>';
    $('#ctrlViewArea').html(html);
  }

  // ─── 항공기 그리드 렌더 ───
  function renderAcftGrid(list) {
    var editable = planData && isEditable(planData.empStCd);
    if (!list || !list.length) {
      $('#acftGridBody').html('<tr><td colspan="' + (editable ? 5 : 4) + '" class="text-center py-3 text-muted small">등록된 항공기가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    list.forEach(function (row) {
      html += '<tr>'
        + '<td>' + escHtml(row.acftTypeCd) + '</td>'
        + '<td>' + escHtml(row.fuelTypeCd) + '</td>'
        + '<td>' + escHtml(row.acftCnt) + '</td>'
        + '<td class="small text-muted">' + formatDateTime(row.frstRegDt) + '</td>';
      if (editable) {
        html += '<td class="edit-col">'
          + '<button type="button" class="btn btn-xs btn-outline-primary me-1" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="openAcftEditModal(' + escHtml(row.sn) + ',\'' + escHtml(row.acftTypeCd) + '\',\'' + escHtml(row.fuelTypeCd) + '\',' + escHtml(row.acftCnt) + ')">수정</button>'
          + '<button type="button" class="btn btn-xs btn-outline-danger" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="deleteChildRow(\'acft\',' + escHtml(row.sn) + ')">삭제</button></td>';
      }
      html += '</tr>';
    });
    $('#acftGridBody').html(html);
  }

  // ─── 국가쌍 그리드 렌더 ───
  function renderCntryGrid(list) {
    var editable = planData && isEditable(planData.empStCd);
    if (!list || !list.length) {
      $('#cntryGridBody').html('<tr><td colspan="' + (editable ? 5 : 4) + '" class="text-center py-3 text-muted small">등록된 국가쌍이 없습니다.</td></tr>');
      return;
    }
    var html = '';
    list.forEach(function (row) {
      html += '<tr>'
        + '<td>' + escHtml(row.dprtrCntryCd) + '</td>'
        + '<td>' + escHtml(row.arvlCntryCd) + '</td>'
        + '<td class="text-center">' + (row.intlYn === 'Y' ? '<span class="badge bg-success">국제선</span>' : '<span class="badge bg-secondary">국내선</span>') + '</td>'
        + '<td>' + dash(row.exemptCd) + '</td>';
      if (editable) {
        html += '<td class="edit-col">'
          + '<button type="button" class="btn btn-xs btn-outline-primary me-1" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="openCntryEditModal(' + escHtml(row.sn) + ',\'' + escHtml(row.dprtrCntryCd) + '\',\'' + escHtml(row.arvlCntryCd) + '\',\'' + escHtml(row.intlYn) + '\',\'' + escHtml(row.exemptCd || '') + '\')">수정</button>'
          + '<button type="button" class="btn btn-xs btn-outline-danger" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="deleteChildRow(\'cntry-pair\',' + escHtml(row.sn) + ')">삭제</button></td>';
      }
      html += '</tr>';
    });
    $('#cntryGridBody').html(html);
  }

  // ─── 담당자 그리드 렌더 ───
  function renderCnctGrid(list) {
    var editable = planData && isEditable(planData.empStCd);
    if (!list || !list.length) {
      $('#cnctGridBody').html('<tr><td colspan="' + (editable ? 5 : 4) + '" class="text-center py-3 text-muted small">등록된 담당자가 없습니다.</td></tr>');
      return;
    }
    var CNCT_LABEL = { MAIN: '주담당자', ALT: '대체담당자' };
    var html = '';
    list.forEach(function (row) {
      html += '<tr>'
        + '<td>' + escHtml(CNCT_LABEL[row.cnctSeCd] || row.cnctSeCd) + '</td>'
        + '<td>' + escHtml(row.userNm) + '</td>'
        + '<td>' + dash(row.mblphnNo) + '</td>'
        + '<td>' + dash(row.emlAddr) + '</td>';
      if (editable) {
        html += '<td class="edit-col">'
          + '<button type="button" class="btn btn-xs btn-outline-primary me-1" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="openCnctEditModal(' + escHtml(row.sn) + ',\'' + escHtml(row.cnctSeCd) + '\',\'' + escHtml(row.userNm) + '\',\'' + escHtml(row.mblphnNo || '') + '\',\'' + escHtml(row.emlAddr || '') + '\')">수정</button>'
          + '<button type="button" class="btn btn-xs btn-outline-danger" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="deleteChildRow(\'cnct\',' + escHtml(row.sn) + ')">삭제</button></td>';
      }
      html += '</tr>';
    });
    $('#cnctGridBody').html(html);
  }

  // ─── CO2 상세 그리드 렌더 ───
  function renderCo2DetailGrid(list) {
    var editable = planData && isEditable(planData.empStCd);
    if (!list || !list.length) {
      $('#co2DetailGridBody').html('<tr><td colspan="' + (editable ? 5 : 4) + '" class="text-center py-3 text-muted small">등록된 CO2 측정 상세가 없습니다.</td></tr>');
      return;
    }
    var html = '';
    list.forEach(function (row) {
      html += '<tr>'
        + '<td>' + escHtml(row.mntrMthdCd) + '</td>'
        + '<td>' + dash(row.mntrPnt) + '</td>'
        + '<td>' + dash(row.equipInfo) + '</td>'
        + '<td>' + dash(row.procDesc) + '</td>';
      if (editable) {
        var safeMthd = escHtml(row.mntrMthdCd).replace(/'/g, "\\'");
        var safeMntrPnt = escHtml(row.mntrPnt || '').replace(/'/g, "\\'");
        var safeEquip = escHtml(row.equipInfo || '').replace(/'/g, "\\'");
        html += '<td class="edit-col">'
          + '<button type="button" class="btn btn-xs btn-outline-primary me-1" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="openCo2dEditModal(\'' + safeMthd + '\',\'' + safeMntrPnt + '\',\'' + safeEquip + '\')">수정</button>'
          + '<button type="button" class="btn btn-xs btn-outline-danger" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="deleteChildRow(\'co2-detail\',\'' + escHtml(row.mntrMthdCd) + '\')">삭제</button></td>';
      }
      html += '</tr>';
    });
    $('#co2DetailGridBody').html(html);
  }

  // ─── 리스크 그리드 렌더 ───
  function renderRiskGrid(list) {
    var editable = planData && isEditable(planData.empStCd);
    if (!list || !list.length) {
      $('#riskGridBody').html('<tr><td colspan="' + (editable ? 5 : 4) + '" class="text-center py-3 text-muted small">등록된 위험·통제 항목이 없습니다.</td></tr>');
      return;
    }
    var html = '';
    list.forEach(function (row, idx) {
      html += '<tr>'
        + '<td class="text-muted small">' + (idx + 1) + '</td>'
        + '<td style="white-space:pre-wrap;">' + escHtml(row.riskDesc) + '</td>'
        + '<td style="white-space:pre-wrap;">' + dash(row.ctrlActv) + '</td>'
        + '<td class="small text-muted">' + formatDateTime(row.frstRegDt) + '</td>';
      if (editable) {
        html += '<td class="edit-col">'
          + '<button type="button" class="btn btn-xs btn-outline-primary me-1" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="openRiskEditModal(' + escHtml(row.sn) + ')">수정</button>'
          + '<button type="button" class="btn btn-xs btn-outline-danger" style="font-size:0.72rem;padding:2px 6px;" '
          + 'onclick="deleteChildRow(\'risk\',' + escHtml(row.sn) + ')">삭제</button></td>';
      }
      html += '</tr>';
    });
    $('#riskGridBody').html(html);
  }

  // ─────────────────────────────────────────────
  // 수정 모달 오픈 함수 (전역 노출 — onclick 에서 사용)
  // ─────────────────────────────────────────────
  window.openAcftEditModal = function (sn, typeCd, fuelCd, cnt) {
    $('#acftEditSn').val(sn);
    $('#acftEditTypeCd').val(typeCd);
    $('#acftEditFuelTypeCd').val(fuelCd);
    $('#acftEditCnt').val(cnt);
    $('#acftEditTypeCd,#acftEditFuelTypeCd,#acftEditCnt').removeClass('is-invalid');
    new bootstrap.Modal(document.getElementById('modalAcftEdit')).show();
  };

  window.openCntryEditModal = function (sn, dprtr, arvl, intlYn, exemptCd) {
    $('#cntryEditSn').val(sn);
    $('#cntryEditDprtr').val(dprtr);
    $('#cntryEditArvl').val(arvl);
    $('#cntryEditIntlYn').val(intlYn);
    $('#cntryEditExemptCd').val(exemptCd);
    $('#cntryEditDprtr,#cntryEditArvl').removeClass('is-invalid');
    new bootstrap.Modal(document.getElementById('modalCntryEdit')).show();
  };

  window.openCnctEditModal = function (sn, seCd, userNm, mblphn, eml) {
    $('#cnctEditSn').val(sn);
    $('#cnctEditSeCd').val(seCd);
    $('#cnctEditUserNm').val(userNm);
    $('#cnctEditMblphnNo').val(mblphn);
    $('#cnctEditEmlAddr').val(eml);
    $('#cnctEditSeCd,#cnctEditUserNm').removeClass('is-invalid');
    new bootstrap.Modal(document.getElementById('modalCnctEdit')).show();
  };

  window.openCo2dEditModal = function (mthdCd, mntrPnt, equipInfo) {
    /* 수정 모드 — mthdCd(기존 key) 저장하고 select 비활성화 */
    $('#co2dEditMthdCd').val(mthdCd);
    $('#co2dEditMntrMthdCd').val(mthdCd).prop('disabled', true);
    $('#co2dEditMntrPnt').val(mntrPnt);
    $('#co2dEditEquipInfo').val(equipInfo);
    $('#co2dEditProcDesc').val(''); // 상세 텍스트는 단건 조회로 채움
    $('#co2dEditError').addClass('d-none').text('');
    $('#modalCo2DetailEditLabel').text('CO2 측정 상세 수정');
    // 단건 조회로 procDesc 채우기
    $.get('/api/emp/plan/' + EMP_PLAN_ID + '/co2-detail/' + encodeURIComponent(mthdCd))
      .done(function (res) {
        var d = res.data || res;
        if (d) { $('#co2dEditProcDesc').val(d.procDesc || ''); }
      });
    new bootstrap.Modal(document.getElementById('modalCo2DetailEdit')).show();
  };

  window.openRiskEditModal = function (sn) {
    // 단건 조회로 폼 채움
    $.get('/api/emp/plan/' + EMP_PLAN_ID + '/risk/' + sn)
      .done(function (res) {
        var d = res.data || res;
        if (!d) return;
        $('#riskEditSn').val(sn);
        $('#riskEditDesc').val(d.riskDesc || '');
        $('#riskEditCtrlActv').val(d.ctrlActv || '');
        $('#riskEditDesc').removeClass('is-invalid');
        new bootstrap.Modal(document.getElementById('modalRiskEdit')).show();
      })
      .fail(function () { IcasAlert.error('데이터 조회 실패'); });
  };

  // ─────────────────────────────────────────────
  // 자식 행 삭제 (전역 노출)
  // ─────────────────────────────────────────────
  window.deleteChildRow = function (childPath, keyVal) {
    if (!confirm('삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({
      url: '/api/emp/plan/' + EMP_PLAN_ID + '/' + childPath + '/' + keyVal,
      method: 'DELETE'
    })
      .done(function () {
        // 해당 탭 리로드
        var tabMap = { 'acft': 'acft', 'cntry-pair': 'cntry', 'cnct': 'cnct', 'co2-detail': 'co2detail', 'risk': 'risk' };
        var tabKey = tabMap[childPath] || childPath;
        loadTabData(tabKey);
      })
      .fail(function (xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) || '삭제에 실패했습니다.';
        IcasAlert.error(msg);
      });
  };

  // ─────────────────────────────────────────────
  // 라이프사이클 액션 처리
  // ─────────────────────────────────────────────
  var pendingAction = null;

  function executeAction(action, reason) {
    var url = '/api/emp/plan/' + EMP_PLAN_ID + '/' + action;
    var data = reason ? JSON.stringify({ reason: reason }) : '{}';
    $.ajax({ url: url, method: 'POST', contentType: 'application/json', data: data })
      .done(function (res) {
        var msg = (res && res.message) || '처리되었습니다.';
        IcasAlert.success(msg);
        if (action === 'new-version' && res.data && res.data.empPlanId) {
          location.href = '/emp/plan/' + res.data.empPlanId;
        } else {
          location.reload();
        }
      })
      .fail(function (xhr) {
        var msg = (xhr.responseJSON && xhr.responseJSON.message) || '처리에 실패했습니다.';
        IcasAlert.error(msg);
      });
  }

  // ─────────────────────────────────────────────
  // inline edit — single 탭 (info, co2, ctrl)
  // ─────────────────────────────────────────────
  function fillForm(tabKey, data) {
    if (!data) return;
    if (tabKey === 'info') {
      $('#infoOprtrNm').val(data.oprtrNm || '');
      $('#infoAddr').val(data.addr || '');
      $('#infoLglrprNm').val(data.lglrprNm || '');
      $('#infoIcaoDesig').val(data.icaoDesig || '');
      $('#infoRegisMark').val(data.regisMark || '');
      $('#infoAocNo').val(data.aocNo || '');
      $('#infoAocIssueDt').val(data.aocIssueDt || '');
      $('#infoAocXprDt').val(data.aocXprDt || '');
      $('#infoParentCo').val(data.parentCo || '');
      $('#infoSbsdryInfo').val(data.sbsdryInfo || '');
    } else if (tabKey === 'co2') {
      $('#co2MntrMthdCd').val(data.mntrMthdCd || '');
      $('#co2CertUseYn').val(data.certUseYn || 'N');
      $('#co2FuelDnstySecCd').val(data.fuelDnstySecCd || '');
      $('#co2EstCo2Emsn').val(data.estCo2Emsn != null ? data.estCo2Emsn : '');
    } else if (tabKey === 'ctrl') {
      $('#ctrlFlowDesc').val(data.flowDesc || '');
      $('#ctrlGapThrshld').val(data.gapThrshld5pct || '');
      $('#ctrlSndSrcDesc').val(data.sndSrcUseDesc || '');
      $('#ctrlRiskAnlys').val(data.riskAnlys || '');
      $('#ctrlSigChgAprvProc').val(data.sigChgAprvProc || '');
      updateCharCount('#ctrlFlowDesc', '#ctrlFlowDescLen');
    }
  }

  function updateCharCount(textareaId, countId) {
    var len = $(textareaId).val().length;
    $(countId).text(len);
  }

  // ─────────────────────────────────────────────
  // 이벤트 바인딩
  // ─────────────────────────────────────────────
  $(function () {
    loadPlan();

    // 탭 클릭 시 데이터 로드 (최초 1회)
    $('#empDetailTabs button[data-bs-toggle="tab"]').on('shown.bs.tab', function (e) {
      var target = $(e.target).data('bs-target'); // #tabInfo, #tabAcft, ...
      var tabKey = target.replace('#tab', '').toLowerCase().replace('co2detail', 'co2detail').replace('co2', 'co2');
      // 매핑
      var keyMap = {
        '#tabInfo': 'info', '#tabAcft': 'acft', '#tabCntry': 'cntry',
        '#tabCnct': 'cnct', '#tabCo2': 'co2', '#tabCo2Detail': 'co2detail',
        '#tabCtrl': 'ctrl', '#tabRisk': 'risk'
      };
      var k = keyMap[target];
      if (k && !tabLoaded[k]) {
        loadTabData(k);
        tabLoaded[k] = true;
      }
    });

    // 라이프사이클 액션 버튼 클릭
    $('#lifecycleActions').on('click', '.lc-action-btn', function () {
      var action = $(this).data('action');
      var def = null;
      LC_ACTIONS.forEach(function (d) { if (d.action === action) def = d; });
      if (!def) return;

      if (def.needReason) {
        pendingAction = action;
        $('#reasonInput').val('').removeClass('is-invalid');
        $('#reasonInput-error').text('');
        var title = def.label + ' 사유 입력';
        $('#modalReasonLabel').text(title);
        new bootstrap.Modal(document.getElementById('modalReason')).show();
      } else if (def.confirm) {
        if (confirm(def.confirm)) { /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
          executeAction(action, null);
        }
      } else {
        executeAction(action, null);
      }
    });

    // 사유 모달 확인
    $('#btnReasonConfirm').on('click', function () {
      var reason = $('#reasonInput').val().trim();
      if (!reason) {
        $('#reasonInput').addClass('is-invalid');
        $('#reasonInput-error').text('사유를 입력해주세요.');
        return;
      }
      bootstrap.Modal.getInstance(document.getElementById('modalReason')).hide();
      if (pendingAction) {
        executeAction(pendingAction, reason);
        pendingAction = null;
      }
    });

    // ── edit-btn (single 탭 편집 시작) ──
    $(document).on('click', '.edit-btn', function () {
      var tabKey = $(this).data('tab');
      var viewAreaMap = { info: '#infoViewArea', co2: '#co2ViewArea', ctrl: '#ctrlViewArea' };
      var formMap = { info: '#infoEditForm', co2: '#co2EditForm', ctrl: '#ctrlEditForm' };
      $(viewAreaMap[tabKey]).hide();
      $(formMap[tabKey]).show();
      // 최신 데이터 다시 읽어 폼 채우기
      $.get(apiUrl(TAB_CONFIG[tabKey].url))
        .done(function (res) { fillForm(tabKey, res.data || res); });
      $(this).hide();
    });

    // ── cancel-edit-btn ──
    $(document).on('click', '.cancel-edit-btn', function () {
      var tabKey = $(this).data('tab');
      toggleSingleTabView(tabKey, true);
    });

    function toggleSingleTabView(tabKey, showView) {
      var viewAreaMap = { info: '#infoViewArea', co2: '#co2ViewArea', ctrl: '#ctrlViewArea' };
      var formMap = { info: '#infoEditForm', co2: '#co2EditForm', ctrl: '#ctrlEditForm' };
      if (showView) {
        $(viewAreaMap[tabKey]).show();
        $(formMap[tabKey]).hide();
        $('.edit-btn[data-tab="' + tabKey + '"]').show();
      } else {
        $(viewAreaMap[tabKey]).hide();
        $(formMap[tabKey]).show();
        $('.edit-btn[data-tab="' + tabKey + '"]').hide();
      }
    }

    // ── save-edit-btn (폼 submit) ──
    $('#infoEditForm, #co2EditForm, #ctrlEditForm').on('submit', function (e) {
      e.preventDefault();
      var $form = $(this);
      var tabKey = $form.find('.save-edit-btn').data('tab');
      var url = apiUrl($form.data('url-tpl'));
      var formData = {};
      $form.serializeArray().forEach(function (item) { formData[item.name] = item.value; });

      $.ajax({ url: url, method: 'PUT', contentType: 'application/json', data: JSON.stringify(formData) })
        .done(function () {
          IcasAlert.success('저장되었습니다.');
          toggleSingleTabView(tabKey, true);
          tabLoaded[tabKey] = false;
          loadTabData(tabKey);
          tabLoaded[tabKey] = true;
        })
        .fail(function (xhr) {
          var msg = (xhr.responseJSON && xhr.responseJSON.message) || '저장에 실패했습니다.';
          IcasAlert.error(msg);
        });
    });

    // ── add-child-btn (행 추가 폼 토글 또는 모달 오픈) ──
    $(document).on('click', '.add-child-btn', function () {
      var tabKey = $(this).data('tab');
      if (tabKey === 'co2detail') {
        // CO2 상세는 모달로 처리
        $('#co2dEditMthdCd').val('');
        $('#co2dEditMntrMthdCd').val('').prop('disabled', false);
        $('#co2dEditMntrPnt').val('');
        $('#co2dEditEquipInfo').val('');
        $('#co2dEditProcDesc').val('');
        $('#co2dEditError').addClass('d-none').text('');
        $('#modalCo2DetailEditLabel').text('CO2 측정 상세 등록');
        new bootstrap.Modal(document.getElementById('modalCo2DetailEdit')).show();
        return;
      }
      var addRowMap = { acft: '#acftAddRow', cntry: '#cntryAddRow', cnct: '#cnctAddRow', risk: '#riskAddRow' };
      var $row = $(addRowMap[tabKey]);
      if ($row.length) $row.slideToggle(150);
    });

    // ── 항공기 저장 ──
    $('#btnAcftAddSave').on('click', function () {
      var payload = {
        acftTypeCd: $('#acftTypeCd').val().toUpperCase().trim(),
        fuelTypeCd: $('#acftFuelTypeCd').val(),
        acftCnt: parseInt($('#acftCnt').val(), 10) || 0
      };
      var valid = true;
      if (!payload.acftTypeCd) { $('#acftTypeCd').addClass('is-invalid'); $('#acftTypeCd-error').text('항공기 유형코드를 입력하세요.'); valid = false; }
      else $('#acftTypeCd').removeClass('is-invalid');
      if (!payload.fuelTypeCd) { $('#acftFuelTypeCd').addClass('is-invalid'); $('#acftFuelTypeCd-error').text('연료 유형을 선택하세요.'); valid = false; }
      else $('#acftFuelTypeCd').removeClass('is-invalid');
      if (!payload.acftCnt || payload.acftCnt < 1) { $('#acftCnt').addClass('is-invalid'); $('#acftCnt-error').text('대수를 1 이상 입력하세요.'); valid = false; }
      else $('#acftCnt').removeClass('is-invalid');
      if (!valid) return;
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/acft', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () { $('#acftAddRow').slideUp(150); loadTabData('acft'); })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });
    $('#btnAcftAddCancel').on('click', function () { $('#acftAddRow').slideUp(150); });

    // ── 국가쌍 저장 ──
    $('#btnCntryAddSave').on('click', function () {
      var payload = {
        dprtrCntryCd: $('#cntryDprtrCd').val().toUpperCase().trim(),
        arvlCntryCd:  $('#cntryArvlCd').val().toUpperCase().trim(),
        intlYn:       $('#cntryIntlYn').val(),
        exemptCd:     $('#cntryExemptCd').val().trim()
      };
      var valid = true;
      if (!payload.dprtrCntryCd) { $('#cntryDprtrCd').addClass('is-invalid'); $('#cntryDprtrCd-error').text('출발국가를 입력하세요.'); valid = false; }
      else $('#cntryDprtrCd').removeClass('is-invalid');
      if (!payload.arvlCntryCd) { $('#cntryArvlCd').addClass('is-invalid'); $('#cntryArvlCd-error').text('도착국가를 입력하세요.'); valid = false; }
      else $('#cntryArvlCd').removeClass('is-invalid');
      if (!valid) return;
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/cntry-pair', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () { $('#cntryAddRow').slideUp(150); loadTabData('cntry'); })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });
    $('#btnCntryAddCancel').on('click', function () { $('#cntryAddRow').slideUp(150); });

    // ── 담당자 저장 ──
    $('#btnCnctAddSave').on('click', function () {
      var payload = {
        cnctSeCd: $('#cnctSeCd').val(),
        userNm:   $('#cnctUserNm').val().trim(),
        mblphnNo: $('#cnctMblphnNo').val().trim(),
        emlAddr:  $('#cnctEmlAddr').val().trim()
      };
      var valid = true;
      if (!payload.cnctSeCd) { $('#cnctSeCd').addClass('is-invalid'); $('#cnctSeCd-error').text('구분을 선택하세요.'); valid = false; }
      else $('#cnctSeCd').removeClass('is-invalid');
      if (!payload.userNm) { $('#cnctUserNm').addClass('is-invalid'); $('#cnctUserNm-error').text('성명을 입력하세요.'); valid = false; }
      else $('#cnctUserNm').removeClass('is-invalid');
      if (!valid) return;
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/cnct', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () { $('#cnctAddRow').slideUp(150); loadTabData('cnct'); })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });
    $('#btnCnctAddCancel').on('click', function () { $('#cnctAddRow').slideUp(150); });

    // ── 리스크 저장 ──
    $('#btnRiskAddSave').on('click', function () {
      var payload = {
        riskDesc: $('#riskDesc').val().trim(),
        ctrlActv: $('#riskCtrlActv').val().trim()
      };
      if (!payload.riskDesc) { $('#riskDesc').addClass('is-invalid'); $('#riskDesc-error').text('위험 설명을 입력하세요.'); return; }
      else $('#riskDesc').removeClass('is-invalid');
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/risk', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () { $('#riskAddRow').slideUp(150); loadTabData('risk'); })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });
    $('#btnRiskAddCancel').on('click', function () { $('#riskAddRow').slideUp(150); });

    // ── textarea 글자수 카운트 ──
    $('#ctrlFlowDesc').on('input', function () { updateCharCount('#ctrlFlowDesc', '#ctrlFlowDescLen'); });

    // ── ICAO 코드 대문자 자동 변환 ──
    $('#infoIcaoDesig').on('input', function () { $(this).val($(this).val().toUpperCase()); });

    // ── 항공기 수정 모달 저장 ──
    $('#btnAcftEditSave').on('click', function () {
      var sn = $('#acftEditSn').val();
      var payload = {
        acftTypeCd: $('#acftEditTypeCd').val().toUpperCase().trim(),
        fuelTypeCd: $('#acftEditFuelTypeCd').val(),
        acftCnt:    parseInt($('#acftEditCnt').val(), 10) || 0
      };
      var valid = true;
      if (!payload.acftTypeCd) { $('#acftEditTypeCd').addClass('is-invalid'); $('#acftEditTypeCd-error').text('유형코드를 입력하세요.'); valid = false; }
      else $('#acftEditTypeCd').removeClass('is-invalid');
      if (!payload.fuelTypeCd) { $('#acftEditFuelTypeCd').addClass('is-invalid'); $('#acftEditFuelTypeCd-error').text('연료유형을 선택하세요.'); valid = false; }
      else $('#acftEditFuelTypeCd').removeClass('is-invalid');
      if (!payload.acftCnt || payload.acftCnt < 1) { $('#acftEditCnt').addClass('is-invalid'); $('#acftEditCnt-error').text('대수를 1 이상 입력하세요.'); valid = false; }
      else $('#acftEditCnt').removeClass('is-invalid');
      if (!valid) return;
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/acft/' + sn, method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () {
          bootstrap.Modal.getInstance(document.getElementById('modalAcftEdit')).hide();
          IcasAlert.success('저장되었습니다.');
          loadTabData('acft');
        })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });

    // ── 국가쌍 수정 모달 저장 ──
    $('#btnCntryEditSave').on('click', function () {
      var sn = $('#cntryEditSn').val();
      var payload = {
        dprtrCntryCd: $('#cntryEditDprtr').val().toUpperCase().trim(),
        arvlCntryCd:  $('#cntryEditArvl').val().toUpperCase().trim(),
        intlYn:       $('#cntryEditIntlYn').val(),
        exemptCd:     $('#cntryEditExemptCd').val().trim()
      };
      var valid = true;
      if (!payload.dprtrCntryCd) { $('#cntryEditDprtr').addClass('is-invalid'); $('#cntryEditDprtr-error').text('출발국가를 입력하세요.'); valid = false; }
      else $('#cntryEditDprtr').removeClass('is-invalid');
      if (!payload.arvlCntryCd) { $('#cntryEditArvl').addClass('is-invalid'); $('#cntryEditArvl-error').text('도착국가를 입력하세요.'); valid = false; }
      else $('#cntryEditArvl').removeClass('is-invalid');
      if (!valid) return;
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/cntry-pair/' + sn, method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () {
          bootstrap.Modal.getInstance(document.getElementById('modalCntryEdit')).hide();
          IcasAlert.success('저장되었습니다.');
          loadTabData('cntry');
        })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });

    // ── 담당자 수정 모달 저장 ──
    $('#btnCnctEditSave').on('click', function () {
      var sn = $('#cnctEditSn').val();
      var payload = {
        cnctSeCd: $('#cnctEditSeCd').val(),
        userNm:   $('#cnctEditUserNm').val().trim(),
        mblphnNo: $('#cnctEditMblphnNo').val().trim(),
        emlAddr:  $('#cnctEditEmlAddr').val().trim()
      };
      var valid = true;
      if (!payload.cnctSeCd) { $('#cnctEditSeCd').addClass('is-invalid'); $('#cnctEditSeCd-error').text('구분을 선택하세요.'); valid = false; }
      else $('#cnctEditSeCd').removeClass('is-invalid');
      if (!payload.userNm) { $('#cnctEditUserNm').addClass('is-invalid'); $('#cnctEditUserNm-error').text('성명을 입력하세요.'); valid = false; }
      else $('#cnctEditUserNm').removeClass('is-invalid');
      if (!valid) return;
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/cnct/' + sn, method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () {
          bootstrap.Modal.getInstance(document.getElementById('modalCnctEdit')).hide();
          IcasAlert.success('저장되었습니다.');
          loadTabData('cnct');
        })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });

    // ── CO2 상세 모달 저장 (등록/수정 공용) ──
    $('#btnCo2dEditSave').on('click', function () {
      var existMthd = $('#co2dEditMthdCd').val(); // 수정이면 기존 코드, 등록이면 빈 문자열
      var payload = {
        mntrMthdCd: $('#co2dEditMntrMthdCd').val(),
        mntrPnt:    $('#co2dEditMntrPnt').val().trim(),
        equipInfo:  $('#co2dEditEquipInfo').val().trim(),
        procDesc:   $('#co2dEditProcDesc').val().trim()
      };
      if (!payload.mntrMthdCd) {
        $('#co2dEditMntrMthdCd').addClass('is-invalid');
        $('#co2dEditMntrMthdCd-error').text('모니터링 방법을 선택하세요.');
        return;
      }
      $('#co2dEditMntrMthdCd').removeClass('is-invalid');
      $('#co2dEditError').addClass('d-none').text('');
      var isEdit = !!existMthd;
      var url = '/api/emp/plan/' + EMP_PLAN_ID + '/co2-detail' + (isEdit ? '/' + encodeURIComponent(existMthd) : '');
      var method = isEdit ? 'PUT' : 'POST';
      $.ajax({ url: url, method: method, contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () {
          bootstrap.Modal.getInstance(document.getElementById('modalCo2DetailEdit')).hide();
          IcasAlert.success('저장되었습니다.');
          loadTabData('co2detail');
        })
        .fail(function (xhr) {
          var msg = (xhr.responseJSON && xhr.responseJSON.message) || '저장 실패';
          $('#co2dEditError').removeClass('d-none').text(msg);
        });
    });

    // ── 리스크 수정 모달 저장 ──
    $('#btnRiskEditSave').on('click', function () {
      var sn = $('#riskEditSn').val();
      var payload = {
        riskDesc: $('#riskEditDesc').val().trim(),
        ctrlActv: $('#riskEditCtrlActv').val().trim()
      };
      if (!payload.riskDesc) { $('#riskEditDesc').addClass('is-invalid'); $('#riskEditDesc-error').text('위험 설명을 입력하세요.'); return; }
      $('#riskEditDesc').removeClass('is-invalid');
      $.ajax({ url: '/api/emp/plan/' + EMP_PLAN_ID + '/risk/' + sn, method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
        .done(function () {
          bootstrap.Modal.getInstance(document.getElementById('modalRiskEdit')).hide();
          IcasAlert.success('저장되었습니다.');
          loadTabData('risk');
        })
        .fail(function (xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
    });

  }); // /document.ready

})();
</script>
</body>
</html>
