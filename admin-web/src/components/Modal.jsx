import { useEffect, useRef } from 'react'
import { Icon } from './Icons.jsx'

export function Modal({ title, description, onClose, children, wide = false }) {
  const closeRef = useRef(null)
  useEffect(() => {
    closeRef.current?.focus()
    const onKey = (event) => { if (event.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose])
  return <div className="modal-layer" role="presentation">
    <button className="modal-backdrop" aria-label="关闭弹窗" onClick={onClose}/>
    <section className={`modal ${wide ? 'wide' : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <header><div><h2 id="modal-title">{title}</h2>{description ? <p>{description}</p> : null}</div>
        <button ref={closeRef} className="icon-button" type="button" aria-label="关闭" onClick={onClose}><Icon name="close"/></button>
      </header>{children}
    </section>
  </div>
}
