-- 목적: 기존 ADMIN 역할명을 OWNER로 변경
-- 배경: 역할 의미를 "플랫폼 관리자"가 아닌 "가게 주인(백오피스 관리자)"로 명확히 하기 위함

UPDATE users
SET role = 'OWNER'
WHERE role = 'ADMIN';