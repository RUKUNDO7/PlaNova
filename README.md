# PlaNova

A full-stack task management workspace for organizing projects with boards, columns, tasks, team collaboration, and real-time notifications.

## Tech Stack

| Layer    | Technology                |
|----------|---------------------------|
| Backend  | Spring Boot 3 · Java 17   |
| Database | PostgreSQL                |
| Auth     | Spring Security · JWT     |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL

### Backend

```bash
cd backend
mvn spring-boot:run
```

API available at `http://localhost:8080/api`


## Features

- **Projects & Workspaces** — create projects and manage team members
- **Boards & Columns** — Kanban-style boards with customizable columns
- **Tasks** — create, assign, and track tasks across boards
- **Collaboration** — comments, file attachments, and activity logs on tasks
- **Notifications** — stay updated on task changes and team activity
- **Authentication** — secure signup/login with role-based access control

## License

This project is for educational and personal use.