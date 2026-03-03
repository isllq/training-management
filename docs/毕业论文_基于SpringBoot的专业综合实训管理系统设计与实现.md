# 五邑大学本科毕业设计（论文）

## 题目
基于SpringBoot的专业综合实训管理系统设计与实现

## 学生信息
- 学院：电子与信息工程学院
- 专业：电子信息工程（信息安全）
- 学号：3122001468
- 姓名：梁露镪
- 指导教师：王柱

---

## 摘要
专业综合实训是应用型本科人才培养的重要环节，其核心目标不只是完成一个课程项目，而是让学生在真实工程约束下完成需求分析、团队协作、阶段交付、质量复盘和结果答辩。传统的实训管理方式通常依赖群聊通知、线下表格和分散文档，导致“任务下达不统一、进度状态不可视、过程数据不可追溯、评价口径不一致”等问题，教师管理成本高，学生协作效率低，学院层面的教学质量分析也缺乏数据支撑。

针对上述问题，本文结合任务书和开题报告提出的目标，设计并实现了一套基于 Spring Boot 的专业综合实训管理系统。系统面向管理员、指导教师、学生三类用户，围绕“项目开设-团队组建-任务发布-过程提交-公告答疑-成绩评定-统计分析”构建完整业务闭环。后端采用 Spring Boot 2.7.18、Spring Security、JWT、MyBatis-Plus 与 MySQL；前端采用 Vue3、Vite 与 Element Plus，形成前后端分离架构。系统支持角色权限控制、班级范围隔离、附件上传下载、公告阅读统计、答疑待回复识别、任务提交进度跟踪等功能。

在评价机制方面，本文将开题报告中“多元综合评价”目标工程化落地，构建了综合成绩模型：

$$
S_{total}=\alpha S_{process}+\beta C_{team}+\gamma S_{final},\quad \alpha+\beta+\gamma=1
$$

其中，$S_{process}$ 为过程得分，$C_{team}$ 为团队协作得分，$S_{final}$ 为答辩与成果得分。系统采用 BigDecimal 实现高精度计算，并在服务启动和写入成绩前进行权重合法性校验（范围校验 + 和约束容差校验），避免配置错误导致结果失真。针对组队并发场景，系统通过事务边界和 `FOR UPDATE` 行级锁实现人数上限一致性控制。

本文完成了系统需求分析、总体架构设计、数据库设计、关键模块实现与测试验证。结合 `seed.sql` 演示数据可复现实训全过程：45 名用户、7 条开设计划、10 个团队、27 项任务、25 条提交、15 条公告、12 个答疑主题、32 条成绩记录。测试结果表明，系统在功能完整性、权限边界与数据一致性方面满足任务书要求，具备毕业设计工程原型应有的可用性与可扩展性。

**关键词**：专业综合实训；过程追踪；Spring Boot；Vue3；JWT；RBAC；综合评价模型

---

## Abstract
Comprehensive professional training is a key practical stage in undergraduate engineering education. Its objective is not only project completion, but also process-oriented competence development, including requirement analysis, team collaboration, milestone delivery, quality review, and final defense. Traditional management practices in many training courses still rely on fragmented communication and offline documents, leading to poor traceability, low coordination efficiency, and inconsistent grading criteria.

This thesis designs and implements a Spring Boot based training management system for three user roles: administrator, instructor, and student. The system supports a full lifecycle workflow: project publish, team formation, task assignment, online submission, announcement and Q&A interaction, score evaluation, and dashboard analytics. The backend is implemented with Spring Boot 2.7.18, Spring Security, JWT, MyBatis-Plus, and MySQL; the frontend is built with Vue3, Vite, and Element Plus.

To realize multi-dimensional assessment, a weighted score model is implemented on the backend:

$$
S_{total}=\alpha S_{process}+\beta C_{team}+\gamma S_{final},\ \alpha+\beta+\gamma=1
$$

The model is integrated with range checks, sum constraints, and BigDecimal-based calculation for reproducibility. For team-joining concurrency, transactional design with row-level lock (`FOR UPDATE`) is adopted to prevent capacity oversell.

The implemented prototype is verified with reproducible demo data and functional tests. Results indicate that the system can effectively support process-oriented training management and provides a feasible digital solution for university practical teaching scenarios.

**Keywords**: training management; process traceability; Spring Boot; Vue3; JWT; RBAC; weighted score model

---

## 第1章 绪论

### 1.1 课题背景
近年来，高校工程教育越来越强调“以项目驱动能力培养”。对信息安全专业而言，专业综合实训承担着将课程知识转化为工程能力的关键任务，教学过程通常包括项目立项、需求分析、分组协作、阶段交付、中期检查、结题答辩等环节。传统管理方式存在以下共性问题：

1. 通知与任务分散：实训信息在多个群聊中传递，容易遗漏与版本混乱。
2. 过程监管困难：教师难以实时掌握每个小组是否按节点推进。
3. 评价依据不足：成绩常以结题成果为主，过程贡献和团队协作缺少量化依据。
4. 数据沉淀不足：学院无法快速获取跨班级、跨项目的统计数据，用于教学优化。

因此，构建一套可追踪、可统计、可复核的实训管理系统，具有明确的现实需求与应用价值。

### 1.2 研究目的与意义
本课题目标是基于 Spring Boot 与 Vue 构建一套支持实训全过程管理的平台，通过系统化工具将“教学管理经验”转化为“标准化流程 + 数据化规则”。其意义包括：

