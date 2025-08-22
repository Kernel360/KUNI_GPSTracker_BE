# ---------- 1) Build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Gradle Wrapper/설정
COPY gradlew ./
COPY gradle ./gradle
# CRLF 제거 + 실행권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 모듈 빌드 스크립트 (Kotlin DSL 사용 시)
COPY build.gradle.kts ./
# Groovy DSL이라면 위 대신: COPY build.gradle ./
# (모듈엔 보통 settings.gradle* 없음)

# 소스
COPY src ./src

# 의존성 캐시(실패해도 계속)
RUN ./gradlew --no-daemon dependencies || true

# 패키징
RUN ./gradlew --no-daemon bootJar

# ---------- 2) Runtime stage ----------
FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul

# 앱 포트 (앱의 server.port와 일치!)
EXPOSE 8082

# JAR 복사
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

ENTRYPOINT ["sh","-c","exec java -jar /app/app.jar"]