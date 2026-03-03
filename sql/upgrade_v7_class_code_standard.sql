USE training_management;
SET NAMES utf8mb4;

-- 统一班级命名：22/23(入学年) + 07/08(学院) + 班号
-- 旧命名到新编码映射：
-- 信安2201 -> 220701
-- 信安2202 -> 220708
-- 物联网2201 -> 220811
-- 软件2201 -> 220847

UPDATE sys_user
SET class_name = CASE class_name
    WHEN '信安2201' THEN '220701'
    WHEN '信安2202' THEN '220708'
    WHEN '物联网2201' THEN '220811'
    WHEN '软件2201' THEN '220847'
    ELSE class_name
END
WHERE user_type = 'STUDENT' AND is_deleted = 0;

UPDATE edu_class
SET class_code = CASE class_name
    WHEN '信安2201' THEN 'CLS220701'
    WHEN '信安2202' THEN 'CLS220708'
    WHEN '物联网2201' THEN 'CLS220811'
    WHEN '软件2201' THEN 'CLS220847'
    ELSE class_code
END,
class_name = CASE class_name
    WHEN '信安2201' THEN '220701'
    WHEN '信安2202' THEN '220708'
    WHEN '物联网2201' THEN '220811'
    WHEN '软件2201' THEN '220847'
    ELSE class_name
END,
grade_year = CASE class_name
    WHEN '信安2201' THEN 2022
    WHEN '信安2202' THEN 2022
    WHEN '物联网2201' THEN 2022
    WHEN '软件2201' THEN 2022
    ELSE grade_year
END
WHERE is_deleted = 0;

UPDATE train_project_publish
SET class_name = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(class_name, '信安2201', '220701'),
            '信安2202', '220708'
        ),
        '物联网2201', '220811'
    ),
    '软件2201', '220847'
)
WHERE is_deleted = 0;

UPDATE train_announcement
SET title = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(title, '信安2201', '220701'),
            '信安2202', '220708'
        ),
        '物联网2201', '220811'
    ),
    '软件2201', '220847'
),
content = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(content, '信安2201', '220701'),
            '信安2202', '220708'
        ),
        '物联网2201', '220811'
    ),
    '软件2201', '220847'
)
WHERE is_deleted = 0;

UPDATE train_qa_thread
SET title = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(title, '信安2201', '220701'),
            '信安2202', '220708'
        ),
        '物联网2201', '220811'
    ),
    '软件2201', '220847'
),
content = REPLACE(
    REPLACE(
        REPLACE(
            REPLACE(content, '信安2201', '220701'),
            '信安2202', '220708'
        ),
        '物联网2201', '220811'
    ),
    '软件2201', '220847'
)
WHERE is_deleted = 0;

-- 预置更多同规则班级，便于后续多学年扩展
INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT 1, 'CLS230711', '230711', 2023, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM edu_class WHERE class_name = '230711' AND is_deleted = 0);

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT 2, 'CLS230822', '230822', 2023, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM edu_class WHERE class_name = '230822' AND is_deleted = 0);

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT 3, 'CLS240747', '240747', 2024, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM edu_class WHERE class_name = '240747' AND is_deleted = 0);
