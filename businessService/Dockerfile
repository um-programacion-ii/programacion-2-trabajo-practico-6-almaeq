# ====== BUILDER ======
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -Dmaven.test.skip=true package

# ====== RUNTIME ======
FROM eclipse-temurin:21-jre
WORKDIR /app

# 👉 Necesario para el healthcheck (curl)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=/app/target/*.jar
COPY --from=build ${JAR_FILE} /app/app.jar

# Importante: esta variable sobreescribe la propiedad Spring `data.service.url`
# (Spring traduce data.service.url -> DATA_SERVICE_URL)
ENV DATA_SERVICE_URL="http://data-service:8081"
ENV JAVA_OPTS=""

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]