1. 对教学管理：实现业务流程线上化，减少重复协调成本。
2. 对教师指导：通过提交进度、答疑记录、公告阅读等数据实现精准干预。
3. 对学生学习：明确阶段目标和责任边界，培养工程协作规范。
4. 对质量改进：沉淀可分析的数据资产，为课程迭代提供证据支持。

### 1.3 国内外研究现状
国内同类系统多基于 Spring Boot + Vue 技术栈，功能上已从“设备/资源管理”延伸到“课程过程管理”，但仍有两个常见不足：

1. 业务闭环不完整：任务、提交、成绩、答疑分散在不同流程，数据难以关联。
2. 评价模型弱化：系统虽有成绩录入，但缺少可解释、可验证的量化计算逻辑。

国外平台更强调 Learning Analytics 与项目制学习（PBL）数据跟踪，但在班级组织、考核制度、角色权限等方面与国内高校管理模式并非完全一致。基于此，本文采用“本土教学场景 + 工程可落地实现”的路线，强调流程适配性与实现可复核性。

### 1.4 研究内容
围绕任务书要求，本文完成以下研究与实现工作：

1. 需求分析：抽取管理员、教师、学生三类角色的功能和非功能需求。
2. 架构设计：构建前后端分离架构，明确控制层、业务层、数据层职责。
3. 数据建模：设计核心业务表与约束，覆盖项目、开设、团队、任务、提交、公告、答疑、成绩。
4. 关键实现：实现认证授权、班级隔离、组队并发控制、任务提交闭环、综合评价模型。
5. 测试验证：完成功能、安全边界、一致性与可运行性验证。

### 1.5 技术路线
技术路线如下：

1. 以任务书业务流程为主线，建立“项目开设驱动”的领域模型。
2. 采用 Spring Security + JWT 实现统一认证入口和无状态会话。
3. 采用 MyBatis-Plus 与注解 SQL 混合方式，兼顾开发效率与复杂查询可控性。
4. 在关键写操作中使用事务、唯一约束和行锁确保一致性。
5. 前端以“角色可见页面 + 同页闭环操作”为原则优化交互流程。

### 1.6 论文结构安排
全文共七章：

1. 第1章 绪论：研究背景、意义、内容与技术路线。
2. 第2章 相关技术与理论基础：系统实现所依赖技术与评价模型。
3. 第3章 需求分析：角色需求、流程需求、约束需求与追溯关系。
4. 第4章 系统设计：架构分层、数据库、接口、权限与关键机制设计。
5. 第5章 系统实现：核心模块实现细节与关键代码逻辑。
6. 第6章 系统测试：测试计划、用例、结果与分析。
7. 第7章 总结与展望：工作总结、局限与后续改进方向。

---

## 第2章 相关技术与理论基础

### 2.1 前后端分离架构
前后端分离是当前信息系统开发主流模式。其核心思想是：

1. 前端负责交互与展示。
2. 后端负责业务规则与数据处理。
3. 通过 API 完成松耦合通信。

在本课题中，该模式的优势体现在：

1. 角色页面可快速迭代（管理员、教师、学生视图差异明显）。
2. 安全策略集中于后端，避免把权限逻辑散落在前端。
3. 后续可扩展移动端或小程序端复用 API。

### 2.2 Spring Boot
Spring Boot 2.7.18 用于构建后端服务，主要贡献包括：

1. 自动配置简化了基础工程搭建。
2. 与 Spring MVC、Validation、Security、Test 生态兼容性高。
3. 易于按模块组织 Controller、Service、Mapper。

结合本项目，Spring Boot 主要承载以下能力：

1. REST API 暴露。
2. 统一异常处理与响应封装。
3. 配置管理（数据库、JWT、评分参数、跨域来源）。

### 2.3 Spring Security 与 JWT
本系统采用“Security 过滤链 + JWT 令牌”的组合方案。

1. 认证：用户登录后签发 JWT，后续请求由过滤器解析 token。
2. 授权：Security 进行 URL 级角色校验，业务层 `RoleGuard` 做细粒度控制。
3. 会话：采用无状态 `STATELESS` 模式，降低服务端会话维护复杂度。

该方案既满足课程系统轻量部署要求，也为后续扩展（黑名单、单点登录）留出空间。

### 2.4 MyBatis-Plus 与 MySQL
系统使用 MyBatis-Plus 作为数据访问基础，关键复杂查询使用注解 SQL 显式实现。该策略兼顾了：

1. 常规 CRUD 开发效率。
2. 聚合统计和范围过滤 SQL 的可读性与可调优性。
3. 与项目现有数据结构兼容。

MySQL 负责事务、索引、唯一约束和外键约束，是保证数据一致性的核心基础设施。

### 2.5 Vue3 + Vite + Element Plus
前端采用 Vue3 组合式 API，配合 Element Plus 组件完成业务页面。实践中重点使用：

1. 表格 + 弹窗 + 抽屉：支撑“列表查看 + 详情操作”主流程。
2. 路由守卫：未登录强制回登录页，越权自动重定向。
3. 统一主题样式：蓝灰专业风格，避免过度装饰化 UI。

### 2.6 评价模型理论基础
开题报告提出从“终结性评价”转向“过程+协作+成果”综合评价。本文采用线性加权模型并在系统中工程化实现：

$$
S_{total}=\alpha S_{process}+\beta C_{team}+\gamma S_{final}
$$

模型使用条件：

1. 权重非负：$\alpha,\beta,\gamma \in [0,1]$。
2. 权重归一：$\alpha+\beta+\gamma=1$。
3. 分值范围：三项输入分均在 0 到 100 之间。

工程上，模型不仅要“能算”，还要满足“可解释、可复核、可配置、可追责”。

---

## 第3章 需求分析

