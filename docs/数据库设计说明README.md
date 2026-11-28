# 数据库设计说明README

# 康复管理系统数据库说明文档（READM\.md）



> 本文档依据 `ER图.puml` 整理。  
> 
> 
> 
> 表名、字段名以 ER 图为准；标注“实施提示”的内容为按图中 legend 设计说明落地时需要补充或特别注意的事项。  
> 
> 
> 
> 通用约定：主键统一使用 `BIGINT`；时间字段使用 `DATETIME/TIMESTAMP/DATE`；枚举字段保存图中所列枚举值；患者、医生共用统一账号体系；业务表按需保留逻辑删除、审计与最后修改信息；表说明中"关键字段"为摘录，完整字段以 ER 图为准。
> 
> 
> 
> 
> 
> 

## 1\. 模块索引

|模块|模块名称|主要表|
|---|---|---|
|模块一|账号与人员模块（账号基础模块）|account、account\_credential、auth\_sms\_code、auth\_session、department、patient、doctor|
|模块二|角色权限与访问审计模块|sys\_role、sys\_permission、sys\_user\_role、sys\_role\_permission、sys\_operate\_log、sys\_login\_log|
|模块三|患者信息采集与病史模块|patient\_health\_profile、patient\_conditions、surgery\_history、pregnancy|
|模块四|临床检查与医务人员记录模块|examination、diastasis、postpartum、pain\_assessment、pelvic\_inspection、general\_inspection、special\_exam、neurological\_exam、anal\_palpation、clinical\_note、clinical\_attachment|
|模块五|患者日记与远程监测模块|urination\_record、defecatio\_record、alert\_rule、alert|
|模块六|自评量表模块|questionnaire、questionnaire\_question、scale\_optio\_dict、scale\_option\_dict\_item、questionnaire\_option、questionnaire\_submission、questionnaire\_answer、answer\_option|
|模块七|康复训练与打卡模块|training\_category、training\_content、training\_day、training\_day\_content、training\_checkin|
|模块八|治疗预约与通知模块|clinic\_location、treatment\_item、schedule\_exception、appointment\_slot、appointment、notification|

---

## 2\. 模块一：账号与人员模块（账号基础模块）

### 表关系

- `account` 1:0\.\.1 `account_credential`：账号可有网页密码凭证，用于网页后台登录。

- `account` 1:N `auth_session`：账号可创建多个登录会话，按客户端类型区分小程序/网页。

- `account` 1:0\.\.1 `patient`：账号可对应一个患者档案。

- `account` 1:0\.\.1 `doctor`：账号可对应一个医生档案。

- `department` 1:N `doctor`：科室下可有多个医生。

- `auth_sms_code` 独立保存短信验证码，按手机号 HMAC 与业务关联，不直接设置账号外键。

    

### 设计规则

1. 患者和医生使用同一账号体系，通过 `patient`、`doctor` 档案区分业务身份。

2. 小程序与网页后台通过 `auth_session.客户端类型` 区分会话。

3. 网页密码凭证仅用于网页登录，一个账号最多一条网页密码凭证。

4. 手机号作为唯一登录身份，统一保存在 `account`（手机号密文、手机号HMAC）；网页登录名即手机号，不单设登录身份绑定表。

5. 短信验证码按使用场景区分注册、登录、绑定手机、重置密码，并记录过期、使用和验证失败次数（验证通过即消费，只保留使用时间）。

6. 医生档案保留审核状态、审核通过时间、审核人账号与原因。

7. 患者档案支持逻辑删除；患者编号、住院号等按业务需要保留唯一或查询约束



### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|account|统一账号主体|PK：账号ID；UK：账号编号|手机号密文、手机号HMAC、账号状态、注册来源、最后登录时间|患者、医生、后台账号的统一主体；手机号登录身份直接存于本表|
|account\_credential<br>|网页密码凭证|PK：凭证ID；UK：账号ID|密码哈希、密码更新时间、连续失败次数、锁定截止时间|网页登录密码凭证（登录名即手机号，存于 account）|
|auth\_sms\_code<br>|短信验证码<br>|PK：验证码ID<br>|手机号密文、手机号HMAC、使用场景、验证码哈希、过期时间、使用时间、验证失败次数、发送IP|短信验证码审计与校验<br>|
|auth\_session<br>|登录会话|PK：会话ID|账号ID、客户端类型、AccessToken哈希、RefreshToken哈希、过期时间、撤销时间、最后活跃时间|登录态管理；客户端类型区分网页后台/医生端小程序/患者端小程序|
|department|科室|PK：科室ID；UK：科室名称|科室说明|医生所属科室|
|patient|患者档案|PK：患者ID；UK：账号ID、患者编号|住院号、姓名、性别、出生年月、身高cm、体重kg、婚姻状况、教育程度、从事职业、是否删除|患者基础档案|
|doctor|医生档案|PK：医生ID；UK：账号ID、编号|科室ID、姓名、性别、职称、审核状态、审核通过时间、审核人账号ID、通过或拒绝原因|医生基础档案及审核信息|

### 举例

患者注册并建立患者档案：

```SQL
-- 逻辑示例
INSERT INTO account (账号编号, 昵称, 账号状态, 注册来源, 手机号密文, 手机号HMAC)
VALUES ('ACC20260001', '患者张三', 'NORMAL', 'MINI_PROGRAM', '加密手机号', 'HMAC手机号');

INSERT INTO patient (账号ID, 患者编号, 姓名, 性别, 出生年月, 是否删除)
VALUES (10001, 'P20260001', '张三', '女', '1995-05-01', 0);
```

---

## 3\. 模块二：角色权限与访问审计模块

### 表关系

- `account` N:M `sys_role`，通过 `sys_user_role` 关联。

- `sys_role` N:M `sys_permission`，通过 `sys_role_permission` 关联。

- `sys_permission` 自关联：`父级权限ID` 指向 `sys_permission.权限ID`，形成菜单/按钮/API 权限树。

- `account` 1:N `sys_operate_log`：账号执行操作产生操作审计日志。

- `patient` 0:N `sys_operate_log`：操作日志可选关联被访问患者。

- `account` 1:N `sys_login_log`：账号产生登录审计日志。

- `sys_user_role` 中 `授权人账号ID` 可关联 `account`，用于记录授权人。



### 设计规则

1. 采用 RBAC：账号通过角色获得功能权限。

2. 角色数据范围支持 `SELF`、`ALL_PATIENTS`、`DEPARTMENT`、`ASSIGNED`。

3. `sys_user_role` 对 `(账号ID, 角色ID)` 唯一；`sys_role_permission` 对 `(角色ID, 权限ID)` 唯一。

4. 权限类型支持 `MENU`、`BUTTON`、`API`；操作类型支持 `VIEW`、`CREATE`、`UPDATE`、`DELETE`、`EXPORT`、`REVIEW`。

5. 操作审计日志记录业务模块、资源类型、资源ID、操作类型、操作结果、操作原因、请求信息、耗时、IP、UserAgent 等。

6. 登录审计日志记录登录方式、客户端类型、登录结果、失败原因、IP、UserAgent。

7. 授权人、操作账号、登录账号均应可追溯。



### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|sys\_role|系统角色|PK：角色ID；UK：角色编码|角色名称、数据范围、角色状态（启用/停用）|角色定义|
|sys\_permission\<br\>|功能权限|PK：权限ID；UK：权限编码|权限名称、权限类型、父级权限ID、所属模块、操作类型、前端路由地址、同级排序号|菜单/按钮/API 权限树|
|sys\_user\_role|账号角色关联|PK：账号角色关联ID；UNIQUE$账号ID, 角色ID$|账号ID、角色ID、授权人账号ID、授权时间|账号授权|
|sys\_role\_permission|角色权限关联|PK：角色权限关联ID；UNIQUE$角色ID, 权限ID$|角色ID、权限ID|角色权限配置|
|sys\_operate\_log<br>|操作审计日志|PK：日志ID|操作账号ID、关联患者ID、业务模块、资源类型、资源ID、操作类型、操作结果、操作原因、请求方法、请求地址、请求参数、耗时毫秒、IP地址、userAgent、操作时间|业务操作审计|
|sys\_login\_log|登录审计日志|PK：登录日志ID|账号ID、登录方式、客户端类型、登录结果、失败原因、IP地址、UserAgent、登录时间|登录审计|

### 举例

