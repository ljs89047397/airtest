-- ================================================================
-- icas-cems — 초기 시드 데이터 (멱등 INSERT)
-- ================================================================

-- ----------------------------------------------------------------
-- 1. 공통코드 그룹
-- ----------------------------------------------------------------
INSERT INTO com.tc_com_cd_group (grp_id, grp_nm, grp_desc) VALUES
    ('OGNZ_SE_CD',     '기관 유형',         '국토부/공단/항공사/검증기관'),
    ('SYS_SE_CD',      '시스템 구분',       'COM/EMP/ER/VR/SAF/PTL'),
    ('WKFLW_ST_CD',    '워크플로우 상태',   'DRAFT/SBMTD/RVWNG/RJCTD/RCMDD/APRVD/CNCLD'),
    ('FUEL_TYPE_CD',   '연료 유형',         'Jet-A/Jet-A1/TS-1/SAF/etc'),
    ('FUEL_CONV_FCTR', '연료 변환계수',     '연료유형별 CO2 변환계수'),
    ('CORSIA_THRSHLD', 'CORSIA 임계치',     '10000t/500000t/50000t 등'),
    ('SAF_CERT_SCHM_CD','SAF 인증 체계',    'ISCC/RSB/etc'),
    ('SAF_CERT_TYPE_CD','SAF 인증서 유형',  'PoS/PoC'),
    ('FDSTK_TYPE_CD',  'SAF 원료 유형',     'UCO/Tallow/PFAD/etc'),
    ('CONV_PROC_CD',   'SAF 전환 공정',     'HEFA/FT/etc'),
    ('MNTR_MTHD_CD',   '모니터링 방법',     'MTHD_A/MTHD_B/BLOCK_ON_OFF/REFUEL/BLOCK_ALLOC'),
    ('FILE_SRC_CD',    '파일 출처',         'EMP/ER/CEF/VR/EUCR/OOM/SAF/PoS/PoC/FLD/RGL/NTC'),
    ('LCA_VALUE_SE_CD','LCA 값 구분',       'DEFAULT/ACTUAL'),
    ('DNSTY_SE_CD',    '연료 밀도 구분',    'STD/ACT'),
    ('OWNR_LS_SE_CD',  '소유/임차 구분',    'OWN/LEASE'),
    ('CUST_CHN_MODL_CD','공급망 추적 모델', 'PHYS_SEG/BAL_BOOK/MASS_BAL'),
    ('CERT_REGIS_MTHD_CD','CERT 등록 방법', 'GCD/BLOCK_TIME'),
    ('FUEL_DNSTY_SE_CD','연료 밀도 사용',   'STD_DNSTY/ACT_DNSTY'),
    ('CNCT_SE_CD',     '담당자 구분',       'PRIMARY/SUB'),
    ('SPLY_CHN_ROLE_CD','공급망 역할',      'MID_BUYER/SHIPPER/BLENDER'),
    ('CRDT_TYPE_CD',   '배출권 유형',       'VCS/Gold Standard/etc'),
    ('NCNFRM_SE_CD',   '부적합 구분',       'MINOR/MAJOR/MISSTATEMENT'),
    ('VR_FINAL_OPNN_CD','VR 최종 의견',     'REASONABLE/LIMITED/QUALIFIED/ADVERSE'),
    ('VR_TEAM_ROLE_CD','VR 검증팀 역할',    'LEAD/MEMBER/INDEP_REVIEWER'),
    ('OOM_RSLT_CD',    'OoM 판정',          'PASS/WARN/FAIL'),
    ('OOM_EVAL_GRD_CD','검증기관 평가 등급','GOOD/AVG/POOR'),
    ('EXEMPT_CD',      '면제 사유',         'HUMANITARIAN/MEDICAL/FIRE'),
    ('EXTR_SCOPE_CD',  'CCR 추출 범위',     'ALL/SELECTED'),
    ('SHARE_SE_CD',    '공유 범위',         'PRIVATE/ORG/PUBLIC'),
    ('RGLT_SE_CD',     '규정 구분',         'LAW/RGLTN/MNL/NTC'),
    ('ATCH_REF_SE_CD', '결재 참조',         'REF/CC/INFORM'),
    ('GAP_CAUSE_CD',   '데이터 갭 원인',    'OUTAGE/MAINTENANCE/MISSING'),
    ('GAP_TYPE_CD',    '데이터 갭 유형',    'FUEL/FLT_CNT/OTHERS'),
    ('USER_ACTN_SE_CD','사용자 행위',       'LOGN/LOGT/SBMT/APRV/RJCT/CNCL/EXTR/DWNLD/AUTHZ_DENY'),
    ('CD_ATTR_DESC',   '코드 속성 설명',    '확장속성 의미 정의')
