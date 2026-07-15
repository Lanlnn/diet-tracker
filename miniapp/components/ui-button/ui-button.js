Component({
  properties: { text: String, disabled: Boolean, loading: Boolean, variant: { type: String, value: 'primary' } },
  methods: { onTap() { if (!this.data.disabled && !this.data.loading) this.triggerEvent('tap'); } }
});
