const api = require('../../services/index');
const date = require('../../shared/date');

const EMPTY_DASHBOARD = {
  goalCalories: 1800,
  intakeCalories: 0,
  remainingCalories: 1800,
  exceededCalories: 0,
  exerciseCalories: 0,
  nutrition: null,
  exercise: null,
  meals: null,
  advice: null
};

Page({
  data: {
    dateLabel: '',
    pageState: 'loading',
    errorMessage: '',
    refreshing: false,
    dashboard: EMPTY_DASHBOARD,
    ringDegrees: 0,
    ringStep: 0,
    remainingLabel: '1,800',
    intakeLabel: '0',
    goalLabel: '1,800',
    exerciseLabel: '0',
    exceededLabel: '0',
    calorieStatus: '状态良好',
    nutritionState: 'loading',
    exerciseState: 'loading',
    mealsState: 'loading',
    nutritionItems: [],
    meals: [],
    recordedMealCount: 0,
    hasRecords: false
  },

  onLoad() {
    this._loadedOnce = false;
    this.setData({ dateLabel: this.formatDateLabel(new Date()) });
  },

  onShow() {
    this.loadData({ preserve: this._loadedOnce });
  },

  loadData(options = {}) {
    const preserve = Boolean(options.preserve && this.data.pageState === 'success');
    if (preserve) this.setData({ refreshing: true, errorMessage: '' });
    else this.setData({ pageState: 'loading', errorMessage: '' });

    return api.getTodayDashboard(date.getToday()).then(result => {
      this._loadedOnce = true;
      this.presentDashboard(result || {});
    }).catch(error => {
      const message = error.message || '今日数据加载失败';
      if (preserve) {
        this.setData({ refreshing: false, errorMessage: message });
        wx.showToast({ title: '刷新失败，已保留当前数据', icon: 'none' });
      } else {
        this.setData({ pageState: 'error', refreshing: false, errorMessage: message });
      }
    });
  },

  presentDashboard(raw) {
    const dashboard = { ...EMPTY_DASHBOARD, ...raw };
    const goal = Number(dashboard.goalCalories || 0);
    const intake = Number(dashboard.intakeCalories || 0);
    const remaining = Math.max(Number(dashboard.remainingCalories || 0), 0);
    const exceeded = Math.max(Number(dashboard.exceededCalories || 0), 0);
    const nutritionItems = dashboard.nutrition ? [
      this.presentNutrient('碳水', dashboard.nutrition.carbs, 'carbs'),
      this.presentNutrient('蛋白质', dashboard.nutrition.protein, 'protein'),
      this.presentNutrient('脂肪', dashboard.nutrition.fat, 'fat')
    ] : [];
    const meals = Array.isArray(dashboard.meals) ? dashboard.meals.map(item => ({
      ...item,
      caloriesLabel: this.formatNumber(item.calories, 0),
      previewLabel: (item.previewItems || []).join(' · '),
      empty: !Number(item.itemCount)
    })) : [];
    const recordedMealCount = meals.filter(item => !item.empty).length;
    const progress = goal > 0 ? Math.min(intake / goal, 1) : 0;

    this.setData({
      dashboard,
      pageState: 'success',
      refreshing: false,
      remainingLabel: this.formatNumber(remaining, 0),
      intakeLabel: this.formatNumber(intake, 0),
      goalLabel: this.formatNumber(goal, 0),
      exerciseLabel: this.formatNumber(dashboard.exerciseCalories, 0),
      exceededLabel: this.formatNumber(exceeded, 0),
      calorieStatus: exceeded > 0 ? '今日已超出 ' + this.formatNumber(exceeded, 0) + ' 千卡' : (intake ? '状态良好' : '等待第一笔记录'),
      ringDegrees: Math.round(progress * 360),
      ringStep: Math.round(progress * 20),
      nutritionState: dashboard.nutrition ? 'success' : 'error',
      exerciseState: dashboard.exercise ? (dashboard.exercise.completedCount ? 'success' : 'empty') : 'error',
      mealsState: Array.isArray(dashboard.meals) ? (recordedMealCount ? 'success' : 'empty') : 'error',
      nutritionItems,
      meals,
      recordedMealCount,
      hasRecords: meals.some(item => !item.empty)
    });
  },

  presentNutrient(label, metric, tone) {
    const value = metric || {};
    return {
      label,
      tone,
      amountLabel: this.formatNumber(value.amount, 1) + 'g',
      progressLabel: Math.max(Number(value.progressPercent || 0), 0) + '%',
      progressWidth: Math.min(Math.max(Number(value.progressPercent || 0), 0), 100),
      progressStep: Math.round(Math.min(Math.max(Number(value.progressPercent || 0), 0), 100) / 5)
    };
  },

  retryModule() {
    this.loadData({ preserve: true });
  },

  openRecord() {
    wx.switchTab({ url: '/pages/add/add' });
  },

  openCamera() {
    wx.showModal({
      title: '拍照留存',
      content: '照片仅用于饮食留存，不进行食物识别或热量估算。该能力将在后续阶段接入。',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  openCalendar() {
    wx.showModal({
      title: '饮食日历',
      content: '日历将在 M9 接入，当前可先在首页查看今日摘要。',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  openExercise() {
    wx.switchTab({ url: '/pages/exercise/exercise' });
  },

  openMeal(event) {
    const meal = event.currentTarget.dataset.meal;
    const count = Number(event.currentTarget.dataset.count || 0);
    if (!count) return this.openRecord();
    wx.navigateTo({
      url: '/packageFood/pages/meal-detail/meal-detail?date=' + date.getToday() + '&mealType=' + meal
    });
  },

  formatDateLabel(value) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return (value.getMonth() + 1) + '月' + value.getDate() + '日 · ' + weekdays[value.getDay()];
  },

  formatNumber(value, digits) {
    const number = Number(value || 0);
    const rounded = digits ? number.toFixed(digits).replace(/\.0$/, '') : String(Math.round(number));
    return rounded.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }
});
