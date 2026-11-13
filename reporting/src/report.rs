//! Агрегация статистики за период и рендер отчёта из шаблона.

use anyhow::{Context, Result};
use chrono::{Duration, NaiveDate, NaiveDateTime};
use serde::Serialize;
use sqlx::{FromRow, PgPool};
use std::collections::HashMap;

use crate::template::{put, REPORT_TEMPLATE};

const TOP_LIMIT: i64 = 10;

// ---------- DTO для ответов API ----------

#[derive(Serialize, FromRow)]
pub struct ReportDto {
    pub id: i32,
    pub period_start: NaiveDate,
    pub period_end: NaiveDate,
    pub file_path: Option<String>,
    pub generated_at: Option<NaiveDateTime>,
    pub generated_by: Option<String>,
}

#[derive(Serialize)]
pub struct GenerateResponse {
    pub report_id: i32,
    pub file_path: String,
}

// ---------- Тело запроса на генерацию (JSON) ----------

#[derive(serde::Deserialize, Default)]
#[serde(rename_all = "UPPERCASE")]
pub enum GeneratedBy {
    #[default]
    MANUAL,
    SYSTEM,
}

#[derive(serde::Deserialize)]
pub struct GenerateRequest {
    #[serde(default)]
    pub generated_by: GeneratedBy,
}

// ---------- Внутренние структуры агрегации ----------

#[derive(FromRow)]
struct DayStat {
    day: NaiveDate,
    count: i64,
}

#[derive(FromRow)]
struct ClientStat {
    last_name: String,
    first_name: String,
    surname: String,
    count: i64,
}

/// Границы периода в TIMESTAMP: [from 00:00:00, to+1day 00:00:00)
fn period_bounds(from: NaiveDate, to: NaiveDate) -> (NaiveDateTime, NaiveDateTime) {
    let start = from.and_hms_opt(0, 0, 0).unwrap();
    let end = (to + Duration::days(1)).and_hms_opt(0, 0, 0).unwrap();
    (start, end)
}

fn fmt_initials(first: &str, last: &str) -> String {
    let f = first.chars().next().unwrap_or(' ');
    let l = last.chars().next().unwrap_or(' ');
    format!("{f}.{l}.")
}

