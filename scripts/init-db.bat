@echo off
chcp 65001 >nul
setlocal

set ROOT_DIR=%~dp0..
set SCHEMA_SQL=%ROOT_DIR%\sql\schema.sql
set SEED_SQL=%ROOT_DIR%\sql\seed.sql

echo === 初始化数据库 ===
echo 请输入 MySQL 连接信息：
set /p DB_HOST=Host(默认 localhost): 
if "%DB_HOST%"=="" set DB_HOST=localhost
set /p DB_PORT=Port(默认 3306): 
if "%DB_PORT%"=="" set DB_PORT=3306
set /p DB_USER=User(默认 root): 
if "%DB_USER%"=="" set DB_USER=root
set /p DB_PASS=Password: 

if not exist "%SCHEMA_SQL%" (
  echo [错误] 找不到 %SCHEMA_SQL%
  pause
  exit /b 1
)

if not exist "%SEED_SQL%" (
  echo [错误] 找不到 %SEED_SQL%
  pause
  exit /b 1
)

echo.
echo 执行 schema.sql ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%SCHEMA_SQL%"
if errorlevel 1 (
  echo [错误] schema.sql 执行失败，请检查 MySQL 客户端是否可用、账号密码是否正确。
  pause
  exit /b 1
)

echo 执行 seed.sql ...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%SEED_SQL%"
if errorlevel 1 (
  echo [错误] seed.sql 执行失败。
  pause
  exit /b 1
)

echo.
echo [完成] 数据库初始化成功。
pause
exit /b 0
