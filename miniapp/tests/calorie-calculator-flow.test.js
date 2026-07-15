const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
let resolveCalculation;
let calculationCalls = 0;
const storage = {};
const food = {
  id: 7,
  name: '鸡胸肉',
  baseAmount: 100,
  baseUnit: 'g',
  servingAmount: null,
  servingUnit: null,
  calories: 165,
  protein: 31,
  fat: 3.6,
  carbs: 0
};
const api = {
  getFood() { return Promise.resolve(food); },
  calculateFood(id, amount) {
    calculationCalls += 1;
    return new Promise(resolve => {
      resolveCalculation = () => resolve({
        foodId: id, amount, unit: 'g', calories: 248, protein: 46.5, fat: 5.4, carbs: 0
      });
    });
  }
};

const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};

global.wx = {
  getStorageSync(key) { return storage[key]; },
  setStorageSync(key, value) { storage[key] = value; },
  showToast() {},
  navigateBack() {}
};
global.Page = definition => { pageDefinition = definition; };
require('../packageFood/pages/calorie-calculator/calorie-calculator');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = (patch, callback) => {
    Object.entries(patch).forEach(([key, value]) => { page.data[key] = value; });
    if (callback) callback();
  };
  return page;
}

(async () => {
  const page = createPage();
  page.onLoad({ id: '7' });
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(page.data.status, 'success');
  assert.equal(page.data.nutrition.calories, 248);
  assert.equal(page.data.nutrition.protein, 46.5);
  assert.equal(page.data.food.servingAmount, null, '缺少 servingAmount 时一份快捷值应保持不可用');

  page.onAmountInput({ detail: { value: '1.11' } });
  assert.match(page.data.amountError, /最多保留 1 位小数/);
  page.onAmountInput({ detail: { value: '150' } });

  page.submitDraft();
  page.submitDraft();
  assert.equal(calculationCalls, 1, '连续点击只能发出一次服务端复核请求');
  resolveCalculation();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(storage.m4PendingMealDraft.amount, 150);
  assert.equal(storage.m4PendingMealDraft.nutrition.calories, 248);
  assert.equal(page.data.submitting, false);

  page.onUnload();
  assert.equal(storage['m4CalculatorDraft:7'].amount, '150', '离开页面后应保留合理草稿状态');
  console.log('calorie calculator flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
