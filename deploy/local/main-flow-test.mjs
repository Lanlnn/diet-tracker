#!/usr/bin/env node

import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';

const port = process.env.BACKEND_BIND_PORT || '8080';
const baseUrl = process.env.LOCAL_API_BASE_URL || `http://127.0.0.1:${port}/api`;
const envFile = process.env.LOCAL_ENV_FILE || (
  existsSync('deploy/local/.env.local') ? 'deploy/local/.env.local' : 'deploy/local/.env.local.example'
);
const localEnv = Object.fromEntries(
  readFileSync(envFile, 'utf8').split(/\r?\n/)
    .filter(line => line && !line.startsWith('#') && line.includes('='))
    .map(line => [line.slice(0, line.indexOf('=')), line.slice(line.indexOf('=') + 1)])
);
const jwtSecret = process.env.JWT_SECRET || localEnv.JWT_SECRET || 'local-only-jwt-secret-at-least-32-characters';
const auditPepper = process.env.DELETION_AUDIT_PEPPER || localEnv.DELETION_AUDIT_PEPPER ||
  'local-only-independent-pepper-at-least-32-characters';
const userId = 'e1-local-main-flow-user';
const isolationUserId = 'e1-local-isolation-user';
const userHash = crypto.createHash('sha256').update(auditPepper + userId).digest('hex');
const idempotencyKey = `e1-main-flow-${Date.now()}`;

function shanghaiDateParts() {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(new Date());
  const value = name => parts.find(part => part.type === name).value;
  return { date: `${value('year')}-${value('month')}-${value('day')}`, month: `${value('year')}-${value('month')}` };
}

function base64url(value) {
  return Buffer.from(value).toString('base64url');
}

function createToken(subject) {
  const now = Math.floor(Date.now() / 1000);
  const header = base64url(JSON.stringify({ alg: 'HS256' }));
  const payload = base64url(JSON.stringify({ sub: subject, iat: now, exp: now + 3600 }));
  const signature = crypto.createHmac('sha256', jwtSecret).update(`${header}.${payload}`).digest('base64url');
  return `${header}.${payload}.${signature}`;
}

function composeExec(...args) {
  return execFileSync('docker', [
    'compose', '--env-file', envFile, '--file', 'deploy/local/compose.yml', ...args
  ], { encoding: 'utf8' }).trim();
}

function runSql(sql) {
  return composeExec(
    'exec', '-T', '--env', 'MYSQL_PWD=local-diet-tracker-password', 'mysql',
    'mysql', '--user=diet_tracker', '--default-character-set=utf8mb4',
    '--batch', '--skip-column-names', 'diet_tracker', `--execute=${sql}`
  );
}

function cleanSyntheticUser(targetUserId) {
  runSql(`
    DELETE FROM meal_record WHERE user_id='${targetUserId}';
    DELETE FROM exercise_record WHERE user_id='${targetUserId}';
    DELETE FROM food_favorite WHERE user_id='${targetUserId}';
    DELETE FROM food_item WHERE user_id='${targetUserId}';
    DELETE FROM user_goal WHERE user_id='${targetUserId}';
    DELETE FROM users WHERE openid='${targetUserId}';
  `);
}

async function api(path, { method = 'GET', body, headers = {}, expected = 200, subject = userId } = {}) {
  const response = await fetch(`${baseUrl}${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${createToken(subject)}`,
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...headers
    },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const raw = await response.text();
  let data = null;
  if (raw) {
    try { data = JSON.parse(raw); } catch { data = raw; }
  }
  assert.equal(response.status, expected, `${method} ${path}: expected ${expected}, got ${response.status}: ${raw}`);
  return data;
}

const { date, month } = shanghaiDateParts();
let accountDeleted = false;

