const root = document.documentElement;
const sidebar = document.querySelector('#sidebar');
const menuButton = document.querySelector('#menuButton');
const overlay = document.querySelector('#overlay');
const themeButton = document.querySelector('#themeButton');
const ruleModal = document.querySelector('#ruleModal');
const toast = document.querySelector('#toast');
let toastTimer;

function showToast(message) {
  window.clearTimeout(toastTimer);
  toast.querySelector('span').textContent = message;
  toast.hidden = false;
  toastTimer = window.setTimeout(() => { toast.hidden = true; }, 3500);
}

function setTheme(theme) {
  root.dataset.theme = theme;
  themeButton.setAttribute('aria-label', theme === 'dark' ? '切换浅色主题' : '切换深色主题');
  localStorage.setItem('admin-theme', theme);
}

setTheme(localStorage.getItem('admin-theme') || 'light');
themeButton.addEventListener('click', () => setTheme(root.dataset.theme === 'dark' ? 'light' : 'dark'));

function closeSidebar() {
  sidebar.classList.remove('is-open');
  menuButton.setAttribute('aria-expanded', 'false');
  overlay.hidden = true;
}

menuButton.addEventListener('click', () => {
  const opening = !sidebar.classList.contains('is-open');
  sidebar.classList.toggle('is-open', opening);
  menuButton.setAttribute('aria-expanded', String(opening));
  overlay.hidden = !opening;
});

document.querySelectorAll('.nav-item').forEach((item) => {
  item.addEventListener('click', (event) => {
    document.querySelectorAll('.nav-item').forEach((nav) => nav.classList.remove('is-active'));
    item.classList.add('is-active');
    if (!document.querySelector(item.getAttribute('href'))) {
      event.preventDefault();
      showToast(`${item.querySelector('span:nth-child(2)').textContent}模块为本次视觉范围预留`);
    }
    if (window.innerWidth <= 860) closeSidebar();
  });
});

document.querySelectorAll('.segmented button').forEach((button) => {
  button.addEventListener('click', () => {
    document.querySelectorAll('.segmented button').forEach((item) => item.classList.remove('is-selected'));
    button.classList.add('is-selected');
    const labels = { today: '今日', '7d': '近 7 天', '30d': '近 30 天' };
    showToast(`已切换为${labels[button.dataset.period]}数据`);
  });
});

document.querySelector('#exportButton').addEventListener('click', () => {
  showToast('周报已生成，原型环境不执行真实下载');
});

function openModal() {
  ruleModal.hidden = false;
  overlay.hidden = false;
  window.setTimeout(() => ruleModal.querySelector('input').focus(), 0);
}

function closeModal() {
  ruleModal.hidden = true;
  overlay.hidden = true;
  document.querySelector('#createRuleButton').focus();
}

document.querySelector('#createRuleButton').addEventListener('click', openModal);
document.querySelectorAll('.close-modal').forEach((button) => button.addEventListener('click', closeModal));
overlay.addEventListener('click', () => {
  if (!ruleModal.hidden) closeModal();
  else closeSidebar();
});

document.addEventListener('keydown', (event) => {
  if (event.key === 'Escape') {
    if (!ruleModal.hidden) closeModal();
    else closeSidebar();
  }
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    document.querySelector('#globalSearch').focus();
  }
});

document.querySelector('#ruleForm').addEventListener('submit', (event) => {
  event.preventDefault();
  const name = new FormData(event.currentTarget).get('name');
  closeModal();
  showToast(`“${name}”已保存为草稿`);
  event.currentTarget.reset();
});

document.querySelectorAll('.row-action').forEach((button) => {
  button.addEventListener('click', () => showToast(`已打开“${button.dataset.food}”审核详情`));
});

document.querySelector('.sort-button').addEventListener('click', (event) => {
  const tbody = document.querySelector('#foodTableBody');
  const rows = [...tbody.querySelectorAll('tr')];
  const descending = event.currentTarget.dataset.direction !== 'desc';
  rows.sort((a, b) => (Number(a.dataset.calories) - Number(b.dataset.calories)) * (descending ? -1 : 1));
  rows.forEach((row) => tbody.appendChild(row));
  event.currentTarget.dataset.direction = descending ? 'desc' : 'asc';
  showToast(`已按热量${descending ? '从高到低' : '从低到高'}排序`);
});

document.querySelector('.chart-data-toggle').addEventListener('click', (event) => {
  const table = document.querySelector('#chartTable');
  table.hidden = !table.hidden;
  event.currentTarget.setAttribute('aria-expanded', String(!table.hidden));
  event.currentTarget.firstChild.textContent = table.hidden ? '查看数据表' : '收起数据表';
});

document.querySelector('#globalSearch').addEventListener('keydown', (event) => {
  if (event.key === 'Enter' && event.currentTarget.value.trim()) {
    showToast(`正在搜索“${event.currentTarget.value.trim()}”`);
  }
});
