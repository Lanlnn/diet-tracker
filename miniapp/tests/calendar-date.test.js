const assert = require('node:assert/strict');
const dates = require('../shared/date');

const leapFebruary = dates.buildMonthGrid('2024-02');
assert.equal(leapFebruary.length, 42);
assert.equal(leapFebruary[0].date, '2024-01-29', '月历应从周一开始');
assert.equal(leapFebruary[41].date, '2024-03-10');
assert.equal(leapFebruary.filter(day => day.inMonth).length, 29, '闰年 2 月应有 29 天');

const commonFebruary = dates.buildMonthGrid('2025-02');
assert.equal(commonFebruary.filter(day => day.inMonth).length, 28, '平年 2 月应有 28 天');
assert.equal(commonFebruary[0].date, '2025-01-27');

const july = dates.buildMonthGrid('2026-07');
assert.equal(july.filter(day => day.inMonth).length, 31, '大月应有 31 天');
assert.equal(july[0].date, '2026-06-29');
assert.equal(july[41].date, '2026-08-09');

const april = dates.buildMonthGrid('2026-04');
assert.equal(april.filter(day => day.inMonth).length, 30, '小月应有 30 天');

assert.equal(dates.addMonths('2025-12', 1), '2026-01', '月份切换应正确跨年');
assert.equal(dates.addMonths('2026-01', -1), '2025-12');
assert.equal(dates.parseMonth('2026-7'), null, '月份必须严格使用两位数');

console.log('calendar date tests passed');
