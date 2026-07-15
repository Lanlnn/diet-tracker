import { Link } from 'react-router-dom'
import { useAuth } from '../auth/context.js'
import { Icon } from '../components/Icons.jsx'

export function OverviewPage() {
  const { session } = useAuth()
  return <><header className="page-head"><div><span>后台交付阶段</span><h1>A3 · 用户只读诊断</h1><p>食品数据闭环之上，新增今日汇总与历史餐次的受审计支持工具。</p></div></header>
    <section className="stage-rail"><article className="done"><b>A1</b><div><strong>安全与骨架</strong><p>独立会话、RBAC、审计和统一状态</p></div><Icon name="check"/></article><article className="done"><b>A2</b><div><strong>食品后台 MVP</strong><p>分类、系统食品、预览和私有食品诊断</p></div><Icon name="check"/></article><article className="current"><b>A3</b><div><strong>用户只读诊断</strong><p>今日同口径对账与历史营养快照</p></div><span>当前</span></article></section>
    <section className="overview-grid"><article className="focus-panel"><header><div><h2>当前可访问范围</h2><p>依据 {session.role} 角色展示</p></div><Icon name="shield"/></header><ul><li><Icon name="check"/>系统食品与分类安全维护</li><li><Icon name="check"/>小程序今日汇总同口径诊断</li><li><Icon name="check"/>用户历史餐次营养快照</li><li><Icon name="check"/>原因、对象与请求 ID 审计</li></ul>{['SUPER_ADMIN','SUPPORT_VIEWER'].includes(session.role) ? <Link className="button primary" to="/user-diagnostics">进入用户诊断</Link> : ['SUPER_ADMIN','FOOD_EDITOR'].includes(session.role) ? <Link className="button primary" to="/foods">进入系统食品</Link> : null}</article>
    <article className="boundary-panel"><h2>只读边界</h2><p>用户历史饮食快照、运动记录和私有食品均不可由后台修改。</p><div><span>明确禁止</span><strong>封禁 · 删除 · 编辑用户或餐次记录</strong><small>诊断查询只接受精确 supportRef，并记录完整审计链路。</small></div></article></section></>
}