给账号授予角色，并查询某患者数据访问日志：

```SQL
INSERT INTO sys_user_role (账号ID, 角色ID, 授权人账号ID, 授权时间)
VALUES (10001, 2, 90001, NOW());

SELECT 操作账号ID, 业务模块, 资源类型, 资源ID, 操作类型, 操作结果, 操作时间
FROM sys_operate_log
WHERE 关联患者ID = 20001
ORDER BY 操作时间 DESC;
```

---

## 4\. 模块三：患者信息采集与病史模块

### 表关系

- `patient` 1:0\.\.1 `patient_health_profile`：患者健康档案与患者一对一，仅保留最新资料。

- `patient` 1:N `patient_conditions`：患者可有多个既往病史项目。

- `patient` 1:N `surgery_history`：患者可有多次手术史。

- `patient` 1:N `pregnancy`：患者可有多条妊娠记录，一行代表一次妊娠。

- `pregnancy` 已建立 `UNIQUE(患者ID, 妊娠序号)` 唯一约束。  

    

### 设计规则

1. 患者健康档案与 `patient` 一对一，仅保留最新资料。

2. 既往病史项目、手术史因可有多条记录而独立保留。

3. `pregnancy` 一行代表一次妊娠，孕期情况及分娩信息并入该表；分娩字段允许为空。

4. 已建立 `UNIQUE(患者ID, 妊娠序号)` 唯一约束。

5. 健康档案、手术史、妊娠记录保留逻辑删除字段；病史项目按记录增删改维护。

6. 健康档案包含主诉、大小便、性生活、冶游史、月经、绝经、吸烟饮酒、久坐久站等生活方式信息。

    

### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|patient\_health\_profile|患者健康档案|PK/FK：健康档案ID；UK：患者ID|主诉、大便情况、小便情况、性生活频率、冶游史、月经信息、绝经信息、吸烟饮酒、久坐久站、其他习惯、逻辑删除|患者最新健康档案|
|patient\_conditions|既往病史项目|PK：病史项目ID|患者ID、项目类别、项目名称、详情|多条既往病史|
|surgery\_history|手术史|PK：手术史ID|患者ID、手术类别、手术名称、手术日期、备注、逻辑删除|多次手术记录|
|pregnancy|妊娠记录|PK：妊娠记录ID；UNIQUE$患者ID, 妊娠序号$|妊娠序号、妊娠结局、孕前/产前体重、孕期增重、睡觉体位、尿失禁、大便情况、血压、血糖、甲状腺功能、疼痛VAS、分娩日期、分娩方式、会阴情况、助产方式、胎儿出生体重、逻辑删除|一次妊娠一行，含孕期与分娩信息|

### 举例

为患者新增健康档案与一次妊娠记录：

```SQL
INSERT INTO patient_health_profile (健康档案ID, 患者ID, 主诉, 大便情况, 小便情况, 是否绝经)
VALUES (30001, 20001, '产后腰背痛', '正常', '压力性尿失禁', 0);

INSERT INTO pregnancy (患者ID, 妊娠序号, 妊娠结局, 分娩日期, 分娩方式, 会阴情况, 逻辑删除)
VALUES (20001, 1, '活产', '2025-12-01', '顺产', '会阴侧切', 0);
```

---

## 5\. 模块四：临床检查与医务人员记录模块

### 表关系

- `patient` 1:N `examination`：患者可有多次临床检查。

- `doctor` 1:N `examination`：医生录入检查；`最后修改医生ID` 记录最后修改医生。

- `examination` 1:0\.\.1 `diastasis`：腹直肌分离评估。

- `examination` 1:0\.\.1 `postpartum`：产后情况评估。

- `examination` 1:0\.\.1 `pain_assessment`：疼痛评估。

- `examination` 1:0\.\.1 `pelvic_inspection`：盆底视诊。

- `examination` 1:0\.\.1 `general_inspection`：一般视诊。

- `examination` 1:0\.\.1 `special_exam`：特殊检查。

- `examination` 1:0\.\.1 `neurological_exam`：神经损伤查体。

- `examination` 1:0\.\.1 `anal_palpation`：肛门触诊。

- `patient` 1:N `clinical_note`；`doctor` 1:N `clinical_note`；`examination` 0:N `clinical_note`。