ON CONFLICT (grp_id) DO UPDATE SET grp_nm = EXCLUDED.grp_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 2. 공통코드 상세
-- ----------------------------------------------------------------
-- 기관 유형
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord) VALUES
    ('OGNZ_SE_CD', 'MOLIT',    '국토교통부',       10),
    ('OGNZ_SE_CD', 'KOTSA',    '한국교통안전공단', 20),
    ('OGNZ_SE_CD', 'AIRLINE',  '항공사',           30),
    ('OGNZ_SE_CD', 'VERIFIER', '검증기관',         40)
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- 시스템 구분
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord) VALUES
    ('SYS_SE_CD', 'COM', '공통',           10),
    ('SYS_SE_CD', 'EMP', '모니터링계획',   20),
    ('SYS_SE_CD', 'ER',  '배출량보고서',   30),
    ('SYS_SE_CD', 'VR',  '검증보고서',     40),
    ('SYS_SE_CD', 'SAF', '지속가능항공유', 50),
    ('SYS_SE_CD', 'PTL', '포털/통계',      60)
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- 워크플로우 상태
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord, cd_attr_1) VALUES
    ('WKFLW_ST_CD', 'DRAFT', '작성중',  10, '#6c757d'),
    ('WKFLW_ST_CD', 'SBMTD', '제출됨',  20, '#0d6efd'),
    ('WKFLW_ST_CD', 'RVWNG', '검토중',  30, '#ffc107'),
    ('WKFLW_ST_CD', 'RJCTD', '반려',    40, '#dc3545'),
    ('WKFLW_ST_CD', 'RCMDD', '권고됨',  50, '#fd7e14'),
    ('WKFLW_ST_CD', 'APRVD', '승인',    60, '#198754'),
    ('WKFLW_ST_CD', 'CNCLD', '취소',    70, '#212529')
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- 연료 유형
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord) VALUES
    ('FUEL_TYPE_CD', 'JET_A',    'Jet-A',  10),
    ('FUEL_TYPE_CD', 'JET_A1',   'Jet-A1', 20),
    ('FUEL_TYPE_CD', 'JET_B',    'Jet-B',  30),
    ('FUEL_TYPE_CD', 'TS_1',     'TS-1',   40),
    ('FUEL_TYPE_CD', 'AVGAS',    'AvGas',  50),
    ('FUEL_TYPE_CD', 'SAF',      '지속가능항공유 (SAF)', 60),
    ('FUEL_TYPE_CD', 'OTHERS',   '기타',   90)
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- 연료 변환계수 (ICAO 표준 — 톤 연료당 CO2 톤)
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord, cd_attr_1) VALUES
    ('FUEL_CONV_FCTR', 'JET_A',  'Jet-A 변환계수',  10, '3.16'),
    ('FUEL_CONV_FCTR', 'JET_A1', 'Jet-A1 변환계수', 20, '3.16'),
    ('FUEL_CONV_FCTR', 'JET_B',  'Jet-B 변환계수',  30, '3.10'),
    ('FUEL_CONV_FCTR', 'TS_1',   'TS-1 변환계수',   40, '3.16'),
    ('FUEL_CONV_FCTR', 'AVGAS',  'AvGas 변환계수',  50, '3.10'),
    ('FUEL_CONV_FCTR', 'SAF',    'SAF 변환계수',    60, '3.16')
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_attr_1 = EXCLUDED.cd_attr_1, last_chg_dt = NOW();

-- CORSIA 임계치
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord, cd_attr_1) VALUES
    ('CORSIA_THRSHLD', 'REPORT_OBLG_TON',  'CORSIA 보고 의무 임계치(t)',         10, '10000'),
    ('CORSIA_THRSHLD', 'CERT_LIMIT_TON_A', 'CERT 사용 임계치 A(t)',              20, '500000'),
    ('CORSIA_THRSHLD', 'CERT_LIMIT_TON_B', 'CERT 사용 임계치 B(t)',              30, '50000'),
    ('CORSIA_THRSHLD', 'DATA_GAP_RATIO',   '데이터 갭 임계치(%)',                40, '5'),
    ('CORSIA_THRSHLD', 'TANKERING_RATIO',  '탱커링 방지 급유 임계치(%)',         50, '90'),
    ('CORSIA_THRSHLD', 'YOY_DVTN_RATIO',   '전년 대비 변동 이상치(%)',           60, '30'),
    ('CORSIA_THRSHLD', 'CONSCUTV_LIMIT',   '검증팀 리더 연속 검증 한도(년)',     70, '3')
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_attr_1 = EXCLUDED.cd_attr_1, last_chg_dt = NOW();

-- SAF 인증 체계
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord) VALUES
    ('SAF_CERT_SCHM_CD', 'ISCC', 'ISCC', 10),
    ('SAF_CERT_SCHM_CD', 'RSB',  'RSB',  20),
    ('SAF_CERT_SCHM_CD', 'REDC', 'REDcert', 30),
    ('SAF_CERT_SCHM_CD', '2BSV', '2BSvs', 40),
    ('SAF_CERT_SCHM_CD', 'OTHERS', '기타', 90)
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- 모니터링 방법
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord) VALUES
    ('MNTR_MTHD_CD', 'MTHD_A',       '방법 A',             10),
    ('MNTR_MTHD_CD', 'MTHD_B',       '방법 B',             20),
    ('MNTR_MTHD_CD', 'BLOCK_ON_OFF', '블록오프/블록온',    30),
    ('MNTR_MTHD_CD', 'REFUEL',       '연료주입 방법',      40),
    ('MNTR_MTHD_CD', 'BLOCK_ALLOC',  '블록시간별 연료할당',50)
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- VR 최종 의견
INSERT INTO com.tc_com_cd_dtl (grp_id, cd, cd_nm, cd_ord) VALUES
    ('VR_FINAL_OPNN_CD', 'REASONABLE', '합리적 확신',  10),
    ('VR_FINAL_OPNN_CD', 'LIMITED',    '제한적 확신',  20),
    ('VR_FINAL_OPNN_CD', 'QUALIFIED',  '한정 의견',    30),
    ('VR_FINAL_OPNN_CD', 'ADVERSE',    '부적정 의견',  40)
