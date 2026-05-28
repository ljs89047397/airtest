-- ================================================================
-- icas-cems — 2차 시드 데이터 (화면 시연용)
-- 각 도메인 자식 테이블 3~10건씩 의미있는 데이터
-- 모두 ON CONFLICT DO NOTHING 으로 멱등 보장
-- ================================================================

-- ----------------------------------------------------------------
-- 1. EMP 자식 테이블
-- ----------------------------------------------------------------

-- 1-1. 항공기 등록부 (운영사별 보유 기종)
INSERT INTO emp.tn_emp_acft (emp_plan_id, acft_sn, acft_type_cd, fuel_type_cd, acft_cnt, rmrk) VALUES
    ('EMP2026KAL', 1, 'B77W', 'JET_A1', 25, '국제선 장거리 주력 기종'),
    ('EMP2026KAL', 2, 'B789', 'JET_A1', 12, '중장거리 노선'),
    ('EMP2026KAL', 3, 'A359', 'JET_A1',  8, '신규 도입 친환경 기종'),
    ('EMP2026KAL', 4, 'A388', 'JET_A1',  6, '대형 여객기'),
    ('EMP2026KAL', 5, 'B738', 'JET_A1', 18, '국내선 및 단거리'),
    ('EMP2026AAR', 1, 'A333', 'JET_A1', 14, '국제선 중거리'),
    ('EMP2026AAR', 2, 'A359', 'JET_A1',  6, '장거리 친환경 기종'),
    ('EMP2026AAR', 3, 'B77W', 'JET_A1',  9, '북미·유럽 노선'),
    ('EMP2026AAR', 4, 'A321', 'JET_A1', 22, '단거리 국제선·국내선'),
    ('EMP2026JJA', 1, 'B738', 'JET_A1', 45, 'LCC 단거리 주력'),
    ('EMP2026JJA', 2, 'B739', 'JET_A1',  8, '국제선 단거리'),
    ('EP0001',     1, 'B77W', 'JET_A1', 25, 'EMP 초안 — 장거리 기종')
ON CONFLICT (emp_plan_id, acft_sn) DO NOTHING;

-- 1-2. 운영사 연락처
INSERT INTO emp.tn_emp_oprtr_cnct (emp_plan_id, cnct_sn, cnct_se_cd, user_nm, mblphn_no, eml_addr) VALUES
    ('EMP2026KAL', 1, 'PRIMARY', '김대한', '010-1234-5678', 'kim.daehan@koreanair.com'),
    ('EMP2026KAL', 2, 'SUB',     '이수연', '010-2345-6789', 'lee.sooyeon@koreanair.com'),
    ('EMP2026KAL', 3, 'SUB',     '박지훈', '010-3456-7890', 'park.jihoon@koreanair.com'),
    ('EMP2026AAR', 1, 'PRIMARY', '정아시', '010-1111-2222', 'jung.asi@flyasiana.com'),
    ('EMP2026AAR', 2, 'SUB',     '한민지', '010-2222-3333', 'han.minji@flyasiana.com'),
    ('EMP2026JJA', 1, 'PRIMARY', '최제주', '010-5555-6666', 'choi.jeju@jejuair.net'),
    ('EMP2026JJA', 2, 'SUB',     '윤하늘', '010-6666-7777', 'yoon.haneul@jejuair.net'),
    ('EP0001',     1, 'PRIMARY', '김대한', '010-1234-5678', 'kim.daehan@koreanair.com')
ON CONFLICT (emp_plan_id, cnct_sn) DO NOTHING;

-- 1-3. 운영사 식별정보
INSERT INTO emp.tn_emp_oprtr_info (emp_plan_id, oprtr_nm, oprtr_nm_en, addr, lglrpr_nm, icao_desig, regis_mark_list, aoc_no, aoc_isue_dt, aoc_xpr_dt, aoc_athrty_nm, parent_co_nm, sbsdry_info) VALUES
    ('EMP2026KAL', '대한항공',     'Korean Air Lines Co., Ltd.', '서울특별시 강서구 하늘길 260', '조원태', 'KAL', 'HL7611, HL7612, HL8085, HL8086', 'KOR-AOC-001', '2010-03-15', '2030-03-14', '국토교통부', '한진그룹', '진에어, 한진칼'),
    ('EMP2026AAR', '아시아나항공', 'Asiana Airlines Inc.',       '서울특별시 강서구 오정로 443-83', '원유석', 'AAR', 'HL7755, HL7756, HL7771', 'KOR-AOC-002', '2008-06-01', '2028-05-31', '국토교통부', '대한항공', '에어부산, 에어서울'),
    ('EMP2026JJA', '제주항공',     'Jeju Air Co., Ltd.',         '제주특별자치도 제주시 공항로 2', '김이배', 'JJA', 'HL8260, HL8261, HL8262', 'KOR-AOC-003', '2006-06-01', '2026-12-31', '국토교통부', 'AK홀딩스', NULL),
    ('EP0001',     '대한항공',     'Korean Air Lines Co., Ltd.', '서울특별시 강서구 하늘길 260', '조원태', 'KAL', 'HL7611, HL7612', 'KOR-AOC-001', '2010-03-15', '2030-03-14', '국토교통부', '한진그룹', '진에어, 한진칼')
ON CONFLICT (emp_plan_id) DO NOTHING;

-- 1-4. 국가쌍
INSERT INTO emp.tn_emp_cntry_pair (emp_plan_id, pair_sn, dprtr_cntry_cd, arvl_cntry_cd, intl_yn, exempt_cd) VALUES
    ('EMP2026KAL', 1, 'KR', 'US', 'Y', NULL),
    ('EMP2026KAL', 2, 'KR', 'JP', 'Y', NULL),
    ('EMP2026KAL', 3, 'KR', 'CN', 'Y', NULL),
    ('EMP2026KAL', 4, 'KR', 'GB', 'Y', NULL),
    ('EMP2026KAL', 5, 'KR', 'DE', 'Y', NULL),
    ('EMP2026KAL', 6, 'KR', 'KR', 'N', NULL),
    ('EMP2026AAR', 1, 'KR', 'US', 'Y', NULL),
    ('EMP2026AAR', 2, 'KR', 'CN', 'Y', NULL),
    ('EMP2026AAR', 3, 'KR', 'VN', 'Y', NULL),
    ('EMP2026JJA', 1, 'KR', 'JP', 'Y', NULL),
    ('EMP2026JJA', 2, 'KR', 'TH', 'Y', NULL),
    ('EMP2026JJA', 3, 'KR', 'PH', 'Y', NULL),
    ('EP0001',     1, 'KR', 'US', 'Y', NULL),
    ('EP0001',     2, 'KR', 'JP', 'Y', NULL)
ON CONFLICT (emp_plan_id, pair_sn) DO NOTHING;

-- 1-5. CO2 계산방법
INSERT INTO emp.tn_emp_co2_calc (emp_plan_id, mntr_mthd_cd, cert_use_yn, cert_regis_mthd_cd, fuel_dnsty_se_cd, est_co2_emsn, est_co2_basis) VALUES
    ('EMP2026KAL', 'MTHD_B',       'Y', 'BLOCK_TIME', 'STD_DNSTY', 6850000.0000, '전년도 실적(2024) 기반 추정, 신규 노선 +3% 반영'),
    ('EMP2026AAR', 'MTHD_A',       'N', NULL,         'STD_DNSTY', 4250000.0000, '연료 사용량 측정 방식, 신뢰도 95%'),
    ('EMP2026JJA', 'BLOCK_ON_OFF', 'N', NULL,         'ACT_DNSTY', 1850000.0000, 'Block On/Off 방식, 항공기 단거리 운항 특성 반영'),
    ('EP0001',     'MTHD_B',       'Y', 'BLOCK_TIME', 'STD_DNSTY', 6850000.0000, 'EMP 초안용 추정치')
ON CONFLICT (emp_plan_id) DO NOTHING;