- `clinical_note` 1:N `clinical_attachment`；`doctor` 1:N `clinical_attachment`。

    

### 设计规则

1. `examination` 仅保存临床检查主信息；八个检查模块分别独立建表，通过 `检查ID` 关联。

2. 每个检查模块对同一次检查最多一条记录；未执行的检查模块不创建记录。

3. 临床检查记录保留最后修改医生及更新时间；不保留修改前后完整内容。

4. `clinical_note`、`clinical_attachment` 独立保存临床补充记录与附件。

5. 临床补充记录可见范围当前为“医务人员”。

6. 附件记录上传医生、文件类别、文件名称、文件地址与上传时间。

### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|examination|临床检查|PK：检查ID|患者ID、录入医生ID、最后修改医生ID、检查时间、主诉、腹部张力、诊断、药物治疗、其他补充|检查主信息|
|diastasis|腹直肌分离评估|PK：评估ID；UK：检查ID|静息/收缩肚脐、脐上、脐下指宽|每检查最多一条|
|postpartum|产后情况评估|PK：评估ID；UK：检查ID|小便情况、大便情况、盆腔会阴痛VAS、脏器脱垂、瘢痕情况、瘢痕质地/颜色/时间/长度、尿道不稳定性、其他|每检查最多一条|
|pain\_assessment|疼痛评估|PK：疼痛评估ID；UK：检查ID|腰背痛、骶髂关节痛、耻骨痛、尾骨痛、坐骨神经痛、腹股沟痛、颈痛、上肢痛、下肢痛等左右/中VAS|每检查最多一条|
|pelvic\_inspection|盆底视诊|PK：视诊ID；UK：检查ID|外阴、阴道裂口、尿道口、阴蒂、阴唇对称、分泌物、阴毛、痔疮、Valsalva表现|每检查最多一条|
|general\_inspection|一般视诊|PK：视诊ID；UK：检查ID|精神面貌、步行情况、畸形、肿胀、疤痕、瘀斑、肛周生殖器肤色、直肠脱垂长度、痔疮|每检查最多一条|
|special\_exam|特殊检查|PK：特殊检查ID；UK：检查ID|泌尿生殖测试、疤痕试验、4字试验、骨盆分离/挤压试验、骶骨下压、股骨冲击、直腿抬高及加强、广泛性触诊、其他|每检查最多一条|
|neurological\_exam|神经损伤查体|PK：查体ID；UK：检查ID|盆底张力、左右侧感觉、导尿管|每检查最多一条|
|anal\_palpation|肛门触诊|PK：触诊ID；UK：检查ID|肛提肌、坐尾肌、髂尾肌、闭孔内肌、脏器偏移、牛津肌力、尾骨嵌顿、肛周温度/湿度、会阴及肛周肌肉张力|每检查最多一条|
|clinical\_note|临床补充记录|PK：临床记录ID|患者ID、检查ID、医生ID、记录类型、记录内容、可见范围|外院资料、阶段评估等|
|clinical\_attachment|临床资料附件|PK：附件ID|临床记录ID、上传医生ID、文件类别、文件名称、文件地址、上传时间|临床记录附件|

### 举例

创建一次临床检查，并只录入实际执行的疼痛评估：

```SQL
INSERT INTO examination (检查ID, 患者ID, 录入医生ID, 检查时间, 主诉, 诊断)
VALUES (40001, 20001, 50001, NOW(), '产后盆底评估', '产后盆底功能障碍');

INSERT INTO pain_assessment (疼痛评估ID, 检查ID, 腰背痛中VAS, 耻骨痛VAS, 下肢痛左VAS, 下肢痛右VAS)
VALUES (41001, 40001, 4, 2, 1, 1);

INSERT INTO clinical_note (临床记录ID, 患者ID, 检查ID, 医生ID, 记录类型, 记录内容, 可见范围)
VALUES (42001, 20001, 40001, 50001, '阶段评估', '建议进行盆底训练', '医务人员');
```

---

## 6\. 模块五：患者日记与远程监测模块

### 表关系

- `patient` 1:N `urination_record`：患者多条排尿记录。

- `patient` 1:N `defecation_record`：患者多条排便记录。

- `doctor` 0:N `urination_record`、`defecation_record`：医生可修订记录，保留最后修改医生。

