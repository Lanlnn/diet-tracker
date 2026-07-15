const api = require('./services/index');

App({
  globalData: {
    token: '',
    nickname: '',
    avatarUrl: '',
    goalType: '',
    dailyCalorieGoal: null,
    currentWeight: null,
    targetWeight: null,
    streakDays: 0,
    needsWechatInfo: false
  },

  _loginPromise: null,
  _refreshPromise: null,
  _profilePromise: null,
  _profileListeners: [],

  onLaunch() {
    const token = wx.getStorageSync('token') || '';
    this.globalData.token = token;
    this.globalData.nickname = wx.getStorageSync('nickname') || '';
    this.globalData.avatarUrl = wx.getStorageSync('avatarUrl') || '';
    this.globalData.needsWechatInfo = !this.globalData.nickname;
    api.setToken(token);
    this.ensureLogin().then(() => this.loadProfile()).catch(() => {});
  },

  ensureLogin() {
    if (this.globalData.token) return Promise.resolve(this.globalData.token);
    if (!this._loginPromise) {
      this._loginPromise = this._performLogin().finally(() => {
        this._loginPromise = null;
      });
    }
    return this._loginPromise;
  },

  reLogin() {
    if (!this._refreshPromise) {
      this._clearSession();
      this._refreshPromise = this.ensureLogin().finally(() => {
        this._refreshPromise = null;
      });
    }
    return this._refreshPromise;
  },

  _performLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: result => result.code ? resolve(result.code) : reject(new Error('WX_LOGIN_FAILED')),
        fail: () => reject(new Error('WX_LOGIN_FAILED'))
      });
    }).then(code => api.login(code)).then(data => {
      if (!data || !data.token) throw new Error('INVALID_LOGIN_RESPONSE');
      this._saveSession(data);
      return data.token;
    });
  },

  _saveSession(data) {
    const profile = data.user || data;
    const nickname = profile.nickname || '';
    const avatarUrl = profile.avatarUrl || '';
    this.globalData.token = data.token;
    this.globalData.nickname = nickname;
    this.globalData.avatarUrl = avatarUrl;
    this.globalData.needsWechatInfo = !nickname;
    api.setToken(data.token);
    wx.setStorageSync('token', data.token);
    wx.setStorageSync('nickname', nickname);
    wx.setStorageSync('avatarUrl', avatarUrl);
    this._saveProfile(profile);
  },

  _clearSession() {
    this.globalData.token = '';
    api.setToken('');
    wx.removeStorageSync('token');
  },

  onProfileUpdate(callback) {
    if (typeof callback === 'function' && !this._profileListeners.includes(callback)) {
      this._profileListeners.push(callback);
    }
  },

  offProfileUpdate(callback) {
    this._profileListeners = this._profileListeners.filter(item => item !== callback);
  },

  _notifyProfileListeners() {
    this._profileListeners.slice().forEach(callback => callback());
  },

  saveWechatInfo(nickname, avatarUrl) {
    return this.updateProfile({
      nickname,
      avatarUrl,
      goalType: this.globalData.goalType,
      dailyCalorieGoal: this.globalData.dailyCalorieGoal,
      currentWeight: this.globalData.currentWeight,
      targetWeight: this.globalData.targetWeight
    });
  },

  updateProfile(data) {
    return api.updateProfile(data).then(profile => {
      this._saveProfile(profile);
      return profile;
    });
  },

  loadProfile(force = false) {
    if (!this._profilePromise) {
      this._profilePromise = api.getProfile()
        .then(profile => {
          this._saveProfile(profile);
          return profile;
        })
        .finally(() => { this._profilePromise = null; });
    }
    return this._profilePromise;
  },

  _saveProfile(profile) {
    const nickname = profile.nickname || '';
    const avatarUrl = profile.avatarUrl || '';
    this.globalData.nickname = nickname;
    this.globalData.avatarUrl = avatarUrl;
    this.globalData.goalType = profile.goalType || '';
    this.globalData.dailyCalorieGoal = profile.dailyCalorieGoal || null;
    this.globalData.currentWeight = profile.currentWeight || null;
    this.globalData.targetWeight = profile.targetWeight || null;
    this.globalData.streakDays = profile.streakDays || 0;
    this.globalData.needsWechatInfo = !nickname;
    wx.setStorageSync('nickname', nickname);
    wx.setStorageSync('avatarUrl', avatarUrl);
    this._notifyProfileListeners();
  }
});
