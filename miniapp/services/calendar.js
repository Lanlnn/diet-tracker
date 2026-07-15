const { request } = require('./request');

function getCalendarSummary(month) {
  return request('/calendar/summary?month=' + encodeURIComponent(month));
}

module.exports = { getCalendarSummary };
