-- user_badge 테이블 생성
CREATE TABLE IF NOT EXISTS user_badge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at DATETIME NOT NULL,
    is_displayed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    -- 외래키 제약조건
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badge_settings(id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_user_badge_user_id (user_id),
    INDEX idx_user_badge_badge_id (badge_id),
    INDEX idx_user_badge_earned_at (earned_at),
    INDEX idx_user_badge_user_badge (user_id, badge_id),
    
    -- 유니크 제약조건 (한 사용자가 같은 뱃지를 중복 획득하지 않도록)
    UNIQUE KEY uk_user_badge (user_id, badge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 설명
ALTER TABLE user_badge COMMENT = '사용자 뱃지 획득 정보';

-- 더미 데이터 삽입 (테스트용)
INSERT INTO user_badge (user_id, badge_id, earned_at, is_displayed, created_at, updated_at) VALUES
(1, 1, NOW() - INTERVAL 30 DAY, TRUE, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY),
(1, 2, NOW() - INTERVAL 25 DAY, TRUE, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY),
(1, 3, NOW() - INTERVAL 20 DAY, TRUE, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY),
(1, 7, NOW() - INTERVAL 15 DAY, TRUE, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY),
(1, 8, NOW() - INTERVAL 10 DAY, TRUE, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(1, 13, NOW() - INTERVAL 5 DAY, TRUE, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(1, 15, NOW() - INTERVAL 2 DAY, TRUE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);

-- 다른 사용자들의 더미 데이터
INSERT INTO user_badge (user_id, badge_id, earned_at, is_displayed, created_at, updated_at) VALUES
(2, 1, NOW() - INTERVAL 20 DAY, TRUE, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY),
(2, 2, NOW() - INTERVAL 15 DAY, TRUE, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY),
(2, 3, NOW() - INTERVAL 10 DAY, TRUE, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(3, 1, NOW() - INTERVAL 25 DAY, TRUE, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY),
(3, 7, NOW() - INTERVAL 18 DAY, TRUE, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY),
(3, 13, NOW() - INTERVAL 12 DAY, TRUE, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY); 