-- 1-6. CO2 상세
INSERT INTO emp.tn_emp_co2_detail (emp_plan_id, mntr_mthd_cd, msr_tming_desc, msr_device_desc, msr_proc_desc, fuel_dnsty_desc) VALUES
    ('EMP2026KAL', 'MTHD_B',       '급유시점 및 출발/도착 시점', 'FQIS(Fuel Quantity Indicating System) — Honeywell HG-2050', '항공기 탑재 FQIS 자동 측정 후 ACARS 전송, 일일 데이터 검증', '표준 밀도 0.800 kg/L 적용, 항공유 ASTM D1655 규격'),
    ('EMP2026AAR', 'MTHD_A',       '급유 직후 게이트',           'Truck Flow Meter — Liquid Controls M-30',                    '급유 차량 유량계로 측정, 일일 합산 보고',          '연료 공급사 BP/Shell 인증 밀도'),
    ('EMP2026JJA', 'BLOCK_ON_OFF', 'Block-On Block-Off 시점',    'ACARS OOOI 자동 기록',                                       'OOOI 시점 기반 연료 잔량 차이 계산',                'ACT_DNSTY — 매 항공편 실제 밀도 측정'),
    ('EP0001',     'MTHD_B',       '급유시점 및 출발/도착 시점', 'FQIS — Honeywell HG-2050',                                    '항공기 탑재 시스템 자동 측정',                       '표준 밀도 0.800 kg/L')
ON CONFLICT (emp_plan_id, mntr_mthd_cd) DO NOTHING;

-- 1-7. 데이터 품질 통제
INSERT INTO emp.tn_emp_data_ctrl (emp_plan_id, flow_desc, gap_thrshld_5pct, snd_src_use_desc, risk_anlys, sig_chg_aprv_proc) VALUES
    ('EMP2026KAL', '운항관리시스템(FOC) → CMS(연료관리) → CEMS 자동 연계', 'Y', '운항일지·급유전표 2차 출처 교차 검증', '연료 미측정 항공편, 항법장비 오류, 시스템 장애 위험 식별', '월별 부서장 검토 → 분기 임원 승인'),
    ('EMP2026AAR', 'CrewSchedule → FuelTracker → CEMS 일일 ETL',          'Y', '관제기록 + 급유사 청구서 2차 검증',     '시스템 다운타임 위험, ACARS 누락 위험',         '월별 데이터관리팀 검증, 분기 안전위원회 보고'),
    ('EMP2026JJA', 'OOOI 자동수집 → 운항통계 → CEMS API 전송',             'Y', '관제 ATC 로그 백업 사용',                'OOOI 송신 실패 위험, 단거리 단편 데이터 위험',  '주간 운항팀장 검토'),
    ('EP0001',     'FOC → CMS → CEMS',                                    'Y', '운항일지 교차 검증',                      '시스템 장애 위험',                            '월별 부서장 검토')
ON CONFLICT (emp_plan_id) DO NOTHING;

-- 1-8. 위험평가
INSERT INTO emp.tn_emp_risk (emp_plan_id, risk_sn, risk_desc, ctrl_actv) VALUES
    ('EMP2026KAL', 1, '연료측정장치(FQIS) 오작동으로 데이터 누락 가능', '월 1회 캘리브레이션, 백업 FOC 데이터로 보정'),
    ('EMP2026KAL', 2, 'ACARS 송신 실패로 일일 데이터 미수신',           '24시간 내 수동 입력 절차 운영'),
    ('EMP2026KAL', 3, '운항계획 변경에 따른 노선 재분류 누락',          '월별 노선 매핑 검토회의'),
    ('EMP2026KAL', 4, '데이터 입력 오류',                                '2인 교차 검증 및 시스템 검증규칙'),
    ('EMP2026AAR', 1, '급유 트럭 유량계 정밀도 부족',                    '연 2회 외부 캘리브레이션 인증'),
    ('EMP2026AAR', 2, '연료 공급사 청구서 지연으로 월말 마감 지연',       '연료공급사 SLA 체결, 자동 EDI 연계'),
    ('EMP2026JJA', 1, '단거리 LCC 특성상 OOOI 데이터 누락 빈번',          '운항 후 24시간 내 정정 보고 의무화'),
    ('EMP2026JJA', 2, '신규 도입 항공기 매개변수 입력 지연',              '도입 60일 전 매개변수 등록 의무화'),
    ('EP0001',     1, '시스템 장애',                                       '월별 백업 운영')
ON CONFLICT (emp_plan_id, risk_sn) DO NOTHING;

-- ----------------------------------------------------------------
-- 2. ER 자식 테이블
-- ----------------------------------------------------------------

-- 2-1. 항공기·연료 등록부
INSERT INTO er.tn_er_acft_fuel (er_id, acft_sn, acft_type_cd, regis_mark, ownr_ls_se_cd, fuel_type_cd, dnsty_se_cd) VALUES
    ('ER2025KAL', 1, 'B77W', 'HL7611', 'OWN',   'JET_A1', 'STD'),
    ('ER2025KAL', 2, 'B789', 'HL8085', 'OWN',   'JET_A1', 'STD'),
    ('ER2025KAL', 3, 'A359', 'HL8366', 'LEASE', 'JET_A1', 'STD'),
    ('ER2025KAL', 4, 'A388', 'HL7611', 'OWN',   'JET_A1', 'STD'),
    ('ER2026KAL', 1, 'B77W', 'HL7612', 'OWN',   'JET_A1', 'STD'),
    ('ER2026KAL', 2, 'B789', 'HL8086', 'OWN',   'JET_A1', 'STD'),
    ('ER2026KAL', 3, 'A359', 'HL8367', 'LEASE', 'JET_A1', 'STD'),
    ('ER2026AAR', 1, 'A333', 'HL7755', 'OWN',   'JET_A1', 'STD'),
    ('ER2026AAR', 2, 'A359', 'HL8081', 'LEASE', 'JET_A1', 'STD'),
    ('ER2026AAR', 3, 'B77W', 'HL7771', 'OWN',   'JET_A1', 'STD'),
    ('ER2026JJA', 1, 'B738', 'HL8260', 'LEASE', 'JET_A1', 'ACT'),
    ('ER2026JJA', 2, 'B738', 'HL8261', 'LEASE', 'JET_A1', 'ACT'),
    ('ER2026JJA', 3, 'B739', 'HL8262', 'OWN',   'JET_A1', 'ACT'),
    ('ER0001',    1, 'B738', 'HL8270', 'LEASE', 'JET_A1', 'ACT'),
    ('ER0002',    1, 'A333', 'HL7780', 'OWN',   'JET_A1', 'STD'),
    ('ER0003',    1, 'B77W', 'HL7613', 'OWN',   'JET_A1', 'STD')
ON CONFLICT (er_id, acft_sn) DO NOTHING;

-- 2-2. 비행장쌍 CO2
INSERT INTO er.tn_er_aerdrm_pair_co2 (er_id, pair_sn, dprtr_aerdrm_cd, arvl_aerdrm_cd, dprtr_cntry_cd, arvl_cntry_cd, flt_cnt, fuel_type_cd, fuel_wght, co2_emsn) VALUES
    ('ER2025KAL', 1, 'RKSI', 'KLAX', 'KR', 'US', 1825, 'JET_A1', 154200.5000, 487272.0000),
    ('ER2025KAL', 2, 'RKSI', 'KJFK', 'KR', 'US', 1460, 'JET_A1', 138900.0000, 438924.0000),
    ('ER2025KAL', 3, 'RKSI', 'RJTT', 'KR', 'JP',  912, 'JET_A1',  18250.0000,  57670.0000),
    ('ER2025KAL', 4, 'RKSI', 'ZBAA', 'KR', 'CN',  730, 'JET_A1',  20100.0000,  63516.0000),
    ('ER2026KAL', 1, 'RKSI', 'KLAX', 'KR', 'US', 1850, 'JET_A1', 156300.0000, 493908.0000),
    ('ER2026KAL', 2, 'RKSI', 'EGLL', 'KR', 'GB',  730, 'JET_A1',  98400.0000, 310944.0000),
    ('ER2026KAL', 3, 'RKSI', 'EDDF', 'KR', 'DE',  624, 'JET_A1',  82500.0000, 260700.0000),
    ('ER2026AAR', 1, 'RKSI', 'KLAX', 'KR', 'US',  720, 'JET_A1',  61200.0000, 193392.0000),
    ('ER2026AAR', 2, 'RKSI', 'ZSPD', 'KR', 'CN',  912, 'JET_A1',  19500.0000,  61620.0000),
    ('ER2026AAR', 3, 'RKSI', 'VVNB', 'KR', 'VN',  365, 'JET_A1',  14200.0000,  44872.0000),
    ('ER2026JJA', 1, 'RKSI', 'RJAA', 'KR', 'JP', 1095, 'JET_A1',  21800.0000,  68888.0000),
    ('ER2026JJA', 2, 'RKPC', 'VTBS', 'KR', 'TH',  548, 'JET_A1',  28500.0000,  90060.0000),
    ('ER2026JJA', 3, 'RKSI', 'RJTT', 'KR', 'JP',  730, 'JET_A1',  14600.0000,  46136.0000)
