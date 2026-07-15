const assert = require('node:assert/strict');
const Module = require('node:module');

let pageDefinition;
let day;
let recommendations;
let savedPayload;
let deletedId;

const api = {
  getExercises() { return Promise.resolve(day); },
  getExerciseRecommendations() { return Promise.resolve(recommendations); },
  createExercise(payload) {
    savedPayload = payload;
    day = {
      date: payload.exerciseDate,
      totalCalories: payload.caloriesBurned,
      totalDurationMinutes: payload.durationMinutes,
      records: [{ id: 7, ...payload, typeLabel: '户外快走', intensityLabel: '中等强度' }],
      weeklyCompletion: { completedDays: 1, targetDays: 4 }
    };
    return Promise.resolve(day.records[0]);
  },
  updateExercise() { return Promise.resolve(); },
  deleteExercise(id) {
    deletedId = id;
    day = { ...day, totalCalories: 0, totalDurationMinutes: 0, records: [], weeklyCompletion: { completedDays: 0, targetDays: 4 } };
    return Promise.resolve();
  }
};

const originalLoad = Module._load;
Module._load = function(request, parent, isMain) {
  if (request === '../../services/index') return api;
  return originalLoad.call(this, request, parent, isMain);
};
global.wx = { showToast() {}, showModal() {} };
global.Page = definition => { pageDefinition = definition; };
require('../pages/exercise/exercise');
Module._load = originalLoad;

function setPath(target, path, value) {
  const parts = path.replace(/\[(\d+)\]/g, '.$1').split('.');
  let current = target;
  for (let i = 0; i < parts.length - 1; i += 1) current = current[parts[i]];
  current[parts[parts.length - 1]] = value;
}

function createPage() {
  const page = { ...pageDefinition, data: JSON.parse(JSON.stringify(pageDefinition.data)) };
  page.setData = (patch, callback) => {
    Object.entries(patch).forEach(([key, value]) => setPath(page.data, key, value));
    if (callback) callback();
  };
  return page;
}

(async () => {
  day = {
    date: '2026-07-15', totalCalories: 0, totalDurationMinutes: 0, records: [],
    weeklyCompletion: { completedDays: 0, targetDays: 4 }
  };
  recommendations = [
    { id: 'walk', name: '饭后舒缓走', exerciseType: 'walking', intensity: 'low', intensityLabel: '低强度', durationMinutes: 20, estimatedCalories: 64 },
    { id: 'core', name: '核心激活', exerciseType: 'strength', intensity: 'low', intensityLabel: '低强度', durationMinutes: 12, estimatedCalories: 57.6 },
    { id: 'overflow', name: '不应显示', exerciseType: 'yoga', intensity: 'low', intensityLabel: '低强度', durationMinutes: 10, estimatedCalories: 24 }
  ];

  const page = createPage();
  page.onLoad();
  await page.loadData();
  assert.equal(page.data.status, 'success');
  assert.equal(page.data.records.length, 0);
  assert.equal(page.data.recommendations.length, 2, '推荐最多显示两项');

  page.setData({
    'form.durationMinutes': '20',
    'form.exerciseType': 'walking',
    'form.intensity': 'medium',
    'form.intensityIndex': 1,
    'form.caloriesBurned': ''
  });
  page.updateEstimate();
  await page.submitExercise();
  assert.equal(savedPayload.durationMinutes, 20);
  assert.equal(savedPayload.caloriesBurned, 80);
  assert.equal(page.data.totalDuration, 20);
  assert.equal(page.data.totalCaloriesLabel, '80');
  assert.equal(page.data.weeklyProgress, 25);

  await page.deleteRecord(7);
  assert.equal(deletedId, 7);
  assert.equal(page.data.records.length, 0);
  assert.equal(page.data.totalCaloriesLabel, '0');

  console.log('exercise flow tests passed');
})().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
