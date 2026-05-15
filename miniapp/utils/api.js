// 开发者工具模拟器用 localhost，真机调试时改为电脑的局域网 IP
const BASE_URL = 'http://192.168.1.100:8080/api';

function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data: (method === 'POST' || method === 'PUT') && typeof data === 'object' ? JSON.stringify(data) : data,
      header: { 'Content-Type': 'application/json' },
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          reject(new Error('请求失败: ' + res.statusCode));
        }
      },
      fail: err => {
        wx.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = {
  // 食物分类
  getCategories: () => request('/foods/categories'),
  getFoods: (categoryId) => request('/foods' + (categoryId ? '?categoryId=' + categoryId : '')),
  // 自定义食物
  addFoodItem: (data) => request('/foods', 'POST', data),
  searchFood: (keyword) => request('/foods/search?keyword=' + encodeURIComponent(keyword)),

  // 饮食记录
  addRecord: (data) => request('/records', 'POST', data),
  getRecords: (date) => request('/records?date=' + date),
  getRecordsByRange: (start, end) => request('/records/range?start=' + start + '&end=' + end),
  deleteRecord: (id) => request('/records/' + id, 'DELETE'),

  // 统计
  getDailyStats: (date) => request('/records/stats/daily?date=' + date),
  getWeeklyStats: (date) => request('/records/stats/weekly?date=' + date),

  BASE_URL
};
