import { api } from "./api.js";
import {
  el,
  formatPrice,
  formatDate,
  toIsoDate,
  renderDetails,
} from "./common.js";

// Что пользователь уже выбрал
const state = { service: null, master: null, date: null, time: null };

const $ = (id) => document.getElementById(id);
const scrollBehavior = matchMedia("(prefers-reduced-motion: reduce)").matches
  ? "auto"
  : "smooth";

// Шаги после выбора услуги
const STEPS = ["step-master", "step-time", "step-contacts"];

// Показать шаг и скрыть все, что идут после него
function showStep(id) {
  const index = STEPS.indexOf(id);
  STEPS.forEach((stepId, i) => {
    $(stepId).hidden = i > index;
  });
  $(id).scrollIntoView({ behavior: scrollBehavior, block: "start" });
}

// Отметить нажатую кнопку выбранной, а остальные в этом блоке — нет
function markSelected(container, button) {
  container
    .querySelectorAll("[aria-pressed]")
    .forEach((b) => b.setAttribute("aria-pressed", "false"));
  button.setAttribute("aria-pressed", "true");
}

// Кнопка выбора с меткой для автотестов
function choiceButton(className, testId, onClick) {
  const button = el("button", className);
  button.type = "button";
  button.dataset.testid = testId;
  button.setAttribute("aria-pressed", "false");
  button.addEventListener("click", onClick);
  return button;
}

/* ---------- Шаг 1: услуга ---------- */

async function loadServices() {
  const box = $("services");
  try {
    const services = await api.getServices();

    // Группируем по категориям: { "Волосы": [...], "Ногти": [...] }
    const groups = new Map();
    services.forEach((s) => {
      if (!groups.has(s.category)) groups.set(s.category, []);
      groups.get(s.category).push(s);
    });

    box.textContent = "";
    groups.forEach((list, category) => {
      box.append(el("h3", "category", category));
      const ul = el("ul", "service-list");

      list.forEach((service) => {
        const button = choiceButton("service", `service-${service.id}`, () => {
          markSelected(box, button);
          chooseService(service);
        });
        button.append(
          el("span", "service__name", service.name),
          el("span", "service__meta", `${service.durationMin} мин`),
          el("span", "service__price", formatPrice(service.price)),
        );
        const li = el("li");
        li.append(button);
        ul.append(li);
      });

      box.append(ul);
    });
  } catch (e) {
    box.textContent = "Не удалось загрузить список услуг. Обновите страницу.";
  }
}

function chooseService(service) {
  state.service = service;
  state.master = state.date = state.time = null;
  showStep("step-master");
  loadMasters();
}

/* ---------- Шаг 2: мастер ---------- */

async function loadMasters() {
  const box = $("masters");
  box.textContent = "Загружаем мастеров…";
  try {
    const masters = await api.getMasters(state.service.id);
    box.textContent = "";

    if (masters.length === 0) {
      box.append(
        el(
          "p",
          "hint",
          "Сейчас эту услугу никто не выполняет. Выберите другую.",
        ),
      );
      return;
    }

    masters.forEach((master) => {
      const button = choiceButton("option", `master-${master.id}`, () => {
        markSelected(box, button);
        chooseMaster(master);
      });
      button.append(
        el("span", null, master.fullName),
        el("small", null, master.specialization),
      );
      box.append(button);
    });
  } catch (e) {
    box.textContent = e.message;
  }
}

function chooseMaster(master) {
  state.master = master;
  state.date = state.time = null;
  showStep("step-time");
  renderDays();
  $("slots").textContent = "";
  $("slots").append(el("p", "hint", "Выберите день."));
}

/* ---------- Шаг 3: день и время ---------- */

function renderDays() {
  const box = $("days");
  box.textContent = "";
  const today = new Date();

  // Две недели начиная с сегодняшнего дня
  for (let i = 0; i < 14; i++) {
    const day = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() + i,
    );
    const iso = toIsoDate(day);

    const button = choiceButton("option day", `day-${iso}`, () => {
      markSelected(box, button);
      chooseDate(iso);
    });
    button.append(
      el("small", null, day.toLocaleDateString("ru-RU", { weekday: "short" })),
      el(
        "span",
        null,
        day.toLocaleDateString("ru-RU", { day: "numeric", month: "short" }),
      ),
    );
    box.append(button);
  }
}

function chooseDate(iso) {
  state.date = iso;
  state.time = null;
  $("step-contacts").hidden = true;
  loadSlots();
}

async function loadSlots() {
  const box = $("slots");
  box.textContent = "Ищем свободное время…";
  try {
    const slots = await api.getSlots(
      state.master.id,
      state.service.id,
      state.date,
    );
    box.textContent = "";

    if (slots.length === 0) {
      box.append(
        el(
          "p",
          "hint",
          "В этот день свободного времени нет. Выберите другой день.",
        ),
      );
      return;
    }

    slots.forEach((slot) => {
      const time = slot.start.slice(0, 5); // "10:00:00" → "10:00"
      const button = choiceButton("option", `slot-${time}`, () => {
        markSelected(box, button);
        chooseTime(time);
      });
      button.textContent = time;
      box.append(button);
    });
  } catch (e) {
    box.textContent = e.message;
  }
}

function chooseTime(time) {
  state.time = time;
  $("summary").textContent =
    `${state.service.name} у мастера ${state.master.fullName}, ` +
    `${formatDate(state.date)}, в ${time}. Стоимость: ${formatPrice(state.service.price)}.`;
  $("form-error").hidden = true;
  showStep("step-contacts");
}

/* ---------- Шаг 4: отправка ---------- */

$("contact-form").addEventListener("submit", async (event) => {
  event.preventDefault(); // не перезагружать страницу
  const form = event.target;
  const button = $("submit-btn");
  const errorBox = $("form-error");

  errorBox.hidden = true;
  button.disabled = true;
  button.textContent = "Записываем…";

  try {
    const appointment = await api.createAppointment({
      masterId: state.master.id,
      serviceId: state.service.id,
      startAt: `${state.date}T${state.time}`,
      clientName: form.clientName.value,
      clientPhone: form.clientPhone.value,
      clientEmail: form.clientEmail.value,
    });
    showDone(appointment);
  } catch (e) {
    if (e.status === 409) {
      // Время уже заняли: возвращаем к выбору времени со свежим списком.
      // Текст ошибки целиком формирует сервер — показываем его как есть
      $("step-contacts").hidden = true;
      await loadSlots();
      $("slots").prepend(el("p", "error", e.message));
      $("step-time").scrollIntoView({
        behavior: scrollBehavior,
        block: "start",
      });
    } else {
      errorBox.textContent = e.message;
      errorBox.hidden = false;
    }
  } finally {
    button.disabled = false;
    button.textContent = "Записаться";
  }
});

/* ---------- Готово ---------- */

function showDone(appointment) {
  ["step-service", ...STEPS].forEach((id) => {
    $(id).hidden = true;
  });
  renderDetails($("done-details"), appointment);
  $("cancel-link").href = `cancel.html?token=${appointment.cancelToken}`;
  $("step-done").hidden = false;
  $("step-done").scrollIntoView({ behavior: scrollBehavior, block: "start" });
}

$("restart-btn").addEventListener("click", () => location.reload());

loadServices();