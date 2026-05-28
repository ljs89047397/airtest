-- ================================================================
-- icas-cems — 전체 오브젝트 생성 SQL (멱등 DDL)
-- 사업명: 국제항공 탄소 배출량 관리 시스템 구축
-- DB     : PostgreSQL 16
-- 스키마 : com / emp / er / vr / saf / ptl
-- 작성   : PL · DA (2026-05-21)
-- ----------------------------------------------------------------
-- 본 파일은 신규 환경 0 빌드의 단일 소스이며, db/migrations/ 에
-- 누적된 모든 변경을 통합 반영한다. 재실행 안전(IF NOT EXISTS).
-- ================================================================

-- 확장
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ================================================================
-- 스키마 com — 공통 (사용자/권한/공통코드/조직/메뉴/결재/파일/외부코드/게시판)
-- ================================================================

-- 공통코드 그룹
CREATE TABLE IF NOT EXISTS com.tc_com_cd_group (
    grp_id              varchar(30)     NOT NULL,
    grp_nm              varchar(200)    NOT NULL,
    grp_desc            varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_com_cd_group PRIMARY KEY (grp_id)
);
COMMENT ON TABLE  com.tc_com_cd_group              IS '공통코드 그룹';
COMMENT ON COLUMN com.tc_com_cd_group.grp_id       IS '코드 그룹 ID';
COMMENT ON COLUMN com.tc_com_cd_group.grp_nm       IS '코드 그룹 명칭';

-- 공통코드 상세
CREATE TABLE IF NOT EXISTS com.tc_com_cd_dtl (
    grp_id              varchar(30)     NOT NULL,
    cd                  varchar(50)     NOT NULL,
    cd_nm               varchar(200)    NOT NULL,
    cd_desc             varchar(1000)   NULL,
    cd_ord              integer         NOT NULL DEFAULT 0,
    cd_attr_1           varchar(200)    NULL,
    cd_attr_2           varchar(200)    NULL,
    cd_attr_3           varchar(200)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_com_cd_dtl PRIMARY KEY (grp_id, cd),
    CONSTRAINT fk_tc_com_cd_dtl_grp FOREIGN KEY (grp_id) REFERENCES com.tc_com_cd_group(grp_id)
);
COMMENT ON TABLE  com.tc_com_cd_dtl                IS '공통코드 상세';
COMMENT ON COLUMN com.tc_com_cd_dtl.cd_attr_1      IS '확장 속성 1 (예: 환산계수, 단위 등)';

-- 국가 코드 (ISO 3166-1 alpha-2)
CREATE TABLE IF NOT EXISTS com.tc_cntry_cd (
    cntry_cd            char(2)         NOT NULL,
    cntry_nm_ko         varchar(100)    NOT NULL,
    cntry_nm_en         varchar(200)    NOT NULL,
    icao_prtcpt_yn      char(1)         NOT NULL DEFAULT 'N',
    icao_prtcpt_bgng_yr char(4)         NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_cntry_cd PRIMARY KEY (cntry_cd)
);
COMMENT ON TABLE  com.tc_cntry_cd                  IS '국가 코드 (ISO 3166-1 alpha-2 + CORSIA 참여국 분류)';
COMMENT ON COLUMN com.tc_cntry_cd.icao_prtcpt_yn   IS 'CORSIA 참여국 여부 (Y/N)';

-- 공항 코드 (ICAO 4자)
CREATE TABLE IF NOT EXISTS com.tc_aerdrm_cd (
    aerdrm_cd           char(4)         NOT NULL,
    iata_cd             char(3)         NULL,
    aerdrm_nm_ko        varchar(200)    NULL,
    aerdrm_nm_en        varchar(200)    NOT NULL,
    cntry_cd            char(2)         NOT NULL,
    cty_nm              varchar(100)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_aerdrm_cd PRIMARY KEY (aerdrm_cd),
    CONSTRAINT fk_tc_aerdrm_cd_cntry FOREIGN KEY (cntry_cd) REFERENCES com.tc_cntry_cd(cntry_cd)
);
COMMENT ON TABLE  com.tc_aerdrm_cd                 IS '공항 코드 (ICAO Doc 7910)';

-- ICAO 항공기 유형 (Doc 8643)
CREATE TABLE IF NOT EXISTS com.tc_acft_type_cd (
    acft_type_cd        varchar(10)     NOT NULL,
    acft_type_nm        varchar(200)    NOT NULL,
    mfctr_nm            varchar(100)    NULL,
    eng_cnt             integer         NULL,
    afbr_dflt_val       numeric(10,4)   NULL,
    afbr_unit           varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_acft_type_cd PRIMARY KEY (acft_type_cd)
);
COMMENT ON TABLE  com.tc_acft_type_cd              IS 'ICAO 항공기 유형 지정어 (Doc 8643)';
COMMENT ON COLUMN com.tc_acft_type_cd.afbr_dflt_val IS '기본 평균 연료연소율 (Avg Fuel Burn Rate)';

-- 기관 (4종)
CREATE TABLE IF NOT EXISTS com.tn_ognz (
    ognz_id             varchar(20)     NOT NULL,
    ognz_se_cd          varchar(10)     NOT NULL,
    ognz_nm             varchar(200)    NOT NULL,
    ognz_nm_en          varchar(200)    NULL,
    biz_no              varchar(20)     NULL,
    addr                varchar(500)    NULL,
    rprstv_nm           varchar(100)    NULL,
    rprstv_cnct         varchar(200)    NULL,
    rmrk                varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_ognz PRIMARY KEY (ognz_id)
);
COMMENT ON TABLE  com.tn_ognz                      IS '기관 (MOLIT/KOTSA/AIRLINE/VERIFIER)';
COMMENT ON COLUMN com.tn_ognz.ognz_se_cd           IS '기관 유형 코드 (MOLIT/KOTSA/AIRLINE/VERIFIER)';

-- 항공기 운영사 (AIRLINE 기관과 1:1)
CREATE TABLE IF NOT EXISTS com.tn_oprtr (
    oprtr_id            varchar(10)     NOT NULL,
    ognz_id             varchar(20)     NOT NULL,
    icao_desig          char(3)         NOT NULL,
    oprtr_nm            varchar(200)    NOT NULL,
    oprtr_nm_en         varchar(200)    NOT NULL,
    aoc_no              varchar(50)     NULL,
    aoc_isue_dt         date            NULL,
    aoc_xpr_dt          date            NULL,
    aoc_athrty_nm       varchar(200)    NULL,
    parent_co_nm        varchar(200)    NULL,
    lglrpr_nm           varchar(100)    NULL,
    lglrpr_cnct         varchar(200)    NULL,
    addr                varchar(500)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_oprtr PRIMARY KEY (oprtr_id),
    CONSTRAINT uk_tn_oprtr_icao UNIQUE (icao_desig),
    CONSTRAINT fk_tn_oprtr_ognz FOREIGN KEY (ognz_id) REFERENCES com.tn_ognz(ognz_id)
);
COMMENT ON TABLE  com.tn_oprtr                     IS '항공기 운영사 (이행의무자)';
COMMENT ON COLUMN com.tn_oprtr.icao_desig          IS 'ICAO 지정어 (3자, 예: AAR/KAL/JNA)';
COMMENT ON COLUMN com.tn_oprtr.lglrpr_nm           IS '법정대리인 (개인정보 - 암호화 대상)';

-- 검증기관 (VERIFIER 기관과 1:1)
CREATE TABLE IF NOT EXISTS com.tn_vrfcn_inst (
    vrfcn_inst_id       varchar(10)     NOT NULL,
    ognz_id             varchar(20)     NOT NULL,
    vrfcn_inst_nm       varchar(200)    NOT NULL,
    vrfcn_inst_nm_en    varchar(200)    NOT NULL,
    addr                varchar(500)    NULL,
    icao_ccr_accrd_yn   char(1)         NOT NULL DEFAULT 'N',
    icao_ccr_accrd_no   varchar(50)     NULL,
    icao_ccr_accrd_xpr_dt date          NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vrfcn_inst PRIMARY KEY (vrfcn_inst_id),
    CONSTRAINT fk_tn_vrfcn_inst_ognz FOREIGN KEY (ognz_id) REFERENCES com.tn_ognz(ognz_id)
);
COMMENT ON TABLE  com.tn_vrfcn_inst                IS '검증기관 (ICAO CCR 공인)';

