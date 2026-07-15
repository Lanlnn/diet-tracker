const api = require('../../../services/index');
const { MEAL_TYPE_MAP, MEAL_TYPE_OPTIONS } = require('../../../shared/meal-types');

const FOOD_ICONS = {
  鸡胸肉: '/assets/foods/chicken.png', 鸡蛋: '/assets/foods/egg.png', 水煮蛋: '/assets/foods/egg.png',
  米饭: '/assets/foods/rice.png', 西兰花: '/assets/foods/broccoli.png', 酸奶: '/assets/foods/milk.png',
  无糖酸奶: '/assets/foods/milk.png'
};

Page({
  data: {
    date: '', mealType: 'lunch', mealLabel: '午餐', status: 'loading', errorMessage: '', records: [],
    total: { calories: 0, protein: 0, fat: 0, carbs: 0 }, displayTime: '', advice: null,
    editMode: false, editorOpen: false, editingId: 0, amountInput: '', amountError: '',
    mealOptions: MEAL_TYPE_OPTIONS, mealIndex: 1, note: '', editingUnit: 'g', saving: false
  },

  onLoad(options) {
    const mealType = MEAL_TYPE_MAP[options.mealType] ? options.mealType : 'lunch';
    const date = /^\d{4}-\d{2}-\d{2}$/.test(options.date || '') ? options.date : this.today();
    this.setData({ date, mealType, mealLabel: MEAL_TYPE_MAP[mealType].label });
    this.loadRecords();
  },

  loadRecords() {
    this.setData({ status: 'loading', errorMessage: '' });
    return api.getRecords(this.data.date, this.data.mealType).then(records => {
      const presented = (records || []).map(record => this.presentRecord(record));
      const total = presented.reduce((sum, item) => ({
        calories: sum.calories + item.calories,
        protein: sum.protein + item.protein,
        fat: sum.fat + item.fat,
        carbs: sum.carbs + item.carbs
      }), { calories: 0, protein: 0, fat: 0, carbs: 0 });
      const rounded = {
        calories: Math.round(total.calories), protein: this.roundOne(total.protein),
        fat: this.roundOne(total.fat), carbs: this.roundOne(total.carbs)
      };
      this.setData({
        records: presented,
        total: rounded,
        displayTime: this.formatDisplayTime(records && records[0]),
        advice: this.buildAdvice(presented, rounded),
        status: presented.length ? 'success' : 'empty'
      });
    }).catch(error => this.setData({
      status: 'error', errorMessage: error.message || '餐次信息加载失败，请稍后重试'
    }));
  },

  presentRecord(record) {
    const base = Number(record.baseAmountSnapshot || 100);
    const ratio = base > 0 ? Number(record.quantity || 0) / base : 0;
    const name = record.foodNameSnapshot || (record.foodItem && record.foodItem.name) || '食物';
    return {
      ...record,
      name,
      iconPath: FOOD_ICONS[name] || '',
      initial: name.slice(0, 1),
      basisLabel: this.formatNumber(record.caloriesSnapshot) + ' 千卡/' + this.formatNumber(base) + (record.baseUnitSnapshot || 'g'),
      calories: Math.round(Number(record.caloriesSnapshot || 0) * ratio),
      protein: this.roundOne(Number(record.proteinSnapshot || 0) * ratio),
      fat: this.roundOne(Number(record.fatSnapshot || 0) * ratio),
      carbs: this.roundOne(Number(record.carbsSnapshot || 0) * ratio),
      quantityLabel: this.formatNumber(record.quantity) + (record.unit || 'g')
    };
  },

  buildAdvice(records, total) {
    if (records.length < 2 || total.calories <= 0) return null;
    if (total.protein >= 25) return { title: '这餐的蛋白质很充足', detail: '搭配深色蔬菜，能让营养结构更均衡。' };
    if (total.carbs >= 60 && total.protein < 20) return { title: '这餐可以补一点优质蛋白', detail: '可以选择鸡蛋、奶类、豆制品或瘦肉。' };
    if (total.fat >= 30) return { title: '这餐的脂肪稍高', detail: '下一餐可以优先选择清蒸、炖煮的食物。' };
    return { title: '这餐搭配比较均衡', detail: '继续保持主食、蛋白质和蔬菜的组合。' };
  },

  toggleEdit() { this.setData({ editMode: !this.data.editMode }); },

  openEditor(event) {
    if (!this.data.editMode) return;
    const id = Number(event.currentTarget.dataset.id);
    const record = this.data.records.find(item => Number(item.id) === id);
    if (!record) return;
    const mealIndex = Math.max(0, this.data.mealOptions.findIndex(item => item.value === record.mealType));
    this.setData({
      editorOpen: true, editingId: id, amountInput: this.formatNumber(record.quantity), amountError: '',
      mealIndex, note: record.note || '', editingUnit: record.unit || 'g'
    });
  },

  closeEditor() { if (!this.data.saving) this.setData({ editorOpen: false }); },
  stopPropagation() {},
  onAmountInput(event) {
    const amountInput = event.detail.value;
    this.setData({ amountInput, amountError: this.validateAmount(amountInput) });
  },
  onMealChange(event) { this.setData({ mealIndex: Number(event.detail.value) }); },
  onNoteInput(event) { this.setData({ note: event.detail.value }); },

  saveEdit() {
    if (this.data.saving) return;
    const amountError = this.validateAmount(this.data.amountInput);
    if (amountError) return this.setData({ amountError });
    const targetMeal = this.data.mealOptions[this.data.mealIndex];
    const record = this.data.records.find(item => Number(item.id) === Number(this.data.editingId));
    this.setData({ saving: true });
    api.updateRecord(this.data.editingId, {
      mealType: targetMeal.value,
      quantity: Number(this.data.amountInput),
      unit: record.unit,
      note: String(this.data.note || '').trim()
    }).then(() => {
      this.setData({ saving: false, editorOpen: false });
      wx.showToast({ title: '记录已更新', icon: 'success' });
      if (targetMeal.value !== this.data.mealType) {
        wx.redirectTo({ url: '/packageFood/pages/meal-detail/meal-detail?date=' + this.data.date + '&mealType=' + targetMeal.value });
      } else this.loadRecords();
    }).catch(error => {
      this.setData({ saving: false });
      wx.showToast({ title: error.message || '更新失败', icon: 'none' });
    });
  },

  confirmDelete() {
    if (this.data.saving) return;
    wx.showModal({
      title: '删除这条记录？', content: '删除后餐次营养汇总会立即更新。', confirmColor: '#B65346',
      success: result => { if (result.confirm) this.deleteEditingRecord(); }
    });
  },

  deleteEditingRecord() {
    this.setData({ saving: true });
    api.deleteRecord(this.data.editingId).then(() => {
      this.setData({ saving: false, editorOpen: false });
      wx.showToast({ title: '已删除', icon: 'success' });
      this.loadRecords();
    }).catch(error => {
      this.setData({ saving: false });
      wx.showToast({ title: error.message || '删除失败', icon: 'none' });
    });
  },

  continueAdding() { wx.switchTab({ url: '/pages/add/add' }); },
  goBack() { wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/index/index' }) }); },
  openMore() {
    wx.showActionSheet({
      itemList: ['返回今日', '查看饮食日历'],
      success: result => {
        if (result.tapIndex === 0) wx.switchTab({ url: '/pages/index/index' });
        else wx.navigateTo({ url: '/pages/history/history' });
      }
    });
  },
  today() {
    const now = new Date();
    const pad = value => String(value).padStart(2, '0');
    return now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate());
  },
  formatDisplayTime(record) {
    const parts = this.data.date.split('-');
    let time = '';
    if (record && record.recordTime) time = String(record.recordTime).slice(11, 16);
    return Number(parts[1]) + '月' + Number(parts[2]) + '日' + (time ? ' · ' + time : '');
  },
  validateAmount(value) {
    const text = String(value == null ? '' : value).trim();
    if (!/^\d+(\.\d{1,2})?$/.test(text) || Number(text) <= 0) return '请输入大于 0 的数量，最多 2 位小数';
    if (Number(text) > 10000) return '数量不能超过 10000';
    return '';
  },
  roundOne(value) { return Math.round((value + Number.EPSILON) * 10) / 10; },
  formatNumber(value) {
    const number = Number(value || 0);
    return Number.isInteger(number) ? String(number) : String(Math.round(number * 10) / 10);
  }
});
