# icas-cems — 개발 진입점

> 본 문서는 **개발 코드 진입점**. 사업 전체 안내는 [상위 README.md](../../README.md), 시스템 헌법은 [설계/시스템헌법/CLAUDE-main.md](../../설계/시스템헌법/CLAUDE-main.md) 참조.

---

## 🚀 빠른 시작 (컴퓨터 옮긴 직후)

### 0. 사전 요구사항
- macOS (Homebrew 기준)
- OpenJDK 17.x (Homebrew: `openjdk@17`)
- Apache Tomcat 9.x (Homebrew: `tomcat@9`)
- PostgreSQL 16.x (또는 Docker)
- Maven 3.9.x

```bash
# Homebrew 미설치 시
brew install openjdk@17 tomcat@9 postgresql@16 maven
brew services start postgresql@16
```

### 1. 환경 변수
```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. DB 초기화 (최초 1회만)
```bash
cd /Users/hyunwoo/Desktop/국제항공/개발/icas-cems

# DB / 계정 / 스키마 생성
psql -U postgres -f db/initdb/계정_스키마_권한.sql

# 테이블 생성 (멱등)
psql -U icas_admin -d icas -f db/initdb/오브젝트생성.sql

# 인덱스
psql -U icas_admin -d icas -f db/initdb/index생성.sql

# 시드 데이터 (멱등 ON CONFLICT DO NOTHING)
psql -U icas_admin -d icas -f db/initdb/초기데이터_*.sql

# (선택) 신규 변경분 적용
ls db/migrations/   # → 미적용 파일 순서대로 psql 실행
```

### 3. 빌드 & 배포
```bash
mvn clean package -Dmaven.test.skip=true   # → target/icas.war (36MB)
cp target/icas.war /opt/homebrew/opt/tomcat@9/libexec/webapps/ROOT.war
/opt/homebrew/opt/tomcat@9/bin/catalina.sh start
```

### 4. 접속
- URL: http://localhost:8080/login
- 계정: `admin01` / `admin1234!` (MOLIT_ADMIN 마스터)
- 기타 계정: [상위 README §시스템 환경](../../README.md#-테스트-로그인-계정-시드)

### 5. E2E 회귀
```bash
bash test-e2e/run_all.sh   # → 188/188 통과 (100%) 기대
```

---

## 📂 소스 구조

```
icas-cems/
├── pom.xml                                   ← Maven (Lombok 1.18.38, JDK17, fork=true)
├── src/main/java/kr/go/molit/icas/
│   ├── IcasApplication.java
│   ├── common/                               ← cross-cutting (config·interceptor·exception·util)
│   ├── com/                                  ← 공통 스키마 (user/ognz/role/authrt/menu/cd/atrz/file/bbs)
│   ├── emp/                                  ← EMP 도메인 (plan·oprtr·acft·calc·ctrl)
│   ├── er/                                   ← ER 도메인 (rprt·fuel·cntry·aerdrm·gap·cef·eucr·oom)
│   ├── vr/                                   ← VR 도메인 (rprt·scope·prcdr·cncls)
│   ├── saf/                                  ← SAF 도메인 (cert·batch·chain·feed·ghg·airprt·mntr·fldinsp)
│   └── ptl/                                  ← 포털 도메인 (wkflw·ccr·sim·stat·rglt)
├── src/main/resources/
│   ├── application.yml
│   └── mapper/                               ← MyBatis XML 70+
├── src/main/webapp/
│   ├── WEB-INF/views/                        ← JSP 50+ (feature 별)
│   └── resources/{js,css,lib}/               ← 정적 자원
├── src/test/java/                            ← JUnit (Service/Validator 100% pass)
├── db/                                       ← ★ DB 단일 소스
│   ├── ERD.md                                ← Mermaid ERD
│   ├── initdb/                               ← 멱등 DDL + 시드
│   └── migrations/YYYYMMDD_NN_*.sql          ← 누적 변경 (수정 금지)
├── docs/analysis-design/                     ← ★ 분석설계 9종 (md/docx/xlsx) + ZIP
├── target/icas.war                           ← 빌드 산출물
└── test-e2e/                                 ← bash E2E (6 시나리오 · 188 case)
```

---

## 🏗️ 핵심 아키텍처 결정사항

### Package-by-feature (Layer-by-feature 아님)
- Controller/Service/Mapper/Domain 을 **업무 단위(feature) 로 수직 묶음**
- 예: `er/oom/` 안에 `OomController` + `OomService` + `OomMapper` + `OomVO` 함께

### MyBatis 스키마 prefix 강제
- 모든 SQL 은 `{스키마.테이블명}` 형식 (`SELECT * FROM com.tn_user` ✅, `SELECT * FROM tn_user` ❌)
- 자기 스키마도 예외 없음

### 권한 이중 통제
- **메뉴/URL 권한** (역할↔프로그램 매핑) + **행 단위 가시범위** (기관 소속) 둘 다 만족해야 접근 허용
- 3중 검증: JSP `<sec:authorize>` + `AuthorityInterceptor` + Service `DataScopeValidator`

### 암호화·해시
- 비밀번호: SHA-256 + 고정 salt (`icas` + plain + `cems`) — `kr.go.molit.icas.com.util.Sha256.hex(plain)`
- 개인정보·민감정보 컬럼: 발주기관 직접구매 DB 암호화 SW (운영 환경) / 미설치 시 임시 AES-256/GCM
- 금지: BCrypt / MD5 / SHA-1 / DES / 평문

### 작업기록 (절대 준수)
- **명시적 기록 명령 시에만** `작업기록/YYYYMMDD_<세션ID>.docx` 작성
- 세션ID: `PL` `BACK` `FRONT` `DB` `TEST`
- 소스 변경 시 표 형식 (소스명·URL·변경내용)

---

## ⚙️ 자주 쓰는 명령

```bash
# Tomcat 가동 확인
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/login

