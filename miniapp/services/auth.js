const { request, uploadFile } = require('./request');

function login(code) {
  return request('/auth/login', 'POST', { code }, {
    auth: false,
    retry401: false
  });
}

function getProfile() {
  return request('/auth/profile');
}

function updateProfile(data) {
  return request('/auth/profile', 'PUT', data);
}

function uploadAvatar(filePath) {
  return uploadFile('/upload/avatar', filePath);
}

module.exports = { getProfile, login, updateProfile, uploadAvatar };
