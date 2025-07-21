-- 출석 테이블 확인 및 조회 스크립트

-- 1. 테이블 존재 여부 확인
SHOW TABLES LIKE 'user_attendance';

-- 2. 테이블 구조 확인
DESCRIBE user_attendance;

-- 3. 테이블 생성 확인 (MySQL)
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'user_attendance';

-- 4. 인덱스 확인
SHOW INDEX FROM user_attendance;

-- 5. 샘플 데이터 조회 (테이블이 비어있을 수 있음)
SELECT * FROM user_attendance ORDER BY created_at DESC LIMIT 10;

-- 6. 오늘 출석한 사용자 수 확인
SELECT 
    COUNT(*) as today_attendance_count,
    COUNT(DISTINCT user_id) as unique_users_today
FROM user_attendance 
WHERE attendance_date = CURDATE();

-- 7. 디바이스 타입별 출석 통계
SELECT 
    device_type,
    COUNT(*) as attendance_count,
    COUNT(DISTINCT user_id) as unique_users
FROM user_attendance 
GROUP BY device_type 
ORDER BY attendance_count DESC;

-- 8. 최근 7일간 출석 통계
SELECT 
    attendance_date,
    COUNT(*) as daily_attendance_count,
    COUNT(DISTINCT user_id) as unique_users
FROM user_attendance 
WHERE attendance_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY attendance_date 
ORDER BY attendance_date DESC;

-- 9. IP 주소별 접속 통계 (보안 확인용)
SELECT 
    ip_address,
    COUNT(*) as access_count,
    COUNT(DISTINCT user_id) as unique_users
FROM user_attendance 
GROUP BY ip_address 
ORDER BY access_count DESC 
LIMIT 10; 