ON CONFLICT (er_id, pair_sn) DO NOTHING;

-- 2-3. 국가쌍 CO2
INSERT INTO er.tn_er_cntry_pair_co2 (er_id, pair_sn, dprtr_cntry_cd, arvl_cntry_cd, cer_estm_yn, flt_cnt, fuel_type_cd, fuel_wght, conv_fctr, co2_emsn, ofst_req_yn, cef_redu_amt) VALUES
    ('ER2025KAL', 1, 'KR', 'US', 'N', 3285, 'JET_A1', 293100.5000, 3.1600, 926197.5800, 'Y', 12500.0000),
    ('ER2025KAL', 2, 'KR', 'JP', 'N',  912, 'JET_A1',  18250.0000, 3.1600,  57670.0000, 'N',     0.0000),
    ('ER2025KAL', 3, 'KR', 'CN', 'N',  730, 'JET_A1',  20100.0000, 3.1600,  63516.0000, 'Y',  3200.0000),
    ('ER2026KAL', 1, 'KR', 'US', 'N', 1850, 'JET_A1', 156300.0000, 3.1600, 493908.0000, 'Y', 25800.0000),
    ('ER2026KAL', 2, 'KR', 'GB', 'N',  730, 'JET_A1',  98400.0000, 3.1600, 310944.0000, 'Y',  8400.0000),
    ('ER2026KAL', 3, 'KR', 'DE', 'N',  624, 'JET_A1',  82500.0000, 3.1600, 260700.0000, 'Y',  6700.0000),
    ('ER2026AAR', 1, 'KR', 'US', 'N',  720, 'JET_A1',  61200.0000, 3.1600, 193392.0000, 'Y',  5400.0000),
    ('ER2026AAR', 2, 'KR', 'CN', 'N',  912, 'JET_A1',  19500.0000, 3.1600,  61620.0000, 'N',     0.0000),
    ('ER2026AAR', 3, 'KR', 'VN', 'N',  365, 'JET_A1',  14200.0000, 3.1600,  44872.0000, 'N',     0.0000),
    ('ER2026JJA', 1, 'KR', 'JP', 'N', 1825, 'JET_A1',  36400.0000, 3.1600, 115024.0000, 'N',     0.0000),
    ('ER2026JJA', 2, 'KR', 'TH', 'N',  548, 'JET_A1',  28500.0000, 3.1600,  90060.0000, 'N',     0.0000)
ON CONFLICT (er_id, pair_sn) DO NOTHING;

-- 2-4. 연료 요약
INSERT INTO er.tn_er_fuel_smry (er_id, fuel_type_cd, ttl_fuel_wght, ttl_co2_emsn) VALUES
    ('ER2025KAL', 'JET_A1', 331450.5000, 1047383.5800),
    ('ER2025KAL', 'SAF',      4500.0000,    7110.0000),
    ('ER2026KAL', 'JET_A1', 337200.0000, 1065552.0000),
    ('ER2026KAL', 'SAF',      6800.0000,   10744.0000),
    ('ER2026AAR', 'JET_A1',  94900.0000,  299884.0000),
    ('ER2026AAR', 'SAF',      1200.0000,    1896.0000),
    ('ER2026JJA', 'JET_A1',  64900.0000,  205084.0000),
    ('ER0001',    'JET_A1',  15000.0000,   47400.0000),
    ('ER0002',    'JET_A1',  88000.0000,  278080.0000),
    ('ER0003',    'JET_A1', 145000.0000,  458200.0000)
ON CONFLICT (er_id, fuel_type_cd) DO NOTHING;

-- 2-5. 데이터 갭
INSERT INTO er.tn_er_data_gap (er_id, gap_sn, gap_dt, ref_info, gap_cause_cd, gap_type_cd, repl_mthd_desc, afct_co2_emsn, thrshld_5pct_xc_yn) VALUES
    ('ER2025KAL', 1, '2025-03-15', 'KE017 LAX→ICN ACARS 송신 누락', 'OUTAGE',      'FUEL',    '동일 노선 직전 30일 평균 연료량 적용', 215.5000, 'N'),
    ('ER2025KAL', 2, '2025-07-22', 'FQIS 정비로 측정 불가',         'MAINTENANCE', 'FUEL',    '급유전표 기반 보정',                  148.2000, 'N'),
    ('ER2026KAL', 1, '2026-01-08', 'OZ701 ICN→PVG 운항일지 누락',   'MISSING',    'FLT_CNT', '항공사 운항이력 재조회로 보정',         0.0000, 'N'),
    ('ER2026AAR', 1, '2026-02-14', 'ATC 시스템 장애',               'OUTAGE',      'OTHERS',  '관제 백업 로그 사용',                   85.0000, 'N'),
    ('ER2026JJA', 1, '2026-04-03', 'OOOI 전송 실패',                'OUTAGE',      'FUEL',    '직전 동일 노선 평균값 대체',           42.3000, 'N')
ON CONFLICT (er_id, gap_sn) DO NOTHING;

-- 2-6. AFBR (항공기 평균연료소비율)
INSERT INTO er.tn_er_afbr (er_id, acft_type_cd, afbr_val, afbr_unit) VALUES
    ('ER2025KAL', 'B77W', 105.2300, 'kg/min'),
    ('ER2025KAL', 'B789',  78.4500, 'kg/min'),
    ('ER2025KAL', 'A359',  72.1000, 'kg/min'),
    ('ER2025KAL', 'A388', 158.6200, 'kg/min'),
    ('ER2026KAL', 'B77W', 104.8900, 'kg/min'),
    ('ER2026KAL', 'B789',  77.9800, 'kg/min'),
    ('ER2026KAL', 'A359',  71.6500, 'kg/min'),
    ('ER2026AAR', 'A333',  82.4500, 'kg/min'),
    ('ER2026AAR', 'A359',  72.3000, 'kg/min'),
    ('ER2026AAR', 'B77W', 105.5000, 'kg/min'),
    ('ER2026JJA', 'B738',  43.2500, 'kg/min'),
    ('ER2026JJA', 'B739',  44.8000, 'kg/min')
ON CONFLICT (er_id, acft_type_cd) DO NOTHING;

-- 2-7. 검증기관 정보
INSERT INTO er.tn_er_vrfr_info (er_id, vrfr_sn, vrfcn_inst_id, cnct_desc, accrd_dtl) VALUES
    ('ER2025KAL', 1, 'VI_KVA', '한국검증협회 김검증 차장 / 02-555-1234 / kim.kva@kva.or.kr', 'KAB 인정 14065 / 항공부문 인증범위'),
    ('ER2026KAL', 1, 'VI_KVA', '한국검증협회 이검증 부장 / 02-555-1235 / lee.kva@kva.or.kr', 'KAB 인정 14065 / 항공부문 인증범위'),
    ('ER2026AAR', 1, 'VI_KVA', '한국검증협회 박검증 과장 / 02-555-1236',                     'KAB 인정 14065'),
    ('ER2026JJA', 1, 'VI_KVA', '한국검증협회 정검증 대리 / 02-555-1237',                     'KAB 인정 14065'),
    ('ER0001',    1, 'VI0001', '한국인정원 / 02-444-5678',                                     'KAB 인정 14065'),
    ('ER0002',    1, 'VI0002', '한국선급 / 02-333-9999',                                       'KAB 인정 14065'),
    ('ER0003',    1, 'VI_KVA', '한국검증협회',                                                 'KAB 인정 14065')
ON CONFLICT (er_id, vrfr_sn) DO NOTHING;

-- ----------------------------------------------------------------
-- 3. CEF 자식 테이블 (CEF0002, CEF0003)
-- ----------------------------------------------------------------

