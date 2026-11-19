/**
 * Student Management System - Production Frontend Application
 * Plain Vanilla JavaScript (ES Module)
 */

const BASE_URL = '/api/v1';

// ==========================================
// API Layer (Direct Fetch with Session Cookie)
// ==========================================
export async function request(url, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include', // Automatically sends JSESSIONID cookie
  });

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  let data = null;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const error = new Error(data && data.message ? data.message : 'Request failed');
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const studentApi = {
  getStudents: (page = 0, size = 10, sort = 'id,asc') => {
    return request(`${BASE_URL}/students?page=${page}&size=${size}&sort=${sort}`);
  },

  getStudentById: (id) => {
    return request(`${BASE_URL}/students/${id}`);
  },

  createStudent: (studentData) => {
    return request(`${BASE_URL}/students`, {
      method: 'POST',
      body: JSON.stringify(studentData),
    });
  },

  updateStudent: (id, studentData) => {
    return request(`${BASE_URL}/students/${id}`, {
      method: 'PUT',
      body: JSON.stringify(studentData),
    });
  },

  deleteStudent: (id) => {
    return request(`${BASE_URL}/students/${id}`, {
      method: 'DELETE',
    });
  },

  searchStudents: (params = {}, page = 0, size = 10, sort = 'id,asc') => {
    const query = new URLSearchParams({
      page,
      size,
      sort,
    });

    if (params.name) query.append('name', params.name);
    if (params.department) query.append('department', params.department);
    if (params.email) query.append('email', params.email);
    if (params.year) query.append('year', params.year);

    return request(`${BASE_URL}/students/search?${query.toString()}`);
  },
};

export const authApi = {
  login: async (username, password) => {
    const data = await request(`${BASE_URL}/auth/login`, {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    localStorage.setItem('sms_user', username);
    return data;
  },

  checkStatus: () => {
    return request(`${BASE_URL}/auth/status`);
  },

  logout: async () => {
    try {
      await request(`${BASE_URL}/auth/logout`, { method: 'POST' });
    } finally {
      localStorage.removeItem('sms_user');
    }
  },
};

// ==========================================
// Application State
// ==========================================
const state = {
  students: [],
  pageNumber: 0,
  pageSize: 10,
  sortField: 'id',
  sortDir: 'asc',
  totalPages: 0,
  totalElements: 0,
  searchQuery: '',
  departmentFilter: '',
  yearFilter: '',
  activeStudentId: null,
  debounceTimer: null,
};

// ==========================================
// 1. Theme Management (Dark Mode)
// ==========================================
export function initTheme() {
  const savedTheme = localStorage.getItem('sms_theme');
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');

  setTheme(initialTheme);

  const themeToggleBtn = document.getElementById('themeToggleBtn');
  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', toggleTheme);
  }

  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    if (!localStorage.getItem('sms_theme')) {
      setTheme(e.matches ? 'dark' : 'light');
    }
  });
}