ON CONFLICT (grp_id, cd) DO UPDATE SET cd_nm = EXCLUDED.cd_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 3. 국가 코드 (CORSIA 참여국 중심 일부)
-- ----------------------------------------------------------------
INSERT INTO com.tc_cntry_cd (cntry_cd, cntry_nm_ko, cntry_nm_en, icao_prtcpt_yn, icao_prtcpt_bgng_yr) VALUES
    ('KR', '대한민국',  'Republic of Korea',   'Y', '2021'),
    ('US', '미국',      'United States',       'Y', '2021'),
    ('JP', '일본',      'Japan',               'Y', '2021'),
    ('CN', '중국',      'China',               'N', NULL),
    ('GB', '영국',      'United Kingdom',      'Y', '2021'),
    ('DE', '독일',      'Germany',             'Y', '2021'),
    ('FR', '프랑스',    'France',              'Y', '2021'),
    ('SG', '싱가포르',  'Singapore',           'Y', '2021'),
    ('HK', '홍콩',      'Hong Kong (China)',   'N', NULL),
    ('VN', '베트남',    'Vietnam',             'Y', '2021'),
    ('TH', '태국',      'Thailand',            'Y', '2021'),
    ('PH', '필리핀',    'Philippines',         'Y', '2027'),
    ('AU', '호주',      'Australia',           'Y', '2021'),
    ('NZ', '뉴질랜드',  'New Zealand',         'Y', '2021'),
    ('CA', '캐나다',    'Canada',              'Y', '2021'),
    ('AE', 'UAE',       'United Arab Emirates','Y', '2021'),
    ('TR', '터키',      'Türkiye',             'Y', '2021'),
    ('IN', '인도',      'India',               'N', NULL),
    ('RU', '러시아',    'Russia',              'N', NULL),
    ('BR', '브라질',    'Brazil',              'Y', '2027')
ON CONFLICT (cntry_cd) DO UPDATE SET cntry_nm_ko = EXCLUDED.cntry_nm_ko, icao_prtcpt_yn = EXCLUDED.icao_prtcpt_yn, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 4. 공항 코드 (한국 + 주요 국제선)
-- ----------------------------------------------------------------
INSERT INTO com.tc_aerdrm_cd (aerdrm_cd, iata_cd, aerdrm_nm_ko, aerdrm_nm_en, cntry_cd, cty_nm) VALUES
    ('RKSI', 'ICN', '인천국제공항',  'Incheon International',  'KR', '인천'),
    ('RKSS', 'GMP', '김포국제공항',  'Gimpo International',    'KR', '서울'),
    ('RKPC', 'CJU', '제주국제공항',  'Jeju International',     'KR', '제주'),
    ('RKPK', 'PUS', '김해국제공항',  'Gimhae International',   'KR', '부산'),
    ('RKTU', 'CJJ', '청주국제공항',  'Cheongju International', 'KR', '청주'),
    ('RKTH', 'TAE', '대구국제공항',  'Daegu International',    'KR', '대구'),
    ('KLAX', 'LAX', 'LA 국제공항',   'Los Angeles Intl',       'US', 'Los Angeles'),
    ('KJFK', 'JFK', 'JFK 국제공항',  'John F. Kennedy Intl',   'US', 'New York'),
    ('RJTT', 'HND', '하네다 공항',   'Tokyo Haneda',           'JP', 'Tokyo'),
    ('RJAA', 'NRT', '나리타 공항',   'Tokyo Narita',           'JP', 'Tokyo'),
    ('ZBAA', 'PEK', '베이징 수도',   'Beijing Capital',        'CN', 'Beijing'),
    ('ZSPD', 'PVG', '상하이 푸동',   'Shanghai Pudong',        'CN', 'Shanghai'),
    ('VHHH', 'HKG', '홍콩 국제',     'Hong Kong Intl',         'HK', 'Hong Kong'),
    ('WSSS', 'SIN', '창이 공항',     'Singapore Changi',       'SG', 'Singapore'),
    ('EGLL', 'LHR', '히드로 공항',   'London Heathrow',        'GB', 'London'),
    ('EDDF', 'FRA', '프랑크푸르트',  'Frankfurt',              'DE', 'Frankfurt'),
    ('LFPG', 'CDG', '샤를드골',      'Charles de Gaulle',      'FR', 'Paris'),
    ('VVNB', 'HAN', '노이바이',      'Noi Bai',                'VN', 'Hanoi'),
    ('VTBS', 'BKK', '수완나품',      'Suvarnabhumi',           'TH', 'Bangkok'),
    ('OMDB', 'DXB', '두바이',        'Dubai Intl',             'AE', 'Dubai')
