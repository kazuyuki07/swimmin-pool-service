mod report;
mod template;

use axum::{
    extract::{Path, Query, State},
    http::{header, StatusCode},
    response::{IntoResponse, Response},
    routing::{get, post},
    Json, Router,
};
use chrono::NaiveDate;
use serde::Deserialize;
use std::sync::Arc;

use report::{GenerateRequest, ReportDto};

#[derive(Clone)]
struct AppState {
    pool: sqlx::PgPool,
    reports_dir: String,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let database_url = std::env::var("DATABASE_URL")?;
    let pool = sqlx::PgPool::connect(&database_url).await?;

    let reports_dir = std::env::var("REPORTS_DIR").unwrap_or_else(|_| "./reports".into());
    tokio::fs::create_dir_all(&reports_dir).await?;

    let state = Arc::new(AppState { pool, reports_dir });

    let app = Router::new()
        .route("/api/v1/reports/generate", post(generate_report))
        .route("/api/v1/reports", get(list_reports))
        .route("/api/v1/reports/{id}/download", get(download_report))
        .with_state(state);

    let port = std::env::var("PORT").unwrap_or_else(|_| "8082".into());
    let listener = tokio::net::TcpListener::bind(format!("0.0.0.0:{port}")).await?;
    axum::serve(listener, app).await?;
    Ok(())
}

// ---------- POST /api/v1/reports/generate?from=&to= ----------

#[derive(Deserialize)]
struct GenerateQuery {
    #[serde(rename = "from")]
    period_from: NaiveDate,
    #[serde(rename = "to")]
    period_to: NaiveDate,
}

async fn generate_report(
    State(state): State<Arc<AppState>>,
    Query(q): Query<GenerateQuery>,
    body: Option<Json<GenerateRequest>>,
) -> Result<Json<report::GenerateResponse>, AppError> {
    if q.period_from > q.period_to {
        return Err(AppError::BadRequest("`from` must be <= `to`".into()));
    }
    let generated_by = match &body {
        Some(Json(req)) => match req.generated_by {
            report::GeneratedBy::MANUAL => "MANUAL",
            report::GeneratedBy::SYSTEM => "SYSTEM",
        },
        None => "MANUAL",
    };
    let resp = report::generate(
        &state.pool,
        &state.reports_dir,
        q.period_from,
        q.period_to,
        generated_by,
    )
    .await?;
    Ok(Json(resp))
}

// ---------- GET /api/v1/reports ----------

async fn list_reports(
    State(state): State<Arc<AppState>>,
) -> Result<Json<Vec<ReportDto>>, AppError> {
    Ok(Json(report::list(&state.pool).await?))
}

// ---------- GET /api/v1/reports/{id}/download ----------

async fn download_report(
    State(state): State<Arc<AppState>>,
    Path(id): Path<i32>,
) -> Result<Response, AppError> {
    let path = report::get_path(&state.pool, id).await?;
    let Some(path) = path else {
        return Err(AppError::NotFound(format!("report {id} not found")));
    };
    let content = tokio::fs::read(&path).await.map_err(|_| {
        AppError::NotFound(format!("file for report {id} is missing on disk"))
    })?;
    let file_name = path.rsplit('/').next().unwrap_or("report.txt");
    let disposition = format!("attachment; filename=\"{file_name}\"");
    let mut resp = Response::new(content.into_body());
    resp.headers_mut()
        .insert(header::CONTENT_TYPE, "text/plain; charset=utf-8".parse().unwrap());
    resp.headers_mut()
        .insert(header::CONTENT_DISPOSITION, disposition.parse().unwrap());
    Ok(resp)
}

// ---------- Ошибки ----------

enum AppError {
    BadRequest(String),
    NotFound(String),
    Internal(anyhow::Error),
}

impl From<anyhow::Error> for AppError {
    fn from(e: anyhow::Error) -> Self {
        AppError::Internal(e)
    }
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let (code, msg) = match self {
            AppError::BadRequest(m) => (StatusCode::BAD_REQUEST, m),
            AppError::NotFound(m) => (StatusCode::NOT_FOUND, m),
            AppError::Internal(e) => (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()),
        };
        (code, Json(serde_json::json!({ "error": msg }))).into_response()
    }
}