export function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('sms_theme', theme);
  const themeToggleBtn = document.getElementById('themeToggleBtn');
  if (themeToggleBtn) {
    themeToggleBtn.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`);
    themeToggleBtn.innerHTML = theme === 'dark' ? '☀️' : '🌙';
  }
}

export function toggleTheme() {
  const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
  const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
  setTheme(newTheme);
  showToast('Theme Changed', `Switched to ${newTheme} theme`, 'info');
}

// ==========================================
// 2. Scroll Progress Bar & Back to Top
// ==========================================
export function initScrollFeatures() {
  const progressBar = document.getElementById('scrollProgressBar');
  const backToTopBtn = document.getElementById('backToTopBtn');

  window.addEventListener('scroll', () => {
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const progress = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0;

    if (progressBar) {
      progressBar.style.width = `${progress}%`;
    }

    if (backToTopBtn) {
      if (scrollTop > 300) {
        backToTopBtn.classList.add('visible');
      } else {
        backToTopBtn.classList.remove('visible');
      }
    }
  });

  if (backToTopBtn) {
    backToTopBtn.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
}

// ==========================================
// 3. Mobile Navigation Menu
// ==========================================
export function initMobileMenu() {
  const menuToggle = document.getElementById('mobileMenuToggle');
  const navLinks = document.getElementById('navLinks');

  if (!menuToggle || !navLinks) return;

  menuToggle.addEventListener('click', () => {
    const isOpen = navLinks.classList.toggle('open');
    menuToggle.setAttribute('aria-expanded', String(isOpen));
  });

  navLinks.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      navLinks.classList.remove('open');
      menuToggle.setAttribute('aria-expanded', 'false');
    });
  });
}

// ==========================================
// 4. Cookie Consent Banner
// ==========================================
export function initCookieBanner() {
  const banner = document.getElementById('cookieBanner');
  if (!banner) return;

  const consent = localStorage.getItem('sms_cookie_consent');
  if (!consent) {
    banner.style.display = 'flex';
  } else {
    banner.style.display = 'none';
  }

  const acceptBtn = document.getElementById('cookieAcceptBtn');
  const declineBtn = document.getElementById('cookieDeclineBtn');

  if (acceptBtn) {
    acceptBtn.addEventListener('click', () => {
      localStorage.setItem('sms_cookie_consent', 'accepted');
      banner.style.display = 'none';
      showToast('Cookies Accepted', 'Preferences saved.', 'info');
    });
  }

  if (declineBtn) {
    declineBtn.addEventListener('click', () => {
      localStorage.setItem('sms_cookie_consent', 'declined');
      banner.style.display = 'none';
      showToast('Cookies Declined', 'Non-essential cookies disabled.', 'info');
    });
  }
}

// ==========================================
// 5. Keyboard Shortcuts
// ==========================================
export function initKeyboardShortcuts() {
  document.addEventListener('keydown', (e) => {
    const activeEl = document.activeElement;
    const isTyping = activeEl && (
      activeEl.tagName === 'INPUT' ||
      activeEl.tagName === 'TEXTAREA' ||
      activeEl.tagName === 'SELECT' ||
      activeEl.isContentEditable
    );

    if (e.key === 'Escape') {
      closeAllModals();
      const navLinks = document.getElementById('navLinks');
      const menuToggle = document.getElementById('mobileMenuToggle');
      if (navLinks && navLinks.classList.contains('open')) {
        navLinks.classList.remove('open');
        if (menuToggle) menuToggle.setAttribute('aria-expanded', 'false');
      }
      return;
    }

    if (isTyping) return;

    if (e.key === '/') {
      const searchInput = document.getElementById('searchInput');
      if (searchInput) {
        e.preventDefault();
        searchInput.focus();
        searchInput.select();
      }
    } else if (e.key.toLowerCase() === 'n') {
      e.preventDefault();
      openStudentModal();
    } else if (e.key.toLowerCase() === 'd') {
      e.preventDefault();
      toggleTheme();
    } else if (e.key === '?') {
      e.preventDefault();
      openShortcutsModal();
    }
  });
}

// ==========================================
// 6. Toast Notifications
// ==========================================
export function showToast(title, message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.setAttribute('role', 'alert');

  const icons = {
    success: '✓',
    error: '✕',
    info: 'ℹ',
  };

  toast.innerHTML = `
    <div class="toast-icon">${icons[type] || '•'}</div>
    <div class="toast-content">
      <div class="toast-title">${escapeHtml(title)}</div>
      <div class="toast-message">${escapeHtml(message)}</div>
    </div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 250);
  }, 4000);
}

// ==========================================
// 7. Modals
// ==========================================
export function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    const firstInput = modal.querySelector('input:not([type="hidden"]), button:not(.modal-close)');
    if (firstInput) firstInput.focus();
  }
}

export function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
  }
}

export function closeAllModals() {
  document.querySelectorAll('.modal-backdrop.open').forEach((modal) => {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
  });
}

export function openShortcutsModal() {
  openModal('shortcutsModal');
}