ON CONFLICT (aerdrm_cd) DO UPDATE SET aerdrm_nm_ko = EXCLUDED.aerdrm_nm_ko, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 5. ICAO 항공기 유형 (주요)
-- ----------------------------------------------------------------
INSERT INTO com.tc_acft_type_cd (acft_type_cd, acft_type_nm, mfctr_nm, eng_cnt, afbr_dflt_val, afbr_unit) VALUES
    ('B738', 'Boeing 737-800',  'Boeing',  2, 41.5, 'kg/min'),
    ('B739', 'Boeing 737-900',  'Boeing',  2, 42.0, 'kg/min'),
    ('B77W', 'Boeing 777-300ER','Boeing',  2, 110.0, 'kg/min'),
    ('B77L', 'Boeing 777-200LR','Boeing',  2, 108.0, 'kg/min'),
    ('B748', 'Boeing 747-8',    'Boeing',  4, 165.0, 'kg/min'),
    ('B763', 'Boeing 767-300',  'Boeing',  2, 76.0, 'kg/min'),
    ('B788', 'Boeing 787-8',    'Boeing',  2, 73.0, 'kg/min'),
    ('B789', 'Boeing 787-9',    'Boeing',  2, 80.0, 'kg/min'),
    ('A320', 'Airbus A320',     'Airbus',  2, 40.0, 'kg/min'),
    ('A321', 'Airbus A321',     'Airbus',  2, 44.0, 'kg/min'),
    ('A332', 'Airbus A330-200', 'Airbus',  2, 95.0, 'kg/min'),
    ('A333', 'Airbus A330-300', 'Airbus',  2, 97.0, 'kg/min'),
    ('A359', 'Airbus A350-900', 'Airbus',  2, 90.0, 'kg/min'),
    ('A35K', 'Airbus A350-1000','Airbus',  2, 100.0, 'kg/min'),
    ('A388', 'Airbus A380-800', 'Airbus',  4, 175.0, 'kg/min')
ON CONFLICT (acft_type_cd) DO UPDATE SET afbr_dflt_val = EXCLUDED.afbr_dflt_val, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 6. 기관 (4종 + 항공사·검증기관 샘플)
-- ----------------------------------------------------------------
INSERT INTO com.tn_ognz (ognz_id, ognz_se_cd, ognz_nm, ognz_nm_en, biz_no) VALUES
    ('MOLIT001',  'MOLIT',    '국토교통부 항공기술과', 'MOLIT Aviation Technology Division', NULL),
    ('KOTSA001',  'KOTSA',    '한국교통안전공단',       'Korea Transportation Safety Authority', NULL),
    ('AIR_AAR',   'AIRLINE',  '대한항공',               'Korean Air',                          '110-81-14794'),
    ('AIR_KAL',   'AIRLINE',  '아시아나항공',           'Asiana Airlines',                     '110-81-19012'),
    ('AIR_JNA',   'AIRLINE',  '제주항공',               'Jeju Air',                            '616-81-86079'),
    ('AIR_ESR',   'AIRLINE',  '이스타항공',             'Eastar Jet',                          '109-86-12345'),
    ('AIR_TWB',   'AIRLINE',  '티웨이항공',             'TWAY Air',                            '210-87-65432'),
    ('AIR_ABL',   'AIRLINE',  '에어부산',               'Air Busan',                           '600-86-87654'),
    ('VRF_001',   'VERIFIER', '한국인정원',             'Korea Accreditation System',          '215-82-09876'),
    ('VRF_002',   'VERIFIER', '한국선급',               'Korean Register',                     '601-82-12345')
ON CONFLICT (ognz_id) DO UPDATE SET ognz_nm = EXCLUDED.ognz_nm, last_chg_dt = NOW();

-- 항공기 운영사
INSERT INTO com.tn_oprtr (oprtr_id, ognz_id, icao_desig, oprtr_nm, oprtr_nm_en, aoc_no, aoc_isue_dt, aoc_xpr_dt) VALUES
    ('OP0001', 'AIR_AAR', 'KAL', '대한항공',     'Korean Air',     'KOR-001', '2020-01-01', '2030-12-31'),
    ('OP0002', 'AIR_KAL', 'AAR', '아시아나항공', 'Asiana Airlines','KOR-002', '2020-01-01', '2030-12-31'),
    ('OP0003', 'AIR_JNA', 'JJA', '제주항공',     'Jeju Air',       'KOR-003', '2020-01-01', '2030-12-31'),
    ('OP0004', 'AIR_ESR', 'ESR', '이스타항공',   'Eastar Jet',     'KOR-004', '2021-01-01', '2030-12-31'),
    ('OP0005', 'AIR_TWB', 'TWB', '티웨이항공',   'TWAY Air',       'KOR-005', '2020-01-01', '2030-12-31'),
    ('OP0006', 'AIR_ABL', 'ABL', '에어부산',     'Air Busan',      'KOR-006', '2020-01-01', '2030-12-31')
ON CONFLICT (oprtr_id) DO UPDATE SET oprtr_nm = EXCLUDED.oprtr_nm, last_chg_dt = NOW();

-- 검증기관
INSERT INTO com.tn_vrfcn_inst (vrfcn_inst_id, ognz_id, vrfcn_inst_nm, vrfcn_inst_nm_en, icao_ccr_accrd_yn, icao_ccr_accrd_no, icao_ccr_accrd_xpr_dt) VALUES
    ('VI0001', 'VRF_001', '한국인정원', 'Korea Accreditation System', 'Y', 'CCR-KR-001', '2028-12-31'),
    ('VI0002', 'VRF_002', '한국선급',   'Korean Register',            'Y', 'CCR-KR-002', '2028-12-31')
