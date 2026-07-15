const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
let resolveCalculation;
let calculationCalls = 0;
let recordCalls = 0;
let savedRequest;
let redirectedTo = '';
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
  },
  addRecord(payload, requestId) {
    recordCalls += 1;
    savedRequest = { payload, requestId };
    return Promise.resolve({ id: 99 });
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
  removeStorageSync(key) { delete storage[key]; },
  showToast() {},
  redirectTo(options) { redirectedTo = options.url; }
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
  page.onMealChange({ detail: { value: '1' } });

  page.submitDraft();
  page.submitDraft();
  assert.equal(calculationCalls, 1, '连续点击只能发出一次服务端复核请求');
  resolveCalculation();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(recordCalls, 1);
  assert.equal(savedRequest.payload.quantity, 150);
  assert.equal(savedRequest.payload.mealType, 'lunch');
  assert.match(savedRequest.requestId, /^meal-/);
  assert.equal(storage.m4PendingMealDraft, undefined, '保存成功后应清理待提交草稿');
  assert.equal(page.data.submitting, false);

  await new Promise(resolve => setTimeout(resolve, 450));
  assert.match(redirectedTo, /meal-detail\?date=.*&mealType=lunch/);

  page.onUnload();
  assert.equal(storage['m4CalculatorDraft:7'], undefined, '保存成功后不应重新写入已完成草稿');
  console.log('calorie calculator flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
