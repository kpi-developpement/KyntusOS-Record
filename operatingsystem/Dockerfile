# ETAPE 1: Build l'application b Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPE 2: Run l'application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 🔥 THE FIX: L'Backend ghay-t-lanca f 8082
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]