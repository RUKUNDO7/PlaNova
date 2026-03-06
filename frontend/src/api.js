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
  list: (params = {}) => request(toQueryString(params)),
  create: (task) => request("", { method: "POST", body: JSON.stringify(task) }),
  update: (id, task) => request(`/${id}`, { method: "PUT", body: JSON.stringify(task) }),
  toggleComplete: (id, completed) =>
    request(`/${id}/complete`, { method: "PATCH", body: JSON.stringify({ completed }) }),
  remove: (id) => request(`/${id}`, { method: "DELETE" }),
  clearCompleted: () => request("/completed", { method: "DELETE" })
};
