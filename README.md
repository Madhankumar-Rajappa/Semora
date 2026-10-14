# CampusTrack — Vercel-Deployable Personal College Life Tracker

CampusTrack is a full-stack academic life tracking and portfolio web application built for students and Web Technology laboratories. It empowers a student to manage their entire collegiate journey from Semester 1 through graduation — recording subjects, internal assessments, lab evaluations, assignments, deadlines, verified certificates, and software development projects with automated SGPA & CGPA calculations and Chart.js analytics.

---

## 1. System Architecture

CampusTrack enforces a clean separation of concerns between client and server:

```
┌─────────────────────────────────┐           ┌─────────────────────────────────────┐
│       Frontend (Vercel)         │           │        Backend (Tomcat / Java)      │
│  - HTML5 / CSS3 / JavaScript    │  HTTPS    │  - Jakarta Servlets 5.0 / REST API │
│  - Bootstrap 5 + Glassmorphism  │ ────────> │  - HikariCP Connection Pooling      │
│  - Chart.js Visual Analytics    │   JSON    │  - HMAC-SHA256 JWT Authentication   │
│  - Configurable API Base URL    │           │  - Optional JSP Demo Views (/jsp/*) │
└─────────────────────────────────┘           └──────────────────┬──────────────────┘
                                                                 │ JDBC
                                                                 ▼
                                              ┌─────────────────────────────────────┐
                                              │       Database (MySQL 8.0)          │
                                              │  - campustrack Database             │
                                              │  - Relational Schema with FKs       │
                                              └─────────────────────────────────────┘
```

- **Frontend Hosting:** Vercel (static edge deployment with client-side API configuration).
- **Backend Runtime:** Apache Tomcat 10+ (Java 17 / Jakarta EE) exposing RESTful JSON endpoints.
- **Database:** Managed MySQL accessible via JDBC with HikariCP connection pooling.
- **Security:** Salted password hashing, JWT Bearer token authentication, CORS filtering, and student record isolation.
- **Lab Compatibility:** Includes traditional JSP views in `src/main/webapp/jsp/` for laboratory evaluator grading.

---

## 2. UpAhead-Inspired Redesign & 4-Year Collegiate Journey Tracking

Taking inspiration from modern academic life trackers (e.g., UpAhead), CampusTrack has been redesigned to deliver visual excellence and comprehensive 4-year progression tracking:

- **Sidebar Academic Portal:** Dedicated navigation sidebar with brand banner, active degree audit widget (`115/160 Credits`, `72%`), and student profile card.
- **Top Header Controls:** Real-time theme toggle (Light / Dark Mode), role switcher (`Student` vs `Lab Evaluator / Faculty Inspection`), and live backend connectivity status.
- **4-Year Collegiate Journey Banner:** Modular cards tracking progression across all 4 undergraduate years:
  - **1st Year (Freshman):** Semesters 1 & 2 &bull; 46 Credits earned &bull; Completed.
  - **2nd Year (Sophomore):** Semesters 3 & 4 &bull; 46 Credits earned &bull; Completed.
  - **3rd Year (Junior):** Semesters 5 & 6 &bull; 36 Credits &bull; Active In-Progress.
  - **4th Year (Senior):** Semesters 7 & 8 &bull; 32 Credits &bull; Graduation Audit.
- **Weekly / Semester Progress Meter:** Dynamic SVG circular gauge tracking weekly deliverable completion (`0/3` or `1/5`) with instant tab toggling between weekly and semester milestones.
- **Course Progress Card:** Subject-by-subject progress bars (`PHYS 211`, `TE 200`, `MATH 241`, etc.) displaying weekly due counts, completion counts (`18/21`), and subject color tags.
- **Upcoming Exams Card:** High-stakes exam highlights categorizing `Final`, `Exam 1`, and `Midterm` schedules with subject folders and dates.
- **Action Bar:** 4 quick-action triggers: `Upload Syllabus`, `Import Assignments`, `Sync to Calendar`, and `+ Add Assignment`.
- **Assignment Overview Table:** Filterable deliverable tracking with status badges (`Overdue`, `Tomorrow`, `This Week`, `Future`, `Completed`), course dots, live search, show-completed toggle, exams-only filter, and instant task completion checkboxes.
- **Analytical Intelligence:** 10-point scale multi-semester SGPA progression line charts and task status doughnut visualizations powered by Chart.js.

