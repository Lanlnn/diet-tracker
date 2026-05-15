const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    today: '',
    greeting: '',
    records: [],
    mealTypes: [],
    stats: { totalCalories: 0, totalProtein: 0, totalFat: 0, totalCarbs: 0 }
  },

  onLoad() {
    const now = new Date();
    const hour = now.getHours();
    let greeting = '你好！';
    if (hour < 6) greeting = '夜深了，早点休息 🌙';
    else if (hour < 9) greeting = '早安！记得吃早餐哦 🌅';
    else if (hour < 12) greeting = '上午好 ☀️';
    else if (hour < 14) greeting = '中午好！吃午饭了吗 🍚';
    else if (hour < 18) greeting = '下午好 🌤';
    else greeting = '晚上好 🌆';

    this.setData({
      today: util.formatDate(now),
      greeting
    });
  },

  onShow() {
    this.loadData();
  },

  loadData() {
    const today = util.getToday();
    Promise.all([
      api.getRecords(today),
      api.getDailyStats(today)
    ]).then(([records, stats]) => {
      const mealTypes = this.groupByMealType(records);
      this.setData({ records, mealTypes, stats });
    });
  },

  groupByMealType(records) {
    const groups = { breakfast: [], lunch: [], dinner: [], snack: [] };
    records.forEach(r => {
      if (groups[r.mealType]) groups[r.mealType].push(r);
    });

    const MEAL_TYPE_MAP = util.MEAL_TYPE_MAP;
    return Object.keys(MEAL_TYPE_MAP).map(type => {
      const items = groups[type] || [];
      const totalCalories = items.reduce((sum, r) =>
        sum + r.foodItem.calories * r.quantity, 0);
      return {
        type,
        label: MEAL_TYPE_MAP[type].label,
        tagClass: MEAL_TYPE_MAP[type].class,
        records: items,
        totalCalories: totalCalories.toFixed(0)
      };
    });
  }
});
