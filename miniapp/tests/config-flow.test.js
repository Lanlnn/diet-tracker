const assert = require('node:assert/strict');
const test = require('node:test');

function loadConfig({ envVersion = 'develop', override = '' } = {}) {
  global.wx = {
    getAccountInfoSync: () => ({ miniProgram: { envVersion } }),
    getStorageSync: key => key === 'apiBaseUrl' ? override : ''
  };
  const modulePath = require.resolve('../shared/config');
  delete require.cache[modulePath];
  return require(modulePath);
}

test('develop and trial default to the frozen LAN backend', () => {
  assert.equal(loadConfig().BASE_URL, 'http://192.168.3.25:8080/api');
  assert.equal(loadConfig({ envVersion: 'trial' }).BASE_URL, 'http://192.168.3.25:8080/api');
});

test('a local storage override supports a changed LAN address', () => {
  const config = loadConfig({ override: ' http://192.168.1.20:8080/api/ ' });
  assert.equal(config.BASE_URL, 'http://192.168.1.20:8080/api');
});

test('release ignores local overrides and invalid values are rejected', () => {
  assert.equal(
    loadConfig({ envVersion: 'release', override: 'http://192.168.1.20:8080/api' }).BASE_URL,
    'https://tigercloud.asia/api'
  );
  assert.equal(loadConfig({ override: 'javascript:alert(1)' }).BASE_URL, 'http://192.168.3.25:8080/api');
});
