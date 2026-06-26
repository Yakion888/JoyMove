FROM openjdk:8-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080

# 使用 Railway 公网代理连接 MySQL（内网 DNS 免费版不解析）
ENTRYPOINT ["sh", "-c", "java -jar app.jar \
    --server.port=${PORT} \
    --spring.datasource.url=jdbc:mysql://thomas.proxy.rlwy.net:51713/railway \
    --spring.datasource.username=root \
    --spring.datasource.password=BTibzCjBafyUOXrfjHabjchoMQcQtKPk \
    --deepseek.api.key=${DEEPSEEK_KEY:-sk-demo}"]
