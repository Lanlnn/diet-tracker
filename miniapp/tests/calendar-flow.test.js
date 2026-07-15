const assert = require('node:assert/strict');
const Module = require('node:module');
const actualDates = require('../shared/date');

let pageDefinition;
const summaryRequests = [];
const detailRequests = [];
const api = {
  getCalendarSummary(month) {
    return new Promise((resolve, reject) => summaryRequests.push({ month, resolve, reject }));
  },
  getTodayDashboard(date) {
    return new Promise((resolve, reject) => detailRequests.push({ date, resolve, reject }));
  }
};
const dates = { ...actualDates, getToday: () => '2026-07-15' };

const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../../services/index') return api;
  if (request === '../../../shared/date') return dates;
  return originalLoad.call(this, request, parent, isMain);
};
global.wx = {
  showToast() {},
  navigateTo() {},
  navigateBack() {}
};
global.Page = definition => { pageDefinition = definition; };
require('../packageFood/pages/calendar/calendar');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = patch => Object.entries(patch).forEach(([key, value]) => { page.data[key] = value; });
  return page;
}

function monthResponse(month, recordedDay = 13) {
  const monthDate = actualDates.parseMonth(month);
  const count = new Date(monthDate.getFullYear(), monthDate.getMonth() + 1, 0).getDate();
  return {
    month,
    goalCalories: 1800,
    goalSource: 'USER',
    days: Array.from({ length: count }, (_, index) => {
      const date = month + '-' + String(index + 1).padStart(2, '0');
      const recorded = index + 1 === recordedDay;
      return {
        date,
        intakeCalories: recorded ? 1240 : 0,
        exerciseCalories: recorded ? 380 : 0,
        remainingCalories: recorded ? 560 : 1800,
        mealCount: recorded ? 2 : 0,
        hasRecord: recorded
      };
    })
  };
}

function dashboardResponse() {
  return {
    meals: [
      { type: 'breakfast', label: '早餐', itemCount: 1, calories: 420, previewItems: ['燕麦酸奶'] },
      { type: 'lunch', label: '午餐', itemCount: 1, calories: 820, previewItems: ['鸡胸肉'] }
    ]
  };
}

(async () => {
  const page = createPage();
  const initial = page.onLoad();
  assert.equal(summaryRequests.length, 1);
  assert.equal(summaryRequests[0].month, '2026-07');
  summaryRequests[0].resolve(monthResponse('2026-07', 15));
  await Promise.resolve();
  assert.equal(detailRequests.length, 1);
  assert.equal(detailRequests[0].date, '2026-07-15');
  detailRequests[0].resolve(dashboardResponse());
  await initial;

  assert.equal(page.data.pageState, 'success');
  assert.equal(page.data.calendarDays.length, 42);
  assert.equal(page.data.calendarDays[0].date, '2026-06-29');
  assert.equal(page.data.intakeLabel, '1,240');
  assert.equal(page.data.exerciseLabel, '380');
  assert.equal(page.data.remainingLabel, '560');
  assert.equal(page.data.meals.length, 2);

  const summaryCountBeforeSwitch = summaryRequests.length;
  const detailCountBeforeSwitch = detailRequests.length;
  const previous = page.previousMonth();
  assert.equal(summaryRequests.length, summaryCountBeforeSwitch + 1, '切换月份只应发起一次月度摘要请求');
  assert.equal(summaryRequests.at(-1).month, '2026-06');
  assert.equal(detailRequests.length, detailCountBeforeSwitch, '切换月份不应逐日请求详情');
  summaryRequests.at(-1).resolve(monthResponse('2026-06'));
  await previous;
  assert.equal(page.data.displayedMonth, '2026-06');
  assert.equal(page.data.selectedDate, '2026-06-01');

  const select = page.selectDay({ currentTarget: { dataset: { date: '2026-06-13' } } });
  assert.equal(summaryRequests.length, summaryCountBeforeSwitch + 1, '同月选日不应重新请求月摘要');
  assert.equal(detailRequests.at(-1).date, '2026-06-13');
  detailRequests.at(-1).resolve(dashboardResponse());
  await select;
  assert.equal(page.data.intakeLabel, '1,240');
  assert.equal(page.data.mealCountLabel, '2');

  const retainedMonth = page.data.displayedMonth;
  const failedSwitch = page.previousMonth();
  summaryRequests.at(-1).reject(new Error('网络繁忙'));
  await failedSwitch;
  assert.equal(page.data.pageState, 'success', '月份刷新失败应保留已有日历');
  assert.equal(page.data.displayedMonth, retainedMonth);
  assert.equal(page.data.errorMessage, '网络繁忙');

  console.log('calendar flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
