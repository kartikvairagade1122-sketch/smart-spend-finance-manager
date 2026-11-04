# Java Developer Interview Preparation Guide

This guide contains in-depth technical questions and structured answers directly derived from the **Smart Spend – Personal Finance Management System**. Use these explanations in technical interviews to demonstrate your software engineering maturity.

---

## Part 1: Architecture & System Design

### Q1: Why did you decouple the JavaFX desktop frontend from the database using a Spring Boot REST API instead of connecting directly via JDBC?
**Answer:**  
In the legacy monolithic architecture, the desktop client connected directly to MySQL over JDBC. In a multi-user environment, this design has severe flaws:
1. **Security Vulnerability:** Database credentials (username and password) must be packaged into the client application binary. Anyone with a basic Java decompiler (like CFR or JD-GUI) could extract the database credentials and compromise all user records.
2. **Exposure of Database Ports:** Direct client connection requires exposing the MySQL port (3306) to the public internet, leaving it vulnerable to brute-force and DDoS attacks.
3. **Bypassing Business Rules:** Client-side business logic can easily be bypassed or modified by an attacker.
4. **Client-Server Decoupling:** By introducing a Spring Boot REST API layer, the database is completely shielded within a private network. The backend acts as a single source of truth for authentication, business validations, and auditing, while the desktop client acts purely as a stateless presentation layer communicating over secure HTTPS/JSON.

---

### Q2: What is the 3-tier architecture, and how does your project implement it?
**Answer:**  
The 3-tier architecture divides an application into three logical, decoupled computing layers:
1. **Presentation Layer (Tier 1):** The JavaFX 21 desktop client built with FXML views, modern CSS, and asynchronous controllers that interact with users and render financial metrics.
2. **Application / Business Logic Layer (Tier 2):** Spring Boot 3 application providing REST endpoints, Spring Security filter chains, business calculations (balance, monthly savings, budget alerts), and transaction boundary management.
3. **Data Layer (Tier 3):** MySQL 8.0 relational database with Flyway versioning, enforcing foreign key integrity, transactions (ACID), and data persistence.

---

## Part 2: Security, Authentication & Multi-Tenancy

### Q3: How did you implement authentication, and why did you choose a dual-token JWT model?
**Answer:**  
Authentication is implemented statelessly using **Spring Security 6** and **JJWT (0.12.6)**:
- **Dual-Token System:** We issue a short-lived **Access Token** (1 hour) and a long-lived **Refresh Token** (7 days).
- **Access Token:** Digitally signed with HMAC-SHA256 (`HS256`). Because it is stateless, the server verifies the cryptographic signature without querying the database on every HTTP request, ensuring high performance.
- **Refresh Token & Rotation:** Persisted in the MySQL database. When the access token expires, the client calls `/api/auth/refresh`. The backend validates the refresh token, revokes it immediately, and issues a new pair. If a refresh token is stolen or leaked, the user can be logged out across all sessions by revoking tokens in the database, solving the traditional stateless JWT revocation dilemma.
- **BCrypt:** Passwords are never stored in plaintext; they are hashed using BCrypt with 12 rounds of salting.

---

### Q4: What is an IDOR vulnerability, and how does Smart Spend prevent it?
**Answer:**  
**IDOR (Insecure Direct Object Reference)** occurs when an application exposes a reference to an internal object (such as a database primary key like `transaction_id = 15`) and fails to verify whether the requesting user owns that object. An attacker could simply change the URL from `/api/transactions/15` to `/api/transactions/16` and read, modify, or delete another person's financial records.

**How Smart Spend prevents IDOR:**
1. **Zero-Trust Client Identity:** The backend never trusts a `userId` supplied in the request body, query parameters, or URL path.
2. **Contextual Identity Extraction:** The user identity is extracted strictly on the server from the verified JWT claims stored in `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`.
3. **Repository-Level Filtering:** Every read, update, and delete query enforces ownership at the SQL/JPA query level:
   ```java
   @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId")
   Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
   ```
