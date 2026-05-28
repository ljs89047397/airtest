<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>검증보고서(VR) 상세 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.status-badge { font-size: 0.72rem; padding: 3px 8px; border-radius: 4px; font-weight: 600; }
/* 라이프사이클 스텝퍼 */
.lifecycle-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.lc-step { display: flex; align-items: center; gap: 6px; font-size: 0.8rem; color: #9ca3af; }
.lc-step.done  { color: #16a34a; }
.lc-step.active { color: #0F2C72; font-weight: 700; }
.lc-step .dot { width: 24px; height: 24px; border-radius: 50%; background: #e5e7eb; display: flex; align-items: center; justify-content: center; font-size: 0.7rem; flex-shrink: 0; }
.lc-step.done  .dot { background: #16a34a; color: white; }
.lc-step.active .dot { background: #0F2C72; color: white; }
.lc-arrow { color: #d1d5db; font-size: 0.9rem; margin: 0 4px; }
/* 탭 */
.nav-tabs .nav-link { color: #6b7280; font-size: 0.85rem; }
.nav-tabs .nav-link.active { color: #0F2C72; font-weight: 600; border-bottom: 2px solid #0F2C72; }
/* 정보 그리드 */
.info-label { font-size: 0.78rem; color: #6b7280; font-weight: 500; }
.info-value { font-size: 0.88rem; font-weight: 600; }
/* 테이블 공통 */
.table-icas thead th { background: #0F2C72; color: white; font-size: 0.8rem; font-weight: 500; border: none; }
.table-icas tbody tr:hover { background: #f8f9ff; }
/* CCR 경고 배너 */
.alert-ccr { background: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 4px; }
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
        <a href="/vr/list" class="btn btn-sm btn-outline-secondary me-3">
          <i class="bi bi-arrow-left me-1"></i>목록으로
        </a>
        <span class="fw-bold" style="color:#0F2C72; font-size:1rem;">&#9989; 검증보고서(VR) 상세</span>
        <nav aria-label="breadcrumb" class="d-inline ms-3">
          <ol class="breadcrumb mb-0 small d-inline-flex">
            <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
            <li class="breadcrumb-item"><a href="/vr/list" class="text-decoration-none">검증보고서(VR)</a></li>
            <li class="breadcrumb-item active" id="bcVrId">-</li>
          </ol>
        </nav>
      </div>
      <!-- 액션 버튼 (상태별 노출) -->
      <div id="actionArea" class="d-flex gap-2"></div>
    </div>
  </div>

  <!-- 라이프사이클 스텝퍼 -->
  <div class="lifecycle-bar px-4 py-2 d-flex align-items-center gap-1">
    <div class="lc-step" id="lc-DRAFT">
      <div class="dot">1</div><span>작성중 (DRAFT)</span>
    </div>
    <span class="lc-arrow">&#8250;</span>
    <div class="lc-step" id="lc-SBMTD">
      <div class="dot">2</div><span>제출됨 (SBMTD)</span>
    </div>
    <span class="lc-arrow">&#8250;</span>
    <div class="lc-step" id="lc-RCMDD">
      <div class="dot">3</div><span>권고 (RCMDD)</span>
    </div>
    <span class="lc-arrow">&#8250;</span>
    <div class="lc-step" id="lc-APRVD">
      <div class="dot">4</div><span>승인 (APRVD)</span>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- CCR 만료 경고 배너 (숨김 상태로 시작) -->
    <div id="ccrWarnBanner" class="alert-ccr p-3 mb-3 d-none">
      <i class="bi bi-exclamation-triangle-fill me-2 text-warning"></i>
      <strong>CCR 인증 만료 경고:</strong>
      검증기관의 ICAO CCR 공인인증이 만료되어 VR 제출이 불가합니다. 검증기관 인증 갱신 후 다시 시도하십시오.
    </div>

    <!-- 부적합 미해결 경고 배너 (숨김 상태로 시작) -->
    <div id="ncnfrmWarnBanner" class="alert alert-warning p-3 mb-3 d-none" role="alert">
      <i class="bi bi-exclamation-circle-fill me-2"></i>
      <strong>미해결 부적합 존재:</strong>
      미해결 부적합 항목이 있어 최종 의견 <code>REASONABLE</code> 저장이 차단됩니다.
      결론 탭에서 검토하십시오.
    </div>

    <!-- 기본정보 카드 -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header py-2 px-3" style="background:#f8fafc; border-bottom:1px solid #e5e7eb;">
        <span class="fw-semibold small" style="color:#0F2C72;">기본 정보</span>
      </div>
      <div class="card-body py-3 px-4">
        <div class="row g-3" id="basicInfoRow">
          <div class="col-md-2">
            <div class="info-label">보고연도</div>
            <div class="info-value" id="inf-rprtYr">-</div>
          </div>
          <div class="col-md-3">
            <div class="info-label">운영사</div>
            <div class="info-value" id="inf-oprtrNm">-</div>
          </div>
          <div class="col-md-2">
            <div class="info-label">VR 유형</div>
            <div class="info-value" id="inf-vrTypeCd">-</div>
          </div>
          <div class="col-md-3">
            <div class="info-label">검증기관</div>
            <div class="info-value" id="inf-vrfcnInstNm">-</div>
          </div>
          <div class="col-md-1">
            <div class="info-label">버전</div>
            <div class="info-value" id="inf-vrVer">-</div>
          </div>
          <div class="col-md-1">
            <div class="info-label">상태</div>
            <div id="inf-status"></div>
          </div>
          <div class="col-md-2">
            <div class="info-label">검증의견</div>
            <div class="info-value" id="inf-finalOpnn">-</div>
          </div>
          <div class="col-md-2">
            <div class="info-label">제출일</div>
            <div class="info-value" id="inf-sbmtDt">-</div>
          </div>
          <div class="col-md-2">
            <div class="info-label">승인일</div>
            <div class="info-value" id="inf-aprvDt">-</div>
          </div>
          <div class="col-md-3">
            <div class="info-label">연계 ER</div>
            <div class="info-value" id="inf-erId">-</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 탭 7개 -->
    <div class="card border-0 shadow-sm">
      <div class="card-header p-0" style="border-bottom:1px solid #e5e7eb; background:white;">
        <ul class="nav nav-tabs border-0 px-3 pt-2" id="vrTabs" role="tablist">
          <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#tab-scope">&#128269; 검증범위</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tab-team">&#128100; 검증팀</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tab-time">&#128336; 시간투입</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tab-inpt">&#128196; 입력정보</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tab-prcdr">&#128203; 검증절차</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tab-ncnfrm">&#9888; 부적합</a></li>
          <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#tab-cncls">&#10003; 결론</a></li>
        </ul>
      </div>
      <div class="card-body p-4">
        <div class="tab-content" id="vrTabContent">

          <!-- ===== TAB: 검증범위 ===== -->
          <div class="tab-pane fade show active" id="tab-scope">
            <div class="row g-3 mb-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">검증기관명 <span class="text-danger">*</span></label>
                <input type="text" id="scope-vrfcnInstNm" class="form-control form-control-sm" maxlength="200" placeholder="검증기관 정식 명칭">
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">검증기관 주소</label>
                <input type="text" id="scope-vrfcnInstAddr" class="form-control form-control-sm" maxlength="300" placeholder="기관 주소">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">검증 유형</label>
                <select id="scope-vrfcnTypeCd" class="form-select form-select-sm">
                  <option value="">선택</option>
                  <option value="ER">ER 검증</option>
                  <option value="EUCR">EUCR 검증</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">검증 기준</label>
                <input type="text" id="scope-vrfcnStnd" class="form-control form-control-sm" maxlength="200" placeholder="예: ISO 14064-3">
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">검증 범위 설명</label>
                <textarea id="scope-scopeCn" class="form-control form-control-sm" rows="4" maxlength="2000" placeholder="검증 대상 항공사 및 배출량 범위를 기재하십시오."></textarea>
              </div>
            </div>
            <div class="text-end">
              <button id="btn-scope-save" class="btn btn-sm" style="background:#0F2C72;color:white;">
                <i class="bi bi-save me-1"></i>저장
              </button>
            </div>
          </div>

          <!-- ===== TAB: 검증팀 ===== -->
          <div class="tab-pane fade" id="tab-team">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="small text-muted">검증팀 구성원 목록 — 리더 연속검증 3년 초과 시 OoM-check 경고</span>
              <button id="btn-team-add" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-person-plus me-1"></i>구성원 추가
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-sm table-icas mb-0">
                <thead>
                  <tr>
                    <th class="ps-3">이름</th>
                    <th>역할</th>
                    <th>자격 상세</th>
                    <th>연속검증(년)</th>
                    <th style="width:80px;">액션</th>
                  </tr>
                </thead>
                <tbody id="teamListBody">
                  <tr><td colspan="5" class="text-center py-3 text-muted small">데이터를 불러오는 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- ===== TAB: 시간투입 ===== -->
          <div class="tab-pane fade" id="tab-time">
            <div class="row g-3 mb-3" style="max-width:480px;">
              <div class="col-md-6">
                <label class="form-label small fw-semibold">현장 검증 시간(h)</label>
                <input type="number" id="time-onsiteHrs" class="form-control form-control-sm" min="0" step="0.5" placeholder="0">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">원격 검증 시간(h)</label>
                <input type="number" id="time-offsiteHrs" class="form-control form-control-sm" min="0" step="0.5" placeholder="0">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">합계 시간(h)</label>
                <input type="number" id="time-totalHrs" class="form-control form-control-sm" readonly placeholder="자동 계산">
              </div>
            </div>
            <div class="text-end">
              <button id="btn-time-save" class="btn btn-sm" style="background:#0F2C72;color:white;">
                <i class="bi bi-save me-1"></i>저장
              </button>
            </div>
          </div>

          <!-- ===== TAB: 입력정보 ===== -->
          <div class="tab-pane fade" id="tab-inpt">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="small text-muted">검증에 활용된 운영사 제공 자료 목록</span>
              <button id="btn-inpt-add" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-file-plus me-1"></i>자료 추가
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-sm table-icas mb-0">
                <thead>
                  <tr>
                    <th class="ps-3">문서명</th>
                    <th>구분</th>
                    <th>파일</th>
                    <th style="width:80px;">액션</th>
                  </tr>
                </thead>
                <tbody id="inptListBody">
                  <tr><td colspan="4" class="text-center py-3 text-muted small">데이터를 불러오는 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- ===== TAB: 검증절차 ===== -->
          <div class="tab-pane fade" id="tab-prcdr">
            <div class="row g-3 mb-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">전략적 분석</label>
                <textarea id="prcdr-strgAnlys" class="form-control form-control-sm" rows="3" maxlength="3000" placeholder="배출량 흐름, 데이터 시스템, 내부통제 등 전략적 분석 내용"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">위험 평가</label>
                <textarea id="prcdr-riskEval" class="form-control form-control-sm" rows="3" maxlength="3000" placeholder="내재위험, 통제위험, 적발위험 등 위험 평가 결과"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">샘플링 활동</label>
                <textarea id="prcdr-smplngActv" class="form-control form-control-sm" rows="3" maxlength="3000" placeholder="샘플링 접근법 및 활동 기술"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">샘플링 결과</label>
                <textarea id="prcdr-smplngRslt" class="form-control form-control-sm" rows="3" maxlength="3000" placeholder="샘플링 수행 결과 및 발견 사항"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">EMP 준수 여부</label>
                <textarea id="prcdr-empCompl" class="form-control form-control-sm" rows="2" maxlength="2000" placeholder="EMP 준수 검토 결과"></textarea>
              </div>
            </div>
            <div class="text-end">
              <button id="btn-prcdr-save" class="btn btn-sm" style="background:#0F2C72;color:white;">
                <i class="bi bi-save me-1"></i>저장
              </button>
            </div>
          </div>

          <!-- ===== TAB: 부적합 ===== -->
          <div class="tab-pane fade" id="tab-ncnfrm">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="small text-muted">
                <span id="ncnfrmUnresolvedBadge" class="badge bg-danger me-2 d-none">미해결</span>
                부적합·허위진술 항목 목록
              </span>
              <button id="btn-ncnfrm-add" class="btn btn-sm btn-outline-danger">
                <i class="bi bi-exclamation-triangle me-1"></i>부적합 등록
              </button>
            </div>
            <div class="table-responsive">
              <table class="table table-sm table-icas mb-0">
                <thead>
                  <tr>
                    <th class="ps-3" style="width:60px;">No</th>
                    <th>구분</th>
                    <th>설명</th>
                    <th>해결 내용</th>
                    <th>해결일</th>
                    <th style="width:100px;">액션</th>
                  </tr>
                </thead>
                <tbody id="ncnfrmListBody">
                  <tr><td colspan="6" class="text-center py-3 text-muted small">데이터를 불러오는 중...</td></tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- ===== TAB: 결론 ===== -->
          <div class="tab-pane fade" id="tab-cncls">
            <div class="row g-3 mb-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">데이터 품질 평가</label>
                <textarea id="cncls-dataQltyEval" class="form-control form-control-sm" rows="3" maxlength="2000" placeholder="데이터 품질 평가 내용"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">중요성 평가</label>
                <textarea id="cncls-mtrltyEval" class="form-control form-control-sm" rows="3" maxlength="2000" placeholder="중요성 수준 및 허위진술 중요성 평가"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">ER 결론</label>
                <textarea id="cncls-erCncls" class="form-control form-control-sm" rows="3" maxlength="2000" placeholder="ER 검증 결론 기술"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">EUCR 결론</label>
                <textarea id="cncls-eucrCncls" class="form-control form-control-sm" rows="2" maxlength="2000" placeholder="EUCR 검증 결론 기술 (해당 시)"></textarea>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">판단 내용</label>
                <textarea id="cncls-judgCn" class="form-control form-control-sm" rows="3" maxlength="2000" placeholder="종합 판단 및 의견 기술"></textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">독립검토자 검토 의견</label>
                <textarea id="cncls-indepReviewCn" class="form-control form-control-sm" rows="2" maxlength="1000" placeholder="독립검토자 의견"></textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">독립검토자 성명</label>
                <input type="text" id="cncls-indepReviewUserNm" class="form-control form-control-sm" maxlength="100" placeholder="독립검토자 성명">
              </div>
              <div class="col-md-4">
                <label class="form-label small fw-semibold">최종 검증 의견 <span class="text-danger">*</span></label>
                <select id="cncls-finalOpnnCd" class="form-select form-select-sm">
                  <option value="">선택</option>
                  <option value="REASONABLE">합리적 확신 (REASONABLE)</option>
                  <option value="LIMITED">제한적 확신 (LIMITED)</option>
                  <option value="QUALIFIED">한정 의견 (QUALIFIED)</option>
                  <option value="ADVERSE">부정적 의견 (ADVERSE)</option>
                </select>
                <div class="form-text text-warning small d-none" id="cncls-reasonableWarn">
                  <i class="bi bi-exclamation-triangle me-1"></i>미해결 부적합이 있어 REASONABLE 선택 시 저장이 차단됩니다.
                </div>
              </div>
            </div>
            <div class="text-end">
              <button id="btn-cncls-save" class="btn btn-sm" style="background:#0F2C72;color:white;">
                <i class="bi bi-save me-1"></i>저장
              </button>
            </div>
          </div>

        </div><!-- /tab-content -->
      </div>
    </div>

  </div><!-- /container-fluid -->
</div><!-- /main-area -->

<!-- ===== 구성원 추가 모달 ===== -->
<div class="modal fade" id="teamModal" tabindex="-1" aria-labelledby="teamModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header py-2 px-3">
        <h6 class="modal-title" id="teamModalLabel">검증팀 구성원 등록</h6>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">성명 <span class="text-danger">*</span></label>
          <input type="text" id="teamForm-userNm" class="form-control form-control-sm" maxlength="100">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">역할 <span class="text-danger">*</span></label>
          <select id="teamForm-roleCd" class="form-select form-select-sm">
            <option value="">선택</option>
            <option value="LEAD">리더 (LEAD)</option>
            <option value="MEMBER">구성원 (MEMBER)</option>
            <option value="INDEP_REVIEWER">독립검토자 (INDEP_REVIEWER)</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">자격 상세</label>
          <input type="text" id="teamForm-accrdDtl" class="form-control form-control-sm" maxlength="300" placeholder="자격증, 교육 이력 등">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">연속 검증 횟수</label>
          <input type="number" id="teamForm-conscutv" class="form-control form-control-sm" min="0" max="99" value="0">
          <div class="form-text text-warning small d-none" id="consecWarn">3년 초과 — OoM-check 에서 경고 발생</div>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btn-team-save" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 입력자료 추가 모달 ===== -->
<div class="modal fade" id="inptModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header py-2 px-3">
        <h6 class="modal-title">입력자료 등록</h6>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label small fw-semibold">문서명 <span class="text-danger">*</span></label>
          <input type="text" id="inptForm-docNm" class="form-control form-control-sm" maxlength="300">
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">문서 구분</label>
          <select id="inptForm-docSeCd" class="form-select form-select-sm">
            <option value="">선택</option>
            <option value="FUEL_LOG">연료 기록</option>
            <option value="FLIGHT_LOG">비행 기록</option>
            <option value="INTERNAL_AUDIT">내부 감사</option>
            <option value="OTHER">기타</option>
          </select>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btn-inpt-save" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== 부적합 등록 모달 ===== -->
<div class="modal fade" id="ncnfrmModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header py-2 px-3">
        <h6 class="modal-title" id="ncnfrmModalTitle">부적합 등록</h6>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="ncnfrmForm-itemNo">
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label small fw-semibold">구분 <span class="text-danger">*</span></label>
            <select id="ncnfrmForm-seCd" class="form-select form-select-sm">
              <option value="">선택</option>
              <option value="MINOR">경미 (MINOR)</option>
              <option value="MAJOR">중대 (MAJOR)</option>
              <option value="MISSTATEMENT">허위진술 (MISSTATEMENT)</option>
            </select>
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">설명 <span class="text-danger">*</span></label>
            <textarea id="ncnfrmForm-desc" class="form-control form-control-sm" rows="3" maxlength="2000"></textarea>
          </div>
          <div class="col-12">
            <label class="form-label small fw-semibold">해결 내용</label>
            <textarea id="ncnfrmForm-resolDesc" class="form-control form-control-sm" rows="2" maxlength="2000"></textarea>
          </div>
          <div class="col-md-4">
            <label class="form-label small fw-semibold">해결일</label>
            <input type="date" id="ncnfrmForm-resolDt" class="form-control form-control-sm">
          </div>
        </div>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btn-ncnfrm-save" class="btn btn-sm" style="background:#0F2C72;color:white;">저장</button>
      </div>
    </div>
  </div>
</div>

<!-- 반려 모달 -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header py-2 px-3">
        <h6 class="modal-title">VR 반려</h6>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <label class="form-label small fw-semibold">반려 사유 <span class="text-danger">*</span></label>
        <textarea id="rejectRsn" class="form-control form-control-sm" rows="3" maxlength="1000" placeholder="반려 사유를 입력하십시오."></textarea>
      </div>
      <div class="modal-footer py-2">
        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
        <button type="button" id="btn-reject-confirm" class="btn btn-sm btn-danger">반려 처리</button>
      </div>
    </div>
  </div>
</div>

<!-- 토스트 컨테이너 -->
<div id="toastContainer" class="position-fixed top-0 end-0 p-3" style="z-index:9999;"></div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="/resources/js/common/icas-alert.js"></script>
<script>/* ── 세션 권한 주입 (서버사이드 EL) ── */
var __OGNZ_SE_CD = '${sessionScope.ognzSeCd}';</script>
<script>
/* ========================================================
   VR 상세 화면 공통 유틸
   ======================================================== */

// URL에서 vrId 추출 (/vr/VR202600001)
const pathParts = location.pathname.split('/').filter(Boolean);
const VR_ID = pathParts[1] || '';

/* ===== 상태 매핑 ===== */
const STATUS_MAP = {
  'DRAFT':  ['bg-secondary', '작성중'],
  'SBMTD':  ['bg-primary',   '제출됨'],
  'RCMDD':  ['bg-warning text-dark', '권고'],
  'APRVD':  ['bg-success',   '승인']
};
const LC_ORDER = ['DRAFT', 'SBMTD', 'RCMDD', 'APRVD'];

/* ===== HTML 이스케이프 ===== */
function esc(s) {
  if (s == null) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

/* ===== 토스트 ===== */
function showToast(msg, type) {
  type = type || 'danger';
  const id = 'toast_' + Date.now();
  const bgMap = { success: 'bg-success', danger: 'bg-danger', warning: 'bg-warning text-dark', info: 'bg-primary' };
  const bgCls = bgMap[type] || 'bg-danger';
  const html = '<div id="' + id + '" class="toast align-items-center text-white ' + bgCls + ' border-0" role="alert">'
    + '<div class="d-flex"><div class="toast-body small">' + esc(msg) + '</div>'
    + '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div></div>';
  $('#toastContainer').append(html);
  const el = document.getElementById(id);
  new bootstrap.Toast(el, { delay: type === 'success' ? 1500 : 4000 }).show();
  el.addEventListener('hidden.bs.toast', function() { el.remove(); });
}

/* ===== 상태 배지 ===== */
function renderBadge(cd) {
  if (!cd) return '<span class="badge status-badge bg-light text-muted border">-</span>';
  const entry = STATUS_MAP[cd] || ['bg-secondary', cd];
  return '<span class="badge status-badge ' + entry[0] + '">' + esc(entry[1]) + '</span>';
}

/* ===== 라이프사이클 스텝퍼 업데이트 ===== */
function updateLifecycle(stCd) {
  const activeIdx = LC_ORDER.indexOf(stCd);
  LC_ORDER.forEach(function(cd, idx) {
    const el = document.getElementById('lc-' + cd);
    if (!el) return;
    el.classList.remove('done', 'active');
    if (idx < activeIdx)  el.classList.add('done');
    else if (idx === activeIdx) el.classList.add('active');
  });
}

/* ===== 액션 버튼 렌더링 (상태 + ognzSeCd 역할 기반) ===== */
function renderActionButtons(stCd) {
  let html = '';

  /* 미인증 세션이면 버튼 전체 숨김 — 화면(조회)은 유지 */
  const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
  if (!ognzSeCd) {
    $('#actionArea').html('');
    bindActionButtons(stCd);
    return;
  }

  /* 제출 — DRAFT, VERIFIER 본인 소유 */
  if (stCd === 'DRAFT' && ognzSeCd === 'VERIFIER') {
    html += '<button id="btn-submit" class="btn btn-sm btn-primary"><i class="bi bi-send me-1"></i>제출</button>';
    html += '<button id="btn-delete" class="btn btn-sm btn-outline-danger ms-1"><i class="bi bi-trash me-1"></i>삭제</button>';
  }
  /* 권고·반려 — SBMTD, KOTSA */
  if (stCd === 'SBMTD' && ognzSeCd === 'KOTSA') {
    html += '<button id="btn-recommend" class="btn btn-sm btn-success"><i class="bi bi-check-circle me-1"></i>권고</button>';
    html += '<button id="btn-reject" class="btn btn-sm btn-outline-danger ms-1"><i class="bi bi-x-circle me-1"></i>반려</button>';
  }
  /* 승인 — RCMDD, MOLIT */
  if (stCd === 'RCMDD' && ognzSeCd === 'MOLIT') {
    html += '<button id="btn-approve" class="btn btn-sm btn-success"><i class="bi bi-check2-all me-1"></i>승인</button>';
  }
  $('#actionArea').html(html);
  bindActionButtons(stCd);
}

/* ===== 액션 버튼 이벤트 바인딩 ===== */
function bindActionButtons(stCd) {
  // 제출
  $('#btn-submit').off('click').on('click', function() {
    // CCR 만료 여부 사전 확인
    $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/ccr-check')
      .done(function(res) {
        const expired = res && res.data && res.data.expired;
        if (expired) {
          $('#ccrWarnBanner').removeClass('d-none');
          showToast('검증기관 CCR 인증이 만료되어 제출할 수 없습니다.', 'danger');
          return;
        }
        confirmAndAct('제출 확인', 'VR 을 제출하시겠습니까? 제출 후에는 수정할 수 없습니다.', '제출', function() {
          doAction('/api/vr/' + encodeURIComponent(VR_ID) + '/submit', 'VR 이 제출되었습니다.');
        });
      })
      .fail(function() {
        // CCR 엔드포인트 없으면 경고만 표시 후 진행
        confirmAndAct('제출 확인', 'VR 을 제출하시겠습니까?', '제출', function() {
          doAction('/api/vr/' + encodeURIComponent(VR_ID) + '/submit', 'VR 이 제출되었습니다.');
        });
      });
  });

  // 권고
  $('#btn-recommend').off('click').on('click', function() {
    confirmAndAct('권고 확인', 'VR 을 권고 처리하시겠습니까?', '권고', function() {
      doAction('/api/vr/' + encodeURIComponent(VR_ID) + '/recommend', 'VR 권고 처리가 완료되었습니다.');
    });
  });

  // 승인
  $('#btn-approve').off('click').on('click', function() {
    confirmAndAct('승인 확인', 'VR 을 최종 승인하시겠습니까?', '승인', function() {
      doAction('/api/vr/' + encodeURIComponent(VR_ID) + '/approve', 'VR 이 승인되었습니다.');
    });
  });

  // 반려
  $('#btn-reject').off('click').on('click', function() {
    $('#rejectRsn').val('');
    new bootstrap.Modal(document.getElementById('rejectModal')).show();
  });
  $('#btn-reject-confirm').off('click').on('click', function() {
    const rsn = $('#rejectRsn').val().trim();
    if (!rsn) { showToast('반려 사유를 입력하십시오.', 'warning'); return; }
    bootstrap.Modal.getInstance(document.getElementById('rejectModal')).hide();
    $.ajax({
      url: '/api/vr/' + encodeURIComponent(VR_ID) + '/reject',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ rjctRsn: rsn })
    }).done(function() {
      showToast('VR 이 반려되었습니다.', 'success');
      setTimeout(function() { location.reload(); }, 1200);
    }).fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
  });

  // 삭제
  $('#btn-delete').off('click').on('click', function() {
    confirmAndAct('삭제 확인', 'VR 을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.', '삭제', function() {
      $.ajax({ url: '/api/vr/' + encodeURIComponent(VR_ID), type: 'DELETE' })
        .done(function() {
          showToast('삭제되었습니다.', 'success');
          setTimeout(function() { location.href = '/vr/list'; }, 1200);
        })
        .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
    });
  });
}

function confirmAndAct(title, msg, okText, onOk) {
  IcasAlert.confirm('[' + title + ']\n' + msg, onOk);
}

function doAction(url, successMsg) {
  $.post(url, {})
    .done(function() {
      showToast(successMsg, 'success');
      setTimeout(function() { location.reload(); }, 1200);
    })
    .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
}

function extractErrMsg(xhr) {
  try { return JSON.parse(xhr.responseText).message || '오류가 발생했습니다.'; } catch(e) { return '오류가 발생했습니다.'; }
}

/* ========================================================
   기본정보 로드
   ======================================================== */
let vrData = null;
let hasUnresolvedNcnfrm = false;

function loadVrDetail() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID))
    .done(function(res) {
      vrData = (res && res.data) ? res.data : res;
      renderBasicInfo(vrData);
    })
    .fail(function() {
      showToast('VR 정보를 불러오지 못했습니다.', 'danger');
    });
}

function renderBasicInfo(d) {
  $('#bcVrId').text(esc(d.vrId || VR_ID));
  $('#inf-rprtYr').text(esc(d.rprtYr));
  $('#inf-oprtrNm').text(esc(d.oprtrNm));
  $('#inf-vrTypeCd').html('<span class="badge bg-light text-dark border">' + esc(d.vrTypeCd) + '</span>');
  $('#inf-vrfcnInstNm').text(esc(d.vrfcnInstNm));
  $('#inf-vrVer').text('v' + esc(d.vrVer));
  $('#inf-status').html(renderBadge(d.vrStCd));
  $('#inf-finalOpnn').text(esc(d.finalOpnnCd) || '-');
  $('#inf-sbmtDt').text(esc(d.sbmtDt) || '-');
  $('#inf-aprvDt').text(esc(d.aprvDt) || '-');
  $('#inf-erId').html(d.erId
    ? '<a href="/er/detail?id=' + esc(d.erId) + '" class="text-decoration-none">' + esc(d.erId) + '</a>'
    : '-');
  updateLifecycle(d.vrStCd);
  renderActionButtons(d.vrStCd);
  applyVrEditGuard(d.vrStCd);
}

/* ===== 편집 권한 가드: 저장 버튼 표시 여부 제어 =====
 * VERIFIER + DRAFT 상태일 때만 저장/추가 버튼 활성화
 */
function applyVrEditGuard(vrStCd) {
  const ognzSeCd = (typeof __OGNZ_SE_CD !== 'undefined') ? __OGNZ_SE_CD : '';
  const canEdit = ognzSeCd === 'VERIFIER' && vrStCd === 'DRAFT';
  // scope, time, prcdr, cncls 저장 버튼
  $('#btn-scope-save, #btn-time-save, #btn-prcdr-save, #btn-cncls-save').toggle(canEdit);
  // team/inpt/ncnfrm 추가 버튼
  $('#btn-team-add, #btn-inpt-add, #btn-ncnfrm-add').toggle(canEdit);
  // scope/time/prcdr/cncls 폼 필드 읽기전용 제어
  const rdOnly = !canEdit;
  $('#scope-vrfcnInstNm, #scope-vrfcnInstAddr, #scope-vrfcnTypeCd, #scope-vrfcnStnd, #scope-scopeCn').prop('disabled', rdOnly);
  $('#time-onsiteHrs, #time-offsiteHrs').prop('disabled', rdOnly);
  $('#prcdr-strgAnlys, #prcdr-riskEval, #prcdr-smplngActv, #prcdr-smplngRslt, #prcdr-empCompl').prop('disabled', rdOnly);
  $('#cncls-dataQltyEval, #cncls-mtrltyEval, #cncls-erCncls, #cncls-eucrCncls, #cncls-judgCn, #cncls-indepReviewCn, #cncls-indepReviewUserNm, #cncls-finalOpnnCd').prop('disabled', rdOnly);
}

/* ========================================================
   탭 데이터 로드 (탭 활성화 시)
   ======================================================== */
$('#vrTabs a[data-bs-toggle="tab"]').on('shown.bs.tab', function(e) {
  const target = e.target.getAttribute('href');
  if (target === '#tab-team')   loadTeam();
  if (target === '#tab-time')   loadTime();
  if (target === '#tab-inpt')   loadInpt();
  if (target === '#tab-ncnfrm') loadNcnfrm();
  if (target === '#tab-cncls')  loadCncls();
  if (target === '#tab-scope')  loadScope();
  if (target === '#tab-prcdr')  loadPrcdr();
});

/* ========================================================
   범위(scope)
   ======================================================== */
function loadScope() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/scope')
    .done(function(res) {
      const d = (res && res.data) ? res.data : res;
      if (!d) return;
      $('#scope-vrfcnInstNm').val(d.vrfcnInstNm || '');
      $('#scope-vrfcnInstAddr').val(d.vrfcnInstAddr || '');
      $('#scope-vrfcnTypeCd').val(d.vrfcnTypeCd || '');
      $('#scope-vrfcnStnd').val(d.vrfcnStnd || '');
      $('#scope-scopeCn').val(d.scopeCn || '');
    })
    .fail(function() { /* 신규 VR은 빈 폼 */ });
}

$('#btn-scope-save').on('click', function() {
  const payload = {
    vrfcnInstNm:  $('#scope-vrfcnInstNm').val().trim(),
    vrfcnInstAddr: $('#scope-vrfcnInstAddr').val().trim(),
    vrfcnTypeCd:  $('#scope-vrfcnTypeCd').val(),
    vrfcnStnd:    $('#scope-vrfcnStnd').val().trim(),
    scopeCn:      $('#scope-scopeCn').val().trim()
  };
  if (!payload.vrfcnInstNm) { showToast('검증기관명을 입력하십시오.', 'warning'); return; }
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/scope',
    type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() { showToast('범위 정보가 저장되었습니다.', 'success'); })
    .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   검증팀(team)
   ======================================================== */
function loadTeam() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/team')
    .done(function(res) {
      const list = (res && res.data) ? res.data : (Array.isArray(res) ? res : []);
      renderTeamTable(list);
    })
    .fail(function() { renderTeamTable([]); });
}

function renderTeamTable(list) {
  if (!list.length) {
    $('#teamListBody').html('<tr><td colspan="5" class="text-center py-3 text-muted small">등록된 구성원이 없습니다.</td></tr>');
    return;
  }
  const ROLE_LABEL = { LEAD: '리더', MEMBER: '구성원', INDEP_REVIEWER: '독립검토자' };
  let html = '';
  list.forEach(function(row) {
    const warn = (row.roleCd === 'LEAD' && row.conscutv_cnt > 3)
      ? '<span class="badge bg-warning text-dark ms-1" title="연속검증 3년 초과">&#9888;</span>' : '';
    html += '<tr>'
      + '<td class="ps-3 small">' + esc(row.userNm) + warn + '</td>'
      + '<td class="small">' + esc(ROLE_LABEL[row.roleCd] || row.roleCd) + '</td>'
      + '<td class="small text-muted">' + esc(row.accrdDtl || '-') + '</td>'
      + '<td class="small text-center">' + esc(row.conscutvCnt || 0) + '</td>'
      + '<td><button class="btn btn-outline-danger btn-del-team" data-sn="' + esc(row.teamSn) + '" style="font-size:0.7rem;padding:2px 6px;">삭제</button></td>'
      + '</tr>';
  });
  $('#teamListBody').html(html);
  $('.btn-del-team').on('click', function() {
    const sn = $(this).data('sn');
    if (!confirm('구성원을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/vr/' + encodeURIComponent(VR_ID) + '/team/' + sn, type: 'DELETE' })
      .done(function() { showToast('삭제되었습니다.', 'success'); loadTeam(); })
      .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
  });
}

$('#btn-team-add').on('click', function() {
  $('#teamForm-userNm, #teamForm-accrdDtl').val('');
  $('#teamForm-roleCd').val('');
  $('#teamForm-conscutv').val(0);
  $('#consecWarn').addClass('d-none');
  new bootstrap.Modal(document.getElementById('teamModal')).show();
});

$('#teamForm-conscutv').on('input', function() {
  $('#consecWarn').toggleClass('d-none', parseInt($(this).val()) <= 3);
});

$('#btn-team-save').on('click', function() {
  const userNm = $('#teamForm-userNm').val().trim();
  const roleCd = $('#teamForm-roleCd').val();
  if (!userNm || !roleCd) { showToast('성명과 역할은 필수 입력입니다.', 'warning'); return; }
  const payload = {
    userNm: userNm, roleCd: roleCd,
    accrdDtl: $('#teamForm-accrdDtl').val().trim(),
    conscutvCnt: parseInt($('#teamForm-conscutv').val()) || 0
  };
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/team',
    type: 'POST', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() {
    bootstrap.Modal.getInstance(document.getElementById('teamModal')).hide();
    showToast('구성원이 추가되었습니다.', 'success');
    loadTeam();
  }).fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   시간투입(time)
   ======================================================== */
function loadTime() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/time')
    .done(function(res) {
      const d = (res && res.data) ? res.data : res;
      if (!d) return;
      $('#time-onsiteHrs').val(d.onsiteHrs || 0);
      $('#time-offsiteHrs').val(d.offsiteHrs || 0);
      $('#time-totalHrs').val(d.totalHrs || 0);
    })
    .fail(function() { });
}

function recalcTotal() {
  const on  = parseFloat($('#time-onsiteHrs').val())  || 0;
  const off = parseFloat($('#time-offsiteHrs').val()) || 0;
  $('#time-totalHrs').val((on + off).toFixed(1));
}
$('#time-onsiteHrs, #time-offsiteHrs').on('input', recalcTotal);

$('#btn-time-save').on('click', function() {
  recalcTotal();
  const payload = {
    onsiteHrs:  parseFloat($('#time-onsiteHrs').val())  || 0,
    offsiteHrs: parseFloat($('#time-offsiteHrs').val()) || 0,
    totalHrs:   parseFloat($('#time-totalHrs').val())   || 0
  };
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/time',
    type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() { showToast('검증 시간이 저장되었습니다.', 'success'); })
    .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   입력자료(inpt)
   ======================================================== */
function loadInpt() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/inpt')
    .done(function(res) {
      const list = (res && res.data) ? res.data : (Array.isArray(res) ? res : []);
      renderInptTable(list);
    })
    .fail(function() { renderInptTable([]); });
}

function renderInptTable(list) {
  if (!list.length) {
    $('#inptListBody').html('<tr><td colspan="4" class="text-center py-3 text-muted small">등록된 자료가 없습니다.</td></tr>');
    return;
  }
  let html = '';
  list.forEach(function(row) {
    html += '<tr>'
      + '<td class="ps-3 small">' + esc(row.docNm) + '</td>'
      + '<td class="small">' + esc(row.docSeCd || '-') + '</td>'
      + '<td class="small">' + (row.fileId ? '<a href="/api/file/' + esc(row.fileId) + '" target="_blank"><i class="bi bi-download"></i> 다운로드</a>' : '-') + '</td>'
      + '<td><button class="btn btn-outline-danger btn-del-inpt" data-sn="' + esc(row.inputSn) + '" style="font-size:0.7rem;padding:2px 6px;">삭제</button></td>'
      + '</tr>';
  });
  $('#inptListBody').html(html);
  $('.btn-del-inpt').on('click', function() {
    const sn = $(this).data('sn');
    if (!confirm('자료를 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/vr/' + encodeURIComponent(VR_ID) + '/inpt/' + sn, type: 'DELETE' })
      .done(function() { showToast('삭제되었습니다.', 'success'); loadInpt(); })
      .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
  });
}

$('#btn-inpt-add').on('click', function() {
  $('#inptForm-docNm').val('');
  $('#inptForm-docSeCd').val('');
  new bootstrap.Modal(document.getElementById('inptModal')).show();
});

$('#btn-inpt-save').on('click', function() {
  const docNm = $('#inptForm-docNm').val().trim();
  if (!docNm) { showToast('문서명을 입력하십시오.', 'warning'); return; }
  const payload = { docNm: docNm, docSeCd: $('#inptForm-docSeCd').val() };
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/inpt',
    type: 'POST', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() {
    bootstrap.Modal.getInstance(document.getElementById('inptModal')).hide();
    showToast('자료가 추가되었습니다.', 'success');
    loadInpt();
  }).fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   검증절차(prcdr)
   ======================================================== */
function loadPrcdr() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/prcdr')
    .done(function(res) {
      const d = (res && res.data) ? res.data : res;
      if (!d) return;
      $('#prcdr-strgAnlys').val(d.strgAnlysCn || '');
      $('#prcdr-riskEval').val(d.riskEvalCn || '');
      $('#prcdr-smplngActv').val(d.smplngActvCn || '');
      $('#prcdr-smplngRslt').val(d.smplngRsltCn || '');
      $('#prcdr-empCompl').val(d.empComplCn || '');
    })
    .fail(function() { });
}

$('#btn-prcdr-save').on('click', function() {
  const payload = {
    strgAnlysCn:  $('#prcdr-strgAnlys').val().trim(),
    riskEvalCn:   $('#prcdr-riskEval').val().trim(),
    smplngActvCn: $('#prcdr-smplngActv').val().trim(),
    smplngRsltCn: $('#prcdr-smplngRslt').val().trim(),
    empComplCn:   $('#prcdr-empCompl').val().trim()
  };
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/prcdr',
    type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() { showToast('절차·분석 정보가 저장되었습니다.', 'success'); })
    .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   부적합(ncnfrm)
   ======================================================== */
function loadNcnfrm() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/ncnfrm')
    .done(function(res) {
      const list = (res && res.data) ? res.data : (Array.isArray(res) ? res : []);
      renderNcnfrmTable(list);
      hasUnresolvedNcnfrm = list.some(function(r) { return !r.resolDt; });
      updateNcnfrmWarnings();
    })
    .fail(function() { renderNcnfrmTable([]); });
}

function updateNcnfrmWarnings() {
  $('#ncnfrmWarnBanner').toggleClass('d-none', !hasUnresolvedNcnfrm);
  $('#ncnfrmUnresolvedBadge').toggleClass('d-none', !hasUnresolvedNcnfrm);
  // 결론 탭 내 REASONABLE 경고 연동
  if ($('#cncls-finalOpnnCd').val() === 'REASONABLE') {
    $('#cncls-reasonableWarn').toggleClass('d-none', !hasUnresolvedNcnfrm);
  }
}

function renderNcnfrmTable(list) {
  if (!list.length) {
    $('#ncnfrmListBody').html('<tr><td colspan="6" class="text-center py-3 text-muted small">등록된 부적합이 없습니다.</td></tr>');
    return;
  }
  const SE_LABEL = { MINOR: '경미', MAJOR: '중대', MISSTATEMENT: '허위진술' };
  const SE_CLS   = { MINOR: 'bg-warning text-dark', MAJOR: 'bg-danger', MISSTATEMENT: 'bg-dark' };
  let html = '';
  list.forEach(function(row) {
    const resolved = !!row.resolDt;
    html += '<tr class="' + (resolved ? '' : 'table-warning') + '">'
      + '<td class="ps-3 small">' + esc(row.itemNo) + '</td>'
      + '<td><span class="badge ' + (SE_CLS[row.ncnfrmSeCd] || 'bg-secondary') + ' status-badge">' + esc(SE_LABEL[row.ncnfrmSeCd] || row.ncnfrmSeCd) + '</span></td>'
      + '<td class="small">' + esc(row.desc) + '</td>'
      + '<td class="small">' + esc(row.resolDescCn || '-') + '</td>'
      + '<td class="small">' + (resolved ? esc(row.resolDt) : '<span class="text-danger fw-semibold">미해결</span>') + '</td>'
      + '<td class="d-flex gap-1">'
      + (!resolved ? '<button class="btn btn-outline-success btn-resolve-ncnfrm" data-no="' + esc(row.itemNo) + '" style="font-size:0.7rem;padding:2px 5px;">해결</button>' : '')
      + '<button class="btn btn-outline-danger btn-del-ncnfrm" data-no="' + esc(row.itemNo) + '" style="font-size:0.7rem;padding:2px 5px;">삭제</button>'
      + '</td>'
      + '</tr>';
  });
  $('#ncnfrmListBody').html(html);

  $('.btn-del-ncnfrm').on('click', function() {
    const no = $(this).data('no');
    if (!confirm('부적합 항목을 삭제하시겠습니까?')) return; /* IcasAlert.confirm 비동기 미변환 — 수동검토 */
    $.ajax({ url: '/api/vr/' + encodeURIComponent(VR_ID) + '/ncnfrm/' + no, type: 'DELETE' })
      .done(function() { showToast('삭제되었습니다.', 'success'); loadNcnfrm(); })
      .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
  });

  $('.btn-resolve-ncnfrm').on('click', function() {
    const no = $(this).data('no');
    const dt = prompt('해결일을 입력하십시오 (YYYY-MM-DD):');
    if (!dt) return;
    $.ajax({
      url: '/api/vr/' + encodeURIComponent(VR_ID) + '/ncnfrm/' + no + '/resolve',
      type: 'PUT', contentType: 'application/json',
      data: JSON.stringify({ resolDescCn: '', resolDt: dt })
    }).done(function() { showToast('해결 처리되었습니다.', 'success'); loadNcnfrm(); })
      .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
  });
}

$('#btn-ncnfrm-add').on('click', function() {
  $('#ncnfrmForm-itemNo').val('');
  $('#ncnfrmForm-seCd').val('');
  $('#ncnfrmForm-desc, #ncnfrmForm-resolDesc').val('');
  $('#ncnfrmForm-resolDt').val('');
  $('#ncnfrmModalTitle').text('부적합 등록');
  new bootstrap.Modal(document.getElementById('ncnfrmModal')).show();
});

$('#btn-ncnfrm-save').on('click', function() {
  const seCd = $('#ncnfrmForm-seCd').val();
  const desc = $('#ncnfrmForm-desc').val().trim();
  if (!seCd || !desc) { showToast('구분과 설명은 필수 입력입니다.', 'warning'); return; }
  const payload = {
    ncnfrmSeCd:   seCd,
    desc:         desc,
    resolDescCn:  $('#ncnfrmForm-resolDesc').val().trim(),
    resolDt:      $('#ncnfrmForm-resolDt').val() || null
  };
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/ncnfrm',
    type: 'POST', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() {
    bootstrap.Modal.getInstance(document.getElementById('ncnfrmModal')).hide();
    showToast('부적합이 등록되었습니다.', 'success');
    loadNcnfrm();
  }).fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   결론(cncls)
   ======================================================== */
function loadCncls() {
  $.get('/api/vr/' + encodeURIComponent(VR_ID) + '/cncls')
    .done(function(res) {
      const d = (res && res.data) ? res.data : res;
      if (!d) return;
      $('#cncls-dataQltyEval').val(d.dataQltyEval || '');
      $('#cncls-mtrltyEval').val(d.mtrltyEval || '');
      $('#cncls-erCncls').val(d.erCncls || '');
      $('#cncls-eucrCncls').val(d.eucrCncls || '');
      $('#cncls-judgCn').val(d.judgCn || '');
      $('#cncls-indepReviewCn').val(d.indepReviewCn || '');
      $('#cncls-indepReviewUserNm').val(d.indepReviewUserNm || '');
      $('#cncls-finalOpnnCd').val(d.finalOpnnCd || '');
      updateNcnfrmWarnings();
    })
    .fail(function() { });
}

$('#cncls-finalOpnnCd').on('change', function() {
  const isReasonable = $(this).val() === 'REASONABLE';
  $('#cncls-reasonableWarn').toggleClass('d-none', !(isReasonable && hasUnresolvedNcnfrm));
});

$('#btn-cncls-save').on('click', function() {
  const finalOpnn = $('#cncls-finalOpnnCd').val();
  if (!finalOpnn) { showToast('최종 검증 의견을 선택하십시오.', 'warning'); return; }
  // REASONABLE 의견 저장 시 미해결 부적합이 있으면 차단
  if (finalOpnn === 'REASONABLE' && hasUnresolvedNcnfrm) {
    showToast('미해결 부적합이 있어 REASONABLE 의견으로 저장할 수 없습니다.', 'danger');
    $('#cncls-reasonableWarn').removeClass('d-none');
    return;
  }
  const payload = {
    dataQltyEval:       $('#cncls-dataQltyEval').val().trim(),
    mtrltyEval:         $('#cncls-mtrltyEval').val().trim(),
    erCncls:            $('#cncls-erCncls').val().trim(),
    eucrCncls:          $('#cncls-eucrCncls').val().trim(),
    judgCn:             $('#cncls-judgCn').val().trim(),
    indepReviewCn:      $('#cncls-indepReviewCn').val().trim(),
    indepReviewUserNm:  $('#cncls-indepReviewUserNm').val().trim(),
    finalOpnnCd:        finalOpnn
  };
  $.ajax({
    url: '/api/vr/' + encodeURIComponent(VR_ID) + '/cncls',
    type: 'PUT', contentType: 'application/json', data: JSON.stringify(payload)
  }).done(function() { showToast('결론·검증 의견이 저장되었습니다.', 'success'); })
    .fail(function(xhr) { showToast(extractErrMsg(xhr), 'danger'); });
});

/* ========================================================
   페이지 초기화
   ======================================================== */
$(function() {
  if (!VR_ID) {
    showToast('잘못된 접근입니다.', 'danger');
    return;
  }
  loadVrDetail();
  loadScope(); // 첫 탭 선로드
});
</script>
</body>
</html>
