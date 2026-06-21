FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system clearance && adduser --system --ingroup clearance clearance
COPY --from=build /workspace/target/clearance-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads && chown -R clearance:clearance /app/uploads

USER clearance
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