4. **404 Not Found Defense:** If a user tries to access a record belonging to another user, our service returns a `ResourceNotFoundException` (HTTP 404) rather than 403 Forbidden. This prevents attackers from guessing valid record IDs of other users.

---

### Q5: Why is CSRF disabled in your Spring Security configuration?
**Answer:**  
CSRF (Cross-Site Request Forgery) attacks occur when a malicious website tricks a victim's web browser into executing unwanted actions on a trusted site where the user is currently authenticated via browser cookies (e.g., `JSESSIONID`).  
Our application is a decoupled desktop client communicating via REST APIs where authentication credentials are transmitted in the `Authorization: Bearer <token>` header rather than stored in ambient browser cookies. Because standalone HTTP clients do not automatically attach cookies to cross-origin requests, CSRF vulnerabilities are not applicable, making `csrf.disable()` both safe and standard practice for token-based REST APIs.

---

## Part 3: Core Java, OOP & Financial Engineering

### Q6: Why did you use `BigDecimal` instead of `double` or `float` for financial amounts?
**Answer:**  
Binary floating-point types (`float` and `double`) represent numbers using IEEE 754 standard binary fractions. Certain decimal numbers like `0.1` cannot be represented precisely in base-2 binary, leading to rounding inaccuracies:
```java
double a = 0.1;
double b = 0.2;
System.out.println(a + b); // Prints: 0.30000000000000004
```
In a financial management application, compounding floating-point errors causes cash discrepancies, balance mismatches, and compliance violations.  
**`BigDecimal`** represents arbitrary-precision decimal numbers with complete precision. In Smart Spend:
- All financial fields use `BigDecimal`.
- In MySQL, the column type is `DECIMAL(12, 2)`.
- All division and percentage calculations use explicit rounding modes: `RoundingMode.HALF_EVEN` (Banker's Rounding), which minimizes cumulative rounding bias.

---

### Q7: How did you format monetary amounts for the Indian financial context?
**Answer:**  
In India, numbers follow the Lakhs and Crores system rather than the Western Millions and Billions system (e.g., `₹ 1,50,000.00` instead of `150,000.00`).  
We implemented a centralized `CurrencyHelper` utility:
```java
public class CurrencyHelper {
    private static final NumberFormat INR_FORMAT = 
        NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static String formatINR(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return INR_FORMAT.format(amount);
    }
}
```
This guarantees that all UI tables, cards, charts, and dialogues strictly display the Indian Rupee symbol (`₹`) with accurate grouping.

---

### Q8: What Object-Oriented Programming (OOP) principles did you apply in this project?
**Answer:**  
1. **Encapsulation:** Entities and DTOs encapsulate private fields with getters, setters, and validation constraints (`@NotBlank`, `@Positive`). Database connection details and token extraction are encapsulated inside helper services (`ApiClient`, `JwtTokenProvider`).
2. **Inheritance & Polymorphism:** JavaFX controllers implement initialize hooks (`Initializable`), while Spring Data JPA repositories extend `JpaRepository<T, ID>`, providing polymorphic query execution.
3. **Abstraction:** Clear interface-driven architecture where controllers interact with service interfaces (`TransactionService`, `AuthService`), hiding the underlying JPA queries and transaction boundaries.
4. **Single Responsibility Principle (SRP):** Each class has one distinct responsibility (e.g., `TransactionController` handles HTTP routing; `TransactionService` validates business logic; `TransactionRepository` handles database persistence).

---

## Part 4: Database, JPA & Hibernate

### Q9: Why did you use Flyway instead of Hibernate's `ddl-auto=update`?
**Answer:**  
Using `ddl-auto=update` is dangerous in production because:
1. It does not track migration history or versions.
2. It cannot safely rename columns, drop constraints, or execute data backfills (like hashing legacy plain passwords or inserting default Indian categories).
3. Concurrent application instances can cause race conditions during schema updates.  
**Flyway** provides:
- Version-controlled migration scripts (`V1__init_schema.sql`, `V2__seed_default_categories.sql`).
- An immutable `flyway_schema_history` audit table recording execution timestamps, checksums, and execution success.
- Consistent, reproducible database schemas across all development, staging, and production environments.

---

### Q10: How do you prevent the N+1 select problem in JPA?
**Answer:**  
The N+1 problem occurs when querying a parent entity causes Hibernate to execute 1 query for the parent list and N additional queries for each child association (e.g., fetching 50 transactions causing 50 separate queries to fetch category details).  
In Smart Spend:
1. Relationships like `Transaction.category` are set to `FetchType.LAZY` by default.
2. For high-volume queries or reporting, we use JPQL `JOIN FETCH` queries:
   ```java
   @Query("SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.user.id = :userId")
   List<Transaction> findAllWithCategoryByUserId(@Param("userId") Long userId);
   ```
This retrieves all transactions and their respective categories in a single SQL `INNER JOIN` query.

---

## Part 5: JavaFX & Client-Side Concurrency

### Q11: How do you prevent UI freezing when making HTTP network calls in JavaFX?
**Answer:**  
JavaFX operates on a single UI thread called the **JavaFX Application Thread**. Performing synchronous network I/O or heavy JSON deserialization on this thread blocks the event loop, causing the application window to freeze.  
To prevent this:
1. We run all API calls inside JavaFX `Task<T>` worker threads running on background thread pools.
2. Once the network request finishes, `task.setOnSucceeded()` or `Platform.runLater(Runnable)` marshals the result back onto the JavaFX Application Thread to safely update UI controls:
   ```java
   Task<DashboardSummaryResponse> task = new Task<>() {
       @Override
       protected DashboardSummaryResponse call() throws Exception {
           return dashboardService.getSummary();
       }
   };
   task.setOnSucceeded(e -> updateDashboardUI(task.getValue()));
   new Thread(task).start();
   ```

---

### Q12: How does the client maintain user session across application restarts?
**Answer:**  
The client implements a singleton `SessionManager`:
- Upon successful login, the `accessToken`, `refreshToken`, and user profile data are held in memory.
- When an API request encounters HTTP 401 (Unauthorized), the `ApiClient` automatically attempts a token refresh using `/api/auth/refresh`.
- If the refresh token has expired, `SessionManager.clearSession()` wipes the in-memory tokens and automatically redirects the user to the login screen using `SceneNavigator.loadLogin()`.

---

## Part 6: Behavioral & Project Walkthrough Questions

### Q13: What was the most challenging technical obstacle you overcame during this project?
**Answer:**  
The most challenging part was migrating the legacy single-user database into a multi-tenant schema without corrupting existing records. Specifically:
1. **Schema Inconsistencies:** Legacy table foreign keys were mismatched (`INT(11)` vs `BIGINT`), causing Flyway foreign key constraint failures (MySQL Error 1005). I resolved this by inspecting MySQL table definitions and executing precise schema adjustments to harmonize ID types across all relational entities.
2. **Password Security Upgrade:** Legacy records contained unhashed passwords. I engineered a secure migration routine to hash existing credentials with BCrypt (12 rounds) before enforcing strict Spring Security authentication.
3. **Case Sensitivity & Spring 6 Compatibility:** Handled parameter reflection requirements in Spring Boot 3 (`-parameters` flag) and aligned legacy transaction type casings (`'Income'` vs `'INCOME'`).

---

### Q14: How would you scale this application to support millions of users?
**Answer:**  
1. **Read/Write Splitting & Read Replicas:** Route heavy dashboard and reporting queries to MySQL read replicas while routing write transactions to the primary database.
2. **Distributed Caching with Redis:** Cache frequently accessed, semi-static data like global default categories and user profile information in Redis.
3. **Database Sharding:** Shard the `transactions` table by `user_id` so that financial journals for different users reside on separate database nodes.
4. **Message Queues for Asynchronous Processing:** Offload CSV report generation, audit log writing, and email notifications to asynchronous worker queues (RabbitMQ or Apache Kafka).
