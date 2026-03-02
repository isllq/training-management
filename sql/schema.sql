CREATE DATABASE IF NOT EXISTS training_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE training_management;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS train_announcement_read;
DROP TABLE IF EXISTS train_announcement;
DROP TABLE IF EXISTS train_file_asset;
DROP TABLE IF EXISTS train_qa_reply;
DROP TABLE IF EXISTS train_qa_thread;
DROP TABLE IF EXISTS train_score_final;
DROP TABLE IF EXISTS train_submission;
DROP TABLE IF EXISTS train_task;
DROP TABLE IF EXISTS train_team_member;
DROP TABLE IF EXISTS train_team;
DROP TABLE IF EXISTS train_project_publish;
DROP TABLE IF EXISTS train_project;
DROP TABLE IF EXISTS edu_class;
DROP TABLE IF EXISTS edu_major;
DROP TABLE IF EXISTS edu_college;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(64) NOT NULL,
  phone VARCHAR(32),
  email VARCHAR(128),
  user_type VARCHAR(32) NOT NULL COMMENT 'ADMIN/TEACHER/STUDENT',
  class_name VARCHAR(64) COMMENT '学生所属班级（仅STUDENT使用）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_user_type(user_type),
  INDEX idx_user_class(class_name),
  INDEX idx_user_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL UNIQUE,
  role_name VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_code VARCHAR(128) NOT NULL UNIQUE,
  perm_name VARCHAR(128) NOT NULL,
  perm_type VARCHAR(32) NOT NULL COMMENT 'MENU/BUTTON/API',
  path VARCHAR(255),
  method VARCHAR(16),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_user_role(user_id, role_id),
  INDEX idx_user_role_role(role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_permission(role_id, permission_id),
  INDEX idx_role_permission_perm(permission_id),
  CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

CREATE TABLE sys_login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  username VARCHAR(64),
  login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ip VARCHAR(64),
  status TINYINT NOT NULL COMMENT '1成功 0失败',
  message VARCHAR(255),
  INDEX idx_login_time(login_time),
  INDEX idx_login_username(username),
  INDEX idx_login_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

CREATE TABLE edu_college (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  college_code VARCHAR(64) NOT NULL UNIQUE,
  college_name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院';

CREATE TABLE edu_major (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  college_id BIGINT NOT NULL,
  major_code VARCHAR(64) NOT NULL UNIQUE,
  major_name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_major_college(college_id),
  CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES edu_college(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业';

CREATE TABLE edu_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  major_id BIGINT NOT NULL,
  class_code VARCHAR(64) NOT NULL UNIQUE,
  class_name VARCHAR(128) NOT NULL,
  grade_year INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_class_major(major_id),
  CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES edu_major(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级';

CREATE TABLE train_project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL UNIQUE,
  project_name VARCHAR(128) NOT NULL,
  description TEXT,
  total_hours INT DEFAULT 32,
  difficulty VARCHAR(32) DEFAULT 'MEDIUM',
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_project_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训项目模板';

CREATE TABLE train_project_publish (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  term_name VARCHAR(64) NOT NULL,
  class_name VARCHAR(64) NOT NULL,
  teacher_id BIGINT NOT NULL,
  group_count INT NOT NULL DEFAULT 0 COMMENT '计划分组数量',
  group_size_limit INT NOT NULL DEFAULT 0 COMMENT '每组人数上限，0表示不限',
  assessment_standard TEXT,
  start_date DATE,
  end_date DATE,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_publish_project(project_id),
  INDEX idx_publish_teacher(teacher_id),
  INDEX idx_publish_class(class_name),
  CONSTRAINT fk_publish_project FOREIGN KEY (project_id) REFERENCES train_project(id),
  CONSTRAINT fk_publish_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目开设实例';

CREATE TABLE train_team (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  publish_id BIGINT NOT NULL,
  team_name VARCHAR(128) NOT NULL,
  leader_student_id BIGINT,
  status TINYINT NOT NULL DEFAULT 1,
  review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
  review_comment VARCHAR(255),
  reviewed_by BIGINT,
  reviewed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_team_publish(publish_id),
  INDEX idx_team_leader(leader_student_id),
  INDEX idx_team_review(review_status),
  CONSTRAINT fk_team_publish FOREIGN KEY (publish_id) REFERENCES train_project_publish(id),
  CONSTRAINT fk_team_leader FOREIGN KEY (leader_student_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训团队';

CREATE TABLE train_team_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  role_in_team VARCHAR(64) DEFAULT '成员',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_team_student(team_id, student_id),
  INDEX idx_member_student(student_id),
  CONSTRAINT fk_member_team FOREIGN KEY (team_id) REFERENCES train_team(id),
  CONSTRAINT fk_member_student FOREIGN KEY (student_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员';

CREATE TABLE train_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  publish_id BIGINT NOT NULL,
  stage_type VARCHAR(32) NOT NULL DEFAULT 'DAILY' COMMENT 'OPENING/MIDTERM/FINAL/DAILY',
  title VARCHAR(128) NOT NULL,
  content TEXT,
  deadline DATETIME,
  weight INT DEFAULT 20,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_task_publish(publish_id),
  INDEX idx_task_stage(stage_type),
  INDEX idx_task_deadline(deadline),
  CONSTRAINT fk_task_publish FOREIGN KEY (publish_id) REFERENCES train_project_publish(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训任务';

CREATE TABLE train_submission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  team_id BIGINT,
  student_id BIGINT,
  version_no INT NOT NULL DEFAULT 1,
  content TEXT,
  file_url VARCHAR(255),
  submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1已提交 2已批阅',
  teacher_feedback VARCHAR(255),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_submission_task(task_id),
  INDEX idx_submission_team(team_id),
  INDEX idx_submission_student(student_id),
  CONSTRAINT fk_submission_task FOREIGN KEY (task_id) REFERENCES train_task(id),
  CONSTRAINT fk_submission_team FOREIGN KEY (team_id) REFERENCES train_team(id),
  CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务提交';

CREATE TABLE train_announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  publish_id BIGINT COMMENT '为空表示全体公告',
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  author_id BIGINT NOT NULL,
  priority INT NOT NULL DEFAULT 1 COMMENT '1普通 2重要 3紧急',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1发布 0草稿',
  publish_time DATETIME,
  expire_time DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_announcement_publish(publish_id),
  INDEX idx_announcement_author(author_id),
  INDEX idx_announcement_status(status),
  INDEX idx_announcement_time(publish_time),
  CONSTRAINT fk_announcement_publish FOREIGN KEY (publish_id) REFERENCES train_project_publish(id),
  CONSTRAINT fk_announcement_author FOREIGN KEY (author_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训公告';

CREATE TABLE train_announcement_read (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  announcement_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_announcement_user(announcement_id, user_id),
  INDEX idx_announcement_read_user(user_id),
  CONSTRAINT fk_announcement_read_announcement FOREIGN KEY (announcement_id) REFERENCES train_announcement(id),
  CONSTRAINT fk_announcement_read_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录';

CREATE TABLE train_file_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(32) NOT NULL COMMENT 'ANNOUNCEMENT/SUBMISSION/MATERIAL',
  biz_id BIGINT COMMENT '业务主键ID',
  original_name VARCHAR(255) NOT NULL,
  storage_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(128),
  file_size BIGINT NOT NULL DEFAULT 0,
  file_path VARCHAR(512) NOT NULL,
  uploader_id BIGINT NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_file_biz(biz_type, biz_id),
  INDEX idx_file_uploader(uploader_id),
  CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资产';

CREATE TABLE train_score_final (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  publish_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  usual_score DECIMAL(5,2) DEFAULT 0,
  task_score DECIMAL(5,2) DEFAULT 0,
  report_score DECIMAL(5,2) DEFAULT 0,
  final_score DECIMAL(5,2) DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_publish_student(publish_id, student_id),
  INDEX idx_score_student(student_id),
  CONSTRAINT fk_score_publish FOREIGN KEY (publish_id) REFERENCES train_project_publish(id),
  CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最终成绩';

CREATE TABLE train_qa_thread (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  publish_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content TEXT,
  creator_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1开放 2关闭',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_qa_thread_publish(publish_id),
  INDEX idx_qa_thread_creator(creator_id),
  CONSTRAINT fk_qa_thread_publish FOREIGN KEY (publish_id) REFERENCES train_project_publish(id),
  CONSTRAINT fk_qa_thread_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线答疑主题';

CREATE TABLE train_qa_reply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  thread_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  creator_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_qa_reply_thread(thread_id),
  INDEX idx_qa_reply_creator(creator_id),
  CONSTRAINT fk_qa_reply_thread FOREIGN KEY (thread_id) REFERENCES train_qa_thread(id),
  CONSTRAINT fk_qa_reply_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线答疑回复';

SET FOREIGN_KEY_CHECKS = 1;
