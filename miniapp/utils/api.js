// 真机调试时改为电脑的局域网 IP，如 http://192.168.x.x:8080/api
const BASE_URL = 'http://10.112.71.7:8080/api';

function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: { 'Content-Type': 'application/json' },
      success: res => resolve(res.data),
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
