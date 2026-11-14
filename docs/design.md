# Technical Design

The application is a layered Spring Boot API. Controllers handle HTTP and DTO validation; `CandidateService` owns business rules and transactions; the repository provides JPA persistence; and `CandidateSpecification` composes optional search criteria.

Candidates use one table with a unique email, lifecycle status, current role, skills text, years of experience, timestamps, and location. This keeps the assignment focused while supporting the dashboard cards and filters. Skills remain text because the product only needs contains matching; a normalized `candidate_skills` table is a later evolution if exact skills analytics become important.

The API returns a dedicated paginated response so the React client receives a stable JSON contract rather than Spring Data's internal `Page` serialization. Filter query parameters map directly to the UI: `q`, repeated `skill`, experience bounds, location, status, page, size, and sort.

PostgreSQL runs in Docker for local development. Flyway owns schema creation and Hibernate validates the schema. Tests use an H2 in-memory database and build their own data, isolating automated tests from development records.

At higher scale, move to managed PostgreSQL, add a normalized skill model and database indexes based on query metrics, introduce authentication, add structured metrics/tracing, and consider full-text search for keyword matching.
