Component({
  properties: { state: { type: String, value: 'empty' }, title: String, message: String },
  methods: { retry() { this.triggerEvent('retry'); } }
});
