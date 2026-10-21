FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

ENV JAVA_OPTS=""

COPY --from=build --chown=app:app /workspace/target/keycloak-mcp-*-SNAPSHOT.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -jar /app/app.jar \
  --server.port=8080 \
  --spring.profiles.active=docker \
  --spring.ai.mcp.server.protocol=STREAMABLE"]