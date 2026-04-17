FROM node:20-alpine AS builder
WORKDIR /app
RUN apk add --no-cache openjdk11 maven
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM node:20-alpine
WORKDIR /app
RUN apk add --no-cache openjdk11-jre
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9020
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
