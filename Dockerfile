# Etapa 1: Construcción
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Instalar herramientas necesarias para ejecutar gradlew
RUN apk add --no-cache bash

# Copiar todos los archivos del proyecto
COPY src ./src
COPY gradlew .
COPY gradlew.bat .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Otorgar permisos de ejecución al wrapper de Gradle
RUN chmod +x ./gradlew

# Construir el proyecto sin ejecutar pruebas
RUN /bin/bash ./gradlew build -x test

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el archivo JAR desde la etapa de construcción
COPY --from=builder /app/build/libs/kata-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
