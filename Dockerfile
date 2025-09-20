# Force cache invalidation v2

# Stage 1: Build the application using a Maven image with JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file first to leverage Docker's layer caching.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping the tests
RUN mvn package -DskipTests


# Stage 2: Create the final, lightweight production image using a JRE 21 image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy only the built .jar file from the 'build' stage into our final image
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8081

# The command to run the application when the container starts
CMD ["java", "-jar", "app.jar"]