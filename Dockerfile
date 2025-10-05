# 1️⃣ Build stage - uses Maven to compile the JAR
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# 2️⃣ Run stage - lightweight Java runtime only
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Render sets $PORT automatically
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
