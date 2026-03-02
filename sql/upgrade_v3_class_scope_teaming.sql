USE training_management;
SET NAMES utf8mb4;

SET @has_class_col := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'class_name'
);
SET @ddl_class_col := IF(
  @has_class_col = 0,
  'ALTER TABLE sys_user ADD COLUMN class_name VARCHAR(64) NULL COMMENT ''学生所属班级（仅STUDENT使用）'' AFTER user_type',
  'SELECT 1'
);
PREPARE stmt_class_col FROM @ddl_class_col;
EXECUTE stmt_class_col;
DEALLOCATE PREPARE stmt_class_col;

SET @has_class_idx := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND INDEX_NAME = 'idx_user_class'
);
SET @ddl_class_idx := IF(
  @has_class_idx = 0,
  'ALTER TABLE sys_user ADD INDEX idx_user_class(class_name)',
  'SELECT 1'
);
PREPARE stmt_class_idx FROM @ddl_class_idx;
EXECUTE stmt_class_idx;
DEALLOCATE PREPARE stmt_class_idx;

UPDATE sys_user u
JOIN (
  SELECT tm.student_id, MAX(tp.class_name) AS class_name
  FROM train_team_member tm
  JOIN train_team t ON tm.team_id = t.id AND t.is_deleted = 0
  JOIN train_project_publish tp ON t.publish_id = tp.id AND tp.is_deleted = 0
  WHERE tm.is_deleted = 0
  GROUP BY tm.student_id
) x ON u.id = x.student_id
SET u.class_name = x.class_name
WHERE u.user_type = 'STUDENT'
  AND (u.class_name IS NULL OR u.class_name = '');

UPDATE sys_user u
JOIN (
  SELECT sf.student_id, MAX(tp.class_name) AS class_name
  FROM train_score_final sf
  JOIN train_project_publish tp ON sf.publish_id = tp.id AND tp.is_deleted = 0
  WHERE sf.is_deleted = 0
  GROUP BY sf.student_id
) x ON u.id = x.student_id
SET u.class_name = x.class_name
WHERE u.user_type = 'STUDENT'
  AND (u.class_name IS NULL OR u.class_name = '');

UPDATE sys_user u
JOIN (
  SELECT s.student_id, MAX(tp.class_name) AS class_name
  FROM train_submission s
  JOIN train_task t ON s.task_id = t.id AND t.is_deleted = 0
  JOIN train_project_publish tp ON t.publish_id = tp.id AND tp.is_deleted = 0
  WHERE s.is_deleted = 0
    AND s.student_id IS NOT NULL
  GROUP BY s.student_id
) x ON u.id = x.student_id
SET u.class_name = x.class_name
WHERE u.user_type = 'STUDENT'
  AND (u.class_name IS NULL OR u.class_name = '');

UPDATE sys_user
SET class_name = NULL
WHERE user_type <> 'STUDENT';