-- 검증기관 ↔ 항공사 배정
CREATE TABLE IF NOT EXISTS com.tn_vrfcn_assgn (
    vrfcn_inst_id       varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    assgn_dt            date            NOT NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vrfcn_assgn PRIMARY KEY (vrfcn_inst_id, oprtr_id, rprt_yr),
    CONSTRAINT fk_tn_vrfcn_assgn_inst FOREIGN KEY (vrfcn_inst_id) REFERENCES com.tn_vrfcn_inst(vrfcn_inst_id),
    CONSTRAINT fk_tn_vrfcn_assgn_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  com.tn_vrfcn_assgn               IS '검증기관-항공사 배정 (보고연도별)';

-- 사용자
CREATE TABLE IF NOT EXISTS com.tn_user (
    user_id             varchar(50)     NOT NULL,
    user_nm             varchar(100)    NOT NULL,
    pswd_hash           char(64)        NOT NULL,
    ognz_id             varchar(20)     NOT NULL,
    eml_addr            varchar(200)    NULL,
    mblphn_no           varchar(20)     NULL,
    tlphn_no            varchar(20)     NULL,
    pswd_chg_dt         timestamp       NULL,
    pswd_fail_cnt       integer         NOT NULL DEFAULT 0,
    acnt_lock_yn        char(1)         NOT NULL DEFAULT 'N',
    last_logn_dt        timestamp       NULL,
    master_yn           char(1)         NOT NULL DEFAULT 'N',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_user PRIMARY KEY (user_id),
    CONSTRAINT fk_tn_user_ognz FOREIGN KEY (ognz_id) REFERENCES com.tn_ognz(ognz_id)
);
COMMENT ON TABLE  com.tn_user                      IS '사용자 (4종 기관 소속)';
COMMENT ON COLUMN com.tn_user.pswd_hash            IS 'SHA-256(icas + plain + cems) hex 64자';
COMMENT ON COLUMN com.tn_user.user_nm              IS '성명 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN com.tn_user.eml_addr             IS '이메일 (개인정보 - 암호화 대상)';
COMMENT ON COLUMN com.tn_user.mblphn_no            IS '휴대전화 (개인정보 - 암호화 대상)';

-- 역할
CREATE TABLE IF NOT EXISTS com.tn_role (
    role_id             varchar(30)     NOT NULL,
    role_nm             varchar(100)    NOT NULL,
    ognz_se_cd_allowed  varchar(200)    NOT NULL,
    role_desc           varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_role PRIMARY KEY (role_id)
);
COMMENT ON TABLE  com.tn_role                      IS '시스템 역할';
COMMENT ON COLUMN com.tn_role.ognz_se_cd_allowed   IS '이 역할을 부여 가능한 기관유형 (쉼표 구분)';

-- 사용자-역할 매핑 (1:N 유효기간 다중)
CREATE TABLE IF NOT EXISTS com.tn_user_role_mpng (
    user_id             varchar(50)     NOT NULL,
    role_id             varchar(30)     NOT NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_user_role_mpng PRIMARY KEY (user_id, role_id, use_bgng_dt),
    CONSTRAINT fk_tn_user_role_mpng_user FOREIGN KEY (user_id) REFERENCES com.tn_user(user_id),
    CONSTRAINT fk_tn_user_role_mpng_role FOREIGN KEY (role_id) REFERENCES com.tn_role(role_id)
);
COMMENT ON TABLE  com.tn_user_role_mpng            IS '사용자-역할 매핑 (시계열)';

-- 시스템 권한
CREATE TABLE IF NOT EXISTS com.tn_sys_authrt (
    authrt_id           varchar(30)     NOT NULL,
    authrt_nm           varchar(100)    NOT NULL,
    authrt_desc         varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_sys_authrt PRIMARY KEY (authrt_id)
);
COMMENT ON TABLE  com.tn_sys_authrt                IS '시스템 권한 정의';

-- 권한 - 역할 매핑
CREATE TABLE IF NOT EXISTS com.tn_sys_authrt_role_mpng (
    authrt_id           varchar(30)     NOT NULL,
    role_id             varchar(30)     NOT NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_sys_authrt_role_mpng PRIMARY KEY (authrt_id, role_id),
    CONSTRAINT fk_tn_sys_authrt_role_mpng_a FOREIGN KEY (authrt_id) REFERENCES com.tn_sys_authrt(authrt_id),
    CONSTRAINT fk_tn_sys_authrt_role_mpng_r FOREIGN KEY (role_id) REFERENCES com.tn_role(role_id)
);
COMMENT ON TABLE  com.tn_sys_authrt_role_mpng      IS '권한-역할 매핑';

-- 프로그램 (화면·API 1개 단위)
CREATE TABLE IF NOT EXISTS com.tn_prgrm (
    prgrm_id            varchar(20)     NOT NULL,
    sys_se_cd           varchar(10)     NOT NULL,
    prgrm_nm            varchar(200)    NOT NULL,
    prgrm_url           varchar(500)    NOT NULL,
    api_path_prefix     varchar(500)    NOT NULL,
    prgrm_desc          varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_prgrm PRIMARY KEY (prgrm_id)
);
COMMENT ON TABLE  com.tn_prgrm                     IS '프로그램 (화면·API 단위)';

-- 권한-프로그램 매핑 (inq/inpt 비트)
CREATE TABLE IF NOT EXISTS com.tn_sys_authrt_prgrm_mpng (
    authrt_id           varchar(30)     NOT NULL,
    prgrm_id            varchar(20)     NOT NULL,
    inq_authrt_yn       char(1)         NOT NULL DEFAULT 'N',
    inpt_authrt_yn      char(1)         NOT NULL DEFAULT 'N',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_sys_authrt_prgrm_mpng PRIMARY KEY (authrt_id, prgrm_id),
    CONSTRAINT fk_tn_sys_authrt_prgrm_mpng_a FOREIGN KEY (authrt_id) REFERENCES com.tn_sys_authrt(authrt_id),
    CONSTRAINT fk_tn_sys_authrt_prgrm_mpng_p FOREIGN KEY (prgrm_id) REFERENCES com.tn_prgrm(prgrm_id)
);
COMMENT ON TABLE  com.tn_sys_authrt_prgrm_mpng     IS '권한-프로그램 매핑 (조회/입력 비트)';

-- 메뉴
CREATE TABLE IF NOT EXISTS com.tn_sys_menu (
    menu_id             varchar(20)     NOT NULL,
    sys_se_cd           varchar(10)     NOT NULL,
    menu_nm             varchar(200)    NOT NULL,
    upper_menu_id       varchar(20)     NULL,
    menu_ord            integer         NOT NULL DEFAULT 0,
    prgrm_id            varchar(20)     NULL,
    icon_nm             varchar(50)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_sys_menu PRIMARY KEY (menu_id),
    CONSTRAINT fk_tn_sys_menu_upper FOREIGN KEY (upper_menu_id) REFERENCES com.tn_sys_menu(menu_id),
    CONSTRAINT fk_tn_sys_menu_prgrm FOREIGN KEY (prgrm_id) REFERENCES com.tn_prgrm(prgrm_id)
);
COMMENT ON TABLE  com.tn_sys_menu                  IS '메뉴 (트리)';

-- 결재 업무
CREATE TABLE IF NOT EXISTS com.tn_atrz_task (
    atrz_task_id        varchar(20)     NOT NULL,
    atrz_task_nm        varchar(200)    NOT NULL,
    atrz_task_desc      varchar(1000)   NULL,
    sys_se_cd           varchar(10)     NOT NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_atrz_task PRIMARY KEY (atrz_task_id)
);
COMMENT ON TABLE  com.tn_atrz_task                 IS '결재 업무 정의';

-- 결재 요청
CREATE TABLE IF NOT EXISTS com.tn_atrz_dmnd (
    atrz_dmnd_id        varchar(20)     NOT NULL,
    atrz_task_id        varchar(20)     NOT NULL,
    rfrnc_tbl_nm        varchar(60)     NOT NULL,
    rfrnc_key_cn        varchar(500)    NOT NULL,
    dmnd_user_id        varchar(50)     NOT NULL,
    dmnd_dt             timestamp       NOT NULL DEFAULT NOW(),
    atrz_st_cd          varchar(20)     NOT NULL DEFAULT 'PEND',
    title               varchar(500)    NOT NULL,
    contents            text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_atrz_dmnd PRIMARY KEY (atrz_dmnd_id),
    CONSTRAINT fk_tn_atrz_dmnd_task FOREIGN KEY (atrz_task_id) REFERENCES com.tn_atrz_task(atrz_task_id),
    CONSTRAINT fk_tn_atrz_dmnd_user FOREIGN KEY (dmnd_user_id) REFERENCES com.tn_user(user_id)
);
COMMENT ON TABLE  com.tn_atrz_dmnd                 IS '결재 요청';
COMMENT ON COLUMN com.tn_atrz_dmnd.rfrnc_key_cn    IS '참조 엔터티 PK 값 (JSON)';

-- 결재 처리
CREATE TABLE IF NOT EXISTS com.tn_atrz_prcs (
    atrz_dmnd_id        varchar(20)     NOT NULL,
    atrz_seq            integer         NOT NULL,
    atrz_user_id        varchar(50)     NOT NULL,
    atrz_role_cd        varchar(30)     NOT NULL,
    atrz_rslt_cd        varchar(20)     NULL,
    atrz_dt             timestamp       NULL,
    atrz_opnn           text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_atrz_prcs PRIMARY KEY (atrz_dmnd_id, atrz_seq),
    CONSTRAINT fk_tn_atrz_prcs_dmnd FOREIGN KEY (atrz_dmnd_id) REFERENCES com.tn_atrz_dmnd(atrz_dmnd_id),
    CONSTRAINT fk_tn_atrz_prcs_user FOREIGN KEY (atrz_user_id) REFERENCES com.tn_user(user_id)
);
COMMENT ON TABLE  com.tn_atrz_prcs                 IS '결재 처리 (단계별)';

-- 파일 그룹
CREATE TABLE IF NOT EXISTS com.tn_file (
    file_id             varchar(20)     NOT NULL,
    file_src_cd         varchar(10)     NOT NULL,
    file_grp_nm         varchar(200)    NULL,
    rmrk                varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_file PRIMARY KEY (file_id)
);
COMMENT ON TABLE  com.tn_file                      IS '파일 그룹 마스터';

-- 파일 상세
CREATE TABLE IF NOT EXISTS com.tn_file_dtl (
    file_id             varchar(20)     NOT NULL,
    file_sn             integer         NOT NULL,
    file_path_nm        varchar(500)    NOT NULL,
    strg_file_nm        varchar(200)    NOT NULL,
    orgnl_file_nm       varchar(500)    NOT NULL,
    file_extn_nm        varchar(20)     NOT NULL,
    file_size           bigint          NOT NULL,
    file_mime_type      varchar(200)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_file_dtl PRIMARY KEY (file_id, file_sn),
    CONSTRAINT fk_tn_file_dtl_file FOREIGN KEY (file_id) REFERENCES com.tn_file(file_id)
);
COMMENT ON TABLE  com.tn_file_dtl                  IS '파일 상세 (file_id 1:N file_sn)';

-- 파일 ↔ 업무 엔터티 범용 매핑
CREATE TABLE IF NOT EXISTS com.tc_atch_file_idntf_info (
    file_id             varchar(20)     NOT NULL,
    rfrnc_tbl_nm        varchar(60)     NOT NULL,
    rfrnc_idntfr_1_nm   varchar(50)     NULL,
    rfrnc_idntfr_2_nm   varchar(50)     NULL,
    rfrnc_idntfr_3_nm   varchar(50)     NULL,
    rfrnc_idntfr_4_nm   varchar(50)     NULL,
    rfrnc_idntfr_5_nm   varchar(50)     NULL,
    rfrnc_idntfr_6_nm   varchar(50)     NULL,
    rfrnc_idntfr_7_nm   varchar(50)     NULL,
    rfrnc_idntfr_8_nm   varchar(50)     NULL,
    rfrnc_idntfr_intg_cn varchar(4000)  NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tc_atch_file_idntf_info PRIMARY KEY (file_id),
    CONSTRAINT uk_tc_atch_file_idntf_info UNIQUE NULLS NOT DISTINCT (rfrnc_tbl_nm,
        rfrnc_idntfr_1_nm, rfrnc_idntfr_2_nm, rfrnc_idntfr_3_nm, rfrnc_idntfr_4_nm,
        rfrnc_idntfr_5_nm, rfrnc_idntfr_6_nm, rfrnc_idntfr_7_nm, rfrnc_idntfr_8_nm),
    CONSTRAINT fk_tc_atch_file_idntf_info_file FOREIGN KEY (file_id) REFERENCES com.tn_file(file_id)
);
COMMENT ON TABLE  com.tc_atch_file_idntf_info      IS '파일-업무엔터티 범용 매핑';

-- 파일 다운로드 이력
CREATE TABLE IF NOT EXISTS com.th_file_dwnld_hstry (
    dwnld_id            bigserial       NOT NULL,
    file_id             varchar(20)     NOT NULL,
    file_sn             integer         NOT NULL,
    dwnld_user_id       varchar(50)     NOT NULL,
    dwnld_dt            timestamp       NOT NULL DEFAULT NOW(),
    ip_addr             varchar(45)     NULL,
    user_agent          varchar(500)    NULL,
    CONSTRAINT pk_th_file_dwnld_hstry PRIMARY KEY (dwnld_id),
    CONSTRAINT fk_th_file_dwnld_hstry_file FOREIGN KEY (file_id, file_sn) REFERENCES com.tn_file_dtl(file_id, file_sn)
);
COMMENT ON TABLE  com.th_file_dwnld_hstry          IS '파일 다운로드 이력 (감사)';

-- 로그인 이력
CREATE TABLE IF NOT EXISTS com.th_logn_hstry (
    logn_hstry_id       bigserial       NOT NULL,
    user_id             varchar(50)     NULL,
    try_dt              timestamp       NOT NULL DEFAULT NOW(),
    logn_rslt_cd        varchar(20)     NOT NULL,
    fail_rsn            varchar(200)    NULL,
    ip_addr             varchar(45)     NULL,
    user_agent          varchar(500)    NULL,
    CONSTRAINT pk_th_logn_hstry PRIMARY KEY (logn_hstry_id)
);
COMMENT ON TABLE  com.th_logn_hstry                IS '로그인 시도 이력 (감사)';
COMMENT ON COLUMN com.th_logn_hstry.logn_rslt_cd   IS 'SUCCESS / FAIL_PWD / FAIL_LOCKED / FAIL_INACTIVE / FAIL_OTHER';

-- 규정 게시판
CREATE TABLE IF NOT EXISTS com.tn_rglt (
    rglt_id             varchar(20)     NOT NULL,
    rglt_se_cd          varchar(20)     NOT NULL,
    rglt_nm             varchar(500)    NOT NULL,
    eff_dt              date            NULL,
    exp_dt              date            NULL,
    summary             varchar(2000)   NULL,
    contents_html       text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_rglt PRIMARY KEY (rglt_id)
);
COMMENT ON TABLE  com.tn_rglt                      IS '규정 게시판 (법령/규정/매뉴얼/공지)';

-- 공지사항
CREATE TABLE IF NOT EXISTS com.tn_ntc (
    ntc_id              varchar(20)     NOT NULL,
    ntc_title           varchar(500)    NOT NULL,
    ntc_contents        text            NOT NULL,
    ntc_target_ognz_se  varchar(100)    NULL,
    inq_cnt             integer         NOT NULL DEFAULT 0,
    fix_yn              char(1)         NOT NULL DEFAULT 'N',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_ntc PRIMARY KEY (ntc_id)
);
COMMENT ON TABLE  com.tn_ntc                       IS '공지사항';

-- ================================================================
-- 스키마 emp — 배출량 모니터링 계획서 (EMP)
-- ================================================================

CREATE TABLE IF NOT EXISTS emp.tn_emp_plan (
    emp_plan_id         varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    emp_ver             varchar(10)     NOT NULL,
    emp_st_cd           varchar(20)     NOT NULL DEFAULT 'DRAFT',
    rprt_yr             char(4)         NOT NULL,
    sbmt_dt             timestamp       NULL,
    aprv_dt             timestamp       NULL,
    aprv_user_id        varchar(50)     NULL,
    rjct_dt             timestamp       NULL,
    rjct_rsn            varchar(2000)   NULL,
    sig_chg_yn          char(1)         NOT NULL DEFAULT 'N',
    prev_emp_plan_id    varchar(10)     NULL,
    rmrk                text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_plan PRIMARY KEY (emp_plan_id),
    CONSTRAINT uk_tn_emp_plan_01 UNIQUE (oprtr_id, emp_ver),
    CONSTRAINT fk_tn_emp_plan_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  emp.tn_emp_plan                  IS '배출량 모니터링 계획서 (EMP) 마스터';
COMMENT ON COLUMN emp.tn_emp_plan.emp_st_cd        IS 'DRAFT/SBMTD/RVWNG/RJCTD/RCMDD/APRVD/CNCLD';
COMMENT ON COLUMN emp.tn_emp_plan.sig_chg_yn       IS '중대 변경 여부 (재승인 대상)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_oprtr_info (
    emp_plan_id         varchar(10)     NOT NULL,
    oprtr_nm            varchar(200)    NOT NULL,
    oprtr_nm_en         varchar(200)    NOT NULL,
    addr                varchar(500)    NULL,
    lglrpr_nm           varchar(100)    NULL,
    icao_desig          char(3)         NULL,
    regis_mark_list     varchar(2000)   NULL,
    aoc_no              varchar(50)     NULL,
    aoc_isue_dt         date            NULL,
    aoc_xpr_dt          date            NULL,
    aoc_athrty_nm       varchar(200)    NULL,
    parent_co_nm        varchar(200)    NULL,
    sbsdry_info         varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_oprtr_info PRIMARY KEY (emp_plan_id),
    CONSTRAINT fk_tn_emp_oprtr_info_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.tn_emp_oprtr_info            IS 'EMP - 운영사 식별정보 (1:1, SFR-002)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_oprtr_cnct (
    emp_plan_id         varchar(10)     NOT NULL,
    cnct_sn             integer         NOT NULL,
    cnct_se_cd          varchar(20)     NOT NULL,
    user_nm             varchar(100)    NOT NULL,
    mblphn_no           varchar(20)     NULL,
    eml_addr            varchar(200)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_oprtr_cnct PRIMARY KEY (emp_plan_id, cnct_sn),
    CONSTRAINT fk_tn_emp_oprtr_cnct_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.tn_emp_oprtr_cnct            IS 'EMP - 담당자 연락처 (PRIMARY/SUB)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_acft (
    emp_plan_id         varchar(10)     NOT NULL,
    acft_sn             integer         NOT NULL,
    acft_type_cd        varchar(10)     NOT NULL,
    fuel_type_cd        varchar(20)     NOT NULL,
    acft_cnt            integer         NOT NULL,
    rmrk                varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_acft PRIMARY KEY (emp_plan_id, acft_sn),
    CONSTRAINT fk_tn_emp_acft_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id),
    CONSTRAINT fk_tn_emp_acft_type FOREIGN KEY (acft_type_cd) REFERENCES com.tc_acft_type_cd(acft_type_cd)
);
COMMENT ON TABLE  emp.tn_emp_acft                  IS 'EMP - 항공기 유형·연료·대수 (SFR-003)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_cntry_pair (
    emp_plan_id         varchar(10)     NOT NULL,
    pair_sn             integer         NOT NULL,
    dprtr_cntry_cd      char(2)         NOT NULL,
    arvl_cntry_cd       char(2)         NOT NULL,
    intl_yn             char(1)         NOT NULL DEFAULT 'Y',
    exempt_cd           varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_cntry_pair PRIMARY KEY (emp_plan_id, pair_sn),
    CONSTRAINT fk_tn_emp_cntry_pair_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id),
    CONSTRAINT fk_tn_emp_cntry_pair_dprtr FOREIGN KEY (dprtr_cntry_cd) REFERENCES com.tc_cntry_cd(cntry_cd),
    CONSTRAINT fk_tn_emp_cntry_pair_arvl FOREIGN KEY (arvl_cntry_cd) REFERENCES com.tc_cntry_cd(cntry_cd)
);
COMMENT ON TABLE  emp.tn_emp_cntry_pair            IS 'EMP - 운항 국가 쌍 (SFR-003)';
COMMENT ON COLUMN emp.tn_emp_cntry_pair.exempt_cd  IS '면제 사유 코드 (HUMANITARIAN/MEDICAL/FIRE/NULL)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_co2_calc (
    emp_plan_id         varchar(10)     NOT NULL,
    mntr_mthd_cd        varchar(20)     NOT NULL,
    cert_use_yn         char(1)         NOT NULL DEFAULT 'N',
    cert_regis_mthd_cd  varchar(20)     NULL,
    fuel_dnsty_se_cd    varchar(20)     NOT NULL,
    est_co2_emsn        numeric(20,4)   NULL,
    est_co2_basis       varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_co2_calc PRIMARY KEY (emp_plan_id),
    CONSTRAINT fk_tn_emp_co2_calc_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.tn_emp_co2_calc              IS 'EMP - 배출량 계산방법 (SFR-004)';
