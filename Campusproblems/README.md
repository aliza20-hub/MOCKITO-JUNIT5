# Campus Resource & Complaint Management System

A backend system built for NIT Srinagar that lets students report campus facility
issues (electrical faults, plumbing, Wi-Fi down, hostel maintenance, mess/food
complaints, cleanliness, etc.) and lets staff/admins track them from submission
to resolution.

I built this mainly to get hands-on with Spring Security + JWT and a proper
layered Spring Boot architecture (controller -> service -> repository), instead
of just doing another CRUD-only project. The complaint lifecycle and role-based
access were the two things I spent the most time getting right.

## Why this project

Every semester there'd be some WhatsApp group flooded with "washroom tap is
broken", "Wi-Fi down in Block C", "mess food quality complaint" type messages
that just get lost. This is basically a proper ticketing system for that,
scoped to a campus.

## Tech Stack

- Java 17
- Spring Boot 3.2.5 (Web, Data JPA, Security, Validation)
- PostgreSQL
- JWT (jjwt) for stateless auth
- Lombok
- Maven

## Roles & what each can do

| Role    | Can do |
|---------|--------|
| STUDENT | Register/login, file a complaint, view & track only their own complaints |
| STAFF   | View complaints assigned to them or routed to their department, update status (In Progress / Resolved / Rejected) |
| ADMIN   | Everything above + assign complaints to staff, manage departments, manage users |

## Complaint lifecycle

```
PENDING --(admin assigns)--> ASSIGNED --(staff picks it up)--> IN_PROGRESS --> RESOLVED
                                                                       \
                                                                        --> REJECTED
```

Every status change is written to `complaint_updates` along with who made the
change and any remarks, so a student can see the full timeline of their
complaint instead of just the current status.

## Database design (high level)

- **users** - students, staff, admins. Staff are tied to a `department`.
- **departments** - e.g. Electrical Maintenance, Plumbing, IT Services, Hostel
  Administration, Mess Committee, Housekeeping.
- **complaints** - the actual ticket: title, description, category, location,
  priority, status, who submitted it, who it's assigned to, which department.
- **complaint_updates** - append-only history of every status change on a
  complaint (previous status -> new status, remarks, who did it, when).

`spring.jpa.hibernate.ddl-auto=update` is used so Hibernate creates/updates the
tables automatically on startup — no manual DDL needed for local dev.

## Project structure

```
src/main/java/com/nitsri/complaintdesk/
 ├── config/         SecurityConfig
 ├── security/        JwtUtil, JwtAuthFilter, CustomUserDetailsService, AuthenticatedUserProvider
 ├── entity/           User, Department, Complaint, ComplaintUpdate + enums
 ├── repository/     Spring Data JPA repositories
 ├── dto/
 │    ├── request/    Request payloads (with validation annotations)
 │    └── response/   Response payloads (mapped from entities, avoids exposing passwords etc.)
 ├── service/          Business logic - AuthService, ComplaintService, DepartmentService, UserService
 ├── controller/     REST endpoints
 └── exception/       Custom exceptions + a single @RestControllerAdvice
```

## Getting it running locally

### 1. Create the database

```sql
CREATE DATABASE campus_complaint_db;
```

### 2. Set your DB credentials

Edit `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=postgres
spring.datasource.password=your_password_here
```

### 3. Run it

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. Tables are created automatically
on first run. You can optionally run `src/main/resources/seed-departments.sql`
manually against the DB to get some starter departments for testing.

## API overview

All endpoints except `/api/auth/**` require a `Authorization: Bearer <token>`
header, obtained from login/register.

### Auth

```bash
# Register a student
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Aamir Rashid",
    "email": "aamir.cse21@nitsri.net",
    "password": "test1234",
    "role": "STUDENT",
    "registrationNumber": "21UCS045"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "aamir.cse21@nitsri.net", "password": "test1234"}'
```

Response includes a `token` — pass it as `Authorization: Bearer <token>` on
every subsequent request.

### Departments

```bash
# Anyone logged in can view departments (e.g. while filing a complaint)
GET /api/departments

# Admin only
POST /api/departments
{ "name": "Electrical Maintenance", "code": "ELEC", "description": "..." }
```

### Complaints

```bash
# Student files a complaint
POST /api/complaints
{
  "title": "Wi-Fi not working in Hostel Block C",
  "description": "No internet connectivity since yesterday evening, whole floor affected.",
  "category": "INTERNET_WIFI",
  "location": "Hostel Block C, 2nd floor",
  "priority": "HIGH",
  "departmentId": 3
}

# View complaints - scoped automatically by role
#   student -> own complaints, staff -> dept/assigned complaints, admin -> all
GET /api/complaints

# Track one complaint (full status history)
GET /api/complaints/{id}

# Admin assigns a complaint to a staff member
PUT /api/complaints/{id}/assign
{ "staffId": 7, "remarks": "Please check by EOD" }

# Staff/Admin updates status
PUT /api/complaints/{id}/status
{ "status": "IN_PROGRESS", "remarks": "Technician dispatched" }

# Filter by status (staff/admin)
GET /api/complaints/status/PENDING
```

### Users (admin only)

```bash
GET /api/users
GET /api/users/staff?departmentId=3   # staff list for the "assign" dropdown
PATCH /api/users/{id}/status?enabled=false
```

## Things I'd add if I keep working on this

- Email/SMS notification when a complaint status changes
- File/image upload for complaint proof (e.g. photo of a broken tap)
- A basic React/Angular frontend on top of these APIs (currently API-only)
- Pagination on the `GET /api/complaints` and `/api/users` list endpoints
- Rate limiting on `/api/auth/login`
- Swagger/OpenAPI docs

## Known limitations

- No refresh token flow yet — once the JWT expires (24h by default) you just
  log in again.
- `ddl-auto=update` is fine for a college project/demo but you'd want proper
  Flyway/Liquibase migrations for anything real.
