# Student Management System (SMS) - Production REST Backend & Vanilla UI

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)]()

A production-grade Student Management System (SMS) built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA**, **MySQL 8**, **Spring Security**, and **Bean Validation**, featuring a responsive **Vanilla HTML5/CSS3/JavaScript (ES modules)** frontend served directly from Spring Boot.

---

## 🏛️ Architecture

Strictly layered enterprise architecture ensuring separation of concerns:

```
[ Web Browser / Client / cURL / Postman ]
                   │
                   ▼
       [ SecurityFilterChain ]  (HTTP Basic + Form / Session Auth)
                   │
                   ▼
           [ Controllers ]      (StudentController, AuthController)
                   │
                   ▼ (DTOs + StudentMapper)
       [ Service Layer (IFace) ] (StudentService)
                   │
                   ▼
       [ Service Implementation ] (StudentServiceImpl)
                   │
                   ▼
           [ Repositories ]     (StudentRepository - JPQL & Derived Queries)
                   │
                   ▼
            [ Entities ]        (Student Entity)
                   │
                   ▼
         [ MySQL 8 Database ]
```

---

## 🚀 Key Features

### Backend
- **Strictly Layered Domain:** Student entity with auto ID, unique `rollNumber`, `firstName`, `lastName`, unique `email`, 10-digit `phone`, `department`, `yearOfStudy` (1–4), `dateOfBirth` (past date), `createdAt`, and `updatedAt`.
- **DTOs & Mappers:** Clean separation via `StudentRequest`, `StudentResponse`, `PageResponse<T>`, and `StudentMapper`.
- **Validation & Exception Handling:** `@RestControllerAdvice` with uniform `ErrorResponse` (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`), handling `ResourceNotFoundException` (404), `DuplicateResourceException` (409), validation errors (400), type mismatch (400), and generic errors (500).
- **Spring Security:** Admin authentication via environment variables `ADMIN_USER` and `ADMIN_PASSWORD` (BCrypt). Public access for static pages (`/`, `/index.html`, `/student-form.html`, `/login.html`, `/faq.html`, `/css/**`, `/js/**`), `/actuator/health`, and `/api/v1/auth/**`, while protecting all `/api/**` endpoints.
- **Offline / Isolated Tests:** 25 comprehensive unit and controller tests passing without requiring a live database.

### Frontend
- **Zero-Dependency Modern UI:** Vanilla HTML5, modern CSS3 variables, and ES modules. No build steps or frameworks.
- **9 Core UI Features:**
  1. 🌓 **Dark Mode Toggle:** Smooth light/dark switching via CSS variables and `data-theme`, respecting `prefers-color-scheme` and saved in `localStorage`.
  2. 🍪 **Cookie Banner:** Fixed bottom banner with Accept / Decline choices saved in `localStorage` and linked to privacy notes.
  3. ⬆️ **Back to Top Button:** Smooth floating button activated after 300px scroll.
  4. 📱 **Mobile Navigation Menu:** Hamburger slide-out drawer on devices below 768px with full keyboard/Escape accessibility and `aria-expanded` synchronization.
  5. ⌨️ **Keyboard Shortcuts:** `/` to focus search, `n` to create a student, `d` to toggle theme, `?` for shortcuts help dialog, and `Esc` to close modals.
  6. 📊 **Scroll Progress Bar:** Thin fixed top indicator tracking page scroll percentage.
  7. 💀 **Skeleton Loader:** Shimmering animated placeholders while student data is retrieved.
  8. ❓ **FAQ Accordion:** 8 comprehensive accordion questions with accessible single-open toggle and keyboard controls.
  9. 👁️ **Password Visibility:** Accessible eye toggle on password fields with `aria-pressed` synchronization.
- **Table UX:** Real-time debounced search against `/search`, department & year filters, sortable columns, pagination controls, inline validation errors mapped from API `fieldErrors`, toast notifications, and empty state.

---

## 📋 REST API Endpoints

Base URL: `http://localhost:8081`

| HTTP Method | Endpoint | Description | Security | Status Codes |
|:---|:---|:---|:---|:---|
| `POST` | `/api/v1/students` | Register a new student | Admin | `201 Created`, `400`, `409` |
| `GET` | `/api/v1/students/{id}` | Retrieve student by ID | Admin | `200 OK`, `404 Not Found` |
| `GET` | `/api/v1/students` | List students (paginated & sortable) | Admin | `200 OK` |
| `PUT` | `/api/v1/students/{id}` | Full update of student | Admin | `200 OK`, `400`, `404`, `409` |
| `DELETE` | `/api/v1/students/{id}` | Delete a student | Admin | `204 No Content`, `404` |
| `GET` | `/api/v1/students/search` | Dynamic search (name, department, email, year) | Admin | `200 OK` |
| `POST` | `/api/v1/auth/login` | Authenticate admin user | Public | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/v1/auth/status` | Current authentication status | Public | `200 OK` |
| `POST` | `/api/v1/auth/logout` | Invalidate session | Public | `200 OK` |
| `GET` | `/actuator/health` | Application health check | Public | `200 OK` |

---

## 💻 How to Run Locally

### Prerequisites
- **Java 21 JDK** installed (`java -version`)
- **MySQL 8** (or run via Docker container)

### Step 1: Configure Environment Variables
You can customize configuration via environment variables (or rely on defaults):
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=studentdb
export DB_USER=root
export DB_PASSWORD=rootpassword
export ADMIN_USER=admin
export ADMIN_PASSWORD=admin123
```

### Step 2: Run Tests & Build
```bash
# On Linux / macOS
./mvnw clean verify

# On Windows PowerShell
.\mvnw.cmd clean verify
```

### Step 3: Run Spring Boot Application
```bash
# On Linux / macOS
./mvnw spring-boot:run

# On Windows PowerShell
.\mvnw.cmd spring-boot:run
```

Once running, access the web portal at:
👉 **[http://localhost:8081](http://localhost:8081)**

---

## 🐳 How to Run with Docker Compose

Run the complete multi-container setup (MySQL 8 + Spring Boot App) with a single command:

```bash
docker compose up --build
```

### Services Started:
- **MySQL 8:** Host port `3307:3306` (isolated volume `mysql_data`, automated healthcheck)
- **App Service:** Host port `8081:8081` (Eclipse Temurin 21 JRE, runs as unprivileged user `appuser`)

To stop the containers:
```bash
docker compose down
```

---

## 🧪 Sample cURL Requests

### 1. Actuator Health Check
```bash
curl -i http://localhost:8081/actuator/health
```

### 2. Create Student (201 Created)
```bash
curl -i -X POST http://localhost:8081/api/v1/students \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "rollNumber": "CS2026001",
    "firstName": "Arjun",
    "lastName": "Kumar",
    "email": "arjun.kumar@university.edu",
    "phone": "9876543210",
    "department": "Computer Science",
    "yearOfStudy": 3,
    "dateOfBirth": "2003-05-15"
  }'
```

### 3. Retrieve Student by ID (200 OK)
```bash
curl -i http://localhost:8081/api/v1/students/1 \
  -u admin:admin123
```

### 4. Paginated & Sorted List
```bash
curl -i "http://localhost:8081/api/v1/students?page=0&size=10&sort=rollNumber,asc" \
  -u admin:admin123
```

### 5. Multi-criteria Search
```bash
curl -i "http://localhost:8081/api/v1/students/search?name=Arjun&department=Computer%20Science&year=3" \
  -u admin:admin123
```

### 6. Full Update (200 OK)
```bash
curl -i -X PUT http://localhost:8081/api/v1/students/1 \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "rollNumber": "CS2026001",
    "firstName": "Arjun",
    "lastName": "Verma",
    "email": "arjun.verma@university.edu",
    "phone": "9876543210",
    "department": "Computer Science",
    "yearOfStudy": 4,
    "dateOfBirth": "2003-05-15"
  }'
```

### 7. Delete Student (204 No Content)
```bash
curl -i -X DELETE http://localhost:8081/api/v1/students/1 \
  -u admin:admin123
```

---

## 📸 Screenshots & Responsive Verification Instructions

To capture verification screenshots:
1. **Desktop View (1920x1080 or 1280x800):**
   - Open [http://localhost:8081](http://localhost:8081) in Chrome / Firefox.
   - Test in **Light Mode** and toggle to **Dark Mode** via the `🌙` button or pressing `d`.
   - Open the **Add Student Modal** (or press `n`) and submit with empty fields to observe field validation highlights.
2. **Mobile View (375px width - iPhone SE / Modern Mobile):**
   - Open Developer Tools (`F12`), toggle Device Toolbar (`Ctrl+Shift+M`), select **375px** width.
   - Verify table horizontal scroll, card stacking, and hamburger mobile menu toggle with slide-in animation.
3. **FAQ & Help Page:**
   - Navigate to `/faq.html` and verify the single-open accordion behavior.
4. **Login Page:**
   - Navigate to `/login.html` and test the password visibility toggle (`👁️`).
