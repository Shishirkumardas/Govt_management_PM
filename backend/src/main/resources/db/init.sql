-- GovOS Bangladesh — PostgreSQL bootstrap
-- Applied automatically by docker-compose on first volume create.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Optional: dedicated app role hardening (docker already creates govos user/db)
COMMENT ON DATABASE govos IS 'GovOS Bangladesh — AI Government Operating System';