### 3.1 角色需求分析

#### 3.1.1 管理员需求
管理员关注的是“系统治理”，核心需求包括：

1. 用户与角色权限维护。
2. 学院/专业/班级基础数据维护。
3. 登录日志与系统运行状态监控。
4. 全局数据看板统计。

#### 3.1.2 指导教师需求
教师关注的是“教学组织与过程控制”，核心需求包括：

1. 项目模板创建与开设计划发布。
2. 团队规则设置与小组管理。
3. 阶段任务发布、提交进度跟踪、未交小组识别。
4. 公告发布、答疑回复、成绩录入与导出。

#### 3.1.3 学生需求
学生关注的是“任务执行与个人可见信息”，核心需求包括：

1. 查看本班可见项目与任务。
2. 加入团队并参与协作。
3. 在任务内提交说明和附件。
4. 查看公告详情与发布人。
5. 参与答疑并查看回复人。
6. 查看个人资料和成绩。

### 3.2 业务流程需求

### 3.2.1 全流程主线
系统采用“开设计划驱动”的流程：

1. 教师发布开设计划（指定学期、班级、分组规则）。
2. 教师生成团队或学生自主加入团队。
3. 教师发布阶段任务并附带附件。
4. 学生在任务内提交成果（文本 + 附件）。
5. 教师跟踪提交进度、处理答疑、发布公告。
6. 教师录入三项分值，系统自动计算总评。

### 3.2.2 团队流程关键规则
1. 学生仅可加入本人班级可见开设下的团队。
2. 一个学生在同一开设下只能属于一个团队。
3. 组长可转让给本组成员。
4. 团队人数受开设配置上限约束。

### 3.2.3 任务流程关键规则
1. 教师发布任务需绑定开设计划。
2. 学生提交任务前必须先加入团队。
3. 逾期任务状态需可识别（进行中、已截止、已完成）。
4. 教师可从任务页面直接查看提交详情与附件。

### 3.2.4 公告与答疑流程规则
1. 公告支持全体或定向开设可见。
2. 学生可查看公告详情并标记已读。
3. 教师可查看公告已读/未读统计。
4. 答疑主题需显示发起人、回复人，便于责任追踪。

### 3.3 非功能需求

### 3.3.1 安全性需求
1. 所有业务接口默认需认证。
2. 管理员接口必须后端硬校验角色。
3. 学生数据访问需按班级范围约束。
4. 异常返回需统一 JSON 格式，避免前端无法识别。

### 3.3.2 一致性需求
1. 重复入组必须幂等或拒绝。
2. 重复已读不可产生脏数据。
3. 同开设同学生成绩记录必须唯一。
4. 并发入组时不能突破人数上限。

### 3.3.3 可维护性与可扩展性需求
1. 模块边界清晰：认证、业务、数据分层。
2. 参数配置外置：评分权重、JWT 密钥、跨域来源。
3. 支持增量升级：SQL 升级脚本保留版本化。
4. 支持新增业务：附件业务类型和统计指标可扩展。

### 3.4 用例分析（文字化）

1. 用例 A：教师发布阶段任务
- 参与者：教师
- 前置条件：已登录且具有教师角色
- 主流程：选择开设 -> 填写任务 -> 上传附件 -> 发布
- 后置结果：任务可被对应班级学生查看并提交

2. 用例 B：学生提交任务
- 参与者：学生
- 前置条件：已登录、已加入团队、任务可见
- 主流程：打开任务详情 -> 填写提交说明 -> 上传附件 -> 提交
- 后置结果：教师可在提交详情查看该组最新提交

3. 用例 C：教师录入成绩
- 参与者：教师
- 前置条件：学生完成阶段任务并进入评分阶段
- 主流程：选择学期/科目/班级 -> 录入三项分 -> 保存
- 后置结果：系统自动生成综合总评并可导出

### 3.5 需求追溯矩阵

| 任务书要求 | 实现模块 | 关键文件 |
|---|---|---|
| 学生端个人信息管理 | 个人资料页 + `/api/auth/profile` | `ProfileView.vue`、`AuthController.java` |
| 团队创建/加入与管理 | 团队管理模块 | `TeamView.vue`、`TeamController.java` |
| 开题/中期/结题文档提交 | 任务与提交一体化 | `TaskView.vue`、`SubmissionController.java` |
| 教师发布任务与考核节点 | 项目开设 + 任务模块 | `ProjectController.java`、`TaskController.java` |
| 在线答疑 | 答疑主题与回复模块 | `QaView.vue`、`QaController.java` |
| 成绩评定与导出 | 成绩模块 | `ScoreView.vue`、`ScoreServiceImpl.java` |
| 管理员导入用户 | Excel 导入接口 | `UserController.java` |
| 系统运行与日志监控 | 系统监控模块 | `SystemController.java` |

---

## 第4章 系统设计

### 4.1 总体技术架构
系统采用前后端分离架构，部署结构为：

1. 前端（Vite Dev 或静态资源）
2. 后端（Spring Boot 应用）
3. 数据库（MySQL）
4. 本地文件存储目录（`uploads/`）

架构设计目标：

1. 将权限与规则放到后端，保证安全可信。
2. 将交互流程放到前端，保证使用效率。
3. 将一致性约束放到数据库与事务中，保证数据正确。

### 4.2 架构分层与职责

1. 表现层（View/Router）
- 负责用户交互、状态展示、可视化提示
- 通过路由 meta 控制页面访问范围

2. 接口层（Controller）
- 负责参数接收、请求分发、响应封装
- 不承载重计算与复杂业务规则

