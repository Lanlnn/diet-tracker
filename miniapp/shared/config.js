const API_URLS = Object.freeze({
  develop: 'http://192.168.3.25:8080/api',
  trial: 'http://192.168.3.25:8080/api',
  release: 'https://tigercloud.asia/api'
});

const LOCAL_API_URL_STORAGE_KEY = 'apiBaseUrl';

function getEnvVersion() {
  try {
    return wx.getAccountInfoSync().miniProgram.envVersion || 'develop';
  } catch (error) {
    return 'develop';
  }
}

const ENV_VERSION = getEnvVersion();

function normalizeApiUrl(value) {
  if (typeof value !== 'string') return '';
  const normalized = value.trim().replace(/\/+$/, '');
  return /^https?:\/\//.test(normalized) ? normalized : '';
}

function getLocalOverride() {
  try {
    return normalizeApiUrl(wx.getStorageSync(LOCAL_API_URL_STORAGE_KEY));
  } catch (error) {
    return '';
  }
}

function resolveBaseUrl(envVersion = ENV_VERSION) {
  const override = getLocalOverride();
  if (override && envVersion !== 'release') return override;
  return API_URLS[envVersion] || API_URLS.develop;
}

const BASE_URL = resolveBaseUrl();

module.exports = {
  API_URLS,
  BASE_URL,
  ENV_VERSION,
  LOCAL_API_URL_STORAGE_KEY,
  normalizeApiUrl,
  resolveBaseUrl
};
