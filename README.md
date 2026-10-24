# University Management System

A Spring Boot REST API for managing a university's **Admins**, **Teachers**, and **Students**, with login, logout, BCrypt password hashing, and role-based access control. The whole system (application and database) runs with Docker Compose.

**Course:** CSE 3230, Lab 4 home task
**Author:** Feroz (Roll 2207087), Dept. of CSE, KUET

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 (Web, Data JPA, Security) |
| Database | PostgreSQL 17 |
| Build tool | Gradle (Groovy) |
| Containers | Docker, Docker Compose |

---

## Features

- CRUD REST API for Admin, Teacher, and Student resources (JSON, proper HTTP status codes)
- Login and logout with Spring Security (session cookie)
- Passwords stored hashed with BCrypt, never returned in responses
- Role-based access control (RBAC)
- Input checks and duplicate detection (username, email, roll number)
- Dockerfile (multi-stage build) and `compose.yaml` running the app and PostgreSQL together

---

## Run with Docker (recommended)

**Requirements:** Docker Desktop (running).

No local PostgreSQL setup is required. Docker Compose creates its own PostgreSQL
container, database, user, and persistent volume automatically.

```bash
docker compose up --build
```

Wait for `Started DemoApplication` in the logs. The API is then available at **http://localhost:8081**.

| Command | What it does |
|---|---|
| `docker compose up --build` | Build and start everything |
| `docker compose up -d` | Start in the background |
| `docker compose logs -f app` | Follow the application logs |
| `docker compose down` | Stop and remove the containers (data is kept) |
| `docker compose down -v` | Also delete the database volume (fresh start) |

The Docker database is also reachable from the host on port **5433** using:

| Setting | Value |
|---|---|
| Host | `localhost` |
| Port | `5433` |
| Database | `mydb` |
| Username | `myuser` |
| Password | `strong_password` |

These credentials belong only to the Docker database and are configured in
`compose.yaml`. They are separate from any PostgreSQL installation on the host.

## Run locally (without Docker)

**Requirements:** JDK 25 and PostgreSQL running locally on port `5432`.

This project currently uses the local credentials configured in
`src/main/resources/application.properties`: username `shah` and password
`shah`.

1. Create the database and user:
   ```sql
   CREATE DATABASE mydb;
   CREATE USER shah WITH PASSWORD 'shah';
   GRANT ALL PRIVILEGES ON DATABASE mydb TO shah;
   \c mydb
   GRANT ALL PRIVILEGES ON SCHEMA public TO shah;
   ```
2. If the database or user already exists, skip its `CREATE` command. Check that
   the datasource settings in `src/main/resources/application.properties` match
   the credentials of your local PostgreSQL installation.
3. Start the app:
   ```bash
   ./gradlew bootRun
   ```
   (On Windows: `gradlew.bat bootRun`, or run `DemoApplication` from IntelliJ.)

---

## Default admin

On first start, if no admin exists, one is created automatically:

| Username | Password |
|---|---|
| `admin` | `admin123` |

Log in as this admin to create teachers and students. Change the password after the first run.

---

## Authentication

| Action | Request |
|---|---|
| Login | `POST /api/auth/login` with form fields `username` and `password` |
| Logout | `POST /api/auth/logout` |

A successful login returns `200` and a session cookie, which the client sends with later requests. A wrong password returns `401`.

---

## Access rules (RBAC)

| Endpoint | ADMIN | TEACHER | STUDENT |
|---|:---:|:---:|:---:|
| `/api/admins/**` | yes | no | no |
| `/api/teachers/**` | yes | yes | no |
| `/api/students/**` | yes | no | yes |

- Admins can manage all three resources.
- Teachers can manage only teachers.
- Students can manage only students.
- Not logged in: **401 Unauthorized**.
- Logged in but not permitted: **403 Forbidden**.

---

## REST API

Each resource (`admins`, `teachers`, `students`) supports the same operations:

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/{resource}` | List all | 200 |
| GET | `/api/{resource}/{id}` | Get one | 200 |
| POST | `/api/{resource}` | Create | 201 (with `Location` header) |
| PUT | `/api/{resource}/{id}` | Update | 200 |
| DELETE | `/api/{resource}/{id}` | Delete | 204 |

**Error responses**

| Status | Meaning |
|---|---|
| 400 | Required fields missing |
| 401 | Not logged in |
| 403 | Role not allowed |
| 404 | Resource not found |
| 409 | Duplicate username, email, or roll number |

### Request bodies

**Admin**
```json
{
  "username": "admin2",
  "password": "admin123",
  "fullName": "Second Admin",
  "email": "admin2@kuet.ac.bd",
  "office": "Registrar Office"
}
```

**Teacher**
```json
{
  "username": "teacher1",
  "password": "teach123",
  "fullName": "Dr. Rahman",
  "email": "teacher1@kuet.ac.bd",
  "department": "CSE",
  "designation": "Professor"
}
```

**Student**
```json
{
  "username": "student1",
  "password": "stud123",
  "fullName": "Feroz",
  "email": "student1@kuet.ac.bd",
  "rollNumber": "2207087",
  "department": "CSE"
}
```

On update (`PUT`), `password` is optional. If it is left out, the existing password is kept.
Responses contain the `id` and `role` and never include the password.

---

## Testing

Sample requests are in [`test.http`](test.http) and can be run with IntelliJ's built-in HTTP client. The file covers login and logout, CRUD for all three resources, and the 401 / 403 / 200 checks for each role.

Quick manual check (after `docker compose up`):

1. Run the "no login" request: `GET /api/teachers` returns **401**.
2. Log in as `admin` / `admin123`, then create a teacher and a student.
3. Log out, log in as the teacher: `/api/teachers` returns **200**, `/api/students` and `/api/admins` return **403**.
4. Log out, log in as the student: `/api/students` returns **200**, the others return **403**.

Always log out before logging in as a different user, because the session cookie is reused.

---

## Project structure

```
src/main/java/com/example/demo
├── config        SecurityConfig, DataInitializer (seeds the first admin)
├── controller    AdminController, TeacherController, StudentController
├── dto           request and response records
├── entity        User (base), Admin, Teacher, Student, Role
├── repository    Spring Data JPA repositories
└── service       business logic, CustomUserDetailsService
Dockerfile        multi-stage build (Gradle build, then JRE runtime)
compose.yaml      app and PostgreSQL services
test.http         sample requests
```

**Data model:** `Admin`, `Teacher`, and `Student` extend an abstract `User` entity (joined-table inheritance). The shared `users` table holds the login fields (username, hashed password, name, email, role), and each role has its own table for its extra fields. Spring Security loads every login from the single `users` table.

---

## Notes

- CSRF protection is disabled because the API is used by REST clients, not browser forms. A browser frontend would need CSRF protection or token-based authentication.
- The database credentials in `compose.yaml` are for the lab only. For real deployments they should come from environment variables or secrets.
- The app listens on port **8081** because port 8080 may already be used on the development machine.

## Possible future work

- Marks and results management for teachers, with students seeing only their own results
- Ownership checks, so users can edit only their own records where appropriate
- Frontend (login page and role-based dashboards)
- Database migrations with Liquibase
