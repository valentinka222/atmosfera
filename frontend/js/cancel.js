import { api } from "./api.js";
import { renderDetails } from "./common.js";

const $ = (id) => document.getElementById(id);

// Токен из адреса: cancel.html?token=...
const token = new URLSearchParams(location.search).get("token");

function showError(text) {
  $("error").textContent = text;
  $("error").hidden = false;
}

function showNotice(text) {
  $("notice").textContent = text;
  $("notice").hidden = false;
  $("new-booking").hidden = false;
}

async function load() {
  if (!token) {
    $("status").hidden = true;
    showError("В ссылке нет кода записи. Откройте ссылку из письма целиком.");
    return;
  }

  try {
    const appointment = await api.getAppointment(token);
    $("status").hidden = true;
    renderDetails($("details"), appointment, true);

    if (appointment.status === "BOOKED") {
      $("cancel-btn").hidden = false;
    } else if (appointment.status === "CANCELLED") {
      showNotice("Эта запись уже отменена.");
    } else {
      showNotice("Этот визит уже состоялся.");
    }
  } catch (e) {
    $("status").hidden = true;
    // 404 — токена нет в базе, 400 — токен испорчен (не похож на UUID)
    showError(
      e.status === 404 || e.status === 400
        ? "Запись не найдена. Проверьте ссылку из письма."
        : e.message,
    );
  }
}

$("cancel-btn").addEventListener("click", async () => {
  const button = $("cancel-btn");
  button.disabled = true;
  button.textContent = "Отменяем…";
  $("error").hidden = true;

  try {
    const appointment = await api.cancelAppointment(token);
    renderDetails($("details"), appointment, true);
    button.hidden = true;
    showNotice("Запись отменена. Подтверждение отправлено на почту.");
  } catch (e) {
    // Например, 409: до визита меньше 2 часов
    showError(e.message);
    button.disabled = false;
    button.textContent = "Отменить запись";
  }
});

load();
