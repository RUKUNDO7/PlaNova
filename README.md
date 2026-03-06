# Task Management App (Spring Boot + React)

This project contains:
- `backend`: Spring Boot REST API with H2 in-memory database
- `frontend`: React app (Vite) consuming the backend API

## Backend

Requirements:
- Java 17+
- Maven 3.9+

Run:
```bash
cd backend
mvn spring-boot:run
```

API base URL:
- `http://localhost:8080/api/tasks`

H2 console:
- `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- User: `sa`
- Password: (empty)

## Frontend

Requirements:
- Node.js 18+

Run:
```bash
cd frontend
npm install
npm run dev
```

Vite dev server:
- `http://localhost:5173`

The frontend proxies `/api` requests to `http://localhost:8080`.

## Task API Endpoints

- `GET /api/tasks` - list tasks
- `GET /api/tasks/{id}` - get task by id
- `POST /api/tasks` - create task
- `PUT /api/tasks/{id}` - update task
- `PATCH /api/tasks/{id}/complete` - set completion state
- `DELETE /api/tasks/{id}` - delete task
