/**
 * notes.js - Frontend logic for the Notes page.
 */

let allNotes   = [];
let viewNoteId = null; // ID of note currently in view modal

const debouncedLoadNotes = debounce(loadNotes, 350);

// ── Load Notes ──────────────────────────────────────────────────────────────
async function loadNotes() {
  const search = document.getElementById('note-search').value.trim();
  const params = search ? '?search=' + encodeURIComponent(search) : '';
  const data   = await apiGet('/notes' + params);
  if (data === null) return;

  allNotes = data;
  renderNotes(allNotes);

  const countEl = document.getElementById('notes-count');
  countEl.textContent = allNotes.length + ' note' + (allNotes.length !== 1 ? 's' : '');
}

// ── Render Notes Grid ───────────────────────────────────────────────────────
function renderNotes(notes) {
  const grid = document.getElementById('notes-grid');

  if (!notes.length) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">
      <div class="empty-icon">📭</div>
      <h3>No notes yet</h3>
      <p>Start capturing your learning — concepts, tips, or code snippets!</p>
      <button class="btn btn-primary" style="margin-top:1rem;" onclick="openAddNoteModal()">
        + Create First Note
      </button>
    </div>`;
    return;
  }

  grid.innerHTML = notes.map(note => `
    <div class="note-card" onclick="viewNote(${note.id})">
      <!-- Action buttons (shown on hover) -->
      <div class="note-actions" onclick="event.stopPropagation()">
        <button class="btn btn-secondary btn-icon btn-sm" title="Edit"
                onclick="openEditNoteModal(${note.id})">✏️</button>
        <button class="btn btn-danger btn-icon btn-sm" title="Delete"
                onclick="deleteNote(${note.id}, event)">🗑️</button>
      </div>

      <div class="note-title">${esc(note.title)}</div>
      <div class="note-preview">${esc(note.content)}</div>
      <div class="note-date">Updated ${timeAgo(note.dateUpdated)}</div>
    </div>`).join('');
}

// ── View Note (full content modal) ─────────────────────────────────────────
function viewNote(noteId) {
  const note = allNotes.find(n => n.id === noteId);
  if (!note) return;

  viewNoteId = noteId;
  document.getElementById('view-note-title').textContent   = note.title;
  document.getElementById('view-note-content').textContent = note.content;
  document.getElementById('view-note-date').textContent    =
    'Created ' + formatDate(note.dateCreated) +
    (note.dateUpdated !== note.dateCreated ? ' · Updated ' + timeAgo(note.dateUpdated) : '');

  // Wire up edit/delete buttons
  document.getElementById('view-edit-btn').onclick   = () => { closeModal('view-note-modal'); openEditNoteModal(noteId); };
  document.getElementById('view-delete-btn').onclick = () => deleteNote(noteId, null, true);

  openModal('view-note-modal');
}

// ── Open Add Note Modal ─────────────────────────────────────────────────────
function openAddNoteModal() {
  document.getElementById('note-modal-title').textContent  = 'New Note';
  document.getElementById('note-submit-btn').textContent   = '💾 Save Note';
  document.getElementById('note-edit-id').value = '';
  resetForm('note-form');
  openModal('note-modal');
  setTimeout(() => document.getElementById('note-title').focus(), 100);
}

// ── Open Edit Note Modal ────────────────────────────────────────────────────
function openEditNoteModal(noteId) {
  const note = allNotes.find(n => n.id === noteId);
  if (!note) return;

  document.getElementById('note-modal-title').textContent  = 'Edit Note';
  document.getElementById('note-submit-btn').textContent   = '💾 Save Changes';
  document.getElementById('note-edit-id').value = note.id;
  document.getElementById('note-title').value   = note.title;
  document.getElementById('note-content').value = note.content;

  openModal('note-modal');
}

// ── Submit Note (add or edit) ───────────────────────────────────────────────
async function submitNote(e) {
  e.preventDefault();

  const editId  = document.getElementById('note-edit-id').value;
  const title   = document.getElementById('note-title').value.trim();
  const content = document.getElementById('note-content').value;

  const params = { title, content };
  params.action = editId ? 'edit' : 'add';
  if (editId) params.id = editId;

  const result = await apiPost('/notes', params);
  if (!result) return;

  if (result.success) {
    showToast(result.message, 'success');
    closeModal('note-modal');
    resetForm('note-form');
    await loadNotes();
  } else {
    showToast(result.message || 'Operation failed', 'error');
  }
}

// ── Delete Note ─────────────────────────────────────────────────────────────
async function deleteNote(noteId, event, fromViewModal = false) {
  if (event) event.stopPropagation();

  const note = allNotes.find(n => n.id === noteId);
  const name = note ? `"${note.title}"` : 'this note';
  if (!confirm(`Delete ${name}? This cannot be undone.`)) return;

  const result = await apiPost('/notes', { action: 'delete', id: noteId });
  if (result && result.success) {
    showToast('Note deleted.', 'success');
    if (fromViewModal) closeModal('view-note-modal');
    allNotes = allNotes.filter(n => n.id !== noteId);
    renderNotes(allNotes);
    const countEl = document.getElementById('notes-count');
    countEl.textContent = allNotes.length + ' note' + (allNotes.length !== 1 ? 's' : '');
  } else {
    showToast(result?.message || 'Delete failed', 'error');
  }
}

// ── Escape HTML ─────────────────────────────────────────────────────────────
function esc(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ── Init ────────────────────────────────────────────────────────────────────
(async function init() {
  const session = await requireAuth();
  if (session) {
    document.getElementById('nav-avatar').textContent = session.userName[0].toUpperCase();
    await loadNotes();
  }
})();
