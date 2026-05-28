-- ================================================================
-- icas-cems — DB / 계정 / 스키마 / TABLESPACE / GRANT
-- DB     : PostgreSQL 16
-- ================================================================

-- DB 생성 (postgres 슈퍼유저로 1회 실행)
-- CREATE DATABASE icas
--     WITH OWNER = postgres
--          ENCODING = 'UTF8'
--          LC_COLLATE = 'ko_KR.UTF-8'
--          LC_CTYPE   = 'ko_KR.UTF-8'
--          TEMPLATE = template0;

-- 본 파일은 \c icas 후 실행

-- ----------------------------------------------------------------
-- 1. 계정
-- ----------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'icas_admin') THEN
        CREATE ROLE icas_admin LOGIN PASSWORD 'CHANGE_ME_ADMIN_PWD'
            CONNECTION LIMIT 50 VALID UNTIL 'infinity';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'icas_readonly') THEN
        CREATE ROLE icas_readonly LOGIN PASSWORD 'CHANGE_ME_READONLY_PWD'
            CONNECTION LIMIT 10 VALID UNTIL 'infinity';
    END IF;
END
$$;

-- ----------------------------------------------------------------
-- 2. 스키마
-- ----------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS com AUTHORIZATION icas_admin;
CREATE SCHEMA IF NOT EXISTS emp AUTHORIZATION icas_admin;
CREATE SCHEMA IF NOT EXISTS er  AUTHORIZATION icas_admin;
CREATE SCHEMA IF NOT EXISTS vr  AUTHORIZATION icas_admin;
CREATE SCHEMA IF NOT EXISTS saf AUTHORIZATION icas_admin;
CREATE SCHEMA IF NOT EXISTS ptl AUTHORIZATION icas_admin;

COMMENT ON SCHEMA com IS '공통 (사용자/권한/공통코드/조직/메뉴/결재/파일/외부코드/게시판)';
COMMENT ON SCHEMA emp IS '배출량 모니터링 계획서 (EMP)';
COMMENT ON SCHEMA er  IS '배출량 보고서 + CEF + EUCR + OoM-check';
COMMENT ON SCHEMA vr  IS '검증 보고서 (VR)';
COMMENT ON SCHEMA saf IS '지속가능항공유 (SAF)';
COMMENT ON SCHEMA ptl IS '통합 포털 + CCR + 시뮬레이션 + 통계';

-- ----------------------------------------------------------------
-- 3. 권한
-- ----------------------------------------------------------------
GRANT USAGE ON SCHEMA com, emp, er, vr, saf, ptl TO icas_admin;
GRANT USAGE ON SCHEMA com, emp, er, vr, saf, ptl TO icas_readonly;

GRANT ALL ON ALL TABLES    IN SCHEMA com, emp, er, vr, saf, ptl TO icas_admin;
GRANT ALL ON ALL SEQUENCES IN SCHEMA com, emp, er, vr, saf, ptl TO icas_admin;
GRANT SELECT ON ALL TABLES IN SCHEMA com, emp, er, vr, saf, ptl TO icas_readonly;

ALTER DEFAULT PRIVILEGES IN SCHEMA com, emp, er, vr, saf, ptl
    GRANT ALL ON TABLES TO icas_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA com, emp, er, vr, saf, ptl
    GRANT ALL ON SEQUENCES TO icas_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA com, emp, er, vr, saf, ptl
    GRANT SELECT ON TABLES TO icas_readonly;

-- search_path 기본값 (백엔드는 ?currentSchema= 로 override)
ALTER ROLE icas_admin    SET search_path = com, emp, er, vr, saf, ptl, public;
ALTER ROLE icas_readonly SET search_path = com, emp, er, vr, saf, ptl, public;
