const api = require('../../services/index');
const date = require('../../shared/date');

const TYPES = [
  { value: 'walking', label: '户外快走', icon: '🚶' },
  { value: 'running', label: '跑步', icon: '🏃' },
  { value: 'cycling', label: '骑行', icon: '🚴' },
  { value: 'strength', label: '力量训练', icon: '🏋️' },
  { value: 'yoga', label: '瑜伽', icon: '🧘' },
  { value: 'stretching', label: '拉伸', icon: '🤸' },
  { value: 'other', label: '其他运动', icon: '⚡' }
];
const INTENSITIES = [
  { value: 'low', label: '低强度', factor: 0.8 },
  { value: 'medium', label: '中等强度', factor: 1 },
  { value: 'high', label: '高强度', factor: 1.25 }
];
const TYPE_RATES = { walking: 4, running: 8, cycling: 6.5, strength: 6, yoga: 3, stretching: 2.5, other: 4 };

function initialForm() {
  return {
    id: null, exerciseType: 'walking', typeIndex: 0, startTime: '18:00', durationMinutes: '20',
    intensity: 'medium', intensityIndex: 1, caloriesBurned: '', source: 'MANUAL', note: ''
  };
}

Page({
  data: {
    dateLabel: '',
    status: 'loading',
    errorMessage: '',
    refreshing: false,
    totalCaloriesLabel: '0',
    totalDuration: 0,
    records: [],
    recommendations: [],
    weekly: { completedDays: 0, targetDays: 4 },
    weeklyProgress: 0,
    types: TYPES,
    intensities: INTENSITIES,
    sheetOpen: false,
    submitting: false,
    deletingId: null,
    form: initialForm(),
    estimatedCalories: 80
  },

  onLoad() {
    this._loadedOnce = false;
    this.setData({ dateLabel: this.formatDateLabel(new Date()) });
  },

  onShow() {
    this.loadData({ preserve: this._loadedOnce });
  },

  loadData(options = {}) {
    const preserve = Boolean(options.preserve && this.data.status === 'success');
    this.setData(preserve ? { refreshing: true, errorMessage: '' } : { status: 'loading', errorMessage: '' });
    const today = date.getToday();
    return Promise.all([api.getExercises(today), api.getExerciseRecommendations(today)]).then(([day, recommendations]) => {
      this._loadedOnce = true;
      this.present(day || {}, recommendations || []);
    }).catch(error => {
      const message = error.message || '运动数据加载失败';
      if (preserve) {
        this.setData({ refreshing: false, errorMessage: message });
        wx.showToast({ title: '刷新失败，已保留当前数据', icon: 'none' });
      } else {
        this.setData({ status: 'error', refreshing: false, errorMessage: message });
      }
    });
  },

  present(day, recommendations) {
    const records = (Array.isArray(day.records) ? day.records : []).map(item => ({
      ...item,
      typeLabel: item.typeLabel || this.typeLabel(item.exerciseType),
      intensityLabel: item.intensityLabel || this.intensityLabel(item.intensity),
      icon: this.typeIcon(item.exerciseType),
      timeLabel: String(item.startTime || '').slice(0, 5),
      caloriesLabel: this.formatNumber(item.caloriesBurned)
    }));
    const weekly = day.weeklyCompletion || { completedDays: 0, targetDays: 4 };
    this.setData({
      status: 'success',
      refreshing: false,
      totalCaloriesLabel: this.formatNumber(day.totalCalories),
      totalDuration: Number(day.totalDurationMinutes || 0),
      records,
      recommendations: (Array.isArray(recommendations) ? recommendations : []).slice(0, 2).map((item, index) => ({
        ...item,
        tone: index ? 'sand' : 'green',
        icon: this.typeIcon(item.exerciseType),
        caloriesLabel: this.formatNumber(item.estimatedCalories)
      })),
      weekly,
      weeklyProgress: weekly.targetDays ? Math.min(Math.round(weekly.completedDays / weekly.targetDays * 100), 100) : 0
    });
  },

  retry() { return this.loadData(); },

  openAdd() {
    const now = new Date();
    const form = initialForm();
    form.startTime = this.two(now.getHours()) + ':' + this.two(now.getMinutes());
    this.setData({ sheetOpen: true, form }, () => this.updateEstimate());
  },

  openRecommendation(event) {
    const item = this.data.recommendations[Number(event.currentTarget.dataset.index)];
    if (!item) return;
    const typeIndex = TYPES.findIndex(type => type.value === item.exerciseType);
    const intensityIndex = INTENSITIES.findIndex(level => level.value === item.intensity);
    const now = new Date();
    this.setData({
      sheetOpen: true,
      form: {
        ...initialForm(),
        exerciseType: item.exerciseType,
        typeIndex,
        intensity: item.intensity,
        intensityIndex,
        durationMinutes: String(item.durationMinutes),
        caloriesBurned: String(item.estimatedCalories),
        source: 'RECOMMENDATION',
        startTime: this.two(now.getHours()) + ':' + this.two(now.getMinutes())
      },
      estimatedCalories: Number(item.estimatedCalories)
    });
  },

  openEdit(event) {
    const id = Number(event.currentTarget.dataset.id);
    const item = this.data.records.find(record => record.id === id);
    if (!item) return;
    const typeIndex = TYPES.findIndex(type => type.value === item.exerciseType);
    const intensityIndex = INTENSITIES.findIndex(level => level.value === item.intensity);
    this.setData({
      sheetOpen: true,
      form: {
        id: item.id,
        exerciseType: item.exerciseType,
        typeIndex,
        startTime: item.timeLabel,
        durationMinutes: String(item.durationMinutes),
        intensity: item.intensity,
        intensityIndex,
        caloriesBurned: String(item.caloriesBurned),
        source: item.source,
        note: item.note || ''
      },
      estimatedCalories: Number(item.caloriesBurned)
    });
  },

  closeSheet() {
    if (!this.data.submitting) this.setData({ sheetOpen: false });
  },

  stopPropagation() {},

  onTypeChange(event) {
    const typeIndex = Number(event.detail.value);
    this.setData({ 'form.typeIndex': typeIndex, 'form.exerciseType': TYPES[typeIndex].value, 'form.caloriesBurned': '' }, () => this.updateEstimate());
  },

  onIntensityChange(event) {
    const intensityIndex = Number(event.detail.value);
    this.setData({ 'form.intensityIndex': intensityIndex, 'form.intensity': INTENSITIES[intensityIndex].value, 'form.caloriesBurned': '' }, () => this.updateEstimate());
  },

  onTimeChange(event) { this.setData({ 'form.startTime': event.detail.value }); },

  onFormInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ ['form.' + field]: event.detail.value }, () => {
      if (field === 'durationMinutes') this.updateEstimate();
    });
  },

  updateEstimate() {
    const form = this.data.form;
    const rate = TYPE_RATES[form.exerciseType] || 4;
    const level = INTENSITIES[form.intensityIndex] || INTENSITIES[1];
    const minutes = Math.max(Number(form.durationMinutes || 0), 0);
    this.setData({ estimatedCalories: Math.round(rate * level.factor * minutes * 10) / 10 });
  },

  submitExercise() {
    if (this.data.submitting) return;
    const form = this.data.form;
    const minutes = Number(form.durationMinutes);
    const calories = Number(form.caloriesBurned || this.data.estimatedCalories);
    if (!Number.isFinite(minutes) || minutes < 1 || minutes > 600) {
      wx.showToast({ title: '运动时长需为 1–600 分钟', icon: 'none' });
      return;
    }
    if (!Number.isFinite(calories) || calories <= 0) {
      wx.showToast({ title: '请确认运动消耗', icon: 'none' });
      return;
    }
    const payload = {
      exerciseDate: date.getToday(),
      exerciseType: form.exerciseType,
      startTime: form.startTime,
      durationMinutes: minutes,
      intensity: form.intensity,
      caloriesBurned: calories,
      source: form.source,
      note: String(form.note || '').trim()
    };
    this.setData({ submitting: true });
    const task = form.id ? api.updateExercise(form.id, payload) : api.createExercise(payload);
    return task.then(() => {
      this.setData({ submitting: false, sheetOpen: false });
      wx.showToast({ title: form.id ? '运动已更新' : '运动已记录', icon: 'success' });
      return this.loadData({ preserve: true });
    }).catch(error => {
      this.setData({ submitting: false });
      const fields = error.fieldErrors || {};
      const first = Object.keys(fields).map(key => fields[key])[0];
      wx.showToast({ title: first || error.message || '保存失败', icon: 'none' });
    });
  },

  confirmDelete(event) {
    const id = Number(event.currentTarget.dataset.id);
    if (!id || this.data.deletingId) return;
    wx.showModal({
      title: '删除运动记录',
      content: '删除后，今日运动消耗和首页摘要会同步更新。',
      confirmText: '删除',
      confirmColor: '#B65346',
      success: result => { if (result.confirm) this.deleteRecord(id); }
    });
  },

  deleteRecord(id) {
    this.setData({ deletingId: id });
    return api.deleteExercise(id).then(() => {
      this.setData({ deletingId: null });
      wx.showToast({ title: '已删除', icon: 'success' });
      return this.loadData({ preserve: true });
    }).catch(error => {
      this.setData({ deletingId: null });
      wx.showToast({ title: error.message || '删除失败', icon: 'none' });
    });
  },

  formatDateLabel(value) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return (value.getMonth() + 1) + '月' + value.getDate() + '日 · ' + weekdays[value.getDay()];
  },
  formatNumber(value) { return String(Math.round(Number(value || 0))); },
  typeLabel(type) { const item = TYPES.find(value => value.value === type); return item ? item.label : '其他运动'; },
  intensityLabel(intensity) { const item = INTENSITIES.find(value => value.value === intensity); return item ? item.label : '中等强度'; },
  typeIcon(type) { const item = TYPES.find(value => value.value === type); return item ? item.icon : '⚡'; },
  two(value) { return ('0' + value).slice(-2); }
});
