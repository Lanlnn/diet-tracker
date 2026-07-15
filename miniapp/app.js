const api = require('./services');

App({
  globalData: {
    openid: null,
    token: null,
    nickname: '',
     avatarUrl: '',
     needsWechatInfo: false
  },

  // 登录状态: false(进行中) / true(完成) / 'failed'(失败)
  _loginReady: false,
  _loginCallbacks: [],
   _profileListeners: [],

  onLaunch() {
    const that = this;
    const storedToken = wx.getStorageSync('token');
     const storedNickname = wx.getStorageSync('nickname') || '';
     const storedAvatarUrl = wx.getStorageSync('avatarUrl') || '';
    if (storedToken) {
      that.globalData.token = storedToken;
       that.globalData.nickname = storedNickname;
       that.globalData.avatarUrl = storedAvatarUrl;
      api.setToken(storedToken);
      console.log('Token found:', storedToken);
      that._loginReady = true;
      that._flushLoginCallbacks();
      // 后台刷新一下最新的用户资料
      that._loadProfile();
       // 如果本地没有缓存昵称，尝试自动获取微信信息
       if (!storedNickname) {
         that._fetchWechatInfo();
       }
      return;
    }

    that._doLogin();
  },

  /** 内部登录流程 */
  _doLogin() {
    const that = this;
    wx.login({
      success(res) {
        if (res.code) {
          api.login(res.code)
            .then(data => {
              // 判空保护：后端返回异常时不覆盖已有 token
                if (data && data.token) {
                  const token = data.token;
                  const openid = data.openid;
                   const nickname = data.nickname || '';
                   const avatarUrl = data.avatarUrl || '';
                  wx.setStorageSync('token', token);
                   wx.setStorageSync('nickname', nickname);
                   wx.setStorageSync('avatarUrl', avatarUrl);
                  that.globalData.token = token;
                  that.globalData.openid = openid;
                   that.globalData.nickname = nickname;
                   that.globalData.avatarUrl = avatarUrl;
                  api.setToken(token);
                  console.log('Login success, openid:', openid);
                  that._loginReady = true;
                } else {
                console.error('Login failed: invalid response', data);
                that._loginReady = 'failed';
                wx.showToast({ title: '登录失败', icon: 'none' });
              }
              that._flushLoginCallbacks();

               // 登录成功后，如果用户还没有昵称，自动获取微信信息
               if (data && data.token && !data.nickname) {
                 that._fetchWechatInfo();
               }
            })
            .catch(err => {
              console.error('Login request failed:', err);
              that._loginReady = 'failed';
              wx.showToast({ title: '网络错误', icon: 'none' });
              that._flushLoginCallbacks();
            });
        } else {
          console.error('wx.login failed, no code');
          that._loginReady = 'failed';
          that._flushLoginCallbacks();
        }
      },
      fail(err) {
        console.error('wx.login failed:', err);
        that._loginReady = 'failed';
        that._flushLoginCallbacks();
      }
    });
  },

  /** 供 pages/模块等待登录完成，返回 Promise<token> */
  getLoginReady() {
    const that = this;
    return new Promise((resolve, reject) => {
      if (that._loginReady === true) {
        resolve(that.globalData.token);
      } else if (that._loginReady === 'failed') {
        reject(new Error('Login failed'));
      } else {
        that._loginCallbacks.push({ resolve, reject });
      }
    });
  },

  /** 重新登录（用于 401 自动重试） */
  reLogin() {
    const that = this;
    wx.removeStorageSync('token');
    that.globalData.token = null;
    that.globalData.openid = null;
    api.setToken('');
    that._loginReady = false;

    console.log('Re-login triggered');
    return new Promise((resolve, reject) => {
      wx.login({
        success(res) {
          if (res.code) {
            api.login(res.code)
              .then(data => {
                if (data && data.token) {
                  const token = data.token;
                  const openid = data.openid;
                   const nickname = data.nickname || '';
                   const avatarUrl = data.avatarUrl || '';
                  wx.setStorageSync('token', token);
                   wx.setStorageSync('nickname', nickname);
                   wx.setStorageSync('avatarUrl', avatarUrl);
                  that.globalData.token = token;
                  that.globalData.openid = openid;
                   that.globalData.nickname = nickname;
                   that.globalData.avatarUrl = avatarUrl;
                  api.setToken(token);
                  that._loginReady = true;
                  console.log('Re-login success');
                  resolve(token);
                   // 重新登录后也尝试获取微信信息
                   if (!nickname) {
                     that._fetchWechatInfo();
                   }
                } else {
                  reject(new Error('Re-login failed: invalid response'));
                }
              })
              .catch(reject);
          } else {
            reject(new Error('wx.login failed'));
          }
        },
        fail(err) { reject(err); }
      });
    });
  },

   /** 对外暴露：监听用户资料变更 */
   onProfileUpdate(callback) {
     this._profileListeners.push(callback);
   },
 
   /** 对外暴露：取消监听 */
   offProfileUpdate(callback) {
     const idx = this._profileListeners.indexOf(callback);
     if (idx >= 0) this._profileListeners.splice(idx, 1);
   },
 
   /** 通知所有监听者资料已变更 */
   _notifyProfileListeners() {
     const callbacks = this._profileListeners.slice();
     callbacks.forEach(cb => {
       try { cb(); } catch (e) {}
     });
   },

  /** 自动获取微信用户信息 */
  _fetchWechatInfo() {
    const that = this;
    if (typeof wx.getUserProfile === 'function') {
      wx.getUserProfile({
        desc: '用于展示用户头像和昵称',
        success(res) {
          const userInfo = res.userInfo;
          if (userInfo) {
            api.updateProfile({
              nickname: userInfo.nickName,
              avatarUrl: userInfo.avatarUrl
            }).then(profile => {
              that.globalData.nickname = profile.nickname || '';
              that.globalData.avatarUrl = profile.avatarUrl || '';
              wx.setStorageSync('nickname', profile.nickname || '');
              wx.setStorageSync('avatarUrl', profile.avatarUrl || '');
              that.globalData.needsWechatInfo = false;
               that._notifyProfileListeners();
              console.log('WeChat profile auto-fetched:', profile.nickname);
            }).catch(() => {});
          }
        },
        fail() {
          // getUserProfile 不可用或被拒绝，在首页显示微信信息获取按钮
          console.log('getUserProfile failed, fallback to button');
          that.globalData.needsWechatInfo = true;
           that._notifyProfileListeners();
        }
      });
    } else {
      // 新版微信不支持 getUserProfile，在首页用 chooseAvatar + nickname
      console.log('getUserProfile not available, fallback to button');
      that.globalData.needsWechatInfo = true;
       that._notifyProfileListeners();
    }
  },

   /** 保存微信获取到的用户信息 */
   saveWechatInfo(nickName, avatarUrl) {
     const that = this;
     api.updateProfile({
       nickname: nickName,
       avatarUrl: avatarUrl
     }).then(profile => {
       that.globalData.nickname = profile.nickname || '';
       that.globalData.avatarUrl = profile.avatarUrl || '';
      that.globalData.needsWechatInfo = false;
      wx.setStorageSync('nickname', profile.nickname || '');
      wx.setStorageSync('avatarUrl', profile.avatarUrl || '');
       that._notifyProfileListeners();
      console.log('WeChat info saved:', profile.nickname);
    }).catch(err => {
      console.error('Failed to save WeChat info', err);
     });
   },

  /** 后台刷新用户资料 */
  _loadProfile() {
    const that = this;
    api.getProfile().then(profile => {
      if (profile.nickname !== undefined) {
        that.globalData.nickname = profile.nickname || '';
        that.globalData.avatarUrl = profile.avatarUrl || '';
        wx.setStorageSync('nickname', profile.nickname || '');
        wx.setStorageSync('avatarUrl', profile.avatarUrl || '');
        that.globalData.needsWechatInfo = false;
      }
       that._notifyProfileListeners();
    }).catch(() => {});
  },

  _flushLoginCallbacks() {
    const that = this;
    const callbacks = that._loginCallbacks.slice();
    that._loginCallbacks = [];
    callbacks.forEach(cb => {
      if (that._loginReady === true) {
        cb.resolve(that.globalData.token);
      } else {
        cb.reject(new Error('Login failed'));
      }
    });
  }
});
