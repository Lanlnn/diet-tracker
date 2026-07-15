const MIN_AMOUNT = 1;
const MAX_AMOUNT = 10000;

function validateAmount(value) {
  const text = String(value == null ? '' : value).trim();
  if (!text) return '请输入食用重量';
  if (!/^-?\d+(\.\d)?$/.test(text)) return '食用重量最多保留 1 位小数';
  const amount = Number(text);
  if (amount < MIN_AMOUNT) return '食用重量不能小于 1g';
  if (amount > MAX_AMOUNT) return '食用重量不能超过 10000g';
  return '';
}

function calculateNutrition(food, value) {
  const error = validateAmount(value);
  if (error) return { error, amount: null, calories: 0, protein: 0, fat: 0, carbs: 0 };
  const baseAmount = Number(food && food.baseAmount);
  const fields = ['calories', 'protein', 'fat', 'carbs'];
  if (!Number.isFinite(baseAmount) || baseAmount <= 0 || String(food.baseUnit || '').toLowerCase() !== 'g') {
    return { error: '该食品缺少有效的每克营养基准', amount: null, calories: 0, protein: 0, fat: 0, carbs: 0 };
  }
  if (fields.some(field => food[field] == null || food[field] === '' || !Number.isFinite(Number(food[field])) || Number(food[field]) < 0)) {
    return { error: '该食品的营养数据不完整', amount: null, calories: 0, protein: 0, fat: 0, carbs: 0 };
  }
  const amount = Number(value);
  const ratio = amount / baseAmount;
  return {
    error: '',
    amount,
    calories: Math.round(Number(food.calories) * ratio),
    protein: roundOne(Number(food.protein) * ratio),
    fat: roundOne(Number(food.fat) * ratio),
    carbs: roundOne(Number(food.carbs) * ratio)
  };
}

function roundOne(value) {
  return Math.round((value + Number.EPSILON) * 10) / 10;
}

module.exports = { MAX_AMOUNT, MIN_AMOUNT, calculateNutrition, validateAmount };
