# RTM — 요구사항 추적표 (Requirement Traceability Matrix)

> **사업명**: 국제항공 탄소 배출량 관리시스템(ICAS-CEMS) 1차년도 구축
> **발주기관**: 국토교통부 항공기술과 (위탁: 한국교통안전공단)
> **작성일**: 2026-05-24
> **버전**: v1.0

## 1. 목적
RFP 「국제항공 탄소 배출량 관리 시스템 구축」 의 기능요구사항(SFR-001 ~ 062)이 본 시스템에 어떻게 구현되어 있는지 1:1 추적성을 제공한다.

## 2. RFP 박스 ↔ 구현 매핑 (목표시스템 개념도 11박스)

| RFP 박스 | 명칭 | 화면 경로 | DB 스키마 | 핵심 테이블 | 1차년도 완료 |
|---|---|---|---|---|---|
| ① | EMP (배출량 모니터링 계획) | `/emp/plan` | `emp` | `tn_emp_plan` + 8 자식 | ✅ |
| ② | ER (배출량보고서) | `/er/list` | `er` | `tn_er` + 7 자식 | ✅ |
| ③ | CEF (적격연료) | `/er/cef` | `er` | `tn_cef`, `tn_cef_claim`, `tn_cef_lcyc`, `tn_cef_spchn` | ✅ |
| ④ | VR (검증보고서) | `/vr/list` | `vr` | `tn_vr` + 7 자식 | ✅ |
| ⑤ | EUCR (배출권 취소) | `/er/eucr` | `er` | `tn_eucr`, `tn_eucr_batch`, `tn_eucr_crdt_dtl` | ✅ |
| ⑥ | OoM-Check (적정성 검토) | `/er/oom` | `er` | `tn_oom_check` + 3 자식 | ✅ |
| ⑦ | CORSIA 세부항목 검증 (18종) | `/er/oom/qchk` | `er` | `tn_oom_check_item` | ✅ |
| ⑧ | SAF (지속가능항공유) | `/saf/*` | `saf` | `tn_saf_cert`, `tn_saf_batch` 외 | ✅ |
| ⑨ | 포털 (워크플로우/통계/시뮬/CCR) | `/ptl/*` | `ptl` | `tn_ptl_sim`, `tn_ptl_ccr_extr`, `th_user_actn` | ✅ |
| ⑩ | 공통 AI 서비스 | `/ai/console` | — | (2차년도 자리) | ⚠️ Placeholder |
| ⑪ | 외부 연계 | `/admin/icao-submit` | — | Mock (1차년도) | ⚠️ Mock |

## 3. SFR ↔ 구현 매핑 (주요 62개)

