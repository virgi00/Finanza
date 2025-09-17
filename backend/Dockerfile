# Dockerfile per Backend Java Spring Boot
FROM alpine:3.18 AS build

# Installa OpenJDK 17 e Maven
RUN apk add --no-cache openjdk17 maven

# Imposta la directory di lavororfile per Backend Java Spring Boot
FROM alpine:latest AS build

# Installa OpenJDK 17 e Maven
RUN apk add --no-cache openjdk17 maven

# Imposta la directory di lavoro
WORKDIR /app

# Copia i file di configurazione Maven
COPY pom.xml .
COPY src ./src

# Compila l'applicazione
RUN mvn clean package -DskipTests

# Stage di produzione
FROM alpine:3.18

# Installa OpenJDK 17 JRE
RUN apk add --no-cache openjdk17-jre

# Imposta la directory di lavoro
WORKDIR /app

# Copia il JAR compilato dal stage di build
COPY --from=build /app/target/finanzanews_be-0.0.1-SNAPSHOT.jar app.jar

# Esponi la porta 8080
EXPOSE 8080

# Comando per avviare l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
