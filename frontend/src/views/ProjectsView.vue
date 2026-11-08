<template>
  <div class="projects-page">

    <div class="page-header">
      <div>
        <h1 class="page-title">My Projects</h1>
        <p class="page-subtitle">Save your market research and run analytics per project.</p>
      </div>
      <button @click="openCreate" class="btn-primary">+ New Project</button>
    </div>

    <div v-if="loading" class="skeleton-grid">
      <div v-for="i in 3" :key="i" class="skeleton-card"></div>
    </div>

    <div v-else-if="projects.length === 0" class="empty-state">
      <span class="empty-icon">📁</span>
      <p>No projects yet.</p>
      <button @click="openCreate" class="btn-primary">Create your first project</button>
    </div>

    <div v-else class="projects-grid">
      <div v-for="project in projects" :key="project.id" class="project-card" @click="router.push('/projects/' + project.id)" style="cursor: pointer;">

        <div class="project-card-header">
          <h3 class="project-name">{{ project.projectName }}</h3>
          <div class="project-actions">
            <button @click.stop="openEdit(project)" class="icon-btn" title="Edit">✏️</button>
            <button @click.stop="confirmDelete(project)" class="icon-btn icon-btn-danger" title="Delete">🗑️</button>
          </div>
        </div>

        <div class="project-tags">
          <span class="badge badge-purple">{{ project.targetGenre }}</span>
          <span v-if="project.targetPlatform" class="badge badge-yellow">{{ project.targetPlatform }}</span>
        </div>

        <div class="project-meta">
          <span class="meta-item" v-if="project.calculatedRisk != null">
            <span class="meta-label">Risk</span>
            <span class="meta-value" :class="riskClass(project.calculatedRisk)">
              {{ project.calculatedRisk }}%
            </span>
          </span>
          <span class="meta-item" v-if="project.createdAt">
            <span class="meta-label">Created</span>
            <span class="meta-value">{{ formatDate(project.createdAt) }}</span>
          </span>
        </div>

      </div>
    </div>

    <ProjectModal
        v-if="showModal"
        :project="editingProject"
        :genres="genres"
        :platforms="platforms"
        @close="closeModal"
        @saved="onSaved"
    />

    <div v-if="deleteTarget" class="modal-overlay" @click.self="deleteTarget = null">
      <div class="confirm-dialog">
        <h3 class="confirm-title">Delete project?</h3>
        <p class="confirm-desc">
          "<strong>{{ deleteTarget.projectName }}</strong>" will be permanently deleted.
        </p>
        <div class="confirm-actions">
          <button @click="deleteTarget = null" class="btn-secondary">Cancel</button>
          <button @click="doDelete" class="btn-danger" :disabled="deleting">
            {{ deleting ? 'Deleting...' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import ProjectModal from '@/components/ProjectModal.vue'

const router = useRouter()

const projects  = ref([])
const genres    = ref([])
const platforms = ref([])
const loading   = ref(true)

const showModal      = ref(false)
const editingProject = ref(null)

const deleteTarget = ref(null)
const deleting     = ref(false)

onMounted(async () => {
  try {
    const res = await api.get('/projects')
    projects.value = res.data.data ?? []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

async function loadModalData() {
  if (genres.value.length && platforms.value.length) return
  try {
    const [genresRes, platformsRes] = await Promise.all([
      api.get('/games/genres'),
      api.get('/platforms/names'),
    ])
    genres.value   = genresRes.data ?? []
    platforms.value = platformsRes.data.data ?? []
  } catch (e) {
    console.error(e)
  }
}

async function openCreate() {
  await loadModalData()
  editingProject.value = null
  showModal.value = true
}

async function openEdit(project) {
  await loadModalData()
  editingProject.value = project
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingProject.value = null
}

async function onSaved() {
  closeModal()
  try {
    const res = await api.get('/projects')
    projects.value = res.data.data ?? []
  } catch (e) {
    console.error(e)
  }
}

function confirmDelete(project) {
  deleteTarget.value = project
}

async function doDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await api.delete(`/projects/${deleteTarget.value.id}`)
    deleteTarget.value = null
    const res = await api.get('/projects')
    projects.value = res.data.data ?? []
  } catch (e) {
    console.error(e)
  } finally {
    deleting.value = false
  }
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric'
  })
}

function riskClass(risk) {
  if (risk >= 60) return 'risk-high'
  if (risk >= 35) return 'risk-mid'
  return 'risk-low'
}
</script>

<style scoped>
.projects-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.page-title {
  font-size: 1.75rem;
  margin: 0 0 0.35rem;
}

.page-subtitle {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
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
  white-space: nowrap;
  transition: background-color 0.15s;
  flex-shrink: 0;
}

.btn-primary:hover { background-color: var(--color-accent-hover); }

.skeleton-grid,
.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.skeleton-card {
  height: 200px;
  border-radius: 12px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  text-align: center;
  padding: 4rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: var(--color-text-muted);
}

.empty-icon { font-size: 3rem; }

.project-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: border-color 0.15s;
}

.project-card:hover { border-color: var(--color-accent); }

.project-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

.project-name {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  color: var(--color-text);
  line-height: 1.3;
}

.project-actions {
  display: flex;
  gap: 0.25rem;
  flex-shrink: 0;
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

.project-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.project-meta {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  padding: 0.3rem 0;
  border-bottom: 1px solid var(--color-border);
}

.meta-item:last-child { border-bottom: none; }
.meta-label { color: var(--color-text-muted); }
.meta-value { font-weight: 500; color: var(--color-text); }

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
  color: var(--color-text);
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
</style>