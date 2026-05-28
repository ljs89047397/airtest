# DB ERD (Entity-Relationship Diagram)

> **DBMS**: PostgreSQL 16 | **스키마**: 6개 (com/emp/er/vr/saf/ptl) | **테이블**: 81개

## 1. 스키마 개요

| 스키마 | 테이블 수 | RFP 박스 |
|---|---|---|
| `com` | 27 | ⑩ 공통관리 (사용자·기관·권한·결재·코드·파일) |
| `emp` | 10 | ① EMP (모니터링 계획) |
| `er` | 19 | ② ER + ③ CEF + ⑤ EUCR + ⑥ OoM + ⑦ CORSIA |
| `vr` | 8 | ④ VR (검증보고서) |
| `saf` | 13 | ⑧ SAF (지속가능항공유) |
| `ptl` | 4 | ⑨ 포털 (시뮬·CCR·통계·감사로그) |

## 2. 명명 규칙

| 접두사 | 의미 | 예 |
|---|---|---|
| `tn_` | 트랜잭션 (수정 가능) | `tn_emp_plan`, `tn_er` |
| `tc_` | 코드성 (마스터·고정) | `tc_com_cd_group`, `tc_cntry_cd` |
| `th_` | 이력 (append only) | `th_emp_chg_hstry`, `th_user_actn` |

**컬럼 규칙**: snake_case + 도메인 접두사
- 공통 컬럼: `frst_reg_dt`, `frst_reg_user_id`, `last_chg_dt`, `last_chg_user_id`, `use_bgng_dt`, `use_end_dt`
- 소프트 삭제: `WHERE use_end_dt > NOW()`

---

## 3. com 스키마 ERD — 공통관리

```mermaid
erDiagram
    tn_ognz ||--o{ tn_user : "기관 소속"
    tn_ognz ||--o{ tn_oprtr : "운영사 기관"
    tn_ognz ||--o{ tn_vrfcn_inst : "검증기관"
    tn_user ||--o{ tn_user_role_mpng : "사용자-역할"
    tn_role ||--o{ tn_user_role_mpng : ""
    tn_role ||--o{ tn_sys_authrt_role_mpng : "역할-권한"
    tn_sys_authrt ||--o{ tn_sys_authrt_role_mpng : ""
    tn_sys_authrt ||--o{ tn_sys_authrt_prgrm_mpng : "권한-프로그램"
    tn_prgrm ||--o{ tn_sys_authrt_prgrm_mpng : ""
    tn_prgrm ||--o{ tn_sys_menu : "프로그램-메뉴"
    tn_atrz_task ||--o{ tn_atrz_dmnd : "결재 업무 정의"
    tn_atrz_dmnd ||--o{ tn_atrz_prcs : "결재 요청-처리"
    tn_user ||--o{ tn_atrz_dmnd : "요청자"
    tn_user ||--o{ tn_atrz_prcs : "결재자"
    tn_vrfcn_inst ||--o{ tn_vrfcn_assgn : "검증 배정"
    tn_oprtr ||--o{ tn_vrfcn_assgn : ""
    tc_com_cd_group ||--o{ tc_com_cd_dtl : "코드 그룹-상세"
    tn_user ||--o{ th_logn_hstry : "로그인 이력"

    tn_user {
        VARCHAR user_id PK
        VARCHAR user_nm
        VARCHAR pswd_hash
        VARCHAR ognz_id FK
        INT pswd_fail_cnt
        CHAR acnt_lock_yn
        CHAR master_yn
    }
    tn_ognz {
        VARCHAR ognz_id PK
        VARCHAR ognz_nm
        VARCHAR ognz_se_cd "MOLIT/KOTSA/AIRLINE/VERIFIER"
    }
    tn_oprtr {
        VARCHAR oprtr_id PK
        VARCHAR oprtr_nm
        VARCHAR icao_desig "ICAO 지정어"
        VARCHAR ognz_id FK
    }
```

### com 주요 테이블
| 테이블 | 용도 | 핵심 컬럼 |
|---|---|---|
| `tn_user` | 시스템 사용자 | user_id, pswd_hash, ognz_id, pswd_fail_cnt, acnt_lock_yn |
| `tn_ognz` | 기관 (MOLIT/KOTSA/AIRLINE/VERIFIER) | ognz_id, ognz_se_cd |
| `tn_oprtr` | 운영사 (항공사) | oprtr_id, icao_desig |
| `tn_vrfcn_inst` | 검증기관 (KVA·선급·인정원) | vrfcn_inst_id |
| `tn_vrfcn_assgn` | 검증기관 ↔ 운영사 배정 | vrfcn_inst_id + oprtr_id + rprt_yr |
| `tn_role`, `tn_sys_authrt`, `tn_prgrm` + 3개 매핑 | RBAC 3단계 (사용자→역할→권한→프로그램) | api_path_prefix |
| `tn_atrz_task/dmnd/prcs` | 결재 (업무 정의·요청·처리) | atrz_dmnd_id, atrz_st_cd |
| `tc_com_cd_group/dtl` | 공통코드 | grp_id, cd, cd_nm |

