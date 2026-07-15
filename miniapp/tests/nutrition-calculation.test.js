const assert = require('node:assert/strict');
const { calculateNutrition, validateAmount } = require('../shared/nutrition');

const chicken = {
  baseAmount: 100,
  baseUnit: 'g',
  calories: 165,
  protein: 31,
  fat: 3.6,
  carbs: 0
};

const result = calculateNutrition(chicken, '150');
assert.equal(result.error, '');
assert.equal(result.calories, 248, '165 千卡/100g × 150g 应四舍五入为 248 千卡');
assert.equal(result.protein, 46.5, '31g/100g × 150g 应为 46.5g');
assert.equal(result.fat, 5.4);
assert.equal(result.carbs, 0);

assert.equal(validateAmount(''), '请输入食用重量');
assert.equal(validateAmount('0'), '食用重量不能小于 1g');
assert.equal(validateAmount('-1'), '食用重量不能小于 1g');
assert.equal(validateAmount('10000.1'), '食用重量不能超过 10000g');
assert.equal(validateAmount('1.11'), '食用重量最多保留 1 位小数');
assert.equal(validateAmount('1.0'), '');
assert.equal(validateAmount('10000'), '');

assert.match(calculateNutrition({ ...chicken, baseUnit: '份' }, 150).error, /营养基准/);
assert.match(calculateNutrition({ ...chicken, protein: -1 }, 150).error, /营养数据/);
assert.match(calculateNutrition({ ...chicken, fat: null }, 150).error, /营养数据/);

console.log('nutrition calculation tests passed');
