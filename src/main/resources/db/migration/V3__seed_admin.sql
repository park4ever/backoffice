-- 초기 Shop (seed)
INSERT INTO shop (id, name, status, created_at, updated_at)
VALUES (1, '데모 상점', 'ACTIVE', NOW(6), NOW(6));

-- 초기 Admin User (seed)
INSERT INTO users (
  id, shop_id, email, password_hash, name, role, status, last_login_at, created_at, updated_at
)
VALUES (
  1, 1,
  'admin@demo.com',
  '{bcrypt}$2a$10$ljQOTx8aHnVTsm20r.ERYuiVPp8LKvUp90t2CyRrrWWLZQ4Y7Kbvu',
  '관리자',
  'ADMIN',
  'ACTIVE',
  NULL,
  NOW(6), NOW(6)
);