---

## 4. emp 스키마 ERD — ① EMP (모니터링 계획)

```mermaid
erDiagram
    tn_emp_plan ||--o| tn_emp_oprtr_info : "운영사 정보"
    tn_emp_plan ||--o{ tn_emp_acft : "항공기"
    tn_emp_plan ||--o{ tn_emp_oprtr_cnct : "운영자 연결"
    tn_emp_plan ||--o{ tn_emp_cntry_pair : "국가쌍"
    tn_emp_plan ||--o| tn_emp_co2_calc : "CO2 계산방법"
    tn_emp_plan ||--o{ tn_emp_co2_detail : "CO2 상세"
    tn_emp_plan ||--o| tn_emp_data_ctrl : "데이터 품질"
    tn_emp_plan ||--o{ tn_emp_risk : "위험 항목"
    tn_emp_plan ||--o{ th_emp_chg_hstry : "변경 이력"

    tn_emp_plan {
        VARCHAR emp_plan_id PK "EMP[YYYY][OPRTR]"
        VARCHAR oprtr_id FK
        VARCHAR rprt_yr "보고연도"
        VARCHAR emp_ver "버전"
        VARCHAR emp_st_cd "DRAFT/SBMTD/RVWNG/RCMDD/APRVD/RJCTD/CNCLD"
        CHAR sig_chg_yn "중요 변경 여부"
        TIMESTAMP sbmt_dt
        TIMESTAMP aprv_dt
    }
```

---

## 5. er 스키마 ERD — ② ER + ③ CEF + ⑤ EUCR + ⑥ OoM

```mermaid
erDiagram
    tn_er ||--o{ tn_er_acft_fuel : "항공기-연료"
    tn_er ||--o{ tn_er_cntry_pair_co2 : "국가쌍 CO2"
    tn_er ||--o{ tn_er_aerdrm_pair_co2 : "비행장쌍 CO2"
    tn_er ||--o| tn_er_fuel_smry : "연료 요약"
    tn_er ||--o{ tn_er_data_gap : "데이터 갭"
    tn_er ||--o| tn_er_afbr : "AFBR"
    tn_er ||--o| tn_er_vrfr_info : "검증기관 정보"
    tn_er ||--o| tn_cef : "CEF 청구 (1:1)"
    tn_cef ||--o{ tn_cef_claim : "청구 라인"
    tn_cef ||--o{ tn_cef_lcyc : "수명주기"
    tn_cef ||--o{ tn_cef_spchn : "공급망"
    tn_eucr ||--o{ tn_eucr_batch : "배치"
    tn_eucr ||--o{ tn_eucr_crdt_dtl : "크레딧 일련번호"
    tn_oom_check ||--o{ tn_oom_check_item : "Rule 18종"
    tn_oom_check ||--o{ tn_oom_check_addl_rqst : "추가 요청"
    tn_oom_check ||--o{ tn_oom_check_vrfr_eval : "검증기관 평가"
    tn_er ||--o| tn_oom_check : "OoM 검토 대상"

    tn_er {
        VARCHAR er_id PK "ER[YYYY][OPRTR]"
        VARCHAR oprtr_id
        VARCHAR rprt_yr
        VARCHAR er_st_cd "라이프사이클"
    }
    tn_cef {
        VARCHAR cef_id PK
        VARCHAR er_id FK
        VARCHAR oprtr_id
        DECIMAL net_fuel_qty "순연료질량"
    }
    tn_eucr {
        VARCHAR eucr_id PK
        VARCHAR oprtr_id
        DECIMAL ofst_req_qty "상쇄의무량"
        DECIMAL ttl_cncl_qty "총취소량"
        CHAR fulfilled_yn
    }
    tn_oom_check {
        VARCHAR oom_id PK
        VARCHAR er_id FK
        VARCHAR oom_st_cd
        VARCHAR rslt_cd "PASS/FAIL/HOLD"
    }
```

---

## 6. vr 스키마 ERD — ④ VR (검증보고서)

```mermaid
erDiagram
    tn_vr ||--o| tn_vr_scope : "검증 범위"
    tn_vr ||--o{ tn_vr_team : "검증팀"
    tn_vr ||--o| tn_vr_time : "시간 투입"
    tn_vr ||--o| tn_vr_input_info : "입력 정보"
    tn_vr ||--o{ tn_vr_prcdr : "검증 절차"
    tn_vr ||--o{ tn_vr_ncnfrm : "부적합 사항"
    tn_vr ||--o| tn_vr_cncls : "결론 (의견)"

    tn_vr {
        VARCHAR vr_id PK "VR[YYYY][OPRTR]"
        VARCHAR oprtr_id
        VARCHAR rprt_yr
        VARCHAR er_id "연계 ER"
        VARCHAR vrfcn_inst_id "검증기관"
        VARCHAR vr_type_cd "ER/EMP"
        VARCHAR vr_ver
        VARCHAR vr_st_cd "라이프사이클"
    }
    tn_vr_cncls {
        VARCHAR vr_id PK
        VARCHAR final_opnn_cd "REASONABLE/LIMITED/ADVERSE"
        TEXT opnn_cn
    }
    tn_vr_ncnfrm {
        VARCHAR vr_id PK
        INT seq PK
        VARCHAR rslv_st "OPEN/CLOSED"
    }
```

