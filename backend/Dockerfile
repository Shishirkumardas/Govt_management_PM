FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S govos && adduser -S govos -G govos
COPY --from=build /app/build/libs/*.jar app.jar
USER govos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
