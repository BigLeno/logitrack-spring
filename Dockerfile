# ---------- Estágio 1: build com JDK + Maven Wrapper ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Copia primeiro só o que define as dependências: enquanto o pom.xml não mudar,
# o Docker reaproveita esta camada e não baixa tudo de novo a cada build.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests package

# ---------- Estágio 2: imagem final só com o JRE e o .jar ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/logitrack-*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
