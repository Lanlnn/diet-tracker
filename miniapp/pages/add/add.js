const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    date: util.getToday(),
    mealTypeOptions: [
      { label: '早餐', value: 'breakfast' },
      { label: '午餐', value: 'lunch' },
      { label: '晚餐', value: 'dinner' },
      { label: '加餐', value: 'snack' }
    ],
    mealTypeIndex: 0,
    categories: [],
    categoryIndex: 0,
    foods: [],
    selectedFood: null,
    quantity: 1,

    // 自定义食物模式
    customMode: false,
    customFoodName: '',
    customUnit: '份',
    customCalories: '',
    customProtein: '',
    customFat: '',
    customCarbs: ''
  },

  onLoad() {
    this.loadCategories();
  },

  loadCategories() {
    api.getCategories().then(categories => {
      this.setData({ categories: categories || [] });
      if (categories && categories.length > 0) {
        this.loadFoods(categories[0].id);
      }
    }).catch(() => {
      wx.showToast({ title: '加载分类失败', icon: 'none' });
    });
  },

  loadFoods(categoryId) {
    api.getFoods(categoryId).then(foods => {
      foods = foods || [];
      this.setData({ foods, selectedFood: foods.length > 0 ? foods[0] : null });
    }).catch(() => {
      this.setData({ foods: [], selectedFood: null });
    });
  },

  onDateChange(e) {
    this.setData({ date: e.detail.value });
  },

  onMealTypeChange(e) {
    this.setData({ mealTypeIndex: e.detail.value });
  },

  onCategoryChange(e) {
    const idx = e.detail.value;
    this.setData({ categoryIndex: idx });
    const cat = this.data.categories[idx];
    if (cat) this.loadFoods(cat.id);
  },

  onFoodChange(e) {
    const idx = e.detail.value;
    this.setData({ selectedFood: this.data.foods[idx] });
  },

  toggleCustom() {
    this.setData({ customMode: !this.data.customMode });
  },

  onCustomNameInput(e) {
    this.setData({ customFoodName: e.detail.value });
  },

  onCustomUnitInput(e) {
    this.setData({ customUnit: e.detail.value || '份' });
  },

  onCustomCaloriesInput(e) {
    this.setData({ customCalories: e.detail.value });
  },

  onCustomProteinInput(e) {
    this.setData({ customProtein: e.detail.value });
  },

  onCustomFatInput(e) {
    this.setData({ customFat: e.detail.value });
  },

  onCustomCarbsInput(e) {
    this.setData({ customCarbs: e.detail.value });
  },

  decreaseQty() {
    if (this.data.quantity > 0.5) {
      this.setData({ quantity: this.data.quantity - 0.5 });
    }
  },

  increaseQty() {
    this.setData({ quantity: this.data.quantity + 0.5 });
  },

  onQtyInput(e) {
    this.setData({ quantity: parseFloat(e.detail.value) || 1 });
  },

  submitRecord() {
    const that = this;
    const { date, mealTypeIndex, quantity, customMode, customFoodName, customUnit,
            customCalories, customProtein, customFat, customCarbs } = this.data;

    if (customMode) {
      // 自定义食物模式
      if (!customFoodName.trim()) {
        wx.showToast({ title: '请输入食物名称', icon: 'none' });
        return;
      }

      wx.showLoading({ title: '保存中...' });

      // 先创建自定义食物
      api.addFoodItem({
        name: customFoodName.trim(),
        unit: customUnit,
        calories: parseFloat(customCalories) || 0,
        protein: parseFloat(customProtein) || 0,
        fat: parseFloat(customFat) || 0,
        carbs: parseFloat(customCarbs) || 0
      }).then(food => {
        // 再创建饮食记录
        return api.addRecord({
          mealDate: date,
          mealType: that.data.mealTypeOptions[mealTypeIndex].value,
          foodItem: { id: food.id },
          quantity: quantity,
          unit: customUnit,
          recordTime: new Date().toISOString()
        });
      }).then(() => {
        wx.hideLoading();
        wx.showToast({ title: '记录成功 🎉', icon: 'success' });
        setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 1500);
      }).catch(() => {
        wx.hideLoading();
        wx.showToast({ title: '保存失败', icon: 'none' });
      });
    } else {
      // 已有食物模式
      if (!this.data.selectedFood) {
        wx.showToast({ title: '请选择食物', icon: 'none' });
        return;
      }

      api.addRecord({
        mealDate: date,
        mealType: this.data.mealTypeOptions[mealTypeIndex].value,
        foodItem: { id: this.data.selectedFood.id },
        quantity: quantity,
        unit: this.data.selectedFood.unit,
        recordTime: new Date().toISOString()
      }).then(() => {
        wx.showToast({ title: '记录成功 🎉', icon: 'success' });
        setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 1500);
      }).catch(() => {
        wx.showToast({ title: '记录失败', icon: 'none' });
      });
    }
  }
});
