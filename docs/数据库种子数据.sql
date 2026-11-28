-- =====================================================================
-- 盆底康复管理平台（pelvic-rehab）数据库种子数据（初始角色、权限、字典）
-- 依据：docs/ER图.puml、docs/数据库设计说明README.md（模块二 RBAC、模块六字典）、
--       docs/后端实现逻辑.md（网页后台 16 页功能清单）、admin-web 页面内选项常量
-- 前置：已执行 docs/数据库建表语句.sql
--
-- 说明：
--   1. 角色：ADMIN 管理员 / DOCTOR 医生 / PATIENT 患者（共用统一账号体系）
--      数据范围：管理员=ALL_PATIENTS，医生=DEPARTMENT（本科室），患者=SELF
--   2. 权限树与网页后台菜单/按钮一一对应（用户管理/数据总览/报表总览/个人主页）
--   3. 字典组合为量表选项模板（量表套用时复制为题目私有选项 questionnaire_option）
--   4. 本脚本为一次性初始化数据，固定主键便于关联；
--      重复执行会因主键冲突报错，如需重跑请先删除相关表数据
-- 生成日期：2026-11-28
-- =====================================================================

USE `pelvic_rehab`;

-- =====================================================================
-- 0. 科室（医生档案前置数据，添加医生/审核流程必需）
-- =====================================================================

INSERT INTO `department` (`科室ID`, `科室名称`, `科室说明`) VALUES
(1, '盆底康复科', '盆底功能障碍康复诊疗科室');

-- =====================================================================
-- 1. 初始角色（sys_role）
-- =====================================================================

INSERT INTO `sys_role` (`角色ID`, `角色编码`, `角色名称`, `数据范围`, `角色状态`) VALUES
(1, 'ADMIN',   '管理员', 'ALL_PATIENTS', '启用'),
(2, 'DOCTOR',  '医生',   'DEPARTMENT',   '启用'),
(3, 'PATIENT', '患者',   'SELF',         '启用');

-- =====================================================================
-- 2. 初始权限（sys_permission）—— 与网页后台菜单/按钮一一对应
-- 权限类型：MENU（菜单/页面）、BUTTON（按钮）
-- =====================================================================

-- 一级菜单：用户管理 / 数据总览 / 报表总览 / 个人主页
INSERT INTO `sys_permission` (`权限ID`, `权限编码`, `权限名称`, `权限类型`, `父级权限ID`, `所属模块`, `操作类型`, `前端路由地址`, `同级排序号`, `权限说明`) VALUES
(100, 'user:menu',      '用户管理',   'MENU', NULL, 'module-system', 'VIEW', '/user',     1, '用户管理页面（管理员/医生双形态）'),
(200, 'dashboard:menu', '数据总览',   'MENU', NULL, 'module-report', 'VIEW', '/dashboard', 2, '数据总览页面'),
(300, 'report:menu',    '报表总览',   'MENU', NULL, 'module-report', 'VIEW', '/reports',   3, '报表总览页面'),
(400, 'profile:menu',   '个人主页',   'MENU', NULL, 'module-auth',   'VIEW', '/profile',   4, '个人主页（信息/修改密码/退出登录）');

-- 用户管理下的按钮
INSERT INTO `sys_permission` (`权限ID`, `权限编码`, `权限名称`, `权限类型`, `父级权限ID`, `所属模块`, `操作类型`, `前端路由地址`, `同级排序号`, `权限说明`) VALUES
(101, 'user:detail',            '查看用户详情',   'BUTTON', 100, 'module-system',  'VIEW',   NULL, 1, '患者/医生详情页（02/03）'),
(102, 'user:add-doctor',        '添加医生',       'BUTTON', 100, 'module-doctor',  'CREATE', NULL, 2, '仅管理员（01-1）'),
(103, 'user:remove-doctor',     '移除医生',       'BUTTON', 100, 'module-doctor',  'DELETE', NULL, 3, '仅管理员（03）'),
(104, 'user:patient-supplement','补充患者信息',   'BUTTON', 100, 'module-patient', 'UPDATE', NULL, 4, '医生临床检查补充（02）');

