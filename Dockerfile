# Use a multi-stage build to reduce the final image size

# Stage 1: Build the application
FROM maven:3.9.6-amazoncorretto-17 AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests

# Stage 2: Create the final image
FROM amazoncorretto:17

# Set the working directory
WORKDIR /app

# Copy the built application from the builder stage
COPY --from=builder /app/target/*.jar microservice.jar

# Expose the port the application runs on
EXPOSE 8080

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "microservice.jar"]