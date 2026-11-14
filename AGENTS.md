# AGENTS.md - Candidate Management System

## 1. Project Overview & Stack

- **Language & Framework:** Java 21, Spring Boot 4.1.2, Apache Maven (`pom.xml`).
- **Database:** File-based H2 (`jdbc:h2:file:./data/candidatesdb`) for zero-config persistence across restarts, using Spring Data JPA + Hibernate.
- **Libraries:** Spring Web, Spring Data JPA, Jakarta Bean Validation (`spring-boot-starter-validation`), Lombok, JUnit 5, Mockito, MockMvc.
- **No Spring Security:** Keep endpoints open for effortless evaluator testing.

## 2. Package & Layer Architecture

Follow strict layered architecture under `com.example.candidatemanager` (adjust base package to match `pom.xml`):

- `controller/` -> REST endpoints (`@RestController`, `/api/v1/candidates`). Accept and return DTOs only.
- `service/` -> Business logic (`@Service`), transaction boundaries (`@Transactional`), pagination, and validation rules.
- `repository/` -> Spring Data JPA interfaces extending `JpaRepository` and `JpaSpecificationExecutor`.
- `specification/` -> Dynamic multi-criteria filtering using Spring Data JPA `Specification<Candidate>`.
- `entity/` -> JPA `@Entity` classes mapped to database tables.
- `dto/` -> Java `record` classes for Request/Response payloads (`CandidateRequest`, `CandidateResponse`, `ErrorResponse`).
- `exception/` -> Custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`) and a global `@RestControllerAdvice` handler.

## 3. Domain Model (`Candidate`)

- `id`: `Long` (auto-generated primary key)
- `fullName`: `String` (required, `@NotBlank`)
- `email`: `String` (required, `@Email`, unique constraint)
- `phone`: `String` (optional/validated format)
- `skills`: `String` or `@ElementCollection` list (e.g., Java, Spring Boot, SQL)
- `experienceYears`: `Integer` (required, `@Min(0)`)
- `location`: `String` (e.g., Hyderabad, Remote)
- `status`: Enum (`APPLIED`, `SCREENING`, `INTERVIEW`, `OFFERED`, `HIRED`, `REJECTED`)
- `createdAt` & `updatedAt`: `LocalDateTime` (auto-managed via `@CreationTimestamp` / `@UpdateTimestamp`)

## 4. API & Engineering Standards

- **REST Conventions:** Use proper HTTP status codes (`201 Created`, `200 OK`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `409 Conflict`).
- **Search & Multi-Criteria Filtering:** Support keyword search (matching name, email, or skills case-insensitively) combined with optional query params (`skill`, `minExperience`, `maxExperience`, `location`, `status`) and Spring `Pageable` sorting/pagination.
- **Error Handling:** Never expose raw stack traces. Return structured JSON error responses with timestamp, status, message, and field-level validation errors.
- **Logging:** Use SLF4J (`@Slf4j`) in controllers and services for key operations and error paths.

## 5. Testing Requirements

- Write unit tests for `CandidateService` using JUnit 5 and Mockito.
- Write integration tests for `CandidateController` using `@SpringBootTest` + `MockMvc` covering CRUD operations, multi-criteria filtering, duplicate email handling, and invalid input validation (`400 Bad Request`).
- Run `mvn test` after generating code to verify everything compiles and passes.

## 6. Token-Saving & Codex Workflow Rules

- Make minimal, targeted edits to only the files requested.
- Do not refactor unrelated files or add unrequested dependencies to `pom.xml`.
- Keep code clean, modular, and self-documenting so it is easy to explain during technical review.
