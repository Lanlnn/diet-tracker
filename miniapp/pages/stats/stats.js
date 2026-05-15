const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    weeklyData: [],
    weeklyTotal: { avgCalories: 0, avgProtein: 0, avgFat: 0, avgCarbs: 0 },
    tips: []
  },

  onShow() {
    this.loadWeeklyStats();
  },

  loadWeeklyStats() {
    const today = new Date();
    const weekStart = new Date(today);
    const day = weekStart.getDay();
    const diff = weekStart.getDate() - day + (day === 0 ? -6 : 1);
    weekStart.setDate(diff);

    const weekDates = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(weekStart);
      d.setDate(d.getDate() + i);
      weekDates.push(util.formatDate(d));
    }

    const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

    // 获取这周每天的统计数据
    const promises = weekDates.map(date =>
      api.getDailyStats(date).catch(() => ({
        totalCalories: 0, totalProtein: 0, totalFat: 0, totalCarbs: 0
      }))
    );
    Promise.all(promises).then(results => {
      let totalCalories = 0, totalProtein = 0, totalFat = 0, totalCarbs = 0;
      let daysWithData = 0;
      let maxCal = 0;

      results.forEach((stat, i) => {
        const cal = stat.totalCalories || 0;
        if (cal > 0) daysWithData++;
        totalCalories += cal;
        totalProtein += stat.totalProtein || 0;
        totalFat += stat.totalFat || 0;
        totalCarbs += stat.totalCarbs || 0;
        if (cal > maxCal) maxCal = cal;
      });

      const weeklyData = results.map((stat, i) => {
        const cal = stat.totalCalories || 0;
        return {
          date: weekDates[i],
          dayLabel: dayLabels[i],
          calories: Math.round(cal),
          height: maxCal > 0 ? Math.round(cal / maxCal * 100) : 0
        };
      });

      const count = daysWithData || 1;
      const weeklyTotal = {
        avgCalories: Math.round(totalCalories / count),
        avgProtein: Math.round(totalProtein / count * 10) / 10,
        avgFat: Math.round(totalFat / count * 10) / 10,
        avgCarbs: Math.round(totalCarbs / count * 10) / 10
      };

      const tips = [];
      const avg = weeklyTotal.avgCalories;
      if (avg === 0) {
        tips.push('还没有记录数据，开始记录你的饮食吧！');
      } else {
        if (avg > 2500) tips.push('⚠️ 日均热量偏高，建议控制在 2000kcal 左右');
        else if (avg < 1200) tips.push('💪 日均热量偏低，注意保证基础营养');
        else tips.push('✅ 日均热量在合理范围内，继续保持！');

        if (weeklyTotal.avgProtein < 50) tips.push('🥩 蛋白质摄入偏少，建议多吃鱼、肉、蛋、豆制品');
        if (weeklyTotal.avgFat > 80) tips.push('🥑 脂肪摄入偏高，注意控制油膩食物');
        if (weeklyTotal.avgCarbs > 300) tips.push('🍚 碳水摄入偏高，可适当减少主食量');
      }

      this.setData({ weeklyData, weeklyTotal, tips });
    });
  }
});