3. 业务层（Service）
- 负责流程编排、规则校验、事务边界定义
- 处理异常语义并抛出业务异常

4. 持久层（Mapper）
- 负责 SQL 执行与对象映射
- 对复杂统计查询进行数据库侧聚合

5. 数据层（MySQL）
- 负责事务、索引、唯一约束、外键约束
- 通过 schema 固化业务不变量

### 4.3 认证与授权设计

### 4.3.1 认证设计
登录成功后签发 JWT，后续请求由过滤器解析并建立认证上下文：

1. `uid`：用户 ID
2. `utype`：用户类型（ADMIN/TEACHER/STUDENT）
3. `uname`：用户名

过滤器对无 token 或无效 token 请求直接返回 401。

### 4.3.2 授权设计
授权采用“双层控制”：

1. Security 路由级控制：管理员域接口使用 `hasRole("ADMIN")`。
2. RoleGuard 业务级控制：如“仅学生可提交”“仅组长可转让”。

### 4.3.3 数据范围设计
对于学生角色，系统在后端强制收敛查询范围：

1. 学生班级从数据库读取，不信任前端传参。
2. 学生访问开设、团队、任务、答疑时必须匹配班级范围。
3. 当开设配置支持多班级字符串时，采用分隔符兼容匹配。

### 4.4 数据库设计

### 4.4.1 E-R 关系描述
核心关系如下：

1. `train_project` 1:N `train_project_publish`
2. `train_project_publish` 1:N `train_team`、1:N `train_task`
3. `train_team` 1:N `train_team_member`
4. `train_task` 1:N `train_submission`
5. `train_announcement` 1:N `train_announcement_read`
6. `train_qa_thread` 1:N `train_qa_reply`
7. `train_project_publish` + `sys_user(student)` 形成 `train_score_final`

### 4.4.2 核心表说明

1. 用户表 `sys_user`
- 存储用户基础信息与角色类型
- 学生通过 `class_name` 标识班级归属

2. 开设表 `train_project_publish`
- 关联项目模板、授课教师、学期、班级、分组规则

3. 团队与成员表
- `train_team` 保存组名、组长、审核信息
- `train_team_member` 保存成员关系与组内角色

4. 任务与提交表
- `train_task` 定义阶段任务、截止时间、权重
- `train_submission` 保存每次提交内容与反馈

5. 成绩表 `train_score_final`
- 保存三项分值与自动计算后的总评

### 4.4.3 一致性约束设计

```sql
UNIQUE KEY uk_team_student(team_id, student_id)
UNIQUE KEY uk_publish_student(publish_id, student_id)
UNIQUE KEY uk_announcement_user(announcement_id, user_id)
```

以上约束分别对应：成员去重、成绩唯一、公告已读幂等。

### 4.5 关键业务设计

### 4.5.1 团队人数上限并发控制
问题场景：两个学生同时加入同一团队，若先计数再插入，可能突破人数上限。

设计方案：

1. `TeamServiceImpl.addMember` 添加事务。
2. 使用 `TrainTeamMapper.selectByIdForUpdate` 锁定团队行。
3. 在锁内执行人数校验和成员写入。

该设计保证“同一团队入组”串行化，消除超卖风险。

### 4.5.2 软删除复活策略
成员退组后保留历史关系，重新加入时复活记录而非新建记录，优势：

1. 保留历史轨迹。
2. 避免重复主键关系膨胀。
3. 与唯一键约束保持一致。

### 4.5.3 公告阅读统计策略
公告查询时直接回传：

1. 当前用户是否已读 `read_flag`
2. 已读人数 `read_count`
3. 目标人数 `target_count`
4. 未读人数 `unread_count`

避免前端二次聚合，提高统计一致性与渲染效率。

### 4.6 接口设计

### 4.6.1 统一响应模型
后端统一使用 `ApiResponse<T>`：

1. `code`：业务码
2. `message`：提示信息
3. `data`：业务数据

统一响应格式可降低前端异常分支复杂度。

### 4.6.2 关键接口示例

1. 认证：`POST /api/auth/login`
2. 团队入组：`POST /api/teams/{teamId}/join`
3. 任务提交：`POST /api/submissions`
4. 公告已读：`PUT /api/announcements/{id}/read`
5. 成绩公式：`GET /api/scores/formula`
6. 成绩录入：`POST /api/scores`

### 4.7 交互与UI设计原则

1. 主导航按“教学流程 + 系统治理”分组，符合角色认知。
2. 任务、提交、附件在一个页面闭环处理，减少跳转。
3. 看板聚焦待办数据（未读公告、临期任务、逾期任务）。
4. 统一蓝灰专业风格，避免浮夸视觉噪声。

### 4.8 关键时序设计（文字化时序图）

### 4.8.1 登录认证时序
1. 前端发起 `POST /api/auth/login` 请求（用户名、密码、验证码）。
2. `AuthController` 调用 `AuthServiceImpl.login`。
3. `AuthServiceImpl` 查询 `sys_user`，校验状态和密码。
4. 登录成功后调用 `JwtUtil.generateToken` 生成 token。
5. 写入 `sys_login_log`，返回 token 与用户基础信息。
6. 前端保存 token 到本地，并跳转角色默认首页。

### 4.8.2 学生提交任务时序
1. 学生在任务页点击“提交任务”。
2. 前端请求 `GET /api/teams/my?publishId=...` 校验是否已入组。
3. 若未入组，前端提示先加入团队；若已入组，展示提交弹窗。
4. 前端调用 `POST /api/submissions` 提交文本与链接信息。
5. 后端校验学生身份、班级范围、入组状态和任务有效性。
6. 若该学生该任务已有提交，走更新逻辑；否则插入新记录。
7. 前端继续调用 `POST /api/files/upload` 上传附件并绑定业务ID。
8. 教师端任务提交抽屉实时可见该组最新提交。

