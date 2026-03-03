# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder

# Install sbt
RUN apt-get update && apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add - && \
    apt-get update && apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Set sbt memory options for large project compilation
ENV SBT_OPTS="-Xmx4G -XX:+UseG1GC"

# Copy the build definition first to cache dependencies
COPY project/ /app/project/
COPY build.sbt /app/

# Fetch dependencies (cached layer)
RUN sbt update

# Copy the rest of the source code
COPY . /app/

# Build and stage the application
RUN sbt "lila/Universal / stage"

# Ensure application.conf exists from default
RUN cp /app/conf/application.conf.default /app/conf/application.conf

# Stage 2: Runtime environment
FROM eclipse-temurin:21-jre

# Install Stockfish and curl for health checks
RUN apt-get update && apt-get install -y stockfish curl && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/lila

# Copy the staged application from the builder stage
COPY --from=builder /app/target/universal/stage /opt/lila

# Explicitly copy config and public directories needed at runtime
COPY --from=builder /app/conf /opt/lila/conf
COPY --from=builder /app/public /opt/lila/public

# Expose the Play application port
EXPOSE 9000

# Define health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD curl -f http://localhost:9000/api/health || exit 1

# Environment variables with defaults
ENV PORT=9000

# Run the application
ENTRYPOINT ["/opt/lila/bin/lila"]
CMD ["-Dhttp.port=9000", "-Dconfig.file=conf/application.conf", "-Dlogger.file=conf/logger.dev.xml"]
