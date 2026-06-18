# Travel Planner Service

Part of the **Employee Travel Desk (ETD)** system — a Cognizant FSE Business Aligned Project.

This microservice manages the **travel request lifecycle** — employees raise travel requests, HRs approve or reject them, and HRs calculate the approved budget for the trip.

---

## What this service does

| Responsibility | Details |
|---|---|
| **Travel requests** | Employees raise requests specifying destination, dates, purpose and priority |
| **Employee validation** | Validates that `raisedByEmployeeId` matches the logged-in user and has `Employee` role; validates that `toBeApprovedByHrId` has `HR` role |
| **Approval workflow** | HR approves or rejects pending requests |
| **Budget calculation** | HR selects travel mode and hotel rating; service calculates total budget based on employee grade |
| **Location lookup** | Provides list of available travel destinations seeded on startup |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Security | Spring Security + JWT validation (JJWT 0.12.6) |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 — own DB `travel_planner` (connector `com.mysql:mysql-connector-j`) |
| HTTP client | Spring Cloud OpenFeign (calls account-management + auth-service) |
| Build tool | Gradle |
| Utilities | Lombok, Springdoc OpenAPI |

---

## Prerequisites

- Java 21
- Gradle (system install — wrapper jar is not committed)
- **auth-service** running on port 8080 (blacklist checks on every request)
- **account-management** running on port 8081 (employee validation + budget grade lookup)

---

## Service Startup Order

```
1. account-management (port 8081)  ← start first
2. auth-service (port 8080)        ← start second
3. travel-planner (port 8082)      ← start last
```

---

## Running the application

```bash
# Build
gradle build

# Run — starts on port 8082
gradle bootRun

# Run tests
gradle test

# Clean
gradle clean
```

On first startup **DataInitializer** automatically seeds 8 locations:
> Mumbai, Delhi, Bangalore, Chennai, Hyderabad, Pune, Kolkata, Ahmedabad

---

## Authentication

Login is handled by **auth-service (port 8080)**:
```
POST http://localhost:8080/login   →  get access token
```

All travel-planner requests must include the token:
```
Authorization: Bearer <accessToken>
```

On every request, travel-planner calls auth-service to check if the token was blacklisted:
```
GET http://localhost:8080/auth/blacklist/check?token=<accessToken>
```
If blacklisted → `403`. Fail-open if auth-service is unreachable.

---

## Database

travel-planner has its **own MySQL 8 database** `travel_planner` — it does not share account-management's database.

| Setting | Value |
|---|---|
| Engine | MySQL 8 (`localhost:3306`) |
| Database | `travel_planner` |
| Driver | `com.mysql.cj.jdbc.Driver` |
| Dialect | `org.hibernate.dialect.MySQLDialect` |
| Schema | `spring.jpa.hibernate.ddl-auto=update` (Hibernate auto-creates/updates tables) |
| Username | `root` |

### Fresh start

DROP and re-CREATE the MySQL `travel_planner` database (or TRUNCATE its tables), then restart the service so Hibernate recreates the schema and the locations are re-seeded on startup.

---

## API Reference

Base URL: `http://localhost:8082`

All endpoints require `Authorization: Bearer <token>` from auth-service.

---

### GET /api/travelrequests/locations
*Any authenticated role*

Returns all available travel destinations.

**Response `200`:**
```json
[
  { "id": 1, "name": "Mumbai" },
  { "id": 2, "name": "Delhi" },
  { "id": 3, "name": "Bangalore" },
  { "id": 4, "name": "Chennai" },
  { "id": 5, "name": "Hyderabad" },
  { "id": 6, "name": "Pune" },
  { "id": 7, "name": "Kolkata" },
  { "id": 8, "name": "Ahmedabad" }
]
```

---

### POST /api/travelrequests/new
*Employee only*

Raises a new travel request.

**Body:**
```json
{
  "raisedByEmployeeId": 100002,
  "toBeApprovedByHrId": 100000,
  "fromDate": 1782864000000,
  "toDate": 1783641600000,
  "purposeOfTravel": "Client meeting and project review",
  "locationId": 3,
  "priority": "TWO"
}
```

