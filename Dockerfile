# CAMBIO: Usamos la versión 22 para que sea compatible con tu código
FROM eclipse-temurin:22-jdk-jammy

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]