@echo off
chcp 65001 >nul
setlocal

set ROOT_DIR=%~dp0..
set BACKEND_DIR=%ROOT_DIR%\backend

if not exist "%BACKEND_DIR%\pom.xml" (
  echo [错误] 找不到后端项目目录：%BACKEND_DIR%
  pause
  exit /b 1
)

echo === 启动后端（Spring Boot）===
cd /d "%BACKEND_DIR%"
mvn spring-boot:run

if errorlevel 1 (
  echo.
  echo [错误] 后端启动失败，请检查：
  echo 1. JDK 1.8 是否正确安装并生效
  echo 2. Maven 是否可用
  echo 3. application.yml 中数据库账号密码是否正确
  pause
  exit /b 1
)

exit /b 0
