-- ================================================================
-- icas-cems — PL 리뷰 반영 패치
-- 일자: 2026-05-22
-- 사유: ERD/DDL 1차 PL 리뷰 결과 P1·P2·P3 항목 반영
-- ================================================================

-- ----------------------------------------------------------------
-- P1-1. CEF 이중청구 방지 (SFR-021)
--   동일 항공사가 같은 batch_id_no 를 2회 청구 못 하도록 UK 추가
--   (다른 항공사간 부분청구는 application 단 cross-check 로 처리)
-- ----------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_tn_cef_claim_batch'
    ) THEN
        ALTER TABLE er.tn_cef_claim
            ADD CONSTRAINT uk_tn_cef_claim_batch UNIQUE (cef_id, batch_id_no);
    END IF;
END $$;

-- ----------------------------------------------------------------
-- P1-2. 상태 코드 무결성 — CHECK 제약 추가
-- ----------------------------------------------------------------
ALTER TABLE emp.tn_emp_plan DROP CONSTRAINT IF EXISTS chk_emp_plan_st_cd;
ALTER TABLE emp.tn_emp_plan ADD CONSTRAINT chk_emp_plan_st_cd
    CHECK (emp_st_cd IN ('DRAFT','SBMTD','RVWNG','RJCTD','RCMDD','APRVD','CNCLD'));

ALTER TABLE er.tn_er DROP CONSTRAINT IF EXISTS chk_er_st_cd;
ALTER TABLE er.tn_er ADD CONSTRAINT chk_er_st_cd
    CHECK (er_st_cd IN ('DRAFT','SBMTD','RVWNG','RJCTD','RCMDD','APRVD','CNCLD'));

ALTER TABLE er.tn_cef DROP CONSTRAINT IF EXISTS chk_cef_st_cd;
ALTER TABLE er.tn_cef ADD CONSTRAINT chk_cef_st_cd
    CHECK (cef_st_cd IN ('DRAFT','SBMTD','RVWNG','RJCTD','RCMDD','APRVD','CNCLD'));

ALTER TABLE er.tn_eucr DROP CONSTRAINT IF EXISTS chk_eucr_st_cd;
ALTER TABLE er.tn_eucr ADD CONSTRAINT chk_eucr_st_cd
    CHECK (eucr_st_cd IN ('DRAFT','SBMTD','RVWNG','RJCTD','RCMDD','APRVD','CNCLD'));

ALTER TABLE vr.tn_vr DROP CONSTRAINT IF EXISTS chk_vr_st_cd;
ALTER TABLE vr.tn_vr ADD CONSTRAINT chk_vr_st_cd
    CHECK (vr_st_cd IN ('DRAFT','SBMTD','RVWNG','RJCTD','RCMDD','APRVD','CNCLD'));

-- OoM 은 별도 상태 코드
ALTER TABLE er.tn_oom_check DROP CONSTRAINT IF EXISTS chk_oom_st_cd;
ALTER TABLE er.tn_oom_check ADD CONSTRAINT chk_oom_st_cd
    CHECK (oom_st_cd IN ('INPRG','DONE','HOLD'));

ALTER TABLE er.tn_oom_check DROP CONSTRAINT IF EXISTS chk_oom_rslt_cd;
ALTER TABLE er.tn_oom_check ADD CONSTRAINT chk_oom_rslt_cd
    CHECK (oom_rslt_cd IS NULL OR oom_rslt_cd IN ('PASS','WARN','FAIL'));

-- 결재 / CCR / SAF / 시뮬레이션 상태
ALTER TABLE com.tn_atrz_dmnd DROP CONSTRAINT IF EXISTS chk_atrz_st_cd;
ALTER TABLE com.tn_atrz_dmnd ADD CONSTRAINT chk_atrz_st_cd
    CHECK (atrz_st_cd IN ('PEND','INPRG','APRVD','RJCTD','CNCLD'));

ALTER TABLE ptl.tn_ptl_ccr_extr DROP CONSTRAINT IF EXISTS chk_extr_st_cd;
ALTER TABLE ptl.tn_ptl_ccr_extr ADD CONSTRAINT chk_extr_st_cd
    CHECK (extr_st_cd IN ('INPRG','DONE','FAIL'));

