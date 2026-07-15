import { Link } from 'react-router-dom'
import { useAuth } from '../auth/context.js'
import { Icon } from '../components/Icons.jsx'

export function OverviewPage() {
  const { session } = useAuth()
  return <><header className="page-head"><div><span>后台交付阶段</span><h1>A2 · 系统食品后台 MVP</h1><p>契约、安全基础与食品数据闭环已接入真实管理 API。</p></div></header>
    <section className="stage-rail"><article className="done"><b>A0</b><div><strong>契约冻结</strong><p>映射、OpenAPI、审计 ADR 与事件口径</p></div><Icon name="check"/></article><article className="done"><b>A1</b><div><strong>安全与骨架</strong><p>独立会话、RBAC、审计和统一状态</p></div><Icon name="check"/></article><article className="current"><b>A2</b><div><strong>食品后台 MVP</strong><p>分类、系统食品、预览和私有食品诊断</p></div><span>当前</span></article></section>
    <section className="overview-grid"><article className="focus-panel"><header><div><h2>当前可操作范围</h2><p>依据 {session.role} 角色展示</p></div><Icon name="shield"/></header><ul><li><Icon name="check"/>系统食品分页、筛选、新增与编辑</li><li><Icon name="check"/>食品分类维护及引用保护</li><li><Icon name="check"/>150g 营养计算同口径预览</li><li><Icon name="check"/>用户私有食品只读诊断</li></ul>{['SUPER_ADMIN','FOOD_EDITOR'].includes(session.role) ? <Link className="button primary" to="/foods">进入系统食品</Link> : null}</article>
    <article className="boundary-panel"><h2>阶段边界</h2><p>用户历史饮食快照、运动记录和私有食品均不可由后台修改。</p><div><span>尚未开放</span><strong>用户诊断扩展 · 产品质量看板 · 规则发布</strong><small>等待对应小程序里程碑与真实数据基础。</small></div></article></section></>
}
