# Etapa de build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

# gera JAR executável do Spring Boot
RUN mvn -q -DskipTests clean package spring-boot:repackage

# Etapa de runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/access-control-modules-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
