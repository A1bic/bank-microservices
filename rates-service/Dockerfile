# syntax=docker/dockerfile:1
# Build context is the repository root: docker build -f rates-service/Dockerfile .

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew :rates-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home --shell /usr/sbin/nologin app
USER app
COPY --from=build /workspace/rates-service/build/libs/rates-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
