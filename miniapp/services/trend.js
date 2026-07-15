const { request } = require('./request');

function getTrend(range) {
  return request('/stats/trend?range=' + encodeURIComponent(range));
}

module.exports = { getTrend };
