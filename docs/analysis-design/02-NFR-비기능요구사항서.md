# NFR — 비기능 요구사항서 (Non-Functional Requirements)

> **시스템**: 국제항공 탄소 배출량 관리시스템(ICAS-CEMS)
> **작성일**: 2026-05-24 | **버전**: v1.0

## 1. 성능 (Performance)

| ID | 요구사항 | 측정 기준 | 달성 방법 |
|---|---|---|---|
| NFR-P-01 | **응답시간 (조회)** | 평균 ≤ 2초, 95%ile ≤ 5초 | PostgreSQL 인덱스 + MyBatis 페이징 |
| NFR-P-02 | **응답시간 (등록·수정)** | 평균 ≤ 3초 | 트랜잭션 단일화 + soft-delete |
| NFR-P-03 | **TPS (동시 처리)** | 50 TPS (보고기 t+1 7월 집중) | Tomcat 200 thread + HikariCP 30 conn |
| NFR-P-04 | **대용량 조회** | 1만 행 그리드 ≤ 5초 | 페이지네이션 강제 (20/page) |
| NFR-P-05 | **CORSIA Rule 18종 실행** | 1 ER 기준 ≤ 10초 | OomQuantCheckService 단일 트랜잭션 |
| NFR-P-06 | **첫 화면 로드** | First Paint ≤ 1.5초 | Bootstrap·jQuery CDN + Pretendard preload |

## 2. 가용성 (Availability)

| ID | 요구사항 | 기준 | 달성 방법 |
|---|---|---|---|
| NFR-A-01 | **연간 가동율** | 99.5% (연 43.8h 다운타임 허용) | Tomcat 9 + PostgreSQL Hot Standby |
| NFR-A-02 | **RPO** (복구 목표 시점) | ≤ 1시간 | 시간별 WAL 아카이브 + 일 1회 pg_dump |
| NFR-A-03 | **RTO** (복구 목표 시간) | ≤ 4시간 | 핫 스탠바이 + 자동 페일오버 (운영 단계) |
| NFR-A-04 | **백업 보관** | 일 1회, 30일 보관 | `/opt/icas/backup/` + S3 외부 보관 |
| NFR-A-05 | **점검 시간** | 매주 일요일 02:00~04:00 | 사전 공지 + `/admin/health` 표시 |

## 3. 확장성 (Scalability)

| ID | 요구사항 | 목표 | 비고 |
|---|---|---|---|
| NFR-S-01 | **동시 사용자** | 100명 (1차) → 500명 (5년차) | Stateless API → 수평 확장 가능 |
| NFR-S-02 | **데이터 증가** | 연 50만 건 (모든 도메인 합계) | 파티셔닝 (보고연도별) 준비 |
| NFR-S-03 | **저장 용량** | 5년차 250GB | 압축 + 감사로그 자동 정리 (5년 초과) |
| NFR-S-04 | **외항사 확장** | 1차 국적사 12 → 2차 외항사 200+ | 운영사 마스터 무제한 INSERT 가능 |

## 4. 보안 (Security)

| ID | 요구사항 | 구현 |
|---|---|---|
| NFR-SEC-01 | **인증** | Spring Security + 비밀번호 SHA-256 해시 |
| NFR-SEC-02 | **비밀번호 정책** | 8자 + 영문+숫자+특수문자 (UI 검증) |
| NFR-SEC-03 | **계정 잠금** | 5회 실패 → 자동 잠금 + 관리자 unlock |
| NFR-SEC-04 | **세션 관리** | 30분 idle 만료 + 25분 경고 토스트 |
| NFR-SEC-05 | **CSRF** | CookieCsrfTokenRepository + 전역 AJAX 자동 첨부 |
| NFR-SEC-06 | **권한 제어** | 8 actor × 32 화면 RBAC + AuthorityInterceptor |
| NFR-SEC-07 | **SQL Injection** | MyBatis #{} 파라미터 바인딩 100% |
| NFR-SEC-08 | **XSS** | IcasEsc 출력 이스케이프 유틸 |
| NFR-SEC-09 | **감사로그** | `ptl.th_user_actn` 모든 변경 적재 (5년 보관) |
| NFR-SEC-10 | **데이터 분리** | DataScopeValidator — 운영사·검증기관 가시 범위 검증 |
| NFR-SEC-11 | **HTTPS** | 운영 단계 HSTS + TLS 1.3 (인프라 책임) |

