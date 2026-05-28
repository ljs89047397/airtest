# icas-cems — ERD (Mermaid)

> **단일 소스**: 본 ERD 는 `db/initdb/오브젝트생성.sql` 의 논리적 시각화. 차이가 발견되면 SQL 이 정답.
> **스키마 6개**: com / emp / er / vr / saf / ptl

---

## 1. com — 공통

```mermaid
erDiagram
    TC_COM_CD_GROUP ||--o{ TC_COM_CD_DTL : "has"
    TC_CNTRY_CD ||--o{ TC_AERDRM_CD : "located_in"
    TN_OGNZ ||--o| TN_OPRTR : "is_airline (1:0..1)"
    TN_OGNZ ||--o| TN_VRFCN_INST : "is_verifier (1:0..1)"
    TN_OGNZ ||--o{ TN_USER : "employs"
    TN_USER ||--o{ TN_USER_ROLE_MPNG : "has"
    TN_ROLE ||--o{ TN_USER_ROLE_MPNG : "granted_to"
    TN_ROLE ||--o{ TN_SYS_AUTHRT_ROLE_MPNG : "has"
    TN_SYS_AUTHRT ||--o{ TN_SYS_AUTHRT_ROLE_MPNG : "to_role"
    TN_SYS_AUTHRT ||--o{ TN_SYS_AUTHRT_PRGRM_MPNG : "to_prgrm"
    TN_PRGRM ||--o{ TN_SYS_AUTHRT_PRGRM_MPNG : "controlled_by"
    TN_PRGRM ||--o| TN_SYS_MENU : "exposed_via"
    TN_SYS_MENU ||--o{ TN_SYS_MENU : "parent_of"
    TN_OPRTR ||--o{ TN_VRFCN_ASSGN : "assigned_to"
    TN_VRFCN_INST ||--o{ TN_VRFCN_ASSGN : "verifies"
    TN_ATRZ_TASK ||--o{ TN_ATRZ_DMND : "requested_as"
    TN_ATRZ_DMND ||--o{ TN_ATRZ_PRCS : "processed_by"
    TN_USER ||--o{ TN_ATRZ_DMND : "demands"
    TN_USER ||--o{ TN_ATRZ_PRCS : "approves"
    TN_FILE ||--o{ TN_FILE_DTL : "contains"
    TN_FILE ||--o| TC_ATCH_FILE_IDNTF_INFO : "mapped_to"
    TN_FILE_DTL ||--o{ TH_FILE_DWNLD_HSTRY : "downloaded"
    TN_USER ||--o{ TH_LOGN_HSTRY : "logs_in"

    TN_OGNZ {
        varchar ognz_id PK
        varchar ognz_se_cd "MOLIT/KOTSA/AIRLINE/VERIFIER"
        varchar ognz_nm
    }
    TN_OPRTR {
        varchar oprtr_id PK
        varchar ognz_id FK
        char icao_desig UK
        varchar oprtr_nm
        varchar aoc_no
    }
    TN_VRFCN_INST {
        varchar vrfcn_inst_id PK
        varchar ognz_id FK
        varchar vrfcn_inst_nm
        char icao_ccr_accrd_yn
        date icao_ccr_accrd_xpr_dt
    }
    TN_VRFCN_ASSGN {
        varchar vrfcn_inst_id PK,FK
        varchar oprtr_id PK,FK
        char rprt_yr PK
        date assgn_dt
    }
    TN_USER {
        varchar user_id PK
        varchar user_nm "암호화"
        char pswd_hash "SHA-256"
        varchar ognz_id FK
        char master_yn
    }
```

---

## 2. emp — 배출량 모니터링 계획서

```mermaid
erDiagram
    TN_OPRTR ||--o{ TN_EMP_PLAN : "creates"
    TN_EMP_PLAN ||--o| TN_EMP_OPRTR_INFO : "describes"
    TN_EMP_PLAN ||--o{ TN_EMP_OPRTR_CNCT : "contacts"
    TN_EMP_PLAN ||--o{ TN_EMP_ACFT : "operates"
    TN_EMP_PLAN ||--o{ TN_EMP_CNTRY_PAIR : "flies"
    TN_EMP_PLAN ||--o| TN_EMP_CO2_CALC : "uses_method"
    TN_EMP_PLAN ||--o{ TN_EMP_CO2_DETAIL : "method_detail"
    TN_EMP_PLAN ||--o| TN_EMP_DATA_CTRL : "quality_control"
    TN_EMP_PLAN ||--o{ TN_EMP_RISK : "identifies"
    TN_EMP_PLAN ||--o{ TH_EMP_CHG_HSTRY : "versioned"

    TN_EMP_PLAN {
        varchar emp_plan_id PK
        varchar oprtr_id FK
        varchar emp_ver
        varchar emp_st_cd "DRAFT/SBMTD/.../APRVD"
        char rprt_yr
        timestamp sbmt_dt
        timestamp aprv_dt
        char sig_chg_yn "중대변경"
    }
    TN_EMP_CO2_CALC {
        varchar emp_plan_id PK,FK
        varchar mntr_mthd_cd "MTHD_A/B/BLOCK/REFUEL/ALLOC"
        char cert_use_yn
        numeric est_co2_emsn
    }
```