INSERT INTO er.tn_cef_claim (cef_id, claim_no, pure_fuel_purch_dt, fuel_prdc_co_nm, fuel_prdc_addr, fuel_prdc_dt, fuel_prdc_lc, fuel_type_cd, fdstk_type_cd, conv_proc_cd, batch_id_no, pure_fuel_mass, batch_purch_ratio, batch_purch_mass) VALUES
    ('CEF0002', 'CLM-2026-JJA-001', '2026-03-15', 'Neste Singapore Pte Ltd', '1 Tuas South Lane, Singapore 637022', '2026-02-28', 'Tuas Refinery', 'SAF', 'UCO',  'HEFA', 'BATCH2026KAL001', 1200.0000, 0.5000,  600.0000),
    ('CEF0002', 'CLM-2026-JJA-002', '2026-05-20', 'World Energy LLC',        '6601 W Imperial Hwy, CA, USA',          '2026-04-30', 'Paramount Refinery', 'SAF', 'Tallow', 'HEFA', 'BATCH2026KAL001', 800.0000, 0.4000,  320.0000),
    ('CEF0003', 'CLM-2026-KAL-001', '2026-02-10', 'Neste Singapore Pte Ltd', '1 Tuas South Lane, Singapore 637022', '2026-01-15', 'Tuas Refinery', 'SAF', 'UCO',  'HEFA', 'BATCH2026KAL001', 5400.0000, 0.8000, 4320.0000),
    ('CEF0003', 'CLM-2026-KAL-002', '2026-06-18', 'SK Energy Co., Ltd.',      '울산광역시 남구 산암로 1',                '2026-05-30', '울산 정유공장',     'SAF', 'PFAD', 'HEFA', 'E2E-BATCH-1779552405', 1400.0000, 0.6000,  840.0000)
ON CONFLICT (cef_id, claim_no) DO NOTHING;

INSERT INTO er.tn_cef_lcyc (cef_id, claim_no, lca_value_se_cd, core_lca_val, iluc_emsn, ttl_lca_val) VALUES
    ('CEF0002', 'CLM-2026-JJA-001', 'DEFAULT', 24.5000, 0.0000, 24.5000),
    ('CEF0002', 'CLM-2026-JJA-002', 'DEFAULT', 22.8000, 0.0000, 22.8000),
    ('CEF0003', 'CLM-2026-KAL-001', 'ACTUAL',  18.4500, 0.0000, 18.4500),
    ('CEF0003', 'CLM-2026-KAL-002', 'DEFAULT', 31.2000, 2.5000, 33.7000)
ON CONFLICT (cef_id, claim_no) DO NOTHING;

INSERT INTO er.tn_cef_spchn (cef_id, claim_no, chn_sn, sply_chn_role_cd, co_nm, co_addr, lc_addr, recv_dt, recv_mass, blnd_ratio) VALUES
    ('CEF0002', 'CLM-2026-JJA-001', 1, 'BLENDER',   'SK Incheon Petrochem',  '인천광역시 서구 정서진로 410',           '인천 항만',     '2026-03-10', 1200.0000, 0.0500),
    ('CEF0002', 'CLM-2026-JJA-001', 2, 'SHIPPER',   '한진해운',                '서울특별시 중구 남대문로 63',            '부산항',         '2026-03-08',  600.0000, NULL),
    ('CEF0002', 'CLM-2026-JJA-002', 1, 'MID_BUYER', 'GS Caltex',              '서울특별시 강남구 논현로 508',           NULL,             '2026-05-18',  800.0000, NULL),
    ('CEF0003', 'CLM-2026-KAL-001', 1, 'BLENDER',   'SK Incheon Petrochem',  '인천광역시 서구 정서진로 410',           '인천 항만',     '2026-02-05', 5400.0000, 0.1000),
    ('CEF0003', 'CLM-2026-KAL-001', 2, 'SHIPPER',   'Maersk Line',            '코펜하겐 본사',                            '부산-싱가포르',  '2026-02-01', 5400.0000, NULL),
    ('CEF0003', 'CLM-2026-KAL-002', 1, 'BLENDER',   'S-OIL',                  '서울특별시 마포구 백범로 192',           '울산공장',       '2026-06-15', 1400.0000, 0.0800)
ON CONFLICT (cef_id, claim_no, chn_sn) DO NOTHING;

-- ----------------------------------------------------------------
-- 4. EUCR 자식 테이블 (EUCR0001)
-- ----------------------------------------------------------------

INSERT INTO er.tn_eucr_batch (eucr_id, batch_no, crdt_type_cd, sub_qty, prgrm_nm, vntg_yr, mthdlgy_id, crdt_no_from, crdt_no_to, cncl_dt) VALUES
    ('EUCR0001', 'EU-BATCH-001', 'VCS',           50000.0000, 'Verra VCS', '2024', 'VM0007', 'VCS-1000001', 'VCS-1050000', NULL),
    ('EUCR0001', 'EU-BATCH-002', 'GOLD_STANDARD', 30000.0000, 'Gold Standard', '2024', 'GS-AGR-001', 'GS-2000001', 'GS-2030000', NULL),
    ('EUCR0001', 'EU-BATCH-003', 'VCS',           20000.0000, 'Verra VCS', '2025', 'VM0042', 'VCS-3000001', 'VCS-3020000', NULL)
ON CONFLICT (eucr_id, batch_no) DO NOTHING;

INSERT INTO er.tn_eucr_crdt_dtl (eucr_id, crdt_no, batch_no, mthdlgy_id, vntg_yr) VALUES
    ('EUCR0001', 'VCS-1000001-A', 'EU-BATCH-001', 'VM0007', '2024'),
    ('EUCR0001', 'VCS-1000002-A', 'EU-BATCH-001', 'VM0007', '2024'),
    ('EUCR0001', 'VCS-1000003-A', 'EU-BATCH-001', 'VM0007', '2024'),
    ('EUCR0001', 'GS-2000001-A',  'EU-BATCH-002', 'GS-AGR-001', '2024'),
    ('EUCR0001', 'GS-2000002-A',  'EU-BATCH-002', 'GS-AGR-001', '2024'),
    ('EUCR0001', 'VCS-3000001-A', 'EU-BATCH-003', 'VM0042', '2025')
ON CONFLICT (eucr_id, crdt_no) DO NOTHING;

-- ----------------------------------------------------------------
-- 5. OoM 자식 테이블 (OOM0001)
-- ----------------------------------------------------------------

INSERT INTO er.tn_oom_check_addl_rqst (oom_id, rqst_sn, rqst_dt, rqst_user_id, rqst_cn, resp_dt, resp_user_id, resp_cn) VALUES
    ('OOM0001', 1, '2026-04-10 09:30:00', 'kotsa01',    'AFBR 산정 근거 추가 자료 요청 — B77W 기종의 30일 평균 데이터 제공 요망',     '2026-04-12 14:20:00', 'vrf_lead', '첨부파일로 30일 평균 데이터(엑셀) 제출 완료'),
    ('OOM0001', 2, '2026-04-15 11:00:00', 'kotsa01',    '데이터갭 보정방식 재확인 요청 — 직전 평균값 적용 시점 명시 필요',           '2026-04-16 10:45:00', 'vrf_lead', '보정 절차서 v1.2 제출 (3.4.2절 참조)'),
    ('OOM0001', 3, '2026-04-20 16:15:00', 'molit_admin','검증팀 독립성 입증자료 요청 — 동일 항공사 연속 검증 횟수 보고서',           NULL, NULL, NULL)
ON CONFLICT (oom_id, rqst_sn) DO NOTHING;

INSERT INTO er.tn_oom_check_vrfr_eval (oom_id, vrfcn_inst_id, eval_grd_cd, eval_rmrk) VALUES
    ('OOM0001', 'VI_KVA', 'GOOD', '검증 절차 준수, 자료 제출 신속, 독립성 유지 — 종합 양호'),
    ('OOM0001', 'VI0001', 'AVG',  '일부 절차 보완 필요 (샘플링 근거 기록 미흡)'),
    ('OOM0001', 'VI0002', 'GOOD', '검증 보고서 품질 우수')
ON CONFLICT (oom_id, vrfcn_inst_id) DO NOTHING;

-- ----------------------------------------------------------------
-- 6. VR 자식 테이블 (VR0001)
-- ----------------------------------------------------------------

INSERT INTO vr.tn_vr_scope (vr_id, vrfcn_inst_nm, vrfcn_inst_addr, rprt_type_desc, rprt_prd_desc) VALUES
    ('VR0001', '한국검증협회 (KVA)', '서울특별시 영등포구 여의대로 24', 'CORSIA 배출량 검증보고서', '2025-01-01 ~ 2025-12-31')
ON CONFLICT (vr_id) DO NOTHING;

