# System Architecture Documentation

## 1. Architectural Philosophy & Principles

The **Smart Spend – Personal Finance Management System** is architected to transition from a single-user monolithic JDBC desktop utility into a scalable, secure, 3-tier enterprise application.

The core architectural principles guiding this system include:

1. **Separation of Concerns (SoC):** Distinct boundaries between presentation (JavaFX), business logic & security (Spring Boot), and data persistence (MySQL).
2. **Zero-Trust Client Boundary:** The JavaFX client operates as an untrusted public client. It holds zero direct database credentials and cannot issue raw queries. All business rules and authorization checks are enforced server-side.
3. **Stateless Scalability:** User sessions are managed statelessly via JSON Web Tokens (JWT). The backend server does not maintain sticky HTTP sessions in memory.
4. **Defense in Depth & Anti-IDOR (Insecure Direct Object Reference):** Multi-tenant data isolation is implemented strictly at the repository query level (`WHERE user_id = :userId`) using identities derived from cryptographically verified tokens.
5. **Zero-Loss Financial Precision:** All monetary amounts are handled strictly as `java.math.BigDecimal` with explicit scale and rounding modes, formatted for the Indian financial context (`₹` / INR).

---

## 2. 3-Tier Layered Architecture

```
+-------------------------------------------------------------------------+
|                          TIER 1: PRESENTATION LAYER                     |
|                                                                         |
|   +-----------------------+     +-----------------------------------+   |
|   |   JavaFX FXML Views   | <-> |       JavaFX Controllers          |   |
|   |    (Dark Fintech CSS) |     |  (Background Tasks / Async UI)    |   |
|   +-----------------------+     +-----------------------------------+   |
|                                                  |                      |
|                                                  v                      |
|                                 +-----------------------------------+   |
|                                 |       ApiClient (java.net.http)   |   |
|                                 |   (SessionManager & Bearer JWT)   |   |
|                                 +-----------------------------------+   |
+---------------------------------------------------|---------------------+
                                                    | JSON over HTTPS (REST API)
                                                    v
+-------------------------------------------------------------------------+
|                      TIER 2: APPLICATION & SECURITY LAYER               |
|                                                                         |
|   +-----------------------------------------------------------------+   |
|   |         Spring Security 6 Filter Chain (Stateless JWT)          |   |
|   |    JwtAuthenticationFilter -> SecurityContext -> UserPrincipal   |   |
|   +-----------------------------------------------------------------+   |
|                                   |                                     |
|                                   v                                     |
|   +-----------------------------------------------------------------+   |
|   |     Spring Web REST Controllers (@RestController, Validation)   |   |
|   |   AuthController, TransactionController, DashboardController...|   |
|   +-----------------------------------------------------------------+   |
|                                   |                                     |
|                                   v                                     |
|   +-----------------------------------------------------------------+   |
|   |           Service Layer (Business Logic & Anti-IDOR)            |   |
|   |  TransactionService, BudgetService, DashboardService, Report... |   |
|   +-----------------------------------------------------------------+   |
|                                   |                                     |
|                                   v                                     |
|   +-----------------------------------------------------------------+   |
|   |       Data Access Layer (Spring Data JPA / Hibernate 6)         |   |
|   |  TransactionRepository, BudgetRepository, CategoryRepository... |   |
|   +-----------------------------------------------------------------+   |
+-----------------------------------|-------------------------------------+
                                    | JDBC Connection Pool (HikariCP)
                                    v
+-------------------------------------------------------------------------+
|                        TIER 3: DATA PERSISTENCE LAYER                   |
|                                                                         |
|   +-----------------------------------------------------------------+   |
|   |                    MySQL 8.0 InnoDB Database                    |   |
|   |  Foreign Keys, Unique Indexes, Flyway Versioned Migrations (V1,V2)|  |
|   +-----------------------------------------------------------------+   |
+-------------------------------------------------------------------------+
```

---

## 3. Security Architecture