### 4.8.3 成绩录入时序
1. 教师在成绩页选择学期、科目、班级和学生。
2. 前端提交三项分值到 `POST /api/scores`。
3. `ScoreServiceImpl` 校验权重、分值范围并计算总评。
4. 写入 `train_score_final`。
5. 前端刷新列表，展示综合总评。

### 4.9 数据字典（正文节选）

### 4.9.1 `sys_user` 字段字典

| 字段 | 类型 | 含义 | 约束 |
|---|---|---|---|
| id | BIGINT | 用户主键 | 主键自增 |
| username | VARCHAR(64) | 登录账号 | 唯一 |
| password_hash | VARCHAR(255) | 密码哈希/演示密码 | 非空 |
| real_name | VARCHAR(64) | 真实姓名 | 非空 |
| user_type | VARCHAR(32) | 角色类型 | ADMIN/TEACHER/STUDENT |
| class_name | VARCHAR(64) | 学生班级 | 仅学生使用 |
| status | TINYINT | 启用状态 | 1启用/0禁用 |
| is_deleted | TINYINT | 逻辑删除 | 0有效/1删除 |

### 4.9.2 `train_project_publish` 字段字典

| 字段 | 类型 | 含义 | 说明 |
|---|---|---|---|
| project_id | BIGINT | 项目模板ID | 关联 `train_project` |
| term_name | VARCHAR(64) | 学期名 | 如 `2025-2026-2` |
| class_name | VARCHAR(64) | 适用班级 | 支持多班级字符串 |
| teacher_id | BIGINT | 指导教师ID | 关联 `sys_user` |
| group_count | INT | 计划小组数 | 自动建组依据 |
| group_size_limit | INT | 每组人数上限 | 0表示不限 |
| assessment_standard | TEXT | 考核标准文本 | 前端展示 |
| start_date/end_date | DATE | 开设周期 | 进度管理依据 |

### 4.9.3 `train_task` 字段字典

| 字段 | 类型 | 含义 | 说明 |
|---|---|---|---|
| publish_id | BIGINT | 开设ID | 任务所属开设 |
| stage_type | VARCHAR(32) | 阶段类型 | OPENING/MIDTERM/FINAL/DAILY |
| title | VARCHAR(128) | 任务标题 | 必填 |
| content | TEXT | 任务说明 | 支持详细描述 |
| deadline | DATETIME | 截止时间 | 用于状态计算 |
| weight | INT | 评分权重 | 0-100 |

### 4.9.4 `train_submission` 字段字典

| 字段 | 类型 | 含义 | 说明 |
|---|---|---|---|
| task_id | BIGINT | 任务ID | 必填 |
| team_id | BIGINT | 团队ID | 由后端填充 |
| student_id | BIGINT | 学生ID | 由后端填充 |
| version_no | INT | 版本号 | 当前实现为1 |
| content | TEXT | 提交说明 | 主要内容 |
| file_url | VARCHAR(255) | 参考链接 | 可选 |
| submit_time | DATETIME | 提交时间 | 自动填充 |
| status | TINYINT | 提交状态 | 1已提交/2已批阅 |

### 4.10 接口契约与错误码设计

### 4.10.1 统一响应契约
系统采用统一响应体：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

约定：
1. `code=0` 表示成功。
2. 非0表示业务错误或鉴权错误。
3. 前端统一根据 `code` 分支处理提示与重试。

### 4.10.2 典型错误码策略
1. `401`：未登录、token无效、token解析失败。
2. `4030`：无权限操作（角色或数据范围不满足）。
3. `4001`：学生未绑定班级，无法访问班级数据。
4. 业务错误：如“团队不存在”“任务不存在”“人数已满”。

这种“HTTP状态码 + 业务码 + 业务消息”组合方式，便于前端提示和日志排障。

---

## 第5章 系统实现

### 5.1 开发环境与项目结构

### 5.1.1 开发环境
- JDK：1.8
- Maven：3.8+
- Node.js：18+
- MySQL：8.x
- IDE：IntelliJ IDEA + VS Code

### 5.1.2 目录结构

```text
backend/src/main/java/com/training
  controller/
  service/impl/
  mapper/
  security/
  common/
frontend/src
  views/
  api/
  router/
  layout/
sql/
  schema.sql
  seed.sql
```

### 5.2 认证与登录实现

### 5.2.1 登录服务实现
文件：`AuthServiceImpl.java`

1. 根据用户名查询用户。
2. 校验用户状态。
3. 校验密码（兼容 BCrypt 或明文演示密码）。
4. 生成 JWT 并写登录日志。

该设计兼顾演示数据兼容性与后续密码加密迁移能力。

### 5.2.2 JWT 过滤器实现
文件：`JwtAuthenticationFilter.java`

关键逻辑：

```java
String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
    writeUnauthorized(response, "未登录或token无效");
    return;
}
```

```java
Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType))
```

通过 `ROLE_` 前缀与 Security 的 `hasRole` 规则保持一致。

### 5.3 权限与范围控制实现

### 5.3.1 URL 级权限
文件：`SecurityConfig.java`

```java
.antMatchers("/api/users/**", "/api/base/**", "/api/system/**", "/api/rbac/**").hasRole("ADMIN")
.anyRequest().authenticated();
```

### 5.3.2 业务级权限
文件：`RoleGuard.java`