COMMENT ON COLUMN emp.tn_emp_co2_calc.mntr_mthd_cd IS 'MTHD_A / MTHD_B / BLOCK_ON_OFF / REFUEL / BLOCK_ALLOC';
COMMENT ON COLUMN emp.tn_emp_co2_calc.est_co2_emsn IS '연간 추정 CO2 배출량 (t)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_co2_detail (
    emp_plan_id         varchar(10)     NOT NULL,
    mntr_mthd_cd        varchar(20)     NOT NULL,
    msr_tming_desc      varchar(2000)   NULL,
    msr_device_desc     varchar(2000)   NULL,
    msr_proc_desc       varchar(2000)   NULL,
    fuel_dnsty_desc     varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_co2_detail PRIMARY KEY (emp_plan_id, mntr_mthd_cd),
    CONSTRAINT fk_tn_emp_co2_detail_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.tn_emp_co2_detail            IS 'EMP - 방법별 측정 상세 (SFR-004)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_data_ctrl (
    emp_plan_id         varchar(10)     NOT NULL,
    flow_desc           text            NULL,
    gap_thrshld_5pct    char(1)         NOT NULL DEFAULT 'Y',
    snd_src_use_desc    text            NULL,
    risk_anlys          text            NULL,
    sig_chg_aprv_proc   text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_data_ctrl PRIMARY KEY (emp_plan_id),
    CONSTRAINT fk_tn_emp_data_ctrl_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.tn_emp_data_ctrl             IS 'EMP - 데이터 품질 통제 (SFR-005)';

