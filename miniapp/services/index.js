const auth = require('./auth');
const food = require('./food');
const record = require('./record');
const dashboard = require('./dashboard');
const exercise = require('./exercise');
const request = require('./request');

module.exports = {
  ...auth,
  ...food,
  ...record,
  ...dashboard,
  ...exercise,
  BASE_URL: request.BASE_URL,
  getToken: request.getToken,
  setToken: request.setToken
};
