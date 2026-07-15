const { request } = require('./request');

function addRecord(data, clientRequestId) {
  return request('/records', 'POST', data, {
    headers: clientRequestId ? { 'X-Idempotency-Key': clientRequestId } : {}
  });
}

function getRecords(date, mealType) {
  const filter = mealType ? '&mealType=' + encodeURIComponent(mealType) : '';
  return request('/records?date=' + encodeURIComponent(date) + filter);
}

function updateRecord(id, data) {
  return request('/records/' + id, 'PUT', data);
}

function getRecordsByRange(start, end) {
  return request('/records/range?start=' + start + '&end=' + end);
}

function deleteRecord(id) {
  return request('/records/' + id, 'DELETE');
}

function getDailyStats(date) {
  return request('/records/stats/daily?date=' + date);
}

function getWeeklyStats(date) {
  return request('/records/stats/weekly?date=' + date);
}

module.exports = {
  addRecord,
  deleteRecord,
  getDailyStats,
  getRecords,
  getRecordsByRange,
  getWeeklyStats,
  updateRecord
};