ON CONFLICT (vrfcn_inst_id) DO UPDATE SET vrfcn_inst_nm = EXCLUDED.vrfcn_inst_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 7. 역할
-- ----------------------------------------------------------------
INSERT INTO com.tn_role (role_id, role_nm, ognz_se_cd_allowed, role_desc) VALUES
    ('MOLIT_ADMIN',     '국토부 관리자',     'MOLIT',    '전사 관리·최종승인·운영사 지정·CCR 추출'),
    ('MOLIT_USER',      '국토부 사용자',     'MOLIT',    '전사 조회·통계·시뮬레이션·현장실사'),
    ('KOTSA_REVIEWER',  '공단 검토자',       'KOTSA',    '사전검토·OoM-check·KOTSA 추천'),
    ('KOTSA_USER',      '공단 사용자',       'KOTSA',    '조회·통계'),
    ('AIRLINE_MANAGER', '항공사 관리자',     'AIRLINE',  '자사 EMP·ER·CEF·EUCR·SAF 작성·제출·승인 신청'),
    ('AIRLINE_OPERATOR','항공사 담당자',     'AIRLINE',  '자사 EMP·ER·SAF 입력 (제출 권한 없음)'),
    ('VERIFIER_LEAD',   '검증팀장',          'VERIFIER', 'VR 작성·제출·독립검토자 의견'),
    ('VERIFIER_MEMBER', '검증팀원',          'VERIFIER', 'VR 작성 보조')
ON CONFLICT (role_id) DO UPDATE SET role_nm = EXCLUDED.role_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 8. 시스템 권한 (간단화 — 메인 권한 그룹만)
-- ----------------------------------------------------------------
INSERT INTO com.tn_sys_authrt (authrt_id, authrt_nm, authrt_desc) VALUES
    ('AUTH_MOLIT_ADMIN',  '국토부 관리자 권한',     '전사 관리'),
    ('AUTH_MOLIT_USER',   '국토부 사용자 권한',     '전사 조회'),
    ('AUTH_KOTSA_RVWR',   '공단 검토자 권한',       '사전검토·OoM'),
    ('AUTH_KOTSA_USER',   '공단 사용자 권한',       '조회'),
    ('AUTH_AIRLINE_MGR',  '항공사 관리자 권한',     '자사 EMP/ER/CEF/EUCR/SAF 작성·제출'),
    ('AUTH_AIRLINE_OPR',  '항공사 담당자 권한',     '자사 EMP/ER/SAF 입력 (제출 권한 없음)'),
    ('AUTH_VERIFIER_LEAD','검증팀장 권한',          'VR 작성·제출'),
    ('AUTH_VERIFIER_MBR', '검증팀원 권한',          'VR 작성 보조')
ON CONFLICT (authrt_id) DO UPDATE SET authrt_nm = EXCLUDED.authrt_nm, last_chg_dt = NOW();

-- 권한 - 역할 매핑
INSERT INTO com.tn_sys_authrt_role_mpng (authrt_id, role_id) VALUES
    ('AUTH_MOLIT_ADMIN',   'MOLIT_ADMIN'),
    ('AUTH_MOLIT_USER',    'MOLIT_USER'),
    ('AUTH_KOTSA_RVWR',    'KOTSA_REVIEWER'),
    ('AUTH_KOTSA_USER',    'KOTSA_USER'),
    ('AUTH_AIRLINE_MGR',   'AIRLINE_MANAGER'),
    ('AUTH_AIRLINE_OPR',   'AIRLINE_OPERATOR'),
    ('AUTH_VERIFIER_LEAD', 'VERIFIER_LEAD'),
    ('AUTH_VERIFIER_MBR',  'VERIFIER_MEMBER')
ON CONFLICT (authrt_id, role_id) DO NOTHING;

-- ----------------------------------------------------------------
-- 9. 프로그램 (주요 화면)
-- ----------------------------------------------------------------
INSERT INTO com.tn_prgrm (prgrm_id, sys_se_cd, prgrm_nm, prgrm_url, api_path_prefix) VALUES
    ('COM_0001', 'COM', '사용자 관리',         '/com/user',           '/api/com/user'),
    ('COM_0002', 'COM', '기관 관리',           '/com/ognz',           '/api/com/ognz'),
    ('COM_0003', 'COM', '운영사 관리',         '/com/oprtr',          '/api/com/oprtr'),
    ('COM_0004', 'COM', '검증기관 관리',       '/com/vrfcn-inst',     '/api/com/vrfcn-inst'),
    ('COM_0005', 'COM', '검증 배정',           '/com/vrfcn-assgn',    '/api/com/vrfcn-assgn'),
    ('COM_0006', 'COM', '역할 관리',           '/com/role',           '/api/com/role'),
    ('COM_0007', 'COM', '권한 관리',           '/com/authrt',         '/api/com/authrt'),
    ('COM_0008', 'COM', '메뉴 관리',           '/com/menu',           '/api/com/menu'),
    ('COM_0009', 'COM', '공통코드 관리',       '/com/cd',             '/api/com/cd'),
    ('COM_0010', 'COM', '파일 관리',           '/com/file',           '/api/com/file'),
    ('COM_0011', 'COM', '규정 게시판',         '/com/rglt',           '/api/com/rglt'),
    ('COM_0012', 'COM', '공지사항',            '/com/ntc',            '/api/com/ntc'),
    ('COM_0013', 'COM', '결재함',              '/com/atrz',           '/api/com/atrz'),
    ('EMP_0001', 'EMP', 'EMP 목록',            '/emp/plan',           '/api/emp/plan'),
    ('EMP_0002', 'EMP', 'EMP 작성',            '/emp/plan/new',       '/api/emp/plan'),
    ('EMP_0003', 'EMP', 'EMP 검토',            '/emp/plan/review',    '/api/emp/plan'),
    ('ER_0001',  'ER',  'ER 목록',             '/er/rprt',            '/api/er/rprt'),
    ('ER_0002',  'ER',  'ER 작성',             '/er/rprt/new',        '/api/er/rprt'),
    ('ER_0003',  'ER',  'CEF 청구',            '/er/cef',             '/api/er/cef'),
    ('ER_0004',  'ER',  'EUCR 작성',           '/er/eucr',            '/api/er/eucr'),
    ('ER_0005',  'ER',  'OoM-check',           '/er/oom',             '/api/er/oom'),
    ('VR_0001',  'VR',  'VR 목록',             '/vr/rprt',            '/api/vr/rprt'),
    ('VR_0002',  'VR',  'VR 작성',             '/vr/rprt/new',        '/api/vr/rprt'),
    ('SAF_0001', 'SAF', 'SAF 인증서',          '/saf/cert',           '/api/saf/cert'),
    ('SAF_0002', 'SAF', 'SAF 배치',            '/saf/batch',          '/api/saf/batch'),
    ('SAF_0003', 'SAF', '공항별 급유',         '/saf/airprt/fuel',    '/api/saf/airprt-fuel'),
    ('SAF_0004', 'SAF', '공항별 SAF 구매',     '/saf/airprt/purch',   '/api/saf/airprt-purch'),
    ('SAF_0005', 'SAF', 'SAF 모니터링',        '/saf/mntr',           '/api/saf/mntr'),
    ('SAF_0006', 'SAF', '이행률 대시보드',     '/saf/dashboard',      '/api/saf/dashboard'),
    ('SAF_0007', 'SAF', '현장실사',            '/saf/fld-inspn',      '/api/saf/fld-inspn'),
    ('PTL_0001', 'PTL', '통합 워크플로우',     '/ptl/workflow',       '/api/ptl/workflow'),
    ('PTL_0002', 'PTL', 'CCR 추출',            '/ptl/ccr',            '/api/ptl/ccr'),
    ('PTL_0003', 'PTL', '시뮬레이션',          '/ptl/sim',            '/api/ptl/sim'),
    ('PTL_0004', 'PTL', '통계 대시보드',       '/ptl/stat',           '/api/ptl/stat')
