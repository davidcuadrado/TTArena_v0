FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

RUN addgroup -S arena && adduser -S arena -G arena && chown arena:arena /app/app.jar
USER arena

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
