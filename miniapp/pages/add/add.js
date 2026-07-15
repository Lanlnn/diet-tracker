const api = require('../../services/index');

const SCOPES = [
  { value: 'common', label: '常用' },
  { value: 'recent', label: '最近' },
  { value: 'favorite', label: '收藏' }
];

const FOOD_ICONS = {
  鸡胸肉: '/assets/foods/chicken.png',
  水煮蛋: '/assets/foods/egg.png',
  鸡蛋: '/assets/foods/egg.png',
  米饭: '/assets/foods/rice.png',
  西兰花: '/assets/foods/broccoli.png',
  无糖酸奶: '/assets/foods/milk.png',
  酸奶: '/assets/foods/milk.png'
};

Page({
  data: {
    scopes: SCOPES,
    activeScope: 'common',
    keyword: '',
    status: 'loading',
    errorMessage: '',
    foods: [],
    page: 0,
    hasMore: false,
    loadingMore: false,
    recentCombination: null,
    customOpen: false,
    submitting: false,
    customFood: {
      name: '', calories: '', protein: '', fat: '', carbs: ''
    }
  },

  onLoad() {
    this._requestVersion = 0;
    this.loadFoods(true);
    this.loadRecentCombination();
  },

  onUnload() {
    if (this._searchTimer) clearTimeout(this._searchTimer);
  },

  goBack() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  onSearchInput(event) {
    const keyword = event.detail.value;
    this.setData({ keyword });
    if (this._searchTimer) clearTimeout(this._searchTimer);
    this._searchTimer = setTimeout(() => this.loadFoods(true), 300);
  },

  clearSearch() {
    if (this._searchTimer) clearTimeout(this._searchTimer);
    this.setData({ keyword: '' }, () => this.loadFoods(true));
  },

  selectScope(event) {
    const scope = event.currentTarget.dataset.scope;
    if (!scope || scope === this.data.activeScope) return;
    this.setData({ activeScope: scope, keyword: '' }, () => this.loadFoods(true));
  },

  loadFoods(reset) {
    const version = ++this._requestVersion;
    const page = reset ? 0 : this.data.page + 1;
    const keyword = String(this.data.keyword || '').trim();
    if (reset) this.setData({ status: 'loading', foods: [], page: 0, errorMessage: '' });
    else this.setData({ loadingMore: true });

    const task = keyword
      ? api.searchFood(keyword, page, 20)
      : api.getFoods({ scope: this.data.activeScope, page, size: 20 });

    task.then(result => {
      if (version !== this._requestVersion) return;
      const next = (result.items || []).map(item => this.presentFood(item));
      const foods = reset ? next : this.data.foods.concat(next);
      this.setData({
        foods,
        page: result.page || page,
        hasMore: Boolean(result.hasMore),
        loadingMore: false,
        status: foods.length ? 'success' : 'empty'
      });
    }).catch(error => {
      if (version !== this._requestVersion) return;
      this.setData({
        status: reset ? 'error' : this.data.status,
        loadingMore: false,
        errorMessage: error.message || '食品加载失败，请稍后重试'
      });
    });
  },

  loadMore() {
    if (!this.data.hasMore || this.data.loadingMore) return;
    this.loadFoods(false);
  },

  loadRecentCombination() {
    api.getFoods({ scope: 'recent', page: 0, size: 2 }).then(result => {
      const items = result.items || [];
      if (!items.length) return;
      this.setData({
        recentCombination: {
          names: items.map(item => item.name).join(' · '),
          calories: items.reduce((sum, item) => sum + Number(item.calories || 0), 0).toFixed(0),
          foodId: items[0].id,
          foodName: items[0].name
        }
      });
    }).catch(() => {});
  },

  presentFood(food) {
    return {
      ...food,
      iconPath: FOOD_ICONS[food.name] || '',
      initial: String(food.name || '食').slice(0, 1),
      basisLabel: this.formatNumber(food.baseAmount || 100) + (food.baseUnit || 'g'),
      calorieLabel: this.formatNumber(food.calories || 0)
    };
  },

  formatNumber(value) {
    const number = Number(value || 0);
    return Number.isInteger(number) ? String(number) : number.toFixed(1);
  },

  selectFood(event) {
    const id = event.currentTarget.dataset.id;
    const name = event.currentTarget.dataset.name || '';
    wx.navigateTo({
      url: '/packageFood/pages/calorie-calculator/calorie-calculator?id=' + id + '&name=' + encodeURIComponent(name)
    });
  },

  toggleFavorite(event) {
    const id = Number(event.currentTarget.dataset.id);
    const index = this.data.foods.findIndex(item => item.id === id);
    if (index < 0) return;
    const current = this.data.foods[index];
    api.setFoodFavorite(id, !current.favorite).then(updated => {
      if (this.data.activeScope === 'favorite' && !updated.favorite && !this.data.keyword) {
        const foods = this.data.foods.filter(item => item.id !== id);
        this.setData({ foods, status: foods.length ? 'success' : 'empty' });
      } else {
        this.setData({ ['foods[' + index + '].favorite']: updated.favorite });
      }
      wx.showToast({ title: updated.favorite ? '已收藏' : '已取消收藏', icon: 'none' });
    }).catch(error => wx.showToast({ title: error.message || '操作失败', icon: 'none' }));
  },

  openPhoto() {
    wx.showModal({
      title: '拍照留存',
      content: '照片仅用于饮食留存，不进行食物识别或热量估算。该能力将在后续阶段接入。',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  openCustomFood() {
    this.setData({ customOpen: true });
  },

  closeCustomFood() {
    if (!this.data.submitting) this.setData({ customOpen: false });
  },

  stopPropagation() {},

  onCustomInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ ['customFood.' + field]: event.detail.value });
  },

  submitCustomFood() {
    if (this.data.submitting) return;
    const value = this.data.customFood;
    const name = String(value.name || '').trim();
    if (!name) {
      wx.showToast({ title: '请输入食物名称', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    api.addFoodItem({
      name,
      baseAmount: 100,
      baseUnit: 'g',
      unit: 'g',
      calories: Number(value.calories || 0),
      protein: Number(value.protein || 0),
      fat: Number(value.fat || 0),
      carbs: Number(value.carbs || 0)
    }).then(food => {
      this.setData({
        submitting: false,
        customOpen: false,
        keyword: name,
        customFood: { name: '', calories: '', protein: '', fat: '', carbs: '' }
      }, () => this.loadFoods(true));
      wx.showToast({ title: '自定义食物已创建', icon: 'success' });
    }).catch(error => {
      this.setData({ submitting: false });
      const fields = error.fieldErrors || {};
      const first = Object.keys(fields).map(key => fields[key])[0];
      wx.showToast({ title: first || error.message || '创建失败', icon: 'none' });
    });
  }
});