ON CONFLICT (prgrm_id) DO UPDATE SET prgrm_nm = EXCLUDED.prgrm_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 10. 메뉴 트리
-- ----------------------------------------------------------------
INSERT INTO com.tn_sys_menu (menu_id, sys_se_cd, menu_nm, upper_menu_id, menu_ord, prgrm_id, icon_nm) VALUES
    ('COM_0001', 'COM', '공통관리',         NULL,        10, NULL,       'cog'),
    ('COM_0002', 'COM', '사용자 관리',      'COM_0001',  10, 'COM_0001', 'users'),
    ('COM_0003', 'COM', '기관/운영사 관리', 'COM_0001',  20, 'COM_0003', 'building'),
    ('COM_0004', 'COM', '권한 관리',        'COM_0001',  30, 'COM_0007', 'shield'),
    ('COM_0005', 'COM', '공통코드',         'COM_0001',  40, 'COM_0009', 'tags'),
    ('COM_0006', 'COM', '규정 게시판',      'COM_0001',  50, 'COM_0011', 'book'),
    ('COM_0007', 'COM', '공지사항',         'COM_0001',  60, 'COM_0012', 'bell'),
    ('COM_0008', 'COM', '결재함',           'COM_0001',  70, 'COM_0013', 'inbox'),
    ('EMP_0001', 'EMP', 'EMP 관리',         NULL,        10, NULL,       'file-text'),
    ('EMP_0002', 'EMP', 'EMP 목록',         'EMP_0001',  10, 'EMP_0001', NULL),
    ('EMP_0003', 'EMP', 'EMP 작성',         'EMP_0001',  20, 'EMP_0002', NULL),
    ('EMP_0004', 'EMP', 'EMP 검토',         'EMP_0001',  30, 'EMP_0003', NULL),
    ('ER_0001',  'ER',  'ER 관리',          NULL,        10, NULL,       'bar-chart'),
    ('ER_0002',  'ER',  'ER 목록',          'ER_0001',   10, 'ER_0001',  NULL),
    ('ER_0003',  'ER',  'CEF 청구',         'ER_0001',   20, 'ER_0003',  NULL),
    ('ER_0004',  'ER',  'EUCR 작성',        'ER_0001',   30, 'ER_0004',  NULL),
    ('ER_0005',  'ER',  'OoM-check',        'ER_0001',   40, 'ER_0005',  NULL),
    ('VR_0001',  'VR',  '검증보고서',       NULL,        10, NULL,       'check-circle'),
    ('VR_0002',  'VR',  'VR 목록',          'VR_0001',   10, 'VR_0001',  NULL),
    ('VR_0003',  'VR',  'VR 작성',          'VR_0001',   20, 'VR_0002',  NULL),
    ('SAF_0001', 'SAF', 'SAF 관리',         NULL,        10, NULL,       'droplet'),
    ('SAF_0002', 'SAF', '인증서 관리',      'SAF_0001',  10, 'SAF_0001', NULL),
    ('SAF_0003', 'SAF', '배치 관리',        'SAF_0001',  20, 'SAF_0002', NULL),
    ('SAF_0004', 'SAF', '공항별 급유',      'SAF_0001',  30, 'SAF_0003', NULL),
    ('SAF_0005', 'SAF', '공항별 SAF 구매',  'SAF_0001',  40, 'SAF_0004', NULL),
    ('SAF_0006', 'SAF', '모니터링',         'SAF_0001',  50, 'SAF_0005', NULL),
    ('SAF_0007', 'SAF', '이행률 대시보드',  'SAF_0001',  60, 'SAF_0006', NULL),
    ('SAF_0008', 'SAF', '현장실사',         'SAF_0001',  70, 'SAF_0007', NULL),
    ('PTL_0001', 'PTL', '포털/통계',        NULL,        10, NULL,       'pie-chart'),
    ('PTL_0002', 'PTL', '통합 워크플로우',  'PTL_0001',  10, 'PTL_0001', NULL),
    ('PTL_0003', 'PTL', 'CCR 추출',         'PTL_0001',  20, 'PTL_0002', NULL),
    ('PTL_0004', 'PTL', '시뮬레이션',       'PTL_0001',  30, 'PTL_0003', NULL),
    ('PTL_0005', 'PTL', '통계 대시보드',    'PTL_0001',  40, 'PTL_0004', NULL)