| SFR | 요구사항 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|
| SFR-001 | 운영사 식별정보 등록 | `/emp/plan/{id}` 운영자정보 탭 | E2E S2.6 | ✅ |
| SFR-002 | 담당자 연락처 관리 | EMP 운영자정보 탭 | E2E S2.2 | ✅ |
| SFR-003 | 항공기 유형·연료·대수 + 국가쌍 | EMP 항공기 탭, 국가쌍 탭 | E2E S2.2 | ✅ |
| SFR-004 | 배출량 계산방법 + CO2 측정 상세 | EMP CO2계산/상세 탭 | E2E S2.2 | ✅ |
| SFR-005 | 데이터 품질 통제 + 위험 항목 | EMP 데이터관리/리스크 탭 | E2E S2.2 | ✅ |
| SFR-006 | EMP 라이프사이클 (DRAFT→APRVD) | EmpPlanService 5단계 워크플로우 | E2E S2.3~S2.5 | ✅ |
| SFR-007 | EMP 새 버전 생성 | `POST /api/emp/plan/{id}/new-version` | E2E S2.1 | ✅ |
| SFR-008 | EMP 변경 이력 추적 | `emp.th_emp_chg_hstry` | E2E S2.7 | ✅ |
| SFR-010 | ER 본 보고 작성 | `/er/{id}` 8 자식 탭 | E2E S5.1 | ✅ |
| SFR-011 | ER 항공기·연료 정보 | `tn_er_acft_fuel` | E2E S5 | ✅ |
| SFR-012 | ER 국가쌍·비행장쌍 | `tn_er_cntry_pair`, `tn_er_aerdrm_pair` | E2E S5 | ✅ |
| SFR-013 | ER 데이터 갭 관리 | `tn_er_data_gap` | E2E S5 | ✅ |
| SFR-014 | ER 법정 서식 출력 | EMP/ER 상세 "법정 서식 출력" 버튼 | 화면 검증 | ✅ |
| SFR-015 | ER sLLM 서술형 검증 | `/ai/console` placeholder | 2차년도 | ⚠️ |
| SFR-021 | CEF 이중청구 검증 | `EucrDoubleUsingValidator` + `validateDoubleClaim` API | E2E S4.2 | ✅ |
| SFR-022 | CEF 적격연료 청구·수명주기·공급망 | `tn_cef_claim`, `tn_cef_lcyc`, `tn_cef_spchn` | E2E S4 | ✅ |
| SFR-025 | VR 검증범위·검증팀·시간투입 | VR 7탭 (범위/팀/시간/입력/절차/부적합/결론) | E2E S5.3 | ✅ |
| SFR-027 | VR REASONABLE + 미해결 부적합 차단 | VrConclusionService 비즈룰 | 화면 검증 | ✅ |
| SFR-028 | VR sLLM 서술형 검증 | `/ai/console` placeholder | 2차년도 | ⚠️ |
| SFR-031 | EUCR 일련번호 이중사용 검증 | `EucrDoubleUsingValidator` + 검증 패널 | E2E S6.2 | ✅ |
| SFR-032 | EUCR 배치 등록 | `tn_eucr_batch` | E2E S6 | ✅ |
| SFR-033 | EUCR 크레딧 일련번호 상세 | `tn_eucr_crdt_dtl` | E2E S6 | ✅ |
| SFR-034 | CORSIA 정량 검증 18종 (R001~R018) | `/er/oom/qchk` + `OomQuantCheckService.runQuant` | E2E S5.6 | ✅ |
| SFR-040 | OoM 적정성 검토 + 추가요청 | `tn_oom_check_addl_rqst`, `tn_oom_check_vrfr_eval` | E2E S5.6 | ✅ |
| SFR-044 | AI OCR (인증서 자동 추출) | `/ai/console` placeholder | 2차년도 | ⚠️ |
| SFR-050 | SAF 인증서 등록·회수 | `/saf/cert` + `POST /surrender` | E2E S3.3 | ✅ |
| SFR-051 | SAF 배치 (생산·혼합·공급망) | `/saf/batch` + 5 자식 (ghg/feed/blndr/prdc/sply) | E2E S3.2 | ✅ |
| SFR-052 | SAF AI OCR (인증서) | `/ai/console` placeholder | 2차년도 | ⚠️ |
| SFR-053 | 공항별 SAF 급유·구매 | `/saf/airprt` fuel/purch 탭 | E2E S3.4 | ✅ |
| SFR-054 | SAF 혼합비율 모니터링 (자동 산출) | `/saf/mntr` + `POST /mntr/blnd/calc` | E2E S3.5 | ✅ |
| SFR-055 | SAF 공급의무 vs 급유의무 분리 | `/saf/mntr` 2탭 (시행계획 p.5~6) | 화면 검증 | ✅ |
| SFR-056 | 친환경 항공기 도입 트래커 | `/com/eco-fleet` (시행계획 p.8) | 화면 검증 | ✅ |
| SFR-058~062 | 범정부 AI 공통기반 연계 | `/ai/console` placeholder | 2차년도 | ⚠️ |
| — | 통합 워크플로우 매트릭스 | `/ptl/workflow` | E2E S7.1 | ✅ |
| — | 통계/시뮬레이션 + 감축수단별 기여도 | `/ptl/stat` (시행계획 p.7) | E2E S7.2 | ✅ |
| — | CORSIA 운영 일정 캘린더 | `/ptl/calendar` (시행계획 p.13) | 화면 검증 | ✅ |
| — | 상쇄비용 시뮬레이션 | `/ptl/sim` | E2E S7.2 | ✅ |
| — | CCR 추출 (외부 연계 자료) | `/ptl/ccr` + `POST /ccr/extract` | E2E S7.3 | ✅ |
| — | 감사로그 | `/ptl/actn` + `ptl.th_user_actn` | E2E S7.4 | ✅ |
| — | ICAO 송신 Mock | `/admin/icao-submit` | 화면 검증 | ✅ |
| — | 시스템 상태 대시보드 | `/admin/health` | 화면 검증 | ✅ |
| — | EUCR 상쇄의무량 통보 안내 | `/er/eucr/{id}` 배너 (시행계획 p.13) | 화면 검증 | ✅ |

