# 盆底康复管理平台（pelvic-rehab）

面向盆底康复科室的一体化管理平台：患者/医生管理、康复训练打卡、排尿/排便日记、盆底自评量表与数据报表。

## 项目结构

| 目录 | 说明 | 状态 |
|------|------|------|
| `admin-web/` | 网页后台（PC 管理端）：Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts | ✅ 全部 16 个页面已接真实接口（联调完成） |
| `server/` | 后端服务（Spring Boot 多模块） | ✅ 网页后台后端已实现（5 个业务模块 + 公共组件） |
| `mini-doctor/` | 医生端小程序 | 📋 规划中 |
| `mini-patient/` | 患者端小程序 | 📋 规划中 |
| `design/` | 设计稿 / 原型图 | 📋 规划中 |
| `docs/` | 需求与设计文档 | ✅ ER 图、建表/种子 SQL、后端实现逻辑文档 |
| `deploy/` | 部署脚本与配置 | 📋 规划中 |
| `scripts/` | 开发辅助脚本 | 📋 规划中 |

## 快速开始（新成员从这里开始）

### 环境要求

- JDK 17+（开发环境 JDK 22 已验证）、Maven 3.9+、Node.js 18+、MySQL 8.0+（9.5 已验证）
- Windows 建议先执行：`git config core.quotepath false`（仓库含中文文件名，避免 git 输出乱码）

### 三步跑起来

**1. 初始化数据库**（用 MySQL 客户端依次执行 `docs/` 下三份 SQL）：

```sql
source docs/数据库建表语句.sql;      -- 51 张表（可重复执行）
source docs/数据库种子数据.sql;      -- 角色/权限/字典（一次性，固定主键）
source docs/数据库管理员账号.sql;    -- 管理员账号（一次性）
```

**2. 配置后端数据库密码**（不要把真实密码提交进 git）：

```bash
cd server/pelvic-boot/src/main/resources
cp application-local.yml.example application-local.yml   # 填入本机 MySQL 密码
# 该文件已被 .gitignore 忽略；也可以不建文件，直接设环境变量 MYSQL_PASSWORD
```

**3. 启动前后端**：

```bash
# 后端（8080）
cd server && mvn -pl pelvic-boot spring-boot:run
# 或 mvn -pl pelvic-boot -am package -DskipTests 后
# java -jar pelvic-boot/target/pelvic-boot-1.0.0-SNAPSHOT.jar

# 前端（5173，Vite 代理 /api → 8080）
cd admin-web && npm install && npm run dev
```

打开 http://localhost:5173 ，管理员初始账号：**13900000000 / 123456**（生产环境务必修改，见 `docs/数据库管理员账号.sql` 说明）。

## admin-web（网页后台）

- 技术栈：Vue 3 + TypeScript + Vite 6 + Vue Router 4 + Pinia + Element Plus + ECharts + Axios
- 页面：登录（密码/短信）、用户管理（管理员/医生双形态）、患者/医生详情、数据总览、患者数据详情、报表总览及 7 张报表、个人主页，共 16 页
- 接口前缀：`/api/admin/v1/**`（开发环境经 Vite 代理转发到后端 `http://localhost:8080`）

```bash
cd admin-web
npm install
npm run dev      # 开发模式 http://localhost:5173
npm run build    # 构建（vue-tsc 类型检查 + vite build）
```

> 目录结构、页面 ↔ 代码对照、TODO 清单见 [admin-web/代码结构说明.md](admin-web/代码结构说明.md)。

## server（后端，Spring Boot 多模块）

```text
server/
├── pelvic-boot/          # 启动模块（入口 + application.yml）
├── pelvic-api/           # API 层（聚合业务模块）
├── pelvic-common/        # 公共组件：core / mybatis / security / web（✅ 已实现）
│                         #          excel / log / mq / oss / redis（预留占位）
├── pelvic-job/           # 定时任务（预留占位）
└── pelvic-modules/       # 业务模块
    ├── module-auth       # ✅ 认证授权（验证码/登录/会话/改密）
    ├── module-system     # ✅ 系统管理（用户列表/RBAC/审计日志）
    ├── module-patient    # ✅ 患者（详情/临床检查补充）
    ├── module-doctor     # ✅ 医生（添加/详情/移除）
    ├── module-report     # ✅ 报表（总览/患者记录/7 张报表/异步导出）
    └── module-appointment / assessment / bladder / diary / inventory
        / notify / training / video（预留占位，待对应端需求）
```