- `alert_rule` 1:N `alert`：预警规则触发多条异常预警。

- `patient` 1:N `alert`：患者多条预警。

- `urination_record` 0:N `alert`：排尿记录可触发预警。

- `defecation_record` 0:N `alert`：排便记录可触发预警。

- `doctor` 0:N `alert`：医生处理预警。



### 设计规则

1. 排尿记录与排便记录分表存储。

2. 患者提交后不可自行修改；医生修订应保留审计信息，图中通过 `最后修改医生ID` 与删除时间等字段支持。

3. 预警规则包含指标类型、比较条件、阈值、统计窗口分钟、预警级别、是否启用；比较条件取值：`>`、`>=`、`<`、`<=`、`=`。

4. 预警指标类型为枚举值：`单次排尿量`、`单次漏尿量`、`饮水量`、`导尿量`、`残余尿量`、`排便次数`；取值来源分别为 `urination_record.自主排尿量ml`、`urination_record.漏尿量ml`、`urination_record.饮水量ml`、`urination_record.导尿量ml`、`urination_record.残余尿量ml`、`defecation_record`（统计窗口内记录数）。

5. 异常预警可关联触发排尿记录或排便记录，二者均可为空。

6. 预警处理状态为 `待处理`、`处理中`、`已处理`、`已忽略`，并记录处理医生、处理时间、处理备注。



### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|urination\_record|排尿记录|PK：排尿记录ID|患者ID、记录类型、发生时间、饮水量、自主排尿量、漏尿量、导尿量、残余尿量、最后修改医生ID、删除时间|饮水、自主排尿、漏尿、间歇导尿|
|defecation\_record|排便记录|PK：排便记录ID|患者ID、发生时间、大便性状、排便速度、排便量、最后修改医生ID、删除时间|排便日记|
|alert\_rule|预警规则|PK：预警规则ID|规则名称、指标类型、比较条件、阈值、统计窗口分钟、预警级别、是否启用|预警阈值配置|
|alert|异常预警|PK：预警ID|患者ID、预警规则ID、触发排尿记录ID、触发排便记录ID、预警时间、预警内容、处理状态、处理医生ID、处理时间、处理备注|预警实例与处理|

### 举例

插入一条排尿记录并生成待处理预警：

```SQL
INSERT INTO urination_record (排尿记录ID, 患者ID, 记录类型, 发生时间, 漏尿量ml)
VALUES (60001, 20001, '漏尿', NOW(), 50);

INSERT INTO alert (预警ID, 患者ID, 预警规则ID, 触发排尿记录ID, 预警时间, 预警内容, 处理状态)
VALUES (61001, 20001, 1, 60001, NOW(), '漏尿量超过阈值', '待处理');
```

---

## 7\. 模块六：自评量表模块

### 表关系

- `account` 1:N `questionnaire`：量表由账号创建。

- `questionnaire` 1:N `questionnaire_question`：量表包含题目。

- `scale_option_dict` 1:N `scale_option_dict_item`：选项字典组合包含字典项。

- `questionnaire_question` 1:N `questionnaire_option`：题目配置私有选项。

- `questionnaire_option` 0:N `answer_option`：题目私有选项可被答卷选中。

- `questionnaire_question` 1:N `questionnaire_answer`：题目对应答案。

- `patient` 1:N `questionnaire_submission`：患者多次填写。

- `questionnaire` 1:N `questionnaire_submission`：量表多次填写记录。

- `questionnaire_submission` 1:N `questionnaire_answer`：填写记录包含答案。

- `questionnaire_answer` 1:N `answer_option`：答案可关联多个选中选项。

- `doctor` 0:N `questionnaire_submission`：医生点评填写记录。



### 设计规则

1. 量表选项字典仅作为模板；套用时复制为 `questionnaire_option` 题目私有选项，答卷选项关联题目私有选项。

2. 量表计分说明及规则归入 `questionnaire`；总分和维度得分快照保存在 `questionnaire_submission`，不单设 `dimension_score` 表。

3. 发布后冻结题目、选项及计分规则。

4. 填写记录保存填写状态、总分、维度得分快照 JSON、评估结果、提交时间、医生点评与点评医生。

    



### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|questionnaire|量表|PK：量表ID；UNIQUE$量表编码$|量表编码、量表名称、量表说明、计分说明、状态、发布时间、创建人账号ID、逻辑删除|量表主表|
|questionnaire\_question|量表题目|PK：题目ID|量表ID、题号、题目内容、题型、是否必答、是否反向计分、排序号、逻辑删除|量表题目表|
|scale\_option\_dict|量表选项字典组合|PK：组合ID|组合名称、排序号、状态（草稿/启用/停用）|选项模板组合|
|scale\_option\_dict\_item|量表选项字典项|PK：字典项ID；UNIQUE$字典组合ID, item\_code$（联合 UK）|字典组合ID、item\_code、item\_label、score、排序号、是否启用|字典模板项|
|questionnaire\_option|量表题目私有选项|PK：选项ID|题目ID、选项文字、选项分值、排序号、字典组合ID、逻辑删除|套用字典后复制为私有选项|
|questionnaire\_submission|量表填写记录|PK：填写记录ID|患者ID、量表ID、填写时间、填写状态、总分、维度得分快照、评估结果、提交时间、医生点评、点评医生ID、点评时间|患者答卷主记录|
|questionnaire\_answer|量表答案|PK：答案ID|填写记录ID、题目ID、文本或数值答案、得分|单题答案|
|answer\_option|答案选项|PK：答案ID、选项ID|创建时间|多选题选中选项关联|

### 举例

患者提交一次量表填写记录并保存题目答案：

```SQL
INSERT INTO questionnaire_submission (填写记录ID, 患者ID, 量表ID, 填写时间, 填写状态, 总分, 维度得分快照, 提交时间)
VALUES (70001, 20001, 1, NOW(), '已提交', 18.5, '{"盆底":10,"疼痛":8.5}', NOW());

INSERT INTO questionnaire_answer (答案ID, 填写记录ID, 题目ID, 文本或数值答案, 得分)
VALUES (71001, 70001, 101, '经常', 3.0);

INSERT INTO answer_option (答案ID, 选项ID, 创建时间)
VALUES (71001, 9001, NOW());
```

---

## 8\. 模块七：康复训练与打卡模块

### 表关系

- `training_category` 自关联：`父分类ID` 指向 `training_category.分类ID`，形成父子分类。

- `training_category` 1:N `training_content`：分类下多个训练内容。

- `doctor` 1:N `training_content`：医生创建/发布训练内容。

- `training_day` N:M `training_content`，通过 `training_day_content` 关联。

- `patient` 1:N `training_checkin`：患者多条训练打卡。

- `training_content` 1:N `training_checkin`：训练内容被多次打卡。

- `training_day` 0:N `training_checkin`：打卡可关联训练日程。

### 设计规则

1. 所有患者使用同一套训练日程。

2. 训练打卡保存患者个人进度；完成当日安排后解锁下一训练日。

3. 训练分类支持父子分类，训练模块分为 `运动训练`、`盆底训练`。

4. 训练内容支持视频、音频、文本，记录内容地址、时长、排序、是否发布、创建医生。

5. `training_day_content` 为训练日程与训练内容的多对多关联，并保存排序号。

6. `training_checkin` 记录开始播放时间、完成时间、播放进度百分比、打卡状态。

### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|training\_category|训练分类|PK：分类ID|父分类ID、分类名称、训练模块、排序号|训练分类树|
|training\_content|训练内容|PK：内容ID|分类ID、内容名称、内容形式、内容说明、内容地址、时长秒、排序号、是否发布、创建医生ID|训练内容库|
|training\_day|训练日程|PK：训练日程ID；UK：训练日序号|日程名称、是否启用|全局训练日程|
|training\_day\_content|日程训练内容|PK：训练日程ID、内容ID|排序号|日程与内容多对多|
|training\_checkin|训练打卡|PK：打卡ID|患者ID、内容ID、训练日程ID、开始播放时间、完成时间、播放进度百分比、打卡状态|患者个人训练进度|

### 举例

患者完成某训练日内容并打卡：

```SQL
INSERT INTO training_checkin (打卡ID, 患者ID, 内容ID, 训练日程ID, 开始播放时间, 完成时间, 播放进度百分比, 打卡状态)
VALUES (80001, 20001, 3001, 1, NOW() - INTERVAL 10 MINUTE, NOW(), 100.00, '已完成');
```