// ==========================================
// 8. Authentication Header Display
// ==========================================
export async function updateAuthDisplay() {
  const authNavContainer = document.getElementById('authNavContainer');
  if (!authNavContainer) return;

  try {
    const status = await authApi.checkStatus();
    if (status && status.authenticated) {
      authNavContainer.innerHTML = `
        <span style="font-size: 0.875rem; color: var(--text-secondary); margin-right: 0.5rem;">
          👤 <strong>${escapeHtml(status.username)}</strong>
        </span>
        <button id="logoutBtn" class="btn btn-secondary btn-sm">Logout</button>
      `;
      const logoutBtn = document.getElementById('logoutBtn');
      if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
          await authApi.logout();
          showToast('Logged Out', 'Session terminated successfully.', 'info');
          updateAuthDisplay();
          loadStudents();
        });
      }
      return;
    }
  } catch (err) {
    // Guest / unauthenticated
  }

  authNavContainer.innerHTML = `
    <a href="login.html" class="btn btn-secondary btn-sm">Login</a>
  `;
}

// ==========================================
// 9. Student Table & CRUD
// ==========================================
export async function loadStudents() {
  renderSkeletons();
  try {
    let result;
    const hasSearch = state.searchQuery || state.departmentFilter || state.yearFilter;
    const sortParam = `${state.sortField},${state.sortDir}`;

    if (hasSearch) {
      result = await studentApi.searchStudents(
        {
          name: state.searchQuery,
          department: state.departmentFilter,
          year: state.yearFilter ? parseInt(state.yearFilter, 10) : undefined,
        },
        state.pageNumber,
        state.pageSize,
        sortParam
      );
    } else {
      result = await studentApi.getStudents(state.pageNumber, state.pageSize, sortParam);
    }

    state.students = result.content || [];
    state.pageNumber = result.pageNumber;
    state.pageSize = result.pageSize;
    state.totalPages = result.totalPages;
    state.totalElements = result.totalElements;

    renderTable();
    renderPagination();
    updateStats();
  } catch (err) {
    console.error('Failed to load students:', err);
    if (err.status === 401 || err.status === 403) {
      renderTableError('Authentication Required. Please log in with admin credentials to access student records.');
    } else {
      renderTableError(err.message || 'Error loading student records.');
    }
  }
}

function renderSkeletons() {
  const tbody = document.getElementById('studentsTableBody');
  if (!tbody) return;

  let skeletonRows = '';
  for (let i = 0; i < 5; i++) {
    skeletonRows += `
      <tr class="skeleton-row">
        <td><div class="skeleton-box" style="width: 70px;"></div></td>
        <td><div class="skeleton-box" style="width: 130px;"></div></td>
        <td><div class="skeleton-box" style="width: 170px;"></div></td>
        <td><div class="skeleton-box" style="width: 90px;"></div></td>
        <td><div class="skeleton-box" style="width: 110px;"></div></td>
        <td><div class="skeleton-box" style="width: 60px;"></div></td>
        <td><div class="skeleton-box" style="width: 90px;"></div></td>
        <td><div class="skeleton-box" style="width: 80px; margin-left: auto;"></div></td>
      </tr>
    `;
  }
  tbody.innerHTML = skeletonRows;
}

function renderTable() {
  const tbody = document.getElementById('studentsTableBody');
  if (!tbody) return;

  if (state.students.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8">
          <div class="empty-state">
            <div class="empty-icon">📂</div>
            <div class="empty-title">No Students Found</div>
            <p class="empty-desc">
              ${state.searchQuery || state.departmentFilter || state.yearFilter
                ? 'No students matched your search criteria. Try clearing filters.'
                : 'Get started by adding the first student to the system.'}
            </p>
            <button id="emptyAddStudentBtn" class="btn btn-primary btn-sm">+ Add Student</button>
          </div>
        </td>
      </tr>
    `;
    const emptyBtn = document.getElementById('emptyAddStudentBtn');
    if (emptyBtn) emptyBtn.addEventListener('click', () => openStudentModal());
    return;
  }

  tbody.innerHTML = state.students.map((student) => `
    <tr>
      <td><strong>${escapeHtml(student.rollNumber)}</strong></td>
      <td>${escapeHtml(student.firstName)} ${escapeHtml(student.lastName)}</td>
      <td>${escapeHtml(student.email)}</td>
      <td>${escapeHtml(student.phone || '—')}</td>
      <td><span class="badge badge-primary">${escapeHtml(student.department)}</span></td>
      <td><span class="badge badge-warning">Year ${student.yearOfStudy}</span></td>
      <td>${escapeHtml(student.dateOfBirth || '—')}</td>
      <td>
        <div class="table-actions">
          <button class="btn btn-secondary btn-sm action-edit-btn" data-id="${student.id}" title="Edit Student">✏️ Edit</button>
          <button class="btn btn-danger btn-sm action-delete-btn" data-id="${student.id}" data-name="${escapeHtml(student.firstName + ' ' + student.lastName)}" title="Delete Student">🗑️</button>
        </div>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.action-edit-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      openStudentModal(Number(id));
    });
  });

  tbody.querySelectorAll('.action-delete-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      const name = btn.getAttribute('data-name');
      openDeleteModal(Number(id), name);
    });
  });
}

