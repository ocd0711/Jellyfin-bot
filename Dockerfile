FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY o9o-bean/pom.xml o9o-bean/pom.xml
COPY o9o-service/pom.xml o9o-service/pom.xml
COPY o9o-util/pom.xml o9o-util/pom.xml
COPY o9o-controller/pom.xml o9o-controller/pom.xml
RUN mvn dependency:go-offline

COPY . .

RUN mvn package -DskipTests

FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fonts-dejavu-core \
    fonts-liberation \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/o9o-controller/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar" , "--spring.config.location=/app/application-prod.yml"]