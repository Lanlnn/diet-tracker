const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
let updated;
let deletedId;
let redirectedTo = '';
const records = [
  {
    id: 1, mealType: 'lunch', quantity: 150, unit: 'g', note: '', recordTime: '2026-07-15T12:36:00',
    foodNameSnapshot: '鸡胸肉', baseAmountSnapshot: 100, baseUnitSnapshot: 'g',
    caloriesSnapshot: 165, proteinSnapshot: 31, fatSnapshot: 3.6, carbsSnapshot: 0
  },
  {
    id: 2, mealType: 'lunch', quantity: 250, unit: 'g', note: '', recordTime: '2026-07-15T12:37:00',
    foodNameSnapshot: '米饭', baseAmountSnapshot: 100, baseUnitSnapshot: 'g',
    caloriesSnapshot: 116, proteinSnapshot: 2.6, fatSnapshot: 0.3, carbsSnapshot: 25.9
  }
];
const api = {
  getRecords() { return Promise.resolve(records.filter(item => item.id !== deletedId)); },
  updateRecord(id, payload) { updated = { id, payload }; return Promise.resolve({}); },
  deleteRecord(id) { deletedId = id; return Promise.resolve(); }
};

const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};
global.wx = {
  showToast() {},
  redirectTo(options) { redirectedTo = options.url; },
  navigateBack() {}, switchTab() {}, navigateTo() {}, showActionSheet() {},
  showModal(options) { options.success({ confirm: true }); }
};
global.Page = definition => { pageDefinition = definition; };
require('../packageFood/pages/meal-detail/meal-detail');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = patch => Object.entries(patch).forEach(([key, value]) => { page.data[key] = value; });
  return page;
}

(async () => {
  const page = createPage();
  page.onLoad({ date: '2026-07-15', mealType: 'lunch' });
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(page.data.status, 'success');
  assert.equal(page.data.total.calories, 538);
  assert.equal(page.data.total.protein, 53);
  assert.equal(page.data.total.carbs, 64.8);
  assert.match(page.data.advice.title, /蛋白质/);

  page.toggleEdit();
  page.openEditor({ currentTarget: { dataset: { id: 1 } } });
  page.onAmountInput({ detail: { value: '180' } });
  page.onMealChange({ detail: { value: '2' } });
  page.saveEdit();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(updated.id, 1);
  assert.equal(updated.payload.quantity, 180);
  assert.equal(updated.payload.mealType, 'dinner');
  assert.match(redirectedTo, /mealType=dinner/);

  page.setData({ editorOpen: true, editingId: 2 });
  page.confirmDelete();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(deletedId, 2);
  assert.equal(page.data.records.length, 1);
  assert.equal(page.data.advice, null, '数据不足两项时不显示建议');
  console.log('meal detail flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
