const api = require('../../services/index');

const RANGE_OPTIONS = [
  { value: '7d', label: '7 天' },
  { value: '30d', label: '30 天' },
  { value: '90d', label: '90 天' }
];
const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六'];

Page({
  data: {
    rangeOptions: RANGE_OPTIONS,
    selectedRange: '7d',
    pageState: 'loading',
    refreshing: false,
    errorMessage: '',
    periodLabel: '',
    averageNetLabel: '0',
    averageIntakeLabel: '0',
    averageExerciseLabel: '0',
    achievementRate: 0,
    comparisonLabel: '',
    comparisonTone: 'steady',
    chartData: [],
    chartRangeClass: 'chart--7d',
    accessibilitySummary: '',
    summaries: []
  },

  onLoad() {
    this._requestVersion = 0;
    this._loadedOnce = false;
  },

  onShow() {
    this.loadTrend({ preserve: this._loadedOnce });
  },

  onHide() {
    this._requestVersion += 1;
  },

  onRangeChange(event) {
    const selectedRange = event.detail.value;
    if (!selectedRange || selectedRange === this.data.selectedRange) return;
    this.setData({ selectedRange });
    return this.loadTrend({ preserve: true });
  },

  loadTrend(options = {}) {
    const requestVersion = ++this._requestVersion;
    const range = this.data.selectedRange;
    const preserve = Boolean(options.preserve && this.data.pageState === 'success');
    this.setData(preserve
      ? { refreshing: true, errorMessage: '' }
      : { pageState: 'loading', refreshing: false, errorMessage: '' });

    return api.getTrend(range).then(response => {
      if (requestVersion !== this._requestVersion || range !== this.data.selectedRange) return;
      this._loadedOnce = true;
      this.presentTrend(response || {});
    }).catch(error => {
      if (requestVersion !== this._requestVersion || range !== this.data.selectedRange) return;
      const message = error.message || '趋势数据加载失败';
      if (preserve) {
        this.setData({ refreshing: false, errorMessage: message });
        wx.showToast({ title: '刷新失败，已保留当前趋势', icon: 'none' });
      } else {
        this.setData({ pageState: 'error', refreshing: false, errorMessage: message });
      }
    });
  },

  presentTrend(response) {
    const source = Array.isArray(response.dailyData) ? response.dailyData : [];
    const maxNet = source.reduce((max, item) => Math.max(max, Number(item.netCalories || 0)), 0);
    const chartData = source.map((item, index) => {
      const net = Number(item.netCalories || 0);
      const hasData = Boolean(item.hasData);
      const height = hasData && maxNet > 0 ? Math.max(10, Math.round(Math.max(net, 0) / maxNet * 100)) : 4;
      return {
        ...item,
        netLabel: this.formatNumber(net),
        intakeLabel: this.formatNumber(item.intakeCalories),
        exerciseLabel: this.formatNumber(item.exerciseCalories),
        dayLabel: this.dayLabel(item.date, source.length),
        height,
        heightStep: Math.max(1, Math.min(20, Math.round(height / 5))),
        tone: index === source.length - 1 ? 'current' : (hasData ? 'recorded' : 'empty'),
        ariaLabel: `${item.date}，摄入 ${this.formatNumber(item.intakeCalories)} 千卡，运动 ${this.formatNumber(item.exerciseCalories)} 千卡，净摄入 ${this.formatNumber(net)} 千卡`
      };
    });
    const change = response.netChangePercent;
    const hasChange = change !== null && change !== undefined;
    const summaries = (Array.isArray(response.summaries) ? response.summaries : []).slice(0, 2)
      .map((item, index) => ({ ...item, icon: index ? '↗' : '✦', tone: index ? 'green' : 'sand' }));

    this.setData({
      pageState: 'success',
      refreshing: false,
      errorMessage: '',
      periodLabel: this.periodLabel(response.startDate, response.endDate),
      averageNetLabel: this.formatNumber(response.averageNetIntake),
      averageIntakeLabel: this.formatNumber(response.averageIntake),
      averageExerciseLabel: this.formatNumber(response.averageExercise),
      achievementRate: Number(response.nutritionAchievementRate || 0),
      comparisonLabel: hasChange ? `较上一周期 ${change > 0 ? '+' : ''}${change}%` : '暂无周期对比',
      comparisonTone: !hasChange || change === 0 ? 'steady' : (change < 0 ? 'down' : 'up'),
      chartData,
      chartRangeClass: `chart--${this.data.selectedRange}`,
      accessibilitySummary: response.accessibilitySummary || '暂无趋势文字摘要',
      summaries
    });
  },

  retry() {
    return this.loadTrend();
  },

  dayLabel(value, length) {
    const parts = String(value || '').split('-').map(Number);
    if (parts.length !== 3) return '';
    if (length <= 7) return WEEKDAYS[new Date(parts[0], parts[1] - 1, parts[2]).getDay()];
    return `${parts[1]}/${parts[2]}`;
  },

  periodLabel(start, end) {
    if (!start || !end) return '';
    return `${String(start).slice(5).replace('-', '.')} – ${String(end).slice(5).replace('-', '.')}`;
  },

  formatNumber(value) {
    const number = Number(value || 0);
    return Math.round(number).toLocaleString('en-US');
  }
});