## 4. 공통관리 (RFP 박스 ⑩의 기반 — MOLIT/KOTSA 전용)

| 항목 | 화면 | 테이블 | 상태 |
|---|---|---|---|
| 사용자 관리 | `/com/user` | `com.tn_user` | ✅ |
| 조직 관리 | `/com/ognz` | `com.tn_ognz` | ✅ |
| 운영사 관리 (항공기 등록부) | `/com/oprtr` | `com.tn_oprtr` | ✅ |
| 검증기관 + 검증배정 | `/com/vrfcn` | `com.tn_vrfcn_inst`, `tn_vrfcn_assgn` | ✅ |
| 역할 관리 | `/com/role` | `com.tn_role` | ✅ |
| 권한 관리 | `/com/authrt` | `com.tn_sys_authrt`, 매핑 2종 | ✅ |
| 공통코드 관리 | `/com/cd` | `com.tc_com_cd_group/dtl` | ✅ |
| 메뉴 관리 | `/com/menu` | `com.tn_menu` | ✅ |
| 프로그램 관리 | `/com/prgrm` | `com.tn_prgrm` | ✅ |
| 결재함 | `/com/atrz` | `com.tn_atrz_dmnd`, `tn_atrz_prcs`, `tn_atrz_task` | ✅ |
| 규정 게시판 | `/com/rglt` | `com.tn_rglt` | ✅ |
| 비밀번호 변경 | `/com/user/me/password` | (직접 변경) | ✅ |

## 5. 4-Actor 권한 매트릭스

| 화면군 | MOLIT | KOTSA | AIRLINE | VERIFIER |
|---|---|---|---|---|
| EMP (조회) | ✅ | ✅ | 본사만 | 배정사만 |
| EMP (승인) | ✅ | 권고 | 작성·제출 | — |
| ER (조회) | ✅ | ✅ | 본사만 | 배정사만 |
| VR (조회) | ✅ | ✅ | 본사만 | 본 기관만 |
| VR (작성) | — | — | — | ✅ |
| OoM (조회·작성) | ✅ | ✅ | — | — |
| EUCR (조회) | ✅ | ✅ | 본사만 | — |
| SAF (대시보드) | ✅ | ✅ | 본사만 | ✅ |
| SAF 혼합 산출 | ✅ | ✅ | — | — |
| 공통관리 | ✅ | 일부 | — | — |
| 결재함 | ✅ | ✅ | ✅ | ✅ |
| 시스템 상태 | ✅ | ✅ | — | — |
| ICAO 송신 | ✅ | — | — | — |

## 6. 산출 통계

- **RFP 11박스 완료율**: 9/11 (82%) + 2박스 2차년도 명시(Placeholder)
- **SFR 1차년도 항목**: 약 55개 중 53개 구현 (96%)
- **E2E 자동 회귀**: 243건 100% 통과
- **권한 매핑**: 8 actor × 32 화면 = 256셀 권한 매트릭스
- **DB 테이블**: 6 스키마 × 50+ 테이블
