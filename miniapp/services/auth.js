const { request, uploadFile } = require('./request');

function login(code) {
  return request('/auth/login', 'POST', { code }, {
    auth: false,
    retry401: false
  });
}

function getProfile() {
  return request('/users/me');
}

function updateProfile(data) {
  return request('/users/me', 'PUT', data);
}

function uploadAvatar(filePath) {
  return uploadFile('/upload/avatar', filePath);
}

function getGoals() { return request('/users/me/goals'); }
function updateGoals(data) { return request('/users/me/goals', 'PUT', data); }
function getProfileSummary() { return request('/users/me/summary'); }
function deleteAccount() {
  return request('/users/me', 'DELETE', {}, { headers: { 'X-Delete-Confirmation': 'DELETE' }, retry401: false });
}

module.exports = {
  deleteAccount, getGoals, getProfile, getProfileSummary, login, updateGoals, updateProfile, uploadAvatar
};
