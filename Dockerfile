# --- Stage 1: Build the Application ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package and keep only the runnable Spring Boot jar as /app/app.jar
RUN mvn clean package -DskipTests \
    && cp "$(ls target/*.jar | grep -v '\.original$' | head -n 1)" /app/app.jar

# --- Stage 2: Run the Application ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy exactly one jar from build stage
COPY --from=build /app/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]