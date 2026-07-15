#!/usr/bin/env node

import crypto from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';

const envFile = process.env.LOCAL_ENV_FILE || (
  existsSync('deploy/local/.env.local') ? 'deploy/local/.env.local' : 'deploy/local/.env.local.example'
);
const localEnv = Object.fromEntries(
  readFileSync(envFile, 'utf8').split(/\r?\n/)
    .filter(line => line && !line.startsWith('#') && line.includes('='))
    .map(line => [line.slice(0, line.indexOf('=')), line.slice(line.indexOf('=') + 1)])
);
const jwtSecret = process.env.JWT_SECRET || localEnv.JWT_SECRET || 'local-only-jwt-secret-at-least-32-characters';
const userId = 'e1-local-ui-preview-user';

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

function clean() {
  runSql(`
    DELETE FROM meal_record WHERE user_id='${userId}';
    DELETE FROM exercise_record WHERE user_id='${userId}';
    DELETE FROM food_favorite WHERE user_id='${userId}';
    DELETE FROM food_item WHERE user_id='${userId}';
    DELETE FROM user_goal WHERE user_id='${userId}';
    DELETE FROM users WHERE openid='${userId}';
  `);
}

function token() {
  const now = Math.floor(Date.now() / 1000);
  const header = Buffer.from(JSON.stringify({ alg: 'HS256' })).toString('base64url');
  const payload = Buffer.from(JSON.stringify({ sub: userId, iat: now, exp: now + 86400 })).toString('base64url');
  const signature = crypto.createHmac('sha256', jwtSecret).update(`${header}.${payload}`).digest('base64url');
  return `${header}.${payload}.${signature}`;
}

if (process.argv.includes('--clean')) {
  clean();
  console.log('Local UI preview fixture removed. Clear the miniapp token storage before real login testing.');
  process.exit(0);
}

clean();
runSql(`
  INSERT INTO users (openid, nickname, goal_type, daily_calorie_goal, current_weight, target_weight)
  VALUES ('${userId}', 'E1 本地预览', 'LOSE_FAT', 2000, 65.5, 60.0);

  INSERT INTO user_goal (
    user_id, daily_calorie_goal, carbs_goal, protein_goal, fat_goal,
    current_weight, target_weight, goal_type, ai_coach_enabled, customized
  ) VALUES ('${userId}', 2000, 250, 100, 66.7, 65.5, 60.0, 'LOSE_FAT', TRUE, TRUE);

  INSERT INTO meal_record (
    meal_date, meal_type, food_item_id, quantity, unit, record_time, user_id, note,
    food_name_snapshot, base_amount_snapshot, base_unit_snapshot,
    calories_snapshot, protein_snapshot, fat_snapshot, carbs_snapshot, client_request_id
  )
  SELECT CURRENT_DATE, 'breakfast', id, 150, 'g', CONCAT(CURRENT_DATE, ' 08:00:00'), '${userId}', '开发者工具预览',
         name, base_amount, base_unit, calories, protein, fat, carbs, 'preview-breakfast'
  FROM food_item WHERE name='鸡胸肉' AND user_id IS NULL LIMIT 1;

  INSERT INTO meal_record (
    meal_date, meal_type, food_item_id, quantity, unit, record_time, user_id, note,
    food_name_snapshot, base_amount_snapshot, base_unit_snapshot,
    calories_snapshot, protein_snapshot, fat_snapshot, carbs_snapshot, client_request_id
  )
  SELECT CURRENT_DATE, 'lunch', id, 200, 'g', CONCAT(CURRENT_DATE, ' 12:00:00'), '${userId}', '开发者工具预览',
         name, base_amount, base_unit, calories, protein, fat, carbs, 'preview-lunch'
  FROM food_item WHERE name='米饭' AND user_id IS NULL LIMIT 1;

  INSERT INTO meal_record (
    meal_date, meal_type, food_item_id, quantity, unit, record_time, user_id, note,
    food_name_snapshot, base_amount_snapshot, base_unit_snapshot,
    calories_snapshot, protein_snapshot, fat_snapshot, carbs_snapshot, client_request_id
  )
  SELECT CURRENT_DATE, 'snack', id, 1, '个', CONCAT(CURRENT_DATE, ' 15:00:00'), '${userId}', '开发者工具预览',
         name, base_amount, base_unit, calories, protein, fat, carbs, 'preview-snack'
  FROM food_item WHERE name='苹果' AND user_id IS NULL LIMIT 1;

  INSERT INTO meal_record (
    meal_date, meal_type, food_item_id, quantity, unit, record_time, user_id, note,
    food_name_snapshot, base_amount_snapshot, base_unit_snapshot,
    calories_snapshot, protein_snapshot, fat_snapshot, carbs_snapshot, client_request_id
  )
  SELECT DATE_SUB(CURRENT_DATE, INTERVAL offsets.day_offset DAY), 'dinner', food.id, 100, 'g',
         CONCAT(DATE_SUB(CURRENT_DATE, INTERVAL offsets.day_offset DAY), ' 18:30:00'), '${userId}', '趋势预览',
         food.name, food.base_amount, food.base_unit, food.calories, food.protein, food.fat, food.carbs,
         CONCAT('preview-history-', offsets.day_offset)
  FROM food_item food
  JOIN (SELECT 1 day_offset UNION ALL SELECT 2 UNION ALL SELECT 6 UNION ALL SELECT 14 UNION ALL SELECT 29 UNION ALL SELECT 59 UNION ALL SELECT 89) offsets
  WHERE food.name='鸡胸肉' AND food.user_id IS NULL;

  INSERT INTO exercise_record (
    exercise_date, exercise_type, start_time, duration_minutes, intensity,
    calories_burned, source, user_id, note
  ) VALUES
    (CURRENT_DATE, 'walking', '18:00:00', 25, 'medium', 100, 'MANUAL', '${userId}', '饭后快走'),
    (DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 'running', '07:30:00', 20, 'high', 200, 'MANUAL', '${userId}', '趋势预览'),
    (DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), 'yoga', '20:00:00', 30, 'low', 72, 'MANUAL', '${userId}', '趋势预览');
`);

const previewToken = token();
console.log('Local UI preview fixture ready. Paste this only into the current miniapp DevTools Console:');
console.log(`wx.setStorageSync('token','${previewToken}');wx.setStorageSync('nickname','E1 本地预览');`);
console.log('Then click 普通编译. This synthetic session is not evidence of real wx.login.');
console.log('Cleanup: node deploy/local/preview-fixture.mjs --clean');