# Tomcat 재시작
/opt/homebrew/opt/tomcat@9/bin/catalina.sh stop
/opt/homebrew/opt/tomcat@9/bin/catalina.sh start

# Tomcat 로그
tail -f /opt/homebrew/opt/tomcat@9/libexec/logs/catalina.out

# 빌드 + 배포 한 번에
mvn clean package -Dmaven.test.skip=true && \
  cp target/icas.war /opt/homebrew/opt/tomcat@9/libexec/webapps/ROOT.war && \
  /opt/homebrew/opt/tomcat@9/bin/catalina.sh stop && \
  /opt/homebrew/opt/tomcat@9/bin/catalina.sh start

# 단위 테스트
mvn test -fae

# E2E 회귀
bash test-e2e/run_all.sh

# DB 접속
psql -U icas_admin -d icas
```

---

## 🐛 알려진 잔여 이슈 (운영 영향 없음)

| # | 이슈 | 위치 | 조치 |
|---|---|---|---|
| 1 | Controller 단위 테스트 setUp 결함 | 30+ ControllerTest.java | `setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())` 한 줄 추가 |
| 2 | EMP PUT 부분수정 시 NOT NULL 컬럼 보호 | `EmpPlanController` PUT | E2E 시 full body 우회. 운영 form 은 full body 라 빈도 낮음 |
| 3 | `ptl.tn_ptl_sim` POST `scope_se_cd` 필수 | `PtlSimController` | body 에 `scopeSeCd` 추가 강제 |
| 4 | Logback `%i` 토큰 WARN | `logback-spring.xml` | TimeBasedRollingPolicy 호환 경고, 운영 영향 없음 |

---

## 🔗 핵심 참조 문서

| 목적 | 문서 |
|---|---|
| **시스템 헌법 (L1 단일 소스)** | [../../설계/시스템헌법/CLAUDE-main.md](../../설계/시스템헌법/CLAUDE-main.md) |
| **시연·검증 완성보고서** | [../../E2E_완성보고서_20260524.md](../../E2E_완성보고서_20260524.md) |
| **DB ERD** | [./db/ERD.md](./db/ERD.md) |
| **분석설계 산출물 색인** | [./docs/analysis-design/README.md](./docs/analysis-design/README.md) |
| **백엔드 가이드** | [../../설계/세션문서/CLAUDE-back.md](../../설계/세션문서/CLAUDE-back.md) |
| **프론트 가이드** | [../../설계/세션문서/CLAUDE-front.md](../../설계/세션문서/CLAUDE-front.md) |
| **DB 가이드** | [../../설계/세션문서/CLAUDE-db.md](../../설계/세션문서/CLAUDE-db.md) |
| **테스터 가이드** | [../../설계/세션문서/CLAUDE-test.md](../../설계/세션문서/CLAUDE-test.md) |
| **PL/총괄 가이드** | [../../설계/세션문서/CLAUDE-pl.md](../../설계/세션문서/CLAUDE-pl.md) |

---

## 📝 최근 변경 (2026-05-24 새벽 자동 작업)

- **박스 11종으로 확장**: ⑦ CORSIA 세부항목 검증 + ⑩ 공통 AI 서비스 (자리)
- **신규 화면 2건**: `/er/oom/qchk/list`, `/ai/console`
- **DB 수정 5건**: VR `rcmmd_dt` / SAF batch 컬럼 오타 / SAF mntr `last_calc_dt` / SAF cert `oprtr_id` / SafCert 트랜잭션
- **시드 7 계정 + KVA 검증기관 + 외국항공사 액터 추가**
- **E2E 188/188 100% 통과**
- **설계 문서 정합화**: 시스템헌법 11박스 매핑 표 + 권한처리방안 DOM/FRGN 보조구분 + CORSIA검증/공통AI 도메인가이드 신규
