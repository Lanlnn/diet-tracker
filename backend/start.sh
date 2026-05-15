#!/bin/bash
export JAVA_HOME="/d/jdk-25.0.3.9"
export PATH="$JAVA_HOME/bin:/d/apache-maven-3.9.9/bin:$PATH"

cd "$(dirname "$0")"

echo "=== 饮食记录小程序 - 后端启动 ==="
echo "正在构建项目..."

mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "构建成功！启动服务..."
    java -jar target/diet-tracker-backend-1.0.0.jar
else
    echo "构建失败，请检查错误信息"
    exit 1
fi
