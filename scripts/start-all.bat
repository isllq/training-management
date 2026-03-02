@echo off
chcp 65001 >nul
setlocal

set ROOT_DIR=%~dp0

echo === 一键启动前后端 ===
start "Backend" cmd /k "%ROOT_DIR%start-backend.bat"
start "Frontend" cmd /k "%ROOT_DIR%start-frontend.bat"

echo 已打开两个窗口：
echo 1. Backend
echo 2. Frontend
echo.
echo 前端地址：http://localhost:5173
echo 后端地址：http://localhost:8080
pause
exit /b 0
