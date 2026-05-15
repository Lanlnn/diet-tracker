function formatDate(date) {
  const y = date.getFullYear();
  const m = ('0' + (date.getMonth() + 1)).slice(-2);
  const d = ('0' + date.getDate()).slice(-2);
  return `${y}-${m}-${d}`;
}

function getToday() {
  return formatDate(new Date());
}

function getWeekStart(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
  return formatDate(d);
}

const MEAL_TYPE_MAP = {
  breakfast: { label: '早餐', class: 'tag-breakfast' },
  lunch: { label: '午餐', class: 'tag-lunch' },
  dinner: { label: '晚餐', class: 'tag-dinner' },
  snack: { label: '加餐', class: 'tag-snack' }
};

module.exports = { formatDate, getToday, getWeekStart, MEAL_TYPE_MAP };
