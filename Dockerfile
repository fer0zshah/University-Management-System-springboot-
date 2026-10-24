# ---------- Stage 1: build the jar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src
RUN ./gradlew bootJar -x test --no-daemon
RUN cp "$(ls build/libs/*.jar | grep -v plain | head -1)" app.jar

# ---------- Stage 2: run it ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]