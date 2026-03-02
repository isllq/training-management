@echo off
chcp 65001 >nul
setlocal

set SCRIPT_DIR=%~dp0

echo === 一键重启前后端 ===
echo 正在关闭旧进程...
call :kill_port 8080
call :kill_port 5173
timeout /t 1 >nul

echo 正在重新启动...
start "Backend" cmd /k "%SCRIPT_DIR%start-backend.bat"
start "Frontend" cmd /k "%SCRIPT_DIR%start-frontend.bat"

echo.
echo 重启完成：
echo 前端地址：http://localhost:5173
echo 后端地址：http://localhost:8080
pause
exit /b 0

:kill_port
set PORT=%~1
set FOUND=0
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
  set FOUND=1
  echo 关闭端口 %PORT% 的进程 PID=%%P
  taskkill /F /PID %%P >nul 2>&1
)
if "%FOUND%"=="0" (
  echo 端口 %PORT% 未检测到运行进程
)
exit /b 0
