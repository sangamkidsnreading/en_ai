-- 뱃지 설정 테이블 구조 변경 SQL (MySQL 호환 버전)
-- 기존 테이블 백업 (선택사항)
-- CREATE TABLE badge_settings_backup AS SELECT * FROM badge_settings;

-- 새로운 컬럼 추가 (기존 컬럼이 있다면 에러가 발생할 수 있음)
ALTER TABLE badge_settings ADD COLUMN attendance_count INT NULL COMMENT '출석 횟수';
ALTER TABLE badge_settings ADD COLUMN streak_count INT NULL COMMENT '연속출석 횟수';
ALTER TABLE badge_settings ADD COLUMN words_count INT NULL COMMENT '단어 학습 횟수';
ALTER TABLE badge_settings ADD COLUMN sentences_count INT NULL COMMENT '문장 학습 횟수';
ALTER TABLE badge_settings ADD COLUMN word_review_count INT NULL COMMENT '복습단어 횟수';
ALTER TABLE badge_settings ADD COLUMN sentence_review_count INT NULL COMMENT '복습문장 횟수';

-- 인덱스 추가 (성능 향상을 위해)
CREATE INDEX idx_badge_attendance ON badge_settings(attendance_count);
CREATE INDEX idx_badge_streak ON badge_settings(streak_count);
CREATE INDEX idx_badge_words ON badge_settings(words_count);
CREATE INDEX idx_badge_sentences ON badge_settings(sentences_count);
CREATE INDEX idx_badge_word_review ON badge_settings(word_review_count);
CREATE INDEX idx_badge_sentence_review ON badge_settings(sentence_review_count);

-- 기존 컬럼 삭제 (새 컬럼 추가 후 실행)
-- ALTER TABLE badge_settings DROP COLUMN badge_type;
-- ALTER TABLE badge_settings DROP COLUMN required_count; 