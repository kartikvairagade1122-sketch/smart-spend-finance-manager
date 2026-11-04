# Architectural Decision Records (ADRs)

This document records the architectural and engineering decisions made during the evolution of the Smart Spend Personal Finance Management System.

---

## ADR 001: Decoupling JavaFX Desktop Client and Spring Boot REST API

### Context
The original legacy application was a 2-tier monolithic desktop app where JavaFX controllers directly imported `java.sql.Connection` and executed SQL queries via JDBC against MySQL. In a multi-user environment, this architecture exhibits severe vulnerabilities:
1. Database credentials must be embedded into the distributed desktop client binary, allowing any user with a decompiler to obtain root database credentials.
2. Direct database access from client machines bypasses centralized business validation and audit logging.
3. Every client requires direct network access to the database port (3306), exposing the database to the public internet.

### Decision
Migrate to a **3-Tier Decoupled Architecture**:
- **Tier 1 (Client):** JavaFX 21 communicates exclusively via JSON REST APIs over HTTP/2 using Java 11 `HttpClient`.
- **Tier 2 (Application):** Spring Boot 3 handles authentication, authorization, business rules, transaction boundaries, and auditing.
- **Tier 3 (Database):** MySQL 8.0 is accessible solely through the Spring Boot service via HikariCP connection pooling.

### Consequences
- **Positive:** Database credentials are never exposed to clients; business logic is centralized; zero risk of client SQL injection; client updates do not break database schemas.
- **Trade-off:** Requires network serialization and deserialization overhead; managed through asynchronous JavaFX `Task` worker threads.

---

## ADR 002: Dual-Token JWT Authentication (Access + Refresh Token Rotation)

### Context
Desktop clients need to authenticate users without storing plaintext passwords or requiring re-login on every application restart, while maintaining the ability to revoke compromised sessions. Traditional HTTP sessions (`JSESSIONID`) rely on server memory state, hindering horizontal scaling. Single long-lived JWTs cannot be revoked without maintaining a centralized token blacklist.

### Decision
Implement a **Dual-Token Architecture with Refresh Token Rotation**:
1. **Access Token:** Short-lived (60 minutes), stateless, digitally signed HMAC-SHA256 JWT containing user identity and role claims.
2. **Refresh Token:** Long-lived (7 days), cryptographically secure random token persisted in the database.
3. **Rotation:** Upon refreshing an access token, the used refresh token is invalidated and a new one is issued.

### Consequences
- **Positive:** Stateless API authentication; low latency (access tokens do not require database verification); instant session revocation capability; protection against stolen tokens.
- **Trade-off:** Slightly increased token exchange complexity in the client `ApiClient`.

---

## ADR 003: Defense-in-Depth Multi-Tenant Isolation (Anti-IDOR)

### Context
In a multi-user financial application, data privacy is paramount. An Insecure Direct Object Reference (IDOR) flaw occurs when an endpoint accepts a database record ID (e.g. `DELETE /api/transactions/15`) and fulfills the request without verifying that the record belongs to the authenticated requester.

### Decision
Enforce user ownership verification at both the repository and service layers:
1. Never accept `userId` as a request parameter or body property from client payloads.
2. Extract the authenticated user's ID exclusively from the verified `SecurityContextHolder`.
3. Parameterize all JPA database queries with both `id` AND `userId`:
   ```java
   @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId")
   Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
   ```
4. Return HTTP 404 (`ResourceNotFoundException`) rather than HTTP 403 when a user queries another user's record ID, preventing account enumeration.

### Consequences
- **Positive:** Mathematically prevents cross-user data leaks; complete immunity to URL manipulation attacks.
- **Trade-off:** Developers must never call generic `findById` without appending tenant filters.

---

## ADR 004: Arbitrary-Precision Financial Arithmetic (`BigDecimal`) and Indian Locale Formatting

### Context
Using binary floating-point numbers (`float` or `double`) for monetary amounts causes rounding errors due to base-2 representation (e.g., `0.1 + 0.2 = 0.30000000000000004`). In financial systems, accumulated rounding discrepancies represent regulatory and accounting violations. Furthermore, formatting in Western millions/billions confuses Indian users accustomed to the Indian numbering system (Lakhs and Crores).

### Decision
1. Represent all financial quantities in Java as `java.math.BigDecimal` and in MySQL as `DECIMAL(12, 2)`.
2. Apply `RoundingMode.HALF_EVEN` (Banker's Rounding) for all percentage calculations and divisions.
3. Standardize UI rendering using Java's `NumberFormat.getCurrencyInstance(new Locale("en", "IN"))` to display the Indian Rupee symbol (`₹`) and grouping (e.g., `₹ 1,50,000.00`).

### Consequences
- **Positive:** Zero financial drift or loss; full alignment with Indian banking and taxation formats.
- **Trade-off:** Slightly more verbose arithmetic syntax compared to primitive operators (`+`, `-`, `*`).

---

## ADR 005: Automated Schema Versioning with Flyway

### Context
Using Hibernate's `ddl-auto=update` in production environments is dangerous as it can silently alter columns, cannot handle data migrations (such as password hashing or backfilling defaults), and does not support rollbacks or migration auditing.

### Decision
Disable Hibernate DDL auto-generation (`spring.jpa.hibernate.ddl-auto=validate`) and employ **Flyway** for database versioning:
- `V1__init_schema.sql`: Establishes initial tables, constraints, foreign keys, and indexes.
- `V2__seed_default_categories.sql`: Seeds 18 standard Indian expense and income categories.

### Consequences
- **Positive:** Predictable, audit-trailed database migrations; seamless CI/CD execution; zero manual SQL execution during upgrades.
- **Trade-off:** Developers must author SQL scripts for any schema change.

---

## ADR 006: Asynchronous JavaFX Concurrency Model

### Context
JavaFX updates UI components solely through the JavaFX Application Thread. Performing synchronous HTTP network calls on this thread blocks the UI event loop, causing visual stuttering, freezing, and OS "Application Not Responding" dialogs.

### Decision
Decouple UI interactions using JavaFX's `Task<T>` and `Platform.runLater()`:
- HTTP requests and JSON parsing execute in background worker threads.
- Callback hooks (`setOnSucceeded`, `setOnFailed`) handle UI modifications safely on the JavaFX Application Thread.
- UI elements display loading spinners and disable submit buttons while requests are in flight.

### Consequences
- **Positive:** 60 FPS smooth desktop user interface; resilient handling of network latencies; clear error feedback.
- **Trade-off:** Controller code requires structured concurrency patterns.
