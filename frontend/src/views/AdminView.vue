<template>
  <div class="admin-page">

    <div class="page-header">
      <h1 class="page-title">Admin Panel</h1>
      <span class="badge badge-yellow">ADMIN</span>
    </div>

    <div class="tabs">
      <button
          v-for="tab in tabs"
          :key="tab.id"
          class="tab-btn"
          :class="{ 'tab-btn-active': activeTab === tab.id }"
          @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="activeTab === 'platforms'" class="tab-content">

      <div class="toolbar">
        <span class="toolbar-count">{{ platforms.length }} platforms</span>
        <button @click="openCreatePlatform" class="btn-primary">+ Add Platform</button>
      </div>

      <div v-if="loadingPlatforms" class="skeleton-list">
        <div v-for="i in 5" :key="i" class="skeleton-row"></div>
      </div>

      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Manufacturer</th>
            <th>Games</th>
            <th></th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="p in platforms" :key="p.id">
            <td class="text-muted mono">{{ p.id }}</td>
            <td class="font-medium">{{ p.name }}</td>
            <td class="text-muted">{{ p.manufacturer ?? '—' }}</td>
            <td class="text-muted">{{ p.gameCount }}</td>
            <td class="actions-cell">
              <button @click="openEditPlatform(p)" class="icon-btn">✏️</button>
              <button @click="confirmDeletePlatform(p)" class="icon-btn icon-btn-danger">🗑️</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="showPlatformForm" class="modal-overlay" @click.self="closePlatformForm">
        <div class="modal">
          <div class="modal-header">
            <h2 class="modal-title">{{ editingPlatform ? 'Edit Platform' : 'New Platform' }}</h2>
            <button class="modal-close" @click="closePlatformForm">✕</button>
          </div>
          <div class="modal-body">
            <div class="field">
              <label class="field-label">Name</label>
              <input v-model="platformForm.name" class="input" type="text" placeholder="e.g. PlayStation 5" />
              <span v-if="platformErrors.name" class="field-error">{{ platformErrors.name }}</span>
            </div>
            <div class="field">
              <label class="field-label">Manufacturer</label>
              <input v-model="platformForm.manufacturer" class="input" type="text" placeholder="e.g. Sony" />
              <span v-if="platformErrors.manufacturer" class="field-error">{{ platformErrors.manufacturer }}</span>
            </div>
            <div v-if="platformApiError" class="api-error">{{ platformApiError }}</div>
          </div>
          <div class="modal-footer">
            <button @click="closePlatformForm" class="btn-secondary">Cancel</button>
            <button @click="submitPlatform" class="btn-primary" :disabled="savingPlatform">
              {{ savingPlatform ? 'Saving...' : (editingPlatform ? 'Save Changes' : 'Create') }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="deletePlatformTarget" class="modal-overlay" @click.self="deletePlatformTarget = null">
        <div class="confirm-dialog">
          <h3 class="confirm-title">Delete platform?</h3>
          <p class="confirm-desc">"<strong>{{ deletePlatformTarget.name }}</strong>" will be permanently deleted.</p>
          <div class="confirm-actions">
            <button @click="deletePlatformTarget = null" class="btn-secondary">Cancel</button>
            <button @click="doDeletePlatform" class="btn-danger" :disabled="deletingPlatform">
              {{ deletingPlatform ? 'Deleting...' : 'Delete' }}
            </button>
          </div>
        </div>
      </div>

    </div>

    <div v-if="activeTab === 'import'" class="tab-content">
      <div class="card import-card">
        <div class="import-header">
          <span class="import-icon">📥</span>
          <div>
            <h2 class="import-title">Data Importer</h2>
            <p class="import-desc">
              Triggers the CSV importer to load game data into the database.
              This may take a while for large datasets.
            </p>
          </div>
        </div>

        <button
            @click="runImport"
            class="btn-primary btn-import"
            :disabled="importing"
        >
          {{ importing ? 'Importing...' : 'Run Import' }}
        </button>

        <div v-if="importResult" class="import-result" :class="importResult.success ? 'result-success' : 'result-error'">
          <span class="result-icon">{{ importResult.success ? '✅' : '❌' }}</span>
          <span>{{ importResult.message }}</span>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '@/services/api'

const tabs = [
  { id: 'platforms', label: '📺 Platforms' },
  { id: 'import',    label: '📥 Data Import' },
]
const activeTab = ref('platforms')

const platforms = ref([])
const loadingPlatforms = ref(true)

const showPlatformForm = ref(false)
const editingPlatform = ref(null)
const savingPlatform = ref(false)
const platformApiError = ref('')

const platformForm = reactive({ name: '', manufacturer: '' })
const platformErrors = reactive({ name: '', manufacturer: '' })

const deletePlatformTarget = ref(null)
const deletingPlatform = ref(false)

const importing = ref(false)
const importResult = ref(null)

onMounted(async () => {
  await loadPlatforms()
})

async function loadPlatforms() {
  loadingPlatforms.value = true
  try {
    const res = await api.get('/platforms')
    platforms.value = res.data.data ?? []
  } catch (e) {
    console.error(e)
  } finally {
    loadingPlatforms.value = false
  }
}

function openCreatePlatform() {
  editingPlatform.value = null
  platformForm.name = ''
  platformForm.manufacturer = ''
  platformErrors.name = ''
  platformErrors.manufacturer = ''
  platformApiError.value = ''
  showPlatformForm.value = true
}

function openEditPlatform(p) {
  editingPlatform.value = p
  platformForm.name = p.name
  platformForm.manufacturer = p.manufacturer ?? ''
  platformErrors.name = ''
  platformErrors.manufacturer = ''
  platformApiError.value = ''
  showPlatformForm.value = true
}

function closePlatformForm() {
  showPlatformForm.value = false
  editingPlatform.value = null
}

function validatePlatform() {
  let valid = true
  platformErrors.name = ''
  platformErrors.manufacturer = ''
  if (!platformForm.name.trim()) {
    platformErrors.name = 'Name is required'
    valid = false
  }
  if (!platformForm.manufacturer.trim()) {
    platformErrors.manufacturer = 'Manufacturer is required'
    valid = false
  }
  return valid
}

async function submitPlatform() {
  if (!validatePlatform()) return
  savingPlatform.value = true
  platformApiError.value = ''
  try {
    const payload = { name: platformForm.name, manufacturer: platformForm.manufacturer }
    if (editingPlatform.value) {
      await api.put(`/platforms/${editingPlatform.value.id}`, payload)
    } else {
      await api.post('/platforms', payload)
    }
    closePlatformForm()
    await loadPlatforms()
  } catch (e) {
    platformApiError.value = 'Something went wrong. Please try again.'
  } finally {
    savingPlatform.value = false
  }
}

function confirmDeletePlatform(p) {
  deletePlatformTarget.value = p
}

async function doDeletePlatform() {
  if (!deletePlatformTarget.value) return
  deletingPlatform.value = true
  try {
    await api.delete(`/platforms/${deletePlatformTarget.value.id}`)
    deletePlatformTarget.value = null
    await loadPlatforms()
  } catch (e) {
    console.error(e)
  } finally {
    deletingPlatform.value = false
  }
}

async function runImport() {
  importing.value = true
  importResult.value = null
  try {
    const res = await api.post('/admin/import')
    importResult.value = { success: true, message: res.data || 'Import completed successfully.' }
  } catch (e) {
    importResult.value = { success: false, message: e.response?.data || 'Import failed.' }
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.page-title {
  font-size: 1.75rem;
  margin: 0;
}

.tabs {
  display: flex;
  gap: 0.25rem;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0;
}

.tab-btn {
  padding: 0.6rem 1.25rem;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: var(--color-text);
}

.tab-btn-active {
  color: var(--color-accent);
  border-bottom-color: var(--color-accent);
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-count {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.btn-primary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color 0.15s;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-accent-hover);
}

.btn-primary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.skeleton-row {
  height: 52px;
  border-radius: 8px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.table-wrap {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.data-table thead tr {
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.data-table th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  background-color: var(--color-surface);
  transition: background-color 0.1s;
}

.data-table tbody tr:last-child {
  border-bottom: none;
}

.data-table tbody tr:hover {
  background-color: var(--color-surface-2);
}

.data-table td {
  padding: 0.75rem 1rem;
  vertical-align: middle;
}

.text-muted { color: var(--color-text-muted); }
.font-medium { font-weight: 500; color: var(--color-text); }
.mono { font-family: 'Space Mono', monospace; font-size: 0.8rem; }

.actions-cell {
  display: flex;
  gap: 0.25rem;
  justify-content: flex-end;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.3rem;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: background-color 0.15s;
  line-height: 1;
}

.icon-btn:hover { background-color: var(--color-surface-2); }
.icon-btn-danger:hover { background-color: rgba(239, 68, 68, 0.1); }

.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 1rem;
  backdrop-filter: blur(2px);
}

.modal {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  width: 100%;
  max-width: 440px;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 0;
}

.modal-title {
  font-size: 1.1rem;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 1rem;
  padding: 0.25rem;
  transition: color 0.15s;
}

.modal-close:hover { color: var(--color-text); }

.modal-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.field-error {
  font-size: 0.775rem;
  color: #fca5a5;
}

.api-error {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background-color: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fca5a5;
  font-size: 0.875rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 0 1.5rem 1.5rem;
}

.btn-secondary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: transparent;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.15s;
}

.btn-secondary:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted);
}

.btn-danger {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s;
}

.btn-danger:hover:not(:disabled) {
  background-color: var(--color-danger);
  color: white;
}

.btn-danger:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.confirm-dialog {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 1.75rem;
  max-width: 400px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.confirm-title {
  font-size: 1.1rem;
  margin: 0;
}

.confirm-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.75rem;
}

.import-card {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 600px;
}

.import-header {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.import-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.import-title {
  font-size: 1.1rem;
  margin: 0 0 0.35rem;
}

.import-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
  line-height: 1.6;
}

.btn-import {
  align-self: flex-start;
  padding: 0.625rem 1.5rem;
}

.import-result {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  border-radius: 10px;
  font-size: 0.875rem;
}

.result-success {
  background-color: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.3);
  color: #86efac;
}

.result-error {
  background-color: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fca5a5;
}

.result-icon {
  font-size: 1.1rem;
  flex-shrink: 0;
}
</style>