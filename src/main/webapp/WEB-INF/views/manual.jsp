<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>📖 ICAS-CEMS 시스템 매뉴얼</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
<style>
  :root { --icas-primary:#0F2C72; --icas-accent:#f97316; }
  body { background:#f8f9fb; font-size:0.92rem; line-height:1.65; color:#1f2937; }
  .manual-header { background:linear-gradient(135deg,#0F2C72 0%,#1e40af 100%); color:white; padding:32px 0; }
  .toc-sticky { position:sticky; top:1rem; max-height:calc(100vh - 2rem); overflow-y:auto; }
  .toc-sticky::-webkit-scrollbar { width:6px; }
  .toc-sticky::-webkit-scrollbar-thumb { background:#cbd5e1; border-radius:3px; }
  .toc a { display:block; padding:4px 12px; color:#475569; text-decoration:none; font-size:0.83rem; border-left:2px solid transparent; }
  .toc a:hover { background:#f1f5f9; color:#0F2C72; border-left-color:#0F2C72; }
  .toc a.lvl2 { padding-left:24px; font-size:0.8rem; color:#64748b; }
  h2.section { color:var(--icas-primary); border-bottom:2px solid #0F2C72; padding-bottom:8px; margin-top:48px; }
  h3.subsection { color:#1e40af; margin-top:32px; font-size:1.15rem; }
  .box-card { border-left:4px solid var(--icas-primary); background:white; border-radius:6px; padding:18px 22px; margin-bottom:14px; box-shadow:0 1px 3px rgba(0,0,0,0.05); }
  .box-card h4 { color:var(--icas-primary); margin-bottom:8px; font-size:1.05rem; }
  .box-num { display:inline-block; width:32px; height:32px; border-radius:50%; background:var(--icas-primary); color:white; font-weight:700; text-align:center; line-height:32px; margin-right:10px; }
  .actor-pill { display:inline-block; padding:3px 10px; border-radius:12px; font-size:0.74rem; font-weight:600; margin:2px 4px 2px 0; }
  .actor-MOLIT  { background:#dbeafe; color:#1e40af; }
  .actor-KOTSA  { background:#e0f2fe; color:#0369a1; }
  .actor-AIR    { background:#fef3c7; color:#92400e; }
  .actor-VRF    { background:#dcfce7; color:#166534; }
  .flow-step { display:flex; align-items:center; gap:14px; padding:10px 14px; background:white; border-radius:6px; margin-bottom:8px; box-shadow:0 1px 2px rgba(0,0,0,0.04); }
  .flow-step .step-num { width:28px; height:28px; border-radius:50%; background:var(--icas-accent); color:white; text-align:center; line-height:28px; font-weight:700; flex:none; }
  .flow-arrow { text-align:center; color:#94a3b8; font-size:1.2rem; margin:4px 0; }
  table.matrix { width:100%; background:white; border-collapse:collapse; font-size:0.83rem; }
  table.matrix th { background:var(--icas-primary); color:white; padding:8px 10px; font-weight:500; }
  table.matrix td { padding:7px 10px; border-bottom:1px solid #f1f5f9; vertical-align:top; }
  table.matrix tr:hover td { background:#f8fafc; }
  .badge-stat { display:inline-block; padding:2px 8px; border-radius:4px; font-size:0.72rem; font-weight:600; }
  .stat-DRAFT { background:#e5e7eb; color:#374151; }
  .stat-SBMTD { background:#dbeafe; color:#1e40af; }
  .stat-RVWNG { background:#fef3c7; color:#92400e; }
  .stat-RCMDD { background:#cffafe; color:#0e7490; }
  .stat-APRVD { background:#dcfce7; color:#166534; }
  .stat-RJCTD { background:#fee2e2; color:#991b1b; }
  .stat-CNCLD { background:#1f2937; color:white; }
  .url-chip { display:inline-block; background:#f1f5f9; color:#0F2C72; padding:2px 8px; border-radius:4px; font-family:'SF Mono','Consolas',monospace; font-size:0.78rem; }
  pre.diagram { background:#1e293b; color:#e2e8f0; padding:18px; border-radius:8px; font-size:0.78rem; line-height:1.5; overflow-x:auto; }
  .role-table th, .role-table td { padding:6px 10px; }
  .check { color:#16a34a; font-weight:700; }
  .cross { color:#dc2626; }
  .tip { background:#fef9c3; border-left:4px solid #facc15; padding:10px 14px; border-radius:4px; font-size:0.86rem; margin:12px 0; }
  .danger { background:#fee2e2; border-left:4px solid #dc2626; padding:10px 14px; border-radius:4px; font-size:0.86rem; margin:12px 0; }
  .info { background:#dbeafe; border-left:4px solid #1e40af; padding:10px 14px; border-radius:4px; font-size:0.86rem; margin:12px 0; }
  body { padding-bottom:80px; }
</style>
</head>
<body>

<header class="manual-header">
  <div class="container">
    <h1 class="mb-2"><i class="bi bi-book me-2"></i>ICAS-CEMS 시스템 매뉴얼</h1>
    <p class="mb-0" style="opacity:0.9;">국제항공 탄소배출량 관리시스템 — 개요·업무흐름·권한·화면별 사용법</p>
    <div class="mt-3 small" style="opacity:0.85;">
      <i class="bi bi-info-circle me-1"></i>본 문서는 RFP 「국제항공 탄소 배출량 관리 시스템 구축」(p.10 목표시스템 개념도)을 기준으로 작성되었습니다.
    </div>
  </div>
</header>

<div class="container my-4">
  <div class="row">

    <!-- ─── 좌측 TOC ─── -->
    <div class="col-md-3">
      <div class="toc-sticky">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white fw-bold" style="color:var(--icas-primary);font-size:0.9rem;">
            <i class="bi bi-list-ul me-1"></i>목차
          </div>
          <div class="card-body p-2 toc">
            <a href="#sec-overview">1. 시스템 개요</a>
            <a href="#sec-architecture">2. 시스템 아키텍처</a>
            <a href="#sec-actors">3. 주요 액터 5종</a>
            <a href="#sec-boxes">4. RFP 11박스 기능</a>
            <a href="#box-1" class="lvl2">① EMP 관리</a>
            <a href="#box-2" class="lvl2">② ER 관리</a>
            <a href="#box-3" class="lvl2">③ CEF 관리</a>
            <a href="#box-4" class="lvl2">④ VR 관리</a>
            <a href="#box-5" class="lvl2">⑤ EUCR 관리</a>
            <a href="#box-6" class="lvl2">⑥ OoM-Check</a>
            <a href="#box-7" class="lvl2">⑦ CORSIA 검증</a>
            <a href="#box-8" class="lvl2">⑧ SAF 관리</a>
            <a href="#box-9" class="lvl2">⑨ 포털 서비스</a>
            <a href="#box-10" class="lvl2">⑩ 공통 AI (2차)</a>
            <a href="#box-11" class="lvl2">⑪ 외부 연계 (2차)</a>
            <a href="#sec-workflow">5. 전체 업무흐름</a>
            <a href="#sec-roles">6. 권한 매트릭스</a>
            <a href="#sec-screens">7. 화면별 사용법</a>
            <a href="#sec-status">8. 상태 코드</a>
            <a href="#sec-faq">9. 자주 묻는 질문</a>
          </div>
        </div>
      </div>
    </div>

    <!-- ─── 본문 ─── -->
    <div class="col-md-9">

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-overview">1. 시스템 개요</h2>

<p><strong>ICAS-CEMS</strong>는 국제항공 부문 탄소배출량을 모니터링·검증·상쇄 관리하는 시스템입니다.</p>

<table class="matrix mb-3">
  <tr><th style="width:25%;">항목</th><th>내용</th></tr>
  <tr><td>발주기관</td><td>국토교통부 항공기술과 (위탁: 한국교통안전공단)</td></tr>
  <tr><td>법적 근거</td><td>「국제항공 탄소 배출량 관리에 관한 법률」(2024.2) + ICAO CORSIA + SAF 로드맵(2025.9)</td></tr>
  <tr><td>적용 대상</td><td>국적 + 외국 항공운송사업자 (국내 취항)</td></tr>
  <tr><td>주요 기능</td><td>① 배출량 모니터링·보고 ② 검증보고서 작성 ③ 상쇄의무 자동 산정 ④ SAF 급유의무 관리 ⑤ 통계·시뮬레이션</td></tr>
  <tr><td>1차 범위</td><td>EMP·ER·CEF·VR·EUCR·OoM·SAF(비AI)·통합포털·통계·시뮬레이션</td></tr>
  <tr><td>2차 범위(별도사업)</td><td>LLM 검증 · AI OCR · 범정부 AI 공통기반 · 시스템 연계</td></tr>
</table>

<div class="info">
  <i class="bi bi-lightbulb me-1"></i><strong>핵심 가치 흐름</strong>:
  항공사가 <b>EMP</b>(계획) → <b>ER</b>(보고) → <b>검증기관 VR</b> → <b>KOTSA OoM</b> → <b>MOLIT 승인</b> →
  <b>EUCR</b>(상쇄). 동시에 <b>SAF</b> 급유의무 자동 이행률 산출.
</div>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-architecture">2. 시스템 아키텍처</h2>

<pre class="diagram">
┌─ 액터 (5종, 좌측) ─┐    ┌─────────── 목표시스템 (11박스, 우측) ───────────┐
                          ┌──────────┐ ┌──────────┐ ┌──────────┐
   국토교통부 ────┐        │ ① EMP    │ │ ② ER     │ │ ③ CEF    │
   한국교통안전공단┤        └──────────┘ └──────────┘ └──────────┘
   국적항공사 ────┼ HTTPS ┌──────────┐ ┌──────────┐ ┌──────────┐
   외국항공사 ────┤        │ ④ VR     │ │ ⑤ EUCR   │ │⑥ OoM-Chk │
   검증기관   ────┘        └──────────┘ └──────────┘ └──────────┘
                          ┌────────────────────────────────────┐
                          │  ⑦ CORSIA 세부항목 검증 (Rule 18종) │
                          └────────────────────────────────────┘
                          ┌──────────┐ ┌──────────────────────┐
                          │ ⑧ SAF    │ │ ⑨ 포털 서비스        │
                          └──────────┘ └──────────────────────┘
                          ┌──────────────────┐ ┌──────────────┐
                          │ ⑩ 공통 AI(2차)   │ │⑪ 외부연계(2차)│
                          └──────────────────┘ └──────────────┘
</pre>

<table class="matrix">
  <tr><th>계층</th><th>기술 스택</th></tr>
  <tr><td>프론트엔드</td><td>JSP 2.3 + jQuery 3.6 + Bootstrap 5.3 + ECharts 5</td></tr>
  <tr><td>백엔드</td><td>전자정부 표준프레임워크 4.x + Spring 5.3 + MyBatis 3.5</td></tr>
  <tr><td>인증</td><td>Spring Security 5.8 + HttpSession + CSRF</td></tr>
  <tr><td>DB</td><td>PostgreSQL 16 — 6 schemas (com / emp / er / vr / saf / ptl)</td></tr>
  <tr><td>WAS</td><td>Apache Tomcat 9 (Servlet 4.0)</td></tr>
  <tr><td>JVM</td><td>OpenJDK 17 LTS</td></tr>
</table>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-actors">3. 주요 액터 5종</h2>

<table class="matrix">
  <tr><th>액터</th><th>약어</th><th>역할</th><th>주요 행위</th></tr>
  <tr>
    <td><span class="actor-pill actor-MOLIT">국토교통부</span></td>
    <td>MOLIT</td>
    <td>주무부처</td>
    <td>운영사 지정·고시, 최종 승인, 상쇄의무량 통보, CCR 추출·보고</td>
  </tr>
  <tr>
    <td><span class="actor-pill actor-KOTSA">한국교통안전공단</span></td>
    <td>KOTSA</td>
    <td>위탁기관 (실무 검토)</td>
    <td>EMP 사전검토, ER·VR 적정성 검토, OoM-Check 실행, CORSIA 18종 정량 검증</td>
  </tr>
  <tr>
    <td><span class="actor-pill actor-AIR">국적항공사</span></td>
    <td>AIRLINE (DOM)</td>
    <td>이행의무자</td>
    <td>EMP·ER·CEF·EUCR 작성/제출, SAF 인증서·배치·구매 등록</td>
  </tr>
  <tr>
    <td><span class="actor-pill actor-AIR">외국항공사</span></td>
    <td>AIRLINE (FRGN)</td>
    <td>이행의무자 (국내 취항)</td>
    <td>국적항공사와 동일. 보고서식만 다국어/국내선 항목 비활성</td>
  </tr>
  <tr>
    <td><span class="actor-pill actor-VRF">검증기관</span></td>
    <td>VERIFIER</td>
    <td>ICAO CCR 공인 검증기관</td>
    <td>VR 작성·제출, 부적합 통보, OoM 품질평가</td>
  </tr>
</table>

<div class="tip"><i class="bi bi-info-circle me-1"></i>
  <strong>가시범위</strong>: AIRLINE은 자기 항공사 데이터만, VERIFIER는 배정된 항공사만, MOLIT·KOTSA는 전사 데이터 접근.
</div>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-boxes">4. RFP 11박스 기능 상세</h2>

<div class="box-card" id="box-1">
  <h4><span class="box-num">①</span>EMP 관리 (Emissions Monitoring Plan) <span class="url-chip">/emp/plan</span></h4>
  <p><strong>배출량 모니터링 계획서</strong> — 항공사가 다음 연도의 배출량 모니터링 방법(연료 측정·항공기 식별·운항 데이터)을 사전 계획하여 승인 받는 절차.</p>
  <ul class="mb-0">
    <li>EMP 버전 관리 · 운영사 식별정보 · 항공기/운항 데이터</li>
    <li>배출량 계산방법 (Method A/B/Block Hour 등)</li>
    <li>데이터 품질 통제 · 위험 평가</li>
    <li>법정 서식 출력 · 변경 이력 추적</li>
    <li>(2차) sLLM 서술형 검증 · 버전 변경내역 분석</li>
  </ul>
</div>

<div class="box-card" id="box-2">
  <h4><span class="box-num">②</span>ER 관리 (Emission Report) <span class="url-chip">/er/list</span></h4>
  <p><strong>연간 배출량 보고서</strong> — 항공사가 한 해의 실제 배출량을 보고. 자동 계산 엔진과 오류 검증 내장.</p>
  <ul class="mb-0">
    <li>운영사·보고서 기본정보 · 항공기/연료 정보</li>
    <li>국가 쌍·비행장 쌍 배출량 (출발/도착 쌍별 CO2)</li>
    <li>데이터 갭 관리 · AFBR(Affected Block Activity 추정)</li>
    <li>검증기관 정보 · 법정 서식 출력 (PDF/Excel)</li>
  </ul>
</div>

<div class="box-card" id="box-3">
  <h4><span class="box-num">③</span>CEF 관리 (CORSIA Eligible Fuels) <span class="url-chip">/er/cef</span></h4>
  <p><strong>적격연료 청구</strong> — ICAO 적격 인증 SAF를 사용해 배출량을 차감 청구. ER의 부속 보고서.</p>
  <ul class="mb-0">
    <li>적격연료 청구 (Claim) · 수명주기 배출량(LCA) · 공급망 정보</li>
    <li>이중청구 스캐닝 (CEF↔CEF + CEF↔SAF 교차 검증)</li>
    <li>BLOCKED / WARNING / OK 3단계 판정</li>
  </ul>
</div>

<div class="box-card" id="box-4">
  <h4><span class="box-num">④</span>VR 관리 (Verification Report) <span class="url-chip">/vr/list</span></h4>
  <p><strong>검증보고서</strong> — ICAO CCR 공인 검증기관이 항공사의 ER을 독립 검증.</p>
  <ul class="mb-0">
    <li>범위/식별정보 · 시간/범위/일반 · 절차 및 분석</li>
    <li>결론/검증의견 (REASONABLE / LIMITED Assurance)</li>
    <li>부적합(Ncnfrm) 발견·해결</li>
  </ul>
</div>

<div class="box-card" id="box-5">
  <h4><span class="box-num">⑤</span>EUCR 관리 (Emission Unit Cancellation Report) <span class="url-chip">/er/eucr</span></h4>
  <p><strong>배출권 취소 보고</strong> — 상쇄의무를 이행하기 위한 배출권(EU/감축단위) 취소.</p>
  <ul class="mb-0">
    <li>운영사 정보 · 상쇄요건/배출권 종류</li>
    <li>취소 배출권 일련번호 등록 · 배치 업로드/합산</li>
    <li>의무량 충족 자동 판정 (SUM(취소량) ≥ 의무량 → 충족)</li>
    <li>이중사용 탐지 (일련번호 unique)</li>
  </ul>
</div>

<div class="box-card" id="box-6">
  <h4><span class="box-num">⑥</span>OoM-Check (Order of Magnitude Check) <span class="url-chip">/er/oom</span></h4>
  <p><strong>적정성 검토</strong> — KOTSA가 ER 총량 적정성을 점검.</p>
  <ul class="mb-0">
    <li>운항 활동 데이터 · 비행장/국가 쌍 · 비행횟수/연료량</li>
    <li>CERT(Certain) 노선별 연료 비교 · OoM 판정 결과 (PASS/FAIL/HOLD)</li>
    <li>증빙/설명 요청 (항공사에게)</li>
    <li>검증기관 품질등급 평가</li>
  </ul>
</div>

<div class="box-card" id="box-7">
  <h4><span class="box-num">⑦</span>CORSIA 세부항목 검증 <span class="url-chip">/er/oom/qchk</span></h4>
  <p><strong>Rule 18종 자동 정량 검증</strong> — OoM 의 하위 도메인. ICAO ETM Appendix 6 의 정량 항목.</p>
  <table class="matrix" style="font-size:0.78rem;margin-top:8px;">
    <tr><th>Rule</th><th>항목</th><th>Rule</th><th>항목</th></tr>
    <tr><td>R001</td><td>ICAO 지정어</td><td>R010</td><td>국가쌍-연료 중복</td></tr>
    <tr><td>R002</td><td>제출기한 준수</td><td>R011</td><td>국내선 오류</td></tr>
    <tr><td>R003</td><td>ER-VR 일치성</td><td>R012</td><td>연료소비 이상치</td></tr>
    <tr><td>R004</td><td>작성일자 적정성</td><td>R013</td><td>CERT 편차</td></tr>
    <tr><td>R005</td><td>보고의무(1만 톤)</td><td>R014</td><td>데이터 갭 초과</td></tr>
    <tr><td>R006</td><td>CERT 일계치</td><td>R015</td><td>데이터 갭 정합</td></tr>
    <tr><td>R007</td><td>연료유형 일치</td><td>R016</td><td>검증기관 인증</td></tr>
    <tr><td>R008</td><td>등록기호 중복</td><td>R017</td><td>팀리더 연속 검증</td></tr>
    <tr><td>R009</td><td>국가 쌍 분류</td><td>R018</td><td>전년대비 이상치</td></tr>
  </table>
</div>

<div class="box-card" id="box-8">
  <h4><span class="box-num">⑧</span>SAF 관리 (Sustainable Aviation Fuel) <span class="url-chip">/saf/dashboard</span></h4>
  <p><strong>지속가능항공유 급유의무 관리</strong> — 2028년부터 항공사 급유의무 단계 시행.</p>
  <ul class="mb-0">
    <li>인증서 업로드 (PoS/PoC) · 검증/회수</li>
    <li>배치 기본정보 · 생산자/공급사/혼합사 정보</li>
    <li>원료·제품 정보 · GHG 배출 정보 · 증빙 문서</li>
    <li>공항별 급유실적 · SAF 모니터링 · 이중청구 스캐닝</li>
    <li>이행률 대시보드 · 공항별 SAF 구매</li>
    <li>(2차) AI OCR 문서 데이터화 · 현장 실사 지원</li>
  </ul>
</div>

<div class="box-card" id="box-9">
  <h4><span class="box-num">⑨</span>포털 서비스 <span class="url-chip">/ptl/workflow</span></h4>
  <p><strong>전사 통합 포털</strong> — 모든 사용자가 자기 가시범위 내 통합 워크플로우/통계 조회.</p>
  <ul class="mb-0">
    <li>이해관계자 포털 (4 액터별 진입화면)</li>
    <li>역할기반 접근제어 (RBAC)</li>
    <li>통합 워크플로우 (6 도메인 상태 매트릭스)</li>
    <li>CCR 데이터 집계·포맷 추출 (ICAO 보고용)</li>
    <li>상쇄비용 시뮬레이션 · 성장률 분석</li>
    <li>규정 업데이트 관리 (게시판)</li>
    <li>배출/상쇄/SAF 통계</li>
  </ul>
</div>

<div class="box-card" id="box-10">
  <h4><span class="box-num">⑩</span>공통 AI 서비스 (2차년도 본격 구현) <span class="url-chip">/ai/console</span></h4>
  <p><strong>1차 자리·인터페이스만</strong>. 2차년도에 본격 도입.</p>
  <ul class="mb-0">
    <li>sLLM 공동 서비스 환경 (로컬 H100 + Gemma)</li>
    <li>AI 로깅·모니터링 (호출 로그·토큰량·신뢰도)</li>
    <li>입·출력 보안대책 (PII 마스킹·환각 필터)</li>
    <li>설명 가능한 AI (XAI — 근거 문장 하이라이트)</li>
  </ul>
</div>

<div class="box-card" id="box-11">
  <h4><span class="box-num">⑪</span>외부 연계 (2차년도, 1차는 IF 정의만)</h4>
  <p>석유관리원 시스템 · 범정부 AI 공통기반 · data.go.kr 등 외부 연계 (1차 범위 외).</p>
</div>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-workflow">5. 전체 업무흐름</h2>

<h3 class="subsection">5.1 연간 사이클 (보고연도 N)</h3>

<div class="flow-step">
  <div class="step-num">1</div>
  <div>
    <strong>N−1 년말</strong>: 국토부 운영사 지정·고시 → 항공사 <b>EMP(N년 적용)</b> 작성·제출 → KOTSA 사전검토 → MOLIT 승인
  </div>
</div>
<div class="flow-arrow"><i class="bi bi-arrow-down"></i></div>

<div class="flow-step">
  <div class="step-num">2</div>
  <div>
    <strong>N 년 운영 (월별 누적)</strong>: 항공사 연료/노선/항공편 데이터 자동 누적, SAF 인증서·구매·공항급유 실적 등록
  </div>
</div>
<div class="flow-arrow"><i class="bi bi-arrow-down"></i></div>

<div class="flow-step">
  <div class="step-num">3</div>
  <div>
    <strong>N+1 Q1</strong>: 항공사 <b>ER(N년)</b> 작성 + <b>CEF 청구</b> → 자체검증 → 제출. 검증기관 <b>VR 작성</b>
  </div>
</div>
<div class="flow-arrow"><i class="bi bi-arrow-down"></i></div>

<div class="flow-step">
  <div class="step-num">4</div>
  <div>
    <strong>N+1 Q2</strong>: KOTSA <b>ER·VR 적정성 검토</b> → <b>OoM-Check</b> (Rule 18종) → 시정요구 또는 권고 → MOLIT 최종 승인
  </div>
</div>
<div class="flow-arrow"><i class="bi bi-arrow-down"></i></div>

<div class="flow-step">
  <div class="step-num">5</div>
  <div>
    <strong>N+1 Q3</strong>: MOLIT <b>상쇄의무량 산정·통보</b> → 항공사 통보서 출력
  </div>
</div>
<div class="flow-arrow"><i class="bi bi-arrow-down"></i></div>

<div class="flow-step">
  <div class="step-num">6</div>
  <div>
    <strong>N+1 Q4</strong>: 항공사 배출권 취소 → <b>EUCR 작성·제출</b> → 검증·승인 (다시 VR→KOTSA→MOLIT)
  </div>
</div>
<div class="flow-arrow"><i class="bi bi-arrow-down"></i></div>

<div class="flow-step">
  <div class="step-num">7</div>
  <div>
    <strong>N+1 ~ N+2</strong>: MOLIT <b>ICAO CCR 추출·보고</b>. 연중 통계/시뮬레이션·규정 관리·SAF 의무비율 모니터링 (월별, 2028~)
  </div>
</div>

<h3 class="subsection">5.2 보고서 공통 상태기 (DRAFT → APRVD)</h3>

<pre class="diagram">
  ┌──────────┐ submit  ┌──────────┐ review  ┌──────────┐ recommend ┌──────────┐ approve ┌──────────┐
  │ DRAFT    │────────▶│ SBMTD    │────────▶│ RVWNG    │──────────▶│ RCMDD    │────────▶│ APRVD    │
  │ 작성중   │         │ 제출됨   │         │ 검토중   │  (KOTSA)   │ 권고됨   │ (MOLIT)  │ 승인     │
  └──────────┘         └──────────┘         └──────────┘            └──────────┘         └──────────┘
        ▲                                          │ reject                                    │ cancel
        └──────────────────────────────────────────┘                                            ▼
                                                                                            ┌──────────┐
                                                                                            │ CNCLD    │
                                                                                            │ 취소     │
                                                                                            └──────────┘
</pre>

<p>적용 보고서: EMP · ER · CEF · EUCR · VR (공통). 코드값: <code>WKFLW_ST_CD</code>.</p>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-roles">6. 권한 매트릭스</h2>

<table class="matrix role-table">
  <tr>
    <th>화면 / 기능</th>
    <th>MOLIT_ADMIN</th>
    <th>KOTSA_REVIEWER</th>
    <th>AIRLINE_MANAGER</th>
    <th>VERIFIER_LEAD</th>
  </tr>
  <tr><td>EMP 작성·제출</td>      <td class="text-center">조회</td><td class="text-center">조회·검토</td><td class="text-center check">✓ 작성·제출</td><td class="text-center">조회</td></tr>
  <tr><td>ER 작성·제출</td>       <td class="text-center">조회</td><td class="text-center">조회·검토</td><td class="text-center check">✓ 작성·제출</td><td class="text-center">조회 (배정)</td></tr>
  <tr><td>VR 작성·제출</td>       <td class="text-center">조회</td><td class="text-center">조회·검토</td><td class="text-center cross">×</td><td class="text-center check">✓ 작성·제출</td></tr>
  <tr><td>CEF 작성·제출</td>      <td class="text-center">조회</td><td class="text-center">조회·승인</td><td class="text-center check">✓ 작성·제출</td><td class="text-center">조회</td></tr>
  <tr><td>EUCR 작성·제출</td>     <td class="text-center">조회</td><td class="text-center">조회·검토</td><td class="text-center check">✓ 작성·제출</td><td class="text-center">조회</td></tr>
  <tr><td>OoM-Check 실행</td>     <td class="text-center check">✓ 최종승인</td><td class="text-center check">✓ 실행·확정</td><td class="text-center">응답</td><td class="text-center">품질평가</td></tr>
  <tr><td>CORSIA 18종 검증</td>   <td class="text-center">조회</td><td class="text-center check">✓ 실행</td><td class="text-center cross">×</td><td class="text-center">품질평가</td></tr>
  <tr><td>SAF 인증서 등록</td>    <td class="text-center">조회</td><td class="text-center">검토</td><td class="text-center check">✓ 등록</td><td class="text-center">조회</td></tr>
  <tr><td>SAF 모니터링 산출</td>  <td class="text-center">조회</td><td class="text-center check">✓ 자동산출</td><td class="text-center">조회</td><td class="text-center cross">×</td></tr>
  <tr><td>CCR 추출</td>           <td class="text-center check">✓</td><td class="text-center cross">×</td><td class="text-center cross">×</td><td class="text-center cross">×</td></tr>
  <tr><td>규정 관리</td>          <td class="text-center check">✓</td><td class="text-center">조회</td><td class="text-center">조회</td><td class="text-center">조회</td></tr>
</table>

<div class="info"><i class="bi bi-shield-check me-1"></i>
  <strong>3중 검증</strong>: ① 프론트 메뉴 필터 → ② Controller 진입 전 AuthorityInterceptor → ③ Service 진입 시 행 단위 가시범위 (DataScopeValidator). 모두 통과해야 접근 허용.
</div>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-screens">7. 화면별 사용법</h2>

<h3 class="subsection">7.1 메인 대시보드 <span class="url-chip">/main</span></h3>
<ul>
  <li>요약 카드 4종: 총 운영사 수 · 보고서 제출율 · SAF 이행율 · 연간 CO2</li>
  <li>워크플로우 매트릭스: 운영사 × 6 도메인 상태 한눈에</li>
  <li>빠른 메뉴: ER/VR/CEF/EUCR/SAF/통합 워크플로우 직진입</li>
</ul>

<h3 class="subsection">7.2 신규 등록 절차 (공통)</h3>
<ol>
  <li>해당 화면 진입 (예: <code>/er/list</code>)</li>
  <li>우상단 <kbd>+ 신규 등록</kbd> 버튼 클릭 → 모달 표시</li>
  <li>필수 필드 입력 후 <kbd>등록</kbd> 클릭</li>
  <li>성공 시 우하단 녹색 토스트 + 목록 자동 갱신</li>
  <li>실패 시 빨간 토스트에 검증 오류 메시지 표시</li>
</ol>

<h3 class="subsection">7.3 라이프사이클 액션 버튼</h3>
<p>상세 화면 우상단 액션바에 상태별 사용 가능 버튼이 노출됩니다.</p>
<table class="matrix">
  <tr><th>현재 상태</th><th>사용 가능 액션</th><th>액터</th></tr>
  <tr><td><span class="badge-stat stat-DRAFT">작성중</span></td><td>수정 · 제출 · 삭제</td><td>작성자(AIRLINE/VERIFIER)</td></tr>
  <tr><td><span class="badge-stat stat-SBMTD">제출됨</span></td><td>검토 진입 · 반려</td><td>KOTSA</td></tr>
  <tr><td><span class="badge-stat stat-RVWNG">검토중</span></td><td>권고 · 반려 · 직접 승인</td><td>KOTSA/MOLIT</td></tr>
  <tr><td><span class="badge-stat stat-RCMDD">권고됨</span></td><td>승인 · 반려</td><td>MOLIT</td></tr>
  <tr><td><span class="badge-stat stat-APRVD">승인</span></td><td>법정서식 출력 · (예외) 취소</td><td>MOLIT</td></tr>
</table>

<h3 class="subsection">7.4 법정 서식 출력</h3>
<p>승인 상태(APRVD) 진입 시 상세 화면 우상단에 <kbd>법정 서식 출력 (PDF)</kbd> · <kbd>Excel</kbd> 버튼이 활성화됩니다.</p>
<div class="tip"><i class="bi bi-info-circle me-1"></i>
  리포팅 툴은 발주기관 직접구매 SW 연계로 1차년도 placeholder, 2차년도 본격 출력.
</div>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-status">8. 상태 코드</h2>

<table class="matrix">
  <tr><th>코드</th><th>라벨</th><th>의미</th></tr>
  <tr><td><span class="badge-stat stat-DRAFT">DRAFT</span></td><td>작성중</td><td>최초 작성 또는 반려 후 재작성</td></tr>
  <tr><td><span class="badge-stat stat-SBMTD">SBMTD</span></td><td>제출됨</td><td>작성자가 제출 (수정 잠금)</td></tr>
  <tr><td><span class="badge-stat stat-RVWNG">RVWNG</span></td><td>검토중</td><td>KOTSA가 검토 진입</td></tr>
  <tr><td><span class="badge-stat stat-RCMDD">RCMDD</span></td><td>권고됨</td><td>KOTSA → MOLIT 권고 (옵션 단계)</td></tr>
  <tr><td><span class="badge-stat stat-APRVD">APRVD</span></td><td>승인</td><td>MOLIT 최종 승인</td></tr>
  <tr><td><span class="badge-stat stat-RJCTD">RJCTD</span></td><td>반려</td><td>사유 첨부 후 작성자로 복귀</td></tr>
  <tr><td><span class="badge-stat stat-CNCLD">CNCLD</span></td><td>취소</td><td>예외 — 사유 필수</td></tr>
</table>

<h3 class="subsection">CEF 라이프사이클 (단순)</h3>
<p>DRAFT → SBMTD → APRVD → CNCLD (KOTSA 승인/반려, MOLIT 취소)</p>

<h3 class="subsection">VR 라이프사이클</h3>
<p>DRAFT → SBMTD → RCMDD → APRVD. 권고 단계에서 CCR 만료 검증 자동 차단.</p>

<h3 class="subsection">OoM 라이프사이클</h3>
<p>INPRG (진행중) → DONE. 결과 코드: PASS / FAIL / HOLD.</p>

<!-- ───────────────────────────────────────────────── -->
<h2 class="section" id="sec-faq">9. 자주 묻는 질문 (FAQ)</h2>

<h3 class="subsection">Q1. 사이드바 메뉴가 잘려요</h3>
<p>사이드바는 스크롤이 활성화되어 있습니다. 사이드바 영역 안에서 마우스 휠 또는 우측 스크롤바를 사용하세요. 메뉴 선택 후에도 스크롤 위치가 보존됩니다.</p>

<h3 class="subsection">Q2. 신규 등록 버튼이 안 보여요</h3>
<p>화면별로 작성 권한이 있는 액터만 버튼이 노출됩니다. 예: VR 신규 등록은 VERIFIER_LEAD 만, CEF 신규 등록은 AIRLINE_MANAGER 만 등록 가능. 본인 권한 확인은 <code>/com/role</code> 에서 가능합니다.</p>

<h3 class="subsection">Q3. 보고서를 제출했는데 수정하고 싶어요</h3>
<p>SBMTD 이상 상태에서는 직접 수정 불가능합니다. KOTSA에게 반려 요청 → DRAFT 복귀 후 재작성.</p>

<h3 class="subsection">Q4. 외국항공사 데이터는 어디서 보나요?</h3>
<p><code>/com/oprtr</code> 의 운영사 목록에서 <code>oprtr_natnl_se_cd</code> 컬럼이 <b>FRGN</b> 인 항목이 외국 항공사입니다. 권한·가시범위는 국적과 동일.</p>

<h3 class="subsection">Q5. CORSIA 18종 정량 검증은 어떻게 실행하나요?</h3>
<p>OoM 상세(<code>/er/oom/{oomId}</code>)에서 <kbd>18종 정량검증 실행</kbd> 버튼 클릭 → 결과는 항목 탭에 자동 표시. KOTSA 권한 필요.</p>

<h3 class="subsection">Q6. SAF 의무이행 비율은 어떻게 계산되나요?</h3>
<p><code>safPurchQty / totalFuelQty × 100 ≥ oblgRatio</code> 이면 충족 (fulfilledYn=Y). 산출은 <code>/saf/mntr</code> 화면의 <kbd>전체 일괄 산출</kbd> 또는 <kbd>운영사별 산출</kbd> 버튼.</p>

<hr class="my-5">

<div class="text-center text-muted small" style="opacity:0.7;">
  ICAS-CEMS 시스템 매뉴얼 · 작성 기준: RFP 「국제항공 탄소 배출량 관리 시스템 구축 - 최종」 (국토교통부 항공기술과)<br>
  본 문서는 1차년도 범위 기준이며, 2차년도 항목은 (2차)로 표시됩니다.
</div>

    </div><!-- /col-md-9 -->
  </div><!-- /row -->
</div>

<script>
  // 부드러운 앵커 스크롤
  document.querySelectorAll('.toc a').forEach(function(a){
    a.addEventListener('click', function(e){
      var href = a.getAttribute('href');
      if (href && href.startsWith('#')) {
        var t = document.querySelector(href);
        if (t) { e.preventDefault(); window.scrollTo({top:t.offsetTop-20, behavior:'smooth'}); history.replaceState(null, '', href); }
      }
    });
  });
</script>

</body>
</html>
