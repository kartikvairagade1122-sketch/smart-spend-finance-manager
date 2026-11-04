# Project Walkthrough & Live Demo Guide

This document provides a structured script and presentation guide for showcasing the **Smart Spend – Personal Finance Management System** during technical interviews and portfolio reviews.

---

## 1. The 2-Minute Elevator Pitch

> *"Smart Spend is an enterprise-grade personal finance management application engineered with a modern 3-tier architecture: a responsive JavaFX 21 desktop client, a Spring Boot 3 REST API backend, and a MySQL 8.0 database.*
>
> *I designed it specifically for the Indian financial context with native Indian Rupee (`₹`) formatting and high-precision `BigDecimal` accounting. Its core engineering highlights include stateless JWT authentication with refresh token rotation, strict multi-tenant data isolation to eliminate IDOR vulnerabilities, automated schema migrations using Flyway, and an asynchronous JavaFX UI that guarantees zero screen freezing.*
>
> *I built it to solve the fundamental flaws of college-level desktop prototypes: hardcoded database credentials, client-side SQL execution, and lack of multi-user privacy."*

---

## 2. The 5-Minute Technical Deep-Dive

Use this structured breakdown when the interviewer asks: **"Can you walk me through the architecture and design decisions of your project?"**

### 1. Architectural Decoupling (1 minute)
- *"Originally, desktop applications connected directly to MySQL over JDBC. This exposes database credentials in the compiled jar and requires opening port 3306 to the internet.*
- *I completely decoupled the architecture into 3 independent tiers: the JavaFX presentation layer connects exclusively via HTTPS/JSON REST APIs to a Spring Boot 3 backend, which manages connection pooling and JPA persistence to MySQL.*
- *This ensures database credentials are completely shielded and business rules are enforced server-side."*

### 2. Multi-Tenant Security & Anti-IDOR Defense (1.5 minutes)
- *"Because financial data is strictly confidential, multi-tenancy was my top priority. I implemented a dual-token JWT mechanism with short-lived access tokens and database-backed refresh tokens.*
- *To prevent Insecure Direct Object References (IDOR), the backend never trusts a `userId` supplied from the client. Instead, the user identity is extracted securely from the verified JWT in Spring Security's `SecurityContextHolder`.*
- *Every single JPA repository query enforces ownership using composite conditions like `WHERE t.id = :id AND t.user.id = :userId`. If User A tries to view or alter User B's transaction, the query returns empty and throws a 404, preventing cross-tenant data leaks."*

### 3. Financial Arithmetic & Indian Rupee Standards (1 minute)
- *"In financial software, IEEE-754 floating-point numbers (`double`/`float`) cause binary rounding errors. I engineered all monetary calculations using `BigDecimal` with `RoundingMode.HALF_EVEN`.*
- *Additionally, for the Indian financial context, numbers are formatted using the Indian numbering system (`₹ xx,xx,xxx.xx`) via `CurrencyHelper` utilizing `Locale("en", "IN")`.*
- *The dashboard computes live metrics: Balance (`Total Income - Total Expenses`), Monthly Savings, and category-wise spending distributions with dynamic interactive charts."*

### 4. Client Responsiveness & Automated Testing (1.5 minutes)
- *"On the JavaFX desktop frontend, making synchronous HTTP requests freezes the UI thread. I implemented structured background tasks using JavaFX `Task<T>` and `Platform.runLater` to maintain smooth 60 FPS interactions.*
- *Finally, I validated the system with automated unit and security integration tests in JUnit 5 and Mockito, verifying multi-tenant isolation, BCrypt hashing, and financial calculations."*

---

## 3. Codebase Tour (What to Open in IntelliJ IDEA)

When sharing your screen during an interview, navigate through the code in this exact order:

