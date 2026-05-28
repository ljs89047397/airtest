<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>적정성검토(OoM) 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.info-card .card-header { background: #0F2C72; color: white; font-size: 0.85rem; font-weight: 600; padding: 0.5rem 1rem; }
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.82rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
.status-badge { font-size: 0.72rem; padding: 3px 8px; border-radius: 4px; font-weight: 600; }
.rslt-badge-lg { font-size: 1rem; padding: 5px 14px; border-radius: 6px; font-weight: 700; letter-spacing: 0.05em; }
.nav-tabs .nav-link { color: #4b5563; font-size: 0.87rem; }
.nav-tabs .nav-link.active { color: #0F2C72; font-weight: 600; border-bottom: 2px solid #0F2C72; }
.rule-row-active { background: #f0f4ff; }
.rule-row-warn   { background: #fffbea; }
.quant-badge-pass { background: #d1fae5; color: #065f46; border-radius: 4px; padding: 2px 8px; font-size: 0.75rem; font-weight: 600; }
.quant-badge-fail { background: #fee2e2; color: #991b1b; border-radius: 4px; padding: 2px 8px; font-size: 0.75rem; font-weight: 600; }
.quant-badge-warn { background: #fef3c7; color: #92400e; border-radius: 4px; padding: 2px 8px; font-size: 0.75rem; font-weight: 600; }
.quant-badge-skip { background: #f3f4f6; color: #6b7280; border-radius: 4px; padding: 2px 8px; font-size: 0.75rem; font-weight: 600; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <!-- 페이지 헤더 -->
  <div class="page-header-bar px-4 py-3">
    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
      <div>
        <div class="d-flex align-items-center gap-2 mb-1">
          <a href="/er/oom/list" class="btn btn-sm btn-outline-secondary py-0 px-2" style="font-size:0.78rem;">
            <i class="bi bi-arrow-left me-1"></i>목록으로
          </a>
          <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#128270; 적정성검토(OoM) 상세</h5>
        </div>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0 small">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/er/oom/list" class="text-decoration-none">적정성검토</a></li>
            <li class="breadcrumb-item active" id="breadcrumbOomId">상세</li>
          </ol>
        </nav>
      </div>
      <!-- 권한별 액션 버튼 -->
      <div id="actionBar" class="d-flex gap-2 flex-wrap"></div>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- ▶ 기본정보 카드 -->
    <div class="card border-0 shadow-sm mb-3 info-card">
      <div class="card-header d-flex align-items-center gap-2">
        <i class="bi bi-info-circle me-1"></i>기본정보
        <span id="rsltBadgeLg" class="ms-auto rslt-badge-lg" style="display:none;"></span>
      </div>
      <div class="card-body">
        <div class="row g-3" id="basicInfoArea">
          <div class="col-12 text-center text-muted small py-3">
            <div class="spinner-border spinner-border-sm me-2" role="status"></div>로딩 중...
          </div>
        </div>
      </div>
    </div>

    <!-- ▶ 운항 활동 요약 — 비행장/국가 쌍 (RFP 박스 ⑥ "비행장/국가 쌍" 항목) -->
    <div class="card border-0 shadow-sm mb-3 info-card" id="cardPairSummary">
      <div class="card-header d-flex align-items-center gap-2">
        <i class="bi bi-globe-asia-australia me-1"></i>운항 활동 — 비행장/국가 쌍 요약
        <span class="badge bg-light text-muted border ms-auto" style="font-size:0.7rem;">연계 ER 데이터</span>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <div class="border rounded p-3">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <span class="fw-semibold small" style="color:#0F2C72;"><i class="bi bi-airplane-engines me-1"></i>비행장 쌍 (Aerodrome Pair)</span>
                <span class="text-muted small" id="aerdrmPairCnt">- 건</span>
              </div>
              <table class="table table-sm mb-0 small" aria-label="비행장 쌍 요약">
                <thead class="text-muted" style="font-size:0.78rem;">
                  <tr>
                    <th>출발 ICAO</th>
                    <th>도착 ICAO</th>
                    <th class="text-end">CO₂ (t)</th>
                    <th class="text-end">비행횟수</th>
                  </tr>
                </thead>
                <tbody id="aerdrmPairBody">
                  <tr><td colspan="4" class="text-center text-muted py-2">로딩 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="col-md-6">
            <div class="border rounded p-3">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <span class="fw-semibold small" style="color:#0F2C72;"><i class="bi bi-globe me-1"></i>국가 쌍 (Country Pair)</span>
                <span class="text-muted small" id="cntryPairCnt">- 건</span>
              </div>
              <table class="table table-sm mb-0 small" aria-label="국가 쌍 요약">
                <thead class="text-muted" style="font-size:0.78rem;">
                  <tr>
                    <th>출발국</th>
                    <th>도착국</th>
                    <th class="text-end">CO₂ (t)</th>
                    <th class="text-end">비행횟수</th>
                  </tr>
                </thead>
                <tbody id="cntryPairBody">
                  <tr><td colspan="4" class="text-center text-muted py-2">로딩 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="small text-muted mt-2">
          <i class="bi bi-info-circle me-1"></i>
          비행장/국가 쌍 데이터는 연계 ER 보고서의 자식 도메인(`/api/er/rprt/{erId}/aerdrm-pair`, `cntry-pair`) 에서 자동 조회됩니다.
        </div>
      </div>
    </div>

    <!-- ▶ 탭 영역 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom-0 pt-3 pb-0">
        <ul class="nav nav-tabs" id="oomTabs" role="tablist">
          <li class="nav-item" role="presentation">
            <button class="nav-link active" id="tab-item" data-bs-toggle="tab" data-bs-target="#pane-item" type="button" role="tab">
              <i class="bi bi-list-check me-1"></i>검토항목
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-rqst" data-bs-toggle="tab" data-bs-target="#pane-rqst" type="button" role="tab">
              <i class="bi bi-chat-left-text me-1"></i>추가요청
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="tab-eval" data-bs-toggle="tab" data-bs-target="#pane-eval" type="button" role="tab">
              <i class="bi bi-award me-1"></i>검증기관평가
            </button>
          </li>
        </ul>
      </div>
      <div class="card-body p-0">
        <div class="tab-content">

          <!-- ─── 탭1: 검토항목 ──────────────────────────────── -->
          <div class="tab-pane fade show active p-3" id="pane-item" role="tabpanel">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-semibold small" style="color:#0F2C72;">검토항목 목록</span>
              <div class="d-flex gap-2">
                <!-- KOTSA: 18종 정량검증 실행 -->
                <button id="btnRunQuant" class="btn btn-sm btn-outline-primary" style="display:none;font-size:0.78rem;">
                  <i class="bi bi-play-circle me-1"></i>18종 정량검증 실행
                </button>
                <!-- KOTSA: 사용자 항목 추가 -->
                <button id="btnAddItem" class="btn btn-sm" style="display:none;background:#0F2C72;color:white;font-size:0.78rem;">
                  <i class="bi bi-plus me-1"></i>항목 추가
                </button>
              </div>
            </div>
            <div class="table-responsive">
              <table class="table table-hover table-sm mb-0 table-icas" aria-label="검토항목 목록">
                <thead>
                  <tr>
                    <th class="ps-3" style="width:60px;">항목번호</th>
                    <th>항목명</th>
                    <th style="width:80px;">구분</th>
                    <th style="width:100px;">정량검증결과</th>
                    <th>비고</th>
                    <th style="width:80px;">관리</th>
                  </tr>
                </thead>
                <tbody id="itemTableBody">
                  <tr><td colspan="6" class="text-center py-3 text-muted small">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>로딩 중...
                  </td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- ─── 탭2: 추가요청 ──────────────────────────────── -->
          <div class="tab-pane fade p-3" id="pane-rqst" role="tabpanel">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-semibold small" style="color:#0F2C72;">추가요청 목록</span>
              <!-- KOTSA: 요청 등록 -->
              <button id="btnAddRqst" class="btn btn-sm" style="display:none;background:#0F2C72;color:white;font-size:0.78rem;">
                <i class="bi bi-plus me-1"></i>요청 등록
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-hover table-sm mb-0 table-icas" aria-label="추가요청 목록">
                <thead>
                  <tr>
                    <th class="ps-3" style="width:50px;">No</th>
                    <th>요청 내용</th>
                    <th>운영사 응답</th>
                    <th style="width:90px;">요청일</th>
                    <th style="width:90px;">응답일</th>
                    <th style="width:80px;">관리</th>
                  </tr>
                </thead>
                <tbody id="rqstTableBody">
                  <tr><td colspan="6" class="text-center py-3 text-muted small">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>로딩 중...
                  </td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- ─── 탭3: 검증기관평가 ──────────────────────────── -->
          <div class="tab-pane fade p-3" id="pane-eval" role="tabpanel">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="fw-semibold small" style="color:#0F2C72;">검증기관 품질평가</span>
              <!-- VERIFIER: 평가 저장 -->
              <button id="btnSaveEval" class="btn btn-sm" style="display:none;background:#0F2C72;color:white;font-size:0.78rem;">
                <i class="bi bi-save me-1"></i>평가 저장
              </button>
            </div>
            <div id="evalArea">
              <div class="text-center text-muted small py-3">
                <div class="spinner-border spinner-border-sm me-2" role="status"></div>로딩 중...
              </div>
            </div>
          </div>

        </div><!-- /tab-content -->
      </div>
    </div><!-- /card -->

  </div><!-- /container-fluid -->
</div><!-- /main -->

<!-- ─────────────── 모달: 항목 추가/편집 (KOTSA) ──────────── -->
<div class="modal fade" id="itemModal" tabindex="-1" aria-labelledby="itemModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="itemModalLabel">검토항목 추가</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="editItemNo">
        <div class="mb-3">
          <label class="form-label small fw-semibold">항목명 <span class="text-danger">*</span></label>
          <input type="text" id="editItemNm" class="form-control form-control-sm" maxlength="200">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">비고</label>
          <textarea id="editItemRm" class="form-control form-control-sm" rows="3" maxlength="1000"></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnSaveItem" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ─────────────── 모달: 추가요청 등록 (KOTSA) ──────────── -->
<div class="modal fade" id="rqstModal" tabindex="-1" aria-labelledby="rqstModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="rqstModalLabel">추가요청 등록</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">요청 내용 <span class="text-danger">*</span></label>
          <textarea id="newRqstCn" class="form-control form-control-sm" rows="5" maxlength="2000" placeholder="운영사에 요청할 내용을 입력하세요."></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnConfirmRqst" class="btn btn-sm" style="background:#0F2C72;color:white;">등록</button>
      </div>
    </div>
  </div>
</div>

<!-- ─────────────── 모달: 응답 입력 (AIRLINE) ──────────── -->
<div class="modal fade" id="respModal" tabindex="-1" aria-labelledby="respModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="respModalLabel">요청 응답 입력</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="respRqstSn">
        <div class="mb-2">
          <label class="form-label small fw-semibold text-muted">요청 내용</label>
          <p id="respRqstCnView" class="small border rounded p-2 bg-light mb-0" style="white-space:pre-wrap;"></p>
        </div>
        <div class="mb-3 mt-3">
          <label class="form-label small fw-semibold">응답 내용 <span class="text-danger">*</span></label>
          <textarea id="newRespCn" class="form-control form-control-sm" rows="5" maxlength="2000" placeholder="응답 내용을 입력하세요."></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnConfirmResp" class="btn btn-sm" style="background:#0F2C72;color:white;">응답 제출</button>
      </div>
    </div>
  </div>
</div>

<!-- ─────────────── 모달: 확정 (KOTSA) ──────────── -->
<div class="modal fade" id="finalizeModal" tabindex="-1" aria-labelledby="finalizeModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header" style="background:#0F2C72;color:white;">
        <h6 class="modal-title fw-bold" id="finalizeModalLabel">OoM 결과 확정</h6>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <p class="small text-muted mb-3">최종 판정 결과를 선택한 뒤 확정합니다. 확정 후에는 수정이 제한됩니다.</p>
        <div class="d-flex gap-3 justify-content-center">
          <label class="form-check-label">
            <input type="radio" name="finalRslt" value="PASS" class="form-check-input me-1"> <span class="badge bg-success px-3 py-2">PASS</span>
          </label>
          <label class="form-check-label">
            <input type="radio" name="finalRslt" value="FAIL" class="form-check-input me-1"> <span class="badge bg-danger px-3 py-2">FAIL</span>
          </label>
          <label class="form-check-label">
            <input type="radio" name="finalRslt" value="HOLD" class="form-check-input me-1"> <span class="badge bg-warning text-dark px-3 py-2">HOLD</span>
          </label>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btnConfirmFinalize" class="btn btn-sm btn-danger">확정</button>
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
/* ══════════════════════════════════════════════════════════════
   OoM 상세 페이지 스크립트
   ══════════════════════════════════════════════════════════════ */
'use strict';

/* ── 18종 정량검증 Rule 메타 ────────────────────────────────── */
var QUANT_RULES = [
  { no:1,  nm:'CO2 배출량 합계 일치성',           active:true  },
  { no:2,  nm:'연료 사용량 → 배출량 변환계수 정합성',active:true  },
  { no:3,  nm:'항공편 수 대비 연료 이상치 탐지',    active:false },
  { no:4,  nm:'CER 추정 여부 일관성',              active:true  },
  { no:5,  nm:'국가쌍 배출량 음수 검사',            active:false },
  { no:6,  nm:'SAF 블렌딩 비율 범위 확인',          active:false },
  { no:7,  nm:'CORSIA 적격 항로 배출량 분리 검증',  active:true  },
  { no:8,  nm:'이전연도 대비 변동률 임계치',         active:false },
  { no:9,  nm:'연료 종류 코드 유효성',              active:false },
  { no:10, nm:'항공기 유형별 배출계수 범위',         active:false },
  { no:11, nm:'출발/도착 국가 ICAO 코드 유효성',    active:false },
  { no:12, nm:'중복 항공편 탐지',                  active:false },
  { no:13, nm:'버전 간 배출량 변동 허용 범위',       active:false },
  { no:14, nm:'연료효율 지표 (CO2/RTK) 이상치',    active:false },
  { no:15, nm:'CORSIA 기준선 초과 여부',           active:false },
  { no:16, nm:'VR 상쇄 금액 정합성',               active:true  },
  { no:17, nm:'CEF 적격 연료량 교차 검증',          active:true  },
  { no:18, nm:'EUCR 취소 배출권 일치성',            active:true  }
];

/* ── 공통 유틸 ─────────────────────────────────────────────── */
function escHtml(s) {
  if (s == null) return '-';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function rsltBadgeClass(cd) {
  var m = { 'PASS':'bg-success','FAIL':'bg-danger','HOLD':'bg-warning text-dark','INPRG':'bg-secondary' };
  return m[cd] || 'bg-secondary';
}

/* ── URL 에서 oomId 추출 ────────────────────────────────────── */
var pathParts = window.location.pathname.split('/');
var oomId = pathParts[pathParts.length - 1] || '';

/* 세션에서 역할 읽기 (서버 EL 주입) */
var userRole    = '${sessionScope.userRole}';
var ognzSeCdOom = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';

/* ── 기본정보 렌더 ──────────────────────────────────────────── */
function renderBasicInfo(d) {
  $('#breadcrumbOomId').text(escHtml(d.oomId));

  /* 판정 배지 */
  var rsltCd = d.rsltCd || 'INPRG';
  var rsltLbl = { 'PASS':'PASS','FAIL':'FAIL','HOLD':'HOLD','INPRG':'진행중' }[rsltCd] || rsltCd;
  var rsltCls = rsltBadgeClass(rsltCd);
  $('#rsltBadgeLg')
    .removeClass().addClass('ms-auto rslt-badge-lg badge ' + rsltCls)
    .text(rsltLbl)
    .show();

  var html = ''
    + '<div class="col-md-3 col-6"><div class="text-muted small">OoM ID</div><div class="fw-semibold small">' + escHtml(d.oomId) + '</div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">보고연도</div><div class="fw-semibold small">' + escHtml(d.rprtYr) + '</div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">운영사</div><div class="fw-semibold small">' + escHtml(d.oprtrNm) + '</div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">ICAO 코드</div><div class="fw-semibold small">' + escHtml(d.icaoCd) + '</div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">연계 ER</div><div class="small"><a href="/er/detail?id=' + escHtml(d.erId) + '" class="text-decoration-none">' + escHtml(d.erId) + '</a></div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">연계 VR</div><div class="small">' + (d.vrId ? '<a href="/vr/detail?id=' + escHtml(d.vrId) + '" class="text-decoration-none">' + escHtml(d.vrId) + '</a>' : '-') + '</div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">생성일</div><div class="small">' + escHtml(d.crtDt) + '</div></div>'
    + '<div class="col-md-3 col-6"><div class="text-muted small">확정일</div><div class="small">' + (d.fnlzDt ? escHtml(d.fnlzDt) : '-') + '</div></div>';
  $('#basicInfoArea').html(html);

  /* 권한별 액션 버튼 렌더 */
  renderActionBar(rsltCd);
}

function renderActionBar(rsltCd) {
  var html = '';

  /* 미인증 세션이면 모든 액션 버튼 숨김 — 기본정보(조회)는 유지 */
  if (!ognzSeCdOom) {
    $('#actionBar').html('');
    return;
  }

  if (userRole === 'KOTSA' && ognzSeCdOom === 'KOTSA') {
    html += '<button id="btnRunQuantTop" class="btn btn-sm btn-outline-primary" style="font-size:0.78rem;"><i class="bi bi-play-circle me-1"></i>정량검증 실행</button>';
    if (rsltCd === 'INPRG') {
      html += '<button id="btnFinalizeTop" class="btn btn-sm btn-success" style="font-size:0.78rem;"><i class="bi bi-check2-circle me-1"></i>결과 확정</button>';
      html += '<button id="btnHoldTop" class="btn btn-sm btn-warning text-dark" style="font-size:0.78rem;"><i class="bi bi-pause-circle me-1"></i>HOLD</button>';
    }
    /* 탭 내 KOTSA 전용 버튼 노출 */
    $('#btnRunQuant,#btnAddItem,#btnAddRqst').show();
  } else if (userRole === 'VERIFIER' && ognzSeCdOom === 'VERIFIER') {
    $('#btnSaveEval').show();
  } else if (userRole === 'AIRLINE' && ognzSeCdOom === 'AIRLINE') {
    /* 응답 버튼은 각 행 생성 시 처리 */
  }
  /* MOLIT: 조회만 — 액션 버튼 없음 (html 빈 상태 그대로) */
  $('#actionBar').html(html);

  /* 상단 버튼 이벤트 */
  $('#btnRunQuantTop').on('click', function() { runQuant(); });
  $('#btnFinalizeTop').on('click', function() {
    new bootstrap.Modal(document.getElementById('finalizeModal')).show();
  });
  $('#btnHoldTop').on('click', function() {
    if (!confirm('HOLD 처리하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/hold', type: 'POST' })
      .done(function() { location.reload(); })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || 'HOLD 처리 실패'); });
  });
}

/* ── OoM 마스터 데이터 로드 ─────────────────────────────────── */
var masterData = null;

var sampleMaster = {
  oomId:'OOM202600001', oprtrNm:'대한항공', icaoCd:'KAL',
  rprtYr:2026, erId:'ER202600001', vrId:'VR202600001',
  rsltCd:'INPRG', crtDt:'2026-04-10', fnlzDt:null
};

function loadMaster() {
  $.get('/api/er/oom/' + encodeURIComponent(oomId))
    .done(function(res) {
      masterData = (res && res.data) ? res.data : sampleMaster;
      renderBasicInfo(masterData);
      loadPairSummary(masterData.erId);
    })
    .fail(function() {
      masterData = sampleMaster;
      renderBasicInfo(masterData);
      loadPairSummary(masterData.erId);
    });
}

/* ── 비행장/국가 쌍 요약 로드 (RFP 박스 ⑥ 항목) ───────────────── */
function loadPairSummary(erId) {
  if (!erId) {
    $('#aerdrmPairBody').html('<tr><td colspan="4" class="text-center text-muted py-2">연계 ER 미연결</td></tr>');
    $('#cntryPairBody').html('<tr><td colspan="4" class="text-center text-muted py-2">연계 ER 미연결</td></tr>');
    return;
  }
  // 비행장 쌍
  $.get('/api/er/rprt/' + encodeURIComponent(erId) + '/aerdrm-pair')
    .done(function(res){
      var rows = (res && res.data) ? res.data : [];
      if (!Array.isArray(rows)) rows = rows.rows || rows.content || [];
      $('#aerdrmPairCnt').text(rows.length + ' 건');
      if (!rows.length) { $('#aerdrmPairBody').html('<tr><td colspan="4" class="text-center text-muted py-2">데이터 없음</td></tr>'); return; }
      var html = rows.slice(0,5).map(function(r){
        return '<tr><td>' + escHtml(r.dprtAerdrmCd) + '</td><td>' + escHtml(r.arvlAerdrmCd) + '</td>'
             + '<td class="text-end">' + escHtml(r.co2Emsn||r.co2Wt||'-') + '</td>'
             + '<td class="text-end">' + escHtml(r.fltCnt||'-') + '</td></tr>';
      }).join('');
      $('#aerdrmPairBody').html(html + (rows.length>5 ? '<tr><td colspan="4" class="text-center text-muted small py-1">… 외 ' + (rows.length-5) + '건</td></tr>' : ''));
    })
    .fail(function(){ $('#aerdrmPairBody').html('<tr><td colspan="4" class="text-center text-danger py-2">조회 실패</td></tr>'); });
  // 국가 쌍
  $.get('/api/er/rprt/' + encodeURIComponent(erId) + '/cntry-pair')
    .done(function(res){
      var rows = (res && res.data) ? res.data : [];
      if (!Array.isArray(rows)) rows = rows.rows || rows.content || [];
      $('#cntryPairCnt').text(rows.length + ' 건');
      if (!rows.length) { $('#cntryPairBody').html('<tr><td colspan="4" class="text-center text-muted py-2">데이터 없음</td></tr>'); return; }
      var html = rows.slice(0,5).map(function(r){
        return '<tr><td>' + escHtml(r.dprtCntryCd) + '</td><td>' + escHtml(r.arvlCntryCd) + '</td>'
             + '<td class="text-end">' + escHtml(r.co2Emsn||r.co2Wt||'-') + '</td>'
             + '<td class="text-end">' + escHtml(r.fltCnt||'-') + '</td></tr>';
      }).join('');
      $('#cntryPairBody').html(html + (rows.length>5 ? '<tr><td colspan="4" class="text-center text-muted small py-1">… 외 ' + (rows.length-5) + '건</td></tr>' : ''));
    })
    .fail(function(){ $('#cntryPairBody').html('<tr><td colspan="4" class="text-center text-danger py-2">조회 실패</td></tr>'); });
}

/* ── 검토항목 탭 ────────────────────────────────────────────── */
var sampleItems = [
  { itemNo:1,  itemNm:'CO2 배출량 합계 일치성',            itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:2,  itemNm:'연료→배출량 변환계수 정합성',        itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:3,  itemNm:'항공편 수 대비 연료 이상치 탐지',    itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:4,  itemNm:'CER 추정 여부 일관성',              itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:5,  itemNm:'국가쌍 배출량 음수 검사',            itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:6,  itemNm:'SAF 블렌딩 비율 범위 확인',          itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:7,  itemNm:'CORSIA 적격 항로 배출량 분리 검증',  itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:8,  itemNm:'이전연도 대비 변동률 임계치',         itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:9,  itemNm:'연료 종류 코드 유효성',              itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:10, itemNm:'항공기 유형별 배출계수 범위',         itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:11, itemNm:'출발/도착 국가 ICAO 코드 유효성',    itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:12, itemNm:'중복 항공편 탐지',                  itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:13, itemNm:'버전 간 배출량 변동 허용 범위',       itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:14, itemNm:'연료효율 지표 이상치',               itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:15, itemNm:'CORSIA 기준선 초과 여부',           itemTpCd:'AUTO', quantRslt:'WARN', rmrk:'데이터 부족' },
  { itemNo:16, itemNm:'VR 상쇄 금액 정합성',               itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:17, itemNm:'CEF 적격 연료량 교차 검증',          itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:18, itemNm:'EUCR 취소 배출권 일치성',            itemTpCd:'AUTO', quantRslt:'PASS', rmrk:null },
  { itemNo:101,itemNm:'추가 사용자 항목 예시',              itemTpCd:'USER', quantRslt:null,   rmrk:'현장 확인 필요' }
];

function quantBadgeHtml(qr) {
  if (!qr) return '<span class="quant-badge-skip">-</span>';
  var map = { 'PASS':'quant-badge-pass','FAIL':'quant-badge-fail','WARN':'quant-badge-warn','SKIP':'quant-badge-skip' };
  var lbl = { 'PASS':'PASS','FAIL':'FAIL','WARN':'데이터 부족','SKIP':'제외' };
  return '<span class="' + (map[qr]||'quant-badge-skip') + '">' + (lbl[qr]||qr) + '</span>';
}

function loadItems() {
  $.get('/api/er/oom/' + encodeURIComponent(oomId) + '/item')
    .done(function(res) { renderItems((res && res.data) ? res.data : sampleItems); })
    .fail(function()    { renderItems(sampleItems); });
}

function renderItems(list) {
  if (!list || !list.length) {
    $('#itemTableBody').html('<tr><td colspan="6" class="text-center py-3 text-muted small">항목이 없습니다.</td></tr>');
    return;
  }
  var html = '';
  list.forEach(function(row) {
    var isAuto = (row.itemTpCd === 'AUTO');
    var rowCls = isAuto ? (row.quantRslt === 'WARN' ? 'rule-row-warn' : 'rule-row-active') : '';
    var tpBadge = isAuto
      ? '<span class="badge bg-secondary" style="font-size:0.68rem;">자동</span>'
      : '<span class="badge bg-primary" style="font-size:0.68rem;">추가</span>';
    var editBtn = (userRole === 'KOTSA')
      ? '<button class="btn btn-link btn-sm p-0 me-2 btn-edit-item" data-no="' + row.itemNo + '" data-nm="' + escHtml(row.itemNm) + '" data-rm="' + escHtml(row.rmrk||'') + '" style="font-size:0.72rem;">편집</button>'
      : '';
    var delBtn = (userRole === 'KOTSA' && !isAuto)
      ? '<button class="btn btn-link btn-sm p-0 text-danger btn-del-item" data-no="' + row.itemNo + '" style="font-size:0.72rem;">삭제</button>'
      : '';
    html += '<tr class="' + rowCls + '">'
      + '<td class="ps-3 small fw-semibold">' + escHtml(row.itemNo) + '</td>'
      + '<td class="small">' + escHtml(row.itemNm) + '</td>'
      + '<td>' + tpBadge + '</td>'
      + '<td>' + quantBadgeHtml(row.quantRslt) + '</td>'
      + '<td class="small text-muted">' + escHtml(row.rmrk) + '</td>'
      + '<td>' + editBtn + delBtn + '</td>'
      + '</tr>';
  });
  $('#itemTableBody').html(html);

  /* 이벤트 바인딩 */
  $('.btn-edit-item').off('click').on('click', function() {
    $('#editItemNo').val($(this).data('no'));
    $('#editItemNm').val($(this).data('nm'));
    $('#editItemRm').val($(this).data('rm'));
    $('#itemModalLabel').text('검토항목 편집');
    new bootstrap.Modal(document.getElementById('itemModal')).show();
  });
  $('.btn-del-item').off('click').on('click', function() {
    var no = $(this).data('no');
    if (!confirm('항목을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/item/' + no, type: 'DELETE' })
      .done(function() { loadItems(); })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '삭제 실패'); });
  });
}

/* ── 18종 정량검증 실행 ─────────────────────────────────────── */
function runQuant() {
  if (!confirm('18종 정량검증을 실행하시겠습니까? 완료까지 수 초가 걸릴 수 있습니다.')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
  $('#btnRunQuant,#btnRunQuantTop').prop('disabled', true).text('실행 중...');
  $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/run-quant', type: 'POST' })
    .done(function() {
      IcasAlert.success('18종 정량검증이 완료되었습니다.');
      loadItems();
    })
    .fail(function(xhr) {
      IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '정량검증 실행 실패');
    })
    .always(function() {
      $('#btnRunQuant').prop('disabled', false).html('<i class="bi bi-play-circle me-1"></i>18종 정량검증 실행');
      $('#btnRunQuantTop').prop('disabled', false).html('<i class="bi bi-play-circle me-1"></i>정량검증 실행');
    });
}

/* ── 추가요청 탭 ────────────────────────────────────────────── */
var sampleRqsts = [
  { rqstSn:1, rqstCn:'2026년 1월~3월 CEF 적격 연료 구매 증빙 서류를 제출해 주십시오.',
    respCn:'증빙 서류를 첨부 파일로 제출하였습니다. 확인 부탁드립니다.',
    rqstDt:'2026-04-15', respDt:'2026-04-18' },
  { rqstSn:2, rqstCn:'국가쌍 배출량 보고서의 노선별 합산 내역을 별도 제출 바랍니다.',
    respCn:null, rqstDt:'2026-04-20', respDt:null }
];

function loadRqsts() {
  $.get('/api/er/oom/' + encodeURIComponent(oomId) + '/rqst')
    .done(function(res) { renderRqsts((res && res.data) ? res.data : sampleRqsts); })
    .fail(function()    { renderRqsts(sampleRqsts); });
}

function renderRqsts(list) {
  if (!list || !list.length) {
    $('#rqstTableBody').html('<tr><td colspan="6" class="text-center py-3 text-muted small">등록된 요청이 없습니다.</td></tr>');
    return;
  }
  var html = '';
  list.forEach(function(row, idx) {
    var respCell = row.respCn
      ? '<span class="small">' + escHtml(row.respCn) + '</span>'
      : (userRole === 'AIRLINE'
          ? '<button class="btn btn-sm btn-outline-primary btn-respond" data-sn="' + row.rqstSn + '" data-cn="' + escHtml(row.rqstCn) + '" style="font-size:0.72rem;">응답 입력</button>'
          : '<span class="text-muted small">미응답</span>');
    var delBtn = (userRole === 'KOTSA')
      ? '<button class="btn btn-link btn-sm p-0 text-danger btn-del-rqst" data-sn="' + row.rqstSn + '" style="font-size:0.72rem;">삭제</button>'
      : '';
    html += '<tr>'
      + '<td class="ps-3 text-muted small">' + (idx+1) + '</td>'
      + '<td class="small">' + escHtml(row.rqstCn) + '</td>'
      + '<td class="small">' + respCell + '</td>'
      + '<td class="small text-muted">' + escHtml(row.rqstDt) + '</td>'
      + '<td class="small text-muted">' + (row.respDt ? escHtml(row.respDt) : '-') + '</td>'
      + '<td>' + delBtn + '</td>'
      + '</tr>';
  });
  $('#rqstTableBody').html(html);

  /* AIRLINE 응답 버튼 */
  $('.btn-respond').off('click').on('click', function() {
    $('#respRqstSn').val($(this).data('sn'));
    $('#respRqstCnView').text($(this).data('cn'));
    $('#newRespCn').val('');
    new bootstrap.Modal(document.getElementById('respModal')).show();
  });
  /* KOTSA 삭제 */
  $('.btn-del-rqst').off('click').on('click', function() {
    var sn = $(this).data('sn');
    if (!confirm('요청을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/rqst/' + sn, type: 'DELETE' })
      .done(function() { loadRqsts(); })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '삭제 실패'); });
  });
}

/* ── 검증기관 평가 탭 ───────────────────────────────────────── */
var sampleEvals = [
  { vrfcnInstId:'VI001', vrfcnInstNm:'한국검증원', qltyCd:'A', evlCn:'전반적으로 배출량 보고 품질이 우수합니다.', evlDt:'2026-04-22' }
];
var currentEval = null;

function loadEvals() {
  $.get('/api/er/oom/' + encodeURIComponent(oomId) + '/eval')
    .done(function(res) { renderEvals((res && res.data) ? res.data : sampleEvals); })
    .fail(function()    { renderEvals(sampleEvals); });
}

function renderEvals(list) {
  if (!list || !list.length) {
    var addHtml = (userRole === 'VERIFIER')
      ? '<div class="alert alert-info small mt-3">아직 등록된 평가가 없습니다. 위 [평가 저장] 버튼으로 평가를 등록하세요.</div>'
      : '<div class="text-muted small py-3 text-center">등록된 평가가 없습니다.</div>';
    renderEvalForm(null);
    $('#evalArea').html(addHtml + $('#evalArea').html());
    return;
  }
  var tableHtml = '<div class="table-responsive mb-3">'
    + '<table class="table table-hover table-sm mb-0 table-icas" aria-label="검증기관 평가">'
    + '<thead><tr><th class="ps-3">검증기관</th><th style="width:80px;">품질등급</th><th>평가 내용</th><th style="width:90px;">평가일</th></tr></thead>'
    + '<tbody>';
  list.forEach(function(row) {
    tableHtml += '<tr>'
      + '<td class="ps-3 small fw-semibold">' + escHtml(row.vrfcnInstNm) + '</td>'
      + '<td><span class="badge ' + (row.qltyCd === 'A' ? 'bg-success' : row.qltyCd === 'B' ? 'bg-primary' : 'bg-warning text-dark') + '">' + escHtml(row.qltyCd) + '</span></td>'
      + '<td class="small">' + escHtml(row.evlCn) + '</td>'
      + '<td class="small text-muted">' + escHtml(row.evlDt) + '</td>'
      + '</tr>';
  });
  tableHtml += '</tbody></table></div>';

  /* VERIFIER 는 본인 평가 편집 폼도 노출 */
  var formHtml = '';
  if (userRole === 'VERIFIER') {
    currentEval = list[0]; // 본인 기관 평가 (서버에서 필터해 옴)
    formHtml = renderEvalFormHtml(currentEval);
  }
  $('#evalArea').html(tableHtml + formHtml);
}

function renderEvalFormHtml(ev) {
  if (userRole !== 'VERIFIER') return '';
  return '<div class="card border-0 bg-light p-3">'
    + '<div class="fw-semibold small mb-2" style="color:#0F2C72;">내 기관 평가 편집</div>'
    + '<div class="mb-3">'
    + '<label class="form-label small fw-semibold">품질등급 <span class="text-danger">*</span></label>'
    + '<select id="evalQltyCd" class="form-select form-select-sm" style="width:120px;">'
    + '<option value="A"' + (ev && ev.qltyCd==='A'?' selected':'') + '>A — 우수</option>'
    + '<option value="B"' + (ev && ev.qltyCd==='B'?' selected':'') + '>B — 양호</option>'
    + '<option value="C"' + (ev && ev.qltyCd==='C'?' selected':'') + '>C — 보통</option>'
    + '<option value="D"' + (ev && ev.qltyCd==='D'?' selected':'') + '>D — 미흡</option>'
    + '</select></div>'
    + '<div class="mb-0">'
    + '<label class="form-label small fw-semibold">평가 내용 <span class="text-danger">*</span></label>'
    + '<textarea id="evalEvlCn" class="form-control form-control-sm" rows="4" maxlength="2000">' + (ev ? escHtml(ev.evlCn) : '') + '</textarea>'
    + '</div></div>';
}
function renderEvalForm(ev) {
  $('#evalArea').html(renderEvalFormHtml(ev));
}

/* ── 이벤트 바인딩 ──────────────────────────────────────────── */
$(function() {
  /* 탭 진입 시 지연 로드 */
  loadMaster();

  $('#tab-item').on('shown.bs.tab', function() { loadItems(); });
  $('#tab-rqst').on('shown.bs.tab', function()  { loadRqsts(); });
  $('#tab-eval').on('shown.bs.tab', function()  { loadEvals(); });
  loadItems(); /* 기본 탭 */

  /* ── 검토항목 추가 (KOTSA) */
  $('#btnAddItem').on('click', function() {
    $('#editItemNo').val('');
    $('#editItemNm').val('');
    $('#editItemRm').val('');
    $('#itemModalLabel').text('검토항목 추가');
    new bootstrap.Modal(document.getElementById('itemModal')).show();
  });

  $('#btnSaveItem').on('click', function() {
    var nm = $.trim($('#editItemNm').val());
    if (!nm) { IcasAlert.warning('항목명은 필수입니다.'); return; }
    var no  = $('#editItemNo').val();
    var url = '/api/er/oom/' + encodeURIComponent(oomId) + '/item' + (no ? '/' + no : '');
    var method = no ? 'PUT' : 'POST';
    var payload = { itemNm: nm, rmrk: $.trim($('#editItemRm').val()) };
    $.ajax({ url: url, type: method, contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function() {
        bootstrap.Modal.getInstance(document.getElementById('itemModal')).hide();
        loadItems();
      })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
  });

  /* ── 정량검증 실행 (탭 내 버튼) */
  $('#btnRunQuant').on('click', function() { runQuant(); });

  /* ── 추가요청 등록 (KOTSA) */
  $('#btnAddRqst').on('click', function() {
    $('#newRqstCn').val('');
    new bootstrap.Modal(document.getElementById('rqstModal')).show();
  });

  $('#btnConfirmRqst').on('click', function() {
    var cn = $.trim($('#newRqstCn').val());
    if (!cn) { IcasAlert.warning('요청 내용은 필수입니다.'); return; }
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/rqst', type: 'POST',
             contentType: 'application/json', data: JSON.stringify({ rqstCn: cn }) })
      .done(function() {
        bootstrap.Modal.getInstance(document.getElementById('rqstModal')).hide();
        loadRqsts();
      })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '등록 실패'); });
  });

  /* ── 요청 응답 (AIRLINE) */
  $('#btnConfirmResp').on('click', function() {
    var cn = $.trim($('#newRespCn').val());
    var sn = $('#respRqstSn').val();
    if (!cn) { IcasAlert.warning('응답 내용은 필수입니다.'); return; }
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/rqst/' + sn + '/respond',
             type: 'PUT', contentType: 'application/json', data: JSON.stringify({ respCn: cn }) })
      .done(function() {
        bootstrap.Modal.getInstance(document.getElementById('respModal')).hide();
        loadRqsts();
      })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '응답 저장 실패'); });
  });

  /* ── 검증기관 평가 저장 (VERIFIER) */
  $('#btnSaveEval').on('click', function() {
    var qltyCd = $('#evalQltyCd').val();
    var evlCn  = $.trim($('#evalEvlCn').val());
    if (!evlCn) { IcasAlert.warning('평가 내용은 필수입니다.'); return; }
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/eval', type: 'PUT',
             contentType: 'application/json', data: JSON.stringify({ qltyCd: qltyCd, evlCn: evlCn }) })
      .done(function() {
        IcasAlert.success('평가가 저장되었습니다.');
        loadEvals();
      })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '저장 실패'); });
  });

  /* ── 확정 (KOTSA) */
  $('#btnConfirmFinalize').on('click', function() {
    var rslt = $('input[name="finalRslt"]:checked').val();
    if (!rslt) { IcasAlert.warning('판정 결과를 선택하세요.'); return; }
    if (!confirm(rslt + '(으)로 확정하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/er/oom/' + encodeURIComponent(oomId) + '/finalize', type: 'POST',
             contentType: 'application/json', data: JSON.stringify({ rsltCd: rslt }) })
      .done(function() {
        bootstrap.Modal.getInstance(document.getElementById('finalizeModal')).hide();
        location.reload();
      })
      .fail(function(xhr) { IcasAlert.error((xhr.responseJSON && xhr.responseJSON.message) || '확정 실패'); });
  });
});
</script>
</body>
</html>
