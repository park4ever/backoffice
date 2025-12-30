CREATE INDEX idx_users_shop_id_id ON users (shop_id, id);
CREATE INDEX idx_users_shop_role ON users (shop_id, role);

ALTER TABLE users DROP FOREIGN KEY fk_users_shop;

ALTER TABLE users
  ADD CONSTRAINT fk_users_shop
  FOREIGN KEY (shop_id) REFERENCES shop(id)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;