> `priority` values: `ONE` (max 30 days), `TWO` (max 20 days), `THREE` (max 10 days)
> Dates are Unix timestamps in milliseconds
> `raisedByEmployeeId` must match the logged-in user and have `Employee` role
> `toBeApprovedByHrId` must have `HR` role
> Both IDs are validated against account-management before the request is saved

**Response `200`:**
```json
{
  "requestId": 1,
  "raisedByEmployeeId": 100002,
  "toBeApprovedByHrId": 100000,
  "requestRaisedOn": 1749135600000,
  "fromDate": 1782864000000,
  "toDate": 1783641600000,
  "purposeOfTravel": "Client meeting and project review",
  "locationName": "Bangalore",
  "requestStatus": "NEW",
  "requestApprovedOn": null,
  "priority": "TWO"
}
```

---

### GET /api/travelrequests/{hrId}/pending
*HR only*

Returns all `NEW` requests assigned to the given HR.

```
GET /api/travelrequests/100000/pending
```

**Response `200`:** Array of travel request objects.

---

### GET /api/travelrequests/{trid}
*Any authenticated role*

Returns full detail of a travel request. Budget fields are included if budget has been calculated.

```
GET /api/travelrequests/1
```

**Response `200` (approved with budget):**
```json
{
  "requestId": 1,
  "raisedByEmployeeId": 100002,
  "toBeApprovedByHrId": 100000,
  "requestRaisedOn": 1749135600000,
  "fromDate": 1782864000000,
  "toDate": 1783641600000,
  "purposeOfTravel": "Client meeting and project review",
  "locationName": "Bangalore",
  "requestStatus": "APPROVED",
  "requestApprovedOn": 1749222000000,
  "priority": "TWO",
  "travelBudgetAllocationId": 1,
  "approvedBudget": 135000,
  "approvedModeOfTravel": "AIR",
  "approvedHotelStarRating": "3-STAR"
}
```

> Budget fields are omitted (`null`) if budget has not been calculated yet.

---

### PUT /api/travelrequests/{trid}/update
*HR only*

Approves or rejects a travel request. A request can only be updated **once**.

**Body:**
```json
{ "requestStatus": "APPROVED" }
```

> `requestStatus` values: `APPROVED`, `REJECTED` (case-insensitive)

**Response `200`:** Updated travel request object.

---

### POST /api/travelrequests/calculatebudget
*HR only — request must be APPROVED first*

Calculates and saves the total travel budget. Can only be called **once** per request.

**Body:**
```json
{
  "travelRequestId": 1,
  "approvedModeOfTravel": "AIR",
  "approvedHotelStarRating": "3-STAR"
}
```

> `approvedModeOfTravel` values: `AIR`, `TRAIN`, `BUS`
> Hotel rating for HR: `5-STAR` or `7-STAR`
> Hotel rating for others: `3-STAR` or `5-STAR`

**Response `200`:**
```json
135000
```

> Returns total budget as a number = `dailyRate × numberOfDays`

---

## Role Permissions

| Endpoint | HR | Employee | TravelDeskExe |
|---|:---:|:---:|:---:|
| `GET /api/travelrequests/locations` | ✅ | ✅ | ✅ |
| `GET /api/travelrequests/{trid}` | ✅ | ✅ | ✅ |
| `POST /api/travelrequests/new` | ❌ | ✅ | ❌ |
| `GET /api/travelrequests/{hrId}/pending` | ✅ | ❌ | ❌ |
| `PUT /api/travelrequests/{trid}/update` | ✅ | ❌ | ❌ |
| `POST /api/travelrequests/calculatebudget` | ✅ | ❌ | ❌ |

---

## Business Rules

### Priority and max trip duration

| Priority | Max days |
|---|---|
| `ONE` (highest) | 30 days |
| `TWO` | 20 days |
| `THREE` (lowest) | 10 days |

### Budget daily rates

Aligned with account-management's grade convention (lower id = higher seniority):

