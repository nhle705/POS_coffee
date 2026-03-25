# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Dmaven.test.skip=true


FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar demo.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "demo.jar"]