function renderTableError(message) {
  const tbody = document.getElementById('studentsTableBody');
  if (!tbody) return;

  tbody.innerHTML = `
    <tr>
      <td colspan="8">
        <div class="empty-state">
          <div class="empty-icon" style="color: var(--danger);">⚠️</div>
          <div class="empty-title">Access Restricted</div>
          <p class="empty-desc">${escapeHtml(message)}</p>
          <a href="login.html" class="btn btn-primary btn-sm">Sign In as Admin</a>
        </div>
      </td>
    </tr>
  `;
}

function renderPagination() {
  const container = document.getElementById('paginationControls');
  const info = document.getElementById('paginationInfo');
  if (!container || !info) return;

  const total = state.totalElements;
  const start = total === 0 ? 0 : state.pageNumber * state.pageSize + 1;
  const end = Math.min((state.pageNumber + 1) * state.pageSize, total);
  info.textContent = `Showing ${start} to ${end} of ${total} students`;

  if (state.totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  let buttons = `
    <button class="page-btn" id="prevPageBtn" ${state.pageNumber === 0 ? 'disabled' : ''} aria-label="Previous Page">‹</button>
  `;

  for (let i = 0; i < state.totalPages; i++) {
    if (
      i === 0 ||
      i === state.totalPages - 1 ||
      (i >= state.pageNumber - 1 && i <= state.pageNumber + 1)
    ) {
      buttons += `
        <button class="page-btn ${i === state.pageNumber ? 'active' : ''}" data-page="${i}">${i + 1}</button>
      `;
    } else if (
      (i === state.pageNumber - 2 && i > 1) ||
      (i === state.pageNumber + 2 && i < state.totalPages - 2)
    ) {
      buttons += `<span style="padding: 0 4px; color: var(--text-muted);">…</span>`;
    }
  }

  buttons += `
    <button class="page-btn" id="nextPageBtn" ${state.pageNumber >= state.totalPages - 1 ? 'disabled' : ''} aria-label="Next Page">›</button>
  `;

  container.innerHTML = buttons;

  const prevBtn = document.getElementById('prevPageBtn');
  const nextBtn = document.getElementById('nextPageBtn');

  if (prevBtn) {
    prevBtn.addEventListener('click', () => {
      if (state.pageNumber > 0) {
        state.pageNumber--;
        loadStudents();
      }
    });
  }

  if (nextBtn) {
    nextBtn.addEventListener('click', () => {
      if (state.pageNumber < state.totalPages - 1) {
        state.pageNumber++;
        loadStudents();
      }
    });
  }

  container.querySelectorAll('[data-page]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.pageNumber = parseInt(btn.getAttribute('data-page'), 10);
      loadStudents();
    });
  });
}

function updateStats() {
  const totalCountEl = document.getElementById('statTotalStudents');
  const deptCountEl = document.getElementById('statTotalDepartments');
  const avgYearEl = document.getElementById('statAvgYear');

  if (totalCountEl) totalCountEl.textContent = state.totalElements;

  if (state.students.length > 0) {
    const depts = new Set(state.students.map((s) => s.department));
    if (deptCountEl) deptCountEl.textContent = depts.size;

    const sumYears = state.students.reduce((acc, s) => acc + (s.yearOfStudy || 1), 0);
    const avg = (sumYears / state.students.length).toFixed(1);
    if (avgYearEl) avgYearEl.textContent = avg;
  }
}

