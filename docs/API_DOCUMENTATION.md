# REST API Documentation

The Smart Spend backend exposes a RESTful JSON API. All protected endpoints require a valid JWT Access Token passed in the `Authorization` HTTP header:

```http
Authorization: Bearer <access_token>
```

All monetary values are represented in **Indian Rupees (`₹` / INR)** as numeric decimals.

---

## 1. Authentication Endpoints (`/api/auth`)

### 1.1 Register User
- **Method:** `POST /api/auth/register`
- **Authentication:** Public
- **Request Body:**
```json
{
  "fullName": "Kartik Vairagade",
  "email": "kartik@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```
- **Response (`201 Created`):**
```json
{
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "8f3b6c22-3860-45c1-8ce2-6b94e0e5a92a",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": 1,
      "fullName": "Kartik Vairagade",
      "email": "kartik@example.com",
      "role": "ROLE_USER"
    }
  }
}
```

### 1.2 Login User
- **Method:** `POST /api/auth/login`
- **Authentication:** Public
- **Request Body:**
```json
{
  "email": "kartik@example.com",
  "password": "password123"
}
```
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "9d8e7f6a-5b4c-3d2e-1f0a-b9c8d7e6f5a4",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": 1,
      "fullName": "Kartik Vairagade",
      "email": "kartik@example.com",
      "role": "ROLE_USER"
    }
  }
}
```

### 1.3 Refresh Access Token
- **Method:** `POST /api/auth/refresh`
- **Authentication:** Public
- **Request Body:**
```json
{
  "refreshToken": "9d8e7f6a-5b4c-3d2e-1f0a-b9c8d7e6f5a4"
}
```
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "c1a2b3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "tokenType": "Bearer",
    "expiresIn": 3600000
  }
}
```

### 1.4 Logout
- **Method:** `POST /api/auth/logout`
- **Authentication:** Required
- **Request Body:**
```json
{
  "refreshToken": "c1a2b3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
}
```
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Logged out successfully",
  "data": null
}
```

---

## 2. Transactions Endpoints (`/api/transactions`)

### 2.1 Get Filtered Transactions (Paginated)
- **Method:** `GET /api/transactions`
- **Authentication:** Required
- **Query Parameters:**
  - `page` *(optional, default: 0)*
  - `size` *(optional, default: 50)*
  - `type` *(optional: `INCOME` or `EXPENSE`)*
  - `categoryId` *(optional: category ID)*
  - `startDate` *(optional: YYYY-MM-DD)*
  - `endDate` *(optional: YYYY-MM-DD)*
  - `query` *(optional: search keyword)*
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Transactions retrieved",
  "data": {
    "content": [
      {
        "id": 33,
        "categoryId": 1,
        "categoryName": "Salary",
        "categoryIcon": "briefcase",
        "categoryColor": "#10b981",
        "type": "INCOME",
        "amount": 5000.00,
        "description": "Freelance web design advance",
        "transactionDate": "2026-11-04",
        "createdAt": "2026-11-04T23:44:32"
      }
    ],
    "page": 0,
    "size": 50,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 2.2 Create Transaction
- **Method:** `POST /api/transactions`
- **Authentication:** Required
- **Request Body:**
```json
{
  "categoryId": 2,
  "type": "EXPENSE",
  "amount": 2850.50,
  "description": "Monthly Grocery shopping at D-Mart",
  "transactionDate": "2026-11-04"
}
```
- **Response (`201 Created`):**
```json
{
  "status": 201,
  "message": "Transaction created successfully",
  "data": {
    "id": 34,
    "categoryId": 2,
    "categoryName": "Groceries & Kirana",
    "categoryIcon": "shopping-cart",
    "categoryColor": "#f59e0b",
    "type": "EXPENSE",
    "amount": 2850.50,
    "description": "Monthly Grocery shopping at D-Mart",
    "transactionDate": "2026-11-04",
    "createdAt": "2026-11-04T23:50:00"
  }
}
```

### 2.3 Update Transaction
- **Method:** `PUT /api/transactions/{id}`
- **Authentication:** Required (Anti-IDOR enforced)
- **Request Body:**
```json
{
  "categoryId": 2,
  "type": "EXPENSE",
  "amount": 3200.00,
  "description": "Monthly Grocery shopping with household items",
  "transactionDate": "2026-11-04"
}
```

### 2.4 Delete Transaction
- **Method:** `DELETE /api/transactions/{id}`
- **Authentication:** Required (Anti-IDOR enforced)
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Transaction deleted successfully",
  "data": null
}
```

---

## 3. Categories Endpoints (`/api/categories`)

### 3.1 List Categories
- **Method:** `GET /api/categories`
- **Authentication:** Required
- **Query Parameters:** `type` *(optional: `INCOME` or `EXPENSE`)*
- **Response (`200 OK`):** Returns default system categories plus custom categories created by the authenticated user.

### 3.2 Create Custom Category
- **Method:** `POST /api/categories`
- **Authentication:** Required
- **Request Body:**
```json
{
  "name": "Gym & Fitness Membership",
  "type": "EXPENSE",
  "icon": "fitness",
  "color": "#ec4899"
}
```

---

## 4. Budgets Endpoints (`/api/budgets`)

