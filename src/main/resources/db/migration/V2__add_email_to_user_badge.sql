ALTER TABLE user_badge ADD COLUMN email VARCHAR(255) NOT NULL DEFAULT '';

-- 기존 데이터에 대해 user_id를 참조하여 email을 업데이트하려면 아래 쿼리를 사용하세요 (MySQL 기준)
-- UPDATE user_badge ub JOIN users u ON ub.user_id = u.id SET ub.email = u.email WHERE ub.email = '' OR ub.email IS NULL; 