// ==========================================
// 10. Student Modal & Validation Handlers
// ==========================================
export async function openStudentModal(studentId = null) {
  state.activeStudentId = studentId;
  const form = document.getElementById('studentForm');
  const modalTitle = document.getElementById('studentModalTitle');
  clearFormErrors();

  if (form) form.reset();

  if (studentId) {
    if (modalTitle) modalTitle.textContent = 'Edit Student Details';
    try {
      const student = await studentApi.getStudentById(studentId);
      document.getElementById('studentId').value = student.id;
      document.getElementById('rollNumber').value = student.rollNumber;
      document.getElementById('firstName').value = student.firstName;
      document.getElementById('lastName').value = student.lastName;
      document.getElementById('email').value = student.email;
      document.getElementById('phone').value = student.phone || '';
      document.getElementById('department').value = student.department;
      document.getElementById('yearOfStudy').value = student.yearOfStudy;
      document.getElementById('dateOfBirth').value = student.dateOfBirth || '';
    } catch (err) {
      showToast('Error', 'Failed to fetch student details: ' + err.message, 'error');
      return;
    }
  } else {
    if (modalTitle) modalTitle.textContent = 'Add New Student';
    document.getElementById('studentId').value = '';
  }

  openModal('studentModal');
}

export function openDeleteModal(studentId, studentName) {
  state.activeStudentId = studentId;
  const targetNameEl = document.getElementById('deleteTargetName');
  if (targetNameEl) targetNameEl.textContent = studentName;
  openModal('deleteConfirmModal');
}

function clearFormErrors() {
  document.querySelectorAll('.form-error').forEach((el) => (el.textContent = ''));
  document.querySelectorAll('.input.has-error').forEach((el) => el.classList.remove('has-error'));
}

function applyFormErrors(fieldErrors) {
  clearFormErrors();
  if (!fieldErrors) return;

  for (const [field, message] of Object.entries(fieldErrors)) {
    const errorEl = document.getElementById(`error_${field}`);
    const inputEl = document.getElementById(field);
    if (errorEl) errorEl.textContent = message;
    if (inputEl) inputEl.classList.add('has-error');
  }
}

export function initStudentForm() {
  const form = document.getElementById('studentForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFormErrors();

    const studentData = {
      rollNumber: document.getElementById('rollNumber').value.trim(),
      firstName: document.getElementById('firstName').value.trim(),
      lastName: document.getElementById('lastName').value.trim(),
      email: document.getElementById('email').value.trim(),
      phone: document.getElementById('phone').value.trim() || null,
      department: document.getElementById('department').value.trim(),
      yearOfStudy: parseInt(document.getElementById('yearOfStudy').value, 10),
      dateOfBirth: document.getElementById('dateOfBirth').value || null,
    };

    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) submitBtn.disabled = true;

    try {
      if (state.activeStudentId) {
        await studentApi.updateStudent(state.activeStudentId, studentData);
        showToast('Success', 'Student record updated successfully!', 'success');
      } else {
        await studentApi.createStudent(studentData);
        showToast('Success', 'New student enrolled successfully!', 'success');
      }
      closeModal('studentModal');
      loadStudents();
    } catch (err) {
      if (err.data && err.data.fieldErrors) {
        applyFormErrors(err.data.fieldErrors);
        showToast('Validation Error', 'Please check the highlighted fields.', 'error');
      } else {
        showToast('Operation Failed', err.message || 'An error occurred.', 'error');
      }
    } finally {
      if (submitBtn) submitBtn.disabled = false;
    }
  });

  const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
  if (confirmDeleteBtn) {
    confirmDeleteBtn.addEventListener('click', async () => {
      if (!state.activeStudentId) return;
      try {
        await studentApi.deleteStudent(state.activeStudentId);
        showToast('Success', 'Student record deleted successfully.', 'success');
        closeModal('deleteConfirmModal');
        loadStudents();
      } catch (err) {
        showToast('Delete Failed', err.message || 'Could not delete student.', 'error');
      }
    });
  }
}

