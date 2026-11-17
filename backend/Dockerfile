# Maven compiles with Java 25. Runtime needs only the JRE.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S inventory && adduser -S inventory -G inventory
WORKDIR /app
COPY --from=build --chown=inventory:inventory /workspace/target/inventory-1.0.0.jar app.jar
USER inventory
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
