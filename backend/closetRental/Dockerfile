# =============================================================================
# Stage 1: Build
# eclipse-temurin:21-jdk-alpine — JDK completo para compilar com Maven Wrapper
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copiar Maven Wrapper isolado (layer muda raramente)
COPY .mvn/ .mvn/
COPY mvnw mvnw
RUN chmod +x mvnw

# Copiar apenas o pom.xml para cachear download das dependências
# Este layer só é invalidado quando o pom.xml muda
COPY pom.xml pom.xml
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copiar código-fonte (layer invalidado a cada commit)
COPY src/ src/

# Compilar e empacotar, pulando testes (rodam no CI separadamente)
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# Extrair layers do JAR com Spring Boot Layertools
# Divide em: dependencies / spring-boot-loader / snapshot-dependencies / application
# Maximiza reuso de layers Docker — apenas "application" muda normalmente
WORKDIR /build/target/extracted
RUN java -Djarmode=layertools -jar /build/target/closetRental-0.0.1-SNAPSHOT.jar extract

# =============================================================================
# Stage 2: Runtime
# eclipse-temurin:21-jre-alpine — apenas JRE (~180MB vs ~600MB com JDK)
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL org.opencontainers.image.title="closet-rental" \
      org.opencontainers.image.description="API REST para aluguel de roupas" \
      org.opencontainers.image.source="https://github.com/josivantarcio/closet-rental"

# Usuário não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
RUN chown appuser:appgroup /app
USER appuser

# Copiar layers em ordem crescente de frequência de mudança
# Docker reutiliza layers idênticos entre builds
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/application/ ./

EXPOSE 8080

# JVM tuning para containers:
# UseContainerSupport: respeita limites de CPU/RAM do container (padrão Java 11+)
# MaxRAMPercentage=75.0: usa até 75% da RAM disponível para heap
# UseG1GC: GC de propósito geral, eficiente para aplicações web
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
