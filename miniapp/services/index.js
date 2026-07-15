const auth = require('./auth');
const food = require('./food');
const record = require('./record');
const request = require('./request');

module.exports = {
  ...auth,
  ...food,
  ...record,
  BASE_URL: request.BASE_URL,
  getToken: request.getToken,
  setToken: request.setToken
};
