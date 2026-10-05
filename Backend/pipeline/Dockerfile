# Stage 1: Build the Spring Boot JAR with Maven
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and configuration files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy application source code
COPY src src

# Fix Windows line endings and make script executable
RUN sed -i 's/\r$//' mvnw
RUN chmod +x mvnw

# Package application
RUN ./mvnw clean package -DskipTests

# Stage 2: Create lightweight production runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]