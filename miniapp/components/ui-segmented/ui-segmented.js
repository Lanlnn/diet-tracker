Component({
  properties: { options: { type: Array, value: [] }, value: { type: String, value: '' } },
  methods: { select(event) { const value = event.currentTarget.dataset.value; this.triggerEvent('change', { value }); } }
});