---

## 3. Directory Structure

```
Web_Project/
├── backend/
│   ├── Dockerfile                           # Tomcat 10 container deployment
│   ├── pom.xml                              # Maven build configuration
│   └── src/
│       └── main/
│           ├── java/com/campustrack/
│           │   ├── dao/                     # JDBC Database Access Objects
│           │   ├── filter/                  # CorsFilter, AuthFilter
│           │   ├── model/                   # Data transfer and entity objects
│           │   ├── service/                 # Business logic & SGPA calculations
│           │   ├── servlet/                 # REST API Servlets (/api/*)
│           │   └── util/                    # DBConnection, JwtUtil, PasswordUtil, JsonUtil
│           └── webapp/
│               ├── WEB-INF/web.xml          # Servlet mapping and filters
│               └── jsp/                     # Web Tech Lab demo views
├── database/
│   ├── schema.sql                           # Production MySQL tables & constraints
│   └── seed.sql                             # Comprehensive demo student dataset
├── frontend/
│   ├── css/
│   │   └── styles.css                       # Glassmorphic CSS design system
│   ├── js/
│   │   ├── config.js                        # Dynamic API Base URL resolution
│   │   ├── api.js                           # Centralized HTTP client & toasts
│   │   ├── auth.js                          # Auth state & session guard
│   │   ├── dashboard.js                     # 4 Chart.js charts & KPI logic
│   │   ├── semesters.js                     # Semester, subject & marks logic
│   │   ├── assignments.js                   # Deadlines & priority queue
│   │   ├── records.js                       # Certificate & document repository
│   │   ├── activities.js                    # Projects & extracurriculars
│   │   └── profile.js                       # Profile & API switcher logic
│   ├── index.html                           # Landing page & authentication modals
│   ├── dashboard.html                       # Analytics & overview dashboard
│   ├── semesters.html                       # Semester & assessment manager
│   ├── assignments.html                     # Assignments & deadline tracker
│   ├── records.html                         # Document & certificate repository
│   ├── activities.html                      # Projects, hackathons & activities
│   ├── profile.html                         # Student settings & API config
│   └── vercel.json                          # Vercel deployment configuration
├── docker-compose.yml                       # Multi-container setup (Tomcat + MySQL + phpMyAdmin)
├── .env.example                             # Environment variable template
└── README.md                                # Comprehensive documentation
```

---

## 4. Database Setup (MySQL)

1. Open your MySQL client (MySQL Workbench, phpMyAdmin, or CLI).
2. Execute the schema:
   ```sql
   SOURCE database/schema.sql;
   ```
3. Load the sample student dataset (Alex Morgan, 5 Semesters, subjects, marks, credentials, and projects):
   ```sql
   SOURCE database/seed.sql;
   ```

### Default Demo Student Credentials:
- **Email:** `alex.morgan@university.edu`
- **Password:** `Password123!`

---

## 5. Local Development

### Option A: Using Maven & Apache Tomcat (Standard Web Tech Lab)
1. Navigate to the `backend` folder:
   ```bash
   cd backend
   ```
2. Build the WAR package:
   ```bash
   mvn clean package
   ```
   *The generated WAR file is at `backend/target/campustrack.war`.*
3. Copy `campustrack.war` to your Tomcat `webapps/` directory and start Tomcat (`catalina run` or `startup.bat`).
4. Access the health check endpoint:
   ```
   http://localhost:8080/campustrack/api/health
   ```
5. Serve the `frontend` folder using any static server:
   ```bash
   npx serve frontend -p 3000
   ```
   Open `http://localhost:3000` in your browser.

### Option B: Using Docker Compose
Run both the database, Tomcat backend, and phpMyAdmin in a single command:
```bash
docker-compose up --build
```
- **Backend API:** `http://localhost:8080/api/health`
- **phpMyAdmin GUI:** `http://localhost:8081` (User: `root`, Password: `rootpassword`)

---

## 6. Production Deployment

