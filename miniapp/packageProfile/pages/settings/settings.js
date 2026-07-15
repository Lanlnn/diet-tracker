const app = getApp();
const api = require('../../../services/index');

Page({
  data: { deleting: false },

  showNotifications() {
    wx.showModal({ title: '通知设置', content: '轻养仅在你授权后发送记录提醒，可随时在微信系统设置中关闭。', showCancel: false });
  },

  showPrivacy() {
    wx.showModal({
      title: '隐私说明',
      content: '昵称和头像用于展示；饮食、运动与目标数据用于生成个人统计。数据不会用于食物识别或医疗诊断。你可以删除账号及全部业务数据。',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  showAbout() {
    wx.showModal({ title: '关于轻养', content: '轻养 1.0 · 饮食与运动记录工具\n健康建议仅供参考，不替代专业医疗意见。', showCancel: false });
  },

  deleteAccount() {
    if (this.data.deleting) return;
    wx.showModal({
      title: '永久删除账号？',
      content: '将删除个人资料、目标、饮食、运动、自定义食品、收藏和已上传头像，且无法恢复。',
      confirmText: '继续删除',
      confirmColor: '#A8443C',
      success: first => {
        if (!first.confirm) return;
        wx.showModal({
          title: '最后确认',
          content: '再次确认永久删除全部账号数据。',
          confirmText: '永久删除',
          confirmColor: '#A8443C',
          success: second => second.confirm && this.confirmDelete()
        });
      }
    });
  },

  confirmDelete() {
    this.setData({ deleting: true });
    api.deleteAccount().then(() => {
      app.clearDeletedAccount();
      wx.showToast({ title: '账号数据已删除', icon: 'success' });
      setTimeout(() => wx.reLaunch({ url: '/pages/index/index' }), 500);
    }).catch(error => {
      this.setData({ deleting: false });
      wx.showToast({ title: error.message || '删除失败，请重试', icon: 'none' });
    });
  }
});
