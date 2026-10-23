# syntax=docker/dockerfile:1
# Build context is the repository root: docker build -f bank-service/Dockerfile .

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew :bank-service:app:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home --shell /usr/sbin/nologin app
USER app
COPY --from=build /workspace/bank-service/app/build/libs/bank-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
