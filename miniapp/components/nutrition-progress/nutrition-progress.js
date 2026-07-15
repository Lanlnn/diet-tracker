Component({
  properties: { label: String, value: { type: Number, value: 0 }, target: { type: Number, value: 100 }, unit: { type: String, value: 'g' }, tone: { type: String, value: 'green' } },
  observers: { 'value,target': function(value, target) { this.setData({ percent: Math.min(100, Math.max(0, Math.round(value / Math.max(target, 1) * 100))) }); } },
  data: { percent: 0 }
});
