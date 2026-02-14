-- SQL Dialect: MySQL

-- User 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(10) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_name VARCHAR(30) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL DEFAULT 'MALE',
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    third_party_consent BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    plan VARCHAR(20) NOT NULL DEFAULT 'FREE',
    trust_score INT NOT NULL DEFAULT 50,
    login_id_updated_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- SocialAccount 테이블
CREATE TABLE social_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tag 테이블
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Post 테이블
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    situation TEXT NOT NULL,
    action TEXT NOT NULL,
    retrospective TEXT NOT NULL,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    required_token INT DEFAULT 0,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_user FOREIGN KEY (author_id) REFERENCES users(id)
);

-- PostTag 테이블
CREATE TABLE post_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post(id),
    CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id),
    UNIQUE (post_id, tag_id) -- 한 게시물에 동일한 태그가 중복 생성되는 것 방지
);

-- Report 테이블
CREATE TABLE report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_post_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    details TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_report_post FOREIGN KEY (target_post_id) REFERENCES post(id),
    CONSTRAINT fk_report_target_user FOREIGN KEY (target_user_id) REFERENCES users(id)
);

-- AdminAction 테이블
CREATE TABLE admin_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT,
    admin_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    penalty_score INT DEFAULT 0,
    reason TEXT NOT NULL,
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_action_report FOREIGN KEY (report_id) REFERENCES report(id),
    CONSTRAINT fk_admin_action_admin FOREIGN KEY (admin_id) REFERENCES users(id),
    CONSTRAINT fk_admin_action_target_user FOREIGN KEY (target_user_id) REFERENCES users(id)
);

-- TokenBalance 테이블
CREATE TABLE token_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_balance_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- TokenHistory 테이블
CREATE TABLE token_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'CHARGE',
    amount INT NOT NULL DEFAULT 0,
    balance_after INT NOT NULL DEFAULT 0,
    description VARCHAR(255), -- 상세 사유 (예: 출석체크 보상)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);