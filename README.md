# Task Management Dashboard

This repository provides a production-grade task management dashboard with:
- `backend`: REST API and persistence layer
- `frontend`: browser-based dashboard for task operations

## Local Development

### Backend
Requirements:
- Java 17+
- Maven 3.9+

Run:
```bash
cd backend
mvn spring-boot:run
```

Default API base:
- `http://localhost:8080/api/tasks`

Database console:
- `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- User: `sa`
- Password: (empty)

### Frontend
Requirements:
- Node.js 18+

Run:
```bash
cd frontend
npm install
npm run dev
```

Default dashboard URL:
- `http://localhost:5173`

The frontend proxies `/api` requests to `http://localhost:8080`.

## Authentication

- `POST /api/auth/signup` register with `email`, `password`, and `displayName`; the signed-in role is returned in the payload.
- `POST /api/auth/login` signs the user in and returns their `id`, `displayName`, `email`, and `role`.
- Admin privileges are granted only when the email matches `gihozoRukundobenise@gmail.com`; every other account becomes a `USER`.
- Seeded credentials: admin (`gihozoRukundobenise@gmail.com` / `AdminPass#1`), operations (`ops@task.local` / `OpsPass#1`), marketing (`marketing@task.local` / `MarketPass#1`).

## Roles & dashboards

- Admins can inspect every task, assign ownership, and navigate to each user's personal dashboard.
- User dashboards trigger `viewerRole=USER&viewerId={userId}`, returning only that user's tasks so they can manage their own workload.
- Task creation/updating requires an `ownerId` (or defaults to the authenticated viewer when in a user dashboard).

## API Endpoints

- `GET /api/users` list the available users (id, email, displayName, role)
- `POST /api/auth/signup` register a new user (admin only if email matches the configured admin address)
- `POST /api/auth/login` authenticate and receive the signed-in user payload
- `GET /api/tasks` list tasks (`viewerRole` defaults to `ADMIN`; include `viewerId` when using `USER`)
- `GET /api/tasks/{id}` get task by id
- `POST /api/tasks` create task (submit `ownerId`, `title`, `priority`, etc.)
- `PUT /api/tasks/{id}` update task (include `ownerId` to reassign)
- `PATCH /api/tasks/{id}/complete` set completion state
- `DELETE /api/tasks/{id}` delete task
- `DELETE /api/tasks/completed` clear completed tasks (respects the viewer context)
