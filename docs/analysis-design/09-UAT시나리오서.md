# UAT 시나리오서 (User Acceptance Test)

> **시스템**: ICAS-CEMS | **검증 시점**: 인수 단계 | **검증자**: 국토부 + 한국교통안전공단

## 1. 인수 환경 정보

| 항목 | 정보 |
|---|---|
| URL | http://(서버주소):8080 |
| 검증 기간 | 5 영업일 (월~금) |
| 검증 대상 | RFP 11박스 + 8 데이터 흐름 시나리오 + 분석설계 9 문서 |
| 검증자 (Actor) | MOLIT 관리자 1명 + KOTSA 검토자 1명 + 항공사 담당자 1명 + 검증기관 1명 |
| 검증 통과 기준 | 시나리오 32 케이스 中 30 이상 통과 (94%+) |

### 1.1 검증 계정

| Actor | userID | password | 기관 |
|---|---|---|---|
| MOLIT 관리자 | admin01 | admin1234! | 국토교통부 |
| KOTSA 검토자 | kotsa01 | admin1234! | 한국교통안전공단 |
| 항공사 담당자 | kal_user | admin1234! | 대한항공 |
| 검증기관 팀장 | vrf_lead | gn12345! | 한국검증협회 |

---

## 2. UAT 시나리오 매트릭스 (8 시나리오 × 4 케이스 = 32 케이스)

### S1. 운영 셋업 (Pre-flight) — MOLIT 검증

| ID | 시나리오 | 기대 결과 | 통과 기준 |
|---|---|---|---|
| UAT-S1-01 | admin01 로그인 → 사용자 관리 진입 | 14명 시드 사용자 표시 | 표 정상 + 검색 동작 |
| UAT-S1-02 | 사용자 등록 모달 (testuser01) | 모달 7필드 입력 → 저장 → 표 갱신 | 신규 행 노출 |
| UAT-S1-03 | 권한 매트릭스 (역할 ↔ 사용자 매핑) | 8 역할 × 14 사용자 표시 | 매핑 1건 추가 가능 |
| UAT-S1-04 | 결재함 / 시스템 상태 진입 | 결재 5건 + 시스템 UP + JVM 메모리 | 정상 가동 |

### S2. EMP 라이프사이클 (① EMP)

| ID | 시나리오 | 액터 전환 | 통과 기준 |
|---|---|---|---|
| UAT-S2-01 | kal_user EMP 새 버전 생성 | AIR | empPlanId 생성 + DRAFT |
| UAT-S2-02 | DRAFT 수정 → 제출 (DRAFT→SBMTD) | AIR | 상태 변경 + 결재 알림 |
| UAT-S2-03 | kotsa01 검토→권고 (SBMTD→RVWNG→RCMDD) | AIR→KOT | 권고일자 적재 |
| UAT-S2-04 | admin01 최종 승인 (RCMDD→APRVD) | KOT→MOL | 승인일자 적재 + 차년도 ER 자격 확보 |

### S3. SAF 공급망 + 혼합비율 (⑧ SAF)

| ID | 시나리오 | 통과 기준 |
|---|---|---|
| UAT-S3-01 | kal_user SAF 인증서 등록 (cert 신규) | certId + ISCC_CORSIA + 만료일 |
| UAT-S3-02 | SAF 배치 + 자식 5종 (ghg/feed/blndr/prdc/sply) | batchId + 5 자식 입력 |
| UAT-S3-03 | 공항별 급유·구매 upsert (RKSI/KAL/2026) | tn_saf_airprt_fuel UPSERT |
| UAT-S3-04 | kotsa01 혼합비율 산출 + 의무이행 판정 | KAL 1.30% 이행 ✓ |

### S4. CEF 청구·차감 (③ CEF)

| ID | 시나리오 | 통과 기준 |
|---|---|---|
| UAT-S4-01 | kal_user CEF 신규 등록 (ER0001 부속) | cefId DRAFT |
| UAT-S4-02 | 이중청구 사전 검증 패널 (batchIdNo) | 충돌 없음 메시지 |
| UAT-S4-03 | CEF 제출 → kotsa01 승인 | SBMTD → APRVD |
| UAT-S4-04 | ER 차감 데이터 확정 | tn_er.net_fuel_qty 갱신 |

### S5. ER + VR + OoM (② ④ ⑥ ⑦)