ON CONFLICT (menu_id) DO UPDATE SET menu_nm = EXCLUDED.menu_nm, last_chg_dt = NOW();

-- ----------------------------------------------------------------
-- 11. 권한-프로그램 매핑 (역할별 가시·입력 권한)
-- ----------------------------------------------------------------
-- MOLIT_ADMIN : 모든 프로그램 조회·입력
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn)
SELECT 'AUTH_MOLIT_ADMIN', prgrm_id, 'Y', 'Y' FROM com.tn_prgrm
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn='Y', inpt_authrt_yn='Y', last_chg_dt=NOW();

-- MOLIT_USER : 모든 프로그램 조회, 시뮬레이션·현장실사·통계만 입력
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn)
SELECT 'AUTH_MOLIT_USER', prgrm_id, 'Y',
       CASE WHEN prgrm_id IN ('PTL_0003','SAF_0007') THEN 'Y' ELSE 'N' END
  FROM com.tn_prgrm
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn='Y', last_chg_dt=NOW();

-- KOTSA_REVIEWER : 검토 관련 입력
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn)
SELECT 'AUTH_KOTSA_RVWR', prgrm_id, 'Y',
       CASE WHEN prgrm_id IN ('EMP_0003','ER_0005','PTL_0001','PTL_0003') THEN 'Y' ELSE 'N' END
  FROM com.tn_prgrm
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn='Y', last_chg_dt=NOW();

-- KOTSA_USER : 전체 조회만
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn)
SELECT 'AUTH_KOTSA_USER', prgrm_id, 'Y', 'N' FROM com.tn_prgrm
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn='Y', last_chg_dt=NOW();

-- AIRLINE_MANAGER : EMP/ER/CEF/EUCR/SAF/PTL 자기 데이터 조회·입력
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn) VALUES
    ('AUTH_AIRLINE_MGR', 'COM_0009', 'Y', 'N'),  -- 공통코드 조회
    ('AUTH_AIRLINE_MGR', 'COM_0011', 'Y', 'N'),  -- 규정
    ('AUTH_AIRLINE_MGR', 'COM_0012', 'Y', 'N'),  -- 공지사항
    ('AUTH_AIRLINE_MGR', 'COM_0013', 'Y', 'Y'),  -- 결재함
    ('AUTH_AIRLINE_MGR', 'EMP_0001', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'EMP_0002', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'ER_0001',  'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'ER_0002',  'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'ER_0003',  'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'ER_0004',  'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'SAF_0001', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'SAF_0002', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'SAF_0003', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'SAF_0004', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'SAF_0005', 'Y', 'N'),
    ('AUTH_AIRLINE_MGR', 'SAF_0006', 'Y', 'N'),
    ('AUTH_AIRLINE_MGR', 'PTL_0001', 'Y', 'N'),
    ('AUTH_AIRLINE_MGR', 'PTL_0003', 'Y', 'Y'),
    ('AUTH_AIRLINE_MGR', 'PTL_0004', 'Y', 'N')
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn=EXCLUDED.inq_authrt_yn, inpt_authrt_yn=EXCLUDED.inpt_authrt_yn, last_chg_dt=NOW();

-- AIRLINE_OPERATOR : MANAGER 와 동일 조회 + 입력 일부 제한 (제출 권한 없음 — 별도 처리)
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn) VALUES
    ('AUTH_AIRLINE_OPR', 'COM_0009', 'Y', 'N'),
    ('AUTH_AIRLINE_OPR', 'COM_0011', 'Y', 'N'),
    ('AUTH_AIRLINE_OPR', 'COM_0012', 'Y', 'N'),
    ('AUTH_AIRLINE_OPR', 'EMP_0001', 'Y', 'Y'),
    ('AUTH_AIRLINE_OPR', 'EMP_0002', 'Y', 'Y'),
    ('AUTH_AIRLINE_OPR', 'ER_0001',  'Y', 'Y'),
    ('AUTH_AIRLINE_OPR', 'ER_0002',  'Y', 'Y'),
    ('AUTH_AIRLINE_OPR', 'SAF_0001', 'Y', 'Y'),
    ('AUTH_AIRLINE_OPR', 'SAF_0003', 'Y', 'Y'),
    ('AUTH_AIRLINE_OPR', 'SAF_0004', 'Y', 'Y')
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn=EXCLUDED.inq_authrt_yn, inpt_authrt_yn=EXCLUDED.inpt_authrt_yn, last_chg_dt=NOW();