提供 `requireAdmin`、`requireTeacherOrAdmin`、`requireSelfOrTeacherOrAdmin` 等方法，覆盖“同接口不同角色行为差异”的场景。

### 5.3.3 班级范围控制
文件：`UserScopeService.java`

学生访问时，系统从 token 中取 uid，再从数据库取 `class_name`，避免前端伪造班级参数。

### 5.4 项目开设与团队实现

### 5.4.1 团队生成实现
文件：`TeamController.java` 的 `/api/teams/generate`

根据开设配置 `group_count` 自动生成“第N组”，解决教师手工建组效率低问题。

### 5.4.2 入组与组长机制
文件：`TeamServiceImpl.java`

1. 首个入组成员在无组长时自动成为组长。
2. 组长转让时，确保受让人必须是本组成员。
3. 删除组长成员时，若组内仍有成员则自动补位。

### 5.4.3 并发一致性
文件：`TrainTeamMapper.java`

```java
@Select("... FOR UPDATE")
TrainTeam selectByIdForUpdate(@Param("id") Long id);
```

配合事务确保人数上限检查原子化执行。

### 5.5 任务与提交一体化实现

### 5.5.1 任务进度聚合
文件：`TrainTaskMapper.java`

SQL 同时返回：

1. `expected_group_count`（应交小组数）
2. `submitted_group_count`（已交小组数）
3. `missing_team_names`（未交小组名称）

前端无需二次拼装即可展示“完成率 + 未交清单”。

### 5.5.2 学生提交行为约束
文件：`SubmissionController.java`

1. 仅学生可提交。
2. 未加入团队禁止提交。
3. 教师不可直接修改学生提交内容，避免责任边界模糊。

### 5.5.3 附件策略
文件：`FileController.java` + `TrainFileAssetMapper.java`

1. 任务附件与提交附件分业务类型存储。
2. 删除业务记录采用逻辑删除，保留审计可能。
3. 下载接口统一权限控制后开放。

### 5.6 公告与答疑实现

### 5.6.1 公告可见性与阅读统计
文件：`TrainAnnouncementMapper.java`

通过多条件 SQL 实现：

1. 全体公告与班级定向公告并存。
2. 阅读状态实时查询。
3. 已读/未读统计回传教师端。

### 5.6.2 答疑待回复排序
文件：`TrainQaThreadMapper.java`

```sql
ORDER BY CASE WHEN reply_count = 0 THEN 0 ELSE 1 END, q.id DESC
```

实现“未回复优先”，提高教师响应效率。

### 5.6.3 回复人可追踪
答疑回复查询包含 `creator_name`，学生可明确看到是谁回复，满足真实教学沟通场景。

### 5.7 成绩模型实现

### 5.7.1 权重校验
文件：`ScoreServiceImpl.java`

```java
if (!isWeightInRange(alpha) || !isWeightInRange(beta) || !isWeightInRange(gamma)) {
    throw new BizException("成绩权重配置错误：alpha、beta、gamma必须在0到1之间");
}
```

```java
BigDecimal sum = alpha.add(beta).add(gamma);
BigDecimal delta = sum.subtract(BigDecimal.ONE).abs();
if (delta.compareTo(new BigDecimal("0.0001")) > 0) {
    throw new BizException("成绩权重配置错误：alpha+beta+gamma必须等于1");
}
```

### 5.7.2 高精度计分

```java
return p.multiply(alpha)
        .add(t.multiply(beta))
        .add(f.multiply(gamma))
        .setScale(2, RoundingMode.HALF_UP);
```

优势：

1. 避免浮点累积误差。
2. 与成绩展示保留两位小数一致。
3. 便于复核和导出。

### 5.8 看板与待办实现

### 5.8.1 看板指标
文件：`DashboardServiceImpl.java`

统计包含：用户数、项目数、团队数、任务数、提交数、答疑数、待审核团队数、未读公告数。

### 5.8.2 待办跳转
文件：`DashboardView.vue`

“当前待办”支持点击直达对应模块，降低教师日常操作路径长度。

### 5.9 前端路由与会话实现

### 5.9.1 路由守卫
文件：`frontend/src/router/index.js`

1. 未登录访问业务路由自动跳转登录。
2. 已登录访问登录页自动跳转默认首页。
3. 越权路由自动回退到角色默认首页。

### 5.9.2 登录验证码
文件：`LoginView.vue`

登录页引入验证码二次确认，减少误登录和简单脚本撞库风险（演示级）。

### 5.10 系统可复现实现

1. `schema.sql`：完整建表。
2. `seed.sql`：全流程演示数据。
3. `start-all.bat` 与 `restart-all.bat`：一键启动/重启。
4. `init-db.bat`：快速初始化数据库。

### 5.11 异常处理与鲁棒性实现

### 5.11.1 全局异常处理
项目通过全局异常处理器统一捕获运行时异常与业务异常，确保前端拿到稳定 JSON，而不是默认 HTML 错误页。其价值在于：

1. 提升用户可理解性：错误信息可直观展示。
2. 降低排障成本：日志和响应语义一致。
3. 避免接口联调中“格式漂移”问题。

### 5.11.2 业务异常语义化
业务层以 `BizException` 抛出可解释错误，例如：
1. “仅学生可提交任务”
2. “该小组人数已满，请选择其他小组”
3. “当前学生未绑定班级，无法查看班级数据”

相比通用异常，这种方式更贴近教学管理场景。

### 5.11.3 配置错误 fail-fast
`ScoreServiceImpl` 在系统启动时执行权重校验。若配置非法，系统直接报错阻断启动，防止错误口径静默进入生产数据。

### 5.12 安全实现细节补充

