# PlaNova Task Management Suite

PlaNova is a full-stack task management workspace with projects, boards, columns, tasks, collaboration, and notifications.

## Stack

- `backend`: Spring Boot 3 / Java 17
- `frontend`: React + Vite

## Local Development

### Backend

Requirements:
- Java 17+
- Maven 3.9+
- PostgreSQL (or override `DB_URL` to use another database)

Run:
```
cd backend
mvn spring-boot:run
```

API base: `http://localhost:8080/api`

### Frontend

Requirements:
- Node.js 18+

Run:
```
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

The Vite dev server proxies `/api` requests to `http://localhost:8080`.

## Authentication

- `POST /api/auth/signup` register with `email`, `password`, `displayName`.
- `POST /api/auth/login` returns `id`, `displayName`, `email`, `role`.

Seeded credentials:
- admin: `gihozoRukundobenise@gmail.com` / `AdminPass#1`
- operations: `ops@task.local` / `OpsPass#1`
- marketing: `marketing@task.local` / `MarketPass#1`

## Core Endpoints

- `GET /api/users` list users
- `PUT /api/users/{id}` update profile display name

Projects & workspaces:
- `GET /api/projects`
- `POST /api/projects`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`
- `GET /api/projects/{id}/members`
- `POST /api/projects/{id}/members`
- `DELETE /api/projects/{id}/members/{memberId}`

Boards & columns:
- `GET /api/boards?projectId=`
- `POST /api/boards`
- `PUT /api/boards/{id}`
- `DELETE /api/boards/{id}`
- `GET /api/columns?boardId=`
- `POST /api/columns`
- `PUT /api/columns/{id}`
- `DELETE /api/columns/{id}`

Tasks:
- `GET /api/tasks?boardId=&projectId=`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/complete`
- `DELETE /api/tasks/{id}`

Collaboration:
- `GET /api/tasks/{taskId}/comments`
- `POST /api/tasks/{taskId}/comments`
- `GET /api/tasks/{taskId}/attachments`
- `POST /api/tasks/{taskId}/attachments`
- `DELETE /api/tasks/{taskId}/attachments/{attachmentId}`
- `GET /api/tasks/{taskId}/activity`

Notifications:
- `GET /api/notifications?recipientId=`
- `PATCH /api/notifications/{id}/read`
- `PATCH /api/notifications/read-all?recipientId=`