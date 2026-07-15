const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
const searches = [];
const api = {
  getFoods() { return Promise.resolve({ items: [], page: 0, hasMore: false }); },
  searchFood(keyword) {
    return new Promise((resolve, reject) => searches.push({ keyword, resolve, reject }));
  },
  setFoodFavorite() { return Promise.resolve({ favorite: true }); },
  addFoodItem() { return Promise.resolve({ id: 99 }); }
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
require('../pages/add/add');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = (patch, callback) => {
    Object.entries(patch).forEach(([key, value]) => {
      const path = key.split('.');
      let target = page.data;
      while (path.length > 1) target = target[path.shift()];
      target[path[0]] = value;
    });
    if (callback) callback();
  };
  page._requestVersion = 0;
  return page;
}

(async () => {
  const page = createPage();

  page.setData({ keyword: '鸡' });
  page.loadFoods(true);
  page.setData({ keyword: '鸡胸' });
  page.loadFoods(true);
  assert.equal(searches.length, 2);

  searches[1].resolve({
    items: [{ id: 2, name: '鸡胸肉', calories: 165, baseAmount: 100, baseUnit: 'g' }],
    page: 0,
    hasMore: false
  });
  await new Promise(resolve => setImmediate(resolve));
  searches[0].resolve({
    items: [{ id: 1, name: '鸡蛋', calories: 144, baseAmount: 100, baseUnit: 'g' }],
    page: 0,
    hasMore: false
  });
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(page.data.foods[0].name, '鸡胸肉', '旧搜索响应不能覆盖新关键词结果');

  let debounceCalls = 0;
  const debouncePage = createPage();
  debouncePage.loadFoods = () => { debounceCalls += 1; };
  debouncePage.onSearchInput({ detail: { value: '鸡' } });
  debouncePage.onSearchInput({ detail: { value: '鸡胸' } });
  assert.equal(debounceCalls, 0, '输入过程中不能立即请求');
  await new Promise(resolve => setTimeout(resolve, 330));
  assert.equal(debounceCalls, 1, '连续输入应只在 300ms 后查询一次');

  page.onUnload();
  debouncePage.onUnload();
  console.log('food search flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
