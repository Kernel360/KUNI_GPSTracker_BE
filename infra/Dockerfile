
# 빌드 환경
FROM gradle:jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# 실행 환경
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