### Frontend Deployment on Vercel
1. Push your repository to GitHub.
2. Log in to [Vercel](https://vercel.com/) and click **Add New Project**.
3. Import your GitHub repository.
4. Set the **Root Directory** to `frontend`.
5. Under **Environment Variables**, add:
   - `VITE_API_BASE_URL`: `https://your-deployed-backend-domain.com/api`
6. Click **Deploy**. Vercel will host your static frontend at `https://campustrack.vercel.app`.

### Backend Deployment on Java Hosting (Railway / Render / Fly.io / AWS)
1. Use the provided `backend/Dockerfile`.
2. Configure the following environment variables on your cloud hosting dashboard:
   - `DB_HOST`: Your cloud MySQL host (e.g., Aiven, Railway, Supabase, PlanetScale)
   - `DB_PORT`: `3306`
   - `DB_NAME`: `campustrack`
   - `DB_USER`: `your_mysql_user`
   - `DB_PASSWORD`: `your_mysql_password`
   - `JWT_SECRET`: `YourSuperSecretKey2026!Min32Characters`
   - `PORT`: `8080`
3. Once deployed, verify your backend with:
   ```
   GET https://your-backend.com/api/health
   ```

---

## 6. Configurable API Base URL Feature

CampusTrack avoids hardcoded localhost endpoints. The frontend dynamically resolves the backend URL through:
1. **User Override:** Configurable in the UI at `profile.html` or via the top navbar indicator pill (`localStorage` cached).
2. **Meta Tag / Build Environment:** `window.__ENV__.API_BASE_URL` or `<meta name="api-base-url">`.
3. **Automatic Fallback:** Uses `http://localhost:8080/api` on local development and cloud endpoints on production Vercel.

---

## 7. REST API Documentation

All protected endpoints require an `Authorization: Bearer <token>` header obtained from `/api/auth/login`.

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/health` | Service & DB connectivity health check | No |
| `POST` | `/api/auth/register` | Register new student profile | No |
| `POST` | `/api/auth/login` | Authenticate and return JWT token | No |
| `GET` | `/api/auth/me` | Fetch active student profile | Yes |
| `PUT` | `/api/auth/profile` | Update student profile information | Yes |
| `PUT` | `/api/auth/password` | Change account password | Yes |
| `GET` | `/api/semesters` | List all semesters for student | Yes |
| `POST` | `/api/semesters` | Create new semester (1 to 8) | Yes |
| `GET` | `/api/semesters/{id}` | Get semester details with subjects | Yes |
| `PUT` | `/api/semesters/{id}` | Update semester status/academic year | Yes |
| `DELETE` | `/api/semesters/{id}` | Delete semester and related records | Yes |
| `GET` | `/api/semesters/{id}/subjects` | Get subjects enrolled in semester | Yes |
| `POST` | `/api/semesters/{id}/subjects` | Add new subject to semester | Yes |
| `GET` | `/api/subjects/{id}/assessments` | Get test marks for subject | Yes |
| `POST` | `/api/subjects/{id}/assessments` | Record test/internal/quiz marks | Yes |
| `GET` | `/api/assignments` | List assignments (supports filters) | Yes |
| `POST` | `/api/assignments` | Create assignment with due date & priority | Yes |
| `PUT` | `/api/assignments/{id}` | Update assignment status / deliverables | Yes |
| `DELETE` | `/api/assignments/{id}` | Remove assignment | Yes |
| `GET` | `/api/records` | List documents & certificates | Yes |
| `POST` | `/api/records` | Save credential metadata & file URL | Yes |
| `DELETE` | `/api/records/{id}` | Delete document record | Yes |
| `GET` | `/api/activities` | List projects, hackathons & internships | Yes |
| `POST` | `/api/activities` | Add project with GitHub/demo links | Yes |
| `DELETE` | `/api/activities/{id}` | Delete project or activity | Yes |
| `GET` | `/api/analytics/dashboard` | Dashboard KPIs & Chart.js data | Yes |

---

## 8. Web Technology Laboratory Demonstration (JSP Views)

For lab evaluations that specifically require server-rendered JSP views:
- Navigate to `http://localhost:8080/campustrack/jsp/index.jsp` (or `http://localhost:8080/jsp/index.jsp` in Docker).
- Sign in with the student credentials to demonstrate classic JSP session tracking and direct JDBC table rendering.
- These JSP views run strictly on the Tomcat server, leaving the primary modern frontend on Vercel decoupled and cloud-native.
