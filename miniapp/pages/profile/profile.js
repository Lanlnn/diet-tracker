const app = getApp();
const api = require('../../services/index');

const GOALS = [
  { value: 'LOSE_FAT', label: '稳步减脂' },
  { value: 'MAINTAIN', label: '保持体重' },
  { value: 'BUILD_MUSCLE', label: '增肌塑形' }
];

Page({
  data: {
    status: 'loading',
    nickname: '',
    avatarUrl: '',
    goalType: '',
    goalLabel: '暂未设置目标',
    dailyCalorieGoal: null,
    currentWeight: null,
    targetWeight: null,
    weightDifference: null,
    streakDays: 0,
    progress: 0,
    editing: false,
    submitting: false,
    uploading: false,
    draft: {},
    goalLabels: GOALS.map(item => item.label),
    goalIndex: 0,
    carbsGoal: 225,
    proteinGoal: 90,
    fatGoal: 60,
    aiCoachEnabled: true,
    customFoodCount: 0,
    favoriteFoodCount: 0,
    exerciseCountThisWeek: 0
  },

  onLoad() {
    this._profileListener = () => this.renderProfile();
    app.onProfileUpdate(this._profileListener);
  },

  onShow() {
    this.renderProfile();
    this.refreshProfile();
  },

  onUnload() {
    app.offProfileUpdate(this._profileListener);
  },

  renderProfile() {
    const profile = app.globalData;
    const currentWeight = profile.currentWeight ? Number(profile.currentWeight) : null;
    const targetWeight = profile.targetWeight ? Number(profile.targetWeight) : null;
    this.setData({
      nickname: profile.nickname || '',
      avatarUrl: profile.avatarUrl || '',
      goalType: profile.goalType || '',
      goalLabel: this.goalLabel(profile.goalType),
      dailyCalorieGoal: profile.dailyCalorieGoal || null,
      currentWeight,
      targetWeight,
      weightDifference: currentWeight && targetWeight ? Math.abs(currentWeight - targetWeight).toFixed(1) : null,
      streakDays: profile.streakDays || 0,
      progress: this.weightProgress(currentWeight, targetWeight)
    });
  },

  refreshProfile() {
    this.setData({ status: 'loading' });
    Promise.all([app.loadProfile(true), api.getGoals(), api.getProfileSummary()])
      .then(([, goals, summary]) => this.setData({
        status: 'success',
        dailyCalorieGoal: goals.dailyCalorieGoal,
        currentWeight: goals.currentWeight,
        targetWeight: goals.targetWeight,
        goalType: goals.goalType,
        goalLabel: this.goalLabel(goals.goalType),
        carbsGoal: goals.carbsGoal,
        proteinGoal: goals.proteinGoal,
        fatGoal: goals.fatGoal,
        aiCoachEnabled: goals.aiCoachEnabled,
        progress: this.weightProgress(Number(goals.currentWeight), Number(goals.targetWeight)),
        weightDifference: goals.currentWeight && goals.targetWeight
          ? Math.abs(Number(goals.currentWeight) - Number(goals.targetWeight)).toFixed(1) : null,
        customFoodCount: summary.customFoodCount || 0,
        favoriteFoodCount: summary.favoriteFoodCount || 0,
        exerciseCountThisWeek: summary.exerciseCountThisWeek || 0,
        streakDays: Number.isFinite(Number(summary.streakDays))
          ? Number(summary.streakDays) : this.data.streakDays
      }))
      .catch(error => {
        this.setData({ status: 'error' });
        if (error && error.requestId) console.warn('资料加载失败 requestId=' + error.requestId);
      });
  },

  retry() { this.refreshProfile(); },

  openEditor() {
    const goalIndex = Math.max(0, GOALS.findIndex(item => item.value === this.data.goalType));
    this.setData({
      editing: true,
      goalIndex,
      draft: {
        nickname: this.data.nickname,
        avatarUrl: this.data.avatarUrl,
        goalType: this.data.goalType || GOALS[goalIndex].value,
        dailyCalorieGoal: this.data.dailyCalorieGoal || 2000,
        currentWeight: this.data.currentWeight || '',
        targetWeight: this.data.targetWeight || '',
        carbsGoal: this.data.carbsGoal,
        proteinGoal: this.data.proteinGoal,
        fatGoal: this.data.fatGoal,
        aiCoachEnabled: this.data.aiCoachEnabled
      }
    });
  },

  closeEditor() {
    if (!this.data.submitting && !this.data.uploading) this.setData({ editing: false });
  },

  stopPropagation() {},

  onNicknameInput(event) { this.setData({ 'draft.nickname': event.detail.value }); },
  onCalorieInput(event) { this.setData({ 'draft.dailyCalorieGoal': event.detail.value }); },
  onCurrentWeightInput(event) { this.setData({ 'draft.currentWeight': event.detail.value }); },
  onTargetWeightInput(event) { this.setData({ 'draft.targetWeight': event.detail.value }); },
  onMacroInput(event) { this.setData({ ['draft.' + event.currentTarget.dataset.field]: event.detail.value }); },
  onAiCoachChange(event) { this.setData({ 'draft.aiCoachEnabled': event.detail.value }); },

  onGoalChange(event) {
    const index = Number(event.detail.value);
    this.setData({ goalIndex: index, 'draft.goalType': GOALS[index].value });
  },

  onChooseAvatar(event) {
    const filePath = event.detail.avatarUrl;
    if (!filePath || this.data.uploading) return;
    this.setData({ uploading: true });
    api.uploadAvatar(filePath)
      .then(result => this.setData({ 'draft.avatarUrl': result.url || '', uploading: false }))
      .catch(error => {
        this.setData({ uploading: false });
        wx.showToast({ title: error.message || '头像上传失败', icon: 'none' });
      });
  },

  saveProfile() {
    if (this.data.submitting || this.data.uploading) return;
    const draft = this.data.draft;
    const nickname = String(draft.nickname || '').trim();
    if (!nickname) {
      wx.showToast({ title: '请填写昵称', icon: 'none' });
      return;
    }
    const calorieGoal = Number(draft.dailyCalorieGoal);
    const carbsGoal = Number(draft.carbsGoal);
    const proteinGoal = Number(draft.proteinGoal);
    const fatGoal = Number(draft.fatGoal);
    if (!Number.isFinite(calorieGoal) || calorieGoal < 1000 || calorieGoal > 5000) {
      wx.showToast({ title: '热量目标需为 1000–5000 千卡', icon: 'none' });
      return;
    }
    if (!Number.isFinite(carbsGoal) || carbsGoal < 0 || carbsGoal > 1000
        || !Number.isFinite(proteinGoal) || proteinGoal < 0 || proteinGoal > 500
        || !Number.isFinite(fatGoal) || fatGoal < 0 || fatGoal > 300) {
      wx.showToast({ title: '请检查三大营养素目标范围', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    const profilePayload = {
      nickname,
      avatarUrl: draft.avatarUrl || '',
      goalType: draft.goalType || '',
      dailyCalorieGoal: calorieGoal,
      currentWeight: draft.currentWeight === '' ? null : Number(draft.currentWeight),
      targetWeight: draft.targetWeight === '' ? null : Number(draft.targetWeight)
    };
    const goalPayload = {
      dailyCalorieGoal: calorieGoal,
      carbsGoal,
      proteinGoal,
      fatGoal,
      currentWeight: profilePayload.currentWeight,
      targetWeight: profilePayload.targetWeight,
      goalType: profilePayload.goalType || 'MAINTAIN',
      aiCoachEnabled: Boolean(draft.aiCoachEnabled)
    };
    app.updateProfile(profilePayload).then(() => api.updateGoals(goalPayload)).then(goals => {
      this.setData({
        carbsGoal: goals.carbsGoal,
        proteinGoal: goals.proteinGoal,
        fatGoal: goals.fatGoal,
        aiCoachEnabled: goals.aiCoachEnabled
      });
      this.setData({ submitting: false, editing: false, status: 'success' });
      wx.showToast({ title: '资料已保存', icon: 'success' });
    }).catch(error => {
      this.setData({ submitting: false });
      const fieldErrors = error && error.fieldErrors;
      const firstFieldError = fieldErrors && Object.keys(fieldErrors).map(key => fieldErrors[key])[0];
      wx.showToast({ title: firstFieldError || error.message || '保存失败，请重试', icon: 'none' });
    });
  },

  goalLabel(value) {
    const goal = GOALS.find(item => item.value === value);
    return goal ? goal.label : '暂未设置目标';
  },

  weightProgress(current, target) {
    if (!current || !target || current === target) return current && target ? 100 : 0;
    if (current > target) return Math.max(8, Math.min(92, Math.round((target / current) * 100)));
    return Math.max(8, Math.min(92, Math.round((current / target) * 100)));
  },

  openHistory() { wx.navigateTo({ url: '/pages/history/history' }); },
  openFoodLibrary() { wx.switchTab({ url: '/pages/add/add' }); },
  openTraining() { wx.switchTab({ url: '/pages/exercise/exercise' }); },
  openSettings() { wx.navigateTo({ url: '/packageProfile/pages/settings/settings' }); },
  openAiCoach() {
    wx.showModal({
      title: 'AI 教练',
      content: this.data.aiCoachEnabled ? '已开启规则化饮食与运动建议。建议仅供日常管理参考，不替代医疗意见。' : '可在身体与营养目标中开启规则化建议。',
      showCancel: false,
      confirmText: '知道了'
    });
  },
  openDesignSystem() { wx.navigateTo({ url: '/packageTools/pages/design-system/design-system' }); },
  unavailable() { wx.showToast({ title: '后续里程碑开放', icon: 'none' }); }
});
