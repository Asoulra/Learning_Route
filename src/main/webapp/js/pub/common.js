/**
* 学生成绩管理系统 - 公共 JS 工具库
* 提供API请求、Toast提示、Modal管理等功能
* 自动适配 Tomcat 部署上下文路径
*/

// ========================
// 自动检测 ContextPath
// ========================
// 通过分析当前页面路径推断 ContextPath
// 例如: /html/personal/admin/students.html → ""
//      /system1/html/personal/admin/students.html → "/system1"
(function() {
  const path = window.location.pathname;
  // 找 /html/ 或 /css/ 或 /js/ 在路径中的位置
  const markers = ['/html/', '/css/', '/js/', '/images/', '/api/'];
  for (const m of markers) {
    const idx = path.indexOf(m);
    if (idx > 0) {
      window.CONTEXT_PATH = path.substring(0, idx);
      return;
    }
  }
  // 默认无上下文
  window.CONTEXT_PATH = '';
})();

const BASE = window.CONTEXT_PATH;  // ContextPath
const API_BASE = BASE + '/api';     // API 根路径

// ========================
// API 配置与工具
// ========================

/**
 * 通用 API 请求函数
 * @param {string} url - 接口路径（以 /api 开头，如 '/api/auth/login'）
 * @param {object} options - 请求配置
 * @returns {Promise<any>}
 */
async function apiRequest(url, options = {}) {
  // 自动拼接 ContextPath
  url = buildUrl(url);

  const defaultHeaders = {
    'Content-Type': 'application/json',
  };

  // 从 localStorage 获取 token
  const token = localStorage.getItem('token');
  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || '请求失败');
    }

    return data;
  } catch (error) {
    console.error('API Error:', error);
    showToast(error.message || '网络错误，请稍后重试', 'error');
    throw error;
  }
}

/**
 * 拼接 URL，自动加上 ContextPath
 * - API 请求：自动补全 /api 前缀
 * - 静态资源：直接返回 BASE + path
 */
function buildUrl(path) {
  if (!path) return BASE || '/';
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  if (!path.startsWith('/')) path = '/' + path;
  // 静态资源路径直接返回
  if (path.startsWith('/html/') || path.startsWith('/css/') ||
      path.startsWith('/js/')  || path.startsWith('/images/')) {
    return BASE + path;
  }
  // API 请求：自动加上 /api 前缀（如果还没有）
  if (!path.startsWith('/api/')) {
    path = '/api' + path;
  }
  return BASE + path;
}

// ========================
// Toast 提示组件
// ========================

