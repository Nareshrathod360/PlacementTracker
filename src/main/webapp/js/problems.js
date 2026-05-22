/**
 * problems.js - Frontend logic for the Problem Tracker page.
 * Handles: loading problems, add/edit/delete, filters, star toggle.
 */

// ── State ──────────────────────────────────────────────────────────────────
let allProblems = [];   // cached list of problems from server
let isEditing   = false; // true when modal is in edit mode

// Debounced search (waits 350ms after user stops typing)
const debouncedLoad = debounce(loadProblems, 350);

// ── Load Problems from Server ───────────────────────────────────────────────
async function loadProblems() {
  const search     = document.getElementById('search-input').value.trim();
  const difficulty = document.getElementById('filter-difficulty').value;
  const platform   = document.getElementById('filter-platform').value;
  const company    = document.getElementById('filter-company').value.trim();

  // Build query string
  const params = new URLSearchParams();
  if (search)     params.append('search',     search);
  if (difficulty) params.append('difficulty', difficulty);
  if (platform)   params.append('platform',   platform);
  if (company)    params.append('company',    company);

  const data = await apiGet('/problems?' + params.toString());
  if (data === null) return;

  allProblems = data;
  renderTable(allProblems);
  renderSummaryChips(allProblems);
}

// ── Render Problems Table ───────────────────────────────────────────────────
function renderTable(problems) {
  const tbody = document.getElementById('problems-tbody');

  if (!problems.length) {
    tbody.innerHTML = `<tr><td colspan="8">
      <div class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>No problems found</h3>
        <p>Try adjusting your filters or add a new problem.</p>
        <button class="btn btn-primary" style="margin-top:1rem;" onclick="openAddModal()">
          + Add Problem
        </button>
      </div></td></tr>`;
    return;
  }

  tbody.innerHTML = problems.map(p => `
    <tr id="row-${p.id}">
      <!-- Star / Favorite -->
      <td>
        <button class="star-btn ${p.favorite ? 'starred' : 'unstarred'}"
                onclick="toggleStar(${p.id}, this)" title="Toggle favourite">
          ${p.favorite ? '⭐' : '☆'}
        </button>
      </td>

      <!-- Problem Name -->
      <td style="max-width:240px;">
        <span style="font-weight:600;" title="${esc(p.problemName)}">${esc(p.problemName)}</span>
      </td>

      <!-- Platform -->
      <td>
        <span class="mono" style="color:var(--accent);font-size:0.8rem;">${esc(p.platform)}</span>
      </td>

      <!-- Difficulty Badge -->
      <td>
        <span class="badge badge-${p.difficulty.toLowerCase()}">${p.difficulty}</span>
      </td>

      <!-- Status (inline editable select) -->
      <td>
        <select class="form-control" style="padding:4px 8px;font-size:0.8rem;width:auto;"
                onchange="quickUpdateStatus(${p.id}, this.value)">
          <option value="Solved"    ${p.status==='Solved'?'selected':''}>✅ Solved</option>
          <option value="Attempted" ${p.status==='Attempted'?'selected':''}>🔄 Attempted</option>
          <option value="Todo"      ${p.status==='Todo'?'selected':''}>📋 Todo</option>
        </select>
      </td>

      <!-- Company -->
      <td style="color:var(--text-secondary);font-size:0.88rem;">
        ${p.company ? esc(p.company) : '<span class="muted">—</span>'}
      </td>

      <!-- Date Added -->
      <td class="mono muted" style="font-size:0.78rem;">${formatDate(p.dateAdded)}</td>

      <!-- Actions -->
      <td>
        <div style="display:flex;gap:4px;">
          <button class="btn btn-secondary btn-icon btn-sm" title="Edit"
                  onclick="openEditModal(${p.id})">✏️</button>
          <button class="btn btn-danger btn-icon btn-sm" title="Delete"
                  onclick="deleteProblem(${p.id}, '${esc(p.problemName)}')">🗑️</button>
        </div>
      </td>
    </tr>`).join('');
}

// ── Summary Chips (count per status) ───────────────────────────────────────
function renderSummaryChips(problems) {
  const counts = { Solved: 0, Attempted: 0, Todo: 0 };
  problems.forEach(p => counts[p.status] = (counts[p.status] || 0) + 1);

  document.getElementById('summary-chips').innerHTML = `
    <span class="badge badge-solved">✅ Solved: ${counts.Solved}</span>
    <span class="badge badge-attempted">🔄 Attempted: ${counts.Attempted}</span>
    <span class="badge badge-todo">📋 Todo: ${counts.Todo}</span>
    <span class="badge" style="background:var(--accent-dim);color:var(--accent);">
      Total: ${problems.length}
    </span>`;
}