try {
  cleanSyntheticUser(userId);
  cleanSyntheticUser(isolationUserId);
  runSql(`DELETE FROM account_deletion_audit WHERE user_hash='${userHash}';`);
  runSql(`INSERT INTO users (openid, nickname) VALUES ('${userId}', 'E1 本地验收');`);
  runSql(`INSERT INTO users (openid, nickname) VALUES ('${isolationUserId}', 'E1 隔离验收');`);

  const profile = await api('/users/me');
  assert.equal(profile.nickname, 'E1 本地验收');

  const updatedProfile = await api('/users/me', {
    method: 'PUT',
    body: {
      nickname: 'E1 主链路', avatarUrl: '', goalType: 'LOSE_FAT',
      dailyCalorieGoal: 2000, currentWeight: 65.5, targetWeight: 60
    }
  });
  assert.equal(updatedProfile.nickname, 'E1 主链路');

  const goals = await api('/users/me/goals', {
    method: 'PUT',
    body: {
      dailyCalorieGoal: 2000, carbsGoal: 250, proteinGoal: 100, fatGoal: 66.7,
      currentWeight: 65.5, targetWeight: 60, goalType: 'LOSE_FAT', aiCoachEnabled: true
    }
  });
  assert.equal(goals.dailyCalorieGoal, 2000);
  assert.equal(goals.goalType, 'LOSE_FAT');

  const categories = await api('/foods/categories');
  assert.equal(categories.length, 7);

  const chickenSearch = await api('/foods/search?keyword=%E9%B8%A1%E8%83%B8%E8%82%89&page=0&size=20');
  assert.equal(chickenSearch.items[0].name, '鸡胸肉');
  const chickenId = chickenSearch.items[0].id;

  const calculation = await api(`/foods/${chickenId}/calculate`, {
    method: 'POST', body: { amount: 150 }
  });
  assert.equal(calculation.calories, 248);
  assert.equal(calculation.protein, 46.5);

  const customFood = await api('/foods', {
    method: 'POST',
    body: {
      name: 'E1 本地三明治', categoryId: 7, baseAmount: 100, baseUnit: 'g',
      calories: 220, protein: 10, fat: 8, carbs: 28
    }
  });
  assert.equal(customFood.custom, true);
  assert.equal(customFood.source, 'USER_CUSTOM');

  const customSearch = await api('/foods/search?keyword=E1%20%E6%9C%AC%E5%9C%B0%E4%B8%89%E6%98%8E%E6%B2%BB&page=0&size=20');
  assert.equal(customSearch.total, 1);
  assert.equal(customSearch.items[0].id, customFood.id);
  const isolatedSearch = await api('/foods/search?keyword=E1%20%E6%9C%AC%E5%9C%B0%E4%B8%89%E6%98%8E%E6%B2%BB&page=0&size=20', {
    subject: isolationUserId
  });
  assert.equal(isolatedSearch.total, 0, 'another user could search the custom food');
  const isolatedCreate = await api('/records', {
    method: 'POST', expected: 404, subject: isolationUserId,
    body: {
      mealDate: date, mealType: 'lunch', foodItemId: customFood.id,
      quantity: 100, unit: 'g', recordTime: `${date}T12:00:00`
    }
  });
  assert.equal(isolatedCreate.code, 'FOOD_NOT_FOUND');

  const chickenRecordBody = {
    mealDate: date, mealType: 'breakfast', foodItemId: chickenId,
    quantity: 150, unit: 'g', recordTime: `${date}T08:00:00`, note: 'E1 自动验收'
  };
  const chickenRecord = await api('/records', {
    method: 'POST', body: chickenRecordBody, headers: { 'X-Idempotency-Key': idempotencyKey }
  });
  const retriedRecord = await api('/records', {
    method: 'POST', body: chickenRecordBody, headers: { 'X-Idempotency-Key': idempotencyKey }
  });
  assert.equal(retriedRecord.id, chickenRecord.id, 'idempotent retry created a duplicate meal');

  const customRecord = await api('/records', {
    method: 'POST',
    body: {
      mealDate: date, mealType: 'lunch', foodItemId: customFood.id,
      quantity: 100, unit: 'g', recordTime: `${date}T12:00:00`, note: '自定义食品隔离验收'
    },
    headers: { 'X-Idempotency-Key': `${idempotencyKey}-custom` }
  });

  const appleSearch = await api('/foods/search?keyword=%E8%8B%B9%E6%9E%9C&page=0&size=20');
  assert.equal(appleSearch.items[0].name, '苹果');
  const snackRecord = await api('/records', {
    method: 'POST',
    body: {
      mealDate: date, mealType: 'snack', foodItemId: appleSearch.items[0].id,
      quantity: 1, unit: '个', recordTime: `${date}T15:00:00`, note: '加餐枚举验收'
    },
    headers: { 'X-Idempotency-Key': `${idempotencyKey}-snack` }
  });

  const records = await api(`/records?date=${date}`);
  assert.equal(records.length, 3);

  const updatedMeal = await api(`/records/${chickenRecord.id}`, {
    method: 'PUT', body: { mealType: 'dinner', quantity: 200, unit: 'g', note: '更新为晚餐' }
  });
  assert.equal(updatedMeal.mealType, 'dinner');
  assert.equal(updatedMeal.quantity, 200);

  const exercise = await api('/exercises', {
    method: 'POST',
    body: {
      exerciseDate: date, exerciseType: 'walking', startTime: '18:00',
      durationMinutes: 20, intensity: 'medium', caloriesBurned: 80,
      source: 'MANUAL', note: 'E1 自动验收'
    }
  });
  const updatedExercise = await api(`/exercises/${exercise.id}`, {
    method: 'PUT',
    body: {
      exerciseDate: date, exerciseType: 'walking', startTime: '18:00',
      durationMinutes: 25, intensity: 'medium', caloriesBurned: 100,
      source: 'MANUAL', note: '更新后的运动'
    }
  });
  assert.equal(updatedExercise.caloriesBurned, 100);

  const dashboard = await api(`/dashboard/today?date=${date}`);
  assert.equal(dashboard.goalCalories, 2000);
  assert.equal(dashboard.goalSource, 'USER');
  assert.equal(dashboard.intakeCalories, 645);
  assert.equal(dashboard.exerciseCalories, 100);
  assert.equal(dashboard.netCalories, 545);
  assert.equal(dashboard.remainingCalories, 1355);

  const trend = await api('/stats/trend?range=7d');
  const todayTrend = trend.dailyData.find(day => day.date === date);
  assert.ok(todayTrend, 'trend did not contain the current date');
  assert.equal(todayTrend.intakeCalories, 645);
  assert.equal(todayTrend.exerciseCalories, 100);
  assert.equal(todayTrend.netCalories, 545);

  const calendar = await api(`/calendar/summary?month=${month}`);
  const todayCalendar = calendar.days.find(day => day.date === date);
  assert.ok(todayCalendar, 'calendar did not contain the current date');
  assert.equal(todayCalendar.intakeCalories, 645);
  assert.equal(todayCalendar.exerciseCalories, 100);
  assert.equal(todayCalendar.mealCount, 3);

  const summary = await api('/users/me/summary');
  assert.equal(summary.customFoodCount, 1);
  assert.ok(summary.exerciseCountThisWeek >= 1);

  await api(`/records/${customRecord.id}`, { method: 'DELETE', expected: 204 });
  await api(`/records/${snackRecord.id}`, { method: 'DELETE', expected: 204 });
  await api(`/exercises/${exercise.id}`, { method: 'DELETE', expected: 204 });
  const afterDelete = await api(`/dashboard/today?date=${date}`);
  assert.equal(afterDelete.intakeCalories, 330);
  assert.equal(afterDelete.exerciseCalories, 0);

  const auditBefore = Number(runSql('SELECT COUNT(*) FROM account_deletion_audit;'));
  await api('/users/me', {
    method: 'DELETE', headers: { 'X-Delete-Confirmation': 'DELETE' }, expected: 200
  });
  accountDeleted = true;

  const remaining = runSql(`
    SELECT COUNT(*) FROM users WHERE openid='${userId}';
    SELECT COUNT(*) FROM user_goal WHERE user_id='${userId}';
    SELECT COUNT(*) FROM meal_record WHERE user_id='${userId}';
    SELECT COUNT(*) FROM exercise_record WHERE user_id='${userId}';
    SELECT COUNT(*) FROM food_item WHERE user_id='${userId}';
  `).split(/\s+/).map(Number);
  assert.deepEqual(remaining, [0, 0, 0, 0, 0]);
  const auditAfter = Number(runSql('SELECT COUNT(*) FROM account_deletion_audit;'));
  assert.equal(auditAfter, auditBefore + 1);
  runSql(`DELETE FROM account_deletion_audit WHERE user_hash='${userHash}';`);

  console.log(
    `Local main flow passed: date=${date} goals=2000 foods=system+custom+isolated ` +
    'meal-types=breakfast/lunch/dinner/snack retry/update/delete exercise=create/update/delete ' +
    'dashboard=645/100/545 trend=645/100/545 calendar=3 account-delete=clean'
  );
} finally {
  if (!accountDeleted) cleanSyntheticUser(userId);
  cleanSyntheticUser(isolationUserId);
  runSql(`DELETE FROM account_deletion_audit WHERE user_hash='${userHash}';`);
}