-- 数据总览下的按钮
INSERT INTO `sys_permission` (`权限ID`, `权限编码`, `权限名称`, `权限类型`, `父级权限ID`, `所属模块`, `操作类型`, `前端路由地址`, `同级排序号`, `权限说明`) VALUES
(201, 'dashboard:patient-detail', '查看患者数据详情', 'BUTTON', 200, 'module-report', 'VIEW', NULL, 1, '患者数据详情页（05）');

-- 报表总览下的七张报表（07-1~07-7）与导出按钮
INSERT INTO `sys_permission` (`权限ID`, `权限编码`, `权限名称`, `权限类型`, `父级权限ID`, `所属模块`, `操作类型`, `前端路由地址`, `同级排序号`, `权限说明`) VALUES
(301, 'report:female',     '女性基础信息表',     'MENU', 300, 'module-report', 'VIEW', '/reports/female',     1, '07-1'),
(302, 'report:male',       '男性基础信息表',     'MENU', 300, 'module-report', 'VIEW', '/reports/male',       2, '07-2'),
(303, 'report:urination',  '排尿日记明细表',     'MENU', 300, 'module-report', 'VIEW', '/reports/urination',  3, '07-3'),
(304, 'report:defecation', '排便日记明细表',     'MENU', 300, 'module-report', 'VIEW', '/reports/defecation', 4, '07-4'),
(305, 'report:assessment', '盆底自评数据表',     'MENU', 300, 'module-report', 'VIEW', '/reports/assessment', 5, '07-5'),
(306, 'report:training',   '康复训练打卡数据表', 'MENU', 300, 'module-report', 'VIEW', '/reports/training',   6, '07-6'),
(307, 'report:summary',    '患者数据汇总表',     'MENU', 300, 'module-report', 'VIEW', '/reports/summary',    7, '07-7'),
(308, 'report:export',     '导出报表（Excel）',  'BUTTON', 300, 'module-report', 'EXPORT', NULL, 8, '异步导出任务（07 系列页）');

-- 个人主页下的按钮
INSERT INTO `sys_permission` (`权限ID`, `权限编码`, `权限名称`, `权限类型`, `父级权限ID`, `所属模块`, `操作类型`, `前端路由地址`, `同级排序号`, `权限说明`) VALUES
(401, 'profile:change-password', '修改密码', 'BUTTON', 400, 'module-auth', 'UPDATE', NULL, 1, '个人主页修改密码弹窗');

-- =====================================================================
-- 3. 角色权限关联（sys_role_permission）
--    ADMIN：全部权限；DOCTOR：除“添加医生/移除医生”外全部（01-2 医生形态）
--    PATIENT：网页后台无权限（小程序端权限另行规划）
-- =====================================================================

-- 管理员（角色ID=1）：全部 17 项权限
INSERT INTO `sys_role_permission` (`角色ID`, `权限ID`) VALUES
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104),
(1, 200), (1, 201),
(1, 300), (1, 301), (1, 302), (1, 303), (1, 304), (1, 305), (1, 306), (1, 307), (1, 308),
(1, 400), (1, 401);

-- 医生（角色ID=2）：无 user:add-doctor、user:remove-doctor
INSERT INTO `sys_role_permission` (`角色ID`, `权限ID`) VALUES
(2, 100), (2, 101), (2, 104),
(2, 200), (2, 201),
(2, 300), (2, 301), (2, 302), (2, 303), (2, 304), (2, 305), (2, 306), (2, 307), (2, 308),
(2, 400), (2, 401);

-- =====================================================================
-- 4. 字典：量表选项字典组合与字典项（scale_option_dict / scale_option_dict_item）
--    字典仅作模板；量表套用时复制为 questionnaire_option 题目私有选项
-- =====================================================================

