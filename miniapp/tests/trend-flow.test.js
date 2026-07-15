const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
const pending = [];
const api = {
  getTrend(range) {
    return new Promise((resolve, reject) => pending.push({ range, resolve, reject }));
  }
};

const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};
global.wx = { showToast() {} };
global.Page = definition => { pageDefinition = definition; };
require('../pages/stats/stats');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = patch => Object.entries(patch).forEach(([key, value]) => { page.data[key] = value; });
  page.onLoad();
  return page;
}

function response(range, days, value) {
  const startDay = range === '30d' ? 1 : 9;
  return {
    range,
    startDate: range === '30d' ? '2026-06-16' : '2026-07-09',
    endDate: '2026-07-15',
    averageNetIntake: value,
    averageIntake: value + 100,
    averageExercise: 100,
    netChangePercent: -6,
    nutritionAchievementRate: 75,
    accessibilitySummary: `趋势共 ${days} 天`,
    dailyData: Array.from({ length: days }, (_, index) => ({
      date: `2026-${range === '30d' && index < 15 ? '06' : '07'}-${String(range === '30d' && index < 15 ? 16 + index : startDay + index - (range === '30d' ? 15 : 0)).padStart(2, '0')}`,
      intakeCalories: value + 100,
      exerciseCalories: 100,
      netCalories: value,
      hasData: index >= days - 3
    })),
    summaries: [
      { type: 'intake-steady', title: '热量控制稳定', message: '继续保持完整记录。' },
      { type: 'exercise-steady', title: '运动习惯在积累', message: '保持节奏。' }
    ]
  };
}

(async () => {
  const page = createPage();
  const first = page.loadTrend();
  assert.equal(pending[0].range, '7d');

  const second = page.onRangeChange({ detail: { value: '30d' } });
  assert.equal(pending[1].range, '30d');
  pending[1].resolve(response('30d', 30, 1500));
  await second;
  assert.equal(page.data.selectedRange, '30d');
  assert.equal(page.data.pageState, 'success');
  assert.equal(page.data.averageNetLabel, '1,500');
  assert.equal(page.data.chartData.length, 30);
  assert.equal(page.data.summaries.length, 2);
  assert.match(page.data.chartData[29].ariaLabel, /净摄入 1,500 千卡/);

  pending[0].resolve(response('7d', 7, 900));
  await first;
  assert.equal(page.data.averageNetLabel, '1,500', '过期的 7 天响应不得覆盖 30 天结果');
  assert.equal(page.data.selectedRange, '30d');

  const refresh = page.loadTrend({ preserve: true });
  pending[2].reject(new Error('网络繁忙'));
  await refresh;
  assert.equal(page.data.pageState, 'success', '刷新失败时应保留当前成功数据');
  assert.equal(page.data.averageNetLabel, '1,500');
  assert.equal(page.data.errorMessage, '网络繁忙');

  console.log('trend flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
