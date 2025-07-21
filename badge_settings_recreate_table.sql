-- 뱃지 설정 테이블 재생성 SQL
-- 기존 테이블 삭제
DROP TABLE IF EXISTS badge_settings;

-- 새 테이블 생성
CREATE TABLE badge_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_name VARCHAR(100) NOT NULL,
    badge_icon VARCHAR(10) NOT NULL,
    badge_description VARCHAR(500),
    attendance_count INT NULL COMMENT '출석 횟수',
    streak_count INT NULL COMMENT '연속출석 횟수',
    words_count INT NULL COMMENT '단어 학습 횟수',
    sentences_count INT NULL COMMENT '문장 학습 횟수',
    word_review_count INT NULL COMMENT '복습단어 횟수',
    sentence_review_count INT NULL COMMENT '복습문장 횟수',
    is_active BIT(1) NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL
);

-- 인덱스 추가
CREATE INDEX idx_badge_attendance ON badge_settings(attendance_count);
CREATE INDEX idx_badge_streak ON badge_settings(streak_count);
CREATE INDEX idx_badge_words ON badge_settings(words_count);
CREATE INDEX idx_badge_sentences ON badge_settings(sentences_count);
CREATE INDEX idx_badge_word_review ON badge_settings(word_review_count);
CREATE INDEX idx_badge_sentence_review ON badge_settings(sentence_review_count);

-- 기본 뱃지 데이터 삽입
INSERT INTO badge_settings (
    badge_name, 
    badge_icon, 
    badge_description, 
    attendance_count, 
    streak_count, 
    words_count, 
    sentences_count, 
    word_review_count, 
    sentence_review_count, 
    is_active, 
    display_order, 
    created_at, 
    updated_at
) VALUES 
-- 첫 걸음 뱃지 (출석)
(
    '첫 걸음', 
    '🎯', 
    '첫 번째 학습을 완료했습니다', 
    1, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    1, 
    NOW(), 
    NOW()
),
-- 열정 학습자 뱃지 (연속출석)
(
    '열정 학습자', 
    '🔥', 
    '연속 출석을 달성했습니다', 
    NULL, 
    7, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    2, 
    NOW(), 
    NOW()
),
-- 단어 마스터 뱃지 (단어)
(
    '단어 마스터', 
    '📚', 
    '단어 학습을 달성했습니다', 
    NULL, 
    NULL, 
    100, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    3, 
    NOW(), 
    NOW()
),
-- 골드 마스터 뱃지 (문장)
(
    '골드 마스터', 
    '🏆', 
    '문장 학습을 달성했습니다', 
    NULL, 
    NULL, 
    NULL, 
    50, 
    NULL, 
    NULL, 
    1, 
    4, 
    NOW(), 
    NOW()
),
-- 전설 수집가 뱃지 (복습단어)
(
    '전설 수집가', 
    '⭐', 
    '복습을 달성했습니다', 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    200, 
    NULL, 
    1, 
    5, 
    NOW(), 
    NOW()
),
-- 문장 복습 마스터 뱃지 (복습문장)
(
    '문장 복습 마스터', 
    '💎', 
    '문장 복습을 달성했습니다', 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    100, 
    1, 
    6, 
    NOW(), 
    NOW()
),
-- 출석 왕 뱃지 (출석)
(
    '출석 왕', 
    '👑', 
    '많은 출석을 달성했습니다', 
    30, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    7, 
    NOW(), 
    NOW()
),
-- 연속 출석 달인 뱃지 (연속출석)
(
    '연속 출석 달인', 
    '🔥🔥', 
    '긴 연속 출석을 달성했습니다', 
    NULL, 
    30, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    8, 
    NOW(), 
    NOW()
),
-- 단어 달인 뱃지 (단어)
(
    '단어 달인', 
    '📖', 
    '많은 단어를 학습했습니다', 
    NULL, 
    NULL, 
    500, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    9, 
    NOW(), 
    NOW()
),
-- 문장 달인 뱃지 (문장)
(
    '문장 달인', 
    '📝', 
    '많은 문장을 학습했습니다', 
    NULL, 
    NULL, 
    NULL, 
    200, 
    NULL, 
    NULL, 
    1, 
    10, 
    NOW(), 
    NOW()
),
-- 복습 단어 달인 뱃지 (복습단어)
(
    '복습 단어 달인', 
    '🔄', 
    '많은 단어를 복습했습니다', 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    500, 
    NULL, 
    1, 
    11, 
    NOW(), 
    NOW()
),
-- 복습 문장 달인 뱃지 (복습문장)
(
    '복습 문장 달인', 
    '🔄📝', 
    '많은 문장을 복습했습니다', 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    300, 
    1, 
    12, 
    NOW(), 
    NOW()
),
-- 완벽한 학습자 뱃지 (출석 + 단어)
(
    '완벽한 학습자', 
    '🌟', 
    '출석과 단어 학습을 모두 달성했습니다', 
    10, 
    NULL, 
    50, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    13, 
    NOW(), 
    NOW()
),
-- 문장 마스터 뱃지 (문장 + 복습문장)
(
    '문장 마스터', 
    '📚🏆', 
    '문장 학습과 복습을 모두 달성했습니다', 
    NULL, 
    NULL, 
    NULL, 
    100, 
    NULL, 
    50, 
    1, 
    14, 
    NOW(), 
    NOW()
),
-- 꾸준한 학습자 뱃지 (연속출석 + 단어)
(
    '꾸준한 학습자', 
    '📈', 
    '연속 출석과 단어 학습을 달성했습니다', 
    NULL, 
    14, 
    200, 
    NULL, 
    NULL, 
    NULL, 
    1, 
    15, 
    NOW(), 
    NOW()
);

-- 결과 확인
SELECT '뱃지 설정 테이블이 성공적으로 생성되었습니다!' AS message;
SELECT COUNT(*) AS total_badges FROM badge_settings; 