// ==========================================
// 11. Table Toolbar Filters & Sorting
// ==========================================
export function initTableToolbar() {
  const searchInput = document.getElementById('searchInput');
  const deptFilter = document.getElementById('deptFilter');
  const yearFilter = document.getElementById('yearFilter');
  const pageSizeSelect = document.getElementById('pageSizeSelect');
  const addStudentBtn = document.getElementById('addStudentBtn');

  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      clearTimeout(state.debounceTimer);
      state.debounceTimer = setTimeout(() => {
        state.searchQuery = e.target.value.trim();
        state.pageNumber = 0;
        loadStudents();
      }, 350);
    });
  }

  if (deptFilter) {
    deptFilter.addEventListener('change', (e) => {
      state.departmentFilter = e.target.value;
      state.pageNumber = 0;
      loadStudents();
    });
  }

  if (yearFilter) {
    yearFilter.addEventListener('change', (e) => {
      state.yearFilter = e.target.value;
      state.pageNumber = 0;
      loadStudents();
    });
  }

  if (pageSizeSelect) {
    pageSizeSelect.addEventListener('change', (e) => {
      state.pageSize = parseInt(e.target.value, 10);
      state.pageNumber = 0;
      loadStudents();
    });
  }

  if (addStudentBtn) {
    addStudentBtn.addEventListener('click', () => openStudentModal());
  }

  document.querySelectorAll('th.sortable').forEach((th) => {
    th.addEventListener('click', () => {
      const field = th.getAttribute('data-sort');
      if (state.sortField === field) {
        state.sortDir = state.sortDir === 'asc' ? 'desc' : 'asc';
      } else {
        state.sortField = field;
        state.sortDir = 'asc';
      }

      document.querySelectorAll('th.sortable .sort-indicator').forEach((ind) => (ind.textContent = '↕'));
      const indicator = th.querySelector('.sort-indicator');
      if (indicator) {
        indicator.textContent = state.sortDir === 'asc' ? '↑' : '↓';
      }

      loadStudents();
    });
  });
}

// ==========================================
// 12. Accordion Component (FAQ Page)
// ==========================================
export function initAccordion() {
  const items = document.querySelectorAll('.accordion-item');
  items.forEach((item) => {
    const header = item.querySelector('.accordion-header');
    if (!header) return;

    header.addEventListener('click', () => {
      const isActive = item.classList.contains('active');

      items.forEach((other) => {
        other.classList.remove('active');
        const body = other.querySelector('.accordion-body');
        const h = other.querySelector('.accordion-header');
        if (body) body.style.maxHeight = null;
        if (h) h.setAttribute('aria-expanded', 'false');
      });

      if (!isActive) {
        item.classList.add('active');
        const body = item.querySelector('.accordion-body');
        if (body) body.style.maxHeight = body.scrollHeight + 32 + 'px';
        header.setAttribute('aria-expanded', 'true');
      }
    });

    header.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        header.click();
      }
    });
  });
}

// ==========================================
// 13. Password Visibility Toggle (Login Page)
// ==========================================
export function initPasswordToggle() {
  const toggleBtn = document.getElementById('passwordToggleBtn');
  const passwordInput = document.getElementById('password');

  if (toggleBtn && passwordInput) {
    toggleBtn.addEventListener('click', () => {
      const isPassword = passwordInput.getAttribute('type') === 'password';
      passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
      toggleBtn.setAttribute('aria-pressed', String(!isPassword));
      toggleBtn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
      toggleBtn.innerHTML = isPassword ? '👁️' : '👁️‍🗨️';
    });
  }
}

