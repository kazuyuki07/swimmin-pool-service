# swimminPool

Микросервисная система учёта посещений бассейна.

## Архитектура

| Сервис      | Стек                    | Порт | Назначение                              |
|-------------|-------------------------|------|------------------------------------------|
| gateway     | Spring Boot + Gateway MVC | 8080 | Единая точка входа для клиента           |
| swimmin-pool| Spring Boot (Java)      | 8081 | Клиенты, абонементы, посещения           |
| reporting   | Rust (axum + sqlx)      | 8082 | Отчёты посещаемости (txt)                |
| frontend    | Spring Boot + Thymeleaf | 8083 | UI для клиента                           |

```
браузер → frontend:8083 (страницы)
    JS → gateway:8080
        /api/v1/clients/**,/api/v1/subscriptions/**,/api/v1/visits/** → swimmin-pool:8081
        /api/v1/reports/**                                             → reporting:8082
```

## Запуск

1. PostgreSQL, БД `swimmin_pool` ( liquibase поднимет схему при старте swimmin-pool ).
2. `cd swimmin-pool && ./mvnw spring-boot:run`
3. `cd reporting && DATABASE_URL=postgres://postgres:root@localhost/swimmin_pool cargo run`
4. `cd gateway && ./mvnw spring-boot:run`
5. `cd frontend && ./mvnw spring-boot:run` → открыть http://localhost:8083

Переменные окружения:
- swimmin-pool: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- reporting: `DATABASE_URL`, `PORT` (по умолчанию 8082), `REPORTS_DIR` (по умолчанию ./reports)
- frontend: `GATEWAY_URL` (по умолчанию http://localhost:8080)

## Запуск через Docker

```bash
docker compose up --build
```

Поднимет всё: postgres:18 (uuidv7() в DDL доступен только с 18-й версии),
swimmin-pool, reporting, gateway и frontend. UI: http://localhost:8083,
API через gateway: http://localhost:8080.

Отдельный сервис (для отладки):
```bash
docker compose up --build reporting
```

Остановка и очистка:
```bash
docker compose down          # остановить
docker compose down -v       # + удалить данные БД и отчёты
```