CREATE TABLE IF NOT EXISTS emp.tn_emp_risk (
    emp_plan_id         varchar(10)     NOT NULL,
    risk_sn             integer         NOT NULL,
    risk_desc           varchar(2000)   NOT NULL,
    ctrl_actv           varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_emp_risk PRIMARY KEY (emp_plan_id, risk_sn),
    CONSTRAINT fk_tn_emp_risk_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.tn_emp_risk                  IS 'EMP - 위험·통제 항목 (SFR-005)';

CREATE TABLE IF NOT EXISTS emp.th_emp_chg_hstry (
    chg_hstry_id        bigserial       NOT NULL,
    emp_plan_id         varchar(10)     NOT NULL,
    prev_emp_plan_id    varchar(10)     NULL,
    chg_dt              timestamp       NOT NULL DEFAULT NOW(),
    chg_chptr           varchar(200)    NULL,
    chg_cn              text            NULL,
    sig_chg_yn          char(1)         NOT NULL DEFAULT 'N',
    chg_user_id         varchar(50)     NOT NULL,
    CONSTRAINT pk_th_emp_chg_hstry PRIMARY KEY (chg_hstry_id),
    CONSTRAINT fk_th_emp_chg_hstry_plan FOREIGN KEY (emp_plan_id) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  emp.th_emp_chg_hstry             IS 'EMP - 버전 변경 이력 (SFR-001/007)';

-- ================================================================
-- 스키마 er — 배출량 보고서 + CEF + EUCR + OoM
-- ================================================================

-- ER 마스터
CREATE TABLE IF NOT EXISTS er.tn_er (
    er_id               varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    er_ver              varchar(10)     NOT NULL DEFAULT '1.0',
    er_st_cd            varchar(20)     NOT NULL DEFAULT 'DRAFT',
    rprt_prd_end_dt     date            NULL,
    isue_dt             date            NULL,
    emp_plan_id_apld    varchar(10)     NULL,
    emp_ver_apld        varchar(10)     NULL,
    emp_aprv_dt         date            NULL,
    emp_eff_dt          date            NULL,
    emp_updt_dt         date            NULL,
    cert_use_yn         char(1)         NOT NULL DEFAULT 'N',
    allc_use_yn         char(1)         NOT NULL DEFAULT 'N',
    sbmt_dt             timestamp       NULL,
    aprv_dt             timestamp       NULL,
    aprv_user_id        varchar(50)     NULL,
    rjct_dt             timestamp       NULL,
    rjct_rsn            varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er PRIMARY KEY (er_id),
    CONSTRAINT uk_tn_er_01 UNIQUE (oprtr_id, rprt_yr, er_ver),
    CONSTRAINT fk_tn_er_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id),
    CONSTRAINT fk_tn_er_emp FOREIGN KEY (emp_plan_id_apld) REFERENCES emp.tn_emp_plan(emp_plan_id)
);
COMMENT ON TABLE  er.tn_er                         IS '배출량 보고서 (ER) 마스터 (SFR-009/010)';

CREATE TABLE IF NOT EXISTS er.tn_er_vrfr_info (
    er_id               varchar(10)     NOT NULL,
    vrfr_sn             integer         NOT NULL,
    vrfcn_inst_id       varchar(10)     NOT NULL,
    cnct_desc           varchar(500)    NULL,
    accrd_dtl           varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_vrfr_info PRIMARY KEY (er_id, vrfr_sn),
    CONSTRAINT fk_tn_er_vrfr_info_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_er_vrfr_info_inst FOREIGN KEY (vrfcn_inst_id) REFERENCES com.tn_vrfcn_inst(vrfcn_inst_id)
);
COMMENT ON TABLE  er.tn_er_vrfr_info               IS 'ER - 참여 검증기관 (SFR-009)';

CREATE TABLE IF NOT EXISTS er.tn_er_acft_fuel (
    er_id               varchar(10)     NOT NULL,
    acft_sn             integer         NOT NULL,
    acft_type_cd        varchar(10)     NOT NULL,
    regis_mark          varchar(20)     NOT NULL,
    ownr_ls_se_cd       varchar(20)     NULL,
    fuel_type_cd        varchar(20)     NOT NULL,
    dnsty_se_cd         varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_acft_fuel PRIMARY KEY (er_id, acft_sn),
    CONSTRAINT fk_tn_er_acft_fuel_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_er_acft_fuel_type FOREIGN KEY (acft_type_cd) REFERENCES com.tc_acft_type_cd(acft_type_cd)
);
COMMENT ON TABLE  er.tn_er_acft_fuel               IS 'ER - 항공기·연료 (SFR-011)';

CREATE TABLE IF NOT EXISTS er.tn_er_afbr (
    er_id               varchar(10)     NOT NULL,
    acft_type_cd        varchar(10)     NOT NULL,
    afbr_val            numeric(10,4)   NOT NULL,
    afbr_unit           varchar(20)     NOT NULL DEFAULT 'kg/min',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_afbr PRIMARY KEY (er_id, acft_type_cd),
    CONSTRAINT fk_tn_er_afbr_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_er_afbr_type FOREIGN KEY (acft_type_cd) REFERENCES com.tc_acft_type_cd(acft_type_cd)
);
COMMENT ON TABLE  er.tn_er_afbr                    IS 'ER - 평균 연료연소율 (SFR-010)';

CREATE TABLE IF NOT EXISTS er.tn_er_cntry_pair_co2 (
    er_id               varchar(10)     NOT NULL,
    pair_sn             integer         NOT NULL,
    dprtr_cntry_cd      char(2)         NOT NULL,
    arvl_cntry_cd       char(2)         NOT NULL,
    cer_estm_yn         char(1)         NOT NULL DEFAULT 'N',
    flt_cnt             integer         NOT NULL DEFAULT 0,
    fuel_type_cd        varchar(20)     NOT NULL,
    fuel_wght           numeric(20,4)   NOT NULL DEFAULT 0,
    conv_fctr           numeric(10,4)   NOT NULL,
    co2_emsn            numeric(20,4)   NOT NULL DEFAULT 0,
    ofst_req_yn         char(1)         NOT NULL DEFAULT 'N',
    cef_redu_amt        numeric(20,4)   NOT NULL DEFAULT 0,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_cntry_pair_co2 PRIMARY KEY (er_id, pair_sn),
    CONSTRAINT fk_tn_er_cntry_pair_co2_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_er_cntry_pair_co2_dprtr FOREIGN KEY (dprtr_cntry_cd) REFERENCES com.tc_cntry_cd(cntry_cd),
    CONSTRAINT fk_tn_er_cntry_pair_co2_arvl FOREIGN KEY (arvl_cntry_cd) REFERENCES com.tc_cntry_cd(cntry_cd)
);
COMMENT ON TABLE  er.tn_er_cntry_pair_co2          IS 'ER - 국가 쌍 배출량 (SFR-012)';

CREATE TABLE IF NOT EXISTS er.tn_er_aerdrm_pair_co2 (
    er_id               varchar(10)     NOT NULL,
    pair_sn             integer         NOT NULL,
    dprtr_aerdrm_cd     char(4)         NOT NULL,
    arvl_aerdrm_cd      char(4)         NOT NULL,
    dprtr_cntry_cd      char(2)         NOT NULL,
    arvl_cntry_cd       char(2)         NOT NULL,
    flt_cnt             integer         NOT NULL DEFAULT 0,
    fuel_type_cd        varchar(20)     NOT NULL,
    fuel_wght           numeric(20,4)   NOT NULL DEFAULT 0,
    co2_emsn            numeric(20,4)   NOT NULL DEFAULT 0,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_aerdrm_pair_co2 PRIMARY KEY (er_id, pair_sn),
    CONSTRAINT fk_tn_er_aerdrm_pair_co2_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_er_aerdrm_pair_co2_dprtr_a FOREIGN KEY (dprtr_aerdrm_cd) REFERENCES com.tc_aerdrm_cd(aerdrm_cd),
    CONSTRAINT fk_tn_er_aerdrm_pair_co2_arvl_a FOREIGN KEY (arvl_aerdrm_cd) REFERENCES com.tc_aerdrm_cd(aerdrm_cd)
);
COMMENT ON TABLE  er.tn_er_aerdrm_pair_co2         IS 'ER - 비행장 쌍 배출량 (SFR-013)';

CREATE TABLE IF NOT EXISTS er.tn_er_fuel_smry (
    er_id               varchar(10)     NOT NULL,
    fuel_type_cd        varchar(20)     NOT NULL,
    ttl_fuel_wght       numeric(20,4)   NOT NULL DEFAULT 0,
    ttl_co2_emsn        numeric(20,4)   NOT NULL DEFAULT 0,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_fuel_smry PRIMARY KEY (er_id, fuel_type_cd),
    CONSTRAINT fk_tn_er_fuel_smry_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id)
);
COMMENT ON TABLE  er.tn_er_fuel_smry               IS 'ER - 연료 유형별 총사용량 (SFR-012)';

