-- =====================================================================
-- 盆底康复管理平台 管理员初始账号
-- 登录手机号：13900000000    密码：123456
-- 说明：
--   1. 手机号密文/HMAC 由后端同款密钥生成（pelvic.encrypt.aes-key / hmac-key，
--      application.yml 开发默认值；若已改密钥，请用后端加密组件重新生成）
--   2. 密码哈希为 BCrypt（spring-security 默认编码器）
--   3. 同时创建医生档案（编号 1 → D001，科室=盆底康复科）并授予 ADMIN 角色
-- 前置：已执行 数据库建表语句.sql + 数据库种子数据.sql（科室ID=1、角色ID=1）
-- =====================================================================

USE `pelvic_rehab`;

INSERT INTO `account` (`账号ID`, `手机号密文`, `手机号HMAC`, `账号编号`, `昵称`, `账号状态`, `注册来源`)
VALUES (1, '8r+L+CYUKSASsQix32WtQhxmuzaxB0/oQnaZ0VwpZdk=',
           '117be33fc4983ea872ffa5bd0e8b573d0a4ff75841ba1186bb260c80f8f5a8cf',
           'ACC000001', '系统管理员', 'NORMAL', 'ADMIN');

INSERT INTO `account_credential` (`账号ID`, `密码哈希`, `密码更新时间`)
VALUES (1, '$2a$10$lHcEqo1zQ.q5qfsJlfuPnOnpePSlR//Isb5WKM9OKwZ2uU2RnZYMC', NOW());

INSERT INTO `sys_user_role` (`账号ID`, `角色ID`, `授权时间`)
VALUES (1, 1, NOW());

INSERT INTO `doctor` (`账号ID`, `编号`, `科室ID`, `姓名`, `性别`, `职称`, `审核状态`, `审核通过时间`, `审核人账号ID`)
VALUES (1, 1, 1, '系统管理员', '男', '主任医师', '审核通过', NOW(), 1);
