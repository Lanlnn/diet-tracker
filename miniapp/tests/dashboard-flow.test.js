const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
let dashboardResult;
let dashboardError;
let requestCount = 0;
const api = {
  getTodayDashboard() {
    requestCount += 1;
    return dashboardError ? Promise.reject(dashboardError) : Promise.resolve(dashboardResult);
  }
};

const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};
global.wx = {
  showToast() {}, showModal() {}, switchTab() {}, navigateTo() {}
};
global.Page = definition => { pageDefinition = definition; };
require('../pages/index/index');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = patch => Object.entries(patch).forEach(([key, value]) => { page.data[key] = value; });
  return page;
}

function response(overrides = {}) {
  return {
    goalCalories: 1800,
    intakeCalories: 1240,
    remainingCalories: 560,
    exceededCalories: 0,
    exerciseCalories: 0,
    nutrition: {
      carbs: { amount: 142, goal: 209, progressPercent: 68 },
      protein: { amount: 86, goal: 105, progressPercent: 82 },
      fat: { amount: 41, goal: 69, progressPercent: 59 }
    },
    exercise: { state: 'empty', completedCount: 0, durationMinutes: 0, caloriesBurned: 0 },
    meals: [
      { type: 'breakfast', label: '早餐', itemCount: 1, calories: 380, previewItems: ['鸡蛋'] },
      { type: 'lunch', label: '午餐', itemCount: 1, calories: 560, previewItems: ['鸡胸肉'] },
      { type: 'dinner', label: '晚餐', itemCount: 1, calories: 300, previewItems: ['西兰花'] },
      { type: 'snack', label: '加餐', itemCount: 0, calories: 0, previewItems: [] }
    ],
    advice: { title: '保持当前节奏', message: '继续完整记录今日饮食' },
    ...overrides
  };
}

(async () => {
  dashboardResult = response();
  const page = createPage();
  page.onLoad();
  await page.loadData();

  assert.equal(requestCount, 1, '首页应只发起一次业务请求');
  assert.equal(page.data.pageState, 'success');
  assert.equal(page.data.remainingLabel, '560');
  assert.equal(page.data.intakeLabel, '1,240');
  assert.equal(page.data.ringDegrees, 248);
  assert.equal(page.data.recordedMealCount, 3);
  assert.equal(page.data.exerciseState, 'empty');
  assert.equal(page.data.nutritionItems[1].amountLabel, '86g');

  page.presentDashboard(response({
    intakeCalories: 1980,
    remainingCalories: 0,
    exceededCalories: 180
  }));
  assert.equal(page.data.remainingLabel, '0');
  assert.equal(page.data.exceededLabel, '180');
  assert.match(page.data.calorieStatus, /超出 180/);

  page.presentDashboard(response({ nutrition: null }));
  assert.equal(page.data.nutritionState, 'error');
  assert.equal(page.data.exerciseState, 'empty');
  assert.equal(page.data.mealsState, 'success');
  assert.equal(page.data.recordedMealCount, 3, '单个模块缺失不应清空餐次摘要');

  page.presentDashboard(response());
  dashboardError = new Error('网络繁忙');
  await page.loadData({ preserve: true });
  assert.equal(page.data.pageState, 'success', '刷新失败应保留已成功内容');
  assert.equal(page.data.remainingLabel, '560');
  assert.equal(page.data.errorMessage, '网络繁忙');

  console.log('dashboard flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
