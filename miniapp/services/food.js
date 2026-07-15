const { request } = require('./request');

function getCategories() {
  return request('/foods/categories');
}

function getFoods(options = {}) {
  const query = [
    'scope=' + encodeURIComponent(options.scope || 'common'),
    'page=' + (options.page || 0),
    'size=' + (options.size || 20)
  ];
  if (options.categoryId) query.push('categoryId=' + options.categoryId);
  return request('/foods?' + query.join('&'));
}

function addFoodItem(data) {
  return request('/foods', 'POST', data);
}

function searchFood(keyword, page = 0, size = 20) {
  return request('/foods/search?keyword=' + encodeURIComponent(keyword) + '&page=' + page + '&size=' + size);
}

function setFoodFavorite(foodId, favorite) {
  return request('/foods/' + foodId + '/favorite', 'PUT', { favorite });
}

module.exports = { addFoodItem, getCategories, getFoods, searchFood, setFoodFavorite };
