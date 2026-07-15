import { Icon } from './Icons.jsx'

export function StateView({ state, title, detail, requestId, onRetry }) {
  if (state === 'loading') return <div className="skeleton-stack" aria-label="正在加载"><i/><i/><i/><i/></div>
  const icon = state === 'forbidden' ? 'lock' : state === 'empty' ? 'search' : 'alert'
  return <div className={`state-view ${state}`} role={state === 'error' ? 'alert' : 'status'}>
    <span className="state-icon"><Icon name={icon} size={22}/></span>
    <h2>{title}</h2><p>{detail}</p>
    {requestId ? <code>请求 ID：{requestId}</code> : null}
    {onRetry ? <button className="button secondary" type="button" onClick={onRetry}>重新加载</button> : null}
  </div>
}
