# ---------- Étape 1 : compilation ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Les dépendances sont téléchargées dans une couche séparée : elle est mise en cache
# tant que le pom.xml ne change pas, ce qui accélère les builds suivants.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Étape 2 : exécution ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# L'application ne tourne pas en root
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]