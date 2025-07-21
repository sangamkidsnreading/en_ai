-- 뱃지 관련 테이블 상태 확인 SQL

-- 1. badge_settings 테이블 확인
SELECT 'badge_settings 테이블 상태:' AS info;
SHOW TABLES LIKE 'badge_settings';

-- 2. user_badge 테이블 확인
SELECT 'user_badge 테이블 상태:' AS info;
SHOW TABLES LIKE 'user_badge';

-- 3. badge_settings 데이터 확인
SELECT 'badge_settings 데이터 개수:' AS info;
SELECT COUNT(*) as badge_count FROM badge_settings;

-- 4. user_badge 데이터 확인
SELECT 'user_badge 데이터 개수:' AS info;
SELECT COUNT(*) as user_badge_count FROM user_badge;

-- 5. 사용자 테이블 확인
SELECT 'users 테이블 상태:' AS info;
SHOW TABLES LIKE 'users';

-- 6. 사용자 데이터 확인
SELECT '사용자 데이터 개수:' AS info;
SELECT COUNT(*) as user_count FROM users;

-- 7. badge_settings 상세 데이터 확인
SELECT 'badge_settings 상세 데이터:' AS info;
SELECT id, badge_name, badge_icon, attendance_count, streak_count, words_count, sentences_count, is_active 
FROM badge_settings 
ORDER BY display_order;

-- 8. user_badge 상세 데이터 확인
SELECT 'user_badge 상세 데이터:' AS info;
SELECT ub.id, ub.user_id, ub.badge_id, ub.earned_at, ub.is_displayed,
       bs.badge_name, bs.badge_icon
FROM user_badge ub
JOIN badge_settings bs ON ub.badge_id = bs.id
ORDER BY ub.earned_at DESC
LIMIT 10; 