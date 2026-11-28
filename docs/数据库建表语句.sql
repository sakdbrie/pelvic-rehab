-- =====================================================================
-- 盆底康复管理平台（pelvic-rehab）数据库建表语句
-- 依据：docs/ER图.puml（最终版）、docs/数据库设计说明README.md
-- 目标：MySQL 8.0（InnoDB，utf8mb4）
--
-- 通用约定：
--   1. 主键统一 BIGINT AUTO_INCREMENT（联合主键除外）
--   2. 时间字段使用 DATETIME；创建时间默认当前时间，更新时间自动维护
--   3. 枚举字段使用 ENUM，取值与 ER 图一致
--   4. 布尔/逻辑删除字段使用 TINYINT(1)，默认 0（1=是/已删除）
--   5. 表名/字段名与 ER 图一致（中文标识符，统一用反引号）
--   6. 外键按 ER 图 <<FK>> 标记建立；auth_sms_code 按设计不设账号外键
--   7. ER 未标注 <<NULL>> 的字段默认 NOT NULL；
--      个别业务渐进填写或创建时无法提供的字段（昵称、住院号、备注、健康档案
--      多数字段、量表得分等）允许 NULL，已在各表列上标注
-- 生成日期：2026-11-28
-- =====================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `pelvic_rehab`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `pelvic_rehab`;

-- =====================================================================
-- 模块一：账号与人员模块（账号基础模块）
-- account、account_credential、auth_sms_code、auth_session、department、patient、doctor
-- =====================================================================

