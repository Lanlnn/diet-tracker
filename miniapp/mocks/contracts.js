// M1 local UI contract examples. They contain no account or production data.
module.exports = Object.freeze({
  profile: { nickname: '轻养用户', avatarUrl: '' },
  dailyStats: { totalCalories: 1260, totalProtein: 62, totalFat: 44, totalCarbs: 158, recordCount: 4 },
  exerciseSummary: { completedMinutes: 20, burnedCalories: 90 },
  pageStates: ['idle', 'loading', 'success', 'empty', 'error', 'submitting']
});