-- 기관유형 CHECK
ALTER TABLE com.tn_ognz DROP CONSTRAINT IF EXISTS chk_ognz_se_cd;
ALTER TABLE com.tn_ognz ADD CONSTRAINT chk_ognz_se_cd
    CHECK (ognz_se_cd IN ('MOLIT','KOTSA','AIRLINE','VERIFIER'));

-- ----------------------------------------------------------------
-- P2-1. 결재선 템플릿 — 업무별 결재 라인 사전 정의
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS com.tn_atrz_line_tmpl (
    line_tmpl_id        varchar(20)     NOT NULL,
    atrz_task_id        varchar(20)     NOT NULL,
    line_seq            integer         NOT NULL,
    atrz_role_cd        varchar(30)     NOT NULL,
    atrz_role_ognz_id   varchar(20)     NULL,
    must_yn             char(1)         NOT NULL DEFAULT 'Y',
    rmrk                varchar(500)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_atrz_line_tmpl PRIMARY KEY (line_tmpl_id),
    CONSTRAINT uk_tn_atrz_line_tmpl UNIQUE (atrz_task_id, line_seq),
    CONSTRAINT fk_tn_atrz_line_tmpl_task FOREIGN KEY (atrz_task_id) REFERENCES com.tn_atrz_task(atrz_task_id)
);
COMMENT ON TABLE com.tn_atrz_line_tmpl IS '결재선 템플릿 (업무별 결재 단계 사전 정의)';

-- ----------------------------------------------------------------
-- P2-2. 알림 / 발송 이력 (SFR-053 알림 추적)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS com.tn_ntfy_tmpl (
    tmpl_id             varchar(20)     NOT NULL,
    tmpl_nm             varchar(200)    NOT NULL,
    chnl_cd             varchar(20)     NOT NULL,
    title_tmpl          varchar(500)    NULL,
    body_tmpl           text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_ntfy_tmpl PRIMARY KEY (tmpl_id),
    CONSTRAINT chk_ntfy_chnl CHECK (chnl_cd IN ('EMAIL','SMS','IN_APP'))
);
COMMENT ON TABLE com.tn_ntfy_tmpl IS '알림 템플릿 (EMAIL/SMS/IN_APP)';

CREATE TABLE IF NOT EXISTS com.th_ntfy_send_hstry (
    ntfy_id             bigserial       NOT NULL,
    tmpl_id             varchar(20)     NULL,
    user_id             varchar(50)     NOT NULL,
    chnl_cd             varchar(20)     NOT NULL,
    title               varchar(500)    NULL,
    body                text            NULL,
    send_dt             timestamp       NOT NULL DEFAULT NOW(),
    send_st_cd          varchar(20)     NOT NULL DEFAULT 'PEND',
    send_rslt_msg       varchar(2000)   NULL,
    target_tbl          varchar(60)     NULL,
    target_pk           varchar(500)    NULL,
    CONSTRAINT pk_th_ntfy_send_hstry PRIMARY KEY (ntfy_id),
    CONSTRAINT chk_ntfy_send_st CHECK (send_st_cd IN ('PEND','SENT','FAIL'))
);
COMMENT ON TABLE com.th_ntfy_send_hstry IS '알림 발송 이력 (감사·재시도용)';
CREATE INDEX IF NOT EXISTS ix_th_ntfy_send_hstry_01 ON com.th_ntfy_send_hstry (user_id, send_dt DESC);

-- ----------------------------------------------------------------
-- P3-1. OoM 표준 점검 항목 마스터 (SFR-034 18종 정량 검증 정의)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS er.tc_oom_chk_item_tmpl (
    item_no             integer         NOT NULL,
    item_nm             varchar(500)    NOT NULL,
    item_se_cd          varchar(20)     NOT NULL,
    item_desc           text            NULL,
    judg_logic_cn       text            NULL,
    auto_chk_yn         char(1)         NOT NULL DEFAULT 'Y',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_oom_chk_item_tmpl PRIMARY KEY (item_no)
);
COMMENT ON TABLE er.tc_oom_chk_item_tmpl IS 'OoM 표준 점검 항목 마스터 (18종 + 추가)';
COMMENT ON COLUMN er.tc_oom_chk_item_tmpl.item_se_cd IS 'EMP_LINK / DATE / CROSS_CHECK / THRSHLD / DUPL / CNTRY / FUEL / OUTLIER / VRFR / YOY';
COMMENT ON COLUMN er.tc_oom_chk_item_tmpl.auto_chk_yn IS '자동 검증 가능 여부';

