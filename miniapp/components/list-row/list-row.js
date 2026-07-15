Component({ properties: { title: String, description: String, value: String }, methods: { tap() { this.triggerEvent('tap'); } } });