// ── Open Add Modal ──────────────────────────────────────────────────────────
function openAddModal() {
  isEditing = false;
  document.getElementById('modal-title').textContent = 'Add New Problem';
  document.getElementById('modal-submit-btn').textContent = '+ Add Problem';
  document.getElementById('edit-id').value = '';
  resetForm('problem-form');
  openModal('problem-modal');
}

// ── Open Edit Modal ─────────────────────────────────────────────────────────
function openEditModal(problemId) {
  const p = allProblems.find(x => x.id === problemId);
  if (!p) { showToast('Problem not found', 'error'); return; }

  isEditing = true;
  document.getElementById('modal-title').textContent = 'Edit Problem';
  document.getElementById('modal-submit-btn').textContent = '💾 Save Changes';
  document.getElementById('edit-id').value = p.id;

  // Pre-fill form fields
  document.getElementById('p-name').value       = p.problemName;
  document.getElementById('p-platform').value   = p.platform;
  document.getElementById('p-difficulty').value = p.difficulty;
  document.getElementById('p-status').value     = p.status;
  document.getElementById('p-company').value    = p.company || '';

  openModal('problem-modal');
}

// ── Submit (Add or Edit) ────────────────────────────────────────────────────
async function submitProblem(e) {
  e.preventDefault();

  const params = {
    problemName: document.getElementById('p-name').value.trim(),
    platform:    document.getElementById('p-platform').value,
    difficulty:  document.getElementById('p-difficulty').value,
    status:      document.getElementById('p-status').value,
    company:     document.getElementById('p-company').value.trim(),
  };

  if (isEditing) {
    params.action = 'edit';
    params.id     = document.getElementById('edit-id').value;
  } else {
    params.action = 'add';
  }

  const result = await apiPost('/problems', params);
  if (!result) return;

  if (result.success) {
    showToast(result.message, 'success');
    closeModal('problem-modal');
    resetForm('problem-form');
    await loadProblems();
  } else {
    showToast(result.message || 'Operation failed', 'error');
  }
}

// ── Quick Status Update (inline select) ────────────────────────────────────
async function quickUpdateStatus(problemId, newStatus) {
  const p = allProblems.find(x => x.id === problemId);
  if (!p) return;

  const result = await apiPost('/problems', {
    action:      'edit',
    id:          problemId,
    problemName: p.problemName,
    platform:    p.platform,
    difficulty:  p.difficulty,
    status:      newStatus,
    company:     p.company || ''
  });

  if (result && result.success) {
    p.status = newStatus; // update local cache
    showToast('Status updated!', 'success');
    renderSummaryChips(allProblems);
  } else {
    showToast('Update failed', 'error');
  }
}

// ── Toggle Star / Favorite ─────────────────────────────────────────────────
async function toggleStar(problemId, btn) {
  const result = await apiPost('/problems', { action: 'star', id: problemId });
  if (result && result.success) {
    const p = allProblems.find(x => x.id === problemId);
    if (p) p.favorite = !p.favorite;
    btn.classList.toggle('starred');
    btn.classList.toggle('unstarred');
    btn.textContent = p.favorite ? '⭐' : '☆';
    btn.title = p.favorite ? 'Remove favourite' : 'Add to favourites';
  }
}

// ── Delete Problem ──────────────────────────────────────────────────────────
async function deleteProblem(problemId, name) {
  if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;

  const result = await apiPost('/problems', { action: 'delete', id: problemId });
  if (result && result.success) {
    showToast('Problem deleted.', 'success');
    // Remove from local cache and re-render (no server round-trip)
    allProblems = allProblems.filter(p => p.id !== problemId);
    renderTable(allProblems);
    renderSummaryChips(allProblems);
  } else {
    showToast(result?.message || 'Delete failed', 'error');
  }
}

// ── Clear Filters ───────────────────────────────────────────────────────────
function clearFilters() {
  document.getElementById('search-input').value     = '';
  document.getElementById('filter-difficulty').value = '';
  document.getElementById('filter-platform').value   = '';
  document.getElementById('filter-company').value    = '';
  loadProblems();
}

// ── HTML Escaping ───────────────────────────────────────────────────────────
function esc(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;')
    .replace(/'/g,'&#39;');
}

// ── Init ────────────────────────────────────────────────────────────────────
(async function init() {
  const session = await requireAuth();
  if (session) {
    document.getElementById('nav-avatar').textContent = session.userName[0].toUpperCase();
    await loadProblems();
  }
})();
