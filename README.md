# Family Finance

A full-stack family expense management application. This repository is structured as a monorepo containing a Spring Boot backend, a React frontend, and a PostgreSQL database, entirely orchestrated via Docker.

> **Status: Active Development**
> The core infrastructure, container orchestration, and secure authentication pipelines are fully implemented and covered by automated tests. Feature development for User and Family entities is currently ongoing.

## Architecture & Current State

*   **Monorepo Strategy:** Both backend (Java/Spring Boot) and frontend (React) are maintained in a single repository for synchronized CI/CD pipelines via GitHub Actions.
*   **Secure Authentication:** Implemented a robust dual-token authentication system using **JWT for stateless access tokens** and **UUIDs for refresh tokens**, fully validated by automated tests.
*   **Infrastructure as Code:** The entire environment (Backend, Frontend, and PostgreSQL) is containerized and orchestrated using Docker Compose, guaranteeing parity between development and production.
*   **Database Management:** Relational data is structured in PostgreSQL, with schema migrations managed via Flyway.

## Tech Stack

*   **Backend:** Java, Spring Boot, Spring Security
*   **Frontend:** React.js
*   **Database:** PostgreSQL
*   **DevOps/Infra:** Docker, Docker Compose, GitHub Actions (CI/CD)

## Development Roadmap

- [x] Docker orchestration (Frontend, Backend, DB)
- [x] JWT + UUID Refresh Token authentication module (Fully tested)
- [ ] **Current:** User entity management (CRUD operations and profile updates)
- [ ] **Next:** `Family` aggregation module (Grouping users and shared finances)
- [ ] Expense tracking and categorization

## Running the Infrastructure Locally

You do not need to install Java, Node.js, or PostgreSQL on your host machine. The entire stack is orchestrated via Docker.

### 1. Environment Configuration
Create a `.env` file in the root directory based on the `.env.example`:
```env
# DATABASE
DB_URL=db_url
DB_USER=postgres
DB_PASSWORD=password

# BACKEND
JWT_SECRET=a_very_safe_secret
JWT_EXPIRATION=60000
REFRESH_EXPIRATION=5
CORS_ALLOWED_ORIGINS="http://localhost:5173,http://192.168.1.6:5173,http://192.168.1.7:5173"

# FRONTEND
VITE_API_URL=http://localhost:8080/api
```

### 2. Build and Execute

Run the following command to spin up the Spring Boot API, React frontend, and PostgreSQL database simultaneously:

```bash
docker compose up --build
```

### 3. Access the Application

* **Frontend (React):** `http://localhost:3000`
* **Backend API:** `http://localhost:8080`
