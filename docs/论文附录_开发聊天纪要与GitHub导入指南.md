# 论文附录：开发聊天纪要与 GitHub 导入指南

## 一、课题与项目概述
- 课题名称：基于 SpringBoot 的专业综合实训管理系统设计与实现
- 技术栈：
  - 后端：Spring Boot 2.7.x + Spring Security + JWT + MyBatis-Plus + MySQL（JDK 1.8）
  - 前端：Vue3 + Vite + Element Plus + Axios
  - 数据库：MySQL 8.x
- 系统目标：构建“实训全过程”管理平台，支持管理员、教师、学生三类角色，覆盖从项目开设、分组、任务布置、提交、答疑、公告到成绩归档的闭环管理。

## 二、聊天开发过程（论文可直接引用）
以下为本项目从“任务书/开题报告”到“可演示系统”的主要迭代过程。

### 1. 初始规划阶段
- 基于任务书与开题报告完成需求重构，形成模块化任务清单与开发顺序。
- 明确架构分层：
  - 表现层（Controller / Vue View）
  - 业务层（Service）
  - 持久层（Mapper / MyBatis）
  - 数据层（MySQL）
- 明确核心功能主线：项目开设 -> 小组组织 -> 任务发布 -> 学生提交 -> 教师评阅/成绩 -> 过程留痕。

### 2. 首版系统落地阶段
- 完成前后端基础骨架与数据库表结构落地。
- 实现核心模块：
  - 用户管理、角色权限、基础数据（学院/专业/班级）
  - 项目模板与开设计划
  - 团队与成员
  - 任务、提交、成绩、在线答疑、公告
  - 系统看板与基础监控
- 完成脚本化初始化（建库、导入演示数据、一键启动）。

### 3. 运行问题修复阶段
- 修复常见连接问题：MySQL `Public Key Retrieval is not allowed`（通过 JDBC 参数与配置修正）。
- 处理后端 404/Whitelabel 误解（说明根路径无页面，前端访问入口在 `5173`）。
- 修复时间反序列化问题：`LocalDateTime` 字符串格式不匹配（统一 `yyyy-MM-dd HH:mm:ss`）。

### 4. 业务逻辑深化阶段（贴近实际教学）
- 强化角色边界：
  - 学生不能越权操作教师/管理员功能。
  - 教师与管理员功能范围区分清晰。
- 班级隔离：
  - 学生仅能看到本班相关项目、公告、任务、答疑与团队。
- 公告优化：
  - 支持查看详情、发布人、班级可见性、未读统计。
- 提交流程优化：
  - 学生直接在任务中提交，不再“任务管理/提交管理”割裂。
  - 教师可在任务内查看各组提交明细、附件、未交小组。

### 5. 小组机制重构阶段
- 由“随意建组”升级为“按开设计划配置分组规则”：
  - 开设计划中配置组数与每组人数上限。
  - 教师可按规则批量生成小组。
  - 学生按项目选择小组加入。
- 组长机制：
  - 先加入者可成为组长（当组长为空）。
  - 支持组长转让；组长退出前需先转让。
- 小组命名优化：
  - 自动/历史命名统一为“第X组”可读风格，避免 A/B/C 命名不直观。

### 6. 界面可用性与体验优化阶段
- UI 风格统一为蓝灰专业风，不使用紫/橙主色。
- 修复多个页面弹窗编辑区域过小的问题，统一弹窗样式与可读布局。
- 登录页重构：
  - 动态背景、视觉层次优化、快捷账号保留。
- 主页面动效优化：
  - 页面切换过渡、按钮/表格/弹窗动画、悬停反馈、低动效兼容。

### 7. 数据展示“可辨识”优化阶段
- 大量字段由“ID语义”改为“名称语义”显示：
  - 如项目名、班级名、教师名、学生名等。
- 弱化“2025-2026-2 信安2201”式难识别标签：
  - 拆分为“项目/学期/班级”分列显示。