-- VERIFIER_LEAD
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn) VALUES
    ('AUTH_VERIFIER_LEAD', 'COM_0009', 'Y', 'N'),
    ('AUTH_VERIFIER_LEAD', 'COM_0011', 'Y', 'N'),
    ('AUTH_VERIFIER_LEAD', 'COM_0012', 'Y', 'N'),
    ('AUTH_VERIFIER_LEAD', 'ER_0001',  'Y', 'N'),
    ('AUTH_VERIFIER_LEAD', 'VR_0001',  'Y', 'Y'),
    ('AUTH_VERIFIER_LEAD', 'VR_0002',  'Y', 'Y')
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn=EXCLUDED.inq_authrt_yn, inpt_authrt_yn=EXCLUDED.inpt_authrt_yn, last_chg_dt=NOW();

-- VERIFIER_MEMBER
INSERT INTO com.tn_sys_authrt_prgrm_mpng (authrt_id, prgrm_id, inq_authrt_yn, inpt_authrt_yn) VALUES
    ('AUTH_VERIFIER_MBR', 'COM_0009', 'Y', 'N'),
    ('AUTH_VERIFIER_MBR', 'COM_0011', 'Y', 'N'),
    ('AUTH_VERIFIER_MBR', 'COM_0012', 'Y', 'N'),
    ('AUTH_VERIFIER_MBR', 'ER_0001',  'Y', 'N'),
    ('AUTH_VERIFIER_MBR', 'VR_0001',  'Y', 'N'),
    ('AUTH_VERIFIER_MBR', 'VR_0002',  'Y', 'Y')
ON CONFLICT (authrt_id, prgrm_id) DO UPDATE SET inq_authrt_yn=EXCLUDED.inq_authrt_yn, inpt_authrt_yn=EXCLUDED.inpt_authrt_yn, last_chg_dt=NOW();

-- ----------------------------------------------------------------
-- 12. 샘플 사용자 (운영자 + 항공사 + 검증기관)
-- 비밀번호: gn12345! (SHA-256('icas' + plain + 'cems')) — 운영 환경은 초기 로그인 후 변경 강제
-- ----------------------------------------------------------------
-- 평문 'gn12345!' → 'icas' + 'gn12345!' + 'cems' SHA-256
-- 미리 계산하여 직접 하드코딩
INSERT INTO com.tn_user (user_id, user_nm, pswd_hash, ognz_id, eml_addr, master_yn) VALUES
    ('admin',         '시스템 관리자', encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'MOLIT001', 'admin@molit.go.kr', 'Y'),
    ('molit_admin',   '국토부 담당자', encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'MOLIT001', 'molit@molit.go.kr', 'N'),
    ('kotsa_rvwr',    '공단 검토자',   encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'KOTSA001', 'rvwr@kotsa.or.kr',  'N'),
    ('aar_mgr',       '대한항공 관리자', encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'AIR_AAR',  'mgr@koreanair.com', 'N'),
    ('aar_opr',       '대한항공 담당자', encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'AIR_AAR',  'opr@koreanair.com', 'N'),
    ('kal_mgr',       '아시아나 관리자', encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'AIR_KAL',  'mgr@flyasiana.com', 'N'),
    ('jna_mgr',       '제주항공 관리자', encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'AIR_JNA',  'mgr@jejuair.net',   'N'),
    ('vrf_lead',      '검증팀장',      encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'VRF_001',  'lead@kas.kr',       'N'),
    ('vrf_member',    '검증팀원',      encode(digest('icas' || 'gn12345!' || 'cems', 'sha256'), 'hex'),
                                       'VRF_001',  'member@kas.kr',     'N')
ON CONFLICT (user_id) DO UPDATE SET user_nm = EXCLUDED.user_nm, last_chg_dt = NOW();

-- 사용자-역할 매핑
INSERT INTO com.tn_user_role_mpng (user_id, role_id) VALUES
    ('admin',       'MOLIT_ADMIN'),
    ('molit_admin', 'MOLIT_ADMIN'),
    ('kotsa_rvwr',  'KOTSA_REVIEWER'),
    ('aar_mgr',     'AIRLINE_MANAGER'),
    ('aar_opr',     'AIRLINE_OPERATOR'),
    ('kal_mgr',     'AIRLINE_MANAGER'),
    ('jna_mgr',     'AIRLINE_MANAGER'),
    ('vrf_lead',    'VERIFIER_LEAD'),
    ('vrf_member',  'VERIFIER_MEMBER')
ON CONFLICT (user_id, role_id, use_bgng_dt) DO NOTHING;

-- ----------------------------------------------------------------
-- 13. 샘플 검증 배정 (2026 보고연도)
-- ----------------------------------------------------------------
INSERT INTO com.tn_vrfcn_assgn (vrfcn_inst_id, oprtr_id, rprt_yr, assgn_dt) VALUES
    ('VI0001', 'OP0001', '2026', '2026-01-15'),
    ('VI0001', 'OP0003', '2026', '2026-01-15'),
    ('VI0002', 'OP0002', '2026', '2026-01-15'),
    ('VI0002', 'OP0004', '2026', '2026-01-15'),
    ('VI0001', 'OP0005', '2026', '2026-01-15'),
    ('VI0002', 'OP0006', '2026', '2026-01-15')
ON CONFLICT (vrfcn_inst_id, oprtr_id, rprt_yr) DO NOTHING;

-- ----------------------------------------------------------------
-- 끝
-- ----------------------------------------------------------------
