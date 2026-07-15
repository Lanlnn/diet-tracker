const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
let deleteCalls = 0;
let clearCalls = 0;
let relaunchUrl = '';
const modalQueue = [{ confirm: true }, { confirm: true }];

const api = {
  deleteAccount() {
    deleteCalls += 1;
    return Promise.resolve();
  }
};
const app = { clearDeletedAccount() { clearCalls += 1; } };
const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};

global.getApp = () => app;
global.Page = definition => { pageDefinition = definition; };
global.wx = {
  showModal(options) { setImmediate(() => options.success(modalQueue.shift())); },
  showToast() {},
  reLaunch({ url }) { relaunchUrl = url; }
};

const originalSetTimeout = global.setTimeout;
global.setTimeout = callback => { callback(); return 0; };
require('../packageProfile/pages/settings/settings');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: { ...pageDefinition.data } };
  page.setData = patch => Object.assign(page.data, patch);
  return page;
}

(async () => {
  const page = createPage();
  page.deleteAccount();
  await new Promise(resolve => setImmediate(resolve));
  await new Promise(resolve => setImmediate(resolve));
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(deleteCalls, 1, '双重确认后只发起一次删除请求');
  assert.equal(clearCalls, 1, '删除成功后必须清理本地登录和资料');
  assert.equal(relaunchUrl, '/pages/index/index', '删除后回到可重新登录的首页');

  global.setTimeout = originalSetTimeout;
  console.log('settings flow tests passed');
})().catch(error => {
  global.setTimeout = originalSetTimeout;
  console.error(error);
  process.exitCode = 1;
});
