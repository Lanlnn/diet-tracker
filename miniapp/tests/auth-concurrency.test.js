const assert = require('node:assert/strict');

const storage = {};
let loginCalls = 0;
let loginRequestCalls = 0;
let profileCalls = 0;
let profileShouldExpire = false;
let failNextLogin = false;
let appDefinition;

global.wx = {
  getAccountInfoSync: () => ({ miniProgram: { envVersion: 'develop' } }),
  getStorageSync: key => storage[key],
  setStorageSync: (key, value) => { storage[key] = value; },
  removeStorageSync: key => { delete storage[key]; },
  login: ({ success, fail }) => {
    loginCalls += 1;
    if (failNextLogin) {
      failNextLogin = false;
      return setImmediate(() => fail({ errMsg: 'login:fail' }));
    }
    setImmediate(() => success({ code: 'test-code-' + loginCalls }));
  },
  request: ({ url, success }) => {
    if (/\/auth\/login$/.test(url)) {
      loginRequestCalls += 1;
      return setImmediate(() => success({ statusCode: 200, data: { token: 'token-' + loginRequestCalls } }));
    }
    assert.match(url, /\/users\/me$/);
    profileCalls += 1;
    if (profileShouldExpire) {
      profileShouldExpire = false;
      return setImmediate(() => success({ statusCode: 401, data: { code: 'TOKEN_EXPIRED', message: '登录状态已失效' } }));
    }
    setImmediate(() => success({ statusCode: 200, data: { nickname: '林晓' } }));
  }
};
global.App = definition => { appDefinition = definition; };
global.getApp = () => appDefinition;

require('../app');

(async () => {
  const first = await Promise.all([
    appDefinition.ensureLogin(),
    appDefinition.ensureLogin(),
    appDefinition.ensureLogin()
  ]);
  assert.deepEqual(first, ['token-1', 'token-1', 'token-1']);
  assert.equal(loginCalls, 1, '首次并发登录只能调用一次 wx.login');
  assert.equal(loginRequestCalls, 1, '首次并发登录只能调用一次后端登录');

  await appDefinition.ensureLogin();
  assert.equal(loginCalls, 1, '缓存 Token 有效时不得重复调用 wx.login');

  const refreshed = await Promise.all([appDefinition.reLogin(), appDefinition.reLogin()]);
  assert.deepEqual(refreshed, ['token-2', 'token-2']);
  assert.equal(loginCalls, 2, '并发刷新只能增加一次 wx.login');
  assert.equal(loginRequestCalls, 2, '并发刷新只能增加一次后端登录');

  profileShouldExpire = true;
  const api = require('../services/index');
  const profile = await api.getProfile();
  assert.equal(profile.nickname, '林晓');
  assert.equal(loginRequestCalls, 3, 'Token 过期后只重新登录一次');
  assert.equal(profileCalls, 2, 'Token 过期后只重试原请求一次');

  appDefinition._clearSession();
  failNextLogin = true;
  await assert.rejects(appDefinition.ensureLogin(), /WX_LOGIN_FAILED/, '微信登录失败必须向页面透传');
  assert.equal(appDefinition._loginPromise, null, '失败后必须释放登录 Promise 以允许重试');
  assert.equal(await appDefinition.ensureLogin(), 'token-4', '登录失败后允许用户重试');
  console.log('auth concurrency tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