INSERT INTO `scale_option_dict` (`组合ID`, `组合名称`, `排序号`, `状态`) VALUES
(1, '通用频率选项',   1, '启用'),
(2, '影响程度选项',   2, '启用'),
(3, '是否选项',       3, '启用'),
(4, '药物治疗类别',   4, '启用');

-- 组合1：通用频率选项（PFDI-20 等频率类量表可用）
INSERT INTO `scale_option_dict_item` (`字典项ID`, `字典组合ID`, `item_code`, `item_label`, `score`, `排序号`, `是否启用`) VALUES
(101, 1, 'F0', '从不', 0.00, 1, 1),
(102, 1, 'F1', '偶尔', 1.00, 2, 1),
(103, 1, 'F2', '有时', 2.00, 3, 1),
(104, 1, 'F3', '经常', 3.00, 4, 1),
(105, 1, 'F4', '总是', 4.00, 5, 1);

-- 组合2：影响程度选项（IIQ-7 等影响类量表可用）
INSERT INTO `scale_option_dict_item` (`字典项ID`, `字典组合ID`, `item_code`, `item_label`, `score`, `排序号`, `是否启用`) VALUES
(201, 2, 'NONE', '无影响', 0.00, 1, 1),
(202, 2, 'MILD', '轻度影响', 1.00, 2, 1),
(203, 2, 'MOD',  '中度影响', 2.00, 3, 1),
(204, 2, 'SEV',  '重度影响', 3.00, 4, 1);

-- 组合3：是否选项（通用是非题）
INSERT INTO `scale_option_dict_item` (`字典项ID`, `字典组合ID`, `item_code`, `item_label`, `score`, `排序号`, `是否启用`) VALUES
(301, 3, 'N', '否', 0.00, 1, 1),
(302, 3, 'Y', '是', 1.00, 2, 1);

-- 组合4：药物治疗类别（来源：PatientDetailView.vue medicationOptions 常量）
INSERT INTO `scale_option_dict_item` (`字典项ID`, `字典组合ID`, `item_code`, `item_label`, `score`, `排序号`, `是否启用`) VALUES
(401, 4, 'MED01', '阴道炎',           0.00, 1, 1),
(402, 4, 'MED02', '精神科药物',       0.00, 2, 1),
(403, 4, 'MED03', '甲状腺类药物',     0.00, 3, 1),
(404, 4, 'MED04', '高血压药物',       0.00, 4, 1),
(405, 4, 'MED05', '糖尿病药物',       0.00, 5, 1),
(406, 4, 'MED06', '泌尿系统药物',     0.00, 6, 1),
(407, 4, 'MED07', '抑制妊娠反应药物', 0.00, 7, 1),
(408, 4, 'MED08', '神经系统药物',     0.00, 8, 1),
(409, 4, 'MED09', 'HRT类药物',        0.00, 9, 1);

-- =====================================================================
-- 附录：管理员账号创建示例（业务代码生成，此处仅作结构参考，勿直接执行）
--   手机号密文/手机号HMAC 需由后端加密组件生成；
--   密码哈希为 BCrypt（如 spring-security 默认编码器）。
--
-- INSERT INTO `account` (`账号编号`, `昵称`, `账号状态`, `注册来源`, `手机号密文`, `手机号HMAC`)
-- VALUES ('ACC000001', '系统管理员', 'NORMAL', 'ADMIN', '<密文>', '<HMAC>');
--
-- INSERT INTO `account_credential` (`账号ID`, `密码哈希`)
-- VALUES (<新账号ID>, '<BCrypt哈希>');
--
-- INSERT INTO `sys_user_role` (`账号ID`, `角色ID`, `授权人账号ID`, `授权时间`)
-- VALUES (<新账号ID>, 1, NULL, NOW());
-- =====================================================================
