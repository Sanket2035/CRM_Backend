# Stage 1: Build the Maven application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the pom.xml and resolve dependencies first to cache them
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal run environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root system user and group for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy build artifact from stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
