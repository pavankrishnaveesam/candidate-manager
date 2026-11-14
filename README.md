# Candidate Manager

Spring Boot API for creating, viewing, updating, deleting, searching, and filtering candidates.

## Run locally

1. Copy `.env.example` to `.env`, keep the PostgreSQL URL's UTC option, and set `POSTGRES_PASSWORD` and `DB_PASSWORD` to the same local value.
2. Start PostgreSQL with `docker compose up -d`.
3. Start the API with `./mvnw.cmd spring-boot:run` on PowerShell, or `./mvnw spring-boot:run` on Git Bash.
4. Open `http://localhost:8080/api/v1/candidates`.

The `dev` profile is the default. Flyway creates the schema, and the development initializer adds candidates when the table is empty. The application sets its database session timezone to UTC for PostgreSQL compatibility on Windows. Set `APP_SEED_ENABLED=false` to skip seed data.

## Test

Run `./mvnw.cmd verify`. This runs tests against an isolated in-memory H2 database, generates the JaCoCo report, and enforces 90% line coverage for executable application code. Tests never modify PostgreSQL.

## API

`POST /api/v1/candidates` creates a candidate. `GET`, `PUT`, and `DELETE /api/v1/candidates/{id}` manage one candidate.

`GET /api/v1/candidates` accepts `q`, repeated `skill`, `minExperience`, `maxExperience`, `location`, `status`, `page`, `size`, and `sort`. For example:

```text
GET /api/v1/candidates?skill=java&skill=figma&minExperience=3&status=INTERVIEW&sort=createdAt,desc
```

See `docs/api.md` for request and response examples. Import `postman/Candidate-Manager.postman_collection.json` into Postman for a ready-made collection.