function showToast(message, type = 'success') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icons = {
    success: '&#10003;',
    error: '&#10007;',
    warning: '&#9888;',
  };

  toast.innerHTML = `<span>${icons[type] || ''}</span><span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(30px)';
    toast.style.transition = 'all 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// ========================
// Modal 弹窗管理
// ========================

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

// 点击遮罩关闭
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('active');
    document.body.style.overflow = '';
  }
});

// ESC 关闭弹窗
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    const activeModal = document.querySelector('.modal-overlay.active');
    if (activeModal) {
      activeModal.classList.remove('active');
      document.body.style.overflow = '';
    }
  }
});

// ========================
// 表单验证
// ========================

function validateForm(formId) {
  const form = document.getElementById(formId);
  if (!form) return false;

  const inputs = form.querySelectorAll('[required]');
  let isValid = true;

  inputs.forEach(input => {
    removeError(input);

    if (!input.value.trim()) {
      showError(input, '此字段为必填项');
      isValid = false;
    }
  });

  return isValid;
}

function showError(input, message) {
  input.style.borderColor = 'var(--danger-500)';
  const errorEl = document.createElement('div');
  errorEl.className = 'form-error';
  errorEl.textContent = message;
  input.parentNode.appendChild(errorEl);
}

function removeError(input) {
  input.style.borderColor = '';
  const existingError = input.parentNode.querySelector('.form-error');
  if (existingError) existingError.remove();
}

// ========================
// 格式化工具
// ========================

function formatDate(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
}

function formatScore(score) {
  if (score === null || score === undefined) return '-';
  return Number(score).toFixed(1);
}

// ========================
// 分页组件渲染
// ========================

function renderPagination(containerId, currentPage, totalPages, onPageChange) {
  const container = document.getElementById(containerId);
  if (!container || totalPages <= 1) {
    if (container) container.innerHTML = '';
    return;
  }

  let html = `
    <div class="pagination-info">
      第 ${currentPage} / ${totalPages} 页
    </div>
    <div class="pagination-btns">
      <button ${currentPage <= 1 ? 'disabled' : ''} onclick="${onPageChange}(${currentPage - 1})">上一页</button>
  `;

  for (let i = 1; i <= totalPages; i++) {
    if (i === 1 || i === totalPages || Math.abs(i - currentPage) <= 1) {
      html += `<button class="${i === currentPage ? 'active' : ''}" onclick="${onPageChange}(${i})">${i}</button>`;
    } else if (Math.abs(i - currentPage) === 2) {
      html += '<button disabled>...</button>';
    }
  }

  html += `
      <button ${currentPage >= totalPages ? 'disabled' : ''} onclick="${onPageChange}(${currentPage + 1})">下一页</button>
    </div>
  `;

  container.innerHTML = html;
}

// ========================
// 确认对话框
// ========================

function showConfirm(message, onConfirm) {
  const confirmed = window.confirm(message);
  if (confirmed && onConfirm) {
    onConfirm();
  }
}

// ========================
// 移动端侧边栏切换
// ========================

function toggleSidebar() {
  const sidebar = document.querySelector('.sidebar');
  if (sidebar) {
    sidebar.classList.toggle('open');
  }
}

// ========================
// 角色感知的导航栏
// ========================

const NAV_ICONS = {
  students: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>',
  classes:  '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>',
  teachers: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  assign:   '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>',
  scores:   '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
  check:    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>',
  stats:    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>',
  schedule: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/><line x1="8" y1="14" x2="8" y2="14"/><line x1="12" y1="14" x2="12" y2="14"/><line x1="16" y1="14" x2="16" y2="14"/></svg>',
};

// 管理员导航：信息管理（3 项）+ 教学安排（1 项）— 无成绩相关
const NAV_ADMIN = [
  { group: '信息管理', items: [
    { key: 'students', label: '学生管理', href: 'students.html' },
    { key: 'classes',  label: '班级管理', href: 'classes.html' },
    { key: 'teachers', label: '教师管理', href: 'teachers.html' },
  ]},
  { group: '教学安排', items: [
    { key: 'assign',   label: '授课安排', href: 'teaching-assign.html' },
  ]},
];

// 教师导航：我的工作台（成绩录入 + 成绩统计 + 我的课表）+ 信息管理（仅学生只读）
const NAV_TEACHER = [
  { group: '我的工作台', items: [
    { key: 'scores',   label: '成绩录入', href: 'scores.html' },
    { key: 'check',    label: '学生名单', href: 'check.html' },
    { key: 'stats',    label: '成绩统计', href: 'statistics.html' },
    { key: 'schedule', label: '我的课表', href: 'my-schedule.html' },
  ]},
];

/** 获取当前登录用户信息（无则跳登录页） */
function requireLogin() {
  const token = localStorage.getItem('token');
  const raw = localStorage.getItem('userInfo');
  if (!token || !raw) {
    window.location.href = BASE + '/html/pub/login/login.html';
    return null;
  }
  try { return JSON.parse(raw); } catch (e) { return null; }
}

/** 渲染侧边栏 - 在 .sidebar 容器内插入 HTML */
function renderSidebar(activeKey) {
  const user = requireLogin();
  if (!user) return;

  const isAdmin = user.role === 'admin';
  const navData = isAdmin ? NAV_ADMIN : NAV_TEACHER;
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;

  // 头部 logo
  const headerHtml = `
    <div class="sidebar-header">
      <a href="${isAdmin ? '../admin/students.html' : 'scores.html'}" class="sidebar-logo">
        <div class="logo-icon">S</div>
        <div>
          <div class="logo-text">成绩管理</div>
          <div class="logo-subtext">${isAdmin ? 'Admin Panel' : 'Teacher Panel'}</div>
        </div>
      </a>
    </div>
  `;

  // 导航项
  const navHtml = navData.map(group => `
    <div style="${group.items.some(i => i.key === activeKey) ? '' : 'margin-top:12px;'}" class="nav-label">${group.group}</div>
    ${group.items.map(item => `
      <a href="${item.href}" class="nav-item ${item.key === activeKey ? 'active' : ''}">
        ${NAV_ICONS[item.key] || ''}
        ${item.label}
      </a>
    `).join('')}
  `).join('');

  // 完整侧边栏
  sidebar.innerHTML = `
    ${headerHtml}
    <nav class="nav-section">${navHtml}</nav>
  `;
}

/** 渲染顶栏用户信息 + 退出按钮 */
function renderHeaderUser() {
  const user = requireLogin();
  if (!user) return;
  const el = document.getElementById('headerUser');
  if (!el) return;
  const initial = (user.name || '?').substring(0, 1);
  const bg = user.role === 'admin'
    ? 'linear-gradient(135deg,#3b82f6,#1d4ed8)'
    : 'linear-gradient(135deg,#10b981,#059669)';
  el.innerHTML = `
    <div class="user-info">
      <div class="user-avatar" style="background:${bg}">${initial}</div>
      <span class="user-name">${user.name}</span>
    </div>
    <button class="btn-logout" onclick="handleLogout()">退出登录</button>
  `;
}

/** 初始化页面通用 UI（导航栏、用户信息、登录检查） */
function initPageUI(activeNavKey) {
  renderSidebar(activeNavKey);
  renderHeaderUser();
}

// ========================
// 退出登录
// ========================

function handleLogout() {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  window.location.href = BASE + '/html/pub/login/login.html';
}

// ========================
// 导出功能
// ========================

function downloadFile(url, filename) {
  // 拼接完整 URL
  const fullUrl = buildUrl(url);
  const link = document.createElement('a');
  link.href = fullUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

// ========================
// 文件上传预览
// ========================

function handleFileSelect(input, previewCallback) {
  const file = input.files[0];
  if (file) {
    const validTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-excel',
      'text/csv'
    ];

    if (!validTypes.includes(file.type)) {
      showToast('请上传 Excel 或 CSV 格式的文件', 'error');
      input.value = '';
      return null;
    }

    if (file.size > 10 * 1024 * 1024) {
      showToast('文件大小不能超过 10MB', 'error');
      input.value = '';
      return null;
    }

    if (previewCallback) {
      previewCallback(file);
    }
    return file;
  }
  return null;
}
