-- -----------------------------------------------------------------------------
-- data.sql - Seed data loaded automatically on application startup
-- -----------------------------------------------------------------------------
-- WHY: We need at least the three roles to exist before any user can register.
--      We also seed one ADMIN user so the system is usable right away.
--
-- Admin credentials:
--   username : admin
--   password : admin123   (BCrypt hash stored below)
-- -----------------------------------------------------------------------------

-- 1. Roles --------------------------------------------------------------------
-- VIEWER  -> read-only access to dashboard data
-- ANALYST -> read + analytics / insights
-- ADMIN   -> full management: users, transactions, roles
INSERT INTO roles (id, name, description, is_active, created_at, updated_at, created_by, updated_by)
VALUES
    (1, 'VIEWER',  'Can view dashboard data only',                      true, NOW(), NOW(), 'system', 'system'),
    (2, 'ANALYST', 'Can view records and access analytics insights',     true, NOW(), NOW(), 'system', 'system'),
    (3, 'ADMIN',   'Full access: manage users, transactions, and roles', true, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;

-- 2. Admin User ---------------------------------------------------------------
-- BCrypt hash of "admin123" (cost factor 10)
INSERT INTO users (id, username, email, password, full_name, status, role_id, is_active, created_at, updated_at, created_by, updated_by)
VALUES
    (1, 'admin', 'admin@finance.com',
     '$2b$10$zTAJyd5/IidkmRyoIBYbUu.vg2p7XD2wcT3U3WmRocnvzNpTJVlzm',
     'System Administrator', 'ACTIVE', 3, true, NOW(), NOW(), 'system', 'system')
ON CONFLICT (id) DO NOTHING;
