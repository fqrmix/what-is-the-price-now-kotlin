# Stage 1
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle fatJar --no-daemon

# Stage 2
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/what-is-the-price-now-kotlin-1.0-all.jar app.jar