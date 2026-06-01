CREATE DATABASE unsocial;
show databases;
USE unsocial;
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
SHOW TABLES;
INSERT INTO users(name,email,password,phone)
VALUES(
'Khushabu',
'khushabu@gmail.com',
'123456',
'9876543210'
);

SELECT * FROM users;