### 5.12.1 密钥外置与跨域收敛
JWT 密钥与允许跨域来源均通过 `application.yml` 和环境变量管理，避免硬编码泄漏风险。后端同时在 Security 与 MVC 配置中统一引用该来源配置，防止策略冲突。

### 5.12.2 ThreadLocal 上下文清理
`JwtAuthenticationFilter` 在 `finally` 中清理 `AuthContext`，避免线程复用造成的身份污染。该细节对线程池环境尤为重要。

### 5.12.3 权限双保险
尽管前端已经做了路由限制，后端仍做严格角色和范围校验，防止通过 Postman 或脚本绕过前端直接调用高权限接口。

### 5.13 数据访问实现细节

### 5.13.1 复杂查询下沉SQL
对于任务进度、公告统计这类聚合查询，项目采用注解 SQL 下沉到数据库执行，减少 Java 侧二次遍历拼装，提升查询效率并降低内存开销。

### 5.13.2 逻辑删除策略
多数业务表采用 `is_deleted` 逻辑删除，优势：
1. 保留历史痕迹，便于教学追溯。
2. 降低误删风险。
3. 支持后续审计与恢复需求。

### 5.13.3 索引策略
在高频过滤字段上建立索引，如：
1. 用户：`idx_user_type`、`idx_user_class`
2. 开设：`idx_publish_class`
3. 任务：`idx_task_publish`、`idx_task_deadline`
4. 提交：`idx_submission_task`、`idx_submission_team`

这些索引支撑了班级过滤、任务进度和统计查询场景。

### 5.14 代码质量与工程规范实践

### 5.14.1 分层与命名规范
Controller、Service、Mapper 职责明确，命名围绕业务语义，便于后续维护与论文答辩展示。

### 5.14.2 配置集中管理
评分权重、安全密钥、跨域来源、上传目录均在配置文件管理，避免散落硬编码。

### 5.14.3 升级脚本管理
项目保留 `upgrade_v2`、`upgrade_v3`、`upgrade_v4`、`upgrade_v5` 升级脚本，支持从早期版本平滑升级数据结构。

---

## 第6章 系统测试与分析

### 6.1 测试目标与策略
测试目标：验证系统是否满足任务书要求并具备可答辩演示稳定性。

测试策略：

1. 功能测试：覆盖角色主流程。
2. 安全测试：覆盖未登录、越权、越范围访问。
3. 一致性测试：覆盖唯一约束和并发入组关键场景。
4. 可运行测试：覆盖构建与启动流程。

### 6.2 测试环境
- OS：Windows 11
- JDK：1.8
- MySQL：8.0
- Node.js：18+
- 浏览器：Edge / Chrome
- 数据：`sql/seed.sql`

### 6.3 功能测试结果

| 编号 | 功能点 | 测试步骤 | 预期结果 | 实际结果 |
|---|---|---|---|---|
| F01 | 登录认证 | 输入正确账号密码和验证码登录 | 登录成功并进入主页 | 通过 |
| F02 | 项目开设 | 教师新增开设并配置分组规则 | 开设记录保存成功 | 通过 |
| F03 | 团队组建 | 学生加入团队，组长转让 | 成员关系正确更新 | 通过 |
| F04 | 任务发布 | 教师发布任务并上传附件 | 学生可见任务和附件 | 通过 |
| F05 | 任务提交 | 学生提交说明与附件 | 教师可查看提交详情 | 通过 |
| F06 | 进度追踪 | 教师查看任务完成率和未交小组 | 统计正确展示 | 通过 |
| F07 | 公告阅读 | 学生查看公告并标记已读 | 未读数减少、统计更新 | 通过 |
| F08 | 在线答疑 | 学生提问、教师回复 | 回复人可见、状态更新 | 通过 |
| F09 | 成绩录入 | 教师录入三项分值 | 总评自动计算 | 通过 |
| F10 | 成绩导出 | 教师导出成绩单 | 生成 CSV 且字段完整 | 通过 |

### 6.4 安全测试结果

| 编号 | 场景 | 输入条件 | 预期 | 结果 |
|---|---|---|---|---|
| S01 | 未登录访问接口 | 无 token 请求 `/api/tasks` | 返回 401 | 通过 |
| S02 | 学生访问管理员接口 | 学生 token 请求 `/api/users` | 返回 403 | 通过 |
| S03 | 学生跨班访问 | 学生传入他班 publishId | 返回无权限 | 通过 |
| S04 | token 无效 | 篡改 token 后请求接口 | 返回 401 | 通过 |

### 6.5 一致性测试结果

| 编号 | 规则 | 测试方式 | 预期 |
|---|---|---|---|
| C01 | 团队成员去重 | 重复加入同团队 | 被拦截或幂等处理 |
| C02 | 公告已读幂等 | 重复标记同公告已读 | 只保留一条记录 |
| C03 | 成绩唯一性 | 同学生同开设重复写入 | 受唯一键约束 |
| C04 | 入组上限并发 | 并发入组触达上限 | 不超过配置上限 |

### 6.6 关键问题定位与修复记录

1. 问题：数据库连接 `Public Key Retrieval is not allowed`
- 原因：MySQL 8 默认安全策略导致连接参数不兼容
- 修复：JDBC URL 增加 `allowPublicKeyRetrieval=true` 并保留 `useSSL=false`

2. 问题：`LocalDateTime` 反序列化失败
- 原因：前端传入时间格式与后端默认解析不一致
- 修复：统一使用 `yyyy-MM-dd HH:mm:ss`，配置 Jackson 时间格式

