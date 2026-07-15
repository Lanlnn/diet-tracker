import { useState } from 'react'
import { api } from '../api/client.js'
import { Icon } from '../components/Icons.jsx'
import { StateView } from '../components/StateView.jsx'

const macroLabels = { carbs: '碳水', protein: '蛋白质', fat: '脂肪' }
const mealLabels = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }

function localDate() {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

function dateTime(value) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'
}

function UserSummary({ user }) {
  return <section className="support-user-card"><div><span>诊断对象</span><strong>{user.nickname || '未设置昵称'}</strong><code>{user.supportRef}</code></div><dl><div><dt>目标类型</dt><dd>{user.goalType || '未设置'}</dd></div><div><dt>每日目标</dt><dd>{user.dailyCalorieGoal ?? '默认'} kcal</dd></div><div><dt>当前 / 目标体重</dt><dd>{user.currentWeight ?? '—'} / {user.targetWeight ?? '—'} kg</dd></div><div><dt>加入时间</dt><dd>{dateTime(user.createdAt)}</dd></div></dl></section>
}

function TodaySummary({ today }) {
  const mealCount = today.meals.reduce((count, meal) => count + meal.itemCount, 0)
  const metrics = [
    ['今日摄入', today.intakeCalories, 'kcal'], ['目标', today.goalCalories, 'kcal'],
    ['剩余', today.remainingCalories, 'kcal'], ['运动', today.exerciseCalories, 'kcal'],
    ['净摄入', today.netCalories, 'kcal'], ['餐次记录', mealCount, '条'],
  ]
  return <><section className="today-metrics" aria-label="今日汇总">{metrics.map(([label, value, unit]) => <article key={label}><span>{label}</span><strong>{value}</strong><small>{unit}</small></article>)}</section><section className="macro-panel"><header><div><span>营养快照汇总</span><strong>三大营养素</strong></div><small>与小程序今日页同口径</small></header><div>{Object.entries(macroLabels).map(([key, label]) => { const metric = today.nutrition[key]; return <article key={key}><div><span>{label}</span><b>{metric.amount} / {metric.goal} g</b></div><progress max="100" value={Math.min(metric.progressPercent, 100)}/><small>{metric.progressPercent}%</small></article> })}</div></section></>
}

function MealSnapshots({ meals }) {
  if (!meals.length) return <div className="meal-empty"><Icon name="search"/><div><strong>当日没有餐次记录</strong><span>今日汇总仍按同一 DashboardService 返回零值。</span></div></div>
  return <section className="meal-snapshots"><header><div><span>保存时数据</span><strong>历史餐次营养快照</strong></div><small>系统食品后续变更不会改写下列值</small></header><div className="table-scroll"><table><thead><tr><th>餐次 / 食品</th><th>输入量</th><th>保存时营养基准</th><th>记录时间</th><th>幂等键</th></tr></thead><tbody>{meals.map((meal) => <tr key={meal.recordId}><td><strong>{mealLabels[meal.mealType] || meal.mealType} · {meal.foodName}</strong><small>记录 #{meal.recordId}</small></td><td><b>{meal.quantity}</b> {meal.unit}</td><td><span>{meal.snapshotBaseAmount}{meal.snapshotBaseUnit} · {meal.snapshotCalories} kcal</span><small>P {meal.snapshotProtein} · F {meal.snapshotFat} · C {meal.snapshotCarbs}</small></td><td>{dateTime(meal.recordTime)}<small>入库 {dateTime(meal.createdAt)}</small></td><td><code>{meal.idempotencyKey || '—'}</code></td></tr>)}</tbody></table></div></section>
}

export function UserDiagnosticsPage() {
  const [form, setForm] = useState({ supportRef: '', date: localDate(), reason: '' })
  const [submitted, setSubmitted] = useState(null)
  const [data, setData] = useState(null)
  const [state, setState] = useState('idle')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const set = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  async function query(values) {
    setSubmitted(values); setSubmitting(true); setState('loading'); setError(null); setData(null)
    const headers = { 'X-Audit-Reason': encodeURIComponent(values.reason) }
    const date = encodeURIComponent(values.date)
    const base = `/support/users/${encodeURIComponent(values.supportRef)}`
    try {
      const [user, today, meals] = await Promise.all([
        api(base, { headers }), api(`${base}/today?date=${date}`, { headers }), api(`${base}/meals?date=${date}`, { headers }),
      ])
      setData({ user, today, meals }); setState('success')
    } catch (failure) {
      setError(failure)
      setState(failure.status === 403 ? 'forbidden' : failure.status === 404 ? 'empty' : 'error')
    } finally { setSubmitting(false) }
  }

  function submit(event) { event.preventDefault(); query({ ...form }) }

  return <><header className="page-head"><div><span>A3 · 支持工具</span><h1>用户今日与餐次诊断</h1><p>精确输入 supportRef，核对指定日期的小程序汇总与保存时餐次快照。</p></div></header>
    <aside className="readonly-banner"><Icon name="shield"/><div><strong>只读诊断，不能代用户修改</strong><span>本页面不提供封禁、删除、编辑用户或餐次记录等操作；每次查询都会审计。</span></div></aside>
    <section className="support-query"><form onSubmit={submit}><label><span>supportRef</span><input required pattern="usr_[a-f0-9]{32}" placeholder="usr_…" autoComplete="off" value={form.supportRef} onChange={(event) => set('supportRef', event.target.value.trim())}/></label><label><span>诊断日期</span><input required type="date" value={form.date} onChange={(event) => set('date', event.target.value)}/></label><label className="reason-field"><span>查询原因</span><input required minLength="2" maxLength="200" placeholder="例如：核对用户反馈的今日热量" value={form.reason} onChange={(event) => set('reason', event.target.value)}/></label><button className="button primary" disabled={submitting} type="submit"><Icon name="search"/>{submitting ? '查询提交中…' : '开始只读诊断'}</button></form><small>仅支持精确编号，不提供昵称或个人信息模糊搜索。</small></section>
    <div className="support-results" aria-live="polite">{state === 'idle' ? <div className="diagnostic-idle"><Icon name="user"/><div><strong>等待输入诊断对象</strong><span>查询成功后将展示今日摄入、目标、运动、净摄入、三大营养素与历史餐次快照。</span></div></div> : state === 'loading' ? <StateView state="loading"/> : state === 'success' ? <><UserSummary user={data.user}/><TodaySummary today={data.today}/><MealSnapshots meals={data.meals}/></> : <StateView state={state} title={state === 'empty' ? '未找到该用户' : state === 'forbidden' ? '无用户诊断权限' : '诊断查询失败'} detail={state === 'empty' ? '请核对完整 supportRef；系统不会按昵称或其他资料扩大搜索。' : error?.message} requestId={error?.requestId} onRetry={state === 'error' && submitted ? () => query(submitted) : undefined}/>}</div>
  </>
}
