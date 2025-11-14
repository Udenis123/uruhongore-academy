# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy project files
COPY . .

# Ensure mvnw is executable
RUN chmod +x mvnw

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR file
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