| ID | 시나리오 | 통과 기준 |
|---|---|---|
| UAT-S5-01 | ER 신규 + 8 자식 입력 (kal_user) | erId + 자식 8건 |
| UAT-S5-02 | vrf_lead VR 작성 + 7 자식 + 제출 | vrId + 결론 REASONABLE |
| UAT-S5-03 | kotsa01 OoM 작성 + Rule 18종 실행 | 18 항목 INSERT + PASS/WARN/FAIL 카운트 |
| UAT-S5-04 | OoM finalize PASS → admin01 ER 승인 | ER 상태 APRVD |

### S6. EUCR 배출권 취소 (⑤ EUCR)

| ID | 시나리오 | 통과 기준 |
|---|---|---|
| UAT-S6-01 | kal_user EUCR 신규 (ofstReqQty=1500) | eucrId DRAFT |
| UAT-S6-02 | 일련번호 이중사용 사전 검증 (3건) | "충돌 없음. 검사 3건 통과" |
| UAT-S6-03 | 배치 등록 + 크레딧 일련번호 확정 | tn_eucr_batch + crdt_dtl |
| UAT-S6-04 | 의무량 수정 → recalc → submit | ttl_cncl_qty 갱신 |

### S7. 포털 통합 소비 (⑨)

| ID | 시나리오 | 통과 기준 |
|---|---|---|
| UAT-S7-01 | admin01 통합 워크플로우 매트릭스 | 12 운영사 × 7 도메인 상태 |
| UAT-S7-02 | 시뮬레이션 실행 | 4개 연도 차트 + 이력 저장 |
| UAT-S7-03 | CCR 추출 (rprtYr=2026, scope=ALL) | 추출 결과 표시 |
| UAT-S7-04 | 감사로그 조회 + CORSIA 운영 일정 | 5W1H 로그 + 12 작업 타임라인 |

### S8. 권한·결재 횡단 (⑩ Cross-cutting)

| ID | 시나리오 | 통과 기준 |
|---|---|---|
| UAT-S8-01 | 4-actor 사이드바 메뉴 가시 차이 | actor별 메뉴 동적 노출 |
| UAT-S8-02 | kal_user → 타사 EMP 접근 차단 | 403 또는 404 |
| UAT-S8-03 | 비밀번호 5회 실패 → 계정 잠금 | acnt_lock_yn=Y |
| UAT-S8-04 | 비밀번호 변경 → 강제 로그아웃 | 5초 카운트 + /login 리다이렉트 |

---

## 3. UAT 케이스 상세 (대표 5건)

### UAT-S2-04: MOLIT 최종 승인

**선행 조건**: S2-03 까지 완료 (EMP RCMDD 상태)

| 단계 | 동작 | 화면 | 기대 |
|---|---|---|---|
| 1 | admin01 로그인 | /login | /main 진입 |
| 2 | 헤더 알림 벨 클릭 | 🔔 | 결재함 진입 + 펜딩 1건 |
| 3 | 결재 행 "처리" 클릭 | /com/atrz | EMP 상세 진입 |
| 4 | 라이프사이클 RCMDD 확인 | EMP 상세 | 4번째 단계 활성 |
| 5 | "승인" 버튼 클릭 | EMP 상세 | API POST /api/emp/plan/{id}/approve |
| 6 | 상태 갱신 | EMP 상세 | "승인" 라벨 + 승인일자 노출 |
| 7 | DB 검증 (선택) | psql | emp_st_cd='APRVD' AND aprv_dt IS NOT NULL |

**통과 기준**: 1~6 모두 정상 + 감사로그 적재 확인

### UAT-S5-03: KOTSA OoM Rule 18종 실행

**선행 조건**: ER 제출 + VR 승인 완료

| 단계 | 동작 | 기대 |
|---|---|---|
| 1 | kotsa01 로그인 → OoM 목록 | 신규 OoM 1건 표시 |
| 2 | 상세 진입 | OoM 기본정보 + 비행장쌍 3건 + 국가쌍 3건 자동 매핑 |
| 3 | "Rule 18종 실행" 버튼 | run-quant API 200 |
| 4 | 항목 탭 진입 | 18 행 (R001~R018) 표시 |
| 5 | 결과 통계 카드 | 총 18 / PASS / WARN / FAIL 카운트 |
| 6 | 항목별 상세 | 룰명·기대값·실제값·결과 |

**통과 기준**: 18 항목 모두 결과 산출 + PASS/FAIL 표시

### UAT-S6-02: EUCR 일련번호 이중사용 검증

