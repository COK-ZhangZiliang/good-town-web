--用户信息表
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_type TINYINT NOT NULL DEFAULT 0 CHECK (user_type IN (0, 1)),
    name VARCHAR(100) NOT NULL,
    id_type VARCHAR(50),
    id_number VARCHAR(50),
    phone VARCHAR(11),
    bio TEXT,
    avatar_url VARCHAR(2550) DEFAULT NULL,  -- 新增头像 URL 字段，默认为 NULL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

--乡镇信息表
CREATE TABLE towns (
    town_id INT AUTO_INCREMENT PRIMARY KEY,
    town_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL
);

--我宣传信息表
CREATE TABLE publicity (
    publicity_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    town_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(2550),
    video_url VARCHAR(2550),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 0 CHECK (status IN (0, -1)),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (town_id) REFERENCES towns(town_id)
);

--我助力信息表
CREATE TABLE assistance (
    assistance_id INT AUTO_INCREMENT PRIMARY KEY,
    publicity_id INT NOT NULL,
    user_id INT NOT NULL,
    description TEXT,
    image_url VARCHAR(2550),
    video_url VARCHAR(2550),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT NOT NULL CHECK (status IN (0, 1, 2, 3)),
    FOREIGN KEY (publicity_id) REFERENCES publicity(publicity_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

--助力成功明细表
CREATE TABLE assistance_success (
    success_id INT AUTO_INCREMENT PRIMARY KEY,
    publicity_id INT NOT NULL,
    publicity_user_id INT NOT NULL,
    assistance_id INT NOT NULL,
    assistance_user_id INT NOT NULL,
    accepted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publicity_id) REFERENCES publicity(publicity_id),
    FOREIGN KEY (publicity_user_id) REFERENCES users(user_id),
    FOREIGN KEY (assistance_id) REFERENCES assistance(assistance_id),
    FOREIGN KEY (assistance_user_id) REFERENCES users(user_id)
);

--助力成功月汇总表 
CREATE TABLE monthly_summary (
    summary_id INT AUTO_INCREMENT PRIMARY KEY,
    month CHAR(6) NOT NULL,
    region VARCHAR(200) NOT NULL,
    town_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    total_publicity_users INT DEFAULT 0,
    total_assistance_users INT DEFAULT 0,
    FOREIGN KEY (town_id) REFERENCES towns(town_id)
);