INSERT INTO vr.tn_vr_team (vr_id, member_sn, user_nm, role_cd, accrd_dtl, conscutv_cnt) VALUES
    ('VR0001', 1, '김검증', 'LEAD',           'KAB 인정 / 항공부문 5년 경력 / ISO14064-3 자격', 2),
    ('VR0001', 2, '이검증', 'MEMBER',         '항공부문 3년 경력 / 데이터 분석 자격',           1),
    ('VR0001', 3, '박검증', 'MEMBER',         '항공부문 2년 경력',                              1),
    ('VR0001', 4, '정독립', 'INDEP_REVIEWER', '검증부문 10년 경력 / 독립검토 전문가',           0)
ON CONFLICT (vr_id, member_sn) DO NOTHING;

INSERT INTO vr.tn_vr_time (vr_id, onsite_hrs, offsite_hrs, total_hrs) VALUES
    ('VR0001', 24.50, 56.00, 80.50)
ON CONFLICT (vr_id) DO NOTHING;

INSERT INTO vr.tn_vr_input_info (vr_id, info_sn, doc_nm, doc_se_cd, file_id) VALUES
    ('VR0001', 1, '2025년도 모니터링계획(EMP)',       'EMP',   NULL),
    ('VR0001', 2, '2025년도 배출량보고서(ER)',        'ER',    NULL),
    ('VR0001', 3, '항공기 등록 원부 사본',              'OTHER', NULL),
    ('VR0001', 4, '연료 공급사 청구서 12개월분',        'OTHER', NULL),
    ('VR0001', 5, '운항관리시스템(FOC) 데이터 추출본', 'OTHER', NULL),
    ('VR0001', 6, '데이터 갭 보정 절차서',              'OTHER', NULL)
ON CONFLICT (vr_id, info_sn) DO NOTHING;

INSERT INTO vr.tn_vr_prcdr (vr_id, strg_anlys_cn, risk_eval_cn, smplng_actv_cn, smplng_rslt_cn, emp_compl_cn) VALUES
    ('VR0001',
     '항공사 전체 운영 규모, 보유 항공기 158대, 운항 노선 130개국 분석. 데이터 수집체계의 자동화 정도 평가.',
     '주요 위험요인: 1) FQIS 측정 오류 위험 — 중간 / 2) ACARS 데이터 누락 위험 — 낮음 / 3) 운항 변동에 따른 노선 재분류 — 중간',
     '데이터 표본 추출: 항공편 단위 무작위 5% 추출 (1,250편), 노선별 가중표본 적용',
     '추출 1,250편 중 1,243편 일치(99.4%), 7편 차이 발견 — 모두 5% 임계치 미만으로 적합 판정',
     'EMP 대비 ER 일관성 검토 — 모니터링 방식(MTHD_B), 측정장치(FQIS), 연료유형(JET_A1) 모두 일치')
ON CONFLICT (vr_id) DO NOTHING;

INSERT INTO vr.tn_vr_ncnfrm (vr_id, item_no, ncnfrm_se_cd, ncnfrm_desc, resol_desc, resol_dt) VALUES
    ('VR0001', 1, 'MINOR',        '2025년 3월분 일부 항공편(KE017) ACARS 데이터 누락', '직전 30일 평균값 적용 보정으로 해결',                  '2026-04-15 10:00:00'),
    ('VR0001', 2, 'MINOR',        '연료측정장치 캘리브레이션 기록 일부 누락',         '추가 캘리브레이션 인증서 제출로 해결',                '2026-04-18 14:00:00'),
    ('VR0001', 3, 'MISSTATEMENT', '운항편수 통계 미세 차이 (0.3%)',                    '재집계 후 정정 — 임계치 5% 미만으로 영향 없음',       '2026-04-20 11:30:00')
ON CONFLICT (vr_id, item_no) DO NOTHING;

INSERT INTO vr.tn_vr_cncls (vr_id, data_qlty_eval, mtrlty_eval, er_cncls, eucr_cncls, judg_cn, indep_review_cn, indep_review_user_nm, final_opnn_cd) VALUES
    ('VR0001',
     '데이터 수집·집계 절차 적정, 측정장치 정밀도 양호, 자료 추적성 확보',
     '발견된 부적합사항(MINOR 2건, MISSTATEMENT 1건) 모두 5% 임계치 미만으로 중요성 기준 미달',
     '2025년 CO2 배출량 총 1,054,493톤 — 합리적 보증 수준에서 적정 표시',
     '상쇄단위 사용량 12,500톤 — 등록정보 및 취소내역 적정 확인',
     '본 검증에서 식별된 부적합사항은 모두 해결되었으며, 보고서가 CORSIA 기준 및 국토교통부 고시에 따라 적정하게 작성되었음을 확인',
     '독립검토 결과 검증절차 적정, 결론 합리적 — 검증의견에 동의',
     '정독립',
     'REASONABLE')
ON CONFLICT (vr_id) DO NOTHING;

-- ----------------------------------------------------------------
-- 7. SAF 자식 테이블
-- ----------------------------------------------------------------

-- 7-1. 공항별 급유실적
INSERT INTO saf.tn_saf_airprt_fuel (airprt_cd, rprt_yr, oprtr_id, flt_cnt, flt_time, req_fuel_qty, actl_fuel_qty, yr_non_tanked_qty, yr_tanked_safety_qty) VALUES
    ('RKSI', '2026', 'KAL', 28500, 142500.00, 285000.0000, 298500.0000, 250000.0000,  35000.0000),
    ('RKSI', '2026', 'AAR', 15200,  76000.00, 152000.0000, 158300.0000, 135000.0000,  20000.0000),
    ('RKSI', '2026', 'JJA', 22000,  44000.00,  88000.0000,  91500.0000,  82000.0000,   8000.0000),
    ('RKPC', '2026', 'JJA',  3800,   7600.00,  15200.0000,  15800.0000,  14500.0000,   1200.0000),
    ('RKSS', '2026', 'KAL',  4200,  21000.00,  42000.0000,  43500.0000,  38000.0000,   5000.0000),
    ('KLAX', '2026', 'KAL',  1850,   9250.00,  18500.0000,  19200.0000,  17500.0000,   1700.0000),
    ('KLAX', '2026', 'AAR',   720,   3600.00,   7200.0000,   7500.0000,   6800.0000,    700.0000),
    ('RJTT', '2026', 'KAL',   912,   1824.00,   3648.0000,   3800.0000,   3500.0000,    300.0000),
    ('RJTT', '2026', 'JJA',  1095,   2190.00,   4380.0000,   4550.0000,   4200.0000,    350.0000),
    ('VTBS', '2026', 'JJA',   548,   3288.00,   6576.0000,   6850.0000,   6300.0000,    550.0000)
ON CONFLICT (airprt_cd, rprt_yr, oprtr_id) DO NOTHING;

-- 7-2. 공항별 SAF 구매
INSERT INTO saf.tn_saf_airprt_purch (airprt_cd, rprt_yr, oprtr_id, purch_sn, sply_co_nm, batch_id, purch_qty, fuel_type_cd, orgn_info) VALUES
    ('RKSI', '2026', 'KAL', 1, 'Neste Singapore',         'BATCH2026KAL001', 5400.0000, 'SAF',    '싱가포르 Neste Tuas Refinery 생산 — UCO 원료'),
    ('RKSI', '2026', 'KAL', 2, 'SK Energy',                'E2E-BATCH-1779552405', 1400.0000, 'SAF', '울산 정유공장 — PFAD 원료'),
    ('RKSI', '2026', 'AAR', 1, 'Neste Singapore',         'BATCH2026KAL001', 1200.0000, 'SAF',    '싱가포르 Neste — UCO 원료'),
    ('RKSI', '2026', 'JJA', 1, 'World Energy LLC',         'BATCH2026KAL001',  800.0000, 'SAF',    'World Energy 캘리포니아 — Tallow 원료'),
    ('KLAX', '2026', 'KAL', 1, 'World Energy LLC',         'E2E-BATCH-1779552405',  650.0000, 'SAF', 'LAX 공항 직접 급유 — Tallow 원료'),
    ('RKPC', '2026', 'JJA', 1, 'SK Energy',                'E2E-BATCH-1779552405',  200.0000, 'SAF', '제주공항 시범 공급')
ON CONFLICT (airprt_cd, rprt_yr, oprtr_id, purch_sn) DO NOTHING;

