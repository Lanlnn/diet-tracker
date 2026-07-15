const { BASE_URL } = require('../shared/config');

let currentToken = '';

function setToken(token) { currentToken = token || ''; }
function getToken() { return currentToken; }

function request(path, method = 'GET', data = {}, options = {}) {
  const useAuth = options.auth !== false;
  const loginReady = useAuth ? getApp().ensureLogin() : Promise.resolve();
  return loginReady.then(() => send(path, method, data, useAuth)).catch(error => {
    if (error && error.statusCode === 401 && useAuth && options.retry401 !== false) {
      return getApp().reLogin().then(() => send(path, method, data, true));
    }
    return Promise.reject(error);
  });
}

function send(path, method, data, useAuth) {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (useAuth && currentToken) header.Authorization = 'Bearer ' + currentToken;
    wx.request({
      url: BASE_URL + path,
      method,
      data,
      header,
      success: response => {
        if (response.statusCode >= 200 && response.statusCode < 300) return resolve(response.data);
        const error = new Error((response.data && response.data.message) || '请求失败');
        error.statusCode = response.statusCode;
        error.code = response.data && response.data.code;
        error.requestId = response.data && response.data.requestId;
        error.fieldErrors = response.data && response.data.fieldErrors;
        reject(error);
      },
      fail: () => {
        const error = new Error('网络连接失败');
        error.code = 'NETWORK_ERROR';
        reject(error);
      }
    });
  });
}

function uploadFile(path, filePath, name = 'file', retry401 = true) {
  return getApp().ensureLogin().then(() => new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + path,
      filePath,
      name,
      header: { Authorization: 'Bearer ' + currentToken },
      success: response => {
        let body = {};
        try { body = JSON.parse(response.data || '{}'); } catch (error) {}
        if (response.statusCode >= 200 && response.statusCode < 300) return resolve(body);
        const failure = new Error(body.message || '上传失败');
        failure.statusCode = response.statusCode;
        failure.code = body.code;
        failure.requestId = body.requestId;
        failure.fieldErrors = body.fieldErrors;
        reject(failure);
      },
      fail: () => reject(new Error('网络连接失败'))
    });
  })).catch(error => {
    if (error && error.statusCode === 401 && retry401) {
      return getApp().reLogin().then(() => uploadFile(path, filePath, name, false));
    }
    return Promise.reject(error);
  });
}

module.exports = { BASE_URL, getToken, request, setToken, uploadFile };
