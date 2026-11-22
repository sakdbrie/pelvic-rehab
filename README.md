# 盆底康复管理平台（pelvic-rehab）

面向盆底康复科室的一体化管理平台：患者/医生管理、康复训练打卡、排尿/排便日记、盆底自评量表与数据报表。

## 项目结构

| 目录 | 说明 | 状态 |
|------|------|------|
| `admin-web/` | 网页后台（PC 管理端）：Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts | ✅ 已完成原型全部 16 个页面（数据为本地 mock，接口已预留） |
| `server/` | 后端服务（Spring Boot 多模块） | 🚧 骨架阶段（模块目录已规划，待填充） |
| `mini-doctor/` | 医生端小程序 | 📋 规划中 |
| `mini-patient/` | 患者端小程序 | 📋 规划中 |
| `design/` | 设计稿 / 原型图 | 📋 规划中 |
| `docs/` | 需求与设计文档 | 📋 规划中 |
| `deploy/` | 部署脚本与配置 | 📋 规划中 |
| `scripts/` | 开发辅助脚本 | 📋 规划中 |

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

> 目录结构、页面 ↔ 代码对照、mock 替换步骤与待办事项见 [admin-web/代码结构说明.md](admin-web/代码结构说明.md)。

## server（后端，Spring Boot 多模块）

```text
server/
├── pelvic-boot/          # 启动模块
├── pelvic-api/           # API 层
├── pelvic-common/        # 公共组件：core / excel / log / mq / mybatis / oss / redis / security / web
├── pelvic-dependencies/  # 依赖版本管理
├── pelvic-job/           # 定时任务
└── pelvic-modules/       # 业务模块
    ├── module-appointment    # 预约
    ├── module-assessment     # 评估
    ├── module-auth           # 认证授权
    ├── module-bladder        # 膀胱管理
    ├── module-diary          # 日记（排尿/排便）
    ├── module-doctor         # 医生
    ├── module-inventory      # 库存
    ├── module-notify         # 通知
    ├── module-patient        # 患者
    ├── module-report         # 报表
    ├── module-system         # 系统管理
    ├── module-training       # 康复训练
    └── module-video          # 视频/宣教
```

> 当前为骨架：模块目录已按业务拆分，`pom.xml` 与各模块代码待填充。

## 当前进度

- ✅ `admin-web`：全部 16 个页面完成，数据为原型占位 mock，接口层按架构预留，联调时替换（见 `admin-web/代码结构说明.md` 第 6、8 节）
- 🚧 `server`：模块骨架已就绪，代码待开发
