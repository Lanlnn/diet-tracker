const api = require('./utils/api');

App({
  globalData: {
    openid: null,
    token: null
  },

  // 登录状态: false(进行中) / true(完成) / 'failed'(失败)
  _loginReady: false,
  _loginCallbacks: [],

  onLaunch() {
    const that = this;
    const storedToken = wx.getStorageSync('token');
    if (storedToken) {
      that.globalData.token = storedToken;
      api.setToken(storedToken);
      console.log('Token found:', storedToken);
      that._loginReady = true;
      that._flushLoginCallbacks();
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
          wx.request({
            url: api.BASE_URL + '/auth/login',
            method: 'POST',
            data: { code: res.code },
            success(resp) {
              // 判空保护：后端返回异常时不覆盖已有 token
              if (resp.data && resp.data.token) {
                const token = resp.data.token;
                const openid = resp.data.openid;
                wx.setStorageSync('token', token);
                that.globalData.token = token;
                that.globalData.openid = openid;
                api.setToken(token);
                console.log('Login success, openid:', openid);
                that._loginReady = true;
              } else {
                console.error('Login failed: invalid response', resp.data);
                that._loginReady = 'failed';
                wx.showToast({ title: '登录失败', icon: 'none' });
              }
              that._flushLoginCallbacks();
            },
            fail(err) {
              console.error('Login request failed:', err);
              that._loginReady = 'failed';
              wx.showToast({ title: '网络错误', icon: 'none' });
              that._flushLoginCallbacks();
            }
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
            wx.request({
              url: api.BASE_URL + '/auth/login',
              method: 'POST',
              data: { code: res.code },
              success(resp) {
                if (resp.data && resp.data.token) {
                  const token = resp.data.token;
                  const openid = resp.data.openid;
                  wx.setStorageSync('token', token);
                  that.globalData.token = token;
                  that.globalData.openid = openid;
                  api.setToken(token);
                  that._loginReady = true;
                  console.log('Re-login success');
                  resolve(token);
                } else {
                  reject(new Error('Re-login failed: invalid response'));
                }
              },
              fail(err) { reject(err); }
            });
          } else {
            reject(new Error('wx.login failed'));
          }
        },
        fail(err) { reject(err); }
      });
    });
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
