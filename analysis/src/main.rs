mod model;
mod repository;
mod scoring;
mod service;

use std::{env, net::SocketAddr, sync::Arc};

use axum::{
    extract::{Query, State},
    http::{header, HeaderValue, Method, StatusCode},
    routing::get,
    Json, Router,
};
use model::{AnalysisOverview, EarningsAnalysis, EarningsQuery, IpoAnalysis, IpoQuery};
use repository::EmbeddedRepository;
use serde::Serialize;
use service::AnalysisService;
use tower_http::{cors::CorsLayer, trace::TraceLayer};
use tracing::info;

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt()
        .with_env_filter(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "ledgerx_analysis=info,tower_http=info".into()),
        )
        .init();

    let host = env::var("ANALYSIS_HOST").unwrap_or_else(|_| "0.0.0.0".into());
    let port = env::var("ANALYSIS_PORT")
        .ok()
        .and_then(|value| value.parse::<u16>().ok())
        .unwrap_or(8082);
    let address: SocketAddr = format!("{host}:{port}")
        .parse()
        .expect("ANALYSIS_HOST and ANALYSIS_PORT must form a valid socket address");

    let cors_origin = env::var("ANALYSIS_CORS_ORIGIN")
        .unwrap_or_else(|_| "http://localhost:3000".into())
        .parse::<HeaderValue>()
        .expect("ANALYSIS_CORS_ORIGIN must be a valid header value");
    let app = app(
        AnalysisService::new(Arc::new(EmbeddedRepository)),
        cors_origin,
    );
    let listener = tokio::net::TcpListener::bind(address)
        .await
        .expect("failed to bind analysis server");

    info!(%address, "LedgerX analysis service started");
    axum::serve(listener, app)
        .await
        .expect("analysis server stopped unexpectedly");
}

fn app(service: AnalysisService, cors_origin: HeaderValue) -> Router {
    Router::new()
        .route("/health", get(health))
        .route("/api/analysis/overview", get(overview))
        .route("/api/analysis/earnings", get(earnings))
        .route("/api/analysis/ipos", get(ipos))
        .layer(
            CorsLayer::new()
                .allow_origin(cors_origin)
                .allow_methods([Method::GET])
                .allow_headers([header::AUTHORIZATION, header::CONTENT_TYPE])
                .allow_credentials(true),
        )
        .layer(TraceLayer::new_for_http())
        .with_state(service)
}

async fn health() -> (StatusCode, Json<HealthResponse>) {
    (
        StatusCode::OK,
        Json(HealthResponse {
            status: "ok",
            service: "ledgerx-analysis",
        }),
    )
}

async fn overview(State(service): State<AnalysisService>) -> Json<AnalysisOverview> {
    Json(service.overview())
}

async fn earnings(
    State(service): State<AnalysisService>,
    Query(query): Query<EarningsQuery>,
) -> Json<Vec<EarningsAnalysis>> {
    Json(service.earnings(query.market))
}

async fn ipos(
    State(service): State<AnalysisService>,
    Query(query): Query<IpoQuery>,
) -> Json<Vec<IpoAnalysis>> {
    Json(service.ipos(query.country))
}

#[derive(Serialize)]
struct HealthResponse {
    status: &'static str,
    service: &'static str,
}