-- 7-3. 혼합사
INSERT INTO saf.tn_saf_blndr (batch_id, blndr_co_nm, blndr_co_addr, blnd_lc_addr, recv_dt, recv_mass, fuel_type_cd, blnd_ratio, trnsprt_co_nm, trnsprt_co_addr, mid_buyer_co_nm, mid_buyer_co_addr) VALUES
    ('BATCH2026KAL001',     'SK Incheon Petrochem', '인천광역시 서구 정서진로 410',   '인천 항만 SAF 혼합시설', '2026-02-15', 5400.0000, 'SAF', 0.1000, 'Maersk Line', '코펜하겐 본사',               'GS Caltex', '서울특별시 강남구 논현로 508'),
    ('E2E-BATCH-1779552405','S-OIL',                '서울특별시 마포구 백범로 192',   '울산공장 혼합시설',       '2026-06-15', 1400.0000, 'SAF', 0.0800, '현대글로비스',  '서울특별시 강남구 테헤란로 521', 'SK Energy', '서울특별시 종로구 종로26')
ON CONFLICT (batch_id) DO NOTHING;

-- 7-4. 원료
INSERT INTO saf.tn_saf_feed (batch_id, fdstk_type_cd, addl_fdstk_dtl, waste_residue_yn, conv_proc_cd) VALUES
    ('BATCH2026KAL001',     'UCO',   '폐식용유 (Used Cooking Oil) — 싱가포르 식당가 수거', 'Y', 'HEFA'),
    ('E2E-BATCH-1779552405','PFAD',  'Palm Fatty Acid Distillate — 말레이시아산 잔여물',     'Y', 'HEFA')
ON CONFLICT (batch_id) DO NOTHING;

-- 7-5. 원료 원산지
INSERT INTO saf.tn_saf_feed_orgn (batch_id, orgn_sn, orgn_cntry_cd) VALUES
    ('BATCH2026KAL001',     1, 'SG'),
    ('BATCH2026KAL001',     2, 'US'),
    ('BATCH2026KAL001',     3, 'JP'),
    ('E2E-BATCH-1779552405',1, 'KR'),
    ('E2E-BATCH-1779552405',2, 'TH')
ON CONFLICT (batch_id, orgn_sn) DO NOTHING;

-- 7-6. GHG 값
INSERT INTO saf.tn_saf_ghg (batch_id, ghg_val_se_cd, core_lca_val, iluc_emsn, ttl_lca_val) VALUES
    ('BATCH2026KAL001',     'DEFAULT', 24.5000, 0.0000, 24.5000),
    ('E2E-BATCH-1779552405','ACTUAL',  18.4500, 0.0000, 18.4500)
ON CONFLICT (batch_id) DO NOTHING;

-- 7-7. 생산공급
INSERT INTO saf.tn_saf_prdc_sply (batch_id, prdc_co_nm, prdc_co_addr, prdc_pos_batch_id, prdc_pos_isue_dt, orgn_saf_qty, saf_prdc_dt, acqstn_dt, prdc_lc_addr, sply_co_nm, sply_co_addr) VALUES
    ('BATCH2026KAL001',     'Neste Singapore Pte Ltd', '1 Tuas South Lane, Singapore', 'NESTE-POS-202602-001', '2026-02-28', 7400.0000, '2026-02-15', '2026-03-10', 'Tuas Refinery, Singapore', 'Neste Korea', '서울특별시 영등포구 여의대로 70'),
    ('E2E-BATCH-1779552405','SK Energy Co., Ltd.',     '울산광역시 남구 산암로 1',     'SK-POS-202606-002',     '2026-06-30', 1400.0000, '2026-05-30', '2026-06-18', '울산 정유공장',           'SK Energy',     '서울특별시 종로구 종로 26')
ON CONFLICT (batch_id) DO NOTHING;

-- 7-8. 타깅 모니터링
INSERT INTO saf.tn_saf_tankering_mntr (oprtr_id, rprt_yr, airprt_cd, accm_qty, actl_fuel_qty, req_fuel_qty, refuel_ratio, ovr_90pct_yn, soln_desc) VALUES
    ('KAL', '2026', 'RKSI', 13500.0000, 298500.0000, 285000.0000, 1.0474, 'N', NULL),
    ('KAL', '2026', 'KLAX',   700.0000,  19200.0000,  18500.0000, 1.0378, 'N', NULL),
    ('AAR', '2026', 'RKSI',  6300.0000, 158300.0000, 152000.0000, 1.0414, 'N', NULL),
    ('JJA', '2026', 'RKSI',  3500.0000,  91500.0000,  88000.0000, 1.0398, 'N', NULL),
    ('JJA', '2026', 'RKPC',   600.0000,  15800.0000,  15200.0000, 1.0395, 'N', NULL),
    ('KAL', '2026', 'RJTT',   152.0000,   3800.0000,   3648.0000, 1.0417, 'N', NULL)
ON CONFLICT (oprtr_id, rprt_yr, airprt_cd) DO NOTHING;

-- 7-9. 현장점검
INSERT INTO saf.tn_saf_fld_inspn (inspn_id, oprtr_id, airprt_cd, inspn_dt, inspctr_user_id, chk_item_rslt, memo) VALUES
    ('FLD0001', 'KAL', 'RKSI', '2026-03-20', 'kotsa01',    '연료측정장치 OK / 급유전표 일치 / SAF 혼합비율 적정', '인천공항 1터미널 K1게이트 — 이상 없음'),
    ('FLD0002', 'AAR', 'RKSI', '2026-04-15', 'kotsa01',    '연료측정장치 OK / 일부 기록 누락 발견',                 '인천공항 2터미널 — 기록 누락 사항 시정요구'),
    ('FLD0003', 'JJA', 'RKPC', '2026-05-08', 'molit_admin','연료측정장치 OK / 운항일지 적정',                       '제주공항 — 양호'),
    ('FLD0004', 'KAL', 'RKSS', '2026-06-22', 'kotsa01',    '연료측정장치 OK / SAF 혼합 시설 점검 완료',             '김포공항 — 양호'),
    ('FLD0005', 'KAL', 'RKSI', '2026-09-10', 'kotsa01',    '데이터 갭 보정 절차 적정 / 캘리브레이션 인증 확인',     '인천공항 정기점검')
ON CONFLICT (inspn_id) DO NOTHING;

-- ----------------------------------------------------------------
-- 8. 포털/공통 테이블
-- ----------------------------------------------------------------

-- 8-1. CCR 추출 이력
INSERT INTO ptl.tn_ptl_ccr_extr (extr_id, rprt_yr, extr_scope_cd, extr_st_cd, file_id, extr_user_id, extr_dt, rmrk) VALUES
    ('EXTR0001', '2025', 'ALL',      'DONE',  NULL, 'admin01',     '2026-03-15 10:30:00', '2025년도 CCR 전체 추출 — ICAO 제출용'),
    ('EXTR0002', '2025', 'SELECTED', 'DONE',  NULL, 'molit_admin', '2026-04-10 14:20:00', '대한항공만 선별 추출'),
    ('EXTR0003', '2026', 'ALL',      'INPRG', NULL, 'admin01',     '2026-05-20 09:15:00', '2026년 1분기 임시 추출')
ON CONFLICT (extr_id) DO NOTHING;

-- 8-2. 시뮬레이션 시나리오
INSERT INTO ptl.tn_ptl_sim (sim_id, sim_nm, owner_user_id, scope_se_cd, scope_oprtr_id, base_yr, prdctn_yr_from, prdctn_yr_to, input_json, rslt_json, share_se_cd) VALUES
    ('SIM0001', '2030년 SAF 10% 도입 시 상쇄비용 예측', 'admin01',     'ALL',   NULL,  '2025', '2026', '2030', '{"saf_ratio":0.10,"crdt_price_usd":85,"yoy_growth":0.02}'::jsonb, '{"ttl_co2_emsn":15800000,"ofst_cost_usd":12500000,"redu_amt":1580000}'::jsonb, 'ORG'),
    ('SIM0002', '대한항공 단독 — 노선 최적화 시나리오',    'molit_admin', 'OPRTR', 'KAL', '2025', '2026', '2028', '{"route_optim":true,"saf_ratio":0.05,"crdt_price_usd":80}'::jsonb,  '{"ttl_co2_emsn":10200000,"ofst_cost_usd":7800000,"redu_amt":510000}'::jsonb,  'PRIVATE'),
    ('SIM0003', '저성장 시나리오 (수요 -5%)',              'admin01',     'ALL',   NULL,  '2024', '2026', '2030', '{"yoy_growth":-0.05,"saf_ratio":0.03,"crdt_price_usd":75}'::jsonb,  '{"ttl_co2_emsn":13500000,"ofst_cost_usd":9200000,"redu_amt":405000}'::jsonb,  'PUBLIC')
