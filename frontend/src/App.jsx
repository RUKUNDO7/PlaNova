import { useCallback, useEffect, useMemo, useState } from "react";
import { taskApi } from "./api";

const initialForm = {
  title: "",
  description: "",
  dueDate: "",
  priority: "MEDIUM"
};

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [priorityFilter, setPriorityFilter] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [direction, setDirection] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const loadTasks = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await taskApi.list({
        status: statusFilter,
        priority: priorityFilter || undefined,
        q: search || undefined,
        sortBy,
        direction
      });
      setTasks(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [statusFilter, priorityFilter, search, sortBy, direction]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  async function handleSubmit(event) {
    event.preventDefault();
    if (!form.title.trim()) {
      return;
    }

    try {
      setSubmitting(true);
      const newTask = await taskApi.create({
        title: form.title.trim(),
        description: form.description.trim(),
        dueDate: form.dueDate || null,
        priority: form.priority,
        completed: false
      });
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
      await taskApi.remove(id);
      setTasks((prev) => prev.filter((task) => task.id !== id));
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleToggle(task) {
    try {
      const updated = await taskApi.toggleComplete(task.id, !task.completed);
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
      const updated = await taskApi.update(task.id, {
        ...task,
        title: cleanedTitle,
        description: nextDescription.trim()
      });
      setTasks((prev) => prev.map((item) => (item.id === task.id ? updated : item)));
      await loadTasks();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleClearCompleted() {
    try {
      const result = await taskApi.clearCompleted();
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

  return (
    <main className="app">
      <header>
        <h1>Task Management</h1>
        <p>Spring Boot + React task tracker</p>
      </header>

      <section className="panel">
        <form onSubmit={handleSubmit} className="task-form">
          <input
            value={form.title}
            onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
            placeholder="Task title"
            maxLength={120}
            required
          />
          <textarea
            value={form.description}
            onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
            placeholder="Description"
            maxLength={400}
          />
          <div className="grid-row">
            <input
              type="date"
              value={form.dueDate}
              onChange={(e) => setForm((prev) => ({ ...prev, dueDate: e.target.value }))}
            />
            <select
              value={form.priority}
              onChange={(e) => setForm((prev) => ({ ...prev, priority: e.target.value }))}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
            <button type="submit" disabled={submitting}>
              {submitting ? "Adding..." : "Add Task"}
            </button>
          </div>
        </form>
      </section>

      <section className="panel controls">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search tasks"
        />
        <div className="grid-row">
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="all">All statuses</option>
            <option value="active">Active</option>
            <option value="completed">Completed</option>
          </select>
          <select value={priorityFilter} onChange={(e) => setPriorityFilter(e.target.value)}>
            <option value="">All priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
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
          <button type="button" onClick={() => loadTasks()}>Refresh</button>
          <button type="button" className="danger" onClick={handleClearCompleted} disabled={stats.completed === 0}>
            Clear Completed ({stats.completed})
          </button>
        </div>
      </section>

      <section className="panel stats">
        <span>In view: {stats.total}</span>
        <span>Active: {stats.active}</span>
        <span>Completed: {stats.completed}</span>
      </section>

      {error && <p className="error">{error}</p>}

      <section className="panel">
        {loading ? (
          <p>Loading tasks...</p>
        ) : tasks.length === 0 ? (
          <p>No tasks found.</p>
        ) : (
          <div className="task-list">
            {tasks.map((task) => (
              <article className={`task ${task.completed ? "done" : ""}`} key={task.id}>
                <div className="task-body">
                  <input
                    type="checkbox"
                    checked={task.completed}
                    onChange={() => handleToggle(task)}
                  />
                  <div>
                    <h3>{task.title}</h3>
                    <p>{task.description || "No description"}</p>
                    <small>
                      Priority: {task.priority} | Due: {task.dueDate || "N/A"}
                    </small>
                  </div>
                </div>
                <div className="task-actions">
                  <button type="button" onClick={() => handleEdit(task)}>Edit</button>
                  <button type="button" className="danger" onClick={() => handleDelete(task.id)}>Delete</button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

function getErrorMessage(error) {
  return error?.message || "Something went wrong.";
}