// ==========================================
// 14. Standalone Student Form Page
// ==========================================
export function initStandaloneStudentForm() {
  const form = document.getElementById('standaloneStudentForm');
  if (!form) return;

  const urlParams = new URLSearchParams(window.location.search);
  const studentId = urlParams.get('id');

  if (studentId) {
    document.getElementById('pageTitle').textContent = 'Edit Student Details';
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) saveBtn.textContent = 'Update Student';
    
    studentApi.getStudentById(studentId).then((student) => {
      document.getElementById('studentId').value = student.id;
      document.getElementById('rollNumber').value = student.rollNumber;
      document.getElementById('firstName').value = student.firstName;
      document.getElementById('lastName').value = student.lastName;
      document.getElementById('email').value = student.email;
      document.getElementById('phone').value = student.phone || '';
      document.getElementById('department').value = student.department;
      document.getElementById('yearOfStudy').value = student.yearOfStudy;
      document.getElementById('dateOfBirth').value = student.dateOfBirth || '';
    }).catch((err) => {
      showToast('Error', 'Failed to fetch student details: ' + err.message, 'error');
    });
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFormErrors();

    const studentData = {
      rollNumber: document.getElementById('rollNumber').value.trim(),
      firstName: document.getElementById('firstName').value.trim(),
      lastName: document.getElementById('lastName').value.trim(),
      email: document.getElementById('email').value.trim(),
      phone: document.getElementById('phone').value.trim() || null,
      department: document.getElementById('department').value.trim(),
      yearOfStudy: parseInt(document.getElementById('yearOfStudy').value, 10),
      dateOfBirth: document.getElementById('dateOfBirth').value || null,
    };

    const id = document.getElementById('studentId').value;
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) saveBtn.disabled = true;

    try {
      if (id) {
        await studentApi.updateStudent(id, studentData);
        showToast('Success', 'Student details updated!', 'success');
      } else {
        await studentApi.createStudent(studentData);
        showToast('Success', 'Student registered successfully!', 'success');
      }
      setTimeout(() => {
        window.location.href = 'index.html';
      }, 1000);
    } catch (err) {
      if (err.data && err.data.fieldErrors) {
        applyFormErrors(err.data.fieldErrors);
        showToast('Validation Error', 'Please correct highlighted errors.', 'error');
      } else {
        showToast('Error', err.message || 'Submission failed.', 'error');
      }
    } finally {
      if (saveBtn) saveBtn.disabled = false;
    }
  });
}

// ==========================================
// 15. Standalone Login Page
// ==========================================
export function initLoginPage() {
  const form = document.getElementById('loginForm');
  if (!form) return;

  const fillBtn = document.getElementById('fillDemoCredsBtn');
  if (fillBtn) {
    fillBtn.addEventListener('click', () => {
      document.getElementById('username').value = 'admin';
      document.getElementById('password').value = 'admin123';
    });
  }

  const alertBox = document.getElementById('loginAlert');
  const submitBtn = document.getElementById('loginSubmitBtn');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (alertBox) alertBox.style.display = 'none';

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    if (!username || !password) {
      if (alertBox) {
        alertBox.textContent = 'Please enter both username and password.';
        alertBox.style.display = 'block';
      }
      return;
    }

    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = 'Signing in...';
    }

    try {
      await authApi.login(username, password);
      showToast('Welcome Back', 'Authentication successful! Redirecting...', 'success');
      setTimeout(() => {
        window.location.href = 'index.html';
      }, 700);
    } catch (err) {
      if (alertBox) {
        alertBox.textContent = err.message || 'Invalid username or password.';
        alertBox.style.display = 'block';
      }
      showToast('Login Failed', 'Please verify your credentials.', 'error');
    } finally {
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Sign In';
      }
    }
  });
}

// ==========================================
// HTML Sanitizer
// ==========================================
export function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// ==========================================
// Lifecycle Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initScrollFeatures();
  initMobileMenu();
  initCookieBanner();
  initKeyboardShortcuts();
  updateAuthDisplay();

  if (document.getElementById('studentsTableBody')) {
    initTableToolbar();
    initStudentForm();
    loadStudents();
  }

  if (document.querySelector('.accordion')) {
    initAccordion();
  }

  if (document.getElementById('loginForm')) {
    initPasswordToggle();
    initLoginPage();
  }

  if (document.getElementById('standaloneStudentForm')) {
    initStandaloneStudentForm();
  }

  document.querySelectorAll('[data-close-modal]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const modalId = btn.getAttribute('data-close-modal');
      closeModal(modalId);
    });
  });
});