ON CONFLICT (sim_id) DO NOTHING;

-- 8-3. 사용자 행위 감사 로그 (20건)
INSERT INTO ptl.th_user_actn (user_id, actn_se_cd, target_tbl, target_pk, actn_dt, rslt_cd, ip_addr, user_agent, rmrk) VALUES
    ('kal_user',    'LOGN', NULL,                NULL,         '2026-05-01 09:00:12', 'OK',   '203.247.10.21', 'Mozilla/5.0 Chrome/124', '로그인 성공'),
    ('kal_user',    'SBMT', 'emp.tn_emp_plan',   'EMP2026KAL', '2026-05-01 09:42:35', 'OK',   '203.247.10.21', 'Mozilla/5.0 Chrome/124', 'EMP2026KAL 제출'),
    ('kotsa_rvwr',  'LOGN', NULL,                NULL,         '2026-05-01 10:15:08', 'OK',   '203.247.20.45', 'Mozilla/5.0 Chrome/124', '로그인 성공'),
    ('kotsa_rvwr',  'APRV', 'emp.tn_emp_plan',   'EMP2026KAL', '2026-05-01 11:20:50', 'OK',   '203.247.20.45', 'Mozilla/5.0 Chrome/124', 'EMP 승인'),
    ('aar_user',    'LOGN', NULL,                NULL,         '2026-05-02 08:55:21', 'OK',   '203.247.10.55', 'Mozilla/5.0 Edge/124',   '로그인 성공'),
    ('aar_user',    'SBMT', 'emp.tn_emp_plan',   'EMP2026AAR', '2026-05-02 10:30:18', 'OK',   '203.247.10.55', 'Mozilla/5.0 Edge/124',   'EMP2026AAR 제출'),
    ('jna_mgr',     'LOGN', NULL,                NULL,         '2026-05-02 09:12:44', 'OK',   '203.247.10.88', 'Mozilla/5.0 Chrome/124', '로그인 성공'),
    ('jna_mgr',     'SBMT', 'er.tn_er',          'ER2026JJA',  '2026-05-02 14:25:30', 'OK',   '203.247.10.88', 'Mozilla/5.0 Chrome/124', 'ER 제출'),
    ('vrf_lead',    'LOGN', NULL,                NULL,         '2026-05-03 09:00:00', 'OK',   '203.247.30.10', 'Mozilla/5.0 Chrome/124', '로그인 성공'),
    ('vrf_lead',    'SBMT', 'vr.tn_vr',          'VR0001',     '2026-05-03 16:40:12', 'OK',   '203.247.30.10', 'Mozilla/5.0 Chrome/124', 'VR 제출'),
    ('admin01',     'LOGN', NULL,                NULL,         '2026-05-04 08:30:00', 'OK',   '210.10.1.5',    'Mozilla/5.0 Chrome/124', '관리자 로그인'),
    ('admin01',     'EXTR', 'ptl.tn_ptl_ccr_extr','EXTR0001',  '2026-05-04 10:30:00', 'OK',   '210.10.1.5',    'Mozilla/5.0 Chrome/124', 'CCR 전체 추출 실행'),
    ('molit_admin', 'LOGN', NULL,                NULL,         '2026-05-05 09:15:00', 'OK',   '210.10.2.7',    'Mozilla/5.0 Chrome/124', '국토부 담당자 로그인'),
    ('molit_admin', 'APRV', 'er.tn_er',          'ER2026KAL',  '2026-05-05 14:00:00', 'OK',   '210.10.2.7',    'Mozilla/5.0 Chrome/124', 'ER 승인'),
    ('kal_user',    'DWNLD','tn_file',           'F00001',     '2026-05-06 11:30:22', 'OK',   '203.247.10.21', 'Mozilla/5.0 Chrome/124', '검증보고서 다운로드'),
    ('aar_opr',     'RJCT', 'er.tn_er',          'ER2026AAR',  '2026-05-07 15:20:00', 'OK',   '203.247.10.60', 'Mozilla/5.0 Chrome/124', 'ER 반려 (데이터 보완 필요)'),
    ('kal_user',    'AUTHZ_DENY', 'ptl.tn_ptl_sim','SIM0001',  '2026-05-08 10:00:00', 'FAIL', '203.247.10.21', 'Mozilla/5.0 Chrome/124', '권한 없음 — 시뮬레이션 PUBLIC 외 접근 거절'),
    ('jna_mgr',     'CNCL', 'er.tn_er',          'ER0001',     '2026-05-09 09:45:00', 'OK',   '203.247.10.88', 'Mozilla/5.0 Chrome/124', 'ER 작성 취소'),
    ('vrf_member',  'LOGN', NULL,                NULL,         '2026-05-10 09:00:00', 'OK',   '203.247.30.20', 'Mozilla/5.0 Chrome/124', '로그인 성공'),
    ('vrf_member',  'LOGT', NULL,                NULL,         '2026-05-10 18:00:00', 'OK',   '203.247.30.20', 'Mozilla/5.0 Chrome/124', '로그아웃');

-- 8-4. 결재 태스크
INSERT INTO com.tn_atrz_task (atrz_task_id, atrz_task_nm, atrz_task_desc, sys_se_cd) VALUES
    ('TASK_EMP_APRV', 'EMP 승인', 'EMP(모니터링계획) 검토·승인 태스크', 'EMP'),
    ('TASK_ER_APRV',  'ER 승인',  'ER(배출량보고서) 검토·승인 태스크',   'ER'),
    ('TASK_VR_APRV',  'VR 승인',  'VR(검증보고서) 검토·승인 태스크',     'VR'),
    ('TASK_CEF_APRV', 'CEF 승인', 'CEF(상쇄배출량) 검토·승인 태스크',    'ER')
ON CONFLICT (atrz_task_id) DO NOTHING;

-- 8-5. 결재 요청
INSERT INTO com.tn_atrz_dmnd (atrz_dmnd_id, atrz_task_id, rfrnc_tbl_nm, rfrnc_key_cn, dmnd_user_id, dmnd_dt, atrz_st_cd, title, contents) VALUES
    ('ATRZ20260501001', 'TASK_EMP_APRV', 'emp.tn_emp_plan',  'EMP2026KAL', 'kal_user',   '2026-05-01 09:42:35', 'APRV', '[KAL] 2026년도 모니터링계획(EMP) 승인 요청', '대한항공 2026년 EMP 작성 완료, 승인 부탁드립니다.'),
    ('ATRZ20260502001', 'TASK_EMP_APRV', 'emp.tn_emp_plan',  'EMP2026AAR', 'aar_user',   '2026-05-02 10:30:18', 'PEND', '[AAR] 2026년도 모니터링계획(EMP) 승인 요청', '아시아나항공 2026년 EMP 작성 완료.'),
    ('ATRZ20260502002', 'TASK_ER_APRV',  'er.tn_er',         'ER2026JJA',  'jna_mgr',    '2026-05-02 14:25:30', 'PEND', '[JJA] 2026년도 배출량보고서(ER) 승인 요청',   '제주항공 2026년 ER 제출.'),
    ('ATRZ20260503001', 'TASK_VR_APRV',  'vr.tn_vr',         'VR0001',     'vrf_lead',   '2026-05-03 16:40:12', 'PEND', '[VR] VR0001 검증보고서 승인 요청',            'KAL 2025년 검증보고서 제출.'),
    ('ATRZ20260601001', 'TASK_CEF_APRV', 'er.tn_cef',        'CEF0003',    'kal_user',   '2026-06-01 11:00:00', 'PEND', '[CEF] CEF0003 상쇄배출량 승인 요청',          'KAL 2026년 SAF 사용량 5,400톤 기반 CEF 신청.')
ON CONFLICT (atrz_dmnd_id) DO NOTHING;