## 5. 사용성 (Usability)

| ID | 요구사항 | 구현 |
|---|---|---|
| NFR-U-01 | **반응형** | 데스크탑 1280px+ 최적화 (관리자 화면) |
| NFR-U-02 | **브라우저 호환** | Chrome 100+, Edge 100+, Safari 15+ |
| NFR-U-03 | **한글 최적화** | Pretendard 폰트 + UTF-8 |
| NFR-U-04 | **빠른 화면 이동** | Cmd/Ctrl+K 전역 검색 팔레트 (33 화면) |
| NFR-U-05 | **알림** | 결재 펜딩 자동 폴링 (1분 주기) + IcasAlert 토스트 |
| NFR-U-06 | **WCAG 2.1 AA** | 의미적 HTML + ARIA 라벨 + 키보드 접근성 |
| NFR-U-07 | **에러 처리** | GlobalExceptionHandler 통합 + 사용자 친화 메시지 |

## 6. 호환성·운영 (Compatibility & Operations)

| ID | 요구사항 | 구현 |
|---|---|---|
| NFR-O-01 | **OS** | Linux (CentOS 7+, Ubuntu 20+) — 운영 / macOS (개발) |
| NFR-O-02 | **JDK** | OpenJDK 17 LTS |
| NFR-O-03 | **DB** | PostgreSQL 16 (UTF-8 ko_KR.UTF-8) |
| NFR-O-04 | **WAS** | Apache Tomcat 9.x (Servlet 4.0) |
| NFR-O-05 | **프레임워크** | Spring 5.3 + 전자정부 표준프레임워크 4.x |
| NFR-O-06 | **빌드** | Maven 3.9+ |
| NFR-O-07 | **모니터링** | `/admin/health` 자체 대시보드 (운영 시 Prometheus+Grafana 권장) |
| NFR-O-08 | **로그** | Logback + 일 단위 롤링 (`logs/icas-YYYY-MM-DD.log`) |

## 7. 법규·표준 준수 (Compliance)

| ID | 요구사항 | 근거 |
|---|---|---|
| NFR-C-01 | **국제항공 탄소 배출량 관리에 관한 법률** | 법률 §5(기본계획), §6(EMP), §7(ER), §9(OoM) |
| NFR-C-02 | **ICAO CORSIA SARP** | MRV 5~7월, 상쇄의무 11월 통보 (시행계획 p.13) |
| NFR-C-03 | **개인정보보호법** | 사용자 비밀번호 해시·세션 만료·5회 잠금 |
| NFR-C-04 | **공공기관 웹 접근성 지침 (KWCAG 2.1)** | AA 등급 목표 |
| NFR-C-05 | **전자정부 표준프레임워크 4.x** | 채택 |
| NFR-C-06 | **공통컴포넌트 표준 (행안부)** | 사용자·권한·감사로그 패턴 준수 |

## 8. 측정 도구 (Verification Tools)

| 영역 | 도구 | 검증 |
|---|---|---|
| 성능 | JMeter / Apache Bench | 50 TPS 시뮬레이션 |
| 보안 | OWASP ZAP | XSS / SQL Injection 자동 점검 |
| 접근성 | Lighthouse Accessibility | 점수 ≥ 90 |
| 코드 품질 | SonarQube | 신규 코드 커버리지 ≥ 80% |
| E2E | 자체 8 시나리오 스크립트 | 243건 100% |
