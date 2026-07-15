@echo off
if "%JAVA_HOME%"=="" (
    echo Java 18 is required. Set JAVA_HOME to an installed JDK 18.
    exit /b 1
)

for /f "tokens=3" %%v in ('"%JAVA_HOME%\bin\java" -version 2^>^&1 ^| findstr /I "version"') do set JAVA_VERSION=%%~v
echo %JAVA_VERSION% | findstr /B /C:"18." >nul
if errorlevel 1 (
    echo Java 18 is required. Current JAVA_HOME is %JAVA_HOME%
    exit /b 1
)

echo === 饮食记录小程序 - 后端启动 ===
echo 正在构建项目...

call mvnw.cmd clean package -DskipTests

if %errorlevel% equ 0 (
    echo 构建成功！启动服务...
    java -jar target\diet-tracker-backend-1.0.0.jar
) else (
    echo 构建失败，请检查错误信息
    pause
)