3. 问题：班级公告学生不可见
- 原因：班级匹配逻辑未覆盖多分隔符场景
- 修复：SQL 中统一替换中文逗号、分号、斜杠和空格后匹配

4. 问题：团队入组并发风险
- 原因：早期实现存在“先计数后写入”窗口
- 修复：事务 + `FOR UPDATE` 串行化关键写流程

5. 问题：安全配置口径不统一
- 原因：Security CORS 与 WebMvc CORS 配置来源不一致
- 修复：统一读取 `training.security.allowed-origin-patterns`

### 6.7 构建与部署验证

1. 后端：`mvn -q -DskipTests clean package` 构建通过。
2. 前端：`npm run build` 构建通过。
3. 脚本：`start-all.bat` 可拉起前后端开发环境。
4. 健康检查：`/api/health` 正常返回。

### 6.8 测试结论
系统在当前毕业设计目标范围内达到“功能完整、权限有效、流程闭环、数据一致”的验收要求。对于实际生产级应用，仍需进一步补充自动化测试和更大规模压测。

### 6.9 测试结果讨论

从测试结果可见，系统当前的主要优势集中在三点：

1. 业务闭环完整：任务发布、提交、反馈、评分形成闭环，减少流程断点。
2. 权限控制有效：认证、角色、班级范围三层拦截可落到后端，非前端“展示级”限制。
3. 一致性保障明确：唯一键、事务、行锁覆盖关键冲突场景。

同时，也暴露了毕业设计版本的边界：

1. 登录安全仍需后端验证码、限流和审计增强。
2. 文件管理缺少对象存储与防篡改能力。
3. 性能测试尚未形成标准化基线报告。

### 6.10 与任务书目标符合性评估

根据任务书“学生端、教师端、管理员端 + 测试优化 + 论文撰写”的要求，系统符合性评估如下：

1. 学生端：已实现个人资料、任务查看、团队加入、任务提交、答疑参与、成绩查看。
2. 教师端：已实现开设发布、团队管理、任务管理、提交查看、公告答疑、成绩录入导出。
3. 管理员端：已实现用户管理、角色权限、基础数据、系统监控与统计看板。
4. 测试与优化：已完成功能、安全、一致性与可运行性验证，并修复关键问题。
5. 文档产出：已形成论文初稿与开发说明文档，可继续按学院格式深化排版。

---

## 第7章 总结与展望

### 7.1 工作总结
本文围绕“专业综合实训全过程管理”完成了完整的软件工程实践：

1. 从任务书出发完成需求建模，明确三类角色边界。
2. 形成前后端分离架构并完成模块化实现。
3. 建立核心数据模型与一致性约束。
4. 将综合评价模型工程化落地，避免评分口径随人而变。
5. 通过测试验证系统可运行性和业务完整性。

相较于传统方式，本系统的实际改进点体现在：

1. 任务进度可视化，教师能快速发现未交小组。
2. 学生操作路径更短，任务内即可完成提交和附件处理。
3. 公告与答疑形成可追溯教学沟通链。
4. 成绩模型可解释、可复核、可配置。

### 7.2 存在不足

1. 验证码目前为前端本地逻辑，安全强度有限。
2. 文件存储为本地磁盘，不具备对象存储级别扩展性。
3. 缺少完整自动化测试流水线。
4. 统计分析仍以基础指标为主，尚未形成深度学习分析。

### 7.3 后续改进方向

1. 安全增强
- 引入后端图形验证码服务与登录失败限流。
- 增加 token 黑名单与主动失效机制。

2. 存储增强
- 将附件迁移到对象存储（OSS/MinIO）。
- 增加断点续传、大文件上传、病毒扫描。

3. 教学分析增强
- 构建“逾期风险评分”模型。
- 基于公告未读、任务逾期、答疑活跃度生成预警。

4. 工程质量增强
- 引入接口自动化测试。
- 引入端到端测试覆盖关键流程。
- 增加性能测试与安全扫描报告。

---

## 参考文献
[1] 郭亮, 陈西宏, 徐红波. 高职院校实训教学质量管理体系的研究[J]. 科技风, 2025(28):28-31.  
[2] 崔靖茹, 文华, 刘宏磊, 等. 基于Vue和SpringBoot框架的高校信息化项目管理系统的设计与实现[J]. 现代信息科技, 2025, 9(22):77-81.  
[3] 余叶兰, 林继民. 实训管理平台评价模型的构建及其应用研究[J]. 景德镇学院学报, 2021, 36(06):59-63.  
[4] 陈颖灵, 朱映辉, 江玉珍, 等. 基于SpringBoot学生实训管理系统的设计与实现[J]. 电脑知识与技术, 2022, 18(19):20-23.  
[5] 张美连. 信息化建设项目管理系统的研究与实践[J]. 网络安全和信息化, 2024(7):86-89.  
[6] 李淑. 高校管理信息化建设现状及对策浅议[J]. 现代信息科技, 2018, 2(11):127-128.  
[7] 夏冠湘. “双创”背景下高职院校实训管理模式建设研究[J]. 知识文库, 2019(15):32-33.  
[8] Spring Boot Reference Documentation[EB/OL].  
[9] Spring Security Reference Documentation[EB/OL].  
[10] MyBatis-Plus Official Guide[EB/OL].  
[11] Vue 3 Documentation[EB/OL].  
[12] Element Plus Documentation[EB/OL].

---

## 致谢
感谢指导教师在选题论证、技术路线和论文写作方面的指导；感谢学院提供实验环境与教学资源；感谢同学在联调与测试阶段提供的反馈建议。通过本课题实践，我对需求分析、架构分层、业务一致性、权限边界与工程文档化有了更系统的认识和提升。