CREATE TABLE IF NOT EXISTS er.tn_er_data_gap (
    er_id               varchar(10)     NOT NULL,
    gap_sn              integer         NOT NULL,
    gap_dt              date            NULL,
    ref_info            varchar(500)    NULL,
    gap_cause_cd        varchar(20)     NULL,
    gap_type_cd         varchar(20)     NULL,
    repl_mthd_desc      text            NULL,
    afct_co2_emsn       numeric(20,4)   NOT NULL DEFAULT 0,
    thrshld_5pct_xc_yn  char(1)         NOT NULL DEFAULT 'N',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_er_data_gap PRIMARY KEY (er_id, gap_sn),
    CONSTRAINT fk_tn_er_data_gap_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id)
);
COMMENT ON TABLE  er.tn_er_data_gap                IS 'ER - 데이터 갭 (SFR-014)';

-- CEF (적격연료 청구)
CREATE TABLE IF NOT EXISTS er.tn_cef (
    cef_id              varchar(10)     NOT NULL,
    er_id               varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    cef_st_cd           varchar(20)     NOT NULL DEFAULT 'DRAFT',
    ttl_redu_amt        numeric(20,4)   NOT NULL DEFAULT 0,
    sbmt_dt             timestamp       NULL,
    aprv_dt             timestamp       NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_cef PRIMARY KEY (cef_id),
    CONSTRAINT uk_tn_cef_01 UNIQUE (er_id),
    CONSTRAINT fk_tn_cef_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_cef_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  er.tn_cef                        IS 'CEF - 적격연료 청구 (SFR-017/020)';

CREATE TABLE IF NOT EXISTS er.tn_cef_claim (
    cef_id              varchar(10)     NOT NULL,
    claim_no            varchar(50)     NOT NULL,
    pure_fuel_purch_dt  date            NOT NULL,
    fuel_prdc_co_nm     varchar(200)    NULL,
    fuel_prdc_addr      varchar(500)    NULL,
    fuel_prdc_dt        date            NULL,
    fuel_prdc_lc        varchar(500)    NULL,
    fuel_type_cd        varchar(20)     NOT NULL,
    fdstk_type_cd       varchar(50)     NULL,
    conv_proc_cd        varchar(50)     NULL,
    batch_id_no         varchar(100)    NOT NULL,
    pure_fuel_mass      numeric(20,4)   NOT NULL,
    batch_purch_ratio   numeric(7,4)    NULL,
    batch_purch_mass    numeric(20,4)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_cef_claim PRIMARY KEY (cef_id, claim_no),
    CONSTRAINT fk_tn_cef_claim_cef FOREIGN KEY (cef_id) REFERENCES er.tn_cef(cef_id)
);
COMMENT ON TABLE  er.tn_cef_claim                  IS 'CEF - 청구 건 (SFR-017)';

CREATE TABLE IF NOT EXISTS er.tn_cef_lcyc (
    cef_id              varchar(10)     NOT NULL,
    claim_no            varchar(50)     NOT NULL,
    lca_value_se_cd     varchar(20)     NOT NULL,
    core_lca_val        numeric(10,4)   NULL,
    iluc_emsn           numeric(10,4)   NULL,
    ttl_lca_val         numeric(10,4)   NULL,
    sus_evid_file_id    varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_cef_lcyc PRIMARY KEY (cef_id, claim_no),
    CONSTRAINT fk_tn_cef_lcyc_claim FOREIGN KEY (cef_id, claim_no) REFERENCES er.tn_cef_claim(cef_id, claim_no)
);
COMMENT ON TABLE  er.tn_cef_lcyc                   IS 'CEF - 수명주기 배출량 (SFR-018)';

CREATE TABLE IF NOT EXISTS er.tn_cef_spchn (
    cef_id              varchar(10)     NOT NULL,
    claim_no            varchar(50)     NOT NULL,
    chn_sn              integer         NOT NULL,
    sply_chn_role_cd    varchar(20)     NOT NULL,
    co_nm               varchar(200)    NULL,
    co_addr             varchar(500)    NULL,
    lc_addr             varchar(500)    NULL,
    recv_dt             date            NULL,
    recv_mass           numeric(20,4)   NULL,
    blnd_ratio          numeric(7,4)    NULL,
    blnd_evid_file_id   varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_cef_spchn PRIMARY KEY (cef_id, claim_no, chn_sn),
    CONSTRAINT fk_tn_cef_spchn_claim FOREIGN KEY (cef_id, claim_no) REFERENCES er.tn_cef_claim(cef_id, claim_no)
);
COMMENT ON TABLE  er.tn_cef_spchn                  IS 'CEF - 공급망 (SFR-019)';

-- EUCR (배출권 취소 보고서)
CREATE TABLE IF NOT EXISTS er.tn_eucr (
    eucr_id             varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    eucr_ver            varchar(10)     NOT NULL DEFAULT '1.0',
    eucr_st_cd          varchar(20)     NOT NULL DEFAULT 'DRAFT',
    ttl_qty             numeric(20,4)   NOT NULL DEFAULT 0,
    ofst_req_qty        numeric(20,4)   NOT NULL DEFAULT 0,
    fulfilled_yn        char(1)         NOT NULL DEFAULT 'N',
    sbmt_dt             timestamp       NULL,
    aprv_dt             timestamp       NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_eucr PRIMARY KEY (eucr_id),
    CONSTRAINT uk_tn_eucr_01 UNIQUE (oprtr_id, rprt_yr, eucr_ver),
    CONSTRAINT fk_tn_eucr_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  er.tn_eucr                       IS 'EUCR - 배출권 취소 보고서 (SFR-030)';

CREATE TABLE IF NOT EXISTS er.tn_eucr_batch (
    eucr_id             varchar(10)     NOT NULL,
    batch_no            varchar(50)     NOT NULL,
    crdt_type_cd        varchar(50)     NOT NULL,
    sub_qty             numeric(20,4)   NOT NULL,
    prgrm_nm            varchar(200)    NULL,
    vntg_yr             char(4)         NULL,
    mthdlgy_id          varchar(100)    NULL,
    crdt_no_from        varchar(100)    NULL,
    crdt_no_to          varchar(100)    NULL,
    cncl_dt             date            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_eucr_batch PRIMARY KEY (eucr_id, batch_no),
    CONSTRAINT fk_tn_eucr_batch_eucr FOREIGN KEY (eucr_id) REFERENCES er.tn_eucr(eucr_id)
);
COMMENT ON TABLE  er.tn_eucr_batch                 IS 'EUCR - 배출권 배치 (SFR-031)';

CREATE TABLE IF NOT EXISTS er.tn_eucr_crdt_dtl (
    eucr_id             varchar(10)     NOT NULL,
    crdt_no             varchar(100)    NOT NULL,
    batch_no            varchar(50)     NOT NULL,
    mthdlgy_id          varchar(100)    NULL,
    vntg_yr             char(4)         NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_eucr_crdt_dtl PRIMARY KEY (eucr_id, crdt_no),
    CONSTRAINT uk_tn_eucr_crdt_dtl UNIQUE (crdt_no),
    CONSTRAINT fk_tn_eucr_crdt_dtl_batch FOREIGN KEY (eucr_id, batch_no) REFERENCES er.tn_eucr_batch(eucr_id, batch_no)
);
COMMENT ON TABLE  er.tn_eucr_crdt_dtl              IS 'EUCR - 일련번호 상세 (이중사용 방지)';

-- OoM-check
CREATE TABLE IF NOT EXISTS er.tn_oom_check (
    oom_id              varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    er_id               varchar(10)     NULL,
    vr_id               varchar(10)     NULL,
    oom_st_cd           varchar(20)     NOT NULL DEFAULT 'INPRG',
    oom_rslt_cd         varchar(20)     NULL,
    inspn_dt            date            NULL,
    inspctr_user_id     varchar(50)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_oom_check PRIMARY KEY (oom_id),
    CONSTRAINT uk_tn_oom_check_01 UNIQUE (oprtr_id, rprt_yr),
    CONSTRAINT fk_tn_oom_check_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id),
    CONSTRAINT fk_tn_oom_check_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id)
);
COMMENT ON TABLE  er.tn_oom_check                  IS 'OoM-check (SFR-033)';
COMMENT ON COLUMN er.tn_oom_check.oom_rslt_cd      IS 'PASS / FAIL / HOLD';

