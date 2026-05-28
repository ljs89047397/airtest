-- 2026-05-23 VR 권고 라이프사이클 컬럼 추가 (KOTSA 권고 일자/작업자)
-- E2E 테스트 중 VrMapper.updateRecommend SQL 이 rcmmd_dt 컬럼을 참조하나 DDL 누락 발견
ALTER TABLE vr.tn_vr ADD COLUMN IF NOT EXISTS rcmmd_dt date;
ALTER TABLE vr.tn_vr ADD COLUMN IF NOT EXISTS rcmmd_user_id varchar(50);

-- SAF 혼합 모니터링: 마지막 산출 일시 컬럼 (E2E 중 발견)
ALTER TABLE saf.tn_saf_blnd_mntr ADD COLUMN IF NOT EXISTS last_calc_dt timestamp;

-- SAF 인증서: 운영사 ID 컬럼 (E2E 중 발견)
ALTER TABLE saf.tn_saf_cert ADD COLUMN IF NOT EXISTS oprtr_id varchar(10);
UPDATE saf.tn_saf_cert SET oprtr_id = (SELECT oprtr_id FROM saf.tn_saf_batch b WHERE b.batch_id = tn_saf_cert.batch_id) WHERE oprtr_id IS NULL;