| 단계 | 동작 | 기대 |
|---|---|---|
| 1 | kal_user EUCR0001 상세 진입 | 일련번호 이중사용 사전 검증 패널 노출 |
| 2 | 텍스트 영역 입력 (3건): VCS-2024-001 ~ 003 | (줄단위) |
| 3 | "검증 실행" 버튼 | POST /api/er/eucr/validate-double-using |
| 4 | 결과: 초록 패널 | "✓ 이중사용 충돌 없음. 검사 3건 통과" |
| 5 | 일부러 중복 일련번호 입력 (다른 EUCR에 등록된 것) | "❌ 이중사용 충돌. 차단" |

### UAT-S8-03: 5회 실패 → 계정 잠금

| 단계 | 동작 | 기대 |
|---|---|---|
| 1 | testuser01 로 잘못된 비밀번호 4회 시도 | 매번 "남은 시도 N회" 메시지 |
| 2 | 5회째 잘못된 비밀번호 | "🔒 계정이 잠겼습니다." |
| 3 | DB 검증 | acnt_lock_yn='Y', pswd_fail_cnt=5 |
| 4 | admin01 로그인 → 사용자 관리 → testuser01 unlock | POST /api/com/user/testuser01/unlock |
| 5 | testuser01 정상 로그인 | acnt_lock_yn='N' 복원 |

### UAT-ADM-01: ICAO 송신 Mock

| 단계 | 동작 | 기대 |
|---|---|---|
| 1 | admin01 → ICAO 송신 콘솔 진입 | Mock 안내 배너 표시 |
| 2 | 보고연도 2025 + 운영사 KAL/AAR/JJA + 채널 CCR | 선택 완료 |
| 3 | "미리보기" 버튼 | ICAO XML 표준 페이로드 표시 |
| 4 | "송신 (Mock)" 버튼 | 확인 모달 → 송신 이력 1건 추가 |
| 5 | 이력 행 | RefId: MOCK-{timestamp}, 200 OK 표시 |

---

## 4. 비기능 검증 항목

| 항목 | 검증 방법 | 통과 기준 |
|---|---|---|
| 성능 — 조회 응답 | 브라우저 개발자도구 Network 탭 | < 2초 |
| 성능 — 등록·수정 | 동상 | < 3초 |
| 성능 — Rule 18종 실행 | OoM run-quant 시간 측정 | < 10초 |
| 보안 — 5회 잠금 | 의도적 5회 실패 | 5회째 잠금 |
| 보안 — 세션 만료 | 25분 대기 → 토스트 / 30분 대기 → 자동 로그아웃 | 알림 + 리다이렉트 |
| 보안 — XSS | 입력 필드에 `<script>alert(1)</script>` | 출력 시 escape |
| 호환성 — Chrome 100+ | 모든 화면 진입 | 깨짐 없음 |
| 호환성 — Edge 100+ | 동상 | 동상 |

---

## 5. UAT 통과 판정 양식

| 시나리오 | 케이스 수 | 통과 | 실패 | 통과율 | 비고 |
|---|---|---|---|---|---|
| S1 운영 셋업 | 4 | | | | |
| S2 EMP 라이프사이클 | 4 | | | | |
| S3 SAF | 4 | | | | |
| S4 CEF | 4 | | | | |
| S5 ER+VR+OoM | 4 | | | | |
| S6 EUCR | 4 | | | | |
| S7 포털 | 4 | | | | |
| S8 권한·결재 횡단 | 4 | | | | |
| **합계** | **32** | | | | |

**인수 통과 기준**: 30/32 이상 통과 (94%+)

---

## 6. 결함 보고 양식

| ID | 시나리오 | 단계 | 기대 | 실제 | 심각도 | 조치 |
|---|---|---|---|---|---|---|
| BUG-001 | UAT-S?-? | n | ... | ... | High/Mid/Low | 수정 후 재검증 |

---

## 7. UAT 사전 준비 체크리스트

- [ ] DB 시드 cleanup 수행 (`bash test-e2e/run_all.sh` 멱등성 보장)
- [ ] 4-actor 계정 활성 + 비밀번호 정상
- [ ] 결재 펜딩 시드 5건 적재
- [ ] SAF 시드 60M L · 운영사 12사 · 12 EMP/ER/VR 시드
- [ ] 매뉴얼(/manual) 접근 가능
- [ ] 시스템 상태(/admin/health) UP 확인
