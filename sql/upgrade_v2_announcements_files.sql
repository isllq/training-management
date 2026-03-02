USE training_management;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS train_announcement (
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

CREATE TABLE IF NOT EXISTS train_announcement_read (
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

CREATE TABLE IF NOT EXISTS train_file_asset (
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

INSERT IGNORE INTO sys_permission (id, perm_code, perm_name, perm_type, path, method, status, is_deleted)
VALUES
(8, 'announcement:manage', '公告管理', 'API', '/api/announcements', 'ALL', 1, 0),
(9, 'file:manage', '文件管理', 'API', '/api/files', 'ALL', 1, 0);

INSERT IGNORE INTO sys_role_permission (id, role_id, permission_id, is_deleted)
VALUES
(13, 1, 8, 0), (14, 2, 8, 0), (15, 3, 8, 0),
(16, 1, 9, 0), (17, 2, 9, 0), (18, 3, 9, 0);

INSERT IGNORE INTO train_announcement (id, publish_id, title, content, author_id, priority, status, publish_time, expire_time, is_deleted)
VALUES
(1, NULL, '实训平台升级通知', '本周三 20:00-22:00 平台进行维护升级，请提前保存阶段文档。', 1, 2, 1, '2026-03-01 09:00:00', '2026-03-20 23:59:59', 0),
(2, 1, '开题材料提交提醒', '信安2201开题材料截止时间为3月20日，请各组在提交管理中上传最新版文档。', 2, 3, 1, '2026-03-05 08:30:00', '2026-03-21 23:59:59', 0),
(3, 3, '中期联调要求', '物联网2201请在中期前完成接口联调并附上测试截图，逾期将影响过程分。', 3, 2, 1, '2026-03-10 10:00:00', '2026-05-05 23:59:59', 0),
(4, 4, '攻防演练安全提醒', '软件2201在演练过程中禁止扫描非授权网段，违规将取消展示资格。', 4, 3, 1, '2026-03-12 14:00:00', '2026-06-22 23:59:59', 0);

INSERT IGNORE INTO train_announcement_read (id, announcement_id, user_id, read_time, is_deleted)
VALUES
(1, 1, 2, '2026-03-01 09:10:00', 0),
(2, 1, 5, '2026-03-01 09:20:00', 0),
(3, 2, 5, '2026-03-05 11:00:00', 0),
(4, 2, 6, '2026-03-05 11:10:00', 0),
(5, 3, 9, '2026-03-11 08:00:00', 0);