### 3.1 Dual-Token Authentication Lifecycle

Authentication uses an asymmetric lifetime pattern:

1. **Access Token (Short-lived, 1 hour):**
   - Signed with HMAC-SHA256 (`HS256`) using a 256-bit secret key.
   - Contains claims: `sub` (User Email), `userId`, `role`, `iat` (issued at), and `exp` (expiration).
   - Carried in the HTTP `Authorization: Bearer <token>` header with every API request.
   - Inspected by `JwtAuthenticationFilter` before reaching any `@RestController`.

2. **Refresh Token (Long-lived, 7 days):**
   - Cryptographically random UUID v4 string stored in the `refresh_tokens` database table.
   - Associated with `user_id`, `expiry_date`, and a `revoked` flag.
   - When the access token expires (HTTP 401), the JavaFX client automatically invokes `/api/auth/refresh` using the refresh token.
   - The backend validates the refresh token, revokes the old one, and generates a new pair (Refresh Token Rotation), preventing replay attacks.

3. **Password Security:**
   - Hashed using **BCrypt** with an adaptive work factor of 12 rounds.
   - Plaintext passwords are never logged, stored, or transmitted in responses.

### 3.2 Insecure Direct Object Reference (IDOR) Defense

A critical flaw in basic multi-user systems is accepting the tenant/user identity from the client payload (e.g., `POST /api/transactions` with `{ "userId": 2, ... }`). An attacker could alter the payload to read or modify another user's records.

In Smart Spend:
1. `userId` in the client request body or URL path is **completely ignored or omitted**.
2. The current user identity is extracted directly from the verified `SecurityContextHolder`:
   ```java
   UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
       .getAuthentication().getPrincipal();
   Long userId = principal.getId();
   ```
3. All repository lookups enforce ownership:
   ```java
   @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId")
   Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
   ```
4. If a user attempts to update or delete a transaction ID that does not belong to them, the query returns empty, triggering a `ResourceNotFoundException` (HTTP 404). This prevents attackers from even discerning whether an ID exists for another user.

---

## 4. Frontend Client Architecture (JavaFX)

### 4.1 UI Thread Responsiveness & Asynchronous Execution
JavaFX runs a single-threaded UI event loop (Application Thread). Executing network requests or heavy JSON parsing on this thread freezes the UI, causing application hangs ("Not Responding").

Smart Spend encapsulates all network requests inside JavaFX background tasks:
```java
Task<List<TransactionItem>> task = new Task<>() {
    @Override
    protected List<TransactionItem> call() throws Exception {
        return transactionService.getTransactions(...);
    }
};

task.setOnSucceeded(e -> {
    // Safely updates UI controls on the JavaFX Application Thread
    transactionTable.getItems().setAll(task.getValue());
});

task.setOnFailed(e -> {
    AlertHelper.showError("Error", task.getException().getMessage());
});

new Thread(task).start();
```

### 4.2 Modular Screen Architecture
- **Root Container:** `main_layout.fxml` provides the persistent sidebar navigation, current user greeting, and dynamic center content pane.
- **View Switching:** `SceneNavigator` loads sub-views (`dashboard.fxml`, `transactions.fxml`, `budgets.fxml`, `reports.fxml`, `profile.fxml`) dynamically into the center pane without flickering or recreating the window frame.

---

## 5. Currency & Financial Arithmetic Standardization

All currency formatting respects the Indian Numbering System (`en_IN`):
- Hundreds, thousands, lakhs ($10^5$), and crores ($10^7$).
- Example: `₹ 1,50,000.00` (One Lakh Fifty Thousand) rather than Western `150,000.00`.
- Implemented via `CurrencyHelper.java`:
  ```java
  private static final NumberFormat INR_FORMAT = 
      NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
  ```
- All calculations (`Total Income - Total Expenses = Balance`) use `BigDecimal.subtract()` and `BigDecimal.setScale(2, RoundingMode.HALF_EVEN)` to adhere to banker's rounding standards.
