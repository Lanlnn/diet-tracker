function formatDate(date) {
  const y = date.getFullYear();
  const m = ('0' + (date.getMonth() + 1)).slice(-2);
  const d = ('0' + date.getDate()).slice(-2);
  return `${y}-${m}-${d}`;
}

function getToday() {
  return formatDate(new Date());
}

function formatMonth(date) {
  const y = date.getFullYear();
  const m = ('0' + (date.getMonth() + 1)).slice(-2);
  return `${y}-${m}`;
}

function parseDate(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value || '');
  if (!match) return null;
  const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
  return formatDate(date) === value ? date : null;
}

function parseMonth(value) {
  const match = /^(\d{4})-(\d{2})$/.exec(value || '');
  if (!match) return null;
  const date = new Date(Number(match[1]), Number(match[2]) - 1, 1);
  return formatMonth(date) === value ? date : null;
}

function addMonths(month, offset) {
  const date = parseMonth(month);
  if (!date) return '';
  date.setMonth(date.getMonth() + offset);
  return formatMonth(date);
}

function buildMonthGrid(month) {
  const first = parseMonth(month);
  if (!first) return [];
  const mondayOffset = (first.getDay() + 6) % 7;
  const cursor = new Date(first);
  cursor.setDate(1 - mondayOffset);
  return Array.from({ length: 42 }, (_, index) => {
    const current = new Date(cursor);
    current.setDate(cursor.getDate() + index);
    return {
      date: formatDate(current),
      dayNumber: current.getDate(),
      inMonth: current.getMonth() === first.getMonth() && current.getFullYear() === first.getFullYear()
    };
  });
}

function getWeekStart(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
  return formatDate(d);
}

module.exports = {
  formatDate,
  formatMonth,
  parseDate,
  parseMonth,
  addMonths,
  buildMonthGrid,
  getToday,
  getWeekStart
};
