@echo off
chcp 65001 >nul
setlocal

set ROOT_DIR=%~dp0..
set FRONTEND_DIR=%ROOT_DIR%\frontend

if not exist "%FRONTEND_DIR%\package.json" (
  echo [错误] 找不到前端项目目录：%FRONTEND_DIR%
  pause
  exit /b 1
)

echo === 启动前端（Vue + Vite）===
cd /d "%FRONTEND_DIR%"

if not exist "%FRONTEND_DIR%\node_modules" (
  echo 首次运行，正在安装依赖...
  npm install
  if errorlevel 1 (
    echo [错误] npm install 失败，请检查 Node.js / npm 环境。
    pause
    exit /b 1
  )
)

npm run dev
if errorlevel 1 (
  echo [错误] 前端启动失败。
  pause
  exit /b 1
)

exit /b 0
