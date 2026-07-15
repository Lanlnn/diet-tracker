const MEAL_TYPE_MAP = {
  breakfast: { label: '早餐', class: 'tag-breakfast' },
  lunch: { label: '午餐', class: 'tag-lunch' },
  dinner: { label: '晚餐', class: 'tag-dinner' },
  snack: { label: '加餐', class: 'tag-snack' }
};

const MEAL_TYPE_OPTIONS = Object.keys(MEAL_TYPE_MAP).map(value => ({
  value,
  label: MEAL_TYPE_MAP[value].label
}));

module.exports = { MEAL_TYPE_MAP, MEAL_TYPE_OPTIONS };
