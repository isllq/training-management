USE training_management;
SET NAMES utf8mb4;

-- 扩充专业数据（幂等）
INSERT INTO edu_major (college_id, major_code, major_name, status, is_deleted)
SELECT c.id, 'CYBERSEC', '网络空间安全', 1, 0
FROM edu_college c
WHERE c.college_code = 'EI'
  AND c.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_major m WHERE m.major_code = 'CYBERSEC');

INSERT INTO edu_major (college_id, major_code, major_name, status, is_deleted)
SELECT c.id, 'COMMENG', '通信工程', 1, 0
FROM edu_college c
WHERE c.college_code = 'EI'
  AND c.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_major m WHERE m.major_code = 'COMMENG');

INSERT INTO edu_major (college_id, major_code, major_name, status, is_deleted)
SELECT c.id, 'AI', '人工智能', 1, 0
FROM edu_college c
WHERE c.college_code = 'CS'
  AND c.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_major m WHERE m.major_code = 'AI');

INSERT INTO edu_major (college_id, major_code, major_name, status, is_deleted)
SELECT c.id, 'DATA', '数据科学与大数据技术', 1, 0
FROM edu_college c
WHERE c.college_code = 'CS'
  AND c.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_major m WHERE m.major_code = 'DATA');

INSERT INTO edu_major (college_id, major_code, major_name, status, is_deleted)
SELECT c.id, 'NETENG', '网络工程', 1, 0
FROM edu_college c
WHERE c.college_code = 'CS'
  AND c.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_major m WHERE m.major_code = 'NETENG');

-- 扩充班级数据（幂等）
INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220702', '220702', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220702');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220703', '220703', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220703');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220711', '220711', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220711');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220712', '220712', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220712');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220713', '220713', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220713');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220731', '220731', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'COMMENG'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220731');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220732', '220732', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'COMMENG'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220732');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220821', '220821', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'IOT'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220821');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220822', '220822', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'IOT'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220822');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220823', '220823', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'IOT'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220823');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220841', '220841', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'SE'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220841');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220842', '220842', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'SE'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220842');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220843', '220843', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'SE'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220843');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220844', '220844', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'DATA'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220844');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220845', '220845', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'DATA'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220845');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220846', '220846', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'AI'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220846');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS220848', '220848', 2022, 1, 0
FROM edu_major m
WHERE m.major_code = 'AI'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS220848');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230701', '230701', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230701');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230708', '230708', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'INFOSEC'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230708');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230811', '230811', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'IOT'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230811');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230847', '230847', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'SE'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230847');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230881', '230881', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'NETENG'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230881');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230844', '230844', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'DATA'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230844');

INSERT INTO edu_class (major_id, class_code, class_name, grade_year, status, is_deleted)
SELECT m.id, 'CLS230846', '230846', 2023, 1, 0
FROM edu_major m
WHERE m.major_code = 'AI'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM edu_class c WHERE c.class_code = 'CLS230846');
