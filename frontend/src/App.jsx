import { useCallback, useEffect, useMemo, useState } from "react";
import {
  activityApi,
  attachmentApi,
  authApi,
  boardApi,
  columnApi,
  commentApi,
  labelApi,
  notificationApi,
  projectApi,
  taskApi,
  userApi
} from "./api";

const initialAuthForm = {
  email: "",
  password: "",
  displayName: ""
};

const initialProjectForm = {
  name: "",
  description: ""
};

const initialBoardForm = {
  name: ""
};

const initialColumnForm = {
  name: "",
  status: "TODO"
};

const initialTaskForm = {
  title: "",
  description: "",
  dueDate: "",
  priority: "MEDIUM",
  ownerId: "",
  columnId: "",
  labelIds: []
};

const initialLabelForm = {
  name: "",
  color: "#0ea5e9"
};

const initialAttachmentForm = {
  name: "",
  url: "",
  type: ""
};

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [authMode, setAuthMode] = useState("login");
  const [authForm, setAuthForm] = useState(initialAuthForm);
  const [authBusy, setAuthBusy] = useState(false);
  const [authError, setAuthError] = useState("");
  const [error, setError] = useState("");

  const [users, setUsers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [projectMembers, setProjectMembers] = useState([]);
  const [boards, setBoards] = useState([]);
  const [columns, setColumns] = useState([]);
  const [labels, setLabels] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [notifications, setNotifications] = useState([]);

  const [activeProjectId, setActiveProjectId] = useState(null);
  const [activeBoardId, setActiveBoardId] = useState(null);

  const [projectForm, setProjectForm] = useState(initialProjectForm);
  const [boardForm, setBoardForm] = useState(initialBoardForm);
  const [columnForm, setColumnForm] = useState(initialColumnForm);
  const [taskForm, setTaskForm] = useState(initialTaskForm);
  const [labelForm, setLabelForm] = useState(initialLabelForm);
  const [attachmentForm, setAttachmentForm] = useState(initialAttachmentForm);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [priorityFilter, setPriorityFilter] = useState("");
  const [labelFilter, setLabelFilter] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [direction, setDirection] = useState("desc");
  const [viewMode, setViewMode] = useState("board");

  const [activeTask, setActiveTask] = useState(null);
  const [taskDrawer, setTaskDrawer] = useState({
    draft: null,
    comments: [],
    attachments: [],
    activity: [],
    commentBody: "",
    mentionIds: []
  });

  const [darkMode, setDarkMode] = useState(() => {
    const stored = window.localStorage.getItem("planova-theme");
    return stored ? stored === "dark" : false;
  });

  const viewerContext = useMemo(() => {
    if (!currentUser) {
      return {};
    }
    return {
      viewerRole: currentUser.role,
      viewerId: currentUser.id
    };
  }, [currentUser]);

  useEffect(() => {
    document.documentElement.dataset.theme = darkMode ? "dark" : "light";
    window.localStorage.setItem("planova-theme", darkMode ? "dark" : "light");
  }, [darkMode]);

  const loadUsers = useCallback(async () => {
    try {
      const data = await userApi.list();
      setUsers(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  const loadProjects = useCallback(async () => {
    if (!currentUser) {
      setProjects([]);
      return;
    }
    try {
      const data = await projectApi.list(viewerContext);
      setProjects(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [currentUser, viewerContext]);

  const loadBoards = useCallback(async () => {
    if (!activeProjectId) {
      setBoards([]);
      return;
    }
    try {
      const data = await boardApi.list(activeProjectId, viewerContext);
      setBoards(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [activeProjectId, viewerContext]);

  const loadColumns = useCallback(async () => {
    if (!activeBoardId) {
      setColumns([]);
      return;
    }
    try {
      const data = await columnApi.list(activeBoardId, viewerContext);
      setColumns(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [activeBoardId, viewerContext]);

  const loadLabels = useCallback(async () => {
    if (!activeProjectId) {
      setLabels([]);
      return;
    }
    try {
      const data = await labelApi.list(activeProjectId, viewerContext);
      setLabels(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [activeProjectId, viewerContext]);

  const loadMembers = useCallback(async () => {
    if (!activeProjectId) {
      setProjectMembers([]);
      return;
    }
    try {
      const data = await projectApi.members(activeProjectId, viewerContext);
      setProjectMembers(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [activeProjectId, viewerContext]);

  const loadTasks = useCallback(async () => {
    if (!activeBoardId) {
      setTasks([]);
      return;
    }
    try {
      const data = await taskApi.list(
        {
          q: search || undefined,
          status: statusFilter,
          priority: priorityFilter || undefined,
          labelId: labelFilter || undefined,
          sortBy,
          direction,
          boardId: activeBoardId,
          projectId: activeProjectId
        },
        viewerContext
      );
      setTasks(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [activeBoardId, activeProjectId, search, statusFilter, priorityFilter, labelFilter, sortBy, direction, viewerContext]);

  const loadNotifications = useCallback(async () => {
    if (!currentUser) {
      setNotifications([]);
      return;
    }
    try {
      const data = await notificationApi.list(currentUser.id);
      setNotifications(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [currentUser]);

  useEffect(() => {
    if (!currentUser) {
      setUsers([]);
      setProjects([]);
      setBoards([]);
      setColumns([]);
      setLabels([]);
      setTasks([]);
      setNotifications([]);
      return;
    }
    loadUsers();
    loadProjects();
    loadNotifications();
  }, [currentUser, loadUsers, loadProjects, loadNotifications]);

  useEffect(() => {
    if (projects.length === 0) {
      setActiveProjectId(null);
      return;
    }
    if (!activeProjectId) {
      setActiveProjectId(projects[0].id);
    }
  }, [projects, activeProjectId]);

  useEffect(() => {
    if (!activeProjectId) {
      setBoards([]);
      setColumns([]);
      setLabels([]);
      setTasks([]);
      setProjectMembers([]);
      return;
    }
  }, [activeProjectId]);


  useEffect(() => {
    if (!activeProjectId) {
      return;
    }
    loadBoards();
    loadLabels();
    loadMembers();
  }, [activeProjectId, loadBoards, loadLabels, loadMembers]);

  useEffect(() => {
    if (boards.length === 0) {
      setActiveBoardId(null);
      return;
    }
    if (!activeBoardId || !boards.some((board) => board.id === activeBoardId)) {
      setActiveBoardId(boards[0].id);
    }
  }, [boards, activeBoardId]);

  useEffect(() => {
    if (!activeBoardId) {
      return;
    }
    loadColumns();
    loadTasks();
  }, [activeBoardId, loadColumns, loadTasks]);

  useEffect(() => {
    if (columns.length === 0) {
      return;
    }
    if (!taskForm.columnId) {
      setTaskForm((prev) => ({ ...prev, columnId: String(columns[0].id) }));
    }
  }, [columns, taskForm.columnId]);


  useEffect(() => {
    if (!activeBoardId) {
      return;
    }
    loadTasks();
  }, [search, statusFilter, priorityFilter, labelFilter, sortBy, direction, activeBoardId, loadTasks]);

  async function handleAuthSubmit(event) {
    event.preventDefault();
    setAuthError("");
    setAuthBusy(true);
    try {
      const payload = {
        email: authForm.email.trim(),
        password: authForm.password
      };
      const user =
        authMode === "login"
          ? await authApi.login(payload)
          : await authApi.signup({ ...payload, displayName: authForm.displayName.trim() });
      setCurrentUser(user);
      setAuthForm(initialAuthForm);
    } catch (err) {
      setAuthError(getErrorMessage(err));
    } finally {
      setAuthBusy(false);
    }
  }

  function handleLogout() {
    setCurrentUser(null);
    setActiveProjectId(null);
    setActiveBoardId(null);
    setProjectMembers([]);
    setBoards([]);
    setColumns([]);
    setLabels([]);
    setTasks([]);
    setNotifications([]);
    setError("");
  }

  async function handleCreateProject(event) {
    event.preventDefault();
    if (!projectForm.name.trim()) {
      return;
    }
    try {
      const created = await projectApi.create(
        {
          name: projectForm.name.trim(),
          description: projectForm.description.trim(),
          ownerId: currentUser.id
        },
        viewerContext
      );
      setProjects((prev) => [created, ...prev]);
      setProjectForm(initialProjectForm);
      setActiveProjectId(created.id);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleCreateBoard(event) {
    event.preventDefault();
    if (!boardForm.name.trim() || !activeProjectId) {
      return;
    }
    try {
      const created = await boardApi.create(
        { name: boardForm.name.trim(), projectId: activeProjectId },
        viewerContext
      );
      setBoards((prev) => [...prev, created]);
      setBoardForm(initialBoardForm);
      setActiveBoardId(created.id);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleCreateColumn(event) {
    event.preventDefault();
    if (!columnForm.name.trim() || !activeBoardId) {
      return;
    }
    try {
      const created = await columnApi.create(
        {
          name: columnForm.name.trim(),
          status: columnForm.status,
          boardId: activeBoardId
        },
        viewerContext
      );
      setColumns((prev) => [...prev, created]);
      setColumnForm(initialColumnForm);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleCreateLabel(event) {
    event.preventDefault();
    if (!labelForm.name.trim() || !activeProjectId) {
      return;
    }
    try {
      const created = await labelApi.create(
        { name: labelForm.name.trim(), color: labelForm.color, projectId: activeProjectId },
        viewerContext
      );
      setLabels((prev) => [...prev, created]);
      setLabelForm(initialLabelForm);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleCreateTask(event) {
    event.preventDefault();
    if (!taskForm.title.trim() || !taskForm.columnId) {
      return;
    }
    try {
      const payload = {
        title: taskForm.title.trim(),
        description: taskForm.description.trim(),
        dueDate: taskForm.dueDate || null,
        priority: taskForm.priority,
        ownerId: Number(taskForm.ownerId || currentUser.id),
        columnId: Number(taskForm.columnId),
        labelIds: taskForm.labelIds.map((id) => Number(id))
      };
      const created = await taskApi.create(payload, viewerContext);
      setTasks((prev) => [created, ...prev]);
      setTaskForm({ ...initialTaskForm, columnId: taskForm.columnId });
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleInviteMember(userId) {
    if (!activeProjectId || !userId) {
      return;
    }
    try {
      await projectApi.addMember(activeProjectId, userId, viewerContext);
      await loadMembers();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleUpdateProfile(displayName) {
    if (!currentUser || !displayName.trim()) {
      return;
    }
    try {
      const updated = await userApi.update(currentUser.id, { displayName: displayName.trim() });
      setCurrentUser((prev) => ({ ...prev, displayName: updated.displayName }));
      await loadUsers();
      await loadMembers();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleMoveColumn(columnId, direction) {
    const index = columns.findIndex((col) => col.id === columnId);
    const targetIndex = index + direction;
    if (index < 0 || targetIndex < 0 || targetIndex >= columns.length) {
      return;
    }
    const reordered = [...columns];
    const [current] = reordered.splice(index, 1);
    reordered.splice(targetIndex, 0, current);
    const updated = reordered.map((col, idx) => ({ ...col, position: idx }));
    setColumns(updated);
    try {
      await Promise.all(
        updated.map((col) =>
          columnApi.update(
            col.id,
            { name: col.name, status: col.status, position: col.position, boardId: col.boardId },
            viewerContext
          )
        )
      );
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleTaskDrop(taskId, columnId) {
    const task = tasks.find((item) => item.id === taskId);
    if (!task || task.columnId === columnId) {
      return;
    }
    try {
      const payload = buildTaskPayload(task, { columnId });
      const updated = await taskApi.update(task.id, payload, viewerContext);
      setTasks((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  function openTask(task) {
    setActiveTask(task);
    setTaskDrawer({
      draft: {
        title: task.title,
        description: task.description || "",
        dueDate: task.dueDate || "",
        priority: task.priority,
        ownerId: task.ownerId ? String(task.ownerId) : "",
        columnId: task.columnId ? String(task.columnId) : "",
        labelIds: task.labels?.map((label) => String(label.id)) || [],
        completed: task.completed
      },
      comments: [],
      attachments: [],
      activity: [],
      commentBody: "",
      mentionIds: []
    });
  }

  const loadTaskDetails = useCallback(
    async (taskId) => {
      if (!taskId) {
        return;
      }
      try {
        const [comments, attachments, activity] = await Promise.all([
          commentApi.list(taskId, viewerContext),
          attachmentApi.list(taskId, viewerContext),
          activityApi.list(taskId, viewerContext)
        ]);
        setTaskDrawer((prev) => ({
          ...prev,
          comments,
          attachments,
          activity
        }));
      } catch (err) {
        setError(getErrorMessage(err));
      }
    },
    [viewerContext]
  );

  useEffect(() => {
    if (activeTask?.id) {
      loadTaskDetails(activeTask.id);
    }
  }, [activeTask, loadTaskDetails]);

  async function handleSaveTaskDetails() {
    if (!activeTask || !taskDrawer.draft) {
      return;
    }
    try {
      const payload = buildTaskPayload(activeTask, taskDrawer.draft);
      const updated = await taskApi.update(activeTask.id, payload, viewerContext);
      setTasks((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
      setActiveTask(updated);
      await loadTaskDetails(updated.id);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleDeleteTask(taskId) {
    try {
      await taskApi.remove(taskId, viewerContext);
      setTasks((prev) => prev.filter((item) => item.id !== taskId));
      if (activeTask?.id === taskId) {
        setActiveTask(null);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleAddComment(event) {
    event.preventDefault();
    if (!activeTask || !taskDrawer.commentBody.trim()) {
      return;
    }
    try {
      const created = await commentApi.create(
        activeTask.id,
        {
          body: taskDrawer.commentBody.trim(),
          authorId: currentUser.id,
          mentionIds: taskDrawer.mentionIds.map((id) => Number(id))
        },
        viewerContext
      );
      setTaskDrawer((prev) => ({
        ...prev,
        comments: [created, ...prev.comments],
        commentBody: "",
        mentionIds: []
      }));
      await loadNotifications();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleAddAttachment(event) {
    event.preventDefault();
    if (!activeTask || !attachmentForm.name.trim() || !attachmentForm.url.trim()) {
      return;
    }
    try {
      const created = await attachmentApi.create(
        activeTask.id,
        {
          name: attachmentForm.name.trim(),
          url: attachmentForm.url.trim(),
          type: attachmentForm.type.trim(),
          uploadedById: currentUser.id
        },
        viewerContext
      );
      setTaskDrawer((prev) => ({
        ...prev,
        attachments: [created, ...prev.attachments]
      }));
      setAttachmentForm(initialAttachmentForm);
      await loadNotifications();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleMarkNotification(id) {
    try {
      const updated = await notificationApi.markRead(id, currentUser.id);
      setNotifications((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleMarkAllNotifications() {
    try {
      await notificationApi.markAllRead(currentUser.id);
      await loadNotifications();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  const activeProject = projects.find((project) => project.id === activeProjectId) || null;
  const activeBoard = boards.find((board) => board.id === activeBoardId) || null;

  const columnTasks = useMemo(() => {
    const map = new Map();
    columns.forEach((column) => map.set(column.id, []));
    tasks.forEach((task) => {
      if (!task.columnId) {
        return;
      }
      if (!map.has(task.columnId)) {
        map.set(task.columnId, []);
      }
      map.get(task.columnId).push(task);
    });
    return map;
  }, [columns, tasks]);

  const unreadNotifications = notifications.filter((notification) => !notification.read).length;

  if (!currentUser) {
    return (
      <main className="auth-shell">
        <section className="hero">
          <div className="logo">PN</div>
          <div>
            <p className="eyebrow">PlaNova Workspace</p>
            <h1>PlaNova</h1>
            <p className="subtitle">Plan, align, and ship with a shared view of what matters.</p>
          </div>
        </section>
        <section className="panel auth">
          <form onSubmit={handleAuthSubmit} className="auth-form">
            <h2>{authMode === "login" ? "Sign in" : "Create account"}</h2>
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
            <p className="toggle">
              {authMode === "login" ? "New to PlaNova?" : "Already have an account?"}
              <button type="button" className="link-button" onClick={() => setAuthMode(authMode === "login" ? "signup" : "login")}>
                {authMode === "login" ? "Create one" : "Sign in"}
              </button>
            </p>
          </form>
        </section>
      </main>
    );
  }

  return (
    <main className="app">
      <header className="topbar">
        <div className="brand">
          <div className="logo">PN</div>
          <div>
            <p className="eyebrow">PlaNova</p>
            <h1>Delivery Command</h1>
          </div>
        </div>
        <div className="topbar-actions">
          <button type="button" className="ghost" onClick={() => setDarkMode((prev) => !prev)}>
            {darkMode ? "Light" : "Dark"} mode
          </button>
          <button type="button" className="ghost" onClick={handleMarkAllNotifications}>
            Notifications {unreadNotifications ? `(${unreadNotifications})` : ""}
          </button>
          <div className="profile">
            <span>{currentUser.displayName}</span>
            <button type="button" className="link-button" onClick={handleLogout}>
              Sign out
            </button>
          </div>
        </div>
      </header>

      <div className="layout">
        <aside className="sidebar">
          <section className="panel">
            <h2>Projects</h2>
            <div className="list">
              {projects.map((project) => (
                <button
                  key={project.id}
                  type="button"
                  className={project.id === activeProjectId ? "list-item active" : "list-item"}
                  onClick={() => setActiveProjectId(project.id)}
                >
                  <span>{project.name}</span>
                  <small>{project.memberCount} members</small>
                </button>
              ))}
              {projects.length === 0 && <p className="muted">No projects yet.</p>}
            </div>
            <form onSubmit={handleCreateProject} className="stack">
              <input
                placeholder="New project"
                value={projectForm.name}
                onChange={(event) => setProjectForm((prev) => ({ ...prev, name: event.target.value }))}
              />
              <textarea
                placeholder="Project brief"
                value={projectForm.description}
                onChange={(event) => setProjectForm((prev) => ({ ...prev, description: event.target.value }))}
              />
              <button type="submit">Create project</button>
            </form>
          </section>

          <section className="panel">
            <h2>Boards</h2>
            <div className="list">
              {boards.map((board) => (
                <button
                  key={board.id}
                  type="button"
                  className={board.id === activeBoardId ? "list-item active" : "list-item"}
                  onClick={() => setActiveBoardId(board.id)}
                >
                  <span>{board.name}</span>
                  <small>Position {board.position + 1}</small>
                </button>
              ))}
              {boards.length === 0 && <p className="muted">No boards yet.</p>}
            </div>
            <form onSubmit={handleCreateBoard} className="stack">
              <input
                placeholder="New board"
                value={boardForm.name}
                onChange={(event) => setBoardForm({ name: event.target.value })}
              />
              <button type="submit">Create board</button>
            </form>
          </section>
        </aside>

        <section className="workspace">
          <section className="panel workspace-header">
            <div>
              <h2>{activeProject ? activeProject.name : "Select a project"}</h2>
              <p className="muted">{activeBoard ? activeBoard.name : ""}</p>
            </div>
            <div className="toolbar">
              <input
                placeholder="Search tasks"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                <option value="all">All</option>
                <option value="active">Active</option>
                <option value="completed">Completed</option>
              </select>
              <select value={priorityFilter} onChange={(event) => setPriorityFilter(event.target.value)}>
                <option value="">Priority</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
              <select value={labelFilter} onChange={(event) => setLabelFilter(event.target.value)}>
                <option value="">Labels</option>
                {labels.map((label) => (
                  <option key={label.id} value={String(label.id)}>
                    {label.name}
                  </option>
                ))}
              </select>
              <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
                <option value="createdAt">Sort: Created</option>
                <option value="updatedAt">Sort: Updated</option>
                <option value="dueDate">Sort: Due</option>
                <option value="priority">Sort: Priority</option>
              </select>
              <button type="button" className="ghost" onClick={() => setDirection((prev) => (prev === "asc" ? "desc" : "asc"))}>
                {direction === "asc" ? "Asc" : "Desc"}
              </button>
              <div className="view-toggle">
                <button type="button" className={viewMode === "board" ? "active" : ""} onClick={() => setViewMode("board")}>
                  Board
                </button>
                <button type="button" className={viewMode === "list" ? "active" : ""} onClick={() => setViewMode("list")}>
                  List
                </button>
                <button type="button" className={viewMode === "calendar" ? "active" : ""} onClick={() => setViewMode("calendar")}>
                  Calendar
                </button>
              </div>
            </div>
          </section>

          {viewMode === "board" && (
            <section className="board">
              <div className="column-add">
                <form onSubmit={handleCreateColumn} className="inline-form">
                  <input
                    placeholder="Add column"
                    value={columnForm.name}
                    onChange={(event) => setColumnForm((prev) => ({ ...prev, name: event.target.value }))}
                  />
                  <select value={columnForm.status} onChange={(event) => setColumnForm((prev) => ({ ...prev, status: event.target.value }))}>
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="DONE">Done</option>
                  </select>
                  <button type="submit">Add</button>
                </form>
              </div>
              <div className="columns">
                {columns.map((column, index) => (
                  <div
                    key={column.id}
                    className="column"
                    onDragOver={(event) => event.preventDefault()}
                    onDrop={(event) => {
                      event.preventDefault();
                      const taskId = Number(event.dataTransfer.getData("text/taskId"));
                      if (taskId) {
                        handleTaskDrop(taskId, column.id);
                      }
                    }}
                  >
                    <div className="column-header">
                      <div>
                        <h3>{column.name}</h3>
                        <p>{column.status.replace("_", " ")}</p>
                      </div>
                      <div className="column-actions">
                        <button type="button" className="ghost" onClick={() => handleMoveColumn(column.id, -1)} disabled={index === 0}>
                          ?
                        </button>
                        <button type="button" className="ghost" onClick={() => handleMoveColumn(column.id, 1)} disabled={index === columns.length - 1}>
                          ?
                        </button>
                      </div>
                    </div>
                    <div className="task-stack">
                      {(columnTasks.get(column.id) || []).map((task) => (
                        <article
                          key={task.id}
                          className={`task-card priority-${task.priority.toLowerCase()}`}
                          draggable
                          onDragStart={(event) => event.dataTransfer.setData("text/taskId", String(task.id))}
                          onClick={() => openTask(task)}
                        >
                          <header>
                            <h4>{task.title}</h4>
                            <span className={task.completed ? "status done" : "status"}>{task.completed ? "Done" : "Active"}</span>
                          </header>
                          <p>{task.description || "No description"}</p>
                          <div className="meta">
                            <span>Due {task.dueDate || "-"}</span>
                            <span>{task.ownerName}</span>
                          </div>
                          <div className="label-row">
                            {task.labels?.map((label) => (
                              <span key={label.id} style={{ background: label.color }}>
                                {label.name}
                              </span>
                            ))}
                          </div>
                        </article>
                      ))}
                      {(columnTasks.get(column.id) || []).length === 0 && <p className="muted">Drop tasks here.</p>}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}

          {viewMode === "list" && (
            <section className="panel list-view">
              {tasks.length === 0 ? (
                <p className="muted">No tasks yet.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Task</th>
                      <th>Owner</th>
                      <th>Due</th>
                      <th>Priority</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tasks.map((task) => (
                      <tr key={task.id} onClick={() => openTask(task)}>
                        <td>{task.title}</td>
                        <td>{task.ownerName}</td>
                        <td>{task.dueDate || "-"}</td>
                        <td>{task.priority}</td>
                        <td>{task.completed ? "Done" : "Active"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}

          {viewMode === "calendar" && (
            <section className="panel calendar">
              {buildCalendar(tasks).map((day) => (
                <div key={day.date} className="calendar-day">
                  <h4>{day.label}</h4>
                  {day.tasks.length === 0 ? (
                    <p className="muted">No tasks</p>
                  ) : (
                    day.tasks.map((task) => (
                      <button key={task.id} type="button" className="calendar-task" onClick={() => openTask(task)}>
                        {task.title}
                      </button>
                    ))
                  )}
                </div>
              ))}
            </section>
          )}
        </section>

        <aside className="insights">
          <section className="panel">
            <h2>Quick Task</h2>
            <form onSubmit={handleCreateTask} className="stack">
              <input
                placeholder="Task title"
                value={taskForm.title}
                onChange={(event) => setTaskForm((prev) => ({ ...prev, title: event.target.value }))}
              />
              <textarea
                placeholder="Description"
                value={taskForm.description}
                onChange={(event) => setTaskForm((prev) => ({ ...prev, description: event.target.value }))}
              />
              <select value={taskForm.columnId} onChange={(event) => setTaskForm((prev) => ({ ...prev, columnId: event.target.value }))}>
                <option value="">Column</option>
                {columns.map((column) => (
                  <option key={column.id} value={String(column.id)}>
                    {column.name}
                  </option>
                ))}
              </select>
              <select value={taskForm.ownerId} onChange={(event) => setTaskForm((prev) => ({ ...prev, ownerId: event.target.value }))}>
                <option value="">Assignee</option>
                {projectMembers.map((member) => (
                  <option key={member.id} value={String(member.id)}>
                    {member.displayName}
                  </option>
                ))}
              </select>
              <input
                type="date"
                value={taskForm.dueDate}
                onChange={(event) => setTaskForm((prev) => ({ ...prev, dueDate: event.target.value }))}
              />
              <select value={taskForm.priority} onChange={(event) => setTaskForm((prev) => ({ ...prev, priority: event.target.value }))}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
              <select
                multiple
                value={taskForm.labelIds}
                onChange={(event) =>
                  setTaskForm((prev) => ({
                    ...prev,
                    labelIds: Array.from(event.target.selectedOptions).map((option) => option.value)
                  }))
                }
              >
                {labels.map((label) => (
                  <option key={label.id} value={String(label.id)}>
                    {label.name}
                  </option>
                ))}
              </select>
              <button type="submit">Add task</button>
            </form>
          </section>

          <section className="panel">
            <h2>Members</h2>
            <div className="member-grid">
              {projectMembers.map((member) => (
                <div key={member.id} className="member-card">
                  <span>{member.displayName}</span>
                  <small>{member.role}</small>
                </div>
              ))}
            </div>
            <div className="invite">
              <select onChange={(event) => handleInviteMember(Number(event.target.value))} value="">
                <option value="">Invite member</option>
                {users
                  .filter((user) => !projectMembers.some((member) => member.id === user.id))
                  .map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.displayName}
                    </option>
                  ))}
              </select>
            </div>
          </section>

          <section className="panel">
            <h2>Labels</h2>
            <div className="label-grid">
              {labels.map((label) => (
                <span key={label.id} style={{ background: label.color }}>
                  {label.name}
                </span>
              ))}
            </div>
            <form onSubmit={handleCreateLabel} className="stack">
              <input
                placeholder="Label name"
                value={labelForm.name}
                onChange={(event) => setLabelForm((prev) => ({ ...prev, name: event.target.value }))}
              />
              <input
                type="color"
                value={labelForm.color}
                onChange={(event) => setLabelForm((prev) => ({ ...prev, color: event.target.value }))}
              />
              <button type="submit">Add label</button>
            </form>
          </section>

          <section className="panel">
            <h2>Notifications</h2>
            {notifications.length === 0 ? (
              <p className="muted">No notifications.</p>
            ) : (
              <div className="notification-list">
                {notifications.map((notification) => (
                  <button
                    key={notification.id}
                    type="button"
                    className={notification.read ? "notification" : "notification unread"}
                    onClick={() => handleMarkNotification(notification.id)}
                  >
                    <span>{notification.message}</span>
                    <small>{formatDate(notification.createdAt)}</small>
                  </button>
                ))}
              </div>
            )}
          </section>

          <section className="panel">
            <h2>Profile</h2>
            <ProfileEditor user={currentUser} onSave={handleUpdateProfile} />
          </section>
        </aside>
      </div>

      {activeTask && (
        <div className="modal">
          <div className="modal-content">
            <header>
              <h3>Edit Task</h3>
              <button type="button" className="ghost" onClick={() => setActiveTask(null)}>
                Close
              </button>
            </header>
            <div className="modal-body">
              <div className="modal-section">
                <label>
                  Title
                  <input
                    value={taskDrawer.draft?.title || ""}
                    onChange={(event) =>
                      setTaskDrawer((prev) => ({
                        ...prev,
                        draft: { ...prev.draft, title: event.target.value }
                      }))
                    }
                  />
                </label>
                <label>
                  Description
                  <textarea
                    value={taskDrawer.draft?.description || ""}
                    onChange={(event) =>
                      setTaskDrawer((prev) => ({
                        ...prev,
                        draft: { ...prev.draft, description: event.target.value }
                      }))
                    }
                  />
                </label>
                <div className="grid">
                  <label>
                    Due date
                    <input
                      type="date"
                      value={taskDrawer.draft?.dueDate || ""}
                      onChange={(event) =>
                        setTaskDrawer((prev) => ({
                          ...prev,
                          draft: { ...prev.draft, dueDate: event.target.value }
                        }))
                      }
                    />
                  </label>
                  <label>
                    Priority
                    <select
                      value={taskDrawer.draft?.priority || "MEDIUM"}
                      onChange={(event) =>
                        setTaskDrawer((prev) => ({
                          ...prev,
                          draft: { ...prev.draft, priority: event.target.value }
                        }))
                      }
                    >
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                    </select>
                  </label>
                  <label>
                    Column
                    <select
                      value={taskDrawer.draft?.columnId || ""}
                      onChange={(event) =>
                        setTaskDrawer((prev) => ({
                          ...prev,
                          draft: { ...prev.draft, columnId: event.target.value }
                        }))
                      }
                    >
                      {columns.map((column) => (
                        <option key={column.id} value={String(column.id)}>
                          {column.name}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Assignee
                    <select
                      value={taskDrawer.draft?.ownerId || ""}
                      onChange={(event) =>
                        setTaskDrawer((prev) => ({
                          ...prev,
                          draft: { ...prev.draft, ownerId: event.target.value }
                        }))
                      }
                    >
                      {projectMembers.map((member) => (
                        <option key={member.id} value={String(member.id)}>
                          {member.displayName}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Labels
                    <select
                      multiple
                      value={taskDrawer.draft?.labelIds || []}
                      onChange={(event) =>
                        setTaskDrawer((prev) => ({
                          ...prev,
                          draft: {
                            ...prev.draft,
                            labelIds: Array.from(event.target.selectedOptions).map((option) => option.value)
                          }
                        }))
                      }
                    >
                      {labels.map((label) => (
                        <option key={label.id} value={String(label.id)}>
                          {label.name}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
                <div className="modal-actions">
                  <button type="button" onClick={handleSaveTaskDetails}>
                    Save changes
                  </button>
                  <button type="button" className="danger" onClick={() => handleDeleteTask(activeTask.id)}>
                    Delete task
                  </button>
                </div>
              </div>

              <div className="modal-section">
                <h4>Comments</h4>
                <form onSubmit={handleAddComment} className="stack">
                  <textarea
                    placeholder="Write a comment"
                    value={taskDrawer.commentBody}
                    onChange={(event) => setTaskDrawer((prev) => ({ ...prev, commentBody: event.target.value }))}
                  />
                  <select
                    multiple
                    value={taskDrawer.mentionIds}
                    onChange={(event) =>
                      setTaskDrawer((prev) => ({
                        ...prev,
                        mentionIds: Array.from(event.target.selectedOptions).map((option) => option.value)
                      }))
                    }
                  >
                    {projectMembers.map((member) => (
                      <option key={member.id} value={String(member.id)}>
                        Mention {member.displayName}
                      </option>
                    ))}
                  </select>
                  <button type="submit">Add comment</button>
                </form>
                <div className="thread">
                  {taskDrawer.comments.map((comment) => (
                    <div key={comment.id} className="thread-item">
                      <strong>{comment.authorName}</strong>
                      <p>{comment.body}</p>
                      <small>{formatDate(comment.createdAt)}</small>
                    </div>
                  ))}
                </div>
              </div>

              <div className="modal-section">
                <h4>Attachments</h4>
                <form onSubmit={handleAddAttachment} className="stack">
                  <input
                    placeholder="File name"
                    value={attachmentForm.name}
                    onChange={(event) => setAttachmentForm((prev) => ({ ...prev, name: event.target.value }))}
                  />
                  <input
                    placeholder="URL"
                    value={attachmentForm.url}
                    onChange={(event) => setAttachmentForm((prev) => ({ ...prev, url: event.target.value }))}
                  />
                  <input
                    placeholder="Type"
                    value={attachmentForm.type}
                    onChange={(event) => setAttachmentForm((prev) => ({ ...prev, type: event.target.value }))}
                  />
                  <button type="submit">Attach</button>
                </form>
                <div className="attachment-list">
                  {taskDrawer.attachments.map((attachment) => (
                    <a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer">
                      {attachment.name}
                    </a>
                  ))}
                </div>
              </div>

              <div className="modal-section">
                <h4>Activity</h4>
                <div className="activity">
                  {taskDrawer.activity.map((activity) => (
                    <div key={activity.id} className="activity-item">
                      <span>{activity.message}</span>
                      <small>{formatDate(activity.createdAt)}</small>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {error && <p className="error toast">{error}</p>}
    </main>
  );
}

function ProfileEditor({ user, onSave }) {
  const [name, setName] = useState(user.displayName);

  useEffect(() => {
    setName(user.displayName);
  }, [user.displayName]);


  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        onSave(name);
      }}
      className="stack"
    >
      <input value={name} onChange={(event) => setName(event.target.value)} />
      <button type="submit">Update profile</button>
    </form>
  );
}

function buildTaskPayload(task, overrides = {}) {
  const data = { ...task, ...overrides };
  return {
    title: data.title,
    description: data.description || "",
    dueDate: data.dueDate || null,
    priority: data.priority,
    completed: data.completed,
    ownerId: data.ownerId ? Number(data.ownerId) : undefined,
    columnId: data.columnId ? Number(data.columnId) : undefined,
    labelIds: data.labelIds
      ? data.labelIds.map((id) => Number(id))
      : data.labels?.map((label) => label.id) || []
  };
}

function buildCalendar(tasks) {
  const days = 14;
  const today = new Date();
  const output = [];
  for (let i = 0; i < days; i += 1) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    const dateKey = date.toISOString().slice(0, 10);
    const label = date.toLocaleDateString(undefined, { month: "short", day: "numeric" });
    output.push({
      date: dateKey,
      label,
      tasks: tasks.filter((task) => task.dueDate === dateKey)
    });
  }
  return output;
}

function formatDate(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString(undefined, { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

function getErrorMessage(error) {
  return error?.message || "Something went wrong.";
}






