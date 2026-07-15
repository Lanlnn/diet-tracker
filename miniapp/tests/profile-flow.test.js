const assert = require('node:assert/strict');
const Module = require('node:module');

let profileListener;
let removedListener;
let pageDefinition;
let updateResolve;
let updateReject;
let updatePayload;

const app = {
  globalData: {
    nickname: '林晓', avatarUrl: '', goalType: 'LOSE_FAT', dailyCalorieGoal: 1800,
    currentWeight: 62.4, targetWeight: 58, streakDays: 18
  },
  onProfileUpdate(listener) { profileListener = listener; },
  offProfileUpdate(listener) { removedListener = listener; },
  loadProfile() { return Promise.resolve(this.globalData); },
  updateProfile(payload) {
    updatePayload = payload;
    return new Promise((resolve, reject) => { updateResolve = resolve; updateReject = reject; });
  }
};

const api = { uploadAvatar: () => Promise.resolve({ url: 'https://img.example/new.png' }) };
const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};

global.getApp = () => app;
global.wx = {
  showToast() {},
  navigateTo() {}
};
global.Page = definition => { pageDefinition = definition; };

require('../pages/profile/profile');
Module._load = originalLoad;

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = patch => {
    Object.entries(patch).forEach(([key, value]) => {
      const path = key.split('.');
      let target = page.data;
      while (path.length > 1) target = target[path.shift()];
      target[path[0]] = value;
    });
  };
  return page;
}

(async () => {
  const page = createPage();
  page.onLoad();
  assert.equal(profileListener, page._profileListener, '资料监听器必须绑定到稳定引用');
  page.renderProfile();
  assert.equal(page.data.nickname, '林晓');
  assert.equal(page.data.goalLabel, '稳步减脂');
  assert.equal(page.data.streakDays, 18);

  page.openEditor();
  page.setData({ 'draft.nickname': '新昵称' });
  page.saveProfile();
  assert.equal(page.data.submitting, true, '接口完成前保持提交态');
  assert.equal(page.data.editing, true, '接口完成前不能提前关闭编辑面板');
  assert.equal(updatePayload.nickname, '新昵称');

  updateReject(new Error('保存失败'));
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(page.data.submitting, false);
  assert.equal(page.data.editing, true, '保存失败必须保留编辑内容供重试');

  page.saveProfile();
  updateResolve({ nickname: '新昵称' });
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(page.data.editing, false, '仅接口成功后关闭编辑面板');

  page.onUnload();
  assert.equal(removedListener, page._profileListener, '页面卸载时必须注销同一个监听器');
  console.log('profile flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