/// Главная функция: собрать статистику, отрендерить шаблон, сохранить файл и запись в БД.
pub async fn generate(
    pool: &PgPool,
    reports_dir: &str,
    from: NaiveDate,
    to: NaiveDate,
    generated_by: &str,
) -> Result<GenerateResponse> {
    let (start_ts, end_ts) = period_bounds(from, to);

    // --- Общая статистика ---
    let (total_visits, unique_clients): (i64, i64) = sqlx::query_as(
        r#"SELECT COUNT(*), COUNT(DISTINCT client_id) FROM visit
           WHERE check_in_time >= $1 AND check_in_time < $2"#,
    )
    .bind(start_ts)
    .bind(end_ts)
    .fetch_one(pool)
    .await?;

    let days_in_period = (to - from).num_days() + 1;
    let avg_per_day = total_visits as f64 / days_in_period as f64;

    // --- Посещения по дням (заодно пиковый день) ---
    let day_stats: Vec<DayStat> = sqlx::query_as(
        r#"SELECT check_in_time::date AS day, COUNT(*) AS count
           FROM visit
           WHERE check_in_time >= $1 AND check_in_time < $2
           GROUP BY 1 ORDER BY 1"#,
    )
    .bind(start_ts)
    .bind(end_ts)
    .fetch_all(pool)
    .await?;

    let peak = day_stats.iter().max_by_key(|d| d.count);

    // --- Распределение по типам абонементов ---
    let type_rows: Vec<(String, i64)> = sqlx::query_as(
        r#"SELECT s.subscription_type, COUNT(*)
           FROM visit v JOIN subscription s ON s.id = v.subscription_id
           WHERE v.check_in_time >= $1 AND v.check_in_time < $2
           GROUP BY s.subscription_type"#,
    )
    .bind(start_ts)
    .bind(end_ts)
    .fetch_all(pool)
    .await?;
    let by_type: HashMap<String, i64> = type_rows.into_iter().collect();

    let pct = |t: &str| {
        let c = by_type.get(t).copied().unwrap_or(0);
        let p = if total_visits > 0 { c as f64 / total_visits as f64 * 100.0 } else { 0.0 };
        (c, format!("{p:.1}"))
    };
    let (cnt_once, pct_once) = pct("ONCE_ONLY");
    let (cnt_v4, pct_v4) = pct("VISIT4");
    let (cnt_v8, pct_v8) = pct("VISIT8");
    let (cnt_v12, pct_v12) = pct("VISIT12");
    let (cnt_nolimit, pct_nolimit) = pct("NO_LIMIT");

    // --- Топ-N клиентов ---
    let top_clients: Vec<ClientStat> = sqlx::query_as(
        r#"SELECT c.last_name, c.first_name, c.surname, COUNT(*) AS count
           FROM visit v JOIN client c ON c.id = v.client_id
           WHERE v.check_in_time >= $1 AND v.check_in_time < $2
           GROUP BY c.id, c.last_name, c.first_name, c.surname
           ORDER BY COUNT(*) DESC
           LIMIT $3"#,
    )
    .bind(start_ts)
    .bind(end_ts)
    .bind(TOP_LIMIT)
    .fetch_all(pool)
    .await?;

    // ---------- Рендер шаблона ----------

    let mut out = REPORT_TEMPLATE.to_string();
    out = put(&out, "PERIOD_START", from.format("%d.%m.%Y"));
    out = put(&out, "PERIOD_END", to.format("%d.%m.%Y"));
    out = put(&out, "GENERATED_AT", chrono::Local::now().format("%d.%m.%Y %H:%M:%S"));
    out = put(&out, "TOTAL_VISITS", total_visits);
    out = put(&out, "UNIQUE_CLIENTS", unique_clients);
    out = put(&out, "AVG_PER_DAY", format!("{avg_per_day:.1}"));
    out = put(&out, "PEAK_DAY", peak.map(|p| p.day.format("%d.%m.%Y").to_string()).unwrap_or_else(|| "-".into()));
    out = put(&out, "PEAK_DAY_VISITS", peak.map(|p| p.count).unwrap_or(0));
    out = put(&out, "CNT_ONCE_ONLY", cnt_once);
    out = put(&out, "PCT_ONCE_ONLY", pct_once);
    out = put(&out, "CNT_VISIT4", cnt_v4);
    out = put(&out, "PCT_VISIT4", pct_v4);
    out = put(&out, "CNT_VISIT8", cnt_v8);
    out = put(&out, "PCT_VISIT8", pct_v8);
    out = put(&out, "CNT_VISIT12", cnt_v12);
    out = put(&out, "PCT_VISIT12", pct_v12);
    out = put(&out, "CNT_NO_LIMIT", cnt_nolimit);
    out = put(&out, "PCT_NO_LIMIT", pct_nolimit);
    out = put(&out, "TOP_LIMIT", TOP_LIMIT);

    let top_block = if top_clients.is_empty() {
        "Нет данных".to_string()
    } else {
        top_clients
            .iter()
            .enumerate()
            .map(|(i, c)| {
                format!(
                    "{}. {} {} - {} посещений",
                    i + 1,
                    c.last_name,
                    fmt_initials(&c.first_name, &c.surname),
                    c.count
                )
            })
            .collect::<Vec<_>>()
            .join("\n")
    };
    out = put(&out, "TOP_CLIENTS", top_block);

    let days_block = if day_stats.is_empty() {
        "Нет данных".to_string()
    } else {
        let wd = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];
        day_stats
            .iter()
            .map(|d| {
                format!(
                    "{} {} {} посещений",
                    d.day.format("%d.%m.%Y"),
                    wd[d.day.weekday().num_days_from_monday() as usize],
                    d.count
                )
            })
            .collect::<Vec<_>>()
            .join("\n")
    };
    out = put(&out, "VISITS_BY_DAY", days_block);

    // ---------- Сохранение файла и записи в БД ----------

    let file_name = format!(
        "attendance_{}_{}.txt",
        from.format("%Y%m%d"),
        to.format("%Y%m%d")
    );
    let file_path = format!("{reports_dir}/{file_name}");
    tokio::fs::write(&file_path, &out)
        .await
        .context("failed to write report file")?;

    let report_id: i32 = sqlx::query_scalar(
        r#"INSERT INTO report (period_start, period_end, file_path, generated_at, generated_by)
           VALUES ($1, $2, $3, NOW(), $4)
           RETURNING id"#,
    )
    .bind(from)
    .bind(to)
    .bind(&file_path)
    .bind(generated_by)
    .fetch_one(pool)
    .await?;

    Ok(GenerateResponse { report_id, file_path })
}

pub async fn list(pool: &PgPool) -> Result<Vec<ReportDto>> {
    let rows = sqlx::query_as::<_, ReportDto>(
        r#"SELECT id, period_start, period_end, file_path, generated_at, generated_by
           FROM report ORDER BY id DESC"#,
    )
    .fetch_all(pool)
    .await?;
    Ok(rows)
}

pub async fn get_path(pool: &PgPool, id: i32) -> Result<Option<String>> {
    let path: Option<Option<String>> = sqlx::query_scalar("SELECT file_path FROM report WHERE id = $1")
        .bind(id)
        .fetch_optional(pool)
        .await?;
    Ok(path.flatten())
}
