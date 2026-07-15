Page({
  data: { completedMinutes: 0, burnedCalories: 0, recommendations: [{ name: '轻快步行', meta: '20 分钟 · 约 90 kcal' }, { name: '舒展拉伸', meta: '12 分钟 · 放松肩颈' }] },
  addExercise() { wx.showToast({ title: '运动记录将在 M7 开放', icon: 'none' }); }
});
