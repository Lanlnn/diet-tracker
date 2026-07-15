const { BASE_URL } = require('../shared/config');

let currentToken = '';

function setToken(token) {
  currentToken = token || '';
}

function getToken() {
  return currentToken;
}

function request(url, method = 'GET', data = {}, options = {}) {
  const useAuth = options.auth !== false;
  const retry401 = options.retry401 !== false;

  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (useAuth && currentToken) {
      header.Authorization = 'Bearer ' + currentToken;
    }

    wx.request({
      url: BASE_URL + url,
      method,
      data: (method === 'POST' || method === 'PUT') && typeof data === 'object'
        ? JSON.stringify(data)
        : data,
      header,
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
          return;
        }

        if (res.statusCode === 401 && useAuth && retry401) {
          retryAfterLogin(url, method, data, options).then(resolve).catch(reject);
          return;
        }

        reject(new Error('Request failed: ' + res.statusCode));
      },
      fail: err => {
        wx.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
}

function retryAfterLogin(url, method, data, options) {
  const app = getApp();
  if (!app || !app.reLogin) {
    return Promise.reject(new Error('Unauthorized'));
  }

  return app.reLogin().then(() => request(url, method, data, {
    ...options,
    retry401: false
  }));
}

function uploadFile(url, filePath, name = 'file') {
  return new Promise((resolve, reject) => {
    const header = {};
    if (currentToken) {
      header.Authorization = 'Bearer ' + currentToken;
    }

    wx.uploadFile({
      url: BASE_URL + url,
      filePath,
      name,
      header,
      success: res => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error('Upload failed: ' + res.statusCode));
          return;
        }

        try {
          resolve(JSON.parse(res.data));
        } catch (error) {
          reject(new Error('Invalid response'));
        }
      },
      fail: reject
    });
  });
}

module.exports = { BASE_URL, getToken, request, setToken, uploadFile };
