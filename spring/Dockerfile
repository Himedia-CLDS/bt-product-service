# 빌드 단계
FROM gradle:7.4.2-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# 프로젝트 파일을 모두 복사
COPY . .

# Gradle Wrapper 실행 권한 설정
RUN chmod +x ./gradlew

# Gradle 빌드
RUN ./gradlew clean build --no-daemon --stacktrace --info

# 런타임 이미지를 사용하여 애플리케이션 실행
FROM openjdk:17-jdk-slim

# 포트 설정
EXPOSE 8080

# 빌드된 JAR 파일을 복사
COPY --from=build /home/gradle/project/build/libs/*.jar /app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]