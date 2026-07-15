import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useAuth } from './auth/context.js'
import { AppShell } from './components/AppShell.jsx'
import { StateView } from './components/StateView.jsx'
import { LoginPage } from './pages/LoginPage.jsx'
import { OverviewPage } from './pages/OverviewPage.jsx'
import { SecurityPage } from './pages/SecurityPage.jsx'
import { FoodsPage } from './pages/FoodsPage.jsx'
import { CategoriesPage } from './pages/CategoriesPage.jsx'
import { CustomFoodsPage } from './pages/CustomFoodsPage.jsx'

const foodRoles = new Set(['SUPER_ADMIN', 'FOOD_EDITOR'])

function Protected() {
  const { status } = useAuth(); const location = useLocation()
  if (status === 'loading') return <div className="boot-state"><span className="brand-mark">叶</span><p>正在验证管理会话…</p></div>
  if (status !== 'authenticated') return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <AppShell />
}
function RoleRoute({ roles, children }) {
  const { session } = useAuth()
  return roles.has(session.role) ? children : <StateView state="forbidden" title="无权访问" detail="当前角色没有此页面对应的管理权限。" />
}
export default function App() {
  return <Routes><Route path="/login" element={<LoginPage/>}/><Route element={<Protected/>}>
    <Route index element={<OverviewPage/>}/><Route path="foods" element={<RoleRoute roles={foodRoles}><FoodsPage/></RoleRoute>}/><Route path="categories" element={<RoleRoute roles={foodRoles}><CategoriesPage/></RoleRoute>}/><Route path="custom-foods" element={<RoleRoute roles={new Set(['SUPER_ADMIN','FOOD_EDITOR','SUPPORT_VIEWER'])}><CustomFoodsPage/></RoleRoute>}/><Route path="security" element={<SecurityPage/>}/><Route path="*" element={<Navigate to="/" replace/>}/>
  </Route></Routes>
}
