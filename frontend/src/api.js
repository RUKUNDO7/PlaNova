const BASE_URL = "/api";

function toQueryString(params = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      searchParams.set(key, value);
    }
  });
  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

function buildPath(endpoint, params = {}) {
  return `${endpoint}${toQueryString(params)}`;
}

async function request(path = "", options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const errorPayload = await response.json();
      if (errorPayload?.message) {
        message = errorPayload.message;
      }
    } catch {
      const errorText = await response.text();
      if (errorText) {
        message = errorText;
      }
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const authApi = {
  login: (payload) => request("/auth/login", { method: "POST", body: JSON.stringify(payload) }),
  signup: (payload) => request("/auth/signup", { method: "POST", body: JSON.stringify(payload) })
};

export const userApi = {
  list: () => request("/users"),
  update: (id, payload) => request(`/users/${id}`, { method: "PUT", body: JSON.stringify(payload) })
};

export const projectApi = {
  list: (context = {}) => request(buildPath("/projects", context)),
  create: (payload, context = {}) =>
    request(buildPath("/projects", context), { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload, context = {}) =>
    request(buildPath(`/projects/${id}`, context), { method: "PUT", body: JSON.stringify(payload) }),
  remove: (id, context = {}) => request(buildPath(`/projects/${id}`, context), { method: "DELETE" }),
  members: (id, context = {}) => request(buildPath(`/projects/${id}/members`, context)),
  addMember: (id, userId, context = {}) =>
    request(buildPath(`/projects/${id}/members`, context), { method: "POST", body: JSON.stringify({ userId }) }),
  removeMember: (id, memberId, context = {}) =>
    request(buildPath(`/projects/${id}/members/${memberId}`, context), { method: "DELETE" })
};

export const boardApi = {
  list: (projectId, context = {}) => request(buildPath("/boards", { projectId, ...context })),
  create: (payload, context = {}) =>
    request(buildPath("/boards", context), { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload, context = {}) =>
    request(buildPath(`/boards/${id}`, context), { method: "PUT", body: JSON.stringify(payload) }),
  remove: (id, context = {}) => request(buildPath(`/boards/${id}`, context), { method: "DELETE" })
};

export const columnApi = {
  list: (boardId, context = {}) => request(buildPath("/columns", { boardId, ...context })),
  create: (payload, context = {}) =>
    request(buildPath("/columns", context), { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload, context = {}) =>
    request(buildPath(`/columns/${id}`, context), { method: "PUT", body: JSON.stringify(payload) }),
  remove: (id, context = {}) => request(buildPath(`/columns/${id}`, context), { method: "DELETE" })
};

export const labelApi = {
  list: (projectId, context = {}) => request(buildPath("/labels", { projectId, ...context })),
  create: (payload, context = {}) =>
    request(buildPath("/labels", context), { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload, context = {}) =>
    request(buildPath(`/labels/${id}`, context), { method: "PUT", body: JSON.stringify(payload) }),
  remove: (id, context = {}) => request(buildPath(`/labels/${id}`, context), { method: "DELETE" })
};

export const taskApi = {
  list: (params = {}, context = {}) => request(buildPath("/tasks", { ...params, ...context })),
  create: (payload, context = {}) =>
    request(buildPath("/tasks", context), { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload, context = {}) =>
    request(buildPath(`/tasks/${id}`, context), { method: "PUT", body: JSON.stringify(payload) }),
  toggleComplete: (id, completed, context = {}) =>
    request(buildPath(`/tasks/${id}/complete`, context), {
      method: "PATCH",
      body: JSON.stringify({ completed })
    }),
  remove: (id, context = {}) => request(buildPath(`/tasks/${id}`, context), { method: "DELETE" })
};

export const commentApi = {
  list: (taskId, context = {}) => request(buildPath(`/tasks/${taskId}/comments`, context)),
  create: (taskId, payload, context = {}) =>
    request(buildPath(`/tasks/${taskId}/comments`, context), { method: "POST", body: JSON.stringify(payload) })
};

export const attachmentApi = {
  list: (taskId, context = {}) => request(buildPath(`/tasks/${taskId}/attachments`, context)),
  create: (taskId, payload, context = {}) =>
    request(buildPath(`/tasks/${taskId}/attachments`, context), { method: "POST", body: JSON.stringify(payload) }),
  remove: (taskId, attachmentId, context = {}) =>
    request(buildPath(`/tasks/${taskId}/attachments/${attachmentId}`, context), { method: "DELETE" })
};

export const activityApi = {
  list: (taskId, context = {}) => request(buildPath(`/tasks/${taskId}/activity`, context))
};

export const notificationApi = {
  list: (recipientId) => request(buildPath("/notifications", { recipientId })),
  markRead: (id, recipientId) => request(buildPath(`/notifications/${id}/read`, { recipientId }), { method: "PATCH" }),
  markAllRead: (recipientId) => request(buildPath("/notifications/read-all", { recipientId }), { method: "PATCH" })
};

export { request };