---

## 7. saf 스키마 ERD — ⑧ SAF

```mermaid
erDiagram
    tn_saf_batch ||--o{ tn_saf_cert : "배치 인증서"
    tn_saf_batch ||--o| tn_saf_ghg : "GHG"
    tn_saf_batch ||--o{ tn_saf_feed : "원료 (피드스톡)"
    tn_saf_feed ||--o{ tn_saf_feed_orgn : "원료 출처"
    tn_saf_batch ||--o{ tn_saf_blndr : "혼합사"
    tn_saf_batch ||--o{ tn_saf_prdc_sply : "생산·공급망"
    tn_saf_cert ||--o{ tn_saf_cert_audit : "인증서 감사"
    tn_saf_blnd_mntr ||--o{ tn_saf_tankering_mntr : "탱커링"

    tn_saf_batch {
        VARCHAR batch_id PK "자연키"
        VARCHAR oprtr_id
        DECIMAL batch_qty
    }
    tn_saf_cert {
        VARCHAR cert_id PK
        VARCHAR batch_id FK
        VARCHAR cert_no "인증서 번호"
        VARCHAR cert_schm_cd "ISCC_CORSIA / RSB"
        DATE cert_xpr_dt "만료일"
        CHAR srrnd_yn "회수 여부"
    }
    tn_saf_airprt_fuel {
        VARCHAR airprt_cd PK "ICAO 공항코드"
        VARCHAR oprtr_id PK
        VARCHAR rprt_yr PK
        INT flt_cnt "항공편 수"
        DECIMAL actl_fuel_qty
    }
    tn_saf_blnd_mntr {
        VARCHAR oprtr_id PK
        VARCHAR rprt_yr PK
        DECIMAL blnd_ratio "혼합비율 %"
        CHAR fulfilled_yn "의무 이행"
    }
```

---

## 8. ptl 스키마 ERD — ⑨ 포털

```mermaid
erDiagram
    tn_ptl_sim {
        VARCHAR sim_id PK "SM[순번]"
        VARCHAR sim_nm
        VARCHAR owner_user_id
        VARCHAR base_yr
        JSONB input_json
        JSONB rslt_json
        VARCHAR share_se_cd "PRIVATE/ORG/PUBLIC"
    }
    tn_ptl_ccr_extr {
        VARCHAR extr_id PK
        VARCHAR rprt_yr
        VARCHAR extr_scope_cd "ALL/OPRTR"
        TIMESTAMP extr_dt
    }
    tn_ptl_stat_yearly {
        VARCHAR rprt_yr PK
        VARCHAR oprtr_id PK
        DECIMAL ttl_co2
        DECIMAL ttl_fuel
        DECIMAL safl_qty
    }
    th_user_actn {
        BIGINT seq PK
        VARCHAR user_id
        VARCHAR actn_cd
        VARCHAR dmn_tbl
        VARCHAR dmn_pk
        TIMESTAMP actn_dt
        VARCHAR client_ip
        VARCHAR rslt_cd
    }
```

---

## 9. 공통 컬럼 패턴

모든 `tn_*` 테이블은 다음 8개 공통 컬럼을 포함:

| 컬럼 | 타입 | 용도 |
|---|---|---|
| `frst_reg_dt` | TIMESTAMP | 최초 등록 일시 |
| `frst_reg_user_id` | VARCHAR(20) | 최초 등록자 |
| `last_chg_dt` | TIMESTAMP | 마지막 변경 일시 |
| `last_chg_user_id` | VARCHAR(20) | 마지막 변경자 |
| `use_bgng_dt` | TIMESTAMP | 사용 시작 (생성 시) |
| `use_end_dt` | TIMESTAMP | 사용 종료 — 소프트 삭제 (`9999-12-31` if 활성) |

소프트 삭제 쿼리: `WHERE use_end_dt > NOW()`

## 10. 인덱스 전략

| 패턴 | 적용 테이블 |
|---|---|
| PK 자동 인덱스 | 전 테이블 |
| (oprtr_id, rprt_yr) 복합 | tn_emp_plan, tn_er, tn_cef, tn_eucr, tn_vr, tn_oom_check, tn_saf_blnd_mntr |
| use_end_dt 단일 | 전 `tn_*` (소프트 삭제 필터) |
| sim_id LIKE 'SM%' | tn_ptl_sim (regex 채번) |
| actn_dt DESC | th_user_actn (감사로그 조회) |

## 11. 산출 통계

- **전체 테이블**: 81개
- **컬럼 수**: 약 800개
- **외래키 관계**: 약 60개
- **공통 코드 그룹**: 14종 (CERT_REGIS_MTHD_CD, ER_ST_CD, DNSTY_SE_CD, ...)
