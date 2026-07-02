# =============================================
# 悦动宝 JoyMove — Docker 镜像（多阶段构建）
# =============================================

# -------- 阶段1：Maven 编译 --------
FROM maven:3.8-openjdk-8 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q || true
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# -------- 阶段2：运行时 --------
FROM eclipse-temurin:8-jre
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m -Djdk.tls.client.protocols=TLSv1.2 -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker} -jar app.jar 2>&1"]
