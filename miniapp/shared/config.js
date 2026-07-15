const API_URLS = Object.freeze({
  develop: 'http://127.0.0.1:8080/api',
  trial: 'https://staging.tigercloud.asia/api',
  release: 'https://tigercloud.asia/api'
});

function getEnvVersion() {
  try {
    return wx.getAccountInfoSync().miniProgram.envVersion || 'develop';
  } catch (error) {
    return 'develop';
  }
}

const ENV_VERSION = getEnvVersion();
const BASE_URL = API_URLS[ENV_VERSION] || API_URLS.develop;

module.exports = { API_URLS, BASE_URL, ENV_VERSION };
