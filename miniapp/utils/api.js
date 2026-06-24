const BASE_URL = 'https://tigercloud.asia/api';
let currentToken = '';

function setToken(t) {
  currentToken = t;
}

function getToken() {
  return currentToken;
}

function request(url, method, data) {
  if (method === undefined) method = 'GET';
  if (data === undefined) data = {};
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (currentToken) {
      header['Authorization'] = 'Bearer ' + currentToken;
    }
    wx.request({
      url: BASE_URL + url,
      method: method,
      data: (method === 'POST' || method === 'PUT') && typeof data === 'object' ? JSON.stringify(data) : data,
      header: header,
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          // Token expired → 自动重登录后重试一次
          handle401(url, method, data, resolve, reject);
        } else {
          reject(new Error('Request failed: ' + res.statusCode));
        }
      },
      fail: err => {
        wx.showToast({ title: 'Network error', icon: 'none' });
        reject(err);
      }
    });
  });
}

/** 401 重试：重新登录后用新 token 重发原请求 */
function handle401(url, method, data, resolve, reject) {
  const app = getApp();
  if (!app || !app.reLogin) {
    reject(new Error('Unauthorized'));
    return;
  }

  app.reLogin()
    .then(() => {
      // 用新 token 重试
      const header = { 'Content-Type': 'application/json' };
      if (currentToken) {
        header['Authorization'] = 'Bearer ' + currentToken;
      }
      wx.request({
        url: BASE_URL + url,
        method: method,
        data: (method === 'POST' || method === 'PUT') && typeof data === 'object' ? JSON.stringify(data) : data,
        header: header,
        success: retryRes => {
          if (retryRes.statusCode >= 200 && retryRes.statusCode < 300) {
            resolve(retryRes.data);
          } else {
            reject(new Error('Request failed after re-login: ' + retryRes.statusCode));
          }
        },
        fail: err => reject(err)
      });
    })
    .catch(err => {
      wx.showToast({ title: 'Re-login failed', icon: 'none' });
      reject(err);
    });
}

module.exports = {
  setToken: setToken,
  getToken: getToken,
  getCategories: function() { return request('/foods/categories'); },
  getFoods: function(categoryId) { return request('/foods' + (categoryId ? '?categoryId=' + categoryId : '')); },
  addFoodItem: function(data) { return request('/foods', 'POST', data); },
  searchFood: function(keyword) { return request('/foods/search?keyword=' + encodeURIComponent(keyword)); },
  addRecord: function(data) { return request('/records', 'POST', data); },
  getRecords: function(date) { return request('/records?date=' + date); },
  getRecordsByRange: function(start, end) { return request('/records/range?start=' + start + '&end=' + end); },
  deleteRecord: function(id) { return request('/records/' + id, 'DELETE'); },
  getDailyStats: function(date) { return request('/records/stats/daily?date=' + date); },
  getWeeklyStats: function(date) { return request('/records/stats/weekly?date=' + date); },
  getProfile: function() { return request('/auth/profile', 'GET'); },
  updateProfile: function(data) { return request('/auth/profile', 'PUT', data); },
   uploadAvatar: function(filePath) {
     return new Promise((resolve, reject) => {
       const header = { 'Authorization': 'Bearer ' + currentToken };
       wx.uploadFile({
         url: BASE_URL + '/upload/avatar',
         filePath: filePath,
         name: 'file',
         header: header,
         success: res => {
           if (res.statusCode === 200) {
             try {
               resolve(JSON.parse(res.data));
             } catch (e) {
               reject(new Error('Invalid response'));
             }
          } else {
             reject(new Error('Upload failed: ' + res.statusCode));
           }
         },
         fail: err => reject(err)
       });
     });
   },
  BASE_URL: BASE_URL
};
