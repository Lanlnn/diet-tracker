const api = require('../../services/index');
const date = require('../../shared/date');
const { MEAL_TYPE_MAP } = require('../../shared/meal-types');
 const app = getApp();

Page({
  data: {
    today: '',
    greeting: '',
    nickname: '',
    avatarUrl: '',
     editWechatNickname: '',
    records: [],
    mealTypes: [],
    stats: { totalCalories: 0, totalProtein: 0, totalFat: 0, totalCarbs: 0 }
    ,pageState: 'loading',
    errorMessage: ''
  },

  onLoad() {
    this.loadProfile();
     // 注册资料变更通知
     this._profileListener = () => this.loadProfile();
     app.onProfileUpdate(this._profileListener);
    const now = new Date();
    const hour = now.getHours();
    let greeting = '你好！';
    if (hour < 6) greeting = '夜深了，早点休息 🌙';
    else if (hour < 9) greeting = '早安！记得吃早餐哦 🌅';
    else if (hour < 12) greeting = '上午好 ☀️';
    else if (hour < 14) greeting = '中午好！吃午饭了吗 🍚';
    else if (hour < 18) greeting = '下午好 🌤';
    else greeting = '晚上好 🌆';

    this.setData({
      today: date.formatDate(now),
      greeting
    });
  },

   onUnload() {
     app.offProfileUpdate(this._profileListener);
   },
 
  loadProfile() {
    const gd = app.globalData;
    this.setData({
      nickname: gd.nickname || '',
      avatarUrl: gd.avatarUrl || '',
      needsWechatInfo: gd.needsWechatInfo
    });
  },

  onShow() {
    this.loadProfile();
    this.loadData();
  },

  /** 新微信：选择头像按钮回调 */
  onChooseAvatar(e) {
     const url = e.detail && e.detail.avatarUrl ? e.detail.avatarUrl : '';
     this.setData({ avatarUrl: url });
   },
 
   /** 备用：手动输入昵称 */
   onManualNicknameInput(e) {
     this.setData({ manualNickname: e.detail.value });
   },
 
   /** 备用：手动输入头像URL */
   onManualAvatarInput(e) {
     this.setData({ manualAvatarUrl: e.detail.value });
   },

  /** 保存通过微信组件获取到的昵称和头像 */
  onSaveWechatInfo() {
    const that = this;
     // 优先用微信组件获取的值，退化到手动输入
     const nicknameInput = this.data.editWechatNickname || this.data.manualNickname || '';
    const avatarUrl = this.data.avatarUrl || '';
     
     if (!nicknameInput && !this.data.manualNickname) {
       wx.showToast({ title: '请填写昵称', icon: 'none' });
       return;
     }
     
     const finalNickname = nicknameInput || '微信用户';
    app.saveWechatInfo(finalNickname, avatarUrl).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' });
    }).catch(() => wx.showToast({ title: '保存失败', icon: 'none' }));
   },

  /** 微信昵称输入框回调 */
  onNicknameInput(e) {
     this.setData({ editWechatNickname: e.detail && e.detail.value ? e.detail.value : '' });
  },

  loadData() {
    const today = date.getToday();
    this.setData({ pageState: 'loading', errorMessage: '' });
    Promise.all([api.getRecords(today), api.getDailyStats(today)]).then(([records, stats]) => {
      const mealTypes = this.groupByMealType(records || []);
      this.setData({ records: records || [], mealTypes, stats: stats || {}, pageState: records.length ? 'success' : 'empty' });
    }).catch(error => this.setData({ pageState: 'error', errorMessage: error.message || '加载失败' }));
  },

  groupByMealType(records) {
    const groups = { breakfast: [], lunch: [], dinner: [], snack: [] };
    records.forEach(r => {
      if (groups[r.mealType]) groups[r.mealType].push({
        ...r,
        displayName: r.foodNameSnapshot || (r.foodItem && r.foodItem.name) || '食物',
        displayCalories: this.recordNutrition(r, 'calories').toFixed(0)
      });
    });

    return Object.keys(MEAL_TYPE_MAP).map(type => {
      const items = groups[type] || [];
      const totalCalories = items.reduce((sum, r) =>
        sum + this.recordNutrition(r, 'calories'), 0);
      return {
        type,
        label: MEAL_TYPE_MAP[type].label,
        tagClass: MEAL_TYPE_MAP[type].class,
        records: items,
        totalCalories: totalCalories.toFixed(0)
      };
    });
  },

  recordNutrition(record, field) {
    const snapshot = Number(record[field + 'Snapshot']);
    const base = Number(record.baseAmountSnapshot);
    if (Number.isFinite(snapshot) && base > 0) return snapshot * Number(record.quantity || 0) / base;
    return Number(record.foodItem && record.foodItem[field] || 0) * Number(record.quantity || 0);
  },

  openMeal(event) {
    const mealType = event.currentTarget.dataset.meal;
    wx.navigateTo({ url: '/packageFood/pages/meal-detail/meal-detail?date=' + date.getToday() + '&mealType=' + mealType });
  }
});