### 4.1 Get Monthly Budgets with Live Utilization
- **Method:** `GET /api/budgets?month=11&year=2026`
- **Authentication:** Required
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Budgets retrieved",
  "data": [
    {
      "id": 5,
      "categoryId": 2,
      "categoryName": "Groceries & Kirana",
      "categoryIcon": "shopping-cart",
      "categoryColor": "#f59e0b",
      "monthlyLimit": 15000.00,
      "spentAmount": 8500.00,
      "remainingAmount": 6500.00,
      "percentageUsed": 56.67,
      "month": 11,
      "year": 2026
    }
  ]
}
```

### 4.2 Create or Update Budget
- **Method:** `POST /api/budgets`
- **Authentication:** Required
- **Request Body:**
```json
{
  "categoryId": 2,
  "monthlyLimit": 15000.00,
  "month": 11,
  "year": 2026
}
```

---

## 5. Dashboard Endpoints (`/api/dashboard`)

### 5.1 Get Real-Time Financial Summary
- **Method:** `GET /api/dashboard/summary`
- **Authentication:** Required
- **Response (`200 OK`):**
```json
{
  "status": 200,
  "message": "Dashboard summary retrieved",
  "data": {
    "totalIncome": 165000.00,
    "totalExpenses": 112000.00,
    "currentBalance": 53000.00,
    "monthlyIncome": 55000.00,
    "monthlyExpenses": 32000.00,
    "monthlySavings": 23000.00,
    "recentTransactions": [ ... ],
    "categoryBreakdown": [
      {
        "categoryName": "Rent & Housing",
        "categoryColor": "#6366f1",
        "totalAmount": 20000.00,
        "percentage": 62.50
      },
      {
        "categoryName": "Groceries & Kirana",
        "categoryColor": "#f59e0b",
        "totalAmount": 12000.00,
        "percentage": 37.50
      }
    ],
    "monthlyTrends": [
      {
        "month": "October",
        "income": 55000.00,
        "expense": 40000.00
      },
      {
        "month": "November",
        "income": 55000.00,
        "expense": 32000.00
      }
    ]
  }
}
```

---

## 6. Reports Endpoints (`/api/reports`)

### 6.1 Get Monthly Spending Breakdown
- **Method:** `GET /api/reports/monthly?year=2026`
- **Authentication:** Required

### 6.2 Get Category Spending Distribution
- **Method:** `GET /api/reports/category-breakdown?month=11&year=2026`
- **Authentication:** Required

### 6.3 Export Transactions as CSV
- **Method:** `GET /api/reports/export/csv?startDate=2026-01-01&endDate=2026-11-04`
- **Authentication:** Required
- **Response Headers:** `Content-Type: text/csv`, `Content-Disposition: attachment; filename="smartspend_transactions.csv"`
- **Response Body:**
```csv
Transaction ID,Date,Type,Category,Description,Amount (INR)
33,2026-11-04,INCOME,Salary,"Freelance web design advance",5000.00
32,2026-11-02,EXPENSE,"Groceries & Kirana","Weekly vegetables and fruits",1250.00
```

---

## 7. Profile Endpoints (`/api/profile`)

### 7.1 Get User Profile
- **Method:** `GET /api/profile`
- **Authentication:** Required

### 7.2 Update User Profile
- **Method:** `PUT /api/profile`
- **Authentication:** Required
- **Request Body:**
```json
{
  "fullName": "Kartik Vairagade"
}
```

### 7.3 Change Password
- **Method:** `POST /api/profile/change-password`
- **Authentication:** Required
- **Request Body:**
```json
{
  "currentPassword": "password123",
  "newPassword": "newSecurePassword@2026",
  "confirmPassword": "newSecurePassword@2026"
}
```


---

## 8. Live Stock Portfolio & Market Rates (`/api/portfolio`)

### 8.1 Get Portfolio Summary & Live Valuation
- **Method:** `GET /api/portfolio`
- **Authentication:** Required
- **Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Portfolio retrieved successfully",
  "data": {
    "totalInvested": 12000.00,
    "currentValue": 13106.00,
    "totalPnl": 1106.00,
    "totalPnlPercentage": 9.22,
    "holdingsCount": 1,
    "profitableCount": 1,
    "lossCount": 0,
    "holdings": [
      {
        "id": 1,
        "symbol": "RELIANCE.NS",
        "companyName": "RELIANCE INDUSTRIES LTD",
        "quantity": 10,
        "buyPrice": 1200.00,
        "buyDate": "2026-10-15",
        "livePrice": 1310.60,
        "totalInvested": 12000.00,
        "currentValue": 13106.00,
        "pnlAmount": 1106.00,
        "pnlPercentage": 9.22,
        "dayChangeAmount": -3.00,
        "dayChangePercent": -0.03,
        "currency": "INR"
      }
    ],
    "currency": "INR"
  }
}
```

### 8.2 Get Real-Time Stock Quote Preview (NSE)
- **Method:** `GET /api/portfolio/quote/{symbol}`
- **Authentication:** Required
- **Example:** `GET /api/portfolio/quote/TCS`
- **Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Quote fetched successfully",
  "data": {
    "symbol": "TCS.NS",
    "companyName": "TATA CONSULTANCY SERV LT",
    "currentPrice": 2485.00,
    "previousClose": 2436.00,
    "dayChange": 49.00,
    "dayChangePercent": 2.01,
    "currency": "INR",
    "marketOpen": true
  }
}
```

### 8.3 Add Stock Holding
- **Method:** `POST /api/portfolio/holdings`
- **Authentication:** Required
- **Request Body:**
```json
{
  "symbol": "TCS",
  "companyName": "Tata Consultancy Services Ltd",
  "quantity": 5,
  "buyPrice": 2400.00,
  "buyDate": "2026-11-01"
}
```

### 8.4 Update Stock Holding
- **Method:** `PUT /api/portfolio/holdings/{id}`
- **Authentication:** Required (Anti-IDOR enforced)

### 8.5 Delete Stock Holding
- **Method:** `DELETE /api/portfolio/holdings/{id}`
- **Authentication:** Required (Anti-IDOR enforced)
