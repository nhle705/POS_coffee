# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Sửa lại đường dẫn copy cho chuẩn xác
COPY --from=build /app/target/*.jar demo.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "demo.jar"]