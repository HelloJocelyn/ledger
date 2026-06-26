# LedgerX Analysis

Rust service for:

- S&P 500 and Nikkei 200 financial statement analysis
- United States and Japan IPO tracking
- deterministic quality/opportunity scoring

The service ships with an embedded demonstration snapshot so the UI works without
API credentials. Replace `EmbeddedRepository` with a market-data provider or
database implementation when production data is available.

## Run

```bash
cargo run
```

The default address is `http://localhost:8082`.

Environment variables:

- `ANALYSIS_HOST` (default `0.0.0.0`)
- `ANALYSIS_PORT` (default `8082`)
- `ANALYSIS_CORS_ORIGIN` (default `http://localhost:3000`)
- `RUST_LOG` (default `ledgerx_analysis=info,tower_http=info`)

## API

- `GET /health`
- `GET /api/analysis/overview`
- `GET /api/analysis/earnings?market=SP500`
- `GET /api/analysis/ipos?country=US`

Supported filters are `SP500`, `NIKKEI_200`, `US`, and `JP`.
