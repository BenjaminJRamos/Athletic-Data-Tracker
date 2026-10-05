# Stage 1: Build the Spring Boot JAR with Maven
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and configuration files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy application source code
COPY src src

# Make Maven wrapper executable and package the application
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Create lightweight production runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]