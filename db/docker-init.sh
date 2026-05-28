#!/bin/bash
set -e
INIT=/db-initdb

echo "[init] 계정/스키마/권한 생성..."
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$INIT/계정_스키마_권한.sql"

echo "[init] 오브젝트(테이블/인덱스) 생성..."
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$INIT/오브젝트생성.sql"

echo "[init] 초기 데이터 입력..."
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$INIT/초기데이터.sql"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$INIT/초기데이터_2차.sql"

echo "[init] icas_admin 패스워드 설정..."
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "ALTER ROLE icas_admin PASSWORD 'icas1234!';"

echo "[init] DB 초기화 완료"
