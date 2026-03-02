# 基于 SpringBoot 的专业综合实训管理系统

## 1. 技术栈
- 后端：Spring Boot 2.7.18、Spring Security、JWT、MyBatis-Plus、MySQL（JDK 1.8）
- 前端：Vue 3、Vite、Element Plus、Axios
- 数据库：MySQL 8.x（5.7 也可运行，建议 8.x）

## 2. 项目结构
- `backend/` 后端源码
- `frontend/` 前端源码
- `sql/schema.sql` 建库建表脚本
- `sql/seed.sql` 初始化演示数据
- `sql/upgrade_v2_announcements_files.sql` 公告与附件升级脚本
- `sql/upgrade_v3_class_scope_teaming.sql` 班级隔离与学生组队升级脚本
- `docs/development_tasks.md` 开发任务拆解

## 3. 启动步骤
1. 初始化数据库
- 先执行 `sql/schema.sql`
- 再执行 `sql/seed.sql`
- 已有旧库升级时，按顺序执行：
  - `sql/upgrade_v2_announcements_files.sql`
  - `sql/upgrade_v3_class_scope_teaming.sql`
- 或直接使用脚本：
  - `scripts/init-db.bat`（建库+导入演示数据）
  - `scripts/load-demo-data.bat`（仅重灌演示数据）

2. 启动后端
- 修改 `backend/src/main/resources/application.yml` 中数据库账号密码
- 在 `backend` 目录执行：

```bash
mvn clean package
mvn spring-boot:run
```

3. 启动前端
- 在 `frontend` 目录执行：

```bash
npm install
npm run dev
```

4. 访问系统
- 前端地址：`http://localhost:5173`
- 后端地址：`http://localhost:8080`
- 健康检查：`GET http://localhost:8080/api/health`
- 一键启动脚本：`scripts/start-all.bat`

## 4. 默认账号
- 管理员：`admin / 123456`
- 教师：`teacher01 / 123456`
- 学生：`student01 / 123456`

## 5. 已实现模块
- 登录认证（Spring Security + JWT）
- 用户管理（增删改查、重置密码）
- Excel 批量导入用户（管理员）
- 项目管理（项目模板 + 开设实例）
- 团队管理（团队 + 成员）
- 团队审核（通过/驳回）
- 任务管理
- 阶段任务（开题/中期/结题/日常）
- 提交管理
- 教师反馈
- 成绩管理
- 成绩导出（CSV）
- 在线答疑（主题 + 回复）
- 基础数据管理（学院/专业/班级）
- 角色权限管理（角色、权限点、绑定）
- 登录日志与运行状态监控
- 看板统计（用户/项目/团队/任务/提交总数）

## 6. Excel 导入模板（用户）
- 接口：`POST /api/users/import`
- 文件格式：`.xlsx`
- 第一行是表头，从第二行开始数据
- 列顺序：
  1. `username`
  2. `real_name`
  3. `user_type`（`ADMIN` / `TEACHER` / `STUDENT`）
  4. `phone`
  5. `email`
  6. `password`（可空，空则默认 `123456`）
  7. `class_name`（仅学生必填，教师/管理员可留空）

## 7. 论文可用亮点
- 三端角色与权限隔离的通用设计
- 实训流程主线闭环（发布 -> 组队 -> 任务 -> 提交 -> 评分）
- 统一数据模型与可扩展表结构
- 蓝灰专业风格界面，适用于课程答辩展示
# training-management
