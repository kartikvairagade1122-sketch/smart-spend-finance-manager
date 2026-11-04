# Resume Project Entry Guide

Use the tailored bullet points and summaries below to showcase the **Smart Spend – Personal Finance Management System** on your resume and LinkedIn profile.

---

## 1. Project Title & Links (Resume Header)

**Smart Spend – Multi-User Personal Finance Management System**  
*Technologies:* Java 17, Spring Boot 3, Spring Security 6, JWT, JavaFX 21, MySQL 8, Hibernate/JPA, Flyway, Maven, Docker  
*GitHub Repository:* [github.com/kartikvairagade1122-sketch/smart-spend-finance-manager](https://github.com/kartikvairagade1122-sketch)

---

## 2. Option A: Java Backend Developer Focus (Recommended for Backend Roles)

- Architected and engineered an enterprise-grade 3-tier personal finance platform using **Java 17**, **Spring Boot 3.3**, and **MySQL 8.0**, decoupling presentation from business persistence via secure RESTful APIs.
- Designed a stateless security architecture utilizing **Spring Security 6** and **JJWT**, implementing HMAC-SHA256 dual-token rotation (short-lived Access Tokens + persisted Refresh Tokens) and **BCrypt (12 rounds)** password hashing.
- Enforced strict **multi-tenant data isolation and Insecure Direct Object Reference (IDOR) prevention** by deriving user context exclusively from JWT `SecurityContextHolder` and applying composite repository-level filters (`WHERE user_id = :userId`).
- Formulated zero-loss monetary calculations using arbitrary-precision **`BigDecimal` arithmetic** and `RoundingMode.HALF_EVEN`, formatting financial streams according to **Indian Rupee (`₹` / INR)** numbering standards (Lakhs & Crores).
- Integrated **Flyway** for database versioning and migration auditing, designing a 3NF normalized schema with composite indexing for sub-100ms dashboard aggregations.
- Developed comprehensive automated test suites using **JUnit 5**, **Mockito**, and **Spring Boot Test**, achieving zero-defect validation across authentication, financial arithmetic, and multi-tenant isolation.
- Containerized the backend service using **Docker** multi-stage builds and orchestrated environments with **Docker Compose**.

---

## 3. Option B: Full-Stack Java / Desktop Application Developer Focus

- Engineered an asynchronous, multi-user personal finance management desktop application in **JavaFX 21** communicating with a **Spring Boot 3** REST API backend.
- Developed an interactive Dark Fintech UI in **FXML** and modern **CSS**, implementing non-blocking asynchronous network requests with JavaFX `Task<T>` and `Platform.runLater` to maintain a responsive 60 FPS user experience.
- Built a real-time financial analytics dashboard featuring dynamic KPI cards (Income, Expenses, Balance, Monthly Savings in `₹`), spending breakdown pie charts, and monthly trend bar charts.
- Designed budget monitoring modules with real-time utilization progress bars, threshold breach alerts, and dynamic category tracking.
- Created financial export utilities enabling users to download transaction journals in **CSV** format for external spreadsheet and tax analysis.
- Transitioned a single-user legacy JDBC monolith into a decoupled, secure client-server architecture with automated schema migrations via **Flyway**.

---

## 4. Option C: Concise 3-Bullet Version (Space-Constrained Resumes)

- **Smart Spend – Personal Finance Management System** *(Java 17, Spring Boot 3, Spring Security 6, JavaFX 21, MySQL 8, Docker)*
  - Developed a decoupled 3-tier personal finance manager with a JavaFX desktop frontend and a Spring Boot 3 REST API backend, featuring localized Indian Rupee (`₹`) accounting.
  - Implemented stateless authentication using Spring Security 6, JJWT token rotation, BCrypt encryption, and strict anti-IDOR repository-level multi-tenant isolation.
  - Optimized database schema with Flyway migrations, composite indexing, and `BigDecimal` financial arithmetic, containerizing deployment with Docker & Docker Compose.

---

## 5. ATS Skills & Keywords Matrix

Ensure these keywords are included in your resume's **Technical Skills** section:

| Category | Keywords |
| :--- | :--- |
| **Languages** | Java (Core Java, Java 17 LTS, Collections Framework, Streams API, Concurrency, Multithreading, OOP) |
| **Frameworks** | Spring Boot 3, Spring Web, Spring Security 6, Spring Data JPA, Hibernate ORM |
| **UI & Desktop** | JavaFX 21, FXML, CSS3, Scene Builder, JavaFX Charts |
| **Web & APIs** | RESTful APIs, JSON, HTTP/2, Java 11 HttpClient, Jackson Databind |
| **Security** | JWT (JSON Web Tokens), BCrypt Hashing, Role-Based Access Control (RBAC), Anti-IDOR Defense |
| **Databases** | MySQL 8.0, Relational Schema Design (3NF), SQL, Indexing, Flyway Migration |
| **Tools & DevOps** | Git, GitHub, Maven, Docker, Docker Compose, IntelliJ IDEA |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, Integration Testing |
| **Domain** | Personal Finance Management, Financial Arithmetic (`BigDecimal`), Indian Rupee (`₹`) Localization |