CREATE TABLE IF NOT EXISTS er.tn_oom_check_item (
    oom_id              varchar(10)     NOT NULL,
    item_no             integer         NOT NULL,
    item_nm             varchar(500)    NOT NULL,
    expctd_val          varchar(100)    NULL,
    rprtd_val           varchar(100)    NULL,
    dvtn_rate           numeric(10,4)   NULL,
    judg_cd             varchar(20)     NULL,
    rmrk                text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_oom_check_item PRIMARY KEY (oom_id, item_no),
    CONSTRAINT fk_tn_oom_check_item_oom FOREIGN KEY (oom_id) REFERENCES er.tn_oom_check(oom_id)
);
COMMENT ON TABLE  er.tn_oom_check_item             IS 'OoM-check 점검 항목 (SFR-034 18종 자동 + 추가)';
COMMENT ON COLUMN er.tn_oom_check_item.judg_cd     IS 'PASS / WARN / FAIL';

CREATE TABLE IF NOT EXISTS er.tn_oom_check_addl_rqst (
    oom_id              varchar(10)     NOT NULL,
    rqst_sn             integer         NOT NULL,
    rqst_dt             timestamp       NOT NULL DEFAULT NOW(),
    rqst_user_id        varchar(50)     NOT NULL,
    rqst_cn             text            NULL,
    resp_dt             timestamp       NULL,
    resp_user_id        varchar(50)     NULL,
    resp_cn             text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_oom_check_addl_rqst PRIMARY KEY (oom_id, rqst_sn),
    CONSTRAINT fk_tn_oom_check_addl_rqst_oom FOREIGN KEY (oom_id) REFERENCES er.tn_oom_check(oom_id)
);
COMMENT ON TABLE  er.tn_oom_check_addl_rqst        IS 'OoM-check 추가 설명 요청 (SFR-033)';

CREATE TABLE IF NOT EXISTS er.tn_oom_check_vrfr_eval (
    oom_id              varchar(10)     NOT NULL,
    vrfcn_inst_id       varchar(10)     NOT NULL,
    eval_grd_cd         varchar(20)     NOT NULL,
    eval_rmrk           text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_oom_check_vrfr_eval PRIMARY KEY (oom_id, vrfcn_inst_id),
    CONSTRAINT fk_tn_oom_check_vrfr_eval_oom FOREIGN KEY (oom_id) REFERENCES er.tn_oom_check(oom_id),
    CONSTRAINT fk_tn_oom_check_vrfr_eval_inst FOREIGN KEY (vrfcn_inst_id) REFERENCES com.tn_vrfcn_inst(vrfcn_inst_id)
);
COMMENT ON TABLE  er.tn_oom_check_vrfr_eval        IS 'OoM-check 검증기관 품질 평가 (GOOD/AVG/POOR)';

-- ================================================================
-- 스키마 vr — 검증보고서 (VR)
-- ================================================================

CREATE TABLE IF NOT EXISTS vr.tn_vr (
    vr_id               varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    vr_ver              varchar(10)     NOT NULL DEFAULT '1.0',
    vr_type_cd          varchar(10)     NOT NULL DEFAULT 'ER',
    vr_st_cd            varchar(20)     NOT NULL DEFAULT 'DRAFT',
    vrfcn_inst_id       varchar(10)     NOT NULL,
    er_id               varchar(10)     NULL,
    eucr_id             varchar(10)     NULL,
    sbmt_dt             timestamp       NULL,
    aprv_dt             timestamp       NULL,
    aprv_user_id        varchar(50)     NULL,
    rjct_dt             timestamp       NULL,
    rjct_rsn            varchar(2000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr PRIMARY KEY (vr_id),
    CONSTRAINT uk_tn_vr_01 UNIQUE (oprtr_id, rprt_yr, vr_type_cd, vr_ver),
    CONSTRAINT fk_tn_vr_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id),
    CONSTRAINT fk_tn_vr_inst FOREIGN KEY (vrfcn_inst_id) REFERENCES com.tn_vrfcn_inst(vrfcn_inst_id),
    CONSTRAINT fk_tn_vr_er FOREIGN KEY (er_id) REFERENCES er.tn_er(er_id),
    CONSTRAINT fk_tn_vr_eucr FOREIGN KEY (eucr_id) REFERENCES er.tn_eucr(eucr_id)
);
COMMENT ON TABLE  vr.tn_vr                         IS '검증 보고서 (VR) 마스터 (SFR-024)';
COMMENT ON COLUMN vr.tn_vr.vr_type_cd              IS 'ER (배출량 보고서 검증) / EUCR (배출권 취소 보고서 검증)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_scope (
    vr_id               varchar(10)     NOT NULL,
    vrfcn_inst_nm       varchar(200)    NOT NULL,
    vrfcn_inst_addr     varchar(500)    NULL,
    rprt_type_desc      varchar(500)    NULL,
    rprt_prd_desc       varchar(500)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_scope PRIMARY KEY (vr_id),
    CONSTRAINT fk_tn_vr_scope_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_scope                   IS 'VR - 범위·식별 (SFR-024/025)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_team (
    vr_id               varchar(10)     NOT NULL,
    member_sn           integer         NOT NULL,
    user_nm             varchar(100)    NOT NULL,
    role_cd             varchar(20)     NOT NULL,
    accrd_dtl           varchar(500)    NULL,
    conscutv_cnt        integer         NOT NULL DEFAULT 0,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_team PRIMARY KEY (vr_id, member_sn),
    CONSTRAINT fk_tn_vr_team_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_team                    IS 'VR - 검증팀 (LEAD/MEMBER/INDEP_REVIEWER)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_time (
    vr_id               varchar(10)     NOT NULL,
    onsite_hrs          numeric(10,2)   NOT NULL DEFAULT 0,
    offsite_hrs         numeric(10,2)   NOT NULL DEFAULT 0,
    total_hrs           numeric(10,2)   NOT NULL DEFAULT 0,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_time PRIMARY KEY (vr_id),
    CONSTRAINT fk_tn_vr_time_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_time                    IS 'VR - 검증 시간 (SFR-025)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_input_info (
    vr_id               varchar(10)     NOT NULL,
    info_sn             integer         NOT NULL,
    doc_nm              varchar(500)    NOT NULL,
    doc_se_cd           varchar(20)     NULL,
    file_id             varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_input_info PRIMARY KEY (vr_id, info_sn),
    CONSTRAINT fk_tn_vr_input_info_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_input_info              IS 'VR - 검증에 활용된 운영사 제공 자료 (SFR-025)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_prcdr (
    vr_id               varchar(10)     NOT NULL,
    strg_anlys_cn       text            NULL,
    risk_eval_cn        text            NULL,
    smplng_actv_cn      text            NULL,
    smplng_rslt_cn      text            NULL,
    emp_compl_cn        text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_prcdr PRIMARY KEY (vr_id),
    CONSTRAINT fk_tn_vr_prcdr_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_prcdr                   IS 'VR - 절차·분석 (SFR-026)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_ncnfrm (
    vr_id               varchar(10)     NOT NULL,
    item_no             integer         NOT NULL,
    ncnfrm_se_cd        varchar(20)     NOT NULL,
    ncnfrm_desc         text            NULL,
    resol_desc          text            NULL,
    resol_dt            timestamp       NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_ncnfrm PRIMARY KEY (vr_id, item_no),
    CONSTRAINT fk_tn_vr_ncnfrm_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_ncnfrm                  IS 'VR - 부적합·허위진술 (MINOR/MAJOR/MISSTATEMENT)';

CREATE TABLE IF NOT EXISTS vr.tn_vr_cncls (
    vr_id               varchar(10)     NOT NULL,
    data_qlty_eval      text            NULL,
    mtrlty_eval         text            NULL,
    er_cncls            text            NULL,
    eucr_cncls          text            NULL,
    judg_cn             text            NULL,
    indep_review_cn     text            NULL,
    indep_review_user_nm varchar(100)   NULL,
    final_opnn_cd       varchar(20)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_vr_cncls PRIMARY KEY (vr_id),
    CONSTRAINT fk_tn_vr_cncls_vr FOREIGN KEY (vr_id) REFERENCES vr.tn_vr(vr_id)
);
COMMENT ON TABLE  vr.tn_vr_cncls                   IS 'VR - 결론·의견 (SFR-027)';
COMMENT ON COLUMN vr.tn_vr_cncls.final_opnn_cd     IS 'REASONABLE / LIMITED / QUALIFIED / ADVERSE';

