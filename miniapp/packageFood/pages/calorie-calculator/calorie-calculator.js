const api = require('../../../services/index');
const { getToday } = require('../../../shared/date');
const { MEAL_TYPE_OPTIONS } = require('../../../shared/meal-types');
const { calculateNutrition } = require('../../../shared/nutrition');

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
    foodId: 0,
    food: null,
    foodInitial: '食',
    iconPath: '',
    status: 'loading',
    errorMessage: '',
    amountInput: '150',
    amountError: '',
    nutrition: { calories: 0, protein: 0, fat: 0, carbs: 0 },
    quickAmounts: [100, 150, 200],
    mealOptions: MEAL_TYPE_OPTIONS,
    mealIndex: 1,
    note: '',
    submitting: false
  },

  onLoad(options) {
    const foodId = Number(options.id || 0);
    this.setData({ foodId });
    this.loadFood();
  },

  onHide() {
    this.persistCalculatorDraft();
  },

  onUnload() {
    this.persistCalculatorDraft();
  },

  loadFood() {
    if (!this.data.foodId) {
      this.setData({ status: 'error', errorMessage: '食品参数无效' });
      return Promise.resolve();
    }
    this.setData({ status: 'loading', errorMessage: '' });
    return api.getFood(this.data.foodId).then(food => {
      const draft = this.readDraft(food.id);
      const patch = {
        food,
        foodInitial: String(food.name || '食').slice(0, 1),
        iconPath: FOOD_ICONS[food.name] || '',
        status: 'success',
        amountInput: draft ? String(draft.amount) : this.defaultAmount(food),
        mealIndex: draft ? Math.max(0, MEAL_TYPE_OPTIONS.findIndex(item => item.value === draft.mealType)) : this.defaultMealIndex(),
        note: draft ? draft.note || '' : ''
      };
      this.setData(patch, () => this.updatePreview());
    }).catch(error => {
      this.setData({ status: 'error', errorMessage: error.message || '食品信息加载失败，请稍后重试' });
    });
  },

  defaultAmount(food) {
    const serving = Number(food.servingAmount);
    return serving >= 1 && serving <= 10000 ? String(serving) : '150';
  },

  defaultMealIndex() {
    const hour = new Date().getHours();
    if (hour < 10) return 0;
    if (hour < 15) return 1;
    if (hour < 21) return 2;
    return 3;
  },

  readDraft(foodId) {
    try {
      const calculatorDraft = wx.getStorageSync('m4CalculatorDraft:' + foodId);
      if (calculatorDraft) return calculatorDraft;
      const pendingDraft = wx.getStorageSync('m4PendingMealDraft');
      return pendingDraft && Number(pendingDraft.foodId) === Number(foodId) ? pendingDraft : null;
    } catch (error) {
      return null;
    }
  },

  persistCalculatorDraft() {
    if (!this.data.food || !this.data.foodId) return;
    const meal = this.data.mealOptions[this.data.mealIndex];
    try {
      wx.setStorageSync('m4CalculatorDraft:' + this.data.foodId, {
        foodId: this.data.foodId,
        amount: this.data.amountInput,
        mealType: meal.value,
        note: this.data.note
      });
    } catch (error) {}
  },

  onAmountInput(event) {
    this.setData({ amountInput: event.detail.value }, () => this.updatePreview());
  },

  adjustAmount(event) {
    const delta = Number(event.currentTarget.dataset.delta || 0);
    const current = Number(this.data.amountInput);
    const next = Math.min(10000, Math.max(1, (Number.isFinite(current) ? current : 0) + delta));
    this.setAmount(next);
  },

  selectQuickAmount(event) {
    this.setAmount(Number(event.currentTarget.dataset.amount));
  },

  selectServing() {
    if (!this.data.food || !this.data.food.servingAmount) return;
    this.setAmount(Number(this.data.food.servingAmount));
  },

  setAmount(amount) {
    this.setData({ amountInput: String(amount) }, () => this.updatePreview());
  },

  updatePreview() {
    const nutrition = calculateNutrition(this.data.food, this.data.amountInput);
    this.setData({ nutrition, amountError: nutrition.error });
  },

  onMealChange(event) {
    this.setData({ mealIndex: Number(event.detail.value) });
  },

  onNoteInput(event) {
    this.setData({ note: event.detail.value });
  },

  submitDraft() {
    if (this.data.submitting || this.data.status !== 'success') return;
    const preview = calculateNutrition(this.data.food, this.data.amountInput);
    if (preview.error) {
      this.setData({ amountError: preview.error });
      return;
    }
    this.setData({ submitting: true });
    api.calculateFood(this.data.foodId, preview.amount).then(snapshot => {
      const meal = this.data.mealOptions[this.data.mealIndex];
      const draft = {
        foodId: this.data.foodId,
        foodName: this.data.food.name,
        mealDate: getToday(),
        mealType: meal.value,
        mealLabel: meal.label,
        amount: Number(snapshot.amount),
        unit: snapshot.unit,
        note: String(this.data.note || '').trim(),
        nutrition: {
          calories: Number(snapshot.calories),
          protein: Number(snapshot.protein),
          fat: Number(snapshot.fat),
          carbs: Number(snapshot.carbs)
        }
      };
      wx.setStorageSync('m4PendingMealDraft', draft);
      this.setData({ submitting: false, nutrition: draft.nutrition });
      wx.showToast({ title: '计算结果已确认', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    }).catch(error => {
      const fieldKeys = error.fieldErrors ? Object.keys(error.fieldErrors) : [];
      const fieldMessage = error.fieldErrors && (error.fieldErrors.amount || error.fieldErrors[fieldKeys[0]]);
      this.setData({ submitting: false, amountError: fieldMessage || '' });
      wx.showToast({ title: fieldMessage || error.message || '确认失败，请重试', icon: 'none' });
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
