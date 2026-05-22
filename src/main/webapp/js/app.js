/**
 * app.js - Shared utilities for Placement Preparation Tracker
 * Handles: API calls, toast notifications, session checking, common helpers
 */

// ── Context Path (adjust if deploying to a sub-path) ──────────────────────
const CTX = '/PlacementTracker-1.0-SNAPSHOT';  // e.g. '/PlacementTracker' if deployed at a sub-path

// ── API Helper ─────────────────────────────────────────────────────────────

/**
 * Generic fetch wrapper for GET requests.
 * Returns parsed JSON or null on error.
 */
async function apiGet(endpoint) {
  try {
    const res = await fetch(CTX + endpoint, { credentials: 'same-origin' });
    if (res.status === 401) {
      window.location.href = CTX + '/index.html';
      return null;
    }
    return await res.json();
  } catch (err) {
    console.error('API GET error:', endpoint, err);
    showToast('Network error. Please try again.', 'error');
    return null;
  }
}

/**
 * Generic fetch wrapper for POST requests.
 * Sends URLSearchParams (form data).
 * Returns parsed JSON or null on error.
 */
async function apiPost(endpoint, params) {
  try {
    const body = new URLSearchParams(params);
    const res = await fetch(CTX + endpoint, {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    });
    if (res.status === 401) {
      window.location.href = CTX + '/index.html';
      return null;
    }
    return await res.json();
  } catch (err) {
    console.error('API POST error:', endpoint, err);
    showToast('Network error. Please try again.', 'error');
    return null;
  }
}

// ── Toast Notifications ────────────────────────────────────────────────────

let toastContainer = null;

function getToastContainer() {
  if (!toastContainer) {
    toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.id = 'toast-container';
      toastContainer.className = 'toast-container';
      document.body.appendChild(toastContainer);
    }
  }
  return toastContainer;
}

/**
 * Show a toast notification.
 * @param {string} message - Message to display
 * @param {'success'|'error'|'info'} type - Toast type
 * @param {number} duration - Auto-dismiss duration in ms (0 = no auto-dismiss)
 */
function showToast(message, type = 'info', duration = 3500) {
  const container = getToastContainer();
  const icons = { success: '✓', error: '✗', info: 'ℹ' };

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${icons[type]}</span><span>${message}</span>`;

  container.appendChild(toast);

  if (duration > 0) {
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  // Click to dismiss
  toast.addEventListener('click', () => toast.remove());
}

// ── Session Management ─────────────────────────────────────────────────────

/**
 * Check if user is logged in. Redirect to login if not.
 * Also populates nav user info.
 */
async function requireAuth() {
  const session = await apiGet('/session');
  if (!session || !session.loggedIn) {
    window.location.href = CTX + '/index.html';
    return null;
  }
  populateUserNav(session);
  return session;
}

function populateUserNav(session) {
  // Set user name in navbar
  const userNameEl = document.getElementById('nav-user-name');
  if (userNameEl) userNameEl.textContent = session.userName;

  const avatarEls = document.querySelectorAll('.user-avatar');
  avatarEls.forEach(el => {
    el.textContent = session.userName ? session.userName[0].toUpperCase() : 'U';
  });
}

// ── Modal Helpers ──────────────────────────────────────────────────────────

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('hidden');
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('hidden');
}

// Close modal when clicking the overlay (outside the modal box)
document.addEventListener('click', function(e) {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.add('hidden');
  }
});

// Close on Escape key
document.addEventListener('keydown', function(e) {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-overlay:not(.hidden)').forEach(m => {
      m.classList.add('hidden');
    });
  }
});

// ── Form Reset Helper ──────────────────────────────────────────────────────

function resetForm(formId) {
  const form = document.getElementById(formId);
  if (form) form.reset();
}

// ── Date Formatting ────────────────────────────────────────────────────────

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric'
  });
}

function timeAgo(dateStr) {
  if (!dateStr) return '';
  const now = new Date();
  const date = new Date(dateStr);
  const diff = Math.floor((now - date) / 1000);

  if (diff < 60)   return 'just now';
  if (diff < 3600) return `${Math.floor(diff/60)}m ago`;
  if (diff < 86400)return `${Math.floor(diff/3600)}h ago`;
  return `${Math.floor(diff/86400)}d ago`;
}

// ── Logout ─────────────────────────────────────────────────────────────────

function logout() {
  window.location.href = CTX + '/auth?action=logout';
}

// ── Highlight Active Nav Link ──────────────────────────────────────────────

(function highlightActiveNav() {
  const currentPage = window.location.pathname.split('/').pop();
  document.querySelectorAll('.nav-links a, .mobile-nav a').forEach(link => {
    const href = link.getAttribute('href');
    if (href && href.includes(currentPage)) {
      link.classList.add('active');
    }
  });
})();

// ── Export to CSV ──────────────────────────────────────────────────────────

function exportTableToCSV(tableId, filename) {
  const table = document.getElementById(tableId);
  if (!table) return;

  const rows = table.querySelectorAll('tr');
  const csv = [];

  rows.forEach(row => {
    const cols = row.querySelectorAll('th, td');
    const rowData = [];
    cols.forEach((col, i) => {
      // Skip action columns (last column)
      if (i < cols.length - 1) {
        rowData.push('"' + col.innerText.replace(/"/g, '""') + '"');
      }
    });
    csv.push(rowData.join(','));
  });

  const csvStr = csv.join('\n');
  const blob = new Blob([csvStr], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename || 'export.csv';
  a.click();
  URL.revokeObjectURL(url);
  showToast('Exported to CSV!', 'success');
}

// ── Debounce (for search input) ────────────────────────────────────────────

function debounce(fn, delay) {
  let timer;
  return function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}
