USE training_management;

SET @has_process := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'train_project_publish'
    AND COLUMN_NAME = 'process_weight'
);
SET @sql_process := IF(
  @has_process = 0,
  "ALTER TABLE train_project_publish ADD COLUMN process_weight DECIMAL(5,4) NULL COMMENT '过程得分权重(0-1)' AFTER assessment_standard",
  "SELECT 1"
);
PREPARE stmt FROM @sql_process;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_team := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'train_project_publish'
    AND COLUMN_NAME = 'team_weight'
);
SET @sql_team := IF(
  @has_team = 0,
  "ALTER TABLE train_project_publish ADD COLUMN team_weight DECIMAL(5,4) NULL COMMENT '团队协作得分权重(0-1)' AFTER process_weight",
  "SELECT 1"
);
PREPARE stmt FROM @sql_team;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_final := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'train_project_publish'
    AND COLUMN_NAME = 'final_weight'
);
SET @sql_final := IF(
  @has_final = 0,
  "ALTER TABLE train_project_publish ADD COLUMN final_weight DECIMAL(5,4) NULL COMMENT '答辩得分权重(0-1)' AFTER team_weight",
  "SELECT 1"
);
PREPARE stmt FROM @sql_final;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE train_project_publish
SET process_weight = COALESCE(process_weight, 0.4000),
    team_weight = COALESCE(team_weight, 0.3000),
    final_weight = COALESCE(final_weight, 0.3000)
WHERE is_deleted = 0;

