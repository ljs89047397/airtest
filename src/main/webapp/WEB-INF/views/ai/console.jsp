<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>공통 AI 서비스 콘솔 &mdash; ICAS-CEMS</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
:root { --icas-primary: #0F2C72; }
body { background: #f0f2f5; }
.page-header-bar { background: white; border-bottom: 1px solid #e5e7eb; }
.feature-card { border-radius: 12px; border: 1px solid #e5e7eb; background: white; }
.feature-card h6 { color: #0F2C72; font-weight: 700; }
.feature-card.disabled { background: #f9fafb; opacity: 0.85; }
.badge-2nd { background:#fef3c7; color:#92400e; font-size:0.72rem; padding:3px 8px; border-radius:4px; font-weight:600; }
</style>
</head>
<body>
<jsp:include page="/WEB-INF/views/include/header.jsp" />
<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />

<div style="margin-left:220px; padding-top:60px;">
  <div class="page-header-bar px-4 py-3">
    <div>
      <h5 class="fw-bold mb-0" style="color:#0F2C72;">&#129302; 공통 AI 서비스 콘솔</h5>
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-0 small">
          <li class="breadcrumb-item"><a href="/main" class="text-decoration-none">홈</a></li>
          <li class="breadcrumb-item active">공통 AI 서비스 (RFP 박스 ⑩)</li>
        </ol>
      </nav>
    </div>
  </div>

  <div class="container-fluid p-4">

    <!-- 1차/2차 안내 -->
    <div class="alert alert-warning d-flex align-items-center" role="alert">
      <i class="bi bi-info-circle-fill me-2 fs-5"></i>
      <div>
        <strong>본 영역은 RFP 2차년도 본격 구현 대상입니다.</strong>
        1차년도는 인터페이스 명세·로그 적재 자리·UI placeholder만 제공합니다.
        본격 구현 항목: sLLM 서술형 검증(EMP/ER/CEF/VR), AI OCR(인증서·현장 데이터), 범정부 AI 공통기반 연계.
      </div>
    </div>

    <!-- 4개 컴포넌트 -->
    <div class="row g-3">
      <div class="col-md-6">
        <div class="card feature-card disabled p-3 h-100">
          <div class="d-flex justify-content-between align-items-start">
            <h6 class="mb-2"><i class="bi bi-cpu me-2"></i>sLLM 공동 서비스 환경</h6>
            <span class="badge-2nd">2차년도</span>
          </div>
          <p class="text-muted small mb-2">로컬 H100 + Gemma 컨테이너 기반 서술형 보고서 자동 검증.</p>
          <div class="small text-muted">
            <div>호출 contract: <code>POST /api/ai/sllm/verify</code></div>
            <div>적용 도메인: EMP / ER / CEF / VR</div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card feature-card disabled p-3 h-100">
          <div class="d-flex justify-content-between align-items-start">
            <h6 class="mb-2"><i class="bi bi-graph-up me-2"></i>AI 로깅·모니터링</h6>
            <span class="badge-2nd">2차년도</span>
          </div>
          <p class="text-muted small mb-2">호출 input/output hash, 토큰량, 소요시간, 신뢰도 실시간 대시보드.</p>
          <div class="small text-muted">
            <div>로그 테이블 자리: <code>ai.tn_ai_call_log</code></div>
            <div>보존기간: 5년 (PII 평문 미저장)</div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card feature-card disabled p-3 h-100">
          <div class="d-flex justify-content-between align-items-start">
            <h6 class="mb-2"><i class="bi bi-shield-lock me-2"></i>입·출력 보안대책</h6>
            <span class="badge-2nd">2차년도</span>
          </div>
          <p class="text-muted small mb-2">개인정보 자동 마스킹, 출력 환각·민감정보 누출 필터.</p>
          <div class="small text-muted">
            <div>마스킹 규칙: <code>데이터모델링.md §"개인정보 분류"</code></div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card feature-card disabled p-3 h-100">
          <div class="d-flex justify-content-between align-items-start">
            <h6 class="mb-2"><i class="bi bi-eye me-2"></i>설명 가능한 AI (XAI)</h6>
            <span class="badge-2nd">2차년도</span>
          </div>
          <p class="text-muted small mb-2">결과 근거 문장 하이라이트, 신뢰도 점수 노출. 사용자 책임 분리.</p>
          <div class="small text-muted">
            <div>AI 출력에 의한 자동 승인·반려 <strong class="text-danger">금지</strong> (참고용)</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 호출 contract 미리보기 -->
    <div class="card border-0 shadow-sm mt-4">
      <div class="card-header bg-white py-2 px-3 fw-semibold" style="color:#0F2C72;">
        호출 인터페이스 (1차 정의, 미구현)
      </div>
      <div class="card-body small">
<pre class="mb-0" style="background:#f8f9fa;border-radius:6px;padding:12px;">POST /api/ai/sllm/verify           ← 서술형 검증 (EMP/ER/CEF/VR 공통)
  body: { domain, targetId, fieldKey, text }
  resp: { callId, status, findings, confidence }   ← 1차: 모두 NOT_AVAILABLE

POST /api/ai/ocr/parse              ← AI OCR (인증서·현장 데이터)
  body: { docId, docType, file }
  resp: { callId, fields, confidence }             ← 1차: 사용자 수동입력 fallback
</pre>
      </div>
    </div>

  </div>
</div>
</body>
</html>
