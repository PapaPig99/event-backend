# ===== Build stage =====
FROM gradle:8.7-jdk17 AS build
WORKDIR /workspace
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -m spring
USER spring
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=10 \
  CMD sh -c "wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '\"status\":\"UP\"' || exit 1"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
