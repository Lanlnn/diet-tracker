const paths = {
  grid: <><rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/></>,
  food: <><path d="M3 11h18M5 11a7 7 0 0 0 14 0M8 7c0-2 1-3 1-4M12 7c0-2 1-3 1-4M16 7c0-2 1-3 1-4"/></>,
  folder: <path d="M3 6h6l2 2h10v11H3Z"/>,
  search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
  shield: <path d="M12 3 4 6v5c0 5 3.4 8.4 8 10 4.6-1.6 8-5 8-10V6Z"/>,
  user: <><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></>,
  logout: <><path d="M10 5H4v14h6M14 8l4 4-4 4M8 12h10"/></>,
  plus: <path d="M12 5v14M5 12h14"/>,
  edit: <><path d="m4 20 4-1 10-10-3-3L5 16Z"/><path d="m13 8 3 3"/></>,
  chevron: <path d="m9 18 6-6-6-6"/>,
  alert: <><circle cx="12" cy="12" r="9"/><path d="M12 7v6M12 17h.01"/></>,
  check: <path d="m5 12 4 4L19 6"/>,
  close: <path d="m6 6 12 12M18 6 6 18"/>,
  menu: <path d="M4 6h16M4 12h16M4 18h16"/>,
  lock: <><rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></>,
}

export function Icon({ name, size = 18, className = '' }) {
  return <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>
}
