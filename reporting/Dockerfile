# ---------- build ----------
FROM rust:1-bookworm AS build
WORKDIR /app
# нативные зависимости sqlx (native-tls)
RUN apt-get update && apt-get install -y --no-install-recommends \
    pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*
COPY Cargo.toml ./
# кэш-слой: собираем зависимости на пустом main.rs
RUN mkdir src && echo "fn main() {}" > src/main.rs \
    && cargo build --release && rm -rf src
COPY src src
# пересобираем с реальным кодом (трюк с таймстампом, чтобы cargo пересобрал)
RUN touch src/main.rs && cargo build --release

# ---------- runtime ----------
FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl3 ca-certificates && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/release/reporting /usr/local/bin/reporting
# каталог для готовых отчётов (можно повесить volume)
ENV REPORTS_DIR=/app/reports
RUN mkdir -p /app/reports
ENTRYPOINT ["reporting"]
