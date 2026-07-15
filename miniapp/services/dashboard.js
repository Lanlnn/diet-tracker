const { request } = require('./request');

function getTodayDashboard(date) {
  return request('/dashboard/today?date=' + encodeURIComponent(date));
}

module.exports = { getTodayDashboard };
