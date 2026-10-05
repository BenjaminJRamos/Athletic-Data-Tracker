# Stage 1: Build the Spring Boot JAR with Maven & Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application JAR
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Create Java 21 production runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Launch application
ENTRYPOINT ["java", "-jar", "app.jar"]