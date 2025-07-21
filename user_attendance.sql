-- 사용자 출석 테이블 생성
CREATE TABLE IF NOT EXISTS user_attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    attendance_date DATE NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 인덱스
    INDEX idx_user_attendance_user_id (user_id),
    INDEX idx_user_attendance_date (attendance_date),
    INDEX idx_user_attendance_email (email),
    INDEX idx_user_attendance_user_date (user_id, attendance_date),
    
    -- 외래키 제약조건
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 테이블 설명
COMMENT ON TABLE user_attendance IS '사용자 출석 기록 테이블';
COMMENT ON COLUMN user_attendance.id IS '출석 기록 고유 ID';
COMMENT ON COLUMN user_attendance.user_id IS '사용자 ID';
COMMENT ON COLUMN user_attendance.email IS '사용자 이메일';
COMMENT ON COLUMN user_attendance.attendance_date IS '출석 날짜';
COMMENT ON COLUMN user_attendance.ip_address IS '접속 IP 주소';
COMMENT ON COLUMN user_attendance.device_type IS '디바이스 타입 (PC, Tablet, Mobile)';
COMMENT ON COLUMN user_attendance.user_agent IS '브라우저 User-Agent 정보';
COMMENT ON COLUMN user_attendance.created_at IS '출석 기록 생성 시간'; 