-- ================================================================
-- 스키마 saf — 지속가능항공유 (SAF)
-- ================================================================

CREATE TABLE IF NOT EXISTS saf.tn_saf_batch (
    batch_id            varchar(50)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    poc_id_no           varchar(100)    NULL,
    poc_isue_dt         date            NULL,
    fuel_supp_oblg_desc varchar(1000)   NULL,
    dprtr_recv_co_nm    varchar(200)    NULL,
    saf_recv_arprt_cd   char(4)         NULL,
    saf_recv_cntry_cd   char(2)         NULL,
    cust_chn_modl_cd    varchar(50)     NULL,
    batch_qty           numeric(20,4)   NULL,
    energy_cn           numeric(20,4)   NULL,
    dnsty_se_cd         varchar(20)     NULL,
    neat_saf_dnsty      numeric(10,4)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_batch PRIMARY KEY (batch_id),
    CONSTRAINT fk_tn_saf_batch_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  saf.tn_saf_batch                 IS 'SAF 배치 (SFR-037/038)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_cert (
    cert_id             varchar(10)     NOT NULL,
    batch_id            varchar(50)     NOT NULL,
    cert_type_cd        varchar(10)     NOT NULL,
    cert_schm_cd        varchar(50)     NOT NULL,
    cert_no             varchar(200)    NOT NULL,
    cert_isue_dt        date            NULL,
    cert_xpr_dt         date            NULL,
    file_id             varchar(20)     NULL,
    srnd_yn             char(1)         NOT NULL DEFAULT 'N',
    srnd_dt             timestamp       NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_cert PRIMARY KEY (cert_id),
    CONSTRAINT uk_tn_saf_cert_01 UNIQUE (cert_no, cert_schm_cd),
    CONSTRAINT fk_tn_saf_cert_batch FOREIGN KEY (batch_id) REFERENCES saf.tn_saf_batch(batch_id)
);
COMMENT ON TABLE  saf.tn_saf_cert                  IS 'SAF 인증서 PoS/PoC (SFR-035/036)';
COMMENT ON COLUMN saf.tn_saf_cert.cert_type_cd     IS 'PoS / PoC';
COMMENT ON COLUMN saf.tn_saf_cert.cert_no          IS '인증서 일련번호 (외부 노출 시 이중청구 위험 - 암호화 대상)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_cert_audit (
    audit_id            bigserial       NOT NULL,
    cert_id             varchar(10)     NOT NULL,
    actn_cd             varchar(20)     NOT NULL,
    actn_user_id        varchar(50)     NOT NULL,
    actn_dt             timestamp       NOT NULL DEFAULT NOW(),
    rmrk                varchar(1000)   NULL,
    CONSTRAINT pk_tn_saf_cert_audit PRIMARY KEY (audit_id),
    CONSTRAINT fk_tn_saf_cert_audit_cert FOREIGN KEY (cert_id) REFERENCES saf.tn_saf_cert(cert_id)
);
COMMENT ON TABLE  saf.tn_saf_cert_audit            IS 'SAF 인증서 감사 추적 (UPLD/EXTR/SRND)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_prdc_sply (
    batch_id            varchar(50)     NOT NULL,
    prdc_co_nm          varchar(200)    NULL,
    prdc_co_addr        varchar(500)    NULL,
    prdc_pos_batch_id   varchar(100)    NULL,
    prdc_pos_isue_dt    date            NULL,
    orgn_saf_qty        numeric(20,4)   NULL,
    saf_prdc_dt         date            NULL,
    acqstn_dt           date            NULL,
    prdc_lc_addr        varchar(500)    NULL,
    sply_co_nm          varchar(200)    NULL,
    sply_co_addr        varchar(500)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_prdc_sply PRIMARY KEY (batch_id),
    CONSTRAINT fk_tn_saf_prdc_sply_batch FOREIGN KEY (batch_id) REFERENCES saf.tn_saf_batch(batch_id)
);
COMMENT ON TABLE  saf.tn_saf_prdc_sply             IS 'SAF 생산사·공급사 (SFR-039)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_blndr (
    batch_id            varchar(50)     NOT NULL,
    blndr_co_nm         varchar(200)    NULL,
    blndr_co_addr       varchar(500)    NULL,
    blnd_lc_addr        varchar(500)    NULL,
    recv_dt             date            NULL,
    recv_mass           numeric(20,4)   NULL,
    fuel_type_cd        varchar(20)     NULL,
    blnd_ratio          numeric(7,4)    NULL,
    trnsprt_co_nm       varchar(200)    NULL,
    trnsprt_co_addr     varchar(500)    NULL,
    mid_buyer_co_nm     varchar(200)    NULL,
    mid_buyer_co_addr   varchar(500)    NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_blndr PRIMARY KEY (batch_id),
    CONSTRAINT fk_tn_saf_blndr_batch FOREIGN KEY (batch_id) REFERENCES saf.tn_saf_batch(batch_id)
);
COMMENT ON TABLE  saf.tn_saf_blndr                 IS 'SAF 혼합사 (SFR-040)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_feed (
    batch_id            varchar(50)     NOT NULL,
    fdstk_type_cd       varchar(50)     NOT NULL,
    addl_fdstk_dtl      varchar(2000)   NULL,
    waste_residue_yn    char(1)         NOT NULL DEFAULT 'N',
    conv_proc_cd        varchar(50)     NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_feed PRIMARY KEY (batch_id),
    CONSTRAINT fk_tn_saf_feed_batch FOREIGN KEY (batch_id) REFERENCES saf.tn_saf_batch(batch_id)
);
COMMENT ON TABLE  saf.tn_saf_feed                  IS 'SAF 원료·제품 (SFR-041)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_feed_orgn (
    batch_id            varchar(50)     NOT NULL,
    orgn_sn             integer         NOT NULL,
    orgn_cntry_cd       char(2)         NOT NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_feed_orgn PRIMARY KEY (batch_id, orgn_sn),
    CONSTRAINT fk_tn_saf_feed_orgn_feed FOREIGN KEY (batch_id) REFERENCES saf.tn_saf_feed(batch_id),
    CONSTRAINT fk_tn_saf_feed_orgn_cntry FOREIGN KEY (orgn_cntry_cd) REFERENCES com.tc_cntry_cd(cntry_cd)
);
COMMENT ON TABLE  saf.tn_saf_feed_orgn             IS 'SAF 원료 원산국 (1:N)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_ghg (
    batch_id            varchar(50)     NOT NULL,
    ghg_val_se_cd       varchar(20)     NOT NULL,
    core_lca_val        numeric(10,4)   NULL,
    iluc_emsn           numeric(10,4)   NULL,
    ttl_lca_val         numeric(10,4)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_ghg PRIMARY KEY (batch_id),
    CONSTRAINT fk_tn_saf_ghg_batch FOREIGN KEY (batch_id) REFERENCES saf.tn_saf_batch(batch_id)
);
COMMENT ON TABLE  saf.tn_saf_ghg                   IS 'SAF 온실가스 배출 (SFR-042)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_airprt_fuel (
    airprt_cd           char(4)         NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    flt_cnt             integer         NOT NULL DEFAULT 0,
    flt_time            numeric(15,2)   NOT NULL DEFAULT 0,
    req_fuel_qty        numeric(20,4)   NOT NULL DEFAULT 0,
    actl_fuel_qty       numeric(20,4)   NOT NULL DEFAULT 0,
    yr_non_tanked_qty   numeric(20,4)   NOT NULL DEFAULT 0,
    yr_tanked_safety_qty numeric(20,4)  NOT NULL DEFAULT 0,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_airprt_fuel PRIMARY KEY (airprt_cd, rprt_yr, oprtr_id),
    CONSTRAINT fk_tn_saf_airprt_fuel_arpt FOREIGN KEY (airprt_cd) REFERENCES com.tc_aerdrm_cd(aerdrm_cd),
    CONSTRAINT fk_tn_saf_airprt_fuel_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  saf.tn_saf_airprt_fuel           IS 'SAF 공항별 급유 실적 (SFR-045/049)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_airprt_purch (
    airprt_cd           char(4)         NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    purch_sn            integer         NOT NULL,
    sply_co_nm          varchar(200)    NULL,
    batch_id            varchar(50)     NULL,
    purch_qty           numeric(20,4)   NOT NULL,
    fuel_type_cd        varchar(20)     NULL,
    orgn_info           varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_airprt_purch PRIMARY KEY (airprt_cd, rprt_yr, oprtr_id, purch_sn),
    CONSTRAINT fk_tn_saf_airprt_purch_arpt FOREIGN KEY (airprt_cd) REFERENCES com.tc_aerdrm_cd(aerdrm_cd),
    CONSTRAINT fk_tn_saf_airprt_purch_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  saf.tn_saf_airprt_purch          IS 'SAF 공항별 구매 (SFR-050)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_tankering_mntr (
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    airprt_cd           char(4)         NOT NULL,
    accm_qty            numeric(20,4)   NOT NULL DEFAULT 0,
    actl_fuel_qty       numeric(20,4)   NOT NULL DEFAULT 0,
    req_fuel_qty        numeric(20,4)   NOT NULL DEFAULT 0,
    refuel_ratio        numeric(7,4)    NULL,
    ovr_90pct_yn        char(1)         NOT NULL DEFAULT 'N',
    soln_desc           text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_tankering_mntr PRIMARY KEY (oprtr_id, rprt_yr, airprt_cd),
    CONSTRAINT fk_tn_saf_tankering_mntr_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id),
    CONSTRAINT fk_tn_saf_tankering_mntr_arpt FOREIGN KEY (airprt_cd) REFERENCES com.tc_aerdrm_cd(aerdrm_cd)
);
COMMENT ON TABLE  saf.tn_saf_tankering_mntr        IS 'SAF 탱커링 90% 모니터링 (SFR-046)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_blnd_mntr (
    oprtr_id            varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    total_fuel_qty      numeric(20,4)   NOT NULL DEFAULT 0,
    saf_cert_purch_qty  numeric(20,4)   NOT NULL DEFAULT 0,
    blnd_ratio          numeric(7,4)    NULL,
    oblg_ratio          numeric(7,4)    NULL,
    fulfilled_yn        char(1)         NOT NULL DEFAULT 'N',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_blnd_mntr PRIMARY KEY (oprtr_id, rprt_yr),
    CONSTRAINT fk_tn_saf_blnd_mntr_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  saf.tn_saf_blnd_mntr             IS 'SAF 혼합비율 의무 모니터링 (SFR-046)';

