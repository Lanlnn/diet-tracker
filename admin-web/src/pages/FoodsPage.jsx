import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api/client.js'
import { Icon } from '../components/Icons.jsx'
import { Modal } from '../components/Modal.jsx'
import { StateView } from '../components/StateView.jsx'

const emptyFood = { name: '', categoryId: '', baseAmount: '100', baseUnit: 'g', servingAmount: '', servingUnit: '', calories: '', protein: '', fat: '', carbs: '', source: 'SYSTEM_EDITORIAL' }

function payload(form) {
  const number = (value) => value === '' ? null : Number(value)
  return { ...form, categoryId: Number(form.categoryId), baseAmount: number(form.baseAmount), servingAmount: number(form.servingAmount), calories: number(form.calories), protein: number(form.protein), fat: number(form.fat), carbs: number(form.carbs), servingUnit: form.servingUnit || null }
}

function FoodEditor({ food, categories, onClose, onSaved }) {
  const [form, setForm] = useState(() => food ? { ...food, categoryId: String(food.categoryId), servingAmount: food.servingAmount ?? '', servingUnit: food.servingUnit ?? '' } : { ...emptyFood, categoryId: String(categories[0]?.id || '') })
  const [reason, setReason] = useState(food ? '修正系统食品营养资料' : '新增已核验的系统食品')
  const [preview, setPreview] = useState(null)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const set = (key, value) => setForm((current) => ({ ...current, [key]: value }))
  async function calculate() {
    setError(null); setPreview(null)
    try { setPreview(await api('/foods/preview', { method: 'POST', body: JSON.stringify({ food: payload(form), amount: 150 }) })) }
    catch (failure) { setError(failure) }
  }
  async function submit(event) {
    event.preventDefault(); setError(null); setSubmitting(true)
    try {
      const result = await api(food ? `/foods/${food.id}` : '/foods', { method: food ? 'PUT' : 'POST', headers: { 'X-Audit-Reason': encodeURIComponent(reason) }, body: JSON.stringify(payload(form)) })
      onSaved(result)
    } catch (failure) { setError(failure) } finally { setSubmitting(false) }
  }
  return <Modal wide title={food ? `编辑 · ${food.name}` : '新增系统食品'} description="只影响未来计算，历史饮食记录继续使用保存时的营养快照。" onClose={onClose}>
    <form className="food-form" onSubmit={submit}><div className="form-grid"><label className="span-2"><span>食品名称</span><input required maxLength="100" value={form.name} onChange={(e) => set('name', e.target.value)}/></label><label><span>食品分类</span><select required value={form.categoryId} onChange={(e) => set('categoryId', e.target.value)}><option value="" disabled>请选择</option>{categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}</select></label><label><span>数据来源</span><select value={form.source} onChange={(e) => set('source', e.target.value)}><option value="SYSTEM_EDITORIAL">人工核验</option><option value="SYSTEM">系统</option><option value="SYSTEM_SEED">基线种子</option></select></label>
      <fieldset className="span-2"><legend>营养基准</legend><div><label><span>基准数量</span><input required min="0.01" step="0.01" type="number" value={form.baseAmount} onChange={(e) => set('baseAmount', e.target.value)}/></label><label><span>基准单位</span><input required maxLength="20" value={form.baseUnit} onChange={(e) => set('baseUnit', e.target.value)}/></label><label><span>每份数量</span><input min="0.01" step="0.01" type="number" value={form.servingAmount} onChange={(e) => set('servingAmount', e.target.value)}/></label><label><span>每份单位</span><input maxLength="20" value={form.servingUnit} onChange={(e) => set('servingUnit', e.target.value)}/></label></div></fieldset>
      {['calories','protein','fat','carbs'].map((key) => <label key={key}><span>{{calories:'热量 (kcal)',protein:'蛋白质 (g)',fat:'脂肪 (g)',carbs:'碳水 (g)'}[key]}</span><input required min="0" step="0.01" type="number" value={form[key]} onChange={(e) => set(key, e.target.value)}/></label>)}
      <label className="span-2"><span>审计原因</span><textarea required minLength="2" maxLength="200" value={reason} onChange={(e) => setReason(e.target.value)}/><small>将与操作者、对象和请求 ID 一并写入审计日志。</small></label></div>
      <div className="preview-strip"><div><strong>150g 同口径预览</strong><p>直接调用与小程序相同的后端营养计算服务。</p></div>{preview ? <dl><div><dt>热量</dt><dd>{preview.calories} kcal</dd></div><div><dt>蛋白质</dt><dd>{preview.protein} g</dd></div><div><dt>脂肪</dt><dd>{preview.fat} g</dd></div><div><dt>碳水</dt><dd>{preview.carbs} g</dd></div></dl> : <button className="button secondary" type="button" onClick={calculate}>计算预览</button>}</div>
      {error ? <div className="inline-error" role="alert"><Icon name="alert"/><span>{error.message}{error.requestId ? <small>请求 ID：{error.requestId}</small> : null}</span></div> : null}
      <footer><button className="button secondary" type="button" onClick={onClose}>取消</button><button className="button primary" disabled={submitting} type="submit">{submitting ? '保存中…' : '保存食品'}</button></footer>
    </form>
  </Modal>
}

