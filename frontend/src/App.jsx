import { useCallback, useEffect, useMemo, useState } from "react";
import { taskApi } from "./api";

const initialForm = {
  title: "",
  description: "",
  dueDate: "",
  priority: "MEDIUM",
  ownerId: ""
};

const initialAuthForm = {
  email: "",
  password: "",
  displayName: ""
};

export default function App() {
  const [users, setUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [authMode, setAuthMode] = useState("login");
  const [authForm, setAuthForm] = useState(initialAuthForm);
  const [authBusy, setAuthBusy] = useState(false);
  const [authError, setAuthError] = useState("");
  const [dashboardRole, setDashboardRole] = useState("ADMIN");
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [priorityFilter, setPriorityFilter] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [direction, setDirection] = useState("desc");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const viewerContext = useMemo(() => {
    if (!currentUser) {
      return {};
    }

    if (dashboardRole === "USER") {
      const targetId = selectedUserId ?? (currentUser.role === "USER" ? currentUser.id : null);
      if (!targetId) {
        return {};
      }
      return {
        viewerRole: "USER",
        viewerId: targetId
      };
    }

    return { viewerRole: "ADMIN" };
  }, [currentUser, dashboardRole, selectedUserId]);

  const loadUsers = useCallback(async () => {
    try {
      const response = await fetch("/api/users");
      if (!response.ok) {
        throw new Error("Unable to fetch users");
      }
      const data = await response.json();
      setUsers(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => {
    if (!currentUser) {
      setUsers([]);
      return;
    }
    loadUsers();
  }, [currentUser, loadUsers]);

  useEffect(() => {
    if (dashboardRole !== "USER" || selectedUserId) {
      return;
    }

    const fallback = users.find((user) => user.role === "USER");
    if (fallback) {
      setSelectedUserId(fallback.id);
    }
  }, [dashboardRole, selectedUserId, users]);

  const loadTasks = useCallback(async () => {
    if (!currentUser) {
      setTasks([]);
      setLoading(false);
      return;
    }

    if (dashboardRole === "USER" && !viewerContext.viewerId) {
      setTasks([]);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const data = await taskApi.list(
        {
          status: statusFilter,
          priority: priorityFilter || undefined,
          q: search || undefined,
          sortBy,
          direction
        },
        viewerContext
      );
      setTasks(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [currentUser, dashboardRole, viewerContext, statusFilter, priorityFilter, search, sortBy, direction]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  async function handleAuthSubmit(event) {
    event.preventDefault();
    setAuthError("");
    setAuthBusy(true);

    try {
      const endpoint = authMode === "login" ? "/api/auth/login" : "/api/auth/signup";
      const payload = {
        email: authForm.email.trim(),
        password: authForm.password
      };
      if (authMode === "signup") {
        payload.displayName = authForm.displayName.trim();
      }

      const response = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Authentication failed");
      }

      const user = await response.json();
      setCurrentUser(user);
      setDashboardRole(user.role === "ADMIN" ? "ADMIN" : "USER");
      setSelectedUserId(user.role === "USER" ? user.id : null);
      setAuthForm(initialAuthForm);
      await loadTasks();
    } catch (err) {
      setAuthError(getErrorMessage(err));
    } finally {
      setAuthBusy(false);
    }
  }

  function handleLogout() {
    setCurrentUser(null);
    setDashboardRole("ADMIN");
    setSelectedUserId(null);
    setTasks([]);
    setForm(initialForm);
    setError("");
    setAuthError("");
  }

  const canSwitchToUserView = currentUser?.role === "ADMIN";

  const canSubmitTask = Boolean(
    currentUser && (dashboardRole !== "USER" || viewerContext.viewerId)
  );

  async function handleSubmit(event) {
    event.preventDefault();
    if (!form.title.trim()) {
      return;
    }

    if (!canSubmitTask) {
      setError("Sign in and select a dashboard before submitting tasks.");
      return;
    }

    const ownerId = dashboardRole === "ADMIN" ? Number(form.ownerId || viewerContext.viewerId) : viewerContext.viewerId;
    if (dashboardRole === "ADMIN" && !ownerId) {
      setError("Assign a user before saving a task.");
      return;
    }

    try {
      setSubmitting(true);
      const newTask = await taskApi.create(
        {
          title: form.title.trim(),
          description: form.description.trim(),
          dueDate: form.dueDate || null,
          priority: form.priority,
          completed: false,
          ownerId
        },
        viewerContext
      );
      setTasks((prev) => [newTask, ...prev]);
      setForm(initialForm);
      await loadTasks();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(id) {
    try {
      await taskApi.remove(id, viewerContext);
      setTasks((prev) => prev.filter((task) => task.id !== id));
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleToggle(task) {
    try {
      const updated = await taskApi.toggleComplete(task.id, !task.completed, viewerContext);
      setTasks((prev) => prev.map((item) => (item.id === task.id ? updated : item)));
      await loadTasks();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleEdit(task) {
    const nextTitle = window.prompt("Edit title", task.title);
    if (nextTitle === null) {
      return;
    }

    const cleanedTitle = nextTitle.trim();
    if (!cleanedTitle) {
      window.alert("Title cannot be empty.");
      return;
    }

    const nextDescription = window.prompt("Edit description", task.description || "");
    if (nextDescription === null) {
      return;
    }

    try {
      const updated = await taskApi.update(
        task.id,
        {
          title: cleanedTitle,
          description: nextDescription.trim(),
          dueDate: task.dueDate || null,
          priority: task.priority,
          completed: task.completed,
          ownerId: task.ownerId
        },
        viewerContext
      );
      setTasks((prev) => prev.map((item) => (item.id === task.id ? updated : item)));
      await loadTasks();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleClearCompleted() {
    if (!currentUser) {
      return;
    }
    try {
      const result = await taskApi.clearCompleted(viewerContext);
      if (result?.deleted > 0) {
        await loadTasks();
      }
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  const stats = useMemo(() => {
    const total = tasks.length;
    const completed = tasks.filter((task) => task.completed).length;
    return { total, completed, active: total - completed };
  }, [tasks]);

  const dashboardSummaries = useMemo(() => {
    if (dashboardRole !== "ADMIN" || currentUser?.role !== "ADMIN" || users.length === 0) {
      return [];
    }

    const accumulator = new Map();
    users
      .filter((user) => user.role === "USER")
      .forEach((user) => {
        accumulator.set(user.id, { user, total: 0, completed: 0 });
      });

    tasks.forEach((task) => {
      if (!accumulator.has(task.ownerId)) {
        return;
      }
      const record = accumulator.get(task.ownerId);
      record.total += 1;
      if (task.completed) {
        record.completed += 1;
      }
    });

    return Array.from(accumulator.values()).map((record) => ({
      ...record,
      active: record.total - record.completed
    }));
  }, [dashboardRole, currentUser, tasks, users]);

  const activeUser = users.find((user) => user.id === selectedUserId) || currentUser;

  return (
    <main className="app">
      <header className="hero">
        <p className="eyebrow">Operations Workspace</p>
        <h1>Task Management App</h1>
        <p className="subtitle">Plan, prioritize, and execute work with a clear view of progress.</p>
        <p className="hero-meta">
          {currentUser
            ? `${currentUser.displayName} · ${currentUser.role} view`
            : "Sign in or create an account to unlock dashboards."}
        </p>
      </header>

      <section className="panel auth">
        {currentUser ? (
          <div className="auth-state">
            <p>
              Signed in as <strong>{currentUser.displayName}</strong> ({currentUser.email})
            </p>
            <button type="button" className="text-link" onClick={handleLogout}>
              Sign out
            </button>
          </div>
        ) : (
          <form onSubmit={handleAuthSubmit} className="auth-form">
          <div className="auth-header">
            <h2>{authMode === "login" ? "Sign in to continue" : "Create your account"}</h2>
          </div>
          <label>
            Email
            <input
              value={authForm.email}
              onChange={(event) => setAuthForm((prev) => ({ ...prev, email: event.target.value }))}
              type="email"
              required
            />
          </label>
          <label>
            Password
            <input
              value={authForm.password}
              onChange={(event) => setAuthForm((prev) => ({ ...prev, password: event.target.value }))}
              type="password"
              minLength={6}
              required
            />
          </label>
          {authMode === "signup" && (
            <label>
              Display name
              <input
                value={authForm.displayName}
                onChange={(event) => setAuthForm((prev) => ({ ...prev, displayName: event.target.value }))}
                required
              />
            </label>
          )}
          <button type="submit" disabled={authBusy}>
            {authMode === "login" ? "Sign in" : "Create account"}
          </button>
          {authError && <p className="error">{authError}</p>}
          <p>
            {authMode === "login" ? (
              <>
                Don’t have an account?{" "}
                <button type="button" className="link-button" onClick={() => setAuthMode("signup")}>
                  Create one
                </button>
              </>
            ) : (
              <>
                Already have an account?{" "}
                <button type="button" className="link-button" onClick={() => setAuthMode("login")}>
                  Sign in
                </button>
              </>
            )}
          </p>
          </form>
        )}
      </section>

      {currentUser && (
        <>
          <section className="panel persona">
            <div className="persona-controls">
          <label>
            Role
            <select
              value={dashboardRole}
              onChange={(event) => {
                const nextRole = event.target.value;
                if (nextRole === "ADMIN" && currentUser?.role !== "ADMIN") {
                  setError("Admin view is only available for the registered admin.");
                  return;
                }
                setError("");
                setDashboardRole(nextRole);
              }}
              disabled={!canSwitchToUserView && currentUser?.role !== "ADMIN"}
            >
              <option value="ADMIN">Admin</option>
              <option value="USER">User</option>
            </select>
          </label>
              <label>
                Dashboard
                <select
                  value={selectedUserId ?? ""}
                  onChange={(event) => setSelectedUserId(event.target.value ? Number(event.target.value) : null)}
                  disabled={dashboardRole !== "USER"}
                >
                  <option value="">Select a user</option>
                  {users
                    .filter((user) => user.role === "USER")
                    .map((user) => (
                      <option key={user.id} value={user.id}>
                        {user.displayName}
                      </option>
                    ))}
                </select>
              </label>
            </div>
            {dashboardRole === "USER" && !viewerContext.viewerId && currentUser?.role === "ADMIN" && (
              <p className="notice">Pick a user so their board can load.</p>
            )}
          </section>

          <section className="panel stats">
            <span>Total Tasks: {stats.total}</span>
            <span>Active: {stats.active}</span>
            <span>Completed: {stats.completed}</span>
          </section>

          {dashboardRole === "ADMIN" && currentUser?.role === "ADMIN" && (
            <section className="panel dashboards">
              <h2>User Dashboards</h2>
              <div className="dashboard-grid">
                {dashboardSummaries.map((dashboard) => (
                  <article key={dashboard.user.id} className="dashboard-card">
                    <div className="dashboard-card-header">
                      <div>
                        <strong>{dashboard.user.displayName}</strong>
                        <p>{dashboard.user.role}</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => {
                          setDashboardRole("USER");
                          setSelectedUserId(dashboard.user.id);
                        }}
                      >
                        View
                      </button>
                    </div>
                    <div className="dashboard-card-stats">
                      <span>{dashboard.total} tasks</span>
                      <span>{dashboard.active} active</span>
                      <span>{dashboard.completed} done</span>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          )}

          <section className="panel">
            <h2>Create Task</h2>
            <form onSubmit={handleSubmit} className="task-form">
              <input
                value={form.title}
                onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))}
                placeholder="Task title"
                maxLength={120}
                required
                disabled={!canSubmitTask}
              />
              <textarea
                value={form.description}
                onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
                placeholder="Description"
                maxLength={400}
                disabled={!canSubmitTask}
              />
              <div className="grid-row">
                <input
                  type="date"
                  value={form.dueDate}
                  onChange={(event) => setForm((prev) => ({ ...prev, dueDate: event.target.value }))}
                  disabled={!canSubmitTask}
                />
                <select
                  value={form.priority}
                  onChange={(event) => setForm((prev) => ({ ...prev, priority: event.target.value }))}
                  disabled={!canSubmitTask}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                </select>
                {dashboardRole === "ADMIN" && (
                  <select
                    value={form.ownerId}
                    onChange={(event) => setForm((prev) => ({ ...prev, ownerId: event.target.value }))}
                    disabled={!currentUser}
                  >
                    <option value="">Assign to</option>
                    {users
                      .filter((user) => user.role === "USER")
                      .map((user) => (
                        <option key={user.id} value={user.id}>
                          {user.displayName}
                        </option>
                      ))}
                  </select>
                )}
                <button type="submit" disabled={submitting || !canSubmitTask}>
                  {submitting ? "Adding..." : "Add Task"}
                </button>
              </div>
            </form>
          </section>

          <section className="panel controls">
            <h2>Search & Filter</h2>
            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search tasks" />
            <div className="grid-row">
              <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                <option value="all">All statuses</option>
                <option value="active">Active</option>
                <option value="completed">Completed</option>
              </select>
              <select value={priorityFilter} onChange={(event) => setPriorityFilter(event.target.value)}>
                <option value="">All priorities</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
              <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
                <option value="createdAt">Sort: Created</option>
                <option value="dueDate">Sort: Due date</option>
                <option value="priority">Sort: Priority</option>
                <option value="title">Sort: Title</option>
              </select>
            </div>
            <div className="filters">
              <button type="button" onClick={() => setDirection((prev) => (prev === "asc" ? "desc" : "asc"))}>
                Direction: {direction.toUpperCase()}
              </button>
              <button type="button" onClick={() => loadTasks()}>
                Refresh
              </button>
              <button type="button" className="danger" onClick={handleClearCompleted} disabled={stats.completed === 0 || !canSubmitTask}>
                Clear Completed ({stats.completed})
              </button>
            </div>
          </section>

          {error && <p className="error">{error}</p>}

          <section className="panel">
            <h2>Task List</h2>
            {loading ? (
              <p>Loading tasks...</p>
            ) : dashboardRole === "USER" && !viewerContext.viewerId ? (
              <p>Select a user to populate this dashboard.</p>
            ) : tasks.length === 0 ? (
              <p>No tasks found.</p>
            ) : (
              <div className="task-list">
                {tasks.map((task) => (
                  <article className={`task ${task.completed ? "done" : ""}`} key={task.id}>
                    <div className="task-body">
                      <input type="checkbox" checked={task.completed} onChange={() => handleToggle(task)} />
                      <div>
                        <h3>{task.title}</h3>
                        <p>{task.description || "No description"}</p>
                        <small>
                          Priority: {task.priority} | Due: {task.dueDate || "N/A"}
                        </small>
                        <p className="owner-pill">
                          {task.ownerName} · {task.ownerRole}
                        </p>
                      </div>
                    </div>
                    <div className="task-actions">
                      <button type="button" onClick={() => handleEdit(task)}>
                        Edit
                      </button>
                      <button type="button" className="danger" onClick={() => handleDelete(task.id)}>
                        Delete
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </main>
  );
}

function getErrorMessage(error) {
  return error?.message || "Something went wrong.";
}
