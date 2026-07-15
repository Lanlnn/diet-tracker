const { request } = require('./request');

function addRecord(data) {
  return request('/records', 'POST', data);
}

function getRecords(date) {
  return request('/records?date=' + date);
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
  getWeeklyStats
};
