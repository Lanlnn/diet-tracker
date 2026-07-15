const api = require('../../services');
const dateUtils = require('../../shared/date');
const { MEAL_TYPE_MAP } = require('../../shared/meal-types');

Page({
  data: {
    currentDate: dateUtils.getToday(),
    displayDate: '',
    weekday: '',
    records: [],
    groupedRecords: [],
    stats: { totalCalories: 0, totalProtein: 0, totalFat: 0, totalCarbs: 0 }
  },

  onLoad() {
    this.updateDisplay();
  },

  onShow() {
    this.loadRecords();
  },

  updateDisplay() {
    const d = new Date(this.data.currentDate + 'T00:00:00');
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    this.setData({
      displayDate: dateUtils.formatDate(d),
      weekday: weekdays[d.getDay()]
    });
  },

  loadRecords() {
    const date = this.data.currentDate;
    Promise.all([
      api.getRecords(date).catch(() => []),
      api.getDailyStats(date).catch(() => ({}))
    ]).then(([records, stats]) => {
      const groupedRecords = this.groupByMealType(records || []);
      this.setData({ records: records || [], groupedRecords, stats: stats || {} });
    });
  },

  groupByMealType(records) {
    const groups = { breakfast: [], lunch: [], dinner: [], snack: [] };
    records.forEach(r => {
      if (groups[r.mealType]) groups[r.mealType].push(r);
    });
    return Object.keys(MEAL_TYPE_MAP).map(type => ({
      type,
      label: MEAL_TYPE_MAP[type].label,
      tagClass: MEAL_TYPE_MAP[type].class,
      records: groups[type] || [],
      totalCalories: (groups[type] || []).reduce((s, r) => s + r.foodItem.calories * r.quantity, 0).toFixed(0)
    }));
  },

  prevDay() {
    const d = new Date(this.data.currentDate + 'T00:00:00');
    d.setDate(d.getDate() - 1);
    this.setData({ currentDate: dateUtils.formatDate(d) });
    this.updateDisplay();
    this.loadRecords();
  },

  nextDay() {
    const d = new Date(this.data.currentDate + 'T00:00:00');
    d.setDate(d.getDate() + 1);
    this.setData({ currentDate: dateUtils.formatDate(d) });
    this.updateDisplay();
    this.loadRecords();
  },

  showPicker() {
    const that = this;
    wx.showModal({
      title: '选择日期',
      editable: true,
      placeholderText: '格式: YYYY-MM-DD',
      success(res) {
        if (res.confirm && res.content) {
          that.setData({ currentDate: res.content });
          that.updateDisplay();
          that.loadRecords();
        }
      }
    });
  },

  deleteRecord(e) {
    const id = e.currentTarget.dataset.id;
    const that = this;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条记录吗？',
      success(res) {
        if (res.confirm) {
          api.deleteRecord(id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' });
            that.loadRecords();
          });
        }
      }
    });
  }
});
