# ---------- Etapa de build ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos primero el wrapper y el pom para cachear dependencias
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Ahora copiamos el código fuente y empaquetamos
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ---------- Etapa de runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Carpeta para uploads persistentes (se monta como Volume en Railway)
RUN mkdir -p /app/uploads/evidencias /app/uploads/repuestos

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
