FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jdk
COPY --from=build /target/*.jar demo.jar
EXPOSE 8089
ENTRYPOINT ["java","-jar","demo.jar"]