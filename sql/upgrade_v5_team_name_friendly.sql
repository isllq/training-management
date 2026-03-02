USE training_management;
SET NAMES utf8mb4;

UPDATE train_team
SET team_name = CONCAT(
    '第',
    ASCII(UPPER(SUBSTRING(team_name, 1, 1))) - 64,
    '组',
    CASE WHEN CHAR_LENGTH(team_name) > 2 THEN SUBSTRING(team_name, 3) ELSE '' END
)
WHERE is_deleted = 0
  AND team_name REGEXP '^[A-Za-z]组';
