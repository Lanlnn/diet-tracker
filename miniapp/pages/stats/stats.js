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
    const today = util.formatDate(new Date());
    const dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const weekDays = [];
    const d = new Date();
    const day = d.getDay();
    const mondayOffset = day === 0 ? -6 : 1 - day;
    for (let i = 0; i < 7; i++) {
      const wd = new Date(d);
      wd.setDate(d.getDate() + mondayOffset + i);
      weekDays.push(util.formatDate(wd));
    }

    api.getWeeklyStats(today).then(resp => {
      const dataMap = {};
      (resp.dailyData || []).forEach(d => { dataMap[d.date] = d; });

      let maxCal = 0;
      (resp.dailyData || []).forEach(d => {
        if (d.calories > maxCal) maxCal = d.calories;
      });

      const weeklyData = weekDays.map((date, i) => {
        const dd = dataMap[date] || { calories: 0, protein: 0, fat: 0, carbs: 0 };
        const cal = dd.calories || 0;
        return {
          date,
          dayLabel: dayLabels[i],
          calories: Math.round(cal),
          height: maxCal > 0 ? Math.round(cal / maxCal * 100) : 0
        };
      });

      const weeklyTotal = {
        avgCalories: resp.avgCalories || 0,
        avgProtein: resp.avgProtein || 0,
        avgFat: resp.avgFat || 0,
        avgCarbs: resp.avgCarbs || 0
      };

      const tips = [];
      const avg = weeklyTotal.avgCalories;
      if (avg === 0) {
        tips.push('No data yet, start recording your meals!');
      } else {
        if (avg > 2500) tips.push('Daily calories high, aim for 2000kcal');
        else if (avg < 1200) tips.push('Daily calories low, ensure nutrition');
        else tips.push('Daily calories in range, keep it up!');
        if (weeklyTotal.avgProtein < 50) tips.push('Protein low, eat more fish, meat, eggs');
        if (weeklyTotal.avgFat > 80) tips.push('Fat high, watch oily foods');
        if (weeklyTotal.avgCarbs > 300) tips.push('Carbs high, reduce portions');
      }

      this.setData({ weeklyData, weeklyTotal, tips });
    }).catch(err => {
      console.error('Weekly stats error:', err);
      this.setData({
        tips: ['Could not load data, check network or login status']
      });
    });
  }
});
