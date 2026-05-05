# ETAPE 1: Build l'application b Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# N-copiyew l'pom.xml w n-téléchargew les dépendances (bach Docker y-cachiha w yzreb)
COPY pom.xml .
RUN mvn dependency:go-offline
# N-copiyew l'code w n-buildiw l .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPE 2: Run l'application (Image khfifa JRE)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# N-jib l'fichier .jar mn l'étape 1
COPY --from=build /app/target/*.jar app.jar
# L'Port li kheddam bih l'Backend dyalek
EXPOSE 8083
# L'Commande bach y-demarri
ENTRYPOINT ["java", "-jar", "app.jar"]