| Grade | Daily rate |
|---|---|
| Grade-1 (most senior) | ₹15,000 |
| Grade-2 | ₹12,500 |
| Grade-3 (most junior) | ₹10,000 |

### Hotel ratings by role

| Role | Allowed ratings |
|---|---|
| HR | `5-STAR`, `7-STAR` |
| Employee / TravelDeskExe | `3-STAR`, `5-STAR` |

### Employee ID validation on create

When an employee raises a request:
- `raisedByEmployeeId` must exist in account-management with `Employee` role
- `raisedByEmployeeId` must match the logged-in user (cannot raise on behalf of others)
- `toBeApprovedByHrId` must exist in account-management with `HR` role

---

## Date Reference

| Date | Milliseconds |
|---|---|
| 2026-07-01 | `1782864000000` |
| 2026-07-06 | `1783296000000` |
| 2026-07-10 | `1783641600000` |
| 2026-07-20 | `1784505600000` |
| 2026-07-30 | `1785369600000` |

---

## Error Response Format

```json
{
  "message": "Human-readable description",
  "fieldName": "Field that caused the error (nullable)",
  "status": "HTTP status name"
}
```

| HTTP Status | When |
|---|---|
| `400 BAD_REQUEST` | Validation failure, date/priority violation, role mismatch |
| `404 NOT_FOUND` | Travel request or employee not found |
| `403 FORBIDDEN` | Missing / expired / blacklisted token, insufficient role |

---

## Recommended Test Flow

```
1. Login as Employee  → POST http://localhost:8080/login  → copy employeeToken
2. Login as HR        → POST http://localhost:8080/login  → copy hrToken
3. Get locations      → GET /api/travelrequests/locations  → pick locationId
4. Create request     → POST /api/travelrequests/new  (use employeeToken) → copy requestId
5. Check pending      → GET /api/travelrequests/100000/pending  (use hrToken)
6. Approve            → PUT /api/travelrequests/{requestId}/update  (use hrToken)
7. Calculate budget   → POST /api/travelrequests/calculatebudget  (use hrToken) → get total
8. View detail        → GET /api/travelrequests/{requestId}  (any token) → see full detail
```

---

## Swagger UI

- **UI:** `http://localhost:8082/swagger-ui.html`
- **JSON spec:** `http://localhost:8082/v3/api-docs`

---

## Project Structure

```
src/main/java/com/etd/travel_planner/
├── client/           AccountManagementClient  (employee validation + budget grade lookup)
│                     AuthServiceClient  (blacklist check on every request)
├── config/           DataInitializer, JwtAuthFilter, SecurityConfig, FeignAuthInterceptor
├── constant/         AppConstant
├── controller/       TravelRequestController, TravelBudgetAllocationController, LocationController
├── dao/              TravelRequestRepo, TravelBudgetAllocationRepo, LocationRepo
├── dto/              TravelRequestDTO, TravelResponseDTO, TravelRequestDetailResponseDTO,
│                     UpdateTravelRequestDTO, TravelBudgetAllocationRequestDTO,
│                     LocationResponseDTO, ErrorDTO
├── entity/           TravelRequest, TravelBudgetAllocation, Location
├── exception/        BadRequestException, NotFoundException, InvalidDateRangeException,
│                     IllegalArgumentException, GlobalExceptionHandler
├── mapper/           TravelRequestMapper, TravelBudgetAllocationMapper, LocationMapper
├── service/
│   ├── interfaces/   TravelRequestService, TravelBudgetAllocationService, LocationService
│   └── classes/      TravelRequestServiceImpl, TravelBudgetAllocationServiceImpl, LocationServiceImpl
└── util/             JWTUtil (token validation + role extraction)
```

---

## Related Services

| Service | Port | Responsibility |
|---|---|---|
| auth-service | 8080 | Login, token refresh, logout, blacklist check |
| account-management | 8081 | Employee / grade CRUD (owns the shared MySQL `account_management` DB) |
| **travel-planner** *(this service)* | **8082** | Travel request lifecycle, budget calculation |
| reservation-management | — | Flight / hotel / cab reservation upload and tracking |
| reimbursement-management | — | Expense claim submission and processing |