CREATE TABLE IF NOT EXISTS saf.tn_saf_fld_inspn (
    inspn_id            varchar(10)     NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    airprt_cd           char(4)         NOT NULL,
    inspn_dt            date            NOT NULL,
    inspctr_user_id     varchar(50)     NOT NULL,
    chk_item_rslt       text            NULL,
    memo                text            NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_saf_fld_inspn PRIMARY KEY (inspn_id),
    CONSTRAINT fk_tn_saf_fld_inspn_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id),
    CONSTRAINT fk_tn_saf_fld_inspn_arpt FOREIGN KEY (airprt_cd) REFERENCES com.tc_aerdrm_cd(aerdrm_cd)
);
COMMENT ON TABLE  saf.tn_saf_fld_inspn             IS 'SAF 현장 실사 (SFR-051)';

-- ================================================================
-- 스키마 ptl — 포털·CCR·시뮬레이션·통계
-- ================================================================

CREATE TABLE IF NOT EXISTS ptl.tn_ptl_ccr_extr (
    extr_id             varchar(10)     NOT NULL,
    rprt_yr             char(4)         NOT NULL,
    extr_scope_cd       varchar(20)     NOT NULL,
    extr_st_cd          varchar(20)     NOT NULL DEFAULT 'INPRG',
    file_id             varchar(20)     NULL,
    extr_user_id        varchar(50)     NOT NULL,
    extr_dt             timestamp       NOT NULL DEFAULT NOW(),
    rmrk                varchar(1000)   NULL,
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_ptl_ccr_extr PRIMARY KEY (extr_id)
);
COMMENT ON TABLE  ptl.tn_ptl_ccr_extr              IS 'CCR 추출 이력 (SFR-055)';

CREATE TABLE IF NOT EXISTS ptl.tn_ptl_sim (
    sim_id              varchar(10)     NOT NULL,
    sim_nm              varchar(200)    NOT NULL,
    owner_user_id       varchar(50)     NOT NULL,
    scope_se_cd         varchar(20)     NOT NULL,
    scope_oprtr_id      varchar(10)     NULL,
    base_yr             char(4)         NULL,
    prdctn_yr_from      char(4)         NULL,
    prdctn_yr_to        char(4)         NULL,
    input_json          jsonb           NULL,
    rslt_json           jsonb           NULL,
    share_se_cd         varchar(20)     NOT NULL DEFAULT 'PRIVATE',
    use_bgng_dt         timestamp       NOT NULL DEFAULT NOW(),
    use_end_dt          timestamp       NOT NULL DEFAULT '9999-12-31 23:59:59',
    frst_reg_dt         timestamp       NOT NULL DEFAULT NOW(),
    frst_reg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    last_chg_dt         timestamp       NOT NULL DEFAULT NOW(),
    last_chg_user_id    varchar(50)     NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tn_ptl_sim PRIMARY KEY (sim_id),
    CONSTRAINT fk_tn_ptl_sim_user FOREIGN KEY (owner_user_id) REFERENCES com.tn_user(user_id)
);
COMMENT ON TABLE  ptl.tn_ptl_sim                   IS '상쇄비용 시뮬레이션 시나리오·결과 (SFR-056)';

CREATE TABLE IF NOT EXISTS ptl.tn_ptl_stat_yearly (
    rprt_yr             char(4)         NOT NULL,
    oprtr_id            varchar(10)     NOT NULL,
    ttl_co2_emsn        numeric(20,4)   NOT NULL DEFAULT 0,
    ttl_ofst_req        numeric(20,4)   NOT NULL DEFAULT 0,
    ttl_cef_redu        numeric(20,4)   NOT NULL DEFAULT 0,
    ttl_flt_cnt         integer         NOT NULL DEFAULT 0,
    ttl_fuel_wght       numeric(20,4)   NOT NULL DEFAULT 0,
    ttl_saf_qty         numeric(20,4)   NOT NULL DEFAULT 0,
    data_gap_cnt        integer         NOT NULL DEFAULT 0,
    last_aggr_dt        timestamp       NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_tn_ptl_stat_yearly PRIMARY KEY (rprt_yr, oprtr_id),
    CONSTRAINT fk_tn_ptl_stat_yearly_oprtr FOREIGN KEY (oprtr_id) REFERENCES com.tn_oprtr(oprtr_id)
);
COMMENT ON TABLE  ptl.tn_ptl_stat_yearly           IS '연도별 통계 집계 (배치 갱신)';

CREATE TABLE IF NOT EXISTS ptl.th_user_actn (
    actn_id             bigserial       NOT NULL,
    user_id             varchar(50)     NOT NULL,
    actn_se_cd          varchar(30)     NOT NULL,
    target_tbl          varchar(60)     NULL,
    target_pk           varchar(500)    NULL,
    actn_dt             timestamp       NOT NULL DEFAULT NOW(),
    rslt_cd             varchar(20)     NULL,
    ip_addr             varchar(45)     NULL,
    user_agent          varchar(500)    NULL,
    rmrk                varchar(2000)   NULL,
    CONSTRAINT pk_th_user_actn PRIMARY KEY (actn_id)
);
COMMENT ON TABLE  ptl.th_user_actn                 IS '사용자 행위 감사 (제출/승인/취소/추출/권한거절)';

-- ================================================================
-- 인덱스 (자주 조회되는 컬럼)
-- ================================================================

CREATE INDEX IF NOT EXISTS ix_tn_user_01 ON com.tn_user (ognz_id);
CREATE INDEX IF NOT EXISTS ix_tn_oprtr_01 ON com.tn_oprtr (ognz_id);
CREATE INDEX IF NOT EXISTS ix_tn_vrfcn_inst_01 ON com.tn_vrfcn_inst (ognz_id);
CREATE INDEX IF NOT EXISTS ix_tn_vrfcn_assgn_01 ON com.tn_vrfcn_assgn (oprtr_id, rprt_yr);
CREATE INDEX IF NOT EXISTS ix_tn_emp_plan_01 ON emp.tn_emp_plan (oprtr_id, rprt_yr);
CREATE INDEX IF NOT EXISTS ix_tn_emp_plan_02 ON emp.tn_emp_plan (emp_st_cd);
CREATE INDEX IF NOT EXISTS ix_tn_er_01 ON er.tn_er (oprtr_id, rprt_yr);
CREATE INDEX IF NOT EXISTS ix_tn_er_02 ON er.tn_er (er_st_cd);
CREATE INDEX IF NOT EXISTS ix_tn_cef_01 ON er.tn_cef (oprtr_id, rprt_yr);
CREATE INDEX IF NOT EXISTS ix_tn_cef_claim_01 ON er.tn_cef_claim (batch_id_no);
CREATE INDEX IF NOT EXISTS ix_tn_eucr_batch_01 ON er.tn_eucr_batch (batch_no);
CREATE INDEX IF NOT EXISTS ix_tn_vr_01 ON vr.tn_vr (oprtr_id, rprt_yr);
CREATE INDEX IF NOT EXISTS ix_tn_vr_02 ON vr.tn_vr (vrfcn_inst_id);
CREATE INDEX IF NOT EXISTS ix_tn_saf_cert_01 ON saf.tn_saf_cert (batch_id);
CREATE INDEX IF NOT EXISTS ix_tn_saf_cert_02 ON saf.tn_saf_cert (cert_no);
CREATE INDEX IF NOT EXISTS ix_tn_saf_batch_01 ON saf.tn_saf_batch (oprtr_id);
CREATE INDEX IF NOT EXISTS ix_th_logn_hstry_01 ON com.th_logn_hstry (user_id, try_dt DESC);
CREATE INDEX IF NOT EXISTS ix_th_user_actn_01 ON ptl.th_user_actn (user_id, actn_dt DESC);
CREATE INDEX IF NOT EXISTS ix_th_user_actn_02 ON ptl.th_user_actn (target_tbl, target_pk);

-- ================================================================
-- 끝
-- ================================================================
