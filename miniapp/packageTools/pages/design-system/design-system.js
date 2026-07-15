Page({
  data: { segment: 'day', segments: [{ label: '今日', value: 'day' }, { label: '本周', value: 'week' }, { label: '本月超长标签测试', value: 'month' }] },
  changeSegment(event) { this.setData({ segment: event.detail.value }); }
});