```
1. pom.xml (Root)
   └── Highlight multi-module Maven setup (smart-spend-backend & smart-spend-frontend).

2. smart-spend-backend/.../config/SecurityConfig.java
   └── Explain Spring Security 6 stateless filter chain, JWT authentication filter, and BCrypt bean.

3. smart-spend-backend/.../security/JwtAuthenticationFilter.java
   └── Show how Bearer token is extracted from the Authorization header, validated, and loaded into SecurityContextHolder.

4. smart-spend-backend/.../service/impl/TransactionServiceImpl.java
   └── Show how userId is derived from SecurityContext (Anti-IDOR) and passed to repository methods.

5. smart-spend-backend/.../repository/TransactionRepository.java
   └── Highlight parameterized JPQL queries with composite user_id filtering.

6. smart-spend-frontend/.../util/CurrencyHelper.java
   └── Show Indian Rupee (₹) Locale formatting and BigDecimal handling.

7. smart-spend-frontend/.../controller/DashboardController.java
   └── Show asynchronous Task execution with Platform.runLater to prevent UI freezing.

8. smart-spend-backend/src/test/.../DataIsolationSecurityTest.java
   └── Run this test live in IntelliJ to prove User A cannot access User B's data!
```

---

## 4. Live Application Demo Script

If asked to run a live demonstration of the software:

### Step 1: Start the Backend & Client
1. Start the Spring Boot backend (`SmartSpendBackendApplication.java`). Show the Flyway migration logs initializing schema and default Indian categories.
2. Launch the JavaFX client (`SmartSpendApp.java`). Show the modern Dark Fintech login screen.

### Step 2: User A Session (Kartik)
1. Log in with `kartik@example.com` / `password123`.
2. **Dashboard:** Point out the live metric cards in Indian Rupees:
   - Total Income (`₹ 1,65,000.00`)
   - Total Expenses (`₹ 1,12,000.00`)
   - Current Balance (`₹ 53,000.00`)
   - Monthly Savings
3. Show the dynamic **Category Spending Pie Chart** and **Income vs Expense Bar Chart**.
4. Go to **Transactions Screen**:
   - Filter by `EXPENSE`.
   - Perform a keyword search (e.g., `"Groceries"`).
   - Click **Add Transaction** &rarr; Add an expense of `₹ 2,450.00` under *Groceries & Kirana*.
   - Notice the table immediately updates and the dashboard balance reflects the new expense.
5. Go to **Budgets Screen**:
   - Show the budget progress bar for Groceries. Show how color dynamically shifts from Green to Amber to Red based on utilization percentage.
6. Go to **Reports Screen**:
   - View the monthly breakdown table.
   - Click **Export CSV** &rarr; Show downloaded CSV file containing exact Indian Rupee transactions.

### Step 3: Demonstrate Multi-User Data Isolation (Crucial!)
1. Click **Logout**.
2. Register a new user:
   - Full Name: `Aarav Sharma`
   - Email: `aarav@example.com`
   - Password: `Password@123`
3. Log in as Aarav Sharma.
4. **Demonstrate Data Isolation:**
   - Notice that Aarav's dashboard displays **`₹0.00` Balance, `₹0.00` Income, `₹0.00` Expenses**.
   - None of Kartik's transactions or budgets appear in Aarav's view.
   - Add an income for Aarav: `₹ 75,000.00` under *Salary*.
   - Aarav's balance shows `₹ 75,000.00`.
5. Explain to the interviewer:
   - *"This proves complete multi-tenant database isolation. Even if an attacker tried to forge API calls with Kartik's transaction IDs while logged in as Aarav, the server rejects it with a 404 Not Found."*

---

## 5. Potential Trap Questions & Smart Responses

### Trap Q1: "Why not use Spring MVC with Thymeleaf or React instead of JavaFX?"
**Answer:**  
*"While web frameworks like React are popular, desktop applications remain standard for internal financial tools, trading platforms, and accounting software (like Tally) due to native OS integration, keyboard shortcuts, and responsiveness. Building this in JavaFX with a Spring Boot REST backend demonstrates that I understand full-stack enterprise boundaries: whether the client is JavaFX, React, or an Android app, the backend REST API and security architecture remain completely decoupled and reusable."*

### Trap Q2: "Why did you use MySQL instead of PostgreSQL or MongoDB?"
**Answer:**  
*"Financial ledgers require strict ACID compliance and relational integrity. In MongoDB (NoSQL), multi-document transactions and relational foreign keys require complex application-level handling. MySQL 8.0 with InnoDB provides rock-solid ACID transactions, foreign key cascades, and high-performance composite B-Tree indexes, making it the right engineering choice for structured ledger accounting."*