---

## 3. er — 배출량 보고서 + CEF + EUCR + OoM

```mermaid
erDiagram
    TN_OPRTR ||--o{ TN_ER : "reports"
    TN_EMP_PLAN ||--o{ TN_ER : "applies_method"
    TN_ER ||--o{ TN_ER_VRFR_INFO : "verified_by"
    TN_ER ||--o{ TN_ER_ACFT_FUEL : "uses"
    TN_ER ||--o{ TN_ER_AFBR : "burns"
    TN_ER ||--o{ TN_ER_CNTRY_PAIR_CO2 : "country_pairs"
    TN_ER ||--o{ TN_ER_AERDRM_PAIR_CO2 : "airport_pairs"
    TN_ER ||--o{ TN_ER_FUEL_SMRY : "summarized"
    TN_ER ||--o{ TN_ER_DATA_GAP : "gaps"
    TN_ER ||--o| TN_CEF : "claims"
    TN_CEF ||--o{ TN_CEF_CLAIM : "items"
    TN_CEF_CLAIM ||--o| TN_CEF_LCYC : "lifecycle"
    TN_CEF_CLAIM ||--o{ TN_CEF_SPCHN : "supply_chain"
    TN_OPRTR ||--o{ TN_EUCR : "cancels"
    TN_EUCR ||--o{ TN_EUCR_BATCH : "batches"
    TN_EUCR_BATCH ||--o{ TN_EUCR_CRDT_DTL : "credits"
    TN_OPRTR ||--o| TN_OOM_CHECK : "checked"
    TN_ER ||--o| TN_OOM_CHECK : "checked"
    TN_OOM_CHECK ||--o{ TN_OOM_CHECK_ITEM : "items"
    TN_OOM_CHECK ||--o{ TN_OOM_CHECK_ADDL_RQST : "extra_requests"
    TN_OOM_CHECK ||--o{ TN_OOM_CHECK_VRFR_EVAL : "verifier_eval"

    TN_ER {
        varchar er_id PK
        varchar oprtr_id FK
        char rprt_yr
        varchar er_ver
        varchar er_st_cd
        varchar emp_plan_id_apld FK
        char cert_use_yn
    }
    TN_CEF {
        varchar cef_id PK
        varchar er_id FK,UK
        varchar oprtr_id FK
        numeric ttl_redu_amt
    }
    TN_CEF_CLAIM {
        varchar cef_id PK,FK
        varchar claim_no PK
        varchar batch_id_no "이중청구 스캔 대상"
        numeric pure_fuel_mass
    }
    TN_EUCR {
        varchar eucr_id PK
        varchar oprtr_id FK
        char rprt_yr
        numeric ttl_qty
        numeric ofst_req_qty
        char fulfilled_yn
    }
    TN_OOM_CHECK {
        varchar oom_id PK
        varchar oprtr_id FK
        char rprt_yr
        varchar er_id FK
        varchar oom_rslt_cd "PASS/FAIL/HOLD"
    }
```

---

## 4. vr — 검증보고서

```mermaid
erDiagram
    TN_OPRTR ||--o{ TN_VR : "verified"
    TN_VRFCN_INST ||--o{ TN_VR : "verifies"
    TN_ER ||--o| TN_VR : "checked_by"
    TN_EUCR ||--o| TN_VR : "checked_by"
    TN_VR ||--o| TN_VR_SCOPE : "scope"
    TN_VR ||--o{ TN_VR_TEAM : "team"
    TN_VR ||--o| TN_VR_TIME : "time"
    TN_VR ||--o{ TN_VR_INPUT_INFO : "input_docs"
    TN_VR ||--o| TN_VR_PRCDR : "procedure"
    TN_VR ||--o{ TN_VR_NCNFRM : "nonconformities"
    TN_VR ||--o| TN_VR_CNCLS : "conclusion"

    TN_VR {
        varchar vr_id PK
        varchar oprtr_id FK
        char rprt_yr
        varchar vr_type_cd "ER/EUCR"
        varchar vr_st_cd
        varchar vrfcn_inst_id FK
    }
    TN_VR_TEAM {
        varchar vr_id PK,FK
        int member_sn PK
        varchar role_cd "LEAD/MEMBER/INDEP_REVIEWER"
        int conscutv_cnt "연속검증 횟수"
    }
    TN_VR_CNCLS {
        varchar vr_id PK,FK
        varchar final_opnn_cd "REASONABLE/LIMITED/QUALIFIED/ADVERSE"
    }
```

---

## 5. saf — 지속가능항공유

