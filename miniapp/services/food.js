const { request } = require('./request');

function getCategories() {
  return request('/foods/categories');
}

function getFoods(categoryId) {
  const query = categoryId ? '?categoryId=' + categoryId : '';
  return request('/foods' + query);
}

function addFoodItem(data) {
  return request('/foods', 'POST', data);
}

function searchFood(keyword) {
  return request('/foods/search?keyword=' + encodeURIComponent(keyword));
}

module.exports = { addFoodItem, getCategories, getFoods, searchFood };