-- 18종 점검 항목 시드 (SFR-034)
INSERT INTO er.tc_oom_chk_item_tmpl (item_no, item_nm, item_se_cd, item_desc, auto_chk_yn) VALUES
    ( 1, 'ICAO 항공사 지정어 유효성',                'EMP_LINK',    '항공사 지정어가 ICAO 코드 마스터에 존재하는지 검증',                          'Y'),
    ( 2, '제출 기한 준수',                           'DATE',        '보고연도 N+1 마감일 이내 제출 여부',                                          'Y'),
    ( 3, 'ER ↔ VR 총연료/총항공편수 일치',           'CROSS_CHECK', 'ER 와 VR 의 합계 필드 일치 검증',                                             'Y'),
    ( 4, '보고서 작성 일자 적정성',                  'DATE',        '발행일이 보고기간 종료일 이후인지 검증',                                      'Y'),
    ( 5, 'CORSIA 보고 의무 충족 (10,000t 임계치)',   'THRSHLD',     '총 배출량이 10,000t 임계치 이상 시 보고 의무',                                'Y'),
    ( 6, 'CERT 사용 임계치 초과 (500,000t/50,000t)', 'THRSHLD',     '운영자 규모별 CERT 사용 가능 임계치',                                         'Y'),
    ( 7, '승인 EMP 기반 연료 유형 일치성',           'EMP_LINK',    'ER 사용 연료가 EMP 에 등록된 연료 유형과 일치',                               'Y'),
    ( 8, '항공기 등록기호 중복',                     'DUPL',        '동일 ER 내 동일 등록기호 중복 등록 차단',                                     'Y'),
    ( 9, 'CORSIA 참여국 기반 국가쌍 분류 정확성',    'CNTRY',       '출발/도착 국가가 CORSIA 참여국 매트릭스에 따라 ofst_req_yn 정확',             'Y'),
    (10, '국가쌍 ↔ 연료유형 중복',                   'DUPL',        '동일 (출발-도착-연료) 조합 중복 행 차단',                                     'Y'),
    (11, '국내선 오류 (동일 국가)',                  'CNTRY',       '출발/도착이 동일 국가코드인 경우 알림',                                       'Y'),
    (12, '항공편당 연료 소비량 이상치 (상/하한)',    'OUTLIER',     '평균 ± n σ 또는 ICAO CERT 예상치 대비 이상',                                  'Y'),
    (13, 'ICAO CORSIA CERT 계산값 편차',             'OUTLIER',     'CERT 추정 연료소비량 대비 보고값 편차',                                       'Y'),
    (14, 'CORSIA 데이터 갭 임계치 초과 (5%)',        'THRSHLD',     '데이터 갭 영향 CO2 / 총 CO2 ≥ 5% 시 경고',                                    'Y'),
    (15, '데이터 갭 표시-상세 정합성',               'CROSS_CHECK', 'ER 데이터 갭 발생 표시와 상세 등록의 1:1 일치',                               'Y'),
    (16, 'ICAO CCR 공인 검증기관 유효성',            'VRFR',        '검증기관 ICAO CCR 인증 만료일 검증',                                          'Y'),
    (17, '검증팀 리더 연속 검증 횟수',               'VRFR',        '동일 검증팀 리더가 동일 운영자를 3년 초과 연속 검증 시 경고',                 'Y'),
    (18, '전년 대비 배출량 변동 이상치',             'YOY',         '전년 보고서 대비 ±30% 변동 시 경고',                                          'Y')
ON CONFLICT (item_no) DO UPDATE SET item_nm = EXCLUDED.item_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- P3-2. 개인정보 COMMENT 보강
-- ----------------------------------------------------------------
COMMENT ON COLUMN emp.tn_emp_oprtr_info.lglrpr_nm    IS '법정대리인 명 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN emp.tn_emp_oprtr_cnct.user_nm      IS '담당자 성명 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN emp.tn_emp_oprtr_cnct.mblphn_no    IS '담당자 휴대전화 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN emp.tn_emp_oprtr_cnct.eml_addr     IS '담당자 이메일 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN vr.tn_vr_team.user_nm              IS '검증팀원 성명 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN vr.tn_vr_cncls.indep_review_user_nm IS '독립검토자 성명 (개인정보 - 암호화 대상)';

-- ----------------------------------------------------------------
-- 끝
-- ----------------------------------------------------------------
