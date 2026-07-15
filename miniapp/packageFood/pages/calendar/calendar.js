const api = require('../../../services/index');
const dateUtils = require('../../../shared/date');

const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日'];

Page({
  data: {
    weekdays: WEEKDAYS,
    pageState: 'loading',
    detailState: 'loading',
    errorMessage: '',
    displayedMonth: '',
    monthLabel: '',
    selectedDate: '',
    selectedDateLabel: '',
    today: '',
    canGoPrevious: true,
    canGoNext: false,
    calendarDays: [],
    selectedSummary: null,
    selectedDashboard: null,
    meals: [],
    intakeLabel: '0',
    exerciseLabel: '0',
    remainingLabel: '0',
    progressLabel: '0%',
    progressStep: 0,
    mealCountLabel: '0',
    hasMealRecords: false
  },

  onLoad() {
    const today = dateUtils.getToday();
    const month = today.slice(0, 7);
    this._monthRequestId = 0;
    this._detailRequestId = 0;
    this.setData({ today, selectedDate: today });
    return this.loadMonth(month, { selectedDate: today, loadDetail: true });
  },

  onShow() {
    if (this._hasShown && this.data.pageState === 'success') {
      this.loadMonth(this.data.displayedMonth, {
        selectedDate: this.data.selectedDate,
        loadDetail: true,
        preserve: true
      });
    }
    this._hasShown = true;
  },

  loadMonth(month, options = {}) {
    const requestId = ++this._monthRequestId;
    const preserve = Boolean(options.preserve || this.data.pageState === 'success');
    if (!preserve) this.setData({ pageState: 'loading', errorMessage: '' });
    else this.setData({ errorMessage: '' });

    return api.getCalendarSummary(month).then(summary => {
      if (requestId !== this._monthRequestId) return;
      const selectedDate = options.selectedDate || this.defaultDateForMonth(month);
      this.presentMonth(summary || {}, month, selectedDate);
      if (options.loadDetail) return this.loadSelectedDetail(selectedDate);
      this.clearDetail();
    }).catch(error => {
      if (requestId !== this._monthRequestId) return;
      const message = error.message || '月历加载失败';
      if (preserve) {
        this.setData({ errorMessage: message });
        wx.showToast({ title: '刷新失败，已保留当前日历', icon: 'none' });
      } else {
        this.setData({ pageState: 'error', errorMessage: message });
      }
    });
  },

  presentMonth(summary, month, selectedDate) {
    const values = new Map((summary.days || []).map(item => [item.date, item]));
    const calendarDays = dateUtils.buildMonthGrid(month).map(day => {
      const value = values.get(day.date);
      return {
        ...day,
        hasRecord: Boolean(value && value.hasRecord),
        isToday: day.date === this.data.today,
        selected: day.date === selectedDate,
        future: day.date > this.data.today
      };
    });
    const monthDate = dateUtils.parseMonth(month);
    this._currentSummary = summary;
    this._summaryByDate = values;
    this.setData({
      pageState: 'success',
      displayedMonth: month,
      monthLabel: monthDate.getFullYear() + '年 ' + (monthDate.getMonth() + 1) + '月',
      selectedDate,
      calendarDays,
      canGoPrevious: month > dateUtils.addMonths(this.data.today.slice(0, 7), -11),
      canGoNext: month < this.data.today.slice(0, 7),
      errorMessage: ''
    });
    this.presentSelectedSummary(selectedDate);
  },

  presentSelectedSummary(selectedDate) {
    const summary = this._summaryByDate && this._summaryByDate.get(selectedDate) || {
      intakeCalories: 0,
      exerciseCalories: 0,
      remainingCalories: 0,
      mealCount: 0,
      hasRecord: false
    };
    const goal = Number((this._currentSummary && this._currentSummary.goalCalories) || 0);
    const intake = Number(summary.intakeCalories || 0);
    const progress = goal > 0 ? Math.min(Math.max(intake / goal, 0), 1) : 0;
    const parsed = dateUtils.parseDate(selectedDate);
    this.setData({
      selectedSummary: summary,
      selectedDateLabel: (parsed.getMonth() + 1) + '月' + parsed.getDate() + '日' + (selectedDate === this.data.today ? ' · 今天' : ''),
      intakeLabel: this.formatNumber(intake),
      exerciseLabel: this.formatNumber(summary.exerciseCalories),
      remainingLabel: this.formatNumber(summary.remainingCalories),
      mealCountLabel: String(summary.mealCount || 0),
      progressLabel: Math.round(progress * 100) + '%',
      progressStep: Math.round(progress * 20)
    });
  },

  loadSelectedDetail(selectedDate) {
    const requestId = ++this._detailRequestId;
    this.setData({ detailState: 'loading' });
    return api.getTodayDashboard(selectedDate).then(dashboard => {
      if (requestId !== this._detailRequestId || selectedDate !== this.data.selectedDate) return;
      const meals = (dashboard.meals || []).filter(item => Number(item.itemCount || 0) > 0).map(item => ({
        ...item,
        caloriesLabel: this.formatNumber(item.calories),
        previewLabel: (item.previewItems || []).join(' · '),
        badgeLabel: (item.label || '餐').slice(0, 1),
        tone: this.mealTone(item.type)
      }));
      this.setData({
        detailState: meals.length ? 'success' : 'empty',
        selectedDashboard: dashboard,
        meals,
        hasMealRecords: meals.length > 0
      });
    }).catch(error => {
      if (requestId !== this._detailRequestId || selectedDate !== this.data.selectedDate) return;
      this.setData({ detailState: 'error' });
    });
  },

  clearDetail() {
    ++this._detailRequestId;
    this.setData({ detailState: 'idle', selectedDashboard: null, meals: [], hasMealRecords: false });
  },

  selectDay(event) {
    const selectedDate = event.currentTarget.dataset.date;
    if (!selectedDate || selectedDate > this.data.today) return;
    const selectedMonth = selectedDate.slice(0, 7);
    if (selectedMonth !== this.data.displayedMonth) {
      return this.loadMonth(selectedMonth, { selectedDate, loadDetail: true, preserve: true });
    }
    const calendarDays = this.data.calendarDays.map(day => ({ ...day, selected: day.date === selectedDate }));
    this.setData({ selectedDate, calendarDays });
    this.presentSelectedSummary(selectedDate);
    return this.loadSelectedDetail(selectedDate);
  },

  previousMonth() {
    if (!this.data.canGoPrevious) return;
    const month = dateUtils.addMonths(this.data.displayedMonth, -1);
    return this.loadMonth(month, { selectedDate: month + '-01', preserve: true });
  },

  nextMonth() {
    if (!this.data.canGoNext) return;
    const month = dateUtils.addMonths(this.data.displayedMonth, 1);
    return this.loadMonth(month, { selectedDate: this.defaultDateForMonth(month), preserve: true });
  },

  retryMonth() {
    return this.loadMonth(this.data.displayedMonth || this.data.today.slice(0, 7), {
      selectedDate: this.data.selectedDate || this.data.today,
      loadDetail: true,
      preserve: this.data.pageState === 'success'
    });
  },

  retryDetail() {
    return this.loadSelectedDetail(this.data.selectedDate);
  },

  openMeal(event) {
    const mealType = event.currentTarget.dataset.meal;
    if (!mealType) return;
    wx.navigateTo({
      url: '/packageFood/pages/meal-detail/meal-detail?date=' + this.data.selectedDate + '&mealType=' + mealType
    });
  },

  openFirstMeal() {
    if (!this.data.meals.length) return;
    this.openMeal({ currentTarget: { dataset: { meal: this.data.meals[0].type } } });
  },

  goBack() {
    wx.navigateBack();
  },

  defaultDateForMonth(month) {
    return month === this.data.today.slice(0, 7) ? this.data.today : month + '-01';
  },

  mealTone(type) {
    return { breakfast: 'morning', lunch: 'noon', dinner: 'evening', snack: 'snack' }[type] || 'snack';
  },

  formatNumber(value) {
    return String(Math.round(Number(value || 0))).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }
});
