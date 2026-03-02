USE training_management;
SET NAMES utf8mb4;

SET @db_name = DATABASE();

SET @group_count_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'train_project_publish'
    AND COLUMN_NAME = 'group_count'
);
SET @sql_group_count = IF(
  @group_count_exists = 0,
  'ALTER TABLE train_project_publish ADD COLUMN group_count INT NOT NULL DEFAULT 0 COMMENT ''计划分组数量'' AFTER teacher_id',
  'SELECT 1'
);
PREPARE stmt_group_count FROM @sql_group_count;
EXECUTE stmt_group_count;
DEALLOCATE PREPARE stmt_group_count;

SET @group_size_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'train_project_publish'
    AND COLUMN_NAME = 'group_size_limit'
);
SET @sql_group_size = IF(
  @group_size_exists = 0,
  'ALTER TABLE train_project_publish ADD COLUMN group_size_limit INT NOT NULL DEFAULT 0 COMMENT ''每组人数上限，0表示不限'' AFTER group_count',
  'SELECT 1'
);
PREPARE stmt_group_size FROM @sql_group_size;
EXECUTE stmt_group_size;
DEALLOCATE PREPARE stmt_group_size;

UPDATE train_project_publish
SET group_count = CASE
    WHEN group_count IS NULL OR group_count < 0 THEN 0
    ELSE group_count
  END,
  group_size_limit = CASE
    WHEN group_size_limit IS NULL OR group_size_limit < 0 THEN 0
    ELSE group_size_limit
  END
WHERE is_deleted = 0;
