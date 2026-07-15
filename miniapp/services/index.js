const auth = require('./auth');
const food = require('./food');
const record = require('./record');
const dashboard = require('./dashboard');
const request = require('./request');

module.exports = {
  ...auth,
  ...food,
  ...record,
  ...dashboard,
  BASE_URL: request.BASE_URL,
  getToken: request.getToken,
  setToken: request.setToken
};