```mermaid
erDiagram
    TN_OPRTR ||--o{ TN_SAF_BATCH : "owns"
    TN_SAF_BATCH ||--o{ TN_SAF_CERT : "certified_by"
    TN_SAF_BATCH ||--o| TN_SAF_PRDC_SPLY : "produced_by"
    TN_SAF_BATCH ||--o| TN_SAF_BLNDR : "blended_by"
    TN_SAF_BATCH ||--o| TN_SAF_FEED : "from_feed"
    TN_SAF_FEED ||--o{ TN_SAF_FEED_ORGN : "origins"
    TN_SAF_BATCH ||--o| TN_SAF_GHG : "emits"
    TN_SAF_CERT ||--o{ TN_SAF_CERT_AUDIT : "audited"
    TN_OPRTR ||--o{ TN_SAF_AIRPRT_FUEL : "refuels"
    TN_OPRTR ||--o{ TN_SAF_AIRPRT_PURCH : "purchases"
    TN_OPRTR ||--o{ TN_SAF_TANKERING_MNTR : "monitored_tankering"
    TN_OPRTR ||--o| TN_SAF_BLND_MNTR : "monitored_blend"
    TN_OPRTR ||--o{ TN_SAF_FLD_INSPN : "inspected"

    TN_SAF_BATCH {
        varchar batch_id PK
        varchar oprtr_id FK
        varchar poc_id_no
        numeric batch_qty
        varchar cust_chn_modl_cd
    }
    TN_SAF_CERT {
        varchar cert_id PK
        varchar batch_id FK
        varchar cert_type_cd "PoS/PoC"
        varchar cert_no "암호화"
        char srnd_yn "회수여부 - 이중계산방지"
    }
    TN_SAF_AIRPRT_FUEL {
        char airprt_cd PK,FK
        char rprt_yr PK
        varchar oprtr_id PK,FK
        numeric req_fuel_qty
        numeric actl_fuel_qty
    }
    TN_SAF_TANKERING_MNTR {
        varchar oprtr_id PK,FK
        char rprt_yr PK
        char airprt_cd PK,FK
        numeric refuel_ratio "90% 기준"
        char ovr_90pct_yn
    }
    TN_SAF_BLND_MNTR {
        varchar oprtr_id PK,FK
        char rprt_yr PK
        numeric blnd_ratio
        numeric oblg_ratio
        char fulfilled_yn
    }
```

---

## 6. ptl — 포털/통계/시뮬레이션

```mermaid
erDiagram
    TN_USER ||--o{ TN_PTL_SIM : "owns"
    TN_OPRTR ||--o{ TN_PTL_STAT_YEARLY : "aggregated"

    TN_PTL_CCR_EXTR {
        varchar extr_id PK
        char rprt_yr
        varchar extr_scope_cd
        varchar extr_st_cd
        varchar file_id
    }
    TN_PTL_SIM {
        varchar sim_id PK
        varchar owner_user_id FK
        char base_yr
        jsonb input_json
        jsonb rslt_json
    }
    TN_PTL_STAT_YEARLY {
        char rprt_yr PK
        varchar oprtr_id PK,FK
        numeric ttl_co2_emsn
        numeric ttl_ofst_req
    }
    TH_USER_ACTN {
        bigint actn_id PK
        varchar user_id
        varchar actn_se_cd
        varchar target_tbl
        varchar target_pk
    }
```

---

## 7. 핵심 크로스 스키마 관계 요약

```mermaid
graph LR
    A[com.TN_OPRTR<br/>운영사] -->|reports| B[emp.TN_EMP_PLAN<br/>EMP]
    A -->|reports| C[er.TN_ER<br/>ER]
    A -->|reports| D[er.TN_CEF<br/>CEF]
    A -->|reports| E[er.TN_EUCR<br/>EUCR]
    A -->|owns| F[saf.TN_SAF_BATCH<br/>SAF 배치]
    B -.applies.-> C
    C -->|verified_by| G[vr.TN_VR<br/>VR]
    C -.checked.-> H[er.TN_OOM_CHECK<br/>OoM]
    G -.checked.-> H
    F -.cross_check.-> D
    I[com.TN_VRFCN_INST<br/>검증기관] -->|verifies| G
    I -.assigned.-> A

    style A fill:#e1f5ff
    style I fill:#e1f5ff
    style C fill:#fff3cd
    style G fill:#d4edda
    style H fill:#f8d7da
```

---

## 8. 테이블 수 통계

| 스키마 | 마스터 (TN_) | 코드 (TC_) | 이력 (TH_) | 합계 |
|--------|-------------|-----------|-----------|------|
| com | 14 | 5 | 2 | 21 |
| emp | 9 | 0 | 1 | 10 |
| er | 14 | 0 | 0 | 14 |
| vr | 8 | 0 | 0 | 8 |
| saf | 11 | 0 | 0 | 11 |
| ptl | 3 | 0 | 1 | 4 |
| **합계** | **59** | **5** | **4** | **68** |

> 68개 테이블이 1차년도 범위. 2차년도 (AI/LLM/OCR) 영역 추가 시 별도 확장.
