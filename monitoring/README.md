# M10 监控与告警门禁

生产 `prod` Profile 暴露 `/actuator/health` 与 `/actuator/prometheus`。Prometheus 必须以 `job="diet-tracker"` 抓取指标，并加载 [`alerts.yml`](./alerts.yml)。指标端点应只向监控网络开放，不应直接暴露到公网。

告警阈值与 PRD、发布回滚手册一致：健康检查连续失败、API 5xx 超过 2%、首页 P95 超过 800ms、搜索 P95 超过 500ms、日历或 90 天趋势 P95 超过 1.2s，以及 429 比例异常升高。

CI 使用 `promtool check rules` 验证规则语法；真实 MySQL 8 作业还运行关键 API 的可重复 P95 基线。上线验收仍需在目标环境确认 Prometheus 能抓到指标，并向实际值班渠道发送一条测试告警，记录接收人与恢复时间。