-- 8-6. 결재 처리
INSERT INTO com.tn_atrz_prcs (atrz_dmnd_id, atrz_seq, atrz_user_id, atrz_role_cd, atrz_rslt_cd, atrz_dt, atrz_opnn) VALUES
    ('ATRZ20260501001', 1, 'kotsa_rvwr',  'REVIEWER', 'APRV', '2026-05-01 11:20:50', 'EMP 검토 완료, 적정'),
    ('ATRZ20260501001', 2, 'molit_admin', 'APPROVER', 'APRV', '2026-05-01 14:30:15', '국토부 최종 승인'),
    ('ATRZ20260502001', 1, 'kotsa_rvwr',  'REVIEWER', NULL,    NULL,                  NULL),
    ('ATRZ20260502002', 1, 'kotsa_rvwr',  'REVIEWER', NULL,    NULL,                  NULL),
    ('ATRZ20260503001', 1, 'molit_admin', 'APPROVER', NULL,    NULL,                  NULL),
    ('ATRZ20260601001', 1, 'kotsa_rvwr',  'REVIEWER', NULL,    NULL,                  NULL)
ON CONFLICT (atrz_dmnd_id, atrz_seq) DO NOTHING;

-- 8-7. 공지사항
INSERT INTO com.tn_ntc (ntc_id, ntc_title, ntc_contents, ntc_target_ognz_se, inq_cnt, fix_yn) VALUES
    ('NTC20260501001', '[안내] 2026년도 모니터링계획(EMP) 제출 마감 안내',
     '안녕하세요. 국토교통부입니다.<br/>2026년도 모니터링계획(EMP) 제출 마감일은 <b>2026년 6월 30일</b>입니다.<br/>일정 내 제출 부탁드립니다.<br/>문의: 044-201-XXXX',
     'AIRLINE', 245, 'Y'),
    ('NTC20260502001', '[공지] CORSIA 변환계수 변경 안내 (2026년 적용)',
     'ICAO 결의에 따라 2026년부터 적용되는 SAF 변환계수가 일부 변경되었습니다.<br/>관련 매뉴얼을 첨부하니 참고 바랍니다.',
     'AIRLINE,VERIFIER', 187, 'Y'),
    ('NTC20260601001', '[안내] 시스템 정기점검 (2026.06.15 02:00~04:00)',
     '시스템 안정화를 위한 정기점검이 실시됩니다.<br/>점검 시간: 2026.06.15 02:00 ~ 04:00<br/>해당 시간 동안 시스템 접속이 제한됩니다.',
     'ALL', 132, 'N')
ON CONFLICT (ntc_id) DO NOTHING;

-- ================================================================
-- 검증 SELECT (각 테이블 건수 확인)
-- ================================================================
SELECT 'emp.tn_emp_acft'           AS tbl, COUNT(*) AS cnt FROM emp.tn_emp_acft        UNION ALL
SELECT 'emp.tn_emp_oprtr_cnct',          COUNT(*) FROM emp.tn_emp_oprtr_cnct           UNION ALL
SELECT 'emp.tn_emp_oprtr_info',          COUNT(*) FROM emp.tn_emp_oprtr_info           UNION ALL
SELECT 'emp.tn_emp_cntry_pair',          COUNT(*) FROM emp.tn_emp_cntry_pair           UNION ALL
SELECT 'emp.tn_emp_co2_calc',            COUNT(*) FROM emp.tn_emp_co2_calc             UNION ALL
SELECT 'emp.tn_emp_co2_detail',          COUNT(*) FROM emp.tn_emp_co2_detail           UNION ALL
SELECT 'emp.tn_emp_data_ctrl',           COUNT(*) FROM emp.tn_emp_data_ctrl            UNION ALL
SELECT 'emp.tn_emp_risk',                COUNT(*) FROM emp.tn_emp_risk                 UNION ALL
SELECT 'er.tn_er_acft_fuel',             COUNT(*) FROM er.tn_er_acft_fuel              UNION ALL
SELECT 'er.tn_er_aerdrm_pair_co2',       COUNT(*) FROM er.tn_er_aerdrm_pair_co2        UNION ALL
SELECT 'er.tn_er_cntry_pair_co2',        COUNT(*) FROM er.tn_er_cntry_pair_co2         UNION ALL
SELECT 'er.tn_er_fuel_smry',             COUNT(*) FROM er.tn_er_fuel_smry              UNION ALL
SELECT 'er.tn_er_data_gap',              COUNT(*) FROM er.tn_er_data_gap               UNION ALL
SELECT 'er.tn_er_afbr',                  COUNT(*) FROM er.tn_er_afbr                   UNION ALL
SELECT 'er.tn_er_vrfr_info',             COUNT(*) FROM er.tn_er_vrfr_info              UNION ALL
SELECT 'er.tn_cef_claim',                COUNT(*) FROM er.tn_cef_claim                 UNION ALL
SELECT 'er.tn_cef_lcyc',                 COUNT(*) FROM er.tn_cef_lcyc                  UNION ALL
SELECT 'er.tn_cef_spchn',                COUNT(*) FROM er.tn_cef_spchn                 UNION ALL
SELECT 'er.tn_eucr_batch',               COUNT(*) FROM er.tn_eucr_batch                UNION ALL
SELECT 'er.tn_eucr_crdt_dtl',            COUNT(*) FROM er.tn_eucr_crdt_dtl             UNION ALL
SELECT 'er.tn_oom_check_addl_rqst',      COUNT(*) FROM er.tn_oom_check_addl_rqst       UNION ALL
SELECT 'er.tn_oom_check_vrfr_eval',      COUNT(*) FROM er.tn_oom_check_vrfr_eval       UNION ALL
SELECT 'vr.tn_vr_scope',                 COUNT(*) FROM vr.tn_vr_scope                  UNION ALL
SELECT 'vr.tn_vr_team',                  COUNT(*) FROM vr.tn_vr_team                   UNION ALL
SELECT 'vr.tn_vr_time',                  COUNT(*) FROM vr.tn_vr_time                   UNION ALL
SELECT 'vr.tn_vr_input_info',            COUNT(*) FROM vr.tn_vr_input_info             UNION ALL
SELECT 'vr.tn_vr_prcdr',                 COUNT(*) FROM vr.tn_vr_prcdr                  UNION ALL
SELECT 'vr.tn_vr_ncnfrm',                COUNT(*) FROM vr.tn_vr_ncnfrm                 UNION ALL
SELECT 'vr.tn_vr_cncls',                 COUNT(*) FROM vr.tn_vr_cncls                  UNION ALL
SELECT 'saf.tn_saf_airprt_fuel',         COUNT(*) FROM saf.tn_saf_airprt_fuel          UNION ALL
SELECT 'saf.tn_saf_airprt_purch',        COUNT(*) FROM saf.tn_saf_airprt_purch         UNION ALL
SELECT 'saf.tn_saf_blndr',               COUNT(*) FROM saf.tn_saf_blndr                UNION ALL
SELECT 'saf.tn_saf_feed',                COUNT(*) FROM saf.tn_saf_feed                 UNION ALL
SELECT 'saf.tn_saf_feed_orgn',           COUNT(*) FROM saf.tn_saf_feed_orgn            UNION ALL
SELECT 'saf.tn_saf_ghg',                 COUNT(*) FROM saf.tn_saf_ghg                  UNION ALL
SELECT 'saf.tn_saf_prdc_sply',           COUNT(*) FROM saf.tn_saf_prdc_sply            UNION ALL
SELECT 'saf.tn_saf_tankering_mntr',      COUNT(*) FROM saf.tn_saf_tankering_mntr       UNION ALL
SELECT 'saf.tn_saf_fld_inspn',           COUNT(*) FROM saf.tn_saf_fld_inspn            UNION ALL
SELECT 'ptl.tn_ptl_ccr_extr',            COUNT(*) FROM ptl.tn_ptl_ccr_extr             UNION ALL
SELECT 'ptl.tn_ptl_sim',                 COUNT(*) FROM ptl.tn_ptl_sim                  UNION ALL
SELECT 'ptl.th_user_actn',               COUNT(*) FROM ptl.th_user_actn                UNION ALL
SELECT 'com.tn_atrz_task',               COUNT(*) FROM com.tn_atrz_task                UNION ALL
SELECT 'com.tn_atrz_dmnd',               COUNT(*) FROM com.tn_atrz_dmnd                UNION ALL
SELECT 'com.tn_atrz_prcs',               COUNT(*) FROM com.tn_atrz_prcs                UNION ALL
SELECT 'com.tn_ntc',                     COUNT(*) FROM com.tn_ntc
ORDER BY tbl;
