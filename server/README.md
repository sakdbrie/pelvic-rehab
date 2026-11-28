# server（后端 · 网页后台）

盆底康复管理平台后端。当前实现范围：**网页后台全部接口**（依据 `docs/后端实现逻辑.md` 第 1~28 节），小程序端（mini-doctor / mini-patient）待规划。

## 技术栈

- Java 17 + Spring Boot 3.3.5
- MyBatis-Plus 3.5.7（中文列名映射，见 `docs/数据库建表语句.sql`）
- MySQL 8.0（InnoDB，utf8mb4）
- 轻量会话鉴权：不透明 accessToken（存 SHA-256 哈希于 `auth_session`）+ refreshToken（httpOnly Cookie 联调 TODO），BCrypt 存密码哈希
- EasyExcel 3.3.4（报表异步导出）

## 模块结构

```
server/
├── pelvic-boot/                  # 启动模块：入口 + application.yml
├── pelvic-api/                   # API 层：聚合业务模块
├── pelvic-common/
│   ├── pelvic-common-core/       # Result / PageResult / BizException / AES-HMAC-SHA256 工具
│   ├── pelvic-common-mybatis/    # MyBatis-Plus 配置（分页、Mapper 扫描）
│   ├── pelvic-common-security/   # 登录上下文 / 鉴权拦截器 / @RequirePerm / @RequireRole
│   └── pelvic-common-web/        # 全局异常处理 / MVC 配置
└── pelvic-modules/
    ├── module-auth/              # 发送验证码、密码/短信登录、退出、修改密码（/auth/**）
    ├── module-system/            # 用户列表、角色权限加载、操作审计（/system/**）
    ├── module-patient/           # 患者详情、临床检查补充（/patient/**）
    ├── module-doctor/            # 添加/详情/移除医生（/doctor/**）
    └── module-report/            # 数据总览、患者记录、7 张报表、导出（/report/**）
```

## 快速启动

1. 初始化数据库（按顺序执行）：
   ```sql
   source docs/数据库建表语句.sql;
   source docs/数据库种子数据.sql;
   ```
2. 配置数据库密码（**不要改 application.yml**，避免把真实密码提交进 git）：
   复制 `pelvic-boot/src/main/resources/application-local.yml.example` 为 `application-local.yml`，
   填入本机 MySQL 密码（该文件已被 .gitignore 忽略），或设置环境变量 `MYSQL_PASSWORD`
3. 启动：
   ```bash
   cd server
   mvn clean package -DskipTests
   java -jar pelvic-boot/target/pelvic-boot-1.0.0-SNAPSHOT.jar
   # 或开发模式
   mvn -pl pelvic-boot spring-boot:run
   ```
4. 接口前缀 `http://localhost:8080/api/admin/v1/**`，前端 `admin-web` 的 Vite 代理已指向 8080

## 接口清单（与 docs/后端实现逻辑.md 章节对应）

| 章节 | 接口 |
|------|------|
| 1 | POST /auth/sms-code |
| 2 | POST /auth/login/password、POST /auth/login/sms |
| 5 | GET /system/users |
| 7 | POST /doctor |
| 9 | GET /patient/{patientId} |
| 10 | PUT /patient/{patientId}/supplement |
| 12 | GET /doctor/{doctorId} |
| 13 | DELETE /doctor/{doctorId} |
| 14/15/17 | GET /report/overview |
| 18/19/20 | GET /report/patient/{patientId}/records |
| 24 | GET /report/{reportType}（female/male/urination/defecation/assessment/training/summary） |
| 25 | POST /report/export-task、GET /report/export-task/{taskId}、GET /report/export-task/{taskId}/download |
| 27 | POST /auth/change-password |
| 28 | POST /auth/logout |

## 已知占位 / TODO

1. **短信发送**：`ConsoleSmsSender` 仅打印验证码到日志，生产接入真实短信服务商
2. **refreshToken**：已签发并落库，`/auth/refresh` 与前端 httpOnly Cookie 流程未实现（对应 admin-web TODO-4）
3. **数据范围权限**：角色数据范围（SELF/ALL_PATIENTS/DEPARTMENT/ASSIGNED）除"管理员全部、医生仅患者"外，科室/授权患者级过滤待实现（需配套授权关系设计）
4. **导出**：任务状态在内存、文件落本地 `./export`；生产建议任务表 + 对象存储（OSS）签名 URL
5. **管理员初始账号**：种子数据不含账号（密码哈希/手机号密文需加密组件生成），参考 `docs/数据库种子数据.sql` 附录示例
6. **`pelvic-common` 预留模块**（excel/log/mq/oss/redis）与小程序相关业务模块（appointment 等）保持骨架占位，待对应端需求启动后再实现
