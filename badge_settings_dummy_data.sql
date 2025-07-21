-- 뱃지 설정 더미 데이터 INSERT 쿼리
-- 기존 데이터 삭제 (필요시)
-- DELETE FROM badge_settings;

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
    true, 
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
    true, 
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
    true, 
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
    true, 
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
    true, 
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
    true, 
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
    true, 
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
    true, 
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
    true, 
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
    true, 
    10, 
    NOW(), 
    NOW()
); 