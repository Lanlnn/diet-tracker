import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/context.js'
import { Icon } from './Icons.jsx'

const foodRoles = new Set(['SUPER_ADMIN', 'FOOD_EDITOR'])
const nav = [
  { to: '/', label: '阶段总览', icon: 'grid' },
  { to: '/foods', label: '系统食品', icon: 'food', roles: foodRoles },
  { to: '/categories', label: '食品分类', icon: 'folder', roles: foodRoles },
  { to: '/custom-foods', label: '私有食品诊断', icon: 'search', roles: new Set(['SUPER_ADMIN', 'FOOD_EDITOR', 'SUPPORT_VIEWER']) },
  { to: '/security', label: '系统与安全', icon: 'shield' },
]
const titles = { '/': '阶段总览', '/foods': '系统食品', '/categories': '食品分类', '/custom-foods': '私有食品诊断', '/security': '系统与安全' }

export function AppShell() {
  const { session, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const location = useLocation()
  const allowed = nav.filter((item) => !item.roles || item.roles.has(session.role))
  return <div className="app-shell">
    <aside className={`sidebar ${open ? 'open' : ''}`}>
      <div className="brand"><span className="brand-mark"><svg viewBox="0 0 24 24"><path d="M6.3 17.7C2.7 14.1 4 7.8 9.2 6.1c.3 2.6 1.8 4.3 4.5 5.2-2.6.5-4.4 2.7-4.8 5.4"/><path d="M11.3 19.5c1-4.3 3.3-7.9 7.2-10.7.6 5.4-1.7 9-7.2 10.7Z"/></svg></span><span><strong>轻养</strong><small>管理后台</small></span></div>
      <nav aria-label="主导航"><p>当前交付 · A2</p>{allowed.map((item) => <NavLink key={item.to} to={item.to} end={item.to === '/'} onClick={() => setOpen(false)}><Icon name={item.icon}/><span>{item.label}</span></NavLink>)}</nav>
      <div className="sidebar-note"><Icon name="shield"/><span><strong>管理身份独立</strong><small>不复用小程序登录态</small></span></div>
      <div className="profile"><span className="avatar">{session.displayName.slice(0, 1)}</span><span><strong>{session.displayName}</strong><small>{session.role}</small></span><button type="button" aria-label="退出登录" onClick={logout}><Icon name="logout"/></button></div>
    </aside>
    <div className="main-shell">
      <header className="topbar"><button className="icon-button menu" type="button" aria-label="打开导航" onClick={() => setOpen((v) => !v)}><Icon name="menu"/></button><span>管理后台</span><Icon name="chevron" size={14}/><strong>{titles[location.pathname] || '页面'}</strong><div className="env"><i/>A0 契约已冻结</div></header>
      <main><Outlet /></main>
    </div>
    {open ? <button className="nav-backdrop" aria-label="关闭导航" onClick={() => setOpen(false)}/> : null}
  </div>
}
