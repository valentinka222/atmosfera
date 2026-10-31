// Адреса без хоста: запрос уйдёт на тот же сервер, откуда загружена страница
const BASE = "/api";

async function request(path, options = {}) {
  const response = await fetch(BASE + path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  // Тело ответа может быть пустым или не JSON — тогда data будет null
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    // Бэкенд присылает ошибки в виде { status, message }
    const error = new Error(
      data?.message ?? `Ошибка сервера (${response.status})`,
    );
    error.status = response.status;
    throw error;
  }
  return data;
}

export const api = {
  getServices: () => request("/services"),

  getMasters: (serviceId) => request(`/services/${serviceId}/masters`),

  getSlots: (masterId, serviceId, date) =>
    request(`/masters/${masterId}/slots?serviceId=${serviceId}&date=${date}`),

  createAppointment: (body) =>
    request("/appointments", { method: "POST", body: JSON.stringify(body) }),

  getAppointment: (token) =>
    request(`/appointments/${encodeURIComponent(token)}`),

  cancelAppointment: (token) =>
    request(`/appointments/${encodeURIComponent(token)}/cancel`, {
      method: "POST",
    }),
};