- 任务状态可视化：
  - 已完成、已截止、进行中。
- 过滤逻辑优化：
  - 选择条件后自动刷新，减少“选完还要点检索”的多余步骤。

### 8. 管理端增强阶段
- 用户管理新增：
  - 按班级筛选
  - 按姓名首字母排序
  - 默认 ID 正序展示
- 基础数据新增：
  - 学院/专业/班级名称联动显示（不再依赖专业ID记忆）
  - 分页与空页回退，避免翻页空白
  - 默认正序排序，便于论文演示时逻辑一致
- 公告新增发布者视角统计：
  - 已读人数/未读人数

### 9. 成绩模块针对教学场景的最终优化
- 教师录入成绩支持“按科目+班级筛选”。
- 成绩查看支持“学期+科目+班级”多条件筛选。
- 录入弹窗中学生列表按班级自动过滤，贴近实际工作流。

## 三、系统最终能力总结
项目最终已具备以下工程与业务特征：
- 多角色权限分离（管理员/教师/学生）
- 班级级别数据隔离
- 任务-提交-评阅闭环
- 公告已读追踪与统计
- 小组规则化生成与成员治理
- 成绩按教学组织方式录入与筛选
- UI 统一与交互过渡优化
- 脚本化初始化与演示数据支持

## 四、论文可写的技术亮点（建议章节）
1. 权限模型与班级隔离策略设计  
2. 小组规则引擎（组数+组容量）与组长治理机制  
3. 任务提交闭环与进度追踪（未交小组、完成率）  
4. 公告可见性与已读统计机制  
5. 前后端协同与可维护分层设计（SpringBoot+MyBatis+Vue）  
6. 工程化能力（SQL升级脚本、批量种子数据、构建脚本）

## 五、如何把这份聊天纪要迁移到网页端
推荐方式：
1. 直接把本文件上传到网页端对话（作为上下文材料）。
2. 或复制本文件“二~四章”内容，粘贴到网页端让其继续生成“论文正文/摘要/结论”。
3. 如果网页端有字数限制，按章节分段上传。

---

## 六、代码导入 GitHub（Windows 命令行版）
以下步骤适用于本项目根目录：`C:\Users\ASUS\Desktop\homework`

### 1. 在 GitHub 创建空仓库
- 进入 GitHub -> `New repository`
- 仓库名例如：`training-management-system`
- 先不要勾选 README / .gitignore（避免冲突）

### 2. 本地初始化并提交
在项目根目录打开 PowerShell，执行：

```powershell
cd C:\Users\ASUS\Desktop\homework
git init
git add .
git commit -m "feat: initial version of training management system"
```

### 3. 绑定远程仓库并推送
将下面 URL 换成你的仓库地址：

```powershell
git branch -M main
git remote add origin https://github.com/你的用户名/training-management-system.git
git push -u origin main
```

如果提示 `remote origin already exists`，先执行：

```powershell
git remote remove origin
git remote add origin https://github.com/你的用户名/training-management-system.git
git push -u origin main
```

### 4. 后续日常更新
```powershell
git add .
git commit -m "chore: update docs and ui"
git push
```

## 七、代码导入 GitHub（GitHub Desktop 版）
1. 打开 GitHub Desktop -> `Add an Existing Repository from your local drive`
2. 选择 `C:\Users\ASUS\Desktop\homework`
3. 若未初始化，先 `Create a repository`（本地）
4. `Publish repository` 到 GitHub
5. 后续改动直接在 Desktop 里 `Commit` + `Push`

## 八、推送前建议检查
1. `backend/src/main/resources/application.yml` 中数据库密码是否包含敏感信息。  
2. `uploads/` 是否要上传（一般建议忽略）。  
3. `node_modules/`、`target/`、日志文件是否已被 `.gitignore` 排除。  
4. 如果是论文答辩版本，建议打一个 tag：`v1.0-thesis-demo`。
