@echo off
chcp 65001 >nul
setlocal

set ROOT_DIR=%~dp0..
set SEED_SQL=%ROOT_DIR%\sql\seed.sql
set UPGRADE_SQL=%ROOT_DIR%\sql\upgrade_v4_team_rule_task_flow.sql

echo === 导入演示数据 ===
set /p DB_HOST=Host(默认 localhost): 
if "%DB_HOST%"=="" set DB_HOST=localhost
set /p DB_PORT=Port(默认 3306): 
if "%DB_PORT%"=="" set DB_PORT=3306
set /p DB_USER=User(默认 root): 
if "%DB_USER%"=="" set DB_USER=root
set /p DB_PASS=Password: 

if not exist "%SEED_SQL%" (
  echo [错误] 找不到 %SEED_SQL%
  pause
  exit /b 1
)

if exist "%UPGRADE_SQL%" (
  echo 执行升级脚本（团队规则字段）...
  mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%UPGRADE_SQL%"
  if errorlevel 1 (
    echo [错误] 升级脚本执行失败，请检查数据库版本或连接信息。
    pause
    exit /b 1
  )
)

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%SEED_SQL%"
if errorlevel 1 (
  echo [错误] 演示数据导入失败，请检查数据库连接信息。
  pause
  exit /b 1
)

echo [完成] 演示数据已导入。
pause
exit /b 0