> 接口清单、模块依赖关系、已知占位（短信发送/refreshToken/数据范围等）见 [server/README.md](server/README.md)。

## Git 注意事项

1. **不要提交真实密码**：数据库密码放 `server/pelvic-boot/src/main/resources/application-local.yml`（已被 .gitignore 忽略，模板见同目录 `.example` 文件）；`pelvic.encrypt.aes-key` / `hmac-key` 为开发密钥，生产必须更换。
2. **不要提交生成物**：`node_modules/`、`dist/`、`target/`、`export/`（报表导出目录）、`*.tsbuildinfo`、`admin-web/src` 下的 `*.js`（裸跑 `tsc` 会产生，已加入忽略）均已配置 .gitignore；新增生成目录时请同步补充。
3. **中文文件名**：`docs/` 下文档为中文文件名，Windows 建议 `git config core.quotepath false`，否则 git 输出为八进制转义。
4. **换行符**：仓库中同时存在 LF 与 CRLF 文件。Windows 建议 `git config core.autocrlf true`；不要对已跟踪文件做整体 renormalize（会产生整文件 diff）。
5. **SQL 脚本的可重复性**：`数据库建表语句.sql` 用了 `IF NOT EXISTS` 可重复执行；`数据库种子数据.sql` 与 `数据库管理员账号.sql` 固定主键、一次性执行，重复执行会主键冲突（需先清空相关表）。
6. **提交前自检**：后端 `mvn -q compile -DskipTests`、前端 `npx vue-tsc --noEmit`，通过后再提交；前端代码改动请同步更新 `admin-web/代码结构说明.md` 与 `docs/后端实现逻辑.md` 对应小节。
7. **提交信息**：中文常规前缀（feat/fix/docs/chore…）+ 说明，见仓库历史记录风格。

## 常见问题

| 现象 | 原因与处理 |
|------|-----------|
| 登录报"该用户不存在" | `数据库管理员账号.sql` 未执行（管理员账号单独一份 SQL，种子数据里没有账号） |
| 后端启动报 `CannotGetJdbcConnection` / 登录 500 | 数据库密码不对或 MySQL 未启动；检查 `application-local.yml`（见快速开始第 2 步） |
| 前端请求 404 | 后端没起（8080）或 Vite 代理未生效；后端带 `/api` 上下文，前端代理原样转发 |
| 重新打包报 `Unable to rename ...jar` | Windows 上残留的 java 进程锁住了旧 jar：`taskkill //F //IM java.exe` 后再打包 |
| 端口占用 | 后端 8080、前端 5173；用 `netstat -ano \| grep <端口>` 查占用进程 |
| 登录后刷新页面回到登录页 | 已知占位：accessToken 存内存，refreshToken（httpOnly Cookie）流程未实现（见 server/README.md TODO-2） |
| 患者/日记/量表/报表数据为空 | 正常现象：小程序端未开发，暂无业务数据；可手工插入测试数据 |

## 当前进度

- ✅ `admin-web`：16 个页面全部接真实接口（登录/用户管理/患者医生详情/数据总览/7 张报表/个人主页），`vue-tsc` 零错误
- ✅ `server`：网页后台后端 17 个接口全部实现并端到端冒烟验证通过（11/11）
- ✅ `docs`：ER 图（51 表）、建表 SQL、种子数据（角色/权限/字典）、管理员账号、后端实现逻辑文档（28 节，含实现代码对照）
- 📋 待规划：医生端/患者端小程序、refreshToken 自动续期、医生数据范围授权、真实短信服务商、OSS 文件存储
