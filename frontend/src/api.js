const BASE_URL = "/api/tasks";

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

function buildPath(endpoint, context = {}) {
  return `${endpoint}${toQueryString(context)}`;
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

export const taskApi = {
  list: (params = {}, context = {}) => request(buildPath("", { ...params, ...context })),
  create: (task, context = {}) => request(buildPath("", context), {
    method: "POST",
    body: JSON.stringify(task)
  }),
  update: (id, task, context = {}) => request(buildPath(`/${id}`, context), {
    method: "PUT",
    body: JSON.stringify(task)
  }),
  toggleComplete: (id, completed, context = {}) =>
    request(buildPath(`/${id}/complete`, context), {
      method: "PATCH",
      body: JSON.stringify({ completed })
    }),
  remove: (id, context = {}) => request(buildPath(`/${id}`, context), { method: "DELETE" }),
  clearCompleted: (context = {}) => request(buildPath("/completed", context), { method: "DELETE" })
};