export function FoodsPage() {
  const [params, setParams] = useSearchParams()
  const query = params.toString()
  const [data, setData] = useState(null); const [categories, setCategories] = useState([])
  const [state, setState] = useState('loading'); const [error, setError] = useState(null)
  const [editing, setEditing] = useState(undefined); const [notice, setNotice] = useState('')
  const filters = useMemo(() => { const value = new URLSearchParams(query); return { keyword: value.get('keyword') || '', categoryId: value.get('categoryId') || '', source: value.get('source') || '', page: value.get('page') || '0', size: '20' } }, [query])
  const [draftKeyword, setDraftKeyword] = useState(filters.keyword)
  const load = useCallback(async () => {
    setState('loading'); setError(null)
    try {
      const search = new URLSearchParams(Object.entries(filters).filter(([, value]) => value !== ''))
      const [foods, categoryData] = await Promise.all([api(`/foods?${search}`), api('/food-categories')])
      setData(foods); setCategories(categoryData); setState(foods.items.length ? 'success' : 'empty')
    } catch (failure) { setError(failure); setState(failure.status === 403 ? 'forbidden' : 'error') }
  }, [filters])
  useEffect(() => { Promise.resolve().then(load) }, [load])
  function update(next) { const value = new URLSearchParams(params); Object.entries(next).forEach(([key, item]) => item ? value.set(key, item) : value.delete(key)); value.delete('page'); setParams(value) }
  function saved(result) { setEditing(undefined); setNotice(`${result.name} 已保存，历史快照未被修改。`); load() }
  return <><header className="page-head"><div><span>食品数据</span><h1>系统食品</h1><p>仅展示和维护公共系统食品；用户私有食品不在此列表。</p></div><button className="button primary" type="button" onClick={() => setEditing(null)}><Icon name="plus"/>新增食品</button></header>
    {notice ? <div className="success-banner" role="status"><Icon name="check"/><span>{notice}</span><button aria-label="关闭提示" onClick={() => setNotice('')}><Icon name="close"/></button></div> : null}
    <section className="toolbar"><form onSubmit={(e) => { e.preventDefault(); update({ keyword: draftKeyword }) }}><label className="search-input"><Icon name="search"/><input placeholder="搜索系统食品" value={draftKeyword} onChange={(e) => setDraftKeyword(e.target.value)}/></label></form><select aria-label="按分类筛选" value={filters.categoryId} onChange={(e) => update({ categoryId: e.target.value })}><option value="">全部分类</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><select aria-label="按来源筛选" value={filters.source} onChange={(e) => update({ source: e.target.value })}><option value="">全部来源</option><option value="SYSTEM_EDITORIAL">人工核验</option><option value="SYSTEM_SEED">基线种子</option><option value="SYSTEM">系统</option></select><span className="result-count">{data ? `共 ${data.total} 条` : '正在读取'}</span></section>
    {state === 'success' ? <section className="table-panel"><div className="table-scroll"><table><thead><tr><th>食品</th><th>分类 / 来源</th><th>营养基准</th><th>热量</th><th>三大营养素</th><th>更新时间</th><th><span className="sr-only">操作</span></th></tr></thead><tbody>{data.items.map((food) => <tr key={food.id}><td><strong>{food.name}</strong><small>#{food.id}</small></td><td><span>{food.categoryName}</span><small>{food.source}</small></td><td>{food.baseAmount}{food.baseUnit}{food.servingAmount ? <small>每份 {food.servingAmount}{food.servingUnit}</small> : null}</td><td><b className="numeric">{food.calories}</b> kcal</td><td><span className="macros">P {food.protein} · F {food.fat} · C {food.carbs}</span></td><td>{food.updatedAt ? new Date(food.updatedAt).toLocaleString('zh-CN', { hour12: false }) : '—'}</td><td><button className="row-button" type="button" onClick={() => setEditing(food)}><Icon name="edit" size={15}/>编辑</button></td></tr>)}</tbody></table></div><footer className="pagination"><button disabled={Number(filters.page) === 0} onClick={() => setParams((current) => { current.set('page', String(Number(filters.page)-1)); return current })}>上一页</button><span>第 {Number(filters.page)+1} 页</span><button disabled={!data.hasNext} onClick={() => setParams((current) => { current.set('page', String(Number(filters.page)+1)); return current })}>下一页</button></footer></section> : <StateView state={state} title={state === 'empty' ? '没有匹配的系统食品' : state === 'forbidden' ? '无权查看系统食品' : state === 'loading' ? '' : '系统食品加载失败'} detail={state === 'empty' ? '请调整筛选条件，或新增一条已核验食品。' : state === 'error' ? error?.message : ''} requestId={error?.requestId} onRetry={state === 'error' ? load : undefined}/>}
    {editing !== undefined ? <FoodEditor food={editing} categories={categories} onClose={() => setEditing(undefined)} onSaved={saved}/> : null}</>
}
