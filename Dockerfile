FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

COPY --from=build /workspace/target/*.jar /app/pikudo-api.jar

ENTRYPOINT ["java", "-jar", "/app/pikudo-api.jar"]
