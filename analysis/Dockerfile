FROM rust:1.87-alpine AS builder
WORKDIR /app
RUN apk add --no-cache musl-dev
COPY Cargo.toml ./
COPY src ./src
RUN cargo build --release

FROM alpine:3.21
RUN addgroup -S ledgerx && adduser -S ledgerx -G ledgerx
COPY --from=builder /app/target/release/ledgerx-analysis /usr/local/bin/ledgerx-analysis
USER ledgerx
EXPOSE 8082
ENV ANALYSIS_HOST=0.0.0.0
ENV ANALYSIS_PORT=8082
CMD ["ledgerx-analysis"]
