@echo off
set JAVA_HOME=D:\jdk-25.0.3.9
set PATH=%JAVA_HOME%\bin;D:\apache-maven-3.9.9\bin;%PATH%

echo === 饮食记录小程序 - 后端启动 ===
echo 正在构建项目...

mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo 构建成功！启动服务...
    java -jar target\diet-tracker-backend-1.0.0.jar
) else (
    echo 构建失败，请检查错误信息
    pause
)