---

## 9\. 模块八：治疗预约与通知模块

### 表关系

- `treatment_item` 1:N `appointment_slot`：治疗项目产生可预约时段。

- `patient` 1:N `appointment`：患者多条预约记录。

- `appointment_slot` 1:N `appointment`：时段被多次预约。

- `clinic_location` 1:N `appointment`：预约记录患者实际选择的就诊院区。

- `account` 1:N `schedule_exception`：账号操作排班例外。

- `account` 1:N `appointment`：核销操作人账号关联预约核销。

- `account` 1:N `notification`：通知接收账号。

- `appointment` 0:N `notification`：预约相关通知。

- `alert` 0:N `notification`：预警相关通知。

    

### 设计规则

1. 两个院区共用同一套治疗项目、预约时段、容量和开放日期规则；`clinic_location` 只记录患者实际选择的就诊院区。

2. 默认周一至周五开放、周六日关闭；排班例外为全局配置，每个日期当前只支持一个连续开放时段。若同一天需要上午、下午多个时段，应增加例外时段明细表。

3. `appointment_slot` 不按院区拆分容量；并发预约需通过事务、行锁或原子更新防止超额。

4. 已有预约的时段不可直接删除或关闭而不处理已有预约。

5. `appointment` 不分配负责医生；核销操作人关联 `account`，仅用于操作审计。

6. 预约二维码保存令牌哈希，不保存原始可用令牌；`预约二维码令牌哈希` 唯一。

7. 患者治疗次数由已完成预约统计得到，不单独维护重复计数。

8. 通知渠道支持小程序、短信；发送状态为待发送、已发送、失败；可关联预约或预警。



### 表说明

|表名|中文名|主键/唯一约束|关键字段|说明|
|---|---|---|---|---|
|clinic\_location|院区|PK：院区ID；UK：院区名称|地址、是否启用|患者实际就诊院区|
|treatment\_item|治疗项目|PK：项目ID|项目名称、项目分类、项目说明及适应症、治疗时长分钟、预约方式、同时预约人数上限、是否启用|治疗项目定义|
|schedule\_exception|排班例外|PK：排班例外ID；UK：例外日期|是否开放预约、开始时间、结束时间、例外说明、操作账号ID|全局排班例外|
|appointment\_slot|可预约时段|PK：时段ID|项目ID、开始时间、结束时间、可预约人数、已预约人数、时段状态|项目时段与容量|
|appointment|预约记录|PK：预约ID；UK：预约二维码令牌哈希|患者ID、时段ID、院区ID、预约状态、预约时间、核销操作人账号ID、核销时间|患者预约与核销|
|notification|系统通知|PK：通知ID|接收账号ID、预约ID、预警ID、通知渠道、通知内容、发送状态、计划发送时间、实际发送时间|预约/预警通知|

### 举例

患者预约某个时段，并生成预约通知：

```SQL
INSERT INTO appointment (预约ID, 患者ID, 时段ID, 院区ID, 预约状态, 预约二维码令牌哈希, 预约时间)
VALUES (90001, 20001, 5001, 1, '待就诊', 'hash_token_value', NOW());

INSERT INTO notification (通知ID, 接收账号ID, 预约ID, 通知渠道, 通知内容, 发送状态, 计划发送时间)
VALUES (91001, 10001, 90001, '小程序', '预约成功，请按时就诊', '待发送', NOW());
```

---

## 10\. 跨模块关键关系汇总

|关系|说明|
|---|---|
|account → patient / doctor|患者、医生共用账号体系，分别一对一关联业务档案|
|patient → examination → 八个检查模块|一次临床检查主记录可挂接多个独立检查模块，每模块最多一条|
|patient → urination\_record / defecation\_record → alert|患者日记数据可触发异常预警|
|questionnaire → questionnaire\_submission → questionnaire\_answer|量表发布、患者填写、答案与得分快照分离|
|training\_day → training\_day\_content → training\_content|全局训练日程与训练内容多对多|
|appointment\_slot → appointment|时段容量与预约并发控制需在事务中保证不超额|
|account → notification|系统通知按接收账号发送，可关联预约或预警|

> 
> 
> 
> 
> 







