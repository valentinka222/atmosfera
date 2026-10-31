// Создать элемент с классом и текстом.
// textContent, а не innerHTML: данные с сервера вставляются как текст,
// поэтому чужой HTML-код в имени клиента не выполнится
export function el(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

export const formatPrice = (price) =>
  new Intl.NumberFormat("ru-RU").format(price) + " ₽";

// Дата в формате 2026-11-02 по местному времени.
// toISOString() не подходит: он переводит в UTC, и ночью дата «съезжает» на день
export const toIsoDate = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

// 2026-11-02 → «понедельник, 2 ноября»
export const formatDate = (isoDate) =>
  new Date(isoDate + "T00:00").toLocaleDateString("ru-RU", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });

export const STATUS_NAMES = {
  BOOKED: "Вы записаны",
  CANCELLED: "Отменена",
  COMPLETED: "Визит состоялся",
};

// Заполнить список «название — значение» данными записи
export function renderDetails(dl, appointment, withStatus = false) {
  const rows = [
    ["Услуга", appointment.serviceName],
    ["Мастер", appointment.masterName],
    [
      "Когда",
      `${formatDate(appointment.startAt.slice(0, 10))}, ${appointment.startAt.slice(11, 16)}`,
    ],
    ["Стоимость", formatPrice(appointment.price)],
  ];
  if (withStatus) rows.push(["Статус", STATUS_NAMES[appointment.status]]);

  dl.textContent = "";
  rows.forEach(([name, value]) =>
    dl.append(el("dt", null, name), el("dd", null, value)),
  );
  dl.hidden = false;
}