CREATE TABLE IF NOT EXISTS `account` (
  `账号ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '账号ID（PK）',
  `手机号密文`    VARCHAR(255) NOT NULL COMMENT '手机号加密存储（唯一登录身份）',
  `手机号HMAC`   VARCHAR(128) NOT NULL COMMENT '手机号HMAC（查询/比对，不存明文）',
  `账号编号`     VARCHAR(64)  NOT NULL COMMENT '账号编号（UK）',
  `昵称`        VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  `账号状态`     ENUM('PENDING','NORMAL','DISABLED','CANCELLED') NOT NULL DEFAULT 'NORMAL' COMMENT '账号状态',
  `注册来源`     ENUM('MINI_PROGRAM','WEB','ADMIN') NOT NULL COMMENT '注册来源',
  `最后登录时间`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`账号ID`),
  UNIQUE KEY `uk_account_no` (`账号编号`),
  KEY `idx_account_phone_hmac` (`手机号HMAC`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一账号主体';

CREATE TABLE IF NOT EXISTS `account_credential` (
  `凭证ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '凭证ID（PK）',
  `账号ID`       BIGINT       NOT NULL COMMENT '账号ID（FK，UK：一账号最多一条）',
  `密码哈希`     VARCHAR(255) NOT NULL COMMENT '密码哈希（不存明文）',
  `密码更新时间`  DATETIME     DEFAULT NULL COMMENT '密码更新时间',
  `连续失败次数`  INT          NOT NULL DEFAULT 0 COMMENT '连续失败次数',
  `锁定截止时间`  DATETIME     DEFAULT NULL COMMENT '锁定截止时间',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`凭证ID`),
  UNIQUE KEY `uk_credential_account` (`账号ID`),
  CONSTRAINT `fk_credential_account` FOREIGN KEY (`账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网页密码凭证（登录名即手机号，存于 account）';

CREATE TABLE IF NOT EXISTS `auth_sms_code` (
  `验证码ID`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '验证码ID（PK）',
  `手机号密文`    VARCHAR(255) NOT NULL COMMENT '手机号密文',
  `手机号HMAC`   VARCHAR(128) NOT NULL COMMENT '手机号HMAC（按手机号关联业务）',
  `使用场景`     ENUM('REGISTER','LOGIN','BIND_PHONE','RESET_PASSWORD') NOT NULL COMMENT '使用场景',
  `验证码哈希`   VARCHAR(255) NOT NULL COMMENT '验证码哈希（不存明文）',
  `过期时间`     DATETIME     NOT NULL COMMENT '过期时间',
  `使用时间`     DATETIME     DEFAULT NULL COMMENT '使用时间（NULL=未使用，非空=已消费，防重放）',
  `验证失败次数`  INT          NOT NULL DEFAULT 0 COMMENT '验证失败次数',
  `发送IP`      VARCHAR(45)  DEFAULT NULL COMMENT '发送IP',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`验证码ID`),
  KEY `idx_sms_phone_scene` (`手机号HMAC`, `使用场景`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码（独立保存，不设账号外键）';

CREATE TABLE IF NOT EXISTS `auth_session` (
  `会话ID`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID（PK）',
  `账号ID`        BIGINT       NOT NULL COMMENT '账号ID（FK）',
  `客户端类型`     ENUM('WEB_ADMIN','MINI_DOCTOR','MINI_PATIENT') NOT NULL COMMENT '客户端类型',
  `AccessToken哈希`  VARCHAR(255) NOT NULL COMMENT 'AccessToken哈希',
  `RefreshToken哈希` VARCHAR(255) NOT NULL COMMENT 'RefreshToken哈希',
  `过期时间`      DATETIME     NOT NULL COMMENT '过期时间',
  `撤销时间`      DATETIME     DEFAULT NULL COMMENT '撤销时间',
  `最后活跃时间`   DATETIME     DEFAULT NULL COMMENT '最后活跃时间',
  `创建时间`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`会话ID`),
  KEY `idx_session_account` (`账号ID`),
  KEY `idx_session_token` (`AccessToken哈希`),
  CONSTRAINT `fk_session_account` FOREIGN KEY (`账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录会话';

CREATE TABLE IF NOT EXISTS `department` (
  `科室ID`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '科室ID（PK）',
  `科室名称`  VARCHAR(100) NOT NULL COMMENT '科室名称（UK）',
  `科室说明`  VARCHAR(255) DEFAULT NULL COMMENT '科室说明',
  PRIMARY KEY (`科室ID`),
  UNIQUE KEY `uk_department_name` (`科室名称`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室';

CREATE TABLE IF NOT EXISTS `patient` (
  `患者ID`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '患者ID（PK）',
  `账号ID`     BIGINT       NOT NULL COMMENT '账号ID（FK，UK）',
  `患者编号`   VARCHAR(32)  NOT NULL COMMENT '患者编号（UK）',
  `住院号`     VARCHAR(50)  DEFAULT NULL COMMENT '住院号',
  `姓名`      VARCHAR(50)  NOT NULL COMMENT '姓名',
  `性别`      ENUM('男','女') NOT NULL COMMENT '性别',
  `出生年月`   DATE         DEFAULT NULL COMMENT '出生年月',
  `身高cm`    DECIMAL(5,2) DEFAULT NULL COMMENT '身高cm',
  `体重kg`    DECIMAL(5,2) DEFAULT NULL COMMENT '体重kg',
  `婚姻状况`   VARCHAR(20)  DEFAULT NULL COMMENT '婚姻状况',
  `教育程度`   VARCHAR(30)  DEFAULT NULL COMMENT '教育程度',
  `从事职业`   VARCHAR(50)  DEFAULT NULL COMMENT '从事职业',
  `备注`      TEXT         DEFAULT NULL COMMENT '备注',
  `是否删除`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`患者ID`),
  UNIQUE KEY `uk_patient_account` (`账号ID`),
  UNIQUE KEY `uk_patient_no` (`患者编号`),
  KEY `idx_patient_name` (`姓名`),
  CONSTRAINT `fk_patient_account` FOREIGN KEY (`账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者档案';

CREATE TABLE IF NOT EXISTS `doctor` (
  `医生ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '医生ID（PK）',
  `账号ID`       BIGINT       NOT NULL COMMENT '账号ID（FK，UK）',
  `编号`        BIGINT       NOT NULL COMMENT '医生编号（UK）',
  `科室ID`       BIGINT       NOT NULL COMMENT '科室ID（FK）',
  `姓名`        VARCHAR(50)  NOT NULL COMMENT '姓名',
  `性别`        ENUM('男','女') NOT NULL COMMENT '性别',
  `职称`        VARCHAR(50)  DEFAULT NULL COMMENT '职称',
  `审核状态`     ENUM('待审核','审核通过','审核未通过') NOT NULL DEFAULT '待审核' COMMENT '审核状态',
  `审核通过时间`  DATETIME     DEFAULT NULL COMMENT '审核通过时间',
  `审核人账号ID`  BIGINT       DEFAULT NULL COMMENT '审核人账号ID（FK）',
  `通过或拒绝原因` VARCHAR(255) DEFAULT NULL COMMENT '通过或拒绝原因',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`医生ID`),
  UNIQUE KEY `uk_doctor_account` (`账号ID`),
  UNIQUE KEY `uk_doctor_no` (`编号`),
  KEY `idx_doctor_name` (`姓名`),
  CONSTRAINT `fk_doctor_account` FOREIGN KEY (`账号ID`) REFERENCES `account` (`账号ID`),
  CONSTRAINT `fk_doctor_department` FOREIGN KEY (`科室ID`) REFERENCES `department` (`科室ID`),
  CONSTRAINT `fk_doctor_reviewer` FOREIGN KEY (`审核人账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生档案';

-- =====================================================================
-- 模块二：角色权限与访问审计模块
-- sys_role、sys_permission、sys_user_role、sys_role_permission、sys_operate_log、sys_login_log
-- =====================================================================

CREATE TABLE IF NOT EXISTS `sys_role` (
  `角色ID`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID（PK）',
  `角色编码`   VARCHAR(50)  NOT NULL COMMENT '角色编码（UK）',
  `角色名称`   VARCHAR(100) NOT NULL COMMENT '角色名称',
  `数据范围`   ENUM('SELF','ALL_PATIENTS','DEPARTMENT','ASSIGNED') NOT NULL DEFAULT 'SELF' COMMENT '数据范围',
  `角色状态`   ENUM('启用','停用') NOT NULL DEFAULT '启用' COMMENT '角色状态',
  `创建时间`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`角色ID`),
  UNIQUE KEY `uk_role_code` (`角色编码`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `权限ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID（PK）',
  `权限编码`     VARCHAR(100) NOT NULL COMMENT '权限编码（UK）',
  `权限名称`     VARCHAR(100) NOT NULL COMMENT '权限名称',
  `权限类型`     ENUM('MENU','BUTTON','API') NOT NULL COMMENT '权限类型',
  `父级权限ID`   BIGINT       DEFAULT NULL COMMENT '父级权限ID（FK，自关联）',
  `所属模块`     VARCHAR(50)  DEFAULT NULL COMMENT '所属模块',
  `操作类型`     ENUM('VIEW','CREATE','UPDATE','DELETE','EXPORT','REVIEW') DEFAULT NULL COMMENT '操作类型',
  `前端路由地址`  VARCHAR(255) DEFAULT NULL COMMENT '前端路由地址',
  `同级排序号`   SMALLINT     NOT NULL DEFAULT 0 COMMENT '同级排序号',
  `权限说明`     VARCHAR(255) DEFAULT NULL COMMENT '权限说明',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`权限ID`),
  UNIQUE KEY `uk_permission_code` (`权限编码`),
  KEY `idx_permission_parent` (`父级权限ID`),
  CONSTRAINT `fk_permission_parent` FOREIGN KEY (`父级权限ID`) REFERENCES `sys_permission` (`权限ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='功能权限（菜单/按钮/API 权限树）';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `账号角色关联ID` BIGINT   NOT NULL AUTO_INCREMENT COMMENT '账号角色关联ID（PK）',
  `账号ID`       BIGINT   NOT NULL COMMENT '账号ID（FK）',
  `角色ID`       BIGINT   NOT NULL COMMENT '角色ID（FK）',
  `授权人账号ID`  BIGINT   DEFAULT NULL COMMENT '授权人账号ID（FK）',
  `授权时间`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `创建时间`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`账号角色关联ID`),
  UNIQUE KEY `uk_user_role` (`账号ID`, `角色ID`),
  CONSTRAINT `fk_user_role_account` FOREIGN KEY (`账号ID`) REFERENCES `account` (`账号ID`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`角色ID`) REFERENCES `sys_role` (`角色ID`),
  CONSTRAINT `fk_user_role_grantor` FOREIGN KEY (`授权人账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号角色关联';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `角色权限关联ID` BIGINT   NOT NULL AUTO_INCREMENT COMMENT '角色权限关联ID（PK）',
  `角色ID`       BIGINT   NOT NULL COMMENT '角色ID（FK）',
  `权限ID`       BIGINT   NOT NULL COMMENT '权限ID（FK）',
  `创建时间`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`角色权限关联ID`),
  UNIQUE KEY `uk_role_permission` (`角色ID`, `权限ID`),
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`角色ID`) REFERENCES `sys_role` (`角色ID`),
  CONSTRAINT `fk_role_permission_perm` FOREIGN KEY (`权限ID`) REFERENCES `sys_permission` (`权限ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联';

CREATE TABLE IF NOT EXISTS `sys_operate_log` (
  `日志ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID（PK）',
  `操作账号ID`   BIGINT       NOT NULL COMMENT '操作账号ID（FK）',
  `关联患者ID`   BIGINT       DEFAULT NULL COMMENT '关联患者ID（FK）',
  `业务模块`     VARCHAR(50)  DEFAULT NULL COMMENT '业务模块',
  `资源类型`     VARCHAR(50)  DEFAULT NULL COMMENT '资源类型',
  `资源ID`      VARCHAR(64)  DEFAULT NULL COMMENT '资源ID',
  `操作类型`     ENUM('VIEW','CREATE','UPDATE','DELETE','EXPORT','LOGIN','OTHER') NOT NULL COMMENT '操作类型',
  `操作结果`     ENUM('SUCCESS','DENIED','FAILED') NOT NULL COMMENT '操作结果',
  `操作原因`     VARCHAR(255) DEFAULT NULL COMMENT '操作原因',
  `请求方法`     ENUM('GET','POST','PUT','DELETE') DEFAULT NULL COMMENT '请求方法',
  `请求地址`     VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
  `请求参数`     TEXT         DEFAULT NULL COMMENT '请求参数',
  `耗时毫秒`     INT          DEFAULT NULL COMMENT '耗时毫秒',
  `IP地址`      VARCHAR(45)  DEFAULT NULL COMMENT 'IP地址',
  `userAgent`   VARCHAR(512) DEFAULT NULL COMMENT 'userAgent',
  `操作时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`日志ID`),
  KEY `idx_oplog_account` (`操作账号ID`),
  KEY `idx_oplog_patient` (`关联患者ID`),
  KEY `idx_oplog_time` (`操作时间`),
  CONSTRAINT `fk_oplog_account` FOREIGN KEY (`操作账号ID`) REFERENCES `account` (`账号ID`),
  CONSTRAINT `fk_oplog_patient` FOREIGN KEY (`关联患者ID`) REFERENCES `patient` (`患者ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `登录日志ID`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '登录日志ID（PK）',
  `账号ID`      BIGINT       DEFAULT NULL COMMENT '账号ID（FK，登录失败可能为空）',
  `登录方式`     ENUM('SMS_CODE','PASSWORD') NOT NULL COMMENT '登录方式',
  `客户端类型`   ENUM('WEB_ADMIN','MINI_DOCTOR','MINI_PATIENT') NOT NULL COMMENT '客户端类型',
  `登录结果`     ENUM('SUCCESS','FAILED') NOT NULL COMMENT '登录结果',
  `失败原因`     VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `IP地址`      VARCHAR(45)  DEFAULT NULL COMMENT 'IP地址',
  `UserAgent`   VARCHAR(512) DEFAULT NULL COMMENT 'UserAgent',
  `登录时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`登录日志ID`),
  KEY `idx_loginlog_account` (`账号ID`),
  KEY `idx_loginlog_time` (`登录时间`),
  CONSTRAINT `fk_loginlog_account` FOREIGN KEY (`账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录审计日志';

-- =====================================================================
-- 模块三：患者信息采集与病史模块
-- patient_health_profile、patient_conditions、surgery_history、pregnancy
-- =====================================================================

CREATE TABLE IF NOT EXISTS `patient_health_profile` (
  `健康档案ID`        BIGINT       NOT NULL COMMENT '健康档案ID（PK/FK，等于患者ID，一对一）',
  `患者ID`           BIGINT       NOT NULL COMMENT '患者ID（FK，UK）',
  `主诉`             TEXT         DEFAULT NULL COMMENT '主诉',
  `大便情况`         ENUM('正常','便秘','大便失禁') DEFAULT NULL COMMENT '大便情况',
  `小便情况`         ENUM('正常','尿痛','尿失禁','尿频尿急','尿潴留') DEFAULT NULL COMMENT '小便情况',
  `性生活频率次每周`  DECIMAL(5,2) DEFAULT NULL COMMENT '性生活频率（次/周）',
  `性生活频率次每月`  DECIMAL(5,2) DEFAULT NULL COMMENT '性生活频率（次/月）',
  `冶游史`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '冶游史（1=有）',
  `既往运动及生活补充` TEXT         DEFAULT NULL COMMENT '既往运动及生活补充',
  `是否绝经`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否绝经（1=是）',
  `月经持续天数`      SMALLINT     DEFAULT NULL COMMENT '月经持续天数',
  `月经周期天数`      SMALLINT     DEFAULT NULL COMMENT '月经周期天数',
  `痛经VAS`         TINYINT      DEFAULT NULL COMMENT '痛经VAS',
  `末次月经日期`      DATE         DEFAULT NULL COMMENT '末次月经日期',
  `绝经年龄`         TINYINT      DEFAULT NULL COMMENT '绝经年龄',
  `吸烟支每天`        DECIMAL(6,2) DEFAULT NULL COMMENT '吸烟（支/天）',
  `饮酒ml每天`        DECIMAL(8,2) DEFAULT NULL COMMENT '饮酒（ml/天）',
  `茶咖啡ml每天`      DECIMAL(8,2) DEFAULT NULL COMMENT '茶/咖啡（ml/天）',
  `久坐小时每天`      DECIMAL(4,1) DEFAULT NULL COMMENT '久坐（小时/天）',
  `久站小时每天`      DECIMAL(4,1) DEFAULT NULL COMMENT '久站（小时/天）',
  `其他习惯`         TEXT         DEFAULT NULL COMMENT '其他习惯',
  `逻辑删除`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`健康档案ID`),
  UNIQUE KEY `uk_profile_patient` (`患者ID`),
  CONSTRAINT `fk_profile_patient` FOREIGN KEY (`健康档案ID`) REFERENCES `patient` (`患者ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者健康档案（与患者一对一，仅保留最新资料）';

CREATE TABLE IF NOT EXISTS `patient_conditions` (
  `病史项目ID` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '病史项目ID（PK）',
  `患者ID`    BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `项目类别`   VARCHAR(40)  NOT NULL COMMENT '项目类别',
  `项目名称`   VARCHAR(100) NOT NULL COMMENT '项目名称',
  `详情`      TEXT         DEFAULT NULL COMMENT '详情',
  `创建时间`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`病史项目ID`),
  KEY `idx_conditions_patient` (`患者ID`),
  CONSTRAINT `fk_conditions_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='既往病史项目';

CREATE TABLE IF NOT EXISTS `surgery_history` (
  `手术史ID`  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '手术史ID（PK）',
  `患者ID`   BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `手术类别`  VARCHAR(50)  NOT NULL COMMENT '手术类别',
  `手术名称`  VARCHAR(255) NOT NULL COMMENT '手术名称',
  `手术日期`  DATE         DEFAULT NULL COMMENT '手术日期',
  `备注`     TEXT         DEFAULT NULL COMMENT '备注',
  `逻辑删除`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`手术史ID`),
  KEY `idx_surgery_patient` (`患者ID`),
  CONSTRAINT `fk_surgery_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='手术史';

CREATE TABLE IF NOT EXISTS `pregnancy` (
  `妊娠记录ID`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '妊娠记录ID（PK）',
  `患者ID`       BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `妊娠序号`     SMALLINT     NOT NULL COMMENT '妊娠序号',
  `妊娠结局`     VARCHAR(30)  DEFAULT NULL COMMENT '妊娠结局',
  `孕前体重kg`   DECIMAL(5,2) DEFAULT NULL COMMENT '孕前体重kg',
  `产前体重kg`   DECIMAL(5,2) DEFAULT NULL COMMENT '产前体重kg',
  `孕期增重kg`   DECIMAL(5,2) DEFAULT NULL COMMENT '孕期增重kg',
  `睡觉体位`     ENUM('左侧','右侧','仰卧') DEFAULT NULL COMMENT '睡觉体位',
  `尿失禁`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '尿失禁（1=有）',
  `大便情况`     ENUM('正常','失禁','便秘') DEFAULT NULL COMMENT '大便情况',
  `血压情况`     ENUM('高','低','正常') DEFAULT NULL COMMENT '血压情况',
  `血糖情况`     ENUM('高','低','正常') DEFAULT NULL COMMENT '血糖情况',
  `甲状腺功能`   ENUM('甲亢','甲减','正常') DEFAULT NULL COMMENT '甲状腺功能',
  `腰背痛VAS`    TINYINT      DEFAULT NULL COMMENT '腰背痛VAS',
  `耻骨疼痛VAS`  TINYINT      DEFAULT NULL COMMENT '耻骨疼痛VAS',
  `下肢疼痛VAS`  TINYINT      DEFAULT NULL COMMENT '下肢疼痛VAS',
  `妊娠反应等级`  VARCHAR(20)  DEFAULT NULL COMMENT '妊娠反应等级',
  `会阴瘙痒`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '会阴瘙痒（1=有）',
  `饮食情况`     VARCHAR(30)  DEFAULT NULL COMMENT '饮食情况',
  `性生活`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '孕期性生活（1=有）',
  `分娩日期`     DATE         DEFAULT NULL COMMENT '分娩日期（未分娩为空）',
  `分娩方式`     ENUM('顺产','剖宫产') DEFAULT NULL COMMENT '分娩方式（未分娩为空）',
  `会阴情况`     ENUM('会阴侧切','会阴撕裂','无损伤') DEFAULT NULL COMMENT '会阴情况（未分娩为空）',
  `无痛分娩`     TINYINT(1)   DEFAULT NULL COMMENT '无痛分娩（未分娩为空）',
  `头位正常分娩`  TINYINT(1)   DEFAULT NULL COMMENT '头位正常分娩（未分娩为空）',
  `助产方式`     ENUM('无','产钳','胎头吸引') DEFAULT NULL COMMENT '助产方式（未分娩为空）',
  `胎儿出生体重kg` DECIMAL(5,2) DEFAULT NULL COMMENT '胎儿出生体重kg（未分娩为空）',
  `备注`         TEXT         DEFAULT NULL COMMENT '备注',
  `逻辑删除`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`妊娠记录ID`),
  UNIQUE KEY `uk_pregnancy_patient_no` (`患者ID`, `妊娠序号`),
  CONSTRAINT `fk_pregnancy_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='妊娠记录（一行代表一次妊娠）';

-- =====================================================================
-- 模块四：临床检查与医务人员记录模块
-- examination、diastasis、postpartum、pain_assessment、pelvic_inspection、
-- general_inspection、special_exam、neurological_exam、anal_palpation、
-- clinical_note、clinical_attachment
-- =====================================================================

CREATE TABLE IF NOT EXISTS `examination` (
  `检查ID`        BIGINT   NOT NULL AUTO_INCREMENT COMMENT '检查ID（PK）',
  `患者ID`        BIGINT   NOT NULL COMMENT '患者ID（FK）',
  `录入医生ID`    BIGINT   NOT NULL COMMENT '录入医生ID（FK）',
  `最后修改医生ID` BIGINT   DEFAULT NULL COMMENT '最后修改医生ID（FK）',
  `检查时间`      DATETIME NOT NULL COMMENT '检查时间',
  `主诉`         TEXT     DEFAULT NULL COMMENT '主诉',
  `腹部张力`      ENUM('正常','增大','降低') DEFAULT NULL COMMENT '腹部张力',
  `诊断`         TEXT     DEFAULT NULL COMMENT '诊断',
  `药物治疗`      TEXT     DEFAULT NULL COMMENT '药物治疗',
  `其他补充`      TEXT     DEFAULT NULL COMMENT '其他补充',
  `创建时间`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`检查ID`),
  KEY `idx_examination_patient` (`患者ID`),
  CONSTRAINT `fk_examination_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_examination_doctor` FOREIGN KEY (`录入医生ID`) REFERENCES `doctor` (`医生ID`),
  CONSTRAINT `fk_examination_editor` FOREIGN KEY (`最后修改医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临床检查主信息';

CREATE TABLE IF NOT EXISTS `diastasis` (
  `评估ID`      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '评估ID（PK）',
  `检查ID`      BIGINT      NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `静息肚脐指宽` DECIMAL(4,1) NOT NULL COMMENT '静息肚脐指宽',
  `静息脐上指宽` DECIMAL(4,1) NOT NULL COMMENT '静息脐上指宽',
  `静息脐下指宽` DECIMAL(4,1) NOT NULL COMMENT '静息脐下指宽',
  `收缩肚脐指宽` DECIMAL(4,1) NOT NULL COMMENT '收缩肚脐指宽',
  `收缩脐上指宽` DECIMAL(4,1) NOT NULL COMMENT '收缩脐上指宽',
  `收缩脐下指宽` DECIMAL(4,1) NOT NULL COMMENT '收缩脐下指宽',
  PRIMARY KEY (`评估ID`),
  UNIQUE KEY `uk_diastasis_exam` (`检查ID`),
  CONSTRAINT `fk_diastasis_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='腹直肌分离评估';

CREATE TABLE IF NOT EXISTS `postpartum` (
  `评估ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评估ID（PK）',
  `检查ID`       BIGINT       NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `小便情况`     ENUM('无','压力性','急迫性','充溢性','混合性') NOT NULL DEFAULT '无' COMMENT '小便情况',
  `大便情况`     ENUM('正常','失禁','便秘') NOT NULL DEFAULT '正常' COMMENT '大便情况',
  `盆腔会阴痛VAS` TINYINT      NOT NULL DEFAULT 0 COMMENT '盆腔会阴痛VAS',
  `脏器脱垂`     VARCHAR(50)  DEFAULT NULL COMMENT '脏器脱垂',
  `瘢痕情况`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '瘢痕情况（1=有）',
  `瘢痕质地`     VARCHAR(100) DEFAULT NULL COMMENT '瘢痕质地',
  `瘢痕颜色`     VARCHAR(50)  DEFAULT NULL COMMENT '瘢痕颜色',
  `瘢痕时间`     DATE         DEFAULT NULL COMMENT '瘢痕时间',
  `瘢痕长度cm`   DECIMAL(5,2) DEFAULT NULL COMMENT '瘢痕长度cm',
  `尿道不稳定性`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '尿道不稳定性（1=有）',
  `其他`        TEXT         DEFAULT NULL COMMENT '其他',
  PRIMARY KEY (`评估ID`),
  UNIQUE KEY `uk_postpartum_exam` (`检查ID`),
  CONSTRAINT `fk_postpartum_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产后情况评估';

CREATE TABLE IF NOT EXISTS `pain_assessment` (
  `疼痛评估ID`   BIGINT  NOT NULL AUTO_INCREMENT COMMENT '疼痛评估ID（PK）',
  `检查ID`      BIGINT  NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `腰背痛左VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '腰背痛左VAS',
  `腰背痛右VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '腰背痛右VAS',
  `腰背痛中VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '腰背痛中VAS',
  `骶髂关节痛左VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '骶髂关节痛左VAS',
  `骶髂关节痛右VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '骶髂关节痛右VAS',
  `骶髂关节痛中VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '骶髂关节痛中VAS',
  `耻骨痛VAS`    TINYINT NOT NULL DEFAULT 0 COMMENT '耻骨痛VAS',
  `尾骨痛左VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '尾骨痛左VAS',
  `尾骨痛右VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '尾骨痛右VAS',
  `尾骨痛中VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '尾骨痛中VAS',
  `坐骨神经痛左VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '坐骨神经痛左VAS',
  `坐骨神经痛右VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '坐骨神经痛右VAS',
  `腹股沟痛左VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '腹股沟痛左VAS',
  `腹股沟痛右VAS` TINYINT NOT NULL DEFAULT 0 COMMENT '腹股沟痛右VAS',
  `颈痛左VAS`    TINYINT NOT NULL DEFAULT 0 COMMENT '颈痛左VAS',
  `颈痛右VAS`    TINYINT NOT NULL DEFAULT 0 COMMENT '颈痛右VAS',
  `上肢痛左VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '上肢痛左VAS',
  `上肢痛右VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '上肢痛右VAS',
  `下肢痛左VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '下肢痛左VAS',
  `下肢痛右VAS`  TINYINT NOT NULL DEFAULT 0 COMMENT '下肢痛右VAS',
  PRIMARY KEY (`疼痛评估ID`),
  UNIQUE KEY `uk_pain_exam` (`检查ID`),
  CONSTRAINT `fk_pain_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疼痛评估';

CREATE TABLE IF NOT EXISTS `pelvic_inspection` (
  `视诊ID`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '视诊ID（PK）',
  `检查ID`      BIGINT       NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `外阴情况`    VARCHAR(30)  NOT NULL COMMENT '外阴情况',
  `阴道裂口`    VARCHAR(20)  NOT NULL COMMENT '阴道裂口',
  `尿道口`      VARCHAR(20)  NOT NULL COMMENT '尿道口',
  `阴蒂`        VARCHAR(20)  NOT NULL COMMENT '阴蒂',
  `阴唇对称`    TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '阴唇对称（1=是）',
  `分泌物情况`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '分泌物情况（1=有）',
  `分泌物颜色`  VARCHAR(50)  DEFAULT NULL COMMENT '分泌物颜色',
  `分泌物气味`  VARCHAR(50)  DEFAULT NULL COMMENT '分泌物气味',
  `分泌物质地`  VARCHAR(50)  DEFAULT NULL COMMENT '分泌物质地',
  `分泌物量`    DECIMAL(5,2) DEFAULT NULL COMMENT '分泌物量',
  `阴毛情况`    VARCHAR(20)  NOT NULL COMMENT '阴毛情况',
  `痔疮情况`    VARCHAR(20)  NOT NULL COMMENT '痔疮情况',
  `Valsalva表现` VARCHAR(100) NOT NULL COMMENT 'Valsalva表现',
  PRIMARY KEY (`视诊ID`),
  UNIQUE KEY `uk_pelvic_exam` (`检查ID`),
  CONSTRAINT `fk_pelvic_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盆底视诊';

CREATE TABLE IF NOT EXISTS `general_inspection` (
  `视诊ID`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '视诊ID（PK）',
  `检查ID`        BIGINT       NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `精神面貌`      VARCHAR(20)  NOT NULL COMMENT '精神面貌',
  `步行情况`      VARCHAR(20)  NOT NULL COMMENT '步行情况',
  `步行异常描述`  TEXT         DEFAULT NULL COMMENT '步行异常描述',
  `畸形部位`      VARCHAR(255) DEFAULT NULL COMMENT '畸形部位',
  `肿胀部位`      VARCHAR(255) DEFAULT NULL COMMENT '肿胀部位',
  `疤痕部位及大小` TEXT         DEFAULT NULL COMMENT '疤痕部位及大小',
  `瘀斑部位及大小` TEXT         DEFAULT NULL COMMENT '瘀斑部位及大小',
  `肛周生殖器肤色` VARCHAR(20)  NOT NULL COMMENT '肛周生殖器肤色',
  `肛周皮疹分泌物` TEXT         DEFAULT NULL COMMENT '肛周皮疹分泌物',
  `毛发分布`      VARCHAR(20)  NOT NULL COMMENT '毛发分布',
  `直肠脱垂长度cm` DECIMAL(5,2) DEFAULT NULL COMMENT '直肠脱垂长度cm',
  `痔疮情况`      VARCHAR(20)  NOT NULL COMMENT '痔疮情况',
  PRIMARY KEY (`视诊ID`),
  UNIQUE KEY `uk_general_exam` (`检查ID`),
  CONSTRAINT `fk_general_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一般视诊';

CREATE TABLE IF NOT EXISTS `special_exam` (
  `特殊检查ID`    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '特殊检查ID（PK）',
  `检查ID`       BIGINT      NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `泌尿生殖测试`  VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '泌尿生殖测试（+/-）',
  `疤痕试验`     VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '疤痕试验（+/-）',
  `4字试验左`    VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '4字试验左（+/-）',
  `4字试验右`    VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '4字试验右（+/-）',
  `骨盆分离试验`  VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '骨盆分离试验（+/-）',
  `骨盆挤压试验`  VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '骨盆挤压试验（+/-）',
  `骶骨下压试验`  VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '骶骨下压试验（+/-）',
  `股骨冲击试验`  VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '股骨冲击试验（+/-）',
  `直腿抬高试验`  VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '直腿抬高试验（+/-）',
  `直腿抬高加强试验` VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '直腿抬高加强试验（+/-）',
  `广泛性触诊试验` VARCHAR(10) NOT NULL DEFAULT '-' COMMENT '广泛性触诊试验（+/-）',
  `其他`         TEXT        DEFAULT NULL COMMENT '其他',
  PRIMARY KEY (`特殊检查ID`),
  UNIQUE KEY `uk_special_exam` (`检查ID`),
  CONSTRAINT `fk_special_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='特殊检查';

CREATE TABLE IF NOT EXISTS `neurological_exam` (
  `查体ID`   BIGINT                        NOT NULL AUTO_INCREMENT COMMENT '查体ID（PK）',
  `检查ID`   BIGINT                        NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `盆底张力`  ENUM('正常','增高','降低')      NOT NULL COMMENT '盆底张力',
  `左侧感觉`  ENUM('正常','减弱','过敏')      NOT NULL COMMENT '左侧感觉',
  `右侧感觉`  ENUM('正常','减弱','过敏')      NOT NULL COMMENT '右侧感觉',
  `导尿管`   TINYINT(1)                    NOT NULL DEFAULT 0 COMMENT '导尿管（1=有）',
  PRIMARY KEY (`查体ID`),
  UNIQUE KEY `uk_nerve_exam` (`检查ID`),
  CONSTRAINT `fk_nerve_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='神经损伤查体';

CREATE TABLE IF NOT EXISTS `anal_palpation` (
  `触诊ID`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '触诊ID（PK）',
  `检查ID`        BIGINT      NOT NULL COMMENT '检查ID（FK，UK：每检查最多一条）',
  `肛提肌`        VARCHAR(30) NOT NULL COMMENT '肛提肌',
  `坐尾肌`        VARCHAR(30) NOT NULL COMMENT '坐尾肌',
  `髂尾肌`        VARCHAR(30) NOT NULL COMMENT '髂尾肌',
  `闭孔内肌`      VARCHAR(30) NOT NULL COMMENT '闭孔内肌',
  `脏器偏移`      VARCHAR(30) NOT NULL COMMENT '脏器偏移',
  `牛津肌力`      TINYINT     DEFAULT NULL COMMENT '牛津肌力（级）',
  `尾骨嵌顿`      TINYINT(1)  DEFAULT NULL COMMENT '尾骨嵌顿（1=有）',
  `肛周温度`      VARCHAR(20) DEFAULT NULL COMMENT '肛周温度',
  `肛周湿度`      VARCHAR(20) DEFAULT NULL COMMENT '肛周湿度',
  `会阴及肛周肌肉张力` VARCHAR(20) DEFAULT NULL COMMENT '会阴及肛周肌肉张力',
  PRIMARY KEY (`触诊ID`),
  UNIQUE KEY `uk_palpation_exam` (`检查ID`),
  CONSTRAINT `fk_palpation_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='肛门触诊';

CREATE TABLE IF NOT EXISTS `clinical_note` (
  `临床记录ID` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '临床记录ID（PK）',
  `患者ID`    BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `检查ID`    BIGINT       DEFAULT NULL COMMENT '检查ID（FK）',
  `医生ID`    BIGINT       NOT NULL COMMENT '医生ID（FK）',
  `记录类型`  ENUM('外院资料','阶段评估','特殊情况','其他') NOT NULL COMMENT '记录类型',
  `记录内容`  TEXT         NOT NULL COMMENT '记录内容',
  `可见范围`  ENUM('医务人员') NOT NULL DEFAULT '医务人员' COMMENT '可见范围',
  `创建时间`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`临床记录ID`),
  KEY `idx_note_patient` (`患者ID`),
  KEY `idx_note_exam` (`检查ID`),
  CONSTRAINT `fk_note_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_note_exam` FOREIGN KEY (`检查ID`) REFERENCES `examination` (`检查ID`),
  CONSTRAINT `fk_note_doctor` FOREIGN KEY (`医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临床补充记录';

CREATE TABLE IF NOT EXISTS `clinical_attachment` (
  `附件ID`      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '附件ID（PK）',
  `临床记录ID`  BIGINT        NOT NULL COMMENT '临床记录ID（FK）',
  `上传医生ID`  BIGINT        NOT NULL COMMENT '上传医生ID（FK）',
  `文件类别`    VARCHAR(50)   NOT NULL COMMENT '文件类别',
  `文件名称`    VARCHAR(255)  NOT NULL COMMENT '文件名称',
  `文件地址`    VARCHAR(1024) NOT NULL COMMENT '文件地址',
  `上传时间`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`附件ID`),
  KEY `idx_attachment_note` (`临床记录ID`),
  CONSTRAINT `fk_attachment_note` FOREIGN KEY (`临床记录ID`) REFERENCES `clinical_note` (`临床记录ID`),
  CONSTRAINT `fk_attachment_doctor` FOREIGN KEY (`上传医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临床资料附件';

-- =====================================================================
-- 模块五：患者日记与远程监测模块
-- urination_record、defecation_record、alert_rule、alert
-- =====================================================================

CREATE TABLE IF NOT EXISTS `urination_record` (
  `排尿记录ID`    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '排尿记录ID（PK）',
  `患者ID`       BIGINT      NOT NULL COMMENT '患者ID（FK）',
  `记录类型`     ENUM('饮水','自主排尿','漏尿','间歇导尿') NOT NULL COMMENT '记录类型',
  `发生时间`     DATETIME    NOT NULL COMMENT '发生时间',
  `饮水量ml`     INT         NOT NULL DEFAULT 0 COMMENT '饮水量ml',
  `自主排尿量ml`  INT         NOT NULL DEFAULT 0 COMMENT '自主排尿量ml',
  `漏尿量ml`     INT         NOT NULL DEFAULT 0 COMMENT '漏尿量ml',
  `导尿量ml`     INT         NOT NULL DEFAULT 0 COMMENT '导尿量ml',
  `残余尿量ml`   INT         NOT NULL DEFAULT 0 COMMENT '残余尿量ml',
  `备注`        TEXT        DEFAULT NULL COMMENT '备注',
  `创建时间`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `最后修改医生ID` BIGINT     DEFAULT NULL COMMENT '最后修改医生ID（FK）',
  `删除时间`     DATETIME    DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`排尿记录ID`),
  KEY `idx_urination_patient_time` (`患者ID`, `发生时间`),
  CONSTRAINT `fk_urination_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_urination_doctor` FOREIGN KEY (`最后修改医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排尿记录';

CREATE TABLE IF NOT EXISTS `defecation_record` (
  `排便记录ID`    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '排便记录ID（PK）',
  `患者ID`       BIGINT      NOT NULL COMMENT '患者ID（FK）',
  `发生时间`     DATETIME    NOT NULL COMMENT '发生时间',
  `大便性状`     ENUM('成型','稀便','水样便') NOT NULL COMMENT '大便性状',
  `排便速度`     ENUM('慢','正常','快') NOT NULL COMMENT '排便速度',
  `排便量`       ENUM('少','正常','多') NOT NULL COMMENT '排便量',
  `备注`        TEXT        DEFAULT NULL COMMENT '备注',
  `创建时间`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `最后修改医生ID` BIGINT     DEFAULT NULL COMMENT '最后修改医生ID（FK）',
  `删除时间`     DATETIME    DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`排便记录ID`),
  KEY `idx_defecation_patient_time` (`患者ID`, `发生时间`),
  CONSTRAINT `fk_defecation_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_defecation_doctor` FOREIGN KEY (`最后修改医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排便记录';

CREATE TABLE IF NOT EXISTS `alert_rule` (
  `预警规则ID`   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '预警规则ID（PK）',
  `规则名称`     VARCHAR(100)  NOT NULL COMMENT '规则名称',
  `指标类型`     ENUM('单次排尿量','单次漏尿量','饮水量','导尿量','残余尿量','排便次数') NOT NULL COMMENT '指标类型',
  `比较条件`     ENUM('>','>=','<','<=','=') NOT NULL COMMENT '比较条件',
  `阈值`        DECIMAL(10,2) NOT NULL COMMENT '阈值',
  `统计窗口分钟`  INT           NOT NULL DEFAULT 0 COMMENT '统计窗口分钟',
  `预警级别`     ENUM('提示','警告','高危') NOT NULL DEFAULT '提示' COMMENT '预警级别',
  `是否启用`     TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用）',
  `更新时间`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`预警规则ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警规则';

CREATE TABLE IF NOT EXISTS `alert` (
  `预警ID`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '预警ID（PK）',
  `患者ID`         BIGINT      NOT NULL COMMENT '患者ID（FK）',
  `预警规则ID`     BIGINT      NOT NULL COMMENT '预警规则ID（FK）',
  `触发排尿记录ID`  BIGINT      DEFAULT NULL COMMENT '触发排尿记录ID（FK）',
  `触发排便记录ID`  BIGINT      DEFAULT NULL COMMENT '触发排便记录ID（FK）',
  `预警时间`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
  `预警内容`       TEXT        DEFAULT NULL COMMENT '预警内容',
  `处理状态`       ENUM('待处理','处理中','已处理','已忽略') NOT NULL DEFAULT '待处理' COMMENT '处理状态',
  `处理医生ID`     BIGINT      DEFAULT NULL COMMENT '处理医生ID（FK）',
  `处理时间`       DATETIME    DEFAULT NULL COMMENT '处理时间',
  `处理备注`       TEXT        DEFAULT NULL COMMENT '处理备注',
  PRIMARY KEY (`预警ID`),
  KEY `idx_alert_patient` (`患者ID`),
  KEY `idx_alert_status` (`处理状态`),
  CONSTRAINT `fk_alert_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_alert_rule` FOREIGN KEY (`预警规则ID`) REFERENCES `alert_rule` (`预警规则ID`),
  CONSTRAINT `fk_alert_urination` FOREIGN KEY (`触发排尿记录ID`) REFERENCES `urination_record` (`排尿记录ID`),
  CONSTRAINT `fk_alert_defecation` FOREIGN KEY (`触发排便记录ID`) REFERENCES `defecation_record` (`排便记录ID`),
  CONSTRAINT `fk_alert_doctor` FOREIGN KEY (`处理医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常预警';

-- =====================================================================
-- 模块六：自评量表模块
-- questionnaire、questionnaire_question、scale_option_dict、scale_option_dict_item、
-- questionnaire_option、questionnaire_submission、questionnaire_answer、answer_option
-- =====================================================================

CREATE TABLE IF NOT EXISTS `questionnaire` (
  `量表ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '量表ID（PK）',
  `量表编码`     VARCHAR(64)  NOT NULL COMMENT '量表编码（UK）',
  `量表名称`     VARCHAR(150) NOT NULL COMMENT '量表名称',
  `量表说明`     TEXT         DEFAULT NULL COMMENT '量表说明',
  `计分说明`     TEXT         DEFAULT NULL COMMENT '计分说明',
  `状态`        ENUM('草稿','已发布','停用') NOT NULL DEFAULT '草稿' COMMENT '状态',
  `发布时间`     DATETIME     DEFAULT NULL COMMENT '发布时间',
  `创建人账号ID` BIGINT       NOT NULL COMMENT '创建人账号ID（FK）',
  `逻辑删除`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`量表ID`),
  UNIQUE KEY `uk_questionnaire_code` (`量表编码`),
  CONSTRAINT `fk_questionnaire_creator` FOREIGN KEY (`创建人账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表';

CREATE TABLE IF NOT EXISTS `questionnaire_question` (
  `题目ID`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题目ID（PK）',
  `量表ID`      BIGINT       NOT NULL COMMENT '量表ID（FK）',
  `题号`        VARCHAR(20)  NOT NULL COMMENT '题号',
  `题目内容`    TEXT         NOT NULL COMMENT '题目内容',
  `题型`        ENUM('单选','多选','数值','文本') NOT NULL COMMENT '题型',
  `是否必答`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否必答（1=是）',
  `是否反向计分` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否反向计分（1=是）',
  `排序号`      SMALLINT     NOT NULL DEFAULT 0 COMMENT '排序号',
  `逻辑删除`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`题目ID`),
  KEY `idx_question_questionnaire` (`量表ID`),
  CONSTRAINT `fk_question_questionnaire` FOREIGN KEY (`量表ID`) REFERENCES `questionnaire` (`量表ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表题目';

CREATE TABLE IF NOT EXISTS `scale_option_dict` (
  `组合ID`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '组合ID（PK）',
  `组合名称`  VARCHAR(150) NOT NULL COMMENT '组合名称',
  `排序号`   SMALLINT     NOT NULL DEFAULT 0 COMMENT '排序号',
  `状态`     ENUM('草稿','启用','停用') NOT NULL DEFAULT '草稿' COMMENT '状态',
  `创建时间`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`组合ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表选项字典组合';

CREATE TABLE IF NOT EXISTS `scale_option_dict_item` (
  `字典项ID`  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典项ID（PK）',
  `字典组合ID` BIGINT       NOT NULL COMMENT '字典组合ID（FK）',
  `item_code` VARCHAR(64)  NOT NULL COMMENT 'item_code',
  `item_label` VARCHAR(255) NOT NULL COMMENT 'item_label',
  `score`     DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT 'score',
  `排序号`    SMALLINT     NOT NULL DEFAULT 0 COMMENT '排序号',
  `是否启用`  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用）',
  PRIMARY KEY (`字典项ID`),
  UNIQUE KEY `uk_dict_item` (`字典组合ID`, `item_code`),
  CONSTRAINT `fk_dict_item_comb` FOREIGN KEY (`字典组合ID`) REFERENCES `scale_option_dict` (`组合ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表选项字典项';

CREATE TABLE IF NOT EXISTS `questionnaire_option` (
  `选项ID`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '选项ID（PK）',
  `题目ID`      BIGINT       NOT NULL COMMENT '题目ID（FK）',
  `选项文字`    VARCHAR(255) NOT NULL COMMENT '选项文字',
  `选项分值`    DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '选项分值',
  `排序号`      SMALLINT     NOT NULL DEFAULT 0 COMMENT '排序号',
  `字典组合ID`  BIGINT       DEFAULT NULL COMMENT '字典组合ID（FK）',
  `逻辑删除`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（1=已删除）',
  `创建时间`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`选项ID`),
  KEY `idx_option_question` (`题目ID`),
  CONSTRAINT `fk_option_question` FOREIGN KEY (`题目ID`) REFERENCES `questionnaire_question` (`题目ID`),
  CONSTRAINT `fk_option_dict` FOREIGN KEY (`字典组合ID`) REFERENCES `scale_option_dict` (`组合ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表题目私有选项';

CREATE TABLE IF NOT EXISTS `questionnaire_submission` (
  `填写记录ID`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '填写记录ID（PK）',
  `患者ID`       BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `量表ID`       BIGINT       NOT NULL COMMENT '量表ID（FK）',
  `填写时间`     DATETIME     NOT NULL COMMENT '填写时间',
  `填写状态`     ENUM('未完成','已提交') NOT NULL DEFAULT '未完成' COMMENT '填写状态',
  `总分`        DECIMAL(8,2) DEFAULT NULL COMMENT '总分',
  `维度得分快照`  JSON         DEFAULT NULL COMMENT '维度得分快照',
  `评估结果`     TEXT         DEFAULT NULL COMMENT '评估结果',
  `提交时间`     DATETIME     DEFAULT NULL COMMENT '提交时间',
  `医生点评`     TEXT         DEFAULT NULL COMMENT '医生点评',
  `点评医生ID`   BIGINT       DEFAULT NULL COMMENT '点评医生ID（FK）',
  `点评时间`     DATETIME     DEFAULT NULL COMMENT '点评时间',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `更新时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`填写记录ID`),
  KEY `idx_submission_patient` (`患者ID`),
  KEY `idx_submission_questionnaire` (`量表ID`),
  CONSTRAINT `fk_submission_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_submission_questionnaire` FOREIGN KEY (`量表ID`) REFERENCES `questionnaire` (`量表ID`),
  CONSTRAINT `fk_submission_doctor` FOREIGN KEY (`点评医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表填写记录';

CREATE TABLE IF NOT EXISTS `questionnaire_answer` (
  `答案ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '答案ID（PK）',
  `填写记录ID`   BIGINT       NOT NULL COMMENT '填写记录ID（FK）',
  `题目ID`       BIGINT       NOT NULL COMMENT '题目ID（FK）',
  `文本或数值答案` TEXT         DEFAULT NULL COMMENT '文本或数值答案',
  `得分`        DECIMAL(8,2) DEFAULT NULL COMMENT '得分',
  `创建时间`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`答案ID`),
  KEY `idx_answer_submission` (`填写记录ID`),
  KEY `idx_answer_question` (`题目ID`),
  CONSTRAINT `fk_answer_submission` FOREIGN KEY (`填写记录ID`) REFERENCES `questionnaire_submission` (`填写记录ID`),
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`题目ID`) REFERENCES `questionnaire_question` (`题目ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='量表答案';

CREATE TABLE IF NOT EXISTS `answer_option` (
  `答案ID`   BIGINT   NOT NULL COMMENT '答案ID（PK，FK）',
  `选项ID`   BIGINT   NOT NULL COMMENT '选项ID（PK，FK）',
  `创建时间`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`答案ID`, `选项ID`),
  CONSTRAINT `fk_answer_option_answer` FOREIGN KEY (`答案ID`) REFERENCES `questionnaire_answer` (`答案ID`),
  CONSTRAINT `fk_answer_option_option` FOREIGN KEY (`选项ID`) REFERENCES `questionnaire_option` (`选项ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案选项（多选题选中选项）';

-- =====================================================================
-- 模块七：康复训练与打卡模块
-- training_category、training_content、training_day、training_day_content、training_checkin
-- =====================================================================

CREATE TABLE IF NOT EXISTS `training_category` (
  `分类ID`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID（PK）',
  `父分类ID`  BIGINT       DEFAULT NULL COMMENT '父分类ID（FK，自关联）',
  `分类名称`  VARCHAR(100) NOT NULL COMMENT '分类名称',
  `训练模块`  ENUM('运动训练','盆底训练') NOT NULL COMMENT '训练模块',
  `排序号`    SMALLINT     NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`分类ID`),
  KEY `idx_category_parent` (`父分类ID`),
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`父分类ID`) REFERENCES `training_category` (`分类ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练分类';

CREATE TABLE IF NOT EXISTS `training_content` (
  `内容ID`      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '内容ID（PK）',
  `分类ID`      BIGINT        NOT NULL COMMENT '分类ID（FK）',
  `内容名称`    VARCHAR(255)  NOT NULL COMMENT '内容名称',
  `内容形式`    ENUM('视频','音频','文本') NOT NULL COMMENT '内容形式',
  `内容说明`    TEXT          DEFAULT NULL COMMENT '内容说明',
  `内容地址`    VARCHAR(1024) NOT NULL COMMENT '内容地址',
  `时长秒`      INT           DEFAULT NULL COMMENT '时长秒',
  `排序号`      SMALLINT      NOT NULL DEFAULT 0 COMMENT '排序号',
  `是否发布`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否发布（1=已发布）',
  `创建医生ID`  BIGINT        NOT NULL COMMENT '创建医生ID（FK）',
  `创建时间`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`内容ID`),
  KEY `idx_content_category` (`分类ID`),
  CONSTRAINT `fk_content_category` FOREIGN KEY (`分类ID`) REFERENCES `training_category` (`分类ID`),
  CONSTRAINT `fk_content_doctor` FOREIGN KEY (`创建医生ID`) REFERENCES `doctor` (`医生ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练内容';

CREATE TABLE IF NOT EXISTS `training_day` (
  `训练日程ID` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '训练日程ID（PK）',
  `训练日序号` SMALLINT     NOT NULL COMMENT '训练日序号（UK）',
  `日程名称`   VARCHAR(100) NOT NULL COMMENT '日程名称',
  `是否启用`   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用）',
  PRIMARY KEY (`训练日程ID`),
  UNIQUE KEY `uk_training_day_no` (`训练日序号`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练日程（全局共用）';

CREATE TABLE IF NOT EXISTS `training_day_content` (
  `训练日程ID` BIGINT   NOT NULL COMMENT '训练日程ID（PK，FK）',
  `内容ID`    BIGINT   NOT NULL COMMENT '内容ID（PK，FK）',
  `排序号`    SMALLINT NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`训练日程ID`, `内容ID`),
  CONSTRAINT `fk_daycontent_day` FOREIGN KEY (`训练日程ID`) REFERENCES `training_day` (`训练日程ID`),
  CONSTRAINT `fk_daycontent_content` FOREIGN KEY (`内容ID`) REFERENCES `training_content` (`内容ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程训练内容（多对多）';

CREATE TABLE IF NOT EXISTS `training_checkin` (
  `打卡ID`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '打卡ID（PK）',
  `患者ID`       BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `内容ID`       BIGINT       NOT NULL COMMENT '内容ID（FK）',
  `训练日程ID`   BIGINT       DEFAULT NULL COMMENT '训练日程ID（FK）',
  `开始播放时间`  DATETIME     NOT NULL COMMENT '开始播放时间',
  `完成时间`     DATETIME     DEFAULT NULL COMMENT '完成时间',
  `播放进度百分比` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '播放进度百分比',
  `打卡状态`     ENUM('进行中','已完成') NOT NULL DEFAULT '进行中' COMMENT '打卡状态',
  PRIMARY KEY (`打卡ID`),
  KEY `idx_checkin_patient_time` (`患者ID`, `开始播放时间`),
  CONSTRAINT `fk_checkin_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_checkin_content` FOREIGN KEY (`内容ID`) REFERENCES `training_content` (`内容ID`),
  CONSTRAINT `fk_checkin_day` FOREIGN KEY (`训练日程ID`) REFERENCES `training_day` (`训练日程ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练打卡';

-- =====================================================================
-- 模块八：治疗预约与通知模块
-- clinic_location、treatment_item、schedule_exception、appointment_slot、appointment、notification
-- =====================================================================

CREATE TABLE IF NOT EXISTS `clinic_location` (
  `院区ID`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '院区ID（PK）',
  `院区名称`  VARCHAR(100) NOT NULL COMMENT '院区名称（UK）',
  `地址`     VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `是否启用`  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用）',
  PRIMARY KEY (`院区ID`),
  UNIQUE KEY `uk_clinic_name` (`院区名称`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院区';

CREATE TABLE IF NOT EXISTS `treatment_item` (
  `项目ID`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目ID（PK）',
  `项目名称`        VARCHAR(150) NOT NULL COMMENT '项目名称',
  `项目分类`        VARCHAR(50)  DEFAULT NULL COMMENT '项目分类',
  `项目说明及适应症` TEXT         DEFAULT NULL COMMENT '项目说明及适应症',
  `治疗时长分钟`    SMALLINT     NOT NULL COMMENT '治疗时长分钟',
  `预约方式`        ENUM('单独预约','可并行') NOT NULL DEFAULT '单独预约' COMMENT '预约方式',
  `同时预约人数上限` SMALLINT     NOT NULL DEFAULT 1 COMMENT '同时预约人数上限',
  `是否启用`        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用）',
  `更新时间`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`项目ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗项目';

CREATE TABLE IF NOT EXISTS `schedule_exception` (
  `排班例外ID`  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '排班例外ID（PK）',
  `例外日期`    DATE         NOT NULL COMMENT '例外日期（UK）',
  `是否开放预约` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否开放预约（1=开放）',
  `开始时间`    TIME         DEFAULT NULL COMMENT '开始时间（开放时段的开始时间）',
  `结束时间`    TIME         DEFAULT NULL COMMENT '结束时间（开放时段的结束时间）',
  `例外说明`    VARCHAR(255) DEFAULT NULL COMMENT '例外说明',
  `操作账号ID`  BIGINT       DEFAULT NULL COMMENT '操作账号ID（FK）',
  PRIMARY KEY (`排班例外ID`),
  UNIQUE KEY `uk_schedule_date` (`例外日期`),
  CONSTRAINT `fk_schedule_account` FOREIGN KEY (`操作账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班例外';

CREATE TABLE IF NOT EXISTS `appointment_slot` (
  `时段ID`      BIGINT   NOT NULL AUTO_INCREMENT COMMENT '时段ID（PK）',
  `项目ID`      BIGINT   NOT NULL COMMENT '项目ID（FK）',
  `开始时间`    DATETIME NOT NULL COMMENT '开始时间',
  `结束时间`    DATETIME NOT NULL COMMENT '结束时间',
  `可预约人数`  SMALLINT NOT NULL COMMENT '可预约人数',
  `已预约人数`  SMALLINT NOT NULL DEFAULT 0 COMMENT '已预约人数（并发预约需原子更新防超额）',
  `时段状态`    ENUM('开放','关闭') NOT NULL DEFAULT '开放' COMMENT '时段状态',
  PRIMARY KEY (`时段ID`),
  KEY `idx_slot_item` (`项目ID`),
  CONSTRAINT `fk_slot_item` FOREIGN KEY (`项目ID`) REFERENCES `treatment_item` (`项目ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可预约时段';

CREATE TABLE IF NOT EXISTS `appointment` (
  `预约ID`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '预约ID（PK）',
  `患者ID`          BIGINT       NOT NULL COMMENT '患者ID（FK）',
  `时段ID`          BIGINT       NOT NULL COMMENT '时段ID（FK）',
  `院区ID`          BIGINT       NOT NULL COMMENT '院区ID（FK）',
  `预约状态`        ENUM('待就诊','已完成','已取消','爽约') NOT NULL DEFAULT '待就诊' COMMENT '预约状态',
  `预约二维码令牌哈希` VARCHAR(255) NOT NULL COMMENT '预约二维码令牌哈希（UK）',
  `预约时间`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
  `核销操作人账号ID` BIGINT       DEFAULT NULL COMMENT '核销操作人账号ID（FK，仅用于审计）',
  `核销时间`        DATETIME     DEFAULT NULL COMMENT '核销时间',
  `创建时间`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`预约ID`),
  UNIQUE KEY `uk_appointment_token` (`预约二维码令牌哈希`),
  KEY `idx_appointment_patient` (`患者ID`),
  KEY `idx_appointment_slot` (`时段ID`),
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`患者ID`) REFERENCES `patient` (`患者ID`),
  CONSTRAINT `fk_appointment_slot` FOREIGN KEY (`时段ID`) REFERENCES `appointment_slot` (`时段ID`),
  CONSTRAINT `fk_appointment_clinic` FOREIGN KEY (`院区ID`) REFERENCES `clinic_location` (`院区ID`),
  CONSTRAINT `fk_appointment_verifier` FOREIGN KEY (`核销操作人账号ID`) REFERENCES `account` (`账号ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录';

CREATE TABLE IF NOT EXISTS `notification` (
  `通知ID`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID（PK）',
  `接收账号ID`    BIGINT       NOT NULL COMMENT '接收账号ID（FK）',
  `预约ID`        BIGINT       DEFAULT NULL COMMENT '预约ID（FK）',
  `预警ID`        BIGINT       DEFAULT NULL COMMENT '预警ID（FK）',
  `通知渠道`      ENUM('小程序','短信') NOT NULL COMMENT '通知渠道',
  `通知内容`      TEXT         NOT NULL COMMENT '通知内容',
  `发送状态`      ENUM('待发送','已发送','失败') NOT NULL DEFAULT '待发送' COMMENT '发送状态',
  `计划发送时间`  DATETIME     NOT NULL COMMENT '计划发送时间',
  `实际发送时间`  DATETIME     DEFAULT NULL COMMENT '实际发送时间',
  PRIMARY KEY (`通知ID`),
  KEY `idx_notification_account` (`接收账号ID`),
  CONSTRAINT `fk_notification_account` FOREIGN KEY (`接收账号ID`) REFERENCES `account` (`账号ID`),
  CONSTRAINT `fk_notification_appointment` FOREIGN KEY (`预约ID`) REFERENCES `appointment` (`预约ID`),
  CONSTRAINT `fk_notification_alert` FOREIGN KEY (`预警ID`) REFERENCES `alert` (`预警ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知';

-- =====================================================================
-- 附录：补充说明
-- 1. 外键列已由 InnoDB 自动建索引（如需命名可控可改为显式 KEY）。
-- 2. 手机号唯一性：如业务要求"一个手机号只能注册一个账号"，可执行：
--    ALTER TABLE `account` ADD UNIQUE KEY `uk_account_phone_hmac` (`手机号HMAC`);
-- 3. 逻辑删除约定：是否删除/逻辑删除=1 表示已删除，业务查询需过滤。
-- 4. 并发预约防超额：预约写入需在事务内原子更新 appointment_slot.已预约人数
--    （UPDATE ... WHERE 已预约人数 < 可预约人数），或对时段行加锁。
-- =====================================================================
