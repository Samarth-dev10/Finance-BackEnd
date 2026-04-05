# Finance Backend API — Postman Collection Guide

This guide explains how to import and run the provided Postman collection to test the **Finance Backend API** end-to-end.

> Collection file: `Finance-Backend-API.postman_collection.json`

---

## 1) Quick Start (2 minutes)

1. **Start the Spring Boot app**
   - Default: `http://localhost:8080`
2. **Import the collection into Postman**
   - Postman → **File** → **Import** → select `Finance-Backend-API.postman_collection.json`
3. In Postman, open the collection → **Variables** and confirm:
   - `baseUrl` = `http://localhost:8080`
4. Run requests in this order:
   1. `00 — PRE-FLIGHT CHECK` → `Health Check — Roles List (Public Endpoint)`
   2. `01 — AUTHENTICATION` → `Login as Admin ⭐ (Run First — Saves Token)`
   3. Then execute the remaining folders top-to-bottom.

---

## 2) What this collection automates for you

This collection includes Postman **Tests** scripts that automatically capture and reuse values so you don’t have to copy/paste.

### Auto-saved collection variables

| Variable | Set by | Used by | Meaning |
|---|---|---|---|
| `baseUrl` | you | all requests | API base URL |
| `authToken` | **Login as Admin** | all protected endpoints | JWT Bearer token |
| `createdUserId` | **Create New User — ANALYST Role** | user GET/PATCH/status/delete | The user created during the run |
| `createdTransactionId` | **Create Transaction — INCOME (Salary)** | transaction GET/PATCH/delete | The transaction created during the run |
| `secondTransactionId` | **Create Transaction — EXPENSE (Rent)** | filter tests | Second transaction for filter validation |

---

## 3) Authentication model

Most endpoints require:

- `Authorization: Bearer {{authToken}}`

The request **`Login as Admin ⭐ (Run First — Saves Token)`** extracts:

- `response.data.token` → stored as `{{authToken}}`

### Seeded credentials

By default the backend seeds an admin user:

- username: `admin`
- password: `admin123`

---

## 4) Recommended execution order (happy path)

Run folders in numeric order:

1. **00 — PRE-FLIGHT CHECK**
   - Confirms server is running and roles are seeded.
2. **01 — AUTHENTICATION**
   - Gets a JWT token and stores it.
3. **02 — ROLES**
   - Public role browsing / sanity checks.
4. **03 — USER MANAGEMENT**
   - Create → list → search → get-by-id → patch → deactivate/reactivate → delete.
5. **04 — TRANSACTION MANAGEMENT**
   - Create sample income/expense → filter → patch → delete.
6. **05 — DASHBOARD ANALYTICS**
   - Summary endpoints using the transactions created earlier.
7. **06 — ACCESS CONTROL VERIFICATION**
   - Validates permissions by role.

---

## 5) Data notes / sample identities

The collection contains realistic sample test input.

It includes **"Samarth Narayankar"** in two places (intentionally, without renaming every sample):

1. In `03 — USER MANAGEMENT` → **Create New User — ANALYST Role**
   - `fullName`: `Samarth Narayankar`
   - `username`: `samarth_analyst`
2. In `04 — TRANSACTION MANAGEMENT` → **Create Transaction — INCOME (Salary)**
   - `notes` includes: `Samarth Narayankar`

All other sample users/notes remain unchanged to keep test data diverse.

---

## 6) Common troubleshooting

### 401 / 403 errors on protected endpoints
- Ensure you ran **Login as Admin** first.
- Confirm the request has header: `Authorization: Bearer {{authToken}}`.

### 400 validation errors
- Some requests intentionally test negatives (e.g., blank fields, wrong password).
- Run the “happy path” requests first.

### Roles list not returning 3 roles
- Verify `src/main/resources/data.sql` ran (or that your DB is clean and seeded).

---

## 7) Tips for team usage

- Prefer using **collection variables** (already built-in) over hardcoded IDs.
- If you need environments (Dev/QA/Prod):
  - Create a Postman Environment and override `baseUrl` there.
  - Keep `authToken` as a **collection variable** (tokens vary per environment and expire).

---

## 8) Optional: run all requests via Postman Runner

- Postman → collection → **Run collection**
- Run in order; keep “Stop on error” enabled initially.

If you want a CI-style run:
- Export an environment file and run with Newman.

