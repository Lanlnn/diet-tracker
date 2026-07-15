Page({
  data: { foodId: null, foodName: '' },

  onLoad(options) {
    this.setData({
      foodId: Number(options.id || 0),
      foodName: decodeURIComponent(options.name || '')
    });
  },

  returnToSearch() {
    wx.navigateBack();
  }
});
