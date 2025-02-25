FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY bot-bean/pom.xml bot-bean/pom.xml
COPY bot-service/pom.xml bot-service/pom.xml
COPY bot-util/pom.xml bot-util/pom.xml
COPY bot-controller/pom.xml bot-controller/pom.xml
RUN mvn dependency:go-offline

COPY . .

RUN mvn package -DskipTests

FROM openjdk:25-slim-bookworm

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fonts-dejavu-core \
    fonts-liberation \
    fontconfig \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY ./fonts/*.ttf /usr/share/fonts/truetype/custom/

RUN fc-cache -f -v

WORKDIR /app

COPY --from=build /app/bot-controller/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar" , "--spring.config.additional-location